package android.HH100.DB;

import java.io.*;
import java.nio.*;
import java.text.*;
import java.util.*;

import NcLibrary.Coefficients;
import android.HH100.*;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Misc.Struct;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.HW_Crytal_Info;
import android.HH100.Structure.NcLibrary;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.*;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.sax.StartElementListener;
import android.util.Log;

import static android.HH100.Structure.NcLibrary.Separate_EveryDash;
import static android.HH100.Structure.NcLibrary.Separate_EveryDash2;
import static android.HH100.Structure.NcLibrary.Separate_EveryDash3;

public class EventDBOper {// extends Thread {
	private static final String TAG = "EventDB";
	public static final boolean D = MainActivity.D;

	public static final String DB_FILE_NAME = "EventDB";
	public static final String DB_TABLE = "Event";
	public static final String DB_FOLDER = "SAM";

	public static final String DB_LIB_FOLDER = "SAM_Lib";
	public static final String MIDEA_FOLDER = "Media";

	public static final String DEVICE_FILE = "DeviceName";
	public static final String DB_VERSION_FILE = "DB_Version";

	public static final String DB_verion = "1.7";

	public static final String ROWID = "_id";
	public static final String DATE = "Date";
	public static final String DATE_BG = "Date_BG";
	public static final String DB_FORMAT_VERSION = "Column_Version";
	public static final String BEGIN = "'begin'";
	public static final String FINISH = "finish";
	public static final String LOCATION = "Location";
	public static final String AVG_GAMMA = "Avg_Gamma";
	public static final String AVG_NEUTRON = "Avg_Neutron";
	public static final String MAX_GAMMA = "Max_Gamma";
	public static final String MAX_NEUTRON = "Min_Neutron";
	public static final String AGENT = "Person_in_charge";
	public static final String EVENT_DETECTOR = "Event_Detector";
	// public static final String DB_FILE_NAME = "TextFile_Name";
	public static final String LATITUDE = "Latitude";
	public static final String LONGITUDE = "Longitude";
	public static final String ACQ_TIME = "AcqTime";
	public static final String ACQ_TIME_BG = "BgAcqTime";
	public static final String COMMENT = "Comment";
	// public static final String ISOTOPE= "Isotope";
	public static final String PHOTO = "Photo";
	public static final String VIDEO = "Video";
	public static final String RECODE = "Recode";
	public static final String FILL_CPS_AVG = "Fill_Cps_Avg";

	public static final String FAVORITE = "Favorite";
	public static final String GMT = "Gmt";
	public static final String CALIB_A = "Cali_A";
	public static final String CALIB_B = "Cali_B";
	public static final String CALIB_C = "Cali_C";
	public static final String SPECTRUM = "SpectrumView";
	public static final String SPECTRUM_BG = "Background";
	public static final String IDENTIFICATION = "Identification";
	public static final String MANUAL_ID = "Manual_ID";
	public static final String INSTRUMENT_MODEL = "Instrument_Model";
	public static final String REAL_ACQ_TIME = "Real_AcqTime";
	public static final String REAL_ACQ_TIME_BG = "Real_BgAcqTime";

	public static HW_Crytal_Info Cry_Info;

	public static boolean mIsBegin = false;

	private static Handler mHandle = null;
	public static DB_Ver mDB = new DB_Ver();
	public static int mAccumulate_Count = 0;

	private int[] mGammaCh_Count = new int[MainActivity.CHANNEL_ARRAY_SIZE];
	private int[] mBgCh_Count = new int[MainActivity.CHANNEL_ARRAY_SIZE];
	private boolean mIs_ManualID = false;

	private double mMAX_Gamma = 0;
	private double mMAX_Neutron = 0;

	private Calendar mBegin = null;
	private Vector<String> mID_Result = new Vector<String>();
	private static String DATABASE_CREATE_v1_4 = "create table " + DB_TABLE + " (" + ROWID
			+ " integer primary key autoincrement," + DATE + " text, " + BEGIN + " text, " + FINISH + " text, "
			+ LOCATION + " text," + AGENT + " text," + AVG_GAMMA + " text, " + AVG_NEUTRON + " text, " + MAX_GAMMA
			+ " text, " + MAX_NEUTRON + " text, " + EVENT_DETECTOR + " text, "
			// + DB_FILE_NAME + " text, "
			+ ACQ_TIME + " text, " + ACQ_TIME_BG + " text,"// -- v1.4
			+ COMMENT + " text, "
			// + ISOTOPE + " text, "
			+ PHOTO + " text," + GMT + " text," + CALIB_A + " real," + CALIB_B + " real," + CALIB_C + " real,"
			+ LONGITUDE + " real, " + LATITUDE + " real," + SPECTRUM + " text," // --v1.4
			+ SPECTRUM_BG + " text,"// --v1.4
			+ MANUAL_ID + " text,"// --v1.4
			+ IDENTIFICATION + " text);"; // --v1.4
	private static String DATABASE_CREATE_v1_5 = "create table " + DB_TABLE + " (" + ROWID
			+ " integer primary key autoincrement," + DB_FORMAT_VERSION + " text, " + INSTRUMENT_MODEL + " text, "
			+ DATE + " text, " + DATE_BG + " text, " + BEGIN + " text, " + FINISH + " text, " + LOCATION + " text,"
			+ AGENT + " text," + AVG_GAMMA + " text, " + AVG_NEUTRON + " text, " + MAX_GAMMA + " text, " + MAX_NEUTRON
			+ " text, " + ACQ_TIME + " text, " + ACQ_TIME_BG + " text,"// --
			// v1.4
			+ COMMENT + " text, " + PHOTO + " text," + VIDEO + " text," + GMT + " text," + CALIB_A + " real," + CALIB_B
			+ " real," + CALIB_C + " real," + LONGITUDE + " real, " + LATITUDE + " real," + SPECTRUM + " text," // --v1.4
			+ SPECTRUM_BG + " text,"// --v1.4
			+ MANUAL_ID + " text,"// --v1.4
			+ IDENTIFICATION + " text);";
	private static String DATABASE_CREATE_v1_6 = "create table " + DB_TABLE + " (" + ROWID
			+ " integer primary key autoincrement," + DB_FORMAT_VERSION + " text, " + INSTRUMENT_MODEL + " text, "
			+ DATE + " text, " + DATE_BG + " text, " + BEGIN + " text, " + FINISH + " text, " + LOCATION + " text,"
			+ AGENT + " text," + AVG_GAMMA + " text, " + AVG_NEUTRON + " text, " + MAX_GAMMA + " text, " + MAX_NEUTRON
			+ " text, " + ACQ_TIME + " text, " + ACQ_TIME_BG + " text,"// --
			// v1.4
			+ COMMENT + " text, " + PHOTO + " text," + VIDEO + " text," + GMT + " text," + CALIB_A + " real," + CALIB_B
			+ " real," + CALIB_C + " real," + LONGITUDE + " real, " + LATITUDE + " real," + SPECTRUM + " text," // --v1.4
			+ SPECTRUM_BG + " text,"// --v1.4
			+ MANUAL_ID + " text,"// --v1.4
			+ IDENTIFICATION + " text," + EVENT_DETECTOR + " text);"; // --v1.4


	private  String DATABASE_CREATE_v1_7 =
			//BNC HH200 version 1.7
			"create table " + DB_TABLE + " ("
					+ ROWID + " integer primary key autoincrement,"
					+ DB_FORMAT_VERSION+ " text, "
					+ INSTRUMENT_MODEL + " text, "
					+ DATE + " text, "
					+ DATE_BG + " text, "
					+ BEGIN+  " text, "
					+ FINISH + " text, "
					+ LOCATION + " text,"
					+ AGENT + " text,"
					+ AVG_GAMMA + " text, "
					+ AVG_NEUTRON + " text, "
					+ MAX_GAMMA + " text, "
					+ MAX_NEUTRON + " text, "
					+ ACQ_TIME + " text, "
					+ REAL_ACQ_TIME + " real, "
					+ ACQ_TIME_BG + " text,"// --
					+ REAL_ACQ_TIME_BG + " real, "
					+ COMMENT + " text, "
					+ PHOTO + " text,"
					+ VIDEO + " text,"
					+ GMT+ " text,"
					+ CALIB_A + " real,"
					+ CALIB_B + " real,"
					+ CALIB_C + " real,"
					+ LONGITUDE + " real,"
					+ LATITUDE + " real,"
					+ SPECTRUM + " text," // --v1.4
					+ SPECTRUM_BG + " text,"// --v1.4
					+ MANUAL_ID + " text,"// --v1.4
					+ IDENTIFICATION + " text,"
					+ EVENT_DETECTOR + " text,"
					+ FAVORITE + " text,"
					+ RECODE + " text,"
					+ FILL_CPS_AVG + " text);"; // --v1.4




	private static SQLiteDatabase _db;

	private String mDB_Path;

	private void Set_DB_Path() {
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}

