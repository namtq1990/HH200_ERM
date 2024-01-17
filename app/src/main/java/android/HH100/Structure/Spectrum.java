package android.HH100.Structure;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import Debug.Debug;
import NcLibrary.Coefficients;
import android.HH100.Structure.Detector.HwPmtProperty_Code;

public class Spectrum implements Serializable {
	private static final long serialVersionUID = -6726229612941490740L;

	private double[] mSPC = new double[1024];
	public int mAcqTime =0;
	private String mDate = "";
	private Coefficients mCoefficients = new Coefficients();
	public double Wnd_Roi = 0;

	public double getWnd_Roi() {
		return Wnd_Roi;
	}

	public void setWnd_Roi(double wnd_Roi) {
		Wnd_Roi = wnd_Roi;
	}

	double[] FWHM;

	public double[] getFWHM() {
		return FWHM;
	}

	public void setFWHM(double[] mFWHM) {
		FWHM = mFWHM;
	}

	public double[] getFindPeakN_Coefficients() {
		return FindPeakN_Coefficients;
	}

	public void setFindPeakN_Coefficients(double[] mCoefficients) {
		FindPeakN_Coefficients = mCoefficients;
	}

	double[] FindPeakN_Coefficients;

	private static double[] mErosionSPC = new double[1024];

	private Date mStartTime;
	private Date mEndTime;
	public static int FillCps;
	public int FillCps1;
	public double AvgFillCps;
	public static int TotalFillCps;
	public static int second, second1, second2, second3;
	public boolean SpecSumMode = false;
	Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
	private boolean mHasSpectra = false;

	public int HealthSafety_Threshold = 0;

	public double mGammaDoserate = 0;

	Debug mDebug = new Debug();

	//public int crystalType = HwPmtProperty_Code.NaI_2x2;
	public int crystalType = HwPmtProperty_Code.CeBr_2x2;

	public Spectrum() {
	}

	public Spectrum(int[] channel) {
		mSPC = new double[channel.length];
		for (int i = 0; i < channel.length; i++) {
			mSPC[i] = channel[i];
		}
		mAcqTime = 1;
	}

	public Spectrum(double[] channel) {
		mSPC = channel;
		mAcqTime = 1;
	}

	public void SetPeakInfo(Vector<NcPeak> mPeakInfo_bg) {

		peakInfo_bg = mPeakInfo_bg;
	}

	public Vector<NcPeak> GetPeakInfo() {

		return peakInfo_bg;

	}

	public void Set_HealthSafety_Threshold(int mHealthSafety_Threshold) {

		HealthSafety_Threshold = mHealthSafety_Threshold;
	}

	public void Set_mGammaDoserate(double mmGammaDoserate) {

		mGammaDoserate = mmGammaDoserate;
	}

	public void Set_crystalType(int mcrystalType) {

		crystalType = mcrystalType;
	}

	public boolean hasSpectra() {
		return mHasSpectra;
	}

	public void setHasSpectra(boolean hasSpectra) {
		this.mHasSpectra = hasSpectra;
	}

	public int Get_HealthSafety_Threshold() {

		return HealthSafety_Threshold;
	}

	public double Get_mGammaDoserate() {

		return mGammaDoserate;
	}

	public int Get_CrystalType() {

		return crystalType;
	}

	public double Get_RoiCount(int start_Ch, int End_Ch) {
		int result = 0;
		for (int i = start_Ch; i <= End_Ch; i++) {
			result += mSPC[i];
		}
		return result;
	}

	/*
	 * public void SetAvgFillCps(int Fill_Cps) { FillCps1 = Fill_Cps;
	 *
	 * }
	 */

	public int GetAvgFillCps() {
		try {

			AvgFillCps = (double) FillCps1 / (double) mAcqTime;

			int abc;
			abc = 0;

		} catch (Exception e) {
			// TODO: handle exception
		}

		return (int) AvgFillCps;
	}

	public void SetFillCps(int Fill_Cps) {
		FillCps = Fill_Cps;
		TotalFillCps += FillCps;
	}

	public int GetFillCps() {
		return FillCps;
	}

	public int GetTotalFillCps() {

		return TotalFillCps;
	}

	public void Set_MeasurementDate(String date) {
		mDate = date;
	}

	public String Get_MesurementDate() {
		return mDate;
	}

