package android.HH100;

import android.HH100.CameraUtil.App;
import android.HH100.CameraUtil.Camera2Activity;
import android.HH100.CameraUtil.VideoActivity;
import android.HH100.Control.Analysis_TopInfor;
import android.HH100.Control.GpsInfo;
import android.HH100.Control.GpsInfo2;
import android.HH100.Control.ProgressBar;
import android.HH100.Control.ScView_Ad;
import android.HH100.Control.SpectrumView;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.FindPeaksM;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.LogActivity.LogPhotoTab.Media;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.MainActivity.Focus;
import android.HH100.MainActivity.Tab_Name;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.Mail;
import android.HH100.Structure.NcLibrary;
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
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import Debug.Debug;
import NcLibrary.Coefficients;
import NcLibrary.NcMath;

public class IDspectrumActivity extends Activity implements View.OnTouchListener {
    private static final boolean D = MainActivity.D;

    Debug mDebug;

    interface IDspectrumValue {
        public String TAG = "IDSpectrum";
        public String TAG_SEQ = "IDSpectrum_seq";
        public String TAG_MANUALID = "IDSpectrum_manual";

        public int MS_SEQUENCE_MODE = 532842;
        public int MS_MANUAL_ID = 532843;
        public int LIST_DRAW = 532844;

        public int FOCUSBTN = 53284;

        public int TEST = 532845;

        public String EXTRA_SPECTRUM = "extra_spc";
        public String EXTRA_BG_SPECTRUM = "extra_bg_spc";
        public String EXTRA_DETECTED_SOURCE = "extra_detected_source";
        public String EXTRA_MANUAL_ID_GOAL_TIME = "extra_mID_goalTime";
        public String EXTRA_MANUAL_ID_ADJUST_TIME = "extra_adjust_goalTime";
        public String EXTRA_SEQ_ACQTIME = "extra_acq_time";
        public String EXTRA_SEQ_REPEAT = "extra_repeat";
        public String EXTRA_SEQ_PAUSE_TIME = "extra_pause_time";
        public String EXTRA_SV_UNIT = "extra_sv_unit";
        public String ACTIVTY = "actvity";

    }

    private int mLogCount = 0;

    public static boolean SOURCE_ID_RESULT_MODE = false;

    private SpectrumView mSpectrumView;
    private Analysis_TopInfor mTopAnal_Info;

    public Spectrum mSPC = new Spectrum();

    private LinearLayout m_MainLayout;
    private LinearLayout m_AnalysisLayout;

    private ScView_Ad mAnalysisView;

    //20.02.18
    private LinearLayout layoutGM;
    private TextView gm, gmTxt;

    /// --Manual ID
    private ImageButton mManualID_TimeUP;
    private ImageButton mManualID_TimeDown;
    private TextView mManualID_Time;


    // -- Acq Time
    private int mManualID_GoalTime = 60;
    private int mManualID_Adjust_sec = 10;

    private int mSequence_acqTime = 0;
    private int mSequence_repeat_Goal = 0;
    private int mSequence_repeat_count = 0;

    private int mPauseTime = 5;// sec
    private int mPauseTime_ElapsedTime = 0;
    // --
    public boolean mIsSequenceMode = false;
    private boolean mIsSvUnit = true;

    private boolean mIsEvent = false;
    /// --End Manual ID
    public static boolean mIsManualID_mode = false;
    private Vector<Integer> mClassColor = new Vector<Integer>();

    private EventData m_EventData = null;

    public int cntt = 0;

    public int mBtnDownCount = 0;

    private ViewFlipper mMainFlipper;

    ImageView filpperImgView;

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

    public int mGain_restTime_over2 = 10;

    public static Vector<EventData> mAllLog;

    public static int mFileNumber = 0;

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
    public static int mEventNumber;

    ProgressDialog mPrgDlg;

    public ListView m_lv = null;
    View m_lv1 = null;

    //180917 camera
    File mFile;
    String photoPath = "", videoPath = "";

    public static int rootFocusCnt = Focus.ID_RESULT_MENU_C;

    public static int currentRootFocusCnt = 0;

    public static int currentMediaCnt = 0;

    public static int rootAsubFocusCnt = 0;

    public static int rootBsubFocusCnt = 0;

    public static int rootCsubFocusCnt = 0;

    public static int checkSelectMode = Activity_Mode.ID_RESULT_UN_CHECK_SELECT_MODE;
    public static int checkSelectModeCount = 0;

    public static int checkMediaModeCount = Activity_Mode.UN_EXCUTE_MODE;

    public static int DoubleClickRock = Activity_Mode.EXCUTE_MODE;

    public static int idBottomSwicthCount = Focus.ID_RESULT_MENU_C_REMOVE_BTN;

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

    int countTest = 0;

    int focusAEnterRock = 0;

    int HWDoubleClickRock = 0;

    int mGalleryListViewCurrentPosition = 0;

    Vector<String> mPhotoName = new Vector<String>();
    Vector<String> mVideoName = new Vector<String>();
    Vector<String> mRecoderName = new Vector<String>();

    Vector<String> mTotalTxt = new Vector<String>();

    Vector<String> mRecoder = new Vector<String>();

    FrameLayout mFrameLayout;

    // video, photo end

    GpsInfo2 mGpsInfo2;
    int mFirstSpec[] = new int[1024];

    //190102 추가
    //TimerTask U2AATimer = null;

    public class MainBCRReceiver extends MainBroadcastReceiver {

