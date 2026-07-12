import 'package:akillisletme/feature/login_process/splash/state/splash_cubit.dart';
import 'package:akillisletme/product/const/app_paddings.dart';
import 'package:akillisletme/product/const/app_string.dart';
import 'package:akillisletme/product/init/language/locale_keys.g.dart';
import 'package:akillisletme/product/navigation/app_router.dart';
import 'package:akillisletme/product/widget/app_primary_button.dart';
import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:url_launcher/url_launcher.dart';

part 'widgets/error_view.dart';
part 'widgets/loading_view.dart';
part 'widgets/update_required_view.dart';

final class SplashView extends StatelessWidget {
  const SplashView({super.key});

  @override
  Widget build(BuildContext context) {
    return const _SplashBody();
  }
}

final class _SplashBody extends StatelessWidget {
  const _SplashBody();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocConsumer<SplashCubit, SplashState>(
        listener: (context, state) {
          state.whenOrNull(
            success: () {
              const HomeRoute().go(context);
            },
          );
        },
        builder: (context, state) {
          return state.when(
            initial: () => const _LoadingView(),
            checking: () => const _LoadingView(),
            success: () => const SizedBox.shrink(),
            updateRequired: (currentVersion, minimumVersion) {
              return _UpdateRequiredView(
                currentVersion: currentVersion,
                minimumVersion: minimumVersion,
              );
            },
            error: (message) {
              return _ErrorView(message: message);
            },
          );
        },
      ),
    );
  }
}
