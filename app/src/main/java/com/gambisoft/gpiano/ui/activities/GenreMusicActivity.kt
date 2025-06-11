package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.databinding.ActivityGenreMusicBinding
import com.gambisoft.gpiano.databinding.LayoutItemSelectFavouriteBinding
import com.gambisoft.gpiano.entities.MusicGenreObject
import kotlinx.coroutines.launch

class GenreMusicActivity :
	BaseActivity<ActivityGenreMusicBinding>(ActivityGenreMusicBinding::inflate) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		lifecycleScope.launch {
			initFlowLayout()
		}
		binding.btnNext.setOnClickListener {
			startActivity(Intent(this, WelcomeActivity::class.java))
			finish()
		}

		onBackPressedDispatcher.addCallback {
			startActivity(Intent(this@GenreMusicActivity, WelcomeActivity::class.java))
			finish()
		}
	}

	private fun initFlowLayout() {
		val list = mutableListOf(
			MusicGenreObject("\uD83C\uDFB5 Classical", keySelect = "classical"),
			MusicGenreObject("\uD83C\uDFB9 Jazz", keySelect = "jazz"),
			MusicGenreObject("\uD83C\uDFB6 Blues ", keySelect = "blues"),
			MusicGenreObject("\uD83C\uDFBC Pop ", keySelect = "pop"),
			MusicGenreObject("\uD83C\uDFB8 Rock", keySelect = "rock"),
			MusicGenreObject("\uD83C\uDFB5 Ballad", keySelect = "ballad"),
			MusicGenreObject("\uD83C\uDFA4 R&B / Soul", keySelect = "rb_soul"),
			MusicGenreObject("\uD83C\uDFAE Game / Anime Music", keySelect = "game_anime"),
			MusicGenreObject("\uD83C\uDFAC Soundtrack ", keySelect = "sound_track")
		)

		list.forEach { musicGenre ->
			val bd = LayoutItemSelectFavouriteBinding.inflate(
				LayoutInflater.from(this),
				binding.layoutFavorite,
				false
			)
			bd.tvMusicGenre.text = musicGenre.nameGenre
			bd.tvMusicGenre.setOnClickListener {
				musicGenre.isSelect = !musicGenre.isSelect
				if (musicGenre.isSelect) {
					bd.tvMusicGenre.setTextColor(ContextCompat.getColor(this, R.color.white))
					bd.tvMusicGenre.setBackgroundResource(R.drawable.bg_border_white_r10_favourite)
				} else {
					bd.tvMusicGenre.setTextColor(ContextCompat.getColor(this, R.color.black_1A1C20))
					bd.tvMusicGenre.setBackgroundResource(R.drawable.bg_border_black_r10)
				}
			}
			binding.layoutFavorite.addView(bd.root)
		}
	}
}