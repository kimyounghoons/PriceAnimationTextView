package com.kyh.priceanimationtextviewlib

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat

class PriceAnimationTextView : LinearLayout {
    var textSize: Float = DEFAULT_TEXT_SIZE.toFloat()

    private val textViewArrayList = arrayListOf<AppCompatTextView>()
    private var textStyle: Int = Typeface.BOLD
    private var hintText: CharSequence = DEFAULT_HINT_TEXT
    private var hintColor: Int = ContextCompat.getColor(context, R.color.gray_color)
    private var textColor: Int = ContextCompat.getColor(context, R.color.black_color)
    private var endText: CharSequence = DEFAULT_END_TEXT
    private var maxLength = DEFAULT_MAX_LENGTH

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    fun init(attrs: AttributeSet? = null) {
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.PriceAnimationTextView).apply {
                endText =
                    getText(R.styleable.PriceAnimationTextView_endText) ?: DEFAULT_END_TEXT
                hintText =
                    getText(R.styleable.PriceAnimationTextView_hintText) ?: DEFAULT_HINT_TEXT
                textSize = getDimensionPixelSize(
                    R.styleable.PriceAnimationTextView_android_textSize,
                    DEFAULT_TEXT_SIZE
                ).toFloat()
                textColor = ContextCompat.getColor(
                    context,
                    getResourceId(
                        R.styleable.PriceAnimationTextView_android_textColor,
                        R.color.black_color
                    )
                )
                hintColor = ContextCompat.getColor(
                    context,
                    getResourceId(
                        R.styleable.PriceAnimationTextView_hintTextColor,
                        R.color.gray_color
                    )
                )
                textStyle = getInt(
                    R.styleable.PriceAnimationTextView_android_textStyle,
                    Typeface.BOLD
                )
                maxLength =
                    getInt(R.styleable.PriceAnimationTextView_android_maxLength, DEFAULT_MAX_LENGTH)

                recycle()
            }
        }
        orientation = HORIZONTAL
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_RTL)
        addHintTextView()
    }

    fun getText(): String {
        val strBuffer = StringBuffer()
        textViewArrayList.filterIsInstance<InputTextView>().forEach {
            strBuffer.append(it.text)
        }
        return strBuffer.toString()
    }

    fun addText(text: CharSequence) {
        if (text == "0") {
            return
        }

        if (textViewArrayList.first() is HintTextView) {
            textViewArrayList.first().let {
                removeView(it)
                textViewArrayList.remove(it)
            }
        }

        if (textViewArrayList.filterIsInstance<InputTextView>().size >= maxLength) {
            vibrateView()
            return
        }

        if (textViewArrayList.size == 0) {
            createWonTextView().let {
                textViewArrayList.add(it)
                addView(it, 0)
            }
        }

        createInputTextView()
            .apply {
                setText(text)
            }.let {
                textViewArrayList.add(it)
                addView(it, 1)
            }

        refreshComma()
    }

    fun backButton() {
        if (textViewArrayList.isEmpty() || textViewArrayList.first() is HintTextView) {
            return
        }

        val lastTextView = textViewArrayList.last()
        val translateAnimate = TranslateAnimation(0f, 0f, 0f, lastTextView.height.toFloat() / 3)
            .apply {
                duration = 200
            }

        val alphaAnimate = AlphaAnimation(1f, 0f)
            .apply {
                duration = 200
            }

        val animationSet = AnimationSet(false).apply {
            addAnimation(translateAnimate)
            addAnimation(alphaAnimate)
        }

        lastTextView.startAnimation(animationSet)

        removeView(lastTextView)
        textViewArrayList.remove(lastTextView)

        if (textViewArrayList.size == 1) {
            val wonTextView = textViewArrayList.last()
            removeView(wonTextView)
            textViewArrayList.remove(wonTextView)
        }
        refreshComma()

        addHintTextView()
    }

    private fun addHintTextView() {
        if (textViewArrayList.isEmpty()) {
            createHintTextView().let {
                textViewArrayList.add(it)
                addView(it, 0)
            }
        }
    }

    private fun refreshComma() {
        if (textViewArrayList.size > 3) {
            removeCommas()
            addCommas()
        }
    }

    private fun removeCommas() {
        val commas = textViewArrayList.filter {
            it is CommaTextView
        }

        for (commaTextView in commas) {
            removeView(commaTextView)
            textViewArrayList.remove(commaTextView)
        }
    }

    private fun addCommas() {
        val inputTextViewList = textViewArrayList.filterIsInstance<InputTextView>()
        val commaCount = (inputTextViewList.size - 1) / 3
        val inputCommaPositionList = arrayListOf<Int>()

        if (commaCount > 0) {
            for (position in 1..commaCount) {
                inputCommaPositionList.add(((position * 4) + endText.length - 1))
            }

            for (position in inputCommaPositionList) {
                createCommaTextView().let {
                    addView(it, position)
                    textViewArrayList.add(position, it)
                }
            }
        }
    }

    private fun createCommaTextView(): AppCompatTextView = CommaTextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
        gravity = Gravity.CENTER
        setTextColor(textColor)
        setTypeface(null, textStyle)
    }

    private fun createWonTextView(): AppCompatTextView = WonTextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
        gravity = Gravity.CENTER
        setTextColor(textColor)
        setTypeface(null, textStyle)
        text = endText
    }

    private fun createInputTextView(): AppCompatTextView = InputTextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
        gravity = Gravity.CENTER
        setTextColor(textColor)
        setTypeface(null, textStyle)
    }

    private fun createHintTextView(): AppCompatTextView = HintTextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
        gravity = Gravity.CENTER
        text = hintText
        setTextColor(hintColor)
        setTypeface(null, textStyle)
    }

    class WonTextView : AppCompatTextView {
        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            init()
        }

        fun init() {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

    }

    class HintTextView : AppCompatTextView {
        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            init()
        }

        fun init() {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            alpha = 0f
            Handler().postDelayed({
                animate().alpha(1f)
            }, 100)
        }
    }

    class CommaTextView : AppCompatTextView {
        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            init()
        }

        fun init() {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            text = ","
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            alpha = 0f
            Handler().postDelayed({
                animate().alpha(1f)
            }, 100)
        }
    }

    class InputTextView : AppCompatTextView {

        constructor(context: Context) : super(context) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        ) {
            init()
        }

        fun init() {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

        override fun onDetachedFromWindow() {
            clearAnimation()
            super.onDetachedFromWindow()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            alpha = 0f
            Handler().postDelayed({
                alpha = 1f
                val animate = TranslateAnimation(0f, 0f, -height.toFloat(), 0f)
                animate.duration = 400
                startAnimation(animate)
            }, 50)
        }

    }

    private fun View.vibrateView() {
        val vibAnim1 = ObjectAnimator.ofFloat(this, "translationX", dpToPx(-5).toFloat())
            .apply { duration = 30 }
        val vibAnim2 = ObjectAnimator.ofFloat(this, "translationX", dpToPx(4).toFloat())
            .apply { duration = 50 }
        val vibAnim3 = ObjectAnimator.ofFloat(this, "translationX", dpToPx(-3).toFloat())
            .apply { duration = 70 }
        val vibAnim4 = ObjectAnimator.ofFloat(this, "translationX", dpToPx(1).toFloat())
            .apply { duration = 100 }
        AnimatorSet().apply {
            play(vibAnim1)
            play(vibAnim2).after(vibAnim1)
            play(vibAnim3).after(vibAnim2)
            play(vibAnim4).after(vibAnim3)
            start()
        }
        vibrate(500)
    }

    private fun vibrate(vibrateDuration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    vibrateDuration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(vibrateDuration)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ) + 0.5f).toInt()
    }

    companion object {
        private const val DEFAULT_TEXT_SIZE = 25
        private const val DEFAULT_MAX_LENGTH = 7
        private const val DEFAULT_END_TEXT = "원"
        private const val DEFAULT_HINT_TEXT = "금액 입력"
    }

}