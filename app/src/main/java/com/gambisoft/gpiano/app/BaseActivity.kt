package com.gambisoft.gpiano.app

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.gambisoft.gpiano.presenter.DialogLoad
import com.gambisoft.gpiano.ui.activities.DoubleKeyActivity
import com.gambisoft.gpiano.ui.activities.DrumActivity
import com.gambisoft.gpiano.ui.activities.IntroActivity
import com.gambisoft.gpiano.ui.activities.MainPianoActivity
import com.gambisoft.gpiano.ui.activities.PlayListActivity
import com.gambisoft.gpiano.ui.activities.ThemePianoActivity
import io.virgo_common.common_libs.baseApp.SimpleActivity
import io.virgo_common.common_libs.functions.GlobalFunction

abstract class BaseActivity<VB : ViewBinding>(bindingInflater: (LayoutInflater) -> VB) :
	SimpleActivity<VB>(bindingInflater) {
	private val _mainApp: App by lazy { App.getInstance() }
	protected val mainApp get() = _mainApp

	private val _dialogLoad: DialogLoad by lazy { DialogLoad(this) }
	protected val dialogLoad get() = _dialogLoad

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		if (binding.root.parent == null) {
			setContentView(binding.root)
		}
		if (javaClass == MainPianoActivity::class.java
			|| javaClass == DoubleKeyActivity::class.java
			|| javaClass == PlayListActivity::class.java
			|| javaClass == DrumActivity::class.java
			|| javaClass == ThemePianoActivity::class.java
		) {
			ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
//				val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
				v.setPadding(0, 0, 0, 0)
				insets
			}
			GlobalFunction.hideSystemUiBar(window)
		} else if (javaClass == IntroActivity::class.java) {
			ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
				val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
				v.setPadding(systemBars.left, 0, systemBars.right, 0)
				insets
			}
			GlobalFunction.hideSystemNavigationBar(window)
		} else {
			ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
				val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
				v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
				insets
			}
			GlobalFunction.hideSystemNavigationBar(window)
		}
	}
}