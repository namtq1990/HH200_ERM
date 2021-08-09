package android.HH100;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import Debug.Debug;

import android.HH100.Dialog.RadresponderEventTypeListActivity;
import android.HH100.Dialog.RadresponderSponsorListActivity;
import android.HH100.Dialog.SaveBtnPreference;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.Service.MainBroadcastReceiver;
import android.HH100.DB.PreferenceDB;
import android.HH100.Dialog.EmailSetupActivity;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.OptionDefaultData;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PreferenceActivity extends android.preference.PreferenceActivity implements OnPreferenceChangeListener {

    private EditTextPreference mPre_inCharge;
    private EditTextPreference mPre_Location;
    private EditTextPreference mPre_GammaThre;
    private EditTextPreference mPre_NeutronThre;
    private EditTextPreference mPre_A;
    private EditTextPreference mPre_B;
    private EditTextPreference menuEnableKey;

    private ListPreference mIsoLib_List;
    private EditTextPreference medit_GammaThre;

    private PreferenceCategory mPreferenceCategory;

    private Preference mLastUser;
    private Preference mLastDetector;
    private Preference mLastTime;
    private PreferenceScreen mSWinformScreen, mBackGroundMesurement, mCalibrationMesurement, mMesurement,
            mInitialization, SystemLog, mResetCalibration, radresponder, radresponder_sponsor, eventlog;

    public static PreferenceScreen mAdmin;
    public static Context mContext;
    public static Activity PreferenceActivity;

    Debug mDebug;
    private SharedPreferences mPref;

    private int mClickCount = 0;
    private long mLstClick = 0;

    public static EditTextPreference HealthThre1;
    //	public static android.HH100.Dialog.IsotopePreference isotope;
//	public static ListPreference isotope;
    public static SaveBtnPreference isotope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // TODO Auto-generated method stub

        super.onCreate(savedInstanceState);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        addPreferencesFromResource(R.xml.setting);
        PreferenceDB mPreb = new PreferenceDB(getApplicationContext());
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = (float) 1.0;
        getWindow().setAttributes(layoutParams);

        PowerManager pm = (PowerManager) PreferenceActivity.this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "");
        wl.acquire();

        MainActivity.ACTIVITY_STATE = Activity_Mode.SETUP_MAIN;

        mContext = this;
        HealthThre1 = (EditTextPreference) findPreference(getResources().getString(R.string.healthy_threshold));
        isotope = (SaveBtnPreference) findPreference(getString(R.string.IsoLib_List_Key));
        mSWinformScreen = (PreferenceScreen) findPreference("p_inform");
        mLastUser = mSWinformScreen.findPreference("p_lastUser");
        mLastTime = mSWinformScreen.findPreference("p_lastTime");
        mLastDetector = mSWinformScreen.findPreference("p_lastDetector");

        if (MainActivity.mLog.D)
            Log.i(MainActivity.mLog.PreferenceActivity,
                    MainActivity.mLog.PreferenceActivity + "OnCreate mSWinformScreen : " + mSWinformScreen);
        Log.i(MainActivity.mLog.PreferenceActivity,
                MainActivity.mLog.PreferenceActivity + "OnCreate mLastUser : " + mLastUser);
        Log.i(MainActivity.mLog.PreferenceActivity,
                MainActivity.mLog.PreferenceActivity + "OnCreate mLastTime : " + mLastTime);
        Log.i(MainActivity.mLog.PreferenceActivity,
                MainActivity.mLog.PreferenceActivity + "OnCreate mLastDetector : " + mLastDetector);

        // DefaultSetupUserLocation();

        // Adminrock();
        mAdmin = (PreferenceScreen) findPreference(getString(R.string.p_admin));
        //20.02.04


        //190207 암호 표시 변경
        final PreferenceScreen menuEnableKey = (PreferenceScreen) findPreference(getString(R.string.menu_enable_key));
        menuEnableKey.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                LinearLayout layout = new LinearLayout(PreferenceActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

/*				//비밀번호를 입력해주세요
				TextView msg1= new TextView(PreferenceActivity.this);
				msg1.setGravity(Gravity.CENTER);
				msg1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
				msg1.setText(getString(R.string.password));
				msg1.setPadding(0,20,0,0);
				layout.addView(msg1);*/

                //password
                final EditText password = new EditText(PreferenceActivity.this);
                password.setLayoutParams(lp);
                //password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
                password.setPrivateImeOptions("defaultInputmode=english;"); //영어로 기본 자판 설정
                password.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_DONE); // IME_FLAG_NO_EXTRACT_UI 키보드 화면에 맞춰서 설정
                password.setTransformationMethod(new PasswordTransformationMethod());
                password.setGravity(Gravity.CENTER_HORIZONTAL);
                password.setMaxLines(1); //최대 한줄
                password.setFilters(new InputFilter[]{filter});

                lp.setMargins(20, 20, 20, 30);
                password.setLayoutParams(lp);
                layout.addView(password);

                password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        switch (actionId) {
                            case EditorInfo.IME_ACTION_DONE:
                                //완료 키를 눌렀을떄 키보드 내림
                                inputMethodManager.hideSoftInputFromWindow(password.getWindowToken(), 0);
                                break;
                            default:
                                // 기본 엔터키 동작
                                //InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(password.getWindowToken(), 0);
                                break;
                            //return false;
                        }
                        return true;
                    }
                });

                AlertDialog.Builder security = new AlertDialog.Builder(new ContextThemeWrapper(PreferenceActivity.this, android.R.style.Theme_Holo_Dialog));
                security.setCancelable(true);
                security.setTitle(getString(R.string.admin));
                security.setView(layout);
                security.setPositiveButton(getResources().getString(R.string.check), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        try {
                            PreferenceDB mPrefDB = new PreferenceDB(PreferenceActivity.this);
                            mPrefDB.Get_AdminPW_From_pref();
                            if (password.getText().toString().equals(mPrefDB.Get_AdminPW_From_pref())) {
                                MainActivity.admin = true; //20.02.04 추가
                                //menuEnableKey.setText("");
                                mAdmin.setEnabled(true);
                                mAdmin.setEnabled(true);
                                menuEnableKey.setEnabled(false);

                            } else {
                                Toast.makeText(PreferenceActivity.this, "failed", Toast.LENGTH_SHORT).show();
                                //menuEnableKey.setText("");
                                mAdmin.setEnabled(false);
                                //mAdmin.setEnabled(false);
                            }
                        } catch (Exception e) {
                            NcLibrary.Write_ExceptionLog(e);
                        }
                    }
                });
                security.setNegativeButton(getResources().getString(R.string.cancel), null);
                AlertDialog dlg = security.create();
                dlg.show();

                return false;
            }
        });

        if (mDebug.IsDebugMode) {
            if (mDebug.IsAdminEnable) {

                mAdmin.setEnabled(false);
                //EventListActivity.eventLogMenu = false;

                if (MainActivity.mLog.D)
                    Log.i(MainActivity.mLog.PreferenceActivity,
                            MainActivity.mLog.PreferenceActivity + "OnCreate IsAdminEnable Excute");
            }

        } else {

            mAdmin.setEnabled(false);
            //	EventListActivity.eventLogMenu = false;
        }


        //20.02.04
        if (mAdmin != null && menuEnableKey != null && MainActivity.admin) {
            mAdmin.setEnabled(true);
            menuEnableKey.setEnabled(false);
        }

        //190530 추가
        SystemLog = (PreferenceScreen) findPreference(getString(R.string.System_Log_Transfer_Key));
        SystemLog.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                NcLibrary.SendSystemLog(mContext);
                return false;
            }
        });

        //190207 수정
        final PreferenceScreen setPw = (PreferenceScreen) findPreference(getString(R.string.Admin_Password));
        setPw.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                PreferenceDB pre = new PreferenceDB(PreferenceActivity.this);
                LinearLayout layout = new LinearLayout(PreferenceActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                //password
                final EditText password = new EditText(PreferenceActivity.this);
                //password.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
                password.setLayoutParams(lp);
                password.setPrivateImeOptions("defaultInputmode=english;"); //영어로 기본 자판 설정
                password.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                password.setTransformationMethod(new PasswordTransformationMethod());
                password.setGravity(Gravity.CENTER_HORIZONTAL);
                password.setHint(pre.Get_AdminPW_From_pref());
                lp.setMargins(20, 20, 20, 30);
                password.setLayoutParams(lp);
                layout.addView(password);

                //password.setFilters(new InputFilter[]{new EmojiInputFilter(PreferenceActivity.this)});
                password.setFilters(new InputFilter[]{filter});

                password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        switch (actionId) {
                            case EditorInfo.IME_ACTION_DONE:
                                //완료 키를 눌렀을떄 키보드 내림
                                inputMethodManager.hideSoftInputFromWindow(password.getWindowToken(), 0);
                                break;
                            default:
                                // 기본 엔터키 동작
                                //InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(password.getWindowToken(), 0);
                                break;
                            //return false;
                        }
                        return true;
                    }
                });

                AlertDialog.Builder security = new AlertDialog.Builder(new ContextThemeWrapper(PreferenceActivity.this, android.R.style.Theme_Holo_Dialog));
                security.setCancelable(true);
                security.setTitle(getString(R.string.admin));
                security.setView(layout);
                security.setPositiveButton(getResources().getString(R.string.check), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        try {

                            if (password.getText().toString().length() != 0) {
                                pre.Set_String_on_pref(PreferenceActivity.this.getString(R.string.Admin_Password), password.getText().toString());
                            }
/*							if (password.getText().toString().equals(mPrefDB.Get_AdminPW_From_pref())|| password.getText().toString().equals(getResources().getString(R.string.master_PW)))
							{
								//menuEnableKey.setText("");
								mAdmin.setEnabled(true);
								mAdmin.setEnabled(true);
								menuEnableKey.setEnabled(false);

							}
							else
							{
								//menuEnableKey.setText("");
								mAdmin.setEnabled(false);
								mAdmin.setEnabled(false);
							}*/
                        } catch (Exception e) {
                            NcLibrary.Write_ExceptionLog(e);
                        }
                    }
                });
                security.setNegativeButton(getResources().getString(R.string.cancel), null);
                AlertDialog dlg = security.create();
                dlg.show();

                return false;
            }
        });


        mAdmin.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                return false;
            }
        });

        mResetCalibration = (PreferenceScreen) findPreference(getString(R.string.reset_Calibartion_key));
        mResetCalibration.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            /*
             * 기존문구<string name="reset_Calibartion_Dlg">This will restore calibration and HW configuration to factory setup. Do you want to proceed?</string>
             */
            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
                dialogBuilder.setTitle(getResources().getString(R.string.reset_Calibartion_sub_title));
                dialogBuilder.setMessage(getResources().getString(R.string.reset_Calibartion_Dlg));
                dialogBuilder.setNegativeButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        PreferenceDB mPreb = new PreferenceDB(getApplicationContext());

                        // mPreb.Set_Initialization();

                        if (mPreb.Get_HW_CaliPeakCh1_From_pref() == 0 || mPreb.Get_HW_CaliPeakCh2_From_pref() == 0) {

                            Toast.makeText(getApplicationContext(), "data does not exist", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();

                            double Cal_A, Cal_B, Cal_C, Cal_Ch1, Cal_Ch2, Cal_Ch3;

                            Cal_A = mPreb.Get_HW_CaliPeak1_From_pref();
                            Cal_B = mPreb.Get_HW_CaliPeak2_From_pref();
                            Cal_C = mPreb.Get_HW_CaliPeak3_From_pref();

                            Cal_Ch1 = mPreb.Get_HW_CaliPeakCh1_From_pref();
                            Cal_Ch2 = mPreb.Get_HW_CaliPeakCh2_From_pref();
                            Cal_Ch3 = mPreb.Get_HW_CaliPeakCh3_From_pref();

                            mPreb.Set_Calibration_Result(Cal_A, Cal_B, Cal_C, Cal_Ch1, Cal_Ch2, Cal_Ch3);

                            mPreb.Get_Cali_A_From_pref();
                            mPreb.Get_Cali_B_From_pref();
                            mPreb.Get_Cali_C_From_pref();

                            MainActivity.sendCali = true;
                            Intent send_gs = new Intent(MainBroadcastReceiver.MSG_FIXED_GC_SEND);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(send_gs);

                        }
                    }
                });
                dialogBuilder.setPositiveButton("NO", null);
                dialogBuilder.setCancelable(false);
                dialogBuilder.show();

                return false;
            }
        });

        mInitialization = (PreferenceScreen) findPreference(getString(R.string.initialization_key));
        mInitialization.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
                dialogBuilder.setTitle(getResources().getString(R.string.option_reset_msg_title));
                dialogBuilder.setMessage(getResources().getString(R.string.option_reset_msg_content));
                dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                PreferenceDB mPreb = new PreferenceDB(getApplicationContext());

                                mPreb.Set_Initialization();
                                OptionDefaultData mDefault = new OptionDefaultData(getApplicationContext());
                                //SetIsotopeList(Integer.parseInt(mDefault.IsoLib_list));

                                finish();
                                startActivity(getIntent());
                            }
                        });
                dialogBuilder.setNegativeButton("Cancel", null);
                dialogBuilder.setCancelable(false);
                dialogBuilder.show();

                return false;
            }
        });

        // SystemLog = (PreferenceScreen)
        // findPreference(getString(R.string.System_Log_Transfer_Key));
        //
        // SystemLog.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        //
        // @Override
        // public boolean onPreferenceClick(Preference preference) {
        //
        // NcLibrary.SendSystemLog(mContext);
        //
        // return false;
        // }
        // });

        mBackGroundMesurement = (PreferenceScreen) findPreference("SetBackgroundMeasurementMode");

        mBackGroundMesurement.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(PreferenceActivity.this, SetupSpectrumActivity.class);
                intent.putExtra(SetupSpectrumActivity.MEASUREMENT_MODE, SetupSpectrumActivity.MEASUREMENT_BACKGROUND);
                intent.putExtra(SetupSpectrumActivity.BG_GOALTIME,
                        MainActivity.mPrefDB.Get_BG_AcqTime_SetValue_From_pref());
                startActivity(intent);

                return false;
            }
        });


        radresponder = (PreferenceScreen) findPreference("radresponder_event");

        //@string/rad_response_eventlist_title
        //radresponder.setTitle(getString(R.string.rad_response_eventlist_title)+"("+MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key))+")");
        radresponder.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

