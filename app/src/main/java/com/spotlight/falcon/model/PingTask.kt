package com.spotlight.falcon.model

import android.os.AsyncTask
import com.spotlight.falcon.contract.FalconApplication.Companion.logFalcon
import com.spotlight.falcon_ui.info.DispositionChildNode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress

class PingTask(private val ipList: MutableList<DispositionChildNode>, private val callback: (List<DispositionChildNode>) -> Unit) : AsyncTask<Void, Void, List<DispositionChildNode>>() {

    override fun doInBackground(vararg params: Void?): List<DispositionChildNode> {
        val results = mutableListOf<DispositionChildNode>()
        if (ipList.isNullOrEmpty()) return ipList
        for (ip in ipList) {
            val result = ping(ip.nodeIp)
            ip.nodePing = result
            results.add(ip)
        }

        return results
    }

    private fun ping(ip: String): Long {
        return try {
            // 使用 ProcessBuilder 执行 ping 命令
            val isWindows = System.getProperty("os.name")?.toLowerCase()?.contains("win") ?: return -1L
            // 根据操作系统选择合适的 ping 命令
            val command = if (isWindows) {
                listOf("ping", "-n", "1", "-w", "1000", ip) // Windows
            } else {
                listOf("ping", "-c", "1", "-W", "1", ip) // Linux/macOS
            }
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            // 读取输出
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            // 等待进程结束并获取返回值
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                // 解析输出以获取延迟时间
                val timeLine = output.lines().find { it.contains("time=") || it.contains("时间=") }
                timeLine?.let {
                    val time = it.substringAfter("time=").substringBefore(" ms").trim()
                    time.toLong() // 返回延迟时间
                } ?: -1L // 如果没有找到时间信息，返回 -1L
            } else {
                -1L // 如果 ping 失败，返回 -1L
            }
        } catch (e: Exception) {
            -1L // 捕获异常并返回 -1L
        }
    }

    override fun onPostExecute(result: List<DispositionChildNode>) {
        super.onPostExecute(result)
        callback(result)
    }
}
