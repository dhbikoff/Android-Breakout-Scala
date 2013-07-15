package com.dhbikoff.breakout

import java.util.Random

import scala.collection.mutable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.Display
import android.view.WindowManager

class Ball(context: Context, sound: Boolean) extends ShapeDrawable(new OvalShape) {
  this.getPaint.setColor(Color.CYAN)
  
  val wm: WindowManager = context.
    getSystemService(Context.WINDOW_SERVICE).
    asInstanceOf[WindowManager]
  val display: Display = wm.getDefaultDisplay
  val ScreenWidth = display.getWidth
  val ScreenHeight = display.getHeight
  
  val radius = ScreenWidth / 72
  val soundOn = sound
  val soundEffects = new SoundEffects(context)
  val rnd = new Random
  
  // timer when ball hits screen bottom
  val ResetBallTimer = 1000
  
  var velocityX = 0
  var velocityY = 0
  resetCoords()
  
  def resetCoords(): Unit = {
    // ball speed
    velocityX = (radius / 2)
    velocityY = radius + (ScreenHeight / 500)

    val left = (ScreenWidth / 2) - radius
    val right = (ScreenWidth / 2) + radius
    val top = (ScreenHeight / 2) - radius
    val bottom = (ScreenHeight / 2) + radius
    this.setBounds(left, top, right, bottom)

    // random start direction    
    if (rnd.nextInt(2) > 0) {
      velocityX = -velocityX
    }
  }

  def drawBall(canvas: Canvas) {
    this.draw(canvas)
  }

  def setVelocity(paddle: Paddle): Int = {
    var bottomHit = 0
    val ballRect = this.getBounds

    // side walls collision
    if (this.getBounds.right >= ScreenWidth) {
      velocityX = -velocityX
    } else if (this.getBounds.left <= 0) {
      this.setBounds(0, ballRect.top, radius * 2, ballRect.bottom)
      velocityX = -velocityX
    }

    // screen top/bottom collisions
    if (ballRect.top <= 0) {
      velocityY = -velocityY
    } else if (ballRect.top > ScreenHeight) {
      bottomHit = 1 // lose a turn
      if (soundOn) {
        soundEffects.play("bottom")
      }
      try {
        resetCoords() // reset ball
        Thread.sleep(ResetBallTimer)
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }

    // move ball
    val left = ballRect.left + velocityX
    val right = ballRect.right + velocityX
    val top = ballRect.top + velocityY
    val bottom = ballRect.bottom + velocityY
    this.setBounds(left, top, right, bottom)

    return bottomHit
  }

  def checkPaddleCollision(paddle: Paddle): Unit = {
    val paddleRect = paddle.getBounds
    val ballRect = this.getBounds

    if (Rect.intersects(ballRect, paddleRect)) {
      if (velocityY > 0) {
        val paddleSplit = (paddleRect.right - paddleRect.left) / 4
        val ballCenter = ballRect.centerX
        if (ballCenter < paddleRect.left + paddleSplit) {
          velocityX = -radius
        } else if (ballCenter < paddleRect.left + (paddleSplit * 2)) {
          velocityX = -(radius / 2)
        } else if (ballCenter < paddleRect.centerX + paddleSplit) {
          velocityX = radius / 2
        } else {
          velocityX = radius
        }
        velocityY = -velocityY

        if (soundOn) {
          soundEffects.play("paddle")
        }
      }
    }
  }

  def checkBlocksCollision(blocks: mutable.ArrayBuffer[Block]): Int = {
    var points = 0
    val blockListLength = blocks.size
    val ballRect = this.getBounds
    
    for (i <- (blockListLength - 1) to 0 by -1) {
      val blockRect = blocks(i).getBounds
      val color = blocks(i).color
    
      if (Rect.intersects(ballRect, blockRect)) {
        blocks.remove(i)
        velocityY = -velocityY
        points += getPoints(color)
        if (soundOn) {
          soundEffects.play("block")
        }
      }
    }
    return points
  }
  
  private def getPoints(color: Int): Int = color match {
    case Color.LTGRAY => 100
    case Color.MAGENTA => 200
    case Color.GREEN => 300
    case Color.YELLOW => 400
    case Color.RED => 500
    case _ => 0
  }

  def close = soundEffects.close()
}