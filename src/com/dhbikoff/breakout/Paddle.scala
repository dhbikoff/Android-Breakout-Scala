package com.dhbikoff.breakout

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.Log

class Paddle extends ShapeDrawable(new RectShape) {

  var screenWidth = 0
  var screenHeight = 0
  var paddleWidth = 0
  var paddleHeight = 0
  var paddleOffset = 0
  var paddleMoveOffset = 0

  var left = 0
  var right = 0
  var top = 0
  var bottom = 0
  
  this.getPaint.setColor(Color.WHITE)

  def drawPaddle(canvas: Canvas): Unit = {
    this.setBounds(left, top, right, bottom)
    this.draw(canvas)
  }

  def initCoords(width: Int, height: Int): Unit = {
    screenHeight = height
    screenWidth = width
    paddleMoveOffset = screenWidth / 15

    paddleWidth = screenWidth / 10
    paddleHeight = screenWidth / 72
    paddleOffset = screenHeight / 6

    left = (screenWidth / 2) - paddleWidth
    right = (screenWidth / 2) + paddleWidth
    top = (screenHeight - paddleOffset) - paddleHeight
    bottom = (screenHeight - paddleOffset) + paddleHeight

  }

  def movePaddle(x: Int): Unit = {
    if (x >= left && x <= right) {
      left = x - paddleWidth
      right = x + paddleWidth
    } else if (x > right) {
      left += paddleMoveOffset
      right += paddleMoveOffset
    } else if (x < left) {
      left -= paddleMoveOffset
      right -= paddleMoveOffset
    }

    if (left < 0) {
      left = 0
      right = paddleWidth * 2
    }

    if (right > screenWidth) {
      right = screenWidth
      left = screenWidth - (paddleWidth * 2)
    }
  }
}

