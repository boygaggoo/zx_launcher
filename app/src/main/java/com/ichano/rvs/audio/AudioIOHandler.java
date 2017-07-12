package com.ichano.rvs.audio;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import com.ichano.rvs.internal.RvsLog;
import java.nio.ByteBuffer;
public class AudioIOHandler {
	private static final String TAG = "AudioIOHandler";
	  private static AudioIOHandler instance;
	  private ByteBuffer _playBuffer;
	  private ByteBuffer _recBuffer;
	  private byte[] _tempBufPlay;
	  private byte[] _tempBufRec;
	  private int _bufferedRecSamples = 0;
	  private int _bufferedPlaySamples = 0;
	  private int _playPosition = 0;
	  private AudioRecord audioRecord = null;
	  private AudioTrack audioPlay = null;
	  private boolean isWorking = false;
	  private AuidoCallback auidoCallback;
	  private int pcmInputSize;
	  private int pcmOutSize;
	  private static final int AUDIOSAMPLERATE = 8000;
	  private Thread recvThread;
	  private Thread sendThread;
	  private boolean isPlayAudio;
	  private boolean isRecordAudio;
	  private static Context context;
	  
	  public static AudioIOHandler getInstance(Context ctx)
	  {
	    context = ctx.getApplicationContext();
	    if (instance == null) {
	      instance = new AudioIOHandler();
	    }
	    return instance;
	  }
	  
	  private AudioIOHandler()
	  {
	    this._playBuffer = ByteBuffer.allocateDirect(2048);
	    this._recBuffer = ByteBuffer.allocateDirect(320);
	    
	    this._tempBufPlay = new byte[2048];
	    this._tempBufRec = new byte[320];
	    
	    init();
	  }
	  
	  public void startAudio(boolean isPlay, boolean isRecord)
	  {
	    creatAudioRecord();
	    createTracker();
	    initAudioVolume();
	    
	    this.isPlayAudio = isPlay;
	    this.isRecordAudio = isRecord;
	    try
	    {
	      this.isWorking = true;
	      start(true, true);
	      if (this.isPlayAudio)
	      {
	        this.audioPlay.play();
	        startPlay();
	      }
	    }
	    catch (Exception localException) {}
	    initThread();
	  }
	  
	  public void resumeAudioPlay()
	  {
	    RvsLog.i(AudioIOHandler.class, "resumeAudioPlay()", "resumeAudioPlay");
	    this._bufferedPlaySamples = 0;
	    try
	    {
	      this.audioPlay.play();
	    }
	    catch (Exception localException) {}
	    this.isPlayAudio = true;
	    startPlay();
	  }
	  
	  public void pauseAudioPlay()
	  {
	    RvsLog.i(AudioIOHandler.class, "pauseAudioPlay()", "pauseAudioPlay");
	    this.isPlayAudio = false;
	    stopPlay();
	    try
	    {
	      this.audioPlay.pause();
	      this.audioPlay.flush();
	    }
	    catch (Exception localException) {}
	  }
	  
	  public void resumeAudioRecord()
	  {
	    RvsLog.i(AudioIOHandler.class, "resumeAudioRecord()", "resumeAudioRecord");
	    try
	    {
	      if (this.audioRecord != null)
	      {
	        this.audioRecord.startRecording();
	        this.isRecordAudio = true;
	      }
	      else
	      {
	        RvsLog.e(AudioIOHandler.class, "resumeAudioRecord()", "audioRecord is null");
	      }
	    }
	    catch (Exception localException) {}
	    startRecord();
	  }
	  
	  public void pauseAudioRecord()
	  {
	    RvsLog.i(AudioIOHandler.class, "pauseAudioRecord()", "pauseAudioRecord");
	    this.isRecordAudio = false;
	    stopRecord();
	    try
	    {
	      if (this.audioRecord != null) {
	        this.audioRecord.stop();
	      }
	    }
	    catch (Exception localException) {}
	  }
	  
