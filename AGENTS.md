# AGENTS.md — clean_start (Flutter Starter Template)

Bu dosya, bu depoda çalışan **herhangi bir AI ajanı** (Claude, Cursor, Copilot, Codex, Windsurf…) için kanonik talimat dosyasıdır. Kökteki `CLAUDE.md` ve `.cursorrules` sadece buraya yönlendirir — **tek kaynak bu dosyadır.**

Bu depo bir Flutter **starter template**'idir. Mimari kuralların ayrıntısı `doc/` klasöründedir; kurallar burada tekrarlanmaz, ilgili doc dosyası okunur. Bu dosya yalnızca üç şeyi yapar: (1) yönlendirme, (2) genel kod prensibine referans, (3) doc'ta olmayan bağlayıcı kurallar.

## Nasıl başlarsın (her oturum)

Kod yazmadan önce sırayla:

1. **`doc/README.md`** — doc haritası + görev→doc yönlendirme tablosu.
2. **`doc/project.md`** — proje ne, hangi sistemler var, uygulama akışı.
3. **`doc/clean_code.md`** — her satır kodda geçerli, taşınabilir temiz-kod standardı.

Yapacağın işe ait doc dosyasını **okumadan** o alanda kod yazma.

## Kim için çalışıyorsun?

Bu depoda çalışıyorsan **template'in geliştiricisisin.** `doc/setup/` altındaki dosyalar (`setup_after_clone.md`, `firebase.md`, `customization.md`, `native_splash.md`, `settings_and_urls.md`) template'i klonlayıp **kendi projesine uyarlayan kullanıcı** içindir. Bu kurulum akışlarını kendi başına çalıştırma; kullanıcı açıkça isterse ona rehberlik et.

## Göreve göre okunacak doc

| Yapılacak iş | Önce oku |
|---|---|
| Temiz kod standardı (her zaman geçerli) | `doc/clean_code.md` |
| Yeni feature | `doc/README.md` (checklist) + `doc/guides/folder_structure.md` |
| Cubit / state | `doc/guides/state_management.md` |
| View / widget yapısı | `doc/guides/view_rules.md` |
| Model (Freezed / Hive) | `doc/guides/model_rules.md` |
| Servis yazma | `doc/guides/service_rules.md` + `doc/guides/service_initialization.md` |
| Veri saklama kararı | `doc/guides/data_storage.md` |
| Route / string ekleme | `doc/guides/route_and_strings.md` |
| Widget yerleşimi / tema | `doc/guides/widget_and_theme.md` |
| Asset ekleme | `doc/guides/assets_and_flutter_gen.md` |
| Enum / sabit | `doc/guides/enums_and_constants.md` |
| Genel bakış / sistemler | `doc/project.md` |
| Template'i yeni projeye uyarlama (cloner) | `doc/setup/` |

## Kod yazarken

- Yazdığın her Dart/Flutter kodu **`doc/clean_code.md`** standardına uyar (widget kuralları, dört-durum, hata yönetimi, paket disiplini, test, bitirme kontrolü). Bu standart **mimari-agnostiktir**: projenin mevcut mimarisine (Cubit+Freezed, GetIt locator, GoRouter) dokunmaz, onun *içinde* temiz kod yazdırır.
- **Mevcut desene uy.** Aynı türde iş nasıl yapılmış (benzer ekran, benzer servis) bak, o deseni takip et. Yeni pattern/yaklaşım icat etme; gerekiyorsa önce sor.

## Doc'ta olmayan bağlayıcı kurallar

- **Dil:** doc ve doc-comment'ler Türkçe; kod tanımlayıcıları (sınıf, değişken, metot) İngilizce.
- **Rapor/analiz/plan/fikir** dokümanları `doc/ideas/` veya `doc/reports/` altına yazılır. Kök dizine veya rastgele klasörlere `.md` bırakma.
- Her yeni feature, `doc/guides/folder_structure.md`'deki kurala göre kendi **`<feature>.md`** dokümanıyla birlikte gelir. Doc yazılmadıysa feature bitmemiştir.
- Template'e yeni bir sistem/pattern eklendiğinde ilgili `doc/` dosyası **aynı commit'te** güncellenir. Doc'u sonraya bırakma.
- Kod stili **`very_good_analysis`**: `flutter analyze` sıfır uyarı ile biter; `// ignore:` ile susturma yapılmaz.
- Inline `TextStyle(fontSize: ...)` ve inline renk **yasak** → `product/theme/` üzerinden git (`doc/guides/widget_and_theme.md`).
- **Bitirme:** `dart format .` → `flutter analyze` → codegen gerektiyse `build_runner` → `flutter test`. Ayrıntı: `doc/clean_code.md`.

## Doc ile kod çelişirse

Hangisinin güncel olduğunu **SOR**; kafana göre birini seçme.

## Bilinen açık kararlar

- **DI:** template şu an servislerde singleton (`.instance`) + locator kaydı kullanıyor. Yeni servislerde `.instance` eklemek yerine `registerLazySingleton` + constructor injection tercih edilir; mevcutları migrate etme kararı ayrıca verilecek.