        @Override
        public void onReceive(Context context, android.content.Intent intent) {

            try {
                String action = intent.getAction();
                switch (action) {
                    case MSG_RECV_SPECTRUM:

                        //190102 추가
/*						if(U2AATimer !=null)
						{
							U2AATimer.cancel();
							U2AATimer = null;
						}*/

                        if (D)
                            Log.d(IDspectrumValue.TAG, "Receive Broadcast- Spectrum");
                        Spectrum spc = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

                        mSpectrumView.Show_Info(true);

                        if (true) {
                            mHandler.obtainMessage(IDspectrumValue.MS_MANUAL_ID, 0, 0, spc).sendToTarget();
                        } else if (mIsSequenceMode) {
                            if (mPauseTime_ElapsedTime >= mPauseTime) {
                                mHandler.obtainMessage(IDspectrumValue.MS_SEQUENCE_MODE, 0, 0, spc).sendToTarget();
                            } else {
                                mPauseTime_ElapsedTime += 1;
                                Set_Info_OnSpectrumView_OnAnalysisView(mPauseTime_ElapsedTime, 0, 0);
                                // mSpectrumView.invalidate();
                                mTopAnal_Info.invalidate();
                            }
                        } else {
                            mSPC.Accumulate_Spectrum(spc);
                            mSpectrumView.SetChArray(mSPC);
                            if (mIsEvent)
                                Set_Info_OnSpectrumView_OnAnalysisView((int) mSPC.Get_AcqTime(), spc.Get_TotalCount(), spc.Get_AcqTime());
                            else
                                Set_Info_OnSpectrumView_OnAnalysisView(0, spc.Get_TotalCount(), spc.Get_AcqTime());
                            // mSpectrumView.invalidate();
                            mTopAnal_Info.invalidate();
                        }

                        break;
                    case MSG_RECV_NEUTRON:
                        // int event_status =
                        // intent.getIntExtra(DATA_EVENT_STATUS,Detector.EVENT_NONE);
                        break;
                    case MSG_EVENT:
                        if (D)
                            Log.d(IDspectrumValue.TAG, "Receive Broadcast- Event");
                        int event_status = intent.getIntExtra(DATA_EVENT_STATUS, Detector.EVENT_NONE);
                        EventData eventdb = (EventData) intent.getSerializableExtra(DATA_EVENT);
                        if (eventdb != null) {
                            if (eventdb.Event_Detector.matches(EventData.EVENT_NEUTRON))
                                break;

                        }

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

                            // mSpectrumView.invalidate();
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
                            Log.d(IDspectrumValue.TAG, "Receive Broadcast- Recalibration (" + En_Coeff.ToString() + " || "
                                    + Ch_Coeff.ToString() + ")");
                        break;
                    case MSG_REMEASURE_BG:
                        Spectrum bg = (Spectrum) intent.getSerializableExtra(DATA_SPECTRUM);

                        if (D)
                            Log.d(IDspectrumValue.TAG, "Receive Broadcast- Remeasured background (" + bg.ToString() + ")");
                        // mBG = bg;
                        break;
                    case MSG_DISCONNECTED_BLUETOOTH:
                        mSpectrumView.Clear_Found_Isotopes();
                        mSPC.ClearSPC();
                        mSpectrumView.SetChArray(mSPC);
                        mAnalysisView.RemoveAll_IsotopeData();

                        if (D)
                            Log.d(IDspectrumValue.TAG, "bluetooth disconnected");
                        break;

                    case MSG_TAB_SIZE_MODIFY_FINISH:

                        //190102 추가
                        //MainActivity.SendU4AA();

                        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        linearLayout = (LinearLayout) inflater.inflate(R.layout.id_result, null);

                        setContentView(linearLayout);

                        SourceIdResultInfo();
                        End_ManualID();

                        SetSourceIdResultActivityMode();
                        // DropDownAnimation();
                        break;

                    case MSG_SOURCE_ID_RUNNING_START:

                        //190102 추가
		/*				Thread.sleep(100);
						MainActivity.SendU2AA();

						U2AATimer = new TimerTask()
						{
							@Override
							public void run() {
								MainActivity.SendU4AA();
								try
								{
									//NcLibrary.SaveText1("Start U2AA \n","test");
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

                        //    NcLibrary.SaveText1("SendU2AA\n","test");
                        MainActivity.tabHost.setCurrentTab(Tab_Name.MenualID);
                        SetSourceIdRunningActivityMode();
                        // MainActivity.ACTIVITY_STATE =
                        // Activity_Mode.SOURCE_ID_RUNNING;
                        onCreatePart();

                        // DropDownAnimation();
                        break;

                    case MSG_SPEC_VIEWFILPPER:

                        View_Filpper();

                        break;
                    case MSG_SOURCE_ID_TIMEDOWN:

                        if (mBtnDownCount == 0) {

                            TimeDown();
                        }
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

    TextView IDAcqTimeTxt = null;

    RelativeLayout filperTouch = null;

    // SourceIdResultInfo

    CheckBox Favorite_Checkbox;

    Bitmap RecBitmap;

    ImageButton ReachBackBtn, CameraBtn, VideoBtn, VoiceBtn;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // TODO Auto-generated method stub5
        super.onCreate(savedInstanceState);

/*		if(MainActivity.mDebug.hw){
			NcLibrary.SaveText1("Manual ID START","spectrum");
		}*/


        mContext = this;
        mGpsInfo2 = new GpsInfo2(mContext);
        //NcLibrary.SaveText1("IDspectrum \n","test");

        /*
         * Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT);
         *
         * LocalBroadcastManager.getInstance(getApplicationContext()).
         * sendBroadcast(send_gs);
         *
         * LocalBroadcastManager.getInstance(mContext).unregisterReceiver( mMainBCR);
         *
         * IntentFilter filter = new IntentFilter();
         *
         * filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_HW_BACK);
         *
         * filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);
         *
         * filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);
         *
         * LocalBroadcastManager.getInstance(getApplicationContext()).
         * registerReceiver(mMainBCR, filter);
         */

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

        IntentFilter filter = new IntentFilter();

        filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);

        filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

        filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_BACK);

        filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

        PowerManager pm = (PowerManager) IDspectrumActivity.this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "");
        wl.acquire();
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            try {
                Spectrum spc = (Spectrum) msg.obj;
				/*
				PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
				IsotopesLibrary IsoLib = new IsotopesLibrary(getApplicationContext());*/

                switch (msg.what) {

                    case IDspectrumValue.MS_MANUAL_ID:

                        if (m_EventData == null) {
                            //Log.i(IDspectrumValue.TAG_MANUALID, "Manual ID: Start Manual ID");
                            // Toast.makeText(getApplicationContext(),
                            // "#"+(mSequence_repeat_count+1)+"
                            // Start",Toast.LENGTH_SHORT).show();

                            m_EventData = new EventData();
                            m_EventData.mInstrument_Name = MainActivity.mDetector.InstrumentModel_Name;
                            m_EventData.Doserate_unit = (MainActivity.mDetector.IsSvUnit) ? Detector.DR_UNIT_SV
                                    : Detector.DR_UNIT_R;

                            m_EventData.Event_Detector = "Manual ID";
                            m_EventData.MS.Set_Coefficients(spc.Get_Coefficients());
                            m_EventData.MS.Set_StartSystemTime();
                            m_EventData.mUser = MainActivity.mDetector.User;
                            m_EventData.mLocation = MainActivity.mDetector.Location;


                            /////////////////////////////////////////////////////////
                            // 2018.02.14  Background spectrum adjustment using current calibration info.
                            //double []FWHM_NaI2_2=new double [] {1.2707811254,-1.5464537062};
                            double[] FWHM = MainActivity.mDetector.MS.getFWHM();
                            double[] mCoefficient = MainActivity.mPrefDB.Get_Cali_ABC_From_pref();

                            // reload background spectrum
                            MainActivity.mDetector.Real_BG.Set_MeasurementDate(MainActivity.mPrefDB.Get_BG_Date_From_pref());
                            //MainActivity.mDetector.Real_BG.Set_Spectrum(prefDB.Get_BG_From_pref(), prefDB.Get_BG_MeasuredAcqTime_From_pref());

                            int K40_Ch = NcLibrary.PeakAna(MainActivity.mDetector.Real_BG.ToInteger(), FWHM, mCoefficient);

                            int K40_New = (int) NcMath.ToPolynomial_FittingValue_AxisX1(1461.0, mCoefficient);

                            //MainActivity.mDetector.Background_GainStabilization(K40_Ch, K40_New);
                            if (K40_Ch > 10) {
                                double[] mSpc = new double[MainActivity.CHANNEL_ARRAY_SIZE];
                                mSpc = NcMath.Background_GainStabilization(MainActivity.mDetector.Real_BG.ToDouble(), K40_Ch, K40_New);
                                MainActivity.mDetector.Real_BG.Set_Spectrum(mSpc, (int) MainActivity.mDetector.Real_BG.Get_AcqTime());
                            }
                            ////////////////////////////////////////////////////////////


                            m_EventData.BG = MainActivity.mDetector.Real_BG;
                            //m_EventData.BG.Set_Spectrum(MainActivity.mDetector.Real_BG);
                            m_EventData.Set_StartTime();
                            m_EventData.IsManualID = true;

                            EventDBOper mEventDBOper = new EventDBOper();
                            mEventDBOper.Set_Crytal_Info(MainActivity.mPrefDB.Get_CryStal_Type_Number_pref());
                            m_EventData.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
                        }

                        m_EventData.MS.Accumulate_Spectrum(spc);
                        m_EventData.mNeutron.Set_CPS(MainActivity.mDetector.mNeutron.Get_CPS());
                        if (m_EventData.Doserate_MAX < MainActivity.mDetector.Get_Gamma_DoseRate_nSV())
                            m_EventData.Doserate_MAX = MainActivity.mDetector.Get_Gamma_DoseRate_nSV();
                        m_EventData.Doserate_AVG += MainActivity.mDetector.Get_Gamma_DoseRate_nSV();

                        if (MainActivity.mDebug.hw) {
                            gmTxt.setText(String.valueOf(MainActivity.mDetector.GM_Cnt));

                        }

                        //NcLibrary.SaveText1("cps  "+spc.Get_AvgCPS()+" ,FillCps "+spc.GetFillCps()+" ,GM "+MainActivity.mDetector.GM_Cnt+" ,spectrum "+spc.ToString(),"spectrum");

						/*Log.d("time:",
								"DoserateNSV_AVG :" + Double.toString(MainActivity.mDetector.Get_Gamma_DoseRate_nSV()));*/

                        // --
                        //191118
/*						prefDB = new PreferenceDB(getApplicationContext());
						IsoLib = new IsotopesLibrary(getApplicationContext());
						IsoLib.Set_LibraryName(prefDB.Get_Selected_IsoLibName());
						IsotopeInvisibleViewFirstFiveSecond(IsoLib);*/

                        if (MainActivity.mIsoLib2 == null) {
                            MainActivity.mIsoLib2 = new IsotopesLibrary(getApplicationContext());
                            MainActivity.mIsoLib2.Set_LibraryName(MainActivity.mPrefDB.Get_Selected_IsoLibName());
                        }
                        m_EventData.Detected_Isotope = MainActivity.mIsoLib2.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG);

                        if (m_EventData.Detected_Isotope.isEmpty() == false)
                            m_EventData.Detected_Isotope = NcLibrary.Quantitative_analysis(MainActivity.mDetector.MS,
                                    m_EventData.BG, m_EventData.Detected_Isotope, MainActivity.mDetector.IsSvUnit,
                                    MainActivity.mDetector.mPmtSurface, MainActivity.mDetector.mCrystal,MainActivity.mDetector.getGECoef());

                        Set_IdResult_toViews(m_EventData.Detected_Isotope);
                        Set_Info_OnSpectrumView_OnAnalysisView((int) m_EventData.MS.Get_AcqTime(), spc.Get_TotalCount(), spc.Get_AcqTime());
                        mSpectrumView.SetChArray(m_EventData.MS);
                        mSpectrumView.invalidate();
                        mTopAnal_Info.invalidate();

                        mManualID_TimeDown.setVisibility(View.VISIBLE);
                        mManualID_TimeUP.setVisibility(View.VISIBLE);

                        double Percent = ((double) m_EventData.MS.Get_AcqTime() / (double) mManualID_GoalTime) * 100.0;
                        mProgBar.Set_Value(Percent);
                        mProgBar.invalidate();

                        //	NcLibrary.SaveText1("m_EventData.MS.Get_AcqTime()  : "+m_EventData.MS.Get_AcqTime() +"\n","test");

                        if (m_EventData.MS.Get_AcqTime() >= mManualID_GoalTime) {

                            IDTh232removeUnkownPeakFilter();

                            Class<? extends EventData> a = m_EventData.getClass();

                            String ac = m_EventData.toString();

                            m_EventData.Set_EndEventTime();
                            m_EventData.Doserate_AVG = m_EventData.Doserate_AVG / m_EventData.MS.Get_AcqTime();
                            m_EventData.Neutron_AVG = m_EventData.mNeutron.Get_AvgCps();
                            m_EventData.Neutron_MAX = m_EventData.mNeutron.Get_MaxCount();

                            m_EventData.GPS_Latitude = mGpsInfo2.GetLat();
                            m_EventData.GPS_Longitude = mGpsInfo2.GetLon();

                            mAnalysisView.RemoveAll_IsotopeData();
                            mSpectrumView.Clear_Found_Isotopes();

                            mSPC.ClearSPC();
                            mSpectrumView.SetChArray(mSPC);
                            mSpectrumView.invalidate();
                            mTopAnal_Info.invalidate();


                            Set_Invisible_ManualID_Contol(true);

                            Log.i(IDspectrumValue.TAG_MANUALID, "Manual ID: End Manual ID");

                            mProgBar.Set_Value(0);

                            mProgBar.invalidate();

                            Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
                            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

                            IntentFilter filter = new IntentFilter();

                            filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_HW_BACK);
                            filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);
                            filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);
                            filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);

                            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

                        }

                        break;

                    case IDspectrumValue.LIST_DRAW:

                        ListViewSizeChange();

                        break;

                    case IDspectrumValue.FOCUSBTN:
                        Log.d("time:", "Log2");
                        CameraBtn = (ImageButton) findViewById(R.id.CameraBtn);
                        CameraBtn.setFocusable(true);
                        CameraBtn.setFocusableInTouchMode(true);
                        CameraBtn.requestFocus();

                        break;

                    case IDspectrumValue.TEST:

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

