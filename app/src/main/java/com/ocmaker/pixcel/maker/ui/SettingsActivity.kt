package com.ocmaker.pixcel.maker.ui

import android.view.LayoutInflater
import com.ocmaker.pixcel.maker.R
import com.ocmaker.pixcel.maker.core.base.BaseActivity
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.handleBackLeftToRight
import com.ocmaker.pixcel.maker.core.extensions.policy
import com.ocmaker.pixcel.maker.core.extensions.select
import com.ocmaker.pixcel.maker.core.extensions.setImageActionBar
import com.ocmaker.pixcel.maker.core.extensions.setTextActionBar
import com.ocmaker.pixcel.maker.core.extensions.shareApp
import com.ocmaker.pixcel.maker.core.extensions.startIntentRightToLeft
import com.ocmaker.pixcel.maker.core.extensions.visible
import com.ocmaker.pixcel.maker.core.utils.key.IntentKey
import com.ocmaker.pixcel.maker.core.utils.state.RateState
import com.ocmaker.pixcel.maker.databinding.ActivitySettingsBinding
import com.ocmaker.pixcel.maker.ui.language.LanguageActivity
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.tvMusic.select()
        initRate()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.settings))
        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}