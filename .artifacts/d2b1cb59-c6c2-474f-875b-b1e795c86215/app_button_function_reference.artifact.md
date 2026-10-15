# Fendly Lost & Found — Complete Screen, Button, & Function Reference

This document provides a granular, exhaustive breakdown of every screen, card, button, input field, and interactive function in the Fendly Lost & Found Android application. It is designed to be passed directly to another AI agent or designer to generate exact UI mockups, themes, and screens.

---

## 1. Splash Screen (`SplashActivity.java`)
- **UI Elements**:
  - **App Logo (`FendlyLogo`)**: Centered at the top/middle.
  - **App Title & Branding**: Displays "Fendly Lost & Found" with loading/initialization animation.
- **Functions & User Interactions**:
  - **Automatic Check**: On launch, verifies user session and token validity.
  - **Transition**: Automatically routes the user to either the **Authentication Screen** (if logged out) or the **Home Dashboard** (if authenticated).

---

## 2. Authentication Screen (`AuthScreen.kt` / `PAGE_AUTH` in `MainActivity.java`)
- **UI Elements**:
  - **Auth Card Container**: A centered card with deep elevation holding authentication controls.
  - **Username Field (`username`)**: Text box for entering the user's handle or phone number/email.
  - **PIN Code Entry Cells (`pinCells`)**: 4 to 6 secure digit entry boxes for PIN verification.
  - **Forgot PIN Action**: Text button to trigger PIN recovery or reset flow.
  - **Biometric Login Button (`fingerprint`)**: Icon button displaying a fingerprint/face icon.
  - **Login Submit Button (`login`)**: Primary filled button to authenticate with credentials.
  - **Create Account Button**: Secondary button at the bottom of the auth card.
- **Functions & User Interactions**:
  - **Typing Username/PIN**: User inputs their credentials.
  - **Tapping Biometric Button (`fingerprint`)**: Triggers Android Biometric Prompt (Fingerprint/Face unlock) for instant biometric sign-in.
  - **Tapping Login Button**: Validates credentials against backend API (`/api/auth/login`) and navigates to `PAGE_HOME` on success.
  - **Tapping Create Account**: Switches view to account registration / profile setup (`PAGE_PROFILE_SETUP`).

---

## 3. Home Dashboard Screen (`PAGE_HOME` in `MainActivity.java`)
- **UI Elements**:
  - **Header Bar**:
    - **User Avatar (`visibleAvatar`)**: Displays user profile picture.
    - **Location Dropdowns (`visibleStateSearch`, `visibleCitySearch`)**: AutoCompleteTextViews to select user's current State and City.
    - **Notification Badge**: Indicator showing unread notifications/alerts count.
  - **Quick Action Cards**:
    - **Lost / Theft Action Button/Card**: Prominent action card for reporting a missing item/person.
    - **Found Action Button/Card**: Prominent action card for reporting a found item/person.
    - **Search Input Bar (`visibleStateSearch` / search view)**: Autocomplete search bar for finding reports.
  - **Welcome & Guide Card**:
    - **Audio Play/Pause Button (`pendingGuidePlayButton`)**: Tapping plays or pauses text-to-speech audio guidance (`speakOrStop`) explaining how to use the app.
  - **Bottom Navigation Bar**: 4-tab bar (Home, Report, My Reports, Profile).
- **Functions & User Interactions**:
  - **Tapping Lost/Theft Card**: Switches `currentPage` to `PAGE_REPORT` pre-set with "Lost" mode.
  - **Tapping Found Card**: Switches `currentPage` to `PAGE_REPORT` pre-set with "Found" mode.
  - **Typing in Search Bar**: Queries reports dynamically as the user types (requires at least 3 characters).
  - **Tapping Audio Guide Button**: Plays spoken instructions in the selected language.
  - **Tapping Bottom Tabs**: Switches between Home, Report Wizard, My Reports, and Profile screens.

---

## 4. Report Creation Wizard Screen (`PAGE_REPORT` in `MainActivity.java`)
- **UI Elements**:
  - **Step 1 Card (Category & Type)**:
    - **Category Buttons**: Toggle between **Items / Valuables**, **Pets / Animals**, and **Missing Persons**.
    - **Report Type Toggle**: Toggle between **Lost / Theft** and **Found**.
  - **Step 2 Card (Details & Media)**:
    - **Title Field (`item`)**: EditText for item title (e.g., "Blue backpack").
    - **Description Field**: EditText for identifying details.
    - **Date Picker Button (`date`)**: Opens Android DatePickerDialog.
    - **Location Fields**: City and State selection.
    - **Photo Attachment Button**: Opens `PhotoCropActivity` or `PortraitCaptureActivity` to capture/crop item/pet/person photo.
  - **Step 3 Card (Review & Submission)**:
    - **Summary Rows (`addWizardSummaryRow`)**: Styled cards displaying a checklist of all entered report details.
    - **Subscription / Premium Upgrade Card (`subscriptionCard`)**: Option to unlock AI matching priority or promotional boost.
    - **Submit / Payment Button**: Triggers report submission API or payment gateway.
