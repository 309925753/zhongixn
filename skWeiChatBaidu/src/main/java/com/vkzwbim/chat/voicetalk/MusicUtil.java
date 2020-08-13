package com.vkzwbim.chat.voicetalk;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;


import com.vkzwbim.chat.R;

import java.util.HashMap;
import java.util.Map;

public class MusicUtil {

	public static final float DEFAULT_VOLUMN = 0.5f;

	static float volumnRatio=DEFAULT_VOLUMN;
	private static Context mContext;
	private static Map<Integer,Integer> spMap = new HashMap<Integer,Integer>();
	private static SoundPool sp;
	private static int curStreamID;
	private static int bgStreamID;




	private static MediaPlayer mediaPlayer1 = null;



	public static void bofang(Context ctxt){
		mediaPlayer1 = MediaPlayer.create(ctxt, R.raw.message);
		mediaPlayer1.start();
	}
	public static void bofang2(Context ctxt){
		mediaPlayer1 = MediaPlayer.create(ctxt, R.raw.qidongvideo);
		mediaPlayer1.start();
	}


	public static void init(Context ctxt) {
		mContext = ctxt;
		if (spMap.size()==0){
			sp = new SoundPool(5, AudioManager.STREAM_SYSTEM, 0);

			spMap.put(1, sp.load(ctxt, R.raw.music1, 1));
//		spMap.put(2, sp.load(ctxt, R.raw.button1, 1));
//		spMap.put(3, sp.load(ctxt, R.raw.button2, 1));
//		spMap.put(4, sp.load(ctxt, R.raw.catch1, 1));
			spMap.put(5, sp.load(ctxt, R.raw.message, 1));
			spMap.put(6, sp.load(ctxt, R.raw.liwushengyin, 1));
		}
	}

	public static void playSound(int sound, int number) {
//		AudioManager am = (AudioManager) mContext
//				.getSystemService(Context.AUDIO_SERVICE);
//		float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//		Log.v("TT","maxVolumn: "+audioMaxVolumn+",curVolumn: "+volumnCurrent);
//		if(volumnRatio==1){
//		volumnRatio = volumnCurrent / audioMaxVolumn;
//		}
//		volumnRatio = 0.1f;
		curStreamID = sp.play(spMap.get(sound), volumnRatio, volumnRatio, 1, number, 1f);
		//sp.setVolume(Util.StreamID,volumnRatio,volumnRatio);

		if(sound == 5) {
			bgStreamID = curStreamID;
		}

//		Util.StreamID=	curStreamID;

	}

	public static void stopPlay() {
		if(curStreamID != 0) {
			sp.stop(curStreamID);
			curStreamID = 0;
		}
		if(bgStreamID != 0) {
			sp.stop(bgStreamID);
			bgStreamID = 0;
		}
	}

	public static int getVolumn() {
		return (int) (volumnRatio*100);
	}

	public static float getVolumnRatio() {
		return volumnRatio;
	}

	public static void setVolume(float a,float b) {
		volumnRatio=a;
		internalSetVolumn();
	}

	public static void setVolumn(int vol) {
		volumnRatio = vol/100.0f;
		internalSetVolumn();
	}

	private static void internalSetVolumn() {
		sp.setVolume(curStreamID,volumnRatio,volumnRatio);
		sp.setVolume(bgStreamID,volumnRatio,volumnRatio);
	}
}
