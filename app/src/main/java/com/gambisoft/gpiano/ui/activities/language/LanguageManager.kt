package com.gambisoft.gpiano.ui.activities.language

import android.content.Context
import android.content.SharedPreferences
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.globals.Constant
import java.util.Locale

class LanguageManager(private val context: Context) {
	private var mListLanguage = mutableListOf<LanguageObject>()
	private var sharedLanguage: SharedPreferences? = null

	init {
		sharedLanguage =
			context.getSharedPreferences(Constant.SHARED_PREFERENCES_APP, Context.MODE_PRIVATE)
		mListLanguage.add(LanguageObject(context.getString(R.string.bulgarian), R.mipmap.bg, "bg"))
		mListLanguage.add(LanguageObject(context.getString(R.string.czech), R.mipmap.cs, "cs"))
		mListLanguage.add(LanguageObject(context.getString(R.string.english), R.mipmap.en, "en"))
		mListLanguage.add(LanguageObject(context.getString(R.string.danish), R.mipmap.da, "da"))
		mListLanguage.add(LanguageObject(context.getString(R.string.dutch), R.mipmap.nl, "nl"))
		mListLanguage.add(LanguageObject(context.getString(R.string.german), R.mipmap.de, "de"))
		mListLanguage.add(LanguageObject(context.getString(R.string.greek), R.mipmap.el, "el"))
		mListLanguage.add(LanguageObject(context.getString(R.string.spanish), R.mipmap.es, "es"))
		mListLanguage.add(LanguageObject(context.getString(R.string.finnish), R.mipmap.fi, "fi"))
		mListLanguage.add(LanguageObject(context.getString(R.string.french), R.mipmap.fr, "fr"))
		mListLanguage.add(LanguageObject(context.getString(R.string.hindi), R.mipmap.hi, "hi"))
		mListLanguage.add(LanguageObject(context.getString(R.string.hungarian), R.mipmap.hu, "hu"))
		mListLanguage.add(LanguageObject(context.getString(R.string.indonesian), R.mipmap.id, "id"))
		mListLanguage.add(LanguageObject(context.getString(R.string.vietnamese), R.mipmap.vn, "vi"))
		mListLanguage.add(LanguageObject(context.getString(R.string.italian), R.mipmap.it, "it"))
		mListLanguage.add(LanguageObject(context.getString(R.string.japanese), R.mipmap.jp, "ja"))
		mListLanguage.add(LanguageObject(context.getString(R.string.khmer), R.mipmap.km, "km"))
		mListLanguage.add(LanguageObject(context.getString(R.string.korean), R.mipmap.ko, "ko"))
		mListLanguage.add(LanguageObject(context.getString(R.string.lao), R.mipmap.lo, "lo"))
		mListLanguage.add(LanguageObject(context.getString(R.string.nepali), R.mipmap.ne, "ne"))
		mListLanguage.add(LanguageObject(context.getString(R.string.norwegian), R.mipmap.no, "no"))
		mListLanguage.add(LanguageObject(context.getString(R.string.portuguese), R.mipmap.pt, "pt"))
		mListLanguage.add(LanguageObject(context.getString(R.string.russian), R.mipmap.ru, "ru"))
		mListLanguage.add(LanguageObject(context.getString(R.string.serbian), R.mipmap.sr, "sr"))
		mListLanguage.add(LanguageObject(context.getString(R.string.thai), R.mipmap.th, "th"))
		mListLanguage.add(LanguageObject(context.getString(R.string.ukrainian), R.mipmap.uk, "uk"))
	}

	fun getListLanguage(): MutableList<LanguageObject> {
		return mListLanguage
	}

	fun updateResource(key: String) {
		val locale = Locale(key)
		Locale.setDefault(locale)
		val resource = context.resources
		val configuration = resource.configuration
		configuration.setLocale(locale)
		resource.updateConfiguration(configuration, resource.displayMetrics)
	}

	fun saveLanguage(keySave: String) {
		sharedLanguage?.let {
			val editor = it.edit()
			editor.apply {
				putString(Constant.SHARED_LANGUAGE, keySave)
			}.apply()
		}
	}

	fun getKeyLanguage(): String {
		val defaultLanguage = Locale.getDefault().language
		var keySet = ""
		sharedLanguage?.let {
			keySet = it.getString(Constant.SHARED_LANGUAGE, defaultLanguage).toString()
		} ?: run { keySet = defaultLanguage }
		return keySet
	}

	fun getLanguage(key: String): String {
		for (languageObject: LanguageObject in mListLanguage) {
			if (languageObject.key == key) {
				return languageObject.language
			}
		}
		return context.getString(R.string.english)
	}

	fun getIndexSelect(key: String): Int {
		for (i in 0 until mListLanguage.size) {
			if (key == mListLanguage[i].key) {
				return i
			}
		}
		return 0
	}
}