# DUMA Fleet Management — Product and Security Build Plan

Owner: Duma Holdings (Pty) Ltd
Platform: Android first; South Africa launch
Product rule: simple, fast, offline-first. Keep the existing DUMA logo, colours and branding.

## Main dashboard
Show exactly four large service cards:
1. MONEY — earned income, Freight/rental income, diesel, maintenance, rescue costs, profit, transaction history and reports.
2. DUMA FREIGHT — Available Loads, My Loads, My Earnings, Post a Load, Find a Truck. Freight is the primary marketplace and uses the green accent.
3. FLEET OVERVIEW — Vehicles, Drivers, Trips, Fuel, Maintenance, Inspections, Documents and Expiry Dates.
4. DUMA RESCUE — I AM BROKEN DOWN, request help, location, request status and rescue history. Use red for emergency actions.
Each card opens its own full detail page with a Back to Dashboard action.
Bottom navigation stays Dashboard, Vehicles, Drivers, Freight, Money. Freight remains the only green/bold tab.
Keep the offline banner and Add Vehicle action. No duplicate Freight banners, unnecessary popups or extra menus.

## Core operational features
- Vehicles: registration details, owner/contact, photos, documents, maintenance, fuel, inspections, trip history and Freight availability.
- Drivers: profile, contact, identity-verification state, licence code/expiry, PrDP category/expiry where applicable, assigned vehicles and trip history.
- Trips: driver/vehicle, route, load, start/end odometer, delivery evidence, timestamps and trip status.
- Money: income and expense records, per-vehicle and per-period summaries, profit calculation, receipts and PDF reports.
- Freight marketplace: two-sided access for load owners and truck owners; load posting, truck listings, search/filter, quote/accept flow, booking status, delivery confirmation, disputes, platform commission and earnings ledger.
- Rescue: breakdown request, location, issue/media, dispatch queue, service-provider acceptance, ETA/status, completion and rating.
- Notifications: new load/booking, status changes, approaching document expiry and rescue updates.
- Offline-first: local queue and conflict-aware sync when connectivity returns; clearly show pending/synced/failed state.
- Accounts and roles: company owner, manager, dispatcher, driver, load owner and rescue provider. Apply least-privilege access.

## Driver and marketplace trust
Verification statuses: NOT_SUBMITTED, PENDING, VERIFIED, REJECTED, EXPIRED, REVIEW_REQUIRED.
- Phone: OTP through an authentication provider. A phone number alone does not prove identity.
- Identity: use a contracted, reputable identity-verification provider for document authenticity, identity match and selfie/liveness. Store provider transaction/reference and result, not raw ID data in local app storage.
- Licence: validate the licence, category/code, validity and expiry using a lawful verification route/provider or documented human review.
- PrDP: determine requirement by vehicle/work category; verify category and expiry where required. Government guidance says PrDP applies to specified work, including goods vehicles over 3,500 kg and breakdown vehicles.
- Human review: failed, inconclusive or mismatched checks go to authorised DUMA staff. Record reviewer, timestamp, decision and reason.
- Marketplace gate: a driver cannot accept a load or represent themselves as verified until every required check is VERIFIED and current. Expiry automatically blocks new Freight acceptances until rechecked.
- Vehicle checks: verify registration/ownership or authority to use the vehicle, roadworthiness/licence disc and insurance where required by the service/contract.
- Business checks: verify load-owner/trucking-business details and authorised contact before enabling high-value transactions.
- Safety: in-app report/block flow, suspicious-account review, trip/job audit trail, delivery PIN or recipient signature, and dispute process.
- Never promise that an ID upload, selfie, OTP or DUMA admin glance alone guarantees a person's safety.

## POPIA and information security
- Publish clear privacy notice and purpose-specific consent before collecting identity, selfie, licence, location or other personal information.
- Collect only information necessary for a stated purpose. Avoid entering full ID numbers in ordinary notes or local plaintext preferences.
- Use a secure backend with encryption in transit and at rest, role-based access, audit logs, backups, retention/deletion controls and breach-response procedure.
- Keep identity documents and biometric templates with the specialist verification provider where possible; DUMA should retain only minimal status, provider reference and necessary audit evidence.
- Complete a privacy impact/risk assessment and confirm Information Officer, operator contracts, cross-border transfers and any prior-authorisation requirements with a South African POPIA professional before launch.
- Never display full ID/licence numbers to other marketplace users.

## Delivery phases
1. Finish the offline app screens, navigation, data forms, validations and local summaries.
2. Add authentication, roles, secure backend, cloud sync and secure document storage.
3. Integrate identity/OTP/licence verification provider and implement admin review workflow.
4. Launch Freight marketplace matching, booking, messaging/notifications, payment/escrow partner and dispute handling.
5. Integrate Rescue provider dispatch, GPS sharing and live status.
6. Run security, privacy, offline-sync, device and real-world operational testing before public release.

## Current implementation boundary
The current Android app stores records locally using SharedPreferences. The driver registration screen can capture verification references and mark new registrations PENDING / Freight BLOCKED. This is an onboarding placeholder, not a real identity, licence or PrDP check. Live OTP, liveness, provider checks, admin approval controls, cloud sync, marketplace matching, payments, GPS dispatch and notifications require backend/provider integration and must not be represented as active until tested.