/*				Intent intent = new Intent(PreferenceActivity.this, RadresponderEventListActivity.class);
				startActivity(intent);*/
                if (MainActivity.radresponderCheck) {
                    Intent intent = new Intent(PreferenceActivity.this, RadresponderEventTypeListActivity.class);
                    startActivity(intent);
                } else {
                    if (!NcLibrary.isNetworkOnline(mContext)) {
                        Toast.makeText(mContext, getString(R.string.internet_not), Toast.LENGTH_SHORT).show();
                        //finish();
                    } else {
                        Intent intent = new Intent(PreferenceActivity.this, RadresponderSponsorActivity.class);
                        intent.putExtra("type", "event"); //type : login, event
                        startActivity(intent);
                    }
                }
                return false;
            }
        });

        //20.01.10
        radresponder_sponsor = (PreferenceScreen) findPreference("radresponder_sponsor");
        //radresponder_sponsor.setTitle(getString(R.string.rad_response_eventlist_title1)+"("+MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key))+")");
        radresponder_sponsor.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (MainActivity.radresponderCheck) {
                    Intent intent = new Intent(PreferenceActivity.this, RadresponderSponsorListActivity.class);
                    startActivity(intent);
                } else {
                    if (!NcLibrary.isNetworkOnline(mContext)) {
                        Toast.makeText(mContext, getString(R.string.internet_not), Toast.LENGTH_SHORT).show();
                        //finish();
                    } else {
                        Intent intent = new Intent(PreferenceActivity.this, RadresponderSponsorActivity.class);
                        intent.putExtra("type", "login"); //type : login, event
                        startActivity(intent);
                    }
                }


