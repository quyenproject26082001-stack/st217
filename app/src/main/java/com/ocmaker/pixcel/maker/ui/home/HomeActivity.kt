package com.ocmaker.pixcel.maker.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.util.Admob
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseActivity
import com.ocmaker.pixcel.maker.core.extensions.hideNavigation
import com.ocmaker.pixcel.maker.core.extensions.loadNativeCollabAds
import com.ocmaker.pixcel.maker.core.extensions.rateApp
import com.ocmaker.pixcel.maker.core.extensions.select
import com.ocmaker.pixcel.maker.core.extensions.setImageActionBar
import com.ocmaker.pixcel.maker.core.extensions.showInterAll
import com.ocmaker.pixcel.maker.core.extensions.startIntentRightToLeft
import com.ocmaker.pixcel.maker.core.helper.LanguageHelper
import com.ocmaker.pixcel.maker.core.helper.MediaHelper
import com.ocmaker.pixcel.maker.core.utils.key.ValueKey
import com.ocmaker.pixcel.maker.core.utils.state.RateState
import com.ocmaker.pixcel.maker.databinding.ActivityHomeBinding
import com.ocmaker.pixcel.maker.ui.SettingsActivity
import com.ocmaker.pixcel.maker.ui.my_creation.MyCreationActivity
import com.ocmaker.pixcel.maker.ui.choose_character.ChooseCharacterActivity
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.extensions.strings
import com.ocmaker.pixcel.maker.ui.random_character.RandomCharacterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        sharePreference.setCountBack(sharePreference.getCountBack() + 1)
        deleteTempFolder()
        binding.tv1.isSelected = true
        binding.tv3.isSelected = true
        binding.tv2.isSelected = true

        // Apply elastic bounce animation to app name
        val elasticBounce = AnimationUtils.loadAnimation(this, R.anim.elastic_bounce)
        binding.imvAppName.startAnimation(elasticBounce)
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarRight.tap(2000) { startIntentRightToLeft(SettingsActivity::class.java) }
            btnCreate.tap(2000) { startIntentRightToLeft(ChooseCharacterActivity::class.java) }
            btnMyAlbum.tap(2000) { showInterAll { startIntentRightToLeft(MyCreationActivity::class.java) } }
            btnQuickMaker.tap(2000) { startIntentRightToLeft(RandomCharacterActivity::class.java) }
        }
    }

    override fun initText() {
        super.initText()
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarRight, R.drawable.ic_settings)
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 == 0) {
            rateApp(sharePreference) { state ->
                if (state != RateState.CANCEL) {
                    showToast(R.string.have_rated)
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        exitProcess(0)
                    }
                }
            }
        } else {
            exitProcess(0)
        }
    }

    private fun deleteTempFolder() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataTemp = MediaHelper.getImageInternal(this@HomeActivity, ValueKey.RANDOM_TEMP_ALBUM)
            if (dataTemp.isNotEmpty()) {
                dataTemp.forEach {
                    val file = File(it)
                    file.delete()
                }
            }
        }
    }

    private fun updateText() {
        binding.apply {
            tv1.text = strings(R.string.character_maker)
            tv2.text = strings(R.string.quick_maker)
            tv3.text = strings(R.string.my_character)
        }
    }

    override fun onRestart() {
        super.onRestart()
        deleteTempFolder()
        LanguageHelper.setLocale(this)
        updateText()
        //initNativeCollab()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
        startStaggeredAnimations()

        }
    }

    private fun startStaggeredAnimations() {
        // Card 1: Slide from right (no delay)
        val slideFromRight1 = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_home)
        binding.btnCreate.startAnimation(slideFromRight1)
        binding.tv1.startAnimation(slideFromRight1)


        // Card 2: Slide from left (200ms delay)
        val slideFromLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left_home)
        binding.btnQuickMaker.postDelayed({
            binding.btnQuickMaker.startAnimation(slideFromLeft)
            binding.tv2.startAnimation(slideFromLeft)
        }, 200)

        // Card 3: Slide from right (400ms delay)
        val slideFromRight2 = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_home)
        binding.btnMyAlbum.postDelayed({
            binding.btnMyAlbum.startAnimation(slideFromRight2)
            binding.tv3.startAnimation(slideFromRight2)
        }, 400)
    }

//    fun initNativeCollab() {
//        loadNativeCollabAds(R.string.native_cl_home, binding.flNativeCollab, binding.scvMain)
//    }

//    override fun initAds() {
//        initNativeCollab()
//        Admob.getInstance().loadInterAll(this, getString(R.string.inter_all))
//        Admob.getInstance().loadNativeAll(this, getString(R.string.native_all))
//    }
}