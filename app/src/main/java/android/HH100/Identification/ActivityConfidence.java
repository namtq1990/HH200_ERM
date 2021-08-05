package android.HH100.Identification;

import java.util.Vector;

import NcLibrary.Coefficients;
import NcLibrary.NewNcAnalsys;
//import android.HH100.Isotope;
import android.HH100.Structure.NcPeak;

public class ActivityConfidence {

	public static void ActivityCorrection_H200() {

		// step 1: Load data into PeakInfo[Msize, 7]
		// 7 coulms: Peak_CH0 | Peak_CH0 | Peak_Roi_Light0 | Eng0 | PP_NetCnt1 |
		// BG_NetCnt0 | LC0

		int NoLineErg = 11; // size of after filter
		double[][] PeakInfo = new double[NoLineErg][7];

		String[] Nu = new String[] { "Cs-137", "U-238", "U-235", "F-18" };

		double[] ListEn = new double[] { 665, 988, 747, 180, 504 };

		// load Measurment Energyz

		double[] MsSpc = new double[1024];

		// MsSpc = ReadFile(@"D:\MS_Original_Spec.txt", MsSpc);

		double[] PeakInfo1 = new double[NoLineErg * 7];
		// PeakInfo1 = ReadFile(@"D:\PeakInfo.txt", PeakInfo1);

		int cnt = 0;
		for (int i = 0; i < NoLineErg; i++) {
			for (int j = 0; j < 7; j++) {
				PeakInfo[i][j] = PeakInfo1[cnt];
				cnt = cnt + 1;
			}
		}

		double[] Eff_Coef = new double[] { -0.027939138, 0.694026779, -6.627760069, 28.20796375, -48.74100729 };

		double[][] Result = new double[2][Nu.length];

		// Result = ActivityCorrection(MsSpc, ListEn, Nu, PeakInfo, , Eff_Coef);

		int m = 10;
	}

	public static Vector<Isotope> ActivityCorrection(double[] Spc, Vector<Isotope> result2, Vector<NcPeak> PeakInfo_new,double MsTime, double[] Eff_Coef) {

		//Hung Modified: 17/11/22
		int NoSrc=result2.size();
		double [] TruePeak=new double [100];

		double[][] PeakEnBr_Ref = new double[][] {};
		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;

		for(int i=0;i<NoSrc;i++)
		{
			PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(result2.get(i));

			NoMaxEn = PeakEnBr_Ref.length;

			for (int j = 0; j < NoMaxEn; j++)
			{
				EnTmp = PeakEnBr_Ref[j][0];
				BrTmp= PeakEnBr_Ref[j][1];

				if(BrTmp>0)
				{
					TruePeak[cntPeakTrue]=EnTmp;
					cntPeakTrue=cntPeakTrue+1;
				}
			}
		}


		//remove noise peak
		int MM= PeakInfo_new.size();
		double roi_tmp,Left_thsld,High_thsld;
		int []Flg= new int [MM];
		for(int i=0;i<MM;i++)
		{
			Flg[i]=0;// reset

			EnTmp= PeakInfo_new.get(i).Peak_Energy;

			for(int j=0;j<cntPeakTrue;j++)
			{
				roi_tmp=Get_Roi_window_by_energy(TruePeak[j]);

				Left_thsld=TruePeak[j]*(1-roi_tmp * 0.01);
				High_thsld=TruePeak[j]*(1+roi_tmp * 0.01);

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Flg[i]=1;
				}

			}
		}


		int M222 = PeakInfo_new.size();

		double[][] PeakInfo = new double[M222][7];
		for (int i = 0; i < PeakInfo_new.size(); i++) {

			PeakInfo[i][0] = PeakInfo_new.get(i).Peak;
			PeakInfo[i][1] = PeakInfo_new.get(i).ROI_Left;
			PeakInfo[i][2] = PeakInfo_new.get(i).ROI_Right;
			PeakInfo[i][3] = PeakInfo_new.get(i).Peak_Energy;
			PeakInfo[i][4] = PeakInfo_new.get(i).NetCnt;
			PeakInfo[i][5] = PeakInfo_new.get(i).Background_Net_Count;
			PeakInfo[i][6] = PeakInfo_new.get(i).LC;

		}

		int M = PeakInfo.length;
		int N = PeakInfo[0].length;
		/*
		int[] Flg = new int[M];

		double M1 = PeakInfo_new.size();

		// step 1: Define candidate energy is candidate

		double erg_tmp = 0;
		for (int i = 0; i < M; i++)
		{
			Flg[i] = 1;
		}
*/
		int M2 = 0;
		for (int i = 0; i < M; i++) {
			M2 = M2 + Flg[i];
		}

		double[][] PeakInfo1 = new double[M2][N];

		int cnt = 0;
		for (int i = 0; i < M; i++)
		{
			if (Flg[i] == 1)
			{
				for (int j = 0; j < N; j++)
				{
					PeakInfo1[cnt][j] = PeakInfo[i][j];
				}
				cnt = cnt + 1;

			}
		}

		// step 2: Calcualte Uncertainty of PeakNet and Efficiency
		M = PeakInfo1.length;
		N = PeakInfo1[0].length;

		double[] ROI_L = new double[M];
		double[] ROI_R = new double[M];
		double[] PeakEn = new double[M];

		double[] PPNet = new double[M];
		double[] BGNet = new double[M];
		double[] W = new double[M]; // weighting factor

		for (int i = 0; i < M; i++) {
			ROI_L[i] = PeakInfo1[i][1];
			ROI_R[i] = PeakInfo1[i][2];
			PeakEn[i] = PeakInfo1[i][3];
			PPNet[i] = PeakInfo1[i][4];
			BGNet[i] = PeakInfo1[i][5];
			W[i] = 1;
		}

		double[] UnPeak = new double[M];
		double[] UnEff = new double[M];

		UnPeak = PeakUncertainty(Spc, ROI_L, ROI_R, BGNet); // uncertainty of
		// Peak net
		UnEff = EffUncertainty(PeakEn); // uncertainty

		// Step 3: Calculate BR
		String[] Nu = new String[result2.size()];
		for (int i = 0; i < result2.size(); i++) {
			Nu[i] = result2.get(i).isotopes;
		}
		int NoIso = Nu.length;
		double[][] BR = new double[M][NoIso];

		double[] BRTemp = new double[M];

		for (int i = 0; i < NoIso; i++) {
			BRTemp = GetBR(PeakEn, Nu[i], result2.get(i));

			for (int j = 0; j < M; j++) {
				BR[j][i] = BRTemp[j];
			}
		}



		// Step 4: Calculate Activtiy
		double[] Act = new double[NoIso];

		Act = ActCorrect(Eff_Coef, PeakEn, BR, PPNet, W, MsTime);



		// step 5: calculate Uncertainty of isotpe
		double[] Uncer = new double[NoIso];
		double [] RMSE= new double[NoIso];

		for (int i = 0; i < NoIso; i++) {
			for (int j = 0; j < M; j++) {
				BRTemp[j] = BR[j][i];
			}

			Uncer[i] = Uncertainty_CValue(Act[i], PeakEn, UnEff, UnPeak, BRTemp, MsTime, Eff_Coef);

			RMSE[i] = StdErr(Act[i], BRTemp, PeakEn, PPNet, MsTime, Eff_Coef);
		}

		//	double[][] Result = new double[3][Nu.length];
		for (int i = 0; i < NoIso; i++) {
			result2.get(i).Act = Act[i];
			result2.get(i).Uncer = Uncer[i];
			result2.get(i).RMSE = RMSE[i];
		}

