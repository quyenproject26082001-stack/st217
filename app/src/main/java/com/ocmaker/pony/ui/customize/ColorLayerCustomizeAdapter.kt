package com.ocmaker.pony.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.ocmaker.pony.core.base.BaseAdapter
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.data.model.custom.ItemColorModel
import com.ocmaker.pony.databinding.ItemColorBinding

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