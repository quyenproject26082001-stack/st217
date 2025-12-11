package com.oc.space.ocmaker.creete.ui

import android.view.LayoutInflater
import com.oc.space.ocmaker.creete.R
import com.oc.space.ocmaker.creete.core.base.BaseActivity
import com.oc.space.ocmaker.creete.core.extensions.gone
import com.oc.space.ocmaker.creete.core.extensions.handleBackLeftToRight
import com.oc.space.ocmaker.creete.core.extensions.policy
import com.oc.space.ocmaker.creete.core.extensions.select
import com.oc.space.ocmaker.creete.core.extensions.setImageActionBar
import com.oc.space.ocmaker.creete.core.extensions.setTextActionBar
import com.oc.space.ocmaker.creete.core.extensions.shareApp
import com.oc.space.ocmaker.creete.core.extensions.startIntentRightToLeft
import com.oc.space.ocmaker.creete.core.extensions.visible
import com.oc.space.ocmaker.creete.core.utils.key.IntentKey
import com.oc.space.ocmaker.creete.core.utils.state.RateState
import com.oc.space.ocmaker.creete.databinding.ActivitySettingsBinding
import com.oc.space.ocmaker.creete.ui.language.LanguageActivity
import com.oc.space.ocmaker.creete.core.extensions.tap
import com.oc.space.ocmaker.creete.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
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