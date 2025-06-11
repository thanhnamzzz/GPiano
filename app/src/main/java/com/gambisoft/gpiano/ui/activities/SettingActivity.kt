package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivitySettingBinding
import com.gambisoft.gpiano.ui.activities.language.LanguageActivity
import com.gambisoft.gpiano.ui.activities.language.LanguageManager
import io.virgo_common.common_libs.functions.GlobalFunction

class SettingActivity : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
	private lateinit var languageManager: LanguageManager
	private var languageSelect = ""
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		inData()
		inView()
		inEvent()
	}

	private fun inData() {
		languageManager = LanguageManager(this)
		languageSelect = languageManager.getLanguage(languageManager.getKeyLanguage())
	}

	private fun inView() {
		binding.tvLanguageSelected.text = languageSelect
	}

	private fun inEvent() {
		binding.btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		binding.btnLanguage.setOnClickListener {
			startActivity(Intent(this, LanguageActivity::class.java))
		}

		binding.btnShareApp.setOnClickListener {
			GlobalFunction.shareApp(this)
		}

		binding.btnPrivacy.setOnClickListener {
			val link = "https://gambi-publishing-app.web.app/privacy-policy.html"
			GlobalFunction.openPrivacyPolicy(this, link)
		}
	}
}