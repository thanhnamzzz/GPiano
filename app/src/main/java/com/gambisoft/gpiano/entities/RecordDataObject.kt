package com.gambisoft.gpiano.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PianoRecord")
data class RecordDataObject(
	@PrimaryKey(autoGenerate = true)
	var id: Int = 0,
	var name: String,
	val dataGson: String,
	val timeRecord: String,
	val lengthRecord: String
)
