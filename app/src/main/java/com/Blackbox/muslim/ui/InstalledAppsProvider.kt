package com.Blackbox.muslim.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledApp(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean
)

class InstalledAppsProvider(private val context: Context) {

    fun getInstalledApps(callback: (List<InstalledApp>) -> Unit) {
        Thread {
            try {
                val pm = context.packageManager
                val packages = pm.getInstalledPackages(0)

                val apps = packages
                    .filter { it.packageName != context.packageName }
                    .map { pkgInfo ->
        val appInfo = pkgInfo.applicationInfo!!
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val name = appInfo.loadLabel(pm).toString()
        val icon = try { appInfo.loadIcon(pm) } catch (e: Exception) { null }
                        InstalledApp(
                            name = name,
                            packageName = pkgInfo.packageName,
                            icon = icon,
                            isSystemApp = isSystemApp
                        )
                    }
                    .filter { app ->
                        !app.isSystemApp ||
                        app.packageName.startsWith("com.whatsapp") ||
                        app.packageName.startsWith("com.google.android.apps.messaging")
                    }
                    .sortedWith(compareByDescending<InstalledApp> { !it.isSystemApp }.thenBy { it.name })

                callback(apps)
            } catch (e: Exception) {
                callback(emptyList())
            }
        }.start()
    }

    fun getInstalledAppsGrouped(callback: (Map<String, List<InstalledApp>>) -> Unit) {
        getInstalledApps { apps ->
            callback(apps.groupBy { app ->
                app.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
            })
        }
    }
}
