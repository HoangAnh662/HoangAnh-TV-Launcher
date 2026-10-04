package com.hoanganh.tvlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var allApps: List<ResolveInfo>
    private lateinit var favoritesAdapter: AppAdapter
    private val prefs by lazy { getSharedPreferences("launcher", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.settingsButton).apply {
            setOnClickListener { runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)) } }
            setOnFocusChangeListener { view, focused -> view.animate().cancel(); view.animate().scaleX(if (focused) 1.07f else 1f).scaleY(if (focused) 1.07f else 1f).setDuration(130).start() }
        }

        allApps = loadApps()
        val favorites = loadFavorites().toMutableList()
        favoritesAdapter = AppAdapter(favorites, { showAddApps() }, { position, app -> showEditMenu(position, app) })
        findViewById<RecyclerView>(R.id.favorites).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.HORIZONTAL, false)
            adapter = favoritesAdapter
            setItemViewCacheSize(12)
            itemAnimator = null
        }
        updateClock()
    }

    private fun loadFavorites(): List<ResolveInfo> {
        val saved = prefs.getString("favorites", null)
        if (saved == null) return allApps.take(7)
        val packages = saved.split("|").filter { it.isNotBlank() }
        return packages.mapNotNull { pkg -> allApps.firstOrNull { it.activityInfo.packageName == pkg } }
    }

    private fun saveFavorites() {
        prefs.edit().putString("favorites", favoritesAdapter.items.joinToString("|") { it.activityInfo.packageName }).apply()
    }

    private fun showAddApps() {
        val existing = favoritesAdapter.items.map { it.activityInfo.packageName }.toSet()
        val available = allApps.filter { it.activityInfo.packageName !in existing }
        if (available.isEmpty()) return
        val names = available.map { it.loadLabel(packageManager).toString() }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Thêm ứng dụng").setItems(names) { _, which ->
            favoritesAdapter.add(available[which]); saveFavorites()
        }.setNegativeButton("Hủy", null).show()
    }

    private fun showEditMenu(position: Int, app: ResolveInfo) {
        val options = arrayOf("Di chuyển sang trái", "Di chuyển sang phải", "Xóa khỏi Yêu thích")
        AlertDialog.Builder(this).setTitle(app.loadLabel(packageManager)).setItems(options) { _, which ->
            when (which) {
                0 -> if (position > 0) favoritesAdapter.move(position, position - 1)
                1 -> if (position < favoritesAdapter.items.lastIndex) favoritesAdapter.move(position, position + 1)
                2 -> favoritesAdapter.remove(position)
            }
            saveFavorites()
        }.setNegativeButton("Hủy", null).show()
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
        collect(Intent.CATEGORY_LEANBACK_LAUNCHER); collect(Intent.CATEGORY_LAUNCHER)
        return found.values.sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
    }

    private fun updateClock() {
        val now = Date()
        findViewById<TextView>(R.id.clock).text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        findViewById<TextView>(R.id.date).text = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN")).format(now)
        handler.postDelayed({ updateClock() }, 30000)
    }

    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
}