	  public void releaseAudio()
	  {
	    RvsLog.i(AudioIOHandler.class, "releaseAudio()", "releaseAudio begin");
	    this.isWorking = false;
	    this.isPlayAudio = false;
	    this.isRecordAudio = false;
	    stop();
	    if (this.recvThread != null)
	    {
	      this.recvThread.interrupt();
	      try
	      {
	        this.recvThread.join();
	      }
	      catch (InterruptedException localInterruptedException) {}
	      this.recvThread = null;
	    }
	    if (this.sendThread != null)
	    {
	      this.sendThread.interrupt();
	      try
	      {
	        this.sendThread.join();
	      }
	      catch (InterruptedException localInterruptedException1) {}
	      this.sendThread = null;
	    }
	    if (this.audioPlay != null)
	    {
	      this.audioPlay.release();
	      this.audioPlay = null;
	    }
	    if (this.audioRecord != null)
	    {
	      this.audioRecord.release();
	      this.audioRecord = null;
	    }
	    RvsLog.i(AudioIOHandler.class, "releaseAudio()", "releaseAudio end");
	  }
	  
	  private void creatAudioRecord()
	  {
	    int minBufSize = AudioRecord.getMinBufferSize(8000, 16, 2);
	    if ((minBufSize != -2) && (minBufSize != -1)) {
	      try
	      {
	        this.audioRecord = new AudioRecord(1, 8000, 16, 2, minBufSize);
	      }
	      catch (Exception e)
	      {
	        throw new RuntimeException("init audio record fail:" + e.getMessage());
	      }
	    } else {
	      throw new RuntimeException("AudioRecord getMinBufferSize fail");
	    }
	    if (this.audioRecord.getState() != 1)
	    {
	      this.audioRecord.release();
	      this.audioRecord = null;
	      throw new RuntimeException("AudioRecord STATE_UNINITIALIZED");
	    }
	  }
	  
	  private void createTracker()
	  {
	    int minBufSize = AudioTrack.getMinBufferSize(8000, 4, 2);
	    if ((minBufSize != -2) && (minBufSize != -1)) {
	      try
	      {
	        this.audioPlay = new AudioTrack(3, 8000, 4, 2, minBufSize, 1);
	      }
	      catch (Exception e)
	      {
	        throw new RuntimeException("init audio play fail:" + e.getMessage());
	      }
	    } else {
	      throw new RuntimeException("AudioTrack getMinBufferSize fail");
	    }
	    if (this.audioPlay.getState() != 1)
	    {
	      this.audioPlay.release();
	      this.audioPlay = null;
	      throw new RuntimeException("AudioTrack STATE_UNINITIALIZED");
	    }
	  }
	  
	  private void initAudioVolume()
	  {
	    AudioManager mAudioManager = (AudioManager)context.getSystemService("audio");
	    int max = mAudioManager.getStreamMaxVolume(3);
	    int index = (int)(0.8D * max);
	    int cur = mAudioManager.getStreamVolume(3);
	    if (cur > index) {
	      mAudioManager.setStreamVolume(3, index, 0);
	    }
	  }
	  
	  private int playAudio(int lengthInBytes)
	  {
	    int bufferedSamples = 0;
	    try
	    {
	      if (!this.isPlayAudio) {
	        return -2;
	      }
	      int written = 0;
	      this._playBuffer.get(this._tempBufPlay);
	      written = this.audioPlay.write(this._tempBufPlay, 0, lengthInBytes);
	      this._playBuffer.rewind();
	      
	      this._bufferedPlaySamples += (written >> 1);
	      

	      int pos = this.audioPlay.getPlaybackHeadPosition();
	      if (pos < this._playPosition) {
	        this._playPosition = 0;
	      }
	      this._bufferedPlaySamples -= pos - this._playPosition;
	      this._playPosition = pos;
	      if (!this.isRecordAudio) {
	        bufferedSamples = this._bufferedPlaySamples;
	      }
	      if (written != lengthInBytes) {
	        return -1;
	      }
	    }
	    catch (Exception e)
	    {
	      RvsLog.e(AudioIOHandler.class, "playAudio()", "playAudio err:" + e.getMessage());
	    }
	    return bufferedSamples;
	  }
	  