		return result2;
	}
	public static Vector<Isotope> ActivityCorrection_H(double[] Spc, Vector<Isotope> result2, Vector<NcPeak> PeakInfo_new,double MsTime, double[] FWHMCoeff,Coefficients coeff, double [] Eff_Coef,double WndROI)
	{
		//Hung Modified: 17/11/22
		int NoSrc=result2.size();
		double [] TruePeak=new double [100];

		double[][] PeakEnBr_Ref = new double[][] {};
		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;

		for(int i=0;i<NoSrc;i++)
		{
			PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(result2.get(i));

			NoMaxEn = PeakEnBr_Ref.length;

			for (int j = 0; j < NoMaxEn; j++)
			{
				EnTmp = PeakEnBr_Ref[j][0];
				BrTmp= PeakEnBr_Ref[j][1];

				if(BrTmp>0)
				{
					TruePeak[cntPeakTrue]=EnTmp;
					cntPeakTrue=cntPeakTrue+1;
				}
			}
		}


		//remove noise peak
		int MM= PeakInfo_new.size();
		double roi_tmp,Left_thsld,High_thsld;
		int []Flg= new int [MM];

		double [] Thshold=new double [2];


		for(int i=0;i<MM;i++)
		{
			Flg[i]=0;// reset

			EnTmp= PeakInfo_new.get(i).Peak_Energy;

			for(int j=0;j<cntPeakTrue;j++)
			{


				Thshold= NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(TruePeak[j], FWHMCoeff,coeff,WndROI);

				Left_thsld=Thshold[0];
				High_thsld=Thshold[1];

				//roi_tmp=Get_Roi_window_by_energy(TruePeak[j]);

				//Left_thsld=TruePeak[j]*(1-roi_tmp * 0.01);
				// High_thsld=TruePeak[j]*(1+roi_tmp * 0.01);

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Flg[i]=1;
				}

			}
		}






		int M222 = PeakInfo_new.size();

		double[][] PeakInfo = new double[M222][7];
		int cnt11=0;
		for (int i = 0; i < PeakInfo_new.size(); i++)
		{
			//if(PeakInfo_new.get(i).Used_for_Boolen >0)
			{
				PeakInfo[cnt11][0] = PeakInfo_new.get(i).Peak;
				PeakInfo[cnt11][1] = PeakInfo_new.get(i).ROI_Left;
				PeakInfo[cnt11][2] = PeakInfo_new.get(i).ROI_Right;
				PeakInfo[cnt11][3] = PeakInfo_new.get(i).Peak_Energy;
				PeakInfo[cnt11][4] = PeakInfo_new.get(i).NetCnt;
				PeakInfo[cnt11][5] = PeakInfo_new.get(i).Background_Net_Count;
				PeakInfo[cnt11][6] = PeakInfo_new.get(i).LC;
				cnt11=cnt11+1;
			}

		}

		int M = PeakInfo.length;
		int N = PeakInfo[0].length;

		int M2 = 0;
		for (int i = 0; i < M; i++)
		{
			M2 = M2 + Flg[i];
		}


		double[][] PeakInfo11 = new double[M2][N];

		int cnt = 0;
		for (int i = 0; i < M; i++)
		{
			if (Flg[i] == 1)
			{
				for (int j = 0; j < N; j++)
				{
					PeakInfo11[cnt][j] = PeakInfo[i][j];
				}
				cnt = cnt + 1;

			}
		}

		M = PeakInfo11.length;
		N = PeakInfo11[0].length;
		double[] PeakEn11 = new double[M];
		for (int i = 0; i < M; i++)
		{
			PeakEn11[i] = PeakInfo11[i][3];

		}
		// Step 2: Calculate BR
		String[] Nu = new String[result2.size()];
		for (int i = 0; i < result2.size(); i++)
		{
			Nu[i] = result2.get(i).isotopes;
		}

		int NoIso = Nu.length;
		double[][] BR11 = new double[M][NoIso];

		double[] BRTemp11 = new double[M];


		for (int i = 0; i < NoIso; i++)
		{
			BRTemp11 = GetBR_H(PeakEn11, Nu[i], result2.get(i),FWHMCoeff,coeff,WndROI);


			for (int j = 0; j < M; j++)
			{
				BR11[j][i] = BRTemp11[j];
			}
		}

		// Step 3.1: Remove BR with row have zeros value
		int NoLineMeaning=0;
		double sumtmp=0;
		for(int i=0; i< M; i++)
		{
			sumtmp=0;

			for(int j=0;j< NoIso;j++)
			{
				sumtmp=sumtmp+BR11[i][j];
			}

			if(sumtmp>0)
			{
				NoLineMeaning=NoLineMeaning+1;
			}
		}


		double[][] PeakInfo1 = new double[NoLineMeaning][N];
		int NoLineMeaningTmp=0;
		for(int i=0; i< M; i++)
		{
			sumtmp=0;

			for(int j=0;j< NoIso;j++)
			{
				sumtmp=sumtmp+BR11[i][j];
			}

			if(sumtmp>0)
			{

				for(int j=0;j<N;j++)
				{
					PeakInfo1[NoLineMeaningTmp][j] = PeakInfo11[i][j];
				}

				NoLineMeaningTmp=NoLineMeaningTmp+1;
			}
		}




		// step 2: Calcualte Uncertainty of PeakNet and Efficiency
		M = PeakInfo1.length;
		N = PeakInfo1[0].length;

		double[] ROI_L = new double[M];
		double[] ROI_R = new double[M];
		double[] PeakEn = new double[M];

		double[] PPNet = new double[M];
		double[] BGNet = new double[M];
		double[] W = new double[M]; // weighting factor

		for (int i = 0; i < M; i++)
		{
			ROI_L[i] = PeakInfo1[i][1];
			ROI_R[i] = PeakInfo1[i][2];
			PeakEn[i] = PeakInfo1[i][3];
			PPNet[i] = PeakInfo1[i][4];
			BGNet[i] = PeakInfo1[i][5];
			W[i] = 1;
		}

		double[] UnPeak = new double[M];
		double[] UnEff = new double[M];

		UnPeak = PeakUncertainty(Spc, ROI_L, ROI_R, BGNet); // uncertainty of
		// Peak net
		UnEff = EffUncertainty(PeakEn); // uncertainty

		// Step 3: Calculate BR
		//String[] Nu = new String[result2.size()];
		//for (int i = 0; i < result2.size(); i++)
		//{
		//	Nu[i] = result2.get(i).isotopes;
		//	}

		NoIso = Nu.length;
		double[][] BR = new double[M][NoIso];

		double[] BRTemp = new double[M];

		String BrSum = "";

		for (int i = 0; i < NoIso; i++)
		{
			BRTemp = GetBR_H(PeakEn, Nu[i], result2.get(i),FWHMCoeff,coeff,WndROI);
			BrSum += "\n";
			for (int j = 0; j < M; j++)
			{
				BR[j][i] = BRTemp[j];
				BrSum += Double.toString(BRTemp[j]) + ",";
			}
		}

		//Step 3.2: Calculate weighting factor
		double en,entemp, eff,std_net,std_eff;
		for (int i=0;i<M;i++)
		{
			en = Math.log(PeakEn[i]);

			entemp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

			eff = Math.exp(entemp);

			std_net=UnPeak[i]/PPNet[i];
			std_eff=UnEff[i]/eff;

			// W[i]=1.0/Math.sqrt(std_net*std_net+std_eff*std_eff);
			W[i]=1.0/(std_net*std_net+std_eff*std_eff);
		}


		//saving BR,PeakInfo_new,PeakInfo
		// Step 4: Calculate Activity
		double[] Act = new double[NoIso];


		Act = ActCorrect(Eff_Coef, PeakEn, BR, PPNet, W, MsTime);



		// step 5: calculate Uncertainty of isotpe
		double[] Uncer = new double[NoIso];
		double[] RMSE = new double[NoIso];

		for (int i = 0; i < NoIso; i++) {
			for (int j = 0; j < M; j++) {
				BRTemp[j] = BR[j][i];
			}

			Uncer[i] = Uncertainty_CValue(Act[i], PeakEn, UnEff, UnPeak, BRTemp, MsTime, Eff_Coef);

			RMSE[i] = StdErr(Act[i], BRTemp, PeakEn, PPNet, MsTime, Eff_Coef);
		}

		// double[][] Result = new double[3][Nu.length];
		for (int i = 0; i < NoIso; i++)
		{
			result2.get(i).Act = Act[i];
			result2.get(i).Uncer = Uncer[i];
			result2.get(i).RMSE = RMSE[i];
		}

		return result2;
	}
	public static Vector<Isotope> ActivityCorrection_H1_old(double[] Spc, Vector<Isotope> result2, Vector<NcPeak> PeakInfo_new,double MsTime,double[] FWHMCoeff,Coefficients coeff, double[] Eff_Coef,double WndROI)
	{
		//Hung Modified: 17/11/22
		int NoSrc=result2.size();
		double [] TruePeak=new double [100];

		double[][] PeakEnBr_Ref = new double[][] {};
		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;

		for(int i=0;i<NoSrc;i++)
		{
			PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(result2.get(i));

			NoMaxEn = PeakEnBr_Ref.length;

			for (int j = 0; j < NoMaxEn; j++)
			{
				EnTmp = PeakEnBr_Ref[j][0];
				BrTmp= PeakEnBr_Ref[j][1];

				if(BrTmp>0)
				{
					TruePeak[cntPeakTrue]=EnTmp;
					cntPeakTrue=cntPeakTrue+1;
				}
			}
		}


		//remove noise peak
		int MM= PeakInfo_new.size();
		double roi_tmp,Left_thsld,High_thsld;
		int []Flg= new int [MM];

		double[] Thshold = new double[2];

		for(int i=0;i<MM;i++)
		{
			Flg[i]=0;// reset

			EnTmp= PeakInfo_new.get(i).Peak_Energy;

			for(int j=0;j<cntPeakTrue;j++)
			{
				//roi_tmp=Get_Roi_window_by_energy(TruePeak[j]);

				// Left_thsld=TruePeak[j]*(1-roi_tmp * 0.01);
				//  High_thsld=TruePeak[j]*(1+roi_tmp * 0.01);

				Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(TruePeak[j], FWHMCoeff, coeff,WndROI);

				Left_thsld = Thshold[0];
				High_thsld = Thshold[1];


				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Flg[i]=1;
				}

			}
		}




		int M222 = PeakInfo_new.size();

		double[][] PeakInfo = new double[M222][7];
		int cnt11=0;
		for (int i = 0; i < PeakInfo_new.size(); i++)
		{
			//if(PeakInfo_new.get(i).Used_for_Boolen >0)
			{
				PeakInfo[cnt11][0] = PeakInfo_new.get(i).Peak;
				PeakInfo[cnt11][1] = PeakInfo_new.get(i).ROI_Left;
				PeakInfo[cnt11][2] = PeakInfo_new.get(i).ROI_Right;
				PeakInfo[cnt11][3] = PeakInfo_new.get(i).Peak_Energy;
				PeakInfo[cnt11][4] = PeakInfo_new.get(i).NetCnt;
				PeakInfo[cnt11][5] = PeakInfo_new.get(i).Background_Net_Count;
				PeakInfo[cnt11][6] = PeakInfo_new.get(i).LC;
				cnt11=cnt11+1;
			}

		}

		int M = PeakInfo.length;
		int N = PeakInfo[0].length;

		int M2 = 0;
		for (int i = 0; i < M; i++)
		{
			M2 = M2 + Flg[i];
		}


		double[][] PeakInfo11 = new double[M2][N];

		int cnt = 0;
		for (int i = 0; i < M; i++)
		{
			if (Flg[i] == 1)
			{
				for (int j = 0; j < N; j++)
				{
					PeakInfo11[cnt][j] = PeakInfo[i][j];
				}
				cnt = cnt + 1;

			}
		}

		M = PeakInfo11.length;
		N = PeakInfo11[0].length;

		double[] PeakEn11 = new double[M];
		for (int i = 0; i < M; i++)
		{
			PeakEn11[i] = PeakInfo11[i][3];

		}

		// Step 2: Calculate BR
		String[] Nu = new String[result2.size()];
		for (int i = 0; i < result2.size(); i++)
		{
			Nu[i] = result2.get(i).isotopes;
		}

		int NoIso = Nu.length;
		double[][] BR11 = new double[M][NoIso];

		double[] BRTemp11 = new double[M];


		for (int i = 0; i < NoIso; i++)
		{
			BRTemp11 = GetBR_H(PeakEn11, Nu[i], result2.get(i),FWHMCoeff, coeff,WndROI);


			for (int j = 0; j < M; j++)
			{
				BR11[j][i] = BRTemp11[j];
			}
		}

		// Step 3.1: Remove BR with row have zeros value
		int NoLineMeaning=0;
		double sumtmp=0;
		for(int i=0; i< M; i++)
		{
			sumtmp=0;

			for(int j=0;j< NoIso;j++)
			{
				sumtmp=sumtmp+BR11[i][j];
			}

			if(sumtmp>0)
			{
				NoLineMeaning=NoLineMeaning+1;
			}
		}


		double[][] PeakInfo1 = new double[NoLineMeaning][N];
		int NoLineMeaningTmp=0;
		for(int i=0; i< M; i++)
		{
			sumtmp=0;

			for(int j=0;j< NoIso;j++)
			{
				sumtmp=sumtmp+BR11[i][j];
			}

			if(sumtmp>0)
			{

				for(int j=0;j<N;j++)
				{
					PeakInfo1[NoLineMeaningTmp][j] = PeakInfo11[i][j];
				}

				NoLineMeaningTmp=NoLineMeaningTmp+1;
			}
		}




		// step 2: Calcualte Uncertainty of PeakNet and Efficiency
		M = PeakInfo1.length;
		N = PeakInfo1[0].length;

		double[] ROI_L = new double[M];
		double[] ROI_R = new double[M];
		double[] PeakEn = new double[M];

		double[] PPNet = new double[M];
		double[] BGNet = new double[M];
		double[] W = new double[M]; // weighting factor

		for (int i = 0; i < M; i++)
		{
			ROI_L[i] = PeakInfo1[i][1];
			ROI_R[i] = PeakInfo1[i][2];
			PeakEn[i] = PeakInfo1[i][3];
			PPNet[i] = PeakInfo1[i][4];
			BGNet[i] = PeakInfo1[i][5];
			W[i] = 1;
		}

		double[] UnPeak = new double[M];
		double[] UnEff = new double[M];

		UnPeak = PeakUncertainty(Spc, ROI_L, ROI_R, BGNet); // uncertainty of
		// Peak net
		UnEff = EffUncertainty(PeakEn); // uncertainty

		// Step 3: Calculate BR
		//String[] Nu = new String[result2.size()];
		//	for (int i = 0; i < result2.size(); i++)
		//{
		//		Nu[i] = result2.get(i).isotopes;
		//	}

		NoIso = Nu.length;
		double[][] BR = new double[M][NoIso];

		double[] BRTemp = new double[M];

		String BrSum = "";

		for (int i = 0; i < NoIso; i++)
		{
			BRTemp = GetBR_H(PeakEn, Nu[i], result2.get(i),FWHMCoeff, coeff,WndROI);
			BrSum += "\n";
			for (int j = 0; j < M; j++)
			{
				BR[j][i] = BRTemp[j];
				BrSum += Double.toString(BRTemp[j]) + ",";
			}
		}

		//Step 3.2: Calculate weighting factor
		double en,entemp, eff,std_net,std_eff;
		for (int i=0;i<M;i++)
		{
			en = Math.log(PeakEn[i]);

			entemp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)
					+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

			eff = Math.exp(entemp);

			std_net=UnPeak[i]/PPNet[i];
			std_eff=UnEff[i]/eff;

			W[i]=1.0/(std_net*std_net+std_eff*std_eff);
		}


		//saving BR,PeakInfo_new,PeakInfo
		// Step 4: Calculate Activity
		double[] Act = new double[NoIso];


		//	Act = ActCorrect(Eff_Coef, PeakEn, BR, PPNet, W, MsTime);

		//18.07.31
		double[] ActTemp = new double[NoIso];
		Act=ActCal_Optimize(result2,Eff_Coef, PeakEn, BR, PPNet, W, MsTime,UnEff,UnPeak,FWHMCoeff,coeff,WndROI);


		// step 5: calculate Uncertainty of isotpe
		double[] Uncer = new double[NoIso];
		double[] RMSE = new double[NoIso];

		for (int i = 0; i < NoIso; i++) {
			for (int j = 0; j < M; j++) {
				BRTemp[j] = BR[j][i];
			}

			Uncer[i] = Uncertainty_CValue(Act[i], PeakEn, UnEff, UnPeak, BRTemp, MsTime, Eff_Coef);

			//Ignore minor peak to Calculate RMSE for Th-232 and Cs-137
			if (result2.get(i).isotopes.equals("Th-232")||result2.get(i).isotopes.equals("Cs-137"))
			{
				BRTemp=RemoveBR_Peak(PeakEn, result2.get(i),FWHMCoeff, coeff, BRTemp,WndROI) ;
			}


			if (result2.get(i).isotopes.equals("Ra-226"))
			{
				//	BRTemp=RemoveBR_SelectedPeak(PeakEn,FWHMCoeff,coeff, BRTemp,0.6, 2615, 0.008);
				BRTemp=RemoveBR_Peak_Thshld(BRTemp, PeakEn,100,0.036);
			}

			RMSE[i] = StdErr(Act[i], BRTemp, PeakEn, PPNet, MsTime, Eff_Coef);
		}

		// double[][] Result = new double[3][Nu.length];
		for (int i = 0; i < NoIso; i++) {
			result2.get(i).Act = Act[i];
			result2.get(i).Uncer = Uncer[i];
			result2.get(i).RMSE = RMSE[i];
		}

		return result2;
	}

	public static Vector<Isotope> ActivityCorrection_H1(double[] Spc, Vector<Isotope> result2, Vector<NcPeak> PeakInfo_new,double MsTime,double[] FWHMCoeff,Coefficients coeff, double[] Eff_Coef,double WndROI)
	{
		//Hung Modified: 17/11/22
		int NoSrc=result2.size();
		double [] TruePeak=new double [100];

		double[][] PeakEnBr_Ref = new double[][] {};
		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;

		for(int i=0;i<NoSrc;i++)
		{
			PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(result2.get(i));

			NoMaxEn = PeakEnBr_Ref.length;

			for (int j = 0; j < NoMaxEn; j++)
			{
				EnTmp = PeakEnBr_Ref[j][0];
				BrTmp= PeakEnBr_Ref[j][1];

				if(BrTmp>0)
				{
					TruePeak[cntPeakTrue]=EnTmp;
					cntPeakTrue=cntPeakTrue+1;
				}
			}
		}


		//remove noise peak
		int MM= PeakInfo_new.size();
		double roi_tmp,Left_thsld,High_thsld;
		int []Flg= new int [MM];

		double[] Thshold = new double[2];

		for(int i=0;i<MM;i++)
		{
			Flg[i]=0;// reset

			EnTmp= PeakInfo_new.get(i).Peak_Energy;

			for(int j=0;j<cntPeakTrue;j++)
			{
				//roi_tmp=Get_Roi_window_by_energy(TruePeak[j]);

				// Left_thsld=TruePeak[j]*(1-roi_tmp * 0.01);
				//  High_thsld=TruePeak[j]*(1+roi_tmp * 0.01);

				Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(TruePeak[j], FWHMCoeff, coeff,WndROI);

				Left_thsld = Thshold[0];
				High_thsld = Thshold[1];


				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Flg[i]=1;
				}

			}
		}




		int M222 = PeakInfo_new.size();

		double[][] PeakInfo = new double[M222][7];
		int cnt11=0;
		for (int i = 0; i < PeakInfo_new.size(); i++)
		{
			//if(PeakInfo_new.get(i).Used_for_Boolen >0)
			{
				PeakInfo[cnt11][0] = PeakInfo_new.get(i).Peak;
				PeakInfo[cnt11][1] = PeakInfo_new.get(i).ROI_Left;
				PeakInfo[cnt11][2] = PeakInfo_new.get(i).ROI_Right;
				PeakInfo[cnt11][3] = PeakInfo_new.get(i).Peak_Energy;
				PeakInfo[cnt11][4] = PeakInfo_new.get(i).NetCnt;
				PeakInfo[cnt11][5] = PeakInfo_new.get(i).Background_Net_Count;
				PeakInfo[cnt11][6] = PeakInfo_new.get(i).LC;
				cnt11=cnt11+1;
			}

		}

		int M = PeakInfo.length;
		int N = PeakInfo[0].length;

		int M2 = 0;
		for (int i = 0; i < M; i++)
		{
			M2 = M2 + Flg[i];
		}


		double[][] PeakInfo11 = new double[M2][N];

		int cnt = 0;
		for (int i = 0; i < M; i++)
		{
			if (Flg[i] == 1)
			{
				for (int j = 0; j < N; j++)
				{
					PeakInfo11[cnt][j] = PeakInfo[i][j];
				}
				cnt = cnt + 1;

			}
		}

		M = PeakInfo11.length;
		N = PeakInfo11[0].length;

		double[] PeakEn11 = new double[M];
		for (int i = 0; i < M; i++)
		{
			PeakEn11[i] = PeakInfo11[i][3];

		}

		// Step 2: Calculate BR
		String[] Nu = new String[result2.size()];
		for (int i = 0; i < result2.size(); i++)
		{
			Nu[i] = result2.get(i).isotopes;
		}

		int NoIso = Nu.length;
		double[][] BR11 = new double[M][NoIso];

		double[] BRTemp11 = new double[M];


		for (int i = 0; i < NoIso; i++)
		{
			BRTemp11 = GetBR_H(PeakEn11, Nu[i], result2.get(i),FWHMCoeff, coeff,WndROI);


			for (int j = 0; j < M; j++)
			{
				BR11[j][i] = BRTemp11[j];
			}
		}

		// Step 3.1: Remove BR with row have zeros value
		int NoLineMeaning=0;
		double sumtmp=0;
		for(int i=0; i< M; i++)
		{
			sumtmp=0;

			for(int j=0;j< NoIso;j++)
			{
				sumtmp=sumtmp+BR11[i][j];
			}

			if(sumtmp>0)
			{
				NoLineMeaning=NoLineMeaning+1;
			}
		}


		double[][] PeakInfo1 = new double[NoLineMeaning][N];
		int NoLineMeaningTmp=0;
		for(int i=0; i< M; i++)
		{
			sumtmp=0;

			for(int j=0;j< NoIso;j++)
			{
				sumtmp=sumtmp+BR11[i][j];
			}

			if(sumtmp>0)
			{

				for(int j=0;j<N;j++)
				{
					PeakInfo1[NoLineMeaningTmp][j] = PeakInfo11[i][j];
				}

				NoLineMeaningTmp=NoLineMeaningTmp+1;
			}
		}




		// step 2: Calcualte Uncertainty of PeakNet and Efficiency
		M = PeakInfo1.length;
		N = PeakInfo1[0].length;

		double[] ROI_L = new double[M];
		double[] ROI_R = new double[M];
		double[] PeakEn = new double[M];

		double[] PPNet = new double[M];
		double[] BGNet = new double[M];
		double[] W = new double[M]; // weighting factor

		for (int i = 0; i < M; i++)
		{
			ROI_L[i] = PeakInfo1[i][1];
			ROI_R[i] = PeakInfo1[i][2];
			PeakEn[i] = PeakInfo1[i][3];
			PPNet[i] = PeakInfo1[i][4];
			BGNet[i] = PeakInfo1[i][5];
			W[i] = 1;
		}

		double[] UnPeak = new double[M];
		double[] UnEff = new double[M];

		UnPeak = PeakUncertainty(Spc, ROI_L, ROI_R, BGNet); // uncertainty of
		// Peak net
		UnEff = EffUncertainty(PeakEn); // uncertainty

		// Step 3: Calculate BR
		//String[] Nu = new String[result2.size()];
		//	for (int i = 0; i < result2.size(); i++)
		//{
		//		Nu[i] = result2.get(i).isotopes;
		//	}

		NoIso = Nu.length;
		double[][] BR = new double[M][NoIso];

		double[] BRTemp = new double[M];

		String BrSum = "";

		for (int i = 0; i < NoIso; i++)
		{
			BRTemp = GetBR_H(PeakEn, Nu[i], result2.get(i),FWHMCoeff, coeff,WndROI);
			BrSum += "\n";
			for (int j = 0; j < M; j++)
			{
				BR[j][i] = BRTemp[j];
				BrSum += Double.toString(BRTemp[j]) + ",";
			}
		}

		//Step 3.2: Calculate weighting factor
		double en,entemp, eff,std_net,std_eff;
		for (int i=0;i<M;i++)
		{
			en = Math.log(PeakEn[i]);

			entemp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)
					+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

			eff = Math.exp(entemp);

			std_net=UnPeak[i]/PPNet[i];
			std_eff=UnEff[i]/eff;

			W[i]=1.0/(std_net*std_net+std_eff*std_eff);
		}


		//saving BR,PeakInfo_new,PeakInfo
		// Step 4: Calculate Activity
		double[] Act = new double[NoIso];


		//	Act = ActCorrect(Eff_Coef, PeakEn, BR, PPNet, W, MsTime);

		//18.07.31
		double[] ActTemp = new double[NoIso];
		Act=ActCal_Optimize(result2,Eff_Coef, PeakEn, BR, PPNet, W, MsTime,UnEff,UnPeak,FWHMCoeff,coeff,WndROI);


		// step 5: calculate Uncertainty of isotpe
		double[] Uncer = new double[NoIso];
		double[] RMSE = new double[NoIso];

		for (int i = 0; i < NoIso; i++) {
			for (int j = 0; j < M; j++) {
				BRTemp[j] = BR[j][i];
			}

			Uncer[i] = Uncertainty_CValue(Act[i], PeakEn, UnEff, UnPeak, BRTemp, MsTime, Eff_Coef);

			//Ignore minor peak to Calculate RMSE for Th-232 and Cs-137
			if (result2.get(i).isotopes.equals("Th-232")||result2.get(i).isotopes.equals("Cs-137"))
			{
				BRTemp=RemoveBR_Peak(PeakEn, result2.get(i),FWHMCoeff, coeff, BRTemp,WndROI) ;
			}


			if (result2.get(i).isotopes.equals("Ra-226"))
			{
				//	BRTemp=RemoveBR_SelectedPeak(PeakEn,FWHMCoeff,coeff, BRTemp,0.6, 2615, 0.008);
				BRTemp=RemoveBR_Peak_Thshld(BRTemp, PeakEn,100,0.036);
			}

			RMSE[i] = StdErr(Act[i], BRTemp, PeakEn, PPNet, MsTime, Eff_Coef);
		}

		// double[][] Result = new double[3][Nu.length];
		for (int i = 0; i < NoIso; i++) {
			result2.get(i).Act = Act[i];
			result2.get(i).Uncer = Uncer[i];
			result2.get(i).RMSE = RMSE[i];
		}

		return result2;
	}

	public static Vector<NcPeak> Return_UnclaimedEn(double[] Spc, Vector<Isotope> result2, Vector<NcPeak> PeakInfo_new,double MsTime, double[] FWHMCoeff,Coefficients coeff, double [] Eff_Coef,double WndROI)
	{

		//Hung Modified: 17/11/22
		int NoSrc=result2.size();
		double [] TruePeak=new double [100];

		double[][] PeakEnBr_Ref = new double[][] {};
		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;

		for(int i=0;i<NoSrc;i++)
		{
			PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(result2.get(i));

			NoMaxEn = PeakEnBr_Ref.length;

			for (int j = 0; j < NoMaxEn; j++)
			{
				EnTmp = PeakEnBr_Ref[j][0];
				BrTmp= PeakEnBr_Ref[j][1];

				if(BrTmp>=0) //comment: =0: include Backscarter peak and Compton peak
				//	if(BrTmp>0) //comment: >0: DO NOT include Backscarter peak and Compton \peak
				{
					TruePeak[cntPeakTrue]=EnTmp;
					cntPeakTrue=cntPeakTrue+1;
				}
			}
		}


		//remove noise peak
		int MM= PeakInfo_new.size();
		double roi_tmp,Left_thsld,High_thsld;
		int []Flg= new int [MM];

		double [] Thshold=new double [2];


		for(int i=0;i<MM;i++)
		{
			Flg[i]=0;// reset

			EnTmp= PeakInfo_new.get(i).Peak_Energy;

			for(int j=0;j<cntPeakTrue;j++)
			{


				Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(TruePeak[j], FWHMCoeff,coeff,WndROI);

				Left_thsld=Thshold[0];
				High_thsld=Thshold[1];

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Flg[i]=1;
				}

			}
		}


		//removed
		Vector<NcPeak> UnClaimedPeakInfo = new Vector<NcPeak>();

		//this is unclaimed line energy
		int cnt=0;
		NcPeak peakData;

		for (int i = 0; i <  MM; i++)
		{
			if (Flg[i] == 0)
			{
				peakData = new NcPeak();


				peakData.Background_Net_Count=PeakInfo_new.get(i).Background_Net_Count;
				peakData.BG_a=PeakInfo_new.get(i).BG_a;
				peakData.BG_b=PeakInfo_new.get(i).BG_b;
				peakData.BR_Factor=PeakInfo_new.get(i).BR_Factor;
				peakData.Channel=PeakInfo_new.get(i).Channel;
				peakData.Doserate=PeakInfo_new.get(i).Doserate;
				peakData.Efficiency=PeakInfo_new.get(i).Efficiency;
				peakData.FWHM_Kev=PeakInfo_new.get(i).FWHM_Kev;
				peakData.Half_life_time=PeakInfo_new.get(i).Half_life_time;
				peakData.Height=PeakInfo_new.get(i).Height;
				peakData.Isotope_Gamma_En_BR=PeakInfo_new.get(i).Isotope_Gamma_En_BR;
				peakData.LC=PeakInfo_new.get(i).LC;
				peakData.Mesuared_Activty=PeakInfo_new.get(i).Mesuared_Activty;
				peakData.NetCnt=PeakInfo_new.get(i).NetCnt;
				peakData.Peak=PeakInfo_new.get(i).Peak;
				peakData.Peak_Energy=PeakInfo_new.get(i).Peak_Energy;
				peakData.Peak_MDA=PeakInfo_new.get(i).Peak_MDA;
				peakData.PeakEst=PeakInfo_new.get(i).PeakEst;
				peakData.ROI_Left=PeakInfo_new.get(i).ROI_Left;
				peakData.ROI_Right=PeakInfo_new.get(i).ROI_Right;
				peakData.sigma=PeakInfo_new.get(i).sigma;
				peakData.True_Current_Activity_Bg=PeakInfo_new.get(i).True_Current_Activity_Bg;
				peakData.Used_for_Boolen=PeakInfo_new.get(i).Used_for_Boolen;



				UnClaimedPeakInfo.add(peakData);
			}
		}




		return UnClaimedPeakInfo;
	}

	public static double Get_Roi_window_by_energy(double en)
	{
		return (72 * Math.pow(en, -0.43));

	}
	//	public static double [] Get_Roi_window_by_energy_used_FWHM(double en,double[] FWHMCoeff,double [] coeff)
