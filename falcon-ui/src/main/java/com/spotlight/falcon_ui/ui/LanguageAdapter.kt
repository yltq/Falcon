package com.spotlight.falcon_ui.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spotlight.falcon_ui.R
import com.spotlight.falcon_ui.databinding.UiLanguageItemViewBinding
import com.spotlight.falcon_ui.info.FalconLanguageInfo

class LanguageAdapter(val activity: Activity, var currentLanguage: String): RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    class LanguageViewHolder(val binding: UiLanguageItemViewBinding): RecyclerView.ViewHolder(binding.root)
    var currentIndex: Int = 0

    var items: MutableList<FalconLanguageInfo> = mutableListOf()

    fun updateList(list: MutableList<FalconLanguageInfo>) {
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

    fun notifyLanguage(now: Int) {
        if (currentIndex == now) return
        val last = currentIndex
        notifyItemChanged(last)
        currentIndex = now
        notifyItemChanged(now)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(
            UiLanguageItemViewBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.ui_language_item_view, parent, false)
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = items[position]
        holder.binding.languageName.text = item.languageL
        holder.binding.languageIcon.isSelected = item.code == currentLanguage
        if (item.code == currentLanguage) {
            currentIndex = position
        }
        holder.binding.root.setOnClickListener {
            holder.binding.languageIcon.isSelected = !holder.binding.languageIcon.isSelected
            currentLanguage = item.code
            notifyLanguage(position)
        }
    }


}