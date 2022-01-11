package swu.cr.mdraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View

class DrawView: View {
    //保存所有路径
    private var pathsArray = mutableListOf<LinePath>()
    //默认颜色
    private var paintColor = Color.BLACK
    //默认画笔粗细
    private var strokeSize = 10f
    private var mPaint = Paint().apply {
        color = paintColor
        strokeWidth = strokeSize
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    private var mPath:Path? = null

    constructor(context: Context):super(context){

    }
    constructor(context: Context,attrs:AttributeSet?):super(context, attrs){

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //绘制之前的路径
        if(pathsArray.size > 0){
            pathsArray.forEach {
                mPaint.color = it.color
                mPaint.strokeWidth = it.size
                canvas?.drawPath(it.path,mPaint)
            }
        }
        //绘制当前路径
        if (mPath != null){
            mPaint.color = paintColor
            mPaint.strokeWidth = strokeSize
            canvas?.drawPath(mPath!!,mPaint)

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                //创建新的路径
                mPath = Path()
                //当前触摸点就是这个路径的起始点
                mPath?.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE ->{
                mPath?.lineTo(event.x, event.y)
                //马上画出来
                invalidate()
            }
            MotionEvent.ACTION_UP ->{
                //保存当前路径
                pathsArray.add(LinePath(mPath!!,paintColor,strokeSize))
                mPath = null
            }
        }
        return true
    }
    /**与外部交互的事件**/
    //画笔颜色
    fun changeColor(colorString:String){
        paintColor = Color.parseColor(colorString)
    }

    //画笔粗细
    fun changeSize(size:Float){
        strokeSize = size
    }

    //撤销
    fun undo(){
        if (pathsArray.size > 0){
            pathsArray.removeLast()
            invalidate()
        }
    }

    //橡皮擦
    fun erase(){
        paintColor = Color.WHITE

    }
}