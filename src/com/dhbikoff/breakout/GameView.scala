package com.dhbikoff.breakout

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.StreamCorruptedException

import scala.collection.mutable
import scala.compat.Platform

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.SurfaceView

class GameView(context: Context, newGameFlag: Int, sound: Boolean) extends SurfaceView(context) with Runnable {
  val objs = Objs(getContext, sound)
  val score = "SCORE = "
  val PlayerTurnsNum = 3
  val GetReady = "GET READY..."
  val PlayerTurnsText = "TURNS = "
  val FilePath = "data/data/com.dhbikoff.breakout/data.dat"
  val FrameRate: Long = 15
  val StartTimer = 60
  val scorePaint = paint(Color.WHITE, 25, Paint.Align.LEFT)
  val turnsPaint = paint(Color.WHITE, 25, Paint.Align.RIGHT)
  val initGameStateVal = if (newGameFlag == 1) "NewGameState" else "LoadGameState"

  var gameThread = new Thread(this)
  var playerTurns = PlayerTurnsNum
  var timer = StartTimer
  var touchEvent = false
  var running = false
  var eventX = 0.0
  var points = 0

  case class Objs(context: Context, sound: Boolean) {
    val ball = new Ball(context, sound)
    val paddle = new Paddle
    val blocksList = new mutable.ArrayBuffer[Block]
  }

  case class State(curr: String) {
    val current = curr
    def update = current match {
      case "GetReadyState" =>
        State("RunningState")
      case "NextLevelState" | "GameOverState" =>
        State("NewGameState")
      case "NewGameState" | "LoadGameState" =>
        State("GetReadyState")
      case _ =>
        throw new IllegalArgumentException("Next State Unknown!")
    }
  }

  def run(): Unit = {
    var startDelta: Long = 0
    var finishDelta: Long = 0
    var state = State(initGameStateVal)

    while (running) {
      finishDelta = Platform.currentTime
      val diff = FrameRate - (finishDelta - startDelta)
      val delta = if (diff >= 0) diff else FrameRate
      try {
        Thread.sleep(delta)
      } catch {
        case e: InterruptedException => e.printStackTrace
      }
      startDelta = Platform.currentTime
      val holder = getHolder
      if (holder.getSurface.isValid) {
        val canvas = holder.lockCanvas
        canvas.drawColor(Color.BLACK)
        state = updateState(state, canvas)
        if (touchEvent)
          objs.paddle.movePaddle(eventX.asInstanceOf[Int])
        drawObjects(canvas)
        canvas.drawText(score + points, 0, 25, scorePaint)
        canvas.drawText(PlayerTurnsText + playerTurns, canvas.getWidth, 25, turnsPaint)
        holder.unlockCanvasAndPost(canvas)
      }
    }
  }

  private def updateState(state: State, canvas: Canvas): State = {
    state.current match {
      case "NewGameState" =>
        resetBlocks(canvas)
        resetBallAndPaddle(canvas, state)
      case "LoadGameState" =>
        val data = restoreGameData
        points = data._1
        playerTurns = data._2
        resetBallAndPaddle(canvas, state)
      case "RunningState" =>
        engine(canvas, state)
      case "NextLevelState" =>
        playerTurns += 1
        state.update
      case "GetReadyState" =>
        showGetReady(canvas, objs.ball, state)
      case "GameOverState" =>
        gameOver(canvas, state)
    }
  }

  private def engine(canvas: Canvas, state: State): State = {
    playerTurns -= objs.ball.setVelocity
    objs.ball.checkPaddleCollision(objs.paddle)
    points += objs.ball.checkBlocksCollision(objs.blocksList)

    if (playerTurns < 0) {
      playerTurns = 0
      State("GameOverState")
    } else if (objs.blocksList.size == 0) State("NextLevelState")
    else state
  }

  private def gameOver(canvas: Canvas, state: State): State = {
    val gameOverPaint = new Paint
    gameOverPaint.setTextAlign(Paint.Align.CENTER)
    gameOverPaint.setColor(Color.RED)
    gameOverPaint.setTextSize(45)
    canvas.drawText("GAME OVER!!!", canvas.getWidth / 2,
      (canvas.getHeight / 2) - (objs.ball.getBounds.height), gameOverPaint)

    if (timer <= 0) {
      timer = StartTimer
      points = 0
      playerTurns = PlayerTurnsNum
      state.update
    } else {
      timer -= 1
      state
    }
  }
  private def paint(color: Int, pt: Int, align: Paint.Align): Paint = {
    val p = new Paint
    p.setColor(color)
    p.setTextSize(pt)
    p.setTextAlign(align)
    p
  }

