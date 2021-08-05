package android.HH100.Structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import android.HH100.IDspectrumActivity.Check;
import android.HH100.Identification.Isotope;
import android.HH100.Structure.Detector.MeasurementInfo;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class EventData implements Serializable {
	private static final long serialVersionUID = -5970081092645798459L;

	public static final String EVENT_GAMMA = "Gamma";
	public static final String EVENT_NEUTRON = "Neutron";
	public static final String EVENT_MANUAL_ID = "Manual ID";

	public String mInstrument_Name;
	public String mColumn_Version;
	public String mLocation;
	public String mUser;
	public String mGMT;

	public Spectrum MS = new Spectrum();
	public Spectrum BG = new Spectrum();
	// public Detector Detector = new Detector();
	public Vector<Isotope> Detected_Isotope;

	public boolean IsManualID = false;
	public String EventData = "";
	public int Event_Number;
	public String StartTime;
	public String EndTime;
	public int Doserate_unit = 0;
	public String Comment = "";
	public Vector<String> PhotoFileName = new Vector<String>();
	public Vector<String> VedioFileName= new Vector<String>();
	public Vector<String> RecodeFileName= new Vector<String>();
	public String Favorite_Checked = Check.Favorite_False;

	public double Doserate_AVG;
	public double Neutron_AVG;
	public double Doserate_MAX;
	public double Neutron_MAX;



	public String Doserate_AVGs;
	public String Neutron_AVGs;
	public String Doserate_MAXs;
	public String Neutron_MAXs;
	public Neutron mNeutron = new Neutron();
	public String Event_Detector;

	public double GPS_Longitude = 0;
	public double GPS_Latitude = 0;



	public Date mStart_SystemTime;

	public int TotalFillCps;

	public int AvgFillCps;

	//190529 EventList 수정
	public String AcqTime;
	public ArrayList<String> Identification = new ArrayList<String>();
	public ArrayList<String> PhotoFileName1 = new ArrayList<String>();
	public ArrayList<String> VedioFileName1= new ArrayList<String>();
	public ArrayList<String> RecodeFileName1= new ArrayList<String>();

	// ----------------------------------------------------------
	public void Set_StartTime() {
		Calendar calendar = Calendar.getInstance();
		EventData = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
				+ String.valueOf(calendar.get(Calendar.MONTH) + 1) + "-" + String.valueOf(calendar.get(Calendar.DATE));

		mStart_SystemTime = calendar.getTime();

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		//StartTime = hour + ":" + minute + ":" + second + "." + MSec;
		//StartTime = hour + ":" + minute + ":" + second;
		StartTime = hour + ":" + minute + ":" + second;

	}

	public void Set_EndEventTime() {
		Calendar calendar = Calendar.getInstance();

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		//EndTime = hour + ":" + minute + ":" + second + "." + MSec;
		EndTime = hour + ":" + minute + ":" + second;
	}

	public String Get_EndEventTime() {
		Calendar calendar = Calendar.getInstance();

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		//String EndTime = hour + ":" + minute + ":" + second + "." + MSec;
		String EndTime = hour + ":" + minute + ":" + second;
		return EndTime;
	}

	public String Get_Date() {
		Calendar calendar = Calendar.getInstance();
		String Event_Date = String.valueOf(calendar.get(Calendar.YEAR)) + "-"
				+ String.valueOf(calendar.get(Calendar.MONTH) + 1) + "-" + String.valueOf(calendar.get(Calendar.DATE));

		return Event_Date;
	}

	public double Get_SystemAcqTime() {

		long acq_mSec = Calendar.getInstance().getTimeInMillis() - mStart_SystemTime.getTime();
		return acq_mSec * 0.001;

	}
}
