# Service Initialization & Locator

All services are initialized with the following flow:

```
ApplicationInit.start()
  -> WidgetsFlutterBinding.ensureInitialized()
  -> Firebase.initializeApp(...)          // When Firebase is enabled
  -> setupLocator()
      -> _registerSingletons()            // Register services
      -> _initializeServices()            // Call async init
```

## ApplicationInit (`lib/product/init/application_init.dart`)

```dart
@immutable
final class ApplicationInit {
  const ApplicationInit();

  Future<void> start() async {
    WidgetsFlutterBinding.ensureInitialized();
    // await Firebase.initializeApp(
    //   options: DefaultFirebaseOptions.currentPlatform,
    // );
    await setupLocator();
  }
}
```

## Service Locator (`lib/product/service/service_locator.dart`)

```dart
final GetIt locator = GetIt.instance;

Future<void> setupLocator() async {
  _registerSingletons();
  await _initializeServices();
}

Future<void> _initializeServices() async {
  await locator<SharedCache>().init();
  await locator<ProductCache>().init();
  await locator<RemoteConfigService>().init();
}

void _registerSingletons() {
  locator
    ..registerSingleton<SharedCache>(SharedCache.instance)
    ..registerSingleton<ProductCache>(
      ProductCache(cacheManager: HiveCacheManager()),
    )
    ..registerSingleton<RemoteConfigService>(
      RemoteConfigService.instance,
    );
}

extension ServiceLocator on GetIt {
  SharedCache get sharedCache => locator<SharedCache>();
  ProductCache get productCache => locator<ProductCache>();
  RemoteConfigService get remoteConfigService => locator<RemoteConfigService>();
}
```

## Adding a new service to locator

1. Add to `_registerSingletons()`:
   ```dart
   ..registerSingleton<NewService>(NewService.instance)
   ```
2. If async init is needed, add to `_initializeServices()`:
   ```dart
   await locator<NewService>().init();
   ```
3. Add getter to `ServiceLocator` extension:
   ```dart
   NewService get newService => locator<NewService>();
   ```

## Init pattern

Services that require async setup follow this pattern:

```dart
class SomeService {
  SomeService._();
  static final SomeService instance = SomeService._();

  bool _initialized = false;

  Future<void> init() async {
    if (_initialized) return;
    // setup logic...
    _initialized = true;
  }
}
```

## Registered services

| Service | Type | Access |
|---------|------|--------|
| SharedCache | Simple key-value cache | `locator.sharedCache` |
| ProductCache | Hive model cache | `locator.productCache` |
| RemoteConfigService | Firebase Remote Config | `locator.remoteConfigService` |
| UrlLauncherService | URL opening / email sending | `locator.urlLauncher` |
| PermissionService | Permission management (permission_handler) | `locator.permission` |
