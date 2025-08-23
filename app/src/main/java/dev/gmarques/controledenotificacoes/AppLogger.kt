/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package dev.gmarques.controledenotificacoes

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.gmarques.controledenotificacoes.data.local.PreferencesImpl
import dev.gmarques.controledenotificacoes.framework.implementations.BackupNotificationAlarmSchedulerImpl.MoshiListConverter

object AppLogger {


    private fun writeToLog(log: AppLog) {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val adapter = moshi.adapter(AppLog::class.java)


        Log.d(
            "USUK",
            "AppLogger:\ncaller: ${log.caller}: \nmsg: ${log.msg}\nobjs: ${log.relevantObjects.joinToString { "\n$it" }}"
        )
        val json = PreferencesImpl.log.value
        val logs: MutableList<String> = MoshiListConverter.fromJson(json) ?: mutableListOf()
        logs.add(adapter.toJson(log))
        PreferencesImpl.log.set(MoshiListConverter.toJson(logs))
    }


    fun d() = d("")

    fun d(msg: String, vararg relevantObjects: Any) {

        val stack = Thread.currentThread().stackTrace[3] // 0=Thread, 1=getStackTrace, 2=log, 3=caller
        val className = stack.className.substringAfterLast('.')
        val methodName = stack.methodName

        val log = AppLog(msg = msg, relevantObjects = relevantObjects.toList(), caller = "$className.$methodName")

        writeToLog(log)
    }

    data class AppLog(
        val msg: String,
        val relevantObjects: List<Any>,
        var caller: String,
        val timeStamp: Long = System.currentTimeMillis()
    )


}