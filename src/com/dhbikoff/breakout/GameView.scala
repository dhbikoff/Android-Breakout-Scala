package com.dhbikoff.breakout

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.StreamCorruptedException
import java.util.ArrayList

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceView

class GameView(context: Context, newGameFlag: Int, sound: Boolean) extends SurfaceView(context) with Runnable {
  val ball = new Ball(this.getContext, soundToggle)
  val paddle = new Paddle
  val blocksList = new ArrayList[Block]
  val holder = getHolder
  val score = "SCORE = "
  val PlayerTurnsNum = 3
  val GetReady = "GET READY..."
  val PlayerTurnsText = "TURNS = "
  val FilePath = "data/data/com.dhbikoff.breakout/data.dat"
  val FrameRate = 33
  val StartTimer = 66
 
  
  var startNewGame = newGameFlag // new game or continue
  var soundToggle = sound
  var showGameOverBanner = false
  var levelCompleted = 0
  var touched = false
  var eventX = 0.0
  var running = false
  var checkSize = true
  var newGame = true
  var waitCount = 0
  var points = 0
  
  var gameThread = new Thread(this)
  var canvas = new Canvas
  var playerTurns = PlayerTurnsNum  
  var scorePaint = new Paint
  var turnsPaint = new Paint
  var getReadyPaint = new Paint
  
  scorePaint.setColor(Color.WHITE)
  scorePaint.setTextSize(25)
  turnsPaint.setTextAlign(Paint.Align.RIGHT)
  turnsPaint.setColor(Color.WHITE)
  turnsPaint.setTextSize(25)
  getReadyPaint.setTextAlign(Paint.Align.CENTER)
  getReadyPaint.setColor(Color.WHITE)
  getReadyPaint.setTextSize(45)

  def run {
    while (running) {
      try {
        Thread.sleep(FrameRate)
      } catch {
        case e: InterruptedException => e.printStackTrace
      }
      
      if (holder.getSurface.isValid) {
        canvas = holder.lockCanvas
        canvas.drawColor(Color.BLACK)
        
        if (blocksList.size == 0) {
          checkSize = true
          newGame = true
          levelCompleted += 1
        }
        if (checkSize) {
          initObjects(canvas)
          checkSize = false
          // extra turn for finished level
          if (levelCompleted > 1) {
            playerTurns += 1
          }
        }
        if (touched) {
          paddle.movePaddle(eventX.asInstanceOf[Int])
        }

        drawToCanvas(canvas)

        // pause screen on new game
        if (newGame) {
          waitCount = 0
          newGame = false
        }        
        waitCount += 1
        engine(canvas, waitCount)
        var printScore = score + points
        canvas.drawText(printScore, 0, 25, scorePaint)
        var turns = PlayerTurnsText + playerTurns
        canvas.drawText(turns, canvas.getWidth, 25, turnsPaint)
        holder.unlockCanvasAndPost(canvas) // release canvas
      }
    }
  }

  def drawToCanvas(canvas: Canvas) {
    drawBlocks(canvas)
    paddle.drawPaddle(canvas)
    ball.drawBall(canvas)
  }

  def engine(canvas: Canvas, waitCt: Int) {
    if (waitCount > StartTimer) {
      showGameOverBanner = false
      playerTurns -= ball.setVelocity
      if (playerTurns < 0) {
        showGameOverBanner = true
        gameOver(canvas)
      }
      // paddle collision
      ball.checkPaddleCollision(paddle)
      // block collision and points tally
      points += ball.checkBlocksCollision(blocksList)
    } else {
      if (showGameOverBanner) {
        getReadyPaint.setColor(Color.RED)
        canvas.drawText("GAME OVER!!!", canvas.getWidth / 2,
          (canvas.getHeight / 2) - (ball.getBounds.height)
            - 50, getReadyPaint)
      }
      getReadyPaint.setColor(Color.WHITE)
      canvas.drawText(GetReady, canvas.getWidth / 2,
        (canvas.getHeight / 2) - (ball.getBounds.height),
        getReadyPaint)
    }
  }

