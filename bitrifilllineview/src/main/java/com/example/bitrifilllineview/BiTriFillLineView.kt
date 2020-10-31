package com.example.bitrifilllineview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context
import android.graphics.Path

val colors : Array<Int> = arrayOf(
    "#F44336",
    "#673AB7",
    "#FF9800",
    "#3F51B5",
    "#4CAF50"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 4
val strokeFactor : Float = 90f
val sizeFactor : Float = 3.2f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val scGap : Float = 0.02f / strokeFactor
val rot : Float = 180f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawTriFillPath(scale : Float, size : Float, paint : Paint) {
    val path : Path = Path()
    save()
    path.moveTo(-size, 0f)
    path.lineTo(0f, -size)
    path.lineTo(size, 0f)
    path.lineTo(-size, 0f)
    clipPath(path)
    drawRect(RectF(-size, -size * scale, size, 0f), paint)
    restore()
}

fun Canvas.drawBiTriFillLine(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val size : Float = Math.min(w, h) / sizeFactor
    save()
    translate(w / 2, h / 2)
    rotate(rot * sf.divideScale(2, parts))
    for (j in 0..1) {
        save()
        scale(1f, 1f - 2 * j)
        for (k in 0..1) {
            save()
            scale(1f - 2 * k, 1f)
            drawLine(-size, 0f, -size + size * sf.divideScale(0, parts), -size, paint)
            restore()
        }
        drawTriFillPath(sf.divideScale(1, parts), size, paint)
        restore()
    }
    restore()
}

fun Canvas.drawBTFLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawBiTriFillLine(scale, w, h, paint)
}

class BiTriFillLineView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BTFLNode(var i : Int, val state : State = State()) {

        private var next : BTFLNode? = null
        private var prev : BTFLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BTFLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBTFLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BTFLNode {
            var curr : BTFLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }
}