package com.hoanganh.tvlauncher

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    val items: MutableList<ResolveInfo>,
    private val onAdd: () -> Unit,
    private val onLongPress: (Int, ResolveInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val interpolator = DecelerateInterpolator()
    private val TYPE_APP = 0
    private val TYPE_ADD = 1

    class AppHolder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.icon)
        val name: TextView = v.findViewById(R.id.name)
    }
    class AddHolder(v: View) : RecyclerView.ViewHolder(v)

    override fun getItemCount() = items.size + 1
    override fun getItemViewType(position: Int) = if (position == items.size) TYPE_ADD else TYPE_APP

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == TYPE_ADD) R.layout.item_add_app else R.layout.item_app
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return if (viewType == TYPE_ADD) AddHolder(view) else AppHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddHolder) {
            holder.itemView.setOnClickListener { onAdd() }
            applyFocus(holder.itemView)
            return
        }
        holder as AppHolder
        val app = items[position]
        val pm = holder.itemView.context.packageManager
        holder.icon.setImageDrawable(app.loadIcon(pm))
        holder.name.text = app.loadLabel(pm)
        holder.itemView.setOnClickListener {
            pm.getLaunchIntentForPackage(app.activityInfo.packageName)?.let { intent -> holder.itemView.context.startActivity(intent) }
        }
        holder.itemView.setOnLongClickListener {
            onLongPress(holder.bindingAdapterPosition, app)
            true
        }
        applyFocus(holder.itemView)
    }

    private fun applyFocus(view: View) {
        view.setOnFocusChangeListener { v, focused ->
            v.animate().cancel()
            v.animate().scaleX(if (focused) 1.09f else 1f).scaleY(if (focused) 1.09f else 1f)
                .translationZ(if (focused) 12f else 0f).alpha(if (focused) 1f else 0.92f)
                .setDuration(if (focused) 150 else 120).setInterpolator(interpolator).start()
        }
    }

    fun add(app: ResolveInfo) { items.add(app); notifyItemInserted(items.lastIndex) }
    fun remove(position: Int) { if (position in items.indices) { items.removeAt(position); notifyItemRemoved(position) } }
    fun move(from: Int, to: Int) {
        if (from !in items.indices || to !in items.indices || from == to) return
        val app = items.removeAt(from); items.add(to, app); notifyItemMoved(from, to)
    }
}
