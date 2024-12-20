package com.spotlight.falcon_ui.ui

import android.app.Activity
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spotlight.falcon_ui.R
import com.spotlight.falcon_ui.databinding.UiDispositionBestViewBinding
import com.spotlight.falcon_ui.databinding.UiDispositionGameViewBinding
import com.spotlight.falcon_ui.databinding.UiDispositionParentViewBinding
import com.spotlight.falcon_ui.info.DispositionChildNode
import com.spotlight.falcon_ui.info.DispositionParentNode

class DispositionNodeAdapter(
    private val activity: Activity,
    val nodeType: String,
    val current: DispositionChildNode?,
    val touchParentNode: (String, Boolean) -> Unit,
    val touchFast: () -> Unit,
    val touchGame: () -> Unit,
    val touchServer: (DispositionChildNode) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class UiDispositionParentViewHolder(val binding: UiDispositionParentViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    class UiDispositionGameViewHolder(val binding: UiDispositionGameViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    class UiDispositionBestViewHolder(val binding: UiDispositionBestViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    var items: MutableList<DispositionParentNode> = mutableListOf()

    fun updateList(list: MutableList<DispositionParentNode>) {
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
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            888 -> {
                UiDispositionBestViewHolder(UiDispositionBestViewBinding.inflate(activity.layoutInflater))
            }

            666 -> {
                UiDispositionGameViewHolder(UiDispositionGameViewBinding.inflate(activity.layoutInflater))
            }

            else -> {
                UiDispositionParentViewHolder(UiDispositionParentViewBinding.inflate(activity.layoutInflater))
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.type) {
            "best" -> {
                888
            }

            "game" -> {
                666
            }

            else -> {
                555
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is UiDispositionBestViewHolder -> {
                holder.binding.dispositionSelect.isSelected = nodeType == "fast"
                holder.binding.dispositionPing.visibility = if (item.ping < 88888) View.VISIBLE else
                    View.GONE
                holder.binding.dispositionPing.text = if (item.ping == -1L) activity.getString(
                    com.spotlight.falcon_language.R.string.disposition_time_out) else "${item.ping}ms"
                if (item.ping == -1L) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#9A9A9A"))
                } else if (item.ping < 100) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#24BD8E"))
                } else if (item.ping in 101 .. 200) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#FFAD02"))
                } else if (item.ping > 200) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#FF3104"))
                }
                holder.binding.root.setOnClickListener { touchFast() }
            }

            is UiDispositionGameViewHolder -> {
                holder.binding.dispositionSelect.isSelected = nodeType == "game"
                holder.binding.dispositionPing.visibility = if (item.ping < 88888) View.VISIBLE else
                    View.GONE
                holder.binding.dispositionPing.text = if (item.ping == -1L) activity.getString(
                    com.spotlight.falcon_language.R.string.disposition_time_out) else "${item.ping}ms"
                if (item.ping == -1L) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#9A9A9A"))
                } else if (item.ping < 100) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#24BD8E"))
                } else if (item.ping in 101 .. 200) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#FFAD02"))
                } else if (item.ping > 200) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#FF3104"))
                }
                holder.binding.root.setOnClickListener {
                    touchGame()
                }
            }

            is UiDispositionParentViewHolder -> {
                holder.binding.dispositionBest.initIconRes(item.nodeCountryCode)
                holder.binding.dispositionPing.visibility = if (item.ping < 88888) View.VISIBLE else
                    View.GONE
                holder.binding.dispositionPing.text = if (item.ping == -1L) activity.getString(
                    com.spotlight.falcon_language.R.string.disposition_time_out) else "${item.ping}ms"
                if (item.ping == -1L) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#9A9A9A"))
                } else if (item.ping < 100) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#24BD8E"))
                } else if (item.ping in 101 .. 200) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#FFAD02"))
                } else if (item.ping > 200) {
                    holder.binding.dispositionPing.setTextColor(Color.parseColor("#FF3104"))
                }

                val text = item.dispositionCountry + "（${item.children.size}）"

                val spannableString = SpannableString(text)

                spannableString.setSpan(
                    ForegroundColorSpan(Color.parseColor("#1C1C1E")),
                    0,
                    item.dispositionCountry.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableString.setSpan(
                    ForegroundColorSpan(Color.parseColor("#A1A1A1")),
                    item.dispositionCountry.length,
                    text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.binding.dispositionBestName.text = spannableString
                holder.binding.dispositionSelect.isSelected = item.expandEnable
                holder.binding.dispositionList.visibility =
                    if (holder.binding.dispositionSelect.isSelected) View.VISIBLE else View.GONE

                holder.binding.dispositionCircle.isSelected = nodeType == "server" && current != null
                        && current.nodeParentName == item.dispositionCountry

                holder.binding.root.setOnClickListener {
                    holder.binding.dispositionSelect.isSelected =
                        !holder.binding.dispositionSelect.isSelected
                    touchParentNode.invoke(item.dispositionCountry, holder.binding.dispositionSelect.isSelected)
                    holder.binding.dispositionList.visibility =
                        if (holder.binding.dispositionSelect.isSelected) View.VISIBLE else View.GONE
                }
                val adapter = DispositionChildNodeAdapter(activity, nodeType, current, touchServer)
                adapter.updateList(item.children)
                holder.binding.dispositionList.adapter = adapter
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


}