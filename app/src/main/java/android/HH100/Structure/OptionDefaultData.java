package android.HH100.Structure;

import android.R.bool;
import android.HH100.*;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.OptionDefaultData;
import android.HH100.Structure.Spectrum;
import android.content.*;
import android.content.res.*;
import android.preference.*;
import android.util.Log;

public class OptionDefaultData {

	public static Context mSuperContext;
	public String location;
	public String fix_alarm = "1000";
	public String var_alarm = "4";
	public String neu_threshold = "0.8";
	public String low_discrimination = "0";
	public String upper_discrimination = "1024";
	public String bgsecond = "60";
	public String Calib_time = "200";
	public String manual_id_time = "60";
	public String manual_id_time_unit = "10";
	public String IsoLib_list = "0";
	public String DoseUnit = "1";
	public boolean p_sequence_mode_available = false;
	public String sequence_mode_acq_time = "60";
	public String sequence_mode_pause_time = "5";
	public String sequence_mode_repeat_time = "5";
	public boolean radresponder_mode_available = false;
	public String response_eventlist_key = "1";
	public String ConnectMode = "0";
	public String Admin_Password = "1234";





	// 10000 rem/h
	public String healthy_threshold = "10000";

	public String getHealthy_threshold() {
		return healthy_threshold;
	}

	public void setHealthy_threshold(String healthy_threshold) {
		this.healthy_threshold = healthy_threshold;
	}

	public OptionDefaultData(Context mContext) {
		mSuperContext = mContext;

	}

	public String user;

	public String getUser() {

		user = "None";
		return user;
	}

	public String getLocation() {

		location = "None";
		return location;
	}

}
