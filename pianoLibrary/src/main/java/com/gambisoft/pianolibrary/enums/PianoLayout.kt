package com.gambisoft.pianolibrary.enums

enum class PianoLayout(val value: Int) {
	Default(0),
	Classic(1),
	Anime(2),
	City(3),
	Nature(4),
	Noel(5);

	companion object {
		fun fromValue(value: Int): PianoLayout {
			return entries.find { it.value == value } ?: Default
		}
	}
}