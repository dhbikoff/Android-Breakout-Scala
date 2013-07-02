package com.dhbikoff.breakout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape

class Block(rect: Rect, colorNum: Int) extends ShapeDrawable(new RectShape) {
  private val paint: Paint = new Paint
  private val blockColor = colorNum
  setBounds(rect)
  paint.setColor(colorNum)
  
  def drawBlock(canvas: Canvas): Unit = 
    canvas.drawRect(getBounds, paint)

  def color: Int = paint.getColor

  def toIntArray: Array[Int] = Array(getBounds.left, getBounds.top, 
      getBounds.right, getBounds.bottom, blockColor)
      
}