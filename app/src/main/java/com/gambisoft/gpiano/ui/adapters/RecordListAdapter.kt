package com.gambisoft.gpiano.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gambisoft.gpiano.callback.IRecordListListener
import com.gambisoft.gpiano.databinding.LayoutItemRecordListBinding
import com.gambisoft.gpiano.entities.RecordDataObject

class RecordListAdapter(
	private val context: Context,
	private val iRecordListListener: IRecordListListener
) :
	RecyclerView.Adapter<RecordListAdapter.ViewHolder>() {
	inner class ViewHolder(private val binding: LayoutItemRecordListBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(position: Int) {
			val data = differ.currentList[position]
			binding.apply {
				tvNameRecord.text = data.name
				tvTimeRecord.text = data.timeRecord
				tvLengthRecord.text = data.lengthRecord
				btnPlayInPiano.setOnClickListener {
					iRecordListListener.onOpenRecord(data.id)
				}
				btnMenuItem.setOnClickListener {
					iRecordListListener.onClickMenu(it, data, position)
				}
			}
		}
	}

	private val diffCallback = object : DiffUtil.ItemCallback<RecordDataObject>() {
		override fun areItemsTheSame(
			oldItem: RecordDataObject,
			newItem: RecordDataObject
		): Boolean {
			return (oldItem.dataGson == newItem.dataGson) && (oldItem.name == newItem.name)
		}

		override fun areContentsTheSame(
			oldItem: RecordDataObject,
			newItem: RecordDataObject
		): Boolean {
			return oldItem == newItem
		}
	}

	private val differ = AsyncListDiffer(this, diffCallback)
	fun setData(list: MutableList<RecordDataObject>) {
		differ.submitList(list.toList())
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding =
			LayoutItemRecordListBinding.inflate(LayoutInflater.from(context), parent, false)
		return ViewHolder(binding)
	}

	override fun getItemCount(): Int = differ.currentList.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(position)
	}
}