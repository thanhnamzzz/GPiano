package com.gambisoft.gpiano.callback

import com.gambisoft.gpiano.entities.RecordDataObject

interface IDialogManager {
	fun saveRecordCallback(accept: Boolean, name: String = "")
	fun renameRecordCallback(accept: Boolean, record: RecordDataObject, position: Int, name: String = "")
	fun deleteRecordCallback(accept: Boolean, record: RecordDataObject)
}