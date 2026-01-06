package cn.binbin.tsra

import android.content.Context
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter

object TouchRunner {

    private fun prepareTouchHidlBinary(context: Context): File {
        val outFile = File(context.filesDir, "touchHidlTest")
        if (!outFile.exists()) {
            context.assets.open("touchHidlTest").use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile.setExecutable(true)
        }
        return outFile
    }

    fun run(
        context: Context,
        value: Int,
        onResult: ((success: Boolean, code: Int) -> Unit)? = null
    ) {
        Thread {
            try {
                val touchHidlFile = prepareTouchHidlBinary(context)
                val internalPath = touchHidlFile.absolutePath
                val tmpPath = "/data/local/tmp/touchHidlTest"

                // 确保 /data/local/tmp 目录存在
                Runtime.getRuntime()
                    .exec(arrayOf("su", "-c", "mkdir -p /data/local/tmp"))
                    .waitFor()

                // 复制二进制到 /data/local/tmp 并赋予 777 权限
                val copyCmd = buildString {
                    append("cp \"").append(internalPath).append("\" ")
                    append(tmpPath)
                    append(" && chmod 777 ")
                    append(tmpPath)
                }
                Runtime.getRuntime()
                    .exec(arrayOf("su", "-c", copyCmd))
                    .waitFor()

                // 从 /data/local/tmp 执行命令
                val execCmd = "$tmpPath -c wo 0 182 $value"
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", execCmd))
                val result = process.waitFor()
                val success = result == 0

                // 执行完成后删除临时文件
                Runtime.getRuntime()
                    .exec(arrayOf("su", "-c", "rm $tmpPath"))
                    .waitFor()

                onResult?.invoke(success, result)

                (context as? MainActivity)?.runOnUiThread {
                    val msg = if (success) {
                        context.getString(R.string.toast_cmd_success)
                    } else {
                        context.getString(R.string.toast_cmd_fail, result)
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
                    val logFile = File(baseDir, "tsra.log")
                    logFile.parentFile?.mkdirs()

                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    e.printStackTrace(pw)
                    pw.flush()

                    logFile.appendText("\n=== Exception ===\n")
                    logFile.appendText(sw.toString())
                } catch (_: Exception) {
                    // ignore logging errors
                }
                onResult?.invoke(false, -1)

                (context as? MainActivity)?.runOnUiThread {
                    val msg = context.getString(R.string.toast_cmd_exception, e.message ?: "")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}
