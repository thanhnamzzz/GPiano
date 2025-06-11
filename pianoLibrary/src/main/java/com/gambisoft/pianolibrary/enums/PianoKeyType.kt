package com.gambisoft.pianolibrary.enums

import com.google.gson.annotations.SerializedName

enum class PianoKeyType(private val value: Int) {
	@SerializedName("0")
	BLACK(0),

	@SerializedName("1")
	WHITE(1);

	override fun toString(): String {
		return "PianoKeyType{value=$value}"
	}
}