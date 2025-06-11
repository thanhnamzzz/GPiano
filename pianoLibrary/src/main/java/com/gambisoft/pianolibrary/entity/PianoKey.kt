package com.gambisoft.pianolibrary.entity

import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.gambisoft.pianolibrary.enums.PianoVoice

class PianoKey {
	@JvmField
	var type: PianoKeyType = PianoKeyType.WHITE

	//音乐类型[DO,RE,MI,FA,SO,LA,SI]
	@JvmField
	var voice: PianoVoice = PianoVoice.DO

	//所属组
	@JvmField
	var group: Int = 0

	//所属组下的位置
	@JvmField
	var positionOfGroup: Int = 0

	//图案
	@JvmField
	var keyDrawable: Drawable? = null

	//音乐ID
	var voiceId: Int = 0

	//标志，是否被点击，默认未点击
	@JvmField
	var isPressed: Boolean = false

	//钢琴键的所占区域
	@JvmField
	var areaOfKey: Array<Rect?> = Array(88) { Rect() }

	//音名（针对白键）
	@JvmField
	var letterName: String? = null

	//被点击的手指的下标
	@JvmField
	var fingerID: Int = -1

	fun contains(x: Int, y: Int): Boolean {
		return areaOfKey.any { it?.contains(x, y) == true }
	}

	fun resetFingerID() {
		fingerID = -1
	}
}