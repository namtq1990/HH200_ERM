package android.HH100.Structure;

import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.FindPeaksM;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.MainActivity;
import android.HH100.Service.MainBroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import Debug.Debug;

public class Detector implements Serializable {
    public final boolean D = MainActivity.D;

    public final String TAG_GAIN = "GainStabilization";
    public final String TAG_Event = "event";

    public interface HwPmtProperty_Code {
        int CeBr_1_5x1_5 = 65;
        int CeBr_2x2 = 72;
        int CeBr_3x3 = 77;
        int LaBr_1_5x1_5 = 66;
        int LaBr_2x2 = 73;
        int LaBr_3x3 = 78;
        int NaI_1x1 = 74;
        int NaI_2x2 = 68;
        int NaI_3x3 = 67;
        int NaI_2x3 = 75;
        int NaI_1_5x1_5 = 69;
        int NaI_2x4x16 = 70;
        int NaI_3x5x16 = 71;
        int NaI_4x4x16 = 76;
    }

    public enum CrystalType {
        NaI, CeBr, LaBr
    }


    // Dose Coeff 200514
    //Hung NM
    double [] GECoef=new double [6];

    public void setGECoef(double []GECoef)
    {
        for (int i = 0; i <GECoef.length ; i++)
        {
            this.GECoef[i]=GECoef[i];
        }

    }

    public double [] getGECoef()
    {
        return GECoef;
    }

    Debug mDebug;

    public CrystalType mCrystal = CrystalType.NaI;

    public double mPmtSurface = 0;

    public static final int DR_UNIT_SV = 20223;
    public static final int DR_UNIT_R = 20224;

    public static class MeasurementInfo {
        private String mInstrumentModel_Name;
        private String mInstrumentModel_MacAddress;
        private String mLocation;
        private String mUser;
        private int mAlarmSound;
        private String mIsoLibraryName;

        private boolean mIsSvUnit = true;
        private boolean mIsManualID = false;

        public MeasurementInfo() {

        }

        public void Set_Info(String instrumentName, String instrumentMacAdd, String location, String User,
                             int AlarmSound, String Isolibrary, boolean IsSvUnit, boolean IsManualID) {
            mInstrumentModel_Name = instrumentName;
            mInstrumentModel_MacAddress = instrumentMacAdd;
            mLocation = location;
            mUser = User;
            mAlarmSound = AlarmSound;
            mIsoLibraryName = Isolibrary;
            mIsSvUnit = IsSvUnit;
            mIsManualID = IsManualID;
        }
    }

    private static final long serialVersionUID = -6726229612941490740L;
    private static final int SPC_DOSERATE_LIMIT = 100000000; // 100mSv
    // define

    public int EVENT_STATUS = 10;
    public int EVENT_STATUS_N = 10;
    public static final int EVENT_NONE = 10;
    public static final int EVENT_BEGIN = 11;
    public static final int EVENT_ING = 12;
    public static final int EVENT_ING_HEALTH = 14;
    public static final int EVENT_FINISH = 13;
    public static final int EVENT_OFF = 15;
    public static final int EVENT_ON = 16;

    public static final int EVENT_MSG_MOVE_FORWARD = 500;
    public static final int EVENT_MSG_DANGER = 501;
    public static final int EVENT_MSG_MOVE_BACK = 502;
    public static final int EVENT_MSG_IN_RANGE = 503;

    private static final int SIGMA_ACCUMUL_SEC = 10;
    private static final int PAST_SPC_ARRAY_SIZE = 40;

    public static final int ID_MODE = 4323;
    public static final int SETUP_MODE = 4324;

    public static final int GAIN_START_IN_SEC = 10;
    public static final int GAIN_THRESHOLD_CNT = 200;

    public int mGain_restTime = 5; // rest time for gain statbilization, init value

    // variable
    private MeasurementInfo mMS_Info = new MeasurementInfo();

    public String InstrumentModel_Name = "";
    public String InstrumentModel_MacAddress;
    public String Location;
    public String User;
    public String GMT;
    public Vector<Double> RealTime_CPS = null;

    public int mHW_GC;

    public int mHW_FixedGC;

    public int mHW_K40_FxiedCh;

    public int mHW_Cs137_FxiedCh1;
    public int mHW_Cs137_FxiedCh2;

    public boolean mIsNeutronModel = false;

    private Spectrum mMS_Gainstabilization = new Spectrum();
    public EventData mGamma_Event = null;
    public EventData mNeturonEvent = null;

    public Spectrum MS = new Spectrum();
    public Spectrum Real_BG = new Spectrum();
    public Spectrum DB_BG = new Spectrum();
    public Neutron mNeutron = new Neutron();
    public int GM_Cnt;
    public double Coeffcients[] = new double[3];
    public double GPS_Longitude;
    public double GPS_Latitude;
    public int AlarmSound;
    public boolean IsSvUnit = true;
    public boolean IsManualID = false;

    public String Event_Detector = "None";

    public double Low_discrimination;
    public double Upper_discrimination;
    /// private
    private boolean IsHealthEvent = false;
    private int mMode = SETUP_MODE;
    public int mGain_elapsedTime = GAIN_START_IN_SEC;

    public boolean IsSigmaThreshold = false;
    private Vector<Integer> Gamma_CPS_SigmaThre = new Vector<Integer>();

    public double Gamma_Threshold;
    public double Gamma_SigmaThreshold;
    public static int HealthSafety_Threshold = 0;
    public static double mGammaDoserate = 0;

    public static ArrayList<String> StrArraylist = new ArrayList<String>();

    //	int mCrystalType = HwPmtProperty_Code.NaI_2x2;
    int mCrystalType = HwPmtProperty_Code.CeBr_2x2;
    public double mGCFactor = 6;
    public double mGCDefFactor = 6;

    public double Neutron_ThresholdCnt;

    public boolean IsGainStb = true;

    // --
    private Context mSuper = null;

    public static int mCrtstalType = 0;

    private Context mRealTimeContext = null;

    // sepctrum test

    public int GainSpeCount = 0;
    public int mSaveCount = 0;

    //190118 real time 추가
    public static int realTime = 1;


    public Detector() {
    }

    public Detector(Context context) {
        mSuper = context;
    }

    public Detector(Context context, int RealtimeContext) {
        mRealTimeContext = context;
    }