/*				if(MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key)) == null ){
					MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.rad_response_eventid_key),getString(R.string.rad_response_eventlist11) );
				}
				if(MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key)) == null ){
					MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.rad_response_sponsor_key), getString(R.string.radresponder_sponsor2) );
				}

				Intent intent = new Intent(PreferenceActivity.this, RadresponderEventListActivity.class);
				startActivity(intent);*/
                return false;
            }
        });


        //20.02.04

        eventlog = (PreferenceScreen) findPreference("eventlog");
        //radresponder_sponsor.setTitle(getString(R.string.rad_response_eventlist_title1)+"("+MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key))+")");
        eventlog.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                NcLibrary.deleteDB(PreferenceActivity.this, new NcLibrary.OnOk() {
                    @Override
                    public void delete(int delete) {
                        if (delete == 1) {
                            ((Activity) PreferenceActivity.this).runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(PreferenceActivity.this, getResources().getString(R.string.delete_success), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if (delete == 2) {
                            ((Activity) PreferenceActivity.this).runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(PreferenceActivity.this, getResources().getString(R.string.delete_failed), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

                return false;
            }
        });


        mCalibrationMesurement = (PreferenceScreen) findPreference("SetCalibrationMode");

        mCalibrationMesurement.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(PreferenceActivity.this, SetupSpectrumActivity.class);
                intent.putExtra(SetupSpectrumActivity.MEASUREMENT_MODE,
                        SetupSpectrumActivity.MEASUREMENT_EN_CALIBRATION);
                intent.putExtra(SetupSpectrumActivity.CALIB_ENDCNT, MainActivity.mPrefDB.Get_Calibration_AcqCnt());
                startActivity(intent);

                return false;
            }
        });

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mLastUser.setSummary(pref.getString(getResources().getString(R.string.last_user), "None"));
        mLastTime.setSummary(pref.getString(getResources().getString(R.string.last_time), "None"));
        mLastDetector.setTitle(pref.getString(getResources().getString(R.string.last_detector), "None"));
        mLastDetector.setSummary(pref.getString(getResources().getString(R.string.last_detectorMac), "None"));

        IsotopesLibrary Isolib = new IsotopesLibrary(this);
        Vector<String> IsoLibList = Isolib.get_IsotopeLibrary_List();

        String[] temp = new String[IsoLibList.size()];
        for (int i = 0; i < IsoLibList.size(); i++) {
            temp[i] = IsoLibList.get(i);
        }

        isotope.setEntries(temp);
        isotope.setEntryValues(temp);

        //ListPreference listpr = (ListPreference) temp2;
/*		IsotopesLibrary Isolib = new IsotopesLibrary(this);
		Vector<String> IsoLibList = Isolib.get_IsotopeLibrary_List();

		String[] temp = new String[IsoLibList.size()];
		for (int i = 0; i < IsoLibList.size(); i++) {
			temp[i] = IsoLibList.get(i);
		}

		isotope.setEntries(temp);
		isotope.setEntryValues(temp);

		//
		String selectIsotope = MainActivity.mPrefDB.Get_Selected_IsoLibName();
		if(selectIsotope.equals("null"))
		{
			//isotope.setValueIndex(0);
		}
		else
		{
			for(int i = 0; i < temp.length; i++)
			{
				if(temp[i].equals(selectIsotope))
				{
					isotope.setValueIndex(i);

					//break;
				}
			}
		}*/
        //


        // =------
        Preference Sw_Update = (Preference) findPreference(getResources().getString(R.string.p_sw_update));
        Sw_Update.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri u = Uri.parse("http://bncsam2.cafe24.com/PeakAboutUpdateK3.html");
                if (MainActivity.mDebug.BP) {
                    u = Uri.parse("http://bncsam2.cafe24.com/PeakAboutUpdateBP.html");
                }
                i.setData(u);
                startActivity(i);

                return false;
            }
        });

        PreferenceScreen setup_email = (PreferenceScreen) findPreference(
                getResources().getString(R.string.p_export_event));
        setup_email.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(PreferenceActivity.this, EmailSetupActivity.class));
                return false;
            }
        });

        EditTextPreference upper_disc = (EditTextPreference) findPreference(
                getResources().getString(R.string.p_upper_discri));
        upper_disc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                try {
                    if (Integer.valueOf(newValue.toString()) > MainActivity.CHANNEL_ARRAY_SIZE) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
                        dialogBuilder.setTitle("Warning");
                        dialogBuilder.setMessage("< 1024");

                        dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
                        dialogBuilder.setCancelable(false);
                        dialogBuilder.show();

                        return false;
                    } else if (Integer.valueOf(newValue.toString()) < 0) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
                        dialogBuilder.setTitle("Warning");
                        dialogBuilder.setMessage("> 0");

                        dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
                        dialogBuilder.setCancelable(false);
                        dialogBuilder.show();

                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    NcLibrary.Write_ExceptionLog(e);
                    return false;
                }
            }
        });
        EditTextPreference low_disc = (EditTextPreference) findPreference(
                getResources().getString(R.string.p_low_discri));
        low_disc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    if (Integer.valueOf(newValue.toString()) > MainActivity.CHANNEL_ARRAY_SIZE) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
                        dialogBuilder.setTitle("Warning");
                        dialogBuilder.setMessage("< 1024");

                        dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
                        dialogBuilder.setCancelable(false);
                        dialogBuilder.show();

                        return false;
                    } else if (Integer.valueOf(newValue.toString()) < 0) {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PreferenceActivity.this);
                        dialogBuilder.setTitle("Warning");
                        dialogBuilder.setMessage("> 0");

                        dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
                        dialogBuilder.setCancelable(false);
                        dialogBuilder.show();

                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    NcLibrary.Write_ExceptionLog(e);
                    return false;
                }
            }
        });

        medit_GammaThre = (EditTextPreference) findPreference("p_gamma_threshold");

        medit_GammaThre.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Set_String_on_pref("p_IsSigma", "0");

                Set_String_on_pref("p_gamma_threshold", String.valueOf(newValue));

                return true;
            }
        });
        EditTextPreference edit_SigmaValue = (EditTextPreference) findPreference("p_gamma_Sigma");
        edit_SigmaValue.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Set_String_on_pref("p_IsSigma", "1");

                Set_String_on_pref("p_gamma_Sigma", String.valueOf(newValue));
                return true;
            }
        });

        // -----
        EditTextPreference edit_User = (EditTextPreference) findPreference(getResources().getString(R.string.user));
        edit_User.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mLastUser.setSummary(String.valueOf(newValue));
                return true;
            }
        });


        String s = Get_String_From_pref(getResources().getString(R.string.setup_dr_unit_key));
        if (s.matches("1")) {
            String summary = HealthThre1.getSummary().toString();
            summary = summary.replace("Sv/h", "rem/h");
            HealthThre1.setSummary(summary);
        } else {
            String summary = HealthThre1.getSummary().toString();
            summary = summary.replace("rem/h", "Sv/h");
            HealthThre1.setSummary(summary);
        }

        ListPreference DoseUnit = (ListPreference) findPreference(getResources().getString(R.string.setup_dr_unit_key));

        DoseUnit.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                String s = Get_String_From_pref(getResources().getString(R.string.setup_dr_unit_key));
                if (s.matches(newValue.toString()))
                    return true;
                Change_HealthAlarm_Unit((Integer.valueOf(newValue.toString()) == 0) ? true : false);
                return true;
            }
        });
        //

        PreferenceActivity = this;

        // DefaultSetupUserLocation();

        if (mPreb.Get_FirstInstall()) {

            mPreb.Set_Initialization();
            OptionDefaultData mDefault = new OptionDefaultData(getApplicationContext());
            //SetIsotopeList(Integer.parseInt(mDefault.IsoLib_list));

            mPreb.Set_FirstInstall(false);

            finish();
            startActivity(getIntent());

        }

        Preference preAppver = (Preference) findPreference(getResources().getString(R.string.CryStal_Type_Key));

        preAppver.setSummary(mPreb.Get_CryStal_Type_Name_pref());


        Preference preAppver1 = (Preference) findPreference(getResources().getString(R.string.app_version));
        try {
            preAppver1.setSummary(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
            preAppver1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    long clickTime = System.currentTimeMillis();
                    mClickCount += 1;
                    if (mClickCount == 1) {
                        mLstClick = clickTime;
                    } else {
                        long time = clickTime - mLstClick;
                        if (time <= 2000) {
                            if (mClickCount >= 3) {
                                mClickCount = 0;
                                Debug.isSWDebug = !Debug.isSWDebug;
                                mPreb.setDebugMode(Debug.isSWDebug);

                                Toast.makeText(PreferenceActivity.this, "Debug mode is " + Debug.isSWDebug, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            mClickCount = 0;
                        }
                        mLstClick = clickTime;
                    }

                    return false;
                }
            });
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            NcLibrary.Write_ExceptionLog(e);
        }


    }

    @Override
    protected void onResume() {

        //NcLibrary.SetActivityState_Timer(Activity_Mode.SETUP_MAIN, 500);
        MainActivity.ACTIVITY_STATE = Activity_Mode.SETUP_MAIN;


        if (MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key)) == null) {
            MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.rad_response_eventid_key), getString(R.string.rad_response_eventlist11));
        }
        if (MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key)) == null) {
            MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.rad_response_sponsor_key), getString(R.string.radresponder_sponsor2));
        }

        if (radresponder != null) {
            radresponder.setSummary(getString(R.string.rad_response_eventlist_summary) + " (" + MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key)) + ")");
        }
        if (radresponder_sponsor != null) {
            radresponder_sponsor.setSummary(getString(R.string.rad_response_eventlist_summary1) + " (" + MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key)) + ")");
        }

        if (mAdmin != null && menuEnableKey != null && MainActivity.admin) {
            mAdmin.setEnabled(true);
            mAdmin.setEnabled(true);
            menuEnableKey.setEnabled(false);
        }

        super.onResume();
    }

    @Override
    public void onContentChanged() {
        // TODO Auto-generated method stub
        super.onContentChanged();
    }

    public void Change_HealthAlarm_Unit(boolean IsSv) {

        if (IsSv) {
            EditTextPreference HealthThre = (EditTextPreference) findPreference(getResources().getString(R.string.healthy_threshold));
            String summary = HealthThre.getSummary().toString();
            summary = summary.replace("rem/h", "Sv/h");
            HealthThre.setSummary(summary);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            int Value = Integer.valueOf(pref.getString(getResources().getString(R.string.healthy_threshold), "0"));
            Value = (int) NcLibrary.Rem_To_Sv(Value);
            HealthThre.setText(String.valueOf(Value));

        } else {
            EditTextPreference HealthThre = (EditTextPreference) findPreference(
                    getResources().getString(R.string.healthy_threshold));
            String summary = HealthThre.getSummary().toString();
            summary = summary.replace("Sv/h", "rem/h");
            HealthThre.setSummary(summary);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            int Value = Integer.valueOf(pref.getString(getResources().getString(R.string.healthy_threshold), "0"));
            Value = (int) NcLibrary.Sv_To_Rem(Value);
            HealthThre.setText(String.valueOf(Value));
        }
    }

    public String Get_String_From_pref(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        return pref.getString(key, "0");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        // TODO Auto-generated method stub
        return false;
    }

    public void Set_String_on_pref(String key, String Value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, Value);
        editor.commit();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        // loadHeadersFromResource(R.xml.setting, target);
    }

    @Override
    protected void onDestroy() {

        MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
        //190102 추가
        //NcLibrary.U2AATimer();
        super.onDestroy();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PreferenceActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = PreferenceActivity.this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_signin, null))
                // Add action buttons
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // LoginDialogFragment.this.getDialog().cancel();
            }
        });
        return builder.create();
    }

    public void Adminrock() {

        PreferenceScreen email_src = (PreferenceScreen) findPreference(
                getResources().getString(R.string.p_export_event));
        email_src.setEnabled(false);

        EditTextPreference edit_CaliTime = (EditTextPreference) findPreference("Calibration measurement time");
        edit_CaliTime.setEnabled(false);
        EditTextPreference edit_BgSec = (EditTextPreference) findPreference("p_second");
        edit_BgSec.setEnabled(false);
        ListPreference edit_AlarmSound = (ListPreference) findPreference(getString(R.string.IsoLib_List_Key));
        edit_AlarmSound.setEnabled(false);

        ListPreference edit_DoseUnit = (ListPreference) findPreference("p_DoseUnit");
        edit_DoseUnit.setEnabled(false);
        PreferenceScreen Admin_Pass = (PreferenceScreen) findPreference("Admin_Password");
        Admin_Pass.setEnabled(false);
        EditTextPreference NeutronThre = (EditTextPreference) findPreference(
                getResources().getString(R.string.neutron_threshold));
        NeutronThre.setEnabled(false);
        EditTextPreference HealthThre = (EditTextPreference) findPreference(
                getResources().getString(R.string.healthy_threshold));
        HealthThre.setEnabled(false);
        PreferenceScreen PrefSrc_SetAlarmMode = (PreferenceScreen) findPreference("SetAlarmMode");
        PrefSrc_SetAlarmMode.setEnabled(false);
        PreferenceScreen disc = (PreferenceScreen) findPreference(getResources().getString(R.string.p_disc));
        disc.setEnabled(false);
        EditTextPreference manualID_defaultTime = (EditTextPreference) findPreference(
                getResources().getString(R.string.p_manual_id_defalut));
        manualID_defaultTime.setEnabled(false);
        EditTextPreference manualID_adjustTime = (EditTextPreference) findPreference(
                getResources().getString(R.string.p_manual_id_adjust));
        manualID_adjustTime.setEnabled(false);
        PreferenceScreen seqMode = (PreferenceScreen) findPreference(
                getResources().getString(R.string.p_psrc_sequential));
        seqMode.setEnabled(false);

        PreferenceScreen admin_pw = (PreferenceScreen) findPreference(
                getResources().getString(R.string.Admin_Password));
        admin_pw.setEnabled(false);

        PreferenceScreen SetBackgroundMeasurementMode = (PreferenceScreen) findPreference(
                getResources().getString(R.string.SetBackgroundMeasurementMode));
        SetBackgroundMeasurementMode.setEnabled(false);

        PreferenceScreen SetCalibrationMode = (PreferenceScreen) findPreference(
                getResources().getString(R.string.SetCalibrationMode));
        SetCalibrationMode.setEnabled(false);

        PreferenceScreen rad_response_option = (PreferenceScreen) findPreference(
                getResources().getString(R.string.rad_response_option_key));
        SetCalibrationMode.setEnabled(false);

    }

    public void AdminUnrock() {

        PreferenceScreen email_src = (PreferenceScreen) findPreference(
                getResources().getString(R.string.p_export_event));
        email_src.setEnabled(true);

        EditTextPreference edit_CaliTime = (EditTextPreference) findPreference("Calibration measurement time");
        edit_CaliTime.setEnabled(true);
        EditTextPreference edit_BgSec = (EditTextPreference) findPreference("p_second");
        edit_BgSec.setEnabled(false);
        ListPreference edit_AlarmSound = (ListPreference) findPreference(getString(R.string.IsoLib_List_Key));
        edit_AlarmSound.setEnabled(true);

        ListPreference edit_DoseUnit = (ListPreference) findPreference("p_DoseUnit");
        edit_DoseUnit.setEnabled(false);
        PreferenceScreen Admin_Pass = (PreferenceScreen) findPreference("Admin_Password");
        Admin_Pass.setEnabled(true);
        EditTextPreference NeutronThre = (EditTextPreference) findPreference(
                getResources().getString(R.string.neutron_threshold));
        NeutronThre.setEnabled(true);
        EditTextPreference HealthThre = (EditTextPreference) findPreference(
                getResources().getString(R.string.healthy_threshold));
        HealthThre.setEnabled(true);
        PreferenceScreen PrefSrc_SetAlarmMode = (PreferenceScreen) findPreference("SetAlarmMode");
        PrefSrc_SetAlarmMode.setEnabled(true);
        PreferenceScreen disc = (PreferenceScreen) findPreference(getResources().getString(R.string.p_disc));
        disc.setEnabled(true);
        EditTextPreference manualID_defaultTime = (EditTextPreference) findPreference(
                getResources().getString(R.string.p_manual_id_defalut));
        manualID_defaultTime.setEnabled(true);
        EditTextPreference manualID_adjustTime = (EditTextPreference) findPreference(
                getResources().getString(R.string.p_manual_id_adjust));
        manualID_adjustTime.setEnabled(true);
        PreferenceScreen seqMode = (PreferenceScreen) findPreference(
                getResources().getString(R.string.p_psrc_sequential));
        seqMode.setEnabled(true);

        PreferenceScreen admin_pw = (PreferenceScreen) findPreference(
                getResources().getString(R.string.Admin_Password));
        admin_pw.setEnabled(true);

        PreferenceScreen SetBackgroundMeasurementMode = (PreferenceScreen) findPreference(
                getResources().getString(R.string.SetBackgroundMeasurementMode));
        SetBackgroundMeasurementMode.setEnabled(true);

        PreferenceScreen SetCalibrationMode = (PreferenceScreen) findPreference(
                getResources().getString(R.string.SetCalibrationMode));
        SetCalibrationMode.setEnabled(true);

        PreferenceScreen rad_response_option = (PreferenceScreen) findPreference(
                getResources().getString(R.string.rad_response_option_key));
        SetCalibrationMode.setEnabled(true);

    }

    private void DefaultSetupUserLocation() {
        EditTextPreference mUser = (EditTextPreference) findPreference(getResources().getString(R.string.user));
        if (mUser.getText().equals("") || mUser.getText().equals("None")) {

            mUser.setText(getResources().getString(R.string.setup_user_default));

        }

        EditTextPreference mLocation = (EditTextPreference) findPreference(getResources().getString(R.string.location));

        if (mLocation.getText().equals("") || mLocation.getText().equals("None")) {

            mLocation.setText(getResources().getString(R.string.setup_location_default));

        }

    }

    public void SetIsotopeList(int index) {
        PreferenceDB mPreDB = new PreferenceDB(mContext);

        ListPreference edit_AlarmSound = (ListPreference) findPreference(getString(R.string.IsoLib_List_Key));
        //edit_AlarmSound.setValueIndex(index);

    }


    //0207
    public InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[A-Za-z0-9!#$%&(){|}~:;<=>?@*+,./^_ ]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };


}