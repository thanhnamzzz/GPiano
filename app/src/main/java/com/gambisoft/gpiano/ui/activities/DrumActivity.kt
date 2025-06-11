package com.gambisoft.gpiano.ui.activities

import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityDrumBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DrumActivity : BaseActivity<ActivityDrumBinding>(ActivityDrumBinding::inflate),
	View.OnClickListener {
	private val animation by lazy { AnimationUtils.loadAnimation(this, R.anim.animation_drum) }
	private lateinit var soundPool: SoundPool
	private val resourceSound = mutableListOf<Int>()
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		soundPool = SoundPool.Builder().setMaxStreams(10).build()

		inData()
		inView()
		inEvent()
	}

	private fun inData() {
		lifecycleScope.launch {
			resourceSound.apply {
				add(soundPool.load(this@DrumActivity, R.raw.bass, 1)) //0
				add(soundPool.load(this@DrumActivity, R.raw.bell, 1)) //1
				add(soundPool.load(this@DrumActivity, R.raw.block, 1)) //2
				add(soundPool.load(this@DrumActivity, R.raw.crash1, 1)) //3
				add(soundPool.load(this@DrumActivity, R.raw.crash2, 1)) //4
				add(soundPool.load(this@DrumActivity, R.raw.crash3, 1)) //5
				add(soundPool.load(this@DrumActivity, R.raw.hihat_open, 1)) //6
				add(soundPool.load(this@DrumActivity, R.raw.hihat_pedal, 1)) //7
				add(soundPool.load(this@DrumActivity, R.raw.ride, 1)) //8
				add(soundPool.load(this@DrumActivity, R.raw.snare, 1)) //9
				add(soundPool.load(this@DrumActivity, R.raw.tambourine, 1)) //10
				add(soundPool.load(this@DrumActivity, R.raw.tom1, 1)) //11
				add(soundPool.load(this@DrumActivity, R.raw.tom2, 1)) //12
				add(soundPool.load(this@DrumActivity, R.raw.tom3, 1)) //13
			}
			Log.d("Namzzz", "DrumActivity: inData load done")
		}
	}

	private fun inView() {

	}

	private fun inEvent() {
		binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
		binding.apply {
			drumCrash1.setOnClickListener(this@DrumActivity)
			drumCrash2.setOnClickListener(this@DrumActivity)
			drumCrash3.setOnClickListener(this@DrumActivity)
			drumHhOpen.setOnClickListener(this@DrumActivity)
			drumRide.setOnClickListener(this@DrumActivity)
			drumTom1.setOnClickListener(this@DrumActivity)
			drumTom2.setOnClickListener(this@DrumActivity)
			drumTom3.setOnClickListener(this@DrumActivity)
			drumSnare.setOnClickListener(this@DrumActivity)
			drumBass1.setOnClickListener(this@DrumActivity)
			drumBass2.setOnClickListener(this@DrumActivity)
			drumBlock.setOnClickListener(this@DrumActivity)
			drumPedal.setOnClickListener(this@DrumActivity)
			drumTambourine.setOnClickListener(this@DrumActivity)
			drumBell.setOnClickListener(this@DrumActivity)
		}
	}

	private fun playAnimation(view: View) {
		view.startAnimation(animation)
	}

	private fun playSound(i: Int) {
		soundPool.play(i, 1f, 1f, 0, 0, 1f)
	}

	override fun onClick(p0: View?) {
		p0?.let { view ->
			CoroutineScope(Dispatchers.Main).launch {
				playAnimation(view)
				val sound = when (view.id) {
					R.id.drum_bass_1, R.id.drum_bass_2 -> resourceSound[0]
					R.id.drum_crash1 -> resourceSound[3]
					R.id.drum_crash2 -> resourceSound[4]
					R.id.drum_crash3 -> resourceSound[5]
					R.id.drum_hh_open -> resourceSound[6]
					R.id.drum_ride -> resourceSound[8]
					R.id.drum_tom_1 -> resourceSound[11]
					R.id.drum_tom_2 -> resourceSound[12]
					R.id.drum_tom_3 -> resourceSound[13]
					R.id.drum_snare -> resourceSound[9]
					R.id.drum_block -> resourceSound[2]
					R.id.drum_pedal -> resourceSound[7]
					R.id.drum_tambourine -> resourceSound[10]
					R.id.drum_bell -> resourceSound[1]
					else -> null
				}
				withContext(Dispatchers.IO) { sound?.let { playSound(it) } }
			}
		}
	}
}