    public void Set_Spectrum(int[] Spc) {
        try {
            // SetCrystalType
            MS.Set_crystalType(mCrystalType);
            MS.Set_Spectrum(Spc);

            if (mMode == ID_MODE) {
                // for FINDER
                if (RealTime_CPS != null) {
                    RealTime_CPS.add((double) MS.Get_TotalCount());
                    if (RealTime_CPS.size() > PAST_SPC_ARRAY_SIZE)
                        RealTime_CPS.remove(0);
                } else {
                    RealTime_CPS = new Vector<Double>();
                    for (int i = 0; i < PAST_SPC_ARRAY_SIZE; i++)
                        RealTime_CPS.add((double) -1);

                    RealTime_CPS.add((double) MS.Get_TotalCount());
                    if (RealTime_CPS.size() > PAST_SPC_ARRAY_SIZE)
                        RealTime_CPS.remove(0);
                }
                //
                /////////////////////////// Event

                // Gamma event
                // if(IsManualID) return;

                Operator_HealthSafetyEventSatus(Get_Gamma_DoseRate_nSV());
                double GammaTh = Get_GammaThreshold();

                if (GammaTh > 0) {
                    if (GammaTh < MS.Get_TotalCount() | IsHealthEvent == true) {
                        //NcLibrary.SaveText1(" GammaTh : "+GammaTh+" MS.Get_TotalCount() : "+ MS.Get_TotalCount()+" IsHealthEvent : "+IsHealthEvent+"\n","test");
                        Operator_GammaEventSatus(true);
                    } else
                        Operator_GammaEventSatus(false);
                }
                // Neutron event
                if (Neutron_ThresholdCnt < mNeutron.Get_CPS())
                    Operator_NeutronEventSatus(true);
                else
                    Operator_NeutronEventSatus(false);

                // When event
                Event_Process();

                if (Is_Event() == false) {
                    Gamma_CPS_SigmaThre.add(MS.Get_TotalCount());
                    if (Gamma_CPS_SigmaThre.size() > SIGMA_ACCUMUL_SEC)
                        Gamma_CPS_SigmaThre.remove(0);

                } else {

                }

            }
            // --===--

            // MS.Set_HealthSafety_Threshold(HealthSafety_Threshold);
            // MS.Set_mGammaDoserate(mGammaDoserate);
            // MS.Set_crystalType(mCrystalTypeInt);

            Intent intent = new Intent(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
            intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, MS);
            // intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
            // --===--

            // --===--
            Intent intent2 = new Intent(MainBroadcastReceiver.MSG_RECV_NEUTRON);
            intent2.putExtra(MainBroadcastReceiver.DATA_NEUTRON, mNeutron);
            // intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
            // --===--

            ////////////////////
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        ;
    }

    public void Set_Spectrum(int[] Spc, int acq) {
        try {

            //190118 추가
            realTime = acq;
            if (realTime <= 0) {
                realTime = 1;
            }

            // SetCrystalType
            MS.Set_crystalType(mCrystalType);
            MS.Set_Spectrum(Spc, acq);

            if (mMode == ID_MODE) {
                // for FINDER
                if (RealTime_CPS != null) {
                    RealTime_CPS.add((double) MS.Get_TotalCount() / realTime);
                    if (RealTime_CPS.size() > PAST_SPC_ARRAY_SIZE)
                        RealTime_CPS.remove(0);
                } else {
                    RealTime_CPS = new Vector<Double>();
                    for (int i = 0; i < PAST_SPC_ARRAY_SIZE; i++)
                        RealTime_CPS.add((double) -1);

                    RealTime_CPS.add((double) MS.Get_TotalCount() / realTime);
                    if (RealTime_CPS.size() > PAST_SPC_ARRAY_SIZE)
                        RealTime_CPS.remove(0);
                }
                //
                /////////////////////////// Event

                // Gamma event
                // if(IsManualID) return;

                Operator_HealthSafetyEventSatus(Get_Gamma_DoseRate_nSV());
                double GammaTh = Get_GammaThreshold();

                if (GammaTh > 0) {
                    if (GammaTh < MS.Get_TotalCount() | IsHealthEvent == true) {
                        //NcLibrary.SaveText1(" GammaTh : "+GammaTh+" MS.Get_TotalCount() : "+ MS.Get_TotalCount()+" IsHealthEvent : "+IsHealthEvent+"\n","test");
                        Operator_GammaEventSatus(true);
                    } else {
                        Operator_GammaEventSatus(false);
                    }

                }
                // Neutron event
                if (Neutron_ThresholdCnt < mNeutron.Get_CPS())
                    Operator_NeutronEventSatus(true);
                else
                    Operator_NeutronEventSatus(false);

                // When event
                Event_Process();

                if (Is_Event() == false) {
                    Gamma_CPS_SigmaThre.add(MS.Get_TotalCount());
                    if (Gamma_CPS_SigmaThre.size() > SIGMA_ACCUMUL_SEC)
                        Gamma_CPS_SigmaThre.remove(0);

                } else {

                }

            }
            // --===--

            // MS.Set_HealthSafety_Threshold(HealthSafety_Threshold);
            // MS.Set_mGammaDoserate(mGammaDoserate);
            // MS.Set_crystalType(mCrystalTypeInt);

            if (MainActivity.mDebug.hw) {
                NcLibrary.SaveText1("cps  " + MS.Get_AvgCPS() + " ,FillCps " + MS.GetFillCps() + " ,GM " + MainActivity.mDetector.GM_Cnt + " ,spectrum " + Arrays.toString(MS.ToDouble()), "spectrum");
            }


            Intent intent = new Intent(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
            intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, MS);
            // intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
            // --===--

            // --===--
            Intent intent2 = new Intent(MainBroadcastReceiver.MSG_RECV_NEUTRON);
            intent2.putExtra(MainBroadcastReceiver.DATA_NEUTRON, mNeutron);
            // intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
            // --===--

            ////////////////////
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        ;
    }

    public void Set_Measured_Data(int[] Spc, double Neutron) {
        MS.Set_Spectrum(Spc);
        mNeutron.Set_CPS(Neutron);
        mNeutron.Reset_Acummul_data();

        if (mMode == ID_MODE) {
            // for FINDER
            if (RealTime_CPS != null) {
                RealTime_CPS.add((double) MS.Get_TotalCount());
                if (RealTime_CPS.size() > PAST_SPC_ARRAY_SIZE)
                    RealTime_CPS.remove(0);
            } else {
                RealTime_CPS = new Vector<Double>();
                for (int i = 0; i < PAST_SPC_ARRAY_SIZE; i++)
                    RealTime_CPS.add((double) -1);

                RealTime_CPS.add((double) MS.Get_TotalCount());
                if (RealTime_CPS.size() > PAST_SPC_ARRAY_SIZE)
                    RealTime_CPS.remove(0);
            }
            //
            /////////////////////////// Event

            // Gamma event
            // if(IsManualID) return;

            Operator_HealthSafetyEventSatus(Get_Gamma_DoseRate_nSV());

            if (Get_GammaThreshold() < MS.Get_TotalCount() | IsHealthEvent == true)
                Operator_GammaEventSatus(true);
            else
                Operator_GammaEventSatus(false);

            // Neutron event
            if (Neutron_ThresholdCnt < mNeutron.Get_CPS())
                Operator_NeutronEventSatus(true);
            else
                Operator_NeutronEventSatus(false);

            // When event
            Event_Process();

            if (Is_Event() == false) {
                Gamma_CPS_SigmaThre.add(MS.Get_TotalCount());
                if (Gamma_CPS_SigmaThre.size() > SIGMA_ACCUMUL_SEC)
                    Gamma_CPS_SigmaThre.remove(0);

            } else {

            }
        }
        // --===--
        Intent intent = new Intent(MainBroadcastReceiver.MSG_RECV_SPECTRUM);
        intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, MS);
        // intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
        LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
        // --===--

        // --===--
        Intent intent2 = new Intent(MainBroadcastReceiver.MSG_RECV_NEUTRON);
        intent2.putExtra(MainBroadcastReceiver.DATA_NEUTRON, mNeutron);
        // intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
        LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
        // --===--
        ////////////////////
    }

    public void Event_Process() {
        // Gamma
        switch (EVENT_STATUS) {
            case EVENT_NONE:
                if (IsGainStb)
                    // MainActivity.mDetector.mGain_elapsedTime =
                    // Detector.GAIN_START_IN_SEC;
                    Gain_Stabilization(MS);
                break;

            case EVENT_BEGIN:
                Log.i(TAG_Event, "Event start");

                mGamma_Event = new EventData();
                mGamma_Event.mInstrument_Name = InstrumentModel_Name;
                mGamma_Event.Doserate_unit = (IsSvUnit) ? DR_UNIT_SV : DR_UNIT_R;
                mGamma_Event.Event_Detector = (IsManualID) ? EventData.EVENT_MANUAL_ID : EventData.EVENT_GAMMA;
                mGamma_Event.MS.Set_Spectrum(MS);
                mGamma_Event.MS.Set_StartSystemTime();
                // mGamma_Event.BG.Set_Spectrum(Real_BG);

                mGamma_Event.BG = Real_BG;
                mGamma_Event.mNeutron.Set_CPS(mNeutron.Get_CPS());
                mGamma_Event.Set_StartTime();
                mGamma_Event.IsManualID = false;
                mGamma_Event.mUser = User;
                mGamma_Event.mLocation = Location;
                if (mGamma_Event.Doserate_MAX < Get_Gamma_DoseRate_nSV())
                    mGamma_Event.Doserate_MAX = Get_Gamma_DoseRate_nSV();
                mGamma_Event.Doserate_AVG += Get_Gamma_DoseRate_nSV();

                // --

                // --===--
                Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
                intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_BEGIN);
                intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
                intent.putExtra(MainBroadcastReceiver.DATA_IS_HEALTH_ALARM, IsHealthEvent);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                // --===--
                //
                break;
            case EVENT_ING:
                Log.i(TAG_Event, "Event ing");

                mGamma_Event.MS.Accumulate_Spectrum(MS);
                mGamma_Event.mNeutron.Set_CPS(mNeutron.Get_CPS());
                if (mGamma_Event.Doserate_MAX < Get_Gamma_DoseRate_nSV())
                    mGamma_Event.Doserate_MAX = Get_Gamma_DoseRate_nSV();
                mGamma_Event.Doserate_AVG += Get_Gamma_DoseRate_nSV();
                // --
/*				PreferenceDB prefDB = new PreferenceDB(mSuper);
				IsotopesLibrary IsoLib = new IsotopesLibrary(mSuper);
				IsoLib.Set_LibraryName(prefDB.Get_Selected_IsoLibName());
				Vector<Isotope> ID_Result = IsoLib .Find_Isotopes_with_Energy(mGamma_Event.MS, mGamma_Event.BG);
				*/
                if (MainActivity.mIsoLib2 == null) {
                    MainActivity.mIsoLib2 = new IsotopesLibrary(mSuper);
                    MainActivity.mIsoLib2.Set_LibraryName(MainActivity.mPrefDB.Get_Selected_IsoLibName());
                }
                Vector<Isotope> ID_Result = MainActivity.mIsoLib2.Find_Isotopes_with_Energy(mGamma_Event.MS, mGamma_Event.BG);
                // Vector<NcPeak> Peaks = FPM.Find_Peak(mGamma_Event.MS, mGamma_Event.BG);


                if (mGamma_Event.MS.Get_AcqTime() <= 5 & ID_Result.size() > 2) {
                    for (int i = 2; i < ID_Result.size(); i++) {
                        ID_Result.remove(i);
                    }
                }
                ///////////////// �젙�웾 遺꾩꽍

                if (ID_Result.isEmpty() == false)
                    ID_Result = NcLibrary.Quantitative_analysis(mGamma_Event.MS, mGamma_Event.BG, ID_Result, IsSvUnit,
                            mPmtSurface, mCrystal,getGECoef());

                if (mGamma_Event.MS.Get_AcqTime() <= 5 & ID_Result.size() > 2) {
                    Isotope iso1 = ID_Result.get(0);
                    Isotope iso2 = ID_Result.get(1);

                    ID_Result.clear();
                    ID_Result.add(iso1);
                    ID_Result.add(iso2);
                }

                mGamma_Event.Detected_Isotope = ID_Result;

                // --===--
                Intent intent3 = new Intent(MainBroadcastReceiver.MSG_EVENT);
                intent3.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_ING);
                intent3.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
                intent3.putExtra(MainBroadcastReceiver.DATA_SOURCE_ID, ID_Result);
                intent3.putExtra(MainBroadcastReceiver.DATA_IS_HEALTH_ALARM, IsHealthEvent);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent3);
                // --===--
                break;