//		event.GPS_Latitude = mGpsInfo2.GetLat();
//		event.GPS_Longitude = mGpsInfo2.GetLon();

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
                return false;
            }
            return true;
        } else
            return false;

        // Start_MediaScan();

    }

    private void End_ManualID() {
        try {
            // mManualID_Adjust_sec = mPrefDB.Get_ManualID_AdjustTime();

            mSPC.ClearSPC();
            mIsManualID_mode = false;
            mManualID_GoalTime = getIntent().getIntExtra(IDspectrumValue.EXTRA_MANUAL_ID_GOAL_TIME, mManualID_GoalTime);
            mSpectrumView.Set_DataColor(getResources().getColor(R.color.Gray));
            mSpectrumView.Clear_Found_Isotopes();

            mAnalysisView.RemoveAll_IsotopeData();

            // --===--
            Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
            intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_ON);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            // --===--
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
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

    public void Set_Info_OnSpectrumView_OnAnalysisView(int total_acqTime, int cps, int acqTime) {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();

        // mSpectrumView.Set_inform(getResources().getString(R.string.cps),
        // NcLibrary.Comma_Format(cps));
        /*
         * mSpectrumView.Set_SoureId_inform(getResources().getString(R.string. cps),
         * Cut_Decimal_Point(cps));
         *
         * mSpectrumView.Set_SoureId_inform2(getResources().getString(R.string.
         * total_count), Cut_Decimal_Point(m_EventData.MS.Get_TotalCount()));
         * mSpectrumView.Set_SoureId_inform3(getResources().getString(R.string.
         * acq_time), String.valueOf(acqtime) + " / " + mManualID_GoalTime + "s");
         */

        //NcLibrary.SaveText1("mDetector.Get_GammaCPS : "+cps+"\n","Gamma");
        if (acqTime <= 0) {
            acqTime = 1;
        }
        //NcLibrary.SaveText1("acqtime : "+acqTime+" cps : "+cps+"\n","test");
        cpsTxt.setText(Cut_Decimal_Point((cps / acqTime)));

        Log.d("CPS", Cut_Decimal_Point(cps) + ";");

        totalCountTxt.setText(Cut_Decimal_Point(m_EventData.MS.Get_TotalCount()));

        acqTimeTxt.setText(String.valueOf(total_acqTime));

        acqTimeTxt2.setText(String.valueOf(mManualID_GoalTime));

        // mSpectrumView.Set_inform4(getResources().getString(R.string.cps),
        // NcLibrary.Comma_Format(cps));

        // --------------------------------------------------------------------------------------------

        mTopAnal_Info.Set_infor1(getResources().getString(R.string.date), calendar.get(Calendar.DAY_OF_MONTH) + "."
                + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR));
        mTopAnal_Info.Set_infor2(getResources().getString(R.string.time),
                date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds());
        mTopAnal_Info.Set_infor3(getResources().getString(R.string.acq_time), String.valueOf(total_acqTime) + "sec");
        mTopAnal_Info.Set_infor4(getResources().getString(R.string.cps), NcLibrary.Prefix(cps, true));

    }

    public Location Get_Location() {

        GpsInfo gps = new GpsInfo(IDspectrumActivity.this);
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
        // mSpectrumView.invalidate();
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

                if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {

                    Source_Id_Result_Cancel();

                } else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

                    Source_Id_Running_Cancel();
                }

                break;

        }
        return true;
    }

    public void Start_ManualID() {

        mIsManualID_mode = true;

    }

    public void Start_sequenceMode() {
        mSPC.ClearSPC();
        mIsSequenceMode = true;
        mPauseTime_ElapsedTime = mPauseTime;
        mSequence_repeat_count = 0;
        Set_Invisible_Sequence_Contol(false);
    }

    private void Set_SeqModeInfo(int repeatCnt) {
        TextView repeatCntTV = (TextView) m_MainLayout.findViewById(R.id.tv_seq_repeat);
        repeatCntTV.setText(repeatCnt + "/" + mSequence_repeat_Goal);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // m_MainLayout.onTouchEvent(event);
        // mSpectrumView.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {

        //190102 추가
        //NcLibrary.U2AATimerTask();
        //NcLibrary.SaveText1("onDestroy\n","test");
        //this.finish();
	/*	if(MainActivity.mDebug.hw){
			NcLibrary.SaveText1("Manual ID FINISH","spectrum");
		}*/
        super.onDestroy();
    }

    /////////////////////////////////////////////////////////////////////
    public void Set_Invisible_ManualID_Contol(boolean IsInvisible) {

        /*
         * int Weight = 0; if (IsInvisible) Weight = 0; else Weight = 1;
         *
         * FrameLayout SpcLayout = (FrameLayout) findViewById(R.id.frameLayout1);
         * LinearLayout.LayoutParams Param = (LinearLayout.LayoutParams)
         * SpcLayout.getLayoutParams(); Param.weight = Weight;
         * SpcLayout.setLayoutParams(Param);
         *
         * if (Weight == 0) { // LinearLayout control_lyaout = (LinearLayout) //
         * m_MainLayout.findViewById(R.id.frameLayout2); //
         * control_lyaout.setVisibility(LinearLayout.GONE); } else { LinearLayout
         * control_lyaout = (LinearLayout) m_MainLayout.findViewById(R.id.frameLayout2);
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
         * m_MainLayout.findViewById(R.id.frameLayout1); LinearLayout.LayoutParams Param
         * = (LinearLayout.LayoutParams) SpcLayout.getLayoutParams(); Param.weight =
         * Weight; SpcLayout.setLayoutParams(Param);
         *
         * if (Weight == 0) { // LinearLayout control_lyaout = (LinearLayout) //
         * m_MainLayout.findViewById(R.id.layout_sequenceInfo); //
         * control_lyaout.setVisibility(LinearLayout.GONE); } else { LinearLayout
         * control_lyaout = (LinearLayout)
         * m_MainLayout.findViewById(R.id.layout_sequenceInfo);
         * control_lyaout.setVisibility(LinearLayout.VISIBLE);
         *
         * LinearLayout seqInfo_Layout = (LinearLayout)
         * m_MainLayout.findViewById(R.id.frameLayout2);
         * seqInfo_Layout.setVisibility(LinearLayout.GONE); } m_MainLayout.invalidate();
         */
    }

    private void Set_Spectrum_Y_toEnergy() {
        mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
    }

    @Override
    public void onBackPressed() {

        if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {
            Source_Id_Result_Cancel();

        } else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
            Source_Id_Running_Cancel();
        }

        //190102 추가
        MainActivity.mDetector.mGain_restTime = mGain_restTime_over2;

        //	NcLibrary.U2AATimer();

        //NcLibrary.SaveText1("Manual ID FINISH \n","spectrum");

