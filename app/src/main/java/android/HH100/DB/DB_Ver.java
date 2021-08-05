package android.HH100.DB;

import android.HH100.MainActivity;
import android.HH100.Identification.IsotopesLibrary;
import android.content.Context;

public class DB_Ver {
	Context mContext;

	DB_1_7_Ver DB;
	public static DB_Ver mDB = new DB_Ver();
	public DB_Ver(Context mContext1) {

		mContext = mContext1;
		DB = new DB_1_7_Ver(mContext);
	}

	public DB_Ver() {
		DB = new DB_1_7_Ver();

	}

	public String Get_CREATE_DATABASE() {

		return DB.Get_CREATE_DATABASE();
	}

	public String GetDBVersion() {

		return DB.GetDBVersion();
	}

	public String GetIsoLibVer() {

		return DB.IsoLib;
	}

	public void Check_DB_File() {

		DB.Check_AndMake_IsoLibraryFile();
		DB.Check_AndMake_DeviceNameFile(true);
		DB.Check_AndMake_EventDB_VersionFile();

	}

}
