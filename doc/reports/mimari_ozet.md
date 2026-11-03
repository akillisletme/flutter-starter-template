# clean_start — Mimari Özet (yeni proje planlaması için)

> Bu dosya `doc/guides/` altındaki 30+ rehberin **tek sayfalık damıtımıdır**.
> Amaç: yeni bir uygulamanın mimarisini bu template'e göre tasarlayan bir AI
> ajanının, kod yazmadan önce "ne nereye, hangi şekilde" sorusunu tam olarak
> cevaplayabilmesi. Ayrıntı gerektiğinde parantez içindeki rehber okunur.

---

## 0. Bir cümlede

Flutter + **Cubit/Freezed** state, **GetIt** DI, **GoRouter (type-safe, codegen)**
navigasyon, **EasyLocalization** metin, **FlutterGen** asset, **Hive + SharedPreferences**
saklama, **Material 3 + token'lı tasarım sistemi**, `very_good_analysis` lint.
Her şey hazır ama **bayrakla kapalı** gelir (`AppConfig`); bayrak kapalıyken servis
sessiz bir no-op'a düşer, uygulama çökmez.

---

## 1. İki katman: `feature/` ve `product/`

```
lib/
├── main.dart
├── feature/          ← ekranlar. Her ekran kendi klasörü.
└── product/          ← paylaşılan altyapı. Feature'lar buradan tüketir.
    ├── cache/        SharedCache (SharedPreferences) + ProductCache (Hive CE)
    ├── const/        AppString, AppPaddings, AppRadius, AppDurations,
    │                 AppIconSizes, RegexTypes, AppSemanticKeys, AppConfig
    ├── enum/         ViewState, TextFieldType — TÜM enum'lar burada
    ├── init/         ApplicationInit, AppErrorHandler, state_initialize, dil
    ├── model/        Hive'a yazılan / app geneli modeller
    ├── navigation/   app_router.dart (TypedGoRoute) + geçiş animasyonları
    ├── service/      servisler + service_locator.dart (GetIt)
    ├── state/        app-geneli cubit'ler (ThemeCubit, AuthCubit)
    ├── theme/        Material 3, 8 varyant, AppThemeColors, AppShadows
    ├── utils/        AppMessenger, extension'lar, Validators, NetworkChecker
    ├── widget/       paylaşılan widget'lar (buton, textfield, state view, sheet)
    └── generated/    FlutterGen çıktısı — elle düzenlenmez
```

**Ayrım kuralı:** bir şey ikinci bir feature tarafından kullanılacaksa `product/`,
tek feature'a özelse `feature/<ad>/` içinde. Tek istisna: **enum'lar her zaman
`product/enum/`**, sabitler her zaman `product/const/`.

---

## 2. Feature anatomisi (yeni ekran ekleme şablonu)

```
lib/feature/<feature_name>/
  <feature_name>_view.dart          # ana view
  <feature_name>_view_model.dart    # YALNIZCA StatefulWidget ise
  <feature_name>.md                 # ZORUNLU modül dokümanı
  state/
    <feature_name>_cubit.dart
    <feature_name>_state.dart       # Freezed
  widget/                           # bu ekrana özel alt widget'lar
  model/                            # (opsiyonel) feature'a özel modeller
  service/                          # (opsiyonel) yalnız bu feature'ın servisi
```

`<feature>.md` yazılmadıysa feature **bitmemiştir**. İçerik: modül ne yapar,
hangi Cubit/State/Widget'ları barındırır.

### View tipi seçimi (`view_rules.md`)

| Durum | Seçim |
|---|---|
| Sadece Bloc/Cubit state yetiyor | `StatelessWidget` (varsayılan) |
| AnimationController / TextEditingController / PageController / ScrollController / initState-dispose | `StatefulWidget` + ayrı `_view_model.dart` |

StatefulWidget deseni: `<Feature>View` (StatefulWidget) → `_<Feature>ViewState
extends <Feature>ViewModel` → `abstract class <Feature>ViewModel extends
State<<Feature>View>` (controller'lar ve lifecycle burada, `build` view'da).

---

## 3. State (`state_management.md`)

