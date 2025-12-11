package com.oc.space.ocmaker.creete.ui.my_creation.fragment

import android.app.ActivityOptions
import android.content.Intent
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
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.key.ValueKey
import com.oc.space.ocmaker.creete.core.utils.state.HandleState
import com.oc.space.ocmaker.creete.databinding.FragmentMyDesignBinding
import com.oc.space.ocmaker.creete.dialog.YesNoDialog
import com.oc.space.ocmaker.creete.ui.customize.CustomizeCharacterActivity
import com.oc.space.ocmaker.creete.ui.home.DataViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.MyCreationActivity
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyCreationViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.adapter.MyAvatarAdapter
import com.oc.space.ocmaker.creete.ui.my_creation.adapter.MyDesignAdapter
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyAvatarViewModel
import com.oc.space.ocmaker.creete.ui.my_creation.view_model.MyDesignViewModel
import com.oc.space.ocmaker.creete.ui.view.ViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class MyDesignFragment : BaseFragment<FragmentMyDesignBinding>() {
    private val viewModel: MyDesignViewModel by viewModels()
    private val myCreationViewModel: MyCreationViewModel by activityViewModels()
    private val myDesignAdapter by lazy { MyDesignAdapter() }

    private val myAlbumActivity: MyCreationActivity
        get() = requireActivity() as MyCreationActivity

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyDesignBinding {
        return FragmentMyDesignBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        initRcv()
        viewModel.loadMyDesign(myAlbumActivity)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.myDesignList.collect { list ->
                        myDesignAdapter.submitList(list)
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
            rcvMyDesign.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
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
            // Action bar buttons disabled - no delete button for MyDesign tab yet
            // myAlbumActivity.binding.actionBar.btnActionBarRight.tap { handleSelectAll() }
            // myAlbumActivity.binding.actionBar.btnActionBarNextToRight.tap { handleDelete(viewModel.getPathSelected()) }

            // Share and Download buttons are handled in MyCreationActivity

            myDesignAdapter.onItemClick = { pathInternal -> handleItemClick(pathInternal) }
            myDesignAdapter.onItemTick = { position -> viewModel.toggleSelect(position) }
            myDesignAdapter.onDeleteClick = { pathInternal -> handleDelete(arrayListOf(pathInternal)) }
            myDesignAdapter.onLongClick = { position -> handleLongClick(position) }
        }
    }

    private fun initRcv() {
        binding.apply {
            rcvMyDesign.apply {
                adapter = myDesignAdapter
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
        }
        dialog.onNoClick = {
            dialog.dismiss()
            myAlbumActivity.hideNavigation()
        }
        dialog.onYesClick = {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.deleteItem(pathInternalList)
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    myAlbumActivity.hideNavigation()
                    resetData()
                }
            }
        }
    }

    private fun handleItemClick(pathInternal: String) {
        val intent = Intent(myAlbumActivity, ViewActivity::class.java)
        intent.putExtra(IntentKey.INTENT_KEY, pathInternal)
        intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
        intent.putExtra(IntentKey.STATUS_KEY, ValueKey.MY_DESIGN_TYPE)
        val options = ActivityOptions.makeCustomAnimation(myAlbumActivity, R.anim.slide_in_right, R.anim.slide_out_left)
        myAlbumActivity.showInterAll { startActivity(intent, options.toBundle()) }
    }

    private fun handleLongClick(position: Int) {
        viewModel.showLongClick(position)
        // Show deleteSection and bottom bar
        myAlbumActivity.binding.lnlBottom.visible()
        myAlbumActivity.enterSelectionMode()
    }

    private fun resetData() {
        viewModel.loadMyDesign(myAlbumActivity)
        // Hide deleteSection and bottom bar
        myAlbumActivity.binding.lnlBottom.gone()
        myAlbumActivity.exitSelectionMode()
    }

    fun getSelectedPaths(): ArrayList<String> {
        return viewModel.getPathSelected()
    }

    fun deleteSelectedItems() {
        handleDelete(viewModel.getPathSelected())
    }

    fun resetSelectionMode() {
        viewModel.clearSelection()
        myAlbumActivity.binding.lnlBottom.gone()
        myAlbumActivity.exitSelectionMode()
        myDesignAdapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        resetData()
    }
}