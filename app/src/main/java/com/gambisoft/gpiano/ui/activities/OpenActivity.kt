package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityOpenBinding
import com.gambisoft.gpiano.globals.Constant
import com.gambisoft.gpiano.ui.activities.language.LanguageActivity
import com.gambisoft.gpiano.ui.activities.language.LanguageManager

class OpenActivity : BaseActivity<ActivityOpenBinding>(ActivityOpenBinding::inflate) {
	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)

		val languageManager = LanguageManager(this)
		languageManager.updateResource(languageManager.getKeyLanguage())
		Handler(Looper.getMainLooper()).postDelayed({
			val intent = Intent(this@OpenActivity, LanguageActivity::class.java)
			intent.putExtra(Constant.FROM_SPLASH, true)
			startActivity(intent)
			finish()
		}, 500)
	}
}