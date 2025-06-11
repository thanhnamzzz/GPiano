package com.gambisoft.pianolibrary.entity

import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.google.gson.annotations.SerializedName

class AutoPlayEntity {
    @JvmField
    var type: PianoKeyType = PianoKeyType.WHITE

    @JvmField
    var group: Int = 0

    @JvmField
    var position: Int = 0

	@JvmField
    @SerializedName("break")
	var currentBreakTime: Long = 0

	constructor()

	constructor(type: PianoKeyType, group: Int, position: Int, currentBreakTime: Long) {
		this.type = type
		this.group = group
		this.position = position
		this.currentBreakTime = currentBreakTime
	}

	override fun toString(): String {
		return ("AutoPlayEntity{"
				+ "type="
				+ type
				+ ", group="
				+ group
				+ ", position="
				+ position
				+ ", currentBreakTime="
				+ currentBreakTime
				+ '}')
	}
}
