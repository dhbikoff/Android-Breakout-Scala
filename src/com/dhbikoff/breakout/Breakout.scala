package com.dhbikoff.breakout

import android.app.Activity
import android.media.AudioManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

class Breakout extends Activity {
  var gameView: GameView = null

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setVolumeControlStream(AudioManager.STREAM_MUSIC)

    // fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    getWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN)

    val intent = getIntent
    val newGame = intent.getIntExtra("NEW_GAME", 1)
    val sound = intent.getBooleanExtra("SOUND_ON_OFF", true)
    gameView = new GameView(this, newGame, sound)

    // init graphics and game Thread
    setContentView(gameView)
  }

  override def onPause = {
    super.onPause
    gameView.pause
  }

  override def onResume = {
    super.onResume
    gameView.resume
  }
}
