package com.dhbikoff.breakout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape

class Block(rect: Rect, color: Int) extends ShapeDrawable(new RectShape) {
  setBounds(rect)
  val paint: Paint = new Paint
  paint.setColor(color)
  val blockColor = color

  def drawBlock(canvas: Canvas) = canvas.drawRect(getBounds, paint)

  def getColor = paint.getColor

  def toIntArray = Array(getBounds.left, getBounds.top, getBounds.right, getBounds.bottom, blockColor)
}