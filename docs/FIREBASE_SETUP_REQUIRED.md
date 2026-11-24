# DUMA Fleet — Firebase connection checklist

This repository is ready for the Firebase setup stage, but it is **not connected to a Firebase project yet**. A Firebase project must be created while signed into the owner's Google account. GitHub cannot create or authorise that Google project on the owner's behalf.

## What Firebase will provide
- Firebase Authentication for driver/dispatcher sign-in.
- Cloud Firestore for trip assignment and live-location records.
- Firestore Security Rules to restrict location access by company and role.
- A later Android foreground-location service, started only for an assigned active trip, with a persistent Android notification and a clear Stop Tracking control.

## Create the project (one-time owner action)
1. Open https://console.firebase.google.com/ while signed into the Google account that should own DUMA Fleet.
2. Choose **Add project** and name it **DUMA Fleet** (project ID can be suggested by Google).
3. Google Analytics is optional; it is not needed for live GPS.
4. In Project Overview, add an **Android app**.
5. Android package name must be exactly `com.duma.fleet`.
6. Download the generated `google-services.json` and place it at `app/google-services.json` in this GitHub repository. Do not paste its contents into chat.
7. In Firebase Console, enable **Authentication** and choose a sign-in method. Phone authentication may require provider setup and can have usage limits.
8. Create a **Cloud Firestore** database. Start in production mode.
9. Publish the rules from `firestore.rules` in this repository (Firestore → Rules).

## Security model
The rules expect trusted custom claims on authenticated users:
- `companyId`: the user's company identifier
- `role`: `owner`, `manager`, `dispatcher`, `admin`, or `driver`

A trusted server/admin process must issue these claims after verification. Never let the Android app assign its own role or company membership. Trip assignment records must be created by a trusted backend/admin process. The supplied rules deny all other reads/writes by default; drivers can write only their own location for their assigned IN_PROGRESS trip, and dispatch roles can read locations for their own company.

## Important
- Do not commit service-account private keys, Admin SDK credentials, passwords, or private signing keys.
- The Android `google-services.json` is app configuration, not a substitute for authentication or secure rules. Restrict the Android app in Firebase settings and monitor usage.
- Location is personal information. Show a clear in-app notice, request Android location permission, track only during an active trip, show a persistent tracking notification, and stop at trip completion or when the driver stops the trip.
- Do not enable production tracking until account creation, verified company membership, trusted trip assignment, and rules have been tested with separate driver and dispatcher accounts.
- The current APK's GPS capture is a one-time local capture. It is not continuous tracking and does not transmit coordinates to dispatch.
