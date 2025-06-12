package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.app.StatePianoKeyManager
import com.gambisoft.gpiano.databinding.ActivityFavouriteBinding
import com.gambisoft.gpiano.ui.adapters.ThemePianoAdapter
import com.gambisoft.pianolibrary.enums.PianoLayout
import io.virgo_common.common_libs.extensions.toastMess
import io.virgo_common.common_libs.views.MyToast
import kotlin.math.abs

class FavouriteActivity :
	BaseActivity<ActivityFavouriteBinding>(ActivityFavouriteBinding::inflate) {
	private lateinit var themeAdapter: ThemePianoAdapter
	private val statePianoKey: StatePianoKeyManager
		get() = StatePianoKeyManager(this)
	private var layoutSelect = PianoLayout.Default
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		layoutSelect = statePianoKey.getLayoutPianoKey()
		themeAdapter = ThemePianoAdapter(this)
		binding.viewPagePiano.adapter = themeAdapter

		val transformer = CompositePageTransformer()
		transformer.addTransformer(MarginPageTransformer(20))
		transformer.addTransformer { page: View, position: Float ->
			val r = (1 - abs(position.toDouble())).toFloat()
			page.scaleX = minOf(0.8f + r * 0.3f, 1f)
			page.scaleY = minOf(0.8f + r * 0.3f, 1f)
			page.alpha = maxOf(0.6f, 1 - abs(position))
		}
		binding.viewPagePiano.apply {
			offscreenPageLimit = 5
			clipChildren = false
			clipToPadding = false
			setPageTransformer(transformer)
			registerOnPageChangeCallback(object :
				ViewPager2.OnPageChangeCallback() {
				override fun onPageSelected(position: Int) {
					super.onPageSelected(position)
					when (position) {
						0 -> {
							layoutSelect = PianoLayout.Default
							binding.tvThemeSelect.text = "Basic"
						}

						1 -> {
							layoutSelect = PianoLayout.Noel
							binding.tvThemeSelect.text = "Noel"
						}

						2 -> {
							layoutSelect = PianoLayout.Anime
							binding.tvThemeSelect.text = "Anime"
						}
					}
				}
			})
		}

		binding.btnSelectTheme.setOnClickListener {
			statePianoKey.setLayoutPianoKey(layoutSelect)
			toastMess(this, "Saved", Toast.LENGTH_SHORT, MyToast.TypeToast.TOAST_SUCCESS)
			startActivity(Intent(this, GenreMusicActivity::class.java))
			finish()
		}

		onBackPressedDispatcher.addCallback {
			startActivity(Intent(this@FavouriteActivity, GenreMusicActivity::class.java))
			finish()
		}
	}

	override fun onResume() {
		super.onResume()
		binding.viewPagePiano.currentItem = layoutSelect.ordinal
	}
}