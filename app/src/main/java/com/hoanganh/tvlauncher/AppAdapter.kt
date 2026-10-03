package com.hoanganh.tvlauncher

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val items: List<ResolveInfo>) : RecyclerView.Adapter<AppAdapter.Holder>() {
    private val interpolator = DecelerateInterpolator()

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val name: TextView = v.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: Holder, p: Int) {
        val app = items[p]
        val pm = h.itemView.context.packageManager
        h.icon.setImageDrawable(app.loadIcon(pm))
        h.name.text = app.loadLabel(pm)

        h.itemView.setOnClickListener {
            pm.getLaunchIntentForPackage(app.activityInfo.packageName)?.let { intent ->
                h.itemView.context.startActivity(intent)
            }
        }

        h.itemView.setOnFocusChangeListener { view, focused ->
            view.animate().cancel()
            view.animate()
                .scaleX(if (focused) 1.09f else 1f)
                .scaleY(if (focused) 1.09f else 1f)
                .translationZ(if (focused) 12f else 0f)
                .alpha(if (focused) 1f else 0.92f)
                .setDuration(if (focused) 150 else 120)
                .setInterpolator(interpolator)
                .start()
        }
    }
}
