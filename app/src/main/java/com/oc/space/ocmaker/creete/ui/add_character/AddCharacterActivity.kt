package com.oc.space.ocmaker.creete.ui.add_character

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseActivity
import com.oc.space.ocmaker.creete.core.extensions.checkPermissions
import com.oc.space.ocmaker.creete.core.extensions.goToSettings
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.hideNavigation
import com.oc.space.ocmaker.creete.core.extensions.hideSoftKeyboard
import com.oc.space.ocmaker.creete.core.extensions.invisible
import com.oc.space.ocmaker.creete.core.extensions.loadImage
import com.oc.space.ocmaker.creete.core.extensions.loadNativeCollabAds
import com.oc.space.ocmaker.creete.core.extensions.openImagePicker
import com.oc.space.ocmaker.creete.core.extensions.requestPermission
import com.oc.space.ocmaker.creete.core.extensions.select
import com.oc.space.ocmaker.creete.core.extensions.setFont
import com.oc.space.ocmaker.creete.core.extensions.setImageActionBar
import com.oc.space.ocmaker.creete.core.extensions.showInterAll
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.helper.BitmapHelper
import com.oc.space.ocmaker.creete.core.helper.UnitHelper
import com.oc.space.ocmaker.creete.core.utils.DataLocal
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.key.RequestKey
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import com.oc.space.ocmaker.creete.core.utils.state.SaveState
import com.oc.space.ocmaker.creete.data.model.draw.Draw
import com.oc.space.ocmaker.creete.data.model.draw.DrawableDraw
import com.oc.space.ocmaker.creete.databinding.ActivityAddCharacterBinding
import com.oc.space.ocmaker.creete.dialog.ChooseColorDialog
import com.oc.space.ocmaker.creete.dialog.DialogSpeech
import com.oc.space.ocmaker.creete.dialog.YesNoDialog
import com.oc.space.ocmaker.creete.listener.listenerdraw.OnDrawListener
import com.oc.space.ocmaker.creete.ui.add_character.adapter.BackgroundColorAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.BackgroundImageAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.StickerAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.TextColorAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.TextFontAdapter
import com.oc.space.ocmaker.creete.ui.permission.PermissionViewModel
import com.oc.space.ocmaker.creete.ui.view.ViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue
import kotlin.toString

class AddCharacterActivity : BaseActivity<ActivityAddCharacterBinding>() {
    private val viewModel: AddCharacterViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()
    private val backgroundImageAdapter by lazy { BackgroundImageAdapter() }
    private val backgroundColorAdapter by lazy { BackgroundColorAdapter() }
    private val stickerAdapter by lazy { StickerAdapter() }
    private val speechAdapter by lazy { StickerAdapter() }
    private val textFontAdapter by lazy { TextFontAdapter(this) }
    private val textColorAdapter by lazy { TextColorAdapter() }

    // Photo Picker - NO PERMISSION REQUIRED
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            handleSetBackgroundImage(uri.toString(), 0)
        }
    }

    private val buttonNavigationList by lazy {
        arrayListOf(
            binding.btnBackground,
            binding.btnSticker,
            binding.btnSpeech,
            binding.btnText,
        )
    }

    private val layoutNavigationList by lazy {
        arrayListOf(
            binding.lnlBackground,
            binding.lnlSticker,
            binding.lnlSpeech,
            binding.scvText,
        )
    }

    override fun setViewBinding(): ActivityAddCharacterBinding {
        return ActivityAddCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.layoutParams = binding.flFunction.layoutParams as ViewGroup.MarginLayoutParams
        initRcv()
        initDrawView()
        initData()
    }
