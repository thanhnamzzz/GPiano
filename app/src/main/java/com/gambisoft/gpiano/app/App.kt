package com.gambisoft.gpiano.app

import android.app.Application
import com.gambisoft.gpiano.globals.Constant

class App : Application() {
	companion object {
		private lateinit var instance: App
		fun getInstance(): App {
			return instance
		}
	}

	override fun onCreate() {
		super.onCreate()
		instance = this
	}

	private val openRecord: MutableMap<String, Pair<Int, Int>> = mutableMapOf()
	fun getOpenRecord(): MutableMap<String, Pair<Int, Int>> = openRecord
	fun setOpenRecord(value1: Int, value2: Int) {
		openRecord[Constant.OPEN_RECORD_KEY] = Pair(value1, value2)
	}
}