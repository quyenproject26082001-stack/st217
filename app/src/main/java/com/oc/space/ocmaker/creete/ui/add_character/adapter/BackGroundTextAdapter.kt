package com.oc.space.ocmaker.creete.ui.add_character.adapter

import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.loadImage
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.data.model.SelectedModel
import com.oc.space.ocmaker.creete.databinding.ItemTextBgBinding

class BackGroundTextAdapter : BaseAdapter<SelectedModel, ItemTextBgBinding>(ItemTextBgBinding::inflate) {
    var onClick : ((Int) -> Unit)? = null

    override fun onBind(binding: ItemTextBgBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImage(root, item.path, imv)
            root.tap { onClick?.invoke(position) }
        }
    }
}
