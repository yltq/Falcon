package com.spotlight.falcon_ui.info

data class DispositionParentNode(
    val type: String,
    val nodeCountryCode: String,
    val dispositionCountry: String,
    val expandEnable: Boolean,
    val children: MutableList<DispositionChildNode>,
    var ping: Long = 88888
)