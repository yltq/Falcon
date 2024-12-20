package com.spotlight.falcon_ui.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieDrawable.INFINITE
import com.spotlight.falcon_ui.R
import com.spotlight.falcon_ui.databinding.UiDisconnectDialogViewBinding
import com.spotlight.falcon_ui.databinding.UiDispositionConnectFailDialogViewBinding
import com.spotlight.falcon_ui.databinding.UiDispositionTestDialogViewBinding
import com.spotlight.falcon_ui.databinding.UiLoadingDialogBinding
import com.spotlight.falcon_ui.databinding.UiNoNetDialogViewBinding
import com.spotlight.falcon_ui.databinding.UiRegionLimitDialogViewBinding
import kotlin.math.roundToInt

class FalconUIDialog() {

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    private fun falconDialogView(context: Context, type: String): View? {
        return when(type) {
            "disconnect" -> LayoutInflater.from(context).inflate(
                R.layout.ui_disconnect_dialog_view, null, false)
            "loading" -> LayoutInflater.from(context).inflate(
                R.layout.ui_loading_dialog, null, false)
            "test" -> LayoutInflater.from(context).inflate(
                R.layout.ui_disposition_test_dialog_view, null, false)
            "connection_fail" -> LayoutInflater.from(context).inflate(
                R.layout.ui_disposition_connect_fail_dialog_view, null, false)
            "no_net" -> LayoutInflater.from(context).inflate(
                R.layout.ui_no_net_dialog_view, null, false)
            "limit" -> LayoutInflater.from(context).inflate(
                R.layout.ui_region_limit_dialog_view, null, false)
            else -> null
        }
    }

    fun falconDisconnectDialog(context: Context, disconnect: () -> Unit): Dialog? {
        val dialog = AlertDialog.Builder(context).setCancelable(false).create()
        val view = falconDialogView(context, "disconnect")?:return null
        val binding = UiDisconnectDialogViewBinding.bind(view)
        binding.disconnectTitle.text = context.getString(com.spotlight.falcon_language.R.string.disconnect_btn)
        binding.disconnectText.text = context.getString(com.spotlight.falcon_language.R.string.disposition_disconnect)
        binding.disconnectBtn.text = context.getString(com.spotlight.falcon_language.R.string.disconnect_btn)
        binding.cancelBtn.text = context.getString(com.spotlight.falcon_language.R.string.cancel_btn)
        binding.disconnectBtn.setOnClickListener {
            disconnect()
            dialog.dismiss()
        }
        binding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setView(binding.root)
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels - dpToPx(context, 24f)).roundToInt()
        val params = dialog.window?.attributes
        params?.width = width
        dialog.window?.attributes = params
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun falconLoadingDialog(context: Context): Dialog? {
        val dialog = AlertDialog.Builder(context).setCancelable(false).create()
        val view = falconDialogView(context, "loading")?:return null
        val binding = UiLoadingDialogBinding.bind(view)

        val animator = ObjectAnimator.ofFloat(binding.loadingIcon, "rotation", 0f, 360f)
        animator.duration = 1000
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = INFINITE
        animator.start()

        dialog.setView(binding.root)
        dialog.show()
        val width = dpToPx(context, 154f).roundToInt()
        val height = dpToPx(context, 165f).roundToInt()
        val params = dialog.window?.attributes
        params?.width = width
        params?.height = height
        dialog.window?.attributes = params
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun falconTestDialog(context: Context): Dialog? {
        val dialog = AlertDialog.Builder(context).setCancelable(false).create()
        val view = falconDialogView(context, "test")?:return null
        val binding = UiDispositionTestDialogViewBinding.bind(view)
        dialog.setView(binding.root)
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels - dpToPx(context, 24f)).roundToInt()
        val params = dialog.window?.attributes
        params?.width = width
        dialog.window?.attributes = params
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun falconConnectionFail(context: Context, retry: () -> Unit, feedback: () -> Unit): Dialog? {
        val dialog = AlertDialog.Builder(context).setCancelable(false).create()
        val view = falconDialogView(context, "connection_fail")?:return null
        val binding = UiDispositionConnectFailDialogViewBinding.bind(view)
        binding.retryBtn.setOnClickListener {
            retry()
            dialog.dismiss()
        }
        binding.sureBtn.setOnClickListener {
            dialog.dismiss()
        }
        binding.feedback.setOnClickListener {
            feedback()
        }
        dialog.setView(binding.root)
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels - dpToPx(context, 24f)).roundToInt()
        val params = dialog.window?.attributes
        params?.width = width
        dialog.window?.attributes = params
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun falconNoNet(context: Context): Dialog? {
        val dialog = AlertDialog.Builder(context).setCancelable(false).create()
        val view = falconDialogView(context, "no_net")?:return null
        val binding = UiNoNetDialogViewBinding.bind(view)
        binding.okBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setView(binding.root)
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels - dpToPx(context, 24f)).roundToInt()
        val params = dialog.window?.attributes
        params?.width = width
        dialog.window?.attributes = params
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    fun falconRegionLimit(context: Context, confirm: () -> Unit): Dialog? {
        val dialog = AlertDialog.Builder(context).setCancelable(false).create()
        val view = falconDialogView(context, "limit")?:return null
        val binding = UiRegionLimitDialogViewBinding.bind(view)
        binding.confirmBtn.setOnClickListener {
            confirm()
            dialog.dismiss()
        }
        dialog.setView(binding.root)
        dialog.show()
        val width = (context.resources.displayMetrics.widthPixels - dpToPx(context, 24f)).roundToInt()
        val params = dialog.window?.attributes
        params?.width = width
        dialog.window?.attributes = params
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}