package com.spotlight.falcon_ui.info

data class DispositionChildNode(
    val nodeIp: String,
    val nodePort: Int,
    val nodeMethod: String,
    val nodePassword: String,
    val nodeParentName: String,
    val nodeChildName: String,
    val nodeParentCode: String,
    var nodeInFast: String,
    var nodePing: Long = 88888
)