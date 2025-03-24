package com.codoon.threadtracker.ui

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codoon.threadthracker.R
import com.codoon.threadtracker.ThreadInfoManager
import com.codoon.threadtracker.TrackerUtils.setStatusBarColor

/**
 * 线程/线程池列表
 */
class TrackerActivity : Activity() {

    private val refreshHandlerThread = HandlerThread("ThreadTracker-Refresh").apply {
        start()
    }
    private val refreshHandler = object : Handler(refreshHandlerThread.looper) {
        override fun handleMessage(msg: Message) {
            refreshList(msg.what == 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.threadtracker_activity_tracker)
        setStatusBarColor(window)

        val refreshBtn = findViewById<View>(R.id.refreshBtn)
        val refreshProgress = findViewById<View>(R.id.refreshProgress)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        refreshBtn.setOnClickListener {
            refreshBtn.visibility = View.GONE
            refreshProgress.visibility = View.VISIBLE
            refreshHandler.sendEmptyMessage(1)
        }
        val adapter = TrackerAdapter(emptyList(), object : OnItemClickListener {
            override fun onItemClick(view: View) {
                val position: Int = recyclerView.getChildAdapterPosition(view)
                ThreadDetailsActivity.startDetailsActivity(
                    this@TrackerActivity,
                    (recyclerView.adapter as TrackerAdapter).getItemList()[position]
                )
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        refreshHandler.sendEmptyMessage(0)
    }

    private fun refreshList(toast: Boolean) {
        val infoResult = ThreadInfoManager.INSTANCE.buildAllThreadInfo(log = true)
        runOnUiThread {
            val refreshBtn = findViewById<View>(R.id.refreshBtn)
            val refreshProgress = findViewById<View>(R.id.refreshProgress)
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

            (recyclerView.adapter as TrackerAdapter).setItemList(infoResult.list)
            refreshBtn.visibility = View.VISIBLE
            refreshProgress.visibility = View.GONE
            // statisticsText.text = "total: ${infoResult.totalNum}      system/unknown: ${infoResult.unknownNum}"
            if (toast) {
                Toast.makeText(
                    this,
                    "total: ${infoResult.totalNum}  system/unknown: ${infoResult.unknownNum}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        refreshHandler.removeCallbacksAndMessages(null)
        refreshHandlerThread.quit()
        super.onDestroy()
    }
}