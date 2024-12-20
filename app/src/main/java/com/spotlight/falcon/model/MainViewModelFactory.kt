package com.spotlight.falcon.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spotlight.falcon.activity.MainActivity
import com.spotlight.falcon.resid.FalconResId
import java.lang.ref.WeakReference

class MainViewModelFactory(private val context: WeakReference<MainActivity>?, private val falconResId: FalconResId) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(context, falconResId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}