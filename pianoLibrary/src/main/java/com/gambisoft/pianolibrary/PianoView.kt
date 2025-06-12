package com.gambisoft.pianolibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.gambisoft.pianolibrary.entity.AutoPlayEntity
import com.gambisoft.pianolibrary.entity.Piano
import com.gambisoft.pianolibrary.entity.PianoKey
import com.gambisoft.pianolibrary.enums.PianoKeyType
import com.gambisoft.pianolibrary.enums.PianoLayout
import com.gambisoft.pianolibrary.listener.OnLoadAudioListener
import com.gambisoft.pianolibrary.listener.OnPianoAutoPlayListener
import com.gambisoft.pianolibrary.listener.OnPianoListener
import com.gambisoft.pianolibrary.utils.AudioUtils
import com.gambisoft.pianolibrary.utils.LayoutKeyFunction
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap

class PianoView @JvmOverloads constructor(
	private val context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
	private var piano: Piano? = null
	private var whitePianoKeys: MutableList<MutableList<PianoKey>> = mutableListOf()
	private var blackPianoKeys: MutableList<MutableList<PianoKey>> = mutableListOf()

	//被点击过的钢琴键 - The piano keys that have been clicked
	private val pressedKeys = ConcurrentLinkedQueue<PianoKey>()

	//画笔
	private val paint = Paint()

	//定义标识音名的正方形 - Define the square that identifies the phonetic name
	private val square: RectF

	//正方形背景颜色 - Square background color
	private val pianoColors: List<String>
		get() = listOf(
			"#C0C0C0",
			"#A52A2A",
			"#FF8C00",
			"#FFFF00",
			"#00FA9A",
			"#00CED1",
			"#4169E1",
			"#FFB6C1",
			"#FFEBCD"
		)

	private var showKeyName = false
	private var scaleWidth = 1f

	//播放器工具 - Player Tools
	private var utils: AudioUtils? = null

	var layoutWidth: Int = 0

	//缩放比例 - Scaling
	private var scale = 1f

	//音频加载接口 - Audio loading interface
	private var loadAudioListener: OnLoadAudioListener? = null

	//自动播放接口 - Automatic playback interface
	private var autoPlayListener: OnPianoAutoPlayListener? = null

	//接口 - interface
	private var pianoListener: OnPianoListener? = null

	//钢琴被滑动的一些属性 - Some properties of piano being sliding
	private var progress = 0

	//设置是否可以点击 - Set whether you can click
	private var canPress = true

	//是否正在自动播放 - Is it automatically playing?
	private var isAutoPlaying = false

	//初始化结束 - Initialization ends
	private var isInitFinish = false
	private var minRange = 0
	private var maxRange = 0

	private var maxStream = 0

	private val autoPlayHandler = Handler(Looper.getMainLooper()) {
		handleAutoPlay(it)
		true
	}
	private var layoutKey = PianoLayout.Default
	fun setLayoutKey(i: PianoLayout) {
		layoutKey = i
//		Log.d("Namzzz", "PianoView: setLayoutKey layoutKey = $layoutKey")
		piano = null
		requestLayout()
	}

	fun getLayoutKey(): PianoLayout {
		return layoutKey
	}

	init {
		attrs?.let {
			context.withStyledAttributes(it, R.styleable.PianoView) {
				showKeyName = getBoolean(R.styleable.PianoView_showKeyName, false)
				val layoutType = getInt(R.styleable.PianoView_layoutKey, 0)
				layoutKey = PianoLayout.fromValue(layoutType)
				scaleWidth = getFloat(R.styleable.PianoView_scaleWidth, 1f)
			}
		}
		paint.isAntiAlias = true
		paint.style = Paint.Style.FILL
		square = RectF()
	}

	private val whiteKeyDrawable: Drawable
		get() = LayoutKeyFunction.getLayoutWhiteKey(context, layoutKey)

	/** onMeasure sẽ được gọi lại khi sử dụng requestLayout() để đo lại kích thước của view.
	 *
	 * Khi gọi requestLayout nó sẽ vào onMeasure -> onLayout -> onDraw
	 */
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val whiteKeyHeight = whiteKeyDrawable.intrinsicHeight
		val width = MeasureSpec.getSize(widthMeasureSpec)
		val heightMode = MeasureSpec.getMode(heightMeasureSpec)
		var height = MeasureSpec.getSize(heightMeasureSpec)
		when (heightMode) {
			MeasureSpec.AT_MOST -> height =
				min(height.toDouble(), whiteKeyHeight.toDouble()).toInt()

			MeasureSpec.UNSPECIFIED -> height = whiteKeyHeight
			else -> {}
		}
		scale = (height - paddingTop - paddingBottom).toFloat() / (whiteKeyHeight).toFloat()
		layoutWidth = width - paddingLeft - paddingRight
		setMeasuredDimension(width, height)
	}

	/** onDraw được gọi lại từ cả requestLayout và invalidate.
	 *
	 * Nhưng invalidate sẽ chỉ gọi mỗi onDraw còn onMeasure và onLayout không được gọi
	 */
	override fun onDraw(canvas: Canvas) {
		if (piano == null) {
			minRange = 0
			maxRange = layoutWidth
			whitePianoKeys.clear()
			blackPianoKeys.clear()
			piano = Piano(context, layoutKey, scale, scaleWidth).also {
				whitePianoKeys = it.whitePianoKeys
				blackPianoKeys = it.blackPianoKeys
			}
			utils ?: run {
				utils = if (maxStream > 0) AudioUtils.getInstance(
					context,
					loadAudioListener,
					autoPlayListener,
					maxStream
				)
				else AudioUtils.getInstance(context, loadAudioListener, autoPlayListener)
				runCatching { utils!!.loadMusic(piano) }
					.onFailure { Log.e("Namzzz", "PianoView: onDraw", it) }
			}
		}
		//初始化白键 - Initialize the white key
		whitePianoKeys.forEachIndexed { i, whiteKeys ->
			whiteKeys.forEach { key ->
				key.apply {
					keyDrawable?.draw(canvas)

					/** Vẽ tên phím lên nút */
					if (showKeyName) {
						paint.color = pianoColors[i].toColorInt()
						keyDrawable?.bounds?.let { r ->
							val sideLength = (r.right - r.left) / (2 * scaleWidth)
							val left = r.left + sideLength * scaleWidth / 2
							val top = r.bottom - (sideLength * 4 / 3)
							val right = r.right - (sideLength * scaleWidth / 2)
							val bottom = r.bottom - sideLength / 3

							square.set(left, top, right, bottom)
//							canvas.drawRoundRect(square, 6f, 6f, paint)

							paint.apply {
								color =
									if (layoutKey == PianoLayout.Default ||
										layoutKey == PianoLayout.Classic
									) Color.BLACK else Color.WHITE
								textSize = sideLength / 1.8f
								textAlign = Paint.Align.CENTER
								typeface = Typeface.DEFAULT_BOLD
							}

							val baseline =
								((square.bottom + square.top - paint.fontMetricsInt.bottom - paint.fontMetricsInt.top) / 2).toInt()
							letterName?.let {
								canvas.drawText(it, square.centerX(), baseline.toFloat(), paint)
							}
						}
//					} else {
//						/** Vẽ hình lên phím trắng */
//						keyDrawable?.bounds?.let { r ->
//							val sideLength = (r.right - r.left) / (2 * scaleWidth)
//							val centerX = ((r.left + r.right) / 2).toFloat()
//							val top = r.bottom - sideLength * 2
//							val bitmap = vectorToBitmap(R.mipmap.noel_icon_1)
//							val positionLeft = centerX - bitmap.width / 2
//							canvas.drawBitmap(bitmap, positionLeft, top, null)
//						}
					}
				}
			}
		}

		blackPianoKeys.forEach { blackKeys ->
			blackKeys.forEach { key ->
				key.keyDrawable?.draw(canvas)
			}
		}
		pianoListener?.let { listener ->
			if (!isInitFinish) {
				isInitFinish = true
				listener.onPianoInitFinish()
			}
		}
	}

	private fun vectorToBitmap(vectorResId: Int): Bitmap {
		val drawable =
			AppCompatResources.getDrawable(context, vectorResId) ?: return createBitmap(1, 1)

		val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
		val canvas = Canvas(bitmap)

		drawable.setBounds(0, 0, canvas.width, canvas.height)
		drawable.draw(canvas)

		return bitmap
	}

	override fun performClick(): Boolean {
		super.performClick()
		return false
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		performClick()
		if (!canPress) return false
		val action = event.actionMasked
		when (action) {
			MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
				handleDown(event.actionIndex, event)

			MotionEvent.ACTION_MOVE -> {
				repeat(event.pointerCount) { i -> handleMove(i, event) }
				repeat(event.pointerCount) { i -> handleDown(i, event) }
			}

			MotionEvent.ACTION_POINTER_UP -> handlePointerUp(event.getPointerId(event.actionIndex))
			MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
				handleUp()
				return false
			}
		}
		return true
	}

	/**
	 * 处理按下事件
	 *
	 * @param which 那个触摸点
	 * @param event 事件对象
	 */
	private fun handleDown(which: Int, event: MotionEvent) {
		val x = event.getX(which).toInt() + this.scrollX
		val y = event.getY(which).toInt()
		//检查白键
		whitePianoKeys.forEach { keys ->
			keys.forEach { key ->
				key.takeIf { !it.isPressed && it.contains(x, y) }?.let {
					handleWhiteKeyDown(which, event, it)
				}
			}
		}
		//检查黑键
		blackPianoKeys.forEach { keys ->
			keys.forEach { key ->
				key.takeIf { !it.isPressed && it.contains(x, y) }?.let {
					handleBlackKeyDown(which, event, it)
				}
			}
		}
	}

	/**
	 * 处理白键点击
	 *
	 * @param which 那个触摸点
	 * @param event 事件
	 * @param key   钢琴按键
	 */
	private fun handleWhiteKeyDown(which: Int, event: MotionEvent?, key: PianoKey) {
		key.apply {
			keyDrawable?.state = intArrayOf(android.R.attr.state_pressed)
			isPressed = true
			fingerID = event?.getPointerId(which) ?: fingerID
		}

		pressedKeys.add(key)
		invalidate()
		utils?.playMusic(key)

		pianoListener?.onPianoClick(key.type, key.voice, key.group, key.positionOfGroup)
	}

	/**
	 * 处理黑键点击
	 *
	 * @param which 那个触摸点
	 * @param event 事件
	 * @param key   钢琴按键
	 */
	private fun handleBlackKeyDown(which: Int, event: MotionEvent?, key: PianoKey) {
		key.apply {
			keyDrawable?.state = intArrayOf(android.R.attr.state_pressed)
			isPressed = true
			fingerID = event?.getPointerId(which) ?: fingerID
		}

		pressedKeys.add(key)
		invalidate()
		utils?.playMusic(key)

		pianoListener?.onPianoClick(key.type, key.voice, key.group, key.positionOfGroup)
	}

	/**
	 * 处理滑动
	 *
	 * @param which 触摸点下标
	 * @param event 事件对象
	 */
	private fun handleMove(which: Int, event: MotionEvent) {
		val x = event.getX(which).toInt() + scrollX
		val y = event.getY(which).toInt()
		val pointerId = event.getPointerId(which)

		pressedKeys.filter { it.fingerID == pointerId && !it.contains(x, y) }
			.forEach { key ->
				key.apply {
					keyDrawable?.state = intArrayOf(-android.R.attr.state_pressed)
					isPressed = false
					resetFingerID()
				}
				invalidate()
				pressedKeys.remove(key)
			}
	}

	/**
	 * 处理多点触控时，手指抬起事件
	 *
	 * @param pointerId 触摸点ID
	 */
	private fun handlePointerUp(pointerId: Int) {
		pressedKeys.find { it.fingerID == pointerId }?.let { key ->
			key.apply {
				isPressed = false
				resetFingerID()
				keyDrawable?.state = intArrayOf(-android.R.attr.state_pressed)
			}
			invalidate()
			pressedKeys.remove(key)
		}
	}

	/**
	 * 处理最后一个手指抬起事件
	 */
	private fun handleUp() {
		if (pressedKeys.isNotEmpty()) {
			pressedKeys.forEach { key ->
				key.apply {
					keyDrawable?.state = intArrayOf(-android.R.attr.state_pressed)
					isPressed = false
				}
			}
			invalidate()
			pressedKeys.clear()
		}
	}

	private var autoPlayJob: Job? = null
	private val exceptionAutoPlay = CoroutineExceptionHandler { _, throwable ->
		Log.e("Namzzz", "PianoView: AutoPlay Error", throwable)
		stopAutoPlay()
		autoPlayHandler.sendEmptyMessage(HANDLE_AUTO_PLAY_END)
	}

	fun autoPlay(autoPlayEntities: MutableList<AutoPlayEntity>) {
		if (isAutoPlaying || autoPlayEntities.isEmpty()) return
		isAutoPlaying = true
		setCanPress(false)
		autoPlayJob = CoroutineScope(Dispatchers.IO).launch(exceptionAutoPlay) {
			autoPlayHandler.sendEmptyMessage(HANDLE_AUTO_PLAY_START)
			autoPlayEntities.forEach { entity ->
				if (!isActive) return@forEach

				val key = when (entity.type) {
					PianoKeyType.BLACK -> blackPianoKeys.getOrNull(entity.group)
						?.getOrNull(entity.position)

					PianoKeyType.WHITE -> whitePianoKeys.getOrNull(entity.group)
						?.getOrNull(entity.position)
				}
				key?.let {
					autoPlayHandler.sendMessage(Message.obtain().apply {
						what =
							if (it.type == PianoKeyType.BLACK) HANDLE_AUTO_PLAY_BLACK_DOWN else HANDLE_AUTO_PLAY_WHITE_DOWN
						obj = it
					})
				}

				delay(entity.currentBreakTime.div(2L))
				autoPlayHandler.sendEmptyMessage(HANDLE_AUTO_PLAY_KEY_UP)
				delay(entity.currentBreakTime.div(2L))
			}
			autoPlayHandler.sendEmptyMessage(HANDLE_AUTO_PLAY_END)
			stopAutoPlay()
		}
	}

	fun stopAutoPlay() {
		autoPlayJob?.cancel()
		autoPlayJob = null
		autoPlayHandler.sendEmptyMessage(HANDLE_AUTO_PLAY_KEY_UP)
		isAutoPlaying = false
		setCanPress(true)
	}

	/**
	 * 释放自动播放
	 */
	fun releaseAutoPlay() {
		utils?.stop()
	}

	val pianoWidth: Int
		get() = piano?.pianoWith ?: run { 0 }

