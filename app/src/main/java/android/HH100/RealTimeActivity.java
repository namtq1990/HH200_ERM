
package android.HH100;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import android.HH100.MainActivity.Activity_Mode;
import android.HH100.Control.GpsInfo2;
import android.HH100.Control.GuageView;
import android.HH100.Control.RealActivitySpectrumView;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Service.AlarmValue;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Service.Crystal_1_5x1_5;
import android.HH100.Service.Crystal_2x2;
import android.HH100.Service.Crystal_3x3;
import android.HH100.Service.*;
import android.HH100.Service.VersionUpdate;
import android.HH100.Structure.Detector;
import android.HH100.Structure.Detector.HwPmtProperty_Code;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.SingleMediaScanner;
import android.HH100.Structure.Spectrum;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

//VISIBLESTARTBUTTON
//MESSAGE_READ_DETECTOR_DATA
//뷰플리퍼
//UIPart1
//UIPart2
//핸들러부분
//메뉴부분
//리스너부분

public class RealTimeActivity extends Activity implements View.OnTouchListener {
	/** Called when the activity is first created. */

	public static final boolean D = false;
	public static final boolean E = false;

	private static final String TAG = "Real Activity";

	public static float TEXT_SIZE = 0;

	private LinearLayout m_MainLayout;

	GuageView m_GammaGuage_Panel;

	private RealActivitySpectrumView mFinder;

	private ImageView m_Bluetooth_Status;

	private ViewFlipper mMainFlipper;
	private int mPreTouchPosX;

	public static EventDBOper mEventDB;
	static Context context;
	public static int Logcount = 0;
	private TextView mt = null;
	private TextView GaugeViewCpsTxt = null;
	private boolean mt_Check = false;
	PreferenceDB mPrefDB = null;
	FrameLayout.LayoutParams param;

	//20.02.19
	private LinearLayout layoutGM;
	private TextView gm,gmTxt;


	boolean connectCheck = false;

	TextView NeutronTxt, NeutronUnderNameTxt, NeutronLineTxt, NeutronCpsTxt, mMoveImgTxt;

	ImageView mMoveImg;

	int mRealTimeCnt = 0;

	int count = 0;

	int mTimeCount = 0;

	ArrayList<String> mNsvDoubleArray = new ArrayList<String>();

	private Vector<Double> mNsv_AVG = new Vector<Double>();
	private Vector<Double> mGamma_AVG = new Vector<Double>();
	private Vector<Integer> mGammaCPS_AVG = new Vector<Integer>();
	// 리스너부분

	public class MainBCRReceiver extends MainBroadcastReceiver {

