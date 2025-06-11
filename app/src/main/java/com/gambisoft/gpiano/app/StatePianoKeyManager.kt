package com.gambisoft.gpiano.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.gambisoft.gpiano.globals.Constant
import com.gambisoft.pianolibrary.enums.PianoLayout

class StatePianoKeyManager(private val context: Context) {
	private var shared: SharedPreferences? = null
	private var editor: SharedPreferences.Editor? = null
	private val layoutPianoKey = "layoutPianoKey"
	private val showKeyName = "showKeyName"
	private val scaleWidth = "scaleWidth"

	init {
		init()
	}

	private fun init() {
		shared =
			context.getSharedPreferences(
				Constant.SHARED_PREFERENCES_APP,
				MODE_PRIVATE
			) as SharedPreferences
		editor = shared!!.edit()
	}

	fun getLayoutPianoKey(): PianoLayout {
		if (shared == null) init()
		val value = shared!!.getInt(layoutPianoKey, 0)
		return PianoLayout.fromValue(value)
	}

	fun setLayoutPianoKey(layout: PianoLayout) {
		if (editor == null) init()
		editor?.apply { putInt(layoutPianoKey, layout.value) }?.apply()
	}

	fun showKeyName(): Boolean {
		if (shared == null) init()
		return shared!!.getBoolean(showKeyName, false)
	}

	fun setShowKeyName(show: Boolean) {
		if (editor == null) init()
		editor?.apply { putBoolean(showKeyName, show) }?.apply()
	}

	fun getScaleWidth(): Float {
		if (shared == null) init()
		return shared!!.getFloat(scaleWidth, 1f)
	}

	fun setScaleWidth(f: Float) {
		if (editor == null) init()
		editor?.apply { putFloat(scaleWidth, f) }?.apply()
	}
}