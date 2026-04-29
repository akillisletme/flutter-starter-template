# Flutter Starter Template — Project Guide

This is the first file to read to understand the project.
It explains what the project is, how it works, and where to find other documentation.

---

## First Clone?

If you just cloned this project from GitHub, follow this guide first:

→ **[setup_after_clone.md](new_feature/setup_after_clone.md)** — Clean generated files, install dependencies, code-gen, Firebase question, and error analysis steps.

Come back to this file after setup is complete.

---

## What Is This Project?

A ready-to-use Flutter boilerplate/template for quickly starting new projects.
Includes theming, caching, navigation, localization, state management, service infrastructure, onboarding, and settings page out of the box.

- **Package name:** `akillisletme` (should be changed for new projects)
- **State Management:** Cubit + Freezed
- **DI:** GetIt (singleton pattern)
- **Routing:** GoRouter (type-safe, code-gen)
- **Cache:** SharedCache (SharedPreferences) + ProductCache (Hive CE)
- **Localization:** EasyLocalization (TR + EN)
- **Assets:** FlutterGen (type-safe)
- **Lint:** `very_good_analysis`

---

## Project Structure

```
doc/
├── project.md                             # ← THIS FILE (project guide)
└── new_feature/                           # All guide documents
    ├── README.md                          # New feature checklist + navigation
    ├── setup_after_clone.md               # Post-clone setup (Claude Code instructions)
    ├── firebase_commented_out.md          # Firebase activation guide
    ├── folder_structure.md                # Feature folder structure
    ├── model_rules.md                     # Model creation rules
    ├── state_management.md                # Cubit + Freezed rules
    ├── view_rules.md                      # View structure rules
    ├── service_rules.md                   # Service placement rules
    ├── service_initialization.md          # Locator + init flow
    ├── data_storage.md                    # Cache/storage decision tree
    ├── route_and_strings.md               # Route + string addition
    ├── widget_and_theme.md                # Widget + theme rules
    ├── enums_and_constants.md             # Enum + constant rules
    ├── assets_and_flutter_gen.md          # FlutterGen asset addition
    └── settings_and_urls.md               # Store URLs + contact info

lib/
├── main.dart                              # App entry point
├── feature/                               # Screens / features
│   ├── home/
│   │   ├── home_view.dart                 # Home page (StatefulWidget + ViewModel)
│   │   ├── home_view_mode.dart            # ViewModel (abstract State)
│   │   ├── android_modules/
│   │   │   └── android_modules_view.dart  # Android native modules demo + izin UI
│   │   └── widget/
│   │       └── home_background.dart       # Global animated background
│   ├── login_process/
│   │   ├── onboarding/                    # 5-step onboarding
│   │   └── splash/                        # Splash screen + SplashCubit
│   └── settings/
│       ├── settings_view.dart             # Settings page
│       └── widget/                        # Theme, language tiles
└── product/                               # Shared infrastructure layer
    ├── cache/                             # SharedCache + Hive (ProductCache)
    ├── const/                             # AppString, AppPaddings, RegexTypes
    ├── init/                              # App init, localization, AppBuilder
    ├── model/                             # Shared immutable data models
    │   └── android_module_info.dart       # AndroidModuleInfo (icon, title, features)
    ├── navigation/                        # GoRouter config + transitions
    ├── service/                           # Services + GetIt DI
    ├── state/                             # App-wide cubits (ThemeCubit)
    ├── theme/                             # Material 3 theme (5 variants, dark/light)
    ├── generated/                         # FlutterGen output
    ├── utils/                             # AppMessenger, responsive, haptics
    └── widget/                            # Shared buttons

android/app/src/main/
├── kotlin/com/cleanstart/akillisletme/
│   ├── MainActivity.kt                    # Flutter bridge (MethodChannels + lifecycle)
│   ├── home_widget/
│   │   └── HomeWidget.kt                  # HomeWidgetProvider + HomeWidgetReceiver
│   └── overlay/
│       └── OverlayService.kt              # Floating overlay foreground service
└── res/
    ├── layout/
    │   ├── widget_home.xml                # Home screen widget layout
    │   └── overlay_window.xml             # Floating overlay layout
    ├── drawable/                          # Widget/overlay shape drawables
    └── xml/
        └── app_widget_info.xml            # Widget provider metadata
```

