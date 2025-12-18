package com.ocmaker.pixcel.maker.ui.add_character.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.view.isVisible
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.setFont
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.data.model.SelectedModel
import com.ocmaker.pixcel.maker.databinding.ItemFontBinding
import com.ocmaker.pixcel.maker.databinding.ItemTextColorBinding

class TextFontAdapter(val context: Context) : BaseAdapter<SelectedModel, ItemFontBinding>(ItemFontBinding::inflate) {
    var onTextFontClick: ((Int, Int) -> Unit) = { _, _ -> }
    private var currentSelected = 0

    override fun onBind(binding: ItemFontBinding, item: SelectedModel, position: Int) {
        binding.apply {
            tvFont.setFont(item.color)

            // Always keep vStroke visible to maintain consistent size
            vStroke.visible()

            if (item.isSelected) {
                // Selected state - circular white background with shadow
                tvFont.setTextColor(android.graphics.Color.parseColor("#F61B1B")) // Red text

                // Show shadow for selected item
                cardShadow.cardElevation = 8f * context.resources.displayMetrics.density

                // Create circular white background
                val circularBackground = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(android.graphics.Color.parseColor("#FFFFFF"))
                }
                vFocus.background = circularBackground
                cvMain.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                vStroke.setBackgroundColor(android.graphics.Color.TRANSPARENT) // Transparent stroke (hidden but still takes space)
            } else {
                // Not selected state - transparent with white stroke, no shadow
                tvFont.setTextColor(android.graphics.Color.parseColor("#FFFFFF")) // White text

                // Hide shadow for non-selected items
                cardShadow.cardElevation = 0f

                vFocus.setBackgroundColor(android.graphics.Color.TRANSPARENT) // Transparent background
                cvMain.setBackgroundColor(android.graphics.Color.TRANSPARENT) // Transparent circle background
                vStroke.setBackgroundResource(R.drawable.bg_100_stroke_white) // White stroke visible
            }

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