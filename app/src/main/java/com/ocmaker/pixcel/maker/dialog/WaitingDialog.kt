package com.ocmaker.pixcel.maker.dialog

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseDialog
import com.ocmaker.pixcel.maker.core.extensions.setBackgroundConnerSmooth
import com.ocmaker.pixcel.maker.databinding.DialogLoadingBinding

class WaitingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    override fun initView() {
        // Start loading animation for dot
        binding.icDotLoading.setImageResource(R.drawable.dot_loading_animation)
        val dotAnimation = binding.icDotLoading.drawable as? AnimationDrawable
        dotAnimation?.start()
    }

    override fun initAction() {}

    override fun onDismissListener() {}

}