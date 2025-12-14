package com.oc.space.ocmaker.creete.ui.my_creation

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.room.util.findColumnIndexBySuffix
import com.lvt.ads.util.Admob
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseActivity
import com.oc.space.ocmaker.creete.core.extensions.checkPermissions
import com.oc.space.ocmaker.creete.core.extensions.goToSettings
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.hideNavigation
import com.oc.space.ocmaker.creete.core.extensions.invisible
import com.oc.space.ocmaker.creete.core.extensions.loadNativeCollabAds
import com.oc.space.ocmaker.creete.core.extensions.requestPermission
import com.oc.space.ocmaker.creete.core.extensions.select
import com.oc.space.ocmaker.creete.core.extensions.setImageActionBar
import com.oc.space.ocmaker.creete.core.extensions.setTextActionBar
import com.oc.space.ocmaker.creete.core.extensions.tap

import com.oc.space.ocmaker.creete.core.extensions.startIntentWithClearTop
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.helper.LanguageHelper
import com.oc.space.ocmaker.creete.core.helper.UnitHelper
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.key.RequestKey
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import com.oc.space.ocmaker.creete.core.utils.share.whatsapp.WhatsappSharingActivity
import com.oc.space.ocmaker.creete.core.utils.state.HandleState
import com.oc.space.ocmaker.creete.databinding.ActivityAlbumBinding
import com.oc.space.ocmaker.creete.dialog.YesNoDialog
import com.oc.space.ocmaker.creete.ui.home.HomeActivity
import com.oc.space.ocmaker.creete.ui.view.ViewActivity
import com.oc.space.ocmaker.creete.databinding.PopupMyAlbumBinding
import com.oc.space.ocmaker.creete.dialog.CreateNameDialog
import com.oc.space.ocmaker.creete.ui.my_creation.adapter.MyAvatarAdapter
import com.oc.space.ocmaker.creete.ui.my_creation.adapter.TypeAdapter
import com.oc.space.ocmaker.creete.ui.my_creation.fragment.MyAvatarFragment
import com.oc.space.ocmaker.creete.ui.my_creation.fragment.MyDesignFragment
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyAvatarViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyCreationViewModel
import com.oc.space.ocmaker.creete.ui.permission.PermissionViewModel
import kotlinx.coroutines.launch
import kotlin.text.replace

class MyCreationActivity : WhatsappSharingActivity<ActivityAlbumBinding>() {
    private val viewModel: MyCreationViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var myAvatarFragment: MyAvatarFragment? = null
    private var myDesignFragment: MyDesignFragment? = null
    private var isInSelectionMode = false
    private var isAllSelected = false

    override fun setViewBinding(): ActivityAlbumBinding {
        return ActivityAlbumBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.setTypeStatus(ValueKey.AVATAR_TYPE)
        viewModel.setStatusFrom(intent.getBooleanExtra(IntentKey.FROM_SAVE, false))

        // Hide action bar buttons by default (only show in selection mode)
        binding.actionBar.apply {
            btnActionBarRight.gone()
            btnActionBarNextRight.gone()
        }
    }

