package com.hoanganh.tvlauncher

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val items: List<ResolveInfo>) : RecyclerView.Adapter<AppAdapter.Holder>() {
    class Holder(v: View): RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val name: TextView = v.findViewById(R.id.name)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: Holder, p: Int) {
        val r = items[p]; val pm = h.itemView.context.packageManager
        h.icon.setImageDrawable(r.loadIcon(pm)); h.name.text = r.loadLabel(pm)
        h.itemView.setOnClickListener {
            pm.getLaunchIntentForPackage(r.activityInfo.packageName)?.let { i -> h.itemView.context.startActivity(i) }
        }
        h.itemView.setOnFocusChangeListener { v, focused -> v.animate().scaleX(if(focused) 1.08f else 1f).scaleY(if(focused) 1.08f else 1f).setDuration(120).start() }
    }
}
