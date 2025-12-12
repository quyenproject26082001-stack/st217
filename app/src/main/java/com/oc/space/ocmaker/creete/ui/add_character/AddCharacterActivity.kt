package com.oc.space.ocmaker.creete.ui.add_character

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseActivity
import com.oc.space.ocmaker.creete.core.custom.drawview.Draw
import com.oc.space.ocmaker.creete.core.custom.drawview.DrawableDraw
import com.oc.space.ocmaker.creete.listener.listenerdraw.OnDrawListener
import com.oc.space.ocmaker.creete.databinding.ActivityAddCharacterBinding
import com.oc.space.ocmaker.creete.dialog.ChooseColorDialog
import com.oc.space.ocmaker.creete.dialog.YesNoDialog
import com.oc.space.ocmaker.creete.dialog.DialogSpeech
import com.oc.space.ocmaker.creete.ui.add_character.adapter.BackgroundImageAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.BackgroundColorAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.StickerAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.TextFontAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.TextColorAdapter
import com.oc.space.ocmaker.creete.ui.add_character.adapter.BackGroundTextAdapter
import com.oc.space.ocmaker.creete.ui.home.HomeActivity
import com.oc.space.ocmaker.creete.ui.view.ViewActivity
import com.oc.space.ocmaker.creete.core.utils.DataLocal
import com.oc.space.ocmaker.creete.core.helper.UnitHelper
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.extensions.invisible
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.helper.BitmapHelper
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCharacterActivity : BaseActivity<ActivityAddCharacterBinding>() {
    val adapterBGText by lazy { BackGroundTextAdapter() }
    val adapterColor by lazy { BackgroundColorAdapter() }
    val adapterColorText by lazy { TextColorAdapter() }
    val adapterFont by lazy { TextFontAdapter(this) }
    val adapterImage by lazy { BackgroundImageAdapter() }
    val adapterStiker by lazy { StickerAdapter() }
    var path = ""
    val viewModel: AddCharacterViewModel by viewModels()

    override fun setViewBinding(): ActivityAddCharacterBinding {
        return ActivityAddCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        if (DataLocal.arrBlackCentered.isEmpty()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            path = intent.getStringExtra(IntentKey.INTENT_KEY) ?: ""
            initRcv()
            initDrawView()
            initData()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (imeVisible) {
                // Keyboard is shown
                setLayoutParam(binding.lnlBottom, 0f, 0f, UnitHelper.dpToPx(this, 140f), 0f)
            } else {
                // Keyboard is hidden
                setLayoutParam(binding.lnlBottom, 0f, 0f, UnitHelper.dpToPx(this, 82f), 0f)
            }
            insets
        }
    }

    fun initRcv() {
        binding.apply {
            iclBg.rcvColor.itemAnimator = null
            iclBg.rcvColor.adapter = adapterColor

            iclBg.rcvImage.itemAnimator = null
            iclBg.rcvImage.adapter = adapterImage

            iclText.rcvColor.itemAnimator = null
            iclText.rcvColor.adapter = adapterColorText

            iclText.rcvFont.itemAnimator = null
            iclText.rcvFont.adapter = adapterFont

            iclStiker.rcv.itemAnimator = null
            iclStiker.rcv.adapter = adapterStiker

            iclTextBG.rcv.itemAnimator = null
            iclTextBG.rcv.adapter = adapterBGText
        }
    }

    fun initData() {
        showLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.loadDataDefault(this@AddCharacterActivity)
            viewModel.updatePathDefault(path)
            addDrawable(viewModel.pathDefault, true)

            withContext(Dispatchers.Main) {
                adapterImage.submitList(viewModel.backgroundImageList)
                adapterColor.submitList(viewModel.backgroundColorList)
                adapterStiker.submitList(viewModel.stickerList)
                adapterBGText.submitList(viewModel.speechList)
                adapterFont.submitList(viewModel.textFontList)
                adapterColorText.submitList(viewModel.textColorList)
                delay(200)
                clearFocus()
                dismissLoading()
                binding.iclText.edt.typeface = ResourcesCompat.getFont(
                    binding.root.context, viewModel.textFontList[0].color
                )
            }
        }
    }

    private fun dismissLoading() {
        binding.llLoading.gone()
    }

    private fun showLoading() {
        binding.llLoading.visible()
    }

    private fun clearFocus() {
        binding.drawView.hideSelect()
    }

    fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.iclText.edt.windowToken, 0)
    }

    private fun addDrawable(
        path: String, isCharacter: Boolean = false, bitmapText: Bitmap? = null
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmapDefault =
                bitmapText
                    ?: Glide.with(this@AddCharacterActivity).load(path).submit()
                        .get().toBitmap()
            val drawableEmoji =
                viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmapDefault, isCharacter)

            withContext(Dispatchers.Main) {
                drawableEmoji.let { binding.drawView.addDraw(it) }
            }
        }
    }

    var checkSee = true

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun viewListener() {
        binding.apply {
            llLoading.tap {
                showToast(getString(R.string.please_wait_a_few_seconds_for_data_to_load))
            }
            main.tap {
                viewModel.setIsFocusEditText(false)
                clearFocus()
            }
            btnSee.tap {
                if (checkSee) {
                    btnSee.setImageResource(R.drawable.imv_see_false)
                    imvBack.invisible()
                    btnReset.invisible()
                    btnSave.invisible()
                    llBottom.invisible()
                    ctl.invisible()
                } else {
                    btnSee.setImageResource(R.drawable.imv_see)
                    imvBack.visible()
                    btnReset.visible()
                    btnSave.visible()
                    llBottom.visible()
                    ctl.visible()
                }
                checkSee = !checkSee
            }
            btnBg.tap {
                hideKeyboard()
                btnBg.setImageResource(R.drawable.imv_bg_true)
                btnItem.setImageResource(R.drawable.imv_item)
                btnBgText.setImageResource(R.drawable.imv_bg_text)
                btnText.setImageResource(R.drawable.imv_text)
                llBG.visible()
                llStiker.gone()
                llText.gone()
                llTextBG.gone()
            }
            btnItem.tap {
                hideKeyboard()
                btnBg.setImageResource(R.drawable.imv_bg)
                btnItem.setImageResource(R.drawable.imv_item_true)
                btnBgText.setImageResource(R.drawable.imv_bg_text)
                btnText.setImageResource(R.drawable.imv_text)
                llBG.gone()
                llStiker.visible()
                llText.gone()
                llTextBG.gone()
            }
            btnBgText.tap {
                hideKeyboard()
                btnBg.setImageResource(R.drawable.imv_bg)
                btnItem.setImageResource(R.drawable.imv_item)
                btnBgText.setImageResource(R.drawable.imv_bg_text_true)
                btnText.setImageResource(R.drawable.imv_text)
                llBG.gone()
                llStiker.gone()
                llTextBG.visible()
                llText.gone()
            }
            btnText.tap {
                hideKeyboard()
                btnBg.setImageResource(R.drawable.imv_bg)
                btnItem.setImageResource(R.drawable.imv_item)
                btnBgText.setImageResource(R.drawable.imv_bg_text)
                btnText.setImageResource(R.drawable.imv_text_true)
                llBG.gone()
                llStiker.gone()
                llTextBG.gone()
                llText.visible()
            }
            iclBg.apply {
                btnImage.tap {
                    rcvImage.visible()
                    rcvColor.gone()
                    btnImage.setBackgroundResource(R.drawable.bg_custom_choose)
                    btnColor.setBackgroundResource(R.drawable.bg_custom_unchoose)
                }
                btnColor.tap {
                    rcvColor.visible()
                    rcvImage.gone()
                    btnColor.setBackgroundResource(R.drawable.bg_custom_choose)
                    btnImage.setBackgroundResource(R.drawable.bg_custom_unchoose)
                }
            }
            imvBack.tap {
                var dialog = YesNoDialog(this@AddCharacterActivity, R.string.exit, R.string.haven_t_saved_it_yet_do_you_want_to_exit)
                dialog.show()
                dialog.onYesClick = {
                    dialog.dismiss()
                    finish()
                }
                dialog.onNoClick = {
                    dialog.dismiss()
                }
            }
            btnReset.tap {
                viewModel.setIsFocusEditText(false)
                var dialog = YesNoDialog(this@AddCharacterActivity, R.string.reset, R.string.change_your_whole_design_are_you_sure)
                dialog.show()
                dialog.onYesClick = {
                    lifecycleScope.launch {
                        showLoading()
                        withContext(Dispatchers.IO) {
                            viewModel.loadDataDefault(this@AddCharacterActivity)
                            viewModel.resetDraw()
                            withContext(Dispatchers.Main) {
                                binding.drawView.removeAllDraw()
                                binding.iclText.edt.setText("")
                                addDrawable(viewModel.pathDefault, true)
                                clearFocusBG()

                                viewModel.textColorList[adapterColorText.posSelect].isSelected = false
                                viewModel.textColorList[1].isSelected = true
                                adapterColorText.posSelect = 1
                                iclText.edt.setTextColor("#000000".toColorInt())
                                adapterColorText.submitList(viewModel.textColorList)

                                if (adapterFont.posSelect > 0) {
                                    viewModel.textFontList[adapterFont.posSelect].isSelected = false
                                    adapterFont.posSelect = 0
                                    viewModel.textFontList[0].isSelected = true
                                    iclText.edt.typeface = ResourcesCompat.getFont(
                                        binding.root.context, viewModel.textFontList[0].color
                                    )
                                    adapterFont.submitList(viewModel.textFontList)
                                }
                                dismissLoading()
                            }

                        }
                    }
                    dialog.dismiss()
                }
                dialog.onNoClick = {
                    dialog.dismiss()
                }
            }
            btnSave.tap {
                hideKeyboard()
                binding.llLoading.visible()
                clearFocus()
                lifecycleScope.launch(Dispatchers.IO) {
                    delay(200)
                    BitmapHelper.saveBitmap(
                        this@AddCharacterActivity, BitmapHelper.viewToBitmap(binding.drawView), "", false
                    ) { success, resultPath, _ ->
                        if (success) {
                            llLoading.visibility = View.GONE
                            startActivity(
                                Intent(
                                    this@AddCharacterActivity, ViewActivity::class.java
                                ).putExtra(IntentKey.INTENT_KEY, resultPath)
                                    .putExtra(IntentKey.STATUS_KEY, ValueKey.MY_DESIGN_TYPE)
                                    .putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_SUCCESS)
                            )

                        } else {
                            llLoading.visibility = View.GONE
                            showToast(getString(R.string.save_failed_please_try_again))
                        }
                    }
                }
            }
            adapterImage.apply {
                onAddImageClick = {
                    pickImage(pickImageLauncher)
                }
                onBackgroundImageClick = { imagePath, pos ->
                    clearFocusBG()
                    Glide.with(applicationContext).load(viewModel.backgroundImageList[pos].path)
                        .into(binding.imvBackground)
                    viewModel.backgroundImageList[pos].isSelected = true
                    adapterImage.posSelect = pos
                    adapterImage.submitList(viewModel.backgroundImageList)
                }
            }
            adapterColor.apply {
                onChooseColorClick = {
                    val dialog = ChooseColorDialog(this@AddCharacterActivity)
                    dialog.show()

                    dialog.onDoneEvent = { color ->
                        clearFocusBG()
                        binding.imvBackground.setBackgroundColor(color)
                        viewModel.backgroundColorList[0].isSelected = true
                        adapterColor.posSelect = 0
                        adapterColor.submitList(viewModel.backgroundColorList)
                        dialog.dismiss()
                    }
                }
                onBackgroundColorClick = { color, pos ->
                    clearFocusBG()
                    binding.imvBackground.setBackgroundColor(viewModel.backgroundColorList[pos].color)
                    viewModel.backgroundColorList[pos].isSelected = true
                    adapterColor.posSelect = pos
                    adapterColor.submitList(viewModel.backgroundColorList)
                }
            }
            adapterStiker.onItemClick = { imagePath ->
                addDrawable(imagePath)
            }
            adapterBGText.onClick = {
                var dialog = DialogSpeech(this@AddCharacterActivity, viewModel.speechList[it].path)
                dialog.show()
                dialog.onDoneClick = { bitmap ->
                    if (bitmap != null) {
                        addDrawable("", false, bitmap)
                    }
                    dialog.dismiss()
                }
            }
            adapterColorText.apply {
                onChooseColorClick = {
                    val dialog = ChooseColorDialog(this@AddCharacterActivity)
                    dialog.show()

                    dialog.onDoneEvent = { color ->
                        viewModel.textColorList[adapterColorText.posSelect].isSelected = false
                        binding.iclText.edt.setTextColor(color)
                        viewModel.textColorList[0].isSelected = true
                        adapterColorText.posSelect = 0
                        adapterColorText.submitList(viewModel.textColorList)
                        dialog.dismiss()
                    }
                }
                onTextColorClick = { color, pos ->
                    viewModel.textColorList[adapterColorText.posSelect].isSelected = false
                    binding.iclText.edt.setTextColor(viewModel.textColorList[pos].color)
                    viewModel.textColorList[pos].isSelected = true
                    adapterColorText.posSelect = pos
                    adapterColorText.submitList(viewModel.textColorList)
                }
            }
            adapterFont.onTextFontClick = { font, pos ->
                viewModel.textFontList[adapterFont.posSelect].isSelected = false
                adapterFont.posSelect = pos
                viewModel.textFontList[pos].isSelected = true
                iclText.edt.typeface = ResourcesCompat.getFont(
                    binding.root.context, viewModel.textFontList[pos].color
                )
                adapterFont.submitList(viewModel.textFontList)
            }
        }
        binding.iclText.imvTick.tap {
            handleDoneText()
        }
        binding.iclText.edt.addTextChangedListener {
            binding.tvGetText.text = it.toString().trim()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                clearFocusBG()
                viewModel.backgroundImageList[0].isSelected = true
                adapterImage.posSelect = 0
                adapterImage.submitList(viewModel.backgroundImageList)
                Glide.with(applicationContext).load(uri).into(binding.imvBackground)
            }
        }
    }

    fun handleDoneText() {
        hideKeyboard()
        binding.root.postDelayed({
            viewModel.setIsFocusEditText(false)
            binding.apply {
                if (iclText.edt.text.toString().trim() == "") {
                    showToast(getString(R.string.null_edt))
                } else {
                    tvGetText.typeface = ResourcesCompat.getFont(
                        binding.root.context, viewModel.textFontList[adapterFont.posSelect].color
                    )
                    tvGetText.setTextColor(iclText.edt.textColors)
                    val bitmap = BitmapHelper.viewToBitmap(tvGetText)
                    val drawableEmoji =
                        viewModel.loadDrawableEmoji(this@AddCharacterActivity, bitmap, isText = true)
                    binding.drawView.addDraw(drawableEmoji)

                    iclText.edt.text = null

                    viewModel.textColorList[adapterColorText.posSelect].isSelected = false
                    viewModel.textColorList[1].isSelected = true
                    adapterColorText.posSelect = 1
                    iclText.edt.setTextColor("#000000".toColorInt())
                    adapterColorText.submitList(viewModel.textColorList)

                    if (adapterFont.posSelect > 0) {
                        viewModel.textFontList[adapterFont.posSelect].isSelected = false
                        adapterFont.posSelect = 0
                        viewModel.textFontList[0].isSelected = true
                        iclText.edt.typeface = ResourcesCompat.getFont(
                            binding.root.context, viewModel.textFontList[0].color
                        )
                        adapterFont.submitList(viewModel.textFontList)
                    }
                }
            }
        }, 300)

    }

    fun clearFocusBG() {
        binding.imvBackground.setBackgroundColor(getColor(R.color.transparent))
        binding.imvBackground.setImageBitmap(null)
        if (adapterColor.posSelect >= 0) {
            viewModel.backgroundColorList[adapterColor.posSelect].isSelected = false
            adapterColor.posSelect = -1
            adapterColor.submitList(viewModel.backgroundColorList)
        }

        if (adapterImage.posSelect >= 0) {
            viewModel.backgroundImageList[adapterImage.posSelect].isSelected = false
            adapterImage.posSelect = -1
            adapterImage.submitList(viewModel.backgroundImageList)
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
                    if (draw.isCharacter) {

                    } else {
                        viewModel.deleteDrawView(draw)
                        viewModel.setIsFocusEditText(false)
                    }

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

    override fun initActionBar() {
    }

    override fun initText() {
    }

    override fun dataObservable() {
    }

    private fun setLayoutParam(view: View, start: Float, top: Float, end: Float, bottom: Float) {
        val params = view.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        params.setMargins(start.toInt(), top.toInt(), end.toInt(), bottom.toInt())
        view.layoutParams = params
    }

    private fun pickImage(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        launcher.launch(intent)
    }
}