//	/**
//	 * 设置显示音名的矩形的颜色<br></br>
//	 * **注:一共9中颜色**
//	 *
//	 * @param pianoColors 颜色数组，长度为9
//	 */
//	fun setPianoColors(pianoColors: List<String>) {
//		if (pianoColors.size == 9) {
//			this.pianoColors = pianoColors
//		}
//	}

	/**
	 * 设置是否可点击
	 *
	 * @param canPress 是否可点击
	 */
	fun setCanPress(canPress: Boolean) {
		this.canPress = canPress
	}

	fun scroll(progress: Int) {
		val maxScroll = (pianoWidth - layoutWidth).coerceAtLeast(0)
		val x = (progress * maxScroll / 100f).toInt().coerceIn(0, maxScroll)
		minRange = x
		maxRange = x + layoutWidth
		this.scrollTo(x, 0)
		this.progress = progress
	}

	fun getProgress(): Int = progress

	/**
	 * 设置soundPool maxStream
	 *
	 * @param maxStream maxStream
	 */
	fun setSoundPollMaxStream(maxStream: Int) {
		this.maxStream = maxStream
	}

	//接口
	/**
	 * 初始化钢琴相关界面
	 *
	 * @param pianoListener 钢琴接口
	 */
	fun setPianoListener(pianoListener: OnPianoListener?) {
		this.pianoListener = pianoListener
	}

	/**
	 * 设置加载音频接口
	 *
	 * @param loadAudioListener 　音频接口
	 */
	fun setLoadAudioListener(loadAudioListener: OnLoadAudioListener?) {
		this.loadAudioListener = loadAudioListener
	}

	/**
	 * 设置自动播放接口
	 *
	 * @param autoPlayListener 　自动播放接口
	 */
	fun setAutoPlayListener(autoPlayListener: OnPianoAutoPlayListener?) {
		this.autoPlayListener = autoPlayListener
	}

	/**
	 * 处理自动播放
	 *
	 * @param msg 消息实体
	 */
	private fun handleAutoPlay(msg: Message) {
		when (msg.what) {
			HANDLE_AUTO_PLAY_BLACK_DOWN -> {
				(msg.obj as? PianoKey)?.let { key ->
					runCatching {
						autoScroll(key)
						handleBlackKeyDown(-1, null, key)
					}.onFailure { Log.e("Namzzz", "PianoView: handleAutoPlay BlackKey", it) }
				}
			}

			HANDLE_AUTO_PLAY_WHITE_DOWN -> {
				(msg.obj as? PianoKey)?.let { key ->
					runCatching {
						autoScroll(key)
						handleWhiteKeyDown(-1, null, key)
					}.onFailure { Log.e("Namzzz", "PianoView: handleAutoPlay WhiteKey", it) }
				}
			}

			HANDLE_AUTO_PLAY_KEY_UP -> handleUp()
			HANDLE_AUTO_PLAY_START -> autoPlayListener?.onPianoAutoPlayStart()

			HANDLE_AUTO_PLAY_END -> {
				isAutoPlaying = false
				setCanPress(true)
				autoPlayListener?.onPianoAutoPlayEnd()
			}

			HANDLE_PLAY_SOUND -> {
				(msg.obj as? PianoKey)?.let {
					utils?.playMusic(it)
				}
			}

			HANDLE_PLAY_SOUND_END -> {
				isAutoPlaying = false
				autoPlayListener?.onPianoAutoPlayEnd()
			}
		}
	}

	/**
	 * 自动滚动
	 *
	 * @param key 　钢琴键
	 */
	private fun autoScroll(key: PianoKey?) {
		if (!isAutoPlaying) return
		key?.let { k ->
			k.areaOfKey.takeIf { it.isNotEmpty() }?.let { areas ->
				val left = areas.minOfOrNull { it?.left ?: Int.MAX_VALUE } ?: return
				val right = areas.maxOfOrNull { it?.right ?: Int.MIN_VALUE } ?: return

				if (left < minRange || right > maxRange) {
					scroll((left * 100 / pianoWidth))
				}
			}
		}
	}

	fun showKeyName(b: Boolean) {
		showKeyName = b
		invalidate()
	}

	fun isShowKeyName(): Boolean = showKeyName

	fun changeScaleWidth(f: Float) {
		this.scaleWidth = f
		piano = null
		requestLayout()
	}

	fun refreshLayout() {
		piano = null
		requestLayout()
	}

	fun getScaleWidth(): Float = scaleWidth

	override fun onRestoreInstanceState(state: Parcelable) {
		super.onRestoreInstanceState(state)
		postDelayed({ scroll(progress) }, 200)
	}

	companion object {

		//消息ID
		private const val HANDLE_AUTO_PLAY_START = 0
		private const val HANDLE_AUTO_PLAY_END = 1
		private const val HANDLE_AUTO_PLAY_BLACK_DOWN = 2
		private const val HANDLE_AUTO_PLAY_WHITE_DOWN = 3
		private const val HANDLE_AUTO_PLAY_KEY_UP = 4
		private const val HANDLE_PLAY_SOUND = 5
		private const val HANDLE_PLAY_SOUND_END = 6
	}
}
