package com.gambisoft.gpiano.globals

import androidx.room.TypeConverter
import com.gambisoft.pianolibrary.entity.AutoPlayEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {

	@TypeConverter
	fun convertToGson(list: MutableList<AutoPlayEntity>): String {
		val gson = Gson()
		return gson.toJson(list)
	}

	@TypeConverter
	fun convertToList(data: String?): MutableList<AutoPlayEntity> {
		if (data == null) return mutableListOf()
		val listType = object : TypeToken<MutableList<AutoPlayEntity>>() {}.type
		val gson = Gson()
		return gson.fromJson(data, listType)
	}
}