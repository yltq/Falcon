package com.spotlight.falcon.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.spotlight.falcon.R
import com.spotlight.falcon.databinding.ActivityIntroductionBinding
import com.spotlight.falcon.databinding.ActivityJoinResultBinding
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon_ui.info.FalconTopBarUI

class JoinResultActivity : BaseFalconActivity(), FalconGesture {
    private lateinit var binding: ActivityJoinResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_join_result, back = {
            gestureBack()
        }) {
            binding = it as ActivityJoinResultBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        finish()
    }

    override fun gestureInit(context: Context) {
        binding.jrTopBar.initIconAction(FalconTopBarUI(com.spotlight.falcon_ui.R.mipmap.jr_left_back,
            0,
            getString(com.spotlight.falcon_language.R.string.jr_title),
            clickLeft = {
                gestureBack()
            },
            clickRight = {})
        )
        val bundle = intent.extras
        val ip = bundle?.getString("ip") ?: ""
        val time = bundle?.getString("time") ?: "00:00:00"
        binding.jrBottomTime.text = time
        if (!TextUtils.isEmpty(ip)) {
            binding.jrIp.text = ip
            binding.jrIpGroup.visibility = View.VISIBLE
        } else {
            binding.jrIpGroup.visibility = View.GONE
        }
    }
}