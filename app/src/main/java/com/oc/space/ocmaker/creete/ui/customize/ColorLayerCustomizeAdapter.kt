package com.oc.space.ocmaker.creete.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.data.model.custom.ItemColorModel
import com.oc.space.ocmaker.creete.databinding.ItemColorBinding

class ColorLayerCustomizeAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            imvFocus.isVisible = item.isSelected
            root.tap { onItemClick.invoke(position) }
        }
    }
}