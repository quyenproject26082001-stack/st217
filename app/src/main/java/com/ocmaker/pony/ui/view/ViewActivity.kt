package com.ocmaker.pony.ui.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ocmaker.pony.R
import com.ocmaker.pony.core.base.BaseActivity
import com.ocmaker.pony.core.extensions.checkPermissions
import com.ocmaker.pony.core.extensions.goToSettings
import com.ocmaker.pony.core.extensions.gone
import com.ocmaker.pony.core.extensions.handleBackLeftToRight
import com.ocmaker.pony.core.extensions.hideNavigation
import com.ocmaker.pony.core.extensions.invisible
import com.ocmaker.pony.core.extensions.loadImage
import com.ocmaker.pony.core.extensions.loadImageFromFile
import com.ocmaker.pony.core.extensions.requestPermission
import com.ocmaker.pony.core.extensions.select
import com.ocmaker.pony.core.extensions.setImageActionBar
import com.ocmaker.pony.core.extensions.setTextActionBar
import com.ocmaker.pony.core.extensions.strings
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.core.helper.LanguageHelper
import com.ocmaker.pony.core.helper.UnitHelper
import com.ocmaker.pony.core.utils.key.IntentKey
import com.ocmaker.pony.core.utils.key.RequestKey
import com.ocmaker.pony.core.utils.key.ValueKey
import com.ocmaker.pony.core.utils.state.HandleState
import com.ocmaker.pony.databinding.ActivityViewBinding
import com.ocmaker.pony.dialog.YesNoDialog
import com.ocmaker.pony.ui.customize.CustomizeCharacterActivity
import com.ocmaker.pony.ui.home.DataViewModel
import com.ocmaker.pony.ui.my_creation.fragment.MyAvatarFragment
import com.ocmaker.pony.ui.my_creation.MyCreationActivity
import com.ocmaker.pony.ui.my_creation.view_model.MyAvatarViewModel
import com.ocmaker.pony.ui.permission.PermissionViewModel
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

        setButtonBackgrounds()
        setupUI()
    }

    private fun setButtonBackgrounds() {
        binding.includeLayoutBottom.apply {
            // Left button - Share
            btnWhatsapp.setBackgroundResource(R.drawable.bg_btn_bottom)
            btnWhatsapp.setPadding(0, 0, 0, 0)
            val paramsLeft = btnWhatsapp.layoutParams as? androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
            paramsLeft?.apply {
                height = UnitHelper.dpToPx(this@ViewActivity, 51f).toInt()
                marginEnd = UnitHelper.dpToPx(this@ViewActivity, 14f).toInt()
                marginStart = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                btnWhatsapp.layoutParams = this
            }
            val cardViewLeft = btnWhatsapp.getChildAt(0) as? androidx.cardview.widget.CardView
            cardViewLeft?.gone()
            val lnlInLeft = btnWhatsapp.getChildAt(1) as? android.view.ViewGroup
            lnlInLeft?.getChildAt(0)?.gone()

            tvWhatsapp.text = strings(R.string.share)
            tvWhatsapp.textSize = 16f
            tvWhatsapp.setTypeface(ResourcesCompat.getFont(this@ViewActivity, R.font.pixelifysans_medium))
            tvWhatsapp.select()

            // Right button - Download
            btnTelegram.setBackgroundResource(R.drawable.bg_btn_bottom)
            btnTelegram.setPadding(0, 0, 0, 0)
            val paramsRight = btnTelegram.layoutParams as? androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
            paramsRight?.apply {
                height = UnitHelper.dpToPx(this@ViewActivity, 51f).toInt()
                marginStart = UnitHelper.dpToPx(this@ViewActivity, 14f).toInt()
                marginEnd = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                btnTelegram.layoutParams = this
            }
            val cardViewRight = btnTelegram.getChildAt(0) as? androidx.cardview.widget.CardView
            cardViewRight?.gone()
            val lnlInRight = btnTelegram.getChildAt(1) as? android.view.ViewGroup
            lnlInRight?.getChildAt(0)?.gone()

            tvTelegram.text = strings(R.string.download)
            tvTelegram.textSize = 16f
            tvTelegram.setTypeface(ResourcesCompat.getFont(this@ViewActivity, R.font.pixelifysans_medium))
            tvTelegram.select()

            btnDownload.gone()
        }
    }

    private fun setupUI() {
        binding.apply {
            actionBar.apply {
                setTextActionBar(tvCenter, getString(R.string.my_pixel))
                setImageActionBar(btnActionBarNextRight, R.drawable.ic_edit_view)
                setImageActionBar(btnActionBarRight, R.drawable.ic_delete)

                // Hide edit icon when coming from design section
                if (viewModel.statusFrom == ValueKey.MY_DESIGN_TYPE) {
                    btnActionBarNextRight.invisible()
                }

                btnShare.gone()
            }

            tvSuccess.gone()
        }
    }

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newPath = result.data?.getStringExtra("NEW_PATH") ?: return@registerForActivityResult
                viewModel.setPath(newPath)
                binding.imvImage.loadImageFromFile(newPath)
            }
        }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pathInternal.collect { path ->
                    loadImage(this@ViewActivity, path, binding.imvImage)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { handleBack() }
                btnActionBarRight.tap { handleDelete() }
                btnActionBarNextRight.tap { handleEditClick(viewModel.pathInternal.value) }
            }

            includeLayoutBottom.btnWhatsapp.tap(2590) {
                viewModel.shareFiles(this@ViewActivity)
            }
            includeLayoutBottom.btnTelegram.tap(2000) {
                checkStoragePermission()
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
        }
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
                            resetMyCreationSelectionMode()

                            setResult(Activity.RESULT_OK, Intent().apply {
                                putExtra("DELETED_PATH", viewModel.pathInternal.value)
                            })
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

    private fun handleBack() {
        resetMyCreationSelectionMode()
        handleBackLeftToRight()
    }

    private fun resetMyCreationSelectionMode() {
        val myCreationActivity = MyCreationActivity.getInstance()
        if (myCreationActivity != null) {
            android.util.Log.d("ViewActivity", "Resetting selection mode in MyCreationActivity")

            val designFragment = myCreationActivity.supportFragmentManager.findFragmentByTag("MyDesignFragment")
            if (designFragment is com.ocmaker.pony.ui.my_creation.fragment.MyDesignFragment) {
                designFragment.resetSelectionMode()
            }

            val avatarFragment = myCreationActivity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")
            if (avatarFragment is MyAvatarFragment) {
                avatarFragment.resetSelectionMode()
            }

            myCreationActivity.exitSelectionMode()
        } else {
            android.util.Log.w("ViewActivity", "MyCreationActivity instance not found - unable to reset selection mode")
        }
    }

    private fun handleEditClick(pathInternal: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            showLoading()
            myAvatarViewModel.editItem(this@ViewActivity, pathInternal, dataViewModel.allData.value)

            withContext(Dispatchers.Main) {
                dismissLoading()

                myAvatarViewModel.checkDataInternet(this@ViewActivity) {
                    val intent = Intent(this@ViewActivity, CustomizeCharacterActivity::class.java).apply {
                        putExtra(IntentKey.INTENT_KEY, myAvatarViewModel.positionCharacter)
                        putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
                    }

                    editLauncher.launch(intent)
                    overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
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

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBack()
    }
}
