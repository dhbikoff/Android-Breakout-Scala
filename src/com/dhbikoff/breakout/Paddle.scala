package com.dhbikoff.breakout

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.WindowManager
import android.view.Display
import android.content.Context
import android.graphics.Rect

class Paddle(context: Context) extends ShapeDrawable(new RectShape) {
  val wm: WindowManager = context.
    getSystemService(Context.WINDOW_SERVICE).
    asInstanceOf[WindowManager]
  val display: Display = wm.getDefaultDisplay

  val ScreenWidth = display.getWidth
  val ScreenHeight = display.getHeight
  val paddleMoveOffset = ScreenWidth / 15
  val paddleWidth = ScreenWidth / 10
  val paddleHeight = ScreenWidth / 72
  val paddleOffset = ScreenHeight / 6
  this.getPaint.setColor(Color.WHITE)
  reset()

  def drawPaddle(canvas: Canvas): Unit = {
    this.draw(canvas)
  }

  def reset(): Unit = {
    val left = (ScreenWidth / 2) - paddleWidth
    val right = (ScreenWidth / 2) + paddleWidth
    val top = (ScreenHeight - paddleOffset) - paddleHeight
    val bottom = (ScreenHeight - paddleOffset) + paddleHeight
    this.setBounds(left, top, right, bottom)
  }

  def movePaddle(x: Int): Unit = {
    val paddleRect = this.getBounds
    val leftAndRight = checkSideBounds(x, paddleRect)

    if (leftAndRight._1 < 0) {
      this.setBounds(0, paddleRect.top, paddleWidth * 2, paddleRect.bottom)
    } else if (leftAndRight._2 > ScreenWidth) {
      this.setBounds(ScreenWidth - (paddleWidth * 2),
        paddleRect.top, ScreenWidth, paddleRect.bottom)
    } else {
      this.setBounds(leftAndRight._1, paddleRect.top, leftAndRight._2, paddleRect.bottom)
    }
  }

  private def checkSideBounds(x: Int, paddleRect: Rect): (Int, Int) = {
    if (x >= paddleRect.left && x <= paddleRect.right) {
      (x - paddleWidth, x + paddleWidth)
    } else if (x > paddleRect.right) {
      (paddleRect.left + paddleMoveOffset, paddleRect.right + paddleMoveOffset)
    } else if (x < paddleRect.left) {
      (paddleRect.left - paddleMoveOffset, paddleRect.right - paddleMoveOffset)
    } else {
      (paddleRect.left, paddleRect.right)
    }
  }
}