**Freezed state:**
```dart
@freezed
abstract class XState with _$XState {
  const factory XState({@Default(false) bool isLoading}) = _XState;
}
```

**Cubit:** servisleri **constructor injection** ile alır, `emit(state.copyWith(...))`
ile günceller. Cubit'in içine `locator.x` gömmek yasaktır (test edilemez hale gelir).

**BlocProvider nereye?**

| Senaryo | Yer |
|---|---|
| Birden çok ekran erişiyor / açılışta yüklenmeli / app geneli (tema, auth) | `product/state/` + global kayıt: `lib/product/init/state_initialize.dart` |
| Yalnız bu ekranın ömrü boyunca yaşıyor | Lokal: view'ın `build`'inde `BlocProvider` |

Global örnek:
```dart
BlocProvider(create: (_) => XCubit(service: locator.someService)..loadData()),
```

---

## 4. Servis ve DI (`service_rules.md`, `service_initialization.md`)

Tek sahip: `lib/product/service/service_locator.dart`.

**Başlatma zinciri** (`main()` bunu `await` eder → ilk kare çizilmeden her şey hazır):
```
ApplicationInit.start()
  → ensureInitialized → AppErrorHandler.register() → orientation + EasyLocalization
  → Firebase.initializeApp()  (yalnız AppConfig.firebaseEnabled ise)
  → setupLocator() → _registerLazySingletons() → _registerAsyncSingletons()
  → locator.allReady()
```

**Karar ağacı:** locator'da var mı? → varsa `locator.x`. Yoksa paylaşılan mı?
→ `product/service/` + locator kaydı. Tek feature'a mı özel? → `feature/<ad>/service/`,
**locator'a kaydedilmez**, cubit'e constructor'dan verilir.

**Yeni servis yazma sırası:**
1. `abstract interface class` ile sözleşme — servis dış dünyanın tipini
   (Firebase, RevenueCat) **sızdırmaz**; kendi modelini ve hata kodunu döndürür.
2. `registerLazySingleton` (durumsuz) veya `registerSingletonAsync` (init gerekiyorsa,
   sıra bağımlılığı varsa `dependsOn:`).
3. `extension ServiceLocator on GetIt` içine getter ekle.
4. Dış bağımlılığı varsa **no-op karşılığını da yaz.**

**Firebase üçlü deseni:** `interface + FirebaseXxx + NoopXxx`. Bayrak kapalıyken
no-op bağlanır → **çağrı yerinde asla `if (firebaseEnabled)` yazılmaz.**
Kayıtlı: `AuthService · CrashReporter · AnalyticsService · PushService ·
StorageService · UserProfileService · AppVersionSource · PurchaseService ·
SharedCache · ProductCache · PermissionService · UrlLauncher`

> Not: eski servisler singleton `.instance` desenini kullanıyor; **yeni servisler**
> `registerLazySingleton` + constructor injection ile yazılır.

---

## 5. Veri saklama (`data_storage.md`)

| Veri | Sistem | Erişim |
|---|---|---|
| bool / int / double / String / List\<String\>, ayarlar, bayraklar | `SharedCache` | `locator.sharedCache` |
| Model listeleri, koleksiyon, offline cache | `ProductCache` (Hive CE) | `locator.productCache` |

- SharedCache'e anahtar ekleme: `SharedKeys` enum'una ekle → `shared_cache.dart`'a
  tiplenmiş getter/setter yaz → `locator.sharedCache.xxx`.
- Hive modeli ekleme: `hive_v2/model/` içinde model → `hive_adapters.dart`'a
  `AdapterSpec<Model>()` → `product_cache.dart`'a `CacheOperation<Model>` → codegen.

**Model kuralı (`model_rules.md`):** feature'a özel → Freezed + elle `toMap`/`fromMap`
(`const X._();` gerekir). Hive'a yazılan → `@HiveType`/`@HiveField`, `product/model/`.

---

## 6. Navigasyon (`route_and_strings.md`)

Type-safe `TypedGoRoute` + `GoRouteData` + codegen. Tek dosya:
`lib/product/navigation/app_router.dart`.

