package com.oc.space.ocmaker.creete.ui.add_character.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.view.isVisible
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseAdapter
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.setFont
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.data.model.SelectedModel
import com.oc.space.ocmaker.creete.databinding.ItemFontBinding
import com.oc.space.ocmaker.creete.databinding.ItemTextColorBinding

class TextFontAdapter(val context: Context) : BaseAdapter<SelectedModel, ItemFontBinding>(ItemFontBinding::inflate) {
    var onTextFontClick: ((Int, Int) -> Unit) = { _, _ -> }
    private var currentSelected = 0

    override fun onBind(binding: ItemFontBinding, item: SelectedModel, position: Int) {
        binding.apply {
            val res = if (item.isSelected) R.drawable.bg_100_solid_white else R.drawable.bg_100_stroke_white
            vFocus.setBackgroundResource(res)

            // Show gradient stroke when selected
            vStroke.isVisible = item.isSelected

            tvFont.setFont(item.color)
            val (color, elevation) = if (item.isSelected) R.color.dark to 6f else R.color.dark to 0f
            tvFont.setTextColor(context.getColor(color))
           // cvMain.cardElevation = elevation
            root.tap { onTextFontClick.invoke(item.color, position) }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>) {
        if (position != currentSelected) {
            items.clear()
            items.addAll(list)

            notifyItemChanged(currentSelected)
            notifyItemChanged(position)

            currentSelected = position
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitListReset(list: ArrayList<SelectedModel>){
        items.clear()
        items.addAll(list)
        currentSelected = 0
        notifyDataSetChanged()
    }
}