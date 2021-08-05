package android.HH100;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import Debug.Debug;
import NcLibrary.Coefficients;
import NcLibrary.NcMath;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.MainActivity.Focus;
import android.HH100.MainActivity.Tab_Name;
import android.HH100.Control.Analysis_TopInfor;
import android.HH100.Control.GpsInfo;
import android.HH100.Control.GpsInfo2;
import android.HH100.Control.ProgressBar;
import android.HH100.Control.ScView_Ad;
import android.HH100.Control.SpectrumView;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Dialog.LoginDlg;
import android.HH100.Identification.FindPeaksM;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.LogActivity.LogPhotoTab.Media;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.Mail;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.SingleMediaScanner;
import android.HH100.Structure.Spectrum;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class SequentialActivity extends Activity implements View.OnTouchListener {
	private static final boolean D = MainActivity.D;
	private static final String TAG = "IDSpectrum";
	private static final String TAG_SEQ = "IDSpectrum_seq";
	private static final String TAG_MANUALID = "IDSpectrum_manual";

	public static final int MS_SEQUENCE_MODE = 532842;
	public static final int MS_MANUAL_ID = 532843;
	public static final int LIST_DRAW = 532844;
	public static final int TEST = 532845;

	public static final String EXTRA_SPECTRUM = "extra_spc";
	public static final String EXTRA_BG_SPECTRUM = "extra_bg_spc";
	public static final String EXTRA_DETECTED_SOURCE = "extra_detected_source";
	public static final String EXTRA_MANUAL_ID_GOAL_TIME = "extra_mID_goalTime";
	public static final String EXTRA_MANUAL_ID_ADJUST_TIME = "extra_adjust_goalTime";
	public static final String EXTRA_SEQ_ACQTIME = "extra_acq_time";
	public static final String EXTRA_SEQ_REPEAT = "extra_repeat";
	public static final String EXTRA_SEQ_PAUSE_TIME = "extra_pause_time";
	public static final String EXTRA_SV_UNIT = "extra_sv_unit";
	public static final String ACTIVTY = "actvity";

	public static boolean SOURCE_ID_RESULT_MODE = false;

	private SpectrumView mSpectrumView;
	private Analysis_TopInfor mTopAnal_Info;

	public Spectrum mSPC = new Spectrum();

	private LinearLayout m_MainLayout;
	private LinearLayout m_AnalysisLayout;
	private ScView_Ad mAnalysisView;

	//190102 추가
	//TimerTask U2AATimer = null;


	/// --Manual ID

	// -- Acq Time

	private int mSequence_acqTime = 0;
	private int mSequence_repeat_Goal = 0;
	private int mSequence_repeat_count = 0;
	private int mSequenceTotalCount = 0;

	private int mPauseTime = 5;// sec
	private int mPauseTime_ElapsedTime = 0;
	private int mPauseTimeElapsedDecreaseTime = 5;

	// --
	public boolean mIsSequenceMode = false;
	private boolean mIsSvUnit = true;

	private boolean mIsEvent = false;
	/// --End Manual ID
	public static boolean mIsManualID_mode = false;
	private Vector<Integer> mClassColor = new Vector<Integer>();

	private EventData m_EventData = null;

	public int cntt = 0;

	private ViewFlipper mMainFlipper;

	ViewFlipper flipper;

	int swicthCount = 0, id_result_menu_b_count = Activity_Mode.UN_EXCUTE_MODE;

	private int mPreTouchPosX;

	boolean filperMove = false;

	View IDspectrum, Iso_analysis;

	ScrollView scrollview1;

	private ProgressBar mProgBar = null;

	TextView ResultLocationInfoTxt, userInfoTxt, IdAcqTime, cpsTxt, totalCountTxt, acqTimeTxt, acqTimeTxt2;

	Button removeEventTxt;

	LinearLayout LayoutA, LayoutB, LayoutC;

	EditText CommentEdit;

	public static Vector<EventData> mAllLog;

	// video, photo declear

	public static String EXTRA_PHOTO_FILE_NAME = "Photo";
	public static String EXTRA_VIDEO_FILE_NAME = "Video";
	public static String EXTRA_EVENT_NUMBER = "EventNumber";

	public boolean IS_TAKE_PHOTO_AND_VIDEO = true;

	public static ListView mGallery = null;
	Vector<Bitmap> mThumnail = new Vector<Bitmap>();
	Vector<String> mPhoto = null;
	Vector<String> mVideo = null;
	TextView mMediaCnt = null;
	int mEventNumber;

	ProgressDialog mPrgDlg;

	public ListView m_lv = null;
	View m_lv1 = null;

	public static int rootFocusCnt = Focus.ID_RESULT_MENU_C;

	public static int currentRootFocusCnt = 0;

	public static int currentMediaCnt = 0;

	public static int checkSelectMode = Activity_Mode.ID_RESULT_UN_CHECK_SELECT_MODE;
	public static int checkSelectModeCount = 0;

	public static int checkMediaModeCount = Activity_Mode.UN_EXCUTE_MODE;

	public static int DoubleClickRock = Activity_Mode.EXCUTE_MODE;

	public ArrayList<String> SpectrumArrayString = new ArrayList<String>();

	public interface Check {

		int IdResult_Checked = 0;
		int IdResult_Not_Checked = 1;
		int Result_Ok = -1;
		int Result_Not_Ok = -2;
		String ListNumber = "ListNumber";
		String ListValue = "ListValue";

		public String Favorite_False = "false";

		public String Favorite_True = "true";
	}

	Debug mDebug;

	int countTest = 0;

	int focusAEnterRock = 0;

	int HWDoubleClickRock = 0;

	int mGalleryListViewCurrentPosition = 0;

	Vector<String> mTotalTxt = new Vector<String>();

	Vector<String> mRecoder = new Vector<String>();

	FrameLayout mFrameLayout;

	GpsInfo2 mGpsInfo2;

	// video, photo end

	public class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, android.content.Intent intent) {

			try {
				String action = intent.getAction();
				switch (action) {
					case MSG_RECV_SPECTRUM:

						if (D)
							Log.d(TAG, "Receive Broadcast- Spectrum");
						Spectrum spc = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

						mSpectrumView.Show_Info(true);

						mSequenceTotalCount++;
						double Percent = 0;
						if (mSequence_repeat_Goal == 0) {
							Percent = (((double) (mSequenceTotalCount))
									/ (double) ((mSequence_acqTime * mSequence_repeat_Goal)
									+ (mPauseTime * mSequence_repeat_Goal)))
									* 100.0;
						} else {

							Percent = (((double) (mSequenceTotalCount))
									/ (double) ((mSequence_acqTime * mSequence_repeat_Goal)
									+ (mPauseTime * (mSequence_repeat_Goal - 1))))
									* 100.0;
						}
						mProgBar.Set_Value(Percent);
						mProgBar.invalidate();
						if (true) {
							if (mPauseTime_ElapsedTime >= mPauseTime) {
								mHandler.obtainMessage(MS_SEQUENCE_MODE, 0, 0, spc).sendToTarget();
								mPauseTimeElapsedDecreaseTime = mPauseTime;
							} else {

								mPauseTime_ElapsedTime += 1;

								mPauseTimeElapsedDecreaseTime -= 1;

								sequentialMode.setText(
										"Pause. Start in " + Integer.toString(mPauseTimeElapsedDecreaseTime) + " sec...");
								Set_Info_OnSpectrumView_OnAnalysisView(mPauseTime_ElapsedTime, 0);
								//mSpectrumView.invalidate();
								mTopAnal_Info.invalidate();

							}
						} else {
							mSPC.Accumulate_Spectrum(spc);
							mSpectrumView.SetChArray(mSPC);
							if (mIsEvent)
								Set_Info_OnSpectrumView_OnAnalysisView((int)mSPC.Get_AcqTime(), spc.Get_TotalCount());
							else
								Set_Info_OnSpectrumView_OnAnalysisView(0, spc.Get_TotalCount());
							//mSpectrumView.invalidate();
							mTopAnal_Info.invalidate();
						}

						break;
					case MSG_RECV_NEUTRON:
						// int event_status =
						// intent.getIntExtra(DATA_EVENT_STATUS,Detector.EVENT_NONE);
						break;
					case MSG_EVENT:
						if (D)
							Log.d(TAG, "Receive Broadcast- Event");
						int event_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
						EventData eventdb = (EventData) intent.getSerializableExtra(DATA_EVENT);
						if (eventdb.Event_Detector.matches(EventData.EVENT_NEUTRON))
							break;

						if (event_status == Detector.EVENT_BEGIN) {
							mSPC.ClearSPC();
							mIsEvent = true;
						} else if (event_status == Detector.EVENT_ING) {

							@SuppressWarnings("unchecked")
							Vector<Isotope> isos = (Vector<Isotope>) intent.getSerializableExtra(DATA_SOURCE_ID);
							Set_IdResult_toViews(isos);
							mIsEvent = true;

						} else if (event_status == Detector.EVENT_FINISH) {
							mAnalysisView.RemoveAll_IsotopeData();
							mSpectrumView.Clear_Found_Isotopes();

							mSpectrumView.invalidate();
							mAnalysisView.invalidate();

							mSPC.ClearSPC();
							mIsEvent = false;
						}

						break;

					case MSG_EN_CALIBRATION:
						Coefficients En_Coeff = (Coefficients) intent.getSerializableExtra(DATA_COEFFCIENTS);
						Coefficients Ch_Coeff = (Coefficients) intent.getSerializableExtra(DATA_CALIBRATION_PEAKS);

						mSPC.Set_Coefficients(En_Coeff.get_Coefficients());
						mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());

						if (D)
							Log.d(TAG, "Receive Broadcast- Recalibration (" + En_Coeff.ToString() + " || "
									+ Ch_Coeff.ToString() + ")");
						break;
					case MSG_REMEASURE_BG:
						Spectrum bg = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

						if (D)
							Log.d(TAG, "Receive Broadcast- Remeasured background (" + bg.ToString() + ")");
						// mBG = bg;
						break;
					case MSG_DISCONNECTED_BLUETOOTH:
						mSpectrumView.Clear_Found_Isotopes();
						mSPC.ClearSPC();
						mSpectrumView.SetChArray(mSPC);
						mAnalysisView.RemoveAll_IsotopeData();

						if (D)
							Log.d(TAG, "bluetooth disconnected");
						break;

					case MSG_SEQUENTAL_MODE_RUNNING_START:
						//190102 추가
					/*	try
						{
							Thread.sleep(500);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
						MainActivity.SendU2AA();
						U2AATimer = new TimerTask()
						{
							@Override
							public void run() {
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
						mTimer.schedule(U2AATimer, 1500);*/

						MainActivity.tabHost.setCurrentTab(Tab_Name.SequentialMode);
						MainActivity.ACTIVITY_STATE = Activity_Mode.SEQUENTAL_MODE_RUNNING;
						onCreatePart();

						// DropDownAnimation();
						break;
					case MSG_SPEC_VIEWFILPPER:

						View_Filpper();

						break;

				}

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	private MainBCRReceiver mMainBCR = new MainBCRReceiver();

	// 인플레이터 선언 부분

	LayoutInflater inflater;
	LinearLayout linearLayout, linearLayout1;

	Button startBtn;
	Context mContext;

	PreferenceDB mPrefDB;

	TextView IDAcqTimeTxt = null, sequentialMode = null, sequentialCount = null;

	RelativeLayout mRelativieLayout1 = null;

	// SourceIdResultInfo
	CheckBox Favorite_Checkbox;

	Bitmap RecBitmap;

	ImageView filpperImgView;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub5
		super.onCreate(savedInstanceState);

		mContext = this;

		mGpsInfo2 = new GpsInfo2(mContext);

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

		IntentFilter filter = new IntentFilter();

		filter.addAction(MainBroadcastReceiver.MSG_SEQUENTAL_MODE_RUNNING_START);

		filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_BACK);

		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

	}

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			try {

				Spectrum spc = (Spectrum) msg.obj;
	/*			PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
				IsotopesLibrary IsoLib = new IsotopesLibrary(getApplicationContext());*/
				EventDBOper mEventDBOper = new EventDBOper();

				switch (msg.what) {
					case MS_SEQUENCE_MODE:

						//190102 추가
			/*			if(U2AATimer !=null)
						{
							U2AATimer.cancel();
							U2AATimer = null;
						}*/


						if (m_EventData == null) {
							Log.i(TAG_SEQ, "SEQ: Start Sequence mode");
							m_EventData = new EventData();
							m_EventData.mInstrument_Name = MainActivity.mDetector.InstrumentModel_Name;
							m_EventData.Doserate_unit = (MainActivity.mDetector.IsSvUnit) ? Detector.DR_UNIT_SV
									: Detector.DR_UNIT_R;
							m_EventData.Event_Detector = "SM" + (mSequence_repeat_count + 1);
							m_EventData.MS.Set_Coefficients(spc.Get_Coefficients());
							m_EventData.MS.Set_StartSystemTime();
							m_EventData.mUser = MainActivity.mDetector.User;
							m_EventData.mLocation = MainActivity.mDetector.Location;

							/////////////////////////////////////////////////////////
							// 2018.02.14  Background spectrum adjustment using current calibration info.
							//double []FWHM_NaI2_2=new double [] {1.2707811254,-1.5464537062}; //향후 검출기 별로 조정 필요
							double []FWHM=MainActivity.mDetector.MS.getFWHM();
							double []mCoefficient= MainActivity.mPrefDB.Get_Cali_ABC_From_pref();

							// reload background spectrum
							MainActivity.mDetector.Real_BG.Set_MeasurementDate(MainActivity.mPrefDB.Get_BG_Date_From_pref());
							//MainActivity.mDetector.Real_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(), mPrefDB.Get_BG_MeasuredAcqTime_From_pref());

							int K40_Ch=NcLibrary.PeakAna(MainActivity.mDetector.Real_BG.ToInteger(), FWHM,mCoefficient);

							int K40_New=(int)NcMath.ToPolynomial_FittingValue_AxisX1(1461.0, mCoefficient);

							//MainActivity.mDetector.Background_GainStabilization(K40_Ch, K40_New);
							if(K40_Ch>10)
							{
								double [] mSpc=new double [MainActivity.CHANNEL_ARRAY_SIZE];
								mSpc=NcMath.Background_GainStabilization(MainActivity.mDetector.Real_BG.ToDouble(),K40_Ch,K40_New);
								MainActivity.mDetector.Real_BG.Set_Spectrum(mSpc, (int)MainActivity.mDetector.Real_BG.Get_AcqTime());
							}


							mEventDBOper.Set_Crytal_Info(MainActivity.mPrefDB.Get_CryStal_Type_Number_pref());
							m_EventData.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);

							///////////////////////////////////////////////////////


							m_EventData.BG = MainActivity.mDetector.Real_BG;
							m_EventData.Set_StartTime();
							m_EventData.IsManualID = false;
						}

						m_EventData.MS.Accumulate_Spectrum(spc);
						m_EventData.mNeutron.Set_CPS(MainActivity.mDetector.mNeutron.Get_CPS());
						if (m_EventData.Doserate_MAX < MainActivity.mDetector.Get_Gamma_DoseRate_nSV())
							m_EventData.Doserate_MAX = MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
						m_EventData.Doserate_AVG += MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
						// --
			/*			prefDB = new PreferenceDB(getApplicationContext());
						IsoLib = new IsotopesLibrary(getApplicationContext());
						IsoLib.Set_LibraryName(prefDB.Get_Selected_IsoLibName());
						IsotopeInvisibleViewFirstFiveSecond(IsoLib);*/

						if(MainActivity.mIsoLib2 == null){
							MainActivity.mIsoLib2 = new IsotopesLibrary(getApplicationContext());
							MainActivity.mIsoLib2.Set_LibraryName(MainActivity.mPrefDB.Get_Selected_IsoLibName());
						}
						IsotopeInvisibleViewFirstFiveSecond(MainActivity.mIsoLib2);

						Log.i(TAG_SEQ, "SEQ: Measured, Acq.time - " + m_EventData.MS.Get_AcqTime());
						///////////////// �젙�웾 遺꾩꽍

						if (m_EventData.Detected_Isotope.isEmpty() == false)
							m_EventData.Detected_Isotope = NcLibrary.Quantitative_analysis(MainActivity.mDetector.MS,
									m_EventData.BG, m_EventData.Detected_Isotope, MainActivity.mDetector.IsSvUnit,
									MainActivity.mDetector.mPmtSurface, MainActivity.mDetector.mCrystal, MainActivity.mDetector.getGECoef());

						Set_IdResult_toViews(m_EventData.Detected_Isotope);
						Set_Info_OnSpectrumView_OnAnalysisView((int)m_EventData.MS.Get_AcqTime(), spc.Get_TotalCount());
						mSpectrumView.SetChArray(m_EventData.MS);
						mSpectrumView.invalidate();
						mTopAnal_Info.invalidate();

						sequentialMode.setText(getString(R.string.measurement_progress));

						String mSequenceRepeatCountStr = Integer.toString(mSequence_repeat_count + 1);

						String mSequence_repeat_GoalStr = Integer.toString(mSequence_repeat_Goal);

						sequentialCount.setText(mSequenceRepeatCountStr + " / " + mSequence_repeat_GoalStr);

						String mSequenceRepeatCountStr1 = Integer
								.toString((int)m_EventData.MS.Get_AcqTime() * mSequence_repeat_Goal);

						String mSequenceRepeatGoalStr1 = Integer.toString(mSequence_acqTime);

						mProgBar.invalidate();

						if (mDebug.IsDebugMode) {
							if (mDebug.IsSequenceCollect) {
								SpectrumArrayString.add(NcLibrary.SpectrumArrayToString(spc));

							}
						}

						if (mSequence_acqTime <= m_EventData.MS.Get_AcqTime()) {

							if (mDebug.IsDebugMode) {

								if (mDebug.IsSequenceCollect) {
									NcLibrary.onTextWritingSourceData("spectrum", SpectrumArrayString, m_EventData);
									SpectrumArrayString = new ArrayList<String>();
								}
							}

							Th232UnkownPeakRock(m_EventData.Detected_Isotope);

							Log.i(TAG_SEQ, "SEQ: wirte to DB, repeat cnt - " + (mSequence_repeat_count + 1));

							m_EventData.Set_EndEventTime();
							m_EventData.Doserate_AVG = m_EventData.Doserate_AVG / m_EventData.MS.Get_AcqTime();
							m_EventData.Neutron_AVG = m_EventData.mNeutron.Get_AvgCps();
							m_EventData.Neutron_MAX = m_EventData.mNeutron.Get_MaxCount();

							m_EventData.GPS_Latitude = mGpsInfo2.GetLat();
							m_EventData.GPS_Longitude = mGpsInfo2.GetLon();

							if (WriteEvent_toDB(m_EventData))
								Toast.makeText(getApplicationContext(), "#" + (mSequence_repeat_count + 1) + " saved in DB",
										Toast.LENGTH_SHORT).show();

							mAnalysisView.RemoveAll_IsotopeData();
							mSpectrumView.Clear_Found_Isotopes();

							mSPC.ClearSPC();
							mSpectrumView.SetChArray(mSPC);
							mSpectrumView.invalidate();
							mTopAnal_Info.invalidate();

							m_EventData = null;

							mSequence_repeat_count += 1;
							mPauseTime_ElapsedTime = 0;

							if (mSequence_repeat_count >= mSequence_repeat_Goal) {
								End_SequenceMode();


								// --===--
								Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
								intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
								// --===--
								Toast.makeText(getApplicationContext(), "Sequential Mode Completed", Toast.LENGTH_SHORT).show();


								SequentalModeRunningCancel();

								Log.i(TAG_SEQ, "SEQ:  Success");
							} else {
								Set_SeqModeInfo(mSequence_repeat_count + 1);
							}

							/*
							 * Intent send_gs = new
							 * Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);
							 * LocalBroadcastManager.getInstance(
							 * getApplicationContext()).sendBroadcast(send_gs);
							 */ }
						break;

					case TEST:

						Toast.makeText(getApplicationContext(), "제거", Toast.LENGTH_LONG).show();

						break;

					case 0:
						Show_Dlg(getResources().getString(R.string.email_transmit_success).toString());
						break;
					case 1:
						Show_Dlg(getResources().getString(R.string.email_transmit_failed).toString());
						break;
					case 2:
						Show_Dlg(getResources().getString(R.string.email_info_fail).toString());
						break;
					case 3:
						Show_Dlg(getResources().getString(R.string.internet_not).toString());
						break;

				}
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	};

	private boolean WriteEvent_toDB(EventData event) {
		// if(event.MS.Get_AcqTime() <=3) return false;

		event.GPS_Latitude = mGpsInfo2.GetLat();
		event.GPS_Longitude = mGpsInfo2.GetLon();

		event.mColumn_Version = EventDBOper.mDB.GetDBVersion();

		EventDBOper eventDB = new EventDBOper();

		int aaa3 = eventDB.GetEventCount();

		if (eventDB.WriteEvent_OnDatabase(event)) {

			try {
				File eventFile = new File(Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/"
						+ EventDBOper.DB_FILE_NAME + ".sql");
				if (eventFile.isFile())
					new SingleMediaScanner(getApplicationContext(), eventFile);
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			return true;
		} else
			return false;

		// Start_MediaScan();

	}

	private void End_SequenceMode() {
		try {
			Set_Invisible_Sequence_Contol(true);
			mSequence_repeat_count = 0;
			mPauseTime_ElapsedTime = mPauseTime;
			mIsSequenceMode = false;
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void Set_Info_OnSpectrumView_OnAnalysisView(int acqtime, int cps) {

		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();

		// mSpectrumView.Set_inform(getResources().getString(R.string.cps),
		// NcLibrary.Comma_Format(cps));
		/*
		 * mSpectrumView.Set_SoureId_inform(getResources().getString(R.string.
		 * cps), Cut_Decimal_Point(cps));
		 *
		 * mSpectrumView.Set_SoureId_inform2(getResources().getString(R.string.
		 * total_count), Cut_Decimal_Point(m_EventData.MS.Get_TotalCount()));
		 * mSpectrumView.Set_SoureId_inform3(getResources().getString(R.string.
		 * acq_time), String.valueOf(acqtime) + " / " + mManualID_GoalTime +
		 * "s");
		 */

		cpsTxt.setText(Cut_Decimal_Point(cps));

		//181105
		if(m_EventData.MS != null)
		{
			totalCountTxt.setText(Cut_Decimal_Point(m_EventData.MS.Get_TotalCount()));
		}

		acqTimeTxt.setText(String.valueOf(acqtime));

		acqTimeTxt2.setText(String.valueOf(mSequence_acqTime));

		// mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
		// NcLibrary.Comma_Format(cps));

		// --------------------------------------------------------------------------------------------

		mTopAnal_Info.Set_infor1(getResources().getString(R.string.date), calendar.get(Calendar.DAY_OF_MONTH) + "."
				+ (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR));
		mTopAnal_Info.Set_infor2(getResources().getString(R.string.time),
				date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
		mTopAnal_Info.Set_infor3(getResources().getString(R.string.acq_time), String.valueOf(acqtime) + "sec");
		mTopAnal_Info.Set_infor4(getResources().getString(R.string.cps), NcLibrary.Prefix(cps, true));

	}

	public Location Get_Location() {

		GpsInfo gps = new GpsInfo(SequentialActivity.this);
		if (gps.isGetLocation()) {
			return gps.getLocation();
		}
		return new Location(LocationManager.GPS_PROVIDER);
	}

	private void Set_IdResult_toViews(Vector<Isotope> result) {

		if (result == null)
			return;

		mSpectrumView.Clear_Found_Isotopes();
		mAnalysisView.RemoveAll_IsotopeData();
		for (int i = 0; i < result.size(); i++) {
			mSpectrumView.Add_Found_Isotope(result.get(i));
			if (!result.get(i).Class.matches(".*UNK.*"))
				mAnalysisView.Add_IsotopeData(result.get(i));
		}
		//mSpectrumView.invalidate();
		mAnalysisView.invalidate();
		mTopAnal_Info.Set_Log_GridCount(mAnalysisView.Get_Grid_Count());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.id_spc, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (SOURCE_ID_RESULT_MODE == false) {
			// menu.removeItem(menu.getItem(1).getItemId());
			// menu.removeItem(menu.getItem(2).getItemId());

		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (mIsManualID_mode == false) {
				// mReDrawTimer.cancel();
				LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
				finish();
			}
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.manualID_Cancel:

				SequentalModeRunningCancel();

				break;

		}
		return true;
	}

	public void Start_sequenceMode() {

		mSPC.ClearSPC();
		mIsSequenceMode = true;
		mPauseTime_ElapsedTime = mPauseTime;
		mSequence_repeat_count = 0;
		Set_Invisible_Sequence_Contol(false);

		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_DISABLE);

		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
	}

	private void Set_SeqModeInfo(int repeatCnt) {

		/*
		 * TextView repeatCntTV = (TextView)
		 * m_MainLayout.findViewById(R.id.tv_seq_repeat);
		 * repeatCntTV.setText(repeatCnt + "/" + mSequence_repeat_Goal);
		 */ }

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// m_MainLayout.onTouchEvent(event);
		// mSpectrumView.onTouchEvent(event);

		return super.onTouchEvent(event);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	/////////////////////////////////////////////////////////////////////
	public void Set_Invisible_ManualID_Contol(boolean IsInvisible) {

		/*
		 * int Weight = 0; if (IsInvisible) Weight = 0; else Weight = 1;
		 *
		 * FrameLayout SpcLayout = (FrameLayout)
		 * findViewById(R.id.frameLayout1); LinearLayout.LayoutParams Param =
		 * (LinearLayout.LayoutParams) SpcLayout.getLayoutParams(); Param.weight
		 * = Weight; SpcLayout.setLayoutParams(Param);
		 *
		 * if (Weight == 0) { // LinearLayout control_lyaout = (LinearLayout) //
		 * m_MainLayout.findViewById(R.id.frameLayout2); //
		 * control_lyaout.setVisibility(LinearLayout.GONE); } else {
		 * LinearLayout control_lyaout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.frameLayout2);
		 * control_lyaout.setVisibility(LinearLayout.VISIBLE);
		 *
		 * LinearLayout seqInfo_Layout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.layout_sequenceInfo);
		 * seqInfo_Layout.setVisibility(LinearLayout.GONE); }
		 *
		 * m_MainLayout.invalidate();
		 */
	}

	private void Set_Invisible_Sequence_Contol(boolean IsVisible) {
		/*
		 * int Weight = 0; if (IsVisible) Weight = 0; else Weight = 1;
		 *
		 * FrameLayout SpcLayout = (FrameLayout)
		 * m_MainLayout.findViewById(R.id.frameLayout1);
		 * LinearLayout.LayoutParams Param = (LinearLayout.LayoutParams)
		 * SpcLayout.getLayoutParams(); Param.weight = Weight;
		 * SpcLayout.setLayoutParams(Param);
		 *
		 * if (Weight == 0) { // LinearLayout control_lyaout = (LinearLayout) //
		 * m_MainLayout.findViewById(R.id.layout_sequenceInfo); //
		 * control_lyaout.setVisibility(LinearLayout.GONE); } else {
		 * LinearLayout control_lyaout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.layout_sequenceInfo);
		 * control_lyaout.setVisibility(LinearLayout.VISIBLE);
		 *
		 * LinearLayout seqInfo_Layout = (LinearLayout)
		 * m_MainLayout.findViewById(R.id.frameLayout2);
		 * seqInfo_Layout.setVisibility(LinearLayout.GONE); }
		 * m_MainLayout.invalidate();
		 */}

	private void Set_Spectrum_Y_toEnergy() {

		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
	}

	@Override
	public void onBackPressed() {

		SequentalModeRunningCancel();
		//NcLibrary.SaveText1("onBackPressed4\n","test");
		return;
	}

	public void onCreatePart() {

		MainActivity.ACTIVITY_STATE = Activity_Mode.SEQUENTAL_MODE_RUNNING;
		mSPC.ClearSPC();
		try {

			Spectrum spcdata = (MainActivity.mDetector.mGamma_Event == null) ? MainActivity.mDetector.MS.ToSpectrum()
					: MainActivity.mDetector.mGamma_Event.MS.ToSpectrum();
			if (spcdata != null)
				mSPC.Set_Spectrum(spcdata);

			mIsSvUnit = MainActivity.mDetector.IsSvUnit;

			mSequence_acqTime = MainActivity.mPrefDB.Get_SequenceMode_acqTime_From_pref();
			mSequence_repeat_Goal = MainActivity.mPrefDB.Get_SequenceMode_Repeat_From_pref();
			mPauseTime = MainActivity.mPrefDB.Get_SequenceMode_PauseTime_From_pref();

			mPauseTimeElapsedDecreaseTime = mPauseTime;

			// String mPauseTime1;
			// mPauseTime1 = getIntent().getStringExtra(ACTIVTY);

			int a;
			a = 0;

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
		// ---------------------

		mClassColor.clear();
		mClassColor.add(Color.rgb(150, 24, 150));
		mClassColor.add(Color.rgb(27, 23, 151));
		mClassColor.add(Color.rgb(44, 192, 185));
		mClassColor.add(Color.rgb(10, 150, 20));
		mClassColor.add(Color.RED);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_EVENT);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_EN_CALIBRATION);
		filter.addAction(MainBroadcastReceiver.MSG_REMEASURE_BG);
		filter.addAction(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_EVENT_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_NEUTRON);
		filter.addAction(MainBroadcastReceiver.MSG_TAB_SOURCE_ID);
		filter.addAction(MainBroadcastReceiver.MSG_SPEC_VIEWFILPPER);

		filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_MainLayout = (LinearLayout) inflater.inflate(R.layout.sequential_spectrum, null);

		///////////////

		m_AnalysisLayout = (LinearLayout) inflater.inflate(R.layout.iso_analysis, null);
		setContentView(m_AnalysisLayout);
		/////////

		LinearLayout layout = (LinearLayout) m_MainLayout.findViewById(R.id.AdLayout);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mAnalysisView = new ScView_Ad(this);
		layout.addView(mAnalysisView, p);
		mAnalysisView.Set_IsSv_Unit(mIsSvUnit);
		////

		AttributeSet attrs = null;
		mTopAnal_Info = new Analysis_TopInfor(this, attrs);
		mTopAnal_Info = (Analysis_TopInfor) m_MainLayout.findViewById(R.id.Iso_analysis);
		mTopAnal_Info.Set_Log_GridCount(mAnalysisView.Get_Grid_Count());
		mTopAnal_Info.Set_Class_Color(mClassColor);
		mTopAnal_Info.Set_Doserate_Unit(mIsSvUnit);

		LinearLayout control_lyaout = (LinearLayout) m_MainLayout.findViewById(R.id.frameLayout2);
		control_lyaout.setVisibility(LinearLayout.VISIBLE);

		Button seq_cancel = (Button) m_MainLayout.findViewById(R.id.btn_seq_Cancel);
		seq_cancel.setOnTouchListener((new Button.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					End_SequenceMode();

					mAnalysisView.RemoveAll_IsotopeData();
					mSpectrumView.Clear_Found_Isotopes();

					//mSpectrumView.invalidate();
					mAnalysisView.invalidate();

					m_EventData = null;
					mSPC.ClearSPC();

					// --===--
					Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
					intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
					// --===--

					Toast.makeText(getApplicationContext(), getResources().getString(R.string.cancel),
							Toast.LENGTH_LONG).show();
					return true;
				}

				return false;
			}
		}));

		mSPC.ClearSPC();

		// -- End Manual Id Picker
		mSpectrumView = (SpectrumView) m_MainLayout.findViewById(R.id.IDspectrum);
		mSpectrumView.setChArraySize(mSPC.Get_Ch_Size());
		mSpectrumView.SetChArray(mSPC);
		mSpectrumView.LogMode(true);
		mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
		mSpectrumView.invalidate();

		setContentView(m_MainLayout);

		flipper = (ViewFlipper) findViewById(R.id.flipper);

		IDspectrum = (View) findViewById(R.id.IDspectrum);

		sequentialMode = (TextView) findViewById(R.id.sequential_mode);
		sequentialCount = (TextView) findViewById(R.id.sequential_count);

		Iso_analysis = (View) findViewById(R.id.Iso_analysis);

		mProgBar = (ProgressBar) m_MainLayout.findViewById(R.id.SetupSpcSrc_ProgressBar);

		mFrameLayout = (FrameLayout) m_MainLayout.findViewById(R.id.frameLayout1);
		mFrameLayout.setOnTouchListener(this);

		// mSpectrumView.setOnTouchListener(this);
		cpsTxt = (TextView) m_MainLayout.findViewById(R.id.cpsTxt);

		totalCountTxt = (TextView) m_MainLayout.findViewById(R.id.totalCountTxt);

		acqTimeTxt = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt);

		acqTimeTxt2 = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt2);

		Start_sequenceMode();
		filpperImgView = (ImageView) findViewById(R.id.filperImgView);

		filpperImgView.setOnTouchListener(mTouchEvent);
	}

	@Override
	protected void onResume() {
		// DoubleClickRock = Activity_Mode.EXCUTE_MODE;

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
		MainActivity.tabHost.getTabWidget().getChildAt(0).setOnTouchListener(this);
		MainActivity.tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);
		MainActivity.tabHost.getTabWidget().getChildAt(2).setOnTouchListener(this);

		super.onResume();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	public void RightMove() {

		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
		flipper.showPrevious();
		filpperImgView.setImageResource(R.drawable.right);
	}

	public void LeftMove() {

		flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
		flipper.showNext();
		filpperImgView.setImageResource(R.drawable.left);
	}

	public void DropDownAnimation() {

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.drop_down);
		linearLayout.startAnimation(anim);

	}
	// video photo function

	public class Img_Video_Array extends BaseAdapter {

		LayoutInflater inflater;

		int GalItemBg;
		private Context cont;
		TextView text1;

		public Img_Video_Array(Context c) {
			cont = c;

			TypedArray typArray = obtainStyledAttributes(R.styleable.GalleryTheme);

			GalItemBg = typArray.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);

			typArray.recycle();

		}

		public int getCount() {

			return mThumnail.size();

		}

		public Object getItem(int position) {

			return position;

		}

		public long getItemId(int position) {

			return position;

		}

		public View getView(int position, View convertView, ViewGroup parent) {

			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// ImageView imgView = new ImageView(cont);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.id_result_addview, null);
			}
			text1 = (TextView) convertView.findViewById(R.id.txt01);
			ImageView imgView = (ImageView) convertView.findViewById(R.id.imgView1);
			LinearLayout mLinearLayout = (LinearLayout) convertView.findViewById(R.id.linearlayout1);

			imgView.setLayoutParams(new LinearLayout.LayoutParams(250, 187));
			text1.setText(mTotalTxt.get(position));
			imgView.setImageBitmap(mThumnail.get(position));

			imgView.setScaleType(ImageView.ScaleType.FIT_XY);

			return convertView;

		}

	}

	public void View_Filpper() {

		switch (swicthCount) {
			case 0:
				LeftMove();
				// filperMove = true;
				// IDspectrum.setVisibility(View.INVISIBLE);
				// Iso_analysis.setVisibility(View.VISIBLE);

				swicthCount = 1;
				break;

			case 1:
				RightMove();

				// filpperImgView.set
				// filperMove = false;
				// IDspectrum.setVisibility(View.VISIBLE);
				// Iso_analysis.setVisibility(View.INVISIBLE);

				swicthCount = 0;
				break;

			default:

				break;
		}

	}

	public void RadresponderExucute() {

		ArrayList<String> mEventID = new ArrayList<String>();
		ArrayList<String> mDate = new ArrayList<String>();
		ArrayList<String> mAcqTime = new ArrayList<String>();
		ArrayList<String> mId = new ArrayList<String>();
		ArrayList<String> mGamma = new ArrayList<String>();
		ArrayList<String> mTime = new ArrayList<String>();
		ArrayList<String> mManual_ID = new ArrayList<String>();

		ArrayList<String> mEndTime = new ArrayList<String>();
		ArrayList<String> mStartTime = new ArrayList<String>();

		ArrayList<String> mSourceName = new ArrayList<String>();

		ArrayList<String> mLatitude = new ArrayList<String>();
		ArrayList<String> mLongitude = new ArrayList<String>();

		ArrayList<String> mDoserate_S = new ArrayList<String>();
		ArrayList<String> mConfidence_Level = new ArrayList<String>();

		ArrayList<String> mComment = new ArrayList<String>();

		mDate.add(m_EventData.EventData);
		mAcqTime.add(String.valueOf(m_EventData.MS.Get_AcqTime()));
		mGamma.add(m_EventData.Doserate_AVGs);
		mTime.add(m_EventData.StartTime);
		mManual_ID.add((m_EventData.Event_Detector));// .IsManualID==true)?"MANUAL
		// ID":"");
		mEventID.add(String.valueOf(mEventNumber + 1));

		// radreponder value Add Part

		Vector<Isotope> Id = m_EventData.Detected_Isotope;
		String temp = "";
		String temp2 = "";
		String temp3 = "";
		String temp4 = "";

		if (Id == null || Id.size() == 0) {
			temp = "None";
			temp2 = "None";
			temp3 = "None";
			temp4 = "None";
		} else {
			for (int k = 0; k < Id.size(); k++) {
				String[] DoseRate_S = Id.get(k).DoseRate_S.split(" ");
				if (k == Id.size() - 1) {

					temp += Id.get(k).isotopes;
					temp2 += DoseRate_S[0];
					temp3 += Double.toString(Id.get(k).Confidence_Level);
					temp4 += Id.get(k).isotopes;
					break;

				} else {
					temp += Id.get(k).isotopes + ", ";
					temp2 += DoseRate_S[0] + ",";
					temp3 += Double.toString(Id.get(k).Confidence_Level) + ",";
					temp4 += Id.get(k).isotopes + ",";
				}

			}
		}
		mId.add(temp);
		mDoserate_S.add(temp2);
		mConfidence_Level.add(temp3);
		mSourceName.add(temp4);

		mComment.add(m_EventData.Comment);

		mStartTime.add(m_EventData.EventData + " " + m_EventData.StartTime);

		mEndTime.add(m_EventData.EventData + " " + m_EventData.EndTime);

		mLatitude.add(Double.toString(m_EventData.GPS_Latitude));
		mLongitude.add(Double.toString(m_EventData.GPS_Longitude));

		Intent intent = new Intent(SequentialActivity.this, RadresponderActivity.class);
		intent.putExtra(RadresponderActivity.GPS_LAT, mLatitude.get(0));
		intent.putExtra(RadresponderActivity.GPS_LONG, mLongitude.get(0));

		// Toast.makeText(getApplicationContext(), mLatitude.get(0) + "," +
		// mLongitude.get(0), 1).show();
		// String[] mGamma1 = mGamma.get(0).split(" ");

		intent.putExtra(RadresponderActivity.DOSERATE_TYPE, "uSv/h");
		intent.putExtra(RadresponderActivity.COLLECTION_DATE, mEndTime.get(0));
		intent.putExtra(RadresponderActivity.START_TIME, mStartTime.get(0));

		intent.putExtra(RadresponderActivity.START_TIME_NOT_UTC, mStartTime.get(0));
		intent.putExtra(RadresponderActivity.STOP_TIME_NOT_UTC, mEndTime.get(0));

		intent.putExtra(RadresponderActivity.SOURCE_NAME_S, mSourceName.get(0));
		intent.putExtra(RadresponderActivity.DOSERATE_S, mDoserate_S.get(0));
		intent.putExtra(RadresponderActivity.LEVEL_S, mConfidence_Level.get(0));

		intent.putExtra(RadresponderActivity.COMMENT_TITLE, mComment.get(0));

		startActivity(intent);

	}

	public void ReachBackExucute() {

		mPrgDlg = new ProgressDialog(mContext);
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SequentialActivity.this);
		dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
		dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event));
		dialogBuilder.setPositiveButton(getResources().getString(R.string.transmit),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

						mPrgDlg.setTitle(getResources().getString(R.string.transmit_N42));
						mPrgDlg.setMessage("Sending...");
						mPrgDlg.show();
						try {
							Thread thread = new Thread() {

								@Override
								public void run() {

									super.run();

									if (isNetworkOnline() == false) {
										mHandler.sendEmptyMessage(3);
										mPrgDlg.dismiss();
										return;
									}

									PreferenceDB pref = new PreferenceDB(SequentialActivity.this.mContext);
									if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) {
										mHandler.sendEmptyMessage(2);
										mPrgDlg.dismiss();
										return;
									}

									String sender = pref.Get_sender_email();
									String sender_pw = pref.Get_sender_pw();
									String sender_server = pref.Get_sender_Server();
									String sender_port = pref.Get_sender_Port();
									String recv_mail = pref.Get_recv_email();

									Mail m = new Mail(sender, sender_pw, sender_server, sender_port);

									// Array of emailIds where you want to
									// sent
									String[] toArr = new String[1];
									toArr[0] = recv_mail;
									m.setTo(toArr);

									// Your emailid(from)
									m.setFrom(sender);
									m.setSubject("SAM III PeakAbout - Event Data");
									m.setBody("From " + m_EventData.mInstrument_Name);
									try {
										m.addAttachment(NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData),
												m_EventData.EventData + "_" + m_EventData.StartTime + "("
														+ m_EventData.mInstrument_Name + ").xml");
									} catch (Exception e1) {
										NcLibrary.Write_ExceptionLog(e1);
									}

									try {
										if (m.send()) {
											mPrgDlg.dismiss();
											mHandler.sendEmptyMessage(0);
											Log.v("Forgot Password mail", "Success");

										} else {
											mPrgDlg.dismiss();
											mHandler.sendEmptyMessage(1);
											Log.v("Forgot Password mail", "Not Success");
										}
									} catch (Exception e) {
										NcLibrary.Write_ExceptionLog(e);
										mPrgDlg.dismiss();
										mHandler.sendEmptyMessage(2);
										Log.e("MailApp", "Could not send email", e);
									}

								}

							};

							thread.start();
						} catch (Exception e) {

							NcLibrary.Write_ExceptionLog(e);
						}

					}
				});
		dialogBuilder.setNegativeButton("Cancel", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();

	}


	public void SequentalModeRunningCancel() {

		//190102 추가
		//MainActivity.SendU4AA();

		MainActivity.tabHost.setCurrentTab(0);
		Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_ENABLE);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

		//Set_Invisible_ManualID_Contol(true);
		mProgBar.Set_Value(0);
		mProgBar.invalidate();

		m_EventData = null;
		mSPC.ClearSPC();
		mSequenceTotalCount = 0;

		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_BACK);
		filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);
		filter.addAction(MainBroadcastReceiver.MSG_SEQUENTAL_MODE_RUNNING_START);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);
		setContentView(R.layout.black_background);

		//190102 추가
		//NcLibrary.U2AATimer();


	}

	public boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
		return status;

	}

	private void Show_Dlg(String Message) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SequentialActivity.this);
		// dialogBuilder.setTitle(Title);
		dialogBuilder.setMessage(Message);
		dialogBuilder.setNegativeButton("OK", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

			case KeyEvent.KEYCODE_DPAD_LEFT: {
				// event.startTracking();

				if (MainActivity.ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {
					View_Filpper();
					return true;
				} else {
					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

						KeyExecute(KeyEvent.KEYCODE_DPAD_LEFT);
						return false;
					}
				}

				// Toast.makeText(getApplicationContext(), "key right", 1).show();

				return true;
			}
			case KeyEvent.KEYCODE_DPAD_RIGHT: {
				// event.startTracking();
				if (MainActivity.ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {
					View_Filpper();

					return true;
				} else {
					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

						KeyExecute(KeyEvent.KEYCODE_DPAD_RIGHT);
						return false;
					}

				}
				return true;
			}
			case KeyEvent.KEYCODE_DPAD_UP: {
				if (MainActivity.ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {

					return true;
				} else if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

					KeyExecute(KeyEvent.KEYCODE_DPAD_UP);
					return false;
				}

				return true;
			}
			case KeyEvent.KEYCODE_DPAD_DOWN: {
				if (MainActivity.ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {

					return true;
				} else if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

					KeyExecute(KeyEvent.KEYCODE_DPAD_DOWN);
					return false;
				}

				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	public void KeyExecute(final int keyvalue) {

		new Thread(new Runnable() {

			public void run() {

				new Instrumentation().sendKeyDownUpSync(keyvalue);

			}
		}).start();

		DoubleClickRock = Activity_Mode.UN_EXCUTE_MODE;

		TimerTask mTask = new TimerTask() {

			@Override

			public void run() {

				DoubleClickRock = Activity_Mode.EXCUTE_MODE;

			}

		};
		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 200);

	}

	public String Cut_Decimal_Point(int value) {

		float sum = 0;
		String sumStr = "";
		if (value > 1000) {

			sum = (float) value / 1000;
			sumStr = String.format("%.2f", sum);

			sumStr = sumStr + "k";
		} else {

			sumStr = Integer.toString(value);
		}

		return sumStr;
	}

	public boolean onTouch(View v, MotionEvent event) {

		if (v.getContext() == MainActivity.tabHost.getTabWidget().getChildAt(1).getContext()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mPreTouchPosX = (int) event.getX();
			}

			if (event.getAction() == MotionEvent.ACTION_UP) {
				int nTouchPosX = (int) event.getX();

				if (nTouchPosX < mPreTouchPosX) {
					LeftMove();
				} else if (nTouchPosX > mPreTouchPosX) {
					RightMove();
				}
			}

			return true;
		}
		return false;
	};

	private OnTouchListener mTouchEvent = new OnTouchListener()

	{
		public boolean onTouch(View v, MotionEvent event)

		{

			/*
			 * if (v.getContext() ==
			 * MainActivity.tabHost.getTabWidget().getChildAt(1).getContext()) {
			 */
			if (event.getAction() == MotionEvent.ACTION_DOWN) {

			}
			if (event.getAction() == MotionEvent.ACTION_UP) {

				switch (swicthCount) {
					case 0:
						LeftMove();
						// filperMove = true;
						// IDspectrum.setVisibility(View.INVISIBLE);
						// Iso_analysis.setVisibility(View.VISIBLE);
						filpperImgView.setImageResource(R.drawable.left);
						swicthCount = 1;
						break;

					case 1:
						RightMove();

						// filpperImgView.set
						// filperMove = false;
						// IDspectrum.setVisibility(View.VISIBLE);
						// Iso_analysis.setVisibility(View.INVISIBLE);
						filpperImgView.setImageResource(R.drawable.right);

						swicthCount = 0;
						break;

					default:

						break;
				}

			}
			/* } */

			return true;
		}
	};

	private void IsotopeInvisibleViewFirstFiveSecond(IsotopesLibrary IsoLib) {
		m_EventData.Detected_Isotope = IsoLib.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG);

	/*	180321
	 * if (m_EventData.MS.Get_AcqTime() <= 5 & m_EventData.Detected_Isotope.size() > 2) {
			Isotope iso1 = m_EventData.Detected_Isotope.get(0);
			Isotope iso2 = m_EventData.Detected_Isotope.get(1);

			m_EventData.Detected_Isotope.clear();
			m_EventData.Detected_Isotope.add(iso1);
			m_EventData.Detected_Isotope.add(iso2);
		}

		if (m_EventData.MS.Get_AcqTime() <= 5) {

			m_EventData.Detected_Isotope.clear();
		}*/
	}

	private boolean Th232UnkownPeakRock(Vector<Isotope> FoundIsotopes) {

		boolean FoundIsotopesTh232 = false;

		for (int i = 0; i < FoundIsotopes.size(); i++) {
			if (FoundIsotopes.get(i).isotopes.matches("Th-232")) {
				FoundIsotopesTh232 = true;
				break;
			}
		}

		if (FoundIsotopesTh232 = true) {

			return true;
		}
		return false;

	}

}
