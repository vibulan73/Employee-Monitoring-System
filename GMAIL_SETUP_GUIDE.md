# How to Generate Gmail API Credentials

## 1. Generate `credentials.json` (Google Cloud Console)

This file authenticates your **application** with Google.

1.  **Go to Google Cloud Console**: [https://console.cloud.google.com/](https://console.cloud.google.com/)
2.  **Create a New Project** (or select an existing one):
    *   Click the project dropdown at the top left.
    *   Click **New Project**.
    *   Name it (e.g., "Employee Monitoring System") and click **Create**.
3.  **Enable Gmail API**:
    *   In the side menu, go to **APIs & Services** > **Library**.
    *   Search for "Gmail API".
    *   Click on it and click **Enable**.
4.  **Configure OAuth Consent Screen**:
    *   Go to **APIs & Services** > **OAuth consent screen**.
    *   Select **External** (unless you have a Google Workspace organization) and click **Create**.
    *   **App Information**: Fill in App Name and User Support Email.
    *   **Developer Contact Information**: Enter your email.
    *   Click **Save and Continue**.
    *   **Scopes**: Click **Add or Remove Scopes**. Search for `.../auth/gmail.send` (or just "gmail"). Select the one for "Send email on your behalf". Click **Update**.
    *   **Test Users**: Add your own email address (the one you will log in with). This is **critical** while the app is in "Testing" mode.
    *   Click **Save and Continue** until finished.
5.  **Create Credentials**:
    *   Go to **APIs & Services** > **Credentials**.
    *   Click **+ CREATE CREDENTIALS** > **OAuth client ID**.
    *   **Application type**: Select **Web application**.
    *   **Name**: e.g., "Spring Boot Backend".
    *   **Authorized redirect URIs**:
        *   Click **ADD URI**.
        *   Enter: `http://localhost:8888/Callback`
        *   (Note: Port 8888 is strictly defined in your `GmailConfig.java` file).
    *   Click **Create**.
6.  **Download JSON**:
    *   A popup will show your Client ID and Secret.
    *   Click the **Download JSON** button (looks like a down arrow/download icon provided on the right side of the created credential in the list).
    *   **Rename** the file to `credentials.json`.
    *   **Move** it to your project folder: `backend/src/main/resources/credentials.json`.

---

## 2. Generate `StoredCredential` (User Token)

This file authenticates **you** (the sender). It is generated automatically when you run the app.

1.  **Prepare the Environment**:
    *   Ensure `backend/src/main/resources/credentials.json` exists and is valid.
    *   Ensure the `tokens` folder in `backend/` is empty or deleted if you want to regenerate.
2.  **Run the Backend**:
    *   Open your terminal in the `backend` folder.
    *   Run: `mvn spring-boot:run`
3.  **Authenticate**:
    *   Watch the console logs.
    *   A browser window should open automatically to the Google Login page.
    *   If it doesn't, copy the URL printed in the console (it will look like `https://accounts.google.com/o/oauth2/auth?...`) and paste it into your browser.
    *   **Login**: Select the email account you added as a "Test User".
    *   **Safety Warning**: You might see "Google hasn't verified this app". Click **Advanced** > **Go to Employee Monitoring System (unsafe)**. This is normal for your own dev app.
    *   **Permissions**: Click **Continue** or **Allow** to grant permission to send emails.
    *   **Success**: You should see a message saying "Received verification code. You may now close this window."
4.  **Verify**:
    *   Check your project folder. You typically will see a new directory called `tokens` created in the root of `backend`.
    *   Inside, there will be a file named `StoredCredential`.

---

## 3. (Optional) Base64 Encode for Deployment

If deploying to Render or another cloud provider where a browser login isn't possible:

1.  Generate the `StoredCredential` locally using the steps above.
2.  Convert the file to a Base64 string.
    *   **PowerShell**:
        ```powershell
        [Convert]::ToBase64String([IO.File]::ReadAllBytes("tokens/StoredCredential"))
        ```
    *   **Mac/Linux**:
        ```bash
        base64 -i tokens/StoredCredential
        ```
3.  Copy this long string.
4.  Set it as an Environment Variable named `GMAIL_TOKEN_BASE64` in your cloud dashboard.
