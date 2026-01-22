package com.ocmaker.pony.ui

import android.view.LayoutInflater
import com.ocmaker.pony.R
import com.ocmaker.pony.core.base.BaseActivity
import com.ocmaker.pony.core.extensions.gone
import com.ocmaker.pony.core.extensions.handleBackLeftToRight
import com.ocmaker.pony.core.extensions.policy
import com.ocmaker.pony.core.extensions.select
import com.ocmaker.pony.core.extensions.setImageActionBar
import com.ocmaker.pony.core.extensions.setTextActionBar
import com.ocmaker.pony.core.extensions.shareApp
import com.ocmaker.pony.core.extensions.startIntentRightToLeft
import com.ocmaker.pony.core.extensions.visible
import com.ocmaker.pony.core.utils.key.IntentKey
import com.ocmaker.pony.core.utils.state.RateState
import com.ocmaker.pony.databinding.ActivitySettingsBinding
import com.ocmaker.pony.ui.language.LanguageActivity
import com.ocmaker.pony.core.extensions.tap
import com.ocmaker.pony.core.helper.RateHelper
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