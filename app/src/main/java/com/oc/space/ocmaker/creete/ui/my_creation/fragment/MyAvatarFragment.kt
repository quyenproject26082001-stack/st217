package com.oc.space.ocmaker.creete.ui.my_creation.fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseFragment
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.hideNavigation
import com.oc.space.ocmaker.creete.core.extensions.invisible
import com.oc.space.ocmaker.creete.core.extensions.showInterAll
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.helper.LanguageHelper
import com.oc.space.ocmaker.creete.core.helper.MediaHelper
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import com.oc.space.ocmaker.creete.core.utils.state.HandleState
import com.oc.space.ocmaker.creete.databinding.FragmentMyAvatarBinding
import com.oc.space.ocmaker.creete.dialog.YesNoDialog
import com.oc.space.ocmaker.creete.ui.customize.CustomizeCharacterActivity
import com.oc.space.ocmaker.creete.ui.customize.CustomizeCharacterViewModel
import com.oc.space.ocmaker.creete.ui.home.DataViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.MyCreationActivity
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyCreationViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.adapter.MyAvatarAdapter
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyAvatarViewModel
import com.oc.space.ocmaker.creete.ui.view.ViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyAvatarFragment : BaseFragment<FragmentMyAvatarBinding>() {
    private val viewModel: MyAvatarViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val myCreationViewModel: MyCreationViewModel by activityViewModels()
    private val myAvatarAdapter by lazy { MyAvatarAdapter(requireActivity()) }

    private val myAlbumActivity: MyCreationActivity
        get() = requireActivity() as MyCreationActivity

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyAvatarBinding {
        return FragmentMyAvatarBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        initRcv()
        dataViewModel.ensureData(myAlbumActivity)
        viewModel.loadMyAvatar(myAlbumActivity)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.myAvatarList.collect { list ->
                        myAvatarAdapter.submitList(list)
                        binding.layoutNoItem.isVisible = list.isEmpty()
                    }
                }
                // Removed - action bar buttons are disabled
                // launch {
                //     viewModel.isLastItem.collect { selectStatus ->
                //         myAlbumActivity.changeImageActionBarRight(selectStatus)
                //     }
                // }
                launch {
                    myCreationViewModel.typeStatus.collect { status ->
                        resetData()
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            rcvMyAvatar.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(
                    recyclerView: RecyclerView, motionEvent: MotionEvent
                ): Boolean {
                    return when {
                        motionEvent.action != MotionEvent.ACTION_UP || recyclerView.findChildViewUnder(
                            motionEvent.x, motionEvent.y
                        ) != null -> false

                        else -> {
                            resetData()
                            true
                        }
                    }
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {}
            })
            // Action bar buttons disabled - using btnDeleteSelect instead
            // myAlbumActivity.binding.actionBar.btnActionBarRight.tap { handleSelectAll() }
            // myAlbumActivity.binding.actionBar.btnActionBarNextToRight.tap { handleDelete(viewModel.getPathSelected()) }

            // Share and Download buttons are handled in MyCreationActivity

            myAvatarAdapter.onItemClick = { pathInternal -> handleItemClick(pathInternal) }
            myAvatarAdapter.onItemTick = { position ->
                viewModel.toggleSelect(position)
                // Check if all items are now selected and update the icon
                val allSelected = viewModel.myAvatarList.value.all { it.isSelected }
                myAlbumActivity.updateSelectAllIcon(allSelected)
            }
            myAvatarAdapter.onEditClick = { pathInternal -> handleEditClick(pathInternal) }
            myAvatarAdapter.onDeleteClick = { pathInternal -> handleDelete(arrayListOf(pathInternal)) }
            myAvatarAdapter.onLongClick = { position -> handleLongClick(position) }
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvMyAvatar.apply {
                adapter = myAvatarAdapter
                itemAnimator = null
            }
        }
    }

    private fun handleDelete(pathInternalList: ArrayList<String>) {
        if (pathInternalList.isEmpty()) {
            myAlbumActivity.showToast(R.string.please_select_an_image)
            return
        }
        val dialog = YesNoDialog(myAlbumActivity, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        LanguageHelper.setLocale(myAlbumActivity)
        dialog.show()
        dialog.onDismissClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
            // Exit selection mode when dialog is dismissed
            resetData()
        }
        dialog.onNoClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
            // Exit selection mode when user cancels
            resetData()
        }
        dialog.onYesClick = {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteItem(myAlbumActivity, pathInternalList)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    myAlbumActivity.hideNavigation()
                    // Exit selection mode and reload data
                    resetData()
                }
            }
        }
    }

    private fun handleEditClick(pathInternal: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            myAlbumActivity.showLoading()
            viewModel.editItem(myAlbumActivity, pathInternal, dataViewModel.allData.value)
            withContext(Dispatchers.Main) {
                myAlbumActivity.dismissLoading()
                viewModel.checkDataInternet(myAlbumActivity) {
                    val intent = Intent(myAlbumActivity, CustomizeCharacterActivity::class.java)
                    intent.putExtra(IntentKey.INTENT_KEY, viewModel.positionCharacter)
                    intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
                    val option = ActivityOptions.makeCustomAnimation(
                        myAlbumActivity, R.anim.slide_out_left, R.anim.slide_in_right
                    )
                    myAlbumActivity.showInterAll { startActivity(intent, option.toBundle()) }
                }
            }
        }
    }

    private fun handleItemClick(pathInternal: String) {
        val intent = Intent(myAlbumActivity, ViewActivity::class.java)
        intent.putExtra(IntentKey.INTENT_KEY, pathInternal)
        intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
        intent.putExtra(IntentKey.STATUS_KEY, ValueKey.AVATAR_TYPE)
        val options = ActivityOptions.makeCustomAnimation(myAlbumActivity, R.anim.slide_in_right, R.anim.slide_out_left)
        myAlbumActivity.showInterAll { startActivity(intent, options.toBundle()) }
    }

    private fun handleLongClick(position: Int) {
        viewModel.showLongClick(position)
        // Show deleteSection and bottom bar
        myAlbumActivity.binding.lnlBottom.visible()
        myAlbumActivity.enterSelectionMode()
        // Enable select mode margins in adapter
        myAvatarAdapter.isSelectMode = true
    }

    private fun resetData() {
        viewModel.loadMyAvatar(myAlbumActivity)
        // Hide deleteSection and bottom bar
        myAlbumActivity.binding.lnlBottom.gone()
        myAlbumActivity.exitSelectionMode()
        // Disable select mode margins in adapter
        myAvatarAdapter.isSelectMode = false
    }

    fun deleteSelectedItems() {
        handleDelete(viewModel.getPathSelected())
    }

    fun getSelectedPaths(): ArrayList<String> {
        return viewModel.getPathSelected()
    }

    fun selectAllItems() {
        viewModel.selectAll(true)
        myAvatarAdapter.notifyDataSetChanged()
    }

    fun deselectAllItems() {
        viewModel.selectAll(false)
        myAvatarAdapter.notifyDataSetChanged()
    }

    fun exitSelectMode() {
        myAvatarAdapter.isSelectMode = false
    }

    fun resetSelectionMode() {
        viewModel.clearSelection()
        myAlbumActivity.binding.lnlBottom.gone()
        myAlbumActivity.exitSelectionMode()
        myAvatarAdapter.isSelectMode = false
        myAvatarAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        resetData()
    }
}