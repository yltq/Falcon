package com.spotlight.falcon.activity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.databinding.ActivityIntroductionBinding
import com.spotlight.falcon.datas.FalconContent
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.resid.FalconResId
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntroductionActivity : BaseFalconActivity(), FalconGesture, ShadowsocksConnection.Callback {
    private lateinit var binding: ActivityIntroductionBinding
    private lateinit var connection: ShadowsocksConnection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection = ShadowsocksConnection(true).apply {
            bandwidthTimeout = 500L
            connect(this@IntroductionActivity, this@IntroductionActivity)
        }
        falconResId.getResId(this, R.layout.activity_introduction, back = {
            gestureBack()
        }) {
            binding = it as ActivityIntroductionBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.disconnect(this)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val animator = ValueAnimator.ofInt(0, 100)
            animator.duration = 2000

            animator.addUpdateListener { animation ->
                val progress = animation.animatedValue as Int
                binding.introLine.progress = progress
                if (progress == 100) {
                    cancel()
                    if (lifecycle.currentState != Lifecycle.State.RESUMED) return@addUpdateListener
                    falconResId.falconToPage(this@IntroductionActivity, MainActivity::class.java, true)
                }
            }
            animator.start() // 启动动画
        }
    }

    override fun gestureBack() {

    }

    override fun gestureInit(context: Context) {

    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {

    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        FalconContent.falconConnectService = state
        when(state) {
            BaseService.State.Connected -> {
                FalconContent.falconConnect = true
            }
            BaseService.State.Stopped -> FalconContent.falconConnect = false
            else -> {}
        }
    }

}