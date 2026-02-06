import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000; // Start with 1 second
        this.subscriptions = new Map(); // Stores callbacks: destination -> callback
        this.stompSubscriptions = new Map(); // Stores actual STOMP subscription objects: destination -> subscription
    }

    connect(onConnect) {
        if (this.connected) {
            console.log('WebSocket already connected');
            if (onConnect) onConnect();
            return;
        }

        const serverUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080';

        this.client = new Client({
            webSocketFactory: () => new SockJS(`${serverUrl}/ws`),
            debug: (str) => {
                // console.log('STOMP Debug:', str);
            },
            reconnectDelay: this.reconnectDelay,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log('WebSocket connected successfully');
                this.connected = true;
                this.reconnectAttempts = 0;
                this.reconnectDelay = 1000; // Reset delay

                // Resubscribe to all topics after reconnection
                this.resubscribeAll();

                if (onConnect) onConnect();
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
                this.connected = false;
                this.stompSubscriptions.clear();
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
                this.handleReconnect(onConnect);
            },
            onWebSocketError: (event) => {
                console.error('WebSocket error:', event);
                this.handleReconnect(onConnect);
            },
        });

        this.client.activate();
    }

    handleReconnect(onConnect) {
        // Clear stomp subscriptions on disconnect/error as they are invalid now
        this.stompSubscriptions.clear();

        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            this.reconnectDelay = Math.min(this.reconnectDelay * 2, 30000); // Max 30 seconds
            console.log(`Reconnecting in ${this.reconnectDelay}ms (attempt ${this.reconnectAttempts})`);

            setTimeout(() => {
                this.connect(onConnect);
            }, this.reconnectDelay);
        } else {
            console.error('Max reconnection attempts reached');
        }
    }

    subscribe(destination, callback) {
        if (!this.client) {
            console.warn('WebSocket client not initialized');
            return null;
        }

        // Store subscription info for resubscription after reconnect
        this.subscriptions.set(destination, callback);

        // If we already have an active subscription for this destination, unsubscribe first
        // This prevents duplicate subscriptions for the same topic
        if (this.stompSubscriptions.has(destination)) {
            console.log(`Unsubscribing from existing subscription to ${destination}`);
            try {
                this.stompSubscriptions.get(destination).unsubscribe();
            } catch (e) {
                console.warn('Error unsubscribing:', e);
            }
            this.stompSubscriptions.delete(destination);
        }

        if (this.connected) {
            console.log(`Subscribing to ${destination}`);
            const subscription = this.client.subscribe(destination, (message) => {
                try {
                    const event = JSON.parse(message.body);
                    callback(event);
                } catch (error) {
                    console.error('Error parsing WebSocket message:', error);
                }
            });

            this.stompSubscriptions.set(destination, subscription);
            return subscription;
        } else {
            console.warn(`Not connected yet, subscription to ${destination} will be made upon connection`);
            return null;
        }
    }

    resubscribeAll() {
        console.log('Resubscribing to all topics');
        this.stompSubscriptions.clear(); // Clear old subscription objects

        this.subscriptions.forEach((callback, destination) => {
            if (this.client && this.client.connected) {
                const subscription = this.client.subscribe(destination, (message) => {
                    try {
                        const event = JSON.parse(message.body);
                        callback(event);
                    } catch (error) {
                        console.error('Error parsing WebSocket message:', error);
                    }
                });
                this.stompSubscriptions.set(destination, subscription);
            }
        });
    }

    unsubscribe(destination) {
        // Remove from persistent subscriptions map
        this.subscriptions.delete(destination);

        // Unsubscribe from actual STOMP client
        if (this.stompSubscriptions.has(destination)) {
            console.log(`Unsubscribing from ${destination}`);
            try {
                this.stompSubscriptions.get(destination).unsubscribe();
                this.stompSubscriptions.delete(destination);
            } catch (e) {
                console.warn(`Error unsubscribing from ${destination}:`, e);
            }
        }
    }

    disconnect() {
        if (this.client) {
            this.subscriptions.clear();
            this.stompSubscriptions.clear();
            this.client.deactivate();
            this.connected = false;
            console.log('WebSocket disconnected');
        }
    }

    isConnected() {
        return this.connected;
    }
}

const websocketService = new WebSocketService();
export default websocketService;
