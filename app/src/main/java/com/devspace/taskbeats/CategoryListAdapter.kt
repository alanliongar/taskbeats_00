package com.devspace.taskbeats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class CategoryListAdapter :
    ListAdapter<CategoryUiData, CategoryListAdapter.CategoryViewHolder>(CategoryListAdapter) {

    private var onClick: (CategoryUiData) -> Unit = {} //optei por inicializar SEMPRE, por padrão, como uma função VAZIA, pra nao dar crash caso eu nao inicialize

    fun setOnClickListener(onClick: (CategoryUiData) -> Unit) {
        this.onClick = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onClick)
    }

    class CategoryViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val tvCategory = view.findViewById<TextView>(R.id.tv_category)

        fun bind(category: CategoryUiData, onClick: (CategoryUiData) -> Unit) {
            tvCategory.text = category.name
            tvCategory.isSelected = category.isSelected

            view.setOnClickListener {
                onClick.invoke(category)
            }
        }
    }

    companion object : DiffUtil.ItemCallback<CategoryUiData>() {
        override fun areItemsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
            return oldItem.name == newItem.name
        }

    }


}