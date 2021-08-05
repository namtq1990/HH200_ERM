
package android.HH100;

import android.HH100.AutoCalibActivity.AutoCalbration;
import android.HH100.CcswService.MappingData;
import android.HH100.Control.BatteryView;
import android.HH100.Control.GpsInfo;
import android.HH100.Control.GpsInfo2;
import android.HH100.Control.GuageView;
import android.HH100.Control.RealActivitySpectrumView;
import android.HH100.DB.DB_Ver;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.NormalDB;
import android.HH100.DB.PreferenceDB;
import android.HH100.Dialog.DeviceListActivity;
import android.HH100.Dialog.LoginDlg;
import android.HH100.IDspectrumActivity.IDspectrumValue;
import android.HH100.Identification.FindPeaksN;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Service.Guide;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Service.VersionUpdate;
import android.HH100.Structure.Detector;
import android.HH100.Structure.Detector.HwPmtProperty_Code;
import android.HH100.Structure.EventData;
import android.HH100.Structure.GCData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.ReadDetectorData;
import android.HH100.Structure.SingleMediaScanner;
import android.HH100.Structure.Spectrum;
import android.HH100.erm_debug.ErmDataManager;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import Debug.Debug;
import Debug.Log_Setting;
import NcLibrary.Coefficients;

import static Debug.Debug.isSWDebug;
import static android.HH100.MainActivity.Signal.MESSAGE_GQ_HW;

//MESSAGE_READ_DETECTOR_DATA
//뷰플리퍼
//UIPart1
//UIPart2
//핸들러부분
//메뉴부분
//리스너부분

