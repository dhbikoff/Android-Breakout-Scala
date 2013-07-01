package com.dhbikoff.breakout

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.StreamCorruptedException

import scala.collection.mutable.StringBuilder

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import android.util.Log

class Splash extends Activity {
  val FilePath = "data/data/com.dhbikoff.breakout/data.dat"
  val NewGame = "NEW_GAME"
  val HighScorePref = "HIGH_SCORE_PREF"
  val SoundOnOff = "SOUND_ON_OFF"
  val SoundPrefs = "SOUND_PREFS"
  val ScoreStr = "High Score = "

  var newGameVal = 0
  var highScore = 0
  var sound = true

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setVolumeControlStream(AudioManager.STREAM_MUSIC)
    setContentView(R.layout.splash)
  }

  def fetchHighScore: Int = {
    var points = 0
    try {
      val fis = new FileInputStream(FilePath)
      val ois = new ObjectInputStream(fis)
      points = ois.readInt
      fis.close
    } catch {
      case e: FileNotFoundException => e.printStackTrace
      case e: StreamCorruptedException => e.printStackTrace
      case e: IOException => e.printStackTrace
	}
    points
  }

  def showHighScore = {
    val points = fetchHighScore
    if (points > highScore)
      highScore = points

    val hiScore = (findViewById(R.id.hiScoreView)).asInstanceOf[TextView]
    hiScore.setText(ScoreStr + highScore)
  }

  def newGame(view: View) = {
    newGameVal = 1
    val intent = new Intent(this, classOf[Breakout])
    intent.putExtra(NewGame, newGameVal)
    intent.putExtra(SoundOnOff, sound)
    startActivity(intent)
  }

  def contGame(view: View) {
    newGameVal = 0
    val intent = new Intent(this, classOf[Breakout]);
    intent.putExtra(NewGame, newGameVal);
    intent.putExtra(SoundOnOff, sound);
    startActivity(intent);
  }

  
  override def onResume = {
    super.onResume
    val soundSettings = getSharedPreferences(SoundPrefs, 0)
	sound = soundSettings.getBoolean("soundOn", true)
	val scoreSettings = getSharedPreferences(HighScorePref, 0)
	highScore = scoreSettings.getInt("highScore", 0)
	val soundButton = (findViewById(R.id.soundToggleButton)).asInstanceOf[ToggleButton]
	soundButton.setChecked(sound)
	showHighScore
  }

  def soundToggle(v: View) = (v.asInstanceOf[ToggleButton]).isChecked

  def showSource(v: View) = {
    val intent = new Intent
    intent.setAction(Intent.ACTION_VIEW)
    intent.addCategory(Intent.CATEGORY_BROWSABLE)
    intent.setData(Uri.parse("http://github.com/dhbikoff/Android-Breakout"))
    startActivity(intent)
  }

  override def onPause = {
    super.onPause
    val soundSettings = getSharedPreferences(SoundPrefs, 0);
    val highScoreSave = getSharedPreferences(HighScorePref,0);
    val soundEditor = soundSettings.edit();
    val scoreEditor = highScoreSave.edit();
    scoreEditor.putInt("highScore", highScore);
    soundEditor.putBoolean("soundOn", sound);
    soundEditor.commit();
    scoreEditor.commit();
  }
}
