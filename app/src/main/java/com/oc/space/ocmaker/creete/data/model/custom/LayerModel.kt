package com.oc.space.ocmaker.creete.data.model.custom

import com.oc.space.ocmaker.creete.data.model.custom.ColorModel

data class LayerModel(
    val image: String,
    val isMoreColors: Boolean = false,
    var listColor: ArrayList<ColorModel> = arrayListOf()
)