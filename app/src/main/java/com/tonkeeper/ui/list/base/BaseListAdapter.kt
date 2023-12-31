package com.tonkeeper.ui.list.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseListAdapter<T>(
    private val items: List<BaseListItem>
): RecyclerView.Adapter<BaseListHolder<out BaseListItem>>() {

    fun get(position: Int) = items[position]

    abstract fun createHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseListHolder<out BaseListItem> {
        return createHolder(parent, viewType)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseListHolder<out BaseListItem>, position: Int) {
        holder.bind(get(position))
    }

    override fun getItemViewType(position: Int): Int {
        return get(position).type
    }
}