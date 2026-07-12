# Firebase — Commented-Out Code

Firebase is not yet configured, so the related code in the following files has been commented out.
After Firebase setup is complete, remove all comment markers tagged with `// TODO: Firebase kurulduktan sonra`.

---

## 1. `pubspec.yaml`

Commented-out packages:

```yaml
firebase_core: ^latest_version
firebase_remote_config: ^latest_version
```

---

## 2. `lib/product/service/service_locator.dart`

- `RemoteConfigService` import
- `RemoteConfigService` singleton registration
- `RemoteConfigService.init()` call
- `remoteConfigService` getter in `ServiceLocator` extension

---

## 3. `lib/feature/login_process/splash/state/splash_cubit.dart`

- `package_info_plus` and `RemoteConfigService` imports
- `RemoteConfigService` dependency injection in constructor
- Version check logic inside `checkApp()` (reads min version from Firebase Remote Config)
- `_isVersionLessThan()` method
- Currently `checkApp()` directly emits `SplashState.success()`

---

## 4. `lib/product/init/application_init.dart`

- `Firebase.initializeApp()` call

---

## 5. `lib/product/navigation/app_router.dart`

- `locator.remoteConfigService` should be passed when creating `SplashCubit`
- `service_locator.dart` import needed

---

## Firebase Setup Steps (to be done by user)

1. Create project in Firebase Console
2. Run `flutterfire configure`
3. Add `GoogleService-Info.plist` (iOS) and `google-services.json` (Android)
4. Uncomment Firebase packages in `pubspec.yaml`
5. Run `flutter pub get`
6. Uncomment all TODO-marked lines in the files listed above
7. Activate `Firebase.initializeApp()` in `application_init.dart`
8. Pass `locator.remoteConfigService` to `SplashCubit` in `app_router.dart`

## App Flow When Firebase Is Active

```
  App opens
         │
         ▼
    /splash (initialLocation)
    SplashCubit.checkApp() runs automatically
         │
         ├─ Gets min version from Firebase Remote Config
         │
         ├─ Version outdated? ── YES ──→ _UpdateRequiredView (redirect to Store)
         │
         ├─ Error? ──────────── YES ──→ _ErrorView (retry button)
         │
         └─ Success ───────────────────→ SharedCache check
                                                │
                                                ├─ isOnboardingCompleted == true
                                                │     └─→ HomeRoute().go()  →  /
                                                │
                                                └─ isOnboardingCompleted == false
                                                      └─→ OnboardingRoute().go()  →  /onboarding
                                                                │
                                                           5-step PageView
                                                                │
                                                           Step 5 "Let's Start"
                                                                │
                                                      completeOnboarding()
                                                      ├─ Write true to SharedCache
                                                      └─ context.go('/')  →  Home
```
