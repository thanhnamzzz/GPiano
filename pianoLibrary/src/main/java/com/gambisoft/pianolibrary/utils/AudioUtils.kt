package com.gambisoft.pianolibrary.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.SparseIntArray
import com.gambisoft.pianolibrary.entity.AutoPlayEntity
import com.gambisoft.pianolibrary.entity.Piano
import com.gambisoft.pianolibrary.entity.PianoKey
import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.gambisoft.pianolibrary.listener.LoadAudioMessage
import com.gambisoft.pianolibrary.listener.OnLoadAudioListener
import com.gambisoft.pianolibrary.listener.OnPianoAutoPlayListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AudioUtils(
	private val context: Context,
	private val loadAudioListener: OnLoadAudioListener?,
	private val onAutoPlayListener: OnPianoAutoPlayListener?,
	maxStream: Int
) : LoadAudioMessage {
	private val service: ExecutorService = Executors.newCachedThreadPool()

	//音频池，用于播放音频
	private var pool: SoundPool? = null

	//存放黑键和白键的音频加载后的ID的集合
	private var whiteKeyMusics = SparseIntArray()
	private var blackKeyMusics = SparseIntArray()

	//是否加载成功
	@Volatile
	private var isLoadFinish = false

	//是否正在加载
	@Volatile
	private var isLoading = false

	//用于处理进度消息
	private val handler = AudioStatusHandler(context.mainLooper)
	private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
	private var currentTime: Long = 0
	private var loadNum = 0
	private var minSoundId = -1
	private var maxSoundId = -1

	init {
		pool = SoundPool.Builder().setMaxStreams(maxStream)
			.setAudioAttributes(
				AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.build()
			)
			.build()
	}

	companion object {
		//最大音频数目
		private const val MAX_STREAM = 11

		@Volatile
		private var instance: AudioUtils? = null

		//消息ID
		private const val LOAD_START = 1
		private const val LOAD_FINISH = 2
		private const val LOAD_ERROR = 3
		private const val LOAD_PROGRESS = 4

		//发送进度的间隙时间
		private const val SEND_PROGRESS_MESSAGE_BREAK_TIME = 500

		//单例模式，只返回一个工具实例
		@JvmStatic
		fun getInstance(
			context: Context,
			listener: OnLoadAudioListener?,
			onAutoPlayListener: OnPianoAutoPlayListener?
		): AudioUtils {
			return getInstance(context, listener, onAutoPlayListener, MAX_STREAM)
		}

		@JvmStatic
		fun getInstance(
			context: Context, listener: OnLoadAudioListener?,
			onAutoPlayListener: OnPianoAutoPlayListener?,
			maxStream: Int
		): AudioUtils {
			if (instance == null || instance!!.pool == null) {
				synchronized(this) {
					if (instance == null || instance!!.pool == null) {
						instance = AudioUtils(context, listener, onAutoPlayListener, maxStream)
					}
				}
			}
			return instance!!
		}
	}

	fun isLoadAudioComplete():Boolean {
		return loadNum == Piano.PIANO_NUM
	}

	fun loadMusic(piano: Piano?) {
		pool?.let {
			piano?.let { piano ->
				if (isLoading || isLoadFinish) return
				isLoading = true
				it.setOnLoadCompleteListener { _, _, _ -> handleLoadComplete() }
				val exception = CoroutineExceptionHandler { _, throwable ->
					Log.e("Namzzz", "AudioUtils: loadMusic", throwable)
					isLoading = false
					sendErrorMessage(Exception(throwable.message))
				}
				CoroutineScope(Dispatchers.IO).launch(exception) {
					sendStartMessage()
					loadKeys(piano.whitePianoKeys, whiteKeyMusics, isWhiteKey = true)
					loadKeys(piano.blackPianoKeys, blackKeyMusics, isWhiteKey = false)
					cancel()
				}
			}
		}
	}

	private fun handleLoadComplete() {
		loadNum++
		if (loadNum == Piano.PIANO_NUM) {
			isLoadFinish = true
			sendProgressMessage(100)
			sendFinishMessage()
			pool?.play(whiteKeyMusics[0], 0f, 0f, 1, -1, 2f) // Chơi âm tắt tiếng để tránh độ trễ
		} else if (System.currentTimeMillis() - currentTime >= SEND_PROGRESS_MESSAGE_BREAK_TIME) {
			sendProgressMessage((loadNum.toFloat() / Piano.PIANO_NUM * 100).toInt())
			currentTime = System.currentTimeMillis()
		}
	}

	private fun loadKeys(
		keys: MutableList<MutableList<PianoKey>>,
		keyMusics: SparseIntArray,
		isWhiteKey: Boolean
	) {
		var keyPos = 0
		keys.forEach { group ->
			group.forEach { key ->
				try {
					val soundID = pool!!.load(context, key.voiceId, 1)
					keyMusics.put(keyPos++, soundID)

					if (isWhiteKey) minSoundId =
						if (minSoundId == -1) soundID else minOf(minSoundId, soundID)
					else maxSoundId = maxOf(maxSoundId, soundID)
				} catch (e: Exception) {
					isLoading = false
					sendErrorMessage(e)
					return
				}
			}
		}
	}

	fun playMusic(key: PianoKey) {
		if (!isLoadFinish) return
		service.execute {
			when (key.type) {
				PianoKeyType.BLACK -> playBlackKeyMusic(key.group, key.positionOfGroup)
				PianoKeyType.WHITE -> playWhiteKeyMusic(key.group, key.positionOfGroup)
			}
		}
	}

	private fun playWhiteKeyMusic(group: Int, positionOfGroup: Int) {
		val position = if (group == 0) positionOfGroup else (group - 1) * 7 + 2 + positionOfGroup
		play(whiteKeyMusics[position])
	}

	private fun playBlackKeyMusic(group: Int, positionOfGroup: Int) {
		val position = if (group == 0) positionOfGroup else (group - 1) * 5 + 1 + positionOfGroup
		play(blackKeyMusics[position])
	}

	private fun play(soundId: Int) {
		val actualVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
		val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
		val volume = if (maxVolume > 0) actualVolume / maxVolume else 1f
		pool?.play(soundId, volume, volume, 1, 0, 1f)
	}

	fun stop() {
		pool!!.release()
		pool = null
		whiteKeyMusics.clear()
		blackKeyMusics.clear()
	}

	override fun sendStartMessage() {
		handler.sendEmptyMessage(LOAD_START)
	}

	override fun sendFinishMessage() {
		handler.sendEmptyMessage(LOAD_FINISH)
	}

	override fun sendErrorMessage(e: Exception?) {
		handler.sendMessage(Message.obtain(handler, LOAD_ERROR, e))
	}

	override fun sendProgressMessage(progress: Int) {
		handler.sendMessage(Message.obtain(handler, LOAD_PROGRESS, progress))
	}

	private inner class AudioStatusHandler(looper: Looper) : Handler(looper) {
		override fun handleMessage(msg: Message) {
			super.handleMessage(msg)
			handleAudioStatusMessage(msg)
		}
	}

	private fun handleAudioStatusMessage(msg: Message) {
		loadAudioListener?.let { load ->
			when (msg.what) {
				LOAD_START -> load.loadPianoAudioStart()
				LOAD_FINISH -> load.loadPianoAudioFinish()
				LOAD_ERROR -> load.loadPianoAudioError(msg.obj as Exception)
				LOAD_PROGRESS -> load.loadPianoAudioProgress(msg.obj as Int)
			}
		}
	}

	private var autoPlayJob: Job? = null
	private val exceptionAutoPlay = CoroutineExceptionHandler { _, throwable ->
		Log.e("Namzzz", "AudioUtils: AutoPlay error", throwable)
		stopPlayOnlySound()
	}
	private var isAutoPlaying = false
	fun isAutoPlaying(): Boolean = isAutoPlaying

	fun autoPlayOnlySound(autoPlayEntities: MutableList<AutoPlayEntity>) {
		if (isAutoPlaying && autoPlayEntities.isEmpty() || pool == null) return
		isAutoPlaying = true
		autoPlayJob = CoroutineScope(Dispatchers.IO).launch(exceptionAutoPlay) {
			onAutoPlayListener?.onPianoAutoPlayStart()
			autoPlayEntities.forEach { entity ->
				if (!isActive) return@forEach
				when (entity.type) {
					PianoKeyType.BLACK -> playBlackKeyMusic(entity.group, entity.position)
					PianoKeyType.WHITE -> playWhiteKeyMusic(entity.group, entity.position)
				}
//				delay(2 * entity.currentBreakTime.div(2L))
//				delay(50)
			}
			stopPlayOnlySound()
		}
	}

	fun stopPlayOnlySound() {
		autoPlayJob?.cancel()
		autoPlayJob = null
		isAutoPlaying = false
		onAutoPlayListener?.onPianoAutoPlayEnd()
	}
}
