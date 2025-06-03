package com.example.doit2.utils

object Constants {

    // 資料庫相關
    const val DATABASE_NAME = "doit_database"
    const val DATABASE_VERSION = 1

    // 介面相關
    const val TASK_ITEM_ANIMATION_DURATION = 300L
    const val TASK_SWIPE_THRESHOLD = 0.5f

    // 任務相關
    const val MAX_TASK_TITLE_LENGTH = 100
    const val MAX_TASK_DESCRIPTION_LENGTH = 500

    // 偏好設定
    const val PREFS_NAME = "doit_preferences"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_DARK_MODE = "dark_mode"

    // 請求代碼
    const val REQUEST_CODE_ADD_TASK = 1001
    const val REQUEST_CODE_EDIT_TASK = 1002
}