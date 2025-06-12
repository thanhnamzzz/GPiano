package com.gambisoft.gpiano.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.app.StatePianoKeyManager
import com.gambisoft.gpiano.databinding.ActivityThemePianoBinding
import com.gambisoft.gpiano.ui.adapters.ThemePianoAdapter
import com.gambisoft.pianolibrary.enums.PianoLayout
import io.virgo_common.common_libs.extensions.toastMess
import io.virgo_common.common_libs.views.MyToast
import io.virgo_common.common_libs.views.loadImage
import kotlin.math.abs


class ThemePianoActivity :
	BaseActivity<ActivityThemePianoBinding>(ActivityThemePianoBinding::inflate) {
	private lateinit var themeAdapter: ThemePianoAdapter
	private val statePianoKey: StatePianoKeyManager
		get() = StatePianoKeyManager(this)
	private var layoutSelect = PianoLayout.Default
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		layoutSelect = statePianoKey.getLayoutPianoKey()
		themeAdapter = ThemePianoAdapter(this)
		binding.viewPageTheme.adapter = themeAdapter
		binding.dotIndicator.attachTo(binding.viewPageTheme)
		val transformer = CompositePageTransformer()
		transformer.addTransformer(MarginPageTransformer(15))
		transformer.addTransformer { page: View, position: Float ->
			val r = (1 - abs(position.toDouble())).toFloat()
			page.scaleX = 0.9f + r * 0.3f
			page.scaleY = 0.9f + r * 0.3f
			page.alpha = maxOf(0.6f, 1 - abs(position))
		}
		binding.viewPageTheme.apply {
			offscreenPageLimit = 3
			clipChildren = false
			clipToPadding = false
			setPageTransformer(transformer)
			registerOnPageChangeCallback(object :
				ViewPager2.OnPageChangeCallback() {
				override fun onPageSelected(position: Int) {
					super.onPageSelected(position)
					when (position) {
						0 -> {
							loadImage(binding.imageBackground, R.color.none)
							binding.tvTitle.setTextColor(
								ContextCompat.getColor(this@ThemePianoActivity, R.color.white)
							)
							layoutSelect = PianoLayout.Default
						}

						1 -> {
							loadImage(
								binding.imageBackground,
								com.gambisoft.pianolibrary.R.mipmap.noel_background_blur
							)
							binding.tvTitle.setTextColor(
								ContextCompat.getColor(this@ThemePianoActivity, R.color.black)
							)
							layoutSelect = PianoLayout.Noel
						}

						2 -> {
							loadImage(
								binding.imageBackground,
								com.gambisoft.pianolibrary.R.mipmap.anime_background_blur
							)
							binding.tvTitle.setTextColor(
								ContextCompat.getColor(this@ThemePianoActivity, R.color.black)
							)
							layoutSelect = PianoLayout.Anime
						}
					}
				}
			})
		}

		binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

		binding.btnSave.setOnClickListener {
			statePianoKey.setLayoutPianoKey(layoutSelect)
			toastMess(this, "Saved", Toast.LENGTH_SHORT, MyToast.TypeToast.TOAST_SUCCESS)
			Handler(Looper.getMainLooper()).postDelayed({ binding.btnBack.callOnClick() }, 500)
		}
	}

	override fun onResume() {
		super.onResume()
		binding.viewPageTheme.currentItem = layoutSelect.ordinal
	}
}