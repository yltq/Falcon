package com.spotlight.falcon.contract

import android.app.ActivityManager
import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.webkit.WebView
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.BuildConfig
import com.spotlight.falcon.contract.FalconApplication.Companion.logFalcon
import com.spotlight.falcon.datas.FalconContent
import com.spotlight.falcon_ui.info.DispositionChildNode
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FalconUtils(val context: Context) {
    fun checkFalconProcess(loadFalconEnable: (Boolean) -> Unit) {
        val currentProcessId = android.os.Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfo =
            activityManager.runningAppProcesses.find { it.pid == currentProcessId }
        val processEnable = processInfo?.processName == context.packageName
        loadFalconEnable.invoke(processEnable)
    }

    fun finishInTrue() {
        processJson(DataStore.falconStorageVPN)
        falconCountryCode()
        falconServer()
    }

    fun finishInFalse() {
        "not in main process".logFalcon()
    }

    var falconLoadingServer: Boolean = false

    fun processString(s: String): String? {
        // 1. 去掉头部 19 个字符
        val trimmedString = s.drop(19)

        // 2. 大小写互换
        val swappedCaseString = trimmedString.map {
            if (it.isUpperCase()) it.toLowerCase() else it.toUpperCase()
        }.joinToString("")

        // 3. Base64 解码
        return try {
            val decodedBytes = Base64.decode(swappedCaseString, 0)
            String(decodedBytes)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun processJson(s: String) {
        fun processFast(array: JSONArray?) {
            if (array == null || array.length() == 0) return
            val list: MutableList<DispositionChildNode> = mutableListOf()
            for (i in 0 until array.length()) {
                kotlin.runCatching {
                    val item = array[i] as JSONObject
                    val nodeIp = item.optString("agbxRsWxLC")
                    val nodePort = item.optInt("Jasy")
                    val nodePassword = item.optString("UAWwqvI")
                    val nodeMethod = item.optString("ShyWL")
                    val nodeChildName = item.optString("eexBqcMnG")
                    val nodeParentName = item.optString("odzRrQ")
                    val nodeParentCode = item.optString("YHcPgsR")

                    val node = DispositionChildNode(
                        nodeIp,
                        nodePort,
                        nodeMethod,
                        nodePassword,
                        nodeParentName,
                        nodeChildName,
                        nodeParentCode,
                        "fast"
                    )
                    list.add(node)
                }
            }
            FalconContent.updateFalconServerList("fast", list)
        }

        fun processServer(array: JSONArray?) {
            if (array == null || array.length() == 0) return
            val list: MutableList<DispositionChildNode> = mutableListOf()
            val game: MutableList<DispositionChildNode> = mutableListOf()
            for (i in 0 until array.length()) {
                kotlin.runCatching {
                    val item = array[i] as JSONObject
                    val nodeIp = item.optString("agbxRsWxLC")
                    val nodePort = item.optInt("Jasy")
                    val nodePassword = item.optString("UAWwqvI")
                    val nodeMethod = item.optString("ShyWL")
                    val nodeChildName = item.optString("eexBqcMnG")
                    val nodeParentName = item.optString("odzRrQ")
                    val nodeParentCode = item.optString("YHcPgsR")

                    if (game.isEmpty()) {
                        val node = DispositionChildNode(
                            nodeIp,
                            nodePort,
                            nodeMethod,
                            nodePassword,
                            nodeParentName,
                            nodeChildName,
                            nodeParentCode,
                            "game"
                        )
                        game.add(node)
                    } else {
                        val node = DispositionChildNode(
                            nodeIp,
                            nodePort,
                            nodeMethod,
                            nodePassword,
                            nodeParentName,
                            nodeChildName,
                            nodeParentCode,
                            "server"
                        )
                        list.add(node)
                    }
                }
            }
            FalconContent.updateFalconServerList("game", game)
            FalconContent.updateFalconServerList("server", list)
        }
        if (TextUtils.isEmpty(s)) return
        kotlin.runCatching {
            JSONObject(s)
        }.onSuccess {
            val data = it.optJSONObject("data")
            if (it.optInt("code") == 200 && data != null) {
                val fast = data.optJSONArray("SpQnGuPm")
                val server = data.optJSONArray("CftIheweOc")
                processFast(fast)
                processServer(server)
            }
        }
    }

    fun falconServer() {
        kotlin.runCatching {
            falconLoadingServer = true
            val request = Request.Builder()
                .url("${FalconContent.falconServerUrl}/kBe/SgqM/")
                .addHeader("PWTS", "ZZ")
                .addHeader("AVOO", BuildConfig.APPLICATION_ID)
                .get()
                .build()
            val client = OkHttpClient.Builder()
                .build()
            client.let {
                val newCall = it.newCall(request)
                newCall.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        falconLoadingServer = false
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            kotlin.runCatching {
                                val resultStr = response.body?.string()
                                if (resultStr?.isNotEmpty() == true && resultStr.length > 19) {
                                    val result = processString(resultStr) ?: ""
                                    "load server is $result".logFalcon()
                                    DataStore.falconStorageVPN = result
                                    processJson(result)
                                }
                                falconLoadingServer = false
                            }.onFailure {
                                falconLoadingServer = false
                            }
                        }
                    }
                })
            }
        }.onFailure {
            falconLoadingServer = false
        }
    }

    fun falconCountryCode() {
        kotlin.runCatching {
            val request = Request.Builder()
                .url("https://api.infoip.io")
                .addHeader("User-Agent", WebView(context).settings.userAgentString)
                .get()
                .build()
            val client = OkHttpClient.Builder()
                .build()
            client.let {
                val newCall = it.newCall(request)
                newCall.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val string = response.body?.string()
                        if (TextUtils.isEmpty(string)) return
                        kotlin.runCatching {
                            JSONObject(string!!)
                        }.onSuccess {
                            FalconContent.falconCountryCode = it.optString("country_short")
                        }
                    }
                })
            }
        }
    }
}