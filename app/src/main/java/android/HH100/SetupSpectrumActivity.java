package android.HH100;

import android.HH100.Control.BatteryView;
import android.HH100.Control.ProgressBar;
import android.HH100.Control.SpectrumView;
import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.FindPeak;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import Debug.Cs137_1;
import NcLibrary.Coefficients;

public class SetupSpectrumActivity extends Activity implements View.OnTouchListener {
	private final boolean D = MainActivity.D;
	private final String TAG = "SetupSpectrum";

	// public static final int MESSAGE_STATE_CHANGE = 1;
	// public static final int MESSAGE_READ_GAMMA = 21;
	// public static final int VIEW_BG_CALI = 1;
	// public static final int VIEW_ID = 2;

	public static final String CALIB_ENDCNT = "calib.endcnt";
	public static final String BG_GOALTIME = "bg.goaltime";

	private final int GOAL_COUNT = 200000;

	private final int MSG_MEASURE_BG = 151235;
	private final int MSG_MEASURE_CALIB = 151237;

	// -----------------------------------------------
	private SpectrumView mSpectrumView;
	// private Handler mHandler;

	private int LANDSCAPE = 2;
	private int PORTRAIT = 1;

	private Spectrum mSPC = new Spectrum();

	private int mBG_GoalTime = 0;
	private boolean mIsBackGrounding = false;
	public static boolean mIsCaling = false;
	private int mCalib_EndCnt = GOAL_COUNT;

	private PreferenceDB mPrefDB = null;
	private ProgressBar mProgBar = null;

	SpecturmSurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	private Camera camera;
	TextView firstword, lastword, acqtimeTxt, cpsTxt, totalCountTxt, bottombarTxt, setup_ModeTxt1, setup_ModeTxt2;

	TimerTask mTask;

	TextView Paired, Library, Alarm, Battery;

	int count = 0, count1 = 0;
	private BatteryView mBatteryProgBar = null;
	public static String AcqTimeStr = null, AcqCountStr = null;

	private final int MSG_CALIBRATION = 301248;
	private final int MSG_CALIBRATION_RESULT = 301249;

	IntentFilter filter;

	// 액티비티 모드 정의

	public static final String MEASUREMENT_MODE = "activity_mode";
	public static final String MEASUREMENT_BACKGROUND = "background_mode";
	public static final String MEASUREMENT_EN_CALIBRATION = "en_calibration_mode";


	private class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			// Action �젙蹂대�� 媛�吏�怨� �삩�떎.
			String action = intent.getAction();

			switch (action) {

				case MSG_RECV_SPECTRUM:
					Spectrum recv_data = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);
					L("Read Spectrum");
					// ------------------------------------------------------------------------

					//190102 추가
/*
					if(U2AATimer !=null)
					{
						U2AATimer.cancel();
						U2AATimer = null;
					}
*/

					mSPC.Accumulate_Spectrum(recv_data);

					Set_SpcInfo_date();
					Set_SpcInfo_cps(recv_data.Get_TotalCount());

					mSpectrumView.SetChArray(mSPC);
					mSpectrumView.invalidate();

					if (mIsBackGrounding) {
						mHandler.obtainMessage(MSG_MEASURE_BG, 0, 0, recv_data).sendToTarget();
					} else if (mIsCaling) {
						if (MainActivity.mDebug.IsDebugMode) {
							if (MainActivity.mDebug.IsCalibrationMode) {
								MainActivity.mDebug.setSpecInfo(new Cs137_1());
								mHandler.obtainMessage(MSG_MEASURE_CALIB, 0, 0, recv_data).sendToTarget();
							}
							else
							{
								mHandler.obtainMessage(MSG_MEASURE_CALIB, 0, 0, recv_data).sendToTarget();
							}

						} else {
							mHandler.obtainMessage(MSG_MEASURE_CALIB, 0, 0, recv_data).sendToTarget();
						}
					}

					break;

				case MSG_DISCONNECTED_BLUETOOTH:

					mSPC.ClearSPC();
					mSpectrumView.SetChArray(mSPC);
					mSpectrumView.invalidate();
					break;

				case MSG_START_BACKGROUND:
					onCreatPart();
					break;

				case MAIN_DATA_SEND1:
					Battery = (TextView) findViewById(R.id.BatteryTxt);

					String mBatteryStr = intent.getStringExtra(MainBCRReceiver.DATA_BATTERY);

					Battery.setText(mBatteryStr + " %");
					mBatteryProgBar.Set_Value((double) Integer.parseInt(mBatteryStr));
					mBatteryProgBar.invalidate();

					break;
				case MSG_START_CALIBRATION:
					onCreatPart();

					break;
				case MSG_BACKGROUND_CANCEL:
					mProgBar.Set_Value(0);
					mProgBar.invalidate();