	  private int recordAudio(int lengthInBytes)
	  {
	    try
	    {
	      if (!this.isRecordAudio) {
	        return -2;
	      }
	      int readBytes = 0;
	      this._recBuffer.rewind();
	      readBytes = this.audioRecord.read(this._tempBufRec, 0, lengthInBytes);
	      this._recBuffer.put(this._tempBufRec);
	      if (readBytes != lengthInBytes) {
	        return -1;
	      }
	    }
	    catch (Exception e)
	    {
	      RvsLog.e(AudioIOHandler.class, "recordAudio()", "recordAudio err:" + e.getMessage());
	    }
	    return this._bufferedPlaySamples;
	  }
	  
	  private void initThread()
	  {
	    this.recvThread = new Thread(new Runnable()
	    {
	      public void run()
	      {
	        int readSize = 0;
	        short[] audiodata = new short[AudioIOHandler.this.pcmInputSize];
	        try
	        {
	          while (AudioIOHandler.this.isWorking) {
	            if (AudioIOHandler.this.isPlayAudio)
	            {
	              if (AudioIOHandler.this.auidoCallback != null)
	              {
	                readSize = AudioIOHandler.this.auidoCallback.onPcmInput(audiodata, AudioIOHandler.this.pcmInputSize);
	                if (readSize > 0) {
	                  AudioIOHandler.this.putPlayData2(audiodata, readSize);
	                } else {
	                  Thread.sleep(10L);
	                }
	              }
	            }
	            else {
	              Thread.sleep(10L);
	            }
	          }
	        }
	        catch (Exception localException) {}finally
	        {
	          RvsLog.i(AudioIOHandler.class, "initThread()", "recvThread exit");
	        }
	      }
	    });
	    this.recvThread.start();
	    
	    this.sendThread = new Thread(new Runnable()
	    {
	      public void run()
	      {
	        try
	        {
	          short[] pcmData = new short[AudioIOHandler.this.pcmOutSize];
	          while (AudioIOHandler.this.isWorking) {
	            if (AudioIOHandler.this.isRecordAudio)
	            {
	              int size = AudioIOHandler.this.getAecData2(pcmData, AudioIOHandler.this.pcmOutSize);
	              if (size == AudioIOHandler.this.pcmOutSize) {
	                if (AudioIOHandler.this.auidoCallback != null) {
	                  AudioIOHandler.this.auidoCallback.onPcmOutput(pcmData, AudioIOHandler.this.pcmOutSize);
	                }
	              }
	            }
	            else
	            {
	              Thread.sleep(10L);
	            }
	          }
	        }
	        catch (Exception localException) {}finally
	        {
	          RvsLog.i(AudioIOHandler.class, "initThread()", "sendThread exit");
	        }
	      }
	    });
	    this.sendThread.start();
	  }
	  
	  public void setAuidoCallback(AuidoCallback callback, int pcmInputSize, int pcmOutSize)
	  {
	    this.auidoCallback = callback;
	    this.pcmInputSize = pcmInputSize;
	    this.pcmOutSize = pcmOutSize;
	  }
	  
	  private native int init();
	  
	  private native int destroy();
	  
	  private native int putPlayData(byte[] paramArrayOfByte, int paramInt);
	  
	  private native int putPlayData2(short[] paramArrayOfShort, int paramInt);
	  
	  private native int getAecData(byte[] paramArrayOfByte, int paramInt);
	  
	  private native int getAecData2(short[] paramArrayOfShort, int paramInt);
	  
	  private native int start(boolean paramBoolean1, boolean paramBoolean2);
	  
	  private native int stop();
	  
	  private native int startPlay();
	  
	  private native int stopPlay();
	  
	  private native int startRecord();
	  
	  private native int stopRecord();
	  
	  public static abstract interface AuidoCallback
	  {
	    public abstract int onPcmInput(short[] paramArrayOfShort, int paramInt);
	    
	    public abstract void onPcmOutput(short[] paramArrayOfShort, int paramInt);
	  }
	}