  def gameOver(canvas: Canvas) {
    levelCompleted = 0
    points = 0
    playerTurns = PlayerTurnsNum
    blocksList.clear
  }

  def initObjects(canvas: Canvas) {
    touched = false // reset paddle location
    ball.initCoords(canvas.getWidth, canvas.getHeight)
    paddle.initCoords(canvas.getWidth, canvas.getHeight)
    if (startNewGame == 0) {
      restoreGameData
    } else {
      initBlocks(canvas)
    }
  }

  def restoreBlocks(arr: ArrayList[Array[Int]]) {
    for (i <- 0 until arr.size) {
      var r = new Rect()
      var blockNums: Array[Int] = arr.get(i)
      r.set(blockNums(0), blockNums(1), blockNums(2), blockNums(3))
      var b = new Block(r, blockNums(4))
      blocksList.add(b)
    }
  }

  def restoreGameData {
    try {
      val fis = new FileInputStream(FilePath)
      val ois = new ObjectInputStream(fis)
      points = ois.readInt // restore player points
      playerTurns = ois.readInt // restore player turns
      val arr = (ois.readObject).asInstanceOf[ArrayList[Array[Int]]]
      restoreBlocks(arr) // restore blocks
      ois.close()
      fis.close()
    } catch {
      case e: FileNotFoundException => e.printStackTrace
      case e: StreamCorruptedException => e.printStackTrace
      case e: IOException => e.printStackTrace
      case e: ClassNotFoundException => e.printStackTrace
    }
    startNewGame = 1 // only restore once
  }

  def initBlocks(canvas: Canvas) {
    val blockHeight = canvas.getWidth / 36
    val spacing = canvas.getWidth / 144
    val topOffset = canvas.getHeight / 10
    val blockWidth = (canvas.getWidth / 10) - spacing

    for (i <- 0 until 10) {
      for (j <- 0 until 10) {
        var y_coordinate = (i * (blockHeight + spacing)) + topOffset
        var x_coordinate = j * (blockWidth + spacing)
        var r = new Rect
        r.set(x_coordinate, y_coordinate, x_coordinate + blockWidth,
          y_coordinate + blockHeight)

        val color = i match {
          case x if (x < 2) => Color.RED
          case x if (x < 4) => Color.YELLOW
          case x if (x < 6) => Color.GREEN
          case x if (x < 8) => Color.MAGENTA
          case _ => Color.LTGRAY
        }
        var block = new Block(r, color)
        blocksList.add(block)
      }
    }
  }

  def drawBlocks(canvas: Canvas) {
    for (i <- 0 until blocksList.size)
      blocksList.get(i).drawBlock(canvas)
  }

  def saveGameData {
    val arr = new ArrayList[Array[Int]]
    for (i <- 0 until blocksList.size) {
      arr.add(blocksList.get(i).toIntArray)
    }
    try {
      val fos = new FileOutputStream(FilePath)
      val oos = new ObjectOutputStream(fos)
      oos.writeInt(points)
      oos.writeInt(playerTurns)
      oos.writeObject(arr)
      oos.close
      fos.close
    } catch {
      case e: FileNotFoundException => e.printStackTrace
      case e: IOException => e.printStackTrace
    }
  }

  def pause() = {
    saveGameData
    running = false
    try {
      gameThread.join
    } catch {
      case e: InterruptedException => e.printStackTrace
    }
    gameThread = null
    ball.close
  }

  def resume() = {
    running = true
    gameThread = new Thread(this)
    gameThread start
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    if (event.getAction() == MotionEvent.ACTION_DOWN
      || event.getAction() == MotionEvent.ACTION_MOVE) {
      eventX = event.getX
      touched = true
    }
    return touched
  }
}
