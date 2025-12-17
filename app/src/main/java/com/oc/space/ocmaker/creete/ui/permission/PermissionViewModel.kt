package com.oc.space.ocmaker.creete.ui.permission

import androidx.lifecycle.ViewModel
import com.oc.space.ocmaker.creete.core.helper.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionViewModel : ViewModel() {

    private val _storageGranted = MutableStateFlow(false)
    val storageGranted: StateFlow<Boolean> = _storageGranted

    private val _notificationGranted = MutableStateFlow(false)
    val notificationGranted: StateFlow<Boolean> = _notificationGranted

    // In-memory counters (reset when app closes)
    private var storagePermissionCount = 0
    private var notificationPermissionCount = 0

    fun updateStorageGranted(granted: Boolean) {
        _storageGranted.value = granted
        storagePermissionCount = if (granted) 0 else storagePermissionCount + 1
    }

    fun updateNotificationGranted(granted: Boolean) {
        _notificationGranted.value = granted
        notificationPermissionCount = if (granted) 0 else notificationPermissionCount + 1
    }

    fun needGoToSettings(storage: Boolean): Boolean {
        return if (storage) {
            storagePermissionCount > 2 && !_storageGranted.value
        } else {
            notificationPermissionCount > 2 && !_notificationGranted.value
        }
    }

    fun getStoragePermissions() = PermissionHelper.storagePermission
    fun getNotificationPermissions() = PermissionHelper.notificationPermission
}