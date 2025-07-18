package com.android.developers.androidify.wear.common

enum class WatchFaceActivationStrategy {
    NO_ACTION_NEEDED,
    CALL_SET_ACTIVE_NO_USER_ACTION,
    FOLLOW_PROMPT_ON_WATCH,
    LONG_PRESS_TO_SET,
    GO_TO_WATCH_SETTINGS;

    companion object {
        fun fromWatchFaceState(
            hasActiveWatchFace: Boolean = false,
            hasGrantedSetActivePermission: Boolean = false,
            canRequestSetActivePermission: Boolean = true,
            hasUsedSetActiveApi: Boolean = false,
        ): WatchFaceActivationStrategy {
            return when {
                hasActiveWatchFace -> NO_ACTION_NEEDED
                hasGrantedSetActivePermission && !hasUsedSetActiveApi -> CALL_SET_ACTIVE_NO_USER_ACTION
                canRequestSetActivePermission && !hasUsedSetActiveApi -> FOLLOW_PROMPT_ON_WATCH
                !canRequestSetActivePermission && !hasGrantedSetActivePermission && !hasUsedSetActiveApi -> GO_TO_WATCH_SETTINGS
                else -> return LONG_PRESS_TO_SET
            }
        }
    }
}