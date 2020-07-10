package viz.commonlib.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.annotation.Nullable

/**
 * @title: SlideButton
 * @projectName InsuranceDoubleRecord
 * @description:
 * @author wei
 * @date 2020-04-27 13:23
 */
class SlideButton : View {
    //状态改变监听
    interface SlideButtonOnCheckedListener {
        fun onCheckedChangeListener(isChecked: Boolean)
    }

    private var mListener: SlideButtonOnCheckedListener? = null

    //椭圆边框颜色
    private val StrokeLineColor = "#bebfc1"

    //椭圆填充颜色
    private val StrokeSolidColor = "#00ffffff"

    //圆形边框颜色
    private val CircleStrokeColor = "#abacaf"

    //圆形checked填充颜色
    private val CircleCheckedColor = "#ff5555"

    //圆形非checked填充颜色
    private val CircleNoCheckedColor = "#bebfc1"

    //圆的x轴圆心
    private var circle_x = 0f

    //是否是大圆
    private var isBigCircle = false

    //圆角矩形的高
    private var strokeHeight = 0

    //圆角矩形的半径
    private var strokeCircleRadius = 0f

    //内部圆的半径
    private var circleRadius = 0f
    private var mScroller: Scroller? = null

    //当前按钮的开关状态
    private var isChecked = false
    private var mWidth = 0
    private var mHeight = 0
    private var mPaint: Paint? = null
    private var circleStartX = 0f
    private var circleEndX = 0f
    private var centerX = 0
    private var centerY = 0
    private var preX = 0f
    private var isMove = false
    private var view_height_int = 0
    private var strokeLineColor_int = 0
    private var strokeCheckedSolidColor_int = 0
    private var strokeNoCheckedSolidColor_int = 0
    private var circleStrokeColor_int = 0
    private var circleChecked_int = 0
    private var circleNoCheckedColor_int = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    fun toggle() {
        setChecked(!isChecked)
    }

    /**
     * * 设置小圆模式
     *
     * @param strokeLineColor      圆角矩形的边颜色
     * @param strokeSolidColor     圆角矩形的填充颜色
     * @param circleCheckedColor   内部小圆被选中的颜色
     * @param circleNoCheckedColor 内部小圆未被选中的颜色
     */
    fun setSmallCircleModel(
        strokeLineColor: Int,
        strokeSolidColor: Int,
        circleCheckedColor: Int,
        circleNoCheckedColor: Int
    ) {
        isBigCircle = false
        strokeLineColor_int = strokeLineColor
        strokeNoCheckedSolidColor_int = strokeSolidColor
        circleChecked_int = circleCheckedColor
        circleNoCheckedColor_int = circleNoCheckedColor
        invalidate()
    }

    /**
     * 设置大圆模式
     *
     * @param strokeLineColor           圆角矩形边线颜色
     * @param strokeCheckedSolidColor   圆角矩形选择状态下的填充颜色
     * @param strokeNoCheckedSolidColor 圆角矩形非选择状态下填充颜色
     * @param circleChecked             滑动圆选择状态下的填充颜色
     * @param circleNoCheckColor        滑动圆非选中状态下的填充颜色
     */
    fun setBigCircleModel(
        strokeLineColor: Int, strokeCheckedSolidColor: Int,
        strokeNoCheckedSolidColor: Int, circleChecked: Int,
        circleNoCheckColor: Int
    ) {
        isBigCircle = true
        strokeLineColor_int = strokeLineColor
        strokeCheckedSolidColor_int = strokeCheckedSolidColor
        strokeNoCheckedSolidColor_int = strokeNoCheckedSolidColor
        circleChecked_int = circleChecked
        circleNoCheckedColor_int = circleNoCheckColor
        invalidate()
    }

    /**
     * 设置点击监听
     *
     * @param listener
     */
    fun setOnCheckedListener(listener: SlideButtonOnCheckedListener?) {
        mListener = listener
    }

    /**
     * 设置按钮状态
     *
     * @param checked
     */
    fun setChecked(checked: Boolean) {
        isChecked = checked
        circle_x = if (isChecked) {
            circleEndX
        } else {
            circleStartX
        }
        if (mListener != null) {
            mListener!!.onCheckedChangeListener(isChecked)
        }
        invalidate()
    }

