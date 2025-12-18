package com.ocmaker.pixcel.maker.dialog

import android.app.Activity
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
    }

    override fun initAction() {}

    override fun onDismissListener() {}

}