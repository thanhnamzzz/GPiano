package com.gambisoft.gpiano.globals

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentFormattedDate(): String {
	val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
	val currentDate = Date()
	return dateFormat.format(currentDate)
}