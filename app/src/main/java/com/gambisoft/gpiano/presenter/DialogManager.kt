package com.gambisoft.gpiano.presenter

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.callback.IDialogManager
import com.gambisoft.gpiano.entities.RecordDataObject
import io.virgo_common.common_libs.extensions.gone

class DialogManager(
	private val activity: Activity,
	private val viewGroup: ViewGroup,
	private val iDialogManager: IDialogManager
) {
	fun showDialogSaveRecord(name: String = "") {
		val dialog = Dialog(activity)
		dialog.setContentView(R.layout.dialog_save_record)
		dialog.setCancelable(false)
		dialog.window?.let {
			it.setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT
			)
			it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			val windowAttribute = it.attributes
			windowAttribute.gravity = android.view.Gravity.CENTER
			it.attributes = windowAttribute
		}
		dialog.show()
		val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btn_cancel)
		val btnAccept = dialog.findViewById<AppCompatButton>(R.id.btn_accept)
		val edtInputName = dialog.findViewById<AppCompatEditText>(R.id.edt_input_name)

		if (name.isNotEmpty()) edtInputName.setText(name)

		btnCancel.setOnClickListener {
			dialog.dismiss()
			iDialogManager.saveRecordCallback(false)
		}

		btnAccept.setOnClickListener {
			dialog.dismiss()
			val nameRecord =
				edtInputName.text.toString().ifEmpty { "Record_${System.currentTimeMillis()}" }
			iDialogManager.saveRecordCallback(true, nameRecord)
		}
	}

	fun showDialogRenameRecord(recordDataObject: RecordDataObject, position: Int) {
		val dialog = Dialog(activity)
		dialog.setContentView(R.layout.dialog_save_record)
		dialog.setCancelable(false)
		dialog.window?.let {
			it.setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT
			)
			it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			val windowAttribute = it.attributes
			windowAttribute.gravity = android.view.Gravity.CENTER
			it.attributes = windowAttribute
		}
		dialog.show()
		val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btn_cancel)
		val btnAccept = dialog.findViewById<AppCompatButton>(R.id.btn_accept)
		val edtInputName = dialog.findViewById<AppCompatEditText>(R.id.edt_input_name)
		val tvTile = dialog.findViewById<AppCompatTextView>(R.id.tv_title_dialog)
		val tvMessage = dialog.findViewById<AppCompatTextView>(R.id.tv_message_dialog)
		tvMessage.gone()
		tvTile.text = activity.getString(R.string.rename)

		edtInputName.setText(recordDataObject.name.ifEmpty { "Record_${System.currentTimeMillis()}" })

		btnCancel.setOnClickListener {
			dialog.dismiss()
			iDialogManager.renameRecordCallback(false, recordDataObject, position)
		}

		btnAccept.setOnClickListener {
			if (edtInputName.text.toString().isEmpty()) {
				edtInputName.error = activity.getString(R.string.please_enter_name)
				return@setOnClickListener
			}
			dialog.dismiss()
			iDialogManager.renameRecordCallback(
				true,
				recordDataObject,
				position,
				edtInputName.text.toString()
			)
		}
	}

	fun showDialogDeleteRecord(recordDataObject: RecordDataObject) {
		val dialog = Dialog(activity)
		dialog.setContentView(R.layout.dialog_save_record)
		dialog.setCancelable(false)
		dialog.window?.let {
			it.setLayout(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT
			)
			it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			val windowAttribute = it.attributes
			windowAttribute.gravity = android.view.Gravity.CENTER
			it.attributes = windowAttribute
		}
		dialog.show()
		val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btn_cancel)
		val btnAccept = dialog.findViewById<AppCompatButton>(R.id.btn_accept)
		val edtInputName = dialog.findViewById<AppCompatEditText>(R.id.edt_input_name)
		val tvTile = dialog.findViewById<AppCompatTextView>(R.id.tv_title_dialog)
		val tvMessage = dialog.findViewById<AppCompatTextView>(R.id.tv_message_dialog)
		edtInputName.gone()
		tvTile.text = activity.getString(R.string.delete)
		tvMessage.text = recordDataObject.name

		btnCancel.setOnClickListener {
			dialog.dismiss()
			iDialogManager.deleteRecordCallback(false, recordDataObject)
		}

		btnAccept.setOnClickListener {
			dialog.dismiss()
			iDialogManager.deleteRecordCallback(true, recordDataObject)
		}
	}
}