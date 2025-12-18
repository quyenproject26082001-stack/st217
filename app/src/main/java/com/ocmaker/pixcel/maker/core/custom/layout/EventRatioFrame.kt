package com.ocmaker.pixcel.maker.core.custom.layout

import android.widget.ImageView
import com.ocmaker.pixcel.maker.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}