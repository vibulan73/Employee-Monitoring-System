# Web UI - Employee Activity Monitoring System

React-based web interface for viewing and analyzing work sessions.

## Features

- Dashboard with session overview
- Session filtering by user and status
- Detailed session analytics
- Activity timeline visualization
- Screenshot gallery with lightbox viewer
- Responsive design
- Modern UI with animations

## Running the Web UI

### Development
```bash
npm install
npm start
```

Access at `http://localhost:3000`

### Production Build
```bash
npm run build
```

Serve the `build/` directory with your web server.

## Configuration

Edit `src/services/api.js` to change the backend URL:

```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Features

### Dashboard
- View all work sessions
- Filter by user ID
- Filter by status (Active/Stopped)
- Click session to view details

### Session Details
- Session metadata (ID, user, duration)
- Activity statistics (active vs idle percentage)
- Activity timeline with timestamps
- Screenshot gallery
- Interactive screenshot viewer

## Technologies

- React 18
- React Router DOM
- Axios (HTTP client)
- CSS3 (gradients, animations, glassmorphism)

## Available Scripts

- `npm start` - Start development server
- `npm run build` - Build for production
- `npm test` - Run tests
- `npm run eject` - Eject from create-react-app
