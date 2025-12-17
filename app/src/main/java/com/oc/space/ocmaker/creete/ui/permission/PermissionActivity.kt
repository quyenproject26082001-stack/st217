package com.oc.space.ocmaker.creete.ui.permission

import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseActivity
import com.oc.space.ocmaker.creete.core.extensions.checkPermissions
import com.oc.space.ocmaker.creete.core.extensions.goToSettings
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.requestPermission
import com.oc.space.ocmaker.creete.core.extensions.select

import com.oc.space.ocmaker.creete.core.extensions.startIntentRightToLeft
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.helper.StringHelper
import com.oc.space.ocmaker.creete.core.utils.key.RequestKey
import com.oc.space.ocmaker.creete.databinding.ActivityPermissionBinding
import com.oc.space.ocmaker.creete.ui.home.HomeActivity
import com.oc.space.ocmaker.creete.core.extensions.setGradientTextHeightColor
import com.oc.space.ocmaker.creete.core.extensions.tap
import kotlinx.coroutines.launch

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    private val viewModel: PermissionViewModel by viewModels()

    private var inter: InterstitialAd? = null

    override fun setViewBinding() = ActivityPermissionBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.btnStorage.visible()
            binding.btnNotification.gone()
        } else {
            binding.btnNotification.visible()
            binding.btnStorage.gone()
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
        val textRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.string.to_access_13 else R.string.to_access

        binding.txtPer.text = TextUtils.concat(
            createColoredText(R.string.allow, R.color.white),
            " ",
            createColoredText(R.string.app_name, R.color.white),
            " ",
            createColoredText(textRes, R.color.white)
        )
    }

    override fun viewListener() {
        binding.swPermission.tap { handlePermissionRequest(isStorage = true) }
        binding.swNotification.tap { handlePermissionRequest(isStorage = false) }
        binding.tvContinue.tap(1500) { handleContinue() }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.storageGranted.collect { granted ->
                        updatePermissionUI(granted, true)
                    }
                }

                launch {
                    viewModel.notificationGranted.collect { granted ->
                        updatePermissionUI(granted, false)
                    }
                }
            }
        }
    }

    private fun handlePermissionRequest(isStorage: Boolean) {
        val perms = if (isStorage) viewModel.getStoragePermissions() else viewModel.getNotificationPermissions()
        if (checkPermissions(perms)) {
            showToast(if (isStorage) R.string.granted_storage else R.string.granted_notification)
        } else if (viewModel.needGoToSettings(sharePreference, isStorage)) {
            goToSettings()
        } else {
            val requestCode = if (isStorage) RequestKey.STORAGE_PERMISSION_CODE else RequestKey.NOTIFICATION_PERMISSION_CODE
            requestPermission(perms, requestCode)
        }
    }

    private fun updatePermissionUI(granted: Boolean, isStorage: Boolean) {
        val imageView = if (isStorage) binding.swPermission else binding.swNotification
        imageView.setImageResource(if (granted) R.drawable.ic_sw_on else R.drawable.ic_sw_off)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.STORAGE_PERMISSION_CODE -> viewModel.updateStorageGranted(sharePreference, granted)

            RequestKey.NOTIFICATION_PERMISSION_CODE -> viewModel.updateNotificationGranted(sharePreference, granted)
        }
        if (granted) {
            showToast(if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) R.string.granted_storage else R.string.granted_notification)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateStorageGranted(
            sharePreference, checkPermissions(viewModel.getStoragePermissions())
        )
        viewModel.updateNotificationGranted(
            sharePreference, checkPermissions(viewModel.getNotificationPermissions())
        )
    }


    override fun initActionBar() {
        binding.actionBar.tvCenter.apply {
            text = getString(R.string.permission)
            visible()
        }
    }

    private fun createColoredText(
        @androidx.annotation.StringRes textRes: Int,
        @androidx.annotation.ColorRes colorRes: Int,
        font: Int = R.font.londrina_solid_regular
    ) = StringHelper.changeColor(this, getString(textRes), colorRes, font)

    private fun handleContinue() {
        Admob.getInstance().showInterAds(this@PermissionActivity, inter, object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                sharePreference.setIsFirstPermission(false)
                startIntentRightToLeft(HomeActivity::class.java)
                finishAffinity()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Reset permission counters to 0 when exiting the permission activity
        sharePreference.setStoragePermission(0)
        sharePreference.setNotificationPermission(0)
    }

//    override fun initAds() {
//        Admob.getInstance().loadInterAds(
//            this@PermissionActivity, getString(R.string.inter_per), object : InterCallback() {
//                override fun onAdLoadSuccess(interstitialAd: InterstitialAd?) {
//                    super.onAdLoadSuccess(interstitialAd)
//                    inter = interstitialAd
//                }
//            })
//
//        Admob.getInstance().loadNativeAd(
//            this@PermissionActivity,
//            getString(R.string.native_per),
//            binding.nativeAds,
//            R.layout.ads_native_big_btn_bottom
//        )
//    }
}