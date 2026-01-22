package com.ocmaker.pony.ui.intro

import android.content.Context
import com.ocmaker.pony.core.base.BaseAdapter
import com.ocmaker.pony.core.extensions.loadImage
import com.ocmaker.pony.core.extensions.select
import com.ocmaker.pony.core.extensions.strings
import com.ocmaker.pony.data.model.IntroModel
import com.ocmaker.pony.databinding.ItemIntroBinding

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