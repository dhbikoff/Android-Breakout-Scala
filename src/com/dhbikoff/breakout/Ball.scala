package com.dhbikoff.breakout

import java.util.Random

import scala.collection.mutable.ArrayBuffer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.media.AudioManager
import android.media.SoundPool

class Ball(context: Context, sound: Boolean) extends ShapeDrawable(new OvalShape) {
  this.getPaint.setColor(Color.CYAN)
  val soundOn = sound
  val soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0)
  val blockSoundId = soundPool.load(context, R.raw.block, 0)
  val paddleSoundId = soundPool.load(context, R.raw.paddle, 0)
  val bottomSoundId = soundPool.load(context, R.raw.bottom, 0)

  // ball dimensions
  var left = 0
  var right = 0
  var top = 0
  var bottom = 0
  var radius = 0

  // ball speed
  var velocityX = 0
  var velocityY = 0

  // timer when ball hits screen bottom
  val ResetBallTimer = 1000

  var ScreenWidth = 0
  var ScreenHeight = 0

  var paddleCollision = false
  var blockCollision = false
  var mPaddle: Rect = null
  var ballRect: Rect = null

  def initCoords(width: Int, height: Int) {
    val rnd = new Random
    paddleCollision = false
    blockCollision = false

    ScreenWidth = width
    ScreenHeight = height
    radius = ScreenWidth / 72
    velocityX = radius
    velocityY = radius * 2

    // ball coordinates
    left = (ScreenWidth / 2) - radius
    right = (ScreenWidth / 2) + radius
    top = (ScreenHeight / 2) - radius
    bottom = (ScreenHeight / 2) + radius

    // random start direction
    if (rnd.nextInt(2) > 0) {
      velocityX = -velocityX
    }
  }

  def drawBall(canvas: Canvas) {
    this.setBounds(left, top, right, bottom)
    this.draw(canvas)
  }

  def setVelocity: Int = {
    var bottomHit = 0

    if (blockCollision) {
      velocityY = -velocityY
      blockCollision = false // reset
    }

    // paddle collision
    if (paddleCollision && velocityY > 0) {
      val paddleSplit = (mPaddle.right - mPaddle.left) / 4
      val ballCenter = ballRect.centerX
      if (ballCenter < mPaddle.left + paddleSplit) {
        velocityX = -(radius * 3)
      } else if (ballCenter < mPaddle.left + (paddleSplit * 2)) {
        velocityX = -(radius * 2)
      } else if (ballCenter < mPaddle.centerX + paddleSplit) {
        velocityX = radius * 2
      } else {
        velocityX = radius * 3
      }
      velocityY = -velocityY
    }

    // side walls collision
    if (this.getBounds.right >= ScreenWidth) {
      velocityX = -velocityX
    } else if (this.getBounds.left <= 0) {
      this.setBounds(0, top, radius * 2, bottom)
      velocityX = -velocityX
    }

    // screen top/bottom collisions
    if (this.getBounds.top <= 0) {
      velocityY = -velocityY
    } else if (this.getBounds.top > ScreenHeight) {
      bottomHit = 1 // lose a turn
      if (soundOn) {
        soundPool.play(bottomSoundId, 1, 1, 1, 0, 1)
      }
      try {
        Thread.sleep(ResetBallTimer)
        initCoords(ScreenWidth, ScreenHeight) // reset ball
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }

    // move ball
    left += velocityX
    right += velocityX
    top += velocityY
    bottom += velocityY
    return bottomHit
  }

  def checkPaddleCollision(paddle: Paddle): Boolean = {
    mPaddle = paddle.getBounds
    ballRect = this.getBounds

    if (ballRect.left >= mPaddle.left - (radius * 2)
      && ballRect.right <= mPaddle.right + (radius * 2)
      && ballRect.bottom >= mPaddle.top - (radius * 2)
      && ballRect.top < mPaddle.bottom) {
      
      if (soundOn && velocityY > 0) {
        soundPool.play(paddleSoundId, 1, 1, 1, 0, 1)
      }      
      paddleCollision = true
    } else
      paddleCollision = false
      
    paddleCollision
  }

  def checkBlocksCollision(blocks: ArrayBuffer[Block]): Int = {
    var points = 0
    var blockListLength = blocks.size
    ballRect = this.getBounds()
    var color = 0

    val ballLeft = ballRect.left + velocityX
    val ballRight = ballRect.right + velocityY
    val ballTop = ballRect.top + velocityY
    val ballBottom = ballRect.bottom + velocityY

    // check collision remove block if true
    for (i <- (blockListLength - 1) to 0 by -1) {
      var localCollision = false
      var blockRect = blocks(i).getBounds
      color = blocks(i).color

      if (ballLeft >= blockRect.left - (radius * 2)
        && ballLeft <= blockRect.right + (radius * 2)
        && (ballTop == blockRect.bottom || ballTop == blockRect.top)) {
        blockCollision = true
        localCollision = true
        blocks.remove(i)
      } else if (ballRight <= blockRect.right
        && ballRight >= blockRect.left
        && ballTop <= blockRect.bottom
        && ballTop >= blockRect.top) {
        blockCollision = true
        localCollision = true
        blocks.remove(i)
      } else if (ballLeft >= blockRect.left
        && ballLeft <= blockRect.right
        && ballBottom <= blockRect.bottom
        && ballBottom >= blockRect.top) {
        blockCollision = true
        localCollision = true
        blocks.remove(i)
      } else if (ballRight <= blockRect.right
        && ballRight >= blockRect.left
        && ballBottom <= blockRect.bottom
        && ballBottom >= blockRect.top) {
        blockCollision = true
        localCollision = true
        blocks.remove(i)
      }

      if (localCollision) {
        points += getPoints(color)
      }
    }

    if (soundOn && blockCollision) {
      soundPool.play(blockSoundId, 1, 1, 1, 0, 1)
    }
    return points
  }

  def getPoints(color: Int): Int = color match {
    case Color.LTGRAY => 100
    case Color.MAGENTA => 200
    case Color.GREEN => 300
    case Color.YELLOW => 400
    case Color.RED => 500
    case _ => 0
  }

  def getVelocityY = velocityY
  def close = soundPool.release
}