public class MainActivity extends TabActivity
		implements TabHost.TabContentFactory, OnTabChangeListener, View.OnTouchListener {
	/** Called when the activity is first created. */

	//191118 수정
	public static IsotopesLibrary mIsoLib2;

	EventDBOper mEventDBOper;

	boolean sendCS = false; //181008 calibration후 send cs여부 확인용
	int cntSendCS = 0; //181008 sendCS  총 3번 보냄
	public static boolean sendCali = false; //환경설정에서 메인으로올대 판단여부
	public static final int CHANNEL_ARRAY_SIZE = 1024;
	public static final String DEVICE_NAME = "SAM 950";

	public static final boolean MAPPING_VERSION = false;
	public static final boolean D = false;
	public static final boolean E = false;
	private static final String TAG_RecvData = "RecvData";
	private static final String TAG = "Main Activity";

	public static Log_Setting mLog = new Log_Setting();

	public static int mAUTO_GAIN_result = 0;
	//public  static ArrayList<RadresponderAdapter3.Radresponder> radresponderList = new ArrayList<>();
	public  static  ArrayList<Radresponder> radresponderList = new ArrayList<>();
	public static boolean radresponderCheck = false;

	public static Debug mDebug = new Debug();
	int SpecCnt = 0;
	public static boolean openMenu = false; // 180712 메뉴키 활성화 해제 안되는 현상 방지
	public static boolean isU4AA = false;

	public static boolean admin = false; //20.02.04 추가

	interface MainMsg {
		public static final int CHANNEL_ARRAY_SIZE = 1024;
		public static final String DEVICE_NAME = "SAM";

		public boolean MAPPING_VERSION = false;
		public boolean D = false;
		public boolean E = false;
		public String TAG_RecvData = "RecvData";
		public String TAG = "Main Activity";
		public int MESSAGE_STATE_CHANGE = 1;
		public int MESSAGE_READ_GAMMA = 21;
		public int MESSAGE_READ_NEUTRON = 22;
		public int MESSAGE_READ_GM = 23;
		public int MESSAGE_READ_LA = 24;
		public int MESSAGE_READ_BATTERY = 25;
		public int MESSAGE_READ_DETECTOR_DATA = 26; //j5및(6.0이전버전) 3095바이트로 들어오는  하드웨어 처리
		public int MESSAGE_SAVE_EVENT = 27;
		public int MESSAGE_MEDIA_SCAN = 28;
		public int MESSAGE_READ_GC = 29;
		public int MESSAGE_USB_READ_GC = 30;
		public int MESSAGE_NEUTRON_RECV = 31;
		public int MESSAGE_ORIGINAL_TOAST = 32;
		public int MESSAGE_READ_SERIAL_DATA = 33;
		public int MESSAGE_READ_DETECTOR_DATA_J3 = 34; //j3 데이터 (400바이트*7 ) + 시간데이터 처리
		public int MESSAGE_WRITE = 3;
		public int MESSAGE_CONNECTED_DEVICE_INFO = 4;
		public int MESSAGE_TOAST = 5;
		public int MESSAGE_SHUTDOWN = 6;

	}

	static final int REQUEST_CONNECT_DEVICE = 1;

	private static final int REQUEST_ENABLE_BT = 3;
	private static final int AUTO_CALIB_FINISH = 55;
	private static final int FINISH_CALIB_BG = 56;
	static final int FINISH_SETUP_PREF = 57;
	public static final int RESULT_LOGIN = 42;

	public static final byte[] MESSAGE_END_HW = { 'U', '4', 'A', 'A' };
	public static final byte[] MESSAGE_START_HW = { 'U', '2', 'A', 'A' };

	private final static int DEAFALUT_GAIN_SEC = 10;// 0;
	private final int GAIN_THRESHOLD = 150;
	private final int GAIN_EVERY_SEC = 10; //
	private static int mGain_Sec = DEAFALUT_GAIN_SEC; // 기본 gain stabilization
	public static MainService mService = null;
	public static MainUsbService mMainUsbService = null;
	public static final String TOAST = "toast";
	private final static float TAB_TEXT_SIZE = 15.5f;
	private final static String TAB_ENABLE_TEXT_COLOR = "#ffffff";
	public static boolean press = false;
	private BluetoothAdapter mBTAdapter;

	public static boolean MANUAL_ID_STATUS = false;

	public static boolean CONNECT_CHECK = false;

	GuageView m_GammaGuage_Panel;

	public static int mLogin = LoginDlg.LOGIN_USER;

	public static Detector mDetector = new Detector();
	int count = 0;

	public static PreferenceDB mPrefDB = null;

	private RealActivitySpectrumView mFinder;

	private ImageView m_Bluetooth_Status, mDeviceIcon;

	// private ViewFlipper mMainFlipper;

	private int mPreTouchPosX;

	private int[] mStbChannel = new int[1024];

	public boolean AUTO_FAIL_CODE_10 = false;

	public static NormalDB mNormalDB;
	public static EventDBOper mEventDB;

	public static MediaPlayer mAlarmSound;

	private ProgressDialog mProgressDialog = null;
	private static Context mContext;
	public static int Logcount = 0;

	int TabMoveCount = 0;

	int AutoCalibrationCnt = Activity_Mode.EXCUTE_MODE;

	CcswService mCCSW_Service = null;

	int mBluetoothImage_flag = 0;

	private boolean mVibrating = false;

	public  static AudioManager audio;

	int tabswitch = 0;

	public static int eventId;

	public static int ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;

	public static int DoubleClickRock = Activity_Mode.EXCUTE_MODE;

	public static int DoubleClickRock2 = Activity_Mode.EXCUTE_MODE;

	ArrayList<String> mGainValue = new ArrayList<String>();

	public static Boolean mCurrentConnectMode = true;
	public static Boolean mChangeConnectMode = true;

	public static int mBattary = 0;

	private String specstr1 = "";
	private String specstr = "";


	private enum ConnectType {

		Bluetooth, USB
	};

	public static boolean menuCalib = false;

	private ConnectType mCnnctMode = ConnectType.USB;
	// ------------

	public LocationManager mLocationManager;
	// Location m_nowLocation = null;

	// Usb And Blutooth

	// Tab

	// 상단바 선언

	TextView Paired, Library, Alarm, Battery, GainstabilizattonTxt;

	int mGainCnt = 0;

	public static String PairedStr;

	public static String LibraryStr;

	public static String AlarmStr;

	public static String BatteryStr;

	public static Intent intent;
	public static TabHost.TabSpec spec;
	public static TabHost tabHost;

	public static TabWidget TabWidget;

	private BatteryView mBatteryProgBar = null;

	TimerTask ShutDownTimeTask;
	Timer ShutDownTimer;

	TimerTask mTask;
	Timer mTimer;

	public static Context mainContext;
	// YKIM 2018.2.19
	// rest time setup
	public int mGain_restTime_under1 = 60; // stabilization rest time if K40diff <= 1%:150
	public int mGain_restTime_under2 = 30; // stabilization rest time if K40diff <= 2%:60
	public int mGain_restTime_over2 = 10; // stabilization rest time if K40diff > 2%:10

	public static final String FilenameCaliInfo = "HWCali.txt"; // New calibration info
	public static final String FilenameCurCaliInfo = "CurrentHWCali.txt"; // from HH200 HW
	public static final String FilenameInstrumentInfo = "InstrumentModel.txt"; // from InstrumentModel HH200 HW

	// 하드웨어키 테스트 선언
	public static final int INPUT_HARDWARE_KEY = 100;

	public static final int HW_KEY_SHORT = 101;
	public static final int HW_KEY_LONG = 102;
	public static final int HW_KEY_DOUBLE = 103;

	public static final int TAB_END_NUMBER = 3;
	public static final int TAB_FIRST_NUMBER = 0;

	public static String strMsg = "Real Time";

	public static int FirstActivityCurrentTab = 0;

	public interface Tab_Name {

		public static int Reatime = 0;
		public static int MenualID = 1;

		public static int SequentialMode = 2;
		public static int EnCalibration = 3;

		public static String RealTime_Str = "Real Time";
		public static String ManualID_Str = "Manual ID.";

		public static String SequentialMode_Str = "Sequential Mode";

		public static String En_Calibration_Str = "En.Calibration";
		public static String Background_Str = "Background";
	};

	public interface HW_Key_Type {

		public static int SHORTPRESS = 101;
		public static int LONGPRESS = 102;
		public static int DOUBLECLICK = 103;

	};

	public interface HW_Key {

		public int Left = 76;
		public int Right = 82;
		public int Up = 85;
		public int Down = 68;
		public int Enter = 77;
		public int Back = 66;

	};

	public interface Signal {

		public byte[] MESSAGE_END_HW = { 'U', '4', 'A', 'A' };
		public byte[] MESSAGE_START_HW = { 'U', '2', 'A', 'A' };
		public byte[] MESSAGE_GS_HW = { 'G', 'S' };
		public byte[] MESSAGE_GQ_HW = { 'G', 'Q' };
		public byte[] MESSAGE_SN_HW = { 'S', 'N' };
	};


	public interface Activity_Mode {

		public int SOURCE_ID_MAIN = 1200;

		public int SOURCE_ID_RUNNING = 1201;
		public int SOURCE_ID_RESULT = 1202;
		public int SOURCE_ID_RESULT_CAMERA = 1203;
		public int SEQUENTAL_MODE_RUNNING = 1204;

		public int BACKGROUND_MAIN = 1303;
		public int BACKGROUND_RUNNING = 1304;
		public int CALIBRATION_MAIN = 1405;
		public int CALIBRATION_RUNNING = 1406;

		public int REALTIME_MAIN = 1507;
		public int FIRST_ACTIVITY = 1608;
		public int NOT_FIRST_ACTIVITY = 1609;

		public int EVENTLOG_LIST_MAIN = 1711;

		public int SETUP_MAIN = 1812;

		public int AUTO_CALIBRATION = 1900;

		public int ID_RESULT_UN_CHECK_SELECT_MODE = 0;

		public int UN_EXCUTE_MODE = 1;

		public int EXCUTE_MODE = 0;

	}

	public interface Focus {

		public int ID_RESULT_MENU_A = 2200;
		public int ID_RESULT_MENU_B = 2201;
		public int ID_RESULT_MENU_C = 2202;

		public int ID_RESULT_MENU_A_ENTER = 2210;
		public int ID_RESULT_MENU_B_ENTER = 2211;
		public int ID_RESULT_MENU_C_SUB = 2212;

		public int ID_RESULT_MENU_C_PHOTO = 2220;
		public int ID_RESULT_MENU_C_VIDEO = 2221;
		public int ID_RESULT_MENU_C_VOICE = 2222;
		public int ID_RESULT_MENU_C_REMOVE_BTN = 2223;

	}

	public static int ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.FIRST_ACTIVITY;

	public static int ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

	// 1023 비정상 종료 (죽을때) 캐치하는 핸들러.
	private Thread.UncaughtExceptionHandler mHnderUncaughException;

	int realTimeSwitch = 0;

	// Activity 구분 선언

	public String ActivityCheck = "";

	int HW_Key_Double_Rock = 0;

	public static Activity MainActivity1;

	// GC Test
	public static String NewGC;
	int mToastCount = 0;
	public static int mCount = 0;
	public static int mSaveCount = 0;
	public static ArrayList<String> StrArraylist;
	// 리스너부분

	public static String autoTime1 = "";
	public static String gainTime1 = "";

	public boolean ISSendU4AA = false;

	public ArrayList<Double> mNeutron = new ArrayList<Double>();

	// 180918
	byte[] ss2;
	TimerTask mSendGSTask;
	int mTimeTaskcount = 0;;
	Timer mSendGSTimer;

	public static boolean wifiConnected = false;

	//190102 추가
	/*public static TimerTask U2AATimerTask = null;
	public static Timer U2AATimer = null;*/

	LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {

			// m_nowLocation = location;
			if (MAPPING_VERSION & mCCSW_Service != null) {
				if (mCCSW_Service.Is_Connected()) {// set
					// location
					Location loc = location;
					MappingData data = new MappingData();
					data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
					data.InstrumentName = mDetector.InstrumentModel_Name;
					data.InstrumentMacAddress = mDetector.InstrumentModel_MacAddress;
					data.Doserate = mDetector.Get_Gamma_DoseRate_nSV();
					data.CPS = mDetector.MS.Get_TotalCount();

					mCCSW_Service.Set_Data(null, data);
				}
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	// ----------------------------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------------------------

	public class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, android.content.Intent intent) {

			try {
				PreferenceDB prefDB = new PreferenceDB(getApplicationContext());

				String action = intent.getAction();
				switch (action) {

					case MSG_MANUAL_ID:
						int status = intent.getIntExtra(DATA_MANUAL_ID_STASTUS, 0);
						if (status == DATA_START) {
							mDetector.IsManualID = true;

							if (mDetector.IsSigmaThreshold)
								mDetector.Gamma_SigmaThreshold = 0;
							else
								mDetector.Gamma_Threshold = 0;

						} else if (status == DATA_END) {
							mDetector.IsManualID = false;

							mDetector.Finish_GammaEvent();
							if (mDetector.IsSigmaThreshold)
								mDetector.Gamma_SigmaThreshold = prefDB.Get_GammaThreshold_Sigma_From_pref();
							else
								mDetector.Gamma_Threshold = prefDB.Get_GammaThreshold_From_pref();

						} else if (status == DATA_CANCEL) {
							mDetector.IsManualID = false;

							mDetector.Cancel_Event();
							if (mDetector.IsSigmaThreshold)
								mDetector.Gamma_SigmaThreshold = prefDB.Get_GammaThreshold_Sigma_From_pref();
							else
								mDetector.Gamma_Threshold = prefDB.Get_GammaThreshold_From_pref();
						}

						break;

					case MSG_EN_CALIBRATION:
						Coefficients En_Coeff = (Coefficients) intent.getSerializableExtra(DATA_COEFFCIENTS);
						Coefficients Ch_Coeff = (Coefficients) intent.getSerializableExtra(DATA_CALIBRATION_PEAKS);

						if (En_Coeff.get_Coefficients()[0] == 0 || En_Coeff.get_Coefficients()[1] == 0)
							break;

						// if(!Send_GC_ToHW((int)Ch_Coeff.Get_Coefficients()[2])){
						// //out of bound :
						mDetector.Set_EnergyFittingArgument(En_Coeff.get_Coefficients());
						prefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());
						mPrefDB.Set_Calibration_Result(En_Coeff.get_Coefficients(), Ch_Coeff.get_Coefficients());
						// 180605 mPrefDB.Set_HW_ABC_From_pref(En_Coeff.get_Coefficients(),
						// Ch_Coeff.get_Coefficients());

						if (D)
							Log.d(TAG,
									"Receive Broadcast - Recalibration, DR: "
											+ NcLibrary.Channel_to_Energy(1024, En_Coeff.get_Coefficients()) + " ("
											+ En_Coeff.ToString() + " || " + Ch_Coeff.ToString() + ")");

						// ----

						break;

					case MSG_REMEASURE_BG:
						Spectrum bg = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);
						mDetector.Real_BG = bg;

						if (mEventDBOper != null) {
							mDetector.Real_BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
							mDetector.Real_BG.setFWHM(mEventDBOper.Cry_Info.FWHM);

							mDetector.Real_BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
							Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
							peakInfo_bg = FindPeaksN.GetPPSpectrum_H(mDetector.Real_BG);
							mDetector.Real_BG.SetPeakInfo(peakInfo_bg);
						}

						prefDB.Set_BG_MeasuredRealAcqTime_From_pref(bg.Get_SystemElapsedTime().getTime() - 1000);
						prefDB.Set_BG_Date_From_pref(bg.Get_MesurementDate());
						prefDB.Set_BG_MeasuredAcqTime_From_pref((int)bg.Get_AcqTime());
						prefDB.Set_BG_On_pref(bg.ToInteger(), bg.Get_Ch_Size());

						if (D)
							Log.d(TAG, "Receive Broadcast - Remeasured background (" + bg.Get_SystemElapsedTime().getTime()
									+ "__" + bg.ToString() + ")");
						break;

					case MAIN_DATA_SEND1:

						break;

					case MSG_HEALTH_EVENT:

						if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

							int Hevent_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
							if (Hevent_status == Detector.EVENT_BEGIN) {

								Start_HealthAlarm();

							} else if (Hevent_status == Detector.EVENT_FINISH) {

								Stop_Alarm();
								if (mDetector.EVENT_STATUS != Detector.EVENT_NONE)
								{
									Start_Alarm(false);
								}

							}

						}

						break;

					case MSG_EVENT:
						if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
							int event_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
							EventData eventdb = (EventData) intent.getSerializableExtra(DATA_EVENT);

							if (event_status == Detector.EVENT_OFF) {
								mDetector.Init_Measure_Data();
								mDetector.EVENT_STATUS = Detector.EVENT_OFF;
								mDetector.EVENT_STATUS_N = Detector.EVENT_OFF;
								break;
							} else if (event_status == Detector.EVENT_ON) {
								mDetector.EVENT_STATUS = Detector.EVENT_NONE;
								mDetector.EVENT_STATUS_N = Detector.EVENT_NONE;
								break;
							}

							if (event_status == Detector.EVENT_BEGIN) {
								if (tabHost.getCurrentTab() == 0)
								{
									Start_Alarm(false);
								}

								if (eventdb.Event_Detector.matches(EventData.EVENT_GAMMA)) {
									GainstabilizattonTxt.setText("");

									if (mDetector.EVENT_STATUS_N == Detector.EVENT_NONE)
									{
										Start_Alarm(false);
									}
								} else if (eventdb.Event_Detector.matches(EventData.EVENT_NEUTRON)) {

									if (mDetector.EVENT_STATUS == Detector.EVENT_NONE)
									{
										Start_Alarm(false);
									}

								} else if (eventdb.Event_Detector.matches(EventData.EVENT_MANUAL_ID)) {
									GainstabilizattonTxt.setText("");
								}

							} else if (event_status == Detector.EVENT_ING) {
								if (tabHost.getCurrentTab() == 0)
								{
									Start_Alarm(false);
								}

							} else if (event_status == Detector.EVENT_FINISH) {
								Stop_Alarm();
								// 180220
								//WriteEvent_toDB(eventdb);
								//Toast.makeText(getApplicationContext(),  " saved in DB", Toast.LENGTH_SHORT).show();
							}



						}
						break;

					/*
					 * case MSG_GAIN_STABILIZATION:
					 *
					 * if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY || ACTIVITY_STATE ==
					 * Activity_Mode.AUTO_CALIBRATION) {
					 *
					 * if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_START) {
					 * GainstabilizattonTxt.setTextColor(Color.parseColor("#ffffff"));
					 * GainstabilizattonTxt.setText("Stabilization in progress..");
					 *
					 * if (gainTime1.equals("")) { gainTime1 = (new
					 * SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
					 * NcLibrary.SaveText("\n Gain_Stabilization time1 :  " + gainTime1); }
					 *
					 * } else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_END) { mGainCnt++;
					 * GainstabilizattonTxt.setText("");
					 *
					 * String Today2 = (new
					 * SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
					 * SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss", Locale.KOREA); Date d1
					 * = f.parse(gainTime1); Date d2 = f.parse(Today2); long diff = d2.getTime() -
					 * d1.getTime(); long sec = diff / 1000;
					 * NcLibrary.SaveText("\n Gain_Stabilization SUCCESS time2 :  " + Today2);
					 * NcLibrary.SaveText("\n Gain_Stabilization time3  : " + (sec) + "sec");
					 * gainTime1 = "";
					 *
					 * double Old_calib_peaks[] = { prefDB.Get_CaliPeak1_From_pref(),
					 * prefDB.Get_CaliPeak2_From_pref(), prefDB.Get_CaliPeak3_From_pref() };
					 *
					 * double K40Peak = (double) intent.getIntExtra(DATA_K40_PEAK, 0);
					 * NcLibrary.SaveText("\n K40Peak by Finding:  " + K40Peak);
					 *
					 * if (K40Peak == 0) break;
					 *
					 * if (!Send_GC_ToHW((int) K40Peak)) {
					 *
					 * double Old_K40_Ch = Old_calib_peaks[2];
					 *
					 * double Ratio = ((K40Peak - Old_K40_Ch) / Old_K40_Ch);
					 * NcLibrary.SaveText("\n Send HW Faild: Old_K40_Ch :  " + Old_K40_Ch +
					 * "    K40Peak : " + K40Peak); double New_Peak1 = Old_calib_peaks[0] +
					 * (Old_calib_peaks[0] * Ratio); double New_Peak2 = Old_calib_peaks[1] +
					 * (Old_calib_peaks[1] * Ratio); double[] FitParam = new double[3];
					 * NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
					 * NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
					 *
					 * NcLibrary.SaveText( "\n New_Peak1 :  " + New_Peak1 ); NcLibrary.SaveText(
					 * "\n New_Peak2 :  " + New_Peak2 );
					 *
					 * NcLibrary.SaveText( "\n Old_calib_peaks[0] :  " + Old_calib_peaks[0] );
					 * NcLibrary.SaveText( "\n Old_calib_peaks[1] :  " + Old_calib_peaks[1] );
					 * NcLibrary.SaveText( "\n Old_calib_peaks[2] :  " + Old_calib_peaks[2] );
					 *
					 *
					 * Coefficients En_coeff = new Coefficients(FitParam); Coefficients Ch_coeff =
					 * new Coefficients( new double[] { New_Peak1, New_Peak2, K40Peak }); Intent
					 * intent41 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
					 * intent41.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
					 * intent41.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
					 * LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
					 * intent41);
					 *
					 * MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, K40Peak);
					 * mDetector.IsGainStb = true;
					 *
					 *
					 * NcLibrary.Write_ExceptionLog1("\nNew_Peak1 = "+New_Peak1);
					 * NcLibrary.Write_ExceptionLog1("\nNew_Peak2 = "+New_Peak2);
					 * NcLibrary.Write_ExceptionLog1("\nK40Peak = "+K40Peak);
					 * NcLibrary.Write_ExceptionLog1("\nEn_coeff = "+En_coeff);
					 * NcLibrary.Write_ExceptionLog1("\nCh_coeff = "+Ch_coeff);
					 *
					 * } else {
					 *
					 * K40Peak = (double) mDetector.mHW_K40_FxiedCh; double Old_K40_Ch =
					 * Old_calib_peaks[2];
					 *
					 * double Ratio = ((K40Peak - Old_K40_Ch) / Old_K40_Ch); double New_Peak1 =
					 * Old_calib_peaks[0] + (Old_calib_peaks[0] * Ratio); double New_Peak2 =
					 * Old_calib_peaks[1] + (Old_calib_peaks[1] * Ratio); double[] FitParam = new
					 * double[3]; NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak,
					 * NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
					 *
					 *
					 *
					 * NcLibrary.SaveText("\n Send HW true: Old_K40_Ch :  " + Old_K40_Ch +
					 * "    K40Peak : " + K40Peak);
					 *
					 *
					 *
					 * NcLibrary.SaveText( "\n New_Peak1 :  " + New_Peak1 ); NcLibrary.SaveText(
					 * "\n New_Peak2 :  " + New_Peak2 );
					 *
					 * NcLibrary.SaveText( "\n Old_calib_peaks[0] :  " + Old_calib_peaks[0] );
					 * NcLibrary.SaveText( "\n Old_calib_peaks[1] :  " + Old_calib_peaks[1] );
					 * NcLibrary.SaveText( "\n Old_calib_peaks[2] :  " + Old_calib_peaks[2] );
					 *
					 *
					 * Coefficients En_coeff = new Coefficients(FitParam); Coefficients Ch_coeff =
					 * new Coefficients( new double[] { New_Peak1, New_Peak2, K40Peak }); Intent
					 * intent41 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
					 * intent41.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
					 * intent41.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
					 * LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
					 * intent41);
					 *
					 * MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, K40Peak);
					 * mDetector.IsGainStb = true; if (D) Log.d(TAG,
					 * "Receive Broadcast - Gain stabilization ( To Fixed K40 Ch )");
					 *
					 *
					 * NcLibrary.Write_ExceptionLog1("\nNew_Peak1 = "+New_Peak1);
					 * NcLibrary.Write_ExceptionLog1("\nNew_Peak2 = "+New_Peak2);
					 * NcLibrary.Write_ExceptionLog1("\nK40Peak = "+K40Peak);
					 * NcLibrary.Write_ExceptionLog1("\nEn_coeff = "+En_coeff);
					 * NcLibrary.Write_ExceptionLog1("\nCh_coeff = "+Ch_coeff);
					 *
					 * }
					 *
					 * } else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_CANCEL) {
					 *
					 * GainstabilizattonTxt.setText("");
					 *
					 * String Today2 = (new
					 * SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
					 * SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss", Locale.KOREA); Date d1
					 * = f.parse(gainTime1); Date d2 = f.parse(Today2); long diff = d2.getTime() -
					 * d1.getTime(); long sec = diff / 1000;
					 * NcLibrary.SaveText("\n Gain_Stabilization SUCCESS time2 :  " + Today2);
					 * NcLibrary.SaveText("\n Gain_Stabilization time3  : " + (sec) + "sec");
					 * gainTime1 = "";
					 *
					 * }
					 *
					 * NcLibrary.SaveText("\n CryStal_Type_Name    : " +
					 * prefDB.Get_CryStal_Type_Name_pref());
					 * NcLibrary.SaveText("\n Selected_IsoLibName  : " +
					 * prefDB.Get_Selected_IsoLibName());
					 *
					 * } break;
					 */

					case MSG_GAIN_STABILIZATION:

						if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY
								|| ACTIVITY_STATE == Activity_Mode.AUTO_CALIBRATION) {

							if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_START) {

								GainstabilizattonTxt.setTextColor(Color.parseColor("#ffffff"));
								GainstabilizattonTxt.setText("Stabilization in progress..");

							} else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_END) {
								mGainCnt++;
								GainstabilizattonTxt.setText("");

								double Old_calib_peaks[] = { prefDB.Get_CaliPeak1_From_pref(),
										prefDB.Get_CaliPeak2_From_pref(), prefDB.Get_CaliPeak3_From_pref() };

								double FindK40Peak = (double) intent.getIntExtra(DATA_K40_PEAK, 0);
								double K40Peak = FindK40Peak;

								if (K40Peak <= 0)
									break;
								int GCValue_Old = mDetector.mHW_GC;
								int resultGc = 10;
								if (GCValue_Old > 0)
									resultGc = Send_GC_ToHW((int) FindK40Peak);

								double Old_K40_Ch = 0;
								double Ratio = 0;
								double New_Peak1 = 0;
								double New_Peak2 = 0;
								double[] FitParam = new double[3];
								Coefficients En_coeff, Ch_coeff;

								String str = "";

								switch (resultGc) {
									case 0: // <1%
										////////////////////////////////////////////
										// str=("GSunder1%,"+ Old_calib_peaks[0]+","+ Old_calib_peaks[1] +
										//////////////////////////////////////////// ","+Old_calib_peaks[2]+",findNewK40,"+
										//////////////////////////////////////////// FindK40Peak+",GCvalueOld,"+GCValue_Old+",GCValueNew,"+mDetector.mHW_GC+"\n");
										// str=str+","+mDetector.MS.ToString()+"\n";

										// NcLibrary.SaveText(str, "GSInfo.txt",true);
										////////////////////////////////

										break;

									case 1: // 1% < < 2%

										/*
										 * Old_K40_Ch = Old_calib_peaks[2]; Ratio = ((K40Peak - Old_K40_Ch) /
										 * Old_K40_Ch); New_Peak1 = Old_calib_peaks[0] + (Old_calib_peaks[0] * Ratio);
										 * New_Peak2 = Old_calib_peaks[1] + (Old_calib_peaks[1] * Ratio);
										 */

										// always fixed calibation using HW value(32kev, 662kev, K40 channel)
										// double findK40Peak=K40Peak;
										K40Peak = (double) mDetector.mHW_K40_FxiedCh;
										// K40Peak= 488; //503

										New_Peak1 = mDetector.mHW_Cs137_FxiedCh1;
										New_Peak2 = mDetector.mHW_Cs137_FxiedCh2;
										// New_Peak1 =9; //9
										// New_Peak2 =219; //230

										FitParam = new double[3];

										NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
												NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

										En_coeff = new Coefficients(FitParam);
										Ch_coeff = new Coefficients(new double[] { New_Peak1, New_Peak2, K40Peak });

										/////////////////////////////////////////////
										// 파일안에 문자열 쓰기
										// str=("GSover1%,"+ Old_calib_peaks[0]+","+ Old_calib_peaks[1] +
										///////////////////////////////////////////// ","+Old_calib_peaks[2]+",findNewK40,"+
										///////////////////////////////////////////// FindK40Peak+",GCvalueOld,"+GCValue_Old+",GCValueNew,"+mDetector.mHW_GC+"\n");
										// str=str+","+mDetector.MS.ToString()+"\n";
										// NcLibrary.SaveText(str, "GSInfo.txt",true);
										//////////////////////////////////////////

										mDetector.Set_EnergyFittingArgument(En_coeff.get_Coefficients());
										prefDB.Set_Calibration_Result(En_coeff.get_Coefficients(), Ch_coeff.get_Coefficients());

										mDetector.IsGainStb = true;
										break;
									/*
									 * case 2: // >2% findK40Peak=K40Peak; K40Peak = (double)
									 * mDetector.mHW_K40_FxiedCh; //K40Peak= 458;
									 *
									 * New_Peak1 = mDetector.mHW_Cs137_FxiedCh1 ; New_Peak2 =
									 * mDetector.mHW_Cs137_FxiedCh2; //New_Peak1 =7; //New_Peak2 =206;
									 *
									 * FitParam = new double[3]; NcLibrary.QuadraticCal(New_Peak1, New_Peak2,
									 * K40Peak, NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK,
									 * FitParam); //NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak,
									 * NcLibrary.CS137_PEAK1, // NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK,
									 * FitParam);
									 *
									 * En_coeff = new Coefficients(FitParam); Ch_coeff = new Coefficients(new
									 * double[] { New_Peak1, New_Peak2, K40Peak }); //Ch_coeff = new
									 * Coefficients(new double[] { New_Peak1, New_Peak2, findK40Peak });
									 *
									 *
									 *
									 * mDetector.Set_EnergyFittingArgument(En_coeff.get_Coefficients());
									 * prefDB.Set_Calibration_Result(En_coeff.get_Coefficients(),
									 * Ch_coeff.get_Coefficients());
									 *
									 * Intent intent42 = new Intent(MainBroadcastReceiver.MSG_EN_CALIBRATION);
									 * intent42.putExtra(MainBroadcastReceiver.DATA_COEFFCIENTS, En_coeff);
									 * intent42.putExtra(MainBroadcastReceiver.DATA_CALIBRATION_PEAKS, Ch_coeff);
									 * LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
									 * intent42);
									 *
									 * // 0214 MainActivity.mDetector.Background_GainStabilization(Old_K40_Ch, //
									 * K40Peak); // YKIM, 2018.2.19, rest time setup
									 * mDetector.mGain_restTime=mGain_restTime_over2; mDetector.IsGainStb = true; if
									 * (D) Log.d(TAG, "Receive Broadcast - Gain stabilization ( To Fixed K40 Ch )");
									 * break;
									 */
									case 10:
										K40Peak = FindK40Peak;
										// K40Peak = (double) mDetector.mHW_K40_FxiedCh;
										// K40Peak=(double)Old_calib_peaks[2]; //??

										Old_K40_Ch = Old_calib_peaks[2];
										Ratio = ((FindK40Peak - Old_K40_Ch) / Old_K40_Ch);
										double Ratioabs = Math.abs(Ratio);
										if (Ratioabs >= 0.01) {
											New_Peak1 = (double) Old_calib_peaks[0] + ((double) Old_calib_peaks[0] * Ratio);
											New_Peak2 = (double) Old_calib_peaks[1] + ((double) Old_calib_peaks[1] * Ratio);
											// K40Peak=findK40Peak
										} else {
											New_Peak1 = Old_calib_peaks[0];
											New_Peak2 = Old_calib_peaks[1];
											K40Peak = Old_calib_peaks[2];
										}

										NcLibrary.QuadraticCal(New_Peak1, New_Peak2, K40Peak, NcLibrary.CS137_PEAK1,
												NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);

										En_coeff = new Coefficients(FitParam);
										Ch_coeff = new Coefficients(new double[] { New_Peak1, New_Peak2, K40Peak });

										/////////////////////////////////////////////
										/////////////////////////////////////////////
										// 파일안에 문자열 쓰기
										// str=("GSNoGC,"+ Old_calib_peaks[0]+","+ Old_calib_peaks[1] +
										///////////////////////////////////////////// ","+Old_calib_peaks[2]+",findNewK40,"+
										///////////////////////////////////////////// FindK40Peak+",changedCali,"+New_Peak1+","+New_Peak2+","+K40Peak+",GCvalueOld,"+GCValue_Old+",GCValueNew,"+mDetector.mHW_GC+"\n");
										// str=str+","+mDetector.MS.ToString()+"\n";
										// NcLibrary.SaveText(str, "NoGC_GSInfo.txt",true);
										//////////////////////////////////////////

										mDetector.Set_EnergyFittingArgument(En_coeff.get_Coefficients());
										mPrefDB.Set_Calibration_Result(En_coeff.get_Coefficients(),
												Ch_coeff.get_Coefficients());
										// mDetector.IsManualID = true;
										break;
								}

							} else if (intent.getIntExtra(DATA_GS_STATUS, 0) == DATA_CANCEL) {
								GainstabilizattonTxt.setText("");

							}

						}
						break;

					case START_SETUP_MODE:
						// mMainUsbService.write(MESSAGE_END_HW);
						if (mDetector.Is_Event())
							mDetector.Finish_GammaEvent();
						mDetector.Finish_NeutronEvent();
						InIt_SPC_Data();

						if (MAPPING_VERSION & mCCSW_Service != null) {
							if (mCCSW_Service.Is_Connected()) {// set location

								Location loc = Get_Location();
								MappingData data = new MappingData();
								data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
								data.InstrumentName = "Setup mode";
								data.InstrumentMacAddress = mDetector.InstrumentModel_MacAddress;
								data.Doserate = 0;

								mCCSW_Service.Set_Data(null, data);
							}
						}

						break;

					case START_ID_MODE:

						InIt_SPC_Data();

						Update_StatusBar();
						break;

					case MSG_TAB_ENABLE:

						tabEnable();
						break;

					case MSG_SOURCE_ID_RUNNING_CANCEL:

						tabEnable();
						break;

					case MSG_TAB_DISABLE:

						tabDisable();
						break;

					case MSG_USB_CONNECTED:

						try {

							if(mMainUsbService !=null)
							{
								mMainUsbService.usbStart();
							}

							Update_StatusBar();
							Update_All_DetectorInfo();

						} catch (Exception e) {

							NcLibrary.Write_ExceptionLog(e);
						}

						break;

					//180919 Energy Calibration 종료 후 OK눌렀을떄 실행
					case MSG_FIXED_HWCALI_SEND:
						mTimeTaskcount = 0;
						sendCS = true;
						int[] caliChInfo = { 0, 0, 0, 0 };

						caliChInfo = NcLibrary.GetTextCli(MainActivity.FilenameCaliInfo, 4);
						if (caliChInfo[0] > 0)
						{
							ss2 = new byte[10];
							ss2[0] = 'C';
							ss2[1] = 'S';
							String str = "";
							// Cs137 32kev
							str = Integer.toString(caliChInfo[2]);
							byte[] GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
							if (GcBytes2.length == 1) {
								ss2[2] = GcBytes2[0];
								ss2[3] = 0;
							} else {
								ss2[2] = GcBytes2[1];
								ss2[3] = GcBytes2[0];
							}
							// Cs137 662kev
							str = Integer.toString(caliChInfo[0]);
							GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
							if (GcBytes2.length == 1) {
								ss2[4] = GcBytes2[0];
								ss2[5] = 0;
							} else {
								ss2[4] = GcBytes2[1];
								ss2[5] = GcBytes2[0];
							}
							// K40
							str = Integer.toString(caliChInfo[1]);
							GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();
							if (GcBytes2.length == 1) {
								ss2[6] = GcBytes2[0];
								ss2[7] = 0;
							} else {
								ss2[6] = GcBytes2[1];
								ss2[7] = GcBytes2[0];
							}
							// GC
							str = Integer.toString(caliChInfo[3]);
							GcBytes2 = new java.math.BigInteger(str, 10).toByteArray();

							if (GcBytes2.length == 1) {
								ss2[8] = GcBytes2[2];
								ss2[9] = 0;
							} else if (GcBytes2.length == 3) {
								ss2[8] = GcBytes2[2];
								ss2[9] = GcBytes2[1];
							} else if (GcBytes2.length == 2) {
								ss2[8] = GcBytes2[1];
								ss2[9] = GcBytes2[0];
							}

							try {

								if (mMainUsbService != null) {
									mMainUsbService.write(ss2);
									mSendGSTask = new TimerTask() {

										@Override
										public void run()
										{

											if (mTimeTaskcount > 3)
											{
												if (mSendGSTask != null)
												{
													mSendGSTask.cancel();
												}
											}
											mMainUsbService.write(Signal.MESSAGE_GS_HW);
											mTimeTaskcount++;

										}
									};

									mSendGSTimer = new Timer();
									mSendGSTimer.schedule(mSendGSTask, 0, 1000);

								}



								if (mService != null) {
									mService.write(ss2);


									mSendGSTask = new TimerTask() {

										@Override
										public void run() {

											if (mTimeTaskcount > 3)
											{
												if (mSendGSTask != null) {
													mSendGSTask.cancel();
												}
											}

							/*				byte[] GetGC = new byte[2];
											GetGC[0] = 'G';
											GetGC[1] = 'S';

											mService.write(GetGC);

											try {
												Thread.sleep(300);
											} catch (Exception e) {
											}*/


											mService.write(Signal.MESSAGE_GQ_HW);

										//	mService.write(Signal.MESSAGE_GS_HW);
											mTimeTaskcount++;


										}
									};

									mSendGSTimer = new Timer();
									mSendGSTimer.schedule(mSendGSTask, 0, 1000);
								}

							} catch (Exception e)
							{
								//NcLibrary.SaveText(e+"\n");
								NcLibrary.Write_ExceptionLog(e);
							}
						}

						break;


					case MSG_SOURCE_ID_RESULT:

						sourceIdResult();

						IDspectrumActivity.SOURCE_ID_RESULT_MODE = true;

						break;

					case MSG_SOURCE_ID_RESULT_CANCEL:

						sourceIdResultCancel();
						tabEnable();

						SetFristActiviyMode();
						break;

					case MSG_USB_DISCONNECT:

						Stop_Alarm();
						Stop_Vibrate();
						break;
					case MSG_FIXED_GC_SEND:
						byte[] ss = new byte[5];
						if (mDetector.mHW_GC > 1024) {
							////////////////////////////////
							// HH200
							NewGC = Integer.toString(mDetector.mHW_FixedGC);
							byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

							ss[0] = 'G';
							ss[1] = 'C';
							if (GcBytes.length == 1) {
								ss[2] = 0;
								ss[3] = GcBytes[2];
							} else if (GcBytes.length == 3) {
								ss[2] = GcBytes[1];
								ss[3] = GcBytes[2];
							} else if (GcBytes.length == 2) {
								ss[2] = GcBytes[0];
								ss[3] = GcBytes[1];
							}
							ss[4] = (byte) Byte.valueOf((byte) 1);
						} else {
							//////////////////////////////////
							// HH100
							NewGC = Integer.toString(mDetector.mHW_FixedGC);
							byte[] GcBytes1 = new java.math.BigInteger(NewGC, 16).toByteArray();
							ss = new byte[5];
							ss[0] = 'G';
							ss[1] = 'C';

							if (GcBytes1.length == 1) {
								ss[2] = 0;
								ss[3] = GcBytes1[0];
							} else {
								ss[2] = GcBytes1[0];
								ss[3] = GcBytes1[1];
							}
							ss[4] = (byte) Byte.valueOf((byte) 1);
						}

						try {

							if (mMainUsbService != null) {
								mMainUsbService.write(ss);
							}

							if (mService != null) {
								mService.write(ss);
							}
						} catch (Exception e) {
							NcLibrary.Write_ExceptionLog(e);
						}

						break;

