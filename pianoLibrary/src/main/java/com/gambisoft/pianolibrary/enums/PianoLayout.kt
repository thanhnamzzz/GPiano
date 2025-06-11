package com.gambisoft.pianolibrary.enums

enum class PianoLayout(val value: Int) {
	Basic(0),
	Noel(1),
	Anime(2);

	companion object {
		fun fromValue(value: Int): PianoLayout {
			return entries.find { it.value == value } ?: Basic
		}
	}
}