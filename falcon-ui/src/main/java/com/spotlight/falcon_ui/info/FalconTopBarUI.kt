package com.spotlight.falcon_ui.info

data class FalconTopBarUI(
    val leftIcon: Int,
    val rightIcon: Int,
    val title: String,
    val clickLeft: () -> Unit,
    val clickRight: () -> Unit
)
