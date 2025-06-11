package com.gambisoft.gpiano.ui.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.callback.IDialogManager
import com.gambisoft.gpiano.callback.IMusicListListener
import com.gambisoft.gpiano.callback.IRecordListListener
import com.gambisoft.gpiano.callback.MenuItemListener
import com.gambisoft.gpiano.database.PianoRecordDao
import com.gambisoft.gpiano.database.PianoRecordDatabase
import com.gambisoft.gpiano.databinding.ActivityPlayListBinding
import com.gambisoft.gpiano.entities.RecordDataObject
import com.gambisoft.gpiano.globals.Constant
import com.gambisoft.gpiano.presenter.DialogManager
import com.gambisoft.gpiano.presenter.MenuItemManager
import com.gambisoft.gpiano.ui.adapters.MusicListAdapter
import com.gambisoft.gpiano.ui.adapters.RecordListAdapter
import io.virgo_common.common_libs.extensions.gone
import io.virgo_common.common_libs.extensions.visibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayListActivity : BaseActivity<ActivityPlayListBinding>(ActivityPlayListBinding::inflate),
	IMusicListListener, IRecordListListener, MenuItemListener, IDialogManager {
	private lateinit var musicListAdapter: MusicListAdapter
	private lateinit var recordListAdapter: RecordListAdapter
	private lateinit var menuItemManager: MenuItemManager
	private lateinit var dialogManager: DialogManager

	private val pianoRecordDatabase: PianoRecordDatabase by lazy {
		PianoRecordDatabase.getInstance(this)
	}
	private val pianoRecordDao: PianoRecordDao
		get() = pianoRecordDatabase.pianoRecordDao()

	private var mListRecord = mutableListOf<RecordDataObject>()
	private var firstOpen = true
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		firstOpen = true
		inData()
		inView()
		inEvent()
	}

	override fun onResume() {
		super.onResume()
		if (firstOpen) {
			firstOpen = false
			binding.btnRecordList.callOnClick()
		}
	}

	private fun inData() {
		menuItemManager = MenuItemManager(this, binding.root, this)
		dialogManager = DialogManager(this, binding.root, this)
		musicListAdapter = MusicListAdapter(this, this)
		recordListAdapter = RecordListAdapter(this, this)
		binding.rvListMusic.adapter = musicListAdapter
		binding.rvListRecord.adapter = recordListAdapter

		lifecycleScope.launch {
			mListRecord = pianoRecordDao.getListPianoRecorded()
			if (mListRecord.isEmpty()) binding.llNoData.visibility() else binding.llNoData.gone()
			recordListAdapter.setData(mListRecord)
		}
	}

	private fun inView() {

	}

	private val musicListSelect: Drawable?
		get() = ContextCompat.getDrawable(this, R.drawable.ic_music_list_select)
	private val musicListUnSelect: Drawable?
		get() = ContextCompat.getDrawable(this, R.drawable.ic_music_list_un_select)
	private val recordListSelect: Drawable?
		get() = ContextCompat.getDrawable(this, R.drawable.ic_record_list_select)
	private val recordListUnSelect: Drawable?
		get() = ContextCompat.getDrawable(this, R.drawable.ic_record_list_un_select)

	private fun inEvent() {
		binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
		binding.btnMusicList.setOnClickListener {
			binding.btnMusicList.setBackgroundResource(R.drawable.bg_purple_r10)
			binding.btnMusicList.setTextColor(ContextCompat.getColor(this, R.color.white))
			binding.btnMusicList.setCompoundDrawablesRelativeWithIntrinsicBounds(
				null,
				musicListSelect,
				null,
				null
			)

			binding.btnRecordList.setBackgroundResource(R.drawable.bg_lavender_r10)
			binding.btnRecordList.setTextColor(ContextCompat.getColor(this, R.color.color_purple))
			binding.btnRecordList.setCompoundDrawablesRelativeWithIntrinsicBounds(
				null,
				recordListUnSelect,
				null,
				null
			)
			binding.rvListMusic.visibility()
			binding.rvListRecord.gone()
			binding.llNoData.gone()
		}

		binding.btnRecordList.setOnClickListener {
			binding.btnMusicList.setBackgroundResource(R.drawable.bg_lavender_r10)
			binding.btnMusicList.setTextColor(ContextCompat.getColor(this, R.color.color_purple))
			binding.btnMusicList.setCompoundDrawablesRelativeWithIntrinsicBounds(
				null,
				musicListUnSelect,
				null,
				null
			)

			binding.btnRecordList.setBackgroundResource(R.drawable.bg_purple_r10)
			binding.btnRecordList.setTextColor(ContextCompat.getColor(this, R.color.white))
			binding.btnRecordList.setCompoundDrawablesRelativeWithIntrinsicBounds(
				null,
				recordListSelect,
				null,
				null
			)
			binding.rvListMusic.gone()
			binding.rvListRecord.visibility()
			if (mListRecord.isEmpty()) binding.llNoData.visibility() else binding.llNoData.gone()
		}

		binding.btnGoToPiano.setOnClickListener {
			binding.btnBack.callOnClick()
		}
	}

	override fun onOpenList(position: Int) {
		mainApp.setOpenRecord(Constant.SELECT_MUSIC_LIST, position)
		setResult(Constant.RESULT_SELECT_RECORD)
		Handler(Looper.getMainLooper()).postDelayed({ binding.btnBack.callOnClick() }, 300)
	}

	override fun onOpenRecord(id: Int) {
		mainApp.setOpenRecord(Constant.SELECT_RECORD_LIST, id)
		setResult(Constant.RESULT_SELECT_RECORD)
		Handler(Looper.getMainLooper()).postDelayed({ binding.btnBack.callOnClick() }, 300)
	}

	override fun onClickMenu(view: View, recordDataObject: RecordDataObject, position: Int) {
		lifecycleScope.launch {
			if (pianoRecordDao.hasInDatabase(recordDataObject.id))
				menuItemManager.showMenuItem(view, recordDataObject, position)
		}
	}

	override fun menuItemCallback(isRenameRecord: Boolean, recordDataObject: RecordDataObject, position: Int) {
		if (isRenameRecord) {
			dialogManager.showDialogRenameRecord(recordDataObject, position)
		} else {
			dialogManager.showDialogDeleteRecord(recordDataObject)
		}
	}

	override fun saveRecordCallback(accept: Boolean, name: String) {
		//No action
	}

	override fun renameRecordCallback(accept: Boolean, record: RecordDataObject, position: Int, name: String) {
		if (accept) {
			CoroutineScope(Dispatchers.IO).launch {
				record.name = name
				pianoRecordDao.updatePianoRecord(record)
				withContext(Dispatchers.Main) {
					mListRecord = pianoRecordDao.getListPianoRecorded()
					recordListAdapter.setData(mListRecord)
					recordListAdapter.notifyItemChanged(position)
				}
				cancel()
			}
		}
	}

	override fun deleteRecordCallback(accept: Boolean, record: RecordDataObject) {
		if (accept) {
			CoroutineScope(Dispatchers.IO).launch {
				pianoRecordDao.deletePianoRecord(record)
				withContext(Dispatchers.Main) {
					mListRecord = pianoRecordDao.getListPianoRecorded()
					recordListAdapter.setData(mListRecord)
				}
				cancel()
			}
		}
	}
}