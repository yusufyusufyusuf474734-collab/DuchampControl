package com.duchamp.control

object RootUtils {

    fun isRooted(): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val out = p.inputStream.bufferedReader().readLine() ?: ""
        p.waitFor()
        out.contains("uid=0")
    } catch (e: Exception) { false }

    fun readFile(path: String): String = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $path"))
        val out = p.inputStream.bufferedReader().readText().trim()
        p.waitFor()
        out
    } catch (e: Exception) { "" }

    fun writeFile(path: String, value: String): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "echo '$value' > $path"))
        p.waitFor() == 0
    } catch (e: Exception) { false }

    fun runCommand(cmd: String): String = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        val out = p.inputStream.bufferedReader().readText().trim()
        p.waitFor()
        out
    } catch (e: Exception) { "" }

    fun readSysfs(path: String): String = readFile(path).ifEmpty { "N/A" }

    fun Long.khzToMhz() = "${this / 1000} MHz"
    fun Long.hzToMhz() = "${this / 1_000_000} MHz"
    fun Int.tenthToC() = "${this / 10.0}°C"
    fun Long.uvToMv() = "${this / 1000} mV"
    fun Long.uaToMa() = "${this / 1000} mA"
}
