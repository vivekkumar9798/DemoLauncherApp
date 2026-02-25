package com.example.demolauncharapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.demolauncharapp.R
import com.example.demolauncharapp.databinding.ItemAppBinding
import com.example.demolauncharapp.helper.AppInfo

class AppAdapter(
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding, onAppClick, onAppLongClick)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position), getLetterHeader(position))
    }

    private fun getLetterHeader(position: Int): String? {
        val current = getItem(position).label.firstOrNull()?.uppercaseChar()
        if (position == 0) return current?.toString()
        val previous = getItem(position - 1).label.firstOrNull()?.uppercaseChar()
        return if (current != previous) current?.toString() else null
    }

    class AppViewHolder(
        private val binding: ItemAppBinding,
        private val onAppClick: (AppInfo) -> Unit,
        private val onAppLongClick: (AppInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo, letterHeader: String?) {
            binding.appIcon.setImageDrawable(appInfo.icon)
            binding.appLabel.text = appInfo.label

            if (letterHeader != null) {
                binding.letterHeader.text = letterHeader
                binding.letterHeader.visibility = View.VISIBLE
            } else {
                binding.letterHeader.visibility = View.GONE
            }

            // Entrance animation
            val entranceAnim = if (bindingAdapterPosition % 2 == 0) {
                R.anim.item_entrance
            } else {
                R.anim.item_entrance_delayed
            }
            binding.root.startAnimation(AnimationUtils.loadAnimation(binding.root.context, entranceAnim))

            // Touch animations
            binding.root.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        binding.root.startAnimation(AnimationUtils.loadAnimation(binding.root.context, R.anim.scale_down))
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        binding.root.startAnimation(AnimationUtils.loadAnimation(binding.root.context, R.anim.scale_up))
                        if (event.action == android.view.MotionEvent.ACTION_UP) {
                            onAppClick(appInfo)
                        }
                        true
                    }
                    else -> false
                }
            }

            binding.root.setOnLongClickListener {
                binding.root.startAnimation(AnimationUtils.loadAnimation(binding.root.context, R.anim.scale_up))
                onAppLongClick(appInfo)
                true
            }
        }
    }
}

class AppDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) =
        oldItem.packageName == newItem.packageName

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) =
        oldItem.packageName == newItem.packageName &&
        oldItem.label == newItem.label &&
        oldItem.dominantColor == newItem.dominantColor
}
