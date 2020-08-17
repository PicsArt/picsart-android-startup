package com.rousetime.sample

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.StartupListener
import com.rousetime.android_startup.StartupManager
import com.rousetime.android_startup.manager.StartupCacheManager
import com.rousetime.android_startup.model.CostTimesModel
import com.rousetime.android_startup.model.LoggerLevel
import com.rousetime.android_startup.model.StartupConfig
import com.rousetime.sample.startup.*
import kotlinx.android.synthetic.main.activity_common.content

/**
 * Created by idisfkj on 2020/8/13.
 * Email: idisfkj@gmail.com.
 */
class SampleCommonActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)

        start()
    }

    fun onClick(view: View) {
        if (R.id.clear == view.id) {
            StartupCacheManager.instance.clear()
            content.text = getString(R.string.clear_cache_success)
        } else if (R.id.get == view.id) {
            start()
        }
    }

    private fun start() {
        // show initialize tips
        content.text = getString(R.string.sample_startup_not_initialized)

        val list = mutableListOf<AndroidStartup<*>>()
        when (intent.getIntExtra("id", -1)) {
            R.id.sync_and_sync -> {
                list.add(SampleSyncOneStartup())
                list.add(SampleSyncTwoStartup())
            }
            R.id.sync_and_async -> {
                list.add(SampleSyncThreeStartup())
                list.add(SampleAsyncOneStartup())
            }
            R.id.async_and_sync -> {
                list.add(SampleAsyncTwoStartup())
                list.add(SampleSyncFourStartup())
            }
            R.id.async_and_async -> {
                list.add(SampleAsyncFiveStartup())
                list.add(SampleAsyncThreeStartup())
            }
            R.id.async_and_async_await_main_thread -> {
                list.add(SampleAsyncSixStartup())
                list.add(SampleAsyncFourStartup())
            }
        }
        val config = StartupConfig.Builder()
            .setLoggerLevel(LoggerLevel.DEBUG)
            .setAwaitTimeout(12000L)
            .setListener(object : StartupListener {
                override fun onCompleted(totalMainThreadCostTime: Long, costTimesModels: List<CostTimesModel>) {
                    // can to do cost time statistics.
                    content.text = buildString {
                        append("Startup Completed: ")
                        append("\n")
                        append("\n")
                        costTimesModels.forEach {
                            append("\n")
                            append("Startup Name: ${it.name}")
                            append("\n")
                            append("CallOnMainThread: ${it.callOnMainThread}")
                            append("\n")
                            append("WaitOnMainThread: ${it.waitOnMainThread}")
                            append("\n")
                            append("Cost times: ${it.endTime - it.startTime} ms")
                            append("\n")
                        }
                        if (costTimesModels.isEmpty()) {
                            append("result form cache.")
                            append("\n")
                            list.forEach {
                                append("\n")
                                append("${it::class.java.simpleName}: ${StartupCacheManager.instance.obtainInitializedResult<String>(it::class.java)}")
                                append("\n")
                            }
                        }
                    }
                    Log.d("StartupTrack", "onCompleted: ${costTimesModels.size}")
                }
            })
            .build()

        // because some scenarios startup block on main thread.
        // in order to show initialize tips,to delay a frame times.
        Handler().postDelayed({
            StartupManager.Builder()
                .setConfig(config)
                .addAllStartup(list)
                .build(this)
                .start()
                .await()
        }, 16)
    }
}