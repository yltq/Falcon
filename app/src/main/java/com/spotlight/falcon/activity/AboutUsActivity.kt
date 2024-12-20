package com.spotlight.falcon.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spotlight.falcon.BuildConfig
import com.spotlight.falcon.R
import com.spotlight.falcon.databinding.ActivityAboutUsBinding
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon.web.FalconWebActivity
import com.spotlight.falcon_ui.info.FalconTopBarUI

class AboutUsActivity : BaseFalconActivity(), FalconGesture {
    private lateinit var binding: ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_about_us, back = {
            gestureBack()
        }) {
            binding = it as ActivityAboutUsBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        finish()
    }

    override fun gestureInit(context: Context) {
        binding.aboutUsTopBar.initIconAction(FalconTopBarUI(
            com.spotlight.falcon_ui.R.mipmap.jr_left_back,
            0,
            getString(com.spotlight.falcon_language.R.string.about_us),
            clickLeft = {
                gestureBack()
            },
            clickRight = {}
        ))
        binding.aboutUsVersion.text = "V" + BuildConfig.VERSION_NAME
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