package com.gambisoft.gpiano.callback

import com.gambisoft.gpiano.entities.RecordDataObject

interface MenuItemListener {
	fun menuItemCallback(isRenameRecord: Boolean, recordDataObject: RecordDataObject, position: Int)
}