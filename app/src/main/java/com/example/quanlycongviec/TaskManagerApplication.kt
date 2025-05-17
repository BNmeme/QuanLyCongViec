package com.example.quanlycongviec

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class TaskManagerApplication : Application() {

    companion object {
        private const val PREFS_NAME = "task_manager_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"

        private var instance: TaskManagerApplication? = null

        fun getInstance(): TaskManagerApplication? {
            return instance
        }

        fun isDarkModeEnabled(): Boolean {
            try {
                val app = getInstance()
                if (app != null) {
                    val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    return prefs.getBoolean(KEY_DARK_MODE, false)
                }
            } catch (e: Exception) {
                Log.e("TaskManagerApp", "Error getting dark mode preference: ${e.message}")
            }
            return false
        }

        fun setDarkModeEnabled(enabled: Boolean) {
            try {
                val app = getInstance()
                if (app != null) {
                    val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
                }
            } catch (e: Exception) {
                Log.e("TaskManagerApp", "Error setting dark mode preference: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
