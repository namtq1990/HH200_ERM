package android.HH100.DB;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Structure.Detector;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class DB_1_7_Ver {

	//String IsoLib = "1.1.1"; //180727 -> 3.1v -> 1.1.0 -> 180827 1.1.1 -> 190610 1.1.3 -> 20.03.12 1.1.4
	/*
	    181025 DB_VERSION \n추가 이전버전 byte array[3]으로 선언 2자리수 버전만 처리가능
	     \n추가해서 한줄로 통채로 인식
	*/
	String IsoLib = "1.1.4\n";
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
	public static final String BEGIN = "begin";
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
	public Context mContext;

	public DB_1_7_Ver() {

	}

	public DB_1_7_Ver(Context mContext1) {
		mContext = mContext1;
	}

	public String Get_CREATE_DATABASE() {

		String DATABASE_CREATE =

				"create table " + DB_TABLE + " (" + ROWID + " integer primary key autoincrement," + DB_FORMAT_VERSION
						+ " text, " + INSTRUMENT_MODEL + " text, " + DATE + " text, " + DATE_BG + " text, " + BEGIN
						+ " text, " + FINISH + " text, " + LOCATION + " text," + AGENT + " text," + AVG_GAMMA
						+ " text, " + AVG_NEUTRON + " text, " + MAX_GAMMA + " text, " + MAX_NEUTRON + " text, "
						+ ACQ_TIME + " text, " + REAL_ACQ_TIME + " real, " + ACQ_TIME_BG + " text,"// --
						// v1.4
						+ REAL_ACQ_TIME_BG + " real, " + COMMENT + " text, " + PHOTO + " text," + VIDEO + " text," + GMT
						+ " text," + CALIB_A + " real," + CALIB_B + " real," + CALIB_C + " real," + LONGITUDE + " real,"
						+ LATITUDE + " real," + SPECTRUM + " text," // --v1.4
						+ SPECTRUM_BG + " text,"// --v1.4
						+ MANUAL_ID + " text,"// --v1.4
						+ IDENTIFICATION + " text," + EVENT_DETECTOR + " text," + FAVORITE + " text," + RECODE
						+ " text," + FILL_CPS_AVG + " text);"; // --v1.4

		return DATABASE_CREATE;
	}

	public String GetDBVersion() {

		return DB_verion;
	}

	void Check_AndMake_IsoLibraryFile_old() {

		String IsoLib_Path = getIsoDB_FilePath("SwLibrary.sql");
		File dbpath = new File(getIsoDB_FilePath(EventDBOper.DB_VERSION_FILE + ".txt"));
		////////////////////////////////
		if (!(dbpath.isFile())) {
			File isoFile = new File(IsoLib_Path);
			if (isoFile.isFile())
				try {
					isoFile.delete();
				} catch (NullPointerException e) {
					// TODO: handle exception
				}

			exdbfile(R.raw.iso_library);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dbpath);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				for(int i = 0; i<IsoLib.length(); i++)
				{
					fos.write(IsoLib.getBytes()[i]);
				}
/*				fos.write(IsoLib.getBytes()[0]);
				fos.write(IsoLib.getBytes()[1]);
				fos.write(IsoLib.getBytes()[2]);*/
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(dbpath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] buf = new byte[5];
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

			if (buf[0] == IsoLib.getBytes()[0] && buf[2] == IsoLib.getBytes()[2] && buf[4] == IsoLib.getBytes()[4])
			{

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
					e1.printStackTrace();
				}
				try {
					fos.write(IsoLib.getBytes());
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				exdbfile(R.raw.iso_library);
			}
		}

	}


	void Check_AndMake_IsoLibraryFile() {

		String IsoLib_Path = getIsoDB_FilePath("SwLibrary.sql");
		File dbpath = new File(getIsoDB_FilePath(EventDBOper.DB_VERSION_FILE + ".txt"));
		String readVersion ="";
		////////////////////////////////
		if (!(dbpath.isFile())) {
			File isoFile = new File(IsoLib_Path);
			if (isoFile.isFile())
				try {
					isoFile.delete();
				} catch (NullPointerException e) {
					// TODO: handle exception
				}

			exdbfile(R.raw.iso_library);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(dbpath);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				for(int i = 0; i<IsoLib.length(); i++) {
					fos.write(IsoLib.getBytes()[i]);
				}
				fos.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}	else {
			FileReader fileReader = null;
			try {
				fileReader = new FileReader(dbpath.getAbsoluteFile().getPath());
				BufferedReader bufReader = new BufferedReader(fileReader);

				if (bufReader != null) {
					String readText = "";
					try {
						if ((readText = bufReader.readLine()) != null) {
							readVersion = readText.replace("\n", "");
						}
						String version = IsoLib.replace("\n", "");
						if (!readVersion.equals(version)) {
							dbpath.delete();
							File isoFile = new File(IsoLib_Path);
							if (isoFile.isFile()) {
								isoFile.delete();
							}
							FileOutputStream fos = null;
							try {
								fos = new FileOutputStream(dbpath);
								fos.write(IsoLib.getBytes());
								fos.close();
							} catch (FileNotFoundException e1) {
								NcLibrary.Write_ExceptionLog(e1);
							} catch (IOException e) {
								NcLibrary.Write_ExceptionLog(e);
							}

							exdbfile(R.raw.iso_library);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				fileReader.close();
				bufReader.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
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
			NcLibrary.Write_ExceptionLog("\nKainacActivity - getIsoDB_FilePath");
			return "";
		}
	}

	public void exdbfile(int rawId) {
		File file = new File(getIsoDB_FilePath("SwLibrary.sql"));
		if (file.isFile() == false) {
			byte[] buffer = new byte[8 * 1024];

			int length = 0;
			InputStream is = mContext.getResources().openRawResource(rawId);
			BufferedInputStream bis = new BufferedInputStream(is);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(getIsoDB_FilePath("SwLibrary.sql"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				while ((length = bis.read(buffer)) >= 0)
					fos.write(buffer, 0, length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				fos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	void Check_AndMake_EventDB_VersionFile() {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {
			// if (D)
			// Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
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

			if (buf[0] == EventDBOper.mDB.GetDBVersion().getBytes()[0]
					&& buf[2] == EventDBOper.mDB.GetDBVersion().getBytes()[2]) {

			} else { // 버젼이 다르다.
				if (MainActivity.mEventDB != null) {
					try {
						if (buf[2] == 51) { // if DB version is 1.3
							MainActivity.mEventDB.Remove_EventFile();
							MainActivity.mEventDB.OpenDB();
							MainActivity.mEventDB.EndDB();

							MainActivity.mNormalDB.OpenDB();
						} else {
							Vector<EventData> temp = MainActivity.mEventDB.Load_ALL_Event();
							MainActivity.mEventDB.Remove_EventFile();
							MainActivity.mEventDB.OpenDB();
							MainActivity.mEventDB.EndDB();

							if (temp != null) {
								for (int i = 0; i < temp.size(); i++) {
									MainActivity.mEventDB.WriteEvent_OnDatabase(temp.get(i));
								}

							}
						}
					} catch (Exception e) {
						NcLibrary.Write_ExceptionLog("\nKainacActivity - Check_AndMake_EventDB_VersionFile");
					}

				}
			}
		} else {
			if (MainActivity.mEventDB != null) {
				FileOutputStream Fos = null;
				try {

					Fos = new FileOutputStream(nameFilePath);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				byte[] buf = new byte[3];
				try {
					Fos.write(EventDBOper.mDB.GetDBVersion().getBytes());
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

	void Check_AndMake_DeviceNameFile(boolean IsEdit) {

		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {
			// if (D)
			// Log.d(TAG, "Create DB directory. " + dbpath.getAbsolutePath());
			dbpath.mkdirs();
		}
		////////////////////////////////
		File nameFilePath = new File(dbpath.getAbsolutePath() + File.separator + EventDBOper.DEVICE_FILE + ".txt"); // 디바이스
		// 네임
		// 폴더를
		// 만든다.
		if (nameFilePath.isFile() & IsEdit == false) {
			nameFilePath.delete();
		}

		if (!nameFilePath.isFile()) {
			try {
				nameFilePath.createNewFile();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(nameFilePath);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				String Last_Dev;
				if (MainActivity.mDetector.InstrumentModel_Name == null
						| MainActivity.mDetector.InstrumentModel_Name.equals("")) {
					Last_Dev = MainActivity.DEVICE_NAME;
				} else {
					Last_Dev = MainActivity.mDetector.InstrumentModel_Name;
				}
				fos.write(String.valueOf(Last_Dev).getBytes());
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