```dart
class FeatureRoute extends GoRouteData with $FeatureRoute {
  const FeatureRoute();
  @override
  Page<void> buildPage(BuildContext c, GoRouterState s) =>
      slideRightTransition(key: s.pageKey, child: const FeatureView());
}
```
Anotasyona ekle (alt rotalar `HomeRoute` altında nested) → `./script/codegen.sh`.

Kullanım: `const FeatureRoute().go(context)` / `.push(context)`.
Veri geçişi `$extra` ile. Geçişler: `slideRightTransition`, `fadeTransition`.

**Guard'lı rotalara daima `go`, asla `push`** (push imperative'dir, redirect
yeniden çalışmaz → login ekranı takılı kalır).

---

## 7. Metinler

- **Kullanıcıya görünen her metin** → `assets/translations/tr.json` **ve** `en.json`
  (nested JSON) → `./script/lang.sh` → `LocaleKeys.feature_title.tr()`.
  `-s en.json` bayrağı zorunlu olduğu için komut elle yazılmaz, script çalıştırılır.
- **Çevrilmeyen sabit** (URL, store linki, teknik string) → `product/const/app_string.dart`.

---

## 8. Ekran durumları — dört durum zorunlu (`ui_states.md`)

Veri çeken her ekran **loading / error / empty / data** dördünü ele alır; biri
eksikse ekran bitmemiştir. Elle `if/else` zinciri yazılmaz:

```dart
AppStateView(
  state: ViewState.from(isLoading: …, hasError: …, isEmpty: …),
  errorMessage: state.errorMessage,
  onRetry: context.read<XCubit>().load,
  loadingPlaceholder: const AppShimmerList(),   // liste ekranında shimmer
  emptyTitle: LocaleKeys.x_emptyTitle.tr(),
  builder: () => XList(items: state.items),     // yalnız data durumunda çalışır
)
```
Öncelik sabit: loading → error → empty → data. **Boş liste hata değildir.**

`errorMessage` **yerelleştirilmiş kullanıcı metnidir**; cubit ham exception'ı
state'e koymaz:
```dart
} on Object catch (e, s) {
  AppErrorHandler.reportHandled(e, s);
  emit(state.copyWith(errorMessage: LocaleKeys.error_generic.tr()));
}
```
Hazır anahtarlar: `error.generic`, `error.network`, `error.timeout`, `error.permission`.
Akışı durduran hata için `AppErrorSheet.show(context)`.

---

## 9. Tasarım sistemi — ham değer yasak (`design_tokens.md`, `widget_and_theme.md`)

| Yapma | Yap |
|---|---|
| `EdgeInsets.all(16)` | `AppPaddings.allL` |
| `SizedBox(height: 12)` | `Column(spacing: AppPaddings.m, …)` |
| `BorderRadius.circular(12)` | `AppRadius.allM` |
| `Duration(milliseconds: 300)` | `AppDurations.short` |
| `Icon(…, size: 24)` | `AppIconSizes.m` |
| `BoxShadow(...)` | `AppShadows.card(context)` |
| `TextStyle(fontSize: 24)` | `context.textTheme.headlineSmall` |
| `Colors.red` / `Color(0xFF…)` | `context.colorScheme.*` → `context.appColors.*` |
| `MediaQuery…width > 600` | `context.isWide` |

Ölçekler: padding xs4 s8 m12 l16 xl20 xxl24 xxxl32 · radius xs4 s8 m12 l16 xl20 xxl28 pill999 ·
süre instant120 short220 medium350 long600 · ikon xs14 s18 m24 l32 xl48 xxl72.

Renk önceliği: **1)** `colorScheme` **2)** `AppThemeColors` semantic token'ları
(`context.appColors.scoreGold`) **3)** asla inline.
Font: Poppins (display/headline), Inter (title/label/body).

**Widget karar ağacı:** `product/widget/`de var mı? → kullan. Yoksa app geneli mi?
→ `product/widget/`. Feature'a özel mi? → `feature/<ad>/widget/`.

