package com.gambisoft.gpiano.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.databinding.LayoutItemThemeBinding
import io.virgo_common.common_libs.views.loadImage

class ThemePianoAdapter(private val context: Context) :
	RecyclerView.Adapter<ThemePianoAdapter.ViewHolder>() {
	inner class ViewHolder(private val binding: LayoutItemThemeBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(position: Int) {
			when (position) {
				0 -> context.loadImage(binding.image, R.mipmap.theme_basic)
				1 -> context.loadImage(binding.image, R.mipmap.theme_noel)
				2 -> context.loadImage(binding.image, R.mipmap.theme_anime)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = LayoutItemThemeBinding.inflate(LayoutInflater.from(context), parent, false)
		return ViewHolder(binding)
	}

	override fun getItemCount(): Int = 3

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(position)
	}
}