package com.spotlight.falcon.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.contract.FalconApplication.Companion.logFalcon
import com.spotlight.falcon.contract.FalconApplication.Companion.toastFalcon
import com.spotlight.falcon.contract.MainResultContracts
import com.spotlight.falcon.databinding.ActivityMainBinding
import com.spotlight.falcon.databinding.ActivityMoreDetailBinding
import com.spotlight.falcon.datas.FalconContent.falconEmail
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon.web.FalconWebActivity
import com.spotlight.falcon_ui.info.FalconTopBarUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoreDetailActivity : BaseFalconActivity(), FalconGesture {
    private lateinit var binding: ActivityMoreDetailBinding
    private val serverRegister = registerForActivityResult(MainResultContracts()) {
        if (!it) return@registerForActivityResult
        binding.moreDetailAutomaticIcon.isSelected = !binding.moreDetailAutomaticIcon.isSelected
        DataStore.falconStorageAutomatic = binding.moreDetailAutomaticIcon.isSelected
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_more_detail, back = {
            gestureBack()
        }) {
            binding = it as ActivityMoreDetailBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        finish()
    }

    override fun gestureInit(context: Context) {
        binding.moreDetailTopBar.initIconAction(FalconTopBarUI(
            com.spotlight.falcon_ui.R.mipmap.jr_left_back,
            0,
            getString(com.spotlight.falcon_language.R.string.more_detail_title),
            clickLeft = {
                gestureBack()
            },
            clickRight = {}
        ))
        binding.moreDetailAutomaticIcon.isSelected = DataStore.falconStorageAutomatic
        binding.moreDetailGroupAutomatic.setOnClickListener {
            serverRegister.launch(null)
        }
        binding.moreDetailGroupLanguage.setOnClickListener {
            falconResId.falconToPage(this, LanguageActivity::class.java)
        }
        binding.moreDetailGroupShare.setOnClickListener {
            val share = "https://play.google.com/store/apps/details?id=${packageName}"
            Intent().also {
                it.action = Intent.ACTION_SEND
                it.putExtra(Intent.EXTRA_TEXT, share)
                it.type = "text/plain"
                startActivity(Intent.createChooser(it, "Share link"))
            }
        }
        binding.moreDetailGroupEvaluateUs.setOnClickListener {
            fun openAppInStore(packageName: String) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=$packageName")
                }

                // 检查是否有应用可以处理这个 Intent
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent) // 启动商店应用
                } else {
                    // 如果没有安装 Google Play 商店，可以使用浏览器打开
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                    startActivity(webIntent)
                }
            }
            openAppInStore(context.packageName)
        }
        binding.moreDetailGroupContactUs.setOnClickListener {
            fun sendEmail(email: String) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$email")
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    getString(com.spotlight.falcon_language.R.string.more_detail_no_email).toastFalcon()
                }
            }
            sendEmail(falconEmail)
        }
        binding.moreDetailGroupAboutUs.setOnClickListener {
            falconResId.falconToPage(this, AboutUsActivity::class.java)
        }
        binding.aboutUsPolicy.setOnClickListener {
            falconResId.falconToPageOwnParams(this, FalconWebActivity::class.java, Bundle().apply {
                putString("type", "policy")
            })
        }
        binding.aboutUsTerms.setOnClickListener {
            falconResId.falconToPageOwnParams(this, FalconWebActivity::class.java, Bundle().apply {
                putString("type", "terms")
            })
        }
    }
}