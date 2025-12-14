package com.oc.space.ocmaker.creete.ui.add_character.adapter

import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.data.model.SelectedModel
import com.oc.space.ocmaker.creete.databinding.ItemBackgroundColorBinding

class BackgroundColorAdapter :
    BaseAdapter<SelectedModel, ItemBackgroundColorBinding>(ItemBackgroundColorBinding::inflate) {
    var onChooseColorClick: (() -> Unit) = {}
    var onBackgroundColorClick: ((Int, Int) -> Unit) = {_,_ ->}

    var currentSelected = -1
    override fun onBind(binding: ItemBackgroundColorBinding, item: SelectedModel, position: Int) {
        binding.apply {
            vFocus.isVisible = item.isSelected
            if (position == 0) {
                val radiusPx = (4 * root.context.resources.displayMetrics.density).toInt()
                Glide.with(root.context)
                    .load(R.drawable.img)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusPx)))
                    .into(imvColor)
                root.tap { onChooseColorClick.invoke() }
            } else {
                imvColor.setBackgroundColor(item.color)
                root.tap { onBackgroundColorClick.invoke(item.color, position) }
            }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>){
        items.clear()
        items.addAll(list)

        if (position != currentSelected){
            notifyItemChanged(currentSelected)
            notifyItemChanged(position)
            currentSelected = position
        } else {
            notifyItemChanged(position)
        }
    }
}