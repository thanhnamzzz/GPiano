package com.gambisoft.pianolibrary.entity

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.view.Gravity
import androidx.core.content.ContextCompat
import com.gambisoft.pianolibrary.R
import com.gambisoft.pianolibrary.enums.BlackKeyPosition
import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.gambisoft.pianolibrary.enums.PianoLayout
import com.gambisoft.pianolibrary.enums.PianoVoice
import com.gambisoft.pianolibrary.utils.LayoutKeyFunction

class Piano(
	private val context: Context,
	private val layout: PianoLayout = PianoLayout.Default,
	scale: Float,
	private val scaleWidth: Float = 1f
) {
	var blackPianoKeys: MutableList<MutableList<PianoKey>> = mutableListOf()
		private set

	var whitePianoKeys: MutableList<MutableList<PianoKey>> = mutableListOf()
		private set

	private val blackDrawable: Drawable
		get() = LayoutKeyFunction.getLayoutBlackKey(context, layout)

	private val whiteDrawable: Drawable
		get() = LayoutKeyFunction.getLayoutWhiteKey(context, layout)

	private var blackKeyWidth = 0
	private var blackKeyHeight = 0
	private var whiteKeyWidth = 0
	private var whiteKeyHeight = 0

	var pianoWith: Int = 0
		private set

	private var scale = 0f

	init {
		this.scale = scale
		initPiano()
	}

	private fun initPiano() {
		if (scale <= 0) return
		blackDrawable.let { black ->
			blackKeyWidth = (black.intrinsicWidth * scaleWidth).toInt()
			blackKeyHeight = (black.intrinsicHeight * scale).toInt()
		}

		whiteDrawable.let { white ->
			whiteKeyWidth = (white.intrinsicWidth * scaleWidth).toInt()
			whiteKeyHeight = (white.intrinsicHeight * scale).toInt()
		}

		blackPianoKeys.clear()
		whitePianoKeys.clear()
		// Khởi tạo phím đen
		for (group in 0 until BLACK_PIANO_KEY_GROUPS) {
			val keys = MutableList(if (group == 0) 1 else 5) { PianoKey() }
			keys.forEachIndexed { index, pianoKey ->
				pianoKey.apply {
					type = PianoKeyType.BLACK
					this.group = group
					positionOfGroup = index
					voiceId = getVoiceFromResources("b$group$index")
					isPressed = false
					keyDrawable = createKeyDrawable(blackDrawable)
					setBlackKeyDrawableBounds(group, index, keyDrawable!!)
					areaOfKey = arrayOf(keyDrawable!!.bounds)
					voice = if (group == 0) PianoVoice.LA else getBlackKeyVoice(index)
				}
			}
			blackPianoKeys.add(keys)
		}
		// Khởi tạo phím trắng
		for (group in 0 until WHITE_PIANO_KEY_GROUPS) {
			val keyCount = when (group) {
				0 -> 2
				8 -> 1
				else -> 7
			}
			val keys = MutableList(keyCount) { PianoKey() }
			keys.forEachIndexed { index, pianoKey ->
				pianoKey.apply {
					type = PianoKeyType.WHITE
					this.group = group
					positionOfGroup = index
					voiceId = getVoiceFromResources("w$group$index")
					isPressed = false
					keyDrawable = createKeyDrawable(whiteDrawable)
					setWhiteKeyDrawableBounds(group, index, keyDrawable!!)
					pianoWith += whiteKeyWidth
					setupWhiteKeyAttributes(group, index, this)
				}
			}
			whitePianoKeys.add(keys)
		}
	}

	/** Tạo key drawable với scale */
	private fun createKeyDrawable(resId: Int): Drawable? {
		return ScaleDrawable(
			ContextCompat.getDrawable(context, resId),
			Gravity.NO_GRAVITY, 1f, scale
		).drawable
	}

	private fun createKeyDrawable(drawable: Drawable): Drawable? {
		return ScaleDrawable(drawable, Gravity.NO_GRAVITY, 1f, scale).drawable
	}

	/** Lấy voice của phím đen */
	private fun getBlackKeyVoice(position: Int): PianoVoice {
		return when (position) {
			0 -> PianoVoice.DO
			1 -> PianoVoice.RE
			2 -> PianoVoice.FA
			3 -> PianoVoice.SO
			4 -> PianoVoice.LA
			else -> PianoVoice.DO
		}
	}

	/** Thiết lập thông tin cho phím trắng */
	private fun setupWhiteKeyAttributes(group: Int, positionInGroup: Int, key: PianoKey) {
		when (group) {
			0 -> {
				key.areaOfKey =
					getWhitePianoKeyArea(
						group,
						positionInGroup,
						if (positionInGroup == 0) BlackKeyPosition.RIGHT else BlackKeyPosition.LEFT
					)
				key.voice = if (positionInGroup == 0) PianoVoice.LA else PianoVoice.SI
				key.letterName = if (positionInGroup == 0) "A0" else "B0"
			}

			8 -> {
				key.areaOfKey = arrayOf(key.keyDrawable!!.bounds)
				key.voice = PianoVoice.DO
				key.letterName = "C8"
			}

			else -> {
				key.areaOfKey = getWhitePianoKeyArea(
					group,
					positionInGroup,
					getWhiteKeyPosition(positionInGroup)
				)
				key.voice = getWhiteKeyVoice(positionInGroup)
				key.letterName = getWhiteKeyLetter(group, positionInGroup)
			}
		}
	}

	/** Lấy voice của phím trắng */
	private fun getWhiteKeyVoice(position: Int): PianoVoice {
		return when (position) {
			0 -> PianoVoice.DO
			1 -> PianoVoice.RE
			2 -> PianoVoice.MI
			3 -> PianoVoice.FA
			4 -> PianoVoice.SO
			5 -> PianoVoice.LA
			6 -> PianoVoice.SI
			else -> PianoVoice.DO
		}
	}

	/** Lấy vị trí phím đen nằm trên phím trắng */
	private fun getWhiteKeyPosition(position: Int): BlackKeyPosition {
		return when (position) {
			0, 3 -> BlackKeyPosition.RIGHT
			1, 4, 5 -> BlackKeyPosition.LEFT_RIGHT
			2, 6 -> BlackKeyPosition.LEFT
			else -> BlackKeyPosition.LEFT_RIGHT
		}
	}

	/** Lấy ký hiệu nốt nhạc của phím trắng */
	private fun getWhiteKeyLetter(group: Int, position: Int): String {
		return when (position) {
			0 -> "C$group"
			1 -> "D$group"
			2 -> "E$group"
			3 -> "F$group"
			4 -> "G$group"
			5 -> "A$group"
			6 -> "B$group"
			else -> ""
		}
	}

	private fun getVoiceFromResources(voiceName: String): Int {
		return runCatching {
			val resClass = R.raw::class.java
			val field = resClass.getDeclaredField(voiceName)
			field.getInt(null)
		}.getOrElse { 0 }
	}

	private fun getWhitePianoKeyArea(
		group: Int, positionOfGroup: Int,
		blackKeyPosition: BlackKeyPosition
	): Array<Rect?> {
		val offset = if (group == 0) 5 else 0
		val baseX = (7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth
		val nextX = (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth
		val halfBlackWidth = blackKeyWidth / 2

		return when (blackKeyPosition) {
			BlackKeyPosition.LEFT -> arrayOf(
				Rect(baseX, blackKeyHeight, baseX + halfBlackWidth, whiteKeyHeight),
				Rect(baseX + halfBlackWidth, 0, nextX, whiteKeyHeight)
			)

			BlackKeyPosition.LEFT_RIGHT -> arrayOf(
				Rect(baseX, blackKeyHeight, baseX + halfBlackWidth, whiteKeyHeight),
				Rect(baseX + halfBlackWidth, 0, nextX - halfBlackWidth, whiteKeyHeight),
				Rect(nextX - halfBlackWidth, blackKeyHeight, nextX, whiteKeyHeight)
			)

			BlackKeyPosition.RIGHT -> arrayOf(
				Rect(baseX, 0, nextX - halfBlackWidth, whiteKeyHeight),
				Rect(nextX - halfBlackWidth, blackKeyHeight, nextX, whiteKeyHeight)
			)
		}
	}

	private fun setWhiteKeyDrawableBounds(group: Int, positionOfGroup: Int, drawable: Drawable) {
		val offset = if (group == 0) 5 else 0
		val baseX = (7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth
		drawable.setBounds(baseX, 0, baseX + whiteKeyWidth, whiteKeyHeight)
	}

	private fun setBlackKeyDrawableBounds(group: Int, positionOfGroup: Int, drawable: Drawable) {
		val whiteOffset = if (group == 0) 5 else 0
		val blackOffset = if (positionOfGroup in 2..4) 1 else 0
		val baseX = (7 * group - 4 + whiteOffset + blackOffset + positionOfGroup) * whiteKeyWidth

		drawable.setBounds(baseX - blackKeyWidth / 2, 0, baseX + blackKeyWidth / 2, blackKeyHeight)
	}

	companion object {
		const val PIANO_NUM: Int = 88

		private const val BLACK_PIANO_KEY_GROUPS = 8
		private const val WHITE_PIANO_KEY_GROUPS = 9
	}
}
