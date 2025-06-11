package com.gambisoft.gpiano.presenter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatTextView
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.callback.MenuItemListener
import com.gambisoft.gpiano.entities.RecordDataObject
import io.virgo_common.common_libs.extensions.getPointLocation

class MenuItemManager(
	private val context: Context,
	private val viewGroup: ViewGroup,
	private val menuItemListener: MenuItemListener
) {
	fun showMenuItem(view: View, recordDataObject: RecordDataObject, position: Int) {
		val layoutInflater =
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val layout = layoutInflater.inflate(R.layout.layout_item_menu, viewGroup, false)

		val popupWindow = PopupWindow(context)
		popupWindow.contentView = layout
		popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
		popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
		popupWindow.isFocusable = true
		popupWindow.animationStyle = R.style.ItemMenuAnimation

		popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		val point = view.getPointLocation()
		popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, point.x, point.y + view.height)

		val btnRename: AppCompatTextView = layout.findViewById(R.id.btn_rename)
		val btnDelete: AppCompatTextView = layout.findViewById(R.id.btn_delete)

		btnRename.setOnClickListener {
			popupWindow.dismiss()
			Handler(Looper.getMainLooper()).postDelayed({
				menuItemListener.menuItemCallback(true, recordDataObject, position)
			}, 300)
		}

		btnDelete.setOnClickListener {
			popupWindow.dismiss()
			Handler(Looper.getMainLooper()).postDelayed({
				menuItemListener.menuItemCallback(false, recordDataObject, position)
			}, 300)
		}
	}
}