---

## App Flow

```
First launch:  Splash → Onboarding (5 steps) → Home
Subsequent:    Splash → Home (direct)
```

- Onboarding completion is saved to `SharedCache.isOnboardingCompleted`
- Router `initialLocation` decides based on this flag
- Settings page is accessible from Home AppBar
- Global background animation is visible on all pages via `AppBuilder`

---

## Built-in Infrastructure

### 1. Theme System

5 color variants (Purple, Blue, Green, Orange, Red) with Material 3. Auto dark/light.

- **Location:** `lib/product/theme/`
- **State:** `ThemeCubit` — saves variant selection to SharedCache
- **Usage:** `context.watch<ThemeCubit>().state`
- **Selection dialog:** `ThemeSelectionDialog.show(context)`
- **Details:** `lib/product/theme/THEME.md`

### 2. Cache System

| Data Type | System | Access |
|-----------|--------|--------|
| bool, int, String | SharedCache | `locator.sharedCache` |
| Model lists | ProductCache (Hive) | `locator.productCache` |

- **Location:** `lib/product/cache/`
- **Details:** `lib/product/cache/CACHE_GUIDE.md`

### 3. Navigation

Type-safe GoRouter with `go_router_builder` code-gen.

| Route | Path | Transition |
|-------|------|------------|
| HomeRoute | `/` | fade |
| SettingsRoute | `/settings` | slide right + fade |
| OnboardingRoute | `/onboarding` | fade |

- **Location:** `lib/product/navigation/app_router.dart`
- **Details:** `doc/new_feature/route_and_strings.md`

### 4. Localization (i18n)

`easy_localization` with TR + EN. JSON-based translation files.

- **Translation files:** `assets/translations/tr.json`, `en.json`
- **Type-safe keys:** `lib/product/init/language/locale_keys.g.dart` (generated)
- **Usage:** `LocaleKeys.home_title.tr()`
- **Change language:** `context.setLocale(Locale('en', 'US'))`

### 5. State Management

flutter_bloc + Freezed.

- **App-wide:** `lib/product/state/` (e.g. ThemeCubit)
- **Feature-level:** `lib/feature/<feature>/state/`
- **Provider registration:** `lib/product/init/state_initialize.dart`
- **Details:** `doc/new_feature/state_management.md`

### 6. Dependency Injection

GetIt singleton service management.

- **Location:** `lib/product/service/service_locator.dart`
- **Access:** `locator.sharedCache`, `locator.productCache`
- **Details:** `doc/new_feature/service_initialization.md`

### 7. Onboarding

5-step PageView. Completion saves `SharedCache.isOnboardingCompleted = true`.

- **Location:** `lib/feature/login_process/onboarding/`
- **Details:** `lib/feature/login_process/onboarding/ONBOARDING.md`

### 8. Settings Page

Theme selection + language switching. Accessible via Home AppBar settings icon.

- **Location:** `lib/feature/settings/`

### 9. Global Background Animation

Animated background visible on all pages.

- **Location:** `lib/feature/home/widget/home_background.dart`
- **Toggle:** `HomeBackground.enabledNotifier`

### 10. FlutterGen

Type-safe asset access with compile-time error checking + IDE autocomplete.

```dart
Assets.image.booom.image(width: 100)     // PNG/JPG
Assets.svg.bomb.svg(width: 24)           // SVG
Assets.lottie.backroundAnimation.lottie() // Lottie
FontFamily.poppins                        // Font
```

- **Details:** `doc/new_feature/assets_and_flutter_gen.md`

### 11. AppPaddings

Consistent spacing constants. Used instead of hardcoded `EdgeInsets`.

- **Values:** xs=4, s=8, m=12, l=16, xl=20, xxl=24, xxxl=32
- **File:** `lib/product/const/app_paddings.dart`

### 12. AppMessenger

Context extensions for SnackBar, Dialog, and BottomSheet.