            case EVENT_FINISH:
                Log.i(TAG_Event, "Event finish");

                mGamma_Event.Set_EndEventTime();
                mGamma_Event.MS = mGamma_Event.MS;
                mGamma_Event.Doserate_AVG = mGamma_Event.Doserate_AVG / mGamma_Event.MS.Get_AcqTime();
                mGamma_Event.Neutron_AVG = mGamma_Event.mNeutron.Get_AvgCps();
                mGamma_Event.Neutron_MAX = mGamma_Event.mNeutron.Get_MaxCount();
                // --===--
                Intent intent2 = new Intent(MainBroadcastReceiver.MSG_EVENT);
                intent2.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_FINISH);
                intent2.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
                intent2.putExtra(MainBroadcastReceiver.DATA_IS_HEALTH_ALARM, IsHealthEvent);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
                // --===--
                Init_GainStabilization();
                mGamma_Event = null;
                break;

            default:
                break;
        }

        // Neutron
        switch (EVENT_STATUS_N) {
            case EVENT_BEGIN:
                Log.i(TAG_Event, "Neutron Event start");

                mNeturonEvent = new EventData();
                mNeturonEvent.Event_Detector = EventData.EVENT_NEUTRON;
                mNeturonEvent.mInstrument_Name = InstrumentModel_Name;
                mNeturonEvent.Doserate_unit = (IsSvUnit) ? DR_UNIT_SV : DR_UNIT_R;
                mNeturonEvent.MS.Set_Spectrum(MS);
                mNeturonEvent.MS.Set_StartSystemTime();
                mNeturonEvent.BG = Real_BG;
                mNeturonEvent.mNeutron.Set_CPS(mNeutron.Get_CPS());
                mNeturonEvent.Set_StartTime();
                mNeturonEvent.mUser = User;
                mNeturonEvent.mLocation = Location;
                mNeturonEvent.IsManualID = false;
                if (mNeturonEvent.Doserate_MAX < Get_Gamma_DoseRate_nSV())
                    mNeturonEvent.Doserate_MAX = Get_Gamma_DoseRate_nSV();
                mNeturonEvent.Doserate_AVG += Get_Gamma_DoseRate_nSV();

                // --===--
                Intent intent = new Intent(MainBroadcastReceiver.MSG_EVENT);
                intent.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_BEGIN);
                intent.putExtra(MainBroadcastReceiver.DATA_EVENT, mNeturonEvent);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                // --===--
                break;
            case EVENT_ING:
                Log.i(TAG_Event, "Neutron Event ing");

                mNeturonEvent.MS.Accumulate_Spectrum(MS);
                mNeturonEvent.mNeutron.Set_CPS(mNeutron.Get_CPS());
                if (mNeturonEvent.Doserate_MAX < Get_Gamma_DoseRate_nSV())
                    mNeturonEvent.Doserate_MAX = Get_Gamma_DoseRate_nSV();
                mNeturonEvent.Doserate_AVG += Get_Gamma_DoseRate_nSV();
                // --
