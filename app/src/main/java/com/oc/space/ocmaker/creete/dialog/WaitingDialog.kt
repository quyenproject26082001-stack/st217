package com.oc.space.ocmaker.creete.dialog

import android.app.Activity
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseDialog
import com.oc.space.ocmaker.creete.core.extensions.setBackgroundConnerSmooth
import com.oc.space.ocmaker.creete.databinding.DialogLoadingBinding

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