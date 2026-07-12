# doc/ — Bilgi Tabanı (AI buradan başla)

Bu klasör bu template'in yerleşik bilgi tabanıdır. Kuralları ezberlemek yerine ilgili dosyayı oku. Giriş noktası ve bağlayıcı kurallar için köke bak: **[`AGENTS.md`](../AGENTS.md)**.

## Klasör haritası

| Klasör / dosya | İçerik |
|---|---|
| [`project.md`](project.md) | Proje genel bakışı: sistemler, mimari, uygulama akışı |
| [`clean_code.md`](clean_code.md) | Her satır kodda geçerli taşınabilir temiz-kod standardı |
| [`guides/`](guides/) | Günlük geliştirme görev rehberleri (feature, state, servis, model…) |
| [`setup/`](setup/) | **Klonlayan kullanıcı** için tek seferlik kurulum ve özelleştirme |
| [`troubleshooting/`](troubleshooting/) | Ortam sorunları (emulator vb.) |
| [`ideas/`](ideas/) | Plan, fikir ve yol haritası dokümanları |

> `setup/` klasörü template'i kendi projesine uyarlayan kullanıcı içindir. Template'in geliştiricisiysen bu akışları kendi başına çalıştırma (bkz. `AGENTS.md` → "Kim için çalışıyorsun?").

## Görev → doc yönlendirme

| Yapılacak iş | Oku |
|---|---|
| Yeni feature klasörü | [guides/folder_structure.md](guides/folder_structure.md) |
| Servis ekleme | [guides/service_rules.md](guides/service_rules.md) + [guides/service_initialization.md](guides/service_initialization.md) |
| Model oluşturma | [guides/model_rules.md](guides/model_rules.md) |
| Enum / sabit ekleme | [guides/enums_and_constants.md](guides/enums_and_constants.md) |
| State kurulumu (Cubit + Freezed) | [guides/state_management.md](guides/state_management.md) |
| Veri saklama (SharedCache / Hive) | [guides/data_storage.md](guides/data_storage.md) |
| View kurma (Stateless / Stateful) | [guides/view_rules.md](guides/view_rules.md) |
| Widget ekleme / tema kullanımı | [guides/widget_and_theme.md](guides/widget_and_theme.md) |
| Route / UI string ekleme | [guides/route_and_strings.md](guides/route_and_strings.md) |
| Asset ekleme (image, SVG, Lottie) | [guides/assets_and_flutter_gen.md](guides/assets_and_flutter_gen.md) |
| Store URL / iletişim güncelleme | [setup/settings_and_urls.md](setup/settings_and_urls.md) |
| Yeni projeye uyarlama | [setup/customization.md](setup/customization.md) |

## Yeni feature checklist

Yeni bir feature eklerken:

- [ ] `lib/feature/<feature>/` klasör yapısını kur ([guides/folder_structure.md](guides/folder_structure.md))
- [ ] `<feature>.md` modül dokümanını yaz ([guides/folder_structure.md](guides/folder_structure.md))
- [ ] Model gerekiyorsa Freezed + toMap/fromMap ile yaz ([guides/model_rules.md](guides/model_rules.md))
- [ ] Freezed state + Cubit'i doğru yerde konumlandır ([guides/state_management.md](guides/state_management.md))
- [ ] App-geneli state mi? → `product/state/`, değilse `feature/state/`
- [ ] BlocProvider: global (state_initialize) mı, lokal (view içinde) mi karar ver
- [ ] `dart run build_runner build --delete-conflicting-outputs` çalıştır
- [ ] View tipini belirle: StatelessWidget / StatefulWidget + ViewModel ([guides/view_rules.md](guides/view_rules.md))
- [ ] StatefulWidget ise `_view.dart` + `_view_model.dart` olarak ayır
- [ ] Widget ihtiyacı: önce `product/widget/` kontrol et, varsa kullan ([guides/widget_and_theme.md](guides/widget_and_theme.md))
- [ ] Ortak widget yoksa ve genelse → `product/widget/`; feature'a özelse → `feature/widget/`
- [ ] Tema: `product/theme/parts/` kontrol et, global stil yeterse override etme
- [ ] Enum → `product/enum/` (önce mevcut var mı bak) ([guides/enums_and_constants.md](guides/enums_and_constants.md))
- [ ] Sabit → `product/const/`
- [ ] Servis ihtiyacı: locator'ı kontrol et, gerekirse ekle ([guides/service_rules.md](guides/service_rules.md) + [guides/service_initialization.md](guides/service_initialization.md))
- [ ] Yeni servis init gerektiriyor mu → locator'daki `_initializeServices()`'e ekle
- [ ] Basit veri cache? → `SharedCache` + `SharedKeys` ([guides/data_storage.md](guides/data_storage.md))
- [ ] Model/liste cache? → `ProductCache` + Hive model ([guides/data_storage.md](guides/data_storage.md))
- [ ] String'leri `AppString`'e ekle ([guides/route_and_strings.md](guides/route_and_strings.md))
- [ ] Route'u `app_router.dart`'a TypedGoRoute olarak ekle + build_runner ([guides/route_and_strings.md](guides/route_and_strings.md))
- [ ] Padding/spacing: `AppPaddings` sabitleri, hardcoded değer yok ([guides/widget_and_theme.md](guides/widget_and_theme.md))
- [ ] Kullanıcı geri bildirimi gerekiyor mu? `AppMessenger` kullan ([guides/widget_and_theme.md](guides/widget_and_theme.md))
- [ ] Asset: `Assets.xxx` üretilmiş accessor'lar, hardcoded path yok ([guides/assets_and_flutter_gen.md](guides/assets_and_flutter_gen.md))
- [ ] Yeni asset eklendi mi? `dart run build_runner build --delete-conflicting-outputs`
- [ ] Responsive kontrol ekle (`isWide`)
- [ ] Tema renklerini kullan (hardcoded renk yok)