/*				PreferenceDB prefDB = new PreferenceDB(mSuper);
				IsotopesLibrary IsoLib = new IsotopesLibrary(mSuper);
				IsoLib.Set_LibraryName(prefDB.Get_Selected_IsoLibName());
				Vector<Isotope> ID_Result = IsoLib.Find_Isotopes_with_Energy(mNeturonEvent.MS, mNeturonEvent.BG);*/

                // Vector<NcPeak> Peaks = FPM.Find_Peak(mGamma_Event.MS, mGamma_Event.BG);

                if (MainActivity.mIsoLib2 == null) {
                    MainActivity.mIsoLib2 = new IsotopesLibrary(mSuper);
                    MainActivity.mIsoLib2.Set_LibraryName(MainActivity.mPrefDB.Get_Selected_IsoLibName());
                }
                Vector<Isotope> ID_Result = MainActivity.mIsoLib2.Find_Isotopes_with_Energy(mNeturonEvent.MS, mNeturonEvent.BG);

                if (mNeturonEvent.MS.Get_AcqTime() <= 5 & ID_Result.size() > 2) {
                    Isotope iso1 = ID_Result.get(0);
                    Isotope iso2 = ID_Result.get(1);

                    ID_Result.clear();
                    ID_Result.add(iso1);
                    ID_Result.add(iso2);
                }

                ///////////////// �젙�웾 遺꾩꽍

                if (ID_Result.isEmpty() == false)
                    ID_Result = NcLibrary.Quantitative_analysis(mNeturonEvent.MS, mNeturonEvent.BG, ID_Result, IsSvUnit,
                            mPmtSurface, mCrystal,getGECoef());

                mNeturonEvent.Detected_Isotope = ID_Result;

                // --===--
                Isotope[] result = new Isotope[ID_Result.size()];
                for (int i = 0; i < ID_Result.size(); i++) {
                    result[i] = ID_Result.get(i);
                }
                Intent intent3 = new Intent(MainBroadcastReceiver.MSG_EVENT);
                intent3.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_ING);
                intent3.putExtra(MainBroadcastReceiver.DATA_EVENT, mNeturonEvent);
                intent3.putExtra(MainBroadcastReceiver.DATA_SOURCE_ID, ID_Result);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent3);
                // --===--
                break;
            case EVENT_FINISH:
                Log.i(TAG_Event, "Neutron Event finish");

                mNeturonEvent.Set_EndEventTime();
                mNeturonEvent.MS = mNeturonEvent.MS;
                mNeturonEvent.Doserate_AVG = mNeturonEvent.Doserate_AVG / mNeturonEvent.MS.Get_AcqTime();
                mNeturonEvent.Neutron_AVG = mNeturonEvent.mNeutron.Get_AvgCps();
                mNeturonEvent.Neutron_MAX = mNeturonEvent.mNeutron.Get_MaxCount();
                // --===--
                Intent intent2 = new Intent(MainBroadcastReceiver.MSG_EVENT);
                intent2.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_FINISH);
                intent2.putExtra(MainBroadcastReceiver.DATA_EVENT, mNeturonEvent);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
                // --===--
                Init_GainStabilization();
                mNeturonEvent = null;
                break;
            default:
                break;
        }
    }

    public void Cancel_Event() {
        EVENT_STATUS = EVENT_NONE;
        mGamma_Event = null;
        Init_GainStabilization();
    }

    public void Finish_GammaEvent() {
        if (EVENT_STATUS != EVENT_NONE & mGamma_Event != null) {
            EVENT_STATUS = EVENT_FINISH;

            mGamma_Event.Set_EndEventTime();
            mGamma_Event.MS = mGamma_Event.MS;
            mGamma_Event.Doserate_AVG = mGamma_Event.Doserate_AVG / mGamma_Event.MS.Get_AcqTime();
            mGamma_Event.Neutron_AVG = mGamma_Event.mNeutron.Get_AvgCps();
            mGamma_Event.Neutron_MAX = mGamma_Event.mNeutron.Get_MaxCount();
            // --===--
            Intent intent2 = new Intent(MainBroadcastReceiver.MSG_EVENT);
            intent2.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_FINISH);
            intent2.putExtra(MainBroadcastReceiver.DATA_EVENT, mGamma_Event);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
            // --===--
            Init_GainStabilization();
            mGamma_Event = null;
        }
    }

    public void Finish_NeutronEvent() {
        if (EVENT_STATUS_N != EVENT_NONE & mNeturonEvent != null) {
            EVENT_STATUS_N = EVENT_FINISH;

            mNeturonEvent.Set_EndEventTime();
            mNeturonEvent.MS = mNeturonEvent.MS;
            mNeturonEvent.Doserate_AVG = mNeturonEvent.Doserate_AVG / mNeturonEvent.MS.Get_AcqTime();
            mNeturonEvent.Neutron_AVG = mNeturonEvent.mNeutron.Get_AvgCps();
            mNeturonEvent.Neutron_MAX = mNeturonEvent.mNeutron.Get_MaxCount();
            // --===--
            Intent intent2 = new Intent(MainBroadcastReceiver.MSG_EVENT);
            intent2.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_FINISH);
            intent2.putExtra(MainBroadcastReceiver.DATA_EVENT, mNeturonEvent);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent2);
            // --===--
            Init_GainStabilization();
            mNeturonEvent = null;
        }

    }

    public Vector<Double> Get_RealActivityArray() {
        Vector<Double> Result = new Vector<Double>();

        if (IsSvUnit) {
            for (int i = 0; i < RealTime_CPS.size(); i++) {

                Result.add((RealTime_CPS.get(i) == -1) ? Double.valueOf(-1) : RealTime_CPS.get(i));// *0.001);
            }
            return Result;
        } else {
            for (int i = 0; i < RealTime_CPS.size(); i++) {

                Result.add((RealTime_CPS.get(i) == -1) ? -1 : RealTime_CPS.get(i) * 0.1);
            }
            return Result;
        }
    }

    public double Get_Gamma_DoseRate_nSV() {
        double DoseRate = 0;

       // DoseRate = NcLibrary.DoseRateCalculate(MS.ToInteger(), Coeffcients, mPmtSurface, mCrystal);

        DoseRate = NcLibrary.DoseRateCalculate_GE(MS.ToDouble(),MS.Get_AcqTime(), Real_BG.ToDouble(), Real_BG.Get_AcqTime(), Coeffcients, mPmtSurface, mCrystal, getGECoef());

        if (DoseRate > SPC_DOSERATE_LIMIT) {
            DoseRate = NcLibrary.GM_to_uSV(GM_Cnt);
        }
        return DoseRate;
    }

    public double Get_GM_DoseRate() {
        double result = 0;
        result = NcLibrary.GM_to_uSV(GM_Cnt);

        return result;
    }

    public double Get_GammaThreshold() {
        if (IsSigmaThreshold) {
            return Calibration_SigmaThreshold();
        } else
            return Gamma_Threshold;
    }

    public boolean Is_HealthEvent() {
        return IsHealthEvent;
    }

    public int Get_GammaCPS() {
        int CPS = MS.Get_TotalCount();

       // double DoseRate = NcLibrary.DoseRateCalculate(MS.ToInteger(), Coeffcients, mPmtSurface, mCrystal);
        double DoseRate = NcLibrary.DoseRateCalculate_GE(MS.ToDouble(), MS.Get_AcqTime(),Real_BG.ToDouble(), Real_BG.Get_AcqTime(), Coeffcients, mPmtSurface, mCrystal,getGECoef());

        if (DoseRate > SPC_DOSERATE_LIMIT) {
            CPS = GM_Cnt;
        }

        //	return CPS;
        if (realTime <= 0) {
            realTime = 1;
        }
        return (int) CPS / realTime;
    }


    public void Init_Measure_Data() {
        IsHealthEvent = false;
        if (RealTime_CPS != null)
            RealTime_CPS.clear();
        RealTime_CPS = null;
        Gamma_CPS_SigmaThre.clear();
        MS.ClearSPC();
        GM_Cnt = 0;
        mNeutron.Init();

        EVENT_STATUS = EVENT_NONE;
        EVENT_STATUS_N = EVENT_NONE;

        Init_GainStabilization();
    }

    public boolean Is_Event() {
        if (EVENT_STATUS == EVENT_BEGIN | EVENT_STATUS == EVENT_ING | EVENT_STATUS_N == EVENT_BEGIN
                | EVENT_STATUS_N == EVENT_ING)
            return true;
        else
            return false;
    }

    private double Calibration_SigmaThreshold() {
        double nSv_avg = 0;

        for (int i = 0; i < Gamma_CPS_SigmaThre.size(); i++) {
            nSv_avg += Gamma_CPS_SigmaThre.get(i);
        }

        nSv_avg = nSv_avg / Gamma_CPS_SigmaThre.size();
        nSv_avg = nSv_avg + (Gamma_SigmaThreshold * Math.sqrt(nSv_avg));

        return nSv_avg;
    }

    public void Set_EnergyFittingArgument(double[] Arg) {
        if (Arg == null)
            return;
        Coeffcients = new double[Arg.length];

        for (int i = 0; i < Arg.length; i++) {
            Coeffcients[i] = Arg[i];
        }

        MS.Set_Coefficients(Arg);
        Real_BG.Set_Coefficients(Arg);
    }

    public double[] Get_Coeff() {
        return MS.Get_Coefficients().get_Coefficients();
    }

    public boolean WasCalibrated() {
        if (Coeffcients[0] == 0 | Coeffcients[1] == 0)
            return false;
        else
            return true;
    }

    private void Gain_Stabilization_Old(Spectrum spc) {

        if (mGain_elapsedTime == 10) {
            Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
            intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_START);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
        }
        mGain_elapsedTime += 1;
        if (D)
            Log.i(TAG_GAIN, "Acq Time - " + mGain_elapsedTime + " sec / " + GAIN_START_IN_SEC + " sec");
        if (mGain_elapsedTime <= GAIN_START_IN_SEC)
            return;

        mMS_Gainstabilization.Accumulate_Spectrum(spc);

        int[] a = spc.ToInteger();
