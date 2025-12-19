package com.ocmaker.pixcel.maker.ui.random_character

import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.ui.customize.CustomizeCharacterActivity
import com.ocmaker.pixcel.maker.ui.customize.CustomizeCharacterViewModel
import com.ocmaker.pixcel.maker.ui.home.DataViewModel
import com.ocmaker.pixcel.maker.core.base.BaseActivity
import com.ocmaker.pixcel.maker.core.extensions.dLog
import com.ocmaker.pixcel.maker.core.extensions.eLog
import com.ocmaker.pixcel.maker.core.extensions.handleBackLeftToRight
import com.ocmaker.pixcel.maker.core.extensions.hideNavigation
import com.ocmaker.pixcel.maker.core.extensions.loadNativeCollabAds
import com.ocmaker.pixcel.maker.core.extensions.setImageActionBar
import com.ocmaker.pixcel.maker.core.extensions.setTextActionBar
import com.ocmaker.pixcel.maker.core.extensions.showInterAll
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.extensions.startIntentRightToLeft
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.core.helper.InternetHelper
import com.ocmaker.pixcel.maker.core.helper.MediaHelper
import com.ocmaker.pixcel.maker.core.utils.key.IntentKey
import com.ocmaker.pixcel.maker.core.utils.key.ValueKey
import com.ocmaker.pixcel.maker.data.model.custom.SuggestionModel
import com.ocmaker.pixcel.maker.databinding.ActivityRandomCharacterBinding
import com.ocmaker.pixcel.maker.dialog.YesNoDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.get
import kotlin.getValue
import kotlin.text.compareTo

class RandomCharacterActivity : BaseActivity<ActivityRandomCharacterBinding>() {
    private val viewModel: RandomCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val customizeCharacterViewModel: CustomizeCharacterViewModel by viewModels()
    private val randomCharacterAdapter by lazy { RandomCharacterAdapter(this) }

    override fun setViewBinding(): ActivityRandomCharacterBinding {
        return ActivityRandomCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            lifecycleScope.launch {
                dataViewModel.allData.collect { data ->
                    if (data.isNotEmpty()) {
                        initData()
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { showInterAll{handleBackLeftToRight()} }

        }

        randomCharacterAdapter.onItemClick = { model -> handleItemClick(model)}

    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.quick_maker_in))
        }
    }

    private fun initData() {
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            eLog("initData: ${throwable.message}")
            CoroutineScope(Dispatchers.Main).launch {
                dismissLoading()
                val dialogExit = YesNoDialog(this@RandomCharacterActivity, R.string.error, R.string.an_error_occurred)
                dialogExit.show()
                dialogExit.onNoClick = {
                    dialogExit.dismiss()
                    finish()
                }
                dialogExit.onYesClick = {
                    dialogExit.dismiss()
                    hideNavigation()
                    startIntentRightToLeft(
                        RandomCharacterActivity::class.java, customizeCharacterViewModel.positionSelected
                    )
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            showLoading()
            // Get data from list
            val deferred1 = async {
                val timeStart1 = System.currentTimeMillis()
                val size = if (dataViewModel.allData.value.size > ValueKey.POSITION_API){
                    dataViewModel.allData.value.size
                }else{
                    if (InternetHelper.isInternetAvailable(this@RandomCharacterActivity)) {
                        dataViewModel.allData.value.size
                    } else {
                        ValueKey.POSITION_API
                    }
                }

                dLog("==========================================================")
                dLog("RandomCharacter: Starting to process $size characters")
                dLog("Total data available: ${dataViewModel.allData.value.size}")
                dLog("POSITION_API: ${ValueKey.POSITION_API}")
                dLog("==========================================================")

                for (i in 0 until size) {
                    try {
                        dLog("---------- Processing Character $i ----------")
                        customizeCharacterViewModel.positionSelected = i
                        val currentData = dataViewModel.allData.value[i]
                        dLog("Character name: ${currentData.dataName}")
                        dLog("Avatar path: ${currentData.avatar}")
                        dLog("Layer count: ${currentData.layerList.size}")

                        customizeCharacterViewModel.setDataCustomize(currentData)
                        customizeCharacterViewModel.updateAvatarPath(currentData.avatar)

                        customizeCharacterViewModel.resetDataList()
                        customizeCharacterViewModel.addValueToItemNavList()
                        customizeCharacterViewModel.setItemColorDefault()
                        customizeCharacterViewModel.setBottomNavigationListDefault()

                        for (j in 0 until ValueKey.RANDOM_QUANTITY) {
                            customizeCharacterViewModel.setClickRandomFullLayer()
                            val suggestion = customizeCharacterViewModel.getSuggestionList()
                            dLog("Generated random $j for character $i - Avatar: ${suggestion.avatarPath}")
                            viewModel.updateRandomList(suggestion)
                        }
                        dLog("✓ Character $i completed successfully")
                    } catch (e: Exception) {
                        eLog("✗ ERROR processing character $i: ${e.message}")
                        e.printStackTrace()
                    }
                }
                viewModel.upsideDownList()

                dLog("==========================================================")
                dLog("RandomCharacter: Finished processing")
                dLog("Total time: ${System.currentTimeMillis() - timeStart1}ms")
                dLog("Final random list size: ${viewModel.randomList.size}")
                dLog("==========================================================")
                return@async true
            }

            withContext(Dispatchers.Main) {
                if (deferred1.await()) {
                    dismissLoading()
                    initRcv()
                }
            }
        }
    }

    private fun initRcv() {
        binding.rcvRandomCharacter.apply {
            adapter = randomCharacterAdapter
            itemAnimator = null
        }
        dLog("==========================================================")
        dLog("initRcv: Submitting ${viewModel.randomList.size} items to adapter")
        viewModel.randomList.forEachIndexed { index, item ->
            dLog("Item $index: Avatar=${item.avatarPath}, Layers=${item.pathSelectedList.size}")
        }
        dLog("==========================================================")
        randomCharacterAdapter.submitList(viewModel.randomList)
    }

    private fun handleItemClick(model: SuggestionModel) {
        customizeCharacterViewModel.positionSelected = dataViewModel.allData.value.indexOfFirst { it.avatar == model.avatarPath }
        // ✅ FIX: Use isFromAPI flag from character data instead of position
        val selectedCharacter = dataViewModel.allData.value.getOrNull(customizeCharacterViewModel.positionSelected)
        viewModel.setIsDataAPI(selectedCharacter?.isFromAPI ?: false)
        viewModel.checkDataInternet(this@RandomCharacterActivity) {
            lifecycleScope.launch {
                showLoading()
                withContext(Dispatchers.IO) {
                    MediaHelper.writeModelToFile(this@RandomCharacterActivity, ValueKey.SUGGESTION_FILE_INTERNAL, model)
                }
                val intent = Intent(this@RandomCharacterActivity, CustomizeCharacterActivity::class.java)
                intent.putExtra(IntentKey.INTENT_KEY, customizeCharacterViewModel.positionSelected)
                intent.putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.SUGGESTION)
                val option = ActivityOptions.makeCustomAnimation(
                    this@RandomCharacterActivity,
                    R.anim.slide_out_left,
                    R.anim.slide_in_right
                )
                dismissLoading()
                showInterAll { startActivity(intent, option.toBundle()) }
            }
        }
    }
//
//    fun initNativeCollab() {
//        loadNativeCollabAds(R.string.native_cl_random, binding.flNativeCollab, binding.rcvRandomCharacter)
//    }
//
//    override fun initAds() {
//        initNativeCollab()
//    }

    override fun onRestart() {
        super.onRestart()
        //initNativeCollab()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideNavigation(false)
        }
    }
}