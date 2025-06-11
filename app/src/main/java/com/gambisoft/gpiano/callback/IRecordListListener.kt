package com.gambisoft.gpiano.callback

import android.view.View
import com.gambisoft.gpiano.entities.RecordDataObject

interface IRecordListListener {
	fun onOpenRecord(id: Int)
	fun onClickMenu(view: View, recordDataObject: RecordDataObject, position: Int)
}