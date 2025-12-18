package com.ocmaker.pixcel.maker.ui.customize

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.helper.UnitHelper
import com.ocmaker.pixcel.maker.data.model.custom.NavigationModel
import com.ocmaker.pixcel.maker.databinding.ItemBottomNavigationBinding

class BottomNavigationCustomizeAdapter(private val context: Context) :
    ListAdapter<NavigationModel, BottomNavigationCustomizeAdapter.BottomNavViewHolder>(DiffCallback) {
    var onItemClick: (Int) -> Unit = {}

    inner class BottomNavViewHolder(
        private val binding: ItemBottomNavigationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NavigationModel, position: Int) = with(binding) {

            // Apply 8dp rounded corners BEFORE loading image (so shimmer is also rounded)
            val cornerRadiusPx = UnitHelper.dpToPx(context, 8f)
            imvImage.clipToOutline = true
            imvImage.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx)
                }
            }

            if (item.isSelected) {
                vFocus.setBackgroundResource(R.drawable.bg_bottom_navi)
                imvImage.setBackgroundColor(Color.parseColor("#A1CCEF"))
                cvContent.strokeColor = Color.TRANSPARENT
            } else {
                vFocus.setBackgroundColor(context.getColor(android.R.color.transparent))
                imvImage.setBackgroundColor(Color.TRANSPARENT)
                cvContent.strokeColor = Color.TRANSPARENT
            }

            loadImage(root, item.imageNavigation, imvImage)

            root.tap { onItemClick.invoke(position) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomNavViewHolder {
        val binding = ItemBottomNavigationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BottomNavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomNavViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<NavigationModel>() {
            override fun areItemsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                // Nếu NavigationModel có id riêng thì nên so sánh id, ở đây tạm so sánh hình
                return oldItem.imageNavigation == newItem.imageNavigation
            }

            override fun areContentsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
