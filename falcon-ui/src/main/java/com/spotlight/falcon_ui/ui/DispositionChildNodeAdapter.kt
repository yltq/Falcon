package com.spotlight.falcon_ui.ui

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spotlight.falcon_ui.R
import com.spotlight.falcon_ui.databinding.UiDispositionChildViewBinding
import com.spotlight.falcon_ui.info.DispositionChildNode

class DispositionChildNodeAdapter(
    val activity: Activity,
    val nodeType: String,
    val current: DispositionChildNode?,
    val touchServer: (DispositionChildNode) -> Unit
) : RecyclerView.Adapter<DispositionChildNodeAdapter.UiDispositionChildViewHolder>() {
    class UiDispositionChildViewHolder(val binding: UiDispositionChildViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    var items: MutableList<DispositionChildNode> = mutableListOf()

    fun updateList(list: MutableList<DispositionChildNode>) {
        val lastSize = items.size
        val newSize = list.size
        if (lastSize > 0) {
            items.clear()
            notifyItemRangeRemoved(0, lastSize)
        }
        if (newSize == 0) return
        items.addAll(0, list)
        notifyItemRangeChanged(0, newSize)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UiDispositionChildViewHolder {
        return UiDispositionChildViewHolder(
            UiDispositionChildViewBinding.bind(LayoutInflater.from(parent.context).inflate(R.layout.ui_disposition_child_view, null, false))
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: UiDispositionChildViewHolder, position: Int) {
        val node = items[position]
        val ipList = node.nodeIp.split(".")
        var lastIp = ""
        if (ipList.isNotEmpty()) {
            val sss = ipList[ipList.size - 1]
            if (sss.isNotEmpty()) {
                try {
                    lastIp = String.format("%03d", sss.toInt())
                } catch (e: Exception){}

            }
        }
        if (lastIp.isNotEmpty()) {
            lastIp = " - $lastIp"
        }
        holder.binding.dispositionPing.visibility = if (node.nodePing < 88888) View.VISIBLE else
            View.GONE
        holder.binding.dispositionPing.text = if (node.nodePing == -1L) activity.getString(
            com.spotlight.falcon_language.R.string.disposition_time_out) else "${node.nodePing}ms"

        if (node.nodePing == -1L) {
            holder.binding.dispositionPing.setTextColor(Color.parseColor("#9A9A9A"))
        } else if (node.nodePing < 100) {
            holder.binding.dispositionPing.setTextColor(Color.parseColor("#24BD8E"))
        } else if (node.nodePing in 101 .. 200) {
            holder.binding.dispositionPing.setTextColor(Color.parseColor("#FFAD02"))
        } else if (node.nodePing > 200) {
            holder.binding.dispositionPing.setTextColor(Color.parseColor("#FF3104"))
        }

        holder.binding.dispositionBestName.text = node.nodeChildName + lastIp
        holder.binding.dispositionSelect.isSelected = nodeType == "server" && current != null &&
                current.nodeIp == node.nodeIp
                && current.nodePort == node.nodePort
                && current.nodeChildName == node.nodeChildName
        holder.binding.root.setOnClickListener {
            touchServer(node)
        }
    }
}
