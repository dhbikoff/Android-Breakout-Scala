package com.dhbikoff.breakout

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.Log

class Paddle extends ShapeDrawable(new RectShape) {
  this.getPaint.setColor(Color.WHITE)

  var ScreenWidth = 0
  var ScreenHeight = 0
  var PaddleWidth = 0
  var PaddleHeight = 0
  var PaddleOffset = 0
  var PaddleMoveOffset = 0

  var left = 0
  var right = 0
  var top = 0
  var bottom = 0

  def drawPaddle(canvas: Canvas) = {
    this.setBounds(left, top, right, bottom)
    this.draw(canvas)
  }

  def initCoords(width: Int, height: Int) {
    ScreenHeight = height
    ScreenWidth = width
    PaddleMoveOffset = ScreenWidth / 15

    PaddleWidth = ScreenWidth / 10
    PaddleHeight = ScreenWidth / 72
    PaddleOffset = ScreenHeight / 6

    left = (ScreenWidth / 2) - PaddleWidth
    right = (ScreenWidth / 2) + PaddleWidth
    top = (ScreenHeight - PaddleOffset) - PaddleHeight
    bottom = (ScreenHeight - PaddleOffset) + PaddleHeight

  }

  def movePaddle(x: Int): Unit = {
    if (x >= left && x <= right) {
      left = x - PaddleWidth
      right = x + PaddleWidth
    } else if (x > right) {
      left += PaddleMoveOffset
      right += PaddleMoveOffset
    } else if (x < left) {
      left -= PaddleMoveOffset
      right -= PaddleMoveOffset
    }

    if (left < 0) {
      left = 0
      right = PaddleWidth * 2
    }

    if (right > ScreenWidth) {
      right = ScreenWidth
      left = ScreenWidth - (PaddleWidth * 2)
    }
  }
}

