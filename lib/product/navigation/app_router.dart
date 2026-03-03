import 'package:akillisletme/feature/home/home_view.dart';
import 'package:akillisletme/feature/settings/language_selection/language_selection_view.dart';
import 'package:akillisletme/feature/login_process/onboarding/onboarding_view.dart';
import 'package:akillisletme/feature/settings/about/about_view.dart';
import 'package:akillisletme/feature/settings/settings_view.dart';
import 'package:akillisletme/product/navigation/route_transitions.dart';
import 'package:akillisletme/product/service/service_locator.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

part 'app_router.g.dart';

@TypedGoRoute<HomeRoute>(
  path: '/',
  routes: [
    TypedGoRoute<SettingsRoute>(
      path: 'settings',
      routes: [
        TypedGoRoute<AboutRoute>(path: 'about'),
        TypedGoRoute<LanguageSelectionRoute>(path: 'language'),
      ],
    ),
  ],
)
class HomeRoute extends GoRouteData with $HomeRoute {
  const HomeRoute();

  @override
  Page<void> buildPage(BuildContext context, GoRouterState state) {
    return fadeTransition(key: state.pageKey, child: const HomeView());
  }
}

class SettingsRoute extends GoRouteData with $SettingsRoute {
  const SettingsRoute();

  @override
  Page<void> buildPage(BuildContext context, GoRouterState state) {
    return slideRightTransition(
      key: state.pageKey,
      child: const SettingsView(),
    );
  }
}

class AboutRoute extends GoRouteData with $AboutRoute {
  const AboutRoute();

  @override
  Page<void> buildPage(BuildContext context, GoRouterState state) {
    return slideRightTransition(
      key: state.pageKey,
      child: const AboutView(),
    );
  }
}

class LanguageSelectionRoute extends GoRouteData with $LanguageSelectionRoute {
  const LanguageSelectionRoute();

  @override
  Page<void> buildPage(BuildContext context, GoRouterState state) {
    return slideRightTransition(
      key: state.pageKey,
      child: const LanguageSelectionView(),
    );
  }
}

@TypedGoRoute<OnboardingRoute>(path: '/onboarding')
class OnboardingRoute extends GoRouteData with $OnboardingRoute {
  const OnboardingRoute();

  @override
  Page<void> buildPage(BuildContext context, GoRouterState state) {
    return fadeTransition(key: state.pageKey, child: const OnboardingView());
  }
}

/// App router configuration
final class AppRouter {
  AppRouter._();

  static final GoRouter router = GoRouter(
    initialLocation: locator.sharedCache.isOnboardingCompleted
        ? '/'
        : '/onboarding',
    routes: $appRoutes,
  );
}
