package com.oc.space.ocmaker.creete.ui.add_character.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import androidx.core.view.isVisible
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.data.model.SelectedModel
import com.oc.space.ocmaker.creete.databinding.ItemTextColorBinding

class TextColorAdapter : BaseAdapter<SelectedModel, ItemTextColorBinding>(ItemTextColorBinding::inflate) {
    var onChooseColorClick: (() -> Unit) = {}
    var onTextColorClick: ((Int, Int) -> Unit) = { _, _ -> }

    private var currentSelected = 1


    override fun onBind(binding: ItemTextColorBinding, item: SelectedModel, position: Int) {
        binding.apply {
            vFocus.isVisible = item.isSelected

            if (position == 0) {
                imvColor.visible()
                imvColor.setImageResource(R.drawable.img0text_color)
                btnAddColor.visible()
                root.tap { onChooseColorClick.invoke() }
            } else {
                imvColor.visible()
                btnAddColor.gone()

                // Create circular drawable for color
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(item.color)
                }
                imvColor.background = drawable

                root.tap { onTextColorClick.invoke(item.color, position) }
            }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>) {
        items.clear()
        items.addAll(list)

        if (position != currentSelected) {
            notifyItemChanged(currentSelected)
            notifyItemChanged(position)
            currentSelected = position
        } else {
            notifyItemChanged(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitListReset(list: ArrayList<SelectedModel>){
        items.clear()
        items.addAll(list)
        currentSelected = 1
        notifyDataSetChanged()
    }
}