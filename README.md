# Login

Build app-less, passwordless login experiences with BindID for customers that want to access your Android Java application. This sample app uses the BindID SDK to initiate strong authentication flows with the BindID service to sign in your users.

## Prerequisites

Before you begin, you'll need to have an application configured in the [BindID Admin Portal](https://admin.bindid-sandbox.io/console/#/applications). From the application settings, obtain the client credentials and configure a redirect URI for this client that will receive the authentication result (such as `bindidexample://login`). 
For more, see [BindID Admin Portal: Get Started](https://developer.bindid.io/docs/guides/admin_portal/topics/getStarted/get_started_admin_portal).

## Instructions

To run the sample on your Android device:

1 - Configure your client credentials in the strings.xml file:

```bash
'bindid_client_id' # Client ID obtained from the BindID Admin Portal
'bindid_redirect_uri' # Redirect URI you defined in the BindID Admin Portal.
```

2 - Set the redirect URI in the app manifest. The URI is composed of [YOUR_SCHEME]//[YOUR_HOST] (such as `bindidexample://login`) and should match the URI you configured in the BindID Admin Portal:

```xml
...
        <activity android:name="com.ts.bindid.BindIdActivity">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:host="[YOUR_HOST]"
                    android:scheme="[YOUR_SCHEME]" />
            </intent-filter>
        </activity>
...
```

3 - Build and run the application in Android Studio on your Android device target.

## Note
For Android Play Services must be registered on the running device; otherwise a Fido2 RuntimeException will be thrown.

## What is BindID?
The BindID service is an app-less, strong portable authenticator offered by Transmit Security. BindID uses FIDO-based biometrics for secure, frictionless, and consistent customer authentication. With one click to create new accounts or sign into existing ones, BindID eliminates passwords and the inconveniences of traditional credential-based logins.
[Learn more about how you can boost your experiences with BindID.](https://www.transmitsecurity.com/developer)

## Author
Transmit Security, https://github.com/TransmitSecurity

## License
This project is licensed under the MIT license. See the LICENSE file for more info.
