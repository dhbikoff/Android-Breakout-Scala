package com.dhbikoff.breakout

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.StreamCorruptedException

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton

class Splash extends Activity {
  private val FilePath = "data/data/com.dhbikoff.breakout/data.dat"
  private val NewGame = "NEW_GAME"
  private val HighScorePref = "HIGH_SCORE_PREF"
  private val SoundOnOff = "SOUND_ON_OFF"
  private val SoundPrefs = "SOUND_PREFS"
  private val ScoreStr = "High Score = "
  private lazy val intent = new Intent(this, classOf[Breakout])
  
  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setVolumeControlStream(AudioManager.STREAM_MUSIC)
    setContentView(R.layout.splash)
  }

  private def currentScore = {
    val scoreSettings = getSharedPreferences(HighScorePref, 0)
	scoreSettings.getInt("highScore", 0)
  }
  
  private def highScore: Int = {
    var oldScore = 0
    try {
      val fis = new FileInputStream(FilePath)
      val ois = new ObjectInputStream(fis)
      oldScore = ois.readInt
      fis.close
    } catch {
      case e: FileNotFoundException => e.printStackTrace
      case e: StreamCorruptedException => e.printStackTrace
      case e: IOException => e.printStackTrace
	}
    if (oldScore > currentScore) oldScore 
    else currentScore
  }

  private def showHighScore() = {
    val highScoreView = findViewById(R.id.hiScoreView).asInstanceOf[TextView]
    highScoreView.setText(ScoreStr + highScore)
  }

  def newGame(view: View): Unit = {  
    intent.putExtra(NewGame, 1)
    intent.putExtra(SoundOnOff, currentSound)
    startActivity(intent)
  }

  def contGame(view: View): Unit = {
    intent.putExtra(NewGame, 0)
    intent.putExtra(SoundOnOff, currentSound)
    startActivity(intent)
  }

  private def savedSound: Boolean = {
    val soundSettings = getSharedPreferences(SoundPrefs, 0)
	soundSettings.getBoolean("soundOn", true)
  }
  
  private def currentSound: Boolean = {
    val soundButton = (findViewById(R.id.soundToggleButton)).asInstanceOf[ToggleButton]
    soundToggle(soundButton)
  }
  
  def soundToggle(v: View): Boolean = 
    v.asInstanceOf[ToggleButton] isChecked
    
  override protected def onResume: Unit = {
    super.onResume
	val soundButton = (findViewById(R.id.soundToggleButton)).asInstanceOf[ToggleButton]
	soundButton.setChecked(savedSound)
	showHighScore()
  }
  
  def showSource(v: View): Unit = {
    val link = new Intent
    link.setAction(Intent.ACTION_VIEW)
    link.addCategory(Intent.CATEGORY_BROWSABLE)
    link.setData(Uri.parse("https://github.com/dhbikoff/AndroidBreakoutScala"))
    startActivity(link)
  }

  override protected def onPause: Unit = {
    super.onPause
    val soundSettings = getSharedPreferences(SoundPrefs, 0)
    val highScoreSave = getSharedPreferences(HighScorePref,0)
    val soundEditor = soundSettings.edit()
    val scoreEditor = highScoreSave.edit()
    scoreEditor.putInt("highScore", highScore)
    soundEditor.putBoolean("soundOn", currentSound)
    soundEditor.commit()
    scoreEditor.commit()
  }
}
