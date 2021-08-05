package android.HH100.Model;

import java.io.Serializable;

import android.HH100.MainActivity;
import NcLibrary.Spectrum;

public class DataModel implements Serializable {
	private static final long serialVersionUID = -6726229612941490740L;
	
	public final boolean D = MainActivity.D;
	
	public static final int DET_CGN = 65;
	public static final int DET_LGN = 66;
	public static final int DET_NaI_3x3 = 67;
	public static final int DET_NaI_2x2 = 68;
	public static final int DET_NaI_15x15 = 69;		
	
	public static final int GAMMA_DOSERATE_LIMIT = 100000000;
	public static final int SIGMA_THRESHOLD_ACCUMUL_SEC = 20;
	public static final int PAST_SPC_ARRAY_SIZE = 40; //sec
	
	enum DoserateUnit {SV,R};
	enum MS_MODE {ID_MODE,SETUP_MODE};
	
	//---
	private MeasurementInfo Information = new MeasurementInfo();
	
	private int _DetType = DET_NaI_3x3;
	
	private Spectrum MS = new Spectrum();
	private Spectrum BG = new Spectrum();
	

	
	public static class MeasurementInfo{
		public String InstrumentModel_Name;
		public String InstrumentModel_MacAddress;
		public String Location;
		public String User;
		public int AlarmSound;
		public String IsoLibraryName;
		
		public DoserateUnit DrUnit;
		public boolean IsManualID = false;
		
		
		public MeasurementInfo()
		{
			
		}		
	}
}