- **Functions & User Interactions**:
  - **Selecting Categories/Types**: Determines report schema and backend endpoints.
  - **Picking Date**: Populates date field with formatted date.
  - **Capturing/Cropping Photo**: Launches camera or gallery, crops image, and attaches it to the report draft (`draftItem`).
  - **Tapping Submit**: Validates all fields (`validateWizardReport`), sends data to backend (`submitItem`), and navigates to My Reports.

---

## 5. My Reports / Browse Reports Screen (`PAGE_REPORTS` in `MainActivity.java`)
- **UI Elements**:
  - **Category Tabs**: Filter reports by Items, Animals, or People.
  - **Report List / Cards**: List of cards showing report thumbnails, titles, status badges (Lost Green / Found Gold), and dates.
  - **Report Details Dialog**: Appears when tapping a report card. Contains:
    - **Update Status Button**: Changes report status (e.g., Resolved, Active).
    - **Delete Report Button**: Triggers confirmation dialog (`confirmDeleteReport`) before deletion.
    - **Share Poster Button**: Generates and shares a printable community poster (`[CommunityPoster.java]`, `[SharePosterUtil.kt]`).
- **Functions & User Interactions**:
  - **Tapping a Report Card**: Opens the Report Details Dialog to inspect full report info.
  - **Tapping Delete**: Prompts confirmation and removes the report from the database.
  - **Tapping Share Poster**: Formats a shareable image card to post on social media or messaging apps.

---

## 6. User Profile & Settings Screen (`ProfileScreen.kt` & `PAGE_PROFILE` in `MainActivity.java`)
- **UI Elements**:
  - **Profile Header**: Avatar, Username, Email & Phone verification badges (`visibleEmailVerify`, `visibleMobileVerify`).
  - **Input Fields**: First Name, Surname, State Search, City Search.
  - **Settings & Preferences Cards**:
    - **Language Selector Dropdown**: Supports 8+ Indian languages (Hindi, Bengali, Gujarati, Kannada, Malayalam, Tamil, Telugu) + English.
    - **Theme Toggle Switch**: Switches between Dark Mode and Light Mode.
    - **Subscription Card**: Links to subscription upgrade screen (`PAGE_SUBSCRIPTION`).
    - **Admin Dashboard Button**: Visible for admins (`PAGE_ADMIN`).
    - **Logout Button**: Opens logout confirmation dialog (`showLogoutConfirmationDialog`).
    - **Delete Account Button**: Opens account deletion confirmation dialog (`showDeleteAccountDialog`).
- **Functions & User Interactions**:
  - **Editing Profile Fields**: Updates user profile info in real-time.
  - **Changing Language**: Instantly re-renders all UI strings in the selected Indian language (`LanguageManager`).
  - **Toggling Theme**: Instantly switches app color palette between Dark and Light mode.
  - **Tapping Logout**: Clears session tokens and returns to `PAGE_AUTH`.
  - **Tapping Delete Account**: Permanently deletes the user account after confirmation.

---

## 7. Vault Screen (`VaultActivity.kt`)
- **UI Elements**:
  - **Vault Item Cards**: List of secure document / ID records.
  - **Add Vault Item Button**: Floating action button or top bar button to add new items.
- **Functions & User Interactions**:
  - **Adding/Viewing Documents**: Users store important personal documents or asset receipts securely.

---

## 8. Adoption Screen (`AdoptionActivity.kt`)
- **UI Elements**:
  - **Pet Listing Cards**: Cards showing animal photos, breed, age, and rescue details.
  - **Contact / Adopt Button**: Action button on each card.
- **Functions & User Interactions**:
  - **Browsing Adoptable Pets**: Users view rescued/found pets available for adoption and contact rescuers.

---

## 9. Admin Dashboard (`PAGE_ADMIN` & `PAGE_ADMIN_SUBSCRIPTIONS` in `MainActivity.java`)
- **UI Elements**:
  - **Pending Reports Queue**: Cards for unmoderated user reports with Approve / Reject buttons.
  - **User & Subscription Tables**: Manage platform users and subscription payments.
- **Functions & User Interactions**:
  - **Approving/Rejecting Reports**: Moderates community submissions.
  - **Managing Subscriptions**: Tracks revenue and active premium subscriptions.

---

## 10. Home Screen Widget (`FendlyWidgetProvider.java` / `fendly_widget.xml`)
- **UI Elements**:
  - **Widget Header**: Fendly logo and lost/found counter.
  - **Quick Action Rows**: Direct shortcuts to report lost or found items directly from the Android home screen.
- **Functions & User Interactions**:
  - **Tapping Widget Buttons**: Launches the app directly into the Lost or Found reporting wizard.