```dart
context.showSuccessSnack('Saved!');
context.showErrorSnack('An error occurred');
context.showConfirmDialog(title: '...', message: '...');
context.showAppBottomSheet<void>(child: MyWidget());
```

- **File:** `lib/product/utils/app_messenger.dart`

### 13. RegexTypes

Validation patterns with Turkish character support: fullName, email, phoneNumber, password, etc.

- **File:** `lib/product/const/regex_types.dart`
- **Usage:** `RegexTypes.email.hasMatch(value)`

### 15. Home Screen Widget (Android)

Native Android home screen widget — no third-party packages. Users can control the counter directly from their home screen without opening the app.

- **Kotlin:** `android/.../home_widget/HomeWidget.kt` (`HomeWidgetProvider` + `HomeWidgetReceiver`)
- **Layout:** `res/layout/widget_home.xml` (RemoteViews — limited to standard Android views)
- **Shared data:** `SharedPreferences("widget_prefs", key: "counter")` — same file read by overlay and Flutter
- **Flutter bridge:** `MethodChannel('counter')` in `MainActivity.kt` → `get / increment / decrement / reset`
- **Demo UI:** `lib/feature/home/android_modules/android_modules_view.dart`

Widget button taps → `HomeWidgetReceiver` → updates SharedPreferences → updates widget UI → sends `ACTION_REFRESH` broadcast to overlay.

### 16. Floating Overlay (Android)

A draggable floating window drawn over all other apps using `WindowManager`. Visible only when the Flutter app is in the background.

- **Kotlin:** `android/.../overlay/OverlayService.kt` (ForegroundService)
- **Layout:** `res/layout/overlay_window.xml`
- **Permission required:** `SYSTEM_ALERT_WINDOW` — requested from `android_modules_view.dart`
- **Lifecycle:** `MainActivity.onStop()` → `startService()`, `MainActivity.onStart()` → `stopService()`
- **Shared data:** same `SharedPreferences("widget_prefs")` as home widget
- **Sync:** listens to `ACTION_REFRESH` broadcast; sends `AppWidgetManager.updateAppWidget()` on counter change

Drag threshold: 8px — smaller touches pass through to buttons.

**Permissions declared in `AndroidManifest.xml`:**
```
SYSTEM_ALERT_WINDOW, FOREGROUND_SERVICE, FOREGROUND_SERVICE_SPECIAL_USE
```

---

> ### 🤖 Claude Code — Android Modülleri Kurulum Soruları
>
> Bu repo klonlandığında ve kullanıcı geliştirmeye başlamadan önce,
> aşağıdaki soruları kullanıcıya sor. Varsayılanları belirt ve
> kullanıcının kendi projesine göre özelleştirmesine yardımcı ol.
>
> **Soru 1 — Veri tipi**
> Sayaç (Int) yerine ne saklamak istiyorsun?
> - Cevaba göre `SharedPreferences` key'ini, `MethodChannel` metodlarını ve widget/overlay layout metin alanlarını güncelle.
> - Varsayılan: `"counter"` (Int)
>
> **Soru 2 — Widget başlığı**
> Ana ekran widget'ının başlık metni ne olsun?
> - `res/layout/widget_home.xml` → `tv_widget_title` TextView'ının `android:text` alanını güncelle.
> - Varsayılan: `"Sayaç"`
>
> **Soru 3 — Overlay başlığı**
> Yüzen penceredeki başlık metni ne olsun?
> - `res/layout/overlay_window.xml` → ilk `TextView`'ın `android:text` alanını güncelle.
> - Varsayılan: `"Sayaç"`
>
> **Soru 4 — Bildirim metni**
> Yüzen pencere arka planda çalışırken gösterilen bildirim metni ne olsun?
> - `OverlayService.kt` → `buildNotification()` içindeki `setContentTitle(...)` değerini güncelle.
> - Varsayılan: `"Uygulama arka planda çalışıyor"`
>
> **Soru 5 — MethodChannel adları**
> Flutter–Kotlin arasındaki channel adlarını değiştirmek istiyor musun?
> - `MainActivity.kt`'deki `OVERLAY_CHANNEL` ve `COUNTER_CHANNEL` sabitlerini ve Flutter tarafındaki `MethodChannel(...)` çağrılarını (`android_modules_view.dart`) güncelle.
> - Varsayılan: `"counter"`, `"overlay_permission"`
>
> **Soru 6 — Overlay butonları**
> Yüzen penceredeki buton seti yeterli mi, yoksa buton eklemek/çıkarmak istiyor musun?
> - `res/layout/overlay_window.xml` layout + `OverlayService.kt` → `bindViews()` metodunu güncelle.
> - Varsayılan: `−`, `Sıfırla`, `+`, `↗ (uygulamayı aç)`, `✕ (kapat)`
>
> **Soru 7 — Demo sayfası**
> `android_modules_view.dart` sadece bir demo/izin sayfasıdır. Bu sayfayı kendi UI'ına dönüştürmek mi istiyorsun, yoksa kaldırmak mı?
> - Kaldırılırsa: `app_router.dart`'tan `AndroidModulesRoute`'u ve `home_view.dart`'tan ilgili butonu da sil.