/*		NcLibrary.Write_ExceptionLog2("\nspc = ", a);
		NcLibrary.Write_ExceptionLog1( "\nAcq Time = " + mGain_elapsedTime + " sec / " + GAIN_START_IN_SEC + " sec");*/

        if (mMS_Gainstabilization.Get_TotalCount() > GAIN_THRESHOLD_CNT) {

            PreferenceDB prefDB = new PreferenceDB(mSuper);
            double[] mPeck = new double[2];
            mPeck[0] = prefDB.Get_CaliPeak1_From_pref();
            mPeck[1] = prefDB.Get_CaliPeak2_From_pref();
            double Be_K40_Ch = prefDB.Get_CaliPeak3_From_pref();

            double Be_A = prefDB.Get_Cali_A_From_pref();
            double Be_B = prefDB.Get_Cali_B_From_pref();
            double Be_C = prefDB.Get_Cali_C_From_pref();
            double[] mABC = {Be_A, Be_B, Be_C};

            //NcLibrary.Write_ExceptionLog1( "\na =  " + Be_A +" b"+Be_B+"c"+Be_C);

            // double Avg_time = mMS_Gainstabilization.Get_TotalCount()/
            // mGain_Acqtime ;

            if (Be_A == 0)
                return;

            int mROI_Ch_start = (int) NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.7, Be_A, Be_B, Be_C));
            int mROI_Ch_end = (int) NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.3, Be_A, Be_B, Be_C));
            int mROI_Ch = (int) NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, Be_A, Be_B, Be_C));

			/*NcLibrary.Write_ExceptionLog1( "\nmROI_Ch_start = " + mROI_Ch_start);
			NcLibrary.Write_ExceptionLog1( "\nmROI_Ch_end = " + mROI_Ch_end);
			NcLibrary.Write_ExceptionLog1( "\nmROI_Ch = " + mROI_Ch);*/

            double[] mMStemp = new double[1024];

            /*
             * double[] temp1 = new double[1024];
             *
             * temp1 = NcLibrary.Smooth(mMS_Gainstabilization.ToInteger(), 1024, 10, 2);
             *
             * int mStbROICnt1 = (int) NcLibrary.ROIAnalysis_GetTotCnt(temp1, mROI_Ch_start,
             * mROI_Ch_end); if (mStbROICnt1 > GAIN_THRESHOLD_CNT) { GainSpeCount++;
             *
             * String BodyText = SpectrumToString(mMS_Gainstabilization.ToDouble());
             *
             * onTextWriting("Original : "+Integer.toString(GainSpeCount), BodyText);
             *
             * BodyText = SpectrumToString(temp1);
             * onTextWriting("Smoothing : "+Integer.toString(GainSpeCount), BodyText);
             *
             * }
             */


            mMStemp = NcLibrary.Smooth(mMS_Gainstabilization.ToInteger(), 1024, 10, 2);

            int mMSROICnt = (int) NcLibrary.ROIAnalysis_GetTotCnt(mMStemp, mROI_Ch_start, mROI_Ch_end);

            if (D)
                Log.i(TAG_GAIN, "K40 Cnt - " + mMSROICnt + " Cnt  /  " + GAIN_THRESHOLD_CNT + " Cnt");

            //NcLibrary.Write_ExceptionLog1( "\nK40 Cnt =  " + mMSROICnt + " Cnt  /  " + GAIN_THRESHOLD_CNT + " Cnt");

            int K40Channel = (int) NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, Be_A, Be_B, Be_C);

            int K40Channel_En = (int) NcLibrary.Energy_to_Channel(K40Channel, mABC);

            if (mMSROICnt > GAIN_THRESHOLD_CNT) {

                double[] mBGtemp = new double[1024];
                mBGtemp = NcLibrary.Smooth(Real_BG.ToInteger(), 1024, 10, 2);
                int mBGROICnt = (int) NcLibrary.ROIAnalysis_GetTotCnt(mBGtemp, mROI_Ch_start, mROI_Ch_end);

                FindPeaksM FPM = new FindPeaksM();
                int[] PeaksCount = FPM.Find_Gain_Count_Peak(mMS_Gainstabilization, Real_BG);

                int[] FindPeaks = FPM.Find_Gain_Peak_List(mMS_Gainstabilization, Real_BG);

                //	NcLibrary.Write_ExceptionLog2( "\nFindPeaks = ",FindPeaks);
                // int PeakCount = 0;
                /*
                 * int[] EnPeaks = new int[PeaksCount.length]; for (int i = 0; i <
                 * PeaksCount.length; i++) {
                 *
                 * EnPeaks[i] = (int) NcLibrary.Channel_to_Energy((double) PeaksCount[i], mABC);
                 *
                 * }
                 */
                /*
                 * for (int i = 0; i < PeaksCount.length; i++) {
                 *
                 * int En = (int) NcLibrary.Channel_to_Energy((double) PeaksCount[i], mABC);
                 *
                 * if (1600 > En && 900 < En) {
                 *
                 * PeakCount++; } }
                 */

                // mROI_Ch

                // int K40_Ch = NcLibrary.ROIAnalysis(mMStemp, mROI_Ch_start,
                // mROI_Ch_end);

                // int K40_Ch = NcLibrary.FindNearbyPeak(FindPeaks, mROI_Ch,
                // mROI_Ch_start, mROI_Ch_end);

                int K40_Ch = NcLibrary.ROIAnalysis(mMStemp, (int) (mROI_Ch * 0.9), (int) (mROI_Ch * 1.1));

                if (K40_Ch == 0) {
                    K40_Ch = NcLibrary.ROIAnalysis(mMStemp, (int) (mROI_Ch * 0.8), (int) (mROI_Ch * 1.2));

                }
                if (K40_Ch == 0) {
                    K40_Ch = NcLibrary.ROIAnalysis(mMStemp, (int) (mROI_Ch * 0.7), (int) (mROI_Ch * 1.3));

                }
                if (D)
                    Log.i(TAG_GAIN, "Old K40 - " + Be_K40_Ch + " ch, Found K40 - " + K40_Ch + " ch");

                //	NcLibrary.Write_ExceptionLog1( "\nOld K40 =  " + Be_K40_Ch + " ch, Found K40 - " + K40_Ch + " ch");

                double mMS_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(mMS_Gainstabilization,
                        (int) mMS_Gainstabilization.Get_AcqTime()) / 1000;
                double mDB_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(DB_BG, (int) DB_BG.Get_AcqTime()) / 1000;

                if (K40_Ch != 0 && mMS_Gainstabilization.Get_AvgCPS() < DB_BG.Get_AvgCPS() * 1.5
                        && mMS_Doserate_Avg < mDB_Doserate_Avg * 1.4 && mMSROICnt
                        / mMS_Gainstabilization.Get_AcqTime() < (mBGROICnt / Real_BG.Get_AcqTime()) * 1.5) {

                    if (mDebug.IsDebugMode) {

                        if (mDebug.IsGainStblizationSaveMode) {
                            mSaveCount++;

                            // double[] temp = new double[1024];
                            // onTextWritingFindK40("Nomal", K40_Ch, mMS_Gainstabilization, StrArraylist,
                            // mSaveCount);
                            // onTextWritingFindK40Spectrum("OriginalSpectrum", mSaveCount,
                            // SpectrumToString(mMS_Gainstabilization.ToDouble()));
                            // onTextWritingFindK40Spectrum("SmoothSpectrum", mSaveCount,
                            // SpectrumToString(NcLibrary.Smooth(mMS_Gainstabilization.ToInteger(), 1024,
                            // 10, 2)));
                        }
                    }
                    Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
                    intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_END);
                    intent.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
                    LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                    Init_GainStabilization();

                } else {
                    if (mDebug.IsDebugMode) {

                        if (mDebug.IsGainStblizationSaveMode) {
                            // mSaveCount++;
                            //
                            // onTextWritingFindK40("False", K40_Ch, mMS_Gainstabilization, StrArraylist,
                            // mSaveCount);
                            // onTextWritingFindK40Spectrum("FalseOriginalSpectrum", mSaveCount,
                            // SpectrumToString(mMS_Gainstabilization.ToDouble()));
                            // onTextWritingFindK40Spectrum("FalseSmoothSpectrum", mSaveCount,
                            // SpectrumToString(NcLibrary.Smooth(mMS_Gainstabilization.ToInteger(), 1024,
                            // 10, 2)));
                        }
                    }
                    Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
                    intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_CANCEL);
                    LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                    Init_GainStabilization();
                }

            }
        }
    }

    //K40 Finder Algorithm
    //Hung: 2018/02/01
	/*
	private void Gain_Stabilization(Spectrum spc)
	{

		//Waiting until 10 sec, if tab is no change, then do "Stabilization in progress..."
		if (mGain_elapsedTime == 10)
		{
			Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
			intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_START);
			LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
		}
		mGain_elapsedTime += 1;
		if (D)
			Log.i(TAG_GAIN, "Acq Time - " + mGain_elapsedTime + " sec / " + GAIN_START_IN_SEC + " sec");
		if (mGain_elapsedTime <= GAIN_START_IN_SEC)
			return;

		mMS_Gainstabilization.Accumulate_Spectrum(spc); //receive spectrum

		mMS_Gainstabilization.Set_Coefficients(spc.Get_Coefficients()); //receive energy calibration

		mMS_Gainstabilization.setFWHM(spc.getFWHM());//receive energy FWHM

		//save txt
		//NcLibrary.SaveText("\n Gain_Stabilization mMS_Gainstabilization.Get_Coefficients()"+mMS_Gainstabilization.Get_Coefficients().ToString());
		//NcLibrary.SaveText("\n Gain_Stabilization mMS_Gainstabilization.getFWHM()"+mMS_Gainstabilization.getFWHM().toString());

		if (mMS_Gainstabilization.Get_TotalCount() > NcLibrary.GAIN_THRESHOLD_CNT)
		{

			PreferenceDB prefDB = new PreferenceDB(mSuper);
			double Be_K40_Ch = prefDB.Get_CaliPeak3_From_pref();

			//Finding Peak
			int mMSROICnt=(int) NcLibrary.CalcROIK40(mMS_Gainstabilization.ToInteger(), mMS_Gainstabilization.getFWHM(), mMS_Gainstabilization.Get_Coefficients().get_Coefficients());


			if (D)
			{
				Log.i(TAG_GAIN, "K40 Cnt - " + mMSROICnt + " Cnt  /  " + NcLibrary.GAIN_THRESHOLD_CNT + " Cnt");
			}



			if (mMSROICnt > NcLibrary.GAIN_THRESHOLD_CNT)
			{

				int mBGROICnt = (int) NcLibrary.CalcROIK40(Real_BG.ToInteger(),Real_BG.getFWHM(),Real_BG.Get_Coefficients().get_Coefficients());

				int [] aaa= Real_BG.ToInteger();
				String bg ="";
				for(int j =0; j<aaa.length; j++)
				{
					bg = bg+aaa[j]+ " ";
				}
				//NcLibrary.SaveText("\n GAIN_STABILIZATION Real_BG :   "+bg);
				//save txt
				//NcLibrary.SaveText("\n Gain_Stabilization Real_BG.Get_Coefficients()"+Real_BG.Get_Coefficients().ToString());
				//NcLibrary.SaveText("\n Gain_Stabilization Real_BG.getFWHM()"+Real_BG.getFWHM().toString());

				int K40_Ch=NcLibrary.PeakAna(mMS_Gainstabilization.ToInteger(), mMS_Gainstabilization.getFWHM(), mMS_Gainstabilization.Get_Coefficients().get_Coefficients());

				double[] FWHM_gen = mMS_Gainstabilization.getFWHM();
				double[] FWHM_gen1 = Real_BG.getFWHM();

				if (D)
				{
					Log.i(TAG_GAIN, "Old K40 - " + Be_K40_Ch + " ch, Found K40 - " + K40_Ch + " ch");
				}


				double mMS_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(mMS_Gainstabilization,
						mMS_Gainstabilization.Get_AcqTime()) / 1000;
				double mDB_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(DB_BG, DB_BG.Get_AcqTime()) / 1000;

				if (K40_Ch != 0 && mMS_Gainstabilization.Get_AvgCPS() < DB_BG.Get_AvgCPS() * 1.5
						&& mMS_Doserate_Avg < mDB_Doserate_Avg * 1.4 && mMSROICnt
								/ mMS_Gainstabilization.Get_AcqTime() < (mBGROICnt / Real_BG.Get_AcqTime()) * 1.5) {

					if (mDebug.IsDebugMode) {

						if (mDebug.IsGainStblizationSaveMode) {
							mSaveCount++;

						}
					}




					Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
					intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_END);
					intent.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
					LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
					Init_GainStabilization();

				} else {
					if (mDebug.IsDebugMode) {

						if (mDebug.IsGainStblizationSaveMode) {

						}
					}
					Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
					intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_CANCEL);
					LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
					Init_GainStabilization();
				}

			}
		}
	}
*/