//					case WifiManager.NETWORK_STATE_CHANGED_ACTION:
//						NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//						if(info != null && info.isConnected())
//						{
//							wifiConnected = true;
//						}
//						else
//						{
//							wifiConnected = false;
//						}
//						break;
//
//					case ConnectivityManager.CONNECTIVITY_ACTION:
//						NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//						if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && ! networkInfo.isConnected())
//						{
//							wifiConnected = false;
//						}
//						else
//						{
//							wifiConnected = true;
//						}
//						break;
				}

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}

		}
	}

	MainBCRReceiver mMainBCR = new MainBCRReceiver();

	public interface CHECK {

		public boolean ACTIVITY_RESET = true;

	};

	TabWidget hello;
	int tabBodyWidth = 0;

	int tabBodyHeight = 0;

	LinearLayout tabBottomLayout, tabBodyLayout;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mContext = this;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		mHnderUncaughException = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler( new CUncaughtExceptionHandlerApp());

		PowerManager pm = (PowerManager) MainActivity.this.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "");
		wl.acquire();

		setContentView(R.layout.maintab);



		mPrefDB = new PreferenceDB(this);

		Resources res = getResources();
		tabHost = getTabHost();
		TabWidget = getTabWidget();
		tabHost.clearAllTabs();

		intent = new Intent().setClass(this, RealTimeActivity.class); 	// 각 탭의 메뉴와 컨텐츠를 위한 객체 생성
		spec = tabHost.newTabSpec(Tab_Name.RealTime_Str).setIndicator(Tab_Name.RealTime_Str).setContent(intent);
		tabHost.addTab(spec);


		Spectrum input_spc = (mDetector.mGamma_Event == null) ? mDetector.MS.ToSpectrum() : mDetector.mGamma_Event.MS.ToSpectrum();

		tabHost.addTab(tabHost.newTabSpec(Tab_Name.ManualID_Str).setIndicator(Tab_Name.ManualID_Str)
				.setContent(new Intent(this, IDspectrumActivity.class)
						.putExtra(IDspectrumValue.EXTRA_SPECTRUM, input_spc)
						.putExtra(IDspectrumValue.EXTRA_MANUAL_ID_GOAL_TIME, mPrefDB.Get_ManualID_DefaultTime())
						.putExtra(IDspectrumValue.EXTRA_MANUAL_ID_ADJUST_TIME, mPrefDB.Get_ManualID_AdjustTime())
						.putExtra(IDspectrumValue.EXTRA_SEQ_ACQTIME, mPrefDB.Get_SequenceMode_acqTime_From_pref())
						.putExtra(IDspectrumValue.EXTRA_SEQ_REPEAT, mPrefDB.Get_SequenceMode_Repeat_From_pref())
						.putExtra(IDspectrumValue.EXTRA_SEQ_PAUSE_TIME, mPrefDB.Get_SequenceMode_PauseTime_From_pref())
						.putExtra(IDspectrumValue.EXTRA_SV_UNIT, mDetector.IsSvUnit)
						.putExtra(IDspectrumValue.ACTIVTY, "hellohello")));

		tabHost.addTab(tabHost.newTabSpec(Tab_Name.SequentialMode_Str).setIndicator(Tab_Name.SequentialMode_Str)
				.setContent(new Intent(this, SequentialActivity.class)
						.putExtra(IDspectrumValue.EXTRA_SPECTRUM, input_spc)
						.putExtra(IDspectrumValue.EXTRA_MANUAL_ID_GOAL_TIME, mPrefDB.Get_ManualID_DefaultTime())
						.putExtra(IDspectrumValue.EXTRA_MANUAL_ID_ADJUST_TIME, mPrefDB.Get_ManualID_AdjustTime())
						.putExtra(IDspectrumValue.EXTRA_SEQ_ACQTIME, mPrefDB.Get_SequenceMode_acqTime_From_pref())
						.putExtra(IDspectrumValue.EXTRA_SEQ_REPEAT, mPrefDB.Get_SequenceMode_Repeat_From_pref())
						.putExtra(IDspectrumValue.EXTRA_SEQ_PAUSE_TIME, mPrefDB.Get_SequenceMode_PauseTime_From_pref())
						.putExtra(IDspectrumValue.EXTRA_SV_UNIT, mDetector.IsSvUnit)
						.putExtra(IDspectrumValue.ACTIVTY, "hellohello")));

		tabHost.setCurrentTab(0); // 현재화면에 보여질 탭의 위치를 결정
		tabHost.setEnabled(true);
		tabHost.setOnTabChangedListener(this);

		if (mPrefDB.Get_SequenceMode_From_pref())
		{
			tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
		}
		else
		{
			tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
		}



		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			// Here, thisActivity is the current activity
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ||
					ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {

				if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
					Log.e("Main", "퍼미션 수락 거절");
					//Toast.makeText(getApplicationContext(), "권한을 설정해 주셔야 앱이 원할하게 돌아갑니다.", Toast.LENGTH_LONG).show();
					ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE,
							android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, 0);
				} else {
					//권한 요청 dlg 띄움
					ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_NETWORK_STATE,
							android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, 0);

					// 필요한 권한과 요청코드 넣고, 요청에 대한 결과 받아야 함.
					Log.e("Main", "퍼미션 요청");


				}
			} else
			{
				requestPermission();
			}
		}
		else
		{
			requestPermission();
		}

		CheckWifiEnable(getApplicationContext());
	}

	private void CheckWifiEnable(Context context)
	{
		try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi.isConnected()) {
                Intent i = new Intent(context, TCPServerService.class);
                context.startService(i);

                wifiConnected = true;
            }
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onRequestPermissionsResult(int code, String per[], int[] res)
	{
		switch (code)
		{
			case 0:
				if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED &&
						res[1] == PackageManager.PERMISSION_GRANTED &&
						res[2] == PackageManager.PERMISSION_GRANTED  &&
						res[3] == PackageManager.PERMISSION_GRANTED  &&
						res[4] == PackageManager.PERMISSION_GRANTED  &&
						res[5] == PackageManager.PERMISSION_GRANTED  )
				{
					// 권한 허가 완료. 해당 작업 진행
					requestPermission();
				}

				break;
		}
	}

	public void requestPermission()
	{


		File mIsEnablesdcard = new File("/sdcard");
		if (!mIsEnablesdcard.exists()) {

			try {
				Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c",
						"am start -a android.intent.action.ACTION_REQUEST_SHUTDOWN --ez KEY_CONFIRM true --activity-clear-task" });
				proc.waitFor();
			} catch (Exception ex) {
				NcLibrary.Write_ExceptionLog(ex);
			}
			finish();

		}

		if (!mDebug.BP)
		{
			String AppName = "Launcher_1_2_1";

			if (!NcLibrary.CheckedHH200Launcher(mContext, AppName))
			{
				Intent Intent = new Intent(MainActivity.this, Guide.class);
				Intent.putExtra(Guide.GuideMode.GetGuideModeTitle, Guide.GuideMode.UpdateLauncher);
				Intent.putExtra(Guide.GuideMode.GetAppFileName, AppName);
				startActivityForResult(Intent, 2);
				finish();

			}
			else
			{
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				ResolveInfo defaultLauncher = getPackageManager().resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);

				String nameOfLauncherPkg = defaultLauncher.activityInfo.packageName;
				Log.e("ahn", "nameOfLauncherPkg");
				if ("ah.hathi.simplelauncher".equals(nameOfLauncherPkg))
				{
					Log.e("ahn", "nameOfLauncherPkg");
				}
				else
				{
					Intent Intent = new Intent(MainActivity.this, Guide.class);
					Intent.putExtra(Guide.GuideMode.GetGuideModeTitle, Guide.GuideMode.SetLauncher);
					startActivityForResult(Intent, 2);
					finish();
				}
			}

		}
		//bp라면
		else
		{
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			ResolveInfo defaultLauncher = getPackageManager().resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);

			String nameOfLauncherPkg = defaultLauncher.activityInfo.packageName;
			Log.e("ahn", "nameOfLauncherPkg");
			//hh200 launcher 라면 런처 해제
			if ("ah.hathi.simplelauncher".equals(nameOfLauncherPkg))
			{
			/*	Intent Intent = new Intent(MainActivity.this, Guide.class);
				Intent.putExtra(Guide.GuideMode.GetGuideModeTitle, Guide.GuideMode.SetLauncher);
				startActivityForResult(Intent, 2);*/
				//finish();

				intent = new Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + "ah.hathi.simplelauncher"));
				startActivity(intent);
				finish();
			}

		}

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)
		{
			turnGPSOn();
		}
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBTAdapter == null)
		{
			Toast.makeText(this, getResources().getString(R.string.bt_not_enabled_leaving), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		mainContext = MainActivity.this;
		StrArraylist = new ArrayList<String>();

		mDetector = new Detector(mContext);
		mNormalDB = new NormalDB();
		mNormalDB.start();
		mEventDB = new EventDBOper();
		mEventDB.setHandler(mHandler);
		ErmDataManager.getInstance().setContext(getApplicationContext());


		Check_SwUpdate_OnFTP();
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAlarmSound = MediaPlayer.create(this, R.raw.beep1);


		DB_Ver mDB = new DB_Ver(mContext);
		mDB.Check_DB_File();

		if (Logcount == 0)
		{
			// Start_Login_Dlg();
			Logcount = 1;
		}

		BrodcastDeclare();

		// test / will be remove
		mLogin = LoginDlg.LOGIN_ADMIN;
		mDetector.Set_PmtProperty(Detector.HwPmtProperty_Code.CeBr_2x2);
		tabEnable();

		CreateMediaFile();

		if (mPrefDB.Get_SequenceMode_From_pref()) {

			tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
		} else {

			tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
		}

		tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);

		Check_AndMake_DeviceNameFile(true);

		Battery = (TextView) findViewById(R.id.BatteryTxt);
		Battery.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openOptionsMenu();
			}
		});
		mBatteryProgBar = (BatteryView) findViewById(R.id.betterView_ProgressBar);
		GainstabilizattonTxt = (TextView) findViewById(R.id.Gainstabilizatton);
		mBatteryProgBar.Set_Value(12);
		mBatteryProgBar.invalidate();

		Battery.setText("--%");
		MainActivity1 = this;

		mCurrentConnectMode = mPrefDB.Get_IsConnect_UsbMode_From_pref();
		mChangeConnectMode = mCurrentConnectMode;
		// mChangeConnectMode: false = Bluetooth, 1true = USB
		if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false)
		{
			BluetoothListExcute();
		}
		else
		{ // USB connection
			mMainUsbService = new MainUsbService(mContext, mHandler);
		}

		try
		{
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null)
			{
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		}
		catch (Exception ex)
		{
			NcLibrary.Write_ExceptionLog(ex);
		}

		DefaultSettingCalAndMail(mPrefDB);
		Update_StatusBar();

		if (mDebug.IsDebugMode)
		{
			StartVisualConnect();
/*
			if (mDebug.IsVolumeDown)
			{
				VolumeDown();
			}
*/

		}
		else
		{
			//VolumeUp();
		}
		mEventDBOper = new EventDBOper();
		mEventDBOper.Set_Crytal_Info(Integer.toString(HwPmtProperty_Code.NaI_3x3));
		Update_All_DetectorInfo();

	}

	public void VolumeDown()
	{
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setStreamVolume(AudioManager.STREAM_RING, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_SYSTEM, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_ALARM, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);
		audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);
	}

	// 핸들러부분

	int accumulateSPC_5m = 0;
	private double[] mSPC_5m = new double[1024];

	@SuppressLint("HandlerLeak")
	final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

				case MainMsg.MESSAGE_STATE_CHANGE:

					switch (msg.arg1) {

						case MainService.STATE_CONNECTED:
							try {

							//	mService.write(Signal.MESSAGE_END_HW);

								byte[] GetGC = new byte[2];
								GetGC[0] = 'G';
								GetGC[1] = 'S';

								if (mService != null)
									mService.write(GetGC);

								try {
									Thread.sleep(300);
								} catch (Exception e) {
								}

							GetGC[0] = 'G';
								GetGC[1] = 'Q';
								if (mService != null)
									mService.write(GetGC);

								//191118 수정
 								while (mTimeTaskcount<5){
									if(mService!=null)
										mService.write(MESSAGE_GQ_HW);
									mTimeTaskcount++;
									Log.e("ahn","MESSAGE_GQ_HW");
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}

								if(mTimeTaskcount>=5){
									SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_BLUETOOTH_CONNECTED);
								}

								//SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_BLUETOOTH_CONNECTED);

							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
							}
							break;

						case MainUsbService.USB_DISCONNECTED:


							CONNECT_CHECK = false;

							Reset_Detector();

							Intent send_gs1 = new Intent(MainBroadcastReceiver.UPDATE_NEUTRONCPS);
							send_gs1.putExtra(MainBroadcastReceiver.UPDATE_NEUTRONCPS_TEXT, -1);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs1);

							Init_stabilization();

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
							// --===--

							try {

								mMainUsbService.usbStop();
								mMainUsbService.write(Signal.MESSAGE_END_HW);
								if (mDetector.Is_Event())
									mDetector.Finish_GammaEvent();
								mDetector.Finish_NeutronEvent();
								InIt_SPC_Data();

								if (MAPPING_VERSION & mCCSW_Service != null) {
									if (mCCSW_Service.Is_Connected()) {// set
										// location

										Location loc = Get_Location();
										MappingData data = new MappingData();
										data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
										data.InstrumentName = "Setup mode";
										data.InstrumentMacAddress = mDetector.InstrumentModel_MacAddress;
										data.Doserate = 0;

										mCCSW_Service.Set_Data(null, data);
									}
								}

								SendBroadcast(getApplicationContext(), MainBroadcastReceiver.START_SETUP_MODE);

							} catch (NullPointerException e) {
								NcLibrary.Write_ExceptionLog(e);
							}
							break;
						case MainUsbService.USB_CONNECTED:
							try {

								CONNECT_CHECK = true;

								mMainUsbService.usbStart();

								Update_All_DetectorInfo();

								SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_START_ID_MODE);

							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
								;
							}
							break;

						case MainService.STATE_CONNECTING:

							try {

								Show_ProgressDlg(getResources().getString(R.string.wait_while_connect));
							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
								;
							}

							break;
						case MainService.STATE_LISTEN:
							try {

								Dismiss_ProgressDlg();

							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
							}
							break;
						case MainService.STATE_NONE:
							try {

								SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_BLUETOOTH_DISCONNECT);
								CONNECT_CHECK = false;

								Dismiss_ProgressDlg();
							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
							}
							break;
						case MainService.STATE_LOST:
							try {

								CONNECT_CHECK = false;

								Reset_Detector();

								Intent send_gs = new Intent(MainBroadcastReceiver.UPDATE_NEUTRONCPS);
								send_gs.putExtra(MainBroadcastReceiver.UPDATE_NEUTRONCPS_TEXT, -1);
								LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

								// Update_StatusBar();
								Init_stabilization();

								SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);
								// --===--
							} catch (Exception e) {
								NcLibrary.Write_ExceptionLog(e);
							}
							break;
					}
					break;
				/// -------------------------------------------
				case MainMsg.MESSAGE_READ_GC:
					try {

						GCData mGCData = new GCData();
						mGCData = (GCData) msg.obj;



						if(sendCS)
						{
							cntSendCS += 1;
							if(cntSendCS==3)
							{
								sendCS = false;
								cntSendCS = 0;
								if ((mDetector.mHW_K40_FxiedCh == mGCData.K40_Ch)
										&& (mDetector.mHW_Cs137_FxiedCh1 == mGCData.Cs137_Ch1)
										&& (mDetector.mHW_Cs137_FxiedCh2 == mGCData.Cs137_Ch2)  )
								{
									((Activity) mContext).runOnUiThread(new Runnable() {
										public void run() {
											Toast toast = Toast.makeText(mContext, "The Current version of the hardware does not support this feature", Toast.LENGTH_SHORT);
											toast.show();
										}
									});
								}
								else
								{
									((Activity) mContext).runOnUiThread(new Runnable()
									{
										public void run() {
											Toast toast = Toast.makeText(mContext, getResources().getString(R.string.ToastEnergyCalibration), Toast.LENGTH_SHORT);
											toast.show();
										}
									});
								}

								mDetector.mHW_GC = mGCData.GC;
								mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
								mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
								mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
								mDetector.mHW_FixedGC = mGCData.GC;
								mDetector.Set_PmtProperty(mGCData.DetType);
								mDetector.mCrtstalType = mGCData.DetType;



								int[] HWinfo = { mDetector.mHW_Cs137_FxiedCh1, mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh, mDetector.mHW_GC, mGCData.DetType };
								NcLibrary.SaveTextCali(HWinfo, MainActivity.FilenameCurCaliInfo, 5);



								double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,(double) mGCData.K40_Ch };
								double[] FitParam = new double[3];

								NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1,NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
								mPrefDB.Set_Calibration_Result(FitParam, PeakCh);

								Write_HW_Calibration_Result(mGCData);

							}
						}
						else
						{
							mDetector.mHW_GC = mGCData.GC;
							mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
							mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
							mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
							mDetector.mHW_FixedGC = mGCData.GC;
							mDetector.Set_PmtProperty(mGCData.DetType);
							mDetector.mCrtstalType = mGCData.DetType;

							int[] HWinfo = { mDetector.mHW_Cs137_FxiedCh1, mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh, mDetector.mHW_GC, mGCData.DetType };
							NcLibrary.SaveTextCali(HWinfo, MainActivity.FilenameCurCaliInfo, 5);

							/*
							 * .......................... * 180404 추가 Hung.18.03.05 Added Code to new
							 * algorithm
							 */
							//////////////////
							// for test to apply calirbation when get the GCdata
							double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,(double) mGCData.K40_Ch };
							double[] FitParam = new double[3];

							NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1,NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
							mPrefDB.Set_Calibration_Result(FitParam, PeakCh);

							mEventDBOper = new EventDBOper();
							mEventDBOper.Set_Crytal_Info(Integer.toString(mGCData.DetType));

							Write_HW_Calibration_Result(mGCData);

							mService.write(Signal.MESSAGE_START_HW);

							Dismiss_ProgressDlg();

							if (IsThere_CalibrationInfo()) {

								Init_stabilization();
								if (!mDebug.IsDebugMode) {
									for (int i = 0; i < 10; i++) {
										mService.write(Signal.MESSAGE_START_HW);

										Thread.sleep(100);
										mService.write(Signal.MESSAGE_END_HW);

										Thread.sleep(100);
									}

									mService.write(Signal.MESSAGE_START_HW);
								}
								Start_AutoCalib();
							} else {
								Toast.makeText(getApplicationContext(),
										getResources().getString(R.string.not_found_calibration), Toast.LENGTH_LONG).show();

							}
						}


					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;

				case MainMsg.MESSAGE_USB_READ_GC:
					try
					{
						GCData mGCData = new GCData();
						mGCData = (GCData) msg.obj;

						if(sendCS)
						{
							cntSendCS += 1;
							if(cntSendCS==3)
							{
								if(mSendGSTask!=null)
								{
									mSendGSTask.cancel();
									mSendGSTask=null;
								}
								sendCS = false;
								cntSendCS = 0;
								if ((mDetector.mHW_K40_FxiedCh == mGCData.K40_Ch)
										&& (mDetector.mHW_Cs137_FxiedCh1 == mGCData.Cs137_Ch1)
										&& (mDetector.mHW_Cs137_FxiedCh2 == mGCData.Cs137_Ch2)  )
								{
									((Activity) mContext).runOnUiThread(new Runnable() {
										public void run() {
											Toast toast = Toast.makeText(mContext, "The Current version of the hardware does not support this feature", Toast.LENGTH_SHORT);
											toast.show();
										}
									});
								}
								else
								{
									((Activity) mContext).runOnUiThread(new Runnable()
									{
										public void run() {
											Toast toast = Toast.makeText(mContext, getResources().getString(R.string.ToastEnergyCalibration), Toast.LENGTH_SHORT);
											toast.show();
										}
									});
									mDetector.mHW_GC = mGCData.GC;
									mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
									mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
									mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
									mDetector.mHW_FixedGC = mGCData.GC;
									mDetector.Set_PmtProperty(mGCData.DetType);
									mDetector.mCrtstalType = mGCData.DetType;

								//	NcLibrary.SaveText1( "Ch1, :"+mDetector.mHW_Cs137_FxiedCh1+" Ch2," +mDetector.mHW_Cs137_FxiedCh2 +" Ch3," +mDetector.mHW_K40_FxiedCh, "CalcROIK40");

									int[] HWinfo = { mDetector.mHW_Cs137_FxiedCh1, mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh, mDetector.mHW_GC, mGCData.DetType };
									NcLibrary.SaveTextCali(HWinfo, MainActivity.FilenameCurCaliInfo, 5);

									double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,(double) mGCData.K40_Ch };
									double[] FitParam = new double[3];

									NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1,NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
									mPrefDB.Set_Calibration_Result(FitParam, PeakCh);

									Write_HW_Calibration_Result(mGCData);
								}
							}
						}
						else
						{

							// mDetector.mHW_GC = msg.arg1;
							// mDetector.mHW_K40_FxiedCh = msg.arg2;

							mDetector.mHW_GC = mGCData.GC;
							mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
							mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
							mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
							mDetector.mHW_FixedGC = mGCData.GC;
							mDetector.Set_PmtProperty(mGCData.DetType);
							mDetector.mCrtstalType = mGCData.DetType;

							/*
							 * .......................... 180404 추가 Hung.18.03.05 Added Code to new
							 * algorithm
							 */
							//////////////////
							// for test to apply calirbation when get the GCdata
							int[] HWinfo = { mDetector.mHW_Cs137_FxiedCh1, mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh, mDetector.mHW_GC, mGCData.DetType };
							NcLibrary.SaveTextCali(HWinfo, MainActivity.FilenameCurCaliInfo, 5);

							double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,(double) mGCData.K40_Ch };
							double[] FitParam = new double[3];

							NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1,NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
							mPrefDB.Set_Calibration_Result(FitParam, PeakCh);


							if (AutoCalibrationCnt == Activity_Mode.EXCUTE_MODE)
							{
								AutoCalibrationCnt = Activity_Mode.UN_EXCUTE_MODE;
								if (IsThere_CalibrationInfo())
								{
									Init_stabilization();
									mDetector.IsGainStb = false;

									if (mDebug.mLog.D) {
										Calendar calendar = Calendar.getInstance();

										int minute = calendar.get(Calendar.MINUTE);
										int second = calendar.get(Calendar.SECOND);

										Log.d(mDebug.mLog.MainActivity, mDebug.mLog.MainActivity + "MESSAGE_USB_READ_GC time : "
												+ Integer.toString(minute) + ":" + Integer.toString(second));
									}
									Start_AutoCalib();

								}

							}
							mDetector.Set_PmtProperty(mGCData.DetType);
							mDetector.mCrtstalType = mGCData.DetType;

							mEventDBOper = new EventDBOper();
							mEventDBOper.Set_Crytal_Info(Integer.toString(mGCData.DetType));

							Write_HW_Calibration_Result(mGCData);

							if (!ISSendU4AA) {
								SendU4AA();
								ISSendU4AA = true;
								TimerTask mTask = new TimerTask() {
									@Override
									public void run() {

										SendU2AA();

									}
								};

								Timer mTimer = new Timer();
								mTimer.schedule(mTask, 1000);
							}

						}

						// mDetector.mHW_GC = msg.arg1;
						// mDetector.mHW_K40_FxiedCh = msg.arg2;

						mDetector.mHW_GC = mGCData.GC;
						mDetector.mHW_K40_FxiedCh = mGCData.K40_Ch;
						mDetector.mHW_Cs137_FxiedCh1 = mGCData.Cs137_Ch1;
						mDetector.mHW_Cs137_FxiedCh2 = mGCData.Cs137_Ch2;
						mDetector.mHW_FixedGC = mGCData.GC;
						mDetector.Set_PmtProperty(mGCData.DetType);
						mDetector.mCrtstalType = mGCData.DetType;

						/*
						 * .......................... 180404 추가 Hung.18.03.05 Added Code to new
						 * algorithm
						 */
						//////////////////
						// for test to apply calirbation when get the GCdata
						int[] HWinfo = { mDetector.mHW_Cs137_FxiedCh1, mDetector.mHW_Cs137_FxiedCh2,mDetector.mHW_K40_FxiedCh, mDetector.mHW_GC, mGCData.DetType };
						NcLibrary.SaveTextCali(HWinfo, MainActivity.FilenameCurCaliInfo, 5);

						double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,(double) mGCData.K40_Ch };
						double[] FitParam = new double[3];

						NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1,NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
						mPrefDB.Set_Calibration_Result(FitParam, PeakCh);


						if (AutoCalibrationCnt == Activity_Mode.EXCUTE_MODE)
						{
							AutoCalibrationCnt = Activity_Mode.UN_EXCUTE_MODE;
							if (IsThere_CalibrationInfo())
							{
								Init_stabilization();
								mDetector.IsGainStb = false;

								if (mDebug.mLog.D) {
									Calendar calendar = Calendar.getInstance();

									int minute = calendar.get(Calendar.MINUTE);
									int second = calendar.get(Calendar.SECOND);

									Log.d(mDebug.mLog.MainActivity, mDebug.mLog.MainActivity + "MESSAGE_USB_READ_GC time : "
											+ Integer.toString(minute) + ":" + Integer.toString(second));
								}
								Start_AutoCalib();

							}

						}
					/*	mDetector.Set_PmtProperty(mGCData.DetType);
						mDetector.mCrtstalType = mGCData.DetType;
*/
						mEventDBOper = new EventDBOper();
						mEventDBOper.Set_Crytal_Info(Integer.toString(mGCData.DetType));

						Write_HW_Calibration_Result(mGCData);

						if (!ISSendU4AA) {
							SendU4AA();
							ISSendU4AA = true;
							TimerTask mTask = new TimerTask() {
								@Override
								public void run() {

									SendU2AA();

								}
							};

							Timer mTimer = new Timer();
							mTimer.schedule(mTask, 1000);
						}

						SendU2AA();

					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
					break;

				case MainMsg.MESSAGE_MEDIA_SCAN:
					Start_MediaScan_AllDBFile();
					break;
				case MainMsg.MESSAGE_READ_BATTERY:
					try {

						mBattary = (int) msg.arg1;

						mBatteryProgBar.Set_Value(mBattary);

						mBatteryProgBar.invalidate();

						Battery.setText(String.valueOf((int) mBattary) + " %");

						if (mDebug.IsDebugMode) {
							if (mDebug.IsBattEnalbe) {
								mBatteryProgBar.Set_Value(100);

								mBatteryProgBar.invalidate();
								Battery.setText(String.valueOf((int) 100) + " %");

							}
						}

						Intent send_gs = new Intent(MainBroadcastReceiver.MAIN_DATA_SEND1);
						send_gs.putExtra(MainBCRReceiver.DATA_BATTERY,
								String.valueOf(NcLibrary.Auto_floor(mBattary)) + "%");

						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
						;
					}
					break;

				//j5및 기존 하드웨어 처리 시간 데이터 없음
				case MainMsg.MESSAGE_READ_DETECTOR_DATA:
					try
					{
						ReadDetectorData mReadData = new ReadDetectorData();
						mReadData = (ReadDetectorData)msg.obj;
						mDetector.GM_Cnt = (int)mReadData.GM;

						if (mReadData.IsThereNeutron == false)
						{
							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_NOT_RECV_USB_NEUTRON);
						}
						else
							{
							mDetector.mIsNeutronModel = true;
							//MainActivity.mDetector.mNeutron.Set_CPS((mReadData.GetAVGNeutron <= 0.08) ? 0 : mReadData.GetAVGNeutron);
							//테스트 MainActivity.mDetector.mNeutron.Set_CPS(mReadData.GetAVGNeutron);
							//181129 0.08이하 0으로 표시 삭제
							MainActivity.mDetector.mNeutron.Set_CPS(mReadData.GetAVGNeutron);
							mHandler.obtainMessage(MainMsg.MESSAGE_NEUTRON_RECV, 0, 0, mReadData).sendToTarget();

						}

						if ((ReadDetectorData) msg.obj != null)
						{
							mDetector.Set_Spectrum(mReadData.pdata);

							SendMCU();
						}
						//NcLibrary.SaveText1("cps : "+mDetector.MS.Get_AvgCPS()+ "cnt : "+mDetector.MS.Get_TotalCount()+"\n","test");

						mDetector.Discrimination();

						CheckCPSIsZero(mDetector.Get_GammaCPS(), mDetector.MS.Get_TotalCount());

						if (mNormalDB != null)
							mNormalDB.addValue(mDetector.User, mDetector.Location, mDetector.Get_Gamma_DoseRate_nSV(),
									mDetector.mNeutron.Get_CPS());



					} catch (Exception e) {
						//NcLibrary.Write_ExceptionLog(e);

					}
					break;

				//j3 및 시간데이터 처리
				case MainMsg.MESSAGE_READ_DETECTOR_DATA_J3:

					try
					{
						ReadDetectorData mReadData = new ReadDetectorData();
						mReadData = (ReadDetectorData) msg.obj;
						mDetector.GM_Cnt = mReadData.GM;

						if (mReadData.IsThereNeutron == false)
						{
							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_NOT_RECV_USB_NEUTRON);
						}
						else
						{
							mDetector.mIsNeutronModel = true;
							MainActivity.mDetector.mNeutron.Set_CPS(mReadData.GetAVGNeutron);
							mHandler.obtainMessage(MainMsg.MESSAGE_NEUTRON_RECV, 0, 0, mReadData).sendToTarget();
						}

						if ((ReadDetectorData) msg.obj != null)
						{
							mDetector.Set_Spectrum(mReadData.pdata, mReadData.mRealTime);

							SendMCU();
						}

						mDetector.Discrimination();

						CheckCPSIsZero(mDetector.Get_GammaCPS(), mDetector.MS.Get_TotalCount());

						if (mNormalDB != null)
							mNormalDB.addValue(mDetector.User, mDetector.Location, mDetector.Get_Gamma_DoseRate_nSV(),
									mDetector.mNeutron.Get_CPS());

						addErmData();

					} catch (Exception e) {
						//NcLibrary.Write_ExceptionLog(e);
						;
					}



					break;

				//181005
				case MainMsg.MESSAGE_READ_SERIAL_DATA:

					try {

						ReadDetectorData mReadData = new ReadDetectorData();
						mReadData = (ReadDetectorData) msg.obj;
/*
						// modification for HW name
						if(mReadData.serial.matches("HH.*"))
						{
							Paired.setText(getResources().getString(R.string.HardwareName));
							mDeviceIcon.setBackgroundResource(R.drawable.device);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareName);
						}
						if(mReadData.serial.matches("BP.*"))
						{
							Paired.setText(getResources().getString(R.string.HardwareNameBP));
							mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareNameBP);
						}
						if(mReadData.serial.matches("VM.*"))
						{
							Paired.setText(getResources().getString(R.string.HardwareNameVM));
							mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareNameVM);
						}
						*/
						String[] serialInfo= {mReadData.MCU, mReadData.FPGA, mReadData.board, mReadData.serial };
						NcLibrary.SaveTextSerial(serialInfo,MainActivity.FilenameInstrumentInfo);

						Update_StatusBar();

					} catch (Exception e) {
						//NcLibrary.Write_ExceptionLog(e);
						;
					}
					break;


				case MainMsg.MESSAGE_NEUTRON_RECV:
					ReadDetectorData mReadData = new ReadDetectorData();
					mReadData = (ReadDetectorData) msg.obj;

					// NcLibrary.SaveText("\nmReadData.GetAVGNeutron : " + mReadData.GetAVGNeutron);
					//Log.e("ahn", "\nmReadData.GetAVGNeutron :  " + mReadData.GetAVGNeutron);

					Intent send_gs = new Intent(MainBroadcastReceiver.MSG_RECV_USB_NEUTRON);
					send_gs.putExtra(MainBroadcastReceiver.DATA_NEUTRON, mReadData.GetAVGNeutron);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

					if (mLog.D)
						Log.i(mLog.MainActivity, mLog.MainActivity + "mHandler MESSAGE_NEUTRON_RECV excute");

					break;

				case MainMsg.MESSAGE_READ_GM:

					Message_Read_Gm(msg.arg1);

					break;
				case MainMsg.MESSAGE_CONNECTED_DEVICE_INFO:
					try {

						BluetoothDevice device = (BluetoothDevice) msg.obj;
						mDetector.InstrumentModel_Name = device.getName();
						mDetector.InstrumentModel_MacAddress = device.getAddress();

						mEventDB.Save_DeviceName(device.getName());
						mPrefDB.Set_Last_Cntd_Detector(mDetector.InstrumentModel_Name);
						mPrefDB.Set_Last_Cntd_DetectorMac(mDetector.InstrumentModel_MacAddress);

						Update_StatusBar();

						// Set_DeviceImage(false);
						Check_AndMake_DeviceNameFile(false);
						Toast.makeText(getApplicationContext(), "Connected to " + mDetector.InstrumentModel_Name,
								Toast.LENGTH_SHORT).show();

					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
						;
					}
					break;

				case MainMsg.MESSAGE_TOAST:

					if (mLog.D)
						Log.i(mLog.MainActivity, mLog.MainActivity + "mHandler MESSAGE_TOAST excute");

					int Index1 = (int) msg.arg1;

					if (Index1 == 0) {

						Toast.makeText(getApplicationContext(), getString(R.string.bt_not_enabled_leaving),
								Toast.LENGTH_SHORT).show();
						DeleteDlg();
						try {

						} catch (NullPointerException e) {

						}

					}

					break;

				case MainMsg.MESSAGE_ORIGINAL_TOAST:
					Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
					break;

				case MainMsg.MESSAGE_SHUTDOWN:
					try {

						UsbBrodcastStop();

						if (mDebug.IsDebugMode) {
							if (mTimer != null) {
								mTimer.cancel();
								mTask.cancel();
							}

						} else {
							finish();
						}
						// android.os.Process.killProcess(android.os.Process.myPid());

					} catch (NullPointerException e) {
						// TODO: handle exception
					}

					break;

				case INPUT_HARDWARE_KEY:

					int Index = (int) msg.arg1;

					int KeyValue = Integer.valueOf((String) msg.obj);

					if (ActionViewExcuteCheck == Activity_Mode.UN_EXCUTE_MODE) {

						HWKey(Index, KeyValue);

					}
					break;

			}
		}
	};

	private void addErmData() {
		ErmDataManager.getInstance().addCurrentSpectra(mDetector);
	}

	int countCheck = 0;
	public void CheckCPSIsZero(int CPS, int totalCount)
	{
		countCheck = countCheck + 1;
//
//		NcLibrary.SaveText1("Receive spectrum", "CheckCPS");
//
//		if(countCheck >=5)
//		{
//			NcLibrary.SaveText1("Test reset after 5 times receive CPS", "CheckCPS");
//
//			MainActivity.SendU4AA();
//
//			Timer TimerSendReset = new Timer();
//			TimerSendReset.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					MainActivity.SendU2AA();
//				}
//			}, 500);
//
//			countCheck = 0;
//		}

		if(CPS <=0 && countCheck>=3) {
			NcLibrary.SaveText1("CPS is 0 in 3 times", "CheckCPS");

			MainActivity.SendU4AA();

			Timer TimerSendReset = new Timer();
			TimerSendReset.schedule(new TimerTask() {
				@Override
				public void run() {
					MainActivity.SendU2AA();
				}
			}, 200);

			countCheck = 0;
		}
	}

	public void SendMCU()
	{
		accumulateSPC_5m = accumulateSPC_5m +1;

		if(accumulateSPC_5m >= 300)
		{
			Spectrum Spcinput = new Spectrum(mSPC_5m);
			Spcinput.Set_Coefficients(MainActivity.mDetector.Coeffcients);
			Spcinput.setWnd_Roi(MainActivity.mDetector.Real_BG.getWnd_Roi());
			Spcinput.setFWHM(MainActivity.mDetector.MS.getFWHM());
			Spcinput.setFindPeakN_Coefficients(MainActivity.mDetector.MS.getFindPeakN_Coefficients());
			Spcinput.mAcqTime = 300;

			SendDoserate(Spcinput);

			accumulateSPC_5m = 0;
			mSPC_5m = new double[1024];
		}
		else
		{
			double[] tempSpc = mDetector.MS.ToDouble();
			for (int i = 0; i < 1024; i++) {
				mSPC_5m[i] += tempSpc[i];
			}
		}
	}

	public void SendDoserate(Spectrum spc) {

		try {
			double doseValue = NcLibrary.DoseRateCalculate_GE(spc.ToDouble(),
					spc.Get_AcqTime(),
					mDetector.Real_BG.ToDouble(),
					MainActivity.mDetector.Real_BG.Get_AcqTime(),
					spc.Get_Coefficients().get_Coefficients(),
					mDetector.mPmtSurface,
					mDetector.mCrystal,
					mDetector.getGECoef()) / (1000 * spc.Get_AcqTime());

			Vector<Isotope> isotopes = mIsoLib2.Find_Isotopes_with_Energy(spc, mDetector.Real_BG);

//		String SumTxt = "";
//		if (isotopes.size() > 0) {
//			for (int j = 0; j < isotopes.size(); j++) {
//				SumTxt = SumTxt + isotopes.get(j).isotopes;
//				SumTxt = SumTxt + ",";
//
//				double ConfidenLevl = isotopes.get(j).Index2 * 100;
//				SumTxt = SumTxt += Double.toString(ConfidenLevl);
//
//				SumTxt = SumTxt + ",";
//			}
//		} else SumTxt = "NOFIND";
//
//		NcLibrary.SaveText1("SourceID , "+SumTxt+ " Spc"+ spc.ToString()
//				+"BG"+ MainActivity.mDetector.Real_BG.ToString(),
//				"Testdosesent");


			byte[] buff = new byte[9];
			buff[0] = 'D';
			buff[1] = 'S';
//		buff[2] = 0x46;
//		buff[3] = 0x43;
//		buff[4] = 0x30;
//		buff[5] = 0x2E;
//		buff[6] = 0x31;
//		buff[7] = 0x32;
//		buff[8] = 0x33;

			buff[2] = 0x46;
			if (isotopes != null) {
				if (isotopes.size() > 0) {
					buff[2] = 0x54;
				}
			}

			if (!isSWDebug) {
				if (wifiConnected) {
					buff[3] = 0x43;
				} else {
					buff[3] = 0x44;
				}
			} else {
				buff[3] = 0x44;
			}

			byte[] bDose = new byte[5];
			DecimalFormat df = new DecimalFormat("#.####");
			df.setRoundingMode(RoundingMode.CEILING);
			String sDose = df.format(doseValue);
			byte[] temps = sDose.getBytes(StandardCharsets.US_ASCII);

			System.arraycopy(temps,0,bDose,0, temps.length);

			buff[4] = bDose[0];
			buff[5] = bDose[1];
			buff[6] = bDose[2];
			buff[7] = bDose[3];
			buff[8] = bDose[4];

			String test = "";
			for (int i = 0; i < buff.length; i++) {
				char c = (char) buff[i];
				test += c;
			}

			NcLibrary.SaveText1(test, "Testdosesent");

			mMainUsbService.write(buff);
		}
		catch (Exception ex)
		{
			NcLibrary.SaveText1(ex.getStackTrace().toString() + ", " + ex.getMessage(), "Testdosesent");

			StackTraceElement[] stackTraceElements = ex.getStackTrace();
			String logMessage = ""
					+ stackTraceElements[0].getMethodName() + " - File name is "
					+ stackTraceElements[0].getFileName()
					+ " - At line number: "
					+ stackTraceElements[0].getLineNumber();

			NcLibrary.SaveText1(logMessage, "Testdosesent");
//			FileOutputStream file = new FileOutputStream("Testdosesent");
//			PrintWriter writer = new PrintWriter(file);
//			ex.printStackTrace(writer);
//			writer.close();
//			file.close();
		}
	}

	public void onTabChanged(String tabId) {

		if (mLog.D)
			Log.i(mLog.MainActivity, mLog.MainActivity + "onTabChanged : " + tabId);

		strMsg = tabId;

		ActivityCheck = tabId;
		TabChangeDraw(tabId);

		// SendU2AA();

		// ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		if (strMsg.equals(Tab_Name.RealTime_Str)) {
			MainActivity.mDetector.mGain_elapsedTime = Detector.GAIN_START_IN_SEC;
			mDetector.IsGainStb = true;
			// UsbConnect();
			// tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);

			MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;
		} else {
			mDetector.IsGainStb = false;
			GainstabilizattonTxt.setText("");
			Stop_Alarm();
			Stop_Vibrate();

		}

		if (strMsg.equals("Background")) {

		}

		if (strMsg.equals("En.Calibration")) {

		}

		if (strMsg.equals(Tab_Name.ManualID_Str)) {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
				FirstActivityCurrentTab = Tab_Name.MenualID;
				SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);
			}
		}

		if (strMsg.equals("Sequential Mode")) {

			if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY)
			{
				//190102 추가
				//SendU4AA();

				FirstActivityCurrentTab = Tab_Name.SequentialMode;
				SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_SEQUENTAL_MODE_RUNNING_START);
			}
		}
	}

	@Override
	public View createTabContent(String tag) {

		// TODO Auto-generated method stub
		return null;
	}

	void turnGPSOn() {
		String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps")) { // if gps is disabled
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			sendBroadcast(poke);
		}
	}

	void Check_SwUpdate_OnFTP() {
		if (NcLibrary.IsWifiAvailable(this) | NcLibrary.Is3GAvailable(this)) {

			new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						VersionUpdate up = new VersionUpdate(NcLibrary.Get_AppVersion(mContext), mHandler);
						up.Update_Version_FromFTP();
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
				}
			}).start();

		}
	}

	void Check_AndMake_EventDB_VersionFile() {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {

			dbpath.mkdirs();
		}
		//
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + EventDBOper.DB_VERSION_FILE + ".txt"); // 디바이스
		// 네임
		// 폴더를
		// 만든다.
		if (nameFilePath.isFile()) {
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(nameFilePath);
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			byte[] buf = new byte[3];
			try {
				fis.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				NcLibrary.Write_ExceptionLog(e);
			}
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				NcLibrary.Write_ExceptionLog(e);
			}

			if (buf[0] == EventDBOper.mDB.GetDBVersion().getBytes()[0]
					&& buf[2] == EventDBOper.mDB.GetDBVersion().getBytes()[2]) {

			} else { // 버젼이 다르다.
				if (mEventDB != null) {
					try {
						if (buf[2] == 51) { // if DB version is 1.3
							mEventDB.Remove_EventFile();
							mEventDB.OpenDB();
							mEventDB.EndDB();

							mNormalDB.OpenDB();
						} else {
							Vector<EventData> temp = mEventDB.Load_ALL_Event();
							mEventDB.Remove_EventFile();
							mEventDB.OpenDB();
							mEventDB.EndDB();

							if (temp != null) {
								for (int i = 0; i < temp.size(); i++) {
									mEventDB.WriteEvent_OnDatabase(temp.get(i));
								}

							}
						}
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}

				}
			}
		} else {
			if (mEventDB != null) {
				FileOutputStream Fos = null;
				try {

					Fos = new FileOutputStream(nameFilePath);
				} catch (FileNotFoundException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				byte[] buf = new byte[3];
				try {
					Fos.write(EventDBOper.mDB.GetDBVersion().getBytes());
				} catch (IOException e) {

					NcLibrary.Write_ExceptionLog(e);
				}
				try {
					Fos.close();
				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
				}
			}
		}
	}

	void Check_AndMake_IsoLibraryFile() {
		DB_Ver DB = new DB_Ver(mContext);

		String IsoLib_Path = getIsoDB_FilePath("SwLibrary.sql");
		File dbpath = new File(getIsoDB_FilePath(EventDBOper.DB_VERSION_FILE + ".txt"));
		////////////////////////////////
		if (!(dbpath.isFile())) {
			File isoFile = new File(IsoLib_Path);
			if (isoFile.isFile())
				isoFile.delete();
			exdbfile(R.raw.iso_library);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dbpath);
			} catch (FileNotFoundException e1) {
				NcLibrary.Write_ExceptionLog(e1);
			}
			try {
				for(int i = 0; i<DB.GetIsoLibVer().length(); i++)
				{
					fos.write(DB.GetIsoLibVer().getBytes()[i]);
				}

				/*fos.write(DB.GetIsoLibVer().getBytes()[0]);
				fos.write(DB.GetIsoLibVer().getBytes()[1]);
				fos.write(DB.GetIsoLibVer().getBytes()[2]);*/

				fos.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		} else {
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(dbpath);
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			byte[] buf = new byte[3];
			try {
				fis.read(buf);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
			try {
				fis.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			if (buf[0] == DB.GetIsoLibVer().getBytes()[0] && buf[2] == DB.GetIsoLibVer().getBytes()[2]) {

			} else {
				dbpath.delete();
				File isoFile = new File(IsoLib_Path);
				if (isoFile.isFile())
					isoFile.delete();

				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(dbpath);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					NcLibrary.Write_ExceptionLog(e1);
				}
				try {
					fos.write(DB.GetIsoLibVer().getBytes());
					fos.close();
				} catch (IOException e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				exdbfile(R.raw.iso_library);
			}
		}

	}

	public String getIsoDB_FilePath(String FileName) {
		try {
			File sdcard = Environment.getExternalStorageDirectory();

			File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_LIB_FOLDER);
			if (!dbpath.exists()) {
				dbpath.mkdirs();
			}

			String dbfile = dbpath.getAbsolutePath() + File.separator + FileName;
			return dbfile;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return "";
		}
	}

	public void exdbfile(int rawId) {
		File file = new File(getIsoDB_FilePath("SwLibrary.sql"));
		if (file.isFile() == false) {
			byte[] buffer = new byte[8 * 1024];

			int length = 0;
			InputStream is = getResources().openRawResource(rawId);
			BufferedInputStream bis = new BufferedInputStream(is);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(getIsoDB_FilePath("SwLibrary.sql"));
			} catch (FileNotFoundException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				while ((length = bis.read(buffer)) >= 0)
					fos.write(buffer, 0, length);
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				fos.flush();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			try {
				fos.close();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	void Check_AndMake_DeviceNameFile(boolean IsEdit) {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {
			dbpath.mkdirs();
		}
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + EventDBOper.DEVICE_FILE + ".txt"); // 디바이스
		// 네임
		// 폴더를
		// 만든다.
		if (nameFilePath.isFile() && IsEdit == false) {
			nameFilePath.delete();
		}

		if (!nameFilePath.isFile()) {
			try {
				nameFilePath.createNewFile();
				FileOutputStream fos = null;
				fos = new FileOutputStream(nameFilePath);
				String Last_Dev;
				if (mDetector.InstrumentModel_Name == null | mDetector.InstrumentModel_Name.equals("")) {
					Last_Dev = DEVICE_NAME;
					mDetector.InstrumentModel_Name = DEVICE_NAME;
				} else {
					Last_Dev = mDetector.InstrumentModel_Name;
				}
				fos.write(String.valueOf(Last_Dev).getBytes());
				fos.close();

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				NcLibrary.Write_ExceptionLog(e1);
			} catch (IOException e2) {
				NcLibrary.Write_ExceptionLog(e2);
			}
		}
	}

	public void onStart() {

		try {
			super.onStart();

		//	Start_MediaScan_AllDBFile();

			SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_USB_CONNECTED);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public boolean Start_MediaScan_AllDBFile() {
		try {

			File file = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_FOLDER); // 외장
			// 디렉토리
			// 가져옴
			File[] fileNames = file.listFiles(new FilenameFilter() { // 특정 확장자만
				// 가진
				// 파일들을
				// 필터링함
				public boolean accept(File dir, String name) {
					return true;
				}
			});

			if (fileNames != null) {
				for (int i = 0; i < fileNames.length; i++) // 파일 갯수 만큼 scanFile을
				// 호출함
				{
					new SingleMediaScanner(mContext.getApplicationContext(), fileNames[i]);
				}
			}

			File file2 = new File(
					Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + EventDBOper.DB_LIB_FOLDER); // 외장
			// 디렉토리
			// 가져옴
			File[] fileNames2 = file2.listFiles(new FilenameFilter() { // 특정
				// 확장자만
				// 가진
				// 파일들을
				// 필터링함
				public boolean accept(File dir, String name) {
					return true;
				}
			});

			if (fileNames2 != null) {
				for (int i = 0; i < fileNames2.length; i++) // 파일 갯수 만큼
				// scanFile을 호출함
				{
					new SingleMediaScanner(mContext.getApplicationContext(), fileNames2[i]);
				}
			}
			return true;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
	}

	void Start_AutoCalib() {

		if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY)

			try {
				Intent intent = null;
				intent = new Intent(MainActivity.this, AutoCalibActivity.class);
				intent.putExtra(AutoCalbration.EXTRA_THRESHOLD_CNT, mDetector.get_K40_ID_Threshold());
				intent.putExtra(AutoCalbration.EXTRA_FAIL_CNT,
						(mDetector.mCrystal != Detector.CrystalType.NaI) ? 1.8 : 4.0);
				startActivityForResult(intent, AUTO_CALIB_FINISH);

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
	}


	boolean IsThere_CalibrationInfo() {
		try {
			if (mPrefDB.Get_Cali_A_From_pref() == 0)
				return false;
			else
				return true;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		SetFristActiviyMode();
		// ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		switch (requestCode) {
			case AUTO_CALIB_FINISH:
				try {
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
					switch (resultCode) {
						case AutoCalbration.RESULT_SUCCESS:

							mDetector.Set_EnergyFittingArgument(mPrefDB.Get_Cali_ABC_From_pref());
							mDetector.Real_BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref());
							mDetector.Real_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(),
									mPrefDB.Get_BG_MeasuredAcqTime_From_pref());

							AUTO_FAIL_CODE_10 = false;
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.auto_cali_success),
									Toast.LENGTH_LONG).show();
							// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_START_ID_MODE);
							Update_All_DetectorInfo();
							mDetector.IsGainStb = true;

							// if (mDebug.IsDebugMode) {
							// ShutDownTimeTask = new TimerTask() {
							//
							// @Override
							// public void run() {
							//
							// try {
							//
							// try {
							// ShutDownTimer.cancel();
							// ShutDownTimeTask.cancel();
							// Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c",
							// "am start -a android.intent.action.ACTION_REQUEST_SHUTDOWN --ez KEY_CONFIRM
							// true --activity-clear-task" });
							// proc.waitFor();
							// } catch (Exception ex) {
							// ex.printStackTrace();
							// }
							//
							// } catch (Exception ex) {
							// ex.printStackTrace();
							// }
							//
							// }
							// };
							// ShutDownTimer = new Timer();
							// ShutDownTimer.schedule(ShutDownTimeTask, 8000);
							//
							// finish();
							// }

							break;

						case AutoCalbration.RESULT_CANCEL:
							// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
							autoTime1 = "";
							Update_All_DetectorInfo();

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_START_ID_MODE);

							mDetector.IsGainStb = true;
							Update_StatusBar();

							break;

						case AutoCalbration.RESULT_OUT_OF_BOUND:
							autoTime1 = "";
							if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false) {
								// mService.write(MESSAGE_END_HW);
							}
							dialogBuilder.setTitle(getResources().getString(R.string.auto_cali_fail));
							dialogBuilder.setMessage(getResources().getString(R.string.auto_cali_gain_shift));
							dialogBuilder.setPositiveButton("Yes", null);
							dialogBuilder.setCancelable(false);
							dialogBuilder.show();
							// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

							break;

						case AutoCalbration.RESULT_CALIB_ERR:
							autoTime1 = "";
							if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false) {
								// mService.write(MESSAGE_END_HW);
							}
							dialogBuilder.setTitle(getResources().getString(R.string.auto_cali_fail));
							dialogBuilder.setMessage(getResources().getString(R.string.not_found_calibration));
							dialogBuilder.setPositiveButton("Yes", null);
							dialogBuilder.setCancelable(false);
							dialogBuilder.show();
							// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
							break;
						case AutoCalbration.RESULT_TIMEOUT_AND_COUNT:
							autoTime1 = "";
							if (AUTO_FAIL_CODE_10) {
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.auto_cali_2nd_fail),
										Toast.LENGTH_LONG).show();
								mAUTO_GAIN_result = 0;
								AUTO_FAIL_CODE_10 = false;
								if (mPrefDB.Get_IsConnect_UsbMode_From_pref() == false) {
									mService.write(Signal.MESSAGE_END_HW);
								}
								// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
								break;
							} else {
								AUTO_FAIL_CODE_10 = true;
								Toast.makeText(getApplicationContext(),
										getResources().getString(R.string.auto_cali_k40_insufficient), Toast.LENGTH_LONG)
										.show();
								Start_AutoCalib();
							}
					}
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}

			case FINISH_SETUP_PREF:
				try
				{
					Update_All_DetectorInfo();
					mPrefDB.Set_Last_Cntd_User(mDetector.User);
					Update_StatusBar();

					//////
					if (mAlarmSound != null) {
						if (mAlarmSound.isPlaying()) {
							if (mDetector.AlarmSound == R.raw.beep1) {
								Start_Alarm(true);
								Stop_Alarm();
							} else
							{
								Start_Alarm(false);
							}

						}
					}
					///////////////////

					mChangeConnectMode = mPrefDB.Get_IsConnect_UsbMode_From_pref();
					if (mCurrentConnectMode != mChangeConnectMode) {

						Toast.makeText(getApplicationContext(),
								"The connection mode has been changed. Please exit the application and run it again.", Toast.LENGTH_LONG)
								.show();
					}
					InIt_SPC_Data();

					// Send_Default_GC_ToHW();

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case RESULT_LOGIN:

				// TextView Login = (TextView)
				// m_MainLayout.findViewById(R.id.n3rd_row_3);
				if (resultCode == LoginDlg.LOGIN_USER) {
					mLogin = LoginDlg.LOGIN_USER;

					// Login.setText(getResources().getString(R.string.login_user));
				} else {
					mLogin = LoginDlg.LOGIN_ADMIN;
				}

				Update_StatusBar();

				// LOGIN_MODE

				// Conntect_With_LastDevice();

				break;

			case FINISH_CALIB_BG:
				/*
				 * try{ mDetector.Set_EnergyFittingArgument(mPrefDB. Get_Cali_ABC_From_pref());
				 * mDetector.BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref()) ;
				 * mDetector.BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(),mPrefDB.
				 * Get_BG_MeasuredAcqTime_From_pref()); }catch(Exception e) {
				 * NcLibrary.Write_ExceptionLog("\nKainacActivity - FINISH_CALIB_BG" ); } break;
				 */

				////////////////////////////////// K40 roi �곸뿭��移댁슫�멸� 異⑸텇�섏�
				////////////////////////////////// 紐삵븷��

				/////////////////////////////////////////////////

				break;
			case REQUEST_ENABLE_BT:
				try {
					if (resultCode == Activity.RESULT_OK) {
						if (mService == null)
							mService = new MainService(this, mHandler);
					} else {

						Toast.makeText(this, "not enabled leaving", Toast.LENGTH_SHORT).show();
						// finish();
					}
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case MainActivity.REQUEST_CONNECT_DEVICE:
				try {
					if (resultCode == Activity.RESULT_OK) {
						String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
						BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

						mService.connect(device);
					}
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void Update_StatusBar() {

		try {

			Paired = (TextView) findViewById(R.id.Paired);
			Library = (TextView) findViewById(R.id.Library);
			Alarm = (TextView) findViewById(R.id.Alarm);
			mDeviceIcon = (ImageView) findViewById(R.id.textView1);


			// Y.Kim 20180423
			// modification for HW name
			if (mDetector.InstrumentModel_Name != null)
			{
				if (mService != null) // bluetooth connection
				{
					if (mDetector.InstrumentModel_Name.matches("HH.*")) // for SAM950
					{
						Paired.setText(getResources().getString(R.string.HardwareName));
						mDeviceIcon.setBackgroundResource(R.drawable.device);
					} else if (mDetector.InstrumentModel_Name.matches("BP.*")) // for for SAMPack
					{
						Paired.setText(getResources().getString(R.string.HardwareNameBP));
						mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
					} else if (mDetector.InstrumentModel_Name.matches("VM.*")) // for for SAMPack
					{
						Paired.setText(getResources().getString(R.string.HardwareNameVM));
						mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
					}
					else
					{
						if(mDebug.BP)
						{
							Paired.setText(getResources().getString(R.string.HardwareNameBP));
							mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareNameBP);
						}
						else
						{
							Paired.setText("SAM");
							mDeviceIcon.setBackgroundResource(R.drawable.device);
						}

					}
				}
				else if (mMainUsbService != null) // usb connection = HH200
				{
					mDetector.InstrumentModel_Name = NcLibrary.getTextSerial(FilenameInstrumentInfo,4);
					if(!NcLibrary.getTextSerial(FilenameInstrumentInfo,4).equals(""))
					{
						// modification for HW name
						if(mDetector.InstrumentModel_Name.matches("HH.*"))
						{
							Paired.setText(getResources().getString(R.string.HardwareName));
							mDeviceIcon.setBackgroundResource(R.drawable.device);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareName);
						}
						if(mDetector.InstrumentModel_Name.matches("BP.*"))
						{
							Paired.setText(getResources().getString(R.string.HardwareNameBP));
							mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareNameBP);
						}
						if(mDetector.InstrumentModel_Name.matches("VM.*"))
						{
							Paired.setText(getResources().getString(R.string.HardwareNameVM));
							mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareNameVM);
						}
					}
					else
					{
						if(mDebug.BP)
						{
							Paired.setText(getResources().getString(R.string.HardwareNameBP));
							mDeviceIcon.setBackgroundResource(R.drawable.devicebp);
							mDetector.InstrumentModel_Name = getResources().getString(R.string.HardwareNameBP);
						}
						else
						{
							Paired.setText("SAM 950");
							mDeviceIcon.setBackgroundResource(R.drawable.device);
						}

					}
				}
				else
				{
					Paired.setText("SAM");
					mDeviceIcon.setBackgroundResource(R.drawable.device);

				}
			}
			mIsoLib2 = new IsotopesLibrary(this);
			Vector<String> temp = mIsoLib2.get_IsotopeLibrary_List();
			if (temp.isEmpty() == true) {
				Library.setText("None");
				LibraryStr = "None";
			} else {
				String temp22 = mPrefDB.Get_Selected_IsoLibName();
				if (temp22.matches("null")) {
					mPrefDB.Set_String_on_pref(getResources().getString(R.string.IsoLib_List_Key), temp.get(0));
					Library.setText(temp.get(0));
					LibraryStr = temp.get(0);
					mIsoLib2.Set_LibraryName(temp.get(0));
				} else {
					Library.setText(temp22);
					LibraryStr = temp22;
					mIsoLib2.Set_LibraryName(temp22);
				}

			}

			if (mDetector.IsSigmaThreshold) {

				Alarm.setText(getResources().getString(R.string.variable));
				AlarmStr = getResources().getString(R.string.variable);

			} else {

				Alarm.setText(getResources().getString(R.string.fixed));
				AlarmStr = getResources().getString(R.string.fixed);
			}
			if (IsThere_CalibrationInfo()) {

			} else {
				tabEnable();
				NoCalDataTabDisable();

			}

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public String GetDeviceName(String str) {
		String DeviceName = "";

		if (str.matches("HH.*")) {
			DeviceName = getResources().getString(R.string.HardwareNameBP);
		}

		return DeviceName;

	}

	void Start_HealthAlarm() {

		if (mAlarmSound != null) {
			if (mAlarmSound.isPlaying()) {
				mAlarmSound.stop();
				mAlarmSound.reset();
			}
		}
		mAlarmSound = MediaPlayer.create(this, R.raw.danger_alarm);

		if (mAlarmSound != null) {
			mAlarmSound.setVolume(100, 100);
			mAlarmSound.start();
			mAlarmSound.setLooping(true);
		}
		return;

	}


	void Start_Alarm(boolean IsBeep) {

		try {

			if(mDebug.IsDebugMode && mDebug.IsVolumeDown)
			{
				return;
			}
			if (IsBeep)
				return;

			AudioManager ssq = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);

			if (ssq.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
				if (mDebug.IsDebugMode) {

				} else {

					Start_Vibrate();
				}
			}

			if (mDetector.Is_HealthEvent())
				return;

			if (mDetector.AlarmSound == R.raw.beep1) {
				if (mAlarmSound == null)
					mAlarmSound = MediaPlayer.create(this, mDetector.AlarmSound);

				if (mAlarmSound.isPlaying() == false) {
					if (mDetector.MS.Get_TotalCount() > mDetector.Get_GammaThreshold() * 3.5) {
						mAlarmSound = MediaPlayer.create(this, R.raw.beep3);
						mAlarmSound.setVolume(100, 100);
						mAlarmSound.start();
					} else if (mDetector.MS.Get_TotalCount() > mDetector.Get_GammaThreshold() * 1.5) {
						mAlarmSound = MediaPlayer.create(this, R.raw.beep2);
						mAlarmSound.setVolume(100, 100);
						mAlarmSound.start();
					} else {
						mAlarmSound = MediaPlayer.create(this, mDetector.AlarmSound);
						mAlarmSound.setVolume(100, 100);
						mAlarmSound.start();

					}
				}
				return;
			}

			if (mAlarmSound != null) {
				if (mAlarmSound.isPlaying()) {
					mAlarmSound.stop();
					mAlarmSound.reset();
				}
			}
			mAlarmSound = MediaPlayer.create(this, mDetector.AlarmSound);

			if (mAlarmSound != null) {
				// if(mAlarmSound.isPlaying() == false){
				mAlarmSound.setVolume(80, 80);
				mAlarmSound.start();
				mAlarmSound.setLooping(true);
				// }
			}

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	void Stop_Alarm() {

		try {
			if (mAlarmSound != null) {
				if (mAlarmSound.isPlaying())
					mAlarmSound.stop();
			}
			Stop_Vibrate();
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void Stop_Vibrate() {

		if (mVibrating) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.cancel();
			mVibrating = false;
		}
	}

	private void Start_Vibrate() {
		if (mVibrating == false) {
			Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibe.vibrate(new long[] { 500, 500, 500, 500, 1000, 1000 }, 0);
			mVibrating = true;

		}
	}

	public Location Get_Location() {

		Location result = null;

		GpsInfo gps = new GpsInfo(this);
		if (gps.isGetLocation()) {
			result = gps.getLocation();
		}
		gps.stopUsingGPS();

		if (result != null)
			return result;
		else
			return new Location(LocationManager.GPS_PROVIDER);

	}

	void WriteEvent_toDB(EventData event) {

	/*	if (event.MS.Get_AcqTime() <= 3)
			return;*/

		// Location gps = Get_Location();

		// eventdb.GPS_Latitude = loc.getLatitude();
		// eventdb.GPS_Longitude = loc.getLongitude();
		GpsInfo2 mGps = new GpsInfo2(mContext);

		event.GPS_Latitude = mGps.GetLat();
		event.GPS_Longitude = mGps.GetLon();
		event.mColumn_Version = EventDBOper.mDB.GetDBVersion();

		// Check_AndMake_DeviceNameFile(true);
		// Check_AndMake_EventDB_VersionFile();

		EventDBOper eventDB = new EventDBOper();
		if (eventDB.WriteEvent_OnDatabase(event)) {
			int cnt = eventDB.GetEventCount();
			String str_event = getResources().getString(R.string.event);

			Message msg = mHandler.obtainMessage(MainMsg.MESSAGE_ORIGINAL_TOAST);
			Bundle bundle = new Bundle();
			bundle.putString(TOAST, getResources().getString(R.string.write_db) + " (" + str_event + " #" + cnt + ")");
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

		// Start_MediaScan();
		File eventFile = new File(Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/"
				+ EventDBOper.DB_FILE_NAME + ".sql");
		if (eventFile.isFile())
			new SingleMediaScanner(getApplicationContext(), eventFile);
	}

	void Update_All_DetectorInfo() {

		try {
			mDetector.Set_EnergyFittingArgument(mPrefDB.Get_Cali_ABC_From_pref());

			mDetector.DB_BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref());
			mDetector.DB_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(), mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			mDetector.DB_BG.Set_StartSystemTime(new Date(0));
			mDetector.DB_BG.Set_EndSystemTime(new Date(mPrefDB.Get_BG_MeasuredRealAcqTime_From_pref()));

			mDetector.Real_BG.Set_MeasurementDate(mPrefDB.Get_BG_Date_From_pref());
			mDetector.Real_BG.Set_Spectrum(mPrefDB.Get_BG_From_pref(), mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			mDetector.Real_BG.Set_StartSystemTime(new Date(0));
			mDetector.Real_BG.Set_EndSystemTime(new Date(mPrefDB.Get_BG_MeasuredRealAcqTime_From_pref()));

			//0207 mEventDB.Save_DeviceName(device.getName());

			if (mEventDBOper != null) {

				mDetector.MS.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
				mDetector.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
				mDetector.DB_BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
				mDetector.Real_BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);

				mDetector.DB_BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
				mDetector.Real_BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);

				mDetector.MS.setFWHM(mEventDBOper.Cry_Info.FWHM);
				mDetector.DB_BG.setFWHM(mEventDBOper.Cry_Info.FWHM);
				mDetector.Real_BG.setFWHM(mEventDBOper.Cry_Info.FWHM);

				Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
				peakInfo_bg = FindPeaksN.GetPPSpectrum_H(mDetector.Real_BG);

				mDetector.DB_BG.SetPeakInfo(peakInfo_bg);
				mDetector.Real_BG.SetPeakInfo(peakInfo_bg);
			}

			mDetector.User = mPrefDB.Get_User_From_pref();
			mDetector.Location = mPrefDB.Get_Location_From_pref();
			mDetector.AlarmSound = Get_AlarmResID(mPrefDB.Get_AlarmSound_From_pref());
			///////////////////
			mDetector.Gamma_Threshold = mPrefDB.Get_GammaThreshold_From_pref();
			mDetector.Gamma_SigmaThreshold = mPrefDB.Get_GammaThreshold_Sigma_From_pref();

			mDetector.Neutron_ThresholdCnt = mPrefDB.Get_NeutronThreshold_From_pref();
			mDetector.HealthSafety_Threshold = mPrefDB.Get_HealthyThreshold_From_pref();

			mDetector.IsSvUnit = mPrefDB.Get_IsSvUnit_From_pref();
			mDetector.IsSigmaThreshold = mPrefDB.Get_IsSigma_From_pref();

			mDetector.GMT = NcLibrary.Get_GMT();

			// if (mCurrentConnectMode != mChangeConnectMode) {
			//
			// } else {
			// mDetector.InstrumentModel_Name = "SAM 950";
			// // mDetector.InstrumentModel_Name = mPrefDB.Get_equipment_From_pref();
			// }
			if (mPrefDB.Get_IsSvUnit_From_pref() == false) {

				mDetector.HealthSafety_Threshold = (int) NcLibrary
						.Rem_To_Sv((double) mPrefDB.Get_HealthyThreshold_From_pref());
			}

			Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SET_TOSVUNIT);
			send_gs.putExtra(MainBCRReceiver.DATA_SET_TOSVUNIT, mDetector.IsSvUnit);

			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

			mDetector.Low_discrimination = mPrefDB.Get_Low_Discrimination_From_pref();
			mDetector.Upper_discrimination = mPrefDB.Get_Upper_Discrimination_From_pref();
			// -----

			Detector.MeasurementInfo info = new Detector.MeasurementInfo();
			info.Set_Info("", "", mPrefDB.Get_Location_From_pref(), mPrefDB.Get_User_From_pref(),
					Get_AlarmResID(mPrefDB.Get_AlarmSound_From_pref()), "", mPrefDB.Get_IsSvUnit_From_pref(), false);

			mDetector.IsSvUnit = mPrefDB.Get_IsSvUnit_From_pref();
			if (m_GammaGuage_Panel != null)
				m_GammaGuage_Panel.Set_toSvUnit(mDetector.IsSvUnit);

			if (mPrefDB.Get_SequenceMode_From_pref()) {

				tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
			} else {

				tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
			}

		} catch (Exception e) {

			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public int Get_AlarmResID(int ListNumber) {
		if (ListNumber == 0)
			return R.raw.warning;
		else if (ListNumber == 1)
			return R.raw.clock;
		else if (ListNumber == 2)
			return R.raw.charmingbell;
		else if (ListNumber == 3)
			return R.raw.trumpet;
		else if (ListNumber == 4)
			return R.raw.bell;
		else if (ListNumber == 5)
			return R.raw.beep1;
		else
			return 0;
	}

	boolean Accumul_Channel_forGain(int[] channel) {
		try {
			int THRESHOLD = GAIN_THRESHOLD;

			double[] mPeck = new double[2];
			mPeck[0] = mPrefDB.Get_CaliPeak1_From_pref();
			mPeck[1] = mPrefDB.Get_CaliPeak2_From_pref();
			double Be_K40_Ch = mPrefDB.Get_CaliPeak3_From_pref();

			double Be_A = mPrefDB.Get_Cali_A_From_pref();
			double Be_B = mPrefDB.Get_Cali_B_From_pref();
			double Be_C = mPrefDB.Get_Cali_C_From_pref();
			if (Be_A == 0)
				return false;

			int mROI_Ch_start = (int) NcLibrary
					.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.94, Be_A, Be_B, Be_C));
			int mROI_Ch_end = (int) NcLibrary
					.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.06, Be_A, Be_B, Be_C));

			double[] temp = new double[1024];
			temp = NcLibrary.Smooth(channel, 1024, 10, 2);

			int mStbWinCnt = (int) NcLibrary.ROIAnalysis_GetTotCnt(temp, mROI_Ch_start, mROI_Ch_end);

			if (mStbWinCnt > THRESHOLD) {
				int K40_Ch = NcLibrary.ROIAnalysis(temp, mROI_Ch_start, mROI_Ch_end);

				if (K40_Ch != 0) {
					Background_GainStabilization(Be_K40_Ch, K40_Ch);
					double Ratio = (((double) K40_Ch - (double) Be_K40_Ch) / (double) Be_K40_Ch);

					double New_Peak1 = (double) mPeck[0] + ((double) mPeck[0] * Ratio);
					double New_Peak2 = (double) mPeck[1] + ((double) mPeck[1] * Ratio);
					double[] FitParam = new double[3];
					NcLibrary.QuadraticCal(New_Peak1, New_Peak2, (double) K40_Ch, NcLibrary.CS137_PEAK1,
							NcLibrary.CS137_PEAK2, NcLibrary.K40_PEAK, FitParam);
					mPrefDB.Set_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], New_Peak1, New_Peak2,
							(double) K40_Ch);
					MainActivity.mDetector.Set_EnergyFittingArgument(FitParam);

					/*
					 * Calendar calendar = Calendar.getInstance(); Date date = calendar.getTime();
					 * Spectrum Spc = new Spectrum(); Spectrum BG = new Spectrum();
					 * Spc.Set_Spectrum(channel); BG.Set_Spectrum(mPrefDB.Get_BG_From_pref());
					 * String LogData = "\n"+date.getHours()+":"+date.getMinutes()+":"+date.
					 * getSeconds() +" _Old 3Peak Ch: "+ mPeck[0]+", "+ mPeck[1] +",  "+ Be_K40_Ch+
					 * "Ch _New 3Peak Ch: "+ New_Peak1+", "+ New_Peak2+", "+ K40_Ch +
					 * "Ch _Old ABC : "+Be_A+", " + Be_B+", "+ Be_C+ " _New ABC : "+FitParam[0]+", "
					 * + FitParam[1]+", "+ FitParam[2]+ " _Spectrum:"
					 * +Spc.ToString()+" _BG:"+BG.ToString();
					 *
					 * NcLibrary.Export_ToTextFile("Gain Stabilization", LogData);
					 */
				} else {

				}

				mGain_Sec = 0;
				// mt_Check = false;
				for (int i = 0; i < 1024; i++) {
					mStbChannel[i] = 0;
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
	}

	void Init_stabilization() {
		try {

			mGain_Sec = DEAFALUT_GAIN_SEC;
			// mt.setText("");
			// mt_Check = false;
			mStbChannel = NcLibrary.Init_ChannelArray(mStbChannel, CHANNEL_ARRAY_SIZE);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	void Background_GainStabilization(double Before_K40, double Now_K40) {

		try {
			if (Before_K40 == Now_K40)
				return;
			int[] BG = new int[CHANNEL_ARRAY_SIZE];
			int[] NewBG = new int[CHANNEL_ARRAY_SIZE];
			BG = mPrefDB.Get_BG_From_pref();

			// background adjustment
			int tempindex = 0;
			float diffgap = 0;

			float temp = 0;
			if (Now_K40 == 0)
				diffgap = 1;
			else
				diffgap = (float) Now_K40 / (float) Before_K40;

			for (int i = 0; i < CHANNEL_ARRAY_SIZE; i++) // 채널
			// 이동
			{
				tempindex = NcLibrary.Auto_floor(((float) i * diffgap));
				if (tempindex >= CHANNEL_ARRAY_SIZE)
					break;
				NewBG[tempindex] = BG[i];
			}

			for (int i = 0; i < CHANNEL_ARRAY_SIZE - 1; i++) // 이빠진곳
			// 보정
			{
				temp = NewBG[i];
				if (temp <= 0 && (i > 0 && i < CHANNEL_ARRAY_SIZE - 1)) {
					if (NewBG[i - 1] > 0 & NewBG[i + 1] > 0) {
						NewBG[i] = (NewBG[i - 1] + NewBG[i + 1]) / 2;
					}
				}
			}

			mPrefDB.Set_BG_On_pref(NewBG, CHANNEL_ARRAY_SIZE);
			MainActivity.mDetector.Real_BG.Set_Spectrum(NewBG, mPrefDB.Get_BG_MeasuredAcqTime_From_pref());
			// Save_BackGround_Data();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	/*
	 * public void Send_Default_GC_ToHW() {
	 *
	 * double mGainValue = Double.valueOf(mPrefDB.Get_GainValue()).doubleValue();
	 *
	 * NewGC = Integer.toString((int) ((double) mGainValue));
	 *
	 * mDetector.mHW_GC = (int) ((double) mGainValue);
	 *
	 * byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();
	 *
	 * byte[] ss = new byte[5]; ss[0] = 'G'; ss[1] = 'C'; // if (GcBytes.length ==
	 * 1) { ss[2] = 0; ss[3] = GcBytes[2]; } else if (GcBytes.length == 2) { ss[2] =
	 * GcBytes[0]; ss[3] = GcBytes[1]; } else if (GcBytes.length == 3) { ss[2] =
	 * GcBytes[1]; ss[3] = GcBytes[2]; } /// ss[4] = (byte) Byte.valueOf((byte) 1);
	 *
	 * try { mMainUsbService.write(ss);
	 *
	 * // mService.write(ss);
	 *
	 * } catch (Exception NullPointerException) {
	 *
	 * }
	 *
	 * // return true; }
	 */

	/*
	 * boolean Send_GC_ToHW(int NewK40Ch) {
	 *
	 * if (mDebug.IsDebugMode) {
	 *
	 * return false; }
	 *
	 * if (mDetector.mHW_K40_FxiedCh == 0) { return false; }
	 *
	 * double FixedK40 = (double) mDetector.mHW_K40_FxiedCh;
	 *
	 * double mChangeK40 = (double) FixedK40 - NewK40Ch;
	 *
	 * //mChangeK40 = mChangeK40 * 9; //YKIM, 2018.2.9, change GC factor value for
	 * each detector type // setup function: HwPmtProperty_Code() in Detector.java
	 * (instance: mDetector, value: mDetector.mGCFactor) mChangeK40 = mChangeK40 *
	 * mDetector.mGCFactor;
	 *
	 * if (FixedK40 * 0.98 < NewK40Ch && FixedK40 * 1.02 > NewK40Ch) {
	 *
	 * return false; }
	 *
	 * double y, x;
	 *
	 * NcLibrary.SaveText("\nmDetector.mHW_K40_FxiedCh :  " + FixedK40);
	 * NcLibrary.SaveText("\nmDetector.mHW_GC :  " + mDetector.mHW_GC);
	 *
	 * NewGC = Integer.toString((int) ((double) mDetector.mHW_GC + mChangeK40));
	 *
	 * NcLibrary.SaveText("\nNewGC:  " + NewGC);
	 *
	 * mDetector.mHW_GC = (int) ((double) mDetector.mHW_GC + mChangeK40);
	 *
	 * NcLibrary.SaveText("\nmDetector.mHW_GC :  " + mDetector.mHW_GC);
	 *
	 * byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();
	 *
	 * NcLibrary.SaveText("\n GcBytes.length:  " + GcBytes.length);
	 *
	 * NcLibrary.SaveText("\n GcBytes[0] :  " + GcBytes[0]);
	 * NcLibrary.SaveText("\n GcBytes[1] :  " + GcBytes[1]);
	 * NcLibrary.SaveText("\n GcBytes[2] :  " + GcBytes[2]);
	 * NcLibrary.SaveText("\n GcBytes[3] :  " + GcBytes[3]);
	 *
	 * String a = ""; for(int k =0; k<GcBytes.length; k++) { a = a +
	 * "  GcBytes["+k+"] : "+ GcBytes[k]; } NcLibrary.SaveText("\n " + a);
	 *
	 * byte[] ss = new byte[5]; ss[0] = 'G'; ss[1] = 'C'; // if (GcBytes.length ==
	 * 1) { ss[2] = 0; ss[3] = GcBytes[2]; } else if (GcBytes.length == 3) { ss[2] =
	 * GcBytes[1]; ss[3] = GcBytes[2]; } else if (GcBytes.length == 2) { ss[2] =
	 * GcBytes[0]; ss[3] = GcBytes[1]; }
	 *
	 * /// ss[4] = (byte) Byte.valueOf((byte) 1);
	 *
	 * try {
	 *
	 * if (mMainUsbService != null) { mMainUsbService.write(ss); }
	 *
	 * if (mService != null) { mService.write(ss); }
	 *
	 * } catch (Exception e) { NcLibrary.Write_ExceptionLog(e); }
	 *
	 * return true; }
	 */

	int Send_GC_ToHW(int NewK40Ch) {

		if (mDebug.IsDebugMode && mDebug.IsSendtoGCMode == false) {

			return 0;
		}

		if (mDetector.mHW_K40_FxiedCh == 0 || NewK40Ch <= 0) {
			return 0;
		}

		/*
		 * mSaveCount++;
		 *
		 * Log.d("time", "FoundK40 : " + Integer.toString(NewK40Ch) + " GcValue : " +
		 * NewGC);
		 *
		 * String mTxtBody = ""; StrArraylist.add("FoundK40 : " +
		 * Integer.toString(NewK40Ch) + " GcValue : " + NewGC); for (int i = 0; i <
		 * StrArraylist.size(); i++) { mTxtBody += StrArraylist.get(i) + "\n"; }
		 *
		 * onTextWriting("Check-Gain-GC-Temp", mTxtBody);
		 */

		double FixedK40 = (double) mDetector.mHW_K40_FxiedCh;

		double mChangeK40 = (double) FixedK40 - (double) NewK40Ch;
		double mChangeK40HH100 = ((double) FixedK40 - (double) NewK40Ch) / (double) FixedK40;
		double mChangeK40ratio = Math.abs(mChangeK40 / (double) FixedK40);

		// double FixedK40 = mDetector.mHW_K40_FxiedCh;
		// double Ratio = (((double) FixedK40 - NewK40Ch) / FixedK40);

		int status = 0;
		// YKIM, 2018.2.9, change GC factor value for each detector type
		// setup function: HwPmtProperty_Code() in Detector.java (instance: mDetector,
		// value: mDetector.mGCFactor)

		if (mChangeK40ratio <= 0.01) {
			mDetector.mGain_restTime = mGain_restTime_under1; // YKIM, 2018.2.19, rest time setup
			return status; // return 0, Not thing to do, rest time for GS 150sec
		} else if (mChangeK40ratio <= 0.02) {
			mDetector.mGain_restTime = mGain_restTime_under2;
			status = 1; // rest time for GS 60sec
		} else {
			mDetector.mGain_restTime = mGain_restTime_over2;
			status = 1; // rest time for GS 10sec
		}

		// Toast.makeText(getApplicationContext(), "count :" +
		// Integer.toString(mToastCount), 1).show();

		if (mDetector.mHW_GC > 0) {
			byte[] ss = new byte[5];
			//////////////////////////////////
			// for HH200
			if (mDetector.mHW_GC > 1024) {
				mChangeK40 = mChangeK40 * mDetector.mGCFactor;

				NewGC = Integer.toString((int) ((double) mDetector.mHW_GC + mChangeK40));
				mDetector.mHW_GC = (int) ((double) mDetector.mHW_GC + mChangeK40);
				byte[] GcBytes = new java.math.BigInteger(NewGC, 10).toByteArray();

				ss[0] = 'G';
				ss[1] = 'C';
				//
				if (GcBytes.length == 1) {
					ss[2] = 0;
					ss[3] = GcBytes[2];
				} else if (GcBytes.length == 3) {
					ss[2] = GcBytes[1];
					ss[3] = GcBytes[2];
				} else if (GcBytes.length == 2) {
					ss[2] = GcBytes[0];
					ss[3] = GcBytes[1];
				}
				///
				ss[4] = (byte) Byte.valueOf((byte) 1);
			}
			/////////////////////////////////////

			////////////////////////////////////
			// HH100 & BP100
			else {
				double New_Peak1 = (double) mDetector.mHW_GC + ((double) mDetector.mHW_GC * mChangeK40HH100);
				mDetector.mHW_GC = NcLibrary.Auto_floor(New_Peak1);
				String NewGC = Integer.toHexString(NcLibrary.Auto_floor(New_Peak1));
				byte[] GcBytes1 = new java.math.BigInteger(NewGC, 16).toByteArray();
				ss = new byte[5];
				ss[0] = 'G';
				ss[1] = 'C';

				if (GcBytes1.length == 1) {
					ss[2] = 0;
					ss[3] = GcBytes1[0];
				} else {
					ss[2] = GcBytes1[0];
					ss[3] = GcBytes1[1];
				}
				ss[4] = (byte) Byte.valueOf((byte) 1);
			}
			/////////////////////////////////


	//		NcLibrary.SaveText1( "GC, :"+mDetector.mHW_GC, "CalcROIK40");


			try {
				if (mMainUsbService != null) {
					mMainUsbService.write(ss);
				}
				if (mService != null) {
					mService.write(ss);
				}

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
		return status;
	}

	void Reset_Detector() {
		mDetector.Finish_GammaEvent();
		mDetector.Finish_NeutronEvent();

		mDetector.MS.ClearSPC();
		mDetector.Set_Mode(Detector.ID_MODE);
		mDetector.mIsNeutronModel = false;

		mDetector.InstrumentModel_Name = "None";
		mDetector.InstrumentModel_MacAddress = "None";

		mDetector.mHW_GC = 0;
		mDetector.mHW_K40_FxiedCh = 0;
		// mDetector.Set_PmtProperty(Detector.HwPmtProperty_Code.NaI_2x2);
		mDetector.Set_PmtProperty(Detector.HwPmtProperty_Code.CeBr_2x2);

		InIt_SPC_Data();

	}

	public void InIt_SPC_Data() {

		try {
			mDetector.Init_Measure_Data();
			//m_GammaGuage_Panel.SETnSv(0, 0);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	@SuppressLint("NewApi")
	void ensureDiscoverable() {

		if (mBTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	void Dismiss_ProgressDlg() {

		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}
		mProgressDialog = null;
	}

	boolean Conntect_With_LastDevice() {

		try {
			String MacAdd = mPrefDB.Get_Last_Cntd_DetectorMac();
			if (MacAdd == null)
				return false;

			BluetoothDevice device = mBTAdapter.getRemoteDevice(MacAdd);
			mService.connect(device);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
		return true;
	}

	void Start_Login_Dlg() {

		Intent intent = null;
		intent = new Intent(MainActivity.this, LoginDlg.class);
		intent.putExtra(LoginDlg.EXTRA_ADMIN_PW, mPrefDB.Get_AdminPW_From_pref());
		startActivityForResult(intent, RESULT_LOGIN);
	}

	public static byte[] hexToBytes(String hex) {
		byte[] result = null;
		if (hex != null) {
			result = new byte[hex.length() / 2];
			for (int i = 0; i < result.length; i++) {
				result[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
			}
		}
		return result;
	}

	void Message_Read_Gm(int Gm_Cnt) {

		// Log.e("GM Read", msg.obj.toString());
		MainActivity.mDetector.GM_Cnt = Gm_Cnt;
		// TextView Tv_GM = (TextView) m_MainLayout.findViewById(R.id.tv_GM);

		// Tv_GM.setText("GM: " + String.valueOf(MainActivity.mDetector.GM_Cnt)
		// + " cps "
		// + NcLibrary.GM_to_uSV(MainActivity.mDetector.GM_Cnt) + " uSv/h");

	}

	@Override
	public void onDestroy() {

		try {

			UsbBrodcastStop();
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);

			if (mDebug.IsDebugMode) {
				if (mTask != null) {
					mTask.cancel();

				}
				if (mTimer != null) {
					mTimer.cancel();
				}
			}

			if (mService != null) {

				mService.write(MESSAGE_END_HW);

			}
			if (mMainUsbService != null) {

				mMainUsbService.write(MESSAGE_END_HW);

			}

			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int date = calendar.get(Calendar.DATE);
			int hour = calendar.get(Calendar.HOUR);
			int minute = calendar.get(Calendar.MINUTE);
			int iAMPM = calendar.get(Calendar.AM_PM);
			String ampm = "";
			if (iAMPM == Calendar.AM)
				ampm = "AM";
			else
				ampm = "PM";

			mPrefDB.Set_Last_Cntd_Date(year + "/" + (month + 1) + "/" + (date >= 10 ? date : "0" + date) + "  " + ampm
					+ " " + (hour >= 10 ? hour : ("0" + hour)) + ":" + (minute >= 10 ? minute : "0" + minute));

			// ActivityManager AM = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			// AM.restartPackage(getPackageName());

			// BrodcastStop();
			// mMainUsbService.StartReceiver();
			// mMainUsbService.usbStop();

			// mMainUsbService.write(MESSAGE_END_HW);
			// mMainUsbService.usbStop();

			// UsbConnect();
			SendU4AA();
			StopUsb();

			System.exit(0);

		} catch (NullPointerException e) {
			NcLibrary.Write_ExceptionLog(e);
		}

		super.onDestroy();
	}

	@Override
	public synchronized void onResume() {

		super.onResume();

		if (mService != null) {
			if (mService.getState() == MainService.STATE_NONE) {
				mService.start();
			}
		}

		super.onNewIntent(intent);
		boolean isKill = intent.getBooleanExtra("KILL_APP", false);
		if (isKill) {
			moveTaskToBack(true);
			finish();
		}

		Check_AndMake_DeviceNameFile(true);
	}

	void Show_ProgressDlg(String messeage) {

		mProgressDialog = null;
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setTitle(getResources().getString(R.string.pleaseWait));
		mProgressDialog.setMessage(messeage);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if (MAPPING_VERSION) {
			if (mService.mState == MainService.STATE_CONNECTED)
				menu.getItem(0).setTitle(getResources().getString(R.string.main_menu1_1));
			else
				menu.getItem(0).setTitle(getResources().getString(R.string.main_menu1));

		}

		openMenu = true;
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		/**
		 * OptionMenu가 강제로 Open될 때 호출 된다.
		 *
		 */
		openMenu = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// if (mDebug.IsDebugMode && mDebug.IsDebugSendMenu) {
		//
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.main_menu3, menu);
		// } else {
		MenuInflater inflater = getMenuInflater();
		menu.clear();
		if(mDebug.hw){
			inflater.inflate(R.menu.main_menu3, menu);
		}else{
			inflater.inflate(R.menu.main_menu, menu);
		}

		// }

		return true;
	}

	@Override

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		/*	case R.id.Send_Error_Menu:
				try {

					NcLibrary.SendSystemLog(mContext);

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;*/

			case R.id.db_Menu:
				try {

					//190102 추가
					//SendU4AA();
					startActivity(new Intent(MainActivity.this, EventListActivity.class));

				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
				break;

			case R.id.calib:

				//190102 추가
				//SendU4AA();
				Intent intent = new Intent(MainActivity.this, SetupSpectrumActivity.class);
				intent.putExtra(SetupSpectrumActivity.MEASUREMENT_MODE,SetupSpectrumActivity.MEASUREMENT_EN_CALIBRATION);
				intent.putExtra(SetupSpectrumActivity.CALIB_ENDCNT, MainActivity.mPrefDB.Get_Calibration_AcqCnt());
				menuCalib = true;
				startActivity(intent);

				break;

			case R.id.setup_Menu:

				//190102 추가
				//SendU4AA();
				Intent Intent1 = null;
				Intent1 = new Intent(MainActivity.this, PreferenceActivity.class);
				startActivityForResult(Intent1, MainActivity.FINISH_SETUP_PREF);

				break;
			case R.id.Finish_Menu:
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH);
				int date = calendar.get(Calendar.DATE);
				int hour = calendar.get(Calendar.HOUR);
				int minute = calendar.get(Calendar.MINUTE);

				int iAMPM = calendar.get(Calendar.AM_PM);
				String ampm = "";
				if (iAMPM == Calendar.AM)
					ampm = "AM";
				else
					ampm = "PM";

				mPrefDB.Set_Last_Cntd_Date(year + "/" + (month + 1) + "/" + (date > 10 ? date : "0" + date) + "  " + ampm
						+ " " + (hour >= 10 ? hour : ("0" + hour)) + ":" + (minute >= 10 ? minute : "0" + minute));

				//isU4AA = true;
				//SendU4AA();
				finish();
				break;

		}
		return true;
	}

	public Handler GetHandler() {
		return mHandler;
	}

	public void tabEnable()
	{

		SetFristActiviyMode();
		tabHost.getTabWidget().getChildAt(1).setOnTouchListener(this);
		tabHost.getTabWidget().getChildAt(2).setOnTouchListener(this);

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(TAB_TEXT_SIZE);
			tv.setTextColor(Color.parseColor(TAB_ENABLE_TEXT_COLOR));
			tv.setTypeface(Typeface.SANS_SERIF);

			relLayout.setBackgroundColor(Color.parseColor("#000000"));

			relLayout.setScaleY((float) 0.8);
			tv.setScaleY((float) 1.2);
		}

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);
		}

		LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(0);
		TextView tv = (TextView) relLayout.getChildAt(1);

		tv.setTextSize(TAB_TEXT_SIZE);
		tv.setTextColor(Color.parseColor(TAB_ENABLE_TEXT_COLOR));

		tv.setTypeface(Typeface.SANS_SERIF);

		relLayout.setBackgroundResource(R.drawable.tab_line);

		// relLayout.

		// relLayout.setScaleY((float)0.80);
		relLayout.setScaleX((float) 0.93);
		tv.setScaleX((float) 1.07);
		tv.setScaleY((float) 1.2);
		// tabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.line1);
	}

	public void tabDisable() {

		Context mContext;
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(TAB_TEXT_SIZE);
			tv.setTextColor(Color.parseColor("#747474"));

			tv.setTypeface(Typeface.SANS_SERIF);

		}

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);

		}

	}

	public void NoCalDataTabDisable() {

		Context mContext;
		for (int i = 1; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) relLayout.getChildAt(1);
			tv.setTextSize(TAB_TEXT_SIZE);
			tv.setTextColor(Color.parseColor("#747474"));

			tv.setTypeface(Typeface.SANS_SERIF);

		}

		for (int i = 1; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(false);

		}

	}

	public void tabRefresh() {

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		/*
		 * // TODO Auto-generated method stub // 여기서 width를 찍어보면 값이 제대로 출력된다.
		 *
		 *
		 * switch (tabswitch) { case 0: tabBottomLayout = (LinearLayout)
		 * findViewById(R.id.tabBottomLayout);
		 *
		 * tabBottomLayout.setVisibility(View.GONE);
		 *
		 * tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout); View
		 * tabBodyLayoutView = (View) this.findViewById(R.id.tabBodyLayout);
		 *
		 * // TODO Auto-generated method stub tabBodyWidth =
		 * tabBodyLayoutView.getWidth(); tabBodyHeight = tabBodyLayoutView.getHeight();
		 *
		 * tabBodyLayout.setLayoutParams(new LinearLayout.LayoutParams(tabBodyWidth,
		 * tabBodyHeight + 68));
		 *
		 * tabswitch = 1; break;
		 *
		 * case 1: tabBottomLayout = (LinearLayout) findViewById(R.id.tabBottomLayout);
		 *
		 * tabBottomLayout.setVisibility(View.GONE);
		 *
		 * tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout); View
		 * tabBodyLayoutView1 = (View) this.findViewById(R.id.tabBodyLayout);
		 *
		 * // TODO Auto-generated method stub tabBodyWidth =
		 * tabBodyLayoutView1.getWidth(); tabBodyHeight =
		 * tabBodyLayoutView1.getHeight();
		 *
		 * tabBodyLayout.setLayoutParams(new LinearLayout.LayoutParams(tabBodyWidth,
		 * tabBodyHeight - 68));
		 *
		 *
		 * tabswitch=0; break;
		 *
		 *
		 * default: break; }
		 *
		 *
		 *
		 */}

	public void sourceIdResult() {

		SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

		tabBottomLayout = (LinearLayout) findViewById(R.id.tabBottomLayout);

		tabBottomLayout.setVisibility(View.GONE);

		tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout);
		View tabBodyLayoutView = (View) this.findViewById(R.id.tabBodyLayout);

		// TODO Auto-generated method stub
		tabBodyWidth = tabBodyLayoutView.getWidth();
		tabBodyHeight = tabBodyLayoutView.getHeight();

		// tabBodyLayout.set

		tabBodyLayout.setLayoutParams(new FrameLayout.LayoutParams(tabBodyWidth, tabBodyHeight + 72));

	}

	public void sourceIdResultCancel() {

		tabBottomLayout = (LinearLayout) findViewById(R.id.tabBottomLayout);

		tabBottomLayout.setVisibility(View.VISIBLE);

		tabBodyLayout = (LinearLayout) findViewById(R.id.tabBodyLayout);
		View tabBodyLayoutView1 = (View) this.findViewById(R.id.tabBodyLayout);

		// TODO Auto-generated method stub
		tabBodyWidth = tabBodyLayoutView1.getWidth();
		tabBodyHeight = tabBodyLayoutView1.getHeight();

		tabBodyLayout.setLayoutParams(new FrameLayout.LayoutParams(tabBodyWidth, tabBodyHeight - 72));

	}

	public void CreateMediaFile() {
		/*
		 * String sdRootPath =
		 * Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
		 * + EventDBOper.DB_FOLDER + File.separator + EventDBOper.MIDEA_FOLDER;
		 */

		String sdRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ EventDBOper.DB_FOLDER;

		for (int i = 0; i < 10; i++) {

			File file;
			file = new File(sdRootPath);
			if (!file.exists()) {

				file.mkdir();
				break;
			}

		}

	}

	public void TabChangeDraw(String TabName) {

		int count = 0;
		if (TabName.equals(Tab_Name.RealTime_Str))
			count = 0;
		if (TabName.equals(Tab_Name.ManualID_Str))
			count = 1;
		if (TabName.equals(Tab_Name.SequentialMode_Str))
			count = 2;
		if (TabName.equals(Tab_Name.En_Calibration_Str))
			count = 3;

		String str = Integer.toString(count);

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(i);

			relLayout.setBackgroundColor(Color.parseColor("#000000"));

		}

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildTabViewAt(i).setEnabled(true);
		}

		LinearLayout relLayout = (LinearLayout) tabHost.getTabWidget().getChildAt(count);
		TextView tv = (TextView) relLayout.getChildAt(1);
		tv.setTypeface(Typeface.SANS_SERIF);
		tv.setTextSize(TAB_TEXT_SIZE);
		tv.setTextColor(Color.parseColor(TAB_ENABLE_TEXT_COLOR));

		relLayout.setBackgroundResource(R.drawable.tab_line);

		relLayout.setScaleX((float) 0.93);
		tv.setScaleX((float) 1.07);
		tv.setScaleY((float) 1.2);
		tabHost.requestFocus();

		FirstActivityCurrentTab = count;

	}

	// KeyEvent

	public void HWKey(int Index, int KeyValue)
	{

		if (Index == HW_Key_Type.SHORTPRESS) {

			DoubleClickRock2 = Activity_Mode.UN_EXCUTE_MODE;
			TimerTask mTask = new TimerTask() {
				@Override
				public void run() {
					DoubleClickRock2 = Activity_Mode.EXCUTE_MODE;
				}
			};

			Timer mTimer = new Timer();
			mTimer.schedule(mTask, 300);
		}

		switch (Index) {

			case HW_Key_Type.SHORTPRESS:

				switch (KeyValue) {

					case HW_Key.Left:

						if (ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING
								|| ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_SPEC_VIEWFILPPER);
						} else {

							ForcedKeyGeneration(KeyEvent.KEYCODE_DPAD_LEFT);
						}
						break;

					case HW_Key.Right:

						if (ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING
								|| ACTIVITY_STATE == Activity_Mode.SEQUENTAL_MODE_RUNNING) {

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_SPEC_VIEWFILPPER);

						} else {

							ForcedKeyGeneration(KeyEvent.KEYCODE_DPAD_RIGHT);
						}
						break;
					case HW_Key.Back:
						if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

						} else {

							ForcedKeyGeneration(KeyEvent.KEYCODE_BACK);

						}
						break;
					case HW_Key.Up:

						ForcedKeyGeneration(KeyEvent.KEYCODE_DPAD_UP);

						break;
					case HW_Key.Down:

						if (ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_SOURCE_ID_TIMEDOWN);

						}

						ForcedKeyGeneration(KeyEvent.KEYCODE_DPAD_DOWN);

						break;
					case HW_Key.Enter:
						count++;
						ForcedKeyGeneration(KeyEvent.KEYCODE_ENTER);
						break;

					default:
						break;
				}
			case HW_Key_Type.LONGPRESS:
				if (DoubleClickRock2 == Activity_Mode.EXCUTE_MODE)

				{
					switch (KeyValue) {
						case HW_Key.Left:

							break;
						case HW_Key.Back:

							ForcedKeyGeneration(KeyEvent.KEYCODE_MENU);
							// KeyValue = "Back";
							break;
						case HW_Key.Right:
							// KeyValue = "Right";
							break;
						case HW_Key.Up:

							// KeyValue = "Up";
							break;
						case HW_Key.Down:
							// KeyValue = "Down";
							break;
						case HW_Key.Enter:

							if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY && IsThere_CalibrationInfo()) {
								// 180712 메뉴키가 열려있으면 강제적으로 백버튼 눌른효과 준 후 화면 이동
								if (openMenu) {
									// menuTemp = false;
									ForcedKeyGeneration(KeyEvent.KEYCODE_BACK);
								}

								tabHost.setCurrentTab(Tab_Name.MenualID);
							} else if ((ACTIVITY_STATE != Activity_Mode.FIRST_ACTIVITY)) {

								longPress(KeyEvent.KEYCODE_ENTER);

							}
							// longPress(KeyEvent.KEYCODE_ENTER);
							// KeyValue = "Menu";

					}
				}
				break;

			default:
				break;

		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {

			case KeyEvent.KEYCODE_DPAD_RIGHT: {

				if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

					++FirstActivityCurrentTab;

					if (mPrefDB.Get_SequenceMode_From_pref() && IsThere_CalibrationInfo()) {

						if (FirstActivityCurrentTab > Tab_Name.SequentialMode) {
							FirstActivityCurrentTab = Tab_Name.SequentialMode;
						}

						switch (FirstActivityCurrentTab) {

							case Tab_Name.Reatime:
								TabChangeDraw(Tab_Name.RealTime_Str);

								break;
							case Tab_Name.MenualID:
								TabChangeDraw(Tab_Name.ManualID_Str);

								break;

							case Tab_Name.SequentialMode:
								TabChangeDraw(Tab_Name.SequentialMode_Str);

								break;

							default:
								break;
						}

					} else if (IsThere_CalibrationInfo() && mPrefDB.Get_SequenceMode_From_pref() == false) {

						if (FirstActivityCurrentTab > Tab_Name.MenualID) {
							FirstActivityCurrentTab = Tab_Name.MenualID;
						}

						switch (FirstActivityCurrentTab) {

							case Tab_Name.Reatime:
								TabChangeDraw(Tab_Name.RealTime_Str);

								break;
							case Tab_Name.MenualID:
								TabChangeDraw(Tab_Name.ManualID_Str);

								break;

							default:
								break;
						}

					} else if (IsThere_CalibrationInfo() == false) {

						if (FirstActivityCurrentTab > Tab_Name.Reatime) {
							FirstActivityCurrentTab = Tab_Name.Reatime;
						}

					}

					return true;
				} else {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						KeyExecute(KeyEvent.KEYCODE_DPAD_RIGHT);
						return false;
					}

				}

				return true;
			}

			case KeyEvent.KEYCODE_DPAD_LEFT: {

				if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
					--FirstActivityCurrentTab;

					if (mPrefDB.Get_SequenceMode_From_pref()) {
						if (FirstActivityCurrentTab < Tab_Name.Reatime) {
							FirstActivityCurrentTab = Tab_Name.Reatime;
						}

						switch (FirstActivityCurrentTab) {

							case Tab_Name.Reatime:

								TabChangeDraw(Tab_Name.RealTime_Str);

								break;
							case Tab_Name.MenualID:
								TabChangeDraw(Tab_Name.ManualID_Str);

								break;
							case Tab_Name.SequentialMode:
								TabChangeDraw(Tab_Name.SequentialMode_Str);

								break;

							default:
								break;
						}
					} else {
						if (FirstActivityCurrentTab < Tab_Name.Reatime) {
							FirstActivityCurrentTab = Tab_Name.Reatime;
						}

						switch (FirstActivityCurrentTab) {

							case Tab_Name.Reatime:

								TabChangeDraw(Tab_Name.RealTime_Str);

								break;
							case Tab_Name.MenualID:
								TabChangeDraw(Tab_Name.ManualID_Str);

								break;

							default:
								break;
						}

					}

				} else {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						KeyExecute(KeyEvent.KEYCODE_DPAD_LEFT);
						return false;
					}

				}

				return true;
			}

			case KeyEvent.KEYCODE_DPAD_UP: {

				if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

					switch (realTimeSwitch) {
						case 0:

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_MOVE_MAIN_NEXTFLIPPER);

							realTimeSwitch = 1;
							break;
						case 1:

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_MOVE_MAIN_PREFLIPPER);

							realTimeSwitch = 0;
							break;

						default:
							break;
					}

				} else {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						KeyExecute(KeyEvent.KEYCODE_DPAD_UP);
						return false;
					}

				}

				return true;
			}

			case KeyEvent.KEYCODE_DPAD_DOWN: {

				if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

					switch (realTimeSwitch) {
						case 0:

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_MOVE_MAIN_NEXTFLIPPER);

							realTimeSwitch = 1;
							break;
						case 1:

							SendBroadcast(getApplicationContext(), MainBroadcastReceiver.MSG_MOVE_MAIN_PREFLIPPER);

							realTimeSwitch = 0;
							break;

						default:
							break;
					}

				} else {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						KeyExecute(KeyEvent.KEYCODE_DPAD_DOWN);
						return false;
					}

				}

				return true;
			}

			case KeyEvent.KEYCODE_ENTER: {

				if (FirstActivityCurrentTab != Tab_Name.Reatime) {

					tabHost.setCurrentTab(FirstActivityCurrentTab);

					return true;
				} else {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						count++;

						KeyExecute(KeyEvent.KEYCODE_ENTER);
						return false;
					}

				}

				return true;
			}

			case KeyEvent.KEYCODE_MENU: {

				if (ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						KeyExecute(KeyEvent.KEYCODE_MENU);
						return false;
					}

				} else if (ACTIVITY_STATE != Activity_Mode.FIRST_ACTIVITY) {

					if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {
						KeyExecute(KeyEvent.KEYCODE_BACK);
						return false;
					}

				}

				return true;
			}

			case KeyEvent.KEYCODE_VOLUME_UP: {

				if (MainActivity.mDetector.Is_Event()) {
					AudioManager ssq = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
					if (ssq.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
						// Stop_Vibrate();
					}
				}

				return true;
			}
			case KeyEvent.KEYCODE_VOLUME_DOWN: {

				if (MainActivity.mDetector.Is_Event()) {
					AudioManager ssq = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
					if (ssq.getStreamVolume(AudioManager.STREAM_MUSIC) == 1) {
						// Start_Vibrate();
					}
				}

				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void longPress_old(final int key) {

		new Thread(new Runnable() {

			public void run() {

				long downTime = SystemClock.uptimeMillis();
				long eventTime = SystemClock.uptimeMillis();

				KeyEvent event1 = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, key, 0);

				KeyEvent event2 = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, key, 1);

				new Instrumentation().sendKeySync(event1);

				new Instrumentation().sendKeySync(event2);

			}
		}).start();

	}

	private void longPress(final int key) {

		new Thread(new Runnable() {

			public void run() {

				long downTime = SystemClock.uptimeMillis();
				long eventTime = SystemClock.uptimeMillis();

				KeyEvent event1 = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 0);

				KeyEvent event2 = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 1);

				new Instrumentation().sendKeySync(event1);

				new Instrumentation().sendKeySync(event2);

			}
		}).start();

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

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		/*
		 * if (v.getContext() == tabHost.getTabWidget().getChildAt(1).getContext()) { if
		 * (event.getAction() == MotionEvent.ACTION_DOWN) { // mPreTouchPosX = (int)
		 * event.getX(); } if (event.getAction() == MotionEvent.ACTION_UP) { int
		 * nTouchPosX = (int) event.getX();
		 *
		 * int nTouchPosY = (int) event.getY();
		 *
		 * String str = Integer.toString(nTouchPosX);
		 *
		 * String str2 = Integer.toString(nTouchPosY);
		 *
		 *
		 *
		 * //Toast.makeText(getApplicationContext(), "X: " + nTouchPosX + ", Y:" +
		 * nTouchPosY, 1).show();
		 *
		 * } }
		 */
		return false;
	}

	@Override
	protected void onPause() {

		Stop_Vibrate();
		Stop_Alarm();

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

		layoutParams.screenBrightness = (float) 1.0;

		getWindow().setAttributes(layoutParams);

		super.onPause();
	}

	private void onTextWriting(String title, ArrayList<String> body) {
		File file;

		String path = Environment.getExternalStorageDirectory().getAbsolutePath();

		try {

			// 파일 객체 생성
			File file1 = new File(path + File.separator + title + ".txt");

			// true 지정시 파일의 기존 내용에 이어서 작성
			FileWriter fw = new FileWriter(file1, true);

			// 파일안에 문자열 쓰기
			for (int i = 0; i < body.size(); i++) {

				fw.write(body.get(i) + "\n");
			}
			fw.flush();

			// 객체 닫기
			fw.close();

			Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}
	public void BrodcastDeclare() {

		IntentFilter filter = new IntentFilter();

		filter.addAction(MainBroadcastReceiver.MSG_EN_CALIBRATION);
		filter.addAction(MainBroadcastReceiver.MSG_REMEASURE_BG);
		filter.addAction(MainBroadcastReceiver.MSG_MANUAL_ID);

		filter.addAction(MainBroadcastReceiver.MAIN_DATA_SEND1);
		filter.addAction(MainBroadcastReceiver.MSG_EVENT);

		filter.addAction(MainBroadcastReceiver.MSG_HEALTH_EVENT);

		filter.addAction(MainBroadcastReceiver.MSG_MANUAL_ID);

		filter.addAction(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);

		filter.addAction(MainBroadcastReceiver.START_ID_MODE);

		filter.addAction(MainBroadcastReceiver.START_SETUP_MODE);

		filter.addAction(MainBroadcastReceiver.MSG_SETTIONG_TAB_BACKGROUND);

		filter.addAction(MainBroadcastReceiver.MSG_SETTION_TAB_CALIBRATION);

		filter.addAction(MainBroadcastReceiver.MSG_TAB_ENABLE);

		filter.addAction(MainBroadcastReceiver.MSG_TAB_DISABLE);

		filter.addAction(MainBroadcastReceiver.MSG_USB_CONNECTED);

		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT);

		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT_CANCEL);

		filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_CANCEL);

		filter.addAction(MainBroadcastReceiver.MSG_STOP_HEALTH_ALARM);

		filter.addAction(MainBroadcastReceiver.MSG_POWER_DISCONNECT);

		filter.addAction(MainBroadcastReceiver.MSG_USB_DISCONNECT);

		filter.addAction(MainBroadcastReceiver.MSG_FIXED_GC_SEND);

		filter.addAction(MainBroadcastReceiver.MSG_FIXED_HWCALI_SEND);

		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);


		// USB_DECLEAR

		LocalBroadcastManager.getInstance(mContext).registerReceiver(mMainBCR, filter);
	}

	public void BrodcastStop() {

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);

	}

	public static void SendU2AA() {

		if (mMainUsbService != null) {
			try {
				mMainUsbService.SendU2AA();
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	public static void SendU4AA() {

		if (mMainUsbService != null) {
			try {
				mMainUsbService.SendU4AA();
			} catch (NullPointerException e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}

	}

	public void StopUsb() {

		if (mMainUsbService != null) {
			mMainUsbService.usbStop();
		}

	}

	// Add1

	public void UsbBrodcastStop() {

		if (mMainUsbService != null) {
			mMainUsbService.UsbBrodcastStop();
		}
	}

	public void DoubleClickRock() {

		DoubleClickRock = Activity_Mode.UN_EXCUTE_MODE;
		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				DoubleClickRock = Activity_Mode.EXCUTE_MODE;
			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 500);

	}

	public void VolumeUp() {

		audio.setStreamVolume(AudioManager.STREAM_RING, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
				AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_SYSTEM,
				(int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0.65), AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_ALARM, (int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0),
				AudioManager.FLAG_PLAY_SOUND);

		audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
				(int) (audio.getStreamMaxVolume(AudioManager.STREAM_RING) * 0), AudioManager.FLAG_PLAY_SOUND);

	}

	public void BluetoothListExcute() {

		try {

			/*
			 * if(mCCSW_Service == null){ String ip = mPrefDB.Get_MappingServer_IP();
			 * if(ip==null) { Toast.makeText(getApplicationContext(), "IP가 설정 되어있지 않습니다.",
			 * Toast.LENGTH_SHORT).show(); break;} mCCSW_Service = new
			 * CcswService(mHandler); mCCSW_Service.connect(ip); } else{ Location loc =
			 * Get_Location(); MappingData data = new MappingData();
			 * data.Set_Coordinate(loc.getLatitude(), loc.getLongitude());
			 * data.InstrumentName = "sadf"; data.InstrumentMacAddress = "123";
			 * data.Doserate = 124512; mCCSW_Service.Set_Data(null,data); }
			 */

			/* if (GetDoubleConnectCheck() == false) { */

			if (mService == null)
				mService = new MainService(this, mHandler);

			if (mService.mState == MainService.STATE_CONNECTED & mCCSW_Service == null & MAPPING_VERSION == true) { // try
				// to
				// connect
				// server
				String ip = mPrefDB.Get_MappingServer_IP();

				if (ip == null) {
					Toast.makeText(getApplicationContext(), "The IP is not set..", Toast.LENGTH_SHORT).show();
					// break;
				}

				mCCSW_Service = new CcswService(mHandler);
				mCCSW_Service.connect(ip);

				Toast.makeText(getApplicationContext(), "Attempting to connect to the server", Toast.LENGTH_SHORT).show();
			} else {
				/* if (mDetector.isIdMode()) { */
				if (false) {
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.now_running),
							Toast.LENGTH_LONG).show();
					// break;
				}
				Intent serverIntent = null;
				serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
			/* } */
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	private void DeleteDlg() {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
		dialogBuilder.setTitle(getResources().getString(R.string.bluetooth_fail));
		dialogBuilder.setMessage(getResources().getString(R.string.bluetooth_retry_msg));
		dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				BluetoothListExcute();

			}
		});
		dialogBuilder.setNegativeButton("Cancel", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();

	}

	public void SetFristActiviyMode() {

		ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;

	}


	public static int[] testspectrum(String mSpec, String SplitUnit) {

		String[] mSpecSplit = mSpec.split(SplitUnit);
		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {
			double a = Double.valueOf(mSpecSplit[i]).doubleValue();

			mSpecInt[i] = (int) (a);

		}

		return mSpecInt;
	}

	public int[] testspectrum_background(String mSpec, String SplitUnit) {

		String[] mSpecSplit = mSpec.split(SplitUnit);

		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {

			mSpecInt[i] = (int) (Double.parseDouble(mSpecSplit[i]));

		}

		return mSpecInt;
	}

	public void StartVisualConnect() {

		if (mDebug.IsSetSpectrumExcute) {
			Setting();
			StartVirsutal();
			StartGCVirsutal();
		}

	}

	public void Setting() {
		if (mDebug.IsDebugMode) {
			if (mDebug.IsBattEnalbe) {
				mBatteryProgBar.Set_Value(100);

				mBatteryProgBar.invalidate();
				Battery.setText(String.valueOf((int) 100) + " %");

			}
		}
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String bg_date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + "T" + date.getHours() + ":" + date.getMinutes() + ":"
				+ date.getSeconds() + "." + MSec + NcLibrary.Get_GMT();

		String mBgSpectrum = "";

		// mDebug.SpecInfo = new Th232_02();
		mBgSpectrum = mDebug.getSpecInfo().GetBackground();

		double[] avg = new double[] { mDebug.getSpecInfo().Coefficients()[0], mDebug.getSpecInfo().Coefficients()[1],
				mDebug.getSpecInfo().Coefficients()[2] };

		double cs137, cs137_2, K1462;

		cs137 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK1, avg);
		cs137_2 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK2, avg);
		K1462 = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, avg);

		mPrefDB.Set_BG_MeasuredAcqTime_From_pref(60);
		mPrefDB.Set_BG_On_pref(testspectrum_background(mBgSpectrum, ","), 1024);
		mPrefDB.Set_BG_Date_From_pref(bg_date);

		mPrefDB.Set_ABC_From_pref(avg);

		mPrefDB.Set_Calibration_Result(avg[0], avg[1], avg[2], (int) cs137, (int) cs137_2, (int) K1462);

		mLogin = LoginDlg.LOGIN_ADMIN;

		Update_StatusBar();

	}

	public int[] testspectrum2(String mSpec) {

		String[] mSpecSplit = mSpec.split(", ");

		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {

			mSpecInt[i] = (int) Double.parseDouble(mSpecSplit[i]);

		}

		return mSpecInt;
	}

	public int[] testspectrum1(String mSpec) {

		String[] mSpecSplit = mSpec.split(" ");

		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {

			mSpecInt[i] = Integer.parseInt(mSpecSplit[i]);

		}

		return mSpecInt;
	}

	public void StartGCVirsutal() {

		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {

				GCData mGCData = new GCData();
				mHandler.obtainMessage(MainMsg.MESSAGE_USB_READ_GC, 1024, 500, mGCData).sendToTarget();

			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 1000);

	}

	public void StartVirsutal() {

		mTask = new TimerTask() {

			@Override
			public void run() {

				SpecCnt++;
				if (SpecCnt >= mDebug.getSpecInfo().GetSource().length - 1) {
					SpecCnt = 0;

				}

				specstr = mDebug.getSpecInfo().GetSource()[SpecCnt];

				int mNeutron = mDebug.getSpecInfo().GetNeutron()[SpecCnt];

				ReadDetectorData mReadData = new ReadDetectorData();
				mReadData.pdata = testspectrum(specstr, ",");
				mReadData.Neutron = mNeutron;

				MainActivity.this.mNeutron.add((double) mReadData.Neutron);

				mReadData.GetAVGNeutron = GetAVGNeutron();

				mHandler.obtainMessage(MainMsg.MESSAGE_READ_DETECTOR_DATA, 0, 0, mReadData).sendToTarget();

			}
		};

		mTimer = new Timer();
		mTimer.schedule(mTask, 1000, 1000);

	}

	private double GetAVGNeutron() {
		while (true) {
			if (mNeutron.size() > MainUsbService.NEUTRON_ACCUM_SEC)
				mNeutron.remove(0);
			else
				break;
		}
		double Avg = 0;
		for (int i = 0; i < mNeutron.size(); i++) {
			Avg += mNeutron.get(i);
		}
		Avg = Avg / MainUsbService.NEUTRON_ACCUM_SEC;
		return Avg;
	}

	public void DefaultSettingCalAndMail(PreferenceDB mPrefDB) {

		/*
		 * try {
		 *
		 * if (mDebug.IsDebugMode) { if (mDebug.IsMailDefaultSetting) {
		 * mPrefDB.Set_sender_Server("mail.nucaremed.com");
		 * mPrefDB.Set_sender_Port("587"); mPrefDB.Set_sender_pw("1dlghdwo");
		 * mPrefDB.Set_sender_email("hongjae.lee@nucaremed.com");
		 * mPrefDB.Set_recv_address("hongjae.lee@nucaremed.com");
		 *
		 * if (mLog.D) Log.i(mLog.MainActivity, mLog.MainActivity +
		 * " DefaultSettingCalAndMail() IsMailDefaultSetting excute"); } } } catch
		 * (Exception e) { NcLibrary.Write_ExceptionLog(e); }
		 */
		// modification
		if (IsThere_CalibrationInfo()) {

		} else {
			try {
				GainK40Reset();

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
				double A = 1.8930099519510156E-4;
				double B = 2.783258244516379;
				double C = 4.2034586689394295;

				double[] avg = new double[] { A, B, C };
				double cs137, cs137_2, K1462;
				cs137 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK1, avg);
				cs137_2 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK2, avg);
				K1462 = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, avg);

				mPrefDB.Set_Calibration_Result(A, B, C, (int) cs137, (int) cs137_2, (int) K1462);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_found_calibration),
						Toast.LENGTH_LONG).show();

				// NoCalDataTabDisable();

			}

		}

	}

	public void ForcedKeyGeneration(final int KeyEvent) {
		try
		{
			new Thread(new Runnable() {

				public void run() {

					new Instrumentation().sendKeyDownUpSync(KeyEvent);
				}
			}).start();

		}
		catch (Exception e)
		{

		}
	}

	public static void inputKeyEvent(final int KeyEvent) {
		try {
			int keyCode = KeyEvent;
			try {
				Instrumentation m_Instrumentation = new Instrumentation();
				m_Instrumentation.sendKeyDownUpSync(keyCode);
			} catch (SecurityException e) {
				NcLibrary.Write_ExceptionLog(e);
				try {
					Process processKeyEvent = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(processKeyEvent.getOutputStream());
					os.writeBytes("input keyevent " + keyCode + "\n");
				} catch (IOException e1) {
					NcLibrary.Write_ExceptionLog(e1);
				}

			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void SendBroadcast(Context mContext, String SendTitleMsg) {

		Intent send_gs = new Intent(SendTitleMsg);

		LocalBroadcastManager.getInstance(mContext).sendBroadcast(send_gs);

	}

	public void GainK40Reset() {

		double DefaultK40 = 500;

		final double PEACK1e = 32.0;
		final double PEACK2e = 661.660;
		double K40e = 1461;

		Vector<EventData> mAllOne = new Vector<EventData>();
		EventDBOper mEventDB = new EventDBOper();
		mEventDB.OpenDB();
		mAllOne = mEventDB.Load_One_Event();
		Coefficients mAvg = mAllOne.get(0).MS.Get_Coefficients();

		double[] avg = mAvg.get_Coefficients();

		mEventDB.EndDB();

		double cs137, cs137_2, K1462;
		cs137 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK1, avg);
		cs137_2 = NcLibrary.Energy_to_Channel(NcLibrary.CS137_PEAK2, avg);
		K1462 = NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, avg);

		double Ratio = (((double) DefaultK40 - (double) K1462) / (double) K1462);
		double New_Peak1 = (double) cs137 + ((double) cs137 * Ratio);
		double New_Peak2 = (double) cs137_2 + ((double) cs137_2 * Ratio);

		double[] FitParam = new double[3];
		NcLibrary.QuadraticCal(New_Peak1, New_Peak2, (double) DefaultK40, PEACK1e, PEACK2e, K40e, FitParam);
		mPrefDB.Set_Calibration_Result(FitParam[0], FitParam[1], FitParam[2], (int) New_Peak1, (int) New_Peak2,
				(int) DefaultK40);

	}

	public void Write_HW_Calibration_Result(GCData mGCData) {

		double[] PeakCh = new double[] { (double) mGCData.Cs137_Ch1, (double) mGCData.Cs137_Ch2,
				(double) mGCData.K40_Ch };
		double[] FitParam = new double[3];

		NcLibrary.QuadraticCal(PeakCh[0], PeakCh[1], PeakCh[2], NcLibrary.CS137_PEAK1, NcLibrary.CS137_PEAK2,
				NcLibrary.K40_PEAK, FitParam);

		mPrefDB.Set_CryStalType_Name_pref(mEventDBOper.Cry_Info.Crystal_Name);

		mPrefDB.Set_CryStalType_Number_pref(Integer.toString(mGCData.DetType));

		mPrefDB.Set_HW_ABC_From_pref(FitParam, PeakCh);
		mPrefDB.Get_HW_CaliPeakCh1_From_pref();
		mPrefDB.Get_HW_CaliPeakCh3_From_pref();
		mPrefDB.Get_HW_CaliPeakCh2_From_pref();

		mPrefDB.Get_HW_CaliPeak1_From_pref();
		mPrefDB.Get_HW_CaliPeak2_From_pref();
		mPrefDB.Get_HW_CaliPeak3_From_pref();

	}

	//1023

	class CUncaughtExceptionHandlerApp implements Thread.UncaughtExceptionHandler
	{
		@Override
		public void uncaughtException(Thread trd, Throwable ex)
		{
			// 예외 상황 처리
			//Logger.log("APP CRASH!!", GetStackTrace(ex));
			try
			{
				Log.e("MainActivity","App Down!!....Exceprion!!");
				NcLibrary.Write_ExceptionLog(GetStackTrace(ex));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				NcLibrary.Write_ExceptionLog(e);
			}
			// 예외 처리 않고 default로 넘김
			mHnderUncaughException.uncaughtException(trd, ex);
		}
	}

	private String GetStackTrace(Throwable th)
	{

		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);

		Throwable cause = th;
		while (cause != null)
		{
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		final String stacktraceAsString = result.toString();
		printWriter.close();

		return stacktraceAsString;
	}

}
