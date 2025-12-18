package com.ocmaker.pixcel.maker.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseActivity
import com.ocmaker.pixcel.maker.core.utils.state.HandleState
import com.ocmaker.pixcel.maker.databinding.ActivitySplashBinding
import com.ocmaker.pixcel.maker.ui.intro.IntroActivity
import com.ocmaker.pixcel.maker.ui.language.LanguageActivity
import com.ocmaker.pixcel.maker.ui.home.DataViewModel
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var intentActivity: Intent? = null
    private val dataViewModel: DataViewModel by viewModels()
    var interCallBack: InterCallback? = null

    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Start loading animation
        val rotateAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.rotate_loading)
        binding.ivLoading.startAnimation(rotateAnimation)

        Admob.getInstance().setOpenShowAllAds(false)
        if (!isTaskRoot &&
            intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
            intent.action != null &&
            intent.action.equals(Intent.ACTION_MAIN)) {
            finish(); return
        }

        intentActivity = if (sharePreference.getIsFirstLang()) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }
        Admob.getInstance().setTimeLimitShowAds(30000)
        Admob.getInstance().setOpenShowAllAds(false)
        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(intentActivity)
                finishAffinity()
            }
        }
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { dataList ->
                if (dataList.isNotEmpty()){
                    dataViewModel.getAllParts(this@SplashActivity).collect { dataAPI ->
                        when(dataAPI){
                            HandleState.LOADING -> {}
                            else -> {
                                Admob.getInstance().loadSplashInterAds(
                                    this@SplashActivity,
                                    getString(R.string.inter_splash),
                                    30000,
                                    2000,
                                    interCallBack
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun viewListener() {
    }

    override fun initText() {}

    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}

    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
    }
}