//    private fun setupKeyboardDetection() {
//        binding.main.viewTreeObserver.addOnGlobalLayoutListener {
//            val rect = android.graphics.Rect()
//            binding.main.getWindowVisibleDisplayFrame(rect)
//
//            val screenHeight = binding.main.rootView.height
//            val keypadHeight = screenHeight - rect.bottom
//
//            // If keyboard height is more than 15% of screen, keyboard is showing
//            val isKeyboardNowShowing = keypadHeight > screenHeight * 0.15
//
//            if (isKeyboardNowShowing != isKeyboardShowing) {
//                isKeyboardShowing = isKeyboardNowShowing
//
//                if (isKeyboardShowing && binding.edtText.hasFocus()) {
//                    // Keyboard just appeared
//                    adjustLayoutForKeyboard(keypadHeight)
//                } else if (!isKeyboardShowing) {
//                    // Keyboard just disappeared
//                    resetLayout()
//                }
//            }
//        }
//    }
//    private fun adjustLayoutForKeyboard(keyboardHeight: Int) {
//        binding.scvText.postDelayed({
//            // Scroll the ScrollView to show EditText
//            val location = IntArray(2)
//            binding.edtText.getLocationOnScreen(location)
//            val edtY = location[1]
//            val edtBottom = edtY + binding.edtText.height
//
//            val screenHeight = resources.displayMetrics.heightPixels
//            val visibleHeight = screenHeight - keyboardHeight
//
//            if (edtBottom > visibleHeight) {
//                // Calculate how much to scroll
//                val scrollAmount = edtBottom - visibleHeight + UnitHelper.dpToPx(this, 50)
//                binding.scvText.smoothScrollBy(0, scrollAmount)
//            }
//        }, 100)
//    }
//
//    private fun resetLayout() {
//        binding.scvText.smoothScrollTo(0, 0)
//    }



    override fun dataObservable() {
        binding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
//                        typeNavigation
                        viewModel.typeNavigation.collect { type ->
                            if (type != -1) {
                                setupTypeNavigation(type)
                            }
                        }
                    }

                    launch {
//                        typeBackground
                        viewModel.typeBackground.collect { type ->
                            if (type != -1) {
                                setupTypeBackground(type)
                            }
                        }
                    }

                    launch {
//                        isFocusEditText
                        viewModel.isFocusEditText.collect { status ->
                            if (status) {
                                // Clear FLAG_LAYOUT_NO_LIMITS to allow adjustResize to work
                                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                                viewModel.layoutParams.topMargin = UnitHelper.dpToPx(this@AddCharacterActivity, -160)
                                flFunction.layoutParams = viewModel.layoutParams
                            } else {
                                viewModel.layoutParams.topMargin = viewModel.originalMarginBottom
                                flFunction.layoutParams = viewModel.layoutParams
                                hideSoftKeyboard()
                                edtText.clearFocus()
                                hideNavigation()  // Re-set FLAG_LAYOUT_NO_LIMITS
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
                btnActionBarLeft.tap { confirmExit() }
                btnActionBarCenter.tap { confirmReset() }
                btnActionBarRightText.tap {
                    handleSave()
                }
            }
            btnBackgroundImage.tap { viewModel.setTypeBackground(ValueKey.IMAGE_BACKGROUND) }
            btnBackgroundColor.tap { viewModel.setTypeBackground(ValueKey.COLOR_BACKGROUND) }
            btnBackground.tap { viewModel.setTypeNavigation(ValueKey.BACKGROUND_NAVIGATION) }
            btnSticker.tap { viewModel.setTypeNavigation(ValueKey.STICKER_NAVIGATION) }
            btnSpeech.tap { viewModel.setTypeNavigation(ValueKey.SPEECH_NAVIGATION) }
            btnText.tap { viewModel.setTypeNavigation(ValueKey.TEXT_NAVIGATION) }

            edtText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    binding.tvGetText.text = p0.toString()
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
            edtText.setOnEditorActionListener { textView, i, keyEvent ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    viewModel.setIsFocusEditText(false)
                    true
                } else {
                    false
                }
            }
            edtText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    viewModel.setIsFocusEditText(true)
                } else {
                    viewModel.setIsFocusEditText(false)
                }
            }
            btnDoneText.tap { handleDoneText() }

            main.tap {
                viewModel.setIsFocusEditText(false)
                clearFocus()
            }

            backgroundImageAdapter.apply {
                onAddImageClick = { checkStoragePermission() }
                onBackgroundImageClick = { path, position -> handleSetBackgroundImage(path, position) }
            }

            backgroundColorAdapter.apply {
                onChooseColorClick = { handleChooseColor() }
                onBackgroundColorClick = { color, position -> handleSetBackgroundColor(color, position) }
            }

            stickerAdapter.onItemClick = { path -> addDrawable(path) }

            speechAdapter.onItemClick = { path -> handleSpeech(path) }

            textFontAdapter.onTextFontClick = { font, position -> handleFontClick(font, position) }

            textColorAdapter.apply {
                onChooseColorClick = { handleChooseColor(true) }
                onTextColorClick = { color, position -> handleTextColorClick(color, position) }
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setImageActionBar(btnActionBarCenter, R.drawable.ic_reset)
            btnActionBarRightText.visible()
            tvRightText.isSelected =true
            tvRightText.text = getString(R.string.save)

            // Căn giữa nút reset vào guideline
            val params = btnActionBarCenter.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.endToEnd = guideline.id
            params.startToStart = guideline.id
            params.horizontalBias = 0.5f
            params.marginEnd = 0
            btnActionBarCenter.layoutParams = params
        }
    }

    override fun initText() {
        binding.apply {
            tvBackgroundImage.select()
            tvBackgroundColor.select()

            // Apply gradient to tvText, tvFont, tvColor
            tvText.post {
                val textHeight = tvText.lineHeight.toFloat()
                val shader = LinearGradient(
                    0f, 0f, 0f, textHeight,
                    Color.parseColor("#8FFFFD"),
                    Color.parseColor("#2641D7"),
                    Shader.TileMode.CLAMP
                )
                tvText.paint.shader = shader
                tvText.invalidate()
            }

            tvFont.post {
                val textHeight = tvFont.lineHeight.toFloat()
                val shader = LinearGradient(
                    0f, 0f, 0f, textHeight,
                    Color.parseColor("#8FFFFD"),
                    Color.parseColor("#2641D7"),
                    Shader.TileMode.CLAMP
                )
                tvFont.paint.shader = shader
                tvFont.invalidate()
            }

            tvColor.post {
                val textHeight = tvColor.lineHeight.toFloat()
                val shader = LinearGradient(
                    0f, 0f, 0f, textHeight,
                    Color.parseColor("#8FFFFD"),
                    Color.parseColor("#2641D7"),
                    Shader.TileMode.CLAMP
                )
                tvColor.paint.shader = shader
                tvColor.invalidate()
            }
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvBackgroundImage.apply {
                adapter = backgroundImageAdapter
                itemAnimator = null
            }

            rcvBackgroundColor.apply {
                adapter = backgroundColorAdapter
                itemAnimator = null
            }

            rcvSticker.apply {
                adapter = stickerAdapter
                itemAnimator = null
            }

            rcvSpeech.apply {
                adapter = speechAdapter
                itemAnimator = null
            }

            rcvFont.apply {
                adapter = textFontAdapter
                itemAnimator = null
            }

            rcvTextColor.apply {
                adapter = textColorAdapter
                itemAnimator = null
            }
        }
    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            showLoading()
            viewModel.loadDataDefault(this@AddCharacterActivity)
            viewModel.updatePathDefault(intent.getStringExtra(IntentKey.INTENT_KEY) ?: "")
            addDrawable(viewModel.pathDefault, true)

            withContext(Dispatchers.Main) {
                viewModel.setTypeNavigation(ValueKey.BACKGROUND_NAVIGATION)
                viewModel.setTypeBackground(ValueKey.IMAGE_BACKGROUND)
                backgroundImageAdapter.submitList(viewModel.backgroundImageList)
                backgroundColorAdapter.submitList(viewModel.backgroundColorList)
                stickerAdapter.submitList(viewModel.stickerList)
                speechAdapter.submitList(viewModel.speechList)
                textFontAdapter.submitListReset(viewModel.textFontList)
                textColorAdapter.submitListReset(viewModel.textColorList)
                // Set initial text color and font to match selected items
                binding.edtText.setFont(viewModel.textFontList.first().color)
                binding.edtText.setTextColor(viewModel.textColorList[1].color)
                binding.tvGetText.setTextColor(viewModel.textColorList[1].color)
                delay(200)
                binding.drawView.autoSelectFirstDraw()

                clearFocus()
                binding.drawView.autoSelectFirstDraw()
                dismissLoading()
            }
        }
    }

    private fun addDrawable(path: String, isCharacter: Boolean = false, bitmapText: Bitmap? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmapDefault = if (bitmapText == null) Glide.with(this@AddCharacterActivity).load(path).submit().get()
                .toBitmap() else bitmapText
            val drawableEmoji = viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmapDefault, isCharacter)

            withContext(Dispatchers.Main) {
                drawableEmoji.let { binding.drawView.addDraw(it) }
            }
        }
    }

    private fun initDrawView() {
        binding.drawView.apply {
            setConstrained(true)
            setLocked(false)
            setOnDrawListener(object : OnDrawListener {
                override fun onAddedDraw(draw: Draw) {
                    viewModel.updateCurrentCurrentDraw(draw)
                    viewModel.addDrawView(draw)
                    viewModel.setIsFocusEditText(false)
                }

                override fun onClickedDraw(draw: Draw) {
                    viewModel.setIsFocusEditText(false)
                }

                override fun onDeletedDraw(draw: Draw) {
                    viewModel.deleteDrawView(draw)
                    viewModel.setIsFocusEditText(false)
                }

                override fun onDragFinishedDraw(draw: Draw) {
                    viewModel.setIsFocusEditText(false)
                }

                override fun onTouchedDownDraw(draw: Draw) {
                    viewModel.updateCurrentCurrentDraw(draw)
                    viewModel.setIsFocusEditText(false)
                }

                override fun onZoomFinishedDraw(draw: Draw) {}

                override fun onFlippedDraw(draw: Draw) {
                    viewModel.setIsFocusEditText(false)
                }

                override fun onDoubleTappedDraw(draw: Draw) {}

                override fun onHideOptionIconDraw() {}

                override fun onUndoDeleteDraw(draw: List<Draw?>) {}

                override fun onUndoUpdateDraw(draw: List<Draw?>) {}

                override fun onUndoDeleteAll() {}

                override fun onRedoAll() {}

                override fun onReplaceDraw(draw: Draw) {}

                override fun onEditText(draw: DrawableDraw) {}

                override fun onReplace(draw: Draw) {}
            })
        }
    }

    private fun setupTypeBackground(type: Int) {
        binding.apply {

            when (type) {
                ValueKey.IMAGE_BACKGROUND -> {
                    rcvBackgroundImage.visible()
                    rcvBackgroundColor.gone()
                    setupSelectedTabBackground(btnBackgroundImage, tvBackgroundImage, imvFocusImage, subTabImage, isLeftTab = true)
                    setupUnselectedTabBackground(btnBackgroundColor, tvBackgroundColor, imvFocusColor, subTabColor, isLeftTab = false)
                    backgroundImageAdapter.submitList(viewModel.backgroundImageList)
                }

                ValueKey.COLOR_BACKGROUND -> {
                    rcvBackgroundImage.gone()
                    rcvBackgroundColor.visible()
                    setupSelectedTabBackground(btnBackgroundColor, tvBackgroundColor, imvFocusColor, subTabColor, isLeftTab = false)
                    setupUnselectedTabBackground(btnBackgroundImage, tvBackgroundImage, imvFocusImage, subTabImage, isLeftTab = true)
                    backgroundColorAdapter.submitList(viewModel.backgroundColorList)
                }

                else -> {}
            }
        }
    }

    private fun setupSelectedTabBackground(
        tabView: View,
        textView: android.widget.TextView,
        focusImage: android.widget.ImageView,
        subTab: View,
        isLeftTab: Boolean
    ) {
        // Set weight = 1.6
        val params = tabView.layoutParams as android.widget.LinearLayout.LayoutParams
        params.weight = 1.6f
        params.topMargin = 0
        tabView.layoutParams = params

        // Set text size = 18sp
        textView.textSize = 18f

        // Apply gradient color from top to bottom
        val textHeight = textView.lineHeight.toFloat()
        val shader = LinearGradient(
            0f, 0f, 0f, textHeight,
            Color.parseColor("#8FFFFD"),
            Color.parseColor("#2641D7"),
            Shader.TileMode.CLAMP
        )
        textView.paint.shader = shader

        // Show selected_tab drawable
        focusImage.setImageResource(R.drawable.selected_tab)
        focusImage.scaleX = 1f
        focusImage.visible()

        // Hide subTab
        subTab.gone()
    }

    private fun setupUnselectedTabBackground(
        tabView: View,
        textView: android.widget.TextView,
        focusImage: android.widget.ImageView,
        subTab: View,
        isLeftTab: Boolean
    ) {
        // Set weight = 1
        val params = tabView.layoutParams as android.widget.LinearLayout.LayoutParams
        params.weight = 1f
        params.topMargin = UnitHelper.dpToPx(this, 10f).toInt()
        tabView.layoutParams = params

        // Set text size = 14sp, color = colorPrimary
        textView.textSize = 14f
        // Remove gradient shader and set solid color
        textView.paint.shader = null
        textView.setTextColor(getColor(R.color.colorPrimary))

        // Show un_selected_tab drawable
        focusImage.setImageResource(R.drawable.un_selected_tab)
        // Flip horizontally if on left side
        focusImage.scaleX = if (isLeftTab) -1f else 1f
        focusImage.visible()

        // Show subTab
        subTab.visible()
    }

    private fun setupTypeNavigation(type: Int) {
        buttonNavigationList.forEachIndexed { index, button ->
            val (res, status) = if (index == type) {
                DataLocal.bottomNavigationSelected[index] to true
            } else {
                DataLocal.bottomNavigationNotSelect[index] to false
            }

            button.setImageResource(res)
            layoutNavigationList[index].isVisible = status
        }

        // Show sectionTab and spaceSectionTab only when Background navigation is selected
        binding.apply {
            if (type == ValueKey.BACKGROUND_NAVIGATION) {
                sectionTab.visible()
                spaceSectionTab.visible()
            } else {
                sectionTab.gone()
                spaceSectionTab.invisible()
            }
        }
    }

    private fun confirmExit() {
        viewModel.setIsFocusEditText(false)
        val dialog = YesNoDialog(this, R.string.exit, R.string.haven_t_saved_it_yet_do_you_want_to_exit)
        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onNoClick = {
            dismissDialog()
        }
        dialog.onYesClick = {
            dismissDialog()
            finish()
        }
    }

    private fun confirmReset() {
        viewModel.setIsFocusEditText(false)
        val dialog = YesNoDialog(this, R.string.reset, R.string.change_your_whole_design_are_you_sure)
        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation()
        }

        dialog.onNoClick = {
            dismissDialog()
        }

        dialog.onYesClick = {
            dismissDialog()
            lifecycleScope.launch {
                showLoading()
                withContext(Dispatchers.IO) {
                    viewModel.loadDataDefault(this@AddCharacterActivity)
                    viewModel.resetDraw()
                }
                binding.drawView.removeAllDraw()
                binding.imvBackground.setImageBitmap(null)
                binding.imvBackground.setBackgroundColor(getColor(R.color.transparent))
                binding.edtText.setText("")
                binding.edtText.setFont(viewModel.textFontList.first().color)
                binding.edtText.setTextColor(viewModel.textColorList[1].color)
                addDrawable(viewModel.pathDefault, true)
                backgroundImageAdapter.submitList(viewModel.backgroundImageList)
                backgroundColorAdapter.submitList(viewModel.backgroundColorList)
                stickerAdapter.submitList(viewModel.stickerList)
                speechAdapter.submitList(viewModel.speechList)
                textFontAdapter.submitListReset(viewModel.textFontList)
                textColorAdapter.submitListReset(viewModel.textColorList)
                dismissLoading()
                showInterAll()
            }
        }
    }

    private fun handleSetBackgroundImage(path: String, position: Int) {
        binding.imvBackground.setBackgroundColor(getColor(R.color.transparent))
        loadImage(this, path, binding.imvBackground)
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.updateBackgroundImageSelected(position)
            withContext(Dispatchers.Main) {
                backgroundImageAdapter.submitItem(position, viewModel.backgroundImageList)
            }
        }
    }

    private fun checkStoragePermission() {
        // Use Photo Picker - NO PERMISSION REQUIRED
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun handleChooseColor(isTextColor: Boolean = false) {
        val dialog = ChooseColorDialog(this)

        dialog.show()

        fun dismissDialog() {
            dialog.dismiss()
            hideNavigation()
        }

        dialog.onCloseEvent = {
            dismissDialog()
        }

        dialog.onDoneEvent = { color ->
            dismissDialog()
            if (!isTextColor) {
                handleSetBackgroundColor(color, 0)
            } else {
                handleTextColorClick(color, 0)
            }
        }
    }

    private fun handleSpeech(path: String) {
        val dialog = DialogSpeech(this, path)
        dialog.show()
        dialog.onDoneClick = { bitmap ->
            dialog.dismiss()
            hideNavigation()
            if (bitmap != null) {
                addDrawable("", false, bitmap)
            }
        }
    }

    private fun handleSetBackgroundColor(color: Int, position: Int) {
        binding.apply {
            imvBackground.setImageBitmap(null)
            imvBackground.setBackgroundColor(color)
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.updateBackgroundColorSelected(position)
                withContext(Dispatchers.Main) {
                    backgroundColorAdapter.submitItem(position, viewModel.backgroundColorList)
                }
            }
        }
    }

    private fun handleFontClick(font: Int, position: Int) {
        binding.apply {
            edtText.hint = SpannableString(getString(R.string.hello_world))
            edtText.setFont(font)
            tvGetText.setFont(font)
            viewModel.updateTextFontSelected(position)
            textFontAdapter.submitItem(position, viewModel.textFontList)
        }
    }

    private fun handleTextColorClick(color: Int, position: Int) {
        binding.apply {
            edtText.setTextColor(color)
            tvGetText.setTextColor(color)
            viewModel.updateTextColorSelected(position)
            textColorAdapter.submitItem(position, viewModel.textColorList)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun handleDoneText() {
        viewModel.setIsFocusEditText(false)
        binding.apply {
            if (edtText.text.toString().trim() == "") {
                showToast(getString(R.string.null_edt))
            } else {
                tvGetText.text = edtText.text.toString().trim()
                val bitmap = BitmapHelper.getBitmapFromEditText(tvGetText)
                val drawableEmoji = viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmap, isText = true)
                binding.drawView.addDraw(drawableEmoji)

                // Reset
                val font = viewModel.textFontList.first().color
                val color = viewModel.textColorList[1].color

                edtText.text = null
                edtText.setFont(font)
                edtText.setTextColor(color)

                viewModel.updateTextFontSelected(0)
                viewModel.updateTextColorSelected(1)

                textFontAdapter.submitListReset(viewModel.textFontList)
                textColorAdapter.submitListReset(viewModel.textColorList)

                tvGetText.text = ""
                tvGetText.setFont(font)
                tvGetText.setTextColor(color)
            }
        }
    }

    private fun clearFocus() {
        binding.drawView.hideSelect()
    }

    private fun handleSave() {
        binding.apply {
            clearFocus()
            lifecycleScope.launch(Dispatchers.IO) {
                showLoading()
                delay(200)
                viewModel.saveImageFromView(this@AddCharacterActivity, flSave).collect { result ->
                    when (result) {
                        is SaveState.Loading -> showLoading()

                        is SaveState.Error -> {
                            dismissLoading()
                            withContext(Dispatchers.Main) {
                                showToast(R.string.save_failed_please_try_again)
                            }
                        }

                        is SaveState.Success -> {
                            val intent = Intent(this@AddCharacterActivity, ViewActivity::class.java)
                            intent.putExtra(IntentKey.INTENT_KEY, result.path)
                            intent.putExtra(IntentKey.STATUS_KEY, ValueKey.MY_DESIGN_TYPE)
                            intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_SUCCESS)
                            val options = ActivityOptions.makeCustomAnimation(
                                this@AddCharacterActivity, R.anim.slide_in_right, R.anim.slide_out_left
                            )
                            dismissLoading()
                            withContext(Dispatchers.Main){
                                showInterAll { startActivity(intent, options.toBundle()) }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                openImagePicker()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestKey.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            handleSetBackgroundImage(selectedImageUri.toString(), 0)
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (viewModel.isFocusEditText.value) {
            viewModel.setIsFocusEditText(false)
        } else {
            confirmExit()
        }
    }

//    fun initNativeCollab() {
//        loadNativeCollabAds(R.string.native_cl_bg, binding.flNativeCollab, binding.lnlBottom)
//    }

//    override fun initAds() {
//        initNativeCollab()
//    }

    override fun onRestart() {
        super.onRestart()
      //  initNativeCollab()
    }

    // Custom Input View Functions
    private fun setupCustomInput() {
       // val customInputView = binding.customInputLayout.root

        // Set height to 1/4 of screen
//        customInputView.post {
//            val screenHeight = resources.displayMetrics.heightPixels
//            val params = customInputView.layoutParams
//            params.height = screenHeight / 4
//            customInputView.layoutParams = params
//        }

        // Disable system keyboard for edtText
        binding.edtText.showSoftInputOnFocus = true

//        // Setup edtText click listener
//        binding.edtText.setOnClickListener {
//           // hideSystemKeyboard()
//          //  showCustomInput()
//        }

//        binding.edtText.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//               // hideSystemKeyboard()
//                //showCustomInput()
//            }
//        }

        // Setup all letter buttons
        setupLetterButtons()

//        // Setup control buttons
//        binding.customInputLayout.btnSpace.tap { appendText(" ") }
//        binding.customInputLayout.btnDelete.tap { deleteLastChar() }
//        binding.customInputLayout.btnDone.tap { hideCustomInput() }
    }

    private fun setupLetterButtons() {
        val letters = listOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
        )

//        letters.forEach { letter ->
//            val buttonId = resources.getIdentifier("btn$letter", "id", packageName)
//            val button = binding.customInputLayout.root.findViewById<android.widget.Button>(buttonId)
//            button?.tap { appendText(letter) }
//        }
    }

//    private fun showCustomInput() {
//        binding.customInputLayout.root.visible()
//    }
//
//    private fun hideCustomInput() {
//        binding.customInputLayout.root.gone()
//    }

    private fun hideSystemKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.edtText.windowToken, 0)
    }

    private fun appendText(text: String) {
        val currentText = binding.edtText.text.toString()
        binding.edtText.setText(currentText + text)
        binding.edtText.setSelection(binding.edtText.text.length)
    }

    private fun deleteLastChar() {
        val currentText = binding.edtText.text.toString()
        if (currentText.isNotEmpty()) {
            binding.edtText.setText(currentText.dropLast(1))
            binding.edtText.setSelection(binding.edtText.text.length)
        }
    }
}