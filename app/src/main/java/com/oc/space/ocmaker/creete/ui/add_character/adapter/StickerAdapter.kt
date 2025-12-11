package com.oc.space.ocmaker.creete.ui.add_character.adapter

import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.loadImage
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.data.model.SelectedModel
import com.oc.space.ocmaker.creete.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImage(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}