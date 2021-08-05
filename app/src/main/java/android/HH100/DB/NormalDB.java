package android.HH100.DB;

import java.io.File;
import java.io.IOException;
import java.util.*;

import android.HH100.MainService;
import android.HH100.Structure.NcLibrary;
import android.content.ContentValues;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class NormalDB extends Thread {
	private static final String TAG = "NormalDB";
	public static String DB_FOLDER = "SAM";
	private static final boolean D = false;

	public static final String AB_TABLE = "CaliAB";
	public static final String CALI_A = "CaliA";
	public static final String CALI_B = "CaliB";
	public static final String CALI_C = "CaliC";

	public static final String BG_TIME_TABLE = "BackGroundTime";
	public static final String BG_TIME = "Time";
	public static final String BG_DATE = "Date";

	public static final String DB_FILE = "NormalDB";
	public static final String DB_TABLE = "Normal";
	public static final String ROWID = "_id";
	public static final String DATE = "Date";
	public static final String TIME = "Time";
	public static final String INCHAGE = "InCharge";
	public static final String LOCATION = "Location";
	public static final String GAMMA = "Gamma";
	public static final String NEUTRON = "Neutron";

	private String mDB_File_Path = null;

	private ContentValues m_Data = new ContentValues();
	private static final String DATABASE_CREATE = "create table " + DB_TABLE + " (" + ROWID
			+ " integer primary key autoincrement," + DATE + " text, " + TIME + " text, " + INCHAGE + " text,"
			+ LOCATION + " text," + GAMMA + " real, " + NEUTRON + " real);";

	private static SQLiteDatabase _db;

	public NormalDB() {
		Set_DB_Path();
		OpenDB();
	}

	@Override
	@Deprecated
	public void destroy() {
		// TODO Auto-generated method stub
		EndDB();
		super.destroy();
	}

	public void OpenDB() {
		try {
			if (D)
				Log.d(TAG, "Open NormalDB");
			_db = SQLiteDatabase.openDatabase(mDB_File_Path, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (Exception e) {
			if (D)
				Log.d(TAG, "DB is not exist. ");
			createDatabase(mDB_File_Path);

		}
	}

	private void Set_DB_Path() {
		if (mDB_File_Path == null) {

			File sdcard = Environment.getExternalStorageDirectory();

			File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
			if (!dbpath.exists()) {
				if (D)
					Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
				dbpath.mkdirs();
			}

			mDB_File_Path = dbpath.getAbsolutePath() + File.separator + DB_FILE + ".sql";
		}
	}

	//20.02.04
	public boolean deleteNormal(){
	    try {
     /*       if (!_db.isOpen()) {

            }*/
			OpenDB();
            String sql = "DELETE FROM Normal";
            _db.execSQL(sql);
            return true;
        }catch (Exception e){
            return  false;
        }
	}

	public boolean IsThereDB_File() {
		File dbpath = new File(mDB_File_Path);
		return dbpath.isFile();
	}

	private void createDatabase(String dbfile) {
		if (D)
			Log.d(TAG, "Try to create mBluetoothImage_flag DB. ");
		_db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);
		_db.execSQL(DATABASE_CREATE);
		_db.execSQL("create table " + AB_TABLE + " (_index integer primary key autoincrement, " + CALI_A + " real, "
				+ CALI_B + " real, " + CALI_C + " real);");
		_db.execSQL("create table " + BG_TIME_TABLE + " (_index integer primary key autoincrement, " + BG_TIME
				+ " integer, " + BG_DATE + " text);");
	}

	@Override
	public void run() {
		super.run();
		// while (this.isAlive()) {
		if (m_Data.size() != 0) {
			WriteData_onDB();
			ClearData();
			// }
		}
	}

	private void All_DB_Remove() {

		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + DB_FOLDER);
		DeleteDir(dbpath.toString());

		if (!dbpath.exists()) {
			if (D)
				Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		String dbfile = dbpath.getAbsolutePath() + File.separator + DB_FILE + ".sql";

		try {
			if (D)
				Log.d(TAG, "Open EventDB");
			_db = SQLiteDatabase.openDatabase(dbfile, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (Exception e) {
			if (D)
				Log.d(TAG, "DB is not exist.");
			createDatabase(dbfile);
		}

	}

	void DeleteDir(String path) {
		File file = new File(path);
		File[] childFileList = file.listFiles();
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				DeleteDir(childFile.getAbsolutePath()); // �븯�쐞 �뵒�젆�넗由� 猷⑦봽
			} else {
				childFile.delete(); // �븯�쐞 �뙆�씪�궘�젣
			}
		}
		file.delete(); // root �궘�젣
	}

	public long addValue(final String Agent, final String Location, final double gamma, final double neutron) {
		// OpenDB();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Calendar calendar = Calendar.getInstance();

					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH);
					int date = calendar.get(Calendar.DATE);
					int hour = calendar.get(Calendar.HOUR_OF_DAY);
					int minute = calendar.get(Calendar.MINUTE);
					int second = calendar.get(Calendar.SECOND);

					m_Data.put(DATE, year + "-" + (month + 1) + "-" + date);
					m_Data.put(TIME, hour + ":" + minute + ":" + second);
					m_Data.put(INCHAGE, Agent);
					m_Data.put(LOCATION, Location);
					m_Data.put(GAMMA, gamma);
					m_Data.put(NEUTRON, neutron);

					// long result = _db.insert(DB_TABLE, null, m_Data);
					// EndDB();
					if (IsThereDB_File()) {
						WriteData_onDB();
					} else {
						createDatabase(mDB_File_Path);
						WriteData_onDB();
					}
					ClearData();
				} catch (Exception e) {
					NcLibrary.Write_ExceptionLog(e);
				}
			}
		}).start();
		if (D)
			Log.d(TAG, "Write on Normal DataBase (G : " + gamma + ", N : " + neutron + ")");

		return 1;
	}

	private void WriteData_onDB() {
		if (_db.isOpen())
			_db.insert(DB_TABLE, null, m_Data);
		else {
			OpenDB();
			if (_db.isOpen())
				_db.insert(DB_TABLE, null, m_Data);
		}
	}

	private void ClearData() {
		m_Data.clear();
	}

	public void EndDB() {
		_db.close();
	}
}
