package com.oc.space.ocmaker.creete.ui.view

import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lvt.ads.util.Admob
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseActivity
import com.oc.space.ocmaker.creete.core.extensions.checkPermissions
import com.oc.space.ocmaker.creete.core.extensions.goToSettings
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.handleBackLeftToRight
import com.oc.space.ocmaker.creete.core.extensions.hideNavigation
import com.oc.space.ocmaker.creete.core.extensions.loadImage
import com.oc.space.ocmaker.creete.core.extensions.loadNativeCollabAds
import com.oc.space.ocmaker.creete.core.extensions.requestPermission
import com.oc.space.ocmaker.creete.core.extensions.select
import com.oc.space.ocmaker.creete.core.extensions.setImageActionBar
import com.oc.space.ocmaker.creete.core.extensions.setTextActionBar
import com.oc.space.ocmaker.creete.core.extensions.showInterAll
import com.oc.space.ocmaker.creete.core.extensions.tap

import com.oc.space.ocmaker.creete.core.extensions.startIntentWithClearTop
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.helper.LanguageHelper
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.key.RequestKey
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import com.oc.space.ocmaker.creete.core.utils.state.HandleState
import com.oc.space.ocmaker.creete.databinding.ActivityViewBinding
import com.oc.space.ocmaker.creete.dialog.YesNoDialog
import com.oc.space.ocmaker.creete.ui.home.HomeActivity
import com.oc.space.ocmaker.creete.ui.my_creation.MyCreationActivity
import com.oc.space.ocmaker.creete.core.extensions.startIntentRightToLeft
import com.oc.space.ocmaker.creete.core.extensions.strings
import com.oc.space.ocmaker.creete.core.helper.UnitHelper
import com.oc.space.ocmaker.creete.ui.customize.CustomizeCharacterActivity
import com.oc.space.ocmaker.creete.ui.home.DataViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyAvatarViewModel
import com.oc.space.ocmaker.creete.ui.permission.PermissionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val viewModel: ViewViewModel by viewModels()
    private val myAvatarViewModel: MyAvatarViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        dataViewModel.ensureData(this)
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY)!!)
        viewModel.updateStatusFrom(intent.getIntExtra(IntentKey.STATUS_KEY, ValueKey.AVATAR_TYPE))
        viewModel.setType(intent.getIntExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW))
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pathInternal.collect { path ->
                        loadImage(this@ViewActivity, path, binding.imvImage)
                    }
                }
                launch {
                    viewModel.typeUI.collect { type ->
                        if (type != -1) {
                            when (type) {
                                ValueKey.TYPE_VIEW -> setUpViewUI()
                                else -> setUpSuccessUI()
                            }
                        }
                    }
                }
            }

        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { handleBackLeftToRight() }
                btnActionBarRight.tap { handleActionBarRight() }
               // btnActionBarNextToRight.tap { handleEditClick(viewModel.pathInternal.value) }
                btnShare.tap(2500) { viewModel.shareFiles(this@ViewActivity) }
            }

            // Access buttons from included layout_bottom
            includeLayoutBottom.btnBottomLeft.tap(2590) { handleBottomBarLeft() }
            includeLayoutBottom.btnBottomRight.tap(2000) { handleBottomBarRight() }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
        }
    }

    private fun setUpViewUI() {
        binding.apply {
          //  loadNativeCollabAds(R.string.native_cl_detail, binding.flNativeCollab, lnlBottom, bottomFailed = 150, bottomLoadSuccess = 82)

          //  nativeAds.visible()
          //  flNativeCollab.gone()

            actionBar.apply {
               // setImageActionBar(btnActionBarRight, R.drawable.ic_delete_view)
               // setImageActionBar(btnActionBarNextToRight, R.drawable.ic_edit_2)
                setTextActionBar(tvCenter, getString(R.string.my_character_in))
                setImageActionBar(btnActionBarRight,R.drawable.delete_select)
                // Hide btnShare in view mode
                btnShare.gone()
            }
//            cvImage.apply {
//                radius = 16f
//                strokeWidth = 2
//                strokeColor = getColor(R.color.red_BA)
//            }

            val params = cvImage.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "1:1"
            cvImage.layoutParams = params

            tvSuccess.visible()

            includeLayoutBottom.tvBottomLeft.text = strings(R.string.share)
            includeLayoutBottom.tvBottomLeft.select()

            includeLayoutBottom.tvBottomRight.text = strings(R.string.download)
            includeLayoutBottom.tvBottomRight.select()
        }
    }

    private fun setUpSuccessUI() {
        binding.apply {
//            Admob.getInstance().loadNativeAd(
//                this@ViewActivity,
//                getString(R.string.native_success),
//                binding.nativeAds,
//                R.layout.ads_native_big_btn_top
//            )

//            nativeAds.visible()
//            flNativeCollab.gone()

            actionBar.apply {
               // setImageActionBar(btnActionBarRight, R.drawable.ic_home)
               // setTextActionBar(tvCenter, getString(R.string.successfully))
                // Show btnShare in action bar for success mode
                tvCenter.gone()
                btnShare.visible()

                // Show imgCenter with home icon
                imgCenter.visible()
                imgCenter.setImageResource(R.drawable.ic_home_ss)
                imgCenter.tap { showInterAll { startIntentWithClearTop(HomeActivity::class.java) } }
            }

//            cvImage.apply {
//                radius = 20f
//                strokeWidth = 8
//                strokeColor = getColor(R.color.white)
//            }

            val params = cvImage.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "1:1"
            cvImage.layoutParams = params

            tvSuccess.visible()

            includeLayoutBottom.tvBottomLeft.text = strings(R.string.my_work)
            includeLayoutBottom.tvBottomLeft.select()

            includeLayoutBottom.tvBottomRight.text = strings(R.string.download)
            includeLayoutBottom.tvBottomRight.select()
        }
    }

    private fun handleActionBarRight() {
        when (viewModel.typeUI.value) {
            ValueKey.TYPE_VIEW -> {
                handleDelete()
            }

            else -> {
                showInterAll{ startIntentWithClearTop(HomeActivity::class.java) }
            }
        }
    }

    private fun handleBottomBarLeft() {
        when (viewModel.typeUI.value) {
            ValueKey.TYPE_VIEW -> {
                viewModel.shareFiles(this@ViewActivity)
            }

            else -> {
                showInterAll{ startIntentRightToLeft(MyCreationActivity::class.java, true) }
            }
        }
    }

    private fun handleBottomBarRight() {
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
        lifecycleScope.launch {
            viewModel.downloadFiles(this@ViewActivity).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()
                        showToast(R.string.download_success)
                    }

                    else -> {
                        dismissLoading()
                        showToast(R.string.download_failed_please_try_again_later)
                    }

                }
            }
        }

    }

    private fun handleDelete() {
        val dialog = YesNoDialog(this, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        LanguageHelper.setLocale(this)
        dialog.show()
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onYesClick = {
            dialog.dismiss()
            lifecycleScope.launch {
                viewModel.deleteFile(this@ViewActivity, viewModel.pathInternal.value).collect { state ->
                    when (state) {
                        HandleState.LOADING -> showLoading()
                        HandleState.SUCCESS -> {
                            dismissLoading()
                            finish()
                        }

                        else -> {
                            dismissLoading()
                            showToast(R.string.delete_failed_please_try_again)
                        }
                    }
                }
            }
        }
    }

    private fun handleEditClick(pathInternal: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            showLoading()
            myAvatarViewModel.editItem(this@ViewActivity, pathInternal, dataViewModel.allData.value)
            withContext(Dispatchers.Main) {
                dismissLoading()
                myAvatarViewModel.checkDataInternet(this@ViewActivity) {
                    val intent = Intent(this@ViewActivity, CustomizeCharacterActivity::class.java)
                    intent.putExtra(IntentKey.INTENT_KEY, myAvatarViewModel.positionCharacter)
                    intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
                    val option = ActivityOptions.makeCustomAnimation(
                        this@ViewActivity, R.anim.slide_out_left, R.anim.slide_in_right
                    )
                    startActivity(intent, option.toBundle())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

}
