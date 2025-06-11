package com.gambisoft.gpiano.ui.fragments

import android.os.Bundle
import android.view.View
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseFragment
import com.gambisoft.gpiano.databinding.FragmentIntroBinding
import io.virgo_common.common_libs.views.loadImage

class IntroFragment : BaseFragment<FragmentIntroBinding>(FragmentIntroBinding::inflate) {
	private var position = 0
	fun setPosition(i: Int) {
		this.position = i
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		when (position) {
			0 -> {
				requireActivity().loadImage(fBinding.imageIntro, R.mipmap.intro1)
				fBinding.tvMessageIntro.text = getString(R.string._mess_intro_1)
			}

			1 -> {
				requireActivity().loadImage(fBinding.imageIntro, R.mipmap.intro2)
				fBinding.tvMessageIntro.text = getString(R.string._mess_intro_2)
			}

			2 -> {
				requireActivity().loadImage(fBinding.imageIntro, R.mipmap.intro3)
				fBinding.tvMessageIntro.text = getString(R.string._mess_intro_3)
			}
		}
	}
}