//	{
//		double Ratio=0.6;
//
//		double Ch=EntoCh_Cali(en,coeff);
//
//
//		double FWHM=FWHMCoeff[0]*Math.sqrt(Ch)+FWHMCoeff[1];
//
//		double ROI_L=Ch-Ratio*FWHM;
//		double ROI_R=Ch+Ratio*FWHM;
//
//		double ROI_L_En=ChntoEn_Cali(ROI_L,coeff);
//		double ROI_R_En=ChntoEn_Cali(ROI_R,coeff);
//
//		double [] Thshold=new double [2];
//		Thshold[0]=ROI_L_En;
//		Thshold[1]=ROI_R_En;
//
//		return Thshold;
//	}
//	public static double EntoCh_Cali(double Erg,double [] coeff)
//	{
//
//		double a = coeff[0];
//		double b = coeff[1];
//		double c = coeff[2]-Erg;
//
//
//		double Ch=(-b+Math.sqrt(b*b-4*a*c))/(2*a);
//
//		return Ch;
//	}
//	public static double ChntoEn_Cali(double Chn,double [] coeff)
//	{
//
//		double a = coeff[0];
//		double b = coeff[1];
//		double c = coeff[2];
//
//
//		double En=a*Chn*Chn+b*Chn+c;
//
//		return En;
//	}
	public static double[][] Get_MajorMinor_PeakEn_BR(Isotope mIso) {

		int num = mIso.IsoMinorPeakEn.size() + mIso.Peaks.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];

		for (int i = 0; i < mIso.Peaks.size(); i++)
		{
			true_en[i] = mIso.Peaks.get(i).Peak_Energy;
			true_br[i] = mIso.Peaks.get(i).Isotope_Gamma_En_BR;
		}

		for (int i = 0; i < mIso.IsoMinorPeakEn.size(); i++)
		{
			true_en[mIso.Peaks.size() + i] = mIso.IsoMinorPeakEn.get(i);
			true_br[mIso.Peaks.size() + i] = mIso.IsoMinorPeakBR.get(i);

		}

		double[][] PeakEn_Br = new double[true_en.length][2];

		for (int i = 0; i < true_en.length; i++)
		{
			PeakEn_Br[i][0] = true_en[i];
			PeakEn_Br[i][1] = true_br[i];
		}


		return PeakEn_Br;
	}
	public static double StdErr(double C, double []Br, double []PeakEn, double [] PPNet, double MSTime, double [] Eff_Coef)
	{
		double rmse = 0;
		double time1 = MSTime;

		int N = PeakEn.length;
		double[] CalNet = new double[N];

		double en, tmp, eff;

		for (int i = 0; i < N; i++)
		{
			CalNet[i] = 0; //initialize

			en = Math.log(PeakEn[i]);

			tmp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0) + Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

			eff = Math.exp(tmp);

			CalNet[i] = C * Br[i] * time1 * eff * 37000;
		}


		int cnt = 0;
		for(int i = 0; i < N; i++)
		{
			if (CalNet[i] > 0)
			{
				cnt = cnt + 1;
			}
		}

		if (cnt > 0)
		{
			double[] C_Cal = new double[cnt];
			double [] C_Mea =new double[cnt];

			cnt = 0;

			for(int i = 0; i < N; i++)
			{
				if (CalNet[i] > 0)
				{
					C_Cal[cnt] = CalNet[i];
					C_Mea[cnt] = PPNet[i];
					cnt = cnt + 1;
				}

			}

			rmse = RMSECal(C_Cal, C_Mea);


		}
		else
		{
			rmse = 100;
		}


		return rmse;
	}

	public static double RMSECal(double []C0, double[] C1)
	{
		double m_rmse = 0;
		int N = C0.length;

		double[] bn = new double[N];
		double tmp = 0;
		for(int i = 0; i < N; i++)
		{
			tmp= (C0[i] + C1[i])/ 2d;

			bn[i] = Math.abs(C0[i] - C1[i]) / tmp;
		}

		double br = 0;
		double sum1 = 0;
		for(int i = 0; i < N; i++)
		{
			sum1 = sum1 + bn[i];
		}

		br = sum1 / (double)N;

		double sb = 0;

		sum1 = 0;
		for(int i= 0; i < N; i++)
		{
			sum1 = sum1 + (bn[i] - br) * (bn[i] - br);
		}

		if (N > 1)
		{
			sb = sum1 / (double)(N - 1);
		}
		else
		{
			sb = sum1 / (double)N;
		}

		sb = Math.sqrt(sb);


		m_rmse = Math.sqrt(sb*sb+ br*br);

		return m_rmse;
	}

	public static double Calculate_PeakUncertainty(double[] Spc, double BGSum, int ROI_L, int ROI_R) {
		double U = 0, G = 0;

		for (int i = ROI_L; i <= ROI_R; i++) {
			G = G + Spc[i];
		}

		double W = ROI_R - ROI_L + 1;
		W=2;
		double BG1 = W / 2d * BGSum;

		if (G + BG1 > 0) {
			U = Math.sqrt(G + W / 2 * BGSum);
		}

		return U;
	}

	public static double Calculate_EffUncertainty(double en) {
		double std = 0;

		double[] En = new double[] { 59.54, 88.03, 122.06, 165.86, 391.7, 661.66, 898.04, 1173.23, 1332.49, 1836.05 };

		double[] StdEff = new double[] { 0.000426431, 0.000598509, 0.00066558, 0.000749811, 0.0009499, 0.000402778,
				0.000531464, 0.000291814, 0.000239788, 0.000218446 };

		int N = En.length;

		int ind = 0;

		for (int i = 0; i < N - 1; i++) {
			if (en >= En[i] && en <= En[i + 1]) {
				ind = i;
				std = StdEff[i] + (StdEff[i + 1] - StdEff[i]) / (En[i + 1] - En[i]) * (en - En[i]);
			}
		}

		if (ind == 0) {
			if (en < En[0]) {
				std = StdEff[0];
			}

			if (en > En[N - 1]) {
				std = StdEff[N - 1];
			}
		}

		return std;
	}

	public static double[] PeakUncertainty(double[] Spc, double[] ROI_L, double[] ROI_R, double[] BGNet) {

		int M = ROI_L.length;

		double[] WF_Std = new double[M];

		double std_net = 0;
		for (int i = 0; i < M; i++) {
			std_net = Calculate_PeakUncertainty(Spc, BGNet[i], (int) ROI_L[i] , (int) ROI_R[i] );
			WF_Std[i] = std_net;
		}
		return WF_Std;
	}

	public static double[] EffUncertainty(double[] PeakEn) {

		int M = PeakEn.length;

		double[] WF_Eff = new double[M];

		double std_eff = 0;
		for (int i = 0; i < M; i++) {
			std_eff = Calculate_EffUncertainty(PeakEn[i]);
			WF_Eff[i] = std_eff;
		}
		return WF_Eff;
	}

	public static double[] GetBR(double[] PeakEn, String Nu, Isotope mIso ) {
		int M = PeakEn.length;

		double[] BR = new double[M];

		for (int i = 0; i < M; i++) {
			BR[i] = 0;
		}


		// load Database

		for(int i=0;i<mIso.IsoPeakEn.size();i++)
		{

		}

		int num = mIso.IsoMinorPeakEn.size() + mIso.Peaks.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];


		for (int i = 0; i < mIso.Peaks.size(); i++) {
			true_en[i] =mIso.Peaks.get(i).Peak_Energy;
			true_br[i] =mIso.Peaks.get(i).Isotope_Gamma_En_BR;

		}

		for (int i = 0; i < mIso.IsoMinorPeakEn.size(); i++) {
			true_en[mIso.Peaks.size() + i] =mIso.IsoMinorPeakEn.get(i);
			true_br[mIso.Peaks.size() + i] =mIso.IsoMinorPeakBR.get(i);

		}



		BR = BRSetup(PeakEn, true_en, true_br);




		return BR;

	}

	public static double[] GetBR_H(double[] PeakEn, String Nu, Isotope mIso,double[] FWHMCoeff,Coefficients coeff,double WndROI)
	{
		int M = PeakEn.length;


		double[] BR = new double[M];

		for (int i = 0; i < M; i++) {
			BR[i] = 0;
		}

		// load Database
		int num = mIso.IsoMinorPeakEn.size() + mIso.Peaks.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];

		for (int i = 0; i < mIso.Peaks.size(); i++)
		{
			true_en[i] = mIso.Peaks.get(i).Peak_Energy;
			true_br[i] = mIso.Peaks.get(i).Isotope_Gamma_En_BR;


		}

		for (int i = 0; i < mIso.IsoMinorPeakEn.size(); i++)
		{
			true_en[mIso.Peaks.size() + i] = mIso.IsoMinorPeakEn.get(i);
			true_br[mIso.Peaks.size() + i] = mIso.IsoMinorPeakBR.get(i);
		}

		BR = BRSetup_H(PeakEn, true_en, true_br,FWHMCoeff,coeff,WndROI);



		return BR;

	}

	public static double[] BRSetup_H(double[] PeakEn, double[] TrueErg, double[] BR0,double[] FWHMCoeff,Coefficients coeff,double WndROI)
	{
		int N = PeakEn.length;
		double[] BR = new double[N];

		int M = TrueErg.length;

		double lowthsld = 0, highthshld = 0;
		double [] Thshold=new double [2];

		double erg = 0, En0 = 0;
		double ROI_Percnt = 0;

		for (int i = 0; i < N; i++)
		{
			erg = PeakEn[i];
			for (int j = 0; j < M; j++)
			{
				En0 = TrueErg[j];


				Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(En0, FWHMCoeff,coeff,WndROI);

				lowthsld=Thshold[0];
				highthshld=Thshold[1];

				//ROI_Percnt = (72 * Math.pow(En0, -0.43)) / 100d;

				//	lowthsld = (1 - ROI_Percnt) * En0;
				//highthshld = (1 + ROI_Percnt) * En0;

				if (erg >= lowthsld && erg <= highthshld)
				{

					BR[i] = BR0[j];

				}
			}
		}

		return BR;
	}

	public static double[] BRSetup(double[] PeakEn, double[] TrueErg, double[] BR0) {
		int N = PeakEn.length;
		double[] BR = new double[N];

		int M = TrueErg.length;

		double lowthsld = 0, highthshld = 0;

		double erg = 0, En0 = 0;
		double ROI_Percnt = 0;
		for (int i = 0; i < N; i++) {
			erg = PeakEn[i];
			for (int j = 0; j < M; j++) {
				En0 = TrueErg[j];

				ROI_Percnt = (72 * Math.pow(En0, -0.43)) / 100d;

				lowthsld = (1 - ROI_Percnt) * En0;
				highthshld = (1 + ROI_Percnt) * En0;

				if (erg >= lowthsld && erg <= highthshld) {
					BR[i] = BR0[j];

				}
			}
		}

		return BR;
	}

	public static double[] ActCorrect(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,	double time) {

		int NoLineMeaning=0;
		double sumtmp=0;

		int N = BR.length; // N: Number of peak erngy
		int M = BR[0].length;// M: number of isotope


		for(int i=0; i< N; i++)
		{
			sumtmp=0;

			for(int j=0;j< M;j++)
			{
				sumtmp=sumtmp+BR[i][j];
			}

			if(sumtmp>0)
			{
				NoLineMeaning=NoLineMeaning+1;
			}
		}

		double [] Peaken1=new double [NoLineMeaning];

		double []  Y1=new double [NoLineMeaning];

		double [] W1=new double [NoLineMeaning];

		double [][] BR1=new double [NoLineMeaning][M];


		int cnt=0;

		for(int i=0; i< N; i++)
		{
			sumtmp=0;

			for(int j=0;j< M;j++)
			{
				sumtmp=sumtmp+BR[i][j];
			}

			if(sumtmp>0)
			{

				Peaken1[cnt]=Peaken[i];
				Y1[cnt]=Y[i];
				W1[cnt]=W[i];

				for(int j=0;j< M;j++)
				{
					BR1[cnt][j]=BR[i][j];
				}
				cnt=cnt+1;
			}
		}



		//int N1 = BR.length; // N: Number of peak erngy
		//int M1 = BR[0].length;// M: number of isotope

		//Modify: 18.15.10
		int N1 = BR1.length; // N: Number of peak energy
		int M1 = BR1[0].length;// M: number of isotope

		double[] Act = new double[M];

		Act=ActCorrect_Processing_IdenticalIso(Eff_Coef, Peaken1,  BR1,  Y1,  W1, time) ;

		/*
		if(N1>=M1)
		{
			Act=ActCorrect_Old(Eff_Coef, Peaken1,  BR1,  Y1,  W1, time) ;

		}
		else
		{
			double [] ActTmp= new double [N1];

			double time1=time;
			double en, eff, tmp;

			double SumAct=0;
			for (int i = 0; i < N1; i++)
			{
				en = Math.log(Peaken1[i]);

				tmp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)
						+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

				eff = Math.exp(tmp);

				for (int k = 0; k < M1; k++)
				{
					if(BR1[i][k]>0)
					{
						ActTmp[i]=ActTmp[i]+Y[i] /(eff*time1 * BR1[i][k]);
					}
				}

				ActTmp[i]=ActTmp[i]/(double)37000;

				SumAct=SumAct+ActTmp[i];
			}

			for (int i = 0; i < M; i++)
			{
				Act[i]=SumAct/(double)M;
			}

		}

		*/
		return Act;

	}

	public static double[] ActCorrect_Old(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,	double time) {

		int N = BR.length; // N: Number of peak erngy
		int M = BR[0].length;// M: number of isotope

		double[][] MA = new double[M][M];
		double[][] MA1 = new double[M][M];
		double[] MB = new double[M];
		double[] MC = new double[M];
		double[] Act = new double[M];

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < M; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
			Act[i] = 0;
		}

		double time1 = time;

		double en, eff, tmp;

		for (int i = 0; i < N; i++) {
			en = Math.log(Peaken[i]);

			tmp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)
					+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

			eff = Math.exp(tmp);

			W[i] = 1;// set weighting factor =1

			for (int k = 0; k < M; k++) {
				for (int q = 0; q < M; q++) {
					MA[k][q] = MA[k][q] + BR[i][q] * W[i] * BR[i][k];
				}

				MB[k] = MB[k] + Y[i] / eff / time1 * W[i] * BR[i][k];
			}

		}

		// solve Matrix
		for (int i = 0; i < M; i++)
		{
			for (int j = 0; j < M; j++)
			{
				if (MA[i][j] == 0)
				{
					MA[i][j] = 0.000000000001;
				}

				//debug 18.01.04
				//MA[i][j] =1;
			}
		}
		MA1 = InverseMatrix(MA1, MA, M);
		MC = MultiMatrix(MC, MA1, MB, M);

		for (int i = 0; i < M; i++) {
			Act[i] = MC[i] / 37000d;
		}

		return Act;


	}
	public static double[] ActCal(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,double time)
	{
		double UnRef=0.1; // 10%
		int M = BR.length; // N: Number of peak erngy
		int N = BR[0].length;// M: number of isotope

		int NoSrc=N;

		double[] ActInd_Src = new double[NoSrc];
		double[] Act = new double[NoSrc];
		ActInd_Src=ActMeaSingle( Eff_Coef, Peaken,  BR,  Y,  W, time);

		int cnt=0;

		for(int i=0;i<NoSrc;i++)
		{
			if(ActInd_Src[i]>0)
			{
				cnt=cnt+1;
				Act[i]=ActInd_Src[i];
			}
		}


		//Condition


		if(cnt<NoSrc)
		{
			int NoSrc1=NoSrc-cnt;
			double [][] BR1 = new double[M][NoSrc1];

			int cnt1=0;

			for(int i=0;i<NoSrc;i++)
			{
				if(ActInd_Src[i]==0)
				{
					for(int j=0;j<M;j++)
					{
						BR1[j][cnt1]=BR[j][i];
					}
					cnt1=cnt1+1;

				}
			}


			//%find Reference
			int NoIsotope=cnt1;
			double[] Flg1 = new double[M];
			int cnt2=0;
			for(int i=0;i<M;i++)
			{
				cnt2=0;
				for(int j=0;j<NoIsotope;j++)
				{
					if(BR1[i][j]>0)
					{
						cnt2=cnt2+1;
					}
				}

				Flg1[i]=cnt2;
			}

			//Step
			int FLg_Inte=0;
			int NoInfer=0;

			for(int i=0;i<M;i++)
			{
				if(Flg1[i]>1)
				{
					NoInfer=NoInfer+1;
					FLg_Inte=1;
				}
			}

			double[] ActTmp = new double[NoIsotope];

			if(NoInfer==0) //%% No interfence
			{
				ActTmp=ActCorrect(Eff_Coef, Peaken, BR1, Y, W, time);
			}
			else
			{
				if(NoIsotope>=NoInfer)
				{
					ActTmp=ActCorrect(Eff_Coef, Peaken, BR1, Y, W, time);
				}
				else
				{

					double []PeakEnMea1= new double[NoInfer];
					double [][] BR2=new double[NoInfer][NoIsotope];
					double [] PPNet1=new double[NoInfer];
					double [] w1=new double[NoInfer];

					int cnt11=0;

					for(int i=0;i<M;i++)
					{
						if(Flg1[i]>1)
						{
							PeakEnMea1[cnt11]=Peaken[i];
							PPNet1[cnt11]=Y[i];
							w1[cnt11]=W[i];

							for(int j=0;j<NoIsotope;j++)
							{
								BR2[cnt11][j]=BR1[i][j];
							}
							cnt11=cnt11+1;
						}
					}

					ActTmp=ActCorrect(Eff_Coef, PeakEnMea1, BR2, PPNet1, w1, time);


					// %% remove act: Threshold of Act, We set
					//%% if candidate <10%: we can exist

					double SumAct=0;

					for(int i=0;i<NoIsotope;i++)
					{
						SumAct=SumAct+ActTmp[i];
					}
					if(SumAct==0) SumAct=0.000000000001;

					double Ratio;
					for(int i=0;i<NoIsotope;i++)
					{
						Ratio=ActTmp[i]/SumAct;

						if(Ratio<UnRef)
						{
							ActTmp[i]=0;
						}
					}


				}
			}


			int cnt3=0;
			for(int i=0;i<NoSrc;i++)
			{
				if(ActInd_Src[i]==0)
				{
					Act[i]=ActTmp[cnt3];
					cnt3=cnt3+1;
				}
			}

		}



		return Act;
	}
	public static double[] ActMeaSingle(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,double time)
	{
		int M = BR.length; // N: Number of peak erngy
		int N = BR[0].length;// M: number of isotope

		int NoSrc=N;


		double [][] FlgMix = new double[M][N];
		double [][] MatrixInter = new double[M][N];
		double [] Independ_Src = new double[N];
		double[] Act = new double[N];

		double sumBr=0;
		for(int src=0;src<NoSrc;src++)
		{

			for (int i=0;i<M;i++)
			{
				if(BR[i][src]>0)
				{
					sumBr=0;

					for(int j=0;j<N;j++)
					{
						if(j==src)
						{

						}
						else
						{
							sumBr=sumBr+BR[i][j];
						}
					}

					if(sumBr==0)
					{
						FlgMix[i][src]=1;
					}

				}
			}

		}


		//Step 2
		int cnt=0;
		for(int i=0;i<M;i++)
		{
			cnt=0;
			for(int j=0;j<N;j++)
			{
				if(BR[i][j]>0)
				{
					cnt=cnt+1;
				}
			}

			if(cnt>1)
			{
				for(int j=0;j<N;j++)
				{
					if(BR[i][j]>0)
					{
						MatrixInter[i][j]=1;
					}
				}
			}
		}


		//Step 3: Independen source
		double SumMI=0;
		for(int i=0;i<N;i++)
		{
			SumMI=0;

			for(int j=0;j<M;j++)
			{
				SumMI=SumMI+MatrixInter[j][i];
			}

			if(SumMI==0)
			{
				Independ_Src[i]=1;
			}
		}

		//step 4: Calculate activity
		double[][] BRTmp = new double[M][1];
		double[] ActTmp = new double[1];


		for(int i=0;i<N;i++)
		{
			if(Independ_Src[i]==1)
			{
				for(int j=0;j<M;j++)
				{
					BRTmp[j][0]=BR[j][i];
				}

				ActTmp=ActCorrect(Eff_Coef, Peaken, BRTmp, Y, W, time);

				Act[i]=ActTmp[0];
			}
			else
			{
				Act[i]=0;
			}
		}

		return Act;
	}

	public static double[] MultiMatrix(double[] M, double[][] A, double[] B, int N) {
		double sum1 = 0;
		for (int i = 0; i < N; i++) {
			sum1 = 0;
			for (int j = 0; j < N; j++) {
				sum1 = sum1 + A[i][j] * B[j];
			}
			M[i] = sum1;
		}
		return M;
	}

	public static double[][] InverseMatrix(double[][] A1, double[][] A, int n) {
		double[][] a = new double[n][2 * n];

		// double[,] a = new double[2 * n,n];

		// for (int i = 0; i < n; i++)
		// {
		// a[i] = new double[2 * n];

		// }

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				a[i][j] = A[i][j];
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = n; j < 2 * n; j++) {
				if (i == j % n)
					a[i][j] = 1;
				else
					a[i][j] = 0;
			}
		}
		a[0][n] = 1;

		int temp;
		for (int j = 0; j < n; j++) {
			temp = j;

			for (int i = j + 1; i < n; i++) {
				if (a[i][j] > a[temp][j])
					temp = i;
			}

			if (temp != j) {
				for (int k = 0; k < 2 * n; k++) {
					double temporary = a[j][k];
					a[j][k] = a[temp][k];
					a[temp][k] = temporary;
				}
			}

			for (int i = 0; i < n; i++) {
				if (i != j) {
					double r = a[i][j];

					for (int k = 0; k < 2 * n; k++) {
						a[i][k] = a[i][k] - (a[j][k] / a[j][j]) * r;
					}

				} else {
					double r = a[i][j];
					for (int k = 0; k < 2 * n; k++) {
						a[i][k] = a[i][k] / r;
					}
				}
			}

		}

		for (int i = 0; i < n; i++) {
			for (int j = n; j < 2 * n; j++) {
				A1[i][j - n] = a[i][j];
			}
		}
		return A1;
	}

	public static double Uncertainty_CValue(double C, double[] PeakEn, double[] UnEff, double[] UnPeakNet, double[] Br,
											double MSTime, double[] Eff_Coef) {
		double time1 = MSTime;

		int N = PeakEn.length;

		double[] CalNet = new double[N];

		double en, tmp, eff;

		for (int i = 0; i < N; i++) {
			en = Math.log(PeakEn[i]);

			tmp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)
					+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

			eff = Math.exp(tmp);

			CalNet[i] = C * Br[i] * time1 * eff * 37000;
		}

		// calculate uncertainty
		double[] UnC = new double[N];
		double[] UnC_per = new double[N];
		double tmp1, tmp2;
		for (int i = 0; i < N; i++) {
			UnC[i] = 0;
			if (CalNet[i] > 0) {
				en = Math.log(PeakEn[i]);

				tmp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0)
						+ Eff_Coef[2] * Math.pow(en, 2.0) + Eff_Coef[3] * Math.pow(en, 1.0)
						+ Eff_Coef[4] * Math.pow(en, 0);

				eff = Math.exp(tmp);

				tmp1 = UnPeakNet[i] / CalNet[i];
				tmp2 = UnEff[i] / eff;

				UnC[i] = C * Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2);
			}
		}

		for (int i = 0; i < N; i++) {
			UnC_per[i] = UnC[i] / C * 100;
		}

		double avg_un = 0, sum1 = 0;
		for (int i = 0; i < N; i++) {
			if (CalNet[i] > 0) {
				sum1 = sum1 + 1 / (UnC_per[i] * UnC_per[i]);
			}
		}

		avg_un = Math.sqrt(1d / sum1);

		return avg_un;
	}
	public static double[] RemoveBR_Peak(double[] PeakEn, Isotope mIso,double[] FWHMCoeff,Coefficients coeff, double [] BRMinorMajor,double WndROI)
	{
		double [][] PeakBr_minor=Get_Minor_PeakEn_BR( mIso);

		int NoMaxEn = PeakBr_minor.length;

		boolean Flg_En=false, Flg_Br=false;
		double [] Thshold=new double [2];
		double lowthsld,highthshld,erg, erg0,br0;

		for(int i=0;i<NoMaxEn;i++)
		{


			erg0=PeakBr_minor[i][0];
			br0=PeakBr_minor[i][1];

			Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(erg0, FWHMCoeff,coeff,WndROI);
			lowthsld=Thshold[0];
			highthshld=Thshold[1];

			for(int j=0;j<BRMinorMajor.length;j++)
			{
				Flg_En=false;

				Flg_Br=false;

				if(br0==BRMinorMajor[j])
				{
					Flg_Br=true;
				}


				erg=PeakEn[j];
				if (erg >= lowthsld && erg <= highthshld)
				{
					Flg_En=true;
				}


				if(Flg_Br==true&&Flg_En==true)
				{
					BRMinorMajor[j]=0;
				}
			}
		}



		return BRMinorMajor;

	}
	//Added more: 18.04.08
	public static double[][] Get_Minor_PeakEn_BR(Isotope mIso) {

		int num = mIso.IsoMinorPeakEn.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];

		for (int i = 0; i < num; i++)
		{
			true_en[i] = mIso.IsoMinorPeakEn.get(i);
			true_br[i] = mIso.IsoMinorPeakBR.get(i);
		}

		double[][] PeakEn_Br = new double[true_en.length][2];

		for (int i = 0; i < true_en.length; i++)
		{
			PeakEn_Br[i][0] = true_en[i];
			PeakEn_Br[i][1] = true_br[i];
		}


		return PeakEn_Br;

	}

	//Remove peak with energy is bigger than threshold of Branching
	//
	public static double[] RemoveBR_Peak_Thshld(double [] BRMinorMajor,double[] PeakEn, double EnThsld , double BrThshld)
	{
		for(int j=0;j<BRMinorMajor.length;j++)
		{
			if(PeakEn[j]<=EnThsld)
			{
				BRMinorMajor[j]=0;
			}
		}

		for(int j=0;j<BRMinorMajor.length;j++)
		{
			if(BRMinorMajor[j]<=BrThshld)
			{
				BRMinorMajor[j]=0;
			}
		}

		return BRMinorMajor;

	}

	//Date:18.07.31
	// ReCalculate activity:
	// Using Reference Peak and not Reference Peak which is major peak
	//Only using minor peak for calculate activity by using Reference Peak

	public static double[] ActCal_Used_RefPeak(Vector<Isotope> result2,double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,double time, double []UnEff,double []UnPeak,
											   double[] FWHMCoeff, Coefficients coeff,double WndROI)
	{
		double UnRef=0.1; // 10%
		int M = BR.length; // N: Number of peak energy
		int N = BR[0].length;// M: number of isotope

		int NoSrc=N;

		double[] ActInd_Src = new double[NoSrc];
		double[] Act = new double[NoSrc];

		// Step 1: Check Nuclide without interfering with other isotope
		ActInd_Src=ActMeaSingle( Eff_Coef, Peaken,  BR,  Y,  W, time);

		int cnt=0;

		for(int i=0;i<NoSrc;i++)
		{
			if(ActInd_Src[i]>0)
			{
				cnt=cnt+1;
				Act[i]=ActInd_Src[i];
			}
		}


		//Condition

		//Step 2: Find interfering Peak

		if(cnt<NoSrc)
		{
			int NoSrc1=NoSrc-cnt;
			double [][] BR1 = new double[M][NoSrc1];

			double [][]  ListPeakMajor=new double[M][NoSrc1];
			int NoMaxEn;
			double[][] PeakEnBr_Ref = new double[][] {};
			double EnTmp,BrTmp;

			int cnt1=0;

			for(int i=0;i<NoSrc;i++)
			{
				if(ActInd_Src[i]==0)
				{
					for(int j=0;j<M;j++)
					{
						BR1[j][cnt1]=BR[j][i];
					}

					//Get Major Peak
					PeakEnBr_Ref = Get_Major_PeakEn_BR(result2.get(i));
					NoMaxEn = PeakEnBr_Ref.length;
					double Left_thsld,High_thsld;
					double[] Thshold = new double[2];
					for (int j=0;j<M;j++)
					{
						EnTmp=Peaken[j];

						for (int k = 0; k < NoMaxEn; k++)
						{
							Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(PeakEnBr_Ref[k][0], FWHMCoeff, coeff,WndROI);

							Left_thsld = Thshold[0];
							High_thsld = Thshold[1];

							if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
							{
								ListPeakMajor[j][cnt1]=EnTmp;
							}
						}
					}

					cnt1=cnt1+1;
				}
			}


			//%find Reference
			int NoIsotope=cnt1;
			double[] Flg1 = new double[M];
			int cnt2=0;
			for(int i=0;i<M;i++)
			{
				cnt2=0;
				for(int j=0;j<NoIsotope;j++)
				{
					if(BR1[i][j]>0)
					{
						cnt2=cnt2+1;
					}
				}

				Flg1[i]=cnt2;
			}

			//Step
			int FLg_Inte=0;
			int NoInfer=0;

			for(int i=0;i<M;i++)
			{
				if(Flg1[i]>1)
				{
					NoInfer=NoInfer+1;
					FLg_Inte=1;
				}
			}

			double[] ActTmp = new double[NoIsotope];

			// NoInfer=0;
			if(NoInfer==0) //%% No interfence
			{
				ActTmp=ActCorrect(Eff_Coef, Peaken, BR1, Y, W, time);
			}
			else
			{
				if(NoIsotope>=NoInfer)
				{
					ActTmp=ActCorrect(Eff_Coef, Peaken, BR1, Y, W, time);
				}
				else
				{

					double []PeakEnMea1= new double[NoInfer];
					double [][] BR2=new double[NoInfer][NoIsotope];
					double [] PPNet1=new double[NoInfer];
					double [] w1=new double[NoInfer];

					double [] UnEff1=new double[NoInfer];
					double [] UnPeak1=new double[NoInfer];

					int cnt11=0;

					for(int i=0;i<M;i++)
					{
						if(Flg1[i]>1)
						{
							PeakEnMea1[cnt11]=Peaken[i];
							PPNet1[cnt11]=Y[i];
							w1[cnt11]=W[i];

							UnEff1[cnt11]=UnEff[i];
							UnPeak1[cnt11]=UnPeak[i];

							for(int j=0;j<NoIsotope;j++)
							{
								BR2[cnt11][j]=BR1[i][j];
							}
							cnt11=cnt11+1;
						}

					}

					ActTmp=ActCorrect(Eff_Coef, PeakEnMea1, BR2, PPNet1, w1, time);

					//Add Peak Information
					//Find Major Peak which are not interference
					double []UnInter_Act=new double [NoIsotope];

					for(int numiso=0;numiso<NoIsotope;numiso++)
					{
						//Find Number Major which are not interference
						for(int j=0;j<M;j++)
						{
							for (int k=0;k<NoInfer;k++)
							{
								if(ListPeakMajor[j][numiso]==PeakEnMea1[k])
								{
									ListPeakMajor[j][numiso]=0;
								}
							}

						}

						// Count

						int UnInterfereMajorPeak=0;
						for(int j=0;j<M;j++)
						{
							if(ListPeakMajor[j][numiso]>0)
							{
								UnInterfereMajorPeak=UnInterfereMajorPeak+1;
							}
						}

						//
						if(UnInterfereMajorPeak>0)
						{
							// Recalculate activity based on UnInterfence
							double []PeakMajorTmp= new double[UnInterfereMajorPeak];
							double [][] BRMajorTmp=new double[UnInterfereMajorPeak][1];
							double [] PPNetMajorTmp=new double[UnInterfereMajorPeak];
							double [] wMajorTmp=new double[UnInterfereMajorPeak];
							double [] UnEffMajorTmp=new double[UnInterfereMajorPeak];
							double [] UnPeakMajorTmp=new double[UnInterfereMajorPeak];

							int CntMajorTmp=0;

							for(int j=0;j<M;j++)
							{
								if(ListPeakMajor[j][numiso]>0)
								{
									if(ListPeakMajor[j][numiso]==Peaken[j])
									{
										PeakMajorTmp[CntMajorTmp]=Peaken[j];
										PPNetMajorTmp[CntMajorTmp]=Y[j];
										wMajorTmp[CntMajorTmp]=W[j];

										UnEffMajorTmp[CntMajorTmp]=UnEff[j];
										UnPeakMajorTmp[CntMajorTmp]=UnPeak[j];

										BRMajorTmp[CntMajorTmp][0]=BR1[j][numiso];

										CntMajorTmp=CntMajorTmp+1;
									}
								}
							}

							//Calculate Activity
							double [] ActTmp11=new double [1];
							ActTmp11=ActCorrect(Eff_Coef, PeakMajorTmp, BRMajorTmp, PPNetMajorTmp, wMajorTmp, time);
							UnInter_Act[numiso]=ActTmp11[0];

						}
					}

					//average activitity
					for(int i=0;i<NoIsotope;i++)
					{
						if(ActTmp[i]<=0)
						{
							ActTmp[i]=0;
						}

						if(UnInter_Act[i]>0)
						{
							if(ActTmp[i]==0)
							{
								ActTmp[i]=UnInter_Act[i];
							}
							else
							{
								ActTmp[i]=(ActTmp[i]+UnInter_Act[i])/2.0;
							}
						}
					}


					// %% remove act: Threshold of Act, We set
					//%% if candidate <10%: we can exist



					double SumAct=0;

					for(int i=0;i<NoIsotope;i++)
					{
						SumAct=SumAct+ActTmp[i];
					}
					// if(SumAct==0) SumAct=0.000000000001;

					if(SumAct>0)
					{
						double Ratio;
						for(int i=0;i<NoIsotope;i++)
						{
							Ratio=ActTmp[i]/SumAct;

							if(Ratio<NewNcAnalsys.ActThreshold)
							{
								ActTmp[i]=0;
							}
						}
					}
				}
			}


			int cnt3=0;
			for(int i=0;i<NoSrc;i++)
			{
				if(ActInd_Src[i]==0)
				{
					Act[i]=ActTmp[cnt3];
					cnt3=cnt3+1;
				}
			}

		}


		return Act;
	}


	public static double[][] Get_Major_PeakEn_BR(Isotope mIso) {

		int num = mIso.Peaks.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];

		for (int i = 0; i < mIso.Peaks.size(); i++)
		{
			true_en[i] = mIso.Peaks.get(i).Peak_Energy;
			true_br[i] = mIso.Peaks.get(i).Isotope_Gamma_En_BR;
		}

		double[][] PeakEn_Br = new double[true_en.length][2];

		for (int i = 0; i < true_en.length; i++)
		{
			PeakEn_Br[i][0] = true_en[i];
			PeakEn_Br[i][1] = true_br[i];
		}

		return PeakEn_Br;
	}


	public static double[] ActCal_Used_MajorPeak(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,double time, double [][]ListPeakMajor)
	{
		double UnRef=0.05; // 5%%, always set activity 5%
		int M = BR.length; // N: Number of peak energy
		int N = BR[0].length;// M: number of isotope

		int NoIsotope=N;

		//Step 1: Use all Major line for Calculate activity
		double[] Act = new double[NoIsotope];
		double EnTmp;



		//
		int NoMajorPeak=0;

		for(int i=0;i<M;i++)
		{
			EnTmp=0;
			for(int j=0;j<NoIsotope;j++)
			{
				if(ListPeakMajor[i][j]>EnTmp)
				{
					EnTmp=ListPeakMajor[i][j];
				}
			}

			if(EnTmp>0)
			{
				NoMajorPeak=NoMajorPeak+1;
			}

		}

		if(NoMajorPeak>=NoIsotope)
		{
			//Step 3: Using interfrence peak
			double []PeakEnMea1= new double[NoMajorPeak];
			double [][] BR2=new double[NoMajorPeak][NoIsotope];
			double [] PPNet1=new double[NoMajorPeak];
			double [] w1=new double[NoMajorPeak];


			int cnt11=0;
			for(int i=0;i<M;i++)
			{
				EnTmp=0;
				for(int j=0;j<NoIsotope;j++)
				{
					if(ListPeakMajor[i][j]>EnTmp)
					{
						EnTmp=ListPeakMajor[i][j];
					}
				}

				if(EnTmp>0)
				{
					PeakEnMea1[cnt11]=Peaken[i];
					PPNet1[cnt11]=Y[i];
					w1[cnt11]=W[i];

					for(int j=0;j<NoIsotope;j++)
					{
						BR2[cnt11][j]=BR[i][j];
					}
					cnt11=cnt11+1;
				}
			}

			Act=ActCorrect(Eff_Coef, PeakEnMea1, BR2, PPNet1, w1, time);
		}
		else
		{
			Act=ActCal_Used_MajorMinorPeak(Eff_Coef,Peaken,BR,Y,W,time,ListPeakMajor);
		}


		return Act;
	}

	public static double[] ActCal_Optimize(Vector<Isotope> result2,double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,double time, double []UnEff,double []UnPeak,
										   double[] FWHMCoeff, Coefficients coeff,double WndROI)
	{
		double UnRef=0.05; // 5%%, always set activity 5%
		int M = BR.length; // N: Number of peak energy
		int N = BR[0].length;// M: number of isotope

		int NoSrc=N;

		double[] ActInd_Src = new double[NoSrc];
		double[] Act = new double[NoSrc];

		// Step 1: Check Nuclide without interfering with other isotope
		ActInd_Src=ActMeaSingle( Eff_Coef, Peaken,  BR,  Y,  W, time);

		int cnt=0;

		for(int i=0;i<NoSrc;i++)
		{
			if(ActInd_Src[i]>0)
			{
				cnt=cnt+1;
				Act[i]=ActInd_Src[i];
			}
		}


		//Condition

		//Step 2: Find interfering Peak

		if(cnt<NoSrc)
		{
			int NoSrc1=NoSrc-cnt;
			double [][] BR1 = new double[M][NoSrc1];

			double [][]  ListPeakMajor=new double[M][NoSrc1];
			int NoMaxEn;
			double[][] PeakEnBr_Ref = new double[][] {};
			double EnTmp,BrTmp;

			int cnt1=0;

			for(int i=0;i<NoSrc;i++)
			{
				if(ActInd_Src[i]==0)
				{
					for(int j=0;j<M;j++)
					{
						BR1[j][cnt1]=BR[j][i];
					}

					//Get Major Peak
					PeakEnBr_Ref = Get_Major_PeakEn_BR(result2.get(i));
					NoMaxEn = PeakEnBr_Ref.length;
					double Left_thsld,High_thsld;
					double[] Thshold = new double[2];
					for (int j=0;j<M;j++)
					{
						EnTmp=Peaken[j];

						for (int k = 0; k < NoMaxEn; k++)
						{
							Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(PeakEnBr_Ref[k][0], FWHMCoeff, coeff,WndROI);

							Left_thsld = Thshold[0];
							High_thsld = Thshold[1];

							if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
							{
								ListPeakMajor[j][cnt1]=EnTmp;
							}
						}
					}

					cnt1=cnt1+1;
				}
			}



			//%find Reference
			int NoIsotope=cnt1;
			double[] ActTmp = new double[NoIsotope];

			//Step 1: Used major only to estimate Act
			if(NoIsotope>2) //if number nuclide >2: Processing all da
			{
				ActTmp=ActCal_Used_MajorMinorPeak(Eff_Coef,Peaken,BR1,Y,W,time,ListPeakMajor);
			}
			else
			{
				ActTmp=ActCal_Used_MajorPeak(Eff_Coef,Peaken,BR1,Y,W,time,ListPeakMajor);

				boolean FLg2=false;
				for(int i=0;i<NoIsotope;i++)
				{
					if(ActTmp[i]<=0)
					{
						FLg2=true;
						ActTmp[i]=0;
					}
				}

				if(FLg2==true)
				{
					//ActTmp=ActCal_Used_MajorMinorPeak(Eff_Coef,Peaken,BR1,Y,W,time,ListPeakMajor);
				}
			}



			int cnt3=0;
			for(int i=0;i<NoSrc;i++)
			{
				if(ActInd_Src[i]==0)
				{
					Act[i]=ActTmp[cnt3];
					cnt3=cnt3+1;
				}
			}

		}


		return Act;
	}
	public static double[] ActCal_Used_MajorMinorPeak(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y,
													  double[] W,double time, double [][]ListPeakMajor)
	{

		int M = BR.length; // N: Number of peak energy
		int N = BR[0].length;// M: number of isotope

		int NoSrc=N;


		//%find Reference
		int NoIsotope=NoSrc;
		double[] Flg1 = new double[M];
		int cnt2=0;
		for(int i=0;i<M;i++)
		{
			cnt2=0;
			for(int j=0;j<NoIsotope;j++)
			{
				if(BR[i][j]>0)
				{
					cnt2=cnt2+1;
				}
			}

			Flg1[i]=cnt2;
		}

		//Step
		int FLg_Inte=0;
		int NoInfer=0;

		for(int i=0;i<M;i++)
		{
			if(Flg1[i]>1)
			{
				NoInfer=NoInfer+1;
				FLg_Inte=1;
			}
		}

		double[] ActTmp = new double[NoIsotope];

		// NoInfer=0;
		if(NoInfer==0) //%% No interfence
		{
			ActTmp=ActCorrect(Eff_Coef, Peaken, BR, Y, W, time);
		}
		else
		{
			if(NoIsotope>=NoInfer)
			{
				ActTmp=ActCorrect(Eff_Coef, Peaken, BR, Y, W, time);
			}
			else
			{

				double []PeakEnMea1= new double[NoInfer];
				double [][] BR2=new double[NoInfer][NoIsotope];
				double [] PPNet1=new double[NoInfer];
				double [] w1=new double[NoInfer];

				int cnt11=0;

				for(int i=0;i<M;i++)
				{
					if(Flg1[i]>1)
					{
						PeakEnMea1[cnt11]=Peaken[i];
						PPNet1[cnt11]=Y[i];
						w1[cnt11]=W[i];

						for(int j=0;j<NoIsotope;j++)
						{
							BR2[cnt11][j]=BR[i][j];
						}
						cnt11=cnt11+1;
					}
				}

				ActTmp=ActCorrect(Eff_Coef, PeakEnMea1, BR2, PPNet1, w1, time);


				//Add Peak Information
				//Find Major Peak which are not interference
				double []UnInter_Act=new double [NoIsotope];

				for(int numiso=0;numiso<NoIsotope;numiso++)
				{
					//Find Number Major which are not interference
					for(int j=0;j<M;j++)
					{
						for (int k=0;k<NoInfer;k++)
						{
							if(ListPeakMajor[j][numiso]==PeakEnMea1[k])
							{
								ListPeakMajor[j][numiso]=0;
							}
						}

					}

					// Count

					int UnInterfereMajorPeak=0;
					for(int j=0;j<M;j++)
					{
						if(ListPeakMajor[j][numiso]>0)
						{
							UnInterfereMajorPeak=UnInterfereMajorPeak+1;
						}
					}

					//
					if(UnInterfereMajorPeak>0)
					{
						// Recalculate activity based on UnInterfence
						double []PeakMajorTmp= new double[UnInterfereMajorPeak];
						double [][] BRMajorTmp=new double[UnInterfereMajorPeak][1];
						double [] PPNetMajorTmp=new double[UnInterfereMajorPeak];
						double [] wMajorTmp=new double[UnInterfereMajorPeak];

						int CntMajorTmp=0;

						for(int j=0;j<M;j++)
						{
							if(ListPeakMajor[j][numiso]>0)
							{
								if(ListPeakMajor[j][numiso]==Peaken[j])
								{
									PeakMajorTmp[CntMajorTmp]=Peaken[j];
									PPNetMajorTmp[CntMajorTmp]=Y[j];
									wMajorTmp[CntMajorTmp]=W[j];

									BRMajorTmp[CntMajorTmp][0]=BR[j][numiso];

									CntMajorTmp=CntMajorTmp+1;
								}
							}
						}

						//Calculate Activity
						double [] ActTmp11=new double [1];
						ActTmp11=ActCorrect(Eff_Coef, PeakMajorTmp, BRMajorTmp, PPNetMajorTmp, wMajorTmp, time);
						UnInter_Act[numiso]=ActTmp11[0];

					}
				}

				//average activitity
				for(int i=0;i<NoIsotope;i++)
				{
					if(ActTmp[i]<=0)
					{
						ActTmp[i]=0;
					}

					if(UnInter_Act[i]>0)
					{
						if(ActTmp[i]==0)
						{
							ActTmp[i]=UnInter_Act[i];
						}
						else
						{
							ActTmp[i]=(ActTmp[i]+UnInter_Act[i])/2.0;
						}
					}
				}

			}
		}
		return ActTmp;
	}

	public static double[] ActCorrect_Processing_IdenticalIso(double[] Eff_Coef, double[] Peaken, double[][] BR, double[] Y, double[] W,double time)
	{
		//Modify: 18.15.10
		int N = BR.length; // N: Number of peak energy
		int M = BR[0].length;// M: number of isotope

		double[] Act = new double[M];

		// Processing: Identical isotope

		int[][] IDMatrix = new int[M][M];	//IDentical matrix

		int [] FLgMatrix = new int[M];	//Flg Matrix
		for(int i=0;i<M;i++) FLgMatrix[i]=0;

		double[][] BRTemp = new double[2][N];

		//Step 1: Finding couple of identical isotope

		int NoCoupleIso=0;

		for(int i=0;i<M;i++) //rows
		{
			int count=1;

			for(int j=i+1;j<M;j++)
			{
				if(i!=j&&FLgMatrix[j]==0)
				{
					for (int k=0;k<N;k++)
					{
						BRTemp[0][k]=BR[k][i];
						BRTemp[1][k]=BR[k][j];
					}

					boolean Flg_Identical=BRCompare(BRTemp);

					if(Flg_Identical) // Identical
					{
						IDMatrix[NoCoupleIso][0]=i+1;
						IDMatrix[NoCoupleIso][count]=j+1;
						FLgMatrix[j]=1;
						FLgMatrix[i]=1;
						count=count+1;
					}
				}
			}
			if(count>1)
				NoCoupleIso=NoCoupleIso+1;
		}


		//Step 3: Calculate single source
		int SingleSrc=0;
		for(int i=0;i<M;i++)
		{
			if(FLgMatrix[i]==0) SingleSrc=SingleSrc+1;
		}

		//Step 4: Calculate activitivty
		int M1=SingleSrc+NoCoupleIso;
		int N1=N;

		if(N1>=M1)
		{
			if(NoCoupleIso==0)
			{
				Act = ActCorrect_Old(Eff_Coef, Peaken, BR, Y, W, time);
			}
			else //identical isotope
			{

				//Processing with identical isotope
				double [][] BR2=new double [N1][M1];
				int count=0;
				int sum1=0;
				for(int j=0;j<N1;j++)
				{
					for(int i=0;i<NoCoupleIso;i++)
					{
						int index=IDMatrix[i][0]-1;
						BR2[j][i]=BR[j][index];
					}
				}

				//Get Branching from single source
				int [] IsoSingSrc=new int [M];
				count=NoCoupleIso;

				for(int i=0;i<M;i++)
				{
					if(FLgMatrix[i]==0)
					{
						for(int k=0;k<N;k++)
						{
							BR2[k][count]=BR[k][i];
						}

						IsoSingSrc[i]=count;
						count=count+1;
					}
				}


				//Solving Equation
				double[] ActIDen = new double[M1];
				double [] WFF=new double [N1];
				for(int i=0;i<N1;i++) WFF[i]=1;

				ActIDen = ActCorrect_Old(Eff_Coef, Peaken, BR2, Y, WFF, time);

				//devide activity
				count=0;
				for(int i=0;i<NoCoupleIso;i++)
				{
					int sum2=0;
					for (int j=0;j<M;j++)
					{
						if(IDMatrix[i][j]>0)
							sum2=sum2+1;
					}

					if(sum2>0)
					{
						for (int j=0;j<M;j++)
						{
							if(IDMatrix[i][j]>0)
							{
								int index=IDMatrix[i][j]-1;
								//Act[index]=ActIDen[i]/(double)(sum2);
								Act[index]=ActIDen[i]; //Though: if isotopes are identical, it mean only 1 source
							}
						}
					}

				}

				//add activity for single source
				for(int i=0;i<M;i++)
				{
					if(FLgMatrix[i]==0)
					{
						int index=IsoSingSrc[i];
						Act[i]=ActIDen[index];
					}
				}

			}

		}
		else
		{
			double[] ActTmp = new double[N1];

			double time1 = time;
			double en, eff, tmp;

			double SumAct = 0;
			for (int i = 0; i < N1; i++)
			{
				en = Math.log(Peaken[i]);

				tmp = Eff_Coef[0] * Math.pow(en, 4.0) + Eff_Coef[1] * Math.pow(en, 3.0) + Eff_Coef[2] * Math.pow(en, 2.0)
						+ Eff_Coef[3] * Math.pow(en, 1.0) + Eff_Coef[4] * Math.pow(en, 0);

				eff = Math.exp(tmp);
				int count=0;
				for (int k = 0; k < M; k++)
				{
					if (BR[i][k]>0)
					{
						ActTmp[i] = ActTmp[i] + Y[i] / (eff*time1 * BR[i][k]);
						count=count+1;
					}
				}

				if(count==0) count=1;

				ActTmp[i] = ActTmp[i] / (double)37000/(double)count;

				SumAct = SumAct + ActTmp[i];
			}

			for (int i = 0; i < M; i++)
			{
				//Act[i] = SumAct / (double)N1;
				Act[i] = SumAct;
			}
		}

		return Act;

	}
	public static boolean BRCompare(double[][] BR)
	{

		double Error=0.01; //If branching ratio is <1%, they are same HEU and U-235
		int N=BR.length; // Number of peak energy
		int M=BR[0].length; //Number of isotope

		double []Diff=new double[M];


		if(N==2)
		{
			for (int k=0;k<M;k++)
			{
				double MS=(BR[0][k]+BR[1][k])/(double) (2.0);
				double TS=BR[0][k]-BR[1][k];
				if(MS>0)
				{
					Diff[k]=Math.abs(TS/MS);
				}
			}
		}

		boolean Flg=true;
		for (int k=0;k<M;k++)
		{
			if(Diff[k]>Error) Flg=false;
		}

		boolean FLg_Identical=false;

		if(Flg==true) FLg_Identical=true;

		return FLg_Identical;
	}
}
