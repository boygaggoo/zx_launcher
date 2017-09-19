package com.ds05.launcher.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.ds05.launcher.R;
import com.ds05.launcher.common.manager.PrefDataManager;
import com.ds05.launcher.common.utils.AppUtil;

/**
 * Created by Chongyang.Hu on 2017/1/21 0021.
 */

public final class SoundManager {

    private static final int[] ALARM_SOUND = new int[] {
            R.raw.di_xi_alarm,
            R.raw.jian_xiao
    };
    private static final int[] DOORBELL_SOUND = new int[] {
            R.raw.ding_dang,
            R.raw.ding_dong,
            R.raw.dong_dong,
            R.raw.ji_cu_qiao_men
    };
    public static final int SOUND_STREAM = AudioManager.STREAM_ALARM;
    private static final int ALARM_SOUND_LOOP = 3;
    private static final int DOORBELL_SOUND_LOOP = 2;

    private Context mContext;
    private SoundPool mSoundPool;
    private SoundPool mTestLoop;
    private int mTestLoopId = -1;

    private int mAlarmSoundId;
    private int mDoorbellSoundId;

    private float mAlarmSoundVolume;
    private float mDoorbellVolume;

    private boolean mAlarmIsSilence = false;

    private static SoundManager mInstance;

    /* Only package called */
    SoundManager(Context ctx) {
        mContext = ctx;
        mInstance = this;

        int alarmSoundIndex = PrefDataManager.getAlarmSoundIndex();
        int doorbellSoundIndex = PrefDataManager.getDoorbellSoundIndex();

        if(doorbellSoundIndex < 0 || doorbellSoundIndex >= DOORBELL_SOUND.length) {
            doorbellSoundIndex = 0;
        }
        mAlarmIsSilence = false;
        if(alarmSoundIndex == 0) {
            mAlarmIsSilence = true;
        } else if(alarmSoundIndex < 0 || alarmSoundIndex > ALARM_SOUND.length) {
            alarmSoundIndex = 0;
        }

        mSoundPool = new SoundPool(1, SOUND_STREAM, 1);
        if(!mAlarmIsSilence) {
            mAlarmSoundId = mSoundPool.load(mContext, ALARM_SOUND[alarmSoundIndex - 1], 1);
        }
        mDoorbellSoundId = mSoundPool.load(mContext, DOORBELL_SOUND[doorbellSoundIndex], 1);

        mAlarmSoundVolume = PrefDataManager.getAlarmSoundVolume();
        mDoorbellVolume = PrefDataManager.getDoorbellVolume();
    }

    public static SoundManager getInstance() {
        if(mInstance == null) {
            throw new IllegalStateException("SoundManager not create");
        }

        return mInstance;
    }

    public float getAlarmSoundVolume() {
        return mAlarmSoundVolume;
    }
    public float getDoorbellVolume() {
        return mDoorbellVolume;
    }

    public void playAlarmSound() {
        if(mAlarmIsSilence) return;

        stopTestSound();
        stopAlarmSound();
        mSoundPool.play(mAlarmSoundId, mAlarmSoundVolume, mAlarmSoundVolume, 1, ALARM_SOUND_LOOP, 1);
    }
    public void stopAlarmSound() {
        if(mAlarmIsSilence) return;
        mSoundPool.stop(mAlarmSoundId);
    }
    public void testAlarmSound(final int index, final float volume) {
        if(index < 0 || index >= ALARM_SOUND.length) {
            throw new IllegalArgumentException("Invalide sound index: " + index);
        }

        if(mTestLoop != null) {
            mTestLoop.stop(mTestLoopId);
            mTestLoop.setOnLoadCompleteListener(null);
            mTestLoop.release();
            mTestLoop = null;
        }
        mTestLoop = new SoundPool(1, SOUND_STREAM, 1);
        mTestLoop.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mTestLoop.play(mTestLoopId, volume, volume, 1, ALARM_SOUND_LOOP, 1);
            }
        });
        mTestLoopId = mTestLoop.load(mContext, ALARM_SOUND[index], 1);
    }
    public void playDoorbellSound() {
        //TODO
        if(AppUtil.isForeground(mContext,"com.ds05.launcher.CameraActivity_ZY")){
            return;
        }
        stopTestSound();
        stopDoorbellSound();
        mSoundPool.play(mDoorbellSoundId, mDoorbellVolume, mDoorbellVolume, 1, DOORBELL_SOUND_LOOP, 1);
    }
    public void stopDoorbellSound() {
        mSoundPool.stop(mDoorbellSoundId);
    }
    public void testDoorbellSound(final int index, final float volume) {
        if(index < 0 || index >= DOORBELL_SOUND.length) {
            throw new IllegalArgumentException("Invalide sound index: " + index);
        }

        if(mTestLoop != null) {
            mTestLoop.stop(mTestLoopId);
            mTestLoop.setOnLoadCompleteListener(null);
            mTestLoop.release();
            mTestLoop = null;
        }
        mTestLoop = new SoundPool(1, SOUND_STREAM, 1);
        mTestLoop.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mTestLoop.play(mTestLoopId, volume, volume, 1, DOORBELL_SOUND_LOOP, 1);
            }
        });
        mTestLoopId = mTestLoop.load(mContext, DOORBELL_SOUND[index], 1);
    }

    public void stopTestSound() {
        if(mTestLoopId < 0) return;
        mTestLoop.stop(mTestLoopId);
        mTestLoopId = -1;
    }

    public void updateAlarmConfig() {
        if(!mAlarmIsSilence) {
            mSoundPool.unload(mAlarmSoundId);
        }

        int index = PrefDataManager.getAlarmSoundIndex();
        if(index < 0 || index > ALARM_SOUND.length) {
            index = 0;
        }

        if(index == 0) {
            mAlarmIsSilence = true;
        } else {
            mAlarmIsSilence = false;
        }

        if(!mAlarmIsSilence) {
            mAlarmSoundId = mSoundPool.load(mContext, ALARM_SOUND[index - 1], 1);
        }
        mAlarmSoundVolume = PrefDataManager.getAlarmSoundVolume();
    }

    public void updateDoorbellConfig() {
        mSoundPool.unload(mDoorbellSoundId);
        int index = PrefDataManager.getDoorbellSoundIndex();
        if(index < 0 || index >= DOORBELL_SOUND.length) {
            index = 0;
        }

        mDoorbellSoundId = mSoundPool.load(mContext, DOORBELL_SOUND[index], 1);
        mDoorbellVolume = PrefDataManager.getDoorbellVolume();
    }
}
