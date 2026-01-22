package com.ocmaker.pony.ui.choose_character

import com.ocmaker.pony.core.base.BaseAdapter
import com.ocmaker.pony.core.extensions.gone
import com.ocmaker.pony.core.extensions.loadImage
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.data.model.custom.CustomizeModel
import com.ocmaker.pony.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}
    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            loadImage(item.avatar, imvImage, onDismissLoading = {
                sflShimmer.stopShimmer()
                sflShimmer.gone()
            })
            root.tap { onItemClick.invoke(position) }
        }
    }
}