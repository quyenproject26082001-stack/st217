package com.ocmaker.pixcel.maker.ui.intro

import android.content.Context
import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.select
import com.ocmaker.pixcel.maker.core.extensions.strings
import com.ocmaker.pixcel.maker.data.model.IntroModel
import com.ocmaker.pixcel.maker.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}