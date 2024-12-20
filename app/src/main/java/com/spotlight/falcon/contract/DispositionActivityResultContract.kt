package com.spotlight.falcon.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.spotlight.falcon.activity.DispositionActivity

class DispositionActivityResultContract : ActivityResultContract<Void?, Map<String, String>>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        val intent = Intent(context, DispositionActivity::class.java)
        return intent
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Map<String, String> {
        return if (resultCode == Activity.RESULT_OK) {
            val map = mutableMapOf<String, String>()
            intent?.apply {
                val type = getStringExtra("type")?:""
                map.put("type", type)
                map.put("nodeIp", getStringExtra("nodeIp")?:"")
                map.put("nodePort", getIntExtra("nodePort", 8800).toString())
                map.put("nodeMethod", getStringExtra("nodeMethod")?:"")
                map.put("nodePassword", getStringExtra("nodePassword")?:"")
                map.put("nodeParentName", getStringExtra("nodeParentName")?:"")
                map.put("nodeChildName", getStringExtra("nodeChildName")?:"")
                map.put("nodeParentCode", getStringExtra("nodeParentCode")?:"")
                map.put("nodeInFast", getStringExtra("nodeInFast")?:"")
            }
            map
        } else {
            mutableMapOf()
        }
    }
}