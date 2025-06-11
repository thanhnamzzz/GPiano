package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityWelcomeBinding
import render.animations.Bounce
import render.animations.Render

class WelcomeActivity : BaseActivity<ActivityWelcomeBinding>(ActivityWelcomeBinding::inflate) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val render = Render(this)
		render.setAnimation(Bounce().In(binding.btnGetStarted))
		render.setDuration(500)
		render.start()

		binding.btnGetStarted.setOnClickListener {
			startActivity(Intent(this, MainActivity::class.java))
			finish()
		}

		onBackPressedDispatcher.addCallback {
			startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
			finish()
		}
	}
}