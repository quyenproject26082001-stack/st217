package com.ocmaker.pixcel.maker.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.data.model.custom.ItemColorModel
import com.ocmaker.pixcel.maker.databinding.ItemColorBinding

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