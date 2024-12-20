package com.spotlight.falcon.activity

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.preference.DataStore
import com.spotlight.falcon.R
import com.spotlight.falcon.contract.FalconApplication
import com.spotlight.falcon.contract.FalconApplication.Companion.logFalcon
import com.spotlight.falcon.contract.FalconApplication.Companion.toastFalcon
import com.spotlight.falcon.databinding.ActivityDispositionBinding
import com.spotlight.falcon.datas.FalconContent
import com.spotlight.falcon.datas.FalconContent.falconServerMap
import com.spotlight.falcon.gesture.FalconGesture
import com.spotlight.falcon.model.BaseFalconActivity
import com.spotlight.falcon.model.PingTask
import com.spotlight.falcon.resid.FalconResId
import com.spotlight.falcon_ui.info.DispositionChildNode
import com.spotlight.falcon_ui.info.DispositionParentNode
import com.spotlight.falcon_ui.info.FalconTopBarUI
import com.spotlight.falcon_ui.ui.DispositionNodeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class DispositionActivity : BaseFalconActivity(), FalconGesture {
    private lateinit var binding: ActivityDispositionBinding
    private var adapter: DispositionNodeAdapter? = null
    private var allServer: MutableList<DispositionChildNode> = mutableListOf()
    private var dialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        falconResId.getResId(this, R.layout.activity_disposition, back = {
            gestureBack()
        }) {
            binding = it as ActivityDispositionBinding
            setContentView(binding.root)
            gestureInit(this)
        }
    }

    override fun gestureBack() {
        finish()
    }

    private fun serverEmpty(empty: Boolean) {
        binding.dispositionList.visibility = if (empty) View.GONE else View.VISIBLE
        binding.dispositionEmptyGroup.visibility = if (empty) View.VISIBLE else View.GONE
        binding.dispositionTopBar.hideRightIcon(empty)
    }

    override fun gestureInit(context: Context) {
        binding.dispositionTopBar.initIconAction(
            FalconTopBarUI(com.spotlight.falcon_ui.R.mipmap.jr_left_back,
                com.spotlight.falcon_ui.R.mipmap.disposition_right_icon,
                getString(com.spotlight.falcon_language.R.string.main_location),
                clickLeft = {
                    gestureBack()
                },
                clickRight = {
                    if (adapter == null) {
                        getString(com.spotlight.falcon_language.R.string.disposition_server_empty).toastFalcon()
                        return@FalconTopBarUI
                    }
                    dialog = FalconApplication.falconAPP.falconUIDialog.falconTestDialog(context)
                    pingIps(allServer)
                })
        )
        binding.dispositionReloadGroup.setOnClickListener {
            dispositionRotate()
            lifecycleScope.launch {
                dialog = FalconApplication.falconAPP.falconUIDialog.falconLoadingDialog(this@DispositionActivity)
                delay(1500)
                FalconApplication.falconAPP.falconUtils.falconServer()
                withTimeoutOrNull(10000) {
                    while (FalconApplication.falconAPP.falconUtils.falconLoadingServer) {
                        delay(500)
                    }
                    dialog?.dismiss()
                    dispositionFalconServer()
                }
                dialog?.dismiss()
            }
        }
        dispositionFalconServer()
    }

    private fun dispositionFalconServer() {
        val fast = falconServerMap["fast"]
        val game = falconServerMap["game"]
        val server = falconServerMap["server"]
        if (server.isNullOrEmpty()) {
            serverEmpty(true)
            return
        }
        val dispositionNodes = mutableListOf<DispositionParentNode>()
        val bestNode = DispositionParentNode(
            "best",
            "",
            getString(com.spotlight.falcon_language.R.string.disposition_auto),
            false,
            mutableListOf()
        )
        val gameNode = DispositionParentNode(
            "game",
            "",
            getString(com.spotlight.falcon_language.R.string.disposition_game),
            false,
            mutableListOf()
        )
        val map = server.groupBy { it.nodeParentName }
        if (map.isEmpty()) {
            serverEmpty(true)
            return
        }
        allServer.clear()
        if (!fast.isNullOrEmpty()) {
            allServer.addAll(fast)
        }
        if (!game.isNullOrEmpty()) {
            allServer.addAll(game)
        }
        if (!server.isNullOrEmpty()) {
            allServer.addAll(server)
        }
        allServer.forEach {
            it.nodePing = 88888
        }

        serverEmpty(false)
        dispositionNodes.add(bestNode)
        if (!game.isNullOrEmpty()) {
            dispositionNodes.add(gameNode)
        }
        val expandList = DataStore.falconExpandList.split(",")
        map.forEach { t, u ->
            val expandEnable = expandList.isNotEmpty() && expandList.contains(t)
            if (u.isNotEmpty()) {
                dispositionNodes.add(
                    DispositionParentNode(
                        "disposition",
                        u[0].nodeParentCode,
                        t,
                        expandEnable,
                        u as MutableList<DispositionChildNode>
                    )
                )
            }
        }
        val node = falconResId.falconNowNode()
        adapter = DispositionNodeAdapter(this@DispositionActivity, node?.nodeInFast ?: "fast", node,
            touchParentNode = { country, select ->
                val oldList: MutableList<String> = DataStore.falconExpandList.split(",") as MutableList<String>
                val expandList: MutableList<String> = mutableListOf<String>().apply {
                    addAll(oldList)
                }

                if (select) {
                    if (expandList.isEmpty()) {
                        expandList.add(country)
                    } else if (!expandList.contains(country)) {
                        expandList.add(country)
                    }
                } else {
                    if (expandList.isNotEmpty() && expandList.contains(country)) {
                        expandList.remove(country)
                    }
                }
                DataStore.falconExpandList = expandList.joinToString(",")
            },
            touchFast = {
                val intent = Intent()
                intent.putExtra("type", "fast")
                setResult(Activity.RESULT_OK, intent)
                finish()
            }, touchGame = {
                val intent = Intent()
                intent.putExtra("type", "game")
                setResult(Activity.RESULT_OK, intent)
                finish()
            }, touchServer = {
                val intent = Intent()
                intent.putExtra("type", "server")
                intent.putExtra("nodeIp", it.nodeIp)
                intent.putExtra("nodePort", it.nodePort)
                intent.putExtra("nodeMethod", it.nodeMethod)
                intent.putExtra("nodePassword", it.nodePassword)
                intent.putExtra("nodeParentName", it.nodeParentName)
                intent.putExtra("nodeChildName", it.nodeChildName)
                intent.putExtra("nodeParentCode", it.nodeParentCode)
                intent.putExtra("nodeInFast", it.nodeInFast)

                setResult(Activity.RESULT_OK, intent)
                finish()
            })
        adapter?.updateList(dispositionNodes)
        binding.dispositionList.adapter = adapter
    }

    private fun dispositionRotate() {
        val animator = ObjectAnimator.ofFloat(binding.dispositionEmptyRotate, "rotation", 0f, 360f)
        animator.duration = 2000
        animator.start()
    }

    private fun pingIps(ipList: MutableList<DispositionChildNode>) {
        PingTask(ipList) { results ->
            val deepCopy = adapter!!.items.map { it.copy() }
            val adapterItems = deepCopy.toMutableList()
            if (results.isNotEmpty() && adapter != null && adapterItems.isNotEmpty()) {
                for (result in results) {

                    val type = result.nodeInFast
                    if (type == "fast") {
                        if (adapterItems.size >= 1 && adapterItems[0].ping > result.nodePing && result.nodePing > 0) {
                            adapterItems[0].ping = result.nodePing
                        }
                    } else if (type == "game") {
                        if (adapterItems.size >= 2 && adapterItems[1].ping > result.nodePing && result.nodePing > 0) {
                            adapterItems[1].ping = result.nodePing
                        }
                    } else {
                        adapterItems.find { it.dispositionCountry == result.nodeParentName }?.apply {
                            if (this.ping > result.nodePing && result.nodePing > 0) {
                                this.ping = result.nodePing
                            }
                            this.children.find { it.nodeIp == result.nodeIp }?.apply {
                                nodePing = result.nodePing
                            }
                        }
                    }
                }
                adapter!!.updateList(adapterItems)
            }
            dialog?.dismiss()
        }.execute()
    }
}