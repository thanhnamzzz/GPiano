package com.gambisoft.gpiano.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gambisoft.gpiano.callback.IMusicListListener
import com.gambisoft.gpiano.databinding.LayoutItemMusicListBinding
import com.gambisoft.pianolibrary.utils.MusicList

class MusicListAdapter(private val context: Context, private val iMusicListListener: IMusicListListener) :
	RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {
	inner class ViewHolder(private val binding: LayoutItemMusicListBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(position: Int) {
			if (position in 0 until MusicList.listMusic.size) {
				binding.tvNameMusic.text = MusicList.listMusic[position]
				binding.btnPlayInPiano.setOnClickListener {
					iMusicListListener.onOpenList(position)
				}
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding =
			LayoutItemMusicListBinding.inflate(LayoutInflater.from(context), parent, false)
		return ViewHolder(binding)
	}

	override fun getItemCount(): Int = MusicList.listMusic.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(position)
	}
}