package android.HH100;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.HH100.IDspectrumActivity.Check;
import android.HH100.LogActivity.LogPhotoTab.Media;
import android.HH100.Structure.NcLibrary;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class RecActivity extends Activity implements View.OnClickListener, OnCompletionListener {

	// �̸� ��� ����
	private static final int REC_STOP = 0;
	private static final int RECORDING = 1;
	private static final int PLAY_STOP = 0;
	private static final int PLAYING = 1;
	private static final int PLAY_PAUSE = 2;

	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	private int mRecState = REC_STOP;
	private int mPlayerState = PLAY_STOP;
	private SeekBar mRecProgressBar, mPlayProgressBar;
	private Button mBtnStartRec, mBtnStartPlay, mBtnStopPlay;
	private String mFilePath, mFileName = null;
	private ArrayList<String> mFilePathArr = new ArrayList<String>();
	private TextView mTvPlayMaxPoint, mTvRecStartPoint, mTvRecMaxPoint, mTvPlayStartPoint;
	Toast mToast;
	Button mRemoveTxt;

	private int mCurRecTimeMs = 0;
	private int mCurProgressTimeDisplay = 0;

	ArrayList<String> recListArray = new ArrayList<String>();
	ArrayList<String> recListArray1 = new ArrayList<String>();

	int mRecSecond = 0, mRecMinute = 0, mRecSecondCnt = 0, mRecSecondMax = 6000, mRecTempSecond = 0;
	int mPlaySecond = 0, mPlayMinute = 0, mPlaySecondCnt = 0, mPlaySecondMax = 6000, mPlayTempSecond = 0;
	double mRecPercent = 0;

	Handler mProgressHandler = new Handler() {
		public void handleMessage(Message msg) {
			mCurRecTimeMs++;
			// mCurProgressTimeDisplay = mCurProgressTimeDisplay + 100;

			if (mCurRecTimeMs < 0) {
			}

			else if (mCurRecTimeMs < mRecSecondMax) {

				// mSecondMax=0;

				mRecTempSecond++;

				mRecSecondCnt++;

				if (mRecTempSecond == mRecSecondMax) {

					mRecSecond = 0;
					mRecMinute = 0;
				}

				if (mRecSecondCnt == 10) {

					mRecSecond++;
					mRecSecondCnt = 0;
				}

				if (mRecSecond == 60) {
					mRecSecond = 0;
					mRecMinute += 1;

				}
				String mMinuteStr, mSecondStr;

				mSecondStr = Integer.toString(mRecSecond);
				mMinuteStr = Integer.toString(mRecMinute);

				mTvRecStartPoint.setText(mMinuteStr + ":" + mSecondStr);

				mRecProgressBar.setProgress(mRecTempSecond);
				mProgressHandler.sendEmptyMessageDelayed(0, 100);
			}

			else {
				mBtnStartRecOnClick();
			}
		}
	};

	Handler mProgressHandler2 = new Handler()

	{
		public void handleMessage(Message msg)

		{
			if (mPlayer == null)
				return;

			try {
				if (mPlayer.isPlaying()) {

					mPlayTempSecond++;

					mPlaySecondCnt++;

					if (mPlayTempSecond == 3000) {

						mPlaySecond = 0;
						mPlayMinute = 0;
					}

					if (mPlaySecondCnt == 10) {

						mPlaySecond++;
						mPlaySecondCnt = 0;

					}

					if (mPlaySecond == 60) {
						mPlaySecond = 0;
						mPlayMinute += 1;

					}
					String mMinuteStr, mSecondStr;

					mSecondStr = Integer.toString(mPlaySecond);
					mMinuteStr = Integer.toString(mPlayMinute);

					mTvPlayMaxPoint.setText(mMinuteStr + ":" + mSecondStr);

					mPlayProgressBar.setProgress(mPlayer.getCurrentPosition());

					mProgressHandler2.sendEmptyMessageDelayed(0, 100);
				}
			} catch (IllegalStateException e) {
				NcLibrary.Write_ExceptionLog(e);
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.rec);

		SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		mBtnStartRec = (Button) findViewById(R.id.btnStartRec);
		mBtnStartPlay = (Button) findViewById(R.id.btnStartPlay);
		mBtnStopPlay = (Button) findViewById(R.id.btnStopPlay);
		mRecProgressBar = (SeekBar) findViewById(R.id.recProgressBar);

		mRecProgressBar.setMax(mRecSecondMax);

		mPlayProgressBar = (SeekBar) findViewById(R.id.playProgressBar);
		mTvPlayMaxPoint = (TextView) findViewById(R.id.tvPlayMaxPoint);

		mTvPlayStartPoint = (TextView) findViewById(R.id.tvPlayStartPoint);

		mTvRecStartPoint = (TextView) findViewById(R.id.tvRecStartPoint);
		mTvRecMaxPoint = (TextView) findViewById(R.id.tvRecMaxPoint);

		mRemoveTxt = (Button) findViewById(R.id.Rec_Remove_Txt);

		mBtnStartRec.requestFocus();

		mRemoveTxt.setOnClickListener(this);
		mBtnStartRec.setOnClickListener(this);
		mBtnStartPlay.setOnClickListener(this);
		mBtnStopPlay.setOnClickListener(this);

	}

	// ��ư�� OnClick �̺�Ʈ ������
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStartRec:
			if (mPlayerState == PLAY_STOP) {
				mBtnStartRecOnClick();
			}
			break;
		case R.id.btnStartPlay:
			if (mRecState == REC_STOP) {
				mBtnStartPlayOnClick();
			}
			break;
		case R.id.btnStopPlay:
			mBtnStopPlayOnClick();
			break;

		case R.id.Rec_Remove_Txt:

			// File f2d = new File(mFilePath);
			// f2d.delete();

			for (int i = 0; i < mFilePathArr.size(); i++) {
				NcLibrary.deleteFile(mFilePathArr.get(i));
			}
			//finish();
			notIntent();
			break;
		default:
			break;
		}
	}

	private void mBtnStartRecOnClick() {
		if (mRecState == REC_STOP) {
			mRecSecond = 0;
			mRecMinute = 0;
			String mSecondStr = Integer.toString(mRecSecond);
			String mMinuteStr = Integer.toString(mRecMinute);

			mTvRecStartPoint.setText(mMinuteStr + ":" + mSecondStr);

			mRecState = RECORDING;

			try {
				if (mToast != null) {
					mToast.cancel();
				}
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			mToast = Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT);
			mToast.setText("Start Recording");
			mToast.show();

			startRec();
			updateUI();

			// Toast.makeText(getApplicationContext(), "Start Recdoing",
			// 1).show();
		} else if (mRecState == RECORDING) {
			try {
				if (mToast != null) {
					mToast.cancel();
				}
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			mToast = Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT);
			mToast.setText("Recording Stopped"); //190114 Complete Record -> Recording Stopped
			mToast.show();

			mRecState = REC_STOP;
			stopRec();
			updateUI();
			// Toast.makeText(getApplicationContext(), "Complete Record",
			// 1).show();

		}
	}

	// ��������
	private void startRec() {

		String mEventNumber;

		Intent intent = getIntent();

		int mListNumber = intent.getIntExtra(Check.ListNumber, 0);

		for (int i = 1; i < 1000; i++) {

			mEventNumber = Integer.toString(i);

			String value = "EventR" + mListNumber + "_" + mEventNumber;
			mFilePath = Media.FolderPath + "/EventR" + mListNumber + "_" + mEventNumber + ".amr";
			File file;
			file = new File(mFilePath);
			if (!file.exists()) {
				mFilePathArr.add(mFilePath);
				recListArray.add(value);

				break;
			}

		}
		mCurRecTimeMs = 0;
		mCurProgressTimeDisplay = 0;

		// SeekBar�� ���¸� 0.1���� üũ ����
		mProgressHandler.sendEmptyMessageDelayed(0, 100);

		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.reset();
		} else {
			mRecorder.reset();
		}

		try {

			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mRecorder.setOutputFile(mFilePath);
			mRecorder.prepare();
			mRecorder.start();
		} catch (IllegalStateException e) {
			NcLibrary.Write_ExceptionLog(e);
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	// ��������
	private void stopRec() {
		try {
			mRecorder.stop();
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		} finally {
			mRecorder.release();
			mRecorder = null;
		}

		mCurRecTimeMs = -999;
		// SeekBar�� ���¸� ��� üũ
		mProgressHandler.sendEmptyMessageDelayed(0, 0);
	}

	private void mBtnStartPlayOnClick() {
		if (mPlayerState == PLAY_STOP) {

			try {
				if (mToast != null) {
					mToast.cancel();
				}
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			mToast = Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT);
			mToast.setText("PLAYING");
			mToast.show();
			mPlayerState = PLAYING;
			initMediaPlayer();
			startPlay();
			updateUI();
		} else if (mPlayerState == PLAYING) {

			try {
				if (mToast != null) {
					mToast.cancel();
				}
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			mToast = Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT);
			mToast.setText("PLAY_PAUSE");
			mToast.show();

			mPlayerState = PLAY_PAUSE;
			stopPlay();
			updateUI();
		} else if (mPlayerState == PLAY_PAUSE) {

			try {
				if (mToast != null) {
					mToast.cancel();
				}
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			mToast = Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT);
			mToast.setText("RESET");
			mToast.show();
			mPlayerState = PLAYING;
			startPlay();
			updateUI();
		}
	}

	private void mBtnStopPlayOnClick() {
		if (mPlayerState == PLAYING || mPlayerState == PLAY_PAUSE) {
			mPlayerState = PLAY_STOP;
			stopPlay();
			releaseMediaPlayer();
			updateUI();
		}
	}

	private void initMediaPlayer() {
		// �̵�� �÷��̾� ����
		if (mPlayer == null)
			mPlayer = new MediaPlayer();
		else
			mPlayer.reset();

		mPlayer.setOnCompletionListener(this);
		String fullFilePath = mFilePath;

		try {
			mPlayer.setDataSource(fullFilePath);
			mPlayer.prepare();
			int point = mPlayer.getDuration();
			mPlayProgressBar.setMax(point);

			int maxMinPoint = point / 1000 / 60;
			int maxSecPoint = (point / 1000) % 60;
			String maxMinPointStr = "";
			String maxSecPointStr = "";

			if (maxMinPoint < 10)
				maxMinPointStr = "0" + maxMinPoint + ":";
			else
				maxMinPointStr = maxMinPoint + ":";

			if (maxSecPoint < 10)
				maxSecPointStr = "0" + maxSecPoint;
			else
				maxSecPointStr = String.valueOf(maxSecPoint);

			mTvPlayStartPoint.setText(maxMinPointStr + maxSecPointStr);

			mPlayProgressBar.setProgress(0);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	// ��� ����
	private void startPlay()

	{
		Log.v("ProgressRecorder", "startPlay().....");

		try {
			mPlayer.start();

			mProgressHandler2.sendEmptyMessageDelayed(0, 100);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void pausePlay() {
		Log.v("ProgressRecorder", "pausePlay().....");

		mPlayer.pause();

		mProgressHandler2.sendEmptyMessageDelayed(0, 0);
	}

	private void stopPlay() {
		Log.v("ProgressRecorder", "stopPlay().....");

		mPlayer.stop();

		mProgressHandler2.sendEmptyMessageDelayed(0, 0);
	}

	private void releaseMediaPlayer() {

		mPlayer.release();
		mPlayer = null;
		mPlayProgressBar.setProgress(0);
	}

	public void onCompletion(MediaPlayer mp) {
		mPlayerState = PLAY_STOP;

		mProgressHandler2.sendEmptyMessageDelayed(0, 0);

		updateUI();
	}

	private void updateUI() {
		if (mRecState == REC_STOP) {
			mBtnStartRec.setText("Rec");
			mRecProgressBar.setProgress(0);
			mRecSecond = 0;
			mRecMinute = 0;
			mRecTempSecond = 0;
		} else if (mRecState == RECORDING)
			mBtnStartRec.setText("Stop");

		if (mPlayerState == PLAY_STOP) {
			mBtnStartPlay.setText("Play");
			mPlayProgressBar.setProgress(0);
			mPlaySecond = 0;
			mPlayMinute = 0;
			mPlayTempSecond = 0;
		} else if (mPlayerState == PLAYING)
			mBtnStartPlay.setText("Pause");
		else if (mPlayerState == PLAY_PAUSE)
			mBtnStartPlay.setText("Start");
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {

			putIntent1();

			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void putIntent1() {

		Intent intent = new Intent();
		intent.putExtra(Check.ListValue, recListArray);
		setResult(Check.Result_Ok, intent);
		finish();

	}

	public void notIntent() {

		Intent intent = new Intent();
		intent.putExtra(Check.ListValue, recListArray1);
		setResult(Check.Result_Ok, intent);
		//setResult(Check.Result_Not_Ok); 190225 수정
		finish();

	}

}
