package com.hoanganh.tvlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val apps = loadApps()
        findViewById<RecyclerView>(R.id.favorites).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
            adapter = AppAdapter(apps.take(8))
            setHasFixedSize(true)
            setItemViewCacheSize(12)
            itemAnimator = null
        }
        updateClock()
    }

    private fun loadApps(): List<ResolveInfo> {
        val found = linkedMapOf<String, ResolveInfo>()
        fun collect(category: String) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
            packageManager.queryIntentActivities(intent, 0).forEach { app ->
                if (app.activityInfo.packageName != packageName) {
                    val key = "${app.activityInfo.packageName}/${app.activityInfo.name}"
                    if (!found.containsKey(key)) found[key] = app
                }
            }
        }
        collect(Intent.CATEGORY_LEANBACK_LAUNCHER)
        collect(Intent.CATEGORY_LAUNCHER)
        return found.values.sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
    }

    private fun updateClock() {
        val now = Date()
        findViewById<TextView>(R.id.clock).text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.date).text = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN")).format(now)
        handler.postDelayed({ updateClock() }, 30000)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