Hazır paylaşılan widget'lar: `AppScaffold` (padding+SafeArea+klavye kapatma),
`AppPrimaryButton` / `AppSecondaryButton` / `AppTextButton` (haptic dahil),
`AppTextField`, `AppStateView` + `AppLoadingView` / `AppErrorView` / `AppEmptyView`,
`AppShimmer(Box|List)`, `AppSelectSheet`, `AppErrorSheet`, `AppSemantics`.

Kullanıcı geri bildirimi: `context.showSuccessSnack()` / `showErrorSnack()` /
`showConfirmDialog()` / `showAppBottomSheet()` (`AppMessenger`).

---

## 10. Form (`forms_and_validation.md`)

```dart
AppTextField(
  controller: emailController,
  label: LocaleKeys.auth_email.tr(),
  type: TextFieldType.email,          // klavye + autofill + filtre + limit buradan
  validator: Validators.email,
)
```
`TextFieldType`: text · multiline · email · password (göster/gizle otomatik) ·
phone · number · url · name · search.

`Validators`: required · optional · email · phone · password · fullName · url ·
minLength · maxLength · numericRange · match · all.
Hata metni **validator'ın içinde** üretilir (`LocaleKeys.validation_*`); çağrı
yerinde metin yazılmaz. Yeni kural → `AppValidator`'dan türet.

Controller'lar `_view_model.dart` içinde açılır ve `dispose` edilir.

---

## 11. Extension'lar (`extensions.md`) — `product/utils/extension/`

- **context:** `isWide` `isPortrait` `r(20)` `rf(16)` `widthRatio(.4)` `theme`
  `colorScheme` `textTheme` `isDarkMode` `isKeyboardOpen` `dismissKeyboard()`
- **String:** `normalize` `initials()` `shortDisplayName` `withHttps` `capitalized`
  `isValidEmail` `isValidPhone` `digitsOnly` `copyToClipboard()` ·
  nullable: `isNullOrBlank` `orElse()`
- **DateTime:** `startOfDay` `isToday` `hm` `shortDate` `dateTimeLabel` `timeAgo`
  `relativeDayLabel`
- **num:** `padded` `grouped` `compact` `3.seconds` `trimmed(1)` `readableFileSize`

Aynı mantık ikinci kez yazılacaksa extension'a çıkar; kullanıcıya görünen metin
döndüren extension `LocaleKeys` kullanır.

---

## 12. Asset (`assets_and_flutter_gen.md`)

`assets/{image,svg,lottie,songs,translations,font}/` → `./script/codegen.sh` →
`Assets.image.logo.image()`, `Assets.svg.star.svg()`, `Assets.lottie.x.lottie()`,
`FontFamily.poppins`. **Hardcoded path yasak.**

---

## 13. Auth (`auth.md`) — kullanılacaksa

```
Ekranlar (feature/auth/) → AuthCubit (product/state/auth/) → AuthService (interface)
                                                              ├─ FirebaseAuthService
                                                              └─ MockAuthService
```
Sert kurallar:
1. **Ekran yönlendirme yapmaz** — oturum `authStateChanges` stream'inden akar,
   `GoRouterRefreshStream` guard'ı yeniden değerlendirir, yönlendirmeyi router yapar.
2. Guard'lı rotalara **`go`**, `push` değil.
3. `AuthActionCanceled` hata değildir, mesaj gösterilmez.
4. Cubit'te elle `emit` yok (2 dokümante istisna dışında).
5. Servis kullanıcı metni üretmez; `AuthException.code` → `AuthErrorLocalizer`.

Korumalı rota gerektiğinde `AppRouter.protectedPrefixes`'e önek eklenir; ekranda
`if (loggedIn)` yazılmaz. `AppConfig.allowGuestBrowsing` varsayılan `true` —
kayıt duvarı dönüşümü düşürür.

`AuthSideEffects` (RevenueCat UID senkronu, Analytics kimliği, Firestore profil
upsert) cubit'ten ayrıdır. **Hesap silme mağaza şartıdır** (App Store 5.1.1(v)).

---

## 14. Test (`testing.md`)

Test dosyası kaynakla **aynı klasör yapısında** `test/` altında.
- Cubit testi: servis constructor'dan geldiği için sahte implementasyon yeterli,
  GetIt'e dokunulmaz.
