package com.gambisoft.pianolibrary.listener

import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.gambisoft.pianolibrary.enums.PianoVoice

interface OnPianoListener {
	fun onPianoInitFinish()

	/**
	 * 点击钢琴键
	 *
	 * @param type 钢琴键类型（黑、白）
	 * @param voice 钢琴音色（DO,RE,MI,FA,SO,LA,SI）
	 * @param group 钢琴键所在组（白：9组；黑：7组）
	 * @param positionOfGroup 钢琴在组内位置
	 */
	fun onPianoClick(
		type: PianoKeyType, voice: PianoVoice?, group: Int,
		positionOfGroup: Int
	)
}
