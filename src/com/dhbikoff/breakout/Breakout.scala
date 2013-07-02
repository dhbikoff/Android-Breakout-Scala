package com.dhbikoff.breakout

import android.app.Activity
import android.media.AudioManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager

class Breakout extends Activity {
  private lazy val intent = getIntent
  private lazy val newGame = intent getIntExtra ("NEW_GAME", 1)
  private lazy val sound = intent getBooleanExtra ("SOUND_ON_OFF", true)
  private lazy val gameView = new GameView(this, newGame, sound)
  
  override protected def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setVolumeControlStream(AudioManager.STREAM_MUSIC)

    // fullscreen
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    getWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN)
      
    setContentView(gameView)
  }

  override protected def onPause = {
    super.onPause()
    gameView.pause()
  }

  override protected def onResume = {
    super.onResume()
    gameView.resume()
  }
}