		@Override
		public void onReceive(Context context, android.content.Intent intent) {

			try {
				// PreferenceDB prefDB = new
				// PreferenceDB(getApplicationContext());

				String action = intent.getAction();
				switch (action) {
					case MSG_RECV_SPECTRUM:
						/*
						 * Calendar calendar = Calendar.getInstance();
						 *
						 * int hour = calendar.get(Calendar.HOUR_OF_DAY); int minute =
						 * calendar.get(Calendar.MINUTE); int second = calendar.get(Calendar.SECOND);
						 *
						 * long now = System.currentTimeMillis();
						 *
						 * String time1 = Long.toString(now);
						 *
						 * Log.d("time:", Integer.toString(minute)+":"+Integer.toString(second));
						 */

						// Log.d("Recv_Spectrum:", "RealTime : ");

						mRealTimeCnt++;
						if (MainActivity.ACTIVITY_STATE == MainActivity.Activity_Mode.FIRST_ACTIVITY) {

							if (mRealTimeCnt > 1) {
								if (D)
									Log.d(TAG, "Receive Broadcast - Spectrum");
								Spectrum spc = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);
								int gm = intent.getIntExtra(DATA_GM, 0);

								if(MainActivity.mDebug.hw){
									gmTxt.setText(String.valueOf(MainActivity.mDetector.GM_Cnt));

								}

								int a = Detector.HealthSafety_Threshold;

								if (mMainFlipper.getDisplayedChild() == 0) {

									/*
									 * mNsv_AVG.add(MainActivity.mDetector. Get_Gamma_DoseRate_nSV()); if
									 * (mNsv_AVG.size() > 30) { mNsv_AVG.remove(0); } double Avg = 0; for (int i =
									 * 0; i < mNsv_AVG.size(); i++) { Avg += mNsv_AVG.get(i); } Avg = Avg / 30;
									 */
									double doserate=MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
									int gammacps= MainActivity.mDetector.Get_GammaCPS();
									double avg_doserate=0;
									int avg_gammacps=0;



									//Y.Kim Add dose rate and cps data into the Vector for average
									mGamma_AVG.add(doserate);
									if (mGamma_AVG.size() > 3)
									{	mGamma_AVG.remove(0);
									}
									mGammaCPS_AVG.add(gammacps);
									if (mGammaCPS_AVG.size() > 3)
									{	mGammaCPS_AVG.remove(0);
									}

									//Y.Kim calculate average dose rate and cps
									int datasize=mGamma_AVG.size();
									for (int i = 0; i < datasize; i++)
									{	avg_doserate += mGamma_AVG.get(i);
									}
									avg_doserate = avg_doserate / datasize;

									datasize=mGammaCPS_AVG.size();
									for (int i = 0; i < datasize; i++)
									{	avg_gammacps += mGammaCPS_AVG.get(i);
									}
									avg_gammacps = (int)((double)avg_gammacps / (double)datasize);

									m_GammaGuage_Panel.SETnSv(avg_doserate, avg_gammacps);

									//m_GammaGuage_Panel.SETnSv(MainActivity.mDetector.Get_Gamma_DoseRate_nSV(),
									//		MainActivity.mDetector.Get_GammaCPS());

									// String nsv =
									// Double.toString(MainActivity.mDetector.Get_Gamma_DoseRate_nSV()/1000);

									String Cps = Integer.toString(MainActivity.mDetector.Get_GammaCPS());

									mTimeCount++;

									if (mTimeCount < 61) {
										// Log.d("CPS", "Default CPS; ; ; ; ; ; ; ;
										// " + nsv + " ;");
									}

									//NcLibrary.SaveText1("mDetector.Get_GammaCPS : "+MainActivity.mDetector.Get_GammaCPS()+"\n","test");
									m_GammaGuage_Panel.invalidate();
									Update_MoveInfo(spc.Get_TotalCount());
									AlarmValue CPS = null;

									switch (spc.Get_CrystalType()) {
										case HwPmtProperty_Code.NaI_3x3:
											CPS = new Crystal_3x3();
											break;
										case HwPmtProperty_Code.LaBr_3x3:
											CPS = new Crystal_3x3();
											break;
										case HwPmtProperty_Code.CeBr_3x3:
											CPS = new Crystal_3x3();
											break;
										case HwPmtProperty_Code.NaI_2x2:
											CPS = new Crystal_2x2();
											break;
										case HwPmtProperty_Code.CeBr_2x2:
											CPS = new Crystal_2x2();
											break;
										case HwPmtProperty_Code.LaBr_2x2:
											CPS = new Crystal_2x2();
											break;
										case HwPmtProperty_Code.CeBr_1_5x1_5:
											CPS = new Crystal_1_5x1_5();
											break;
										case HwPmtProperty_Code.LaBr_1_5x1_5:
											CPS = new Crystal_1_5x1_5();
											break;
										case HwPmtProperty_Code.NaI_1_5x1_5:
											CPS = new Crystal_1_5x1_5();
											break;
										case HwPmtProperty_Code.NaI_1x1:
											CPS = new Crystal_1_5x1_5();
											break;
										case HwPmtProperty_Code.NaI_2x4x16:
											CPS = new Crystal_4x4x16();
											break;
										case HwPmtProperty_Code.NaI_3x5x16:
											CPS = new Crystal_4x4x16();
											break;
										case HwPmtProperty_Code.NaI_4x4x16:
											CPS = new Crystal_4x4x16();
											break;
										default:
											CPS = new Crystal_3x3();
											break;
									}

									if (Detector.HealthSafety_Threshold * 1000 > Detector.mGammaDoserate) {
										if (spc.Get_TotalCount() > 0 && spc.Get_TotalCount() <= CPS.Get_Move_Forward()) {

											mMoveImgTxt.setText(getString(R.string.move_forward));
											mMoveImg.setBackgroundResource(R.drawable.msg_forward);

										} else if (spc.Get_TotalCount() > CPS.Get_Move_Forward()
												&& spc.Get_TotalCount() <= CPS.Get_In_Range()) {

											mMoveImgTxt.setText(getString(R.string.in_range));
											mMoveImg.setBackgroundResource(R.drawable.msg_inrange);

										} else if (spc.Get_TotalCount() > CPS.Get_Move_Back()) {

											mMoveImgTxt.setText(getString(R.string.move_back));
											mMoveImg.setBackgroundResource(R.drawable.msg_back);

										}
									} else {

										mMoveImgTxt.setText(getString(R.string.danger));
										mMoveImg.setBackgroundResource(R.drawable.msg_rad);

									}

								} else
								{
									mFinder.Set_Data(MainActivity.mDetector.RealTime_CPS);
									//NcLibrary.SaveText1("mDetector.Get_GammaCPS1 : "+MainActivity.mDetector.Get_GammaCPS()+"\n","test");
									GaugeViewCpsTxt.setText(Integer.toString(MainActivity.mDetector.Get_GammaCPS()));
									mFinder.invalidate();
								}
							}
						}
						break;
					case MSG_RECV_USB_NEUTRON:

						// if (MainActivity.mDetector.mIsNeutronModel) {
						double neutron = intent.getDoubleExtra(DATA_NEUTRON, 2);

						NeutronLineTxt.setVisibility(View.VISIBLE);
						NeutronCpsTxt.setVisibility(View.VISIBLE);
						NeutronTxt.setVisibility(View.VISIBLE);
						NeutronUnderNameTxt.setVisibility(View.VISIBLE);

						// String str = Double.toString(neutron);

						// Toast.makeText(getApplicationContext(), str, 1).show();

						//181129 수정 neutron 0.08이하시 0으로 표시되는거 삭제됨으로 데이터 값 바로 찍음
						String neutronStr = String.format("%.1f", neutron);
						NeutronTxt.setText(neutronStr);
/*
				if (MainActivity.mDetector.Neutron_ThresholdCnt < neutron) {

						String neutronStr = String.format("%.1f", neutron);
						NeutronTxt.setText(neutronStr);
					} else {
						// String neutronStr = String.format("%.1f", 0.0);

						NeutronTxt.setText("0.0");

					}
*/

						// Update_NeutronCPS_Text(neutron.Get_CPS());
						// }

						break;

					case MSG_START_ID_MODE:

						// Toast.makeText(getApplicationContext(), "START_ID_MODE",
						// 1).show();

						/*
						 * Start_ID_mode();
						 *
						 * m_GammaGuage_Panel.invalidate();
						 */
						break;

					case M_GAMMAGUAGE_PANEL:
						// Toast.makeText(getApplicationContext(),
						// "M_GAMMAGUAGE_PANEL", 1).show();
						String abc4 = intent.getStringExtra(M_GAMMAGUAGE_PANEL_YN);

						if (abc4.equals("true")) {

							m_GammaGuage_Panel.SetEvent(true);

						} else if (abc4.equals("true"))
							m_GammaGuage_Panel.SetEvent(false);

						break;

					case UPDATE_NEUTRONCPS_TEXT:

						break;

					case MSG_SET_TOSVUNIT:

						boolean Set_Tosvunit = intent.getBooleanExtra(MainBroadcastReceiver.DATA_SET_TOSVUNIT, false);

						if (m_GammaGuage_Panel != null)
							m_GammaGuage_Panel.Set_toSvUnit(Set_Tosvunit);

						break;

					case MSG_BLUETOOTH_CONNECTED:
						// Toast.makeText(getApplicationContext(),
						// "MSG_BLUETOOTH_CONNECTED", 1).show();
						connectCheck = true;
						Start_ID_mode();
						break;

					case MSG_DISCONNECTED_BLUETOOTH:
						// Toast.makeText(getApplicationContext(),
						// "MSG_DISCONNECTED_BLUETOOTH", 1).show();
						connectCheck = false;
						Start_Setup_mode();
						break;

					case MSG_EVENT:
						int event_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
						EventData eventdb = (EventData) intent.getSerializableExtra(DATA_EVENT);

						if (event_status == Detector.EVENT_BEGIN) {
							m_GammaGuage_Panel.SetEvent(true);
						} else if (event_status == Detector.EVENT_FINISH) {
							m_GammaGuage_Panel.SetEvent(false);

					/*	if (MainActivity.ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY) {
							WriteEvent_toDB(eventdb);
						}*/
						}
						break;

					case MSG_MOVE_MAIN_NEXTFLIPPER:
						count++;
						// Toast.makeText(getApplicationContext(),
						// Integer.toString(count), 1).show();
						Move_NextMainFlipper();

						break;

					case MSG_MOVE_MAIN_PREFLIPPER:

						Move_PreMainFlipper();

						break;

					case MSG_HEALTH_EVENT_IMG:

						int Hevent_status = intent.getIntExtra(DATA_EVENT_STATUS_IMG, Detector.EVENT_MSG_MOVE_FORWARD);

						// Toast.makeText(getApplicationContext(), "옴", 1).show();

						switch (Hevent_status) {
							case Detector.EVENT_MSG_MOVE_FORWARD:

								break;
							case Detector.EVENT_MSG_IN_RANGE:
								mMoveImgTxt.setText(getString(R.string.in_range));
								mMoveImg.setBackgroundResource(R.drawable.msg_inrange);
								break;
							case Detector.EVENT_MSG_MOVE_BACK:
								mMoveImgTxt.setText(getString(R.string.move_back));
								mMoveImg.setBackgroundResource(R.drawable.msg_back);
								break;
							case Detector.EVENT_MSG_DANGER:
								mMoveImgTxt.setText(getString(R.string.danger));
								mMoveImg.setBackgroundResource(R.drawable.msg_rad);
								break;

							default:
								break;

							case VersionUpdate.MESSAGE_UPDATE_SW:
								Start_UpdateText_Anime();
								// 중요
								break;

						}
						break;

					case MSG_NOT_RECV_USB_NEUTRON:

						NeutronLineTxt.setVisibility(View.GONE);
						NeutronCpsTxt.setVisibility(View.GONE);
						NeutronTxt.setVisibility(View.GONE);
						NeutronUnderNameTxt.setVisibility(View.GONE);
						break;

				}

			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}
		}
	}

	MainBCRReceiver mMainBCR = new MainBCRReceiver();

	// ----------------------------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------------------------

	private Handler mHandler = new Handler() {

		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case VersionUpdate.MESSAGE_UPDATE_SW:
					Start_UpdateText_Anime();
					break;
			}

			super.handleMessage(msg);

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// ID Test
		// Manual_Identification();

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 1000);
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		PowerManager pm = (PowerManager) RealTimeActivity.this.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "");
		wl.acquire();


		context = this;
		param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_MainLayout = (LinearLayout) inflater.inflate(R.layout.realtime, null);

		m_GammaGuage_Panel = (GuageView) m_MainLayout.findViewById(R.id.Gamma_guage_panel);
		m_GammaGuage_Panel.setOnTouchListener(this);

		// m_Neutron_Pannel = (TextView)
		// m_MainLayout.findViewById(R.id.n3rd_row_4);

		mFinder = (RealActivitySpectrumView) m_MainLayout.findViewById(R.id.MainFinder);

		GaugeViewCpsTxt = (TextView) m_MainLayout.findViewById(R.id.GaugeViewCpsTxt);

		NeutronTxt = (TextView) m_MainLayout.findViewById(R.id.NeutronTxt);

		NeutronLineTxt = (TextView) m_MainLayout.findViewById(R.id.NeutronLineTxt);

		NeutronCpsTxt = (TextView) m_MainLayout.findViewById(R.id.NeutronCpsTxt);

		NeutronUnderNameTxt = (TextView) m_MainLayout.findViewById(R.id.NeutronUnderNameTxt);


		//20.02.18
		layoutGM = (LinearLayout) m_MainLayout.findViewById(R.id.layoutGM);
		gm = (TextView) m_MainLayout.findViewById(R.id.gm);
		gmTxt = (TextView) m_MainLayout.findViewById(R.id.gmTxt);

		if(MainActivity.mDebug.hw == false){
			layoutGM.setVisibility(View.INVISIBLE);
			gm.setVisibility(View.INVISIBLE);
			gmTxt.setVisibility(View.INVISIBLE);
		}else{
			layoutGM.setVisibility(View.VISIBLE);
			gm.setVisibility(View.VISIBLE);
			gmTxt.setVisibility(View.VISIBLE);
		}

		NeutronLineTxt.setVisibility(View.GONE);
		NeutronCpsTxt.setVisibility(View.GONE);
		NeutronTxt.setVisibility(View.GONE);
		NeutronUnderNameTxt.setVisibility(View.GONE);

		mt = (TextView) m_MainLayout.findViewById(R.id.text1);
		mt.setText("");

		mMoveImgTxt = (TextView) m_MainLayout.findViewById(R.id.MoveImgTxt);

		mMoveImg = (ImageView) m_MainLayout.findViewById(R.id.MoveImg);

		TEXT_SIZE = mt.getTextSize();
		mPrefDB = new PreferenceDB(this);

		Start_ID_mode();

		mMainFlipper = (ViewFlipper) m_MainLayout.findViewById(R.id.MainFlipper);
		mMainFlipper.setOnTouchListener(this);

		BrodcastDeclare();
		/////////////////////////////////////////
		/*
		 * if (mEventDB.Get_WroteDB_version().matches(mEventDB.DB_verion) == false)
		 * Check_AndMake_EventDB_VersionFile(); Check_AndMake_IsoLibraryFile();
		 * Check_AndMake_DeviceNameFile(true);
		 */

		// Start login
		if (Logcount == 0) {
			// Start_Login_Dlg();
			Logcount = 1;
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(m_MainLayout);

		Check_SwUpdate_OnFTP();

	}

	// 핸들러부분
	public boolean onTouch(View v, MotionEvent event) {

		if (v.getContext() == m_GammaGuage_Panel.getContext()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mPreTouchPosX = (int) event.getX();
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {

				int nTouchPosX = (int) event.getX();

				int nTouchPosY = (int) event.getY();

				/*
				 * String str = Integer.toString(nTouchPosX);
				 *
				 * String str2 = Integer.toString(nTouchPosY);
				 *
				 * Toast.makeText(getApplicationContext(), "X: " + nTouchPosX + ", Y:" +
				 * nTouchPosY, 1).show();
				 */

				if (nTouchPosX < mPreTouchPosX) {
					Move_NextMainFlipper();
				} else if (nTouchPosX > mPreTouchPosX) {
					Move_PreMainFlipper();
				}
			}
		}
		return true;
	};

	@SuppressLint("NewApi")
	@Override

	public void onStart() {

		try {
			super.onStart();
			if (D)
				Log.e(TAG, "++ ON START ++");

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	@Override
	public synchronized void onResume()
	{

		//NcLibrary.SaveText1("onResume1 \n","test");
		m_GammaGuage_Panel.invalidate();
		Start_ID_mode(); //guage 초기화 및 gain start
		super.onResume();

	}

	public void BrodcastDeclare() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(MainBroadcastReceiver.MSG_EVENT);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_EVENT_SPECTRUM);
		filter.addAction(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
		filter.addAction(MainBroadcastReceiver.MSG_RECV_NEUTRON);
		filter.addAction(MainBroadcastReceiver.MSG_HEALTH_EVENT);
		filter.addAction(MainBroadcastReceiver.MSG_HEALTH_EVENT_IMG);

		filter.addAction(MainBroadcastReceiver.MSG_START_ID_MODE);
		filter.addAction(MainBroadcastReceiver.M_GAMMAGUAGE_PANEL);
		filter.addAction(MainBroadcastReceiver.M_GAMMAGUAGE_PANEL_YN);
		filter.addAction(MainBroadcastReceiver.DATA_SET_TOSVUNIT);
		filter.addAction(MainBroadcastReceiver.MSG_SET_TOSVUNIT);

		filter.addAction(MainBroadcastReceiver.MSG_RECV_USB_NEUTRON);
		filter.addAction(MainBroadcastReceiver.MSG_NOT_RECV_USB_NEUTRON);
		filter.addAction(MainBroadcastReceiver.DATA_NEUTRON);

		filter.addAction(MainBroadcastReceiver.DATA_EVENT_STATUS);

		filter.addAction(MainBroadcastReceiver.DATA_EVENT_STATUS_IMG);

		filter.addAction(MainBroadcastReceiver.DATA_EVENT_STATUS);

		filter.addAction(MainBroadcastReceiver.MSG_BLUETOOTH_CONNECTED);
		filter.addAction(MainBroadcastReceiver.MSG_DISCONNECTED_BLUETOOTH);

		filter.addAction(MainBroadcastReceiver.MSG_MOVE_MAIN_NEXTFLIPPER);

		filter.addAction(MainBroadcastReceiver.MSG_MOVE_MAIN_PREFLIPPER);

		// USB_DECLEAR

		LocalBroadcastManager.getInstance(context).registerReceiver(mMainBCR, filter);

	}

	// 메뉴부분
	@Override

	public void onDestroy() {

		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
		super.onDestroy();
	}

	@Override
	public synchronized void onPause() {

		// LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override

	public void onStop() {

		// mService.Disconnect_Server();
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");

	}

	// UIPart1
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	void Update_NeutronCPS_Text(double CPS) {

		try {
			double cps = CPS;
			String temp = null;
			temp = String.format("%.2f cps", cps);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	void Start_Animation_StatusIcon() {

		try {
			Animation aaa = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_out);
			if (m_Bluetooth_Status.getAnimation() == null)
				m_Bluetooth_Status.startAnimation(aaa);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}

	}

	void End_Animation_StatusIcon() {
		try {
			m_Bluetooth_Status.clearAnimation();
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void Start_UpdateText_Anime() {

		TextView tv_update = (TextView) m_MainLayout.findViewById(R.id.tv_Update);

		tv_update.setVisibility(TextView.VISIBLE);

		tv_update.setAnimation(AnimationUtils.loadAnimation(this, R.anim.moving_to_left));

	}

	void Stop_UpdateText_Anime() {
		TextView tv_update = (TextView) m_MainLayout.findViewById(R.id.tv_Update);
		tv_update.setAnimation(null);
		tv_update.setVisibility(TextView.INVISIBLE);
	}

	// 뷰플리퍼
	void Move_NextMainFlipper() {

		try {
			mMainFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
			mMainFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
			mMainFlipper.showNext();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void Move_PreMainFlipper() {

		count = 0;
		try {
			mMainFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
			mMainFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
			mMainFlipper.showPrevious();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	// UIPart2
	private void Update_MoveInfo(int cps) {

		try {
			if (cps > 40000)
				m_GammaGuage_Panel.Show_WarningInfo_Text("");
			if (cps > 2000)
				m_GammaGuage_Panel.Show_WarningInfo_Text("");

			else if (cps > 250)
				m_GammaGuage_Panel.Show_WarningInfo_Text("");
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	void Start_ID_mode() {
		// MainActivity.SendU2AA();
		// Toast.makeText(getApplicationContext(), "Start_ID_mode", 1).show();
		try {

			MainActivity.mDetector.Set_Mode(Detector.ID_MODE);
			// 181220 InIt_SPC_Data();

			m_GammaGuage_Panel.Start();
			m_GammaGuage_Panel.SetEvent(false);
			// Intent send_gs = new Intent(MainBroadcastReceiver.START_ID_MODE);

			// LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

			mFinder.Set_Data(MainActivity.mDetector.RealTime_CPS);
			GaugeViewCpsTxt.setText(Integer.toString(MainActivity.mDetector.Get_GammaCPS()));
			mFinder.invalidate();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	void Start_Setup_mode() {
		// MainActivity.SendU4AA();
		try {
			mt.setText("");

			Update_NeutronCPS_Text(-1);
			m_GammaGuage_Panel.Stop();

			InIt_SPC_Data();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void InIt_SPC_Data() {
		try {
			MainActivity.mDetector.Init_Measure_Data();
			m_GammaGuage_Panel.SETnSv(0, 0);
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
		}
	}

	private void Check_SwUpdate_OnFTP() {

		if (NcLibrary.IsWifiAvailable(this) | NcLibrary.Is3GAvailable(this)) {

			new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						VersionUpdate up = new VersionUpdate(NcLibrary.Get_AppVersion(context), mHandler);
						up.Update_Version_FromFTP();
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog(e);
					}
				}
			}).start();

		}
	}

	public void BrodCastStop() {

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMainBCR);

	}

	void WriteEvent_toDB(EventData event) {

		if (event.MS.Get_AcqTime() <= 3)
			return;

		GpsInfo2 gps = new GpsInfo2(this);
		event.GPS_Latitude = gps.GetLat();
		event.GPS_Longitude = gps.GetLon();
		event.mColumn_Version = EventDBOper.mDB.GetDBVersion();

		EventDBOper eventDB = new EventDBOper();
		if (eventDB.WriteEvent_OnDatabase(event)) {
			int cnt = eventDB.GetEventCount();
			String str_event = getResources().getString(R.string.event);

			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.write_db) + " (" + str_event + " #" + cnt + ")", Toast.LENGTH_LONG).show();

		}

		// Start_MediaScan();
		File eventFile = new File(Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/"
				+ EventDBOper.DB_FILE_NAME + ".sql");
		if (eventFile.isFile())
			new SingleMediaScanner(getApplicationContext(), eventFile);
	}

}