    private fun init(context: Context) {
        isEnabled = true
        isClickable = true
        mPaint = Paint()
        mScroller = Scroller(context)
        view_height_int =
            dip2px(context, VIEW_HEIGHT.toFloat())
        strokeLineColor_int = Color.parseColor(StrokeLineColor)
        strokeNoCheckedSolidColor_int = Color.parseColor(StrokeSolidColor)
        circleStrokeColor_int = Color.parseColor(CircleStrokeColor)
        circleChecked_int = Color.parseColor(CircleCheckedColor)
        circleNoCheckedColor_int = Color.parseColor(CircleNoCheckedColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode == MeasureSpec.AT_MOST) {
            //如果是wrap_content
            heightSize = view_height_int
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = heightSize * 2
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        PADDING = if (isBigCircle) {
            h / 10
        } else {
            h / 15
        }
        MOVE_DISTANCE = mWidth / 100
        //圆角椭圆的高
        strokeHeight = h - PADDING * 2
        //外部圆角矩形的半径
        strokeCircleRadius = strokeHeight / 2.toFloat()
        centerY = mHeight / 2
        //内部圆的半径
        circleRadius = if (isBigCircle) {
            strokeCircleRadius + PADDING
        } else {
            strokeCircleRadius - PADDING * 2
        }
        Log.i(
            "TAG",
            "mHeight:$mHeight   strokeCircleRadius: $strokeCircleRadius"
        )
        //内部圆的x轴起始坐标
        circleStartX = PADDING + strokeCircleRadius
        //内部圆的x轴终点坐标
        circleEndX = mWidth - circleStartX
        circle_x = if (isChecked) {
            circleEndX
        } else {
            circleStartX
        }

        //控件的中线
        centerX = mWidth / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRect(canvas)
        drawCircle(canvas)
    }

    //画圆角矩形
    private fun drawRect(canvas: Canvas) {
        mPaint!!.reset()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        if (isBigCircle && isChecked) {
            mPaint!!.color = strokeCheckedSolidColor_int
        } else {
            mPaint!!.color = strokeNoCheckedSolidColor_int
        }
        //画填充
        canvas.drawRoundRect(
            PADDING.toFloat(),
            PADDING.toFloat(),
            mWidth - PADDING.toFloat(),
            mHeight - PADDING.toFloat(),
            strokeCircleRadius,
            strokeCircleRadius,
            mPaint!!
        )

        //画边框
        mPaint!!.strokeWidth = strokeLineWidth.toFloat()
        mPaint!!.color = strokeLineColor_int
        mPaint!!.style = Paint.Style.STROKE
        canvas.drawRoundRect(
            PADDING.toFloat(),
            PADDING.toFloat(),
            mWidth - PADDING.toFloat(),
            mHeight - PADDING.toFloat(),
            strokeCircleRadius,
            strokeCircleRadius,
            mPaint!!
        )
    }

    //画里面的圆
    private fun drawCircle(canvas: Canvas) {
        mPaint!!.reset()
        mPaint!!.isAntiAlias = true
        mPaint!!.isDither = true
        var circleRadiusNew = circleRadius
        if (isBigCircle) {
            circleRadiusNew -= circleStrokeWidth.toFloat()
        }
        if (isChecked) {
            mPaint!!.color = circleChecked_int
        } else {
            mPaint!!.color = circleNoCheckedColor_int
        }
        canvas.drawCircle(circle_x, centerY.toFloat(), circleRadiusNew, mPaint!!)
        if (isBigCircle) {
            //画圆的边
            mPaint!!.color = circleStrokeColor_int
            mPaint!!.style = Paint.Style.STROKE
            mPaint!!.strokeWidth = circleStrokeWidth.toFloat()
            canvas.drawCircle(circle_x, centerY.toFloat(), circleRadiusNew, mPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                preX = event.x
                isMove = false
                circle_x = if (!isChecked) {
                    PADDING + strokeCircleRadius
                } else {
                    mWidth - PADDING - strokeCircleRadius
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val move_x = event.x
                if (Math.abs(move_x - preX) > MOVE_DISTANCE) {
                    isMove = true
                    if (move_x < circleStartX) {
                        circle_x = circleStartX
                        isChecked = false
                    } else if (move_x > circleEndX) {
                        circle_x = circleEndX
                        isChecked = true
                    } else {
                        circle_x = move_x
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                isChecked = if (isMove) {
                    if (circle_x >= centerX) {
                        //关闭(执行开启)
                        mScroller!!.startScroll(
                            circle_x.toInt(), 0,
                            (circleEndX - circle_x).toInt(), 0
                        )
                        true
                    } else {
                        //开启（执行关闭）
                        mScroller!!.startScroll(
                            circle_x.toInt(),
                            0,
                            (circleStartX - circle_x).toInt(),
                            0
                        )
                        false
                    }
                } else {
                    if (!isChecked) {
                        //关闭(执行开启)
                        mScroller!!.startScroll(
                            circle_x.toInt(), 0,
                            (circleEndX - circle_x).toInt(), 0
                        )
                        true
                    } else {
                        //开启（执行关闭）
                        mScroller!!.startScroll(
                            circle_x.toInt(),
                            0,
                            (circleStartX - circle_x).toInt(),
                            0
                        )
                        false
                    }
                }
                if (mListener != null) {
                    mListener!!.onCheckedChangeListener(isChecked)
                }
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (mScroller!!.computeScrollOffset()) {
            circle_x = mScroller!!.currX.toFloat()
            invalidate()
        }
    }

    companion object {
        //view默认的高,view默认的宽是高的两倍(单位:dp)
        const val VIEW_HEIGHT = 20

        //椭圆的边框宽度
        private const val strokeLineWidth = 3

        //圆的边框宽度
        private const val circleStrokeWidth = 3

        //控件内边距
        private var PADDING = 20

        //移动的判定距离
        private var MOVE_DISTANCE = 50

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}
