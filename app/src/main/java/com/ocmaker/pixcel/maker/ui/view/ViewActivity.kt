package com.ocmaker.pixcel.maker.ui.view

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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lvt.ads.util.Admob
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseActivity
import com.ocmaker.pixcel.maker.core.extensions.checkPermissions
import com.ocmaker.pixcel.maker.core.extensions.goToSettings
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.handleBackLeftToRight
import com.ocmaker.pixcel.maker.core.extensions.hideNavigation
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.loadNativeCollabAds
import com.ocmaker.pixcel.maker.core.extensions.requestPermission
import com.ocmaker.pixcel.maker.core.extensions.select
import com.ocmaker.pixcel.maker.core.extensions.setImageActionBar
import com.ocmaker.pixcel.maker.core.extensions.setTextActionBar
import com.ocmaker.pixcel.maker.core.extensions.showInterAll
import com.ocmaker.pixcel.maker.core.extensions.tap

import com.ocmaker.pixcel.maker.core.extensions.startIntentWithClearTop
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.core.helper.LanguageHelper
import com.ocmaker.pixcel.maker.core.utils.key.IntentKey
import com.ocmaker.pixcel.maker.core.utils.key.RequestKey
import com.ocmaker.pixcel.maker.core.utils.key.ValueKey
import com.ocmaker.pixcel.maker.core.utils.state.HandleState
import com.ocmaker.pixcel.maker.databinding.ActivityViewBinding
import com.ocmaker.pixcel.maker.dialog.YesNoDialog
import com.ocmaker.pixcel.maker.ui.home.HomeActivity
import com.ocmaker.pixcel.maker.ui.my_creation.MyCreationActivity
import com.ocmaker.pixcel.maker.core.extensions.startIntentRightToLeft
import com.ocmaker.pixcel.maker.core.extensions.strings
import com.ocmaker.pixcel.maker.core.helper.UnitHelper
import com.ocmaker.pixcel.maker.ui.customize.CustomizeCharacterActivity
import com.ocmaker.pixcel.maker.ui.home.DataViewModel
import com.ocmaker.pixcel.maker.ui.my_creation.view_model.MyAvatarViewModel
import com.ocmaker.pixcel.maker.ui.permission.PermissionViewModel
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

        // Set bg_btn_bottom for both buttons
        setButtonBackgrounds()
    }

    private fun setButtonBackgrounds() {
        binding.includeLayoutBottom.apply {
            // Left button (Whatsapp)
            btnWhatsapp.setBackgroundResource(R.drawable.bg_btn_bottom)
            btnWhatsapp.setPadding(0, 0, 0, 0)
            val paramsLeft = btnWhatsapp.layoutParams as? androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
            paramsLeft?.apply {
                height = UnitHelper.dpToPx(this@ViewActivity, 51f).toInt()
                marginEnd = UnitHelper.dpToPx(this@ViewActivity, 14f).toInt()
                marginStart = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                btnWhatsapp.layoutParams = this
            }
            // Hide the CardView with rounded corners
            val cardViewLeft = btnWhatsapp.getChildAt(0) as? androidx.cardview.widget.CardView
            cardViewLeft?.gone()
            // Hide WhatsApp icon
            val lnlInLeft = btnWhatsapp.getChildAt(1) as? ViewGroup
            lnlInLeft?.getChildAt(0)?.gone()

            // Update tvWhatsapp text properties
            tvWhatsapp.textSize = 16f
            tvWhatsapp.setTypeface(ResourcesCompat.getFont(this@ViewActivity, R.font.pixelifysans_medium))

            // Right button (Telegram)
            btnTelegram.setBackgroundResource(R.drawable.bg_btn_bottom)
            btnTelegram.setPadding(0, 0, 0, 0)
            val paramsRight = btnTelegram.layoutParams as? androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
            paramsRight?.apply {
                height = UnitHelper.dpToPx(this@ViewActivity, 51f).toInt()
                marginStart = UnitHelper.dpToPx(this@ViewActivity, 14f).toInt()
                marginEnd = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                btnTelegram.layoutParams = this
            }
            // Hide the CardView with rounded corners
            val cardViewRight = btnTelegram.getChildAt(0) as? androidx.cardview.widget.CardView
            cardViewRight?.gone()
            // Hide Telegram icon
            val lnlInRight = btnTelegram.getChildAt(1) as? ViewGroup
            lnlInRight?.getChildAt(0)?.gone()

            tvTelegram.textSize = 16f
            tvTelegram.setTypeface(ResourcesCompat.getFont(this@ViewActivity, R.font.pixelifysans_medium))

            // Hide download button
            btnDownload.gone()
        }
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
                btnActionBarNextRight.tap { handleEditClick(viewModel.pathInternal.value) }
                btnShare.tap(2500) { viewModel.shareFiles(this@ViewActivity) }
            }

            // Access buttons from included layout_bottom
            includeLayoutBottom.btnWhatsapp.tap(2590) { handleBottomBarLeft() }
            includeLayoutBottom.btnTelegram.tap(2000) { handleBottomBarRight() }
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
                setTextActionBar(tvCenter, getString(R.string.my_pixel))

                // Hide delete icon when coming from design section
                if (viewModel.statusFrom == ValueKey.MY_DESIGN_TYPE) {
                    btnActionBarRight.gone()
                } else {
                    setImageActionBar(btnActionBarRight, R.drawable.ic_delete)
                    btnActionBarRight.visible()
                }

                setImageActionBar(btnActionBarNextRight, R.drawable.ic_edit_view)
                btnActionBarNextRight.visible()
                // Hide btnShare in view mode
                btnShare.gone()

            }
//            cvImage.apply {
//                radius = 16f
//                strokeWidth = 2
//                strokeColor = getColor(R.color.red_BA)
//            }

            val params = cvImage.layoutParams as ConstraintLayout.LayoutParams
            cvImage.layoutParams = params

            tvSuccess.gone()

            includeLayoutBottom.tvWhatsapp.text = strings(R.string.share)
            includeLayoutBottom.tvWhatsapp.select()

            includeLayoutBottom.tvTelegram.text = strings(R.string.download)
            includeLayoutBottom.tvTelegram.select()
        }
    }

    private fun setUpSuccessUI() {
        binding.apply {
            actionBar.apply {
                // Hide center text and imgCenter
                tvCenter.visible()
                tvCenter.setText(getString(R.string.successfully))
                imgCenter.gone()

                // Hide left and next right buttons
                btnActionBarLeft.visible()
                btnActionBarNextRight.gone()

                // Show and configure btnShare as home button
                btnShare.visible()
                btnShare.setImageResource(R.drawable.ic_home_ss)
                btnShare.setOnClickListener(null) // Clear any existing listeners
                btnShare.tap {
                    showInterAll {
                        startIntentWithClearTop(HomeActivity::class.java)
                    }
                }
            }

            val params = cvImage.layoutParams as ConstraintLayout.LayoutParams
            cvImage.layoutParams = params

            tvSuccess.visible()

            includeLayoutBottom.tvWhatsapp.text = strings(R.string.my_album)
            includeLayoutBottom.tvWhatsapp.select()

            includeLayoutBottom.tvTelegram.text = strings(R.string.download)
            includeLayoutBottom.tvTelegram.select()
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