					mIsBackGrounding = false;
					mIsCaling = false;
					mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
					mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
					mSpectrumView.invalidate();
					Set_SpcInfo_AcqTime(0);

					Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancel), Toast.LENGTH_LONG)
							.show();

					mSPC.ClearSPC();
					Intent send_gs1 = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);

					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);

					MainActivity.tabHost.setCurrentTab(0);
					break;

				case MSG_CALIBRATION_CANCEL:

					mIsBackGrounding = false;
					mIsCaling = false;

					break;



				default:
					break;
			}
		}
	}

	private MainBCRReceiver mMainBCR = new MainBCRReceiver();

	// -----------------------
	public SetupSpectrumActivity() {

	}

	protected boolean inProgress;
	LayoutInflater inflater;
	LinearLayout linearLayout, linearLayout1;

	Button startBtn;

	Context mContext;

	FrameLayout mFrameLayout;

	//190102
	TimerTask U2AATimer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		super.onCreate(savedInstanceState);

		// Regist brc receiver
		mContext = this;

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

		layoutParams.screenBrightness = (float) 1.0;

		getWindow().setAttributes(layoutParams);

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// StartBtn
		IntentFilter filter1 = new IntentFilter();

		filter1.addAction(MainBroadcastReceiver.MSG_TAB_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_TAB_EN_CALIBRATION);
		filter1.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);
		filter1.addAction(MainBroadcastReceiver.MSG_CALIBRATION_CANCEL);

		filter1.addAction(MainBroadcastReceiver.MSG_START_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_START_CALIBRATION);

		filter1.addAction(MainBroadcastReceiver.MAIN_DATA_SEND1);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter1);

		String activity = null;
		activity = getIntent().getStringExtra(MEASUREMENT_MODE);

		onCreatPart();

	}

	private void L(String log) {
		if (D)
			Log.i(TAG, log);
	}

	/////// --------------------------------------------
	public String Prefix_CPS(int CPS) {
		DecimalFormat format = new DecimalFormat();
		String Result = null;

		char Pref = 0;
		int ConversionFactor = 1;
		if (CPS > 100000) {
			ConversionFactor = 1000;
			Pref = 'K';
		} // count媛� 留롮븘吏� 寃쎌슦 SI Prefix�궗�슜
		if (CPS > 100000000) {
			ConversionFactor = 1000000;
			Pref = 'M';
		}

		if (ConversionFactor == 1)
			Result = String.valueOf(CPS);
		else
			format.applyLocalizedPattern("0.#");
		Result = format.format(CPS / (double) ConversionFactor) + Pref;

		return Result;
	}

	@Override
	public void onBackPressed() {

		mProgBar.Set_Value(0);
		mProgBar.invalidate();
		mIsCaling = false;
		mIsBackGrounding = false;
		mIsCaling = false;
		mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
		mSpectrumView.invalidate();
		Set_SpcInfo_AcqTime(0);

		//190102 추가
		//MainActivity.SendU4AA();


		/*
		 * Toast.makeText(getApplicationContext(),
		 * getResources().getString(R.string.cancel), Toast.LENGTH_LONG) .show();
		 */

		mSPC.ClearSPC();

		finish();
		return;
	}

	private void Set_Spectrum_X_toEnergy() {

		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients()[0],
				mSPC.Get_Coefficients().get_Coefficients()[1], mSPC.Get_Coefficients().get_Coefficients()[2]);
	}

	@Override

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.spectrum_menu, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		/*
		 * if (MainActivity.mLogin == LoginDlg.LOGIN_USER) {
		 *
		 * if (menu.size() > 1) menu.removeItem(menu.getItem(1).getItemId()); }
		 */

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.Cancel_Menu:

				mProgBar.Set_Value(0);
				mProgBar.invalidate();
				mIsCaling = false;
				mIsBackGrounding = false;
				mIsCaling = false;
				mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
				mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
				mSpectrumView.invalidate();
				Set_SpcInfo_AcqTime(0);

				Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancel), Toast.LENGTH_LONG)
						.show();

				mSPC.ClearSPC();

				//190102 추가
				//MainActivity.SendU4AA();

				finish();

				break;
		}

		return true;
	}

	@Override
	protected void onDestroy() {

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
		L("--- ON DESTROY ---");
		//190102 추가
		//NcLibrary.U2AATimer();

		//20.02.18추가
		if(MainActivity.mDebug.hw){
			//main -> menu 에서 온건지 확인
			if(MainActivity.menuCalib)
			{
				MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
				MainActivity.menuCalib = false;
			}
		}


		super.onDestroy();
	}

	private void Write_Calibration_Result(double A, double B, double C, double PeakCh1, double PeakCh2,

										  double PeakCh3) {
		mPrefDB.Set_Calibration_Result(A, B, C, PeakCh1, PeakCh2, PeakCh3);
	}

	private Spectrum Background_GainStabilization(double Before_K40, double Now_K40) {

		Spectrum result = null;

		if (Before_K40 == 0)
			return result;
		try {
			if (Before_K40 == Now_K40)
				return result;
			int[] BG = new int[MainActivity.CHANNEL_ARRAY_SIZE];
			int[] NewBG = new int[MainActivity.CHANNEL_ARRAY_SIZE];
			BG = mPrefDB.Get_BG_From_pref();

			// background adjustment
			int tempindex = 0;
			float diffgap = 0;

			float temp = 0;
			if (Now_K40 == 0)
				diffgap = 1;
			else
				diffgap = (float) Now_K40 / (float) Before_K40;

			for (int i = 0; i < MainActivity.CHANNEL_ARRAY_SIZE; i++) // 梨꾨꼸
			// �씠�룞
			{
				tempindex = NcLibrary.Auto_floor(((float) i * diffgap));
				if (tempindex >= MainActivity.CHANNEL_ARRAY_SIZE)
					break;
				NewBG[tempindex] = BG[i];
			}

			for (int i = 0; i < MainActivity.CHANNEL_ARRAY_SIZE - 1; i++) // �씠鍮좎쭊怨�
			// 蹂댁젙
			{
				temp = NewBG[i];
				if (temp <= 0 && (i > 0 && i < MainActivity.CHANNEL_ARRAY_SIZE - 1)) {
					if (NewBG[i - 1] > 0 & NewBG[i + 1] > 0) {
						NewBG[i] = (NewBG[i - 1] + NewBG[i + 1]) / 2;
					}
				}
			}
			result = new Spectrum();
			result.Set_Spectrum(BG, mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			result.Save_DateNow();
			result.Set_Coefficients(mSPC.Get_Coefficients());
			// mPrefDB.Set_BG_On_pref(NewBG,MainActivity.CHANNEL_ARRAY_SIZE);
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return result;
		}
	}

	private void WriteOnDB_Background_Data(Spectrum SPC) {

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String bg_date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + "T" + date.getHours() + ":" + date.getMinutes() + ":"
				+ date.getSeconds() + "." + MSec + NcLibrary.Get_GMT();
		////

		mPrefDB.Set_BG_On_pref(SPC.ToInteger(), SPC.Get_Ch_Size());
		mPrefDB.Set_BG_Date_From_pref(bg_date);
		mPrefDB.Set_BG_MeasuredAcqTime_From_pref((int)SPC.Get_AcqTime());
	}

	private void Set_SpcInfo_cps(int CPS) {

		// mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
		// String.valueOf(CPS));

		cpsTxt.setText(NcLibrary.Cut_Decimal_Point(CPS));

		totalCountTxt.setText(NcLibrary.Cut_Decimal_Point(mSPC.Get_TotalCount()));
	}

	private void Set_SpcInfo_AcqTime(int AcqTime) {
		acqtimeTxt.setText(String.valueOf(AcqTime) + " sec");
		// mSpectrumView.Set_inform3(getResources().getString(R.string.acq_time),
		// String.valueOf(AcqTime) + " sec");
	}

	private void Set_SpcInfo_date() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		/*
		 * mSpectrumView.Set_inform(getResources().getString(R.string.date),
		 * calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) +
		 * 1) + "." + calendar.get(Calendar.YEAR));
		 * mSpectrumView.Set_inform2(getResources().getString(R.string.time),
		 * date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
		 */
	}

	private void Set_SpcInfo(int AcqTime, int CPS) {
		/*
		 * Calendar calendar = Calendar.getInstance(); Date date = calendar.getTime();
		 * mSpectrumView.Set_inform(getResources().getString(R.string.date),
		 * calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) +
		 * 1) + "." + calendar.get(Calendar.YEAR));
		 * mSpectrumView.Set_inform2(getResources().getString(R.string.time),
		 * date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
		 * mSpectrumView.Set_inform3(getResources().getString(R.string.acq_time) ,
		 * String.valueOf(AcqTime) + " sec");
		 * mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
		 * String.valueOf(CPS));
		 */

	}

	private SurfaceHolder.Callback sufaceListener = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			camera.release();
			camera = null;
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			// TODO Auto-generated method stub
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			try {
				camera.setPreviewDisplay(holder);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			Camera.Parameters param = camera.getParameters();
			param.setPreviewSize(width, height);
			camera.startPreview();

		}
	};

	private android.hardware.Camera.PictureCallback takePicture = new android.hardware.Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			camera.startPreview();
			inProgress = false;
		}
	};

	public void onCreatPart() {

		////////// �젅�씠�븘�썐 �벑濡�

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout m_MainLayout = (LinearLayout) inflater.inflate(R.layout.spectrum, null);

		/////////
		mSPC.ClearSPC();

		mPrefDB = new PreferenceDB(this);
		mBG_GoalTime = mPrefDB.Get_BG_AcqTime_SetValue_From_pref();
		mSPC.Set_Coefficients(mPrefDB.Get_Cali_ABC_From_pref());
		//
		mProgBar = (ProgressBar) m_MainLayout.findViewById(R.id.SetupSpcSrc_ProgressBar);
		//
		mSpectrumView = (SpectrumView) m_MainLayout.findViewById(R.id.Spectrum);

		acqtimeTxt = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt);

		cpsTxt = (TextView) m_MainLayout.findViewById(R.id.cpsTxt);

		totalCountTxt = (TextView) m_MainLayout.findViewById(R.id.totalCountTxt);

		// bottombarTxt = (TextView)
		// m_MainLayout.findViewById(R.id.bottombarTxt);

		setup_ModeTxt1 = (TextView) m_MainLayout.findViewById(R.id.setup_ModeTxt1);
		setup_ModeTxt2 = (TextView) m_MainLayout.findViewById(R.id.setup_ModeTxt2);

		Paired = (TextView) m_MainLayout.findViewById(R.id.Paired);
		Library = (TextView) m_MainLayout.findViewById(R.id.Library);
		Alarm = (TextView) m_MainLayout.findViewById(R.id.Alarm);

		mBatteryProgBar = (BatteryView) m_MainLayout.findViewById(R.id.betterView_ProgressBar);

		mBatteryProgBar.Set_Value((double) MainActivity.mBattary);
		mBatteryProgBar.invalidate();

		Battery = (TextView) m_MainLayout.findViewById(R.id.BatteryTxt);

		// Y.KIM 20180423 modify deveice name (HH or BP)
		Paired.setText(getResources().getString(R.string.HardwareName));

		Library.setText(MainActivity.LibraryStr);

		Alarm.setText(MainActivity.AlarmStr);

		Battery.setText(Integer.toString(MainActivity.mBattary) + " %");

		//190102
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
/*
		MainActivity.SendU2AA();
		U2AATimer = new TimerTask()
		{
			@Override
			public void run() {
				MainActivity.SendU4AA();
				try
				{
					Thread.sleep(300);
					MainActivity.SendU2AA();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		};
		Timer mTimer = new Timer();
		mTimer.schedule(U2AATimer, 1500);
*/


		mSpectrumView.setChArraySize(MainActivity.CHANNEL_ARRAY_SIZE);
		mSpectrumView.LogMode(true);
		Set_Spectrum_X_toEnergy();
		Set_SpcInfo(0, 0);

		mCalib_EndCnt = MainActivity.mPrefDB.Get_Calibration_AcqCnt();
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);
		filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
		filter.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);



		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(m_MainLayout);

		mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout1);

		mFrameLayout.setOnTouchListener(this);

		String activity = getIntent().getStringExtra(MEASUREMENT_MODE);

		if (activity.equals(MEASUREMENT_BACKGROUND)) {
			// bottombarTxt.setText(getString(R.string.background));

			setup_ModeTxt1.setText(getString(R.string.b));
			setup_ModeTxt2.setText(getString(R.string.ackground));
			StartBackground();
			// openOptionsMenu();
		}
		else if (activity.equals(MEASUREMENT_EN_CALIBRATION)) {

			setup_ModeTxt1.setText(getString(R.string.e));
			setup_ModeTxt2.setText(getString(R.string.nergy_calibration));


			if (MainActivity.mDebug.hw) {
				//hw test용  바로 calibration 시작
				StartCalibration();
			} else {

				LayoutInflater inflater1 = getLayoutInflater();
				View dlg = inflater1.inflate(R.layout.cali_dlg2, null);

				TextView msg = (TextView) dlg.findViewById(R.id.IDTXT_DLG);
				msg.setText(getResources().getString(R.string.OverwriteEnergyCalibration));

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(SetupSpectrumActivity.this, android.R.style.Theme_Holo_Dialog));
				dialogBuilder.setView(dlg);
				dialogBuilder.setPositiveButton(getResources().getString(R.string.Continue), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						StartCalibration();

					}
				});
				dialogBuilder.setNegativeButton(getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//190102 추가
						//MainActivity.SendU4AA();
						SetupSpectrumActivity.this.finish();
					}
				});
				dialogBuilder.setCancelable(false);

				Dialog dialog = dialogBuilder.create();


				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				lp.copyFrom(dialog.getWindow().getAttributes());
				lp.width = 1100;
				lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
				lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;    // 배경에 반투명
				lp.dimAmount = 0.8f;    // 반투명 정도

				dialog.show();
				dialog.getWindow().setAttributes(lp);

				//.dialogBuilder.show();


			}
		}

	}

	public boolean onTouch(View v, MotionEvent event) {

		if (v.getContext() == mFrameLayout.getContext()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// mPreTouchPosX = (int) event.getX();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				int nTouchPosX = (int) event.getX();

				int nTouchPosY = (int) event.getY();

				String str = Integer.toString(nTouchPosX);

				String str2 = Integer.toString(nTouchPosY);

				// Toast.makeText(getApplicationContext(), "X: " + nTouchPosX +
				// ", Y:" + nTouchPosY, 1).show();

			}
		}
		return true;
	};

	public void StartBackground() {

		MainActivity.ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.NOT_FIRST_ACTIVITY;
		MainActivity.ACTIVITY_STATE = Activity_Mode.BACKGROUND_RUNNING;
		LocalBroadcastManager.getInstance(mContext).registerReceiver(mMainBCR, filter);
		// MainActivity.mService.write(MainActivity.MESSAGE_START_HW);
		Set_SpcInfo(0, 0);

		mSPC.ClearSPC();

		mIsCaling = false;
		mIsBackGrounding = false;

		Set_SpcInfo_AcqTime(0);

		mSPC.ClearSPC();
		mIsBackGrounding = true;
		mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));

		mSPC.Set_StartSystemTime();

	}

	public void StartCalibration() {
		MainActivity.ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.NOT_FIRST_ACTIVITY;
		MainActivity.ACTIVITY_STATE = Activity_Mode.CALIBRATION_RUNNING;
		LocalBroadcastManager.getInstance(mContext).registerReceiver(mMainBCR, filter);

		// MainActivity.mService.write(MainActivity.MESSAGE_START_HW);
		mIsCaling = false;
		mIsBackGrounding = false;

		mSPC.ClearSPC();
		mIsCaling = true;
		mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
		mSpectrumView.Change_X_to_Channel();

	}

	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case MSG_MEASURE_BG:
					try {

						double Percent = ((double) mSPC.Get_AcqTime() / (double) mBG_GoalTime) * 100.0;
						mProgBar.Set_Value(Percent);
						mProgBar.invalidate();
						Set_SpcInfo_AcqTime((int)mSPC.Get_AcqTime());
						mSpectrumView.invalidate();

						L("Mesurement BG: AcqTime- " + mSPC.Get_AcqTime() + " / " + mBG_GoalTime + " sec");
						// ---

						if (mBG_GoalTime <= mSPC.Get_AcqTime())
						{

							//190102 추가
							//MainActivity.SendU4AA();

							mProgBar.Set_Value(0);
							mProgBar.invalidate();
							mIsBackGrounding = false;

							mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));
							Set_SpcInfo_AcqTime(0);

							double[] before_Coeffcient = mPrefDB.Get_Cali_ABC_From_pref();
							if (before_Coeffcient[0] != 0) {
								double[] SmthedBG = NcLibrary.ft_smooth(mSPC.ToInteger(), 0.015, 2.96474);
								double ROI_Start = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.8, before_Coeffcient);
								double ROI_End = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.2, before_Coeffcient);
								int K40PeakCh = NcLibrary.ROIAnalysis(SmthedBG, (int) ROI_Start, (int) ROI_End);