	public String ToString() {
		String result = "";

		for (int i = 0; i < mSPC.length; i++) {
			result += ((int) mSPC[i]) + ";";
		}
		return result;
	}

	public String ToString_() {
		try {
			String result = "";

			for (int i = 0; i < mSPC.length; i++) {
				result += ((int) mSPC[i]) + " ";
			}
			return result;
		} catch (Exception e) {
			return "";
		}
	}

	public String ToString_Double() {
		try {
			String result = "";

			for (int i = 0; i < mSPC.length; i++) {
				result += mSPC[i] + " ";
			}
			return result;
		} catch (Exception e) {
			return "";
		}
	}

	public int Get_AcqTime() {
		return mAcqTime;
	}

	public void Set(int Channel, double Count) {
		mSPC[Channel] = Count;
	}

	public double at(int Channel) {
		if (Channel < 0 | Channel >= mSPC.length)
			return 0;
		return mSPC[Channel];
	}

	public double Erosion_At(int Channel) {
		if (Channel < 0 | Channel >= mErosionSPC.length)
			return 0;
		return mErosionSPC[Channel];
	}

	public double[] Get_Erosion_Spec() {

		return mErosionSPC;
	}

	public int at_ToInt(int Channel) {
		if (Channel < 0 | Channel >= mSPC.length)
			return 0;
		return (int) mSPC[Channel];
	}

	public boolean Set_Spectrum(int[] spc) {
		mSPC = new double[spc.length];
		for (int i = 0; i < spc.length; i++) {
			mSPC[i] = spc[i];
		}
		mAcqTime = 1;

		return true;
	}



	public boolean Set_Spectrum(int[] spc, final int acqTime)
	{
		mSPC = new double[spc.length];
		for (int i = 0; i < spc.length; i++) {
			mSPC[i] = spc[i];
		}
		mAcqTime = acqTime;
		return true;
	}

	public boolean Set_Spectrum(double[] spc, int acqTime) {

		mSPC = new double[spc.length];
		for (int i = 0; i < spc.length; i++) {
			mSPC[i] = spc[i];
		}
		mAcqTime = acqTime;

		return true;
	}

	public boolean Set_Spectrum(Spectrum spc) {
		mSPC = new double[spc.mSPC.length];
		for (int i = 0; i < spc.mSPC.length; i++) {
			mSPC[i] = spc.mSPC[i];
		}
		mAcqTime = spc.Get_AcqTime();
		mCoefficients = spc.mCoefficients;
		mDate = spc.mDate;
		mStartTime = spc.Get_StartSystemTime();
		mEndTime = spc.mEndTime;
		return true;
	}

	public double [] Get_Spectrum(){
		return mSPC;
	}

	public boolean Accumulate_Spectrum(Spectrum spc) {

		SpecSumMode = true;

		if (mSPC.length != spc.mSPC.length)
			return false;

		if (mDebug.IsDebugMode && mDebug.IsScaleSpectrumMode) {
			mAcqTime = mDebug.IsScaleSpectrumAcqtime;
			for (int i = 0; i < spc.mSPC.length; i++) {
				mSPC[i] = spc.mSPC[i];
			}
		} else {
			for (int i = 0; i < spc.mSPC.length; i++) {
				mSPC[i] += spc.mSPC[i];
			}

			mAcqTime += spc.Get_AcqTime();
		//	NcLibrary.SaveText1("spc.Get_AcqTime() : "+spc.Get_AcqTime()+"  mAcqTime : "+mAcqTime+"\n","test");
		}
		FillCps1 += FillCps;

		// SetAvgFillCps(FillCps1);

		if (mStartTime == null)
			Set_StartSystemTime();
		Set_EndSystemTime();
		return true;
	}

	///
	public double Get_NetCount(int StartChannel, int EndChannel) {
		if (StartChannel > EndChannel)
			return 0;
		if (StartChannel < 0 | EndChannel >= mSPC.length)
			return 0;

		double Result = 0;

		for (int i = StartChannel; i <= EndChannel; i++) {
			Result += mSPC[i];
		}
		return Result;
	}

	public void Set_Coefficients(double[] Arg) {
		mCoefficients = new Coefficients(Arg);
	}

	public void Set_Coefficients(Coefficients Arg) {
		mCoefficients = Arg;
	}

	public Coefficients Get_Coefficients() {
		return mCoefficients;
	}

	public int Get_Ch_Size() {
		return mSPC.length;
	}

	public int Get_TotalCount() {

		/*
		 * Calendar calendar = Calendar.getInstance();
		 *
		 * second = calendar.get(Calendar.MILLISECOND); second2 =
		 * calendar.get(Calendar.SECOND);
		 */int Result = 0;
		for (int i = 0; i < mSPC.length; i++) {
			Result += mSPC[i];
		}

		if (SpecSumMode)
		{
			//NcLibrary.SaveText1("Result : "+Result+"  FillCps1 : "+FillCps1+ "\n","test");
			return Result + FillCps1;
		} else {
			/*
			 * if (second1 != second && second3 != second2) {
			 *
			 * String nsv = String.format("%.3f",
			 * (MainActivity.mDetector.Get_Gamma_DoseRate_nSV()/1000));
			 *
			 * Log.d("CPS :", "Default usv/h; " + Integer.toString(Result) + ";" +
			 * Integer.toString(FillCps) + ";" + nsv);
			 *
			 * second1 = second; second3 = second2; }
			 */
			return Result + FillCps;
		}
	}

	public double Get_AvgCPS() {
		int Result = 0;
		for (int i = 0; i < mSPC.length; i++) {
			Result += mSPC[i];
		}
		if (Result == 0)
			return 0;
		return Result / mAcqTime;
	}

	public double Get_AvgCPS1() {
		int Result = 0;
		for (int i = 0; i < mSPC.length; i++) {
			Result += mSPC[i];
		}
		if (Result == 0)
			return 0;
		return Result / 1;
	}

	public void ClearSPC() {
		for (int i = 0; i < mSPC.length; i++) {
			mSPC[i] = 0;
		}
		mAcqTime = 0;

		mStartTime = null;
		mEndTime = null;
	}

	public void ClearAllData() {
		for (int i = 0; i < mSPC.length; i++) {
			mSPC[i] = 0;
		}
		mAcqTime = 0;
		mDate = "";
		mCoefficients = new Coefficients();

		mStartTime = null;
		mEndTime = null;
	}

	public int[] ToInteger() {
		int Result[] = new int[mSPC.length];

		for (int i = 0; i < mSPC.length; i++) {
			Result[i] = (int) mSPC[i];
		}
		return Result;
	}

	public double[] ToDouble() {
		double Result[] = new double[mSPC.length];

		for (int i = 0; i < mSPC.length; i++) {
			Result[i] = mSPC[i];
		}
		return Result;
	}

	public void Set_StartSystemTime() {
		Calendar calendar = Calendar.getInstance();
		mStartTime = new Date((calendar.getTimeInMillis()));

	}

	public void Set_StartSystemTime(Date start) {
		mStartTime = start;

	}

	public void Set_EndSystemTime() {
		mEndTime = new Date((Calendar.getInstance().getTimeInMillis()));
	}

	public void Set_EndSystemTime(Date End) {
		mEndTime = new Date((End.getTime()));
	}

	public Date Get_StartSystemTime() {
		if (mStartTime != null)
			return mStartTime;
		else
			return new Date(0);

	}

	public boolean Set_Erosion_Spectrum(Spectrum mSpc) {
		// FindPeaksN mNewFindPeak = new FindPeaksN();
		// mErosionSPC = new double[mSpc.Get_Ch_Size()];
		// mErosionSPC = mNewFindPeak.GetPPSpectrum(mSpc);

		return true;
	}

	public Date Get_SystemElapsedTime() {
		if (mStartTime != null & mEndTime != null) {
			return new Date((mEndTime.getTime() - mStartTime.getTime()) + 1200);
		} else if (mStartTime != null) {
			return mStartTime;
		} else
			return new Date(0);
	}

	public String Get_SystemElapsedTime_String() {
		return String.valueOf(Get_SystemElapsedTime().getTime() * 0.001) + 'S';
	}

	public void Save_DateNow() {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		int MSec = Integer.valueOf((int) (calendar.get(Calendar.MILLISECOND) * 0.01));
		String bg_date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + "T" + date.getHours() + ":" + date.getMinutes() + ":"
				+ date.getSeconds() + "." + MSec + NcLibrary.Get_GMT();
		Set_MeasurementDate(bg_date);
	}

	public Spectrum ToSpectrum() {
		Spectrum result = new Spectrum();
		result.Set_Spectrum(this);
		return result;
	}
}
