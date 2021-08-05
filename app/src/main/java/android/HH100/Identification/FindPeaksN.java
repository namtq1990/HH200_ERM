package android.HH100.Identification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import Debug.Debug;
import Debug.Log_Setting;
import NcLibrary.Coefficients;
import NcLibrary.NewNcAnalsys;
import android.HH100.MainActivity;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;
import android.os.Environment;

public class FindPeaksN implements FindPeakMode {

	public static final int CHANNEL_ARRAY_SIZE = 1024;

	public static double mPmtSurface = 0;

	public static Spectrum Real_BG = new Spectrum();
	private static Spectrum mMS_Gainstabilization = new Spectrum();
	private static Spectrum mDB_BG = new Spectrum();
	static int CHSIZE = 1024;
	static int BINSIZE = 850;
	static int[] ChSpec = new int[CHSIZE];

	static Debug mDebug = new Debug();
	static int mCount = 0;

	public static Log_Setting mLog = new Log_Setting();

	static Vector<NcPeak> PeakInfo = null;

	public double[] GetPPSpectrum(Spectrum mSpec) {

		long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		int TimerCount = 0;

		double Theshold = 0.3;
		double[] FWHM_gen = mSpec.getFWHM();
		// double[] IterCoeff = mSpec.getFindPeakN_Coefficients();

		Vector<NcPeak> PeakInfo = new Vector<NcPeak>();
		int CHSIZE = 1024;
		int BINSIZE = 850;

		double[] ChSpec = new double[CHSIZE];
		int[] BGSpec = new int[CHSIZE];
		double[] BinSpec = new double[BINSIZE];
		double[] TF = new double[BINSIZE];
		double[] BGEroBinSpec = new double[BINSIZE];
		double[] BGEroChSpec = new double[CHSIZE];
		double[] PPChSpec = new double[CHSIZE];
		double[] DChSpec = new double[CHSIZE];

		for (int i = 0; i < CHSIZE; i++) {
			ChSpec[i] = 0;
			BGEroChSpec[i] = 0;
			PPChSpec[i] = 0;
			DChSpec[i] = 0;

			if (i < BINSIZE) {
				BinSpec[i] = 0;
				TF[i] = 0;
				BGEroBinSpec[i] = 0;
			}
		}

		for (int i = 0; i < mSpec.Get_Ch_Size(); i++) {
			ChSpec[i] = mSpec.at(i);
		}

		// ft_smooth(ChSpec, 0.015, 2.96474);
		// ft_smooth(ChSpec, 0.015, 2.96474);

		// NcLibrary.TimeCheck(elapsedTime, startTime, TimerCount);
		char[] str1 = new char[1000];

		long now = System.currentTimeMillis();
		String time1 = Long.toString(now);

		TF = NewNcAnalsys.TransferFunct(FWHM_gen, TF);

		// NcLibrary.TimeCheck(elapsedTime, startTime, TimerCount);

		BinSpec = NewNcAnalsys.ReBinning(ChSpec, TF, BinSpec);

		double[] IterCoeff = new double[] { -0.0000000001, 0.0000005531, -0.0008610261, 0.5684236932, -53.5185548731,
				0.0002779219, -0.0100275772, 5.8129370431 };

		BGEroBinSpec = NewNcAnalsys.BGErosion(BinSpec, IterCoeff, BGEroBinSpec, TF, mSpec.Get_Coefficients());

		// NcLibrary.TimeCheck(elapsedTime, startTime, TimerCount);

		BGEroChSpec = NewNcAnalsys.ReturnReBinning(BGEroBinSpec, TF, BGEroChSpec);

		// NcLibrary.TimeCheck(elapsedTime, startTime, TimerCount);

		PPChSpec = NewNcAnalsys.BGSubtration(ChSpec, BGEroChSpec);

		mCount = 0;

		if (MainActivity.mDebug.IsDebugMode && MainActivity.mDebug.IsTextSaveMode) {

			String SourceName = "BG_Spectrum";
			String UpTitle = "BeforFillter";
			String DownTitle = "AfterFillter";

			String PeakInfoFillterTxt = PeakInfoToString(PeakInfo);

			// String Subtaction = "\n Subtaction " + SpectrumToString(ChSpec);

			String D_Spec = "\n D_Spec " + SpectrumToString(DChSpec);

			String BG_PP_Spec = "\n BG_PP_Spec " + SpectrumToString(PPChSpec);
			String BG_BGEroChSpec_Spec = "\n BG_BGEroChSpec_Spec " + SpectrumToString(BGEroChSpec);
			String BG_Original_Spec = "\n BG_Original_Spec " + SpectrumToString(mSpec);

			String BGAcqTime = "\n BGAcqTime " + Integer.toString((int)mSpec.Get_AcqTime());
			// String Ms_OriginalDChSpec = "\n MsOriginalDChSpec " +
			// SpectrumToString(MsOriginalDChSpec);

			// String BG_DSpectrum = "\n BG_DSpectrum " +
			// SpectrumToString(BG_Erosion);

			String SumTxt = SourceName + "\n\n\n" + PeakInfoFillterTxt + D_Spec + BG_PP_Spec + BG_BGEroChSpec_Spec
					+ BG_Original_Spec + BGAcqTime + BG_PP_Spec;

			WriteText(SourceName, SumTxt);

		}

//		NcLibrary.LogData(mLog.FindPeaksN,
//				mDebug.mLog.FindPeakN_GetPPSpectrum + "BGPPSpec : " + SpectrumToString(PPChSpec));

		return PPChSpec;

	}

	public Vector<NcPeak> Find_Peak(Spectrum MS, Spectrum BG) {

		// 1st: Define parameters

		//2x2 FWHM

		// double[] FWHM_gen = new double[] { 1.2707811254, -1.5464537062 };

		double[] FWHM_gen = BG.getFWHM();


		Coefficients EnCoeff_Cali = MS.Get_Coefficients();// Energy Calibration

		// 3x3 Eff
		//double[] Eff_Coeff = new double[] { -0.027939138, 0.694026779, -6.627760069, 28.20796375, -48.74100729 };


		double[] Eff_Coeff = BG.getFindPeakN_Coefficients();


		double[] InterCoeff = new double[] { -0.0000000001, 0.0000005531, -0.0008610261, 0.5684236932, -53.5185548731,
				0.0002779219, -0.0100275772, 5.8129370431 };




		double Theshold = 0.3;
		double Thshold_Index2 = 0.8;
		int CHSIZE = 1024;
		int BINSIZE = 850;

		// .......................Start Procesisng.......................
		double[] originalSpc = new double[CHSIZE];
		int[] BGSpec = new int[CHSIZE];
		double[] BinSpec = new double[BINSIZE];
		double[] TF = new double[BINSIZE];
		double[] BGEroBinSpec = new double[BINSIZE];
		double[] BGEroChSpec = new double[CHSIZE];
		double[] PPChSpec = new double[CHSIZE];
		double[] DChSpec = new double[CHSIZE];

		Vector<NcPeak> peakInfo = new Vector<NcPeak>();
		double[] ChSpec = new double[CHSIZE];
		for (int i = 0; i < MS.Get_Ch_Size(); i++) {
			ChSpec[i] = MS.at(i);
		}

		Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
		// peakInfo_bg = GetPPSpectrum_H(BG);
		peakInfo_bg = BG.GetPeakInfo();
		// BGProcess = false;
		// }

		// Vector<NcPeak> peakInfo = new Vector<NcPeak>();

		// Processing
		// Step 0: Generate tranfer function
		TF = NewNcAnalsys.TransferFunct(FWHM_gen, TF);

		// Step 1: Smooth data to reduce noise
		originalSpc = NewNcAnalsys.Smooth_Spc(ChSpec);

		// Step 2: ReBinning
		BinSpec = NewNcAnalsys.ReBinning(originalSpc, TF, BinSpec);

		// Step 3: BGErosion
		BGEroChSpec = NewNcAnalsys.BGErosion(BinSpec, InterCoeff, BGEroChSpec, TF, EnCoeff_Cali);

		// Step 4:ReturnReBinning
		double[] reBincEmptySpc = new double[1024];
		reBincEmptySpc = NewNcAnalsys.ReturnReBinning(BGEroChSpec, TF, reBincEmptySpc);
		BGEroBinSpec = NewNcAnalsys.Smooth_Spc(reBincEmptySpc);

		// Step 5:BGSubtration
		PPChSpec = NewNcAnalsys.BGSubtration(originalSpc, BGEroBinSpec, PPChSpec); // Chek

		// Step 6: GenDSpecrum
		DChSpec = NewNcAnalsys.GenDSpecrum(PPChSpec, BGEroBinSpec, FWHM_gen, DChSpec, true);

		// Step 7:Find Peak
		//peakInfo = NewNcAnalsys.FindPeak(DChSpec, peakInfo); // C# 전용 함수로 변경
		peakInfo = NewNcAnalsys.FindPeak_Beta(PPChSpec,DChSpec, peakInfo,FWHM_gen); // C# 전용 함수로 변경


		// Step 8:Search ROI based on FWHM
		peakInfo = NewNcAnalsys.SearchROI_N(PPChSpec, peakInfo, FWHM_gen);

		peakInfo = NewNcAnalsys.PeakChannelToEnergy(peakInfo, peakInfo.size(), EnCoeff_Cali);

		// Step 9: NetCount
		peakInfo = NewNcAnalsys.NetCount_N(PPChSpec, peakInfo, FWHM_gen);

		// Step 10: Calculate BG: This function: HoongJae Lee has error because he added
		// Orignal BG to BG
		peakInfo = NewNcAnalsys.BGNetCount(BGEroBinSpec, peakInfo, peakInfo.size());

		// Step 11: BG Subtract
		peakInfo = NewNcAnalsys.NetBGSubtract_N(peakInfo, peakInfo_bg, MS.Get_AcqTime(), BG.Get_AcqTime(),FWHM_gen,EnCoeff_Cali,MS.getWnd_Roi());

		// Step 12: Calculate Critical Level filter
		peakInfo = NewNcAnalsys.Calculate_LC(peakInfo);

		// Step 13: Applied to Critical Level filter
		peakInfo = NewNcAnalsys.LC_Filter(peakInfo);

		// WBCLog log = new WBCLog();

		// step 14: PeakMatching Isotope

		return peakInfo;

	}

	public static Vector<NcPeak> GetPPSpectrum_H(Spectrum mSpec) {
		// 1st: Define parameters
		//double[] FWHM_gen = new double[] { 1.87167, -6.24957 };


		//2x2 FWHM

		//double[] FWHM_gen = new double[] { 1.2707811, -1.5464537 };
		//double[] FWHM_gen = new double[] { 1.2707811254, -1.5464537062 };

		double[] FWHM_gen = mSpec.getFWHM();

		Coefficients EnCoeff_Cali = mSpec.Get_Coefficients();// Energy Calibration

		// Eff 2x2
		//double[] Eff_Coeff = new double[] { -0.027939138, 0.694026779, -6.627760069, 28.20796375, -48.74100729 };

		double[] Eff_Coeff = mSpec.getFindPeakN_Coefficients();

		// Eff 2x2
		// double[] Eff_Coeff = new double[] { -0.241522359, 5.784633023, -51.63572634,
		// 203.0083263, -302.0872868 };

		double[] InterCoeff = new double[] { -0.0000000001, 0.0000005531, -0.0008610261, 0.5684236932, -53.5185548731,
				0.0002779219, -0.0100275772, 5.8129370431 };

		double Theshold = 0.3;

		int CHSIZE = 1024;
		int BINSIZE = 850;

		// .......................Start Procesisng.......................
		double[] originalSpc = new double[CHSIZE];
		int[] BGSpec = new int[CHSIZE];
		double[] BinSpec = new double[BINSIZE];
		double[] TF = new double[BINSIZE];
		double[] BGEroBinSpec = new double[BINSIZE];
		double[] BGEroChSpec = new double[CHSIZE];
		double[] PPChSpec = new double[CHSIZE];
		double[] DChSpec = new double[CHSIZE];

		Vector<NcPeak> peakInfo = new Vector<NcPeak>();
		double[] ChSpec = new double[CHSIZE];
		for (int i = 0; i < mSpec.Get_Ch_Size(); i++) {
			ChSpec[i] = mSpec.at(i);
		}

		// Processing
		// Step 0: Generate tranfer function
		TF = NewNcAnalsys.TransferFunct(FWHM_gen, TF);

		// Step 1: Smooth data to reduce noise
		originalSpc = NewNcAnalsys.Smooth_Spc(ChSpec);

		// Step 2: ReBinning
		BinSpec = NewNcAnalsys.ReBinning(originalSpc, TF, BinSpec);

		// Step 3: BGErosion
		BGEroBinSpec = NewNcAnalsys.BGErosion(BinSpec, InterCoeff, BGEroBinSpec, TF, EnCoeff_Cali);

		// Step 4:ReturnReBinning
		double[] reBincEmptySpc = new double[1024];
		reBincEmptySpc = NewNcAnalsys.ReturnReBinning(BGEroBinSpec, TF, reBincEmptySpc);
		BGEroBinSpec = NewNcAnalsys.Smooth_Spc(reBincEmptySpc);

		// Step 5:BGSubtration
		PPChSpec = NewNcAnalsys.BGSubtration(originalSpc, BGEroBinSpec, PPChSpec); // Chek

		// Step 6: GenDSpecrum
		DChSpec = NewNcAnalsys.GenDSpecrum(PPChSpec, BGEroBinSpec, FWHM_gen, DChSpec, true);

		// Step 7:Find Peak
		//peakInfo = NewNcAnalsys.FindPeak(DChSpec, peakInfo); // C# 전용 함수로 변경

		peakInfo = NewNcAnalsys.FindPeak_Beta(PPChSpec,DChSpec, peakInfo,FWHM_gen); // C# 전용 함수로 변경

		// Step 7.1: Convert Peak channel to Peak Energy

		peakInfo = NewNcAnalsys.PeakChannelToEnergy(peakInfo, peakInfo.size(), EnCoeff_Cali);

		// Step 8:Search ROI based on FWHM
		peakInfo = NewNcAnalsys.SearchROI_N(PPChSpec, peakInfo, FWHM_gen);

		// Step 9: NetCount
		peakInfo = NewNcAnalsys.NetCount_N(PPChSpec, peakInfo, FWHM_gen);

		// Step 10: Calculate BG: This function: HoongJae Lee has error because he added
		// Orignal BG to BG
		peakInfo = NewNcAnalsys.BGNetCount(BGEroBinSpec, peakInfo, peakInfo.size());

		// Step 11: BG Subtract

		// Step 12: Calculate Critical Level filter
		peakInfo = NewNcAnalsys.Calculate_LC(peakInfo);

		// Step 13: Applied to Critical Level filter
		peakInfo = NewNcAnalsys.LC_Filter(peakInfo);

		// String BGSmooth = "\n BGSmooth " + SpectrumToString(BG);

		// BG_Erosion = BG.Get_Erosion_Spec();

		return peakInfo;
	}

	public static void WriteText(String Title, String mBody) {

		File file;

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SpectrumFolder";
		file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				NcLibrary.Write_ExceptionLog("\n FindPeakN - WriteText");
			}
		}

		for (int i = 0; i < 100; i++) {
			file = new File(path + File.separator + Title + "_" + Integer.toString(i) + ".txt");

			if (!file.exists()) {

				break;
			}
		}

		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter buw = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
			buw.write(mBody);
			buw.close();
			fos.close();

			// Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다",
			// 1).show();
		} catch (IOException e) {
			NcLibrary.Write_ExceptionLog("\n FindPeakN - WriteText");
		}

	}

	public static double[] SpectrumOneSecond(double[] Spec, int Acqtime) {

		// ft_smooth(Spec, 0.015, 2.96474);
		// ft_smooth(Spec, 0.015, 2.96474);

		for (int i = 0; i < Spec.length; i++) {

			Spec[i] = Spec[i] / Acqtime;

		}
		// ft_smooth(Spec, 0.015, 2.96474);

		return Spec;
		// ft_smooth(Sum, 0.015, 2.96474);

	}

	public static double[] SubTraction_One_Secound(Spectrum MS, Spectrum BG) {

		double[] Sum = new double[MS.Get_Ch_Size()];
		double[] BG_1_Second = new double[MS.Get_Ch_Size()];
		ft_smooth(BG_1_Second, 0.015, 2.96474);
		double[] MS_1_Second = new double[MS.Get_Ch_Size()];
		ft_smooth(MS_1_Second, 0.015, 2.96474);
		for (int i = 0; i < MS.Get_Ch_Size(); i++) {

			BG_1_Second[i] = BG.at(i) / BG.Get_AcqTime();
			MS_1_Second[i] = MS.at(i) / MS.Get_AcqTime();

			Sum[i] = MS_1_Second[i] - BG_1_Second[i];

			if (Sum[i] < 0) {
				Sum[i] = 0;
			}

		}

		return Sum;
	}

	public static double[] SubTraction(double[] MS, Spectrum BG, int mAcqTime) {

		double[] Sum = new double[MS.length];
		double[] BG_1_Second = new double[MS.length];
		double[] MS_Temp_Spec = new double[MS.length];
		double[] BG_Temp_Spec = new double[BG.Get_Ch_Size()];
		double[] BG_Time_Temp_Spec = new double[BG.Get_Ch_Size()];

		for (int i = 0; i < MS.length; i++) {
			MS_Temp_Spec[i] = MS[i];
			BG_Temp_Spec[i] = (BG.Erosion_At(i) / BG.Get_AcqTime()) * mAcqTime;
		}

		for (int i = 0; i < BG_Time_Temp_Spec.length; i++) {
			BG_Time_Temp_Spec[i] = (BG_Temp_Spec[i] / BG.Get_AcqTime()) * mAcqTime;
		}

		for (int i = 0; i < MS.length; i++) {
			BG_1_Second[i] = BG_Temp_Spec[i] / BG.Get_AcqTime();

			Sum[i] = MS_Temp_Spec[i] - BG_Temp_Spec[i];
			if (Sum[i] < 0) {
				Sum[i] = 0;
			}

		}

		int SubTractionSpecSum = 0;
		int PPChSpecSum = 0;
		int BGEroSepctrum = 0;

		for (int i = 380; i < 580; i++) {
			SubTractionSpecSum += (int) Sum[i];
			PPChSpecSum += (int) MS[i];
			BGEroSepctrum += (BG_Temp_Spec[i] / BG.Get_AcqTime()) * mAcqTime;
		}
//		NcLibrary.LogData(mDebug.mLog.FindPeaksN, mDebug.mLog.FindPeaksN + "SubTraction K40 SubTractionSpecSum : "
//				+ Integer.toString(SubTractionSpecSum));
//		NcLibrary.LogData(mDebug.mLog.FindPeaksN,
//				mDebug.mLog.FindPeaksN + "SubTraction K40 PPChSpecSum : " + Integer.toString(PPChSpecSum));
//		NcLibrary.LogData(mDebug.mLog.FindPeaksN,
//				mDebug.mLog.FindPeaksN + "SubTraction K40 BGEroSepctrum : " + Integer.toString(BGEroSepctrum));

		// ft_smooth(Sum, 0.015, 2.96474);

		return Sum;
	}

	public static double[] SubTraction(Spectrum MS, Spectrum BG) {

		double[] Sum = new double[MS.Get_Ch_Size()];
		double[] BG_1_Second = new double[MS.Get_Ch_Size()];
		double[] MS_Temp_Spec = new double[MS.Get_Ch_Size()];
		double[] BG_Temp_Spec = new double[BG.Get_Ch_Size()];

		for (int i = 0; i < MS.Get_Ch_Size(); i++) {
			MS_Temp_Spec[i] = MS.at(i);
			BG_Temp_Spec[i] = BG.at(i);
		}

		ft_smooth(MS_Temp_Spec, 0.015, 2.96474);
		ft_smooth(BG_Temp_Spec, 0.015, 2.96474);

		for (int i = 0; i < MS.Get_Ch_Size(); i++) {
			BG_1_Second[i] = BG_Temp_Spec[i] / BG.Get_AcqTime();

			Sum[i] = MS_Temp_Spec[i] - (BG_1_Second[i] * MS.Get_AcqTime());
			if (Sum[i] < 0) {
				Sum[i] = 0;
			}

		}

		// ft_smooth(Sum, 0.015, 2.96474);
		return Sum;
	}

	public static double[] SubTractionToOneSecond(Spectrum MS, Spectrum BG) {

		double[] MS_Temp = new double[MS.Get_Ch_Size()];
		double[] BG_Temp = new double[MS.Get_Ch_Size()];

		for (int i = 0; i < MS.Get_Ch_Size(); i++) {
			MS_Temp[i] = MS.at(i);
			BG_Temp[i] = BG.at(i);
		}

		ft_smooth(BG_Temp, 0.015, 2.96474);
		ft_smooth(BG_Temp, 0.015, 2.96474);
		ft_smooth(MS_Temp, 0.015, 2.96474);
		ft_smooth(MS_Temp, 0.015, 2.96474);

		for (int i = 0; i < MS.Get_Ch_Size(); i++) {
			// BG_1_Second[i] = BG[i] / BackgroundAcqTime;
			BG_Temp[i] = BG_Temp[i] / BG.Get_AcqTime();
			MS_Temp[i] = MS_Temp[i] / MS.Get_AcqTime();
			MS_Temp[i] = MS_Temp[i] - BG_Temp[i];
			if (MS_Temp[i] < 0) {
				MS_Temp[i] = 0;
			}

		}
		ft_smooth(MS_Temp, 0.015, 2.96474);

		return MS_Temp;

	}

	public static void ft_smooth(double[] ihist, double aval, double bval) {

		int temp_wind = (int) Math.ceil(aval * 1024 + bval);
		double temp;
		int window = 0;
		int window_half = 0;
		double temp_sum = 0;
		double temphist1[] = new double[1024];
		double temphist2[] = new double[1024];
		for (int i = 0; i < 1024; i++) {
			temphist1[i] = 0;
			temphist2[i] = 0;
		}

		for (int i = 4; i < 1024 - temp_wind; i++) {

			temp_sum = 0;
			temp = 0;
			window = (int) (aval * i + bval);

			if (window <= 0)
				continue;

			if (window % 2 == 0)
				window = window + 1;

			window_half = (int) Math.ceil((double) (window / 2.0)) - 1;

			for (int j = i - window_half; j <= i + window_half; j++)
				temp_sum = temp_sum + ihist[j];

			temp = temp_sum / window;
			temphist1[i] = temp;
		}

		for (int i = 4; i < 1024 - temp_wind; i++) {

			temp_sum = 0;
			temp = 0;
			window = (int) (aval * i + bval);

			if (window <= 0)
				continue;

			if (window % 2 == 0)
				window = window + 1;

			window_half = (int) Math.ceil((double) (window / 2.0)) - 1;
			// window_half = window / 2 - 1;

			for (int j = i - window_half; j <= i + window_half; j++)
				temp_sum = temp_sum + temphist1[j];

			if (i == 60) {

				int a;

				a = 1;
			}

			temp = temp_sum / window;
			temphist2[i] = temp;
		}
		for (int i = 0; i < 1024; i++)
			ihist[i] = temphist2[i];
	}

	public static Vector<NcPeak> GetRoiA_B_And_Height(double[] PPSpec_Ch, Vector<NcPeak> PeakInfo,
													  double[] EnergyCoeff3P) {

		float resultA = 0;
		float resultB = 0;
		int resultAx = 0;
		int resultBx = 0;

		double calib_A2 = EnergyCoeff3P[0];
		double calib_B2 = EnergyCoeff3P[1];
		double calib_C2 = EnergyCoeff3P[2];

		for (int i = 0; i < PeakInfo.size(); i++) {

			PeakInfo.get(i).Peak = NcLibrary.FindMaxValue(PPSpec_Ch, (int) PeakInfo.get(i).ROI_Left,
					(int) PeakInfo.get(i).ROI_Right);

			PeakInfo.get(i).ROI_Left = (int) ((double) PeakInfo.get(i).ROI_Left * 0.9);
			PeakInfo.get(i).ROI_Right = (int) ((double) PeakInfo.get(i).ROI_Right * 1.1);

			double En = NcLibrary.Channel_to_Energy(PeakInfo.get(i).Peak, calib_A2, calib_B2, calib_C2);
			double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(En);
			double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
			double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

			L_ROI_Percent = (NcLibrary.Energy_to_Channel(En * L_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1-factor);
			R_ROI_Percent = (NcLibrary.Energy_to_Channel(En * R_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1+factor);

			VallyResult Result = Find_Vally(PPSpec_Ch, (int) PeakInfo.get(i).ROI_Left, (int) PeakInfo.get(i).ROI_Right,
					(int) PeakInfo.get(i).Peak, resultA, resultB, resultAx, resultBx);

			PeakInfo.get(i).BG_a = Result.A;
			PeakInfo.get(i).BG_b = Result.B;
			PeakInfo.get(i).ROI_Left = (int) Result.Ax;
			PeakInfo.get(i).ROI_Right = (int) Result.Bx;

			double OriginalHeight = PPSpec_Ch[(int) PeakInfo.get(i).Peak];
			double ScatterY = Result.A * (int) PeakInfo.get(i).Peak + Result.B;
			PeakInfo.get(i).Height = OriginalHeight - ScatterY;

		}

//		NcLibrary.LogData(mLog.FindPeaksN,
//				mLog.FindPeaksN + "GetRoiA_B_And_Height -------------------------------------------");
//		for (int i1 = 0; i1 < PeakInfo.size(); i1++) {
//
//			NcLibrary.LogData(mLog.FindPeaksN, mLog.FindPeaksN + "GetRoiA_B_And_Height, PeakInfo.get(i).Height :"
//					+ Double.toString(PeakInfo.get(i1).Height));
//
//		}
//		NcLibrary.LogData(mLog.FindPeaksN,
//				mLog.FindPeaksN + "GetRoiA_B_And_Height Total : " + Integer.toString(PeakInfo.size()));

		return PeakInfo;
	}

	public static Vector<NcPeak> Peak_Fillter_1(Vector<NcPeak> PeakInfo) {

		for (int i = 0; i < PeakInfo.size(); i++) {
			if (PeakInfo.get(i).Height > 0) {

			} else {
				PeakInfo.remove(i);
			}

		}
		return PeakInfo;

	}

	public static VallyResult Find_Vally(double[] arrfloat, int firstlocation, int secondlocation, int peakvalue,
										 float resulta, float resultb, int resultax, int resultbx) {
		VallyResult result = new VallyResult();
		// peak value가 없으면 결과로 0을 return
		if (peakvalue == 0) {
			resulta = 0;
			resultb = 0;
			return result; //
		}
		double startpx = 0;
		double startpy = 0;
		double endpx = 0;
		double endpy = 0;
		double midpx = 0;
		double midpy = 0;

		// int center = firstlocation+ (int)((secondlocation -
		// firstlocation)/2);
		int center = peakvalue;
		int contcnt = 0;
		int cntlimit = 2;// NcLibrary.Auto_floor(NcLibrary.Get_Roi_window_by_energy(NcLibrary.Channel_to_Energy(peakvalue,
		// calib_A2, calib_B2, calib_C2)));
		double threshold = 0;

		int findflag = 0;

		// 170217 Modify int i = center - (int) (center * 0.01) -> int i =
		// center - (int) (center * 0.02)

		for (int i = center - (int) (center * 0.02); i > firstlocation; i--) {
			if ((arrfloat[i - 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						startpx = i + (cntlimit - 1);
						startpy = arrfloat[i];
						break;
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}
		if (findflag == 0 || contcnt < cntlimit) {
			startpx = firstlocation;
			startpy = arrfloat[firstlocation];
		}
		contcnt = 0;
		findflag = 0;
		for (int i = center + (int) (center * 0.01); i < secondlocation; i++) {
			if ((arrfloat[i + 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						endpx = i - (cntlimit - 1);
						endpy = arrfloat[i];
						break;
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}
		if (findflag == 0 || contcnt < cntlimit) {
			endpx = secondlocation;
			endpy = arrfloat[secondlocation];
		}

		midpx = peakvalue;
		midpy = arrfloat[peakvalue];

		// final A, B 값계산
		// resulta=(float)((startpy-endpy)/(startpx-endpx));
		// resultb=(float)(startpy-(resulta*startpx));

		int sss = (int) startpx;
		int sss2 = (int) endpx;
		resultax = sss;
		resultbx = sss2;

		if (startpy == endpy & startpx == endpx) {
			result.A = 0;
			result.B = 0;
		} else if (startpx == endpx) {
			result.A = 0;
			result.B = 0;
		} else {
			result.A = (startpy - endpy) / (startpx - endpx);
			result.B = startpy - (result.A * startpx);
		}
		result.Ax = (int) startpx;
		result.Bx = (int) endpx;
		return result;
	}

	public static String PeakInfoToString(Vector<NcPeak> PeakInfo) {

		String Peak_Energy = "";
		String Peak_CH = "";
		String Peak_PP_NetCount = "";
		String Peak_BG_NetCount = "";
		String Peak_BG_A = "";
		String Peak_BG_B = "";
		String Peak_Roi_Left = "";
		String Peak_Roi_Light = "";
		String Peak_Height = "";

		String Sum = "";
		for (int i = 0; i < PeakInfo.size(); i++) {

			Peak_Energy = " Eng" + Integer.toString(i) + " " + Integer.toString((int) PeakInfo.get(i).Peak_Energy);
			Peak_CH = " Peak_CH" + Integer.toString(i) + " " + Integer.toString((int) PeakInfo.get(i).Peak);
			Peak_PP_NetCount = " PP_NetCnt" + Integer.toString(i) + " "
					+ Integer.toString((int) PeakInfo.get(i).NetCnt);
			Peak_BG_NetCount = " BG_NetCnt" + Integer.toString(i) + " "
					+ Integer.toString((int) PeakInfo.get(i).Background_Net_Count);

			Peak_BG_A = " Peak_BG_A" + Integer.toString(i) + " " + Double.toString(PeakInfo.get(i).BG_a);
			Peak_BG_B = " Peak_BG_B" + Integer.toString(i) + " " + Double.toString(PeakInfo.get(i).BG_b);

			Peak_Roi_Left = " Peak_Roi_Left" + Integer.toString(i) + " "
					+ Integer.toString((int) PeakInfo.get(i).ROI_Left);
			Peak_Roi_Light = " Peak_Roi_Light" + Integer.toString(i) + " "
					+ Integer.toString((int) PeakInfo.get(i).ROI_Right);

			Peak_Height = " Peak_Height" + Integer.toString(i) + " " + Double.toString(PeakInfo.get(i).Height);

			Sum += Peak_CH + Peak_Roi_Left + Peak_Roi_Light + Peak_Energy + Peak_PP_NetCount + Peak_BG_NetCount
					+ Peak_BG_A + Peak_BG_B + Peak_Height + "\n";

		}

		return Sum;

	}

	public static void PeakInfoWriteText1(String Title, Vector<NcPeak> PeakInfo) {

		String Peak_Energy = "";
		String Peak_CH = "";
		String Peak_PP_NetCount = "";
		String Peak_BG_NetCount = "";
		String Sum = "";
		for (int i = 0; i < PeakInfo.size(); i++) {

			Peak_Energy = " Eng" + Integer.toString(i) + " " + Integer.toString((int) PeakInfo.get(i).Peak_Energy);
			Peak_CH = " Peak_CH" + Integer.toString(i) + " " + Integer.toString((int) PeakInfo.get(i).Peak);
			Peak_PP_NetCount = " Peak_PP_NetCount" + Integer.toString(i) + " "
					+ Integer.toString((int) PeakInfo.get(i).NetCnt);
			Peak_BG_NetCount = " Peak_BG_NetCount" + Integer.toString(i) + " "
					+ Integer.toString((int) PeakInfo.get(i).Background_Net_Count);

			Sum += Peak_CH + Peak_Energy + Peak_PP_NetCount + Peak_BG_NetCount + "\n";

		}

		/*
		 * String Ch = ""; String Ch = ""; String Ch = ""; String Ch = ""; String Ch =
		 * ""; String Ch = "";
		 */

		WriteText(Title, Sum);

	}

	public static void PeakInfoWriteText(String Title, Vector<NcPeak> PeakInfo) {

		String Peak_Energy = "";
		String Peak_CH = "";
		String Peak_PP_NetCount = "";
		String Peak_BG_NetCount = "";
		for (int i = 0; i < PeakInfo.size(); i++) {

			Peak_Energy += " Eng" + Integer.toString(i) + " " + Double.toString(PeakInfo.get(i).Peak_Energy);
			Peak_CH += " Peak_CH" + Integer.toString(i) + " " + Double.toString(PeakInfo.get(i).PeakEst);
			Peak_PP_NetCount += " Peak_PP_NetCount" + Integer.toString(i) + " "
					+ Double.toString(PeakInfo.get(i).NetCnt);
			Peak_BG_NetCount += " Peak_BG_NetCount" + Integer.toString(i) + " "
					+ Double.toString(PeakInfo.get(i).Background_Net_Count);

		}

		String Sum = "";

		Sum = Peak_Energy + "\n" + Peak_CH + "\n" + Peak_PP_NetCount + "\n" + Peak_BG_NetCount;

		/*
		 * String Ch = ""; String Ch = ""; String Ch = ""; String Ch = ""; String Ch =
		 * ""; String Ch = "";
		 */

		WriteText(Title, Sum);

	}

	public static Vector<NcPeak> Peak_Fillter_0(Vector<NcPeak> PeakInfo, double k) {
		// k = 1.642;
		// double k = 0.842;
		// double k = 1.282;

		for (int i = 0; i < PeakInfo.size(); i++) {

			if (PeakInfo.get(i).NetCnt > k * Math.sqrt(2 * PeakInfo.get(i).Background_Net_Count)) {

			} else {
				PeakInfo.remove(i);
				--i;
			}

		}

		return PeakInfo;

	}

	public static int FindPeakSize(double[] DChSpec, double Theshold) {
		int cnt = 0;
		double a1, a2, b1, b2;

		for (int i = 3; i < CHSIZE; i++) {
			if (DChSpec[i] > Theshold) {
				a1 = DChSpec[i] - DChSpec[i + 1];
				a2 = DChSpec[i] - DChSpec[i - 1];

				b1 = DChSpec[i] - DChSpec[i + 2];
				b2 = DChSpec[i] - DChSpec[i - 2];

				if (a1 > 0 && a2 > 0 && b1 > 0 && b2 > 0) {

					cnt = cnt + 1;
				}
			}
		}

		return cnt;
	}

	public static double ROIAnalysis_GetTotCnt(double[] Smoothed_ChArray, int ROI_Start, int ROI_End) {
		try {
			double result = 0;
			for (int i = ROI_Start; i < ROI_End; i++) {
				result += Smoothed_ChArray[i];
			}
			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog("\n FindPeakN - ROIAnalysis_GetTotCnt");
			return 0;
		}
	}

	public static int ROIAnalysis(double[] Smoothed_ChArray, int ROI_Start, int ROI_End) {

		try {
			double MAX = 0;
			double AVG = 0;
			double SUM = 0;
			double Base_Pyuncha = 0;
			Vector<Double> roiSPC = new Vector<Double>();
			Vector<Double> Pyuncha = new Vector<Double>();
			for (int i = ROI_Start; i <= ROI_End; i++) {
				roiSPC.add(Smoothed_ChArray[i]);
				SUM += Smoothed_ChArray[i];
				if (Smoothed_ChArray[i] > MAX) {
					MAX = Smoothed_ChArray[i];
				}
			}
			AVG = SUM / roiSPC.size();

			for (int i = ROI_Start; i <= ROI_End; i++) {
				double bunsan_per = ((AVG - Smoothed_ChArray[i]) / MAX) * 100;
				Pyuncha.add(bunsan_per * bunsan_per);

				Base_Pyuncha += bunsan_per * bunsan_per;
			}
			AVG = Base_Pyuncha / roiSPC.size();
			Base_Pyuncha = Math.sqrt(AVG);

			if (Base_Pyuncha < 17)
				return 0;
			/////////
			double[] sst = new double[1024];
			for (int i = 0; i < 1024; i++) {
				sst[i] = Smoothed_ChArray[i];
			}

			double y2max = 0;
			double xmax = 0;
			double temp_y2max = 0;
			double x2max = 0;

			for (int n = ROI_Start; n < ROI_End; n++) {
				if (sst[n] > y2max)// && Smoothed_ChArray[n]>50)
				{
					if (sst[n - 10] < sst[n] && sst[n + 10] < sst[n]) {
						if (sst[n - 30] < 0.8 * sst[n] && sst[n + 30] < 0.8 * sst[n]) // 洹쇱쿂�쓽
						// 媛믩뱾�씠
						// �뵾�겕濡�
						// 遺��꽣
						// 湲됯꺽�엳
						// 媛먯냼�븯�뒗吏�
						// �뙋�떒
						{
							if (Math.abs(xmax - n) > 100) // 50ch 洹쇱쿂�쓽 �뵾�겕�뱾��
							// �씤�젙�븯吏� �븡�쓬
							{
								if (sst[n] > temp_y2max) {
									y2max = sst[n];
									x2max = n;
									temp_y2max = sst[n];
								}
							}
						}
					}
				}
			}

			ROI_End = NcLibrary.Auto_floor(Base_Pyuncha);
			return NcLibrary.Auto_floor(x2max);

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog("\n FindPeakN - ROIAnalysis");
			return 0;
		}
	}

	public static double[] Smooth(int[] ChArray, int ChArray_Size, int WindowSize, int repeat) {
		double[] result = new double[ChArray_Size];
		try {

			WindowSize = (WindowSize % 2 == 0) ? WindowSize + 1 : WindowSize; // Window
			// Size媛�
			// ���닔
			// 吏앹닔
			// �씤吏�
			// �뙋蹂꾪썑
			// ���닔濡�
			int WindowHalfSize = (WindowSize - 1) / 2; // Window Size�쓽 �젅諛�
			int Smooth_Start = ((WindowSize - 1) / 2); // 梨꾨꼸 �뜲�씠�꽣�뿉�꽌 �뒪臾대뵫�씠
			// �떆�옉�릺�뒗 吏��젏
			int Soomth_End = ChArray_Size - ((WindowSize - 1) / 2); // 梨꾨꼸
			// �뜲�씠�꽣�뿉�꽌
			// �뒪臾대뵫�씠
			// 醫낅즺�릺�뒗
			// 吏��젏

			double[] Smoothed_ChArray = new double[ChArray_Size];

			for (int i = Smooth_Start; i < Soomth_End; i++) // �뒪臾대뵫
			{
				Smoothed_ChArray[i] = 0;

				for (int k = 0; k < WindowSize; k++) {
					Smoothed_ChArray[i] += (ChArray[i - WindowHalfSize + k]);
				}
				Smoothed_ChArray[i] = (Smoothed_ChArray[i] / (double) WindowSize);

			}

			if (repeat > 1) // 諛섎났
			{
				for (int j = 0; j < repeat - 1; j++) {// 諛섎났 �슏�닔

					double[] Smoothed_TempArray = new double[ChArray_Size];

					for (int i = Smooth_Start; i < Soomth_End; i++)// �뒪臾대뵫
					{
						Smoothed_TempArray[i] = 0;
						for (int k = 0; k < WindowSize; k++) {
							Smoothed_TempArray[i] += (Smoothed_ChArray[i - WindowHalfSize + k]);
						}
						Smoothed_TempArray[i] = (Smoothed_TempArray[i] / (double) WindowSize);

					}

					for (int i = 0; i < ChArray_Size; i++) {// �뒪臾대뵫 寃곌낵 由ы꽩
						Smoothed_ChArray[i] = Smoothed_TempArray[i];
					}

				}

			}

			for (int i = 0; i < ChArray_Size; i++) // 理쒖쥌 寃곌낵 由ы꽩
			{
				result[i] = (Smoothed_ChArray[i] <= 0) ? 0 : Smoothed_ChArray[i];
			}

			return result;
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog("\n FindPeakN - Smooth");
			return result;
		}

	}

	public static String ListToString(int[] mlist) {
		String a = "";
		for (int i = 0; i < mlist.length; i++) {

			a += Integer.toString(mlist[i]) + ",";

		}

		return a;
	}

	public static int FindNearbyPeak(int[] mNewPeakCh, int mOldK40PeakCh, int mROI_Ch_start, int mROI_Ch_end) {

		int Min = 1000;
		int NearPeakCh = 0;
		int sum = 0;
		for (int i = 0; i < mNewPeakCh.length; i++) {
			if (mNewPeakCh[i] > mROI_Ch_start && mNewPeakCh[i] < mROI_Ch_end) {
				sum = mNewPeakCh[i] - mOldK40PeakCh;

				if (Min > Math.abs(sum)) {

					Min = Math.abs(sum);

					NearPeakCh = mNewPeakCh[i];

				}

			}

		}
		if (Math.abs(sum) == 0) {
			return mOldK40PeakCh;
		} else {

			return NearPeakCh;
		}

	}

	public static String SpectrumToString(double[] spectrum) {

		String SpectrumStr = "";
		for (int i = 0; i < spectrum.length; i++) {

			SpectrumStr += Double.toString(spectrum[i]) + ",";

		}

		return SpectrumStr;

	}

	public static String SpectrumToString(Spectrum spectrum) {

		String SpectrumStr = "";
		for (int i = 0; i < spectrum.Get_Ch_Size(); i++) {

			SpectrumStr += Double.toString(spectrum.at(i)) + ",";

		}

		return SpectrumStr;

	}

	public static double Channel_to_Energy(double Channel, double A, double B, double C) // 梨꾨꼸媛믪쓣
	// A,B媛믪뿉
	// �쓽�븳
	// �뿉�꼫吏�媛믪쑝濡�
	// 諛붽씔�떎.
	{
		double Result = 0;

		try {
			if (C == 0) {
				Result = (A * Channel) + B;
			} else {
				Result = (A * (Channel * Channel)) + (B * Channel) + C;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog("\n FindPeakN - Channel_to_Energy");
			return 0;
		}

		return Result;
	}

	public static int[] testspectrum(String mSpec, String SplitUnit) {

		String[] mSpecSplit = mSpec.split(SplitUnit);
		int[] mSpecInt = new int[1024];

		for (int i = 0; i < mSpecInt.length; i++) {
			double a = Double.valueOf(mSpecSplit[i]).doubleValue();

			mSpecInt[i] = (int) a;

		}

		return mSpecInt;
	}

	public static double Get_AvgCPS(int[] SpecList, int AcqTime) {
		int Result = 0;
		for (int i = 0; i < SpecList.length; i++) {
			Result += SpecList[i];
		}
		if (Result == 0)
			return 0;
		return Result / AcqTime;
	}

}
