package com.gambisoft.pianolibrary.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat
import com.gambisoft.pianolibrary.R
import com.gambisoft.pianolibrary.enums.PianoLayout

object LayoutKeyFunction {
	fun getLayoutWhiteKey(context: Context, layoutKey: PianoLayout): StateListDrawable {
		val selector = StateListDrawable()
		val pressed: Drawable?
		val normal: Drawable?
		when (layoutKey) {
			PianoLayout.Basic -> {
				pressed = ContextCompat.getDrawable(context, R.drawable.white_key1_down)
				normal = ContextCompat.getDrawable(context, R.drawable.white_key1_up)
			}

			else -> {
				pressed = ContextCompat.getDrawable(context, R.drawable.white_key2_down)
				normal = ContextCompat.getDrawable(context, R.drawable.white_key2_up)
			}
		}
		selector.apply {
			addState(intArrayOf(android.R.attr.state_pressed), pressed)
			addState(intArrayOf(), normal)
		}
		return selector
	}

	fun getLayoutBlackKey(context: Context, layoutKey: PianoLayout): StateListDrawable {
		val selector = StateListDrawable()
		val pressed: Drawable?
		val normal: Drawable?
		when (layoutKey) {
			PianoLayout.Basic -> {
				pressed = ContextCompat.getDrawable(context, R.drawable.black_key1_down)
				normal = ContextCompat.getDrawable(context, R.drawable.black_key1_up)
			}

			else -> {
				pressed = ContextCompat.getDrawable(context, R.drawable.black_key2_down)
				normal = ContextCompat.getDrawable(context, R.drawable.black_key2_up)
			}
		}
		selector.apply {
			addState(intArrayOf(android.R.attr.state_pressed), pressed)
			addState(intArrayOf(), normal)
		}
		return selector
	}
}