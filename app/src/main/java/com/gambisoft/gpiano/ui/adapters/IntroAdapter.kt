package com.gambisoft.gpiano.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gambisoft.gpiano.ui.fragments.IntroFragment

class IntroAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
	private val introFragment0 = IntroFragment()
	private val introFragment1 = IntroFragment()
	private val introFragment2 = IntroFragment()

	init {
		introFragment0.setPosition(0)
		introFragment1.setPosition(1)
		introFragment2.setPosition(2)
	}

	override fun getItemCount(): Int = 3

	override fun createFragment(position: Int): Fragment {
		return when (position) {
			0 -> introFragment0
			1 -> introFragment1
			else -> introFragment2
		}
	}
}