/*		MainActivity.SendU2AA();
		try
		{
			Thread.sleep(500);
			MainActivity.SendU2AA();

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}*/
        return;
    }

    public void onCreatePart() {

        try {

            //NcLibrary.SaveText1("Manual ID START","spectrum");

            mBtnDownCount = 1;

            TimerTask mTask5 = new TimerTask() {

                @Override
                public void run() {

                    mBtnDownCount = 0;
                }
            };

            Timer mTimer5 = new Timer();
            mTimer5.schedule(mTask5, 1000);
            Spectrum spcdata = (MainActivity.mDetector.mGamma_Event == null) ? MainActivity.mDetector.MS.ToSpectrum()
                    : MainActivity.mDetector.mGamma_Event.MS.ToSpectrum();

            if (spcdata != null)

                for (int i = 0; i < 1024; i++) {

                    mFirstSpec[i] = 0;

                }

            spcdata.Set_Spectrum(mFirstSpec);
            mSPC.Set_Spectrum(spcdata);

            mManualID_GoalTime = MainActivity.mPrefDB.Get_ManualID_DefaultTime();
            mManualID_Adjust_sec = MainActivity.mPrefDB.Get_ManualID_AdjustTime();

            mIsSvUnit = MainActivity.mDetector.IsSvUnit;

            mSequence_acqTime = MainActivity.mPrefDB.Get_SequenceMode_acqTime_From_pref();
            mSequence_repeat_Goal = MainActivity.mPrefDB.Get_SequenceMode_Repeat_From_pref();
            mPauseTime = MainActivity.mPrefDB.Get_SequenceMode_PauseTime_From_pref();

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
        filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_TIMEDOWN);

        filter.addAction(MainBroadcastReceiver.MSG_TAB_SIZE_MODIFY_FINISH);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

        /*
         * @SuppressWarnings("unchecked") Vector<Isotope> id_result = (Vector<Isotope>)
         * getIntent().getSerializableExtra(EXTRA_DETECTED_SOURCE); if(id_result !=
         * null) { Log.i(TAG, "asdf"); Set_IdResult_toViews(id_result); }
         */
        //////// �씠�븯 �옄�룞 �뒳由쎈え�뱶 �빐�젣
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_MainLayout = (LinearLayout) inflater.inflate(R.layout.id_spectrum, null);

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

        mManualID_Time = (TextView) m_MainLayout.findViewById(R.id.ManualID_time);

        mTopAnal_Info = (Analysis_TopInfor) m_MainLayout.findViewById(R.id.Iso_analysis);
        mTopAnal_Info.Set_Log_GridCount(mAnalysisView.Get_Grid_Count());
        mTopAnal_Info.Set_Class_Color(mClassColor);
        mTopAnal_Info.Set_Doserate_Unit(mIsSvUnit);

        //20.02.18
        layoutGM = (LinearLayout) m_MainLayout.findViewById(R.id.layoutGM);
        gm = (TextView) m_MainLayout.findViewById(R.id.gm);
        gmTxt = (TextView) m_MainLayout.findViewById(R.id.gmTxt);

        if (MainActivity.mDebug.hw == false) {
            layoutGM.setVisibility(View.INVISIBLE);
            gm.setVisibility(View.INVISIBLE);
            gmTxt.setVisibility(View.INVISIBLE);
        } else {
            layoutGM.setVisibility(View.VISIBLE);
            gm.setVisibility(View.VISIBLE);
            gmTxt.setVisibility(View.VISIBLE);
        }

        ///////////////// Manual ID Picker
        mManualID_TimeUP = (ImageButton) m_MainLayout.findViewById(R.id.button_up);
        mManualID_TimeUP.setOnTouchListener((new Button.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    mManualID_TimeUP.setBackgroundResource(R.drawable.time_up_focus);

                    mManualID_GoalTime += mManualID_Adjust_sec;
                    acqTimeTxt2.setText(Integer.toString(mManualID_GoalTime));

                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mManualID_TimeUP.setBackgroundResource(R.drawable.time_up);
                    // mManualID_sec =
                    // Integer.valueOf(String.valueOf(mManualID_Time.getText()));

                }
                return false;
            }
        }));
        mManualID_TimeDown = (ImageButton) m_MainLayout.findViewById(R.id.button_down);
        mManualID_TimeDown.setOnTouchListener((new Button.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                try {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (m_EventData.MS != null) {
                            if (m_EventData.MS.Get_AcqTime() < mManualID_GoalTime - mManualID_Adjust_sec) {

                                mManualID_TimeDown.setBackgroundResource(R.drawable.time_down_focus);

                                mManualID_GoalTime -= mManualID_Adjust_sec;

                                acqTimeTxt2.setText(Integer.toString(mManualID_GoalTime));

                            }
                        }
                        return false;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mManualID_TimeDown.setBackgroundResource(R.drawable.time_down);

                    }
                    return false;
                } catch (Exception e) {
                    NcLibrary.Write_ExceptionLog(e);
                    return false;
                }
            }
        }));

        LinearLayout control_lyaout = (LinearLayout) m_MainLayout.findViewById(R.id.frameLayout2);
        control_lyaout.setVisibility(LinearLayout.VISIBLE);

        Button seq_cancel = (Button) m_MainLayout.findViewById(R.id.btn_seq_Cancel);
        seq_cancel.setOnTouchListener((new Button.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    End_SequenceMode();

                    mAnalysisView.RemoveAll_IsotopeData();
                    mSpectrumView.Clear_Found_Isotopes();

                    // mSpectrumView.invalidate();
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

        // -- End Manual Id Picker
        mSpectrumView = (SpectrumView) m_MainLayout.findViewById(R.id.IDspectrum);
        mSpectrumView.setChArraySize(mSPC.Get_Ch_Size());
        mSpectrumView.SetChArray(mSPC);
        mSpectrumView.LogMode(true);
        mSpectrumView.Change_X_to_Energy(mSPC.Get_Coefficients().get_Coefficients());
        mSpectrumView.invalidate();

        setContentView(m_MainLayout);

        filpperImgView = (ImageView) findViewById(R.id.filperImgView);

        filperTouch = (RelativeLayout) findViewById(R.id.filperTouch);
        flipper = (ViewFlipper) findViewById(R.id.flipper);

        IDspectrum = (View) findViewById(R.id.IDspectrum);

        Iso_analysis = (View) findViewById(R.id.Iso_analysis);

        filpperImgView.setOnTouchListener(mTouchEvent);

        mProgBar = (ProgressBar) m_MainLayout.findViewById(R.id.SetupSpcSrc_ProgressBar);

        mFrameLayout = (FrameLayout) m_MainLayout.findViewById(R.id.frameLayout1);

        cpsTxt = (TextView) m_MainLayout.findViewById(R.id.cpsTxt);

        totalCountTxt = (TextView) m_MainLayout.findViewById(R.id.totalCountTxt);

        acqTimeTxt = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt);

        acqTimeTxt2 = (TextView) m_MainLayout.findViewById(R.id.Acq_TimeTxt2);

        acqTimeTxt2.setText(Integer.toString(mManualID_GoalTime));
        // mFrameLayout.setOnTouchListener(this);

        Manual_Nuclide_Analysis();

    }

    public void Manual_Nuclide_Analysis() {

        // MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RUNNING;
        SetSourceIdRunningActivityMode();
        // MainActivity.mService.write(MainActivity.MESSAGE_START_HW);

        Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
        intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, Detector.EVENT_OFF);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        // --===--
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        ///////////////
        mManualID_Time.setText(String.valueOf(mManualID_GoalTime));
        Set_Invisible_ManualID_Contol(false);
        ///
        Start_ManualID();

        Intent send_gs = new Intent(MainBroadcastReceiver.MSG_TAB_DISABLE);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

    }

    @Override
    protected void onResume() {

        // DoubleClickRock = Activity_Mode.EXCUTE_MODE;

        /*
         * if(MainActivity.ACTIVITY_STATE == Activity_Mode.FIRST_ACTIVITY){
         *
         * MainActivity.ACTIVITY_STATE }
         */
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

    private OnTouchListener mTouchEvent = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {

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

    public void RightMove() {

        flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_left));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_right));
        flipper.showPrevious();

    }

    public void LeftMove() {

        flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.appear_from_right));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear_to_left));
        flipper.showNext();

    }

    public void DropDownAnimation() {

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.drop_down);
        linearLayout.startAnimation(anim);

    }

    public void SourceIdResultInfo() {

        mHandler.obtainMessage(IDspectrumValue.LIST_DRAW).sendToTarget();

        MainActivity.ACTIVITY_HW_KEY_ROOT_CHECK = Activity_Mode.NOT_FIRST_ACTIVITY;

        mPhotoName = new Vector<String>();
        mVideoName = new Vector<String>();
        mRecoderName = new Vector<String>();
        mTotalTxt = new Vector<String>();
        mRecoder = new Vector<String>();

        SetSourceIdResultActivityMode();

        ReachBackBtn = (ImageButton) findViewById(R.id.ReachBackBtn);
        CameraBtn = (ImageButton) findViewById(R.id.CameraBtn);
        VideoBtn = (ImageButton) findViewById(R.id.VideoBtn);
        VoiceBtn = (ImageButton) findViewById(R.id.VoiceBtn);

        ReachBackBtn.requestFocus();

        RecBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rec);

        ResultLocationInfoTxt = (TextView) findViewById(R.id.s_location_info);
        userInfoTxt = (TextView) findViewById(R.id.s_user_info);

        PreferenceDB mPreb = new PreferenceDB(mContext);
        userInfoTxt.setText(mPreb.Get_User_Name());

        ResultLocationInfoTxt.setText(mPreb.Get_Location_Info());

        TextView iDAcqTimeTxt, dateTimeTxt, dateTxt, EventId, INDTxt;

        removeEventTxt = (Button) findViewById(R.id.removeEventTxt);

        iDAcqTimeTxt = (TextView) findViewById(R.id.alarm_duration_info);

        dateTimeTxt = (TextView) findViewById(R.id.time_info);

        dateTxt = (TextView) findViewById(R.id.date_info);

        EventId = (TextView) findViewById(R.id.event_info);

        iDAcqTimeTxt.setText(String.valueOf(mManualID_GoalTime) + " " + getResources().getString(R.string.sec));

        LayoutA = (LinearLayout) findViewById(R.id.FocusA);
        LayoutB = (LinearLayout) findViewById(R.id.FocusB);
        LayoutC = (LinearLayout) findViewById(R.id.FocusC);

        CommentEdit = (EditText) findViewById(R.id.editT_Comment1);
        // '확인' 글자버튼
        CommentEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
        // 0124 comment 입력후 완료 누르면 키보드 내려가게
        CommentEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        Id_Result_Reset();

        mGallery = (ListView) findViewById(R.id.Gallery_List);

        dateTimeTxt.setText(m_EventData.Get_EndEventTime());

        dateTxt.setText(m_EventData.Get_Date());

        EventDBOper eventDB = new EventDBOper();

        int eventNumber = eventDB.GetEventCount();

        EventId.setText("#" + String.valueOf(eventNumber + 1));

        mEventNumber = eventNumber + 1;

        if (mEventNumber == -1) {
            IS_TAKE_PHOTO_AND_VIDEO = false;
            return;
        }
        mThumnail.clear();

        EventDBOper DB = new EventDBOper();
        DB.OpenDB();
        mPhoto = new Vector<String>();
        DB.EndDB();
        DB = null;

        EventDBOper DB1 = new EventDBOper();
        DB1.OpenDB();

        if (mVideo == null)
            mVideo = new Vector<String>();
        DB1.EndDB();
        DB1 = null;

        Init_GalleryView();
        // Detected_Isotope List

        findViewById(R.id.removeEventTxt).setOnClickListener(mClickListener);
        findViewById(R.id.RadresponderBtn).setOnClickListener(mClickListener);
        findViewById(R.id.ReachBackBtn).setOnClickListener(mClickListener);

        findViewById(R.id.CameraBtn).setOnClickListener(mClickListener);
        findViewById(R.id.VideoBtn).setOnClickListener(mClickListener);
        findViewById(R.id.VoiceBtn).setOnClickListener(mClickListener);

        String[] nuclide1 = null;
        String[] nuclide2 = null;
        String[] doserate = null;
        String[] level = null;
        String[] sp = null;
        if (m_EventData.Detected_Isotope.size() != 0) {

            nuclide1 = new String[m_EventData.Detected_Isotope.size()];
            nuclide2 = new String[m_EventData.Detected_Isotope.size()];
            doserate = new String[m_EventData.Detected_Isotope.size()];
            level = new String[m_EventData.Detected_Isotope.size()];
            sp = new String[m_EventData.Detected_Isotope.size()];

        }

        for (int i = 0; i < m_EventData.Detected_Isotope.size(); i++) {

            nuclide1[i] = m_EventData.Detected_Isotope.get(i).Class;
            nuclide2[i] = m_EventData.Detected_Isotope.get(i).isotopes;
            doserate[i] = m_EventData.Detected_Isotope.get(i).DoseRate_S;
            level[i] = String.format("%.0f", m_EventData.Detected_Isotope.get(i).Confidence_Level);

            if (m_EventData.Detected_Isotope.get(i).Screening_Process == 1) {
                sp[i] = "1";
            } else {
                sp[i] = "1";
            }
        }
        // ListViewSizeChange();

        Result_List_Array adapter = new Result_List_Array(this, R.layout.id_result_row, nuclide1, nuclide2, doserate, level, sp);

        m_lv = (ListView) findViewById(R.id.ListView);

        m_lv.setDivider(null);
        m_lv.setAdapter(adapter);

        Favorite_Checkbox = (CheckBox) findViewById(R.id.Favorite_Checkbox);

        CreateCsvFile();

    }

    // video photo function

    private void Init_GalleryView() {

        // mGallery.setDivider(null);

        mGallery.setAdapter(new Img_Video_Array(this));

        // mGallery.setSpacing(25);

        mGallery.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView parent, View v, int position, long id) {

                MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
                Intent intent = new Intent();

                intent.setAction(android.content.Intent.ACTION_VIEW);

                intent.addCategory(Intent.CATEGORY_DEFAULT);
                if (position < mPhoto.size()) {
                    Uri uri = Uri.fromFile(new File(mPhoto.get(position)));
                    intent.setDataAndType(uri, "image/*");
                } else if (position >= mPhoto.size() && position < mPhoto.size() + mVideo.size()
                        && mPhoto.size() != mPhoto.size() + mVideo.size()) {
                    Uri uri = Uri.fromFile(new File(mVideo.get(position - mPhoto.size())));
                    intent.setDataAndType(uri, "video/*");
                } else if (position >= mPhoto.size() + mVideo.size()
                        && position < mPhoto.size() + mVideo.size() + mRecoder.size()) {
                    Uri uri = Uri.fromFile(new File(mRecoder.get(position - (mPhoto.size() + mVideo.size()))));
                    intent.setDataAndType(uri, "audio/*");
                }

                // intent.setAction(intent.ACTION_CAMERA_BUTTON);
                startActivity(intent);
            }
        });

        mGallery.setOnItemSelectedListener(new ListView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,

                                       int position, long id) {

                mGalleryListViewCurrentPosition = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // TODO Auto-generated method stub

            }

        });

        mGallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {

                DeleteDlg(pos);

                return true;
            }

        });

    }

    private Bitmap overlayMark(Bitmap baseBmp, Bitmap overlayBmp) {
        Bitmap resultBmp = Bitmap.createBitmap(baseBmp.getWidth(), baseBmp.getHeight(), baseBmp.getConfig());
        Canvas canvas = new Canvas(resultBmp);
        canvas.drawBitmap(baseBmp, 0, 0, null);

        Paint paint = new Paint();
        paint.setColor(Color.argb(150, 40, 40, 40));
        canvas.drawRect(new Rect(0, 0, baseBmp.getWidth(), baseBmp.getHeight()), paint);

        RectF OverRect = new RectF(0f, 0f, overlayBmp.getWidth() * 1.2f, overlayBmp.getHeight() * 1.1f);
        canvas.drawBitmap(overlayBmp, new Rect(0, 0, overlayBmp.getWidth(), overlayBmp.getHeight()),
                new RectF((baseBmp.getWidth() / 2) - (OverRect.width() / 2),
                        (baseBmp.getHeight() / 2) - (OverRect.height() / 2),
                        OverRect.width() + (baseBmp.getWidth() / 2) - (OverRect.width() / 2),
                        OverRect.height() + (baseBmp.getHeight() / 2) - (OverRect.height() / 2)),
                null);
        return resultBmp;
    }

    public void Load_Thumnail() {

        mTotalTxt.clear();
        mThumnail.clear();

        if (mPhoto != null) {

            for (int i = 0; i < mPhoto.size(); i++) {
                // mPhoto.set(i,Environment.getExternalStorageDirectory()+"/"+Event.DB_FOLDER+"/"+mPhoto.get(i));
                mThumnail.add(BitmapFactory.decodeFile(mPhoto.get(i)));
                mTotalTxt.add(mPhotoName.get(i));

            }
        }

        if (mVideo != null) {
            for (int i = 0; i < mVideo.size(); i++) {
                // mVideo.set(i,Environment.getExternalStorageDirectory()+"/"+Event.DB_FOLDER+"/"+mVideo.get(i)+".mp4");
                Bitmap Thum = ThumbnailUtils.createVideoThumbnail(mVideo.get(i),
                        android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                mThumnail.add(overlayMark(Thum, new BitmapFactory().decodeResource(getResources(), R.drawable.play)));

                mTotalTxt.add(mVideoName.get(i));
            }
        }

        if (mRecoder != null) {
            for (int i = 0; i < mRecoder.size(); i++) {
                // mVideo.set(i,Environment.getExternalStorageDirectory()+"/"+Event.DB_FOLDER+"/"+mVideo.get(i)+".mp4");

                mThumnail.add(RecBitmap);

                mTotalTxt.add(mRecoderName.get(i));

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        //	if (resultCode == RESULT_OK) {
        File file = null;
        switch (requestCode) {
            case App.TAKE_PHOTO_CUSTOM:
            case 1:

                CameraBtn.setFocusable(true);
                CameraBtn.setFocusableInTouchMode(true);
                CameraBtn.requestFocus();


                if (mPhoto == null)
                    mPhoto = new Vector<String>();
                file = new File(Media.FolderPath + "/" + photoPath);
                if (file.exists()) {
                    Long size = file.length();
                    if (size > 0) {
                        mPhotoName.add("EventP" + mEventNumber + "_" + mFileNumber);
                        mPhoto.add(Media.FolderPath + "/EventP" + mEventNumber + "_" + mFileNumber + ".png");
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        String LastImgPath = mPhoto.lastElement();
                        Bitmap Photo = BitmapFactory.decodeFile(LastImgPath);
                        Bitmap resized = Bitmap.createScaledBitmap(Photo, (int) (Photo.getWidth() * 0.3), (int) (Photo.getHeight() * 0.3), true);
                        File Resizedfile = new File(LastImgPath);
                        FileOutputStream fileStream = null;
                        try {
                            fileStream = new FileOutputStream(Resizedfile);
                        } catch (FileNotFoundException e) {
                            NcLibrary.Write_ExceptionLog(e);
                        }

                        if (fileStream != null)
                            resized.compress(CompressFormat.PNG, 0, fileStream);
                        Photo.recycle();
                        resized.recycle();

                        mThumnail.add(BitmapFactory.decodeFile(LastImgPath));
                        Load_Thumnail();
                        Init_GalleryView();

                        new SingleMediaScanner(getApplicationContext(), Resizedfile);

                    }

                }


                break;
            case 2:
                VideoBtn.setFocusable(true);
                VideoBtn.setFocusableInTouchMode(true);
                VideoBtn.requestFocus();
                if (mVideo == null)
                    mVideo = new Vector<String>();

                //file = new File(Media.FolderPath + "/EventV" + mEventNumber + "_" + mFileNumber + ".mp4");
                file = new File(Media.FolderPath + "/" + videoPath);
                if (file.exists()) {
                    Long size = file.length();
                    if (size > 0) {
                        mVideo.add(Media.FolderPath + "/EventV" + mEventNumber + "_" + mFileNumber + ".mp4");

                        mVideoName.add("EventV" + mEventNumber + "_" + mFileNumber);
                        Bitmap Thum = ThumbnailUtils.createVideoThumbnail(mVideo.lastElement(),
                                android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                        Thum = overlayMark(Thum, new BitmapFactory().decodeResource(getResources(), R.drawable.play));

                        File Resizedfile = new File(Media.FolderPath + "/EventVP" + mEventNumber + "_" + mFileNumber + ".png");
                        FileOutputStream fileStream = null;
                        try {
                            fileStream = new FileOutputStream(Resizedfile);
                        } catch (FileNotFoundException e) {
                            NcLibrary.Write_ExceptionLog(e);
                        }

                        if (fileStream != null)
                            Thum.compress(CompressFormat.PNG, 0, fileStream);

                        MediaScannerConnection.scanFile(this, new String[]{Resizedfile.getAbsolutePath()}, null, null);

                        mThumnail.add(Thum);
                        Load_Thumnail();
                        Init_GalleryView();

                        new SingleMediaScanner(getApplicationContext(), Resizedfile);
                        new SingleMediaScanner(getApplicationContext(), new File(mVideo.lastElement()));
                    } else {
                        file.delete();
                    }
                }

                break;
            case 3:

                VoiceBtn.setFocusable(true);
                VoiceBtn.setFocusableInTouchMode(true);
                VoiceBtn.requestFocus();

                ArrayList<String> recodeFileList = new ArrayList<String>();
                recodeFileList = data.getStringArrayListExtra(Check.ListValue);
                if (recodeFileList.size() > 0) {
/*					file = new File(Media.FolderPath +"/" +recodeFileList.get(0));
					if(file.exists())
					{
						Long size = file.length();
						if(size > 0)
						{*/
                    for (int i = 0; i < recodeFileList.size(); i++) {
                        mRecoder.add(Media.FolderPath + "/" + recodeFileList.get(i) + ".amr");
                        mRecoderName.add(recodeFileList.get(i));
                    }
                    mThumnail.add(RecBitmap);
                    Load_Thumnail();
                    Init_GalleryView();
                    //		}
                    //	}


                }


                break;


            default:
                break;
            //		}

        }

/*		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;
			}
		};

		Timer mTimer = new Timer();
		mTimer.schedule(mTask, 1500);*/
        // MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;

    }

    Button.OnClickListener mClickListener = new View.OnClickListener() {

        public void onClick(View v) {

            File file;

            if (v.getId() == R.id.CameraBtn) {
                //MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
                //MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
                PhotoExcute();

            } else if (v.getId() == R.id.VideoBtn) {
                //MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
                //MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
                VideoExcute();

            } else if (v.getId() == R.id.VoiceBtn) {

                VoiceRecoderExcute();

            } else if (v.getId() == R.id.removeEventTxt) {

                RemoveExucute();

            } else if (v.getId() == R.id.RadresponderBtn) {

                PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
                //String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventlist_key));
                //20.01.06 rad_response_eventid_key로 수정 기존(0,1로만 저장)
                //String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key));
                String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key)) == null ? getString(R.string.rad_response_eventlist11) : prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key));
/*				if (abc == null) {
					abc = "0";
				}*/
                if (abc.equals(getString(R.string.rad_response_eventlist11))) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IDspectrumActivity.this);
                    dialogBuilder.setTitle(getResources().getString(R.string.Radresponder_Emergency_Msg_Title));
                    dialogBuilder.setMessage(getResources().getString(R.string.Radresponder_Emergency_Msg_Contents));
                    dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData);
                                    RadresponderExucute();
                                }
                            });
                    dialogBuilder.setNegativeButton("Cancel", null);
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.show();

                } else if (abc.equals(RadresponderActivity.mTestingTraining)) {
                    NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData);
                    RadresponderExucute();
                }

            } else if (v.getId() == R.id.ReachBackBtn) {

                NcLibrary.SendEmail(m_EventData, mContext, mHandler);

                // ReachBackExucute();

            }
        }
    };

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

    class Result_List_Array extends ArrayAdapter {

        // 생성자 내부에서 초기화
        // Context mContext = null;
        String[] nuclide1 = new String[10];
        String[] nuclide2 = new String[10];
        String[] doserate = new String[10];
        String[] level = new String[10];
        String[] sp = new String[10];

        public Result_List_Array(Context context, int resource, String[] Nuclide1, String[] Nuclide2, String[] Doserate,
                                 String[] Level, String[] Sp) {
            super(context, resource);
            // TODO Auto-generated constructor stub

            mContext = context;

            nuclide1 = Nuclide1;

            nuclide2 = Nuclide2;

            doserate = Doserate;

            level = Level;

            sp = Sp;

        }

        // 어뎁터 카운트를 설정한다

        @Override
        public int getCount() {

            return m_EventData.Detected_Isotope.size();
        }

        @Override
        public Object getItem(int position) {

            return position;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        // ListView에서 각 행(row)을 화면에 표시하기 전 호출됨.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = (View) inflater.inflate(R.layout.id_result_row, null);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

            // LayoutInflater의 객체 inflater를 현재 context와 연결된 inflater로 초기화.

            // 가장 첫번재 글 글 배경 색 바꾸기
            // 카테고리를 담을 공간

            // 아이디를 담은 공간 생성
            TextView nuclide1Txt = (TextView) row.findViewById(R.id.NuclideTxt);
            nuclide1Txt.setText(nuclide1[position]);
            if (nuclide1[position].equals("IND")) {

                nuclide1Txt.setTextColor(Color.rgb(157, 207, 255));

            } else if (nuclide1[position].equals("MED")) {

                nuclide1Txt.setTextColor(Color.rgb(44, 192, 185));

            } else if (nuclide1[position].equals("NRM")) {

                nuclide1Txt.setTextColor(Color.rgb(0, 150, 20));

            } else if (nuclide1[position].equals("SNM")) {

                nuclide1Txt.setTextColor(Color.rgb(150, 24, 150));

            } else if (nuclide1[position].equals("UNK")) {

                nuclide1Txt.setTextColor(Color.rgb(206, 28, 32));

            }

            // 댓글 갯수를 담을 공간
            TextView nuclide2Txt = (TextView) row.findViewById(R.id.NuclideTxt2);


            // TextView 객체 label을 row 객체 내부에 있는 R.id.label로 초기화
            // 내용 담을 공간
            TextView doserateTxt = (TextView) row.findViewById(R.id.DoserateTxt);
            // label에 텍스트 설정.


            // 날짜 시간 담을 공간
            TextView levelTxt = (TextView) row.findViewById(R.id.LevelTxt);


            // 커스터마이징 된 View 리턴.

            if (sp[position].equals("1")) {
                nuclide2Txt.setTextColor(Color.rgb(230, 220, 0));
                doserateTxt.setTextColor(Color.rgb(230, 220, 0));
                levelTxt.setTextColor(Color.rgb(230, 220, 0));
            } else {
                nuclide2Txt.setTextColor(Color.rgb(230, 220, 0));
                doserateTxt.setTextColor(Color.rgb(230, 220, 0));
                levelTxt.setTextColor(Color.rgb(230, 220, 0));
            }
            nuclide2Txt.setText(nuclide2[position]);
            doserateTxt.setText(doserate[position]);
            levelTxt.setText(level[position] + "%");
            return row;
        }

    }

    ;

    public void ListViewSizeChange() {

        m_lv = (ListView) findViewById(R.id.ListView);

        View m_lv1 = (View) this.findViewById(R.id.ListView);
        int tabBodyWidth = m_lv1.getWidth();
        int tabBodyHeight = m_lv1.getHeight();

        String str = Integer.toString(m_EventData.Detected_Isotope.size());
        String str1 = Integer.toString(tabBodyWidth);
        String str2 = Integer.toString(tabBodyHeight);

        // Toast.makeText(getApplicationContext(), "isotope 사이즈: " + str + "가로크기
        // : " + str1 + "세로크기 :" + str2, 1).show();

        if (m_EventData.Detected_Isotope.size() > 2) {
            m_lv.setLayoutParams(
                    new LinearLayout.LayoutParams(tabBodyWidth, tabBodyHeight * m_EventData.Detected_Isotope.size()));

        } else {
            m_lv.setLayoutParams(new LinearLayout.LayoutParams(tabBodyWidth, tabBodyHeight * 2));
        }
    }

    public byte[] bitmapToByteArray(Bitmap $bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        $bitmap.compress(CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public Bitmap byteArrayToBitmap(byte[] $byteArray) {

        Bitmap bitmap = BitmapFactory.decodeByteArray($byteArray, 0, $byteArray.length);
        return bitmap;
    }

    public void TimeUp() {
        mManualID_GoalTime += mManualID_Adjust_sec;
        acqTimeTxt2.setText(String.valueOf(mManualID_GoalTime));

    }

    public void TimeDown() {

        if (m_EventData.MS.Get_AcqTime() < mManualID_GoalTime - mManualID_Adjust_sec) {
            mManualID_GoalTime -= mManualID_Adjust_sec;
            acqTimeTxt2.setText(String.valueOf(mManualID_GoalTime));
        }

    }

    public void View_Filpper() {

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

    public void Id_Result_Reset() {

        checkMediaModeCount = Activity_Mode.UN_EXCUTE_MODE;

        rootFocusCnt = Focus.ID_RESULT_MENU_C;

        idBottomSwicthCount = Focus.ID_RESULT_MENU_C_REMOVE_BTN;

        id_result_menu_b_count = Activity_Mode.UN_EXCUTE_MODE;
    }

    public void VoiceRecoderExcute() {

        Intent intent = new Intent(IDspectrumActivity.this, RecActivity.class);

        intent.putExtra(Check.ListNumber, mEventNumber);

        startActivityForResult(intent, 3);

    }

    public void VideoExcute() {

        File file;
        if (mVideo == null)
            mVideo = new Vector<String>();

        for (int i = 1; i < 1000; i++) {

            File mFile;
            mFile = new File(Media.FolderPath + "/EventV" + mEventNumber + "_" + i + ".mp4");
            if (!mFile.exists()) {

                mFileNumber = i;
                break;
            }

        }

        Intent intent = new Intent(IDspectrumActivity.this, VideoActivity.class);
        videoPath = "EventV" + mEventNumber + "_" + mFileNumber + ".mp4";
        intent.putExtra("path", videoPath);
        //file = new File(Media.FolderPath, File);
        // mFile = new File(getIntent().getStringExtra("file"));
        startActivityForResult(intent, 2);

/*		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			Intent intent = new Intent(IDspectrumActivity.this, VideoActivity.class);
			String filePath = "EventV" + mEventNumber + "_" + mFileNumber + ".mp4";
			intent.putExtra("path",filePath);
			//file = new File(Media.FolderPath, File);
			// mFile = new File(getIntent().getStringExtra("file"));
			startActivityForResult(intent, 2);
		}
		else
		{
			Intent Intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			String File = "EventV" + mEventNumber + "_" + mFileNumber + ".mp4";
			file = new File(Media.FolderPath, File);
			Intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(Intent, 2);
		}*/
    }

    public void PhotoExcute() {

        File file;

        if (mPhoto == null)
            mPhoto = new Vector<String>();

        for (int i = 1; i < 1000; i++) {

            //	File mFile;
            mFile = new File(Media.FolderPath + "/EventP" + mEventNumber + "_" + i + ".png");
            if (!mFile.exists()) {

                mFileNumber = i;
                break;
            }

        }

        photoPath = "EventP" + mEventNumber + "_" + mFileNumber + ".png";

        Intent intent = null;
        intent = new Intent(IDspectrumActivity.this, Camera2Activity.class);
        //mFile = CommonUtils.createImageFile("mFile");
        //파일 저장 경로와 이름
        intent.putExtra("file", photoPath);
        intent.putExtra("hint", getResources().getString(R.string.camera_area));
        //프레이밍 영역 (전체 밝은 영역)으로 전체 화면 사용 여부
        intent.putExtra("hideBounds", false);
        //최대 허용 카메라 크기 (픽셀 수)
        intent.putExtra("maxPicturePixels", 3840 * 2160);
        startActivityForResult(intent, App.TAKE_PHOTO_CUSTOM);
	/*	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			intent = new Intent(IDspectrumActivity.this, Camera2Activity.class);
			//mFile = CommonUtils.createImageFile("mFile");
			//파일 저장 경로와 이름
			intent.putExtra("file", mFile.toString());
			intent.putExtra("hint", getResources().getString(R.string.camera_area));
			//프레이밍 영역 (전체 밝은 영역)으로 전체 화면 사용 여부
			intent.putExtra("hideBounds", false);
			//최대 허용 카메라 크기 (픽셀 수)
			intent.putExtra("maxPicturePixels", 3840 * 2160);
			startActivityForResult(intent, App.TAKE_PHOTO_CUSTOM);
		}
		else
		{
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			String FileName = "EventP" + mEventNumber + "_" + mFileNumber + ".png";
			file = new File(Media.FolderPath, FileName);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			// cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,600*600);
			startActivityForResult(cameraIntent, 1);
		}
*/


    }

    public void RemoveExucute() {
        MainActivity.tabHost.setCurrentTab(0);
        Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT_CANCEL);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
        SOURCE_ID_RESULT_MODE = false;
        m_EventData = null;
        setContentView(R.layout.black_background);

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
        ArrayList<String> mDoserate_Unit = new ArrayList<String>();
        ArrayList<String> mConfidence_Level = new ArrayList<String>();

        ArrayList<String> mComment = new ArrayList<String>();
        ArrayList<String> mInstrumentModel_Name = new ArrayList<String>();


        m_EventData.Comment = "";
        // comment 저장
        if (CommentEdit.getText().toString().length() != 0) {
            String tem = CommentEdit.getText().toString();
            // tem = tem.replace("\"", "'");
            tem = tem.replace("'", "''");
            m_EventData.Comment = tem;
        }


        mDate.add(m_EventData.EventData);
        mAcqTime.add(String.valueOf(m_EventData.MS.Get_AcqTime()));
        mGamma.add(m_EventData.Doserate_AVGs);
        mTime.add(m_EventData.StartTime);
        mManual_ID.add((m_EventData.Event_Detector));// .IsManualID==true)?"MANUAL
        // ID":"");
        mEventID.add(String.valueOf(mEventNumber + 1));
        mInstrumentModel_Name.add(m_EventData.mInstrument_Name);

        // radreponder value Add Part

        Vector<Isotope> Id = m_EventData.Detected_Isotope;

        String temp = "";
        String temp2 = "";
        String temp3 = "";
        String temp4 = "";
        String temp5 = "";
        String temp6 = "";
        if (Id == null || Id.size() == 0) {
            temp = "None";
            temp2 = "None";
            temp3 = "None";
            temp4 = "None";
            temp5 = "None";
            temp6 = "None";
        } else {
            for (int k = 0; k < Id.size(); k++) {
                String[] DoseRate_S = Id.get(k).DoseRate_S.split(" ");
                if (k == Id.size() - 1) {

                    temp += Id.get(k).isotopes;
                    temp2 += DoseRate_S[0];
                    temp3 += Integer.toString((int) Id.get(k).Confidence_Level);
                    temp4 += Id.get(k).isotopes;
                    temp5 += Id.get(k).isotopes;

                    temp6 += DoseRate_S[1] + ",";
                    break;

                } else {
                    temp += Id.get(k).isotopes + ", ";
                    temp2 += DoseRate_S[0] + ",";
                    temp3 += Integer.toString((int) Id.get(k).Confidence_Level) + ",";
                    temp4 += Id.get(k).isotopes + ",";

                    temp5 += Id.get(k).isotopes + " ";

                    temp6 += DoseRate_S[1] + ",";
                }

            }
        }

        mId.add(temp);
        mDoserate_S.add(temp2);
        mDoserate_Unit.add(temp6);
        mConfidence_Level.add(temp3);
        mSourceName.add(temp4);

        mComment.add(m_EventData.Comment);

        mStartTime.add(m_EventData.EventData + " " + m_EventData.StartTime);

        mEndTime.add(m_EventData.EventData + " " + m_EventData.EndTime);

        mLatitude.add(Double.toString(m_EventData.GPS_Latitude));
        mLongitude.add(Double.toString(m_EventData.GPS_Longitude));

        Intent intent = new Intent(IDspectrumActivity.this, RadresponderActivity.class);
        intent.putExtra(RadresponderActivity.GPS_LAT, mLatitude.get(0));
        intent.putExtra(RadresponderActivity.GPS_LONG, mLongitude.get(0));

        // Toast.makeText(getApplicationContext(), mLatitude.get(0) + "," +
        // mLongitude.get(0), 1).show();
        // String[] mGamma1 = mGamma.get(0).split(" ");

        intent.putExtra(RadresponderActivity.DOSERATE_TYPE, "uSv/h");

        intent.putExtra(RadresponderActivity.DOSERATE_UNIT, mDoserate_Unit.get(0));

        //191031 eventlist에 맞춰서 수정
        String[] date = mDate.get(0).split(" ");
        intent.putExtra(RadresponderActivity.COLLECTION_DATE, date[0]);
        date = mStartTime.get(0).split(" ");
        intent.putExtra(RadresponderActivity.START_TIME, date[1]);
        date = mEndTime.get(0).split(" ");
        intent.putExtra(RadresponderActivity.END_TIME, date[1]);

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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IDspectrumActivity.this);
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

                                    PreferenceDB pref = new PreferenceDB(IDspectrumActivity.this.mContext);
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
                                        m.addAttachment(GetCsvPath(), "SAM (" + CurrentDate() + " ).csv");
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

    public void Source_id_Hw_Key_Back() {

        if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

            Source_Id_Running_Cancel();

        } else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {

            Source_Id_Result_Cancel();

        } else if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT_CAMERA) {

            new Thread(new Runnable() {

                public void run() {

                    new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                }
            }).start();

        }
    }

    public void Source_Id_Running_Cancel() {

        MainActivity.tabHost.setCurrentTab(0);
        Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_CANCEL);

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

        Set_Invisible_ManualID_Contol(true);
        mProgBar.Set_Value(0);
        mProgBar.invalidate();
        End_ManualID();
        m_EventData = null;
        mSPC.ClearSPC();

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMainBCR);

        IntentFilter filter = new IntentFilter();

        filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_BACK);

        filter.addAction(MainBroadcastReceiver.MSG_HW_KEY_ENTER);

        filter.addAction(MainBroadcastReceiver.MSG_SOURCE_ID_RUNNING_START);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMainBCR, filter);

        setContentView(R.layout.black_background);

    }

    public void Source_Id_Result_Cancel() {

        //190102 추가
        //MainActivity.SendU4AA();
        //NcLibrary.SaveText1("Source_Id_Result_Cancel U4AA \n","test");
        //NcLibrary.SaveText1("u4aa\n","test");

        //NcLibrary.SaveText1("Finish \n","test");
        MainActivity.tabHost.setCurrentTab(0);

        // comment 저장
        if (CommentEdit.getText().toString().length() != 0) {
            String tem = CommentEdit.getText().toString();
            tem = tem.replace("\"", "'");
            tem = tem.replace("'", "\"");
            m_EventData.Comment = tem;
        }

        if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RESULT) {

            Intent send_gs = new Intent(MainBroadcastReceiver.MSG_SOURCE_ID_RESULT_CANCEL);

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);
            SOURCE_ID_RESULT_MODE = false;

            m_EventData.PhotoFileName = mPhotoName;

            m_EventData.VedioFileName = mVideoName;

            m_EventData.RecodeFileName = mRecoderName;
            if (Favorite_Checkbox != null) {
                if (Favorite_Checkbox.isChecked()) {

                    m_EventData.Favorite_Checked = Check.Favorite_True;
                } else {

                    m_EventData.Favorite_Checked = Check.Favorite_False;

                }
            }


            if (WriteEvent_toDB(m_EventData)) {
                Toast.makeText(getApplicationContext(), " saved in DB", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "failed to save to DB", Toast.LENGTH_SHORT).show();
            }


            m_EventData = null;

            Id_Result_Reset();
            setContentView(R.layout.black_background);

        }
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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IDspectrumActivity.this);
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

                if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
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
                if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
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
                if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {
                    TimeUp();

                    return true;
                } else if (DoubleClickRock == Activity_Mode.EXCUTE_MODE) {

                    KeyExecute(KeyEvent.KEYCODE_DPAD_UP);
                    return false;
                }

                return true;
            }
            case KeyEvent.KEYCODE_DPAD_DOWN: {
                if (MainActivity.ACTIVITY_STATE == Activity_Mode.SOURCE_ID_RUNNING) {

                    if (mBtnDownCount == 0) {
                        // TimeDown();

                    }

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

        DoubleClickRock();

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
        Log.d("time:", "touch : idspectrum");

        if (v.getContext() == MainActivity.tabHost.getTabWidget().getChildAt(1).getContext()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPreTouchPosX = (int) event.getX();
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

                /*
                 * int nTouchPosY = (int) event.getY();
                 *
                 * String str = Integer.toString(nTouchPosX);
                 *
                 * String str2 = Integer.toString(nTouchPosY);
                 *
                 * Toast.makeText(getApplicationContext(), "X: " + nTouchPosX + ", Y:" +
                 * nTouchPosY, 1).show();
                 */

                return true;
            }
        }
        return false;
    }

    ;

    private void DeleteDlg(final int position1) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IDspectrumActivity.this);
        dialogBuilder.setTitle(getResources().getString(R.string.delete));

        dialogBuilder.setMessage(getResources().getString(R.string.delete_message));

        if (position1 < mPhoto.size()) {

            dialogBuilder
                    .setMessage(getResources().getString(R.string.delete_message) + "\n\n" + mPhotoName.get(position1));

        } else if (position1 >= mPhoto.size() && position1 < mPhoto.size() + mVideo.size()
                && mPhoto.size() != mPhoto.size() + mVideo.size()) {

            dialogBuilder.setMessage(getResources().getString(R.string.delete_message) + "\n\n"
                    + mVideoName.get(position1 - mPhoto.size()));

        } else if (position1 >= mPhoto.size() + mVideo.size()
                && position1 < mPhoto.size() + mVideo.size() + mRecoder.size()) {

            dialogBuilder.setMessage(getResources().getString(R.string.delete_message) + "\n\n"
                    + mRecoderName.get(position1 - (mPhoto.size() + mVideo.size())));

        }

        dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                if (position1 < mPhoto.size()) {

                    File f2d = new File(mPhoto.get(position1));
                    f2d.delete();
                    mPhoto.remove(position1);
                    mPhotoName.remove(position1);

                } else if (position1 >= mPhoto.size() && position1 < mPhoto.size() + mVideo.size()
                        && mPhoto.size() != mPhoto.size() + mVideo.size()) {

                    File f2d = new File(mVideo.get(position1 - mPhoto.size()));
                    f2d.delete();

                    mVideo.remove(position1 - mPhoto.size());
                    mVideoName.remove(position1 - mPhoto.size());

                } else if (position1 >= mPhoto.size() + mVideo.size()
                        && position1 < mPhoto.size() + mVideo.size() + mRecoder.size()) {

                    File f2d = new File(mRecoder.get(position1 - (mPhoto.size() + mVideo.size())));
                    f2d.delete();

                    mRecoder.remove(position1 - (mPhoto.size() + mVideo.size()));
                    mRecoderName.remove(position1 - (mPhoto.size() + mVideo.size()));

                }

                Load_Thumnail();
                Init_GalleryView();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.show();

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
        mTimer.schedule(mTask, 200);

    }

    public void SetSourceIdRunningActivityMode() {

        MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RUNNING;

    }

    public void SetSourceIdResultActivityMode() {

        MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT;

    }

    public void CreateCsvFile() {

        String[] mGamma1 = NcLibrary.SvToString(m_EventData.Doserate_AVG, true,
                (m_EventData.Doserate_unit == Detector.DR_UNIT_SV) ? true : false).split(" ");
        String mDoserate = "";
        if (mGamma1[1].equals("uSv/h")) {
            double d = Double.valueOf(mGamma1[0]).doubleValue() * 100;
            mDoserate = String.format("%.6f", d);
        }

        if (mGamma1[1].equals("urem/h")) {
            double d = Double.valueOf(mGamma1[0]).doubleValue();
            mDoserate = String.format("%.6f", d);
        }
        if (mGamma1[1].equals("mSv/h")) {
            double d = Double.valueOf(mGamma1[0]).doubleValue() * 1000000;
            mDoserate = String.format("%.6f", d);
        }

        if (mGamma1[1].equals("Sv/h")) {
            double d = Double.valueOf(mGamma1[0]).doubleValue() * 10000;
            mDoserate = String.format("%.6f", d);
        }

        String mGPS_Latitude = String.format("%.6f", m_EventData.GPS_Latitude);
        String mGPS_Longitude = String.format("%.6f", m_EventData.GPS_Longitude);
        String mDate = DateChange();

        String EventLogNumber = Integer.toString(mEventNumber);

        ArrayList<String> mId = new ArrayList<String>();

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
                    temp3 += Integer.toString((int) Id.get(k).Confidence_Level);
                    temp4 += Id.get(k).isotopes;
                    break;

                } else {
                    temp += Id.get(k).isotopes + ", ";
                    temp2 += DoseRate_S[0] + ",";
                    temp3 += Integer.toString((int) Id.get(k).Confidence_Level) + ",";
                    temp4 += Id.get(k).isotopes + ",";
                }

            }
        }

        mId.add(temp);

        String[] Isotope = mId.get(0).split(",");

        String mIsotope = "";
        for (int i = 0; i < Isotope.length; i++) {

            mIsotope += Isotope[i] + " ";
        }

        String mCPM = String.valueOf((int) ((m_EventData.MS.Get_AvgCPS() + m_EventData.MS.GetAvgFillCps()) * 60));

        String mComment = m_EventData.Comment;

        String enc = new java.io.OutputStreamWriter(System.out).getEncoding();
        String mInstrument_Name = m_EventData.mInstrument_Name;

        try {

            String head = " ,EventLogNumber, Serial number, DATE, Background_CPM, Latitude , Longitude, ISOTOPE, Avg. Dose Rate(uR/h) \r\n";

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(GetCsvPath()), "MS949"));
            writer.write(head);

            String Row = "\r\n" + " " + "," + EventLogNumber + "," + mInstrument_Name + "," + mDate + ", " + mCPM + ", "
                    + mGPS_Latitude + "," + mGPS_Longitude + "," + mIsotope + "," + mDoserate;

            writer.write(Row);

            writer.close();

        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }

    }

    private String GetCsvPath() {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EventDB.csv";

        return path;
    }

    private String DateChange() {

        String mDateStr = m_EventData.EventData;

        // mAllLog.get(i).EventData
        String[] mDateArray = mDateStr.split("-");

        String mDateSub = mDateArray[2] + "/" + mDateArray[1] + "/" + mDateArray[0];

        return mDateSub;
    }

    public String CurrentDate() {
        Date date = new Date();
        Format formatter;
        formatter = new SimpleDateFormat("dd-MM-yyyy"); // d는 day 이런식
        String mDateStr = formatter.format(date);

        return mDateStr;
    }

    private void IsotopeInvisibleViewFirstFiveSecond(IsotopesLibrary IsoLib) {


        m_EventData.Detected_Isotope = IsoLib.Find_Isotopes_with_Energy(m_EventData.MS, m_EventData.BG);

/*		if (m_EventData.MS.Get_AcqTime() <= 5 & m_EventData.Detected_Isotope.size() > 2) {
			Isotope iso1 = m_EventData.Detected_Isotope.get(0);
			Isotope iso2 = m_EventData.Detected_Isotope.get(1);

			m_EventData.Detected_Isotope.clear();
			m_EventData.Detected_Isotope.add(iso1);
			m_EventData.Detected_Isotope.add(iso2);
		}

		if (mDebug.IsDebugMode) {

			if (mDebug.IsIsotopeInvisibleViewFirstFiveSecond) {
				if (m_EventData.MS.Get_AcqTime() <= 5) {

					m_EventData.Detected_Isotope.clear();
				}
			}

		} else {

			if (m_EventData.MS.Get_AcqTime() <= 5) {

				m_EventData.Detected_Isotope.clear();
			}

		}
*/
    }

    public void IDTh232removeUnkownPeakFilter() {

        boolean IdTh232 = false;

        for (int i = 0; i < m_EventData.Detected_Isotope.size(); i++) {
            if (m_EventData.Detected_Isotope.get(i).isotopes.matches("Th-232")) {
                IdTh232 = true;
            }
            if (IdTh232 == true && m_EventData.Detected_Isotope.get(i).isotopes.matches("Unknown"))
                m_EventData.Detected_Isotope.remove(i);
        }
    }


}