---

### 14. Firebase (Optional)

Firebase Remote Config integration is ready but commented out. To activate:

→ **[firebase_commented_out.md](new_feature/firebase_commented_out.md)**

---

## Shared Widgets

| Widget | Location | Description |
|--------|----------|-------------|
| AppPrimaryButton | `product/widget/` | FilledButton |
| AppSecondaryButton | `product/widget/` | OutlinedButton |
| AppTextButton | `product/widget/` | TextButton |
| ThemeSettingTile | `product/theme/widget/` | Theme selection tile |
| ThemeSelectionDialog | `product/theme/widget/` | Theme selection dialog |
| SettingsSection | `feature/settings/widget/` | Settings group card |

## Utilities

| Utility | File | Description |
|---------|------|-------------|
| AppMessenger | `product/utils/` | SnackBar, Dialog, BottomSheet |
| AppPaddings | `product/const/` | Padding/spacing constants |
| RegexTypes | `product/const/` | Validation regex patterns |
| ResponsiveExtension | `product/utils/` | `context.r(20)`, `context.rf(16)` |
| ButtonFeedback | `product/utils/` | Haptic + sound feedback |
| ThemeDecorations | `product/utils/` | Theme-based container decoration |

## Fonts

| Font | Usage |
|------|-------|
| Poppins | Display + Headline (large titles) |
| Inter | Title + Label + Body (body text) |

---

## Documentation Map

To find which file to read when adding a new feature:

→ **[doc/new_feature/README.md](new_feature/README.md)** — Task-based navigation table + 24-item checklist

### Inline guides (inside code)

| File | Location | Topic |
|------|----------|-------|
| `CACHE_GUIDE.md` | `lib/product/cache/` | Cache system usage guide |
| `THEME.md` | `lib/product/theme/` | Theme system documentation |
| `ONBOARDING.md` | `lib/feature/login_process/onboarding/` | Onboarding module |

---

## New Project Setup (Customization)

After post-clone setup (`setup_after_clone.md`) is complete:

1. Change `name` field in `pubspec.yaml`
2. Update package name across the project (`akillisletme` → new name)
3. Update strings in `assets/translations/`
4. Fill store URLs in `AppString`
5. Change or customize the animation text in `home_background.dart`
6. Remove unneeded color variants from `app_theme_variant.dart`
7. Update onboarding step contents
8. **Android native modules** — answer the 7 setup questions above (§15–16) to adapt the widget and overlay to your data model
9. Run `flutter pub get && dart run build_runner build --delete-conflicting-outputs`
10. Follow the checklist in `doc/new_feature/README.md` to start adding features

---

## Setup Commands (Reference)

```bash
# Dependencies
flutter pub get

# Code-gen (Freezed, GoRouter, Hive, FlutterGen)
dart run build_runner build --delete-conflicting-outputs

# EasyLocalization key generation
flutter pub run easy_localization:generate -O lib/product/init/language -f keys -o locale_keys.g.dart --source-dir assets/translations

# Error analysis
flutter analyze
```