  private def drawObjects(canvas: Canvas) = {
    objs.blocksList foreach { x: Block => x.drawBlock(canvas) }
    objs.paddle.drawPaddle(canvas)
    objs.ball.drawBall(canvas)
  }

  private def showGetReady(canvas: Canvas, ball: Ball, state: State): State = {
    val getReadyPaint = new Paint
    getReadyPaint.setTextAlign(Paint.Align.CENTER)
    getReadyPaint.setColor(Color.WHITE)
    getReadyPaint.setTextSize(45)
    canvas.drawText(GetReady, canvas.getWidth / 2,
      (canvas.getHeight / 2) - (ball.getBounds.height), getReadyPaint)

    if (timer <= 0) {
      timer = StartTimer
      state.update
    } else {
      timer -= 1
      state
    }
  }

  private def resetBlocks(canvas: Canvas) = {
    val blockHeight = canvas.getWidth / 36
    val spacing = canvas.getWidth / 144
    val topOffset = canvas.getHeight / 10
    val blockWidth = (canvas.getWidth / 10) - spacing

    for (i <- 0 until 10) {
      for (j <- 0 until 10) {
        val y_coordinate = (i * (blockHeight + spacing)) + topOffset
        val x_coordinate = j * (blockWidth + spacing)
        val r = new Rect
        r.set(x_coordinate, y_coordinate, x_coordinate + blockWidth,
          y_coordinate + blockHeight)

        val color = i match {
          case x if (x < 2) => Color.RED
          case x if (x < 4) => Color.YELLOW
          case x if (x < 6) => Color.GREEN
          case x if (x < 8) => Color.MAGENTA
          case _ => Color.LTGRAY
        }
        val block = new Block(r, color)
        objs.blocksList.append(block)
      }
    }
  }

  private def resetBallAndPaddle(canvas: Canvas, state: State): State = {
    touchEvent = false // reset paddle location
    objs.ball.initCoords(canvas.getWidth, canvas.getHeight)
    objs.paddle.initCoords(canvas.getWidth, canvas.getHeight)
    state.update
  }

  private def restoreGameData: (Int, Int) = {
    var pts = 0
    var turns = PlayerTurnsNum
    try {
      val fis = new FileInputStream(FilePath)
      val ois = new ObjectInputStream(fis)
      pts = ois.readInt()
      turns = ois.readInt()
      val arr = (ois.readObject).asInstanceOf[mutable.ArrayBuffer[Array[Int]]]
      restoreBlocks(arr)
      ois.close()
      fis.close()
    } catch {
      case e: FileNotFoundException => e.printStackTrace()
      case e: StreamCorruptedException => e.printStackTrace()
      case e: IOException => e.printStackTrace()
      case e: ClassNotFoundException => e.printStackTrace()
    }
    (pts, turns)
  }

  private def restoreBlocks(arr: mutable.ArrayBuffer[Array[Int]]) = {
    arr foreach { blockArr: Array[Int] =>
      val rect = new Rect(blockArr(0), blockArr(1), blockArr(2), blockArr(3))
      val block = new Block(rect, blockArr(4))
      objs.blocksList.append(block)
    }
  }

  private def saveGameData() = {
    val arr = new mutable.ArrayBuffer[Array[Int]]
    for (i <- 0 until objs.blocksList.size) {
      arr.append(objs.blocksList(i).toIntArray)
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

  def pause(): Unit = {
    saveGameData()
    running = false
    try {
      gameThread.join
    } catch {
      case e: InterruptedException => e.printStackTrace
    }
    gameThread = null
    objs.ball.close
  }

  def resume(): Unit = {
    running = true
    gameThread = new Thread(this)
    gameThread start
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    if (event.getAction == MotionEvent.ACTION_DOWN
      || event.getAction == MotionEvent.ACTION_MOVE) {
      eventX = event.getX
      touchEvent = true
    }
    touchEvent
  }
}
