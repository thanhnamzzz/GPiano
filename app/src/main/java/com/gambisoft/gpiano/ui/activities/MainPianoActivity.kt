package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.app.StatePianoKeyManager
import com.gambisoft.gpiano.callback.IDialogManager
import com.gambisoft.gpiano.database.PianoRecordDao
import com.gambisoft.gpiano.database.PianoRecordDatabase
import com.gambisoft.gpiano.databinding.ActivityMainPianoBinding
import com.gambisoft.gpiano.entities.RecordDataObject
import com.gambisoft.gpiano.globals.Constant
import com.gambisoft.gpiano.globals.Converters
import com.gambisoft.gpiano.globals.getCurrentFormattedDate
import com.gambisoft.gpiano.presenter.DialogManager
import com.gambisoft.pianolibrary.entity.AutoPlayEntity
import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.gambisoft.pianolibrary.enums.PianoLayout
import com.gambisoft.pianolibrary.enums.PianoVoice
import com.gambisoft.pianolibrary.listener.OnLoadAudioListener
import com.gambisoft.pianolibrary.listener.OnPianoAutoPlayListener
import com.gambisoft.pianolibrary.listener.OnPianoListener
import com.gambisoft.pianolibrary.utils.MusicList
import io.virgo_common.common_libs.extensions.gone
import io.virgo_common.common_libs.extensions.visibility
import io.virgo_common.common_libs.views.loadImage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class MainPianoActivity :
	BaseActivity<ActivityMainPianoBinding>(ActivityMainPianoBinding::inflate),
	OnSeekBarChangeListener, OnPianoListener, OnLoadAudioListener, OnPianoAutoPlayListener,
	IDialogManager {
	private lateinit var dialogManager: DialogManager
	private val miniScroll: Int
		get() = (binding.pianoView.layoutWidth * 100) / binding.pianoView.pianoWidth

	private val statePianoKey: StatePianoKeyManager
		get() = StatePianoKeyManager(this)

	private var isRecordPiano = false
	private var isAutoPlaying = false
	private var lastClickTime: Long = 0
	private val recordList = mutableListOf<AutoPlayEntity>()
	private val playList = mutableListOf<AutoPlayEntity>()
	private var secondsElapsed = 0
	private var job: Job? = null

	private var lengthRecord = ""
	private var dataConvertToGson = ""

	private lateinit var pianoRecordDatabase: PianoRecordDatabase
	private val pianoRecordDao: PianoRecordDao
		get() = pianoRecordDatabase.pianoRecordDao()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		dialogManager = DialogManager(this, binding.root, this)
		dialogLoad.showLoading()

		binding.pianoView.apply {
			setSoundPollMaxStream(10)
			setPianoListener(this@MainPianoActivity)
			setAutoPlayListener(this@MainPianoActivity)
			setLoadAudioListener(this@MainPianoActivity)
		}

		inData()
		inEvent()

		binding.btnSelectAudio.text = MusicList.listMusic[0]
		playList.clear()
		playList.addAll(MusicList.musicLittleStar())
	}

	private fun hiddenAnimation() {
		CoroutineScope(Dispatchers.Main).launch {
			delay(1500)
			val animation =
				AnimationUtils.loadAnimation(this@MainPianoActivity, R.anim.animation_bottom_top)
			binding.llButtonTop.startAnimation(animation)
			delay(1000)
			binding.btnShowButtonTop.callOnClick()
			cancel()
		}
	}

	override fun onResume() {
		super.onResume()
		inView()
		binding.pianoView.apply {
			setLayoutKey(statePianoKey.getLayoutPianoKey())
			showKeyName(statePianoKey.showKeyName())
			changeScaleWidth(statePianoKey.getScaleWidth())
		}
		when (statePianoKey.getLayoutPianoKey()) {
			PianoLayout.Basic -> loadImage(binding.imageBackground, R.color.none)
			PianoLayout.Noel -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.noel_background
			)

			PianoLayout.Anime -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.anime_background
			)
		}
	}

	private fun inData() {
		pianoRecordDatabase = PianoRecordDatabase.getInstance(this)
	}

	private fun inView() {
		viewButtonShowButtonTop()
		viewButtonZoomKey()
		viewButtonShowKeyName()
		viewButtonPlay()
	}

	private fun inEvent() {
		binding.seekbarScroll.setOnSeekBarChangeListener(this)
		binding.seekbarScroll.setPadding(0, 0, 0, 0)
		binding.btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		binding.btnShowButtonTop.setOnClickListener {
			if (binding.llButtonTop.isVisible) {
				binding.llButtonTop.gone()
			} else {
				binding.llButtonTop.visibility()
			}
			viewButtonShowButtonTop()
		}

		binding.btnForwardBar.setOnClickListener {
			val newProgress = (binding.seekbarScroll.progress - miniScroll)
				.coerceAtLeast(0)
			binding.seekbarScroll.progress = newProgress
		}

		binding.btnNextBar.setOnClickListener {
			val newProgress = (binding.seekbarScroll.progress + miniScroll)
				.coerceAtMost(100)
			binding.seekbarScroll.progress = newProgress
		}

		binding.btnZoomMinus.setOnClickListener {
			if (binding.pianoView.getScaleWidth() != 1f) {
				val scale = (binding.pianoView.getScaleWidth() - 0.2f).coerceAtLeast(1f)
				binding.pianoView.changeScaleWidth(scale)
			}
			viewButtonZoomKey()
			statePianoKey.setScaleWidth(binding.pianoView.getScaleWidth())
		}

		binding.btnZoomPlus.setOnClickListener {
			if (binding.pianoView.getScaleWidth() != 1.8f) {
				val scale = (binding.pianoView.getScaleWidth() + 0.2f).coerceAtMost(1.8f)
				binding.pianoView.changeScaleWidth(scale)
			}
			viewButtonZoomKey()
			statePianoKey.setScaleWidth(binding.pianoView.getScaleWidth())
		}

		binding.btnDoubleKey.setOnClickListener {
			val intent = Intent(this, DoubleKeyActivity::class.java).apply {
				putExtra(Constant.OPTION_DOUBLE_KEY, true)
			}
			startActivity(intent)
		}

		binding.btnTwoPlayer.setOnClickListener {
			val intent = Intent(this, DoubleKeyActivity::class.java).apply {
				putExtra(Constant.OPTION_DOUBLE_KEY, false)
			}
			startActivity(intent)
		}

		binding.btnShowKey.setOnClickListener {
			val show = !statePianoKey.showKeyName()
			binding.pianoView.showKeyName(show)
			statePianoKey.setShowKeyName(show)
			viewButtonShowKeyName()
		}

		binding.btnChangeTheme.setOnClickListener {
			startActivity(Intent(this, ThemePianoActivity::class.java))
//			when (binding.pianoView.getLayoutKey()) {
//				PianoLayout.Basic -> {
//					binding.pianoView.setLayoutKey(PianoLayout.Noel)
//					loadImage(
//						binding.imageBackground,
//						com.gambisoft.pianolibrary.R.mipmap.noel_background
//					)
//				}
//
//				PianoLayout.Noel -> {
//					binding.pianoView.setLayoutKey(PianoLayout.Anime)
//					loadImage(
//						binding.imageBackground,
//						com.gambisoft.pianolibrary.R.mipmap.anime_background
//					)
//				}
//
//				PianoLayout.Anime -> {
//					binding.pianoView.setLayoutKey(PianoLayout.Basic)
//					loadImage(binding.imageBackground, R.color.none)
//				}
//			}
//			statePianoKey.setLayoutPianoKey(binding.pianoView.getLayoutKey())
		}

		binding.btnRecord.setOnClickListener {
			isRecordPiano = !isRecordPiano
			viewButtonRecord()
			if (isRecordPiano) {
				recordList.clear()
				job = lifecycleScope.launch {
					while (isActive) {
						val minutes = secondsElapsed / 60
						val seconds = secondsElapsed % 60
						lengthRecord =
							String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
						binding.btnRecord.text = lengthRecord
						secondsElapsed++
						delay(1000)
					}
				}
			} else {
				job?.cancel()
				dataConvertToGson = Converters.convertToGson(recordList)
				dialogManager.showDialogSaveRecord()
			}
		}

		binding.btnPlayAudio.setOnClickListener {
			isAutoPlaying = !isAutoPlaying
			if (isAutoPlaying) {
				binding.pianoView.autoPlay(playList)
			} else {
				binding.pianoView.stopAutoPlay()
			}
			viewButtonPlay()
		}

		binding.btnSelectAudio.setOnClickListener {
			val intent = Intent(this, PlayListActivity::class.java)
			launcherOpenRecordList.launch(intent)
		}
	}

	private fun viewButtonShowButtonTop() {
		if (binding.llButtonTop.isVisible) {
			binding.btnShowButtonTop.setImageResource(R.drawable.ic_up)
		} else {
			binding.btnShowButtonTop.setImageResource(R.drawable.ic_down)
		}
		binding.pianoView.refreshLayout()
	}

	private fun viewButtonZoomKey() {
		when (binding.pianoView.getScaleWidth()) {
			1f -> {
				binding.btnZoomMinus.setImageResource(R.drawable.ic_zoom_minus_none)
				binding.btnZoomPlus.setImageResource(R.drawable.ic_zoom_plus)
			}

			1.8f -> {
				binding.btnZoomMinus.setImageResource(R.drawable.ic_zoom_minus)
				binding.btnZoomPlus.setImageResource(R.drawable.ic_zoom_plus_none)
			}

			else -> {
				binding.btnZoomMinus.setImageResource(R.drawable.ic_zoom_minus)
				binding.btnZoomPlus.setImageResource(R.drawable.ic_zoom_plus)
			}
		}
	}

	private fun viewButtonShowKeyName() {
		when (statePianoKey.showKeyName()) {
			true -> {
				binding.btnShowKey.setBackgroundResource(R.drawable.bg_button_click_circle)
			}

			false -> {
				binding.btnShowKey.setBackgroundResource(R.drawable.bg_button_un_click_circle)
			}
		}
	}

	private val drawableRec: Drawable?
		get() = ContextCompat.getDrawable(this, R.drawable.ic_rec)
	private val drawableUnRec: Drawable?
		get() = ContextCompat.getDrawable(this, R.drawable.ic_rec_un_rec)

	private fun viewButtonRecord() {
		if (isRecordPiano) {
			binding.btnRecord.setCompoundDrawablesRelativeWithIntrinsicBounds(
				drawableRec,
				null,
				null,
				null
			)
			binding.btnRecord.setTextColor(ContextCompat.getColor(this, R.color.white))
			binding.btnRecord.text = getString(R.string._00_00)
		} else {
			binding.btnRecord.setCompoundDrawablesRelativeWithIntrinsicBounds(
				drawableUnRec,
				null,
				null,
				null
			)
			binding.btnRecord.setTextColor(ContextCompat.getColor(this, R.color.text_hidden))
			binding.btnRecord.text = getString(R.string._rec)
		}
	}

	private fun viewButtonPlay() {
		if (isAutoPlaying) {
			binding.btnPlayAudio.setImageResource(R.drawable.ic_pause)
		} else {
			binding.btnPlayAudio.setImageResource(R.drawable.ic_play)
		}
	}

	override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
		binding.pianoView.scroll(p1)
	}

	override fun onStartTrackingTouch(p0: SeekBar?) {
		//No action
	}

	override fun onStopTrackingTouch(p0: SeekBar?) {
		//No action
	}

	override fun onPianoInitFinish() {
		Log.d("Namzzz", "MainPianoActivity: onPianoInitFinish")
	}

	override fun onPianoClick(
		type: PianoKeyType,
		voice: PianoVoice?,
		group: Int,
		positionOfGroup: Int
	) {
		if (isRecordPiano) {
			val currentTime = System.currentTimeMillis()
			if (lastClickTime != 0L) {
				val elapsedTime = currentTime - lastClickTime
				val autoPlayEntity = AutoPlayEntity(
					type, group, positionOfGroup, elapsedTime
				)
				recordList.add(autoPlayEntity)
			}
			lastClickTime = currentTime
		}
	}

	override fun loadPianoAudioStart() {
		Log.d("Namzzz", "MainPianoActivity: loadPianoAudioStart")
	}

	override fun loadPianoAudioFinish() {
		Log.d("Namzzz", "MainPianoActivity: loadPianoAudioFinish")
		binding.seekbarScroll.progress = 50
		dialogLoad.closeLoading()
		hiddenAnimation()
	}

	override fun loadPianoAudioError(e: Exception?) {
		Log.e("Namzzz", "MainPianoActivity: loadPianoAudioError", e)
	}

	override fun loadPianoAudioProgress(progress: Int) {
		Log.d("Namzzz", "MainPianoActivity: loadPianoAudioProgress $progress")
	}

	override fun onPianoAutoPlayStart() {
		Log.d("Namzzz", "MainPianoActivity: onPianoAutoPlayStart")
	}

	override fun onPianoAutoPlayEnd() {
		Log.d("Namzzz", "MainPianoActivity: onPianoAutoPlayEnd")
		isAutoPlaying = false
		viewButtonPlay()
	}

	override fun onDestroy() {
		super.onDestroy()
		binding.pianoView.releaseAutoPlay()
		job?.cancel()
	}

	override fun saveRecordCallback(accept: Boolean, name: String) {
		val exception = CoroutineExceptionHandler { _, throwable ->
			Log.e("Namzzz", "MainPianoActivity: saveRecordCallback", throwable)
		}
		CoroutineScope(Dispatchers.IO + Job()).launch(exception) {
			val job = async {
				if (accept) {
					val recordDataObject = RecordDataObject(
						name = name,
						dataGson = dataConvertToGson,
						timeRecord = getCurrentFormattedDate(),
						lengthRecord = lengthRecord
					)
					pianoRecordDao.addPianoRecord(recordDataObject)
				}
				true
			}

			if (job.await()) {
				if (accept) {
					binding.btnSelectAudio.text = name
					playList.clear()
					playList.addAll(recordList)
				}
				dataConvertToGson = ""
				lengthRecord = ""
				secondsElapsed = 0
				recordList.clear()
			}
			cancel()
		}
	}

	override fun renameRecordCallback(
		accept: Boolean,
		record: RecordDataObject,
		position: Int,
		name: String
	) {
		//No action
	}

	override fun deleteRecordCallback(accept: Boolean, record: RecordDataObject) {
		//No action
	}

	private val launcherOpenRecordList = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) {
		if (it.resultCode == Constant.RESULT_SELECT_RECORD) {
			val value = mainApp.getOpenRecord()[Constant.OPEN_RECORD_KEY]
			value?.let { va ->
				when (va.first) {
					Constant.SELECT_MUSIC_LIST -> {
						when (va.second) {
							0 -> {
								playList.clear()
								playList.addAll(MusicList.musicLittleStar())
								binding.btnSelectAudio.text = MusicList.listMusic[0]
							}

							1 -> {
								playList.clear()
								playList.addAll(MusicList.musicHappyBirthDay())
								binding.btnSelectAudio.text = MusicList.listMusic[1]
							}

							2 -> {
								playList.clear()
								playList.addAll(MusicList.musicJingleBell())
								binding.btnSelectAudio.text = MusicList.listMusic[2]
							}

							else -> {}
						}
					}

					Constant.SELECT_RECORD_LIST -> {
						lifecycleScope.launch {
							if (pianoRecordDao.hasInDatabase(va.second)) {
								playList.clear()
								val recordDataObject = pianoRecordDao.getPianoRecordById(va.second)
								binding.btnSelectAudio.text = recordDataObject.name
								playList.addAll(Converters.convertToList(recordDataObject.dataGson))
							}
						}
					}

					else -> {}
				}
			}
		}
	}
}