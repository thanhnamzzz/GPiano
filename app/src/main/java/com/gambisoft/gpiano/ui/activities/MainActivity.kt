package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding.btnPiano.setOnClickListener {
			val intent = Intent(this, MainPianoActivity::class.java)
			startActivity(intent)
		}

		binding.btnSettings.setOnClickListener {
			val intent = Intent(this, SettingActivity::class.java)
			startActivity(intent)
		}

		binding.btnDrum.setOnClickListener {
			val intent = Intent(this, DrumActivity::class.java)
			startActivity(intent)
		}
	}
}