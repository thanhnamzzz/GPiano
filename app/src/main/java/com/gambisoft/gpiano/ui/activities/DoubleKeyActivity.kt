package com.gambisoft.gpiano.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gambisoft.gpiano.R
import com.gambisoft.gpiano.app.BaseActivity
import com.gambisoft.gpiano.app.StatePianoKeyManager
import com.gambisoft.gpiano.callback.IDialogManager
import com.gambisoft.gpiano.database.PianoRecordDao
import com.gambisoft.gpiano.database.PianoRecordDatabase
import com.gambisoft.gpiano.databinding.ActivityDoubleKeyBinding
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
import com.gambisoft.pianolibrary.utils.AudioUtils
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

class DoubleKeyActivity :
	BaseActivity<ActivityDoubleKeyBinding>(ActivityDoubleKeyBinding::inflate),
	OnPianoListener, OnLoadAudioListener, OnPianoAutoPlayListener, IDialogManager {
	private lateinit var dialogManager: DialogManager
	private val statePianoKey: StatePianoKeyManager
		get() = StatePianoKeyManager(this)
	private var isDoubleKey = true

	private var isRecordPiano = false
	private var lastClickTime: Long = 0
	private val recordList = mutableListOf<AutoPlayEntity>()
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
		pianoRecordDatabase = PianoRecordDatabase.getInstance(this)

		isDoubleKey = intent.getBooleanExtra(Constant.OPTION_DOUBLE_KEY, true)
		val audioUtils = AudioUtils.getInstance(this, this, this)
		if (!audioUtils.isLoadAudioComplete())
			dialogLoad.showLoading()

		binding.pianoView1.apply {
			setSoundPollMaxStream(10)
			setPianoListener(this@DoubleKeyActivity)
			setAutoPlayListener(this@DoubleKeyActivity)
			setLoadAudioListener(this@DoubleKeyActivity)
		}
		binding.pianoView2.apply {
			setSoundPollMaxStream(10)
			setPianoListener(this@DoubleKeyActivity)
			setAutoPlayListener(this@DoubleKeyActivity)
			setLoadAudioListener(this@DoubleKeyActivity)
		}
		binding.pianoView3.apply {
			setSoundPollMaxStream(10)
			setPianoListener(this@DoubleKeyActivity)
			setAutoPlayListener(this@DoubleKeyActivity)
			setLoadAudioListener(this@DoubleKeyActivity)
		}

		binding.seekbarScroll1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				binding.pianoView1.scroll(p1)
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {

			}

			override fun onStopTrackingTouch(p0: SeekBar?) {

			}
		})

		binding.seekbarScroll2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				binding.pianoView2.scroll(p1)
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {

			}

			override fun onStopTrackingTouch(p0: SeekBar?) {

			}
		})

		binding.seekbarScroll3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
				binding.pianoView3.scroll(p1)
			}

			override fun onStartTrackingTouch(p0: SeekBar?) {

			}

			override fun onStopTrackingTouch(p0: SeekBar?) {

			}
		})

		binding.btnShowKey.setOnClickListener {
			val show = !statePianoKey.showKeyName()
			binding.pianoView1.showKeyName(show)
			binding.pianoView2.showKeyName(show)
			binding.pianoView3.showKeyName(show)
			statePianoKey.setShowKeyName(show)
			viewButtonShowKeyName()
		}

		binding.btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		binding.btnChangeTheme.setOnClickListener {
			startActivity(Intent(this, ThemePianoActivity::class.java))
//			when (binding.pianoView1.getLayoutKey()) {
//				PianoLayout.Basic -> {
//					binding.pianoView1.setLayoutKey(PianoLayout.Noel)
//					binding.pianoView2.setLayoutKey(PianoLayout.Noel)
//					binding.pianoView3.setLayoutKey(PianoLayout.Noel)
//					loadImage(
//						binding.imageBackground,
//						com.gambisoft.pianolibrary.R.mipmap.noel_background
//					)
//				}
//
//				PianoLayout.Noel -> {
//					binding.pianoView1.setLayoutKey(PianoLayout.Anime)
//					binding.pianoView2.setLayoutKey(PianoLayout.Anime)
//					binding.pianoView3.setLayoutKey(PianoLayout.Anime)
//					loadImage(
//						binding.imageBackground,
//						com.gambisoft.pianolibrary.R.mipmap.anime_background
//					)
//				}
//
//				PianoLayout.Anime -> {
//					binding.pianoView1.setLayoutKey(PianoLayout.Basic)
//					binding.pianoView2.setLayoutKey(PianoLayout.Basic)
//					binding.pianoView3.setLayoutKey(PianoLayout.Basic)
//					loadImage(binding.imageBackground, R.color.none)
//				}
//			}
//			statePianoKey.setLayoutPianoKey(binding.pianoView1.getLayoutKey())
		}

		binding.btnZoomMinus.setOnClickListener {
			if (binding.pianoView1.getScaleWidth() != 1f) {
				val scale = (binding.pianoView1.getScaleWidth() - 0.2f).coerceAtLeast(1f)
				binding.pianoView1.changeScaleWidth(scale)
				binding.pianoView2.changeScaleWidth(scale)
				binding.pianoView3.changeScaleWidth(scale)
			}
			viewButtonZoomKey()
			statePianoKey.setScaleWidth(binding.pianoView1.getScaleWidth())
		}

		binding.btnZoomPlus.setOnClickListener {
			if (binding.pianoView1.getScaleWidth() != 1.8f) {
				val scale = (binding.pianoView1.getScaleWidth() + 0.2f).coerceAtMost(1.8f)
				binding.pianoView1.changeScaleWidth(scale)
				binding.pianoView2.changeScaleWidth(scale)
				binding.pianoView3.changeScaleWidth(scale)
			}
			viewButtonZoomKey()
			statePianoKey.setScaleWidth(binding.pianoView1.getScaleWidth())
		}

		binding.btnDoubleKey.setOnClickListener {
			if (!isDoubleKey) {
				isDoubleKey = true
				inView()
			}
		}

		binding.btnTwoPlayer.setOnClickListener {
			if (isDoubleKey) {
				isDoubleKey = false
				inView()
			}
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
				secondsElapsed = 0
				job?.cancel()
				dataConvertToGson = Converters.convertToGson(recordList)
				dialogManager.showDialogSaveRecord()
			}
		}
	}

	private fun viewButtonZoomKey() {
		when (binding.pianoView1.getScaleWidth()) {
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

	private fun inView() {
		if (isDoubleKey) {
			binding.llPianoView1.visibility()
			binding.llPianoView3.gone()
			binding.btnDoubleKey.setBackgroundResource(R.drawable.bg_button_click_rectangle)
			binding.btnTwoPlayer.setBackgroundResource(R.drawable.bg_button_un_click_circle)
		} else {
			binding.llPianoView1.gone()
			binding.llPianoView3.visibility()
			binding.btnDoubleKey.setBackgroundResource(R.drawable.bg_button_un_click_rectangle)
			binding.btnTwoPlayer.setBackgroundResource(R.drawable.bg_button_click_circle)
		}
	}

	override fun onResume() {
		super.onResume()
		binding.pianoView1.apply {
			setLayoutKey(statePianoKey.getLayoutPianoKey())
			showKeyName(statePianoKey.showKeyName())
			changeScaleWidth(statePianoKey.getScaleWidth())
		}
		binding.pianoView2.apply {
			setLayoutKey(statePianoKey.getLayoutPianoKey())
			showKeyName(statePianoKey.showKeyName())
			changeScaleWidth(statePianoKey.getScaleWidth())
		}
		binding.pianoView3.apply {
			setLayoutKey(statePianoKey.getLayoutPianoKey())
			showKeyName(statePianoKey.showKeyName())
			changeScaleWidth(statePianoKey.getScaleWidth())
		}
		viewButtonShowKeyName()
		inView()

		when (statePianoKey.getLayoutPianoKey()) {
			PianoLayout.Default -> loadImage(binding.imageBackground, R.color.none)
			PianoLayout.Classic -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.classic_background
			)

			PianoLayout.Anime -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.anime_background
			)

			PianoLayout.City -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.city_background
			)

			PianoLayout.Nature -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.nature_background
			)

			PianoLayout.Noel -> loadImage(
				binding.imageBackground,
				com.gambisoft.pianolibrary.R.mipmap.noel_background
			)
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

	override fun onPianoInitFinish() {

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
			Log.d(
				"Namzzz",
				"DoubleKeyActivity: sizeRecord = ${recordList.size} onPianoClick $group|$positionOfGroup"
			)
		}
	}

	override fun loadPianoAudioStart() {

	}

	override fun loadPianoAudioFinish() {
		Log.d("Namzzz", "DoubleKeyActivity: loadPianoAudioFinish")
		dialogLoad.closeLoading()
	}

	override fun loadPianoAudioError(e: Exception?) {

	}

	override fun loadPianoAudioProgress(progress: Int) {

	}

	override fun onPianoAutoPlayStart() {

	}

	override fun onPianoAutoPlayEnd() {

	}

	override fun onDestroy() {
		super.onDestroy()
//		binding.pianoView1.releaseAutoPlay()
//		binding.pianoView2.releaseAutoPlay()
//		binding.pianoView3.releaseAutoPlay()
		job?.cancel()
	}

	override fun saveRecordCallback(accept: Boolean, name: String) {
		val exception = CoroutineExceptionHandler { _, throwable ->
			Log.e("Namzzz", "DoubleKeyActivity: saveRecordCallback", throwable)
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
}