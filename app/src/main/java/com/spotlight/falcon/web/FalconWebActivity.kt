package com.spotlight.falcon.web

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.contract.FalconApplication.Companion.logFalcon
import com.spotlight.falcon.contract.MainResultContracts
import com.spotlight.falcon.databinding.ActivityFalconWebBinding
import com.spotlight.falcon.databinding.ActivityMainBinding
import com.spotlight.falcon.databinding.ActivityMoreDetailBinding
import com.spotlight.falcon.datas.FalconContent.webPolicyUrl
import com.spotlight.falcon.datas.FalconContent.webTermsUrl
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon_ui.info.FalconTopBarUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FalconWebActivity : BaseFalconActivity(), FalconGesture {
    private lateinit var binding: ActivityFalconWebBinding
    private var url: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_falcon_web, back = {
            gestureBack()
        }) {
            binding = it as ActivityFalconWebBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        finish()
    }

    override fun gestureInit(context: Context) {
        var type = ""
        intent.extras?.apply {
            type = getString("type")?:""
        }
        binding.webTopBar.initIconAction(FalconTopBarUI(
            com.spotlight.falcon_ui.R.mipmap.jr_left_back,
            0,
            if(type == "policy") getString(com.spotlight.falcon_language.R.string.web_policy) else
                getString(com.spotlight.falcon_language.R.string.web_terms),
            clickLeft = {
                gestureBack()
            },
            clickRight = {}
        ))
        url = if (type == "policy") {
            webPolicyUrl
        } else {
            webTermsUrl
        }
        binding.webView.webViewClient = WebViewClient()
        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
            }

        }
        binding.webView.loadUrl(url)
    }
}