- Widget testi: `setUpAll(initializeTestBindings)` + `tester.pumpApp(widget)`.
  **Çevrilmiş metne değil, widget tipine ve davranışa bakılır**
  (`find.byType(AppPrimaryButton)`, `find.text('Tekrar Dene')` değil).
- `pumpAndSettle` kullanma (sonsuz animasyon → timeout); sabit `pump()` kullan.
- UI/E2E kimliği: `AppSemantics(semanticKey: AppSemanticKeys.retryButton, …)`.
  Flutter'da `Key()` erişilebilirlik id'si DEĞİLDİR; Maestro yalnız
  `Semantics(identifier:)` görür.

Test edilir: cubit/servis davranışı, extension/validator kuralları, dört durumun
dallanması, sınır durumlar. Edilmez: framework'ün işi, çeviri içeriği, piksel/renk.

---

## 15. Bayraklar — `lib/product/const/app_config.dart`

Tek anahtar dosyası: `firebaseEnabled`, `enableEmailSignIn`, `enableGoogleSignIn`,
`enableAppleSignIn`, `googleServerClientId`, `requireEmailVerification`,
`allowGuestBrowsing`, push/ödeme bayrakları. Kapalıyken ilgili servis no-op'a düşer;
**başka hiçbir dosya değişmez.**

---

## 16. Temiz kod standardı (`clean_code.md`) — her satırda geçerli

- Önce `StatelessWidget`; `build()` içinde ağır işlem yok; `build()` ~80 satırı
  geçiyorsa parçala — tekrar eden UI **metot değil ayrı widget sınıfı**.
- Mümkün olan her yerde `const`. İş mantığı widget'ta olmaz.
- Boş `catch {}` yasak. Hata yakalandığı katmanda loglanır, anlamlı tipe çevrilir;
  UI ham exception görmez. Her `async` çağrıda 4 senaryo: timeout, ağ yok, yetki, boş yanıt.
- Magic number/string yok. Ölü kod ve yorum satırına alınmış kod commit'lenmez.
- Yorum "ne"yi değil "neden"i anlatır.
- **Yeni paket eklemeden önce SOR.** Animasyonda önce Flutter'ın kendi araçları.
- Yeni iş mantığı testle gelir.
- `// ignore:` ile lint susturma yasak; `flutter analyze` sıfır uyarı.

**Mevcut desene uy — yeni pattern icat etme.** Doc ile kod çelişirse SOR.

**Dil:** doc ve doc-comment **Türkçe**; kod tanımlayıcıları **İngilizce**.

---

## 17. Komutlar — elle uzun komut yazılmaz

```bash
./script/general.sh    # pub get + codegen + locale keys (klon sonrası tek komut)
./script/codegen.sh    # Freezed / GoRouter / Hive / FlutterGen
./script/lang.sh       # locale_keys.g.dart (-s en.json garantili)
./script/verify.sh     # BİTİRME: dart format → flutter analyze → flutter test
./script/deep_clean.sh # sıfırdan kurulum
```

---

## 18. Yeni feature checklist (kısa)

1. `lib/feature/<ad>/` iskeleti + `<ad>.md`
2. Model gerekiyorsa Freezed (+ Hive ise `product/model/`)
3. Freezed state + Cubit → app geneli mi (`product/state/` + global) yoksa lokal mi
4. View tipi: Stateless mi, Stateful + ViewModel mi
5. Widget: önce `product/widget/`e bak
6. Veri çekiyorsa `AppStateView` ile dört durum + liste ise shimmer
7. Form varsa `AppTextField` + `Validators`
8. Servis gerekiyorsa locator karar ağacı
9. Cache gerekiyorsa SharedCache / ProductCache
10. Metinler `tr.json` + `en.json` → `./script/lang.sh`
11. Route → `app_router.dart` TypedGoRoute → codegen
12. Token'lar (padding/radius/gölge/süre/ikon/renk/tipografi) — ham değer yok
13. `context.isWide` ile responsive kontrol
14. E2E gerekiyorsa `AppSemanticKeys` kimliği
15. Yeni iş mantığı için test
16. **`./script/verify.sh` sıfır hata**
