package com.ocmaker.pixcel.maker.ui.add_character.adapter

import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.data.model.SelectedModel
import com.ocmaker.pixcel.maker.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImage(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}