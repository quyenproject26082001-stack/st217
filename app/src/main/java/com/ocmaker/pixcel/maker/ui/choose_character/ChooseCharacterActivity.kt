package com.ocmaker.pixcel.maker.ui.choose_character

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.event.AdmobEvent
import com.lvt.ads.util.Admob
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.ui.customize.CustomizeCharacterActivity
import com.ocmaker.pixcel.maker.ui.home.DataViewModel
import com.ocmaker.pixcel.maker.ui.random_character.RandomCharacterActivity
import com.ocmaker.pixcel.maker.core.base.BaseActivity
import com.ocmaker.pixcel.maker.core.extensions.handleBackLeftToRight
import com.ocmaker.pixcel.maker.core.extensions.loadNativeCollabAds
import com.ocmaker.pixcel.maker.core.extensions.setImageActionBar
import com.ocmaker.pixcel.maker.core.extensions.setTextActionBar
import com.ocmaker.pixcel.maker.core.extensions.showInterAll
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.extensions.startIntentRightToLeft
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.core.helper.InternetHelper
import com.ocmaker.pixcel.maker.core.utils.key.IntentKey
import com.ocmaker.pixcel.maker.core.utils.key.ValueKey
import com.ocmaker.pixcel.maker.core.utils.state.HandleState
import com.ocmaker.pixcel.maker.databinding.ActivityChooseCharacterBinding
import kotlinx.coroutines.launch

class ChooseCharacterActivity : BaseActivity<ActivityChooseCharacterBinding>() {
    private val viewModel: ChooseCharacterViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val chooseCharacterAdapter by lazy { ChooseCharacterAdapter() }
    override fun setViewBinding(): ActivityChooseCharacterBinding {
        return ActivityChooseCharacterBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { data ->
                if (data.isNotEmpty()) {
                    chooseCharacterAdapter.submitList(data)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { showInterAll { handleBackLeftToRight() } }
        }
        chooseCharacterAdapter.onItemClick = { position ->
            AdmobEvent.logEvent(this@ChooseCharacterActivity, "click_item_$position", null)
            // ✅ FIX: Use isFromAPI flag from character data instead of position
            val selectedCharacter = dataViewModel.allData.value.getOrNull(position)
            val needsInternet = selectedCharacter?.isFromAPI ?: false

            if (needsInternet) {
                InternetHelper.checkInternet(this) { state ->
                    if (state == HandleState.SUCCESS) {
                        showInterAll { startIntentRightToLeft(CustomizeCharacterActivity::class.java, position) }
                    } else {
                        // Show No Internet dialog
                        val dialog = com.ocmaker.pixcel.maker.dialog.YesNoDialog(
                            this@ChooseCharacterActivity,
                            R.string.error,
                            R.string.please_check_your_internet,
                            isError = true
                        )
                        dialog.show()
                        dialog.onYesClick = {
                            dialog.dismiss()
                        }
                    }
                }
            } else {
                showInterAll { startIntentRightToLeft(CustomizeCharacterActivity::class.java, position) }
            }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.category))
        }
    }

    private fun initRcv() {
        binding.rcvCharacter.apply {
            adapter = chooseCharacterAdapter
            itemAnimator = null
        }
    }

    fun initNativeCollab() {
       // loadNativeCollabAds(R.string.native_cl_category, binding.flNativeCollab, binding.rcvCharacter)
    }

//    override fun initAds() {
//        initNativeCollab()
//        Admob.getInstance().loadNativeAd(
//            this,
//            getString(R.string.native_category),
//            binding.nativeAds,
//            R.layout.ads_native_banner
//        )
//    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

}