package com.oc.space.ocmaker.creete.ui.my_creation.adapter

import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.loadImage
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.data.model.MyAlbumModel
import com.oc.space.ocmaker.creete.databinding.ItemMyDesignBinding

class MyDesignAdapter() : BaseAdapter<MyAlbumModel, ItemMyDesignBinding>(ItemMyDesignBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}
    var onEditClick: ((String) -> Unit) = {}
    var onDeleteClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemMyDesignBinding, item: MyAlbumModel, position: Int) {
        binding.apply {

            loadImage(root, item.path, imvImage)

            if (item.isShowSelection) {
                btnSelect.visible()
                btnDelete.gone()
                btnEdit.gone()
            } else {
                btnSelect.gone()
                btnDelete.visible()
                btnEdit.visible()
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
            btnEdit.tap { onEditClick.invoke(item.path) }
            btnDelete.tap { onDeleteClick.invoke(item.path) }
            btnSelect.tap { onItemTick.invoke(position) }
        }
    }
}