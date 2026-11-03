package com.cleanstart.akillisletme

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.RemoteViews
import com.cleanstart.akillisletme.home_widget.HomeWidgetProvider
import com.cleanstart.akillisletme.overlay.OverlayService
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    /// Flutter ilk karesini çizene kadar splash ekranda tutulur; böylece
    /// sistem splash'i ile uygulama arasında boş/siyah kare olmaz.
    private var flutterReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { !flutterReady }
        splash.setOnExitAnimationListener { provider -> animateSplashExit(provider) }
        super.onCreate(savedInstanceState)
    }

    /// Splash çıkış animasyonu.
    ///
    /// **Neden burada:** `windowSplashScreenAnimatedIcon` (AVD) ile çizilen ikon
    /// 192dp'lik dairesel maskenin içinde kalır ve ortadan oynatılamaz. Buradaki
    /// `iconView` ise gerçek bir View — maske yok, tam ekran serbest. Gmail'deki
    /// "ikon uygulamaya dönüşüyor" hissi bu adımdan geliyor.
    ///
    /// ⚠️ `provider.remove()` çağrılmazsa splash ekranda TAKILI KALIR.
    private fun animateSplashExit(provider: androidx.core.splashscreen.SplashScreenViewProvider) {
        val icon = provider.iconView
        val root = provider.view

        // İkon önce hafif küçülüp sonra büyüyerek "içeri çekiliyor" hissi verir
        // (AnticipateInterpolator), aynı anda zemin siliniyor.
        val scaleX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, 0.75f, 1.6f)
        val scaleY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, 0.75f, 1.6f)
        val iconFade = ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 1f, 0f)
        val rootFade = ObjectAnimator.ofFloat(root, View.ALPHA, 1f, 0f)

        scaleX.interpolator = AnticipateInterpolator(1.4f)
        scaleY.interpolator = AnticipateInterpolator(1.4f)
        iconFade.interpolator = AccelerateInterpolator()
        rootFade.interpolator = AccelerateInterpolator()

        AnimatorSet().apply {
            duration = 420L
            playTogether(scaleX, scaleY, iconFade, rootFade)
            doOnEndCompat { provider.remove() }
            start()
        }
    }

    /// `AnimatorSet` için sade bitiş kancası — androidx-ktx bağımlılığı eklemeden.
    private fun AnimatorSet.doOnEndCompat(action: () -> Unit) {
        addListener(
            object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) = action()
            },
        )
    }

    override fun onFlutterUiDisplayed() {
        super.onFlutterUiDisplayed()
        // İlk kare çizildi → splash bekleme koşulu düşer, çıkış animasyonu başlar.
        flutterReady = true
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        setupOverlayChannel(flutterEngine)
        setupCounterChannel(flutterEngine)
    }

    override fun onStart() {
        super.onStart()
        stopService(Intent(this, OverlayService::class.java))
    }

    override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val overlayEnabled = prefs.getBoolean(KEY_OVERLAY_ENABLED, true)
        if (overlayEnabled && Settings.canDrawOverlays(this)) {
            startService(Intent(this, OverlayService::class.java))
        }
    }

    private fun setupOverlayChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, OVERLAY_CHANNEL).apply {
            setMethodCallHandler { call, result ->
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                when (call.method) {
                    "isGranted" -> result.success(Settings.canDrawOverlays(this@MainActivity))
                    "request" -> {
                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                        result.success(null)
                    }
                    "isEnabled" -> result.success(prefs.getBoolean(KEY_OVERLAY_ENABLED, true))
                    "setEnabled" -> {
                        val enabled = call.argument<Boolean>("enabled") ?: true
                        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
        }
    }

    private fun setupCounterChannel(flutterEngine: FlutterEngine) {
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, COUNTER_CHANNEL).apply {
            setMethodCallHandler { call, result ->
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                when (call.method) {
                    "get" -> result.success(prefs.getInt(KEY_COUNTER, 0))
                    "increment" -> {
                        val v = prefs.getInt(KEY_COUNTER, 0) + 1
                        prefs.edit().putInt(KEY_COUNTER, v).apply()
                        updateHomeWidget(v)
                        result.success(v)
                    }
                    "decrement" -> {
                        val v = prefs.getInt(KEY_COUNTER, 0) - 1
                        prefs.edit().putInt(KEY_COUNTER, v).apply()
                        updateHomeWidget(v)
                        result.success(v)
                    }
                    "reset" -> {
                        prefs.edit().putInt(KEY_COUNTER, 0).apply()
                        updateHomeWidget(0)
                        result.success(0)
                    }
                    else -> result.notImplemented()
                }
            }
        }
    }

    private fun updateHomeWidget(counter: Int) {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, HomeWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val views = RemoteViews(packageName, R.layout.widget_home)
        views.setTextViewText(R.id.tv_counter, counter.toString())
        manager.updateAppWidget(ids, views)
    }

    companion object {
        private const val OVERLAY_CHANNEL = "overlay_permission"
        private const val COUNTER_CHANNEL = "counter"
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_COUNTER = "counter"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
    }
}
