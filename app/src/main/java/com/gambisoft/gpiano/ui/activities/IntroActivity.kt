package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.addCallback
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityIntroBinding
import com.gambisoft.gpiano.ui.adapters.IntroAdapter
import io.virgo_common.common_libs.extensions.visibility

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {
	private lateinit var introAdapter: IntroAdapter
	private var countDownTimer: CountDownTimer? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		introAdapter = IntroAdapter(this)
		binding.viewPageIntro.adapter = introAdapter
		binding.dotIndicator.attachTo(binding.viewPageIntro)
		binding.btnSkip.setOnClickListener {
			skip()
		}

		binding.btnNext.setOnClickListener {
			val currentIndex = binding.viewPageIntro.currentItem
			if (currentIndex < 2) {
				binding.viewPageIntro.currentItem = currentIndex + 1
			} else skip()
		}

		countDownTimer = object : CountDownTimer(5000, 1000) {
			override fun onTick(l: Long) {}
			override fun onFinish() {
				binding.btnSkip.visibility()
			}
		}
		(countDownTimer as CountDownTimer).start()

		onBackPressedDispatcher.addCallback { skip() }
	}

	private fun skip() {
		startActivity(Intent(this, FavouriteActivity::class.java))
		finish()
	}

	override fun onDestroy() {
		super.onDestroy()
		countDownTimer?.cancel()
	}
}