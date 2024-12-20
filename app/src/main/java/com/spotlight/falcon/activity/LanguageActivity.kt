package com.spotlight.falcon.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.contract.FalconApplication.Companion.toastFalcon
import com.spotlight.falcon.databinding.ActivityJoinResultBinding
import com.spotlight.falcon.databinding.ActivityLanguageBinding
import com.spotlight.falcon.datas.LanguageEvent
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon_ui.info.FalconLanguageInfo
import com.spotlight.falcon_ui.info.FalconTopBarUI
import com.spotlight.falcon_ui.ui.LanguageAdapter
import org.greenrobot.eventbus.EventBus
import java.util.Locale
import kotlin.system.exitProcess

class LanguageActivity : BaseFalconActivity(), FalconGesture {
    private lateinit var binding: ActivityLanguageBinding
    private var adapter: LanguageAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_language, back = {
            gestureBack()
        }) {
            binding = it as ActivityLanguageBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        finish()
    }

    override fun gestureInit(context: Context) {
        binding.languageTopBar.initIconAction(FalconTopBarUI(
            com.spotlight.falcon_ui.R.mipmap.jr_left_back,
            0,
            getString(com.spotlight.falcon_language.R.string.more_detail_language),
            clickLeft = {
                gestureBack()
            },
            clickRight = {}
        ))
        val language = DataStore.falconStorageLanguage
        adapter = LanguageAdapter(this, language)
        adapter?.updateList(mutableListOf(
            FalconLanguageInfo("English", "en"),
            FalconLanguageInfo("Français", "fr"),
            FalconLanguageInfo("Türkçe", "tr"),
            FalconLanguageInfo("हिंदी", "hi"),
        ))
        binding.languageList.adapter = adapter
        binding.languageSave.setOnClickListener {
            if (adapter == null) return@setOnClickListener
            DataStore.falconStorageLanguage = adapter!!.currentLanguage
            //进行语言全局化
            EventBus.getDefault().post(LanguageEvent(DataStore.falconStorageLanguage))
            falconResId.falconToPageFlag(this, MainActivity::class.java,
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK, true)
        }

    }
}