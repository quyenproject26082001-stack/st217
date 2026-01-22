package com.ocmaker.pony.core.custom.layout

import android.widget.ImageView
import com.ocmaker.pony.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}