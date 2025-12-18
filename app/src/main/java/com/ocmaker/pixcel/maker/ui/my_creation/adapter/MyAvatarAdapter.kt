package com.ocmaker.pixcel.maker.ui.my_creation.adapter

import android.content.Context
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.data.model.MyAlbumModel
import com.ocmaker.pixcel.maker.databinding.ItemMyAlbumBinding

class MyAvatarAdapter(val context: Context) :
    BaseAdapter<MyAlbumModel, ItemMyAlbumBinding>(ItemMyAlbumBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}

    var onEditClick: ((String) -> Unit) = {}
    var onDeleteClick: ((String) -> Unit) = {}

    var isSelectMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBind(binding: ItemMyAlbumBinding, item: MyAlbumModel, position: Int) {
        binding.apply {

            loadImage(root, item.path, imvImage)

            if (item.isShowSelection) {
                btnSelect.visible()
                btnEdit.gone()
                btnDelete.gone()
            } else {
                btnSelect.gone()
                btnEdit.visible()
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
            btnEdit.tap { onEditClick.invoke(item.path) }
            btnDelete.tap { onDeleteClick.invoke(item.path) }
            btnSelect.tap { onItemTick.invoke(position) }
        }
    }
}