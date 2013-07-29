package com.dhbikoff.breakout

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

class SoundEffects(context: Context) {
  val soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0)
  val blockSoundId = soundPool.load(context, R.raw.block, 0)
  val paddleSoundId = soundPool.load(context, R.raw.paddle, 0)
  val bottomSoundId = soundPool.load(context, R.raw.bottom, 0)
  def playPaddle(): Unit = soundPool.play(paddleSoundId, 1, 1, 1, 0, 1)
  def playBottom(): Unit = soundPool.play(bottomSoundId, 1, 1, 1, 0, 1)
  def playBlock: Unit = soundPool.play(blockSoundId, 1, 1, 1, 0, 1)
  def close() = soundPool.release()
}