/*180321
 * 							if (K40PeakCh != 0) {
								double Old_CaliCh1 = mPrefDB.Get_CaliPeak1_From_pref();
								double Old_CaliCh2 = mPrefDB.Get_CaliPeak2_From_pref();
								double Old_K40CH = mPrefDB.Get_CaliPeak3_From_pref();
								double Ratio = (((double) K40PeakCh - Old_K40CH) / Old_K40CH);
								double New_Peak1 = (double) Old_CaliCh1 + ((double) Old_CaliCh1 * Ratio);
								double New_Peak2 = (double) Old_CaliCh2 + ((double) Old_CaliCh2 * Ratio);

								double[] FitParam = new double[3];
								NcLibrary.QuadraticCal(New_Peak1, New_Peak2, (double) K40PeakCh, NcLibrary.CS137_PEAK1,
										NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

								Coefficients En_coeff = new Coefficients(FitParam);
								Coefficients Ch_coeff = new Coefficients(
										new double[] { New_Peak1, New_Peak2, K40PeakCh });
								Intent intent = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
								intent.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
								intent.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

								mSPC.Set_Coefficients(FitParam);
								mSpectrumView.Change_X_to_Energy(FitParam);
								L("Recalibration: found K40- " + K40PeakCh + "ch, DR- "
										+ NcLibrary.Channel_to_Energy(1024, FitParam) + " kev");
							}*/
							}
							L("Measured BG- " + mSPC.ToString());
							L("Mesurement BG Success");
							mSPC.Save_DateNow();
							WriteOnDB_Background_Data(mSPC);


							Intent intent = new Intent(MainBroadcastReceiver.MSG_REMEASURE_BG);
							intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, mSPC.ToSpectrum());
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

							mSPC.ClearSPC();

							Toast.makeText(getApplicationContext(), getResources().getString(R.string.success),
									Toast.LENGTH_LONG).show();

							finish();

						}
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;

				case MSG_MEASURE_CALIB:
					try {
						double Percents = ((double) mSPC.Get_TotalCount() / (double) mCalib_EndCnt) * 100.0;
						mProgBar.Set_Value(Percents);
						mProgBar.invalidate();
						Set_SpcInfo_AcqTime((int)mSPC.Get_AcqTime());
						mSpectrumView.invalidate();

						L("Energy Calibration: AcqTime- " + mSPC.Get_TotalCount() + " / " + mCalib_EndCnt + " cnt");
						// -----------------

						if (mCalib_EndCnt < mSPC.Get_TotalCount())
						{
							//MainActivity.SendU4AA();

							mProgBar.Set_Value(0);
							mProgBar.invalidate();
							Set_SpcInfo_AcqTime(0);
							mIsCaling = false;
							mSpectrumView.Set_DataColor(Color.rgb(255, 201, 14));

							// ----
							double[] Peaks = new double[2];
							Peaks[0] = (double) NcLibrary.FindPeak(mSPC.ToInteger())[0];

							double[] temp = new double[1024];
							temp = NcLibrary.ft_smooth(mSPC.ToInteger(), 0.015, 2.96474);

							Peaks[1] = (double) NcLibrary.FindPeak(mSPC.ToInteger())[1];

							if (Peaks[0] == 0 || Peaks[1] == 0) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed),Toast.LENGTH_LONG).show();

					/*		Toast toast = Toast.makeText(mContext, "The Current version of the hardware does not support this feature", Toast.LENGTH_SHORT);
							toast.show();*/

								mSPC.ClearSPC();
								finish();

							} else {


								double resultA = ((float) (Math.max(NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2)
										- Math.min(NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2))
										/ (Math.max(Peaks[0], Peaks[1]) - Math.min(Peaks[0], Peaks[1])));
								double resultB = (float) (NcLibrary.CS137_PEAK1 - Peaks[0] * resultA);
								int ch1 = (int) NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.8, resultA, resultB, 0);
								int ch2 = (int) NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.2, resultA, resultB, 0);


								int K40_Ch = NcLibrary.ROIAnalysis(temp, ch1, ch2);

								if (K40_Ch<=10) {
									Toast.makeText(getApplicationContext(), "Can't found K40 peak. Please check linearity!", Toast.LENGTH_LONG).show();

									mSPC.ClearSPC();
									finish();
								}

								double[] FitParam = new double[3];
								NcLibrary.QuadraticCal(Peaks[0], Peaks[1], K40_Ch, NcLibrary.CS137_PEAK1,NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
								Write_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], Peaks[0], Peaks[1], K40_Ch);

								// --===--

								Coefficients En_coeff = new Coefficients(FitParam);
								Coefficients Ch_coeff = new Coefficients(new double[] { Peaks[0], Peaks[1], K40_Ch });

								int[] caliInfo= {(int)Peaks[0], (int)Peaks[1], K40_Ch, MainActivity.mDetector.mHW_GC };

								//180919 수정
								/*
								 * 현재 있는 Energy calibration  메뉴를 admin 메뉴로 변경하고 Energy calibration수행하면 새로운값 저장할지말지 안내창 팝업.
								 */
								//mSPC.ClearSPC();
								//mSpectrumView.invalidate();

								LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

								if(MainActivity.mDebug.hw == true){

									NcLibrary.SaveTextCali(caliInfo,MainActivity.FilenameCaliInfo, 4);
									MainActivity.mDetector.Set_EnergyFittingArgument(En_coeff.get_Coefficients());
									MainActivity.mPrefDB.Set_Calibration_Result(En_coeff.get_Coefficients(), Ch_coeff.get_Coefficients());
									//MainActivity.mPrefDB.Set_Calibration_Result(En_coeff.get_Coefficients(), Ch_coeff.get_Coefficients());

									mSPC.Set_Coefficients(FitParam);
									mSpectrumView.Change_X_to_Energy(FitParam);

									int ms[]= mSPC.ToInteger();

									for(int i=0; i<1024; i++){

										temp[i] = ms[i];
									}
									temp = NcLibrary.Smooth(temp, 1024, 2, 1);
									double[] VD = FindPeak.findVally(temp, NcLibrary.Auto_floor(Peaks[1]*0.8), NcLibrary.Auto_floor(Peaks[1]*1.2),NcLibrary.Auto_floor( Peaks[1]));

									for(int i=(int)VD[0]; i<=(int)VD[1]; i++) {
										try{
											temp[i-1] = (temp[i]-NcLibrary.Channel_to_Energy(i, VD[2],VD[3], 0));
										}catch(Exception e){
											NcLibrary.Write_ExceptionLog("\nID_VD"); //_ MS:"+SPC_Data.ToString()+" _ BG:"+BG_Data.ToString());
										}
									}

									double eff = NcLibrary.FWHM(temp, 1024, NcLibrary.Auto_floor(Peaks[1]), resultA, resultB,0 , true);
									Show_Dlg("result","Efficient - "+eff+" %\n( "+Peaks[0]+",  "+Peaks[1]+",  "+K40_Ch+" )");

									LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);
								}else{
									AlertDialog builder = resultDlg(caliInfo, FitParam,En_coeff,Ch_coeff ).create();
									WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
									lp.copyFrom(builder.getWindow().getAttributes());
									lp.width = 1100;
									lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
									lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;    // 배경에 반투명
									lp.dimAmount = 0.8f;    // 반투명 정도
									builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
									builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
									builder.show();
									builder.getWindow().setAttributes(lp);

								}


							}

							//finish();

						}
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;

				case MSG_CALIBRATION:

					break;

				case MSG_CALIBRATION_RESULT:

					break;

				default:
					break;
			}

		}
	};

	private void Show_Dlg(String Title, String Message){
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SetupSpectrumActivity.this);
		dialogBuilder.setTitle(Title);
		dialogBuilder.setMessage(Message);
		dialogBuilder.setNegativeButton(getResources().getString(R.string.ok),new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton)
			{
				SetupSpectrumActivity.this.finish();
			}

		});
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();
	}

	public  AlertDialog.Builder resultDlg(final int[] caliInfo, final double[] FitParam,final Coefficients En_Coeff, final Coefficients Ch_Coeff)
	{

		LayoutInflater inflater = getLayoutInflater();
		View dlg = inflater.inflate(R.layout.cali_dlg2, null);

		TextView msg = (TextView)dlg.findViewById(R.id.IDTXT_DLG);
		msg.setText(getResources().getString(R.string.CompleteEnergyCalibration));

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(SetupSpectrumActivity.this, android.R.style.Theme_Holo_Dialog));
		//dialogBuilder.setTitle(getResources().getString(R.string.UpdateCalibrationParameters_title));
		//dialogBuilder.setMessage(getResources().getString(R.string.CompleteEnergyCalibration));
		dialogBuilder.setView(dlg);
		dialogBuilder.setNegativeButton(getResources().getString(R.string.Save),new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton)
			{
				//Toast.makeText(getApplicationContext(), getResources().getString(R.string.ToastEnergyCalibration),Toast.LENGTH_LONG).show();


				if (En_Coeff.get_Coefficients()[0] == 0 || En_Coeff.get_Coefficients()[1] == 0)
				{
					//Intent intent = new Intent(MainBroadcastReceiver.MSG_FIXED_HWCALI_SEND);
					//LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

					//MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
					//PreferenceActivity.PreferenceActivity.finish();
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed),Toast.LENGTH_LONG).show();
					SetupSpectrumActivity.this.finish();
				}

							/*Intent intent = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
							intent.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
							intent.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);*/
				//intent 안하고 바로 수정

				else
				{
					NcLibrary.SaveTextCali(caliInfo,MainActivity.FilenameCaliInfo, 4);
					MainActivity.mDetector.Set_EnergyFittingArgument(En_Coeff.get_Coefficients());
					MainActivity.mPrefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());
					MainActivity.mPrefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());

					mSPC.Set_Coefficients(FitParam);
					mSpectrumView.Change_X_to_Energy(FitParam);


					Intent intent = new Intent(MainBroadcastReceiver.MSG_FIXED_HWCALI_SEND);
					LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

					SetupSpectrumActivity.this.finish();
					//PreferenceActivity.PreferenceActivity.finish();

				}



			}
		});
		dialogBuilder.setPositiveButton(getResources().getString(R.string.Cancel),new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton)
			{
				SetupSpectrumActivity.this.finish();
			}
		});
		dialogBuilder.setCancelable(false);
		//dialogBuilder.show();

		return dialogBuilder;
	}



	/*	public  AlertDialog.Builder resultDlg(final int[] caliInfo, final double[] FitParam,final Coefficients En_Coeff, final Coefficients Ch_Coeff)
        {
            LayoutInflater inflater = getLayoutInflater();
            View dlg = inflater.inflate(R.layout.cali_dlg, null);

            TextView caliA = (TextView)dlg.findViewById(R.id.IDTXT_CALIA);
            TextView caliB = (TextView)dlg.findViewById(R.id.IDTXT_CALIB);
            TextView caliC = (TextView)dlg.findViewById(R.id.IDTXT_CALIC);
            TextView hw = (TextView)dlg.findViewById(R.id.IDTXT_HWGC);


            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(SetupSpectrumActivity.this, android.R.style.Theme_Holo_Dialog));
            dialogBuilder.setTitle(getResources().getString(R.string.UpdateCalibrationParameters_title));
            dialogBuilder.setView(dlg);
            caliA.setText(" "+caliInfo[0]+",");
            caliB.setText(" "+caliInfo[1]+",");
            caliC.setText(" "+caliInfo[2]+",");
            hw.setText(" "+caliInfo[3]);

            dialogBuilder.setMessage(getResources().getString(R.string.UpdateCalibrationParameters));
            dialogBuilder.setNegativeButton(getResources().getString(R.string.ok),new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton)
                        {

                            NcLibrary.SaveTextCali(caliInfo,MainActivity.FilenameCaliInfo, 4);

                                Intent intent = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
                                intent.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
                                intent.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                //intent 안하고 바로 수정
                                if (En_Coeff.get_Coefficients()[0] == 0 || En_Coeff.get_Coefficients()[1] == 0)
                                {

                                }
                                else
                                {

                                    MainActivity.mDetector.Set_EnergyFittingArgument(En_Coeff.get_Coefficients());
                                    MainActivity.mPrefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());
                                    MainActivity.mPrefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());

                                    mSPC.Set_Coefficients(FitParam);
                                    mSpectrumView.Change_X_to_Energy(FitParam);

                                    Intent send_gs = new Intent(MainBroadcastReceiver.MSG_FIXED_HWCALI_SEND);
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);

                                }
                                SetupSpectrumActivity.this.finish();

                        }
                    });
            dialogBuilder.setPositiveButton("NO",new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton)
                {
                    SetupSpectrumActivity.this.finish();
                }
            });
            dialogBuilder.setCancelable(false);
            //dialogBuilder.show();

            return dialogBuilder;
        }
    */
	@Override
	protected void onResume() {

		// Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);

		// LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
		/*
		 * String activity = null; activity =
		 * getIntent().getStringExtra(MEASUREMENT_MODE);
		 *
		 * if (activity.equals(MEASUREMENT_BACKGROUND)) {
		 *
		 * Start_Measurement_BG();
		 *
		 * } else if (activity.equals(MEASUREMENT_EN_CALIBRATION)) {
		 *
		 * Start_EnCalibration();
		 *
		 * }
		 */
		super.onResume();
	}

	public void Start_EnCalibration() {

		IntentFilter filter1 = new IntentFilter();

		filter1.addAction(MainBroadcastReceiver.MSG_TAB_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_TAB_EN_CALIBRATION);
		filter1.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);
		filter1.addAction(MainBroadcastReceiver.MSG_CALIBRATION_CANCEL);

		filter1.addAction(MainBroadcastReceiver.MSG_START_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_START_CALIBRATION);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter1);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linearLayout = (LinearLayout) inflater.inflate(R.layout.start_calibration, null);

		setContentView(linearLayout);

		AcqCountStr = String.valueOf(MainActivity.mPrefDB.Get_Calibration_AcqCnt());

		// StartBtn

		startBtn = (Button) findViewById(R.id.StartBtn);

		startBtn.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				onCreatPart();

			}

		});

	}

	public void Start_Measurement_BG() {

		IntentFilter filter1 = new IntentFilter();

		filter1.addAction(MainBroadcastReceiver.MSG_TAB_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_TAB_EN_CALIBRATION);
		filter1.addAction(MainBroadcastReceiver.MSG_BACKGROUND_CANCEL);
		filter1.addAction(MainBroadcastReceiver.MSG_CALIBRATION_CANCEL);

		filter1.addAction(MainBroadcastReceiver.MSG_START_BACKGROUND);
		filter1.addAction(MainBroadcastReceiver.MSG_START_CALIBRATION);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter1);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		linearLayout = (LinearLayout) inflater.inflate(R.layout.start_background, null);

		setContentView(linearLayout);

		IntentFilter filter2 = new IntentFilter();

		AcqTimeStr = String.valueOf(MainActivity.mPrefDB.Get_BG_AcqTime_SetValue_From_pref());

		// StartBtn

		startBtn = (Button) findViewById(R.id.StartBtn);

		startBtn.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {

				onCreatPart();

			}

		});

	}

}
