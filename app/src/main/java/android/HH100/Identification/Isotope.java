package android.HH100.Identification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.HH100.Structure.Detector;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View.MeasureSpec;

public class Isotope implements Serializable {
	private static final long serialVersionUID = -6726229612941490740L;

	public static final String CLASS_SNM = "SNM";
	public static final String CLASS_IND = "IND";
	public static final String CLASS_MED = "MED";
	public static final String CLASS_NORM = "NORM";
	public static final String CLASS_UNK = "UNK";

	public static final int COLOR_SNM = Color.rgb(150, 24, 150);
	public static final int COLOR_IND = Color.rgb(27, 23, 151);
	public static final int COLOR_MED = Color.rgb(44, 192, 185);
	public static final int COLOR_NORM = Color.rgb(10, 150, 20);
	public static final int COLOR_UNK = Color.RED;

	public static final String DB_VER_1_4 = "1.4";
	public static final String DB_VER_1_5 = "1.5";
	public static final String DB_VER_1_6 = "1.6";
	public static final String DB_VER_1_7 = "1.7";

	public Vector<NcPeak> Peaks = new Vector<NcPeak>();// new
	public Vector<NcPeak> Unknown_Peak = new Vector<NcPeak>();
	public Vector<NcPeak> FoundPeaks = new Vector<NcPeak>();
	public ArrayList<Double> FoundPeakBR = new ArrayList<Double>();
	public ArrayList<Double> IsoPeakEn = new ArrayList<Double>();

	//Adding peak for Drawing: Major and MinoPeak
	public Vector<NcPeak> ListPeakDrawEn = new Vector<NcPeak>(); //All major and minor peak
	public ArrayList<Double> ListPeakDrawBR = new ArrayList<Double>();



	public ArrayList<Double> C = new ArrayList<Double>();

	public double Index1 = 0;// new
	public double Index2 = 0;// new
	public double IndexMax = 0;
	// public Vector<NcPeak> Peaks = new Vector<NcPeak>();// new
	public String HelpVideo ="";
	//
	public int Screening_Process;

	public String isotopes;
	public double DoseRate = 0;
	public String DoseRate_S = "";

	public String Class;
	public int ClassColor = COLOR_UNK;
	public String Comment;
	public double measure_eff = 0;
	public double Confidence_Level = 0;

	public ArrayList<Double> IsoMinorPeakEn = new ArrayList<Double>();
	public ArrayList<Double> IsoMinorPeakBR = new ArrayList<Double>();

	public double Act = 0;
	public double Uncer = 0;
	public double RMSE = 0;
	/*
	 * public double Index1 = 0; public double Index2 = 0;
	 */

	public Isotope() {

	}

	public int Get_OnlyIdEnergy_Cnt() {

		return Peaks.size();
	}

	public String Get_DoseRate(int Unit) {
		if (Unit == Detector.DR_UNIT_SV)
			return NcLibrary.SvToString(DoseRate, true, true);
		else if (Unit == Detector.DR_UNIT_R)
			return NcLibrary.SvToString(DoseRate, true, false);
		else
			return "Unit Error";
	}

	public NcPeak Find_Peak_WithEnergy_InPeaks(int Energy) {
		for (int i = 0; i < Peaks.size(); i++) {
			if (Peaks.get(i).Energy_InWindow(Energy)) {
				return Peaks.get(i);
			}
		}
		return null;
	}

	public void Set_Class(String ClassCode) {
		if (ClassCode == null)
			return;

		Class = ClassCode;

		if (Class.matches(CLASS_SNM))
			ClassColor = COLOR_SNM;
		if (Class.matches(CLASS_IND))
			ClassColor = COLOR_IND;
		if (Class.matches(CLASS_MED))
			ClassColor = COLOR_MED;
		if (Class.matches(CLASS_NORM))
			ClassColor = COLOR_NORM;
		if (Class.matches(CLASS_UNK))
			ClassColor = COLOR_UNK;
	}

	public String Get_Result_OnlyDB(String DB_Version) {

		try {
			if (DB_Version.matches(DB_VER_1_4)) {
				String temp = NcLibrary.Auto_floor(Confidence_Level) + "%";
				if (Confidence_Level == 0)
					temp = "";
				if (DoseRate_S == null)
					DoseRate_S = "";
				if (Class == null)
					Class = "";
				if (Comment == null)
					Comment = "";

				return isotopes + ";" + temp + ";" + DoseRate_S + ";";
			} else if (DB_Version.matches(DB_VER_1_5) | DB_Version.matches(DB_VER_1_6)
					| DB_Version.matches(DB_VER_1_7)) {
				String Temp = "";
				for (int i = 0; i < FoundPeaks.size(); i++) {
					Temp += FoundPeaks.get(i).Peak_Energy + ";";
				}

				// if(Temp.length()==0) Temp+=";";

				// float nNumber = 123.456789;
				// String strNumber = String.format("%.2f", Confidence_Level);

				String temp = NcLibrary.Auto_floor(Confidence_Level) + "%";
				if (Confidence_Level == 0)
					temp = "";
				if (DoseRate_S == null)
					DoseRate_S = "";
				if (Class == null)
					Class = "";
				if (Comment == null)
					Comment = "";

				String Temp2 = "1";
				if(Screening_Process != 1)
					Temp2 = "2";


				return isotopes + ";" + temp + ";" + DoseRate_S + ";" + Class + ";" + Comment + ";" + Temp + Temp2+"|";
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void Set_Result_OnlyDB_v1_5(String Data)
	{
		Vector<String> temp = NcLibrary.Separate_EveryDash2(Data);
		isotopes = temp.get(0);
		Confidence_Level = Integer.valueOf((temp.get(1).replace("%", "").matches("")) ? "0" : temp.get(1).replace("%", ""));
		DoseRate_S = temp.get(2);
		Class = temp.get(3);
		Comment = temp.get(4);

		for(int i = 5; i <= temp.size()-2;  i++)
		{
			NcPeak tPeak = new NcPeak();
			tPeak.Peak_Energy = Double.valueOf(temp.get(i));
			FoundPeaks.add(tPeak);
		}
		
/*		if (temp.size() >= 6) 
		{
			NcPeak tPeak = new NcPeak();
			tPeak.Peak_Energy = Double.valueOf(temp.get(5));
			FoundPeaks.add(tPeak);
		}
		if (temp.size() >= 7) 
		{
			NcPeak tPeak = new NcPeak();
			tPeak.Peak_Energy = Double.valueOf(temp.get(6));
			FoundPeaks.add(tPeak);
		}
		if (temp.size() >= 8) 
		{
			NcPeak tPeak = new NcPeak();
			tPeak.Peak_Energy = Double.valueOf(temp.get(7));
			FoundPeaks.add(tPeak);
		}
		if (temp.size() >= 9) 
		{
			NcPeak tPeak = new NcPeak();
			tPeak.Peak_Energy = Double.valueOf(temp.get(8));
			FoundPeaks.add(tPeak);
		}
		if (temp.size() >= 10) 
		{
			NcPeak tPeak = new NcPeak();
			tPeak.Peak_Energy = Double.valueOf(temp.get(9));
			FoundPeaks.add(tPeak);
		}*/

		if(temp.get(temp.size()-1).equals("1"))
			Screening_Process = 1;
		else
			Screening_Process = 2;
	}

	public String Get_ConfidenceLevel() {
		return String.format("%.0f", Confidence_Level);

	}
}
