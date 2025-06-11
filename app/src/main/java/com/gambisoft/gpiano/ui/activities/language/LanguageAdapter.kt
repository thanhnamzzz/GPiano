package com.gambisoft.gpiano.ui.activities.language

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.databinding.LayoutItemLanguageBinding
import io.virgo_common.common_libs.extensions.gone
import io.virgo_common.common_libs.extensions.visibility
import io.virgo_common.common_libs.views.loadImage

class LanguageAdapter(
	private val context: Context,
	private val iLanguageSelect: ILanguageSelect
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
	private val drawable
		get() = ContextCompat.getDrawable(context, R.drawable.ic_check)
	private val drawableUncheck
		get() = ContextCompat.getDrawable(context, R.drawable.ic_check_un_check)
	private var indexSelect = -1

	inner class ViewHolder(private val binding: LayoutItemLanguageBinding) :
		RecyclerView.ViewHolder(binding.root) {
		fun bind(position: Int) {
			val languageObject = mListLanguage[position]
			binding.apply {
				tvLanguage.text = languageObject.language
				context.loadImage(imageLanguage, languageObject.resource)
				val isFirstItemUnselected = (position == 0 && indexSelect == -1)
				val isSelected = (position == indexSelect)
				with(animation) {
					if (isFirstItemUnselected) {
						visibility()
						playAnimation()
					} else {
						cancelAnimation()
						gone()
					}
				}
				tvLanguage.apply {
					setTextColor(
						ContextCompat.getColor(
							context,
							if (isSelected) R.color.color_purple else R.color.text_hidden_1
						)
					)
					setCompoundDrawablesRelativeWithIntrinsicBounds(
						null,
						null,
						if (isSelected) drawable else drawableUncheck,
						null
					)
				}

				itemView.setOnClickListener {
					val oldIndex = if (indexSelect in 0 until itemCount) indexSelect else 0
					notifyItemChanged(oldIndex, false)
					indexSelect = position
					notifyItemChanged(position, true)
					iLanguageSelect.onClickLanguage(languageObject)
				}
			}
		}

		fun select(b: Boolean) {
			if (b) {
				binding.tvLanguage.setTextColor(
					ContextCompat.getColor(
						context,
						R.color.color_purple
					)
				)
				binding.tvLanguage.setCompoundDrawablesRelativeWithIntrinsicBounds(
					null,
					null,
					drawable,
					null
				)
			} else {
				binding.tvLanguage.setTextColor(
					ContextCompat.getColor(
						context,
						R.color.text_hidden_1
					)
				)
				binding.tvLanguage.setCompoundDrawablesRelativeWithIntrinsicBounds(
					null,
					null,
					drawableUncheck,
					null
				)
			}
		}

		fun hiddenAnimation() {
			if (binding.animation.isVisible) {
				binding.animation.cancelAnimation()
				binding.animation.gone()
			}
		}
	}

	interface ILanguageSelect {
		fun onClickLanguage(languageObject: LanguageObject)
	}

	private var mListLanguage: MutableList<LanguageObject> = mutableListOf()
	fun setData(list: MutableList<LanguageObject>) {
		this.mListLanguage = list
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding =
			LayoutItemLanguageBinding.inflate(LayoutInflater.from(context), parent, false)
		return ViewHolder(binding)
	}

	override fun getItemCount(): Int = mListLanguage.size

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(position)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
		if (payloads.isEmpty())
			super.onBindViewHolder(holder, position, payloads)
		else {
			for (payload in payloads) {
				if (position == 0) holder.hiddenAnimation()
				holder.select(payload as Boolean)
			}
		}
	}
}