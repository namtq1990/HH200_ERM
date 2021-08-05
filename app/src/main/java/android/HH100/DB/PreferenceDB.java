package android.HH100.DB;

import android.R.bool;
import android.HH100.*;
import android.HH100.Structure.Detector;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.OptionDefaultData;
import android.HH100.Structure.Spectrum;
import android.content.*;
import android.content.res.*;
import android.preference.*;
import android.util.Log;

import Debug.Debug;

public class PreferenceDB {

    public static final String PREF_DEBUG_MODE = "pref_debug_mode";

    // public static Resources mResource ;
    public static Context mSuperContext;
    private String mAB_table; // = mResource.getString(R.string.ab_table);
    private String mHW_AB_table; // = mResource.getString(R.string.ab_table);
    private String mBG_table;// = mResource.getString(R.string.bg_table);

    private SharedPreferences mPref;

    public PreferenceDB(Context context) {
        mSuperContext = context;
        mAB_table = mSuperContext.getString(R.string.ab_table);
        mBG_table = mSuperContext.getString(R.string.bg_table);
        mHW_AB_table = mSuperContext.getString(R.string.hw_ab_table);

        mPref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        Debug.isSWDebug = getDebugMode();
    }

    public String Get_Selected_IsoLibName() {
        try {
            String Temp = Get_String_From_pref((mSuperContext.getString(R.string.IsoLib_List_Key)));
            if (Temp == null)
                Temp = "null";
            if (Temp.matches("0.*"))
                Temp = "null";

            return Temp;
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
            return "null";
        }
    }

    public boolean getDebugMode() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        return pref.getBoolean(PREF_DEBUG_MODE, false);
    }

    public void setDebugMode(boolean isDebugMode) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        pref.edit().putBoolean(PREF_DEBUG_MODE, isDebugMode)
                .apply();
    }

    public String Get_equipment_From_pref() {
        return Get_String_From_pref(mSuperContext.getString(R.string.Setup_Instrument_Key));
    }

    public Spectrum Get_Background_Data() {
        try {
            Spectrum Result = new Spectrum();

            Result.Set_Spectrum(Get_BG_From_pref(), Get_BG_MeasuredAcqTime_From_pref());
            Result.Set_MeasurementDate(Get_BG_Date_From_pref());

            return Result;
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
            return new Spectrum();
        }
    }

    public int[] Get_BG_From_pref() {
        try {
            int[] ChArray = new int[1024];

            SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
            for (int i = 0; i < 1024; i++) {
                ChArray[i] = pref.getInt(mSuperContext.getString(R.string.bg_array) + String.valueOf(i), 0);
            }

            return ChArray;
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
            return new int[1024];
        }
        // TODO Auto-generated method stub
    }

    public void Set_BG_On_pref(int[] ChArray, int ArraySize) {
        try {
            SharedPreferences pref = mSuperContext.getSharedPreferences(mBG_table, 0);
            SharedPreferences.Editor editor = pref.edit();
            for (int i = 0; i < ArraySize; i++) {
                editor.putInt(mSuperContext.getString(R.string.bg_array) + String.valueOf(i), ChArray[i]);
            }
            editor.commit();
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);

        }
    }

    public void Set_FirstInstall(Boolean IsInstalled) {
        try {
            SharedPreferences pref = mSuperContext.getSharedPreferences("IsInstall", 0);
            SharedPreferences.Editor editor = pref.edit();

            editor.putBoolean("IsInstall", IsInstalled);

            editor.commit();
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);

        }
    }

    public void Set_BG_Date_From_pref(String Date) {
        try {
            SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(mSuperContext.getString(R.string.bg_date), Date);
            editor.commit();
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public void Set_BG_MeasuredAcqTime_From_pref(int AcqTime) {
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.bg_acqtime), String.valueOf(AcqTime));
        editor.commit();
    }

    public void Set_CryStalType_Name_pref(String CrystalTypeName) {
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.crystal_type_table), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.crystal_type_name), CrystalTypeName);
        editor.commit();
    }


    public void Set_CryStalType_Number_pref(String CrystalTypeNumber) {
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.crystal_type_table), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.crystal_type_number), CrystalTypeNumber);
        editor.commit();
    }

    public void Set_BG_MeasuredRealAcqTime_From_pref(long AcqTime) {
        Log.i("DB", "Set real acq time- " + AcqTime);
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.bg_acqtime) + "_real", String.valueOf(AcqTime));
        editor.commit();
    }

    public void Set_Calibration_Result(double A, double B, double C, double PeakCh1, double PeakCh2, double PeakCh3) {
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.ab_table), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.calia), String.valueOf(A));
        editor.putString(mSuperContext.getString(R.string.calib), String.valueOf(B));
        editor.putString(mSuperContext.getString(R.string.calic), String.valueOf(C));
        editor.putString(mSuperContext.getString(R.string.cali_peak1), String.valueOf(PeakCh1));
        editor.putString(mSuperContext.getString(R.string.cali_peak2), String.valueOf(PeakCh2));
        editor.putString(mSuperContext.getString(R.string.cali_peak3), String.valueOf(PeakCh3));
        editor.commit();
    }

    public void Set_Calibration_Result(double[] EnCoeff, double[] ChCoeff) {

        if (EnCoeff.length < 3 | ChCoeff.length < 3)
            return;

        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.ab_table), 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.calia), String.valueOf(EnCoeff[0]));
        editor.putString(mSuperContext.getString(R.string.calib), String.valueOf(EnCoeff[1]));
        editor.putString(mSuperContext.getString(R.string.calic), String.valueOf(EnCoeff[2]));
        editor.putString(mSuperContext.getString(R.string.cali_peak1), String.valueOf(ChCoeff[0]));
        editor.putString(mSuperContext.getString(R.string.cali_peak2), String.valueOf(ChCoeff[1]));
        editor.putString(mSuperContext.getString(R.string.cali_peak3), String.valueOf(ChCoeff[2]));
        editor.commit();
    }

    public void Set_CaliPeak3_From_pref(double K40_Channel) {
        try {
            SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.ab_table), 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(mSuperContext.getString(R.string.cali_peak3), String.valueOf(K40_Channel));
            editor.commit();
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public Boolean Get_FirstInstall() {

        SharedPreferences pref = mSuperContext.getSharedPreferences("IsInstall", 0);

        return pref.getBoolean("IsInstall", true);
    }

    ///
    public String Get_BG_Date_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.bg_date), String.valueOf(0));

        return Temp;
    }

    public String Get_CryStal_Type_Name_pref() {
        String Temp;
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.crystal_type_table), 0);
        Temp = pref.getString(mSuperContext.getString(R.string.crystal_type_name), "NULL");
        //Temp = pref.getString(mSuperContext.getString(R.string.crystal_type_name), Integer.toString(Detector.HwPmtProperty_Code.CeBr_2x2));

        return Temp;
    }

    public String Get_CryStal_Type_Number_pref() {
        String Temp;
        SharedPreferences pref = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.crystal_type_table), 0);
        Temp = pref.getString(mSuperContext.getString(R.string.crystal_type_number), Integer.toString(Detector.HwPmtProperty_Code.NaI_3x3));
        //Temp = pref.getString(mSuperContext.getString(R.string.crystal_type_number), Integer.toString(Detector.HwPmtProperty_Code.CeBr_2x2));

        return Temp;
    }

    public int Get_BG_MeasuredAcqTime_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.bg_acqtime), String.valueOf(0));

        return Integer.valueOf(Temp);
    }

    public long Get_BG_MeasuredRealAcqTime_From_pref() {

        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mSuperContext.getString(R.string.bg_table), 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.bg_acqtime) + "_real", String.valueOf(0));

        Log.i("DB", "get real acq time- " + Temp);
        return Long.valueOf(Temp);
    }

    public int Get_Calibration_AcqCnt() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        String temp = pref.getString(mSuperContext.getString(R.string.Calib_time), null);
        int Result;
        if (temp == null)
            Result = 200000;
        else
            Result = Integer.valueOf(temp) * 1000;
        return Result;
    }

    public double Get_Cali_A_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.calia), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_Cali_B_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.calib), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_Cali_C_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.calic), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double[] Get_Cali_ABC_From_pref() {
        double Result[] = new double[3];
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);

        Temp = prefAB.getString(mSuperContext.getString(R.string.calia), String.valueOf(0));
        Result[0] = Double.valueOf(Temp);

        Temp = prefAB.getString(mSuperContext.getString(R.string.calib), String.valueOf(0));
        Result[1] = Double.valueOf(Temp);

        Temp = prefAB.getString(mSuperContext.getString(R.string.calic), String.valueOf(0));
        Result[2] = Double.valueOf(Temp);

        return Result;
    }

    public void Set_ABC_From_pref(double A, double B, double C) {
        SharedPreferences pref = mSuperContext.getSharedPreferences(mAB_table, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(mSuperContext.getString(R.string.calia), String.valueOf(A));
        editor.putString(mSuperContext.getString(R.string.calib), String.valueOf(B));
        editor.putString(mSuperContext.getString(R.string.calic), String.valueOf(C));
        editor.commit();
    }

    public void Set_ABC_From_pref(double[] ABC) {
        try {
            SharedPreferences pref = mSuperContext.getSharedPreferences(mAB_table, 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(mSuperContext.getString(R.string.calia), String.valueOf(ABC[0]));
            editor.putString(mSuperContext.getString(R.string.calib), String.valueOf(ABC[1]));
            editor.putString(mSuperContext.getString(R.string.calic), String.valueOf(ABC[2]));
            editor.commit();
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public void Set_HW_ABC_From_pref(double[] ABC, double[] PeakCh) {
        try {

            SharedPreferences pref = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(mSuperContext.getString(R.string.hw_calia), String.valueOf(ABC[0]));
            editor.putString(mSuperContext.getString(R.string.hw_calib), String.valueOf(ABC[1]));
            editor.putString(mSuperContext.getString(R.string.hw_calic), String.valueOf(ABC[2]));

            editor.putString(mSuperContext.getString(R.string.hw_cali_peak1_ch), String.valueOf(PeakCh[0]));
            editor.putString(mSuperContext.getString(R.string.hw_cali_peak2_ch), String.valueOf(PeakCh[1]));
            editor.putString(mSuperContext.getString(R.string.hw_cali_peak3_ch), String.valueOf(PeakCh[2]));
            editor.commit();

        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public int Get_ManualID_DefaultTime() {
        try {
            String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_manual_id_defalut));
            int result = 0;
            if (temp == null)
                result = 60;
            else
                result = Integer.valueOf(temp);

            return result;
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
            return 0;
        }
    }

    public int Get_ManualID_AdjustTime() {
        try {
            String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_manual_id_adjust));
            int result = 0;
            if (temp == null)
                result = 10;
            else
                result = Integer.valueOf(temp);

            return result;
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
            return 0;
        }
    }

    public String Get_String_From_pref(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        return pref.getString(key, null);
    }

    public boolean Get_Bool_From_pref(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        return pref.getBoolean(key, false);
    }

    /*
     * public String Get_GainValue() { return Get_String_From_pref("GainValue_Key");
     * }
     *
     *
     * public void Set_GainValue(String CurrentGain) { try {
     * Set_String_on_pref("GainValue_Key", CurrentGain); } catch (Exception e) {
     *
     * } }
     */

    public String Get_User_From_pref() {
        return Get_String_From_pref(mSuperContext.getString(R.string.user));
    }

    public String Get_AdminPW_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.Admin_Password));
        String result = "";
        if (temp == null)
            result = "1234";
        else
            result = temp;

        return result;
    }

    public String Get_Location_From_pref() {
        return Get_String_From_pref(mSuperContext.getString(R.string.location));
    }

    public int Get_BG_AcqTime_SetValue_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.bgsecond));
        int result = 0;
        if (temp == null)
            result = 60;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public double Get_GammaThreshold_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.gamma_threshold));
        double result = 0;
        if (temp == null)
            result = 1000;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public double Get_NeutronThreshold_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.neutron_threshold));
        double result = 0;
        if (temp == null)
            result = 1;
        else
            result = Double.valueOf(temp);

        return result;
    }

    public int Get_HealthyThreshold_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.healthy_threshold));
        int result = 0;
        if (temp == null)
            result = 1000;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public boolean Get_SequenceMode_From_pref() {
        boolean result = Get_Bool_From_pref(mSuperContext.getString(R.string.p_sequence_mode_available));

        return result;
    }

    public boolean Get_RadresponderMode_From_pref() {
        boolean result = Get_Bool_From_pref(mSuperContext.getString(R.string.p_radresponder_mode_available));

        return result;
    }

    public int Get_SequenceMode_acqTime_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_sequence_mode_acq_time));
        int result = 0;
        if (temp == null)
            result = 30;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public int Get_SequenceMode_Repeat_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_sequence_mode_repeat));
        int result = 0;
        if (temp == null)
            result = 3;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public int Get_SequenceMode_PauseTime_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_sequence_mode_pause_time));
        int result = 0;
        if (temp == null)
            result = 5;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public int Get_AlarmSound_From_pref() {

        String temp = Get_String_From_pref(mSuperContext.getString(R.string.alarm_list));
        int result = 0;
        if (temp == null)
            result = 0;
        else
            result = Integer.valueOf(temp);
        return result;
    }

    public boolean Get_IsSvUnit_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.setup_dr_unit_key));
        int result = 0;
        if (temp == null)
            result = 1;
        else
            result = Integer.valueOf(temp);

        return (result == 0) ? true : false;
    }

    public boolean Get_IsConnect_UsbMode_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.Connect_Mode_Key));
        int result = 0;
        if (temp == null)
            result = 0;
        else
            result = Integer.valueOf(temp);
        // result 0= USB, 1=bluetoorh
        return (result == 0) ? true : false;
    }

    public boolean Get_IsSigma_From_pref() {

        String temp = Get_String_From_pref(mSuperContext.getString(R.string.is_sigma_gamma));
        int result = 0;
        if (temp == null)
            result = 0;
        else
            result = Integer.valueOf(temp);

        return (result == 0) ? false : true;
    }

    public double Get_GammaThreshold_Sigma_From_pref() {

        String temp = Get_String_From_pref(mSuperContext.getString(R.string.gamma_threshold_S));
        double result = 0;
        if (temp == null)
            result = 4;
        else
            result = Integer.valueOf(temp);

        return result;
    }

    public String Get_LastUser_Sigma_From_pref() {
        return Get_String_From_pref(mSuperContext.getString(R.string.last_user));
    }

    public void Set_Initialization() {

        OptionDefaultData mDefault = new OptionDefaultData(mSuperContext);

        Set_String_on_pref(mSuperContext.getString(R.string.user), mDefault.getUser());
        Set_String_on_pref(mSuperContext.getString(R.string.location), mDefault.getLocation());

        Set_String_on_pref(mSuperContext.getString(R.string.gamma_threshold), mDefault.fix_alarm);
        Set_String_on_pref(mSuperContext.getString(R.string.gamma_threshold_S), mDefault.var_alarm);
        Set_String_on_pref(mSuperContext.getString(R.string.neutron_threshold), mDefault.neu_threshold);

        Set_String_on_pref(mSuperContext.getString(R.string.healthy_threshold), mDefault.healthy_threshold);

        Set_String_on_pref(mSuperContext.getString(R.string.p_low_discri), mDefault.low_discrimination);
        Set_String_on_pref(mSuperContext.getString(R.string.p_upper_discri), mDefault.upper_discrimination);
        Set_String_on_pref(mSuperContext.getString(R.string.bgsecond), mDefault.bgsecond);
        Set_String_on_pref(mSuperContext.getString(R.string.Calib_time), mDefault.Calib_time);
        Set_String_on_pref(mSuperContext.getString(R.string.p_manual_id_defalut), mDefault.manual_id_time);

        Set_String_on_pref(mSuperContext.getString(R.string.p_manual_id_adjust), mDefault.manual_id_time_unit);

        //Set_String_on_pref(mSuperContext.getString(R.string.IsoLib_List_Key), mDefault.IsoLib_list);

        Set_String_on_pref(mSuperContext.getString(R.string.setup_dr_unit_key), mDefault.DoseUnit);

        Set_Boolean_pref(mSuperContext.getString(R.string.p_sequence_mode_available),
                mDefault.p_sequence_mode_available);

        Set_String_on_pref(mSuperContext.getString(R.string.p_sequence_mode_acq_time), mDefault.sequence_mode_acq_time);
        Set_String_on_pref(mSuperContext.getString(R.string.p_sequence_mode_pause_time),
                mDefault.sequence_mode_pause_time);

        Set_String_on_pref(mSuperContext.getString(R.string.p_sequence_mode_repeat),
                mDefault.sequence_mode_repeat_time);

        Set_Boolean_pref(mSuperContext.getString(R.string.p_radresponder_mode_available),
                mDefault.radresponder_mode_available);

        Set_String_on_pref(mSuperContext.getString(R.string.rad_response_eventlist_key),
                mDefault.response_eventlist_key);

        Set_String_on_pref(mSuperContext.getString(R.string.Connect_Mode_Key), mDefault.ConnectMode);
        // Set_String_on_pref(mSuperContext.getString(R.string.Admin_Password),
        // mDefault.Admin_Password);

    }

    public String Get_User_Name() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.user));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_Location_Info() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.location));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public void Set_String_on_pref(String key, String Value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, Value);

        editor.commit();
    }

    public void Set_Boolean_pref(String key, Boolean Value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mSuperContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, Value);

        editor.commit();

    }

    public double Get_CaliPeak1_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.cali_peak1), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_CaliPeak2_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.cali_peak2), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_CaliPeak3_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mAB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.cali_peak3), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_HW_CaliPeak1_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.hw_calia), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_HW_CaliPeak2_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.hw_calib), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_HW_CaliPeak3_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.hw_calic), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_HW_CaliPeakCh1_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.hw_cali_peak1_ch), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_HW_CaliPeakCh2_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.hw_cali_peak2_ch), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_HW_CaliPeakCh3_From_pref() {
        String Temp;
        SharedPreferences prefAB = mSuperContext.getSharedPreferences(mHW_AB_table, 0);
        Temp = prefAB.getString(mSuperContext.getString(R.string.hw_cali_peak3_ch), String.valueOf(0));
        return Double.valueOf(Temp);
    }

    public double Get_Low_Discrimination_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_low_discri));
        int result = 0;
        if (temp == null)
            result = 0;
        else
            result = Integer.valueOf(temp);

        return (double) result;
    }

    public double Get_Upper_Discrimination_From_pref() {
        String temp = Get_String_From_pref(mSuperContext.getString(R.string.p_upper_discri));
        int result = 0;
        if (temp == null)
            result = 0;
        else
            result = Integer.valueOf(temp);

        return (double) result;
    }

    public String Get_Last_Cntd_User() {

        String temp = "None";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.last_user));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_Last_Cntd_Date() {

        String temp = "None";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.last_time));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_Last_Cntd_Detector() {

        String temp = "None";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.last_detector));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_Last_Cntd_DetectorMac() {

        String temp = "None";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.last_detectorMac));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public void Set_Last_Cntd_User(String User) {
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.last_user), User);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public void Set_Last_Cntd_Date(String Date) {
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.last_time), Date);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public void Set_Last_Cntd_Detector(String DetectorName) {
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.last_detector), DetectorName);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public void Set_Last_Cntd_DetectorMac(String DetectorMac) {
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.last_detectorMac), DetectorMac);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    ///
    public void Set_sender_Server(String address) {

        try {
            Set_String_on_pref(mSuperContext.getString(R.string.p_email_server), address);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
    }

    public String Set_sender_Port(String port) {

        String temp = "";
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.p_email_port), port);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Set_sender_email(String address) {

        String temp = "";
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.p_email_address), address);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Set_sender_pw(String pw) {

        String temp = "";
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.p_email_pw), pw);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Set_recv_address(String address) {

        String temp = "";
        try {
            Set_String_on_pref(mSuperContext.getString(R.string.p_recv_email_address), address);
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_sender_Server() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.p_email_server));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_sender_Port() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.p_email_port));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_sender_email() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.p_email_address));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_sender_pw() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.p_email_pw));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_recv_email() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.p_recv_email_address));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }

    public String Get_MappingServer_IP() {

        String temp = "";
        try {
            temp = Get_String_From_pref(mSuperContext.getString(R.string.p_server_ip));
        } catch (Exception e) {
            NcLibrary.Write_ExceptionLog(e);
        }
        return temp;
    }
}