    override fun dataObservable() {
        binding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    launch {
                        viewModel.typeStatus.collect { type ->
                            if (type != -1) {
                                if (type == ValueKey.AVATAR_TYPE) {
                                    // MyAvatar selected
                                    setupSelectedTab(btnMyPixel, tvSpace, imvFocusMyAvatar, subTabMyAvatar, isLeftTab = true)
                                    setupUnselectedTab(btnMyDesign, tvMyDesign, imvFocusMyDesign, subTabMyDesign, isLeftTab = false)
                                    showFragment(ValueKey.AVATAR_TYPE)
                                } else {
                                    // MyDesign selected
                                    setupSelectedTab(btnMyDesign, tvMyDesign, imvFocusMyDesign, subTabMyDesign, isLeftTab = false)
                                    setupUnselectedTab(btnMyPixel, tvSpace, imvFocusMyAvatar, subTabMyAvatar, isLeftTab = true)
                                    showFragment(ValueKey.MY_DESIGN_TYPE)
                                }
                                // Update bottom buttons visibility when tab changes
                                updateBottomButtonsVisibility()
                            }

                        }
                    }
                    launch {
                        viewModel.downloadState.collect { state ->
                            when (state) {
                                HandleState.LOADING -> {
                                    showLoading()
                                }

                                HandleState.SUCCESS -> {
                                    dismissLoading()
                                    hideNavigation()
                                    showToast(R.string.download_success)
                                }

                                else -> {
                                    dismissLoading()
                                    hideNavigation()
                                    showToast(R.string.download_failed_please_try_again_later)
                                }
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
                btnActionBarLeft.tap {
                    if (isInSelectionMode) {
                        // Exit selection mode
                        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
                        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

                        when {
                            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> {
                                avatarFragment.resetSelectionMode()
                            }
                            designFragment is MyDesignFragment && designFragment.isVisible -> {
                                designFragment.resetSelectionMode()
                            }
                        }
                    } else {
                        startIntentWithClearTop(HomeActivity::class.java)
                    }
                }

                // Select All button
                btnActionBarRight.tap {
                    handleSelectAllFromCurrentFragment()
                }

                // Delete All button
                btnActionBarNextRight.tap {
                    handleDeleteSelectedFromCurrentFragment()
                }
            }

            btnMyPixel.tap { viewModel.setTypeStatus(ValueKey.AVATAR_TYPE) }
            btnMyDesign.tap { viewModel.setTypeStatus(ValueKey.MY_DESIGN_TYPE) }

            // WhatsApp, Telegram, and Download buttons in lnlBottom
            val layoutBottom = lnlBottom.getChildAt(0)
            layoutBottom.findViewById<View>(R.id.btnWhatsapp)?.tap(2500) {
                val selectedPaths = getSelectedPathsFromCurrentFragment()
                handleAddToWhatsApp(selectedPaths)
            }
            layoutBottom.findViewById<View>(R.id.btnTelegram)?.tap(2500) {
                val selectedPaths = getSelectedPathsFromCurrentFragment()
                handleAddToTelegram(selectedPaths)
            }
            layoutBottom.findViewById<View>(R.id.btnDownload)?.tap(2500) {
                handleDownloadFromCurrentFragment()
            }

            // Delete button in deleteSection         }
        }
    }

    private fun handleShareFromCurrentFragment() {
        val selectedPaths = getSelectedPathsFromCurrentFragment()
        handleShare(selectedPaths)
    }

    private fun handleDownloadFromCurrentFragment() {
        val selectedPaths = getSelectedPathsFromCurrentFragment()
        handleDownload(selectedPaths)
    }

    private fun handleSelectAllFromCurrentFragment() {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

        when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> {
                if (isAllSelected) {
                    // Deselect all
                    avatarFragment.deselectAllItems()
                    isAllSelected = false
                    binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
                } else {
                    // Select all
                    avatarFragment.selectAllItems()
                    isAllSelected = true
                    binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_select_all)
                }
            }
            designFragment is MyDesignFragment && designFragment.isVisible -> {
                if (isAllSelected) {
                    // Deselect all
                    designFragment.deselectAllItems()
                    isAllSelected = false
                    binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
                } else {
                    // Select all
                    designFragment.selectAllItems()
                    isAllSelected = true
                    binding.actionBar.btnActionBarRight.setImageResource(R.drawable.ic_select_all)
                }
            }
        }
    }

    private fun handleDeleteSelectedFromCurrentFragment() {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

        when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> {
                avatarFragment.deleteSelectedItems()
            }
            designFragment is MyDesignFragment && designFragment.isVisible -> {
                designFragment.deleteSelectedItems()
            }
        }
    }

    private fun getSelectedPathsFromCurrentFragment(): ArrayList<String> {
        val avatarFragment = supportFragmentManager.findFragmentByTag("MyAvatarFragment")
        val designFragment = supportFragmentManager.findFragmentByTag("MyDesignFragment")

        return when {
            avatarFragment is MyAvatarFragment && avatarFragment.isVisible -> avatarFragment.getSelectedPaths()
            designFragment is MyDesignFragment && designFragment.isVisible -> designFragment.getSelectedPaths()
            else -> arrayListOf()
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.my_pixel))

            // Select All button (btnActionBarRight) - hidden initially, only shown in selection mode
            btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
            btnActionBarRight.gone()

            // Delete All button - hidden initially, only shown in selection mode
            btnActionBarNextRight.setImageResource(R.drawable.ic_delete_all)
            btnActionBarNextRight.gone()
        }
    }

    override fun initText() {
        binding.apply {
            tvSpace.select()
            tvMyDesign.select()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                showToast(R.string.granted_storage)
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    fun handleShare(list: ArrayList<String>) {
        if (list.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }
        viewModel.shareImages(this, list)
    }

    fun handleAddToTelegram(list: ArrayList<String>) {
        if (list.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }
        viewModel.addToTelegram(this, list)
    }

    fun handleAddToWhatsApp(list: ArrayList<String>) {
        if (list.size < 3) {
            showToast(R.string.limit_3_items)
            return
        }
        if (list.size > 30) {
            showToast(R.string.limit_30_items)
            return
        }

        val dialog = CreateNameDialog(this)
        LanguageHelper.setLocale(this)
        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onNoClick = {
            dismissDialog()
        }
        dialog.onDismissClick = {
            dismissDialog()
        }

        dialog.onYesClick = { packageName ->
            dismissDialog()
            viewModel.addToWhatsapp(this, packageName, list) { stickerPack ->
                if (stickerPack != null) {
                    addToWhatsapp(stickerPack)
                }
            }
        }
    }

    fun handleDownload(list: ArrayList<String>) {
        if (list.isEmpty()) {
            showToast(R.string.please_select_an_image)
            return
        }
        viewModel.downloadFiles(this, list)
    }

    private fun showFragment(type: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        // Initialize fragments if null
        if (myAvatarFragment == null) {
            myAvatarFragment = MyAvatarFragment()
            transaction.add(R.id.frmList, myAvatarFragment!!, "MyAvatarFragment")
        }
        if (myDesignFragment == null) {
            myDesignFragment = MyDesignFragment()
            transaction.add(R.id.frmList, myDesignFragment!!, "MyDesignFragment")
        }

        // Show/Hide based on type
        if (type == ValueKey.AVATAR_TYPE) {
            myAvatarFragment?.let { transaction.show(it) }
            myDesignFragment?.let { transaction.hide(it) }
        } else {
            myAvatarFragment?.let { transaction.hide(it) }
            myDesignFragment?.let { transaction.show(it) }
        }

        transaction.commit()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        startIntentWithClearTop(HomeActivity::class.java)
    }

//    fun initNativeCollab() {
//        loadNativeCollabAds(R.string.native_cl_myCharactor, binding.flNativeCollab, binding.lnlBottom)
//    }
//    override fun initAds() {
//        initNativeCollab()
//        Admob.getInstance().loadNativeAd(
//            this,
//            getString(R.string.native_myCharactor),
//            binding.nativeAds,
//            R.layout.ads_native_banner
//        )
//    }

    override fun onRestart() {
        super.onRestart()
       // initNativeCollab()
    }

    fun enterSelectionMode() {
        isInSelectionMode = true
        isAllSelected = false
        binding.actionBar.apply {
            // Show select all and delete all buttons
            btnActionBarRight.setImageResource(R.drawable.ic_not_select_all)
            btnActionBarRight.visible()
            btnActionBarNextRight.visible()
        }
        updateBottomButtonsVisibility()
        android.util.Log.d("MyCreationActivity", "enterSelectionMode called - showing buttons")
    }

    fun exitSelectionMode() {
        isInSelectionMode = false
        isAllSelected = false
        binding.actionBar.apply {
            // Hide select all and delete all buttons
            btnActionBarRight.gone()
            btnActionBarNextRight.gone()
        }
        updateBottomButtonsVisibility()
        android.util.Log.d("MyCreationActivity", "exitSelectionMode called - hiding buttons")
    }

    private fun updateBottomButtonsVisibility() {
        val layoutBottom = binding.lnlBottom.getChildAt(0)
        val btnWhatsapp = layoutBottom.findViewById<View>(R.id.btnWhatsapp)
        val btnTelegram = layoutBottom.findViewById<View>(R.id.btnTelegram)
        val btnDownload = layoutBottom.findViewById<View>(R.id.btnDownload)

        if (isInSelectionMode && viewModel.typeStatus.value == ValueKey.MY_DESIGN_TYPE) {
            // In My Design tab selection mode: show only Download button
            btnWhatsapp?.gone()
            btnTelegram?.gone()
            btnDownload?.visible()
        } else {
            // In My Pixel tab or not in selection mode: show WhatsApp and Telegram, hide Download
            btnWhatsapp?.visible()
            btnTelegram?.visible()
            btnDownload?.gone()
        }
    }

    private fun setupSelectedTab(
        tabView: View,
        textView: android.widget.TextView,
        focusImage: android.widget.ImageView,
        subTab: View,
        isLeftTab: Boolean
    ) {
        // Set weight = 1.6
        val params = tabView.layoutParams as android.widget.LinearLayout.LayoutParams
        params.weight = 1.0f
        params.topMargin = 0
        tabView.layoutParams = params

        // Set text size = 20sp
        textView.textSize = 16f

        // Apply gradient color from top to bottom (using fixed height based on text size)
        val textHeight = textView.lineHeight.toFloat()
        textView.post {
            val textHeight = textView.lineHeight.toFloat()
            val shader = LinearGradient(
                0f, 0f, 0f, textHeight,
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FFFFFF"),
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
        }

        // Show selected_tab drawable
        focusImage.setImageResource(R.drawable.selected_tab_album)
        // Flip horizontally if on right side
        focusImage.scaleX = if (isLeftTab) 1f else -1f
        focusImage.visible()

        // Hide subTab
        subTab.gone()
    }

    private fun setupUnselectedTab(
        tabView: View,
        textView: android.widget.TextView,
        focusImage: android.widget.ImageView,
        subTab: View,
        isLeftTab: Boolean
    ) {
        // Set weight = 1
        val params = tabView.layoutParams as android.widget.LinearLayout.LayoutParams
        params.weight = 1f
        params.topMargin = 0
        tabView.layoutParams = params

        // Set text size = 16sp, color = colorPrimary
        textView.textSize = 16f
        // Remove gradient shader and set solid color
        textView.paint.shader = null
        textView.post {
            val textHeight = textView.lineHeight.toFloat()
            val shader = LinearGradient(
                0f, 0f, 0f, textHeight,
                Color.parseColor("#01579B"),
                Color.parseColor("#01579B"),
                Shader.TileMode.CLAMP
            )
            textView.paint.shader = shader
            textView.invalidate()
        }

        // Show un_selected_tab drawable
        focusImage.setImageResource(R.drawable.un_selected_tab_album)
        // Flip horizontally if on left side
        focusImage.scaleX = if (isLeftTab) -1f else 1f
        focusImage.visible()

        // Show subTab
        subTab.gone()
    }
}