package com.kyh.priceanimationtextviewlib

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.*
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children


class PriceAnimationTextView : LinearLayout {
    var textSize: Float = DEFAULT_TEXT_SIZE.toFloat()

    private val textViewArrayList = arrayListOf<AppCompatTextView>()
    private var textStyle: Int = Typeface.BOLD
    private var hintText: CharSequence = DEFAULT_HINT_TEXT
    private var hintColor: Int = ContextCompat.getColor(context, R.color.gray_color)
    private var textColor: Int = ContextCompat.getColor(context, R.color.black_color)
    private var endText: CharSequence = DEFAULT_END_TEXT
    private var maxPrice = DEFAULT_MAX_PRICE
    private var priceAnimationTextViewListener: PriceAnimationTextViewListener? = null

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

    private fun init(attrs: AttributeSet? = null) {
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
                maxPrice =
                    getInt(R.styleable.PriceAnimationTextView_maxPrice, DEFAULT_MAX_PRICE)

                recycle()
            }
        }
        orientation = HORIZONTAL
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_RTL)
        addHintTextView()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            putParcelable(KEY_ON_SAVE_INSTANCES_TATE, super.onSaveInstanceState())
            putStringArrayList(KEY_SAVE_DATA, ArrayList(textViewArrayList.map {
                it.text.toString()
            }))
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.get(KEY_ON_SAVE_INSTANCES_TATE) as Parcelable)
            state.getStringArrayList(KEY_SAVE_DATA)?.let {
                if (it.isNotEmpty()) {
                    removeAllViews()
                    it.filter { originContent ->
                        originContent != "," && originContent != endText
                    }.forEach { filteredContent ->
                        addText(filteredContent, false)
                    }
                }
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun getText(): String {
        val strBuffer = StringBuffer()
        textViewArrayList.filterIsInstance<InputTextView>().forEach {
            strBuffer.append(it.text)
        }
        return strBuffer.toString()
    }

    fun getAllText(): String {
        if (childCount == 0) {
            return ""
        }

        if (getChildAt(0) is HintTextView) {
            return ""
        }

        val strBuffer = StringBuffer()
        children.toMutableList().asReversed().forEach {
            if (it is TextView) {
                strBuffer.append(it.text)
            }
        }
        return strBuffer.toString()
    }

    fun addText(text: CharSequence, needAnimation: Boolean = true) {
        if (TextUtils.isDigitsOnly(text).not()) {
            priceAnimationTextViewListener?.onMessageOnlyInputDigit()
            return
        }

        if (hasHintText() && text == "0") {
            priceAnimationTextViewListener?.onMessageZeroStart()
            return
        }

        if (hasHintText()) {
            textViewArrayList.clear()
            removeAllViews()
        }

        if ((getText() + text).toInt() > maxPrice) {
            for (textView in textViewArrayList) {
                textView.clearAnimation()
            }
            textViewArrayList.clear()
            removeAllViews()
            maxPrice.toString().toList().map {
                addText(it.toString(), false)
            }
            priceAnimationTextViewListener?.onMessageMaxPrice(maxPrice)
            priceAnimationTextViewListener?.onMessageMaxPrice(getAllText())
            vibrateView()
            return
        }

        if (textViewArrayList.size == 0) {
            createEndTextView().let {
                textViewArrayList.add(it)
                addView(it, 0)
            }
        }

        createInputTextView(needAnimation)
            .apply {
                setText(text)
            }.let {
                textViewArrayList.add(it)
                addView(it, 1)
            }

        refreshComma(needAnimation)
    }

    fun backButton(needAnimation: Boolean = true) {
        if (textViewArrayList.isEmpty() || textViewArrayList.first() is HintTextView) {
            return
        }

        val lastTextView = textViewArrayList.last()
        if (needAnimation) {
            val translateAnimate =
                TranslateAnimation(0f, 0f, 0f, -(lastTextView.height.toFloat() / 3))
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
        }

        removeView(lastTextView)
        textViewArrayList.remove(lastTextView)

        if (textViewArrayList.size == 1) {
            val wonTextView = textViewArrayList.last()
            removeView(wonTextView)
            textViewArrayList.remove(wonTextView)
        }
        refreshComma(needAnimation)
        addHintTextView(needAnimation)
    }

    private fun addHintTextView(needAnimation: Boolean = true) {
        if (textViewArrayList.isEmpty()) {
            createHintTextView(needAnimation).let {
                textViewArrayList.add(it)
                addView(it, 0)
            }
        }
    }

    private fun refreshComma(needAnimation: Boolean) {
        if (textViewArrayList.size > 3) {
            removeCommas()
            addCommas(needAnimation)
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

    private fun addCommas(needAnimation: Boolean) {
        val inputTextViewList = textViewArrayList.filterIsInstance<InputTextView>()
        val commaCount = (inputTextViewList.size - 1) / 3
        val inputCommaPositionList = arrayListOf<Int>()

        if (commaCount > 0) {
            for (position in 1..commaCount) {
                inputCommaPositionList.add(((position * 4) + endText.length - 1))
            }

            for (position in inputCommaPositionList) {
                createCommaTextView(needAnimation).let {
                    addView(it, position)
                    textViewArrayList.add(position, it)
                }
            }
        }
    }

    private fun createCommaTextView(needAnimation: Boolean): AppCompatTextView =
        CommaTextView(context, needAnimation).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
            gravity = Gravity.CENTER
            setTextColor(textColor)
            setTypeface(null, textStyle)
        }

    private fun createEndTextView(): AppCompatTextView = EndTextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
        gravity = Gravity.CENTER
        setTextColor(textColor)
        setTypeface(null, textStyle)
        text = endText
    }

    private fun createInputTextView(needAnimation: Boolean): AppCompatTextView =
        InputTextView(context, needAnimation).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
            gravity = Gravity.CENTER
            setTextColor(textColor)
            setTypeface(null, textStyle)
        }

    private fun createHintTextView(needAnimation: Boolean): AppCompatTextView =
        HintTextView(context, needAnimation).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, this@PriceAnimationTextView.textSize)
            gravity = Gravity.CENTER
            text = hintText
            setTextColor(hintColor)
            setTypeface(null, textStyle)
        }

    private fun hasHintText() = textViewArrayList.filterIsInstance<HintTextView>().isNotEmpty()

    fun setPriceAnimationTextViewListener(priceAnimationTextViewListener: PriceAnimationTextViewListener) {
        this.priceAnimationTextViewListener = priceAnimationTextViewListener
    }

    private inner class EndTextView : AppCompatTextView {
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

        private fun init() {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

    }

    private inner class HintTextView : AppCompatTextView {
        var isSavedData = false

        constructor(context: Context, isSavedData: Boolean) : super(context) {
            this.isSavedData = isSavedData
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
            if (isSavedData.not()) {
                alpha = 0f
                Handler().postDelayed({
                    animate().alpha(1f)
                }, 50)
            }
        }
    }

    private inner class CommaTextView : AppCompatTextView {
        var needAnimation = true

        constructor(context: Context, needAnimation: Boolean) : super(context) {
            this.needAnimation = needAnimation
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

        private fun init() {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            text = ","
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (needAnimation) {
                alpha = 0f
                Handler().postDelayed({
                    animate().alpha(1f)
                }, 50)
            }
        }
    }

    private inner class InputTextView : AppCompatTextView {
        var needAnimation = false

        constructor(context: Context, needAnimation: Boolean) : super(context) {
            this.needAnimation = needAnimation
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

        private fun init() {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        }

        override fun onDetachedFromWindow() {
            clearAnimation()
            super.onDetachedFromWindow()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (needAnimation) {
                Handler().postDelayed({
                    val translateAnimate =
                        TranslateAnimation(0f, 0f, -(height.toFloat() / 4), -5f).apply {
                            duration = 100
                        }

                    val alphaAnimate = AlphaAnimation(0f, 1f)
                        .apply {
                            duration = 100
                        }

                    val animationSet = AnimationSet(false).apply {
                        addAnimation(translateAnimate)
                        addAnimation(alphaAnimate)
                    }
                    startAnimation(animationSet)
                }, 50)
            }
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
        private const val DEFAULT_MAX_PRICE = 2000000
        private const val DEFAULT_END_TEXT = "원"
        private const val DEFAULT_HINT_TEXT = "금액 입력"
        private const val KEY_ON_SAVE_INSTANCES_TATE = "KEY_ON_SAVE_INSTANCES_TATE"
        private const val KEY_SAVE_DATA = "KEY_SAVE_DATA"
    }

}