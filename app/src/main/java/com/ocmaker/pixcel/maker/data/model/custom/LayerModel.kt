package com.ocmaker.pixcel.maker.data.model.custom

import com.ocmaker.pixcel.maker.data.model.custom.ColorModel

data class LayerModel(
    val image: String,
    val isMoreColors: Boolean = false,
    var listColor: ArrayList<ColorModel> = arrayListOf()
)