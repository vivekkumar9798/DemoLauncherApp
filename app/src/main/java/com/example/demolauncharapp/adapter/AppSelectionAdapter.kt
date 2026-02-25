package com.example.demolauncharapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demolauncharapp.databinding.ItemAppSelectionBinding
import com.example.demolauncharapp.helper.AppInfo

class AppSelectionAdapter(
    private val apps: List<AppInfo>,
    initialSelected: Set<String>
) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

    // Mutable copy so we can toggle items
    private val selectedPackages: MutableSet<String> = initialSelected.toMutableSet()
    private var onSelectionChanged: (() -> Unit)? = null

    fun setOnSelectionChanged(listener: () -> Unit) {
        onSelectionChanged = listener
    }

    inner class ViewHolder(val binding: ItemAppSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: AppInfo) {
            binding.imgAppIcon.setImageDrawable(app.icon)
            binding.tvAppName.text = app.label
            binding.checkBoxApp.isChecked = selectedPackages.contains(app.packageName)

            binding.root.setOnClickListener {
                toggleSelection(app.packageName)
                binding.checkBoxApp.isChecked = selectedPackages.contains(app.packageName)
                onSelectionChanged?.invoke()
            }

            binding.checkBoxApp.setOnClickListener {
                toggleSelection(app.packageName)
                binding.checkBoxApp.isChecked = selectedPackages.contains(app.packageName)
                onSelectionChanged?.invoke()
            }
        }

        private fun toggleSelection(packageName: String) {
            if (selectedPackages.contains(packageName)) {
                selectedPackages.remove(packageName)
            } else {
                selectedPackages.add(packageName)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    fun getSelectedPackages(): Set<String> = selectedPackages.toSet()

    fun selectAll() {
        apps.forEach { selectedPackages.add(it.packageName) }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun deselectAll() {
        selectedPackages.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun isAllSelected(): Boolean = apps.isNotEmpty() && selectedPackages.containsAll(apps.map { it.packageName })
}