/*
	private void Gain_Stabilization(Spectrum spc)
	{

		// YKIM, 2018.2.19
		// to display "Stabilization in progress.." on top of main view
		if (mGain_elapsedTime == mGain_restTime)
		{
			Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
			intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_START);
			LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
		}
		mGain_elapsedTime += 1;
		if (D)
			Log.i(TAG_GAIN, "Acq Time - " + mGain_elapsedTime + " sec / " + GAIN_START_IN_SEC + " sec");

		//YKIM, 2018.2.19
		// rest time
		if (mGain_elapsedTime <= mGain_restTime)
			return;

		mMS_Gainstabilization.Accumulate_Spectrum(spc); //receive spectrum

		mMS_Gainstabilization.Set_Coefficients(spc.Get_Coefficients()); //receive energy calibration

		mMS_Gainstabilization.setFWHM(spc.getFWHM());//receive energy FWHM

		//save txt
		//NcLibrary.SaveText("\n Gain_Stabilization mMS_Gainstabilization.Get_Coefficients()"+mMS_Gainstabilization.Get_Coefficients().ToString());
		//NcLibrary.SaveText("\n Gain_Stabilization mMS_Gainstabilization.getFWHM()"+mMS_Gainstabilization.getFWHM().toString());
		PreferenceDB prefDB = new PreferenceDB(mSuper);
		double Be_A = prefDB.Get_Cali_A_From_pref();
		double Be_B = prefDB.Get_Cali_B_From_pref();
		double Be_C = prefDB.Get_Cali_C_From_pref();
		double[] mABC = { Be_A, Be_B, Be_C };

		//NcLibrary.Write_ExceptionLog1( "\na =  " + Be_A +" b"+Be_B+"c"+Be_C);

		// double Avg_time = mMS_Gainstabilization.Get_TotalCount()/
		// mGain_Acqtime ;

		if (Be_A == 0)
			return;

		int mROI_Ch_start = (int) NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 0.7, Be_A, Be_B, Be_C));
		int mROI_Ch_end = (int) NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK * 1.3, Be_A, Be_B, Be_C));
		int mROI_Ch = (int) NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, Be_A, Be_B, Be_C));


		double[] mMStemp = new double[1024];


		 * double[] temp1 = new double[1024];
		 *
		 * temp1 = NcLibrary.Smooth(mMS_Gainstabilization.ToInteger(), 1024, 10, 2);
		 *
		 * int mStbROICnt1 = (int) NcLibrary.ROIAnalysis_GetTotCnt(temp1, mROI_Ch_start,
		 * mROI_Ch_end); if (mStbROICnt1 > GAIN_THRESHOLD_CNT) { GainSpeCount++;
		 *
		 * String BodyText = SpectrumToString(mMS_Gainstabilization.ToDouble());
		 *
		 * onTextWriting("Original : "+Integer.toString(GainSpeCount), BodyText);
		 *
		 * BodyText = SpectrumToString(temp1);
		 * onTextWriting("Smoothing : "+Integer.toString(GainSpeCount), BodyText);
		 *
		 * }



		mMStemp = NcLibrary.Smooth(mMS_Gainstabilization.ToInteger(), 1024, 10, 2);

		int mMSROICnt = (int) NcLibrary.ROIAnalysis_GetTotCnt(mMStemp, mROI_Ch_start, mROI_Ch_end);

		if (D)
			Log.i(TAG_GAIN, "K40 Cnt - " + mMSROICnt + " Cnt  /  " + GAIN_THRESHOLD_CNT + " Cnt");

		//NcLibrary.Write_ExceptionLog1( "\nK40 Cnt =  " + mMSROICnt + " Cnt  /  " + GAIN_THRESHOLD_CNT + " Cnt");

		int K40Channel = (int) NcLibrary.Energy_to_Channel(NcLibrary.K40_PEAK, Be_A, Be_B, Be_C);

		int K40Channel_En = (int) NcLibrary.Energy_to_Channel(K40Channel, mABC);

		if (mMS_Gainstabilization.Get_TotalCount() > NcLibrary.GAIN_THRESHOLD_CNT)
		{

			//PreferenceDB prefDB = new PreferenceDB(mSuper);
			double Be_K40_Ch = prefDB.Get_CaliPeak3_From_pref();

			//Finding Peak
			//int mMSROICnt=(int) NcLibrary.CalcROIK40(mMS_Gainstabilization.ToInteger(), mMS_Gainstabilization.getFWHM(), mMS_Gainstabilization.Get_Coefficients().get_Coefficients());


			if (D)
			{
				Log.i(TAG_GAIN, "K40 Cnt - " + mMSROICnt + " Cnt  /  " + NcLibrary.GAIN_THRESHOLD_CNT + " Cnt");
			}



			if (mMSROICnt > NcLibrary.GAIN_THRESHOLD_CNT)
			{

				//int mBGROICnt = (int) NcLibrary.CalcROIK40(Real_BG.ToInteger(),Real_BG.getFWHM(),Real_BG.Get_Coefficients().get_Coefficients());
				double[] mBGtemp = new double[1024];
				mBGtemp = NcLibrary.Smooth(Real_BG.ToInteger(), 1024, 10, 2);
				int mBGROICnt = (int) NcLibrary.ROIAnalysis_GetTotCnt(mBGtemp, mROI_Ch_start, mROI_Ch_end);

				int K40_Ch = NcLibrary.ROIAnalysis(mMStemp, (int) (mROI_Ch * 0.9), (int) (mROI_Ch * 1.1));

				if (K40_Ch == 0) {
					K40_Ch = NcLibrary.ROIAnalysis(mMStemp, (int) (mROI_Ch * 0.8), (int) (mROI_Ch * 1.2));

				}
				if (K40_Ch == 0) {
					K40_Ch = NcLibrary.ROIAnalysis(mMStemp, (int) (mROI_Ch * 0.7), (int) (mROI_Ch * 1.3));
				}

				double[] FWHM_gen = mMS_Gainstabilization.getFWHM();
				double[] FWHM_gen1 = Real_BG.getFWHM();
				if (D)
				{
					Log.i(TAG_GAIN, "Old K40 - " + Be_K40_Ch + " ch, Found K40 - " + K40_Ch + " ch");
				}


				double mMS_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(mMS_Gainstabilization,
						mMS_Gainstabilization.Get_AcqTime()) / 1000;
				double mDB_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(DB_BG, DB_BG.Get_AcqTime()) / 1000;

				if (K40_Ch != 0 && mMS_Gainstabilization.Get_AvgCPS() < DB_BG.Get_AvgCPS() * 1.5
						&& mMS_Doserate_Avg < mDB_Doserate_Avg * 1.4 && mMSROICnt
								/ mMS_Gainstabilization.Get_AcqTime() < (mBGROICnt / Real_BG.Get_AcqTime()) * 1.5) {

					if (mDebug.IsDebugMode) {

						if (mDebug.IsGainStblizationSaveMode) {
							mSaveCount++;

						}
					}




					Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
					intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_END);
					intent.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
					LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
					Init_GainStabilization();

				} else {
					if (mDebug.IsDebugMode) {

						if (mDebug.IsGainStblizationSaveMode) {

						}
					}
					Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
					intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_CANCEL);
					LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
					Init_GainStabilization();
				}

			}
		}
	}
	*/

    private void Gain_Stabilization(Spectrum spc) {
        // YKIM, 2018.2.19
        // to display "Stabilization in progress.." on top of main view
        //NcLibrary.SaveText1("Gain_Stabilization\n","test");
        if (mGain_elapsedTime == mGain_restTime) {
            Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
            intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_START);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
        }
        //NcLibrary.SaveText1("Acq Time - "+ mGain_elapsedTime + " sec /  "+ GAIN_START_IN_SEC + "sec\n","test");
        mGain_elapsedTime += 1;
        if (D)
            Log.i(TAG_GAIN, "Acq Time - " + mGain_elapsedTime + " sec / " + GAIN_START_IN_SEC + " sec");

        //YKIM, 2018.2.19
        // rest time
        if (mGain_elapsedTime <= mGain_restTime)
            return;

        mMS_Gainstabilization.Accumulate_Spectrum(spc); //receive spectrum
        mMS_Gainstabilization.Set_Coefficients(spc.Get_Coefficients()); //receive energy calibration
        mMS_Gainstabilization.setFWHM(spc.getFWHM());//receive energy FWHM

        //save txt
        //NcLibrary.SaveText("\n Gain_Stabilization mMS_Gainstabilization.Get_Coefficients()"+mMS_Gainstabilization.Get_Coefficients().ToString());
        //NcLibrary.SaveText("\n Gain_Stabilization mMS_Gainstabilization.getFWHM()"+mMS_Gainstabilization.getFWHM().toString());


        if (mMS_Gainstabilization.Get_TotalCount() > NcLibrary.GAIN_THRESHOLD_CNT) {

            PreferenceDB prefDB = new PreferenceDB(mSuper);
            double Be_K40_Ch = prefDB.Get_CaliPeak3_From_pref();

            //Finding Peak
            int mMSROICnt = (int) NcLibrary.CalcROIK40(mMS_Gainstabilization.ToInteger(), mMS_Gainstabilization.getFWHM(), mMS_Gainstabilization.Get_Coefficients().get_Coefficients());


            if (D) {
                Log.i(TAG_GAIN, "K40 Cnt - " + mMSROICnt + " Cnt  /  " + NcLibrary.GAIN_THRESHOLD_CNT + " Cnt");
            }


            if (mMSROICnt > NcLibrary.GAIN_THRESHOLD_CNT) {

                int mBGROICnt = (int) NcLibrary.CalcROIK40(Real_BG.ToInteger(), Real_BG.getFWHM(), Real_BG.Get_Coefficients().get_Coefficients());


                int K40_Ch = NcLibrary.PeakAna(mMS_Gainstabilization.ToInteger(), mMS_Gainstabilization.getFWHM(), mMS_Gainstabilization.Get_Coefficients().get_Coefficients());

                double[] FWHM_gen = mMS_Gainstabilization.getFWHM();
                double[] FWHM_gen1 = Real_BG.getFWHM();

                if (D) {
                    Log.i(TAG_GAIN, "Old K40 - " + Be_K40_Ch + " ch, Found K40 - " + K40_Ch + " ch");
                }


                double mMS_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(mMS_Gainstabilization,
                        (int) mMS_Gainstabilization.Get_AcqTime()) / 1000;
                double mDB_Doserate_Avg = Get_Gamma_DoseRate_Spectrum_AVG_nSV(DB_BG, (int) DB_BG.Get_AcqTime()) / 1000;

                if (DB_BG.Get_AvgCPS() > 0 && mBGROICnt > 0) {
                    // existing background spectrum
                    if (K40_Ch > 0 && mMS_Gainstabilization.Get_AvgCPS() <= DB_BG.Get_AvgCPS() * 1.5
                            && mMS_Doserate_Avg <= mDB_Doserate_Avg * 1.4
                            && (double) mMSROICnt / (double) mMS_Gainstabilization.Get_AcqTime() <= ((double) mBGROICnt / (double) Real_BG.Get_AcqTime()) * 1.5) {

                        if (mDebug.IsDebugMode) {

                            if (mDebug.IsGainStblizationSaveMode) {
                                mSaveCount++;

                            }
                        }
                        // 파일안에 문자열 쓰기
                        //
                        //String str=("True K40ch,"+K40_Ch+", avgcps,"+ mMS_Gainstabilization.Get_AvgCPS()+",BGavgcps,"+DB_BG.Get_AvgCPS() + ",mMS_Doserate_Avg,"+mMS_Doserate_Avg+","
                        //		+"mDB_Doserate_Avg,"+mDB_Doserate_Avg+",mMSROICnt,"+mMSROICnt+",MSTime,"+mMS_Gainstabilization.Get_AcqTime()+",mBGROICnt,"+mBGROICnt+",BGtime,"+ Real_BG.Get_AcqTime()+"\n");
                        //		NcLibrary.SaveText(str);
                        //////////////////////////////////////////
                        Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
                        intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_END);
                        intent.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
                        LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                        Init_GainStabilization();

                    } else {
                        if (mDebug.IsDebugMode) {

                            if (mDebug.IsGainStblizationSaveMode) {

                            }
                        }
                        mGain_restTime = 10;
                        /////////////////////////////////////////////
                        // 파일안에 문자열 쓰기

                        //String str=("Fail K40ch,"+K40_Ch+", avgcps,"+ mMS_Gainstabilization.Get_AvgCPS()+",BGavgcps,"+DB_BG.Get_AvgCPS() + ",mMS_Doserate_Avg,"+mMS_Doserate_Avg+","
                        //		+"mDB_Doserate_Avg,"+mDB_Doserate_Avg+",mMSROICnt,"+mMSROICnt+",MSTime,"+mMS_Gainstabilization.Get_AcqTime()+",mBGROICnt,"+mBGROICnt+",BGtime,"+ Real_BG.Get_AcqTime()+"\n");
                        //		NcLibrary.SaveText(str);
                        //////////////////////////////////////////
                        Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
                        intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_CANCEL);
                        LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                        Init_GainStabilization();
                    }
                } else {    // if No BG
                    Intent intent = new Intent(MainBroadcastReceiver.MSG_GAIN_STABILIZATION);
                    intent.putExtra(MainBroadcastReceiver.DATA_GS_STATUS, MainBroadcastReceiver.DATA_END);
                    intent.putExtra(MainBroadcastReceiver.DATA_K40_PEAK, K40_Ch);
                    LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
                    Init_GainStabilization();

                }
            }
        }
    }

    public String SpectrumToString(double[] spectrum) {

        String SpectrumStr = "";
        for (int i = 0; i < spectrum.length; i++) {

            SpectrumStr += Double.toString(spectrum[i]) + ",";

        }

        return SpectrumStr;

    }

    public double Get_Gamma_DoseRate_Spectrum_AVG_nSV(Spectrum MS, int Acqtime) {
        double DoseRate = 0;

        //DoseRate = NcLibrary.DoseRateCalculate(MS.ToInteger(), Coeffcients, mPmtSurface, mCrystal);
        DoseRate = NcLibrary.DoseRateCalculate_GE(MS.ToDouble(), MS.Get_AcqTime(), Real_BG.ToDouble(), Real_BG.Get_AcqTime(), Coeffcients, mPmtSurface, mCrystal,getGECoef());


        if (EVENT_STATUS == EVENT_ING & mCrystal == CrystalType.LaBr)
            DoseRate += 50;
        if (DoseRate > SPC_DOSERATE_LIMIT) {
            DoseRate = NcLibrary.GM_to_uSV(GM_Cnt);
        }
        return DoseRate / Acqtime;
    }

    public void Background_GainStabilization(double Before_K40, double Now_K40) {
        try {
            if (Before_K40 == Now_K40)
                return;

            int[] BG = new int[this.Real_BG.Get_Ch_Size()];
            int[] NewBG = new int[this.Real_BG.Get_Ch_Size()];
            BG = this.Real_BG.ToInteger();

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
            this.Real_BG.Set_Spectrum(NewBG, this.Real_BG.Get_AcqTime());

            // --===--
            Intent intent = new Intent(MainBroadcastReceiver.MSG_REMEASURE_BG);
            intent.putExtra(MainBroadcastReceiver.DATA_SPECTRUM, this.Real_BG);
            LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent);
            // --===--
            //NcLibrary.Write_ExceptionLog1("DATA_SPECTRUM = "+Real_BG);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    private void Init_GainStabilization() {
        mGain_elapsedTime = 0;
        mMS_Gainstabilization.ClearAllData();
    }

    ////
    private void Operator_NeutronEventSatus(boolean IsEvent) {
        if (IsEvent == true) {
            if (EVENT_STATUS_N == EVENT_NONE)
                EVENT_STATUS_N = EVENT_BEGIN;
            else if (EVENT_STATUS_N == EVENT_BEGIN)
                EVENT_STATUS_N = EVENT_ING;
            else if (EVENT_STATUS_N == EVENT_FINISH)
                EVENT_STATUS_N = EVENT_BEGIN;
            else if (EVENT_STATUS_N == EVENT_ING)
                EVENT_STATUS_N = EVENT_ING;
        } else {
            if (EVENT_STATUS_N == EVENT_NONE)
                EVENT_STATUS_N = EVENT_NONE;
            else if (EVENT_STATUS_N == EVENT_ING)
                EVENT_STATUS_N = EVENT_FINISH;
            else if (EVENT_STATUS_N == EVENT_FINISH)
                EVENT_STATUS_N = EVENT_NONE;
            else if (EVENT_STATUS_N == EVENT_BEGIN)
                EVENT_STATUS_N = EVENT_FINISH;
        }
    }

    private void Operator_GammaEventSatus(boolean IsEvent) {
        if (IsEvent == true) {
            if (EVENT_STATUS == EVENT_NONE)
                EVENT_STATUS = EVENT_BEGIN;
            else if (EVENT_STATUS == EVENT_BEGIN)
                EVENT_STATUS = EVENT_ING;
            else if (EVENT_STATUS == EVENT_FINISH)
                EVENT_STATUS = EVENT_BEGIN;
            else if (EVENT_STATUS == EVENT_ING)
                EVENT_STATUS = EVENT_ING;
        } else {
            if (EVENT_STATUS == EVENT_NONE)
                EVENT_STATUS = EVENT_NONE;
            else if (EVENT_STATUS == EVENT_ING)
                EVENT_STATUS = EVENT_FINISH;
            else if (EVENT_STATUS == EVENT_FINISH)
                EVENT_STATUS = EVENT_NONE;
            else if (EVENT_STATUS == EVENT_BEGIN)
                EVENT_STATUS = EVENT_FINISH;
        }
    }

    private void Operator_HealthSafetyEventSatus(double Gamma_doserate) {

        mGammaDoserate = Gamma_doserate;

        if (HealthSafety_Threshold * 1000 < Gamma_doserate) {
            if (IsHealthEvent == false) {
                Intent intent3 = new Intent(MainBroadcastReceiver.MSG_HEALTH_EVENT);
                intent3.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_BEGIN);

                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent3);

                Log.i(TAG_Event, "Health Event start");
            }
            IsHealthEvent = true;
        } else {
            if (IsHealthEvent) {
                Intent intent3 = new Intent(MainBroadcastReceiver.MSG_HEALTH_EVENT);
                intent3.putExtra(MainBroadcastReceiver.DATA_EVENT_STATUS, EVENT_FINISH);
                LocalBroadcastManager.getInstance(mSuper).sendBroadcast(intent3);

                Log.i(TAG_Event, "Health Event finish");
            }
            IsHealthEvent = false;
        }

    }

    public Detector Get_Detector() {
        Detector det = new Detector();
        det = this;
        return det;
    }

    public void Discrimination() {
        if (Low_discrimination >= Upper_discrimination)
            return;

        try {
            int temp[] = new int[MS.Get_Ch_Size()];

            for (int i = (int) Low_discrimination; i < (int) Upper_discrimination; i++) {
                temp[i] = MS.at_ToInt(i);
            }
            MS.Set_Spectrum(temp);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public void Set_Mode(int Mode) {
        mMode = Mode;
    }

    public int Get_Mode() {
        return mMode;
    }

    public boolean isIdMode() {
        return (mMode == ID_MODE) ? true : false;
    }

    public void Set_Measurement_info(MeasurementInfo ms_info) {
        mMS_Info = ms_info;
    }

    public MeasurementInfo Get_Measurement_info() {
        return mMS_Info;
    }

    private int Get_IsoClass_Color(String Class) {

        if (Class == null)
            return 0;
        if (Class.matches(".*SNM.*"))
            return Color.rgb(150, 24, 150);
        if (Class.matches(".*IND.*"))
            return Color.rgb(27, 23, 151);
        if (Class.matches(".*MED.*"))
            return Color.rgb(44, 192, 185);
        if (Class.matches(".*NORM.*"))
            return Color.rgb(10, 150, 20);
        if (Class.matches(".*UNK.*"))
            return Color.RED;

        return Color.GRAY;
    }

    public int get_K40_ID_Threshold() {
        //YKIM K-40 threshold가 는 검출기와 상관없이 동일하게 사용함.
        switch (mCrystal) {
            case CeBr:
                //return 160;
                return 200;
            case LaBr:
                //return 160;
                return 200;
            default:
                return 450;
        }
    }

    public void Set_PmtProperty(int Packet_code) {
        double Cm, rad;
        mCrystalType = Packet_code;
        double [] GECoef1=new double[]{};

        switch (Packet_code) {
            case HwPmtProperty_Code.CeBr_1_5x1_5:
                Cm = 1.5 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.CeBr;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0188103175,0.0505349777,-0.0516779963,0.0258608290,-0.0064929916,0.0006763102};
                setGECoef(GECoef1);


                break;
            case HwPmtProperty_Code.CeBr_2x2:
                Cm = 2 * 2.54;

                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);

                mCrystal = CrystalType.CeBr;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0105347600,0.0282449861,-0.0287542184,0.0142575609,-0.0035193428,0.0003568289};
                setGECoef(GECoef1);


                break;
            case HwPmtProperty_Code.CeBr_3x3:
                Cm = 3 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.CeBr;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0047249372,0.0126035481,-0.0127116779,0.0062011971,-0.0014908868,0.0001455325};
                setGECoef(GECoef1);


                break;

            case HwPmtProperty_Code.LaBr_1_5x1_5:
                Cm = 1.5 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.LaBr;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0188103175,0.0505349777,-0.0516779963,0.0258608290,-0.0064929916,0.0006763102};
                setGECoef(GECoef1);


                break;
            case HwPmtProperty_Code.LaBr_2x2:
                Cm = 2 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.LaBr;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0105347600,0.0282449861,-0.0287542184,0.0142575609,-0.0035193428,0.0003568289};
                setGECoef(GECoef1);


                break;
            case HwPmtProperty_Code.LaBr_3x3:
                Cm = 3 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.LaBr;
                mGCFactor = mGCDefFactor;


                //Dose Factor
                GECoef1=new double[] {-0.0047249372,0.0126035481,-0.0127116779,0.0062011971,-0.0014908868,0.0001455325};
                setGECoef(GECoef1);

                break;

            case HwPmtProperty_Code.NaI_1x1:
                Cm = 1 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.039451281,0.112142277,-0.122991796,0.067406637,-0.018932194,0.002225656};
                setGECoef(GECoef1);

                break;
            case HwPmtProperty_Code.NaI_3x3:
                Cm = 3 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.003941285,0.010927882,-0.011436229,0.005822865,-0.001475765,0.00015334};
                setGECoef(GECoef1);



                break;
            case HwPmtProperty_Code.NaI_2x3:
                Cm = 2.5 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0024418741,0.0067535527,-0.0070657287,0.0035947883,-0.0009078989,0.0000935149};
                setGECoef(GECoef1);

                break;
            case HwPmtProperty_Code.NaI_2x2:
                Cm = 2 * 2.54;

                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mPmtSurface = mPmtSurface * 0.85;
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.009219166,0.025628772,-0.027106762,0.014082004,-0.003681875,0.000398323};
                setGECoef(GECoef1);

                break;
            case HwPmtProperty_Code.NaI_1_5x1_5:
                Cm = 1.5 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mPmtSurface = mPmtSurface * 0.75;
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;


                //Dose Factor
                GECoef1=new double[] {-0.0169633927,0.0475735783,-0.0510098808,0.0270256301,-0.0072543774,0.0008096110};
                setGECoef(GECoef1);

                break;
            case HwPmtProperty_Code.NaI_2x4x16:

                mPmtSurface = 5.08 * 40.64;
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0004337293,0.0012719968,-0.0013967972,0.0007428293,-0.0001954654,0.0000208335};
                setGECoef(GECoef1);


                break;
            case HwPmtProperty_Code.NaI_3x5x16:
                mPmtSurface = 5 * 16;
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;


                //Dose Factor
                GECoef1=new double[] {-0.0003333685,0.0009655947,-0.0010402498,0.0005384217,-0.0001366689,0.0000139522};
                setGECoef(GECoef1);


                break;
            case HwPmtProperty_Code.NaI_4x4x16:
                mPmtSurface = 4 * 16;
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                GECoef1=new double[] {-0.0003992128,0.0011449423,-0.0012162643,0.0006177587,-0.0001530905,0.0000152088};
                setGECoef(GECoef1);

                break;

            default:
                Cm = 3 * 2.54;
                rad = (Cm / 2);

                mPmtSurface = ((rad * rad) * Math.PI);
                mCrystal = CrystalType.NaI;
                mGCFactor = mGCDefFactor;
                mGCFactor = mGCDefFactor;

                //Dose Factor
                //NaI 3x3
                GECoef1=new double[] {-0.003941285,0.010927882,-0.011436229,0.005822865,-0.001475765,0.00015334};
                setGECoef(GECoef1);


                break;
        }
    }

    public int Get_MeasurementMode() {
        return mMode;
    }

    private void onTextWritingFindK40(String Title, int K40_Ch, Spectrum mMS_Gainstabilization,
                                      ArrayList<String> StrArraylist, int count) {

        String body = "";

        StrArraylist.add(
                Integer.toString(count) + "- " + Title + " - FoundK40 : " + Integer.toString(K40_Ch) + " GcValue : "
                        + MainActivity.NewGC + "Acqtime :" + Integer.toString((int) mMS_Gainstabilization.Get_AcqTime()));
        for (int i = 0; i < StrArraylist.size(); i++) {
            body += StrArraylist.get(i) + "\n";
        }

        File file;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(path + File.separator + "FoundK40.txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter buw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
            buw.write(body);
            buw.close();
            fos.close();

            // Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다",
            // 1).show();
        } catch (IOException e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    private void onTextWritingFindK40Spectrum(String title, int count, String Spectrum) {

        File file;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(path + File.separator + Integer.toString(count) + "-" + title + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedWriter buw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
            buw.write(Spectrum);
            buw.close();
            fos.close();

            // Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다",
            // 1).show();
        } catch (IOException e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }
}
