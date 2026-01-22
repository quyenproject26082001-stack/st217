package com.ocmaker.pony.ui.customize

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ironsource.adqualitysdk.sdk.i.ct
import com.ocmaker.pony.R
import com.ocmaker.pony.core.extensions.dp
import com.ocmaker.pony.core.extensions.loadImage
import com.ocmaker.pony.core.extensions.setMargins
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.core.helper.UnitHelper
import com.ocmaker.pony.data.model.custom.NavigationModel
import com.ocmaker.pony.databinding.ItemBottomNavigationBinding
import kotlin.math.roundToInt

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

            val offset = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15f,
                cvContent.resources.displayMetrics
            )


            if (item.isSelected) {
              //  vFocus.setBackgroundResource(R.drawable.bg_bottom_navi)
                imvImage.setBackgroundColor(Color.TRANSPARENT)
                cvContent.strokeColor = Color.TRANSPARENT
                cvContent.setBackgroundResource(R.drawable.bg_select_navi)
               // binding.main.setMargins(0, 5.dp(context),8.dp(context), 25.dp(context))

                cvContent.translationZ = 50f
                cvContent.animate()
                    .translationY(-offset)
                    .setDuration(180)
                    .start()

            } else {
                binding.main.setMargins(0, 15.dp(context),8.dp(context), 15.dp(context))

                //   vFocus.setBackgroundColor(context.getColor(android.R.color.transparent))
                imvImage.setBackgroundColor(Color.TRANSPARENT)
                cvContent.setBackgroundResource(R.drawable.bg_uslt_navi)
                cvContent.animate()
                    .translationY(0f)
                    .setDuration(180)
                    .start()
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