		mDB_Path = dbpath.getAbsolutePath() + File.separator + "EventDB.sql";
	}

	public String Get_WroteDB_version() {

		String Result = "1.0";
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + DB_VERSION_FILE + ".txt"); // �뵒諛붿씠�뒪
		// �꽕�엫
		// �뤃�뜑瑜�
		// 留뚮뱺�떎.
		if (!(nameFilePath.isFile())) {
			// All_DB_Remove();
		} else {
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(nameFilePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] buf = new byte[3];
			try {
				fis.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Result = new String(buf);
		}
		return Result;
	}

	public void Create_DB_VersionFile() {

		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + DB_VERSION_FILE + ".txt"); // �뵒諛붿씠�뒪
		// �꽕�엫
		// �뤃�뜑瑜�
		// 留뚮뱺�떎.
		if (!(nameFilePath.isFile())) {
			FileOutputStream Fos = null;
			try {

				Fos = new FileOutputStream(nameFilePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] buf = new byte[3];
			try {
				Fos.write(DB_verion.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (Get_DB_Version() != Double.valueOf(DB_verion)) {
				nameFilePath.delete();

				FileOutputStream Fos = null;
				try {

					Fos = new FileOutputStream(nameFilePath);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				byte[] buf = new byte[3];
				try {
					Fos.write(DB_verion.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public EventDBOper() {

		Set_DB_Path();
		OpenDB();
		EndDB();
	}

	public boolean OpenDB() {

		try {
			if (D)
				Log.d(TAG, "Open EventDB");
			_db = SQLiteDatabase.openDatabase(mDB_Path, null, SQLiteDatabase.OPEN_READWRITE);
			checkDB();
		} catch (Exception e) {
			if (D)
				Log.d(TAG, "DB is not exist.");
			createDatabase(mDB_Path);
		}

		return true;
	}

	private void createDatabase(String dbfile) {

		if (D)
			Log.d(TAG, "Try to create mBluetoothImage_flag DB. ");

		_db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		_db.execSQL(DATABASE_CREATE_v1_7);
		Create_DB_VersionFile();
		_db.execSQL("create table EventCount (count integer primary key autoincrement);");
	}

	public static void Remove_SAM_Folder() {

		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER + "/" + DB_FOLDER);

		DeleteDir(dbpath.toString());

		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdir();
		}
	}

	public void Remove_EventFile() {
		if (_db != null)
			_db.close();
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER + "/" + DB_FILE_NAME + ".sql");
		dbpath.delete();
	}

	public static void DeleteDir(String path) {

		File file = new File(path);
		File[] childFileList = file.listFiles();
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				DeleteDir(childFile.getAbsolutePath());// �븯�쐞 �뵒�젆�넗由� 猷⑦봽
			} else {
				childFile.delete();// �븯�쐞 �뙆�씪�궘�젣
			}
		}
		// file.delete();//root �궘�젣
	}

	public EventData SaveEvent(Detector detector, Vector<Isotope> ID_Result, double Gamma_MAX_DR,

							   double Neutron_MAX_cps, boolean IsManual, Location gps) {

		OpenDB();
		if (D)
			Log.d(TAG, "Write on Normal DataBase File");

		Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		month += 1; // month�뒗 0�씠 1�썡
		int date = calendar.get(Calendar.DATE);
		////
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String BeginTime = hour + ":" + minute + ":" + second + "." + MSec;

		///
		long finishTime = calendar.getTimeInMillis();

		// if(mIs_ManualID){
		finishTime -= detector.MS.Get_AcqTime() * 1000;
		// }
		// else finishTime += mAccumulate_Count*1000;

		calendar.setTimeInMillis(finishTime);

		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		second = calendar.get(Calendar.SECOND);

		MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String FinishTime = hour + ":" + minute + ":" + second + "." + MSec;

		// if(mIs_ManualID){
		String Temp = "";
		Temp = BeginTime;
		BeginTime = FinishTime;
		FinishTime = Temp;
		// }
		///

		if (detector.MS.Get_AcqTime() < 3) {
			return null;
		}

		Cursor cu;
		_db = SQLiteDatabase.openOrCreateDatabase(mDB_Path, null);
		cu = _db.rawQuery("SELECT count FROM EventCount", null);

		int result = 0;
		cu.moveToFirst();

		if (cu.getCount() == 0) {
			ContentValues newValue1 = new ContentValues();
			newValue1.put("count", 1);
			_db.insert("EventCount", null, newValue1);
			result = 1;
		} else {
			result = cu.getInt(0);
			_db.delete("EventCount", "count=" + result, null);

			ContentValues newValue1 = new ContentValues();
			newValue1.put("count", result + 1);
			result += 1;
			_db.insert("EventCount", null, newValue1);
		}

		cu.close();

		EventData eventLog = new EventData();

		eventLog.mColumn_Version = DB_verion;
		eventLog.Doserate_AVG = detector.Get_Gamma_DoseRate_nSV() / detector.MS.Get_AcqTime();
		eventLog.Doserate_MAX = Gamma_MAX_DR;
		eventLog.StartTime = BeginTime;
		eventLog.EndTime = FinishTime;
		eventLog.IsManualID = detector.IsManualID;
		eventLog.EventData = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(date);

		if (detector.mNeutron.Get_CPS() <= -1)
			eventLog.Neutron_AVG = -1;
		else
			eventLog.Neutron_AVG = detector.mNeutron.Get_AvgCps();
		if (Neutron_MAX_cps <= -1)
			eventLog.Neutron_MAX = -1;
		else
			eventLog.Neutron_MAX = Neutron_MAX_cps;

		eventLog.Detected_Isotope = ID_Result;
		eventLog.Event_Detector = detector.Event_Detector;
		if (gps != null) {
			eventLog.GPS_Latitude = gps.getLatitude();
			eventLog.GPS_Longitude = gps.getLongitude();
		}

		WriteEvent_OnDatabase(eventLog);

		///////////////

		EndDB();
		return eventLog; // _db.insert(DB_TABLE, null, newValue);
	}

	public boolean WriteEvent_OnDatabase(EventData event) {

		Cursor cu;
		_db = SQLiteDatabase.openOrCreateDatabase(mDB_Path, null);
		cu = _db.rawQuery("SELECT * FROM " + DB_TABLE, null);
		int Number = cu.getCount() + 1;
		try {
			if (DB_verion.matches("1.5")) {
				ContentValues newValue = new ContentValues();
				newValue.put(ROWID, Number);
				newValue.put(DB_FORMAT_VERSION, event.mColumn_Version);
				newValue.put(INSTRUMENT_MODEL, event.mInstrument_Name);
				newValue.put(DATE, event.EventData);
				newValue.put(DATE_BG, event.BG.Get_MesurementDate());
				newValue.put(BEGIN, event.StartTime);
				newValue.put(FINISH, event.EndTime);
				newValue.put(ACQ_TIME, event.MS.Get_AcqTime());
				newValue.put(ACQ_TIME_BG, event.BG.Get_AcqTime());
				newValue.put(LOCATION, event.mLocation);
				newValue.put(AGENT, event.mUser);
				newValue.put(AVG_GAMMA, event.Doserate_AVG);
				newValue.put(AVG_NEUTRON, event.Neutron_AVG);
				newValue.put(MAX_GAMMA, event.Doserate_MAX);
				newValue.put(MAX_NEUTRON, event.Neutron_MAX);
				String PhotoS = "";
				if (event.PhotoFileName != null) {
					for (int i = 0; i < event.PhotoFileName.size(); i++) {
						PhotoS += event.PhotoFileName.get(i) + ";";
					}
				}
				newValue.put(PHOTO, PhotoS);
				newValue.put(CALIB_A, event.MS.Get_Coefficients().get_Coefficients()[0]);
				newValue.put(CALIB_B, event.MS.Get_Coefficients().get_Coefficients()[1]);
				newValue.put(CALIB_C, event.MS.Get_Coefficients().get_Coefficients()[2]);
				newValue.put(GMT, event.mGMT);
				newValue.put(LATITUDE, event.GPS_Latitude);
				newValue.put(LONGITUDE, event.GPS_Longitude);
				newValue.put(SPECTRUM, event.MS.ToString());
				newValue.put(SPECTRUM_BG, event.BG.ToString());
				newValue.put(MANUAL_ID, event.IsManualID);
				String temp = "";
				for (int i = 0; i < event.Detected_Isotope.size(); i++) {
					if (event.Detected_Isotope.get(i).Class != null) {
						if (event.Detected_Isotope.get(i).Class.matches("UNK") == false)
							temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
					} else {
						temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
					}
				}
				newValue.put(IDENTIFICATION, temp);
				// newValue.put(EVENT_DETECTOR, event.Event_Detector);
				_db.insert(DB_TABLE, null, newValue);
				newValue.clear();
			} else if (DB_verion.matches("1.6")) {
				ContentValues newValue = new ContentValues();
				newValue.put(ROWID, Number);
				newValue.put(DB_FORMAT_VERSION, event.mColumn_Version);
				newValue.put(INSTRUMENT_MODEL, event.mInstrument_Name);
				newValue.put(DATE, event.EventData);
				newValue.put(DATE_BG, event.BG.Get_MesurementDate());
				newValue.put(BEGIN, event.StartTime);
				newValue.put(FINISH, event.EndTime);
				newValue.put(ACQ_TIME, event.MS.Get_AcqTime());
				newValue.put(ACQ_TIME_BG, event.BG.Get_AcqTime());
				newValue.put(LOCATION, event.mLocation);
				newValue.put(AGENT, event.mUser);
				newValue.put(AVG_GAMMA, event.Doserate_AVG);
				newValue.put(AVG_NEUTRON, event.Neutron_AVG);
				newValue.put(MAX_GAMMA, event.Doserate_MAX);
				newValue.put(MAX_NEUTRON, event.Neutron_MAX);
				String PhotoS = "";
				if (event.PhotoFileName != null) {
					for (int i = 0; i < event.PhotoFileName.size(); i++) {
						PhotoS += event.PhotoFileName.get(i) + ";";
					}
				}
				newValue.put(PHOTO, PhotoS);
				newValue.put(CALIB_A, event.MS.Get_Coefficients().get_Coefficients()[0]);
				newValue.put(CALIB_B, event.MS.Get_Coefficients().get_Coefficients()[1]);
				newValue.put(CALIB_C, event.MS.Get_Coefficients().get_Coefficients()[2]);
				newValue.put(GMT, event.mGMT);
				newValue.put(LATITUDE, event.GPS_Latitude);
				newValue.put(LONGITUDE, event.GPS_Longitude);
				newValue.put(SPECTRUM, event.MS.ToString());
				newValue.put(SPECTRUM_BG, event.BG.ToString());
				newValue.put(MANUAL_ID, event.IsManualID);
				String temp = "";
				for (int i = 0; i < event.Detected_Isotope.size(); i++) {
					if (event.Detected_Isotope.get(i).Class != null) {
						if (event.Detected_Isotope.get(i).Class.matches("UNK") == false)
							temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
					} else {
						temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
					}
				}
				newValue.put(IDENTIFICATION, temp);
				newValue.put(EVENT_DETECTOR, event.Event_Detector);
				_db.insert(DB_TABLE, null, newValue);
				newValue.clear();
			} else if (DB_verion.matches("1.7")) {
				ContentValues values = new ContentValues();
				values.put(ROWID, Number);
				values.put(DB_FORMAT_VERSION, event.mColumn_Version);
				values.put(INSTRUMENT_MODEL, event.mInstrument_Name);
				values.put(DATE, event.EventData);
				values.put(DATE_BG, event.BG.Get_MesurementDate());
				values.put(BEGIN, event.StartTime);
				values.put(FINISH, event.EndTime);
				values.put(ACQ_TIME, event.MS.Get_AcqTime());
				values.put(REAL_ACQ_TIME, event.MS.Get_SystemElapsedTime().getTime() * 0.001);
				values.put(ACQ_TIME_BG, event.BG.Get_AcqTime());
				values.put(REAL_ACQ_TIME_BG, event.BG.Get_SystemElapsedTime().getTime() * 0.001);
				values.put(LOCATION, event.mLocation);
				values.put(AGENT, event.mUser);
				values.put(FILL_CPS_AVG, Integer.toString(event.MS.GetAvgFillCps()));
				values.put(AVG_GAMMA, NcLibrary.SvToString(event.Doserate_AVG, true,
						(event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false));
				values.put(AVG_NEUTRON, String.valueOf(event.Neutron_AVG + " cps"));
				values.put(MAX_GAMMA, NcLibrary.SvToString(event.Doserate_MAX, true,
						(event.Doserate_unit == Detector.DR_UNIT_SV) ? true : false));
				values.put(MAX_NEUTRON, String.valueOf(event.Neutron_MAX + " cps"));
				String PhotoS = "";
				if (event.PhotoFileName != null) {
					for (int i = 0; i < event.PhotoFileName.size(); i++) {
						PhotoS += event.PhotoFileName.get(i) + ";";
					}
				}
				String VideoS = "";
				if (event.VedioFileName != null) {
					for (int i = 0; i < event.VedioFileName.size(); i++) {
						VideoS += event.VedioFileName.get(i) + ";";
					}
				}

				values.put(VIDEO, VideoS);

				String RecodeS = "";
				if (event.RecodeFileName != null) {
					for (int i = 0; i < event.RecodeFileName.size(); i++) {
						RecodeS += event.RecodeFileName.get(i) + ";";
					}
				}

				values.put(RECODE, RecodeS);

				values.put(PHOTO, PhotoS);

				values.put(FAVORITE, event.Favorite_Checked);

				values.put(CALIB_A, event.MS.Get_Coefficients().get_Coefficients()[0]);
				values.put(CALIB_B, event.MS.Get_Coefficients().get_Coefficients()[1]);
				values.put(CALIB_C, event.MS.Get_Coefficients().get_Coefficients()[2]);
				values.put(GMT, event.mGMT);
				values.put(LATITUDE, event.GPS_Latitude);
				values.put(LONGITUDE, event.GPS_Longitude);
				values.put(SPECTRUM, event.MS.ToString());
				values.put(SPECTRUM_BG, event.BG.ToString());
				values.put(MANUAL_ID, event.IsManualID);
				String temp = "";
				if(event.Detected_Isotope!=null){
					for (int i = 0; i < event.Detected_Isotope.size(); i++) {
						if (event.Detected_Isotope.get(i).Class != null) {
							if (event.Detected_Isotope.get(i).Class.matches("UNK") == false)
								temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
						} else {
							temp += event.Detected_Isotope.get(i).Get_Result_OnlyDB(DB_verion);
						}
					}
				}
				values.put(IDENTIFICATION, temp);
				values.put(EVENT_DETECTOR, event.Event_Detector);

				//values.put(RECODE, RecodeS);

				values.put(COMMENT, event.Comment);
				//_db.insert(DB_TABLE, null, values);

					_db.insertOrThrow(DB_TABLE, null, values);



				values.clear();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void setHandler(Handler Handle) {
		mHandle = Handle;
	}

	public String Get_ColumnData_inEvent(String ColumnName, int ColumnNumber) {
		OpenDB();
		Cursor cu;
		if (IsThere_Column(ColumnName) == false) {
			EndDB();
			return "None";
		}
		cu = _db.rawQuery("SELECT " + ColumnName + " FROM Event", null);

		String result = "null";
		cu.move(ColumnNumber);

		if (cu.getCount() != 0 | cu.getCount() > ColumnNumber) {
			result = cu.getString(0);
			cu.close();
			EndDB();
			return result;
		} else {
			cu.close();
			EndDB();
			return result;
		}
	}

	private boolean IsThere_Column(String ColumnName) {
		Cursor cu = _db.rawQuery("SELECT * FROM Event", null);
		String columnNames[] = cu.getColumnNames();
		boolean Result = false;

		for (int i = 0; i < columnNames.length; i++) {
			if (columnNames[i].matches(ColumnName))
				Result = true;
		}

		return Result;
	}

	public void Set_Comment(String Comment, int ColumnNumber) {
		OpenDB();
		Cursor cu;
		cu = _db.rawQuery("SELECT " + COMMENT + " FROM Event", null);

		int PmKey = 0;
		cu.move(ColumnNumber);

		if (cu.getCount() != 0 | cu.getCount() > ColumnNumber) {
			PmKey = cu.getInt(0);
			cu.close();
		} else {
			cu.close();
			EndDB();
			return;
		}
		PmKey += 1;
		String temp = "update Event set " + COMMENT + " = '" + Comment + "' where " + ROWID + " = " + (ColumnNumber)
				+ ";";
		_db.execSQL(temp);
		// _db.rawQuery(temp,null);

		cu.close();
		EndDB();
	}

	public void Update_RecoderFileNames(String Comment, int ColumnNumber) {

		String PhotoName = Get_ColumnData_inEvent(RECODE, ColumnNumber - 1);
		if (PhotoName == null)
			PhotoName = "";

		OpenDB();
		Cursor cu;
		cu = _db.rawQuery("SELECT " + RECODE + " FROM Event", null);

		int PmKey = 0;
		cu.move(ColumnNumber);

		if (cu.getCount() != 0 | cu.getCount() > ColumnNumber) {
			PmKey = cu.getInt(0);
			cu.close();
		} else {
			cu.close();
			EndDB();
			return;
		}
		PmKey += 1;
		String temp = "update Event set " + RECODE + " = '" + PhotoName + Comment + ";' where " + ROWID + " = "
				+ (ColumnNumber) + ";";
		_db.execSQL(temp);
		// _db.rawQuery(temp,null);

		cu.close();
		EndDB();
	}

	public void Update_PhotoFileNames(String Comment, int ColumnNumber) {

		String PhotoName = Get_ColumnData_inEvent(PHOTO, ColumnNumber - 1);
		if (PhotoName == null)
			PhotoName = "";

		OpenDB();
		Cursor cu;
		cu = _db.rawQuery("SELECT " + PHOTO + " FROM Event", null);

		int PmKey = 0;
		cu.move(ColumnNumber);

		if (cu.getCount() != 0 | cu.getCount() > ColumnNumber) {
			PmKey = cu.getInt(0);
			cu.close();
		} else {
			cu.close();
			EndDB();
			return;
		}
		PmKey += 1;
		String temp = "update Event set " + PHOTO + " = '" + PhotoName + Comment + ";' where " + ROWID + " = "
				+ (ColumnNumber) + ";";
		_db.execSQL(temp);
		// _db.rawQuery(temp,null);

		cu.close();
		EndDB();
	}

	public void Update_VideoFileNames(String Comment, int ColumnNumber) {

		String PhotoName = Get_ColumnData_inEvent(VIDEO, ColumnNumber - 1);
		if (PhotoName == null)
			PhotoName = "";

		OpenDB();
		Cursor cu;
		cu = _db.rawQuery("SELECT " + VIDEO + " FROM Event", null);

		int PmKey = 0;
		cu.move(ColumnNumber);

		if (cu.getCount() != 0 | cu.getCount() > ColumnNumber) {
			PmKey = cu.getInt(0);
			cu.close();
		} else {
			cu.close();
			EndDB();
			return;
		}
		PmKey += 1;
		String temp = "update Event set " + VIDEO + " = '" + PhotoName + Comment + ";' where " + ROWID + " = "
				+ (ColumnNumber) + ";";
		_db.execSQL(temp);
		// _db.rawQuery(temp,null);

		cu.close();
		EndDB();
	}

	public Vector<Isotope> Get_Isotope(int EventNum) {
		Vector<Isotope> Result = new Vector<Isotope>();

		String temp = "";
		temp = Get_ColumnData_inEvent(IDENTIFICATION, EventNum);

		///
		if (temp.matches(""))
			return Result;

		Vector<Integer> temp2 = new Vector<Integer>();
		for (int i = 0; i < temp.length(); i++) {
			if (temp.charAt(i) == ';') {
				temp2.add(i);

			}
		}

		Vector<String> StringTemp = new Vector<String>();

		int F = 0;
		int L = temp2.get(0);
		char[] buf = new char[L - F];
		temp.getChars(F, L, buf, 0);

		StringTemp.add(String.valueOf(buf));
		for (int i = 0; i < temp2.size() - 1; i++) {
			F = temp2.get(i) + 1;
			L = temp2.get(i + 1);
			char[] buf2 = new char[L - F];
			temp.getChars(temp2.get(i) + 1, temp2.get(i + 1), buf2, 0);
			StringTemp.add(String.valueOf(buf2));
		}

		for (int i = 0; i < StringTemp.size(); i += 3) {
			Isotope iso = new Isotope();
			iso.isotopes = StringTemp.get(i);
			iso.Confidence_Level = Integer.valueOf(StringTemp.get(i + 1).replace("%", ""));

			String doserate = "";
			for (int k1 = 0; k1 < StringTemp.get(i + 2).length(); k1++) {
				if (StringTemp.get(i + 2).charAt(k1) != ' ') {
					doserate += StringTemp.get(i + 2).charAt(k1);
				} else {
					break;
				}
			}
			if (doserate != "")
				iso.DoseRate = Double.valueOf(doserate);
			if (StringTemp.get(i + 1).matches("rem")) {

			}

			Result.add(iso);
		}

		return Result;
	}

	public Vector<String> Get_RecoderFileName(int EventNum) {
		Vector<String> Result = new Vector<String>();

		String FileN = Get_ColumnData_inEvent(RECODE, EventNum);
		if (FileN == null || FileN.equals("None"))
			return Result;

		Vector<Integer> temp2 = new Vector<Integer>();
		for (int i = 0; i < FileN.length(); i++) {
			if (FileN.charAt(i) == ';')
				temp2.add(i);
		}

		if (temp2 == null | FileN.length() == 0)
			return Result;
		int F = 0;
		int L = temp2.get(0);
		char[] buf = new char[L - F];
		FileN.getChars(F, L, buf, 0);

		Result.add(String.valueOf(buf));
		for (int i = 0; i < temp2.size() - 1; i++) {
			F = temp2.get(i) + 1;
			L = temp2.get(i + 1);
			char[] buf2 = new char[L - F];
			FileN.getChars(temp2.get(i) + 1, temp2.get(i + 1), buf2, 0);
			Result.add(String.valueOf(buf2));

		}

		return Result;
	}

	public Vector<String> Get_PhotoFileName(int EventNum) {

		Vector<String> Result = new Vector<String>();

		String FileN = Get_ColumnData_inEvent(PHOTO, EventNum);
		if (FileN == null || FileN.equals("None"))
			return Result;

		Vector<Integer> temp2 = new Vector<Integer>();
		for (int i = 0; i < FileN.length(); i++) {
			if (FileN.charAt(i) == ';')
				temp2.add(i);
		}

		if (temp2 == null | FileN.length() == 0)
			return Result;
		int F = 0;
		int L = temp2.get(0);
		char[] buf = new char[L - F];
		FileN.getChars(F, L, buf, 0);

		Result.add(String.valueOf(buf));
		for (int i = 0; i < temp2.size() - 1; i++) {
			F = temp2.get(i) + 1;
			L = temp2.get(i + 1);
			char[] buf2 = new char[L - F];
			FileN.getChars(temp2.get(i) + 1, temp2.get(i + 1), buf2, 0);
			Result.add(String.valueOf(buf2));

		}

		return Result;
	}

	public int GetEventCount() {
		OpenDB();
		Cursor cu;
		_db = SQLiteDatabase.openOrCreateDatabase(mDB_Path, null);
		cu = _db.rawQuery("SELECT " + DATE + " FROM " + DB_TABLE, null);

		cu.moveToFirst();

		int count = cu.getCount();
		cu.close();

		// cu = _db.rawQuery("SELECT count FROM EventCount",null);
		EndDB();
		return count;
	}

	public void setBegin(String InCharge, String Location, String Detector) {

		Calendar calendar = Calendar.getInstance();

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		mBegin = Calendar.getInstance();

	}

	public EventData Load_One_Event(int pos) {

		try {

			Vector<EventData> Result = new Vector<EventData>();
			OpenDB();
			Cursor cu = _db.rawQuery("SELECT * FROM " + DB_TABLE, null);
			// String ColumnList[] = Make_DB_ColumnList(Get_DB_Version());
			cu.moveToPosition(pos);
			if (cu.getCount() == 0)
				return null;
			/// ------------------
			EventData event;

			event = new EventData();
			if (Get_DB_Version() == 1.4) {
				event.Event_Number = cu.getInt(0);
				event.mInstrument_Name = "None";
				event.mColumn_Version = DB_verion;
				event.BG.Set_MeasurementDate("None");
				if (!cu.isNull(1))
					event.EventData = cu.getString(1);
				if (!cu.isNull(2))
					event.StartTime = cu.getString(2);
				if (!cu.isNull(3))
					event.EndTime = cu.getString(3);
				if (!cu.isNull(4))
					event.mLocation = cu.getString(4);
				if (!cu.isNull(5))
					event.mUser = cu.getString(5);
				if (!cu.isNull(6))
					event.Doserate_AVG = cu.getFloat(6);
				event.Neutron_AVG = -1;
				if (!cu.isNull(8))
					event.Doserate_MAX = cu.getFloat(8);
				event.Neutron_MAX = -1;
				if (!cu.isNull(13))
					event.Comment = cu.getString(13);
				if (!cu.isNull(14))
					event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(14));
				if (!cu.isNull(15))
					event.mGMT = cu.getString(15);

				double ABC[] = new double[3];
				if (!cu.isNull(16))
					ABC[0] = cu.getDouble(16);
				if (!cu.isNull(17))
					ABC[1] = cu.getDouble(17);
				if (!cu.isNull(18))
					ABC[2] = cu.getDouble(18);

				event.MS.Set_Coefficients(ABC);

				if (!cu.isNull(19))
					event.GPS_Longitude = cu.getDouble(19);
				if (!cu.isNull(20))
					event.GPS_Latitude = cu.getDouble(20);
				if (!cu.isNull(21))
					event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(21), true), cu.getInt(11));
				if (!cu.isNull(22))
					event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(22), true), cu.getInt(12));

				if (!cu.isNull(23))
					event.IsManualID = (cu.getInt(23) == 0) ? false : true;

				String temp = cu.getString(24);
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp);
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i += 3) {
					Isotope tt = new Isotope();
					tt.isotopes = IsoTemp.get(i);

					tt.Confidence_Level = Integer.valueOf(IsoTemp.get(i + 1).replace("%", ""));

					// tt.Confidence_Level =
					// IsoTemp.get(i+1);//Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0));
					tt.DoseRate_S = IsoTemp.get(i + 2);// (Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+2),'
					// ').get(0)))*1000;
					IsoData.add(tt);
				}
				event.Detected_Isotope = IsoData;

			} else if (Get_DB_Version() == 1.5) {
				event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
				event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.mInstrument_Name = event.mInstrument_Name;
				event.EventData = cu.getString(cu.getColumnIndex(DATE));
				event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

				event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
				event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
				event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
				event.mUser = cu.getString(cu.getColumnIndex(AGENT));
				event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
				event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
				event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
				event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
				event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
				event.mGMT = cu.getString(cu.getColumnIndex(GMT));

				double ABC[] = new double[3];
				ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
				ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
				ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

				event.MS.Set_Coefficients(ABC);

				event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
				event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME)));
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

				event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

				String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i++) {
					Isotope iso = new Isotope();
					iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
					IsoData.add(iso);
				}
				/*
				 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
				 * = IsoTemp.get(i); tt.Confidence_Level =
				 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
				 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
				 */
				event.Detected_Isotope = IsoData;
			} else if (Get_DB_Version() == 1.6) {

				event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
				event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.EventData = cu.getString(cu.getColumnIndex(DATE));
				event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

				event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
				event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
				event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
				event.mUser = cu.getString(cu.getColumnIndex(AGENT));
				event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
				event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
				event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
				event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
				event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
				event.mGMT = cu.getString(cu.getColumnIndex(GMT));

				double ABC[] = new double[3];
				ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
				ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
				ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

				event.MS.Set_Coefficients(ABC);

				event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
				event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME)));
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

				event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

				String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i++) {
					Isotope iso = new Isotope();
					iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
					IsoData.add(iso);
				}
				/*
				 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
				 * = IsoTemp.get(i); tt.Confidence_Level =
				 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
				 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
				 */
				event.Detected_Isotope = IsoData;
				event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
			} else if (Get_DB_Version() == 1.7) {

				event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
				event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.EventData = cu.getString(cu.getColumnIndex(DATE));
				event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

				event.AvgFillCps = Integer.parseInt(cu.getString(cu.getColumnIndex(FILL_CPS_AVG)));

				event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
				event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
				event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
				event.mUser = cu.getString(cu.getColumnIndex(AGENT));
				event.Doserate_AVGs = cu.getString(cu.getColumnIndex(AVG_GAMMA));
				event.Neutron_AVGs = cu.getString(cu.getColumnIndex(AVG_NEUTRON));
				event.Doserate_MAXs = cu.getString(cu.getColumnIndex(MAX_GAMMA));
				event.Neutron_MAXs = cu.getString(cu.getColumnIndex(MAX_NEUTRON));
				event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
				event.mGMT = cu.getString(cu.getColumnIndex(GMT));
				event.Favorite_Checked = cu.getString(cu.getColumnIndex(FAVORITE));

				double ABC[] = new double[3];
				ABC[0] = cu.getDouble(cu.getColumnIndex(CALIB_A));
				ABC[1] = cu.getDouble(cu.getColumnIndex(CALIB_B));
				ABC[2] = cu.getDouble(cu.getColumnIndex(CALIB_C));

				event.MS.Set_Coefficients(ABC);
				event.BG.Set_Coefficients(ABC);

				event.GPS_Longitude = cu.getDouble(cu.getColumnIndex(LONGITUDE));
				event.GPS_Latitude = cu.getDouble(cu.getColumnIndex(LATITUDE));
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME)));
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));
				event.MS.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME)) * 1000)));
				event.BG.Set_StartSystemTime(
						new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME_BG)) * 1000)));

				event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

				String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i++) {
					Isotope iso = new Isotope();
					iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
					IsoData.add(iso);
				}
				/*
				 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
				 * = IsoTemp.get(i); tt.Confidence_Level =
				 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
				 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
				 */
				event.Detected_Isotope = IsoData;
				event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
			}
			// Result.add(event);
			// if (cu.isLast() == true)
			// break;
			// cu.moveToNext();

			cu.close();
			EndDB();
			return event;
		} catch (IllegalStateException ex) {
			// TODO: handle exception
		}
		return null;
	}

	public Vector<EventData> Load_ALL_Event() {

		try {

			Vector<EventData> Result = new Vector<EventData>();
			OpenDB();
			Cursor cu = _db.rawQuery("SELECT * FROM " + DB_TABLE, null);
			// String ColumnList[] = Make_DB_ColumnList(Get_DB_Version());
			cu.moveToFirst();
			if (cu.getCount() == 0)
				return null;
			/// ------------------
			while (true) {
				EventData event = new EventData();
				if (Get_DB_Version() == 1.4) {
					event.Event_Number = cu.getInt(0);
					event.mInstrument_Name = "None";
					event.mColumn_Version = DB_verion;
					event.BG.Set_MeasurementDate("None");
					if (!cu.isNull(1))
						event.EventData = cu.getString(1);
					if (!cu.isNull(2))
						event.StartTime = cu.getString(2);
					if (!cu.isNull(3))
						event.EndTime = cu.getString(3);
					if (!cu.isNull(4))
						event.mLocation = cu.getString(4);
					if (!cu.isNull(5))
						event.mUser = cu.getString(5);
					if (!cu.isNull(6))
						event.Doserate_AVG = cu.getFloat(6);
					event.Neutron_AVG = -1;
					if (!cu.isNull(8))
						event.Doserate_MAX = cu.getFloat(8);
					event.Neutron_MAX = -1;
					if (!cu.isNull(13))
						event.Comment = cu.getString(13);
					if (!cu.isNull(14))
						event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(14));
					if (!cu.isNull(15))
						event.mGMT = cu.getString(15);

					double ABC[] = new double[3];
					if (!cu.isNull(16))
						ABC[0] = cu.getDouble(16);
					if (!cu.isNull(17))
						ABC[1] = cu.getDouble(17);
					if (!cu.isNull(18))
						ABC[2] = cu.getDouble(18);

					event.MS.Set_Coefficients(ABC);

					if (!cu.isNull(19))
						event.GPS_Longitude = cu.getDouble(19);
					if (!cu.isNull(20))
						event.GPS_Latitude = cu.getDouble(20);
					if (!cu.isNull(21))
						event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(21), true), cu.getInt(11));
					if (!cu.isNull(22))
						event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(22), true), cu.getInt(12));

					if (!cu.isNull(23))
						event.IsManualID = (cu.getInt(23) == 0) ? false : true;

					String temp = cu.getString(24);
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp);
					Vector<Isotope> IsoData = new Vector<Isotope>();
					for (int i = 0; i < IsoTemp.size(); i += 3) {
						Isotope tt = new Isotope();
						tt.isotopes = IsoTemp.get(i);

						tt.Confidence_Level = Integer.valueOf(IsoTemp.get(i + 1).replace("%", ""));

						// tt.Confidence_Level =
						// IsoTemp.get(i+1);//Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0));
						tt.DoseRate_S = IsoTemp.get(i + 2);// (Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+2),'
						// ').get(0)))*1000;
						IsoData.add(tt);
					}
					event.Detected_Isotope = IsoData;

				} else if (Get_DB_Version() == 1.5) {
					event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
					event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
					event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
					event.mInstrument_Name = event.mInstrument_Name;
					event.EventData = cu.getString(cu.getColumnIndex(DATE));
					event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

					event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
					event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
					event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
					event.mUser = cu.getString(cu.getColumnIndex(AGENT));
					event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
					event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
					event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
					event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
					event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
					event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
					event.mGMT = cu.getString(cu.getColumnIndex(GMT));

					double ABC[] = new double[3];
					ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
					ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
					ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

					event.MS.Set_Coefficients(ABC);

					event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
					event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
					event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
							cu.getInt(cu.getColumnIndex(ACQ_TIME)));
					event.BG.Set_Spectrum(
							NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
							cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

					event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

					String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
					Vector<Isotope> IsoData = new Vector<Isotope>();
					for (int i = 0; i < IsoTemp.size(); i++) {
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						IsoData.add(iso);
					}
					/*
					 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
					 * = IsoTemp.get(i); tt.Confidence_Level =
					 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
					 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
					 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
					 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
					 */
					event.Detected_Isotope = IsoData;
				} else if (Get_DB_Version() == 1.6) {

					event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
					event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
					event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
					event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
					event.EventData = cu.getString(cu.getColumnIndex(DATE));
					event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

					event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
					event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
					event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
					event.mUser = cu.getString(cu.getColumnIndex(AGENT));
					event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
					event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
					event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
					event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
					event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
					event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
					event.mGMT = cu.getString(cu.getColumnIndex(GMT));

					double ABC[] = new double[3];
					ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
					ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
					ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

					event.MS.Set_Coefficients(ABC);

					event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
					event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
					event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
							cu.getInt(cu.getColumnIndex(ACQ_TIME)));
					event.BG.Set_Spectrum(
							NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
							cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

					event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

					String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
					Vector<Isotope> IsoData = new Vector<Isotope>();
					for (int i = 0; i < IsoTemp.size(); i++) {
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						IsoData.add(iso);
					}
					/*
					 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
					 * = IsoTemp.get(i); tt.Confidence_Level =
					 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
					 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
					 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
					 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
					 */
					event.Detected_Isotope = IsoData;
					event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
				} else if (Get_DB_Version() == 1.7) {

					event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
					event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
					event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
					event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
					event.EventData = cu.getString(cu.getColumnIndex(DATE));
					event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

					event.AvgFillCps = Integer.parseInt(cu.getString(cu.getColumnIndex(FILL_CPS_AVG)));

					event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
					event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
					event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
					event.mUser = cu.getString(cu.getColumnIndex(AGENT));
					event.Doserate_AVGs = cu.getString(cu.getColumnIndex(AVG_GAMMA));
					event.Neutron_AVGs = cu.getString(cu.getColumnIndex(AVG_NEUTRON));
					event.Doserate_MAXs = cu.getString(cu.getColumnIndex(MAX_GAMMA));
					event.Neutron_MAXs = cu.getString(cu.getColumnIndex(MAX_NEUTRON));
					event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
					event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
					event.mGMT = cu.getString(cu.getColumnIndex(GMT));
					event.Favorite_Checked = cu.getString(cu.getColumnIndex(FAVORITE));

					double ABC[] = new double[3];
					ABC[0] = cu.getDouble(cu.getColumnIndex(CALIB_A));
					ABC[1] = cu.getDouble(cu.getColumnIndex(CALIB_B));
					ABC[2] = cu.getDouble(cu.getColumnIndex(CALIB_C));

					event.MS.Set_Coefficients(ABC);
					event.BG.Set_Coefficients(ABC);

					event.GPS_Longitude = cu.getDouble(cu.getColumnIndex(LONGITUDE));
					event.GPS_Latitude = cu.getDouble(cu.getColumnIndex(LATITUDE));
					event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
							cu.getInt(cu.getColumnIndex(ACQ_TIME)));
					event.BG.Set_Spectrum(
							NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
							cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));
					event.MS.Set_StartSystemTime(
							new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME)) * 1000)));
					event.BG.Set_StartSystemTime(
							new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME_BG)) * 1000)));

					event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

					String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
					Vector<Isotope> IsoData = new Vector<Isotope>();
					for (int i = 0; i < IsoTemp.size(); i++) {
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						IsoData.add(iso);
					}
					/*
					 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
					 * = IsoTemp.get(i); tt.Confidence_Level =
					 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
					 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
					 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
					 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
					 */
					event.Detected_Isotope = IsoData;
					event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
				}
				Result.add(event);
				if (cu.isLast() == true)
					break;
				cu.moveToNext();
			}

			cu.close();
			EndDB();
			return Result;
		} catch (IllegalStateException ex) {
			// TODO: handle exception
		}
		return null;
	}

	public Vector<EventData> Load_One_Event() {

		try {
			Vector<EventData> Result = new Vector<EventData>();
			OpenDB();
			Cursor cu = _db.rawQuery("SELECT * FROM " + DB_TABLE, null);
			// String ColumnList[] = Make_DB_ColumnList(Get_DB_Version());
			cu.moveToFirst();
			if (cu.getCount() == 0)
				return null;
			/// ------------------

			EventData event = new EventData();
			if (Get_DB_Version() == 1.4) {
				event.Event_Number = cu.getInt(0);
				event.mInstrument_Name = "None";
				event.mColumn_Version = DB_verion;
				event.BG.Set_MeasurementDate("None");
				if (!cu.isNull(1))
					event.EventData = cu.getString(1);
				if (!cu.isNull(2))
					event.StartTime = cu.getString(2);
				if (!cu.isNull(3))
					event.EndTime = cu.getString(3);
				if (!cu.isNull(4))
					event.mLocation = cu.getString(4);
				if (!cu.isNull(5))
					event.mUser = cu.getString(5);
				if (!cu.isNull(6))
					event.Doserate_AVG = cu.getFloat(6);
				event.Neutron_AVG = -1;
				if (!cu.isNull(8))
					event.Doserate_MAX = cu.getFloat(8);
				event.Neutron_MAX = -1;
				if (!cu.isNull(13))
					event.Comment = cu.getString(13);
				if (!cu.isNull(14))
					event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(14));
				if (!cu.isNull(15))
					event.mGMT = cu.getString(15);

				double ABC[] = new double[3];
				if (!cu.isNull(16))
					ABC[0] = cu.getDouble(16);
				if (!cu.isNull(17))
					ABC[1] = cu.getDouble(17);
				if (!cu.isNull(18))
					ABC[2] = cu.getDouble(18);

				event.MS.Set_Coefficients(ABC);

				if (!cu.isNull(19))
					event.GPS_Longitude = cu.getDouble(19);
				if (!cu.isNull(20))
					event.GPS_Latitude = cu.getDouble(20);
				if (!cu.isNull(21))
					event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(21), true), cu.getInt(11));
				if (!cu.isNull(22))
					event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(22), true), cu.getInt(12));

				if (!cu.isNull(23))
					event.IsManualID = (cu.getInt(23) == 0) ? false : true;

				String temp = cu.getString(24);
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp);
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i += 3) {
					Isotope tt = new Isotope();
					tt.isotopes = IsoTemp.get(i);

					tt.Confidence_Level = Integer.valueOf(IsoTemp.get(i + 1).replace("%", ""));

					// tt.Confidence_Level =
					// IsoTemp.get(i+1);//Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0));
					tt.DoseRate_S = IsoTemp.get(i + 2);// (Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+2),'
					// ').get(0)))*1000;
					IsoData.add(tt);
				}
				event.Detected_Isotope = IsoData;

			} else if (Get_DB_Version() == 1.5) {
				event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
				event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.mInstrument_Name = event.mInstrument_Name;
				event.EventData = cu.getString(cu.getColumnIndex(DATE));
				event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

				event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
				event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
				event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
				event.mUser = cu.getString(cu.getColumnIndex(AGENT));
				event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
				event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
				event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
				event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
				event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
				event.mGMT = cu.getString(cu.getColumnIndex(GMT));

				double ABC[] = new double[3];
				ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
				ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
				ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

				event.MS.Set_Coefficients(ABC);

				event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
				event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME)));
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

				event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

				String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i++) {
					Isotope iso = new Isotope();
					iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
					IsoData.add(iso);
				}
				/*
				 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
				 * = IsoTemp.get(i); tt.Confidence_Level =
				 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
				 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
				 */
				event.Detected_Isotope = IsoData;
			} else if (Get_DB_Version() == 1.6) {

				event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
				event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.EventData = cu.getString(cu.getColumnIndex(DATE));
				event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

				event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
				event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
				event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
				event.mUser = cu.getString(cu.getColumnIndex(AGENT));
				event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
				event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
				event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
				event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
				event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
				event.mGMT = cu.getString(cu.getColumnIndex(GMT));

				double ABC[] = new double[3];
				ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
				ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
				ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

				event.MS.Set_Coefficients(ABC);

				event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
				event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME)));
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

				event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

				String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i++) {
					Isotope iso = new Isotope();
					iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
					IsoData.add(iso);
				}
				/*
				 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
				 * = IsoTemp.get(i); tt.Confidence_Level =
				 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
				 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
				 */
				event.Detected_Isotope = IsoData;
				event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
			} else if (Get_DB_Version() == 1.7) {

				event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
				event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
				event.EventData = cu.getString(cu.getColumnIndex(DATE));
				event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

				event.AvgFillCps = Integer.parseInt(cu.getString(cu.getColumnIndex(FILL_CPS_AVG)));

				event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
				event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
				event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
				event.mUser = cu.getString(cu.getColumnIndex(AGENT));
				event.Doserate_AVGs = cu.getString(cu.getColumnIndex(AVG_GAMMA));
				event.Neutron_AVGs = cu.getString(cu.getColumnIndex(AVG_NEUTRON));
				event.Doserate_MAXs = cu.getString(cu.getColumnIndex(MAX_GAMMA));
				event.Neutron_MAXs = cu.getString(cu.getColumnIndex(MAX_NEUTRON));
				event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
				event.mGMT = cu.getString(cu.getColumnIndex(GMT));
				event.Favorite_Checked = cu.getString(cu.getColumnIndex(FAVORITE));

				double ABC[] = new double[3];
				ABC[0] = cu.getDouble(cu.getColumnIndex(CALIB_A));
				ABC[1] = cu.getDouble(cu.getColumnIndex(CALIB_B));
				ABC[2] = cu.getDouble(cu.getColumnIndex(CALIB_C));

				event.MS.Set_Coefficients(ABC);

				event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
				event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME)));
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
						cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));
				event.MS.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME)) * 1000)));
				event.BG.Set_StartSystemTime(
						new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME_BG)) * 1000)));

				event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

				String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
				Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
				Vector<Isotope> IsoData = new Vector<Isotope>();
				for (int i = 0; i < IsoTemp.size(); i++) {
					Isotope iso = new Isotope();
					iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
					IsoData.add(iso);
				}
				/*
				 * for(int i=0; i<IsoTemp.size(); i+=3){ Isotope tt = new Isotope(); tt.isotopes
				 * = IsoTemp.get(i); tt.Confidence_Level =
				 * IsoTemp.get(i+1);//Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0)); tt.DoseRate_S =
				 * IsoTemp.get(i+2);//(Double.valueOf(NcLibrary.
				 * Separate_EveryDash2(IsoTemp.get(i+2),' ').get(0)))*1000; IsoData.add(tt); }
				 */
				event.Detected_Isotope = IsoData;
				event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
			}
			Result.add(event);

			cu.close();
			EndDB();
			return Result;
		} catch (IllegalStateException e) {
			// TODO: handle exception
		}
		return null;
	}

	public EventData Load_Event(int EventIndex) {

		OpenDB();
		Cursor cu = _db.rawQuery("SELECT * FROM " + DB_TABLE, null);
		cu.moveToFirst();
		if (cu.getCount() == 0)
			return null;
		if (cu.getCount() < EventIndex)
			return null;
		cu.move(EventIndex);
		/// ------------------

		EventData event = new EventData();

		if (DB_verion.matches("1.4")) {
			event.Event_Number = cu.getInt(0);
			event.mInstrument_Name = "None";
			event.mColumn_Version = DB_verion;
			event.BG.Set_MeasurementDate("None");
			if (!cu.isNull(1))
				event.EventData = cu.getString(1);
			if (!cu.isNull(2))
				event.StartTime = cu.getString(2);
			if (!cu.isNull(3))
				event.EndTime = cu.getString(3);
			if (!cu.isNull(4))
				event.mLocation = cu.getString(4);
			if (!cu.isNull(5))
				event.mUser = cu.getString(5);
			if (!cu.isNull(6))
				event.Doserate_AVG = cu.getFloat(6);
			event.Neutron_AVG = -1;
			if (!cu.isNull(8))
				event.Doserate_MAX = cu.getFloat(8);
			event.Neutron_MAX = -1;
			if (!cu.isNull(13))
				event.Comment = cu.getString(13);
			if (!cu.isNull(14))
				event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(14));
			if (!cu.isNull(15))
				event.mGMT = cu.getString(15);

			double ABC[] = new double[3];
			if (!cu.isNull(16))
				ABC[0] = cu.getDouble(16);
			if (!cu.isNull(17))
				ABC[1] = cu.getDouble(17);
			if (!cu.isNull(18))
				ABC[2] = cu.getDouble(18);

			event.MS.Set_Coefficients(ABC);

			if (!cu.isNull(19))
				event.GPS_Longitude = cu.getDouble(19);
			if (!cu.isNull(20))
				event.GPS_Latitude = cu.getDouble(20);
			if (!cu.isNull(21))
				event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(21), true), cu.getInt(11));
			if (!cu.isNull(22))
				event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(22), true), cu.getInt(12));

			if (!cu.isNull(23))
				event.IsManualID = (cu.getInt(23) == 0) ? false : true;

			String temp = cu.getString(24);
			Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp);
			Vector<Isotope> IsoData = new Vector<Isotope>();
			for (int i = 0; i < IsoTemp.size(); i += 3) {
				Isotope tt = new Isotope();
				tt.isotopes = IsoTemp.get(i);

				tt.Confidence_Level = Integer.valueOf(IsoTemp.get(i + 1).replace("%", ""));
				// tt.Confidence_Level =
				// IsoTemp.get(i+1);//Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+1),'%').get(0));
				tt.DoseRate_S = IsoTemp.get(i + 2);// (Double.valueOf(NcLibrary.Separate_EveryDash2(IsoTemp.get(i+2),'
				// ').get(0)))*1000;
				IsoData.add(tt);
			}
			event.Detected_Isotope = IsoData;

		}
		if (DB_verion.matches("1.5")) {
			event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
			event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
			event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
			event.EventData = cu.getString(cu.getColumnIndex(DATE));
			event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

			event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
			event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
			event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
			event.mUser = cu.getString(cu.getColumnIndex(AGENT));
			event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
			event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
			event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
			event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
			event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
			event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
			event.mGMT = cu.getString(cu.getColumnIndex(GMT));

			double ABC[] = new double[3];
			ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
			ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
			ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

			event.MS.Set_Coefficients(ABC);

			event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
			event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
			event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
					cu.getInt(cu.getColumnIndex(ACQ_TIME)));
			event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
					cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

			event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

			String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
			Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
			Vector<Isotope> IsoData = new Vector<Isotope>();
			for (int i = 0; i < IsoTemp.size(); i++) {
				Isotope iso = new Isotope();
				iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
				IsoData.add(iso);
			}
			event.Detected_Isotope = IsoData;

		}
		if (DB_verion.matches("1.6")) {
			event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
			event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
			event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
			event.EventData = cu.getString(cu.getColumnIndex(DATE));
			event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

			event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
			event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
			event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
			event.mUser = cu.getString(cu.getColumnIndex(AGENT));
			event.Doserate_AVG = cu.getFloat(cu.getColumnIndex(AVG_GAMMA));
			event.Neutron_AVG = cu.getFloat(cu.getColumnIndex(AVG_NEUTRON));
			event.Doserate_MAX = cu.getFloat(cu.getColumnIndex(MAX_GAMMA));
			event.Neutron_MAX = cu.getFloat(cu.getColumnIndex(MAX_NEUTRON));
			event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
			event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
			event.mGMT = cu.getString(cu.getColumnIndex(GMT));

			double ABC[] = new double[3];
			ABC[0] = cu.getFloat(cu.getColumnIndex(CALIB_A));
			ABC[1] = cu.getFloat(cu.getColumnIndex(CALIB_B));
			ABC[2] = cu.getFloat(cu.getColumnIndex(CALIB_C));

			event.MS.Set_Coefficients(ABC);

			event.GPS_Longitude = cu.getFloat(cu.getColumnIndex(LONGITUDE));
			event.GPS_Latitude = cu.getFloat(cu.getColumnIndex(LATITUDE));
			event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
					cu.getInt(cu.getColumnIndex(ACQ_TIME)));
			event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
					cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

			event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

			String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
			Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
			Vector<Isotope> IsoData = new Vector<Isotope>();
			for (int i = 0; i < IsoTemp.size(); i++) {
				Isotope iso = new Isotope();
				iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
				IsoData.add(iso);
			}
			event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
			event.Detected_Isotope = IsoData;

		}
		if (DB_verion.matches("1.7")) {
			event.Event_Number = cu.getInt(cu.getColumnIndex(ROWID));
			event.mColumn_Version = cu.getString(cu.getColumnIndex(DB_FORMAT_VERSION));
			event.mInstrument_Name = cu.getString(cu.getColumnIndex(INSTRUMENT_MODEL));
			event.EventData = cu.getString(cu.getColumnIndex(DATE));
			event.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex(DATE_BG)));

			event.StartTime = cu.getString(cu.getColumnIndex(BEGIN));
			event.EndTime = cu.getString(cu.getColumnIndex(FINISH));
			event.mLocation = cu.getString(cu.getColumnIndex(LOCATION));
			event.mUser = cu.getString(cu.getColumnIndex(AGENT));
			event.Doserate_AVGs = cu.getString(cu.getColumnIndex(AVG_GAMMA));
			event.Neutron_AVGs = cu.getString(cu.getColumnIndex(AVG_NEUTRON));
			event.Doserate_MAXs = cu.getString(cu.getColumnIndex(MAX_GAMMA));
			event.Neutron_MAXs = cu.getString(cu.getColumnIndex(MAX_NEUTRON));
			event.Comment = cu.getString(cu.getColumnIndex(COMMENT));
			event.PhotoFileName = NcLibrary.Separate_EveryDash2(cu.getString(cu.getColumnIndex(PHOTO)));
			event.mGMT = cu.getString(cu.getColumnIndex(GMT));

			double ABC[] = new double[3];
			ABC[0] = cu.getDouble(cu.getColumnIndex(CALIB_A));
			ABC[1] = cu.getDouble(cu.getColumnIndex(CALIB_B));
			ABC[2] = cu.getDouble(cu.getColumnIndex(CALIB_C));

			event.MS.Set_Coefficients(ABC);

			event.AvgFillCps = Integer.parseInt(cu.getString(cu.getColumnIndex(FILL_CPS_AVG)));

			event.GPS_Longitude = cu.getDouble(cu.getColumnIndex(LONGITUDE));
			event.GPS_Latitude = cu.getDouble(cu.getColumnIndex(LATITUDE));
			event.MS.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM)), true),
					cu.getInt(cu.getColumnIndex(ACQ_TIME)));
			event.BG.Set_Spectrum(NcLibrary.Separate_EveryDash(cu.getString(cu.getColumnIndex(SPECTRUM_BG)), true),
					cu.getInt(cu.getColumnIndex(ACQ_TIME_BG)));

			event.MS.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME)) * 1000)));
			event.BG.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex(REAL_ACQ_TIME_BG)) * 1000)));

			event.IsManualID = (cu.getInt(cu.getColumnIndex(MANUAL_ID)) == 0) ? false : true;

			String temp = cu.getString(cu.getColumnIndex(IDENTIFICATION));
			Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');
			Vector<Isotope> IsoData = new Vector<Isotope>();
			for (int i = 0; i < IsoTemp.size(); i++) {
				Isotope iso = new Isotope();
				iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
				IsoData.add(iso);
			}
			event.Event_Detector = cu.getString(cu.getColumnIndex(EVENT_DETECTOR));
			event.Detected_Isotope = IsoData;

		}
		cu.close();
		EndDB();
		return event;
	}

	public void EndDB() {
		_db.close();
	}

	public void Save_DeviceName(String Name) {
		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + DEVICE_FILE + ".txt"); // �뵒諛붿씠�뒪
		// �꽕�엫
		// �뤃�뜑瑜�
		// 留뚮뱺�떎.
		if (nameFilePath.isFile()) {
			nameFilePath.delete();
		}
		if (!(nameFilePath.isFile())) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(nameFilePath);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				fos.write(String.valueOf(Name).getBytes());
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			}

		}
	}

	private double Get_DB_Version() {
		String Result = "1.4";
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + DB_VERSION_FILE + ".txt"); // �뵒諛붿씠�뒪
		// �꽕�엫
		// �뤃�뜑瑜�
		// 留뚮뱺�떎.
		if (nameFilePath.isFile()) {

			FileInputStream fis = null;
			try {

				fis = new FileInputStream(nameFilePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] buf = new byte[3];
			try {
				fis.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Result = new String(buf);
		}
		return Double.valueOf(Result);
	}

	///////////////////////////////////////////////////////////////////////////
	public int pow(int x, int y) { // �젣怨� 怨꾩궛
		int result = 1;
		for (int i = 0; i < y; i++) {
			result *= x;
		}
		return result;
	}

	public String SvToString(double nSv, boolean point) { // �닽�옄�삎 �떆蹂댄듃 媛믪쓣
		// string�쑝濡�
		if (MainActivity.mDetector.IsSvUnit == false)
			nSv = nSv * 100;

		DecimalFormat format = new DecimalFormat();
		String unit = " Sv/h";
		double value = 1;

		if (point == true) {
			if (nSv < pow(10, 3)) {
				value = nSv;
				unit = " nSv/h";
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = (nSv * 0.000001);
				unit = " mSv/h";
			} else if (nSv > pow(10, 9)) {
				value = (nSv * 0.000000001);
				unit = " Sv/h";
			}
		} else {
			if (nSv < pow(10, 3)) {
				value = nSv;
				unit = " nSv/h";
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = nSv * 0.000001;
				unit = " mSv/h";
			} else if (nSv > pow(10, 9)) {
				value = nSv * 0.000000001;
				unit = " Sv/h";
			}
		}

		format.applyLocalizedPattern("0.##");

		if (MainActivity.mDetector.IsSvUnit == false) {
			unit = unit.replace("Sv/h", "rem/h");
		}
		return format.format(value) + unit;
	}

	public void DeleteUpdateRow(Vector<String> RecodeFileName, int ColumnNumber, String Type) {

		String RecodeS = "";
		if (RecodeFileName != null) {
			for (int i = 0; i < RecodeFileName.size(); i++) {
				RecodeS += RecodeFileName.get(i) + ";";
			}
		}

		OpenDB();

		String temp = "";

		try {
			if (Type.equals(PHOTO)) {

				temp = "update Event set " + PHOTO + " = '" + RecodeS + "' where " + ROWID + " = " + (ColumnNumber)
						+ ";";
			}

			if (Type.equals(RECODE)) {

				temp = "update Event set " + RECODE + " = '" + RecodeS + "' where " + ROWID + " = " + (ColumnNumber)
						+ ";";
			}

			if (Type.equals(VIDEO)) {

				temp = "update Event set " + VIDEO + " = '" + RecodeS + "' where " + ROWID + " = " + (ColumnNumber)
						+ ";";
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

		_db.execSQL(temp);
		// _db.rawQuery(temp,null);

		EndDB();

	}

	public void Set_Crytal_Info(String Crystal_Number) {

		Cry_Info = new HW_Crytal_Info();

		SQLiteDatabase _db;
		_db = SQLiteDatabase.openDatabase(NcLibrary.get_LIB_FilePath("SwLibrary.sql"), null,
				SQLiteDatabase.OPEN_READWRITE);

		Vector<String> result = new Vector<String>();

		Cursor cu;
		cu = _db.rawQuery("SELECT * FROM Crystal_Info WHERE Crystal_Number = '" + Crystal_Number + "'", null);
		cu.moveToFirst();

		Cry_Info.Crystal_Name = cu.getString(Cry_Info.Cu_Crystal_Name);
		Cry_Info.Crystal_Number = Double.valueOf(cu.getString(Cry_Info.Cu_Crystal_Number)).doubleValue();

		String Coefficients_Str = cu.getString(Cry_Info.Cu_Coefficients);

		String[] Coefficients_Split = Coefficients_Str.split(";");

		double Coefficients_A = Double.valueOf(Coefficients_Split[0]).doubleValue();

		double Coefficients_B = Double.valueOf(Coefficients_Split[1]).doubleValue();

		double Coefficients_C = Double.valueOf(Coefficients_Split[2]).doubleValue();

		double Coefficients_D = Double.valueOf(Coefficients_Split[3]).doubleValue();

		double Coefficients_E = Double.valueOf(Coefficients_Split[4]).doubleValue();

		Cry_Info.FindPeakN_Coefficients = new double[] { Coefficients_A, Coefficients_B, Coefficients_C, Coefficients_D,
				Coefficients_E };

		String FWHM_Str = cu.getString(Cry_Info.Cu_FWHM);

		String[] FWHM_Split = FWHM_Str.split(";");

		double FWHM_A = Double.valueOf(FWHM_Split[0]).doubleValue();

		double FWHM_B = Double.valueOf(FWHM_Split[1]).doubleValue();

		Cry_Info.FWHM = new double[] { FWHM_A, FWHM_B };

		Cry_Info.Wnd_ROI_En = Double.valueOf(cu.getString(Cry_Info.Cu_Wnd_ROI_En)).doubleValue();

		cu.close();

	}
	//190529 hh100->200으로 프로그램만 바꿨을시 컬럼 확인해서 없으면 db삭제 후 다시 설정

	//Favorite coulmn은 200에만 있음
	public void checkDB()
	{
		Cursor cu = null;
		try
		{
			String sql = "select EXISTS (select * from Event where Favorite) as success";
			cu = _db.rawQuery(sql,  null);
		}
		catch (Exception e)
		{
/*
			 File file = new File(mDB_Path);
			 if(file.exists())
			 {
			 	file.delete();
				 createDatabase(mDB_Path);
			 }
*/

			_db.execSQL("ALTER TABLE Event ADD COLUMN Favorite text DEFAULT 'false'");
			_db.execSQL("ALTER TABLE Event ADD COLUMN Recode text");
			_db.execSQL("ALTER TABLE Event ADD COLUMN Fill_Cps_Avg text DEFAULT '0'");
		}

	}

	//20.02.04
	public boolean deleteEvent(){
		try {
			//if(!_db.isOpen())
			OpenDB();
			String sql = "DELETE FROM Event";
			_db.execSQL(sql);
			sql = "UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME = 'Event'";
			_db.execSQL(sql);

			return true;
		}catch (Exception e){
			return false;
		}
	}
	//190529 EventList 수정
	public Cursor LoadEventList(int start, int end)
	{
		//String sql = "SELECT _id , Date, Avg_Gamma, AcqTime, Identification, Favorite, Event_Detector,begin,Photo FROM Event AS Pre order by _id DESC limit "+start+","+end;
		String sql = "SELECT _id , Date, Avg_Gamma, AcqTime, Identification, Favorite, Event_Detector,begin,Photo FROM Event order by _id DESC ";
		//String sql = "SELECT _id , Date, Avg_Gamma, AcqTime, Identification, Favorite, Event_Detector,'begin',Photo FROM Event AS Pre order by _id DESC";
		Cursor cu = _db.rawQuery(sql,  null);
		return cu;
	}
	//190529 db 전체 갯수
	public long getEventDBCount()
	{
		return DatabaseUtils.queryNumEntries(_db, "Event");
	}
	//190529 변경 Radresponder 등 index를 통한 db호출 잦아서 아예 함수로 뺌
	public EventData LoadEventDB(int _id) {
		Cursor cu = _db.rawQuery("SELECT * FROM Event WHERE _id = '"+_id+"'",  null);
		EventData Item = new EventData();
		try {
			if (cu.getCount() != 0) {
				while (cu.moveToNext()) {
					Item.Event_Number = Integer.parseInt(cu.getString(cu.getColumnIndex("_id")));
					Item.EventData = cu.getString(cu.getColumnIndex("Date"));
					Item.AcqTime = cu.getString(cu.getColumnIndex("AcqTime"));
					Item.Doserate_AVGs = cu.getString(cu.getColumnIndex("Avg_Gamma"));
					Item.Favorite_Checked = cu.getString(cu.getColumnIndex("Favorite"));
					Item.Event_Detector = cu.getString(cu.getColumnIndex("Event_Detector"));
					Item.StartTime = cu.getString(cu.getColumnIndex("begin"));
					Item.mInstrument_Name = cu.getString(cu.getColumnIndex("Instrument_Model"));
					Item.BG.Set_MeasurementDate(cu.getString(cu.getColumnIndex("Date_BG")));
					Item.EndTime = cu.getString(cu.getColumnIndex("finish"));
					Item.mLocation = cu.getString(cu.getColumnIndex("Location"));
					Item.mUser = cu.getString(cu.getColumnIndex("Person_in_charge"));
					Item.Neutron_AVGs = cu.getString(cu.getColumnIndex("Avg_Neutron"));
					Item.Doserate_MAXs = cu.getString(cu.getColumnIndex("Max_Gamma"));
					Item.Neutron_MAXs = cu.getString(cu.getColumnIndex("Min_Neutron"));
					Item.Comment = cu.getString(cu.getColumnIndex("Comment"));
					Item.PhotoFileName1 = Separate_EveryDash3(cu.getString(cu.getColumnIndex("Photo")));
					Item.VedioFileName1 = Separate_EveryDash3(cu.getString(cu.getColumnIndex("Video")));
					Item.RecodeFileName1 = Separate_EveryDash3(cu.getString(cu.getColumnIndex("Recode")));
					Item.mGMT = cu.getString(cu.getColumnIndex("Gmt"));

					double ABC[] = new double[3];
		/*			ABC[0]  =  cu.getDouble(cu.getColumnIndex("Cali_A"));
					ABC[1]  =  cu.getDouble(cu.getColumnIndex("Cali_B"));
					ABC[2]  =  cu.getDouble(cu.getColumnIndex("Cali_C"));*/

					ABC[0]  =  cu.getDouble(cu.getColumnIndex("Cali_A"));
					ABC[1]  =  cu.getDouble(cu.getColumnIndex("Cali_B"));
					ABC[2]  =  cu.getDouble(cu.getColumnIndex("Cali_C"));

					Item.MS.Set_Coefficients(ABC);
					Item.BG.Set_Coefficients(ABC);

					Item.AvgFillCps = Integer.parseInt(cu.getString(cu.getColumnIndex("Fill_Cps_Avg")));
					Item.GPS_Longitude =cu.getDouble(cu.getColumnIndex("Longitude"));
					Item.GPS_Latitude = cu.getDouble(cu.getColumnIndex("Latitude"));
					Item.MS.Set_Spectrum(Separate_EveryDash(cu.getString(cu.getColumnIndex("SpectrumView")), true), Integer.parseInt(Item.AcqTime));
					Item.BG.Set_Spectrum(Separate_EveryDash(cu.getString(cu.getColumnIndex("Background")), true), cu.getInt(cu.getColumnIndex("BgAcqTime")));
					Item.MS.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex("Real_AcqTime")) * 1000)));
					Item.BG.Set_StartSystemTime(new Date((long) (cu.getFloat(cu.getColumnIndex("Real_BgAcqTime")) * 1000)));

					Item.IsManualID = (cu.getInt(cu.getColumnIndex("Manual_ID")) == 0) ? false : true;

					String temp = cu.getString(cu.getColumnIndex("Identification"));
					Vector<String> IsoTemp = Separate_EveryDash2(temp, '|');
					Vector<Isotope> IsoData = new Vector<Isotope>();
					for (int i = 0; i < IsoTemp.size(); i++)
					{
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						IsoData.add(iso);
					}
					Item.Detected_Isotope = IsoData;
					Item.Event_Detector = cu.getString(cu.getColumnIndex("Event_Detector"));
				}
				cu.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			NcLibrary.Write_ExceptionLog(e);
			return  null;
		}

		return Item;
	}




}
