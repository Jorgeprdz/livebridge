package com.appsfolder.livebridge.liveupdate

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log

/**
 * Dispatch source PendingIntent from a notification-launched Activity.
 * A successful send does not guarantee that a remote UI is visible.
 */
class MirrorTapActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) dispatchTap(intent)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    private fun dispatchTap(tap: Intent) {
        val sourceKey = tap.getStringExtra(EXTRA_SOURCE_KEY)?.takeIf { it.isNotBlank() } ?: return
        val packageName = tap.getStringExtra(EXTRA_SOURCE_PACKAGE)?.takeIf { it.isNotBlank() }
        val sourceIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            tap.getParcelableExtra(EXTRA_SOURCE_CONTENT_INTENT, PendingIntent::class.java)
        } else {
            @Suppress("DEPRECATION")
            tap.getParcelableExtra(EXTRA_SOURCE_CONTENT_INTENT)
        }

        val dispatched = if (sourceIntent?.isActivity == true) {
            try {
                sourceIntent.send()
                true
            } catch (error: PendingIntent.CanceledException) {
                Log.w(TAG, "Source PendingIntent expired; trying launcher: $sourceKey", error)
                openLauncher(packageName)
            } catch (error: RuntimeException) {
                Log.w(TAG, "Source PendingIntent dispatch failed: $sourceKey", error)
                false
            }
        } else {
            // A broadcast/service PendingIntent does not guarantee an Activity opens.
            openLauncher(packageName)
        }
        if (dispatched) {
            LiveUpdateNotifier.handleMirroredContentTap(this, sourceKey)
        } else {
            Log.w(TAG, "Keeping mirror because launch failed: $sourceKey")
        }
    }

    private fun openLauncher(sourcePackage: String?): Boolean {
        if (sourcePackage == null) return false
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(sourcePackage) ?: return false
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            true
        } catch (error: RuntimeException) {
            Log.w(TAG, "Launcher fallback failed for $sourcePackage", error)
            false
        }
    }

    companion object {
        const val EXTRA_SOURCE_KEY = "source_key"
        const val EXTRA_SOURCE_PACKAGE = "source_package"
        const val EXTRA_SOURCE_CONTENT_INTENT = "source_content_intent"
        private const val TAG = "LiveBridgeMirrorTap"
    }
}
