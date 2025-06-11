package com.gambisoft.gpiano.presenter

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.WindowManager
import com.gambisoft.gpiano.R

class DialogLoad(activity: Activity) {
	private var dialogLoad = Dialog(activity)

	init {
		dialogLoad.setContentView(R.layout.dialog_loading)
		dialogLoad.setCancelable(false)
		val window = dialogLoad.window
		window?.let {
			it.setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
			)
			it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			val windowAttribute = it.attributes
			windowAttribute.gravity = Gravity.CENTER
			it.attributes = windowAttribute
		}
	}

	fun showLoading() {
		if (!dialogLoad.isShowing) {
			dialogLoad.show()
		}
	}

	fun closeLoading() {
		if (dialogLoad.isShowing)
			dialogLoad.dismiss()
	}
}