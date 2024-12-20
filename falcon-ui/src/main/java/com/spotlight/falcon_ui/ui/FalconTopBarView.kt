package com.spotlight.falcon_ui.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.spotlight.falcon_ui.R
import com.spotlight.falcon_ui.databinding.UiTopBarViewBinding
import com.spotlight.falcon_ui.info.FalconTopBarUI

class FalconTopBarView(var mContext: Context, val mAttr: AttributeSet?): LinearLayoutCompat(mContext, mAttr) {
    private lateinit var binding: UiTopBarViewBinding

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.ui_top_bar_view, this, true)
        binding = UiTopBarViewBinding.bind(view)
    }

    fun initIconAction(topBarUI: FalconTopBarUI) {
        binding.topBarTitle.text = topBarUI.title
        topBarUI.leftIcon.takeIf { it > 0 }?.let {
            binding.topBarLeftIcon.setImageResource(it)
        }
        topBarUI.rightIcon.takeIf { it > 0 }?.let {
            binding.topBarRightIcon.setImageResource(it)
        }

        binding.topBarLeftIcon.setOnClickListener {
            topBarUI.clickLeft.invoke()
        }
        binding.topBarRightIcon.setOnClickListener {
            topBarUI.clickRight.invoke()
        }
    }

    fun hideRightIcon(b: Boolean) {
        binding.topBarRightIcon.visibility = if (b) View.INVISIBLE else View.VISIBLE
    }
}