package com.ocmaker.pixcel.maker.ui.choose_character

import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.data.model.custom.CustomizeModel
import com.ocmaker.pixcel.maker.databinding.ItemChooseAvatarBinding

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