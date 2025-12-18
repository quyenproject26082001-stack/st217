package com.ocmaker.pixcel.maker.ui.my_creation.adapter

import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.data.model.MyAlbumModel
import com.ocmaker.pixcel.maker.databinding.ItemMyDesignBinding

class MyDesignAdapter() : BaseAdapter<MyAlbumModel, ItemMyDesignBinding>(ItemMyDesignBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}

    var onDeleteClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemMyDesignBinding, item: MyAlbumModel, position: Int) {
        binding.apply {

            loadImage(root, item.path, imvImage)

            if (item.isShowSelection) {
                btnSelect.visible()
                btnDelete.gone()
            } else {
                btnSelect.gone()
                btnDelete.visible()
            }

            if (item.isSelected) {
                btnSelect.setImageResource(R.drawable.ic_selected)
            } else {
                btnSelect.setImageResource(R.drawable.ic_not_select)
            }

            root.tap { onItemClick.invoke(item.path) }

            root.setOnLongClickListener {
                if (items.any { album -> album.isShowSelection }) {
                    return@setOnLongClickListener false
                } else {
                    onLongClick.invoke(position)
                    return@setOnLongClickListener true

                }
            }
            btnDelete.tap { onDeleteClick.invoke(item.path) }
            btnSelect.tap { onItemTick.invoke(position) }
        }
    }
}