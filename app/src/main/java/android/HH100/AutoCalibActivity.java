package android.HH100;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.sql.NClob;
import java.text.SimpleDateFormat;
import java.util.*;

import NcLibrary.NcMath;
import NcLibrary.SpcAnalysis;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.R;
import android.HH100.Control.*;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.Detector;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.os.*;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class AutoCalibActivity extends Activity {
	public final boolean D = MainActivity.D;
	private final String TAG = "AutoCalib";

	// public static final String EXTRA_THRESHOLD_CNT = "extra_threshold";
	// public static final String EXTRA_FAIL_CNT = "extra_fail_cnt";

	// public static final int RESULT_CALIB_ERR = 9;
	// public static final int RESULT_TIMEOUT_AND_COUNT = 10;
	// public static final int RESULT_OUT_OF_BOUND = 11;

	// public static final int RESULT_SUCCESS = 12;
	// public static final int RESULT_CANCEL = 1;

	interface AutoCalbration {
		public String EXTRA_THRESHOLD_CNT = "extra_threshold";
		public String EXTRA_FAIL_CNT = "extra_fail_cnt";

		public int RESULT_CALIB_ERR = 9;
		public int RESULT_TIMEOUT_AND_COUNT = 10;
		public int RESULT_OUT_OF_BOUND = 11;

		public int RESULT_SUCCESS = 12;
		public int RESULT_CANCEL = 1;

	};

	private int HANDLE_FINISH = 5012;
	private int mThreshold_cnt = 400;// count
	private int mFail_time = 300;// sec;
	private double mFail_cnt = 4;// count

	private Handler mHandler;

	private Spectrum mSPC = new Spectrum();
	private double[] mBefore_cs137Peak = new double[2];
	private TextView mV_Percent;
	private Button mBtn_Cancel;

	private AutoCal_ProgressBar mProgBar = null;

	int mAutoCalCnt = 0;

	int loadingCnt = 0;
	public static Activity autoCalibrationActivity;

	public class MainBCRReceiver extends MainBroadcastReceiver {
		@Override
		public void onReceive(Context context, android.content.Intent intent) {
			// Action �젙蹂대�� 媛�吏�怨� �삩�떎.
			String action = intent.getAction();
			switch (action) {

				case MSG_RECV_SPECTRUM:
			/*	
				try {
					
					if (MainActivity.mDetector.MS.Get_TotalCount() > 1000)
					{
						// setResult(RESULT_CANCEL);

						Finish(AutoCalbration.RESULT_CANCEL);
						finish();
						return;
					}
					
					Spectrum recv_data = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);
					
					//save txt file
					// ------------------------------------------------------------------------
					mSPC.Accumulate_Spectrum(recv_data);
					mSPC.Set_Coefficients(recv_data.Get_Coefficients());

					if (mFail_time < mSPC.Get_AcqTime())
						Finish(AutoCalbration.RESULT_TIMEOUT_AND_COUNT);
					if (mSPC.Get_Coefficients().At(0) == 0)
						Finish(AutoCalbration.RESULT_CALIB_ERR);

					int ROI_Ch_start = 0;
					int ROI_Ch_end = 0;
					
					// YKIM ROI range reduce 10% for CeBr, 0.8 -> 0.87 1.2 -> 1.13
					
					if (MainActivity.mDetector.mCrystal == Detector.CrystalType.NaI)
					{	ROI_Ch_start = SpcAnalysis.ToChannel(NcLibrary.K40_PEAK * 0.8, mSPC.Get_Coefficients());// NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK*0.7,
					  																				// mSPC.Get_Coefficients()));
						ROI_Ch_end = SpcAnalysis.ToChannel(NcLibrary.K40_PEAK * 1.2, mSPC.Get_Coefficients());
					}else
					{	ROI_Ch_start = SpcAnalysis.ToChannel(NcLibrary.K40_PEAK * 0.87, mSPC.Get_Coefficients());
						ROI_Ch_end = SpcAnalysis.ToChannel(NcLibrary.K40_PEAK * 1.13, mSPC.Get_Coefficients());
						
					}
			

					double[] temp = new double[mSPC.Get_Ch_Size()];
					temp = NcLibrary.Smooth(mSPC.ToInteger(), mSPC.Get_Ch_Size(), 10, 1);

					int K40_ROI_cnt = NcLibrary
							.Auto_floor(NcLibrary.ROIAnalysis_GetTotCnt(temp, ROI_Ch_start, ROI_Ch_end));
					/////////

					L("K40 Range- " + ROI_Ch_start + " ~ " + ROI_Ch_end + "Ch, " + "Count - " + K40_ROI_cnt + "cnt, "
							+ "Elapsed Time- " + mSPC.Get_AcqTime() + " sec");

					double percent = NcMath.Percent(K40_ROI_cnt, mThreshold_cnt);// ((double)K40_ROI_cnt/(double)mThreshold_cnt)*100.0;
					if (percent >= 100)
						percent = 100;

					mV_Percent.setText(String.valueOf((int) percent) + "%");
					mProgBar.Set_Value(percent);

					mProgBar.invalidate();

					if (K40_ROI_cnt > mThreshold_cnt) {
						double Be_K40_Ch = Get_CaliPeak3_From_pref();

						////
						int K40_Ch = NcLibrary.ROIAnalysis(temp, ROI_Ch_start, ROI_Ch_end);
						if (K40_Ch != 0) {

							if (temp[K40_Ch] < mFail_cnt) {
								Finish(AutoCalbration.RESULT_TIMEOUT_AND_COUNT);

							} else {
								if (K40_Ch != Be_K40_Ch) {

									Intent send_gs = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
									send_gs.putExtra(MainBroadcastReceiver.DATA_GS_STATUS,
											MainBroadcastReceiver.DATA_END);
									send_gs.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
									LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

								} else
									L("Success: Not shifted , Old K40 - " + Be_K40_Ch + ", New K40 - " + K40_Ch);
								Finish(AutoCalbration.RESULT_SUCCESS);

							}
						} else {
							Finish(AutoCalbration.RESULT_OUT_OF_BOUND);
						}

					}

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				break;
*/

					//Hung:2018.02.01
					//save current time:2018
					try
					{

						if (MainActivity.mDetector.MS.Get_TotalCount() > 1000)
						{
							Finish(AutoCalbration.RESULT_CANCEL);
							//finish();
							//return;
						}

						Spectrum recv_data = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

						//save txt file
						// ------------------------------------------------------------------------
						mSPC.Accumulate_Spectrum(recv_data);

						mSPC.Set_Coefficients(recv_data.Get_Coefficients());

						mSPC.setFWHM(recv_data.getFWHM());


						//save txt file
						//save txt
	/*				NcLibrary.SaveText("\n AutoCalibration mSPC.Get_Coefficients()"+mSPC.Get_Coefficients().ToString());
					NcLibrary.SaveText("\n AutoCalibration mSPC.getFWHM()"+mSPC.getFWHM());*/

						if (NcLibrary.mFail_time < mSPC.Get_AcqTime())
						{
							Finish(AutoCalbration.RESULT_TIMEOUT_AND_COUNT);
						}
						if (mSPC.Get_Coefficients().At(0) == 0)
						{
							Finish(AutoCalbration.RESULT_CALIB_ERR);
						}


						//Add finding peak in here
						//Finding Peak
					//	int K40_ROI_cnt=(int) NcLibrary.CalcROIK40(mSPC.ToInteger(), mSPC.getFWHM(), mSPC.Get_Coefficients().get_Coefficients());
						int K40_ROI_cnt=(int) NcLibrary.CalcROIK40(mSPC.ToInteger(), mSPC.getFWHM(), MainActivity.mDetector.MS.Get_Coefficients().get_Coefficients());


						double percent = NcMath.Percent(K40_ROI_cnt,NcLibrary.GAIN_THRESHOLD_CNT);// ((double)K40_ROI_cnt/(double)mThreshold_cnt)*100.0;
						if (percent >= 100)
							percent = 100;

						mV_Percent.setText(String.valueOf((int) percent) + "%");
						mProgBar.Set_Value(percent);

						mProgBar.invalidate();

						if (K40_ROI_cnt > NcLibrary.GAIN_THRESHOLD_CNT)
						{

							double Be_K40_Ch = Get_CaliPeak3_From_pref();


							int K40_Ch=NcLibrary.PeakAna(mSPC.ToInteger(), mSPC.getFWHM(), mSPC.Get_Coefficients().get_Coefficients());
						
					/*
						int[] spc = mSPC.ToInteger();
						String strSpc ="";
						for(int k = 0; k < spc.length; k++)
						{
							strSpc = strSpc + spc[k]+" ";
						}
						NcLibrary.SaveText("\n AutoCalibration mSPC.ToInteger() "+strSpc);
						
						NcLibrary.SaveText("\n AutoCalibration mSPC.Get_Coefficients() "+mSPC.Get_Coefficients().ToString());
						double[] FWHM_gen = mSPC.getFWHM();
						NcLibrary.SaveText("\n AutoCalibration mSPC.getFWHM() "+FWHM_gen[0]+FWHM_gen[1]);*/

							if (K40_Ch > 0) {

								if (K40_Ch != Be_K40_Ch) {

									Intent send_gs = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
									send_gs.putExtra(MainBroadcastReceiver.DATA_GS_STATUS,
											MainBroadcastReceiver.DATA_END);
									send_gs.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
									LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

								} else
									L("Success: Not shifted , Old K40 - " + Be_K40_Ch + ", New K40 - " + K40_Ch);



								Finish(AutoCalbration.RESULT_SUCCESS);


							} else {
								Finish(AutoCalbration.RESULT_OUT_OF_BOUND);
							}


						}

						//
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}

					break;
				case MSG_DISCONNECTED_BLUETOOTH:
					finish();
					break;
			}

		}
	}

	private MainBCRReceiver mMainBCR = new MainBCRReceiver();

	public AutoCalibActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout m_MainLayout = (LinearLayout) inflater.inflate(R.layout.auto_calib, null);
		setContentView(m_MainLayout);

		MainActivity.ACTIVITY_STATE = Activity_Mode.AUTO_CALIBRATION;

		mProgBar = (AutoCal_ProgressBar) m_MainLayout.findViewById(R.id.AutoCal_ProgressBar);

		mProgBar.Set_Value(0);
		mV_Percent = (TextView) m_MainLayout.findViewById(R.id.percent);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

		mThreshold_cnt = getIntent().getIntExtra(AutoCalbration.EXTRA_THRESHOLD_CNT, mThreshold_cnt);
		mFail_cnt = getIntent().getDoubleExtra(AutoCalbration.EXTRA_FAIL_CNT, mFail_cnt);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (msg.what == HANDLE_FINISH)
					finish();
			}
		};

		mBtn_Cancel = (Button) findViewById(R.id.AutoCali_Cancel);
		mBtn_Cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub
				setResult(AutoCalbration.RESULT_CANCEL);
				mHandler.removeMessages(0);
				finish();
			}
		});

		setContentView(m_MainLayout);
		autoCalibrationActivity = this;
	}

	@Override
	public void onDestroy() {

		try {

			LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);

		} catch (NullPointerException e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		super.onDestroy();
	}

	//////
	private void L(String log) {
		if (D)
			Log.i(TAG, log);
	}

	@Override
	protected void onStart() {
		super.onStart();

		try {

			PreferenceDB prefDB = new PreferenceDB(this);
			mBefore_cs137Peak[0] = prefDB.Get_CaliPeak1_From_pref();
			mBefore_cs137Peak[1] = prefDB.Get_CaliPeak2_From_pref();
			double[] ABC = prefDB.Get_Cali_ABC_From_pref();
			if (ABC[0] == 0 || ABC[1] == 0) {
				setResult(AutoCalbration.RESULT_CALIB_ERR);
				finish();
			}

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	@Override
	public void finish() {
		mHandler.removeMessages(0);
		// mReDrawTimer.cancel();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
		super.finish();
	}

	public void Finish(int Result) {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
		setResult(Result);
		mHandler.sendEmptyMessage(HANDLE_FINISH);

	}

	@Override
	public void onBackPressed() {
		setResult(AutoCalbration.RESULT_CANCEL);
		mHandler.removeMessages(0);
		finish();
		// finish();
		return;
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
			BG = Get_BG_From_pref();

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
			PreferenceDB prefDB = new PreferenceDB(getApplicationContext());

			result = new Spectrum();
			result.Set_Spectrum(BG, prefDB.Get_BG_MeasuredAcqTime_From_pref());
			result.Save_DateNow();
			result.Set_Coefficients(mSPC.Get_Coefficients());
			// mPrefDB.Set_BG_On_pref(NewBG,MainActivity.CHANNEL_ARRAY_SIZE);
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return result;
		}
	}

	private boolean Send_GC_ToHW(int NewK40Ch) {
		if (MainActivity.mDetector.mHW_K40_FxiedCh == 0)
			return false;

		double FixedK40 = MainActivity.mDetector.mHW_K40_FxiedCh;
		if (FixedK40 * 0.98 < NewK40Ch && FixedK40 * 1.02 > NewK40Ch) {
			if (MainActivity.D)
				Log.i("GC", "Found K40 Channel In 2%");
			return false;
		}

		double peak = Double.valueOf(NewK40Ch);
		double Ratio = (((double) NewK40Ch - FixedK40) / FixedK40);
		Ratio = Ratio * -1;

		double New_Peak1 = (double) MainActivity.mDetector.mHW_GC + ((double) MainActivity.mDetector.mHW_GC * Ratio);
		String NewGC = Integer.toHexString(NcLibrary.Auto_floor(New_Peak1));
		byte[] GcBytes = new java.math.BigInteger(NewGC, 16).toByteArray();

		byte[] ss = new byte[5];
		ss[0] = 'G';
		ss[1] = 'C';
		//
		if (GcBytes.length == 1) {
			ss[2] = 0;
			ss[3] = GcBytes[0];
		}

		else {
			ss[2] = GcBytes[0];
			ss[3] = GcBytes[1];
		}
		///
		ss[4] = (byte) Byte.valueOf((byte) 1);

		// MainActivity.mService.write(ss);

		if (MainActivity.D)
			Log.i("GC", "Found K40Ch: " + NewK40Ch + " _New GC:" + New_Peak1 + "(" + Ratio + " %)");
		MainActivity.mDetector.mHW_GC = NcLibrary.Auto_floor(New_Peak1);
		return true;
	}

	public int[] Get_BG_From_pref() {
		int[] ChArray = new int[1024];
		SharedPreferences pref = getSharedPreferences(getString(R.string.bg_table), 0);
		for (int i = 0; i < 1024; i++) {
			ChArray[i] = pref.getInt(getString(R.string.bg_array) + String.valueOf(i), 0);
		}
		int check = 0;
		for (int i = 0; i < 1024; i++) {
			check += ChArray[i];
		}
		return ChArray;

		// TODO Auto-generated method stub
	}

	public double Get_CaliPeak3_From_pref() {
		String Temp;
		SharedPreferences prefAB = getSharedPreferences(getString(R.string.ab_table), 0);
		Temp = prefAB.getString(getString(R.string.cali_peak3), String.valueOf(0));
		return Double.valueOf(Temp);
	}

}
