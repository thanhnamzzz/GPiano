package com.gambisoft.gpiano.ui.activities.language

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityLanguageBinding
import com.gambisoft.gpiano.globals.Constant
import com.gambisoft.gpiano.ui.activities.IntroActivity
import com.gambisoft.gpiano.ui.activities.MainActivity
import io.virgo_common.common_libs.extensions.gone
import io.virgo_common.common_libs.extensions.toastMess
import io.virgo_common.common_libs.views.MyToast

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
	private lateinit var languageManager: LanguageManager
	private lateinit var languageAdapter: LanguageAdapter
	private var mListLanguage = mutableListOf<LanguageObject>()
	private var keySelect = ""
	private var fromSplash = false
	private var isChangeLanguage = false
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fromSplash = intent.getBooleanExtra(Constant.FROM_SPLASH, false)

		inData()
		inView()
		inEvent()
	}

	private fun inData() {
		languageManager = LanguageManager(this)
		mListLanguage = languageManager.getListLanguage()
		keySelect = languageManager.getKeyLanguage()
		languageAdapter = LanguageAdapter(this, object :
			LanguageAdapter.ILanguageSelect {
			override fun onClickLanguage(languageObject: LanguageObject) {
				keySelect = languageObject.key
				if (!isChangeLanguage) {
					isChangeLanguage = true
				}
			}
		})
		languageAdapter.setData(mListLanguage)
		binding.rvListLanguage.adapter = languageAdapter
	}

	private fun inView() {
		if (fromSplash) binding.btnBack.gone()
	}

	private fun inEvent() {
		binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
		binding.btnSave.setOnClickListener {
			if (!isChangeLanguage) {
				toastMess(
					this,
					"Please select language",
					Toast.LENGTH_SHORT,
					MyToast.TypeToast.TOAST_NONE
				)
				return@setOnClickListener
			}
			languageManager.saveLanguage(keySelect)
			languageManager.updateResource(keySelect)
//			recreate()
			if (fromSplash)
				startActivity(Intent(this, IntroActivity::class.java))
			else
				startActivity(Intent(this, MainActivity::class.java))
			finishAffinity()
		}
	}
}