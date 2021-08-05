package android.HH100.Identification;

import java.io.File;
import java.math.*;
import java.util.Arrays;
import java.util.Vector;

import android.HH100.Control.*;
import android.HH100.R.string;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;
import android.annotation.SuppressLint;
import android.content.*;
import android.util.Log;
import android.view.*;

class VallyResult {
	double A;
	double B;
	double Ax;
	double Bx;
}

public class FindPeaksM implements FindPeakMode{

	public static boolean D = true;
	public static String TAG = "FindPeakM";

	public FindPeaksM(Context context) {

		// super(context);

		// TODO Auto-generated constructor stub

	}

	public FindPeaksM() {

		// super(context);

		// TODO Auto-generated constructor stub

	}

	public final int NUM_CH = 1024;

	public final int RNUM_CH = 256;

	double[] FP_m_BkgData = new double[NUM_CH];

	double[] FP_m_BkgTemplate_extend = new double[NUM_CH + 100];

	double[] FP_m_BkgTemplate = new double[NUM_CH]; // Background Radiation
	// Template (CHANNEL)

	double[] FP_m_BkgTemplate_EN = new double[6000]; // Background Radiation
	// Template (ENERGY)

	double[] FP_m_NormalizedBkgTemplate = new double[NUM_CH]; // Normalized
	// Background
	// Radiation
	// Template

	double[] FP_m_ChCnt = new double[NUM_CH];

	double[] FP_m_temp = new double[NUM_CH];

	double[] FP_m_BiasReduced = new double[NUM_CH]; // BTS

	double[] FP_m_nor_sbt = new double[NUM_CH];

	double[] FP_m_WaveletTransformed_s = new double[NUM_CH]; // smooth coeff.

	double[] FP_m_WaveletTransformed_d = new double[NUM_CH]; // detail coeff.

	double[][] FP_m_PrincipalComponent = new double[NUM_CH][3]; // Principal
	// Component

	double[] FP_m_PrincipalComponent_D = new double[NUM_CH]; // Principal
	// Component,
	// dynamic
	// memory
	// allocation

	double[] FP_m_FirstPC = new double[NUM_CH]; // First Principal Component

	double[] FP_m_temp_Cnt = new double[NUM_CH];

	double[] FP_log_data = new double[NUM_CH];

	double[] FP_log_bkg = new double[NUM_CH];

	double[] FP_log_sbt = new double[NUM_CH];

	int[] FP_m_FindPeaksData = new int[100]; // OUTPUT : Peaks (CHANNEL)

	int[] FP_m_FindPeaksData_EN = new int[100]; // OUTPUT : Peaks (ENERGY)

	int[] FP_m_ValleyData = new int[200]; // OUTPUT : Peaks (CHANNEL)

	int[] FP_m_ValleyData_EN = new int[200]; // OUTPUT : Peaks (ENERGY)

	int FP_Len_FindPeaksData; // OUTPUT : Length of Peaks

	int FP_NumOfData_PCA;

	float FP_Threshold_WBPD = 0.01f;

	double calib_A1, calib_B1;

	double calib_A2, calib_B2, calib_C2;

	double BkgTemplateNormalizedFactor1;

	double BkgTemplateNormalizedFactor2;

	double FP_A_max;

	double FP_A_min;

	double FP_B_max;

	double FP_B_min;

	int sample_cnt;

	int sample_cnt_bkg;

	int WAVELET_LEVEL = 2; // 6

	int Array_bias = 0;

	// FILE *FilePtr;

	public double SIGN(double a, double b) {

		if (b < 0)
			return Math.abs(a) * (-0.1f);

		else
			return Math.abs(a);

	}

	int bk_time;

	int cn_time;

	double BBTH;

	double[] tempArray = new double[1024];

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class FindPC1

	{

		int ROW = 1024;

		int COL = 0;

		void FindPC_main(double[] dataTemp, double[] FirstPC, int NumOfData)
		{
			int n, m, i, j, k, k2;

			double symmat2, evals = 0, interm = 0;

			double in_value;

			char option;// , strncpy();

			n = ROW;

			m = COL = NumOfData;

			double[] data = new double[n + 1];

			double symmat = 0;

			for (i = 1; i <= n; i++)
			{
				data[i] = dataTemp[i - 1];// CFPeaks.FP_m_PrincipalComponent[i-1][j-1];
			}

			corcol(data, n, m, symmat);

			for (i = 1; i <= n; i++) {
				FirstPC[i - 1] = data[i];
			}
		}

		void tqli(double d, double e, int n, double z)
		{
			int m, l, iter, i, k;

			double s, r, p, g, f, dd, c, b;

			e = 0.0;

			z = 0.0;

			for (l = 1; l <= n; l++)
			{
				iter = 0;

				for (m = l; m <= n - 1; m++)

				{

					dd = Math.abs(d);

					if (Math.abs(e) + dd == dd)
						break;

				}

			}

		}

		void tred2(double a, int n, double d, double e)

		{

			int l, k, j, i;

			double scale, hh, h, g, f;

			d = 0.0;

			e = 0.0;

			d = a;

			a = 0.0;

		}

		void corcol(double[] data, int n, int m, double symmat)

		{

			double eps = 0.005;

			double x, mean = 0, stddev = 0;// , *vector();

			int i, j, j1, j2;

			for (j = 1; j <= m; j++)

			{

				mean = 0.0;

				for (i = 1; i <= n; i++)

				{

					mean += data[i];

				}

				mean /= n;

			}

			for (j = 1; j <= m; j++)

			{

				stddev = 0.0;

				for (i = 1; i <= n; i++)

				{

					stddev += ((data[i] - mean) *

							(data[i] - mean));

				}

				stddev /= n;

				stddev = Math.sqrt(stddev);

				if (stddev <= eps)
					stddev = 1.0;

			}

			for (i = 1; i <= n; i++)

			{

				for (j = 1; j <= m; j++)

				{

					// min data[i] -= mean;

					x = Math.sqrt(n);

					x *= stddev;

					data[i] /= x;

				}

			}

			symmat = 1.0;

		}

	}

	public Vector<NcPeak> Find_Peak(Spectrum MS, Spectrum BG) {

		float input_PeakDetectionThreshold = 0.005f;

		int[] Energy = new int[100];
		int[] PeaksCh = new int[100];

		int[] VallyEn = new int[200];
		int[] VallyCh = new int[200];
		double[] VallyAB = new double[200];
		int aaw = 0;
		int Peaks_count = 0;

		//
		calib_A2 = MS.Get_Coefficients().get_Coefficients()[0];
		calib_B2 = MS.Get_Coefficients().get_Coefficients()[1];
		calib_C2 = MS.Get_Coefficients().get_Coefficients()[2];
		int input_NumOfData = 1;

		for (int i = 0; i < NUM_CH; i++) {
			FP_m_ChCnt[i] = FP_m_FirstPC[i] = 0;
		}
		FP_NumOfData_PCA = 1;

		FP_Threshold_WBPD = input_PeakDetectionThreshold;

		//
		int energy;

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_ChCnt[i] = MS.at(i);

		}

		bk_time = (int)BG.Get_AcqTime();
		cn_time = (int)MS.Get_AcqTime();

		FP_m_BkgData = BG.ToDouble();

		for (int i = 0; i < NUM_CH; i++) {
			// FP_m_BkgTemplate[i] = 0;

			FP_m_BkgTemplate[i] = FP_m_BkgData[i];

			FP_m_BkgTemplate_EN[i] = 0;

		}
		ft_smooth(FP_m_BkgTemplate, 0.015, 2.96474);
		// M.Comment 1
		// 백그라운드를 빼는 부분
		FindPeaksM_BTS();

		// M.Comment 2
		// 로그를 씌울때 1을 더해줘서 로그를 풀때 최소값이 0이 아닌 1로 나왔음.
		// 전체적으로 스펙트럼이 높아져 있는 것을 낮추어 주는 부분임.
		// 원래는 -1만 하였으나 정보량이 부족하여 2를 곱한 후 2를 빼게 되었음.
		for (int i = 0; i < NUM_CH; i++) {
			FP_m_WaveletTransformed_s[i] = FP_m_BiasReduced[i] * 2 - 2;
		}

		ft_smooth(FP_m_WaveletTransformed_s, 0.015, 2.96474);

		for (int i = 0; i < 100; i++) {

			PeaksCh[i] = FP_m_FindPeaksData[i] = 0;
			Energy[i] = FP_m_FindPeaksData_EN[i] = 0;

		}

		for (int i = 0; i < 200; i++) {

			VallyCh[i] = FP_m_ValleyData[i] = 0;
			VallyEn[i] = FP_m_ValleyData_EN[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = FP_m_WaveletTransformed_s[i];

		}

		// M.Comment 3
		// 표준화 시켜주는 부분임.
		// 스펙트럼 값에서 평균을 빼고 표준편차로 나누어 주게 됨.
		// 특이사항이 없는 한 변동이 없는 편이 좋음.
		FindPeaksM_PCA();

		// M.Comment 4
		// 최종 피크를 찾기 전에 음수인 부분이 없도록 전체적으로 스펙트럼을 x축 위로 올리는 부분임.
		// FP_m_FirstPC를 이용하여 피크를 찾음.
		double mmiinn = 0;

		for (int i = 1; i < NUM_CH; i++) {

			if (i == 1) {

				if (FP_m_FirstPC[0] < FP_m_FirstPC[1])

					mmiinn = FP_m_FirstPC[0];

				else

					mmiinn = FP_m_FirstPC[1];

			}

			else {

				if (FP_m_FirstPC[i] < mmiinn)

					mmiinn = FP_m_FirstPC[i];

			}

		}

		for (int i = 1; i < NUM_CH; i++) {

			FP_m_FirstPC[i] = FP_m_FirstPC[i] - mmiinn;

		}

		// M.Comment 5
		// 피크찾는 부분임.
		Vector<String> temp = new Vector<String>();
		for (int i = 0; i < 1024; i++) {
			temp.add(String.format("%.4f", FP_m_FirstPC[i]));
		}

		FindPeaksM_WBPD(FP_m_FirstPC, FP_m_FindPeaksData, FP_Len_FindPeaksData);

		// M.Comment 6
		// 밸리찾는 부분
		// 5pt를 이용하여 찾음. 현재는 FP_log_data를 이용하였음.
		float resultA = 0;
		float resultB = 0;
		int resultAx = 0;
		int resultBx = 0;
		int ValCount = 0;

		double[] VallySPC = new double[1024]; // 이하 밸리는 찾는다.
		for (int i = 0; i < 1024; i++) {
			VallySPC[i] = FP_m_ChCnt[i] - ((FP_m_BkgData[i] / (double) bk_time) * (double) cn_time);
		}

		ft_smooth(VallySPC, 0.015, 2.96474);// 0.007413
		// NcLibrary.Export_spectrum_data(VallySPC, 1024);

		for (int i = 0; i < FP_Len_FindPeaksData; i++) {
			double En = NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2);
			double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(En);
			double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
			double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

			// double factor = 0.2;
			// if(En < 63) factor = 0.2;

			L_ROI_Percent = (NcLibrary.Energy_to_Channel(En * L_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1-factor);
			R_ROI_Percent = (NcLibrary.Energy_to_Channel(En * R_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1+factor);

			VallyResult Result = Find_Vally(VallySPC, (int) L_ROI_Percent, (int) R_ROI_Percent, FP_m_FindPeaksData[i],
					resultA, resultB, resultAx, resultBx);

			FP_m_ValleyData_EN[ValCount] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Ax, calib_A2, calib_B2, calib_C2));
			FP_m_ValleyData_EN[ValCount + 1] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Bx, calib_A2, calib_B2, calib_C2));

			VallyAB[ValCount] = Result.A;
			VallyAB[ValCount + 1] = Result.B;
			FP_m_ValleyData[ValCount] = (int) Result.Ax;
			FP_m_ValleyData[ValCount + 1] = (int) Result.Bx;

			ValCount += 2;
		}
		/////////////////
		for (int i = 0; i < FP_Len_FindPeaksData; i++) {

			PeaksCh[i] = FP_m_FindPeaksData[i];
			Energy[i] = FP_m_FindPeaksData_EN[i] = (int) Math
					.rint(NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2));
			if (FP_m_FindPeaksData_EN[i] < 0) {
				Energy[i] = FP_m_FindPeaksData_EN[i] = 0;
			}

			for (int j = 0; j < 2; j++) {

				VallyCh[2 * i + j] = FP_m_ValleyData[2 * i + j];
				VallyEn[2 * i + j] = FP_m_ValleyData_EN[2 * i + j];// = (int)
				// Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i+j],calib_A2,calib_B2,calib_C2));

				if (FP_m_ValleyData_EN[2 * i + j] < 0) {
					VallyEn[2 * i + j] = FP_m_ValleyData_EN[2 * i + j] = 0;
				}

			}

		}
		/////////////// M.Comment 6 랑 대비
		Peaks_count = FP_Len_FindPeaksData;

		Vector<NcPeak> Result = new Vector<NcPeak>();

		for (int i = 0; i < Peaks_count; i++) {
			NcPeak PData_temp = new NcPeak();
			PData_temp.Channel = PeaksCh[i];
			PData_temp.Peak_Energy = Energy[i];
			PData_temp.ROI_Left = FP_m_ValleyData[i * 2];
			PData_temp.ROI_Right = FP_m_ValleyData[(i * 2) + 1];

			double Coefficients[] = new double[2];
			Coefficients[0] = VallyAB[0];
			Coefficients[1] = VallyAB[1];
			// PData_temp.Vally_Coefficients = Coefficients;
			PData_temp.BG_a = VallyAB[0];
			PData_temp.BG_b = VallyAB[1];

			Result.add(PData_temp);
		}

		String log = "Found Peaks - ";
		for (int i = 0; i < Result.size(); i++) {
			log += Result.get(i).Channel + "(" + Result.get(i).Peak_Energy + " Kev),  ";

		}
		if (D)
			Log.i(TAG, log);// 33
		//
		return Result;
	}

	public void FindPeaksM_Main(int[] input_m_ChCnt, int[] m_FindPeaksData_EN, int[] m_FindPeaksData,
								int[] m_ValleyData_EN, int[] m_ValleyData, double[] m_VallyAB, int Len_FindPeaksData, int t_bkg, int t_cnt,
								float input_PeakDetectionThreshold) {

		int input_NumOfData = 1;

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_ChCnt[i] = FP_m_FirstPC[i] = 0;

		}

		FP_NumOfData_PCA = 1;

		FP_Threshold_WBPD = input_PeakDetectionThreshold;

		/////////////////////////////

		int energy;

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_ChCnt[i] = input_m_ChCnt[i];

		}

		bk_time = t_bkg;

		cn_time = t_cnt;

		// M.Comment 1
		// 백그라운드를 빼는 부분
		FindPeaksM_BTS();

		// M.Comment 2
		// 로그를 씌울때 1을 더해줘서 로그를 풀때 최소값이 0이 아닌 1로 나왔음.
		// 전체적으로 스펙트럼이 높아져 있는 것을 낮추어 주는 부분임.
		// 원래는 -1만 하였으나 정보량이 부족하여 2를 곱한 후 2를 빼게 되었음.
		for (int i = 0; i < NUM_CH; i++) {
			FP_m_WaveletTransformed_s[i] = FP_m_BiasReduced[i] * 2 - 2;
		}

		ft_smooth(FP_m_WaveletTransformed_s, 0.015, 2.96474);

		for (int i = 0; i < 100; i++) {

			m_FindPeaksData[i] = FP_m_FindPeaksData[i] = 0;

			m_FindPeaksData_EN[i] = FP_m_FindPeaksData_EN[i] = 0;

		}

		for (int i = 0; i < 200; i++) {

			m_ValleyData[i] = FP_m_ValleyData[i] = 0;

			m_ValleyData_EN[i] = FP_m_ValleyData_EN[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = FP_m_WaveletTransformed_s[i];

		}

		// M.Comment 3
		// 표준화 시켜주는 부분임.
		// 스펙트럼 값에서 평균을 빼고 표준편차로 나누어 주게 됨.
		// 특이사항이 없는 한 변동이 없는 편이 좋음.
		FindPeaksM_PCA();

		// M.Comment 4
		// 최종 피크를 찾기 전에 음수인 부분이 없도록 전체적으로 스펙트럼을 x축 위로 올리는 부분임.
		// FP_m_FirstPC를 이용하여 피크를 찾음.
		double mmiinn = 0;

		for (int i = 1; i < NUM_CH; i++) {

			if (i == 1) {

				if (FP_m_FirstPC[0] < FP_m_FirstPC[1])

					mmiinn = FP_m_FirstPC[0];

				else

					mmiinn = FP_m_FirstPC[1];

			}

			else {

				if (FP_m_FirstPC[i] < mmiinn)

					mmiinn = FP_m_FirstPC[i];

			}

		}

		for (int i = 1; i < NUM_CH; i++) {

			FP_m_FirstPC[i] = FP_m_FirstPC[i] - mmiinn;

		}

		// M.Comment 5
		// 피크찾는 부분임.
		FindPeaksM_WBPD(FP_m_FirstPC, FP_m_FindPeaksData, FP_Len_FindPeaksData);

		// M.Comment 6
		// 밸리찾는 부분
		// 5pt를 이용하여 찾음. 현재는 FP_log_data를 이용하였음.
		float resultA = 0;
		float resultB = 0;
		int resultAx = 0;
		int resultBx = 0;
		int ValCount = 0;

		double[] VallySPC = new double[1024]; // 이하 밸리는 찾는다.
		for (int i = 0; i < 1024; i++) {
			VallySPC[i] = FP_m_ChCnt[i] - ((FP_m_BkgData[i] / (double) bk_time) * (double) cn_time);
		}

		ft_smooth(VallySPC, 0.015, 2.96474);// 0.007413
		// NcLibrary.Export_spectrum_data(VallySPC, 1024);
		for (int i = 0; i < FP_Len_FindPeaksData; i++) {
			double En = NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2);
			double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(En);
			double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
			double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

			// double factor = 0.2;
			// if(En < 63) factor = 0.2;

			L_ROI_Percent = (NcLibrary.Energy_to_Channel(En * L_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1-factor);
			R_ROI_Percent = (NcLibrary.Energy_to_Channel(En * R_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1+factor);

			VallyResult Result = Find_Vally(VallySPC, (int) L_ROI_Percent, (int) R_ROI_Percent, FP_m_FindPeaksData[i],
					resultA, resultB, resultAx, resultBx);

			FP_m_ValleyData_EN[ValCount] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Ax, calib_A2, calib_B2, calib_C2));
			FP_m_ValleyData_EN[ValCount + 1] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Bx, calib_A2, calib_B2, calib_C2));

			m_VallyAB[ValCount] = Result.A;
			m_VallyAB[ValCount + 1] = Result.B;
			FP_m_ValleyData[ValCount] = (int) Result.Ax;
			FP_m_ValleyData[ValCount + 1] = (int) Result.Bx;

			/*
			 * if(Result.Ax != (int)L_ROI_Percent | Result.Bx !=
			 * (int)R_ROI_Percent){
			 *
			 * Isotope isotope = new Isotope(); isotope.Channel1 =
			 * FP_m_FindPeaksData[i]; isotope.Energy1 = FP_m_FindPeaksData_EN[i];
			 * isotope.Channel1_Vally.x = FP_m_ValleyData[ValCount];
			 * isotope.Channel1_Vally.y = FP_m_ValleyData[ValCount+1];
			 * isotope.Channel1_AB.x = (float) m_VallyAB[ValCount];
			 * isotope.Channel1_AB.y = (float) m_VallyAB[ValCount+1];
			 *
			 * temp.add(isotope); }
			 */
			ValCount += 2;
		}
		/*
		 * FP_Len_FindPeaksData = temp.size(); //// for(int i=0; i<100; i++){
		 *
		 * m_FindPeaksData[i] = FP_m_FindPeaksData[i] = 0;
		 *
		 * m_FindPeaksData_EN[i] = FP_m_FindPeaksData_EN[i] = 0;
		 *
		 * }
		 *
		 * for(int i=0; i<200; i++){
		 *
		 * m_ValleyData[i] = FP_m_ValleyData[i] = 0;
		 *
		 * m_ValleyData_EN[i] = FP_m_ValleyData_EN[i] = 0;
		 *
		 * } *
		 *
		 *
		 * for(int i=0; i<temp.size(); i++){
		 * FP_m_FindPeaksData[i]=(int)temp.get(i).Channel1;
		 * FP_m_FindPeaksData_EN[i] = (int) temp.get(i).Energy1;
		 * FP_m_ValleyData[i*2] = temp.get(i).Channel1_Vally.x;
		 * FP_m_ValleyData[(i*2)+1] = temp.get(i).Channel1_Vally.y;
		 * m_VallyAB[i*2] = temp.get(i).Channel1_AB.x; m_VallyAB[(i*2)+1] =
		 * temp.get(i).Channel1_AB.y; }
		 */
		/*
		 * for(int i=0; i<FP_Len_FindPeaksData; i++){
		 *
		 * double delta_5pt1=0; double delta_5pt2=0; int countt=0; int
		 * count_5pt=0; int temp=0;
		 *
		 * for(int lv=FP_m_FindPeaksData[i]; lv>FP_m_ValleyData[2*i];lv--) { if
		 * ((FP_m_FindPeaksData[i]-FP_m_ValleyData[2*i])<5 &&
		 * FP_m_ValleyData[2*i]<0) { m_ValleyData[2*i] = 0; m_ValleyData_EN[2*i]
		 * = FP_m_ValleyData_EN[2*i] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(0,calib_A2,calib_B2,calib_C2));
		 * break; } if (countt==0){ delta_5pt1 =
		 * (FP_log_data[lv]-FP_log_data[lv-4])/4.0; delta_5pt2 =
		 * (FP_log_data[lv]-FP_log_data[lv-4])/4.0; countt=1; continue; } else {
		 * delta_5pt1= delta_5pt2; delta_5pt2=
		 * (FP_log_data[lv]-FP_log_data[lv-4])/4.0; }
		 *
		 * if (temp==0 && delta_5pt1-delta_5pt2>0) { temp=1; count_5pt++; }
		 *
		 * if (temp==1 && delta_5pt1-delta_5pt2>0) { temp=1; count_5pt++; } else
		 * { temp=0; count_5pt=0; }
		 *
		 * if (count_5pt==4) { m_ValleyData[2*i] = lv; m_ValleyData_EN[2*i] =
		 * FP_m_ValleyData_EN[2*i] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(lv,calib_A2,calib_B2,calib_C2))
		 * ; break; } else if ( count_5pt<4 && lv==FP_m_ValleyData[2*i]+1){
		 * m_ValleyData[2*i] = FP_m_ValleyData[2*i]; m_ValleyData_EN[2*i] =
		 * FP_m_ValleyData_EN[2*i] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i],calib_A2,
		 * calib_B2,calib_C2)); break; }
		 *
		 * }
		 *
		 * delta_5pt1=0; delta_5pt2=0; countt=0; count_5pt=0; temp=0;
		 *
		 * for(int rv=FP_m_FindPeaksData[i]; rv<FP_m_ValleyData[2*i+1];rv++) {
		 * if ((FP_m_ValleyData[2*i+1]-FP_m_FindPeaksData[i])<5) {
		 * m_ValleyData[2*i+1] = FP_m_ValleyData[2*i+1]; m_ValleyData_EN[2*i+1]
		 * = FP_m_ValleyData_EN[2*i+1] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i+1],calib_A2
		 * ,calib_B2,calib_C2)); break; } if (countt==0){ delta_5pt1 =
		 * (FP_log_data[rv]-FP_log_data[rv+4])/4.0; delta_5pt2 =
		 * (FP_log_data[rv]-FP_log_data[rv+4])/4.0; countt=1; continue; } else {
		 * delta_5pt1= delta_5pt2; delta_5pt2=
		 * (FP_log_data[rv]-FP_log_data[rv+4])/4.0; }
		 *
		 * if (temp==0 && delta_5pt1-delta_5pt2>0) { temp=1; count_5pt++; }
		 *
		 * if (temp==1 && delta_5pt1-delta_5pt2>0) { temp=1; count_5pt++; } else
		 * { temp=0; count_5pt=0; }
		 *
		 * if (count_5pt==4) { m_ValleyData[2*i+1] = rv; m_ValleyData_EN[2*i+1]
		 * = FP_m_ValleyData_EN[2*i+1] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(rv,calib_A2,calib_B2,calib_C2))
		 * ; break; } else if ( count_5pt<4 && rv==FP_m_ValleyData[2*i+1]-1){
		 * m_ValleyData[2*i+1] = FP_m_ValleyData[2*i+1]; m_ValleyData_EN[2*i+1]
		 * = FP_m_ValleyData_EN[2*i+1] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i+1],calib_A2
		 * ,calib_B2,calib_C2)); break; }
		 *
		 * } m_FindPeaksData[i] = FP_m_FindPeaksData[i];
		 *
		 * m_FindPeaksData_EN[i] = FP_m_FindPeaksData_EN[i] = (int)
		 * Math.rint(NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i],calib_A2,
		 * calib_B2,calib_C2));
		 *
		 * if(FP_m_FindPeaksData_EN[i]<0) {
		 * m_FindPeaksData_EN[i]=FP_m_FindPeaksData_EN[i]=0;}
		 *
		 *
		 * }
		 */

		/////////////////
		for (int i = 0; i < FP_Len_FindPeaksData; i++) {

			m_FindPeaksData[i] = FP_m_FindPeaksData[i];

			m_FindPeaksData_EN[i] = FP_m_FindPeaksData_EN[i] = (int) Math
					.rint(NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2));

			if (FP_m_FindPeaksData_EN[i] < 0) {
				m_FindPeaksData_EN[i] = FP_m_FindPeaksData_EN[i] = 0;
			}

			for (int j = 0; j < 2; j++) {

				m_ValleyData[2 * i + j] = FP_m_ValleyData[2 * i + j];

				m_ValleyData_EN[2 * i + j] = FP_m_ValleyData_EN[2 * i + j];// =
				// (int)
				// Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i+j],calib_A2,calib_B2,calib_C2));

				if (FP_m_ValleyData_EN[2 * i + j] < 0) {
					m_ValleyData_EN[2 * i + j] = FP_m_ValleyData_EN[2 * i + j] = 0;
				}

			}

		}
		/////////////// M.Comment 6 랑 대비

		Len_FindPeaksData = FP_Len_FindPeaksData;

	}

	public void FindPeaksM_BTS() {
		float tempval = 0;
		float prst = 0;

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_temp_Cnt[i] = FP_m_ChCnt[i];

		}

		ft_smooth(FP_m_temp_Cnt, 0.015, 2.96474);

		for (int i = 0; i < NUM_CH; i++) {
			FP_m_BiasReduced[i] = 0;
			FP_m_NormalizedBkgTemplate[i] = 0;
			FP_m_nor_sbt[i] = 0;
		}

		/////////////////////////////// log version

		// M.Comment 1-1
		// 백그라운드를 측정시간에 맞추는 것이 아니라 백그라운드와 측정 스펙트럼 모두 1초 데이터로 만들어 준 후 빼게 됨.
		// 이 부분은 백그라운드를 빼기 전에 백그라운드를 1초데이터로 만드는 부분
		for (int i = 0; i < NUM_CH; i++) {

			FP_m_NormalizedBkgTemplate[i] = FP_m_BkgTemplate[i] / bk_time;

		}

		for (int i = 0; i < NUM_CH; i++) {

			// M.Comment 1-2
			// 스펙트럼을 1초데이터로 만든 후 로그를 씌우는 부분
			// 여기서 1을 더한 이유는 log(0)을 방지하기 위함임.
			// 나중에 저 부분은 로그를 풀어준 다음에 정상화 됨.
			FP_log_data[i] = Math.log10(FP_m_temp_Cnt[i] / cn_time + 1) / Math.log10(i + 10.0);

			// 백그라운드 로그
			FP_log_bkg[i] = Math.log10(FP_m_NormalizedBkgTemplate[i] + 1) / Math.log10(i + 10.0);

			// 로그 데이터끼리 빼줌.
			FP_log_sbt[i] = FP_log_data[i] - FP_log_bkg[i];

			// 백그라운드 보다 스펙트럼이 작으면 0으로 만들어줌.
			if (FP_log_sbt[i] < 0)

				FP_log_sbt[i] = 0;

		}

		// M.Comment 1-3
		// 로그 데이터를 exponentional로 풀어줌.
		// 로그는 10으로 하고 푸는건 exp로 푸는 이유는 정보량을 보다 증가시키려고 하였음.
		for (int i = 0; i < NUM_CH; i++) {

			FP_m_BiasReduced[i] = Math.pow(Math.exp(1), FP_log_sbt[i]);

		}

		////////////////////////// Linear version

		// M.Comment 1-4
		// 일반적인 백그라운드 제거 방법도 들어가 있음.
		// FP_m_nor_sbt 이 배열이 나중에 peak 검사하는데 사용됨.
		for (int i = 0; i < NUM_CH; i++) {

			tempval = (float) (FP_m_temp_Cnt[i] / cn_time - FP_m_NormalizedBkgTemplate[i]);

			if (FP_m_temp_Cnt[i] / cn_time <= 0 || (FP_m_temp_Cnt[i] / cn_time < FP_m_NormalizedBkgTemplate[i])) {

				prst = 1;

			}

			else {

				prst = (float) (tempval / (FP_m_temp_Cnt[i] / cn_time));

			}

			FP_m_nor_sbt[i] = tempval * prst;

		}
		/*
		 * Vector<String> temp = new Vector<String>(); for(int i=0; i<1024;i++)
		 * { temp.add(String.format("%.4f", FP_m_nor_sbt[i])); }
		 */
		//////////////////////////////

	}

	private void FindPeaksM_PCA() {

		FindPC1 CFPC = new FindPC1();

		CFPC.FindPC_main(FP_m_PrincipalComponent_D, FP_m_FirstPC, FP_NumOfData_PCA);

		if (FindMax(FP_m_FirstPC) < (FindMin(FP_m_FirstPC) * (-1))) {

			for (int i = 0; i < NUM_CH; i++) {

				FP_m_FirstPC[i] *= -1;

			}

		}

	}

	@SuppressLint("DefaultLocale")
	public void FindPeaksM_WBPD(double[] w, int[] Peaks, int Len)

	{

		int i, j, wlevel;

		int cnt = 0, cnt_p = 0, cnt_m = 0, len = 0, zeroCnt = 0;

		double[] PmaxVal = new double[10];

		double[] PminVal = new double[10];

		double[] PmaxVal_tmp = new double[10];

		double[] PminVal_tmp = new double[10];

		double[] wstmp = new double[NUM_CH];

		double[] wdtmp = new double[NUM_CH];

		double[] wdtmp_tmp = new double[NUM_CH];

		int[] tempPP = new int[100];

		for (j = 0; j < 100; j++) {

			tempPP[j] = 0;

		}

		double deltta = FP_Threshold_WBPD;

		double peakdet_temp = 0;

		double mn = 1.7 * Math.pow(10.0, 308);

		double mx = -1.7 * 1.7 * Math.pow(10.0, 308);

		int mnpos = 0, mxpos = 0;

		boolean lookformax = true;

		int iddxx = 0;

		int maxvalue = 0;

		int tmpenergy = 0;
		int right = 0;
		int left = 0;

		int energy_max = 0;
		int energy_right = 0;
		int energy_left = 0;

		// M.Comment 5-1
		// 기본적으로 피크찾는 방법은 시작점부터 하나씩 가며 Max 값을 찾음
		// Max 값을 찾으면 내려가며 Max-현재값 > Threshold 가 넘는 지점이 생기면 Max 를 peak로 인정함.
		// 헌데 peak 모양에 대한 파라미터가 전혀 없어서 peak가 아닌 것도 찾을 수 있음.
		// 그래서 앞뒤 7프로를 검사하여 기본적으로 삼각형 모양을 갖는지 추가로 검사하게 됨.
		// 또한 백그라운드 뺄때 FP_m_nor_sbt 를 찾아봐서 적당한 카운트를 지니고 있는지 또한 검사하게 됨.

		/*
		 * String temp[] = new String[1024]; for(int i1=0; i1<1024; i1++){
		 * temp[i1]= String.format("%.6f", FP_m_nor_sbt[i1]); } String isotope[] =
		 * new String[1024]; for(int i1=0; i1<1024; i1++){ isotope[i1]=
		 * String.format("%.6f", FP_log_data[i1]); }
		 *
		 * Spectrum ss = new Spectrum(FP_m_nor_sbt); Spectrum ss2 = new
		 * Spectrum(FP_log_data);
		 *
		 * NcLibrary.Export_ToTextFile("SAM"+File.separator+"FindPeaks_Result",
		 * "\r\n"+ss.ToString_Double()+";"+ss2.ToString_Double());
		 * NcLibrary.Export_ToTextFile("SAM"+File.separator+"FindPeaks_Result",
		 * ";"+calib_A2+";"+calib_B2+";"+calib_C2);
		 */
		for (i = 1; i < NUM_CH; i++) {

			if (i > 800)
				deltta = 0.004;

			peakdet_temp = w[i];

			if (peakdet_temp > mx) {

				mx = peakdet_temp;

				mxpos = i;

			}

			if (peakdet_temp < mn) {

				mn = peakdet_temp;

				mnpos = i;

			}

			if (lookformax) {

				if (peakdet_temp < (mx - deltta) && mxpos > 3) {
					// 백그라운드를 빼면서 peak 위치가 변하는 것에 대한 보정을 해줌.
					// peak는 FP_m_FirstPC로 찾고 보정은 FP_log_data(원본 스펙트럼 로그취한 값)로
					// 하게 됨.
					// 실험적으로 제일 좋았음.
					for (int xxx = mxpos - 4; xxx < mxpos + 4; xxx++) {
						if (xxx == mxpos - 4) {
							maxvalue = xxx;
							continue;
						}

						if (FP_log_data[maxvalue] < FP_log_data[xxx]) {

							maxvalue = xxx;

						}
					}

					for (int j1 = maxvalue + 1; j1 < 1024; j1++) {

						energy_max = (int) Math
								.rint(NcLibrary.Channel_to_Energy(maxvalue, calib_A2, calib_B2, calib_C2));

						energy_right = (int) Math.rint(NcLibrary.Channel_to_Energy(j1, calib_A2, calib_B2, calib_C2));

						if (energy_right - energy_max > energy_max * 0.07) {
							right = j1;
							break;
						}
					}

					for (int j1 = maxvalue - 1; j1 > -1; j1--) {

						energy_max = (int) Math
								.rint(NcLibrary.Channel_to_Energy(maxvalue, calib_A2, calib_B2, calib_C2));

						energy_left = (int) Math.rint(NcLibrary.Channel_to_Energy(j1, calib_A2, calib_B2, calib_C2));

						if (energy_max - energy_left > energy_max * 0.07) {
							left = j1;
							break;
						}
					}

					// if (FP_log_data[right]==0 || FP_log_data[left]==0)
					// {
					// continue;
					// }

					// 또한 앞뒤 7프로에서 피크성분을 지니는지 또한 FP_log_data로 검사하게됨.
					// 적당량 카운트를 지니는건 FP_m_nor_sbt를 사용함.
					if (((FP_log_data[maxvalue] - FP_log_data[right] > deltta)
							&& (FP_log_data[maxvalue] - FP_log_data[left] > deltta)) && (FP_m_nor_sbt[maxvalue] > 0.1)) // wavelet
					// 0.7
					{
						// NcLibrary.Export_ToTextFile("SAM"+File.separator+"FindPeaks_Result",";"+left+";"+right+";"+maxvalue+";"+FP_log_data[left]+";"+FP_log_data[right]+";"+FP_log_data[maxvalue]);

						mn = peakdet_temp;
						mnpos = i;
						// 찾은 peak와 원본 스펙트럼에서 있을지 모르는 peak shift 에 대한 보정임.
						// 아까는 FP_log_data로 하였고 지금은 원본 스펙트럼을 가지고 보정함.
						for (int xxx = mxpos - 4; xxx < mxpos + 4; xxx++) {
							if (xxx == mxpos - 4)

							{

								maxvalue = xxx;

								continue;

							}

							if (FP_m_ChCnt[maxvalue] < FP_m_ChCnt[xxx]) {

								maxvalue = xxx;

							}

						}

						// 밸리찾는 부분임.
						// 현재 앞뒤 7프로를 밸리로 판단하여 값을 넘겨주게 되어있음.
						// 2013.0911 수정예정임.
						for (int j1 = maxvalue + 1; j1 < 1024; j1++) {

							energy_max = (int) Math
									.rint(NcLibrary.Channel_to_Energy(maxvalue, calib_A2, calib_B2, calib_C2));

							energy_right = (int) Math
									.rint(NcLibrary.Channel_to_Energy(j1, calib_A2, calib_B2, calib_C2));

							if (energy_right - energy_max > energy_max * 0.10) //// M.Comment
							//// 6
							//// window
							{
								right = j1;
								break;
							}
						}

						for (int j1 = maxvalue - 1; j1 > -1; j1--) {

							energy_max = (int) Math
									.rint(NcLibrary.Channel_to_Energy(maxvalue, calib_A2, calib_B2, calib_C2));

							energy_left = (int) Math
									.rint(NcLibrary.Channel_to_Energy(j1, calib_A2, calib_B2, calib_C2));

							if (energy_max - energy_left > energy_max * 0.10) //// M.Comment
							//// 6
							//// window
							{
								left = j1;
								break;
							}
						}
						FP_m_ValleyData[2 * iddxx] = left;
						FP_m_ValleyData[2 * iddxx + 1] = right;
						tempPP[iddxx] = maxvalue;
						maxvalue = 0;
						iddxx++;
					}

					////////////////////// min0610

					lookformax = false;

					if (iddxx > 100)
						break;

				}

			}

			else {

				if (peakdet_temp > (mn + deltta / 100)) {

					mx = peakdet_temp;

					mxpos = i;

					lookformax = true;

				}

			}

		}

		Len = iddxx;
		FP_Len_FindPeaksData = iddxx;

		for (int j1 = 0; j1 < iddxx; j1++) {

			Peaks[j1] = tempPP[j1];

		}

		for (int j1 = iddxx; j1 < 100; j1++) {

			Peaks[j1] = 0;

		}

	}

	public double FindMax_near_Min(double data[])

	{

		double minVal = 10000, maxVal = -10000;

		int cnt = 0;

		int minPos = 0, maxPos = 0;

		for (int i = 0; i < NUM_CH; i++) {

			if (minVal > data[i]) {

				minVal = data[i];

				minPos = cnt;

			}

			cnt++;

		}

		for (int i = minPos; i < (minPos + 200); i++) {

			if (maxVal < data[i]) {

				maxVal = data[i];

				maxPos = i;

			}

		}

		return maxPos;

	}

	public int FindZeroCrossing_LargestWLV(double data[], double Pmin, double Pmax)

	{

		int i, maxPos = 0, minPos = 0, Pzero;

		double maxVal = -10000, minVal = 10000;

		for (i = 0; i < NUM_CH; i++) {

			if (i >= Pmin && i <= Pmax) {

				if (data[i] < 0) {

					if (maxVal < data[i]) {

						maxVal = data[i];

						maxPos = i;

					}

				} else if (data[i] >= 0) {

					if (minVal > data[i]) {

						minVal = data[i];

						minPos = i;

					}

				}

			}

		}

		if (maxVal != -10000 && minVal != 10000) {

			if ((maxVal) >= (minVal * (-1))) {

				Pzero = minPos;

			} else {

				Pzero = maxPos;

			}

		} else {

			Pzero = 0;

		}

		return Pzero;

	}

	public int FindZeroCrossing(double data[], double Pmin, double Pmax, double Pzero_tmp[])

	{

		int i;

		double maxVal = -10000, minVal = 10000;

		int[] maxPos = new int[100];

		int[] minPos = new int[100];

		int maxCnt = 1, minCnt = 1, zeroCnt = 1;

		for (i = 0; i < NUM_CH; i++) {

			if (i >= Pmin && i <= Pmax) {

				if (data[i] < 0) {

					if (maxVal < data[i]) {

						maxVal = data[i];

						maxPos[0] = i;

						maxCnt = 1;

					} else if (maxVal == data[i]) {

						maxCnt++;

						maxPos[maxCnt] = i;

					}

				} else if (data[i] >= 0) {

					if (minVal > data[i]) {

						minVal = data[i];

						minPos[0] = i;

						minCnt = 1;

					} else if (minVal == data[i]) {

						minCnt++;

						minPos[minCnt] = i;

					}

				}

			}

		}

		if ((maxVal) >= (minVal * (-1))) {

			zeroCnt = minCnt;

			Pzero_tmp[zeroCnt - 1] = minPos[zeroCnt - 1];

		} else {

			zeroCnt = maxCnt;

			Pzero_tmp[zeroCnt - 1] = maxPos[zeroCnt - 1];

		}

		return zeroCnt;

	}

	public void haar_s(double ws[], int n, int wlv)

	{

		int i, j, K;

		int K2;// Ver.003

		double[] wstmp = new double[NUM_CH];

		K = CalPower(2, (wlv - 1));

		K2 = (int) (Math.rint((float) (K) / 2));// Ver.003

		for (i = 0; i < n - K; i++) {
			wstmp[i] = (ws[i] + ws[i + K]) / Math.sqrt(2.0);
		}

		for (j = 1; j <= K; j++) {
			wstmp[n - j] = (ws[n - j] + ws[K - j]) / Math.sqrt(2.0);
		}

		// for(i=0;i<n;i++) { ws[i]=wstmp[i]; }

		for (i = K2; i < n; i++) {
			ws[i] = wstmp[i - K2];
		} // Ver.003

		for (i = 0; i < K2; i++) {
			ws[i] = wstmp[n - K2 + i];
		} // Ver.003

	}

	static public double[] haar_d(double wd[], int n, int wlv)

	{
		double[] result = wd;

		int i, j, K;

		int K2;// Ver.003

		double[] wdtmp = new double[n];

		K = CalPower(2, (wlv - 1));

		K2 = (int) (Math.rint((float) (K) / 2));// Ver.003

		for (i = 0; i < n - K; i++) {
			wdtmp[i] = (result[i] - result[i + K]) / Math.sqrt(2.0);
		}

		for (j = 1; j <= K; j++) {
			wdtmp[n - j] = (result[n - j] - result[K - j]) / Math.sqrt(2.0);
		}

		// for(i=0;i<n;i++) { wd[i]=wdtmp[i]; }

		for (i = K2; i < n; i++) {
			result[i] = wdtmp[i - K2];
		} // Ver.003

		for (i = 0; i < K2; i++) {
			result[i] = wdtmp[n - K2 + i];
		} // Ver.003

		return result;
	}

	static int CalPower(int num, int cnt)

	{

		return (cnt == 0 ? 1 : num * CalPower(num, --cnt));

	}

	public double FindMax(double data[]) {

		double maxVal = -10000;

		for (int i = 0; i < NUM_CH; i++) {
			if (maxVal < data[i])
				maxVal = data[i];
		}

		return maxVal;

	}

	public double FindMin(double data[]) {

		double minVal = 10000;

		for (int i = 0; i < NUM_CH; i++) {
			if (minVal > data[i])
				minVal = data[i];
		}

		return minVal;

	}

	public double FindMax_pos(double data[]) {

		double maxVal = -10000;

		int cnt = 0;

		int maxPos = 0;

		for (int i = 0; i < NUM_CH; i++) {

			if (maxVal < data[i]) {

				maxVal = data[i];

				maxPos = cnt;

			}

			cnt++;

		}

		return maxPos;

	}

	public double FindMin_pos(double data[]) {

		double minVal = 10000;

		int cnt = 0;

		int minPos = 0;

		for (int i = 0; i < NUM_CH; i++) {

			if (minVal > data[i]) {

				minVal = data[i];

				minPos = cnt;

			}

			cnt++;

		}

		return minPos;

	}

	//////////////////////////////////////////

	/* Bias Reduction by Normalized Background Radiation Template Subtraction */

	/* Noise Reduction by Maximal Overlapped Discrete Wavelet Transform */

	public void FindPeaksM_MODWT(double xs[], double xd[])

	{

		int i, wlevel;

		for (wlevel = 1; wlevel <= WAVELET_LEVEL; wlevel++) {

			haar_s(xs, NUM_CH, wlevel);

			if (wlevel == WAVELET_LEVEL - 1) {

				for (i = 0; i < NUM_CH; i++) {

					xd[i] = xs[i];

				}

				haar_d(xd, NUM_CH, WAVELET_LEVEL);

			}

		}

	}

	@SuppressWarnings("null")

	public void FindPeaksM_GenBT(int input_m_BkgChCnt[], double a, double b)

	{

		int i, j, wlevel, energy; // ekek 1108

		calib_A1 = a;

		calib_B1 = b;

		// 1 template 110820

		for (i = 0; i < NUM_CH; i++) {

			FP_m_BkgData[i] = input_m_BkgChCnt[i];

			// FP_m_BkgTemplate[i] = 0;

			FP_m_BkgTemplate[i] = FP_m_BkgData[i];

			FP_m_BkgTemplate_EN[i] = 0;

		}

	}

	public void FindPeaksM_Set_Calibration(double a, double b, double c) {
		calib_A2 = a;
		calib_B2 = b;
		calib_C2 = c;
		ft_smooth(FP_m_BkgTemplate, 0.015, 2.96474);
	}

	public void FindPeaksM_Set_Calibration(double[] abc) {
		calib_A2 = abc[0];
		calib_B2 = abc[1];
		calib_C2 = abc[2];
		ft_smooth(FP_m_BkgTemplate, 0.015, 2.96474);
	}

	public static double decimalScale(String decimal, int loc, int mode) {

		BigDecimal bd = new BigDecimal(decimal);

		BigDecimal result = null;

		if (mode == 1) {

			result = bd.setScale(loc, BigDecimal.ROUND_DOWN); // ?대┝

		}

		else if (mode == 2) {

			result = bd.setScale(loc, BigDecimal.ROUND_HALF_UP); // 諛섏삱由?

		}

		else if (mode == 3) {

			result = bd.setScale(loc, BigDecimal.ROUND_UP); // ?щ┝

		}

		return result.doubleValue();

	}

	public void Set_ABC(double a,

						double b, double c) {

		calib_A2 = a;

		calib_B2 = b;

		calib_C2 = c;

	}

	public void Set_ABC(double[] abc) {

		calib_A2 = abc[0];

		calib_B2 = abc[1];

		calib_C2 = abc[2];

	}

	public void ft_smooth(double[] ihist, double aval, double bval) {

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

	public VallyResult Find_Vally(double[] arrfloat, int firstlocation, int secondlocation, int peakvalue,
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
		for (int i = center - (int) (center * 0.01); i > firstlocation; i--) {
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

	public int[] Find_Gain_Count_Peak(Spectrum MS, Spectrum BG) {

		float input_PeakDetectionThreshold = 0.005f;

		int[] Energy = new int[100];
		int[] PeaksCh = new int[100];

		int[] VallyEn = new int[200];
		int[] VallyCh = new int[200];
		double[] VallyAB = new double[200];
		int aaw = 0;
		int Peaks_count = 0;

		//
		calib_A2 = BG.Get_Coefficients().get_Coefficients()[0];
		calib_B2 = BG.Get_Coefficients().get_Coefficients()[1];
		calib_C2 = BG.Get_Coefficients().get_Coefficients()[2];
		int input_NumOfData = 1;

		for (int i = 0; i < NUM_CH; i++) {
			FP_m_ChCnt[i] = FP_m_FirstPC[i] = 0;
		}
		FP_NumOfData_PCA = 1;

		FP_Threshold_WBPD = input_PeakDetectionThreshold;

		//
		int energy;

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_ChCnt[i] = MS.at(i);

		}

		for (int i = 0; i < 250; i++) {
			FP_m_ChCnt[i] = 0;
		}
		for (int i = 800; i < 1024; i++) {
			FP_m_ChCnt[i] = 0;
		}

		bk_time = (int)BG.Get_AcqTime();
		cn_time = (int)MS.Get_AcqTime();

		FP_m_BkgData = BG.ToDouble();

		for (int i = 0; i < NUM_CH; i++) {
			// FP_m_BkgTemplate[i] = 0;

			FP_m_BkgTemplate[i] = 0;

			FP_m_BkgTemplate_EN[i] = 0;

		}
		ft_smooth(FP_m_BkgTemplate, 0.015, 2.96474);
		// M.Comment 1
		// 백그라운드를 빼는 부분
		FindPeaksM_BTS();

		// M.Comment 2
		// 로그를 씌울때 1을 더해줘서 로그를 풀때 최소값이 0이 아닌 1로 나왔음.
		// 전체적으로 스펙트럼이 높아져 있는 것을 낮추어 주는 부분임.
		// 원래는 -1만 하였으나 정보량이 부족하여 2를 곱한 후 2를 빼게 되었음.
		for (int i = 0; i < NUM_CH; i++) {
			FP_m_WaveletTransformed_s[i] = FP_m_BiasReduced[i] * 2 - 2;
		}

		ft_smooth(FP_m_WaveletTransformed_s, 0.015, 2.96474);

		for (int i = 0; i < 100; i++) {

			PeaksCh[i] = FP_m_FindPeaksData[i] = 0;
			Energy[i] = FP_m_FindPeaksData_EN[i] = 0;

		}

		for (int i = 0; i < 200; i++) {

			VallyCh[i] = FP_m_ValleyData[i] = 0;
			VallyEn[i] = FP_m_ValleyData_EN[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = FP_m_WaveletTransformed_s[i];

		}

		// M.Comment 3
		// 표준화 시켜주는 부분임.
		// 스펙트럼 값에서 평균을 빼고 표준편차로 나누어 주게 됨.
		// 특이사항이 없는 한 변동이 없는 편이 좋음.
		FindPeaksM_PCA();

		// M.Comment 4
		// 최종 피크를 찾기 전에 음수인 부분이 없도록 전체적으로 스펙트럼을 x축 위로 올리는 부분임.
		// FP_m_FirstPC를 이용하여 피크를 찾음.
		double mmiinn = 0;

		for (int i = 1; i < NUM_CH; i++) {

			if (i == 1) {

				if (FP_m_FirstPC[0] < FP_m_FirstPC[1])

					mmiinn = FP_m_FirstPC[0];

				else

					mmiinn = FP_m_FirstPC[1];

			}

			else {

				if (FP_m_FirstPC[i] < mmiinn)

					mmiinn = FP_m_FirstPC[i];

			}

		}

		for (int i = 1; i < NUM_CH; i++) {

			FP_m_FirstPC[i] = FP_m_FirstPC[i] - mmiinn;

		}

		// M.Comment 5
		// 피크찾는 부분임.
		Vector<String> temp = new Vector<String>();
		for (int i = 0; i < 1024; i++) {
			temp.add(String.format("%.4f", FP_m_FirstPC[i]));
		}

		FindPeaksM_WBPD(FP_m_FirstPC, FP_m_FindPeaksData, FP_Len_FindPeaksData);

		// M.Comment 6
		// 밸리찾는 부분
		// 5pt를 이용하여 찾음. 현재는 FP_log_data를 이용하였음.
		float resultA = 0;
		float resultB = 0;
		int resultAx = 0;
		int resultBx = 0;
		int ValCount = 0;

		double[] VallySPC = new double[1024]; // 이하 밸리는 찾는다.
		for (int i = 0; i < 1024; i++) {
			VallySPC[i] = FP_m_ChCnt[i] - ((FP_m_BkgData[i] / (double) bk_time) * (double) cn_time);
		}

		ft_smooth(VallySPC, 0.015, 2.96474);// 0.007413
		// NcLibrary.Export_spectrum_data(VallySPC, 1024);

		final int[] intArray = new int[VallySPC.length];
		for (int i = 0; i < intArray.length; ++i)
			intArray[i] = (int) VallySPC[i];
		String a = Arrays.toString(intArray);

		for (int i = 0; i < FP_Len_FindPeaksData; i++) {
			double En = NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2);
			double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(En);
			double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
			double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

			// double factor = 0.2;
			// if(En < 63) factor = 0.2;

			L_ROI_Percent = (NcLibrary.Energy_to_Channel(En * L_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1-factor);
			R_ROI_Percent = (NcLibrary.Energy_to_Channel(En * R_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1+factor);

			VallyResult Result = Find_Vally(VallySPC, (int) L_ROI_Percent, (int) R_ROI_Percent, FP_m_FindPeaksData[i],
					resultA, resultB, resultAx, resultBx);

			FP_m_ValleyData_EN[ValCount] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Ax, calib_A2, calib_B2, calib_C2));
			FP_m_ValleyData_EN[ValCount + 1] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Bx, calib_A2, calib_B2, calib_C2));

			VallyAB[ValCount] = Result.A;
			VallyAB[ValCount + 1] = Result.B;
			FP_m_ValleyData[ValCount] = (int) Result.Ax;
			FP_m_ValleyData[ValCount + 1] = (int) Result.Bx;

			ValCount += 2;
		}
		/////////////////
		for (int i = 0; i < FP_Len_FindPeaksData; i++) {

			PeaksCh[i] = FP_m_FindPeaksData[i];
			Energy[i] = FP_m_FindPeaksData_EN[i] = (int) Math
					.rint(NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2));
			if (FP_m_FindPeaksData_EN[i] < 0) {
				Energy[i] = FP_m_FindPeaksData_EN[i] = 0;
			}

			for (int j = 0; j < 2; j++) {

				VallyCh[2 * i + j] = FP_m_ValleyData[2 * i + j];
				VallyEn[2 * i + j] = FP_m_ValleyData_EN[2 * i + j];// = (int)
				// Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i+j],calib_A2,calib_B2,calib_C2));

				if (FP_m_ValleyData_EN[2 * i + j] < 0) {
					VallyEn[2 * i + j] = FP_m_ValleyData_EN[2 * i + j] = 0;
				}

			}

		}
		/////////////// M.Comment 6 랑 대비
		Peaks_count = FP_Len_FindPeaksData;

		Vector<NcPeak> Result = new Vector<NcPeak>();

		for (int i = 0; i < Peaks_count; i++) {
			NcPeak PData_temp = new NcPeak();
			PData_temp.Channel = PeaksCh[i];
			PData_temp.Peak_Energy = Energy[i];
			PData_temp.ROI_Left = FP_m_ValleyData[i * 2];
			PData_temp.ROI_Right = FP_m_ValleyData[(i * 2) + 1];

			double Coefficients[] = new double[2];
			Coefficients[0] = VallyAB[0];
			Coefficients[1] = VallyAB[1];
			// PData_temp.Vally_Coefficients = Coefficients;
			// PData_temp.Vally_Coefficients = Coefficients;
			PData_temp.BG_a = VallyAB[0];
			PData_temp.BG_b = VallyAB[1];
			Result.add(PData_temp);
		}

		String log = "Found Peaks - ";
		for (int i = 0; i < Result.size(); i++) {
			log += Result.get(i).Channel + "(" + Result.get(i).Peak_Energy + " Kev),  ";

		}
		if (D)
			Log.i(TAG, log);// 33
		//
		return PeaksCh;
	}

	public int[] Find_Gain_Peak_List(Spectrum MS, Spectrum BG) {

		float input_PeakDetectionThreshold = 0.005f;

		int[] Energy = new int[100];
		int[] PeaksCh = new int[100];

		int[] VallyEn = new int[200];
		int[] VallyCh = new int[200];
		double[] VallyAB = new double[200];
		int aaw = 0;
		int Peaks_count = 0;

		//
		calib_A2 = BG.Get_Coefficients().get_Coefficients()[0];
		calib_B2 = BG.Get_Coefficients().get_Coefficients()[1];
		calib_C2 = BG.Get_Coefficients().get_Coefficients()[2];

		int input_NumOfData = 1;

		FP_NumOfData_PCA = 1;

		FP_Threshold_WBPD = input_PeakDetectionThreshold;

		//
		int energy;

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_ChCnt[i] = MS.at(i);

		}

		for (int i = 0; i < 250; i++) {

			FP_m_ChCnt[i] = 0;

		}

		for (int i = 800; i < 1024; i++) {

			FP_m_ChCnt[i] = 0;

		}

		bk_time = 1;
		cn_time = 1;

		FP_m_BkgData = BG.ToDouble();

		for (int i = 0; i < NUM_CH; i++) {
			FP_m_BkgTemplate[i] = 0;

			// FP_m_BkgTemplate[i] = FP_m_BkgData[i];
			FP_m_BkgTemplate[i] = 0;

			FP_m_BkgTemplate_EN[i] = 0;

		}
		ft_smooth(FP_m_BkgTemplate, 0.015, 2.96474);
		// M.Comment 1
		// 백그라운드를 빼는 부분
		FindPeaksM_BTS();

		// M.Comment 2
		// 로그를 씌울때 1을 더해줘서 로그를 풀때 최소값이 0이 아닌 1로 나왔음.
		// 전체적으로 스펙트럼이 높아져 있는 것을 낮추어 주는 부분임.
		// 원래는 -1만 하였으나 정보량이 부족하여 2를 곱한 후 2를 빼게 되었음.
		for (int i = 0; i < NUM_CH; i++) {
			FP_m_WaveletTransformed_s[i] = FP_m_BiasReduced[i] * 2 - 2;
		}

		ft_smooth(FP_m_WaveletTransformed_s, 0.015, 2.96474);

		for (int i = 0; i < 100; i++) {

			PeaksCh[i] = FP_m_FindPeaksData[i] = 0;
			Energy[i] = FP_m_FindPeaksData_EN[i] = 0;

		}

		for (int i = 0; i < 200; i++) {

			VallyCh[i] = FP_m_ValleyData[i] = 0;
			VallyEn[i] = FP_m_ValleyData_EN[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = 0;

		}

		for (int i = 0; i < NUM_CH; i++) {

			FP_m_PrincipalComponent_D[i] = FP_m_WaveletTransformed_s[i];

		}

		// M.Comment 3
		// 표준화 시켜주는 부분임.
		// 스펙트럼 값에서 평균을 빼고 표준편차로 나누어 주게 됨.
		// 특이사항이 없는 한 변동이 없는 편이 좋음.
		FindPeaksM_PCA();

		// M.Comment 4
		// 최종 피크를 찾기 전에 음수인 부분이 없도록 전체적으로 스펙트럼을 x축 위로 올리는 부분임.
		// FP_m_FirstPC를 이용하여 피크를 찾음.
		double mmiinn = 0;

		for (int i = 1; i < NUM_CH; i++) {

			if (i == 1) {

				if (FP_m_FirstPC[0] < FP_m_FirstPC[1])

					mmiinn = FP_m_FirstPC[0];

				else

					mmiinn = FP_m_FirstPC[1];

			}

			else {

				if (FP_m_FirstPC[i] < mmiinn)

					mmiinn = FP_m_FirstPC[i];

			}

		}

		for (int i = 1; i < NUM_CH; i++) {

			FP_m_FirstPC[i] = FP_m_FirstPC[i] - mmiinn;

		}

		// M.Comment 5
		// 피크찾는 부분임.
		Vector<String> temp = new Vector<String>();
		for (int i = 0; i < 1024; i++) {
			temp.add(String.format("%.4f", FP_m_FirstPC[i]));
		}
		// SaveText mText = new SaveTextImpl();

		// ConvertFunction mFunction = new ConvertFunctionImpl();

		// mText.onTextWriting("FindPeakSpectrum",
		// mFunction.SpectrumToString(FP_m_FirstPC));

		FindPeaksM_WBPD(FP_m_FirstPC, FP_m_FindPeaksData, FP_Len_FindPeaksData);

		// M.Comment 6
		// 밸리찾는 부분
		// 5pt를 이용하여 찾음. 현재는 FP_log_data를 이용하였음.
		float resultA = 0;
		float resultB = 0;
		int resultAx = 0;
		int resultBx = 0;
		int ValCount = 0;

		double[] VallySPC = new double[1024]; // 이하 밸리는 찾는다.
		for (int i = 0; i < 1024; i++) {
			VallySPC[i] = FP_m_ChCnt[i] - ((FP_m_BkgData[i] / (double) bk_time) * (double) cn_time);
		}

		ft_smooth(VallySPC, 0.015, 2.96474);// 0.007413
		// NcLibrary.Export_spectrum_data(VallySPC, 1024);

		final int[] intArray = new int[VallySPC.length];
		for (int i = 0; i < intArray.length; ++i)
			intArray[i] = (int) VallySPC[i];
		String a = Arrays.toString(intArray);

		for (int i = 0; i < FP_Len_FindPeaksData; i++) {
			double En = NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2);
			double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(En);
			double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
			double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

			// double factor = 0.2;
			// if(En < 63) factor = 0.2;

			L_ROI_Percent = (NcLibrary.Energy_to_Channel(En * L_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1-factor);
			R_ROI_Percent = (NcLibrary.Energy_to_Channel(En * R_ROI_Percent, calib_A2, calib_B2, calib_C2));// *(1+factor);

			VallyResult Result = Find_Vally(VallySPC, (int) L_ROI_Percent, (int) R_ROI_Percent, FP_m_FindPeaksData[i],
					resultA, resultB, resultAx, resultBx);

			FP_m_ValleyData_EN[ValCount] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Ax, calib_A2, calib_B2, calib_C2));
			FP_m_ValleyData_EN[ValCount + 1] = NcLibrary
					.Auto_floor(NcLibrary.Channel_to_Energy(Result.Bx, calib_A2, calib_B2, calib_C2));

			VallyAB[ValCount] = Result.A;
			VallyAB[ValCount + 1] = Result.B;
			FP_m_ValleyData[ValCount] = (int) Result.Ax;
			FP_m_ValleyData[ValCount + 1] = (int) Result.Bx;

			ValCount += 2;
		}
		/////////////////
		for (int i = 0; i < FP_Len_FindPeaksData; i++) {

			PeaksCh[i] = FP_m_FindPeaksData[i];
			Energy[i] = FP_m_FindPeaksData_EN[i] = (int) Math
					.rint(NcLibrary.Channel_to_Energy(FP_m_FindPeaksData[i], calib_A2, calib_B2, calib_C2));
			if (FP_m_FindPeaksData_EN[i] < 0) {
				Energy[i] = FP_m_FindPeaksData_EN[i] = 0;
			}

			for (int j = 0; j < 2; j++) {

				VallyCh[2 * i + j] = FP_m_ValleyData[2 * i + j];
				VallyEn[2 * i + j] = FP_m_ValleyData_EN[2 * i + j];// = (int)
				// Math.rint(NcLibrary.Channel_to_Energy(FP_m_ValleyData[2*i+j],calib_A2,calib_B2,calib_C2));

				if (FP_m_ValleyData_EN[2 * i + j] < 0) {
					VallyEn[2 * i + j] = FP_m_ValleyData_EN[2 * i + j] = 0;
				}

			}

		}
		/////////////// M.Comment 6 랑 대비
		Peaks_count = FP_Len_FindPeaksData;

		Vector<NcPeak> Result = new Vector<NcPeak>();

		for (int i = 0; i < Peaks_count; i++) {
			NcPeak PData_temp = new NcPeak();
			PData_temp.Channel = PeaksCh[i];
			PData_temp.Peak_Energy = Energy[i];
			PData_temp.ROI_Left = FP_m_ValleyData[i * 2];
			PData_temp.ROI_Right = FP_m_ValleyData[(i * 2) + 1];

			double Coefficients[] = new double[2];
			Coefficients[0] = VallyAB[0];
			Coefficients[1] = VallyAB[1];
			// PData_temp.Vally_Coefficients = Coefficients;
			// PData_temp.Vally_Coefficients = Coefficients;
			PData_temp.BG_a = VallyAB[0];
			PData_temp.BG_b = VallyAB[1];

			Result.add(PData_temp);
		}

		String log = "Found Peaks - ";
		for (int i = 0; i < Result.size(); i++) {
			log += Result.get(i).Channel + "(" + Result.get(i).Peak_Energy + " Kev),  ";

		}

		//
		return PeaksCh;
	}

}