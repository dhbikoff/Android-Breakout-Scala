package com.dhbikoff.breakout

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.StreamCorruptedException

import scala.collection.mutable.ArrayBuffer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceView

class GameView(context: Context, newGameFlag: Int, sound: Boolean) extends SurfaceView(context) with Runnable {

  val ball = new Ball(context, sound)
  val paddle = new Paddle
  val blocksList = new ArrayBuffer[Block]
  val holder = getHolder
  val score = "SCORE = "
  val PlayerTurnsNum = 3
  val GetReady = "GET READY..."
  val PlayerTurnsText = "TURNS = "
  val FilePath = "data/data/com.dhbikoff.breakout/data.dat"
  val FrameRate = 33
  val StartTimer = 66

  var newGameState = if (newGameFlag == 1) true else false
  var loadGameState = if (newGameFlag != 1) true else false
  var getReadyState = false
  var runningState = false
  var nextLevelState = false
  var gameOverState = false
  var timer = StartTimer

  var touched = false
  var eventX = 0.0
  var running = false

  var points = 0

  var gameThread = new Thread(this)
  var canvas = new Canvas
  var playerTurns = PlayerTurnsNum
  var scorePaint = new Paint
  var turnsPaint = new Paint

  scorePaint.setColor(Color.WHITE)
  scorePaint.setTextSize(25)
  turnsPaint.setTextAlign(Paint.Align.RIGHT)
  turnsPaint.setColor(Color.WHITE)
  turnsPaint.setTextSize(25)

  var getReadyPaint = new Paint
  getReadyPaint.setTextAlign(Paint.Align.CENTER)
  getReadyPaint.setColor(Color.WHITE)
  getReadyPaint.setTextSize(45)

  def run(): Unit = {
    while (running) {
      try {
        Thread.sleep(FrameRate) // ADD DELTA, set timer?
      } catch {
        case e: InterruptedException => e.printStackTrace
      }

      if (holder.getSurface.isValid) {
        canvas = holder.lockCanvas
        canvas.drawColor(Color.BLACK)

        // BREAKUP FUNCTIONS TO MUTUALLY EXCLUSIVE BOOLEANS
        if (newGameState) {
          initObjects(canvas)
          initBlocks(canvas)
          newGameState = false
          getReadyState = true
        }
        if (loadGameState) {
          initObjects(canvas)
          restoreGameData
          loadGameState = false
          getReadyState = true
        }
        if (nextLevelState) {
          playerTurns += 1
          nextLevelState = false
          newGameState = true
        }
        if (getReadyState) {
          getReadyPaint.setColor(Color.WHITE)
          canvas.drawText(GetReady, canvas.getWidth / 2,
            (canvas.getHeight / 2) - (ball.getBounds.height),
            getReadyPaint)
          if (timer <= 0) {
            getReadyState = false
            runningState = true
            timer = StartTimer
          }
          timer -= 1
        }
        if (runningState) {
          engine(canvas)

        }
        if (gameOverState) {
          gameOver(canvas)
          getReadyPaint.setColor(Color.RED)
          canvas.drawText("GAME OVER!!!", canvas.getWidth / 2,
            (canvas.getHeight / 2) - (ball.getBounds.height) - 50, getReadyPaint)
          if (timer <= 0) {
            gameOverState = false
            newGameState = true
            timer = StartTimer
          }
          timer -= 1
        }

        if (touched) {
          paddle.movePaddle(eventX.asInstanceOf[Int])
        }

        drawToCanvas(canvas)
        canvas.drawText(score + points, 0, 25, scorePaint)
        canvas.drawText(PlayerTurnsText + playerTurns, canvas.getWidth, 25, turnsPaint)
        holder.unlockCanvasAndPost(canvas)
      }
    }
  }

  private def drawToCanvas(canvas: Canvas) {
    blocksList foreach { x: Block => x.drawBlock(canvas) }
    paddle.drawPaddle(canvas)
    ball.drawBall(canvas)
  }

  private def engine(canvas: Canvas) {
    playerTurns -= ball.setVelocity

    if (playerTurns < 0) {
      gameOverState = true
      runningState = false
    }

    // TODO Create Collision Class 
    ball.checkPaddleCollision(paddle)
    points += ball.checkBlocksCollision(blocksList)
    
    if (blocksList.size == 0) {
      runningState = false
      nextLevelState = true
    }
    
  }

  private def gameOver(canvas: Canvas) {
    points = 0
    playerTurns = PlayerTurnsNum
  }

  private def initObjects(canvas: Canvas) {
    touched = false // reset paddle location
    ball.initCoords(canvas.getWidth, canvas.getHeight)
    paddle.initCoords(canvas.getWidth, canvas.getHeight)
  }

  private def restoreGameData {
    try {
      val fis = new FileInputStream(FilePath)
      val ois = new ObjectInputStream(fis)
      points = ois.readInt()
      playerTurns = ois.readInt()
      val arr = (ois.readObject).asInstanceOf[ArrayBuffer[Array[Int]]]
      restoreBlocks(arr)
      ois.close()
      fis.close()
    } catch {
      case e: FileNotFoundException => e.printStackTrace()
      case e: StreamCorruptedException => e.printStackTrace()
      case e: IOException => e.printStackTrace()
      case e: ClassNotFoundException => e.printStackTrace()
    }
  }

  private def restoreBlocks(arr: ArrayBuffer[Array[Int]]) {
    arr foreach { blockArr: Array[Int] =>
      val rect = new Rect(blockArr(0), blockArr(1), blockArr(2), blockArr(3))
      val block = new Block(rect, blockArr(4))
      blocksList.append(block)
    }
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
        blocksList.append(block)
      }
    }
  }

  def saveGameData {
    val arr = new ArrayBuffer[Array[Int]]
    for (i <- 0 until blocksList.size) {
      arr.append(blocksList(i).toIntArray)
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
