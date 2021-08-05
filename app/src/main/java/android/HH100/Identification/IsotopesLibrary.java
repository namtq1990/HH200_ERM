package android.HH100.Identification;

import java.io.File;
import java.util.Vector;

import Debug.Debug;
import NcLibrary.Coefficients;
import NcLibrary.NewNcAnalsys;
import NcLibrary.SpcAnalysis;
import android.HH100.DB.EventDBOper;
import android.HH100.MainActivity;
import android.HH100.Structure.IsotopeDBData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class IsotopesLibrary {
	private SQLiteDatabase _db;
	private Isotope[][] mIsotopes;
	private Vector<Isotope> mSel_Library = new Vector<Isotope>();
	private Vector<String> mIsoLib_List;
	private boolean mLoadCheck = false;
	public static String DB_VERSION = "2.9";
	public static int mLogCount = 0;
	Vector<IsotopeDBData> IsotopeData;
	// double[] mFWHM;

	Debug mDebug = new Debug();
	int mDebugCnt = 0;

	public IsotopesLibrary(Context context) {
		// super(context, "SS.db", null, 1);
		File file = new File(getFilePath("SwLibrary.sql"));

		if (file.isFile() == true) {
			_db = SQLiteDatabase.openDatabase(getFilePath("SwLibrary.sql"), null, SQLiteDatabase.OPEN_READWRITE);
			String aas = _db.getPath();
			Read_data_From_DBfile();
			mLoadCheck = true;
			_db.close();
		} else {

			mLoadCheck = false;
		}
	}

	public SQLiteDatabase Get_DB() {
		return _db;
	}

	public void Set_LibraryName(String name) {

		for (int i = 0; i < mIsoLib_List.size(); i++) {
			String temp = mIsoLib_List.get(i).toString();
			if (name.matches(temp)) {
				for (int k = 0; k < mIsotopes[i].length; k++) {
					mSel_Library.add(mIsotopes[i][k]);
				}
				return;
			}
		}
	}

	public Vector<Isotope> Get_Isotopes() {

		return mSel_Library;
	}

	/*
	 * private Vector<Isotope> find_Ba133_81keV(Vector<Isotope> Target, int[]
	 * Energy, int[] channel, int[] VallyCh,double[] VallyAB,int Energy_Count){
	 * boolean WasFound = false; if(Target.isEmpty()) return Target;
	 *
	 * Vector<Isotope> result2 = new Vector<Isotope>();
	 *
	 * for(int i =0 ; i<Target.size(); i++){ result2.add(Target.get(i));
	 * if(Target.get(i).isotopes.matches("Ba-133")){ WasFound = true; } }
	 * if(WasFound)return result2; //result2.remove(result2.size());
	 *
	 * double L_ROI_Percent = 0; double R_ROI_Percent = 0; double measu_sum = 0;
	 * boolean check = false; Isotope TempResult = new Isotope(); Isotope Ba133 =
	 * Get_Isotope("Ba-133"); if(Ba133.Energy1 == 161) Ba133.Energy1 = 81;
	 * if(Ba133.Energy2 == 161) Ba133.Energy2 = 81; if(Ba133.Energy3 == 161)
	 * Ba133.Energy3 = 81; TempResult = Ba133;
	 *
	 * for(int k=0; k<Energy_Count;k++){ if(Ba133.Energy1== 0) break; double
	 * Roi_window = NcLibrary.Get_Roi_window_by_energy(Ba133.Energy1); L_ROI_Percent
	 * = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
	 *
	 * //L_ROI_Percent = L_ROI_Percent*1.01; //R_ROI_Percent = R_ROI_Percent*0.99;
	 * if(Ba133.Energy1*L_ROI_Percent < Energy[k] && Ba133.Energy1*R_ROI_Percent >
	 * Energy[k]) { check=true; double
	 * measu=Math.abs((((Energy[k]-Ba133.Energy1)/Ba133.Energy1)*100)); measu = 100
	 * - measu; measu_sum = measu;
	 *
	 * TempResult.isotopes = Ba133.isotopes; TempResult.Energy1 = Ba133.Energy1;
	 * TempResult.Channel1 = channel[k]; TempResult.Channel1_Vally.x =
	 * VallyCh[(k*2)]; TempResult.Channel1_Vally.y = VallyCh[(k*2)+1];
	 * TempResult.Channel1_AB.x = (float) VallyAB[(k*2)]; TempResult.Channel1_AB.y =
	 * (float) VallyAB[(k*2)+1];
	 *
	 * break; } else { check=false; }
	 *
	 * }
	 *
	 * for(int k=0; k<Energy_Count;k++){ if(Ba133.Energy2 == 0) break; double
	 * Roi_window = NcLibrary.Get_Roi_window_by_energy(Ba133.Energy2); L_ROI_Percent
	 * = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
	 *
	 * if(Ba133.Energy2*L_ROI_Percent < Energy[k] &&Ba133.Energy2*R_ROI_Percent >
	 * Energy[k]) { check=true;
	 *
	 * double measu=Math.abs((((Energy[k]-Ba133.Energy2)/Ba133.Energy2)*100)); measu
	 * = 100 - measu; measu_sum += measu; measu_sum = (measu_sum/2);
	 *
	 * TempResult.isotopes = Ba133.isotopes; TempResult.Energy2 = Ba133.Energy2;
	 * TempResult.Channel2 = channel[k]; TempResult.Channel2_Vally.x =
	 * VallyCh[(k*2)]; TempResult.Channel2_Vally.y = VallyCh[(k*2)+1];
	 * TempResult.Channel2_AB.x = (float) VallyAB[(k*2)]; TempResult.Channel2_AB.y =
	 * (float) VallyAB[(k*2)+1]; break;} else { check=false;
	 *
	 * } }
	 *
	 * for(int k=0; k<Energy_Count;k++){ if(Ba133.Energy3 == 0) break; double
	 * Roi_window = NcLibrary.Get_Roi_window_by_energy(Ba133.Energy3); L_ROI_Percent
	 * = 1.0-(Roi_window*0.01); R_ROI_Percent = 1.0+(Roi_window*0.01);
	 *
	 * if(Ba133.Energy3*L_ROI_Percent < Energy[k] && Ba133.Energy3*R_ROI_Percent >
	 * Energy[k]){ check=true; double
	 * measu=Math.abs((((Energy[k]-Ba133.Energy3)/Ba133.Energy3)*100)); measu = 100
	 * - measu; measu_sum += measu; measu_sum = (measu_sum/2);
	 *
	 * TempResult.isotopes = Ba133.isotopes; TempResult.Energy3 = Ba133.Energy3;
	 * TempResult.Channel3 = channel[k]; TempResult.Channel3_Vally.x =
	 * VallyCh[(k*2)]; TempResult.Channel3_Vally.y = VallyCh[(k*2)+1];
	 * TempResult.Channel3_AB.x = (float) VallyAB[(k*2)]; TempResult.Channel3_AB.y =
	 * (float) VallyAB[(k*2)+1]; break;} else { check=false;
	 *
	 * } }
	 *
	 *
	 * if(check == true){ int re = (int)measu_sum;
	 *
	 * TempResult.Confidence_Level = re; TempResult.measure_eff =measu_sum;
	 * TempResult.Class = Ba133.Class; result2.add(TempResult); check = false; }
	 *
	 * return result2; }
	 */
	private Vector<Isotope> find_Ba133_81keV(Vector<Isotope> Target, Vector<NcPeak> Peak_data) {
		boolean WasFound = false;
		// if(Target.isEmpty()) return Target;

		Vector<Isotope> result2 = new Vector<Isotope>();

		for (int i = 0; i < Target.size(); i++) {
			result2.add(Target.get(i));
			if (Target.get(i).isotopes.matches("Ba-133")) {
				WasFound = true;
			}
		}
		if (WasFound)
			return result2;
		// result2.remove(result2.size());

		double L_ROI_Percent = 0;
		double R_ROI_Percent = 0;
		double measu_sum = 0;
		boolean check = false;
		Isotope TempResult = new Isotope();
		Isotope Ba133 = Get_Isotope("Ba-133");
		for (int i = 0; i < Ba133.Peaks.size(); i++)
			if (Ba133.Peaks.get(i).Peak_Energy == 161)
				Ba133.Peaks.get(i).Peak_Energy = 81;
		for (int i = 0; i < Ba133.Unknown_Peak.size(); i++)
			if (Ba133.Unknown_Peak.get(i).Peak_Energy == 81)
				Ba133.Unknown_Peak.get(i).Peak_Energy = 161;

		TempResult = Ba133;

		for (int EnCnt = 0; EnCnt < TempResult.Peaks.size(); EnCnt++) {

			NcPeak TempPeak = TempResult.Peaks.get(EnCnt);
			for (int k = 0; k < Peak_data.size(); k++) {

				boolean isIn = TempPeak.Energy_InWindow(Peak_data.get(k).Peak_Energy);
				if (isIn) {

					check = true;
					double measu = Math.abs(
							(((Peak_data.get(k).Peak_Energy - TempPeak.Peak_Energy) / TempPeak.Peak_Energy) * 100));
					measu = 100 - measu;
					measu_sum += measu;

					TempResult.FoundPeaks.add(Peak_data.get(k));
					break;
				} else {
					check = false;
				}
			}
			if (check == false)
				break;
		}
		if (check) {
			TempResult.Confidence_Level = measu_sum / TempResult.Get_OnlyIdEnergy_Cnt();
			result2.add(TempResult);

		}
		measu_sum = 0;
		check = false;

		return result2;
	}

	@SuppressWarnings("null")
	private void Read_data_From_DBfile() {

		Vector<String> IsoLib_list = Read_Isolib_List();
		mIsoLib_List = IsoLib_list;
		/////
		Cursor cu = _db.rawQuery("SELECT * FROM IsoLibName", null);
		cu.moveToFirst();
		if (cu.getCount() == 0)
			return;
		else {
			mIsotopes = new Isotope[cu.getCount()][0];
		}
		/////

		for (int i = 0; i < IsoLib_list.size(); i++) {

			String sql = "SELECT * FROM " + IsoLib_list.get(i);

			cu = _db.rawQuery(sql, null);

			cu.moveToFirst();
			if (cu.getCount() == 0)
				return;
			mIsotopes[i] = new Isotope[cu.getCount()];

			int count = 0;
			while (true) {
				Isotope temp = new Isotope();
				temp.isotopes = cu.getString(0);

				String Energy = "";
				if (!cu.isNull(1)) {

					Energy = cu.getString(1);
					Vector<String> EnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int q = 0; q < EnBr.size(); q += 2) {
						NcPeak tempPeak = new NcPeak();
						tempPeak.Peak_Energy = Integer.valueOf(EnBr.get(q));
						tempPeak.Isotope_Gamma_En_BR = Double.valueOf(EnBr.get(q + 1));
						temp.Peaks.add(tempPeak);
					}
				}
				if (!cu.isNull(2)) {
					Energy = cu.getString(2);
					Vector<String> EnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int q = 0; q < EnBr.size(); q += 2) {
						NcPeak tempPeak = new NcPeak();
						tempPeak.Peak_Energy = Double.valueOf(EnBr.get(q));

						temp.Unknown_Peak.add(tempPeak);
					}

					// String MinorEnergy = "";
					// MinorEnergy = IsotopeData.get(count).MinorEnergy;
					// if (IsotopeData.get(count).MinorEnergy != null) {
					Vector<String> MinorEnBr = NcLibrary.Separate_EveryDash2(Energy);

					for (int j = 0; j < MinorEnBr.size(); j += 2) {
						temp.IsoMinorPeakEn.add(Double.valueOf(MinorEnBr.get(j)));
						temp.IsoMinorPeakBR.add(Double.valueOf(MinorEnBr.get(j + 1)));

					}

					// }

				}

				if (!cu.isNull(3))
					temp.Class = cu.getString(3);
				if (!cu.isNull(4))
					temp.Comment = cu.getString(4);
				if (!cu.isNull(5))
					temp.HelpVideo = cu.getString(5);

				mIsotopes[i][count] = temp;
				count += 1;
				if (cu.isLast() == true)
					break;
				cu.moveToNext();
			}

			cu.close();
		}
	}

	public Vector<String> Read_Isolib_List() {
		Vector<String> result = new Vector<String>();

		Cursor cu;
		cu = _db.rawQuery("SELECT * FROM IsoLibName", null);
		cu.moveToFirst();
		if (cu.getCount() == 0)
			return result;
		////////// -----------------

		while (true) {
			String name = cu.getString(0);
			result.add(name);

			if (cu.isLast() == true)
				break;
			cu.moveToNext();
		}
		cu.close();

		return result;
	}

	//////////////////////////////////////////////////////////
	public String getFilePath(String FileName) {
		File sdcard = Environment.getExternalStorageDirectory();

		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_LIB_FOLDER);
		if (!dbpath.exists()) {
			dbpath.mkdirs();
		}

		String dbfile = dbpath.getAbsolutePath() + File.separator + FileName;
		return dbfile;

	}

	public Vector<String> get_IsotopeLibrary_List() {
		return mIsoLib_List;
	}

	public Vector<Isotope> Find_Isotopes_with_Energy_old(Spectrum SPC, Spectrum BG)
	{
		double Thrshld_Index1=0.95;
		double Thrshld_Index2=0.9;
		double Thrshld_UnClaimed_Index1=0.95;

		double Thrshld_UnClaimed_Index2=0.5;	//should change Index2 to 0.5, before we set 0.2

		//180727
		double Thrshld_Index2_MinorMajor_Ra226=0.9; // Calculate confidence index for all minor and major
		double ThrShld_Act_Except_Ra226=0.1;
		double ThrShld_Act_Except_OtherIso=0.1;
		double Act_Thsold=0.05; //5% // Actvity theshold
		double  WndROI_CEPeak= 1.0;


		Coefficients EnCoeff_Cali = SPC.Get_Coefficients();// Energy Calibration
		// double[] FWHM_gen = new double[] { 1.2707811254, -1.5464537062 };
		double WndROI = BG.getWnd_Roi();

		double[] FWHM_gen = BG.getFWHM();

		double[] Eff_Coeff = BG.getFindPeakN_Coefficients();

		FindPeaksN FPM = new FindPeaksN();
		// Step 1: Processing: Find PeakInfor. BG Subtracted is minus in this step

		Vector<NcPeak> mFoundPeak_data = FPM.Find_Peak(SPC, BG);

		Vector<Isotope> result2 = new Vector<Isotope>();

		if (mLoadCheck == false)
			return result2;

		// Step 2: ID Isotope by templete matching

		// result2 = PeakMatchIsotope(mFoundPeak_data);

		result2 = PeakMatchIsotope_H(mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

		// Step 3: Applied Confidence filter
		// result2 = IndexFillter(result2);
		result2 = IndexFillter_H(result2, FWHM_gen, EnCoeff_Cali, Eff_Coeff, WndROI,Thrshld_Index1,Thrshld_Index2);

		int CHSIZE = 1024;
		double[] ChSpec = new double[CHSIZE];
		for (int i = 0; i < SPC.Get_Ch_Size(); i++)
		{
			ChSpec[i] = SPC.at(i);
		}



		//18.05.16: adding logic table C.E peak for Cs137 and Co60
		result2=NewNcAnalsys.LogicComptonPeakCs_Co60(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI_CEPeak);
		result2=NewNcAnalsys.AddCondition_Cs_U233HE_U235HE(result2) ;
		result2=NewNcAnalsys.LogicHighEnricUranium(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

		//condition for WGPu and RGPu
		result2=NewNcAnalsys.AddCondition_WGPu_RGPU(result2, mFoundPeak_data,FWHM_gen,	EnCoeff_Cali,WndROI);

		//1st Screening Processing
		// Step 4: Activity Calculation
		if (result2.size() > 0) {
			// result2 = NewNcAnalsys.CValue_Filter(NewNcAnalsys.Smooth_Spc(ChSpec),
			// result2, mFoundPeak_data, FWHM_gen,Eff_Coeff, SPC.Get_AcqTime());
			result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec), result2, mFoundPeak_data, FWHM_gen,EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI,Act_Thsold);

			//Adding information
			for (int i = 0; i < result2.size(); i++)
			{
				result2.get(i).Screening_Process=1;
			}

		}

		// Step 5: Last condition for validation isotope


		// 2nd Sreening Processing
		if (result2.size() >= 0)
		{

			Vector<NcPeak> UnClaimedPeak = NewNcAnalsys.CValue_Return_UnclaimedEn(NewNcAnalsys.Smooth_Spc(ChSpec),
					result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI);

			// condition
			int NoFoundPeak = mFoundPeak_data.size();

			int NoUnclaimedPeak = UnClaimedPeak.size();

			double RatioPeak=(double)NoUnclaimedPeak/(double)NoFoundPeak;

			if(RatioPeak>0.5) //Peak: 50% is unclaime peak
			{
				Act_Thsold=0.01; //1%
			}


			if (RatioPeak > 0.25) // at least more than 25% unclaimed peak will
			// be processed
			{
				Vector<Isotope> Unclaimed_Result = new Vector<Isotope>();

				//Template Matching
				//Unclaimed_Result = PeakMatchIsotope_H(UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI);
				Unclaimed_Result = PeakMatchIsotope_HH(UnClaimedPeak,FWHM_gen,EnCoeff_Cali, WndROI, result2);


				//Remove Peaks which was still remembered in memory in Java
				Unclaimed_Result=IsoRemoveLines(Unclaimed_Result,UnClaimedPeak);


				//resett actiivity
				for(int i=0;i<Unclaimed_Result.size();i++)
				{
					Unclaimed_Result.get(i).Act=0;
				}

				//Re calcute confidence index
				Unclaimed_Result = IndexFillter_H(Unclaimed_Result, FWHM_gen, EnCoeff_Cali, Eff_Coeff, WndROI,Thrshld_UnClaimed_Index1,Thrshld_UnClaimed_Index2);

				//18.05.16: adding logic table C.E peak for Cs137 and Co60
				Unclaimed_Result=NewNcAnalsys.LogicComptonPeakCs_Co60(Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI_CEPeak);

				result2=NewNcAnalsys.AddCondition_Cs_U233HE_U235HE(result2) ;

				//Condition for High enriched uranium
				Unclaimed_Result=NewNcAnalsys.LogicHighEnricUranium(Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI);

				//condition for WGPu and RGPu
				Unclaimed_Result=NewNcAnalsys.AddCondition_WGPu_RGPU(Unclaimed_Result, UnClaimedPeak,FWHM_gen,	EnCoeff_Cali,WndROI);

				// Step 1: Find best isotope with max number of line
				if (Unclaimed_Result.size() > 0)
				{
					Unclaimed_Result = NewNcAnalsys.IsotopeID_UnClaimedLine(NewNcAnalsys.Smooth_Spc(ChSpec),Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(),	WndROI);

					//Adding information
					for (int i = 0; i < Unclaimed_Result.size(); i++)
					{
						Unclaimed_Result.get(i).Screening_Process=2;
					}
				}

				// Step2 : Final calculation

				if (Unclaimed_Result.size() > 0)
				{

					result2 = NewNcAnalsys.AddingIsotope(result2, Unclaimed_Result);

					if (result2.size() > 0)
					{
						//18.05.16: adding logic table C.E peak for Cs137 and Co60
						result2=NewNcAnalsys.LogicComptonPeakCs_Co60(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI_CEPeak);
						result2=NewNcAnalsys.AddCondition_Cs_U233HE_U235HE(result2) ;
						result2=NewNcAnalsys.LogicHighEnricUranium(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

						result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec), result2,mFoundPeak_data, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI,Act_Thsold);
					}
				}

			}

		}

		//Adding condition to prohibit WGPU by logic when RGPu is IDed
		// Adding condition to prohibit Ra and Ba
		if (result2.size() > 0)
		{
			//result2=NewNcAnalsys.AddCondition_Ra_Ba(result2);
			//result2=NewNcAnalsys.AddCondition_Ra_Ba(result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,WndROI,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Ba-133",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","U-235",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","HEU",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Ga-67",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","In-111",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//Co57 vs Eu152: Activty Ratio of Co57/Eu=11~15%, so take theshold 0.2
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Eu-152","Co-57",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,0.2);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Eu-152","U-235",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","Tl-201",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","I-131",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//18.12.21
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","I-131",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);


		}

		//sTEP 4: Adding Minor Peak
		//Adding Minor Peak for Showing
		if (result2.size() > 0)
		{
			result2 = AddPeakDraw(result2,mFoundPeak_data,FWHM_gen,EnCoeff_Cali,WndROI);
		}


		return result2;
	}// end Find_Isotopes_with_Energy

	public Vector<Isotope> Find_Isotopes_with_Energy(Spectrum SPC, Spectrum BG)
	{
		double Thrshld_Index1=0.95;
		double Thrshld_Index2=0.9;
		double Thrshld_UnClaimed_Index1=0.95;

		double Thrshld_UnClaimed_Index2=0.5;	//should change Index2 to 0.5, before we set 0.2

		//180727
		double Thrshld_Index2_MinorMajor_Ra226=0.9; // Calculate confidence index for all minor and major
		double ThrShld_Act_Except_Ra226=0.2; //increasing 0.1 to 0.2 (20%) for activity
		double ThrShld_Act_Except_OtherIso=0.2; //increasing 0.1 to 0.2 (20%) for activity
		double Act_Thsold=0.05; //5% // Actvity theshold
		double  WndROI_CEPeak= 1.0;
		double ThrShld_Act_Except_Ra226_Ba133=0.3;

		Coefficients EnCoeff_Cali = SPC.Get_Coefficients();// Energy Calibration
		// double[] FWHM_gen = new double[] { 1.2707811254, -1.5464537062 };
		double WndROI = BG.getWnd_Roi();

		double[] FWHM_gen = BG.getFWHM();

		double[] Eff_Coeff = BG.getFindPeakN_Coefficients();

		FindPeaksN FPM = new FindPeaksN();

		// Step 1: Processing: Find PeakInfor. BG Subtracted is minus in this step

		Vector<NcPeak> mFoundPeak_data = FPM.Find_Peak(SPC, BG);

		Vector<Isotope> result2 = new Vector<Isotope>();

		if (mLoadCheck == false)
			return result2;

		// Step 2: ID Isotope by templete matching

		// result2 = PeakMatchIsotope(mFoundPeak_data);

		result2 = PeakMatchIsotope_H(mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

		// Step 3: Applied Confidence filter
		// result2 = IndexFillter(result2);
		result2 = IndexFillter_H(result2, FWHM_gen, EnCoeff_Cali, Eff_Coeff, WndROI,Thrshld_Index1,Thrshld_Index2);

		int CHSIZE = 1024;
		double[] ChSpec = new double[CHSIZE];
		for (int i = 0; i < SPC.Get_Ch_Size(); i++)
		{
			ChSpec[i] = SPC.at(i);
		}



		//18.05.16: adding logic table C.E peak for Cs137 and Co60
		result2=NewNcAnalsys.LogicComptonPeakCs_Co60(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI_CEPeak);
		result2=NewNcAnalsys.AddCondition_Cs_U233HE_U235HE(result2) ;
		result2=NewNcAnalsys.LogicHighEnricUranium(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

		//condition for WGPu and RGPu
		result2=NewNcAnalsys.AddCondition_WGPu_RGPU(result2, mFoundPeak_data,FWHM_gen,	EnCoeff_Cali,WndROI);

		//adding logic for Lu177 and Sm153
		result2 = NewNcAnalsys.Logic_Lu177_Sm153(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);
		result2 = NewNcAnalsys.Logic_Cs137_Co67(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

		//1st Screening Processing
		// Step 4: Activity Calculation
		if (result2.size() > 0) {
			// result2 = NewNcAnalsys.CValue_Filter(NewNcAnalsys.Smooth_Spc(ChSpec),
			// result2, mFoundPeak_data, FWHM_gen,Eff_Coeff, SPC.Get_AcqTime());
			result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec), result2, mFoundPeak_data, FWHM_gen,EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI,Act_Thsold);

			//Adding information
			for (int i = 0; i < result2.size(); i++)
			{
				result2.get(i).Screening_Process=1;
			}

		}

		// Step 5: Last condition for validation isotope


		// 2nd Sreening Processing
		if (result2.size() >= 0)
		{

			Vector<NcPeak> UnClaimedPeak = NewNcAnalsys.CValue_Return_UnclaimedEn(NewNcAnalsys.Smooth_Spc(ChSpec),
					result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI);

			// condition
			int NoFoundPeak = mFoundPeak_data.size();

			int NoUnclaimedPeak = UnClaimedPeak.size();

			double RatioPeak=(double)NoUnclaimedPeak/(double)NoFoundPeak;

			if(RatioPeak>0.5) //Peak: 50% is unclaime peak
			{
				Act_Thsold=0.01; //1%
			}


			if (RatioPeak > 0.25) // at least more than 25% unclaimed peak will
			// be processed
			{
				Vector<Isotope> Unclaimed_Result = new Vector<Isotope>();

				//Template Matching
				//Unclaimed_Result = PeakMatchIsotope_H(UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI);
				Unclaimed_Result = PeakMatchIsotope_HH(UnClaimedPeak,FWHM_gen,EnCoeff_Cali, WndROI, result2);


				//Remove Peaks which was still remembered in memory in Java
				Unclaimed_Result=IsoRemoveLines(Unclaimed_Result,UnClaimedPeak);


				//resett actiivity
				for(int i=0;i<Unclaimed_Result.size();i++)
				{
					Unclaimed_Result.get(i).Act=0;
				}

				//Re calcute confidence index
				Unclaimed_Result = IndexFillter_H(Unclaimed_Result, FWHM_gen, EnCoeff_Cali, Eff_Coeff, WndROI,Thrshld_UnClaimed_Index1,Thrshld_UnClaimed_Index2);

				//18.05.16: adding logic table C.E peak for Cs137 and Co60
				Unclaimed_Result=NewNcAnalsys.LogicComptonPeakCs_Co60(Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI_CEPeak);

				result2=NewNcAnalsys.AddCondition_Cs_U233HE_U235HE(result2) ;

				//Condition for High enriched uranium
				Unclaimed_Result=NewNcAnalsys.LogicHighEnricUranium(Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, WndROI);

				//condition for WGPu and RGPu
				Unclaimed_Result=NewNcAnalsys.AddCondition_WGPu_RGPU(Unclaimed_Result, UnClaimedPeak,FWHM_gen,	EnCoeff_Cali,WndROI);

				// Step 1: Find best isotope with max number of line
				if (Unclaimed_Result.size() > 0)
				{
					Unclaimed_Result = NewNcAnalsys.IsotopeID_UnClaimedLine(NewNcAnalsys.Smooth_Spc(ChSpec),Unclaimed_Result, UnClaimedPeak, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(),	WndROI);

					//Adding information
					for (int i = 0; i < Unclaimed_Result.size(); i++)
					{
						Unclaimed_Result.get(i).Screening_Process=2;
					}
				}

				// Step2 : Final calculation

				if (Unclaimed_Result.size() > 0)
				{

					result2 = NewNcAnalsys.AddingIsotope(result2, Unclaimed_Result);

					if (result2.size() > 0)
					{
						//18.05.16: adding logic table C.E peak for Cs137 and Co60
						result2=NewNcAnalsys.LogicComptonPeakCs_Co60(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI_CEPeak);
						result2=NewNcAnalsys.AddCondition_Cs_U233HE_U235HE(result2) ;
						result2=NewNcAnalsys.LogicHighEnricUranium(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);
						//adding logic for Lu177 and Sm153
						result2 = NewNcAnalsys.Logic_Lu177_Sm153(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);
						result2 = NewNcAnalsys.Logic_Cs137_Co67(result2, mFoundPeak_data, FWHM_gen, EnCoeff_Cali, WndROI);

						result2 = NewNcAnalsys.CValue_Filter_H(NewNcAnalsys.Smooth_Spc(ChSpec), result2,mFoundPeak_data, FWHM_gen, EnCoeff_Cali, Eff_Coeff, SPC.Get_AcqTime(), WndROI,Act_Thsold);


					}
				}

			}

		}

		//Adding condition to prohibit WGPU by logic when RGPu is IDed
		// Adding condition to prohibit Ra and Ba
		if (result2.size() > 0)
		{
			//result2=NewNcAnalsys.AddCondition_Ra_Ba(result2);
			//result2=NewNcAnalsys.AddCondition_Ra_Ba(result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,WndROI,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Ba-133",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","U-235",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","HEU",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Ga-67",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","In-111",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//Co57 vs Eu152: Activty Ratio of Co57/Eu=11~15%, so take theshold 0.2
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Eu-152","Co-57",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,0.2);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Eu-152","U-235",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","Tl-201",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","I-131",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//18.12.21
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","I-131",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//19.08.28
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Cs-137","I-125",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//Adding more condition for Ra+Ba
			//2019.09.02
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Ba-133",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226_Ba133);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","In-111",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);


			//2019.09.06
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","I-123",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Ga-67",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Tl-201",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","I-125",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","I-123",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","Ga-67",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","I-125",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_OtherIso);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Cs-137","I-123",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Cs-137","Ga-67",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","U-233HE",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Th-232","U-233HE",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//2019.10.31
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Cs-137","Ga-67",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Cs-137","In-111",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Cs-137","I-131",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ba-133","Pu-239",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

			//2020.01.31
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","U-235HE",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Th-232","U-235HE",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);

            result2=NewNcAnalsys.AddCondition_Exception_Isopte("Ra-226","Np-237HE",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
            result2=NewNcAnalsys.AddCondition_Exception_Isopte("Th-232","Np-237HE",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);
			result2=NewNcAnalsys.AddCondition_Exception_Isopte("Th-232","Ir-192",result2,mFoundPeak_data, FWHM_gen,EnCoeff_Cali,0.6,Eff_Coeff, SPC.Get_AcqTime(),Thrshld_Index2_MinorMajor_Ra226,ThrShld_Act_Except_Ra226);


        }

		//sTEP 4: Adding Minor Peak
		//Adding Minor Peak for Showing
		if (result2.size() > 0)
		{
			result2 = AddPeakDraw(result2,mFoundPeak_data,FWHM_gen,EnCoeff_Cali,WndROI);
		}


		return result2;
	}// end Find_Isotopes_with_Energy

	public Vector<Isotope> IndexFillter(Vector<Isotope> result2) {
		String UnKnownPeak = "Unknown";

		for (int i = 0; i < result2.size(); i++) {

			result2.get(i).IndexMax = Peak_Index2(result2, i);
			result2.get(i).Confidence_Level = result2.get(i).IndexMax;

		}

		for (int i = 0; i < result2.size(); i++) {
			String Peak = "";
			for (int j = 0; j < result2.get(i).FoundPeaks.size(); j++) {
				Peak += ", " + Integer.toString((int) result2.get(i).FoundPeaks.get(j).Peak_Energy);
			}

			Peak = result2.get(i).isotopes + " : " + Peak + " (" + String.format("%.2f", result2.get(i).IndexMax) + ")";

		}

		for (int s = 0; s < result2.size(); s++) {

			result2.get(s).IndexMax = Peak_Index2(result2, s);
			result2.get(s).Confidence_Level = result2.get(s).IndexMax;

			if (result2.get(s).IndexMax <= 0.8) {
				result2.remove(s);
				s--;
			}

		}
		return result2;
	}

	public Vector<Isotope> IndexFillter_H_old(Vector<Isotope> result2, double[] FWHMCoeff, Coefficients coeff,	double[] mEfficiency, double WndROI,double Confiden_Index1, 	double Confiden_Index2 )
	{

		boolean Flg;

		for (int s = 0; s < result2.size(); s++)
		{

			double TmpIndex2= Peak_Index_H(result2, s, FWHMCoeff, coeff, mEfficiency, WndROI);
			//result2.get(s).IndexMax = Peak_Index_H(result2, s, FWHMCoeff, coeff, mEfficiency, WndROI);
			//result2.get(s).Confidence_Level = result2.get(s).IndexMax * 100;

			//Update information
			if(result2.get(s).IndexMax <=0)
			{
				result2.get(s).IndexMax = TmpIndex2;
			}
			result2.get(s).Confidence_Level = result2.get(s).IndexMax * 100;

			Flg = false;

			if (result2.get(s).Index1 <= Confiden_Index1)
			{
				Flg = true;
			}

			if (result2.get(s).IndexMax <= Confiden_Index2)
			{
				Flg = true;
			}


			// satisfies condition will be removed
			if (Flg == true)
			{
				result2.remove(s);
				s--;
			}

		}
		return result2;

	}

	public Vector<Isotope> IndexFillter_H(Vector<Isotope> result2, double[] FWHMCoeff, Coefficients coeff,	double[] mEfficiency, double WndROI,double Confiden_Index1, 	double Confiden_Index2 )
	{

		boolean Flg;

		for (int s = 0; s < result2.size(); s++)
		{

			double TmpIndex2= Peak_Index_H(result2, s, FWHMCoeff, coeff, mEfficiency, WndROI);
			//result2.get(s).IndexMax = Peak_Index_H(result2, s, FWHMCoeff, coeff, mEfficiency, WndROI);
			//result2.get(s).Confidence_Level = result2.get(s).IndexMax * 100;

			//Update information
			if(result2.get(s).IndexMax <=0)
			{
				result2.get(s).IndexMax = TmpIndex2;
			}

			//update information :20191218
			if(result2.get(s).IndexMax <=TmpIndex2)
			{
				result2.get(s).IndexMax = TmpIndex2;
			}

			//update confiden level
			//if(result2.get(s).Confidence_Level <=TmpIndex2*100)
			//result2.get(s).Confidence_Level=TmpIndex2*100;

			result2.get(s).Confidence_Level = result2.get(s).IndexMax * 100;

			if (result2.get(s).isotopes.equals("Th-232")==true)
			{
				Flg = false;

				if (result2.get(s).Index1 <= 0.92) {
					Flg = true;
				}

				if (result2.get(s).IndexMax <= Confiden_Index2) {
					Flg = true;
				}


				// satisfies condition will be removed
				if (Flg == true) {
					result2.remove(s);
					s--;
				}
			}
			else
			{

				Flg = false;

				if (result2.get(s).Index1 <= Confiden_Index1) {
					Flg = true;
				}

				if (result2.get(s).IndexMax <= Confiden_Index2) {
					Flg = true;
				}


				// satisfies condition will be removed
				if (Flg == true) {
					result2.remove(s);
					s--;
				}
			}

		}
		return result2;

	}
	double Peak_Index_H(Vector<Isotope> result2, int mPos, double[] FWHMCoeff, Coefficients coeff, double[] mEfficiency,
						double WndROI) {

		// Hung Function
		int NoPeakEnTrue = result2.get(mPos).Peaks.size();
		int NoFoundPeakEn = result2.get(mPos).FoundPeaks.size();

		// Step 1: Get information to calculation
		double[] ListIsoEn = new double[NoPeakEnTrue];
		double[] ListIsoBr = new double[NoPeakEnTrue];

		double[] ListFoundEn = new double[NoPeakEnTrue];
		double[] ListFoundBr = new double[NoPeakEnTrue];

		double sumListIsoBr = 0;
		for (int i = 0; i < NoPeakEnTrue; i++) {
			ListIsoEn[i] = result2.get(mPos).Peaks.get(i).Peak_Energy;
			ListIsoBr[i] = result2.get(mPos).Peaks.get(i).Isotope_Gamma_En_BR;

			sumListIsoBr = sumListIsoBr + ListIsoBr[i];

			for (int j = 0; j < NoFoundPeakEn; j++) {
				if (ListIsoEn[i] == result2.get(mPos).IsoPeakEn.get(j)) {
					ListFoundEn[i] = result2.get(mPos).FoundPeaks.get(j).Peak_Energy;
					ListFoundBr[i] = ListIsoBr[i];
				}
			}
		}

		// Step 2: For each observed peak,confidence of each peak
		double Index1 = 1;

		double[] Thshld = new double[2];
		double ETOL, dev_en, tmp, tmp1;

		for (int i = 0; i < NoPeakEnTrue; i++) {
			if (ListFoundEn[i] > 0 && ListIsoBr[i] > 0) {
				Thshld = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(ListIsoEn[i], FWHMCoeff, coeff, WndROI);

				ETOL = Thshld[1] - Thshld[0];

				dev_en = ListFoundEn[i] - ListIsoEn[i];

				tmp = -0.16 / (ETOL * ETOL) * (dev_en * dev_en) * ListFoundBr[i];

				tmp1 = tmp / sumListIsoBr;

				Index1 = Index1 * Math.exp(tmp1);

			}
		}

		result2.get(mPos).Index1 = Index1;

		// Step 3: Caluate Index 2
		double Index2 = 0;
		double SumNotMatchObservePeak = 0;
		double SumPeakLibrary = 0;

		double mCalEfficiency = 0, X = 0, efftmp3;

		for (int i = 0; i < NoPeakEnTrue; i++) {

			X = Math.log(ListIsoEn[i]);

			efftmp3 = mEfficiency[0] * Math.pow(X, 4) + mEfficiency[1] * Math.pow(X, 3)
					+ mEfficiency[2] * Math.pow(X, 2) + mEfficiency[3] * X + mEfficiency[4];
			mCalEfficiency = Math.exp(efftmp3);

			if (ListFoundEn[i] == 0) {
				SumNotMatchObservePeak = SumNotMatchObservePeak + ListIsoBr[i] * Math.sqrt(mCalEfficiency);
			}

			SumPeakLibrary = SumPeakLibrary + ListIsoBr[i] * Math.sqrt(mCalEfficiency);

		}

		if (SumPeakLibrary > 0) {
			Index2 = Index1 - 1.6 * SumNotMatchObservePeak / SumPeakLibrary;
		} else {
			Index2 = 0;
		}

		result2.get(mPos).Index2 = Index2;
		return Index2;

	}

	public Vector<Isotope> PeakMatchIsotope(Vector<NcPeak> mFoundPeak_data) {

		Vector<Isotope> result2 = new Vector<Isotope>();
		Isotope SourceInfo = new Isotope();
		boolean check = false;
		double Peak_Confidence_Value_sum = 0;
		int PeakCnt = 0;

		for (int i = 0; i < mSel_Library.size(); i++) {

			SourceInfo = mSel_Library.get(i);

			for (int EnCnt = 0; EnCnt < SourceInfo.Peaks.size(); EnCnt++) {

				for (int k = 0; k < mFoundPeak_data.size(); k++) {

					boolean isIn = SourceInfo.Peaks.get(EnCnt).Energy_InWindow(mFoundPeak_data.get(k).Peak_Energy);

					if (isIn) {

						Peak_Confidence_Value_sum += Confidence_Level_Cal(mFoundPeak_data.get(k).Peak_Energy,
								SourceInfo.Peaks.get(EnCnt).Peak_Energy);
						SourceInfo.FoundPeaks.add(mFoundPeak_data.get(k));
						SourceInfo.FoundPeakBR.add(SourceInfo.Peaks.get(EnCnt).Isotope_Gamma_En_BR);

						SourceInfo.IsoPeakEn.add(SourceInfo.Peaks.get(EnCnt).Peak_Energy);

						PeakCnt++;
						// check = true;
						break;
					}
				}

			}
			if (PeakCnt != 0) {

				PeakCnt = 0;

				// SourceInfo.Confidence_Level = Peak_Confidence_Value_sum /
				// SourceInfo.Get_OnlyIdEnergy_Cnt();

				result2.add(SourceInfo);

			}
			Peak_Confidence_Value_sum = 0;
			check = false;

		}
		return result2;
	}

	public Vector<Isotope> PeakMatchIsotope_H(Vector<NcPeak> mFoundPeak_data, double[] FWHMCoeff, Coefficients coeff,double WndROI)
	{
		// double [] EnCalCoeff=new double [3];
		// EnCalCoeff[0] = coeff.get_Coefficients()[0];
		// EnCalCoeff[1]= coeff.get_Coefficients()[1];
		// EnCalCoeff[2]= coeff.get_Coefficients()[2];

		Vector<Isotope> result2 = new Vector<Isotope>();
		Isotope SourceInfo = new Isotope();
		boolean check = false;
		double Peak_Confidence_Value_sum = 0;
		int PeakCnt = 0;

		int CountPeak = 0;
		double[] FoundMSEn = new double[10];
		double[] FoundMSNet = new double[10];
		double[] FoundSourceInfo = new double[10];
		int index = 0;
		double max1 = 0;

		for (int i = 0; i < mSel_Library.size(); i++)
		{

			SourceInfo = mSel_Library.get(i);

			//reset memory because in Java still keep information of previous step
			SourceInfo = mSel_Library.get(i);

			for(int ii=0;ii<SourceInfo.FoundPeaks.size();ii++)
			{
				SourceInfo.FoundPeaks.remove(ii);
				ii--;
			}


			for(int ii=0;ii<SourceInfo.FoundPeakBR .size();ii++)
			{
				SourceInfo.FoundPeakBR.remove(ii);
				ii--;
			}


			for(int ii=0;ii<SourceInfo.IsoPeakEn.size();ii++)
			{
				SourceInfo.IsoPeakEn.remove(ii);
				ii--;
			}
			//if(SourceInfo.Index2<0||SourceInfo.Index2=0)
			SourceInfo.Index1=0;
			SourceInfo.Index2=0;

			//End memory reset


			for (int EnCnt = 0; EnCnt < SourceInfo.Peaks.size(); EnCnt++)
			{

				CountPeak = 0;
				for (int k = 0; k < mFoundPeak_data.size(); k++)
				{

					boolean isIn = SourceInfo.Peaks.get(EnCnt).Energy_InWindow_H(mFoundPeak_data.get(k).Peak_Energy,
							FWHMCoeff, coeff, WndROI);

					if (isIn) {
						FoundMSEn[CountPeak] = mFoundPeak_data.get(k).Peak_Energy;
						FoundMSNet[CountPeak] = mFoundPeak_data.get(k).NetCnt;
						CountPeak = CountPeak + 1;
					}
				}

				if (CountPeak > 0) {
					max1 = 0.0;
					for (int j = 0; j < CountPeak; j++)
					{
						if (FoundMSNet[j] > max1)
						{
							max1 = FoundMSNet[j];
							index = j;
						}
					}

					// adding to source infor

					for (int k = 0; k < mFoundPeak_data.size(); k++)
					{

						boolean isIn = SourceInfo.Peaks.get(EnCnt).Energy_InWindow_H(mFoundPeak_data.get(k).Peak_Energy,
								FWHMCoeff, coeff, WndROI);

						if (isIn) {
							if (FoundMSEn[index] == mFoundPeak_data.get(k).Peak_Energy)
							{
								Peak_Confidence_Value_sum += Confidence_Level_Cal(mFoundPeak_data.get(k).Peak_Energy,
										SourceInfo.Peaks.get(EnCnt).Peak_Energy);

								SourceInfo.FoundPeaks.add(mFoundPeak_data.get(k));

								SourceInfo.FoundPeakBR.add(SourceInfo.Peaks.get(EnCnt).Isotope_Gamma_En_BR);

								SourceInfo.IsoPeakEn.add(SourceInfo.Peaks.get(EnCnt).Peak_Energy);

								PeakCnt++;

								break;
							}
						}
					}

				}

			}
			if (PeakCnt != 0) {

				PeakCnt = 0;

				SourceInfo.Confidence_Level = Peak_Confidence_Value_sum / SourceInfo.Get_OnlyIdEnergy_Cnt();

				result2.add(SourceInfo);

			}
			Peak_Confidence_Value_sum = 0;
			check = false;

		}
		return result2;
	}

	public double Confidence_Level_Cal(double Found_Peak_Energy, double Iso_Peak_Energy) {

		double Peak_Confidence_Value = 0;
		Peak_Confidence_Value = 100 - Math.abs((Found_Peak_Energy - Iso_Peak_Energy) / Iso_Peak_Energy * 100);
		return Peak_Confidence_Value;
	}

	double Peak_Index2(Vector<Isotope> result2, int mPos) {

		double mPeak_Index = 0;
		double mMaxBRFoundEn = 0;
		double mMaxBRIsoEn = 0;
		String mPeakName = "";
		double mMaxBR = 0;
		double mMaxCh = 0;
		double mMaxFoundPeak = 0;
		double mBRSum = 0;
		double mIndex1 = 0;
		double mIndex2Mid = 0;
		double mIndex2 = 0;

		// Eff 2x2
		double[] mEfficiency = new double[] { -0.027939138, 0.694026779, -6.627760069, 28.20796375, -48.74100729 };

		// Eff 2x2
		// double[] mEfficiency = new double[] { -0.241522359, 5.784633023,
		// -51.63572634, 203.0083263, -302.0872868 };

		// double[] mEfficiency = new double[] { -0.027939138, 0.694026779,
		// -6.627760069, 28.20796375, -48.74100729 };

		double mCount = 0;
		double BrSqrtEffiSum = 0;
		double BrSqrtEffi = 0;
		double mFoundBrSqrtEffiSum = 0;
		// Step 1. Max BR Peak Search

		for (int i = 0; i < result2.get(mPos).FoundPeaks.size(); i++) {

			if (result2.get(mPos).FoundPeakBR.get(i) >= mMaxBR) {

				mPeakName = result2.get(mPos).isotopes;
				mMaxBRFoundEn = result2.get(mPos).FoundPeaks.get(i).Peak_Energy;
				mMaxBR = result2.get(mPos).FoundPeakBR.get(i);
				mMaxCh = (double) result2.get(mPos).FoundPeaks.get(i).Channel;
				mMaxBRIsoEn = result2.get(mPos).IsoPeakEn.get(i);
				mCount = i;

			}

			mFoundBrSqrtEffiSum += NcLibrary.Get_BrSqrtEffi(mEfficiency,
					result2.get(mPos).FoundPeaks.get(i).Peak_Energy, result2.get(mPos).FoundPeakBR.get(i));

		}

		for (int i = 0; i < result2.get(mPos).Peaks.size(); i++) {

			mBRSum += result2.get(mPos).Peaks.get(i).Isotope_Gamma_En_BR;
			BrSqrtEffiSum += NcLibrary.Get_BrSqrtEffi(mEfficiency, result2.get(mPos).Peaks.get(i).Peak_Energy,
					result2.get(mPos).Peaks.get(i).Isotope_Gamma_En_BR);
		}

		if (mBRSum == 0) {
			mMaxBR = 1;
			mBRSum = 0.00001;
		}

		// double[] mFWHM = new double[2];
		// mFWHM[0] = 1.87167;
		// mFWHM[1] = -6.24957;

		// 2x2 FWHM
		// double[] mFWHM = new double[] { 1.2707811, 1.5464537 };

		double[] mFWHM = new double[] { 1.2707811254, -1.5464537062 };

		// Index1 Calculration

		// mIndex1 = Math.exp(((-0.16 / Math.pow(mMaxCh * Math.sqrt(mFWHM[0]) +
		// mFWHM[1], 2))
		// * Math.pow(mMaxBRIsoEn - mMaxBRFoundEn, 2) * mMaxBR) / mBRSum);

		mIndex1 = Math.exp(((-0.16 / Math.pow(mFWHM[0] * Math.sqrt(mMaxCh) + mFWHM[1], 2))
				* Math.pow(mMaxBRIsoEn - mMaxBRFoundEn, 2) * mMaxBR) / mBRSum);

		// Hung
		// mIndex1 = Math.exp(((-0.16 / Math.pow(Math.sqrt(mFWHM[0] * mMaxCh) +
		// mFWHM[1], 2))
		// * Math.pow(mMaxBRIsoEn - mMaxBRFoundEn, 2) * mMaxBR) / mBRSum);

		result2.get(mPos).Index1 = mIndex1;

		// Index2 Calculration
		BrSqrtEffi = NcLibrary.Get_BrSqrtEffi(mEfficiency, mMaxBRIsoEn, mMaxBR);

		if ((BrSqrtEffiSum - mFoundBrSqrtEffiSum) != 0) {
			mIndex2Mid = 1.6 * ((BrSqrtEffiSum - mFoundBrSqrtEffiSum) / BrSqrtEffiSum);
			mIndex2 = mIndex1 - mIndex2Mid;
		} else {

			mIndex2 = mIndex1;
		}

		return mIndex2;
	}

	double Peak_Index(Vector<Isotope> result2, int mPos) {

		double mPeak_Index = 0;
		double mMaxBRFoundEn = 0;
		double mMaxBRIsoEn = 0;
		String mPeakName = "";
		double mMaxBR = 0;
		double mMaxCh = 0;
		double mMaxFoundPeak = 0;
		double mBRSum = 0;
		double mIndex1 = 0;
		double mIndex2Mid = 0;
		double mIndex2 = 0;

		double[] mEfficiency = new double[] { -0.027939138, 0.694026779, -6.627760069, 28.20796375, -48.74100729 };

		// double[] mEfficiency = new double[] { -0.027939138, 0.694026779,
		// -6.627760069, 28.20796375, -48.74100729 };

		double mCount = 0;
		double BrSqrtEffiSum = 0;
		double BrSqrtEffi = 0;
		double mFoundBrSqrtEffiSum = 0;
		// Step 1. Max BR Peak Search

		for (int i = 0; i < result2.get(mPos).FoundPeaks.size(); i++) {

			if (result2.get(mPos).FoundPeakBR.get(i) >= mMaxBR) {

				mPeakName = result2.get(mPos).isotopes;
				mMaxBRFoundEn = result2.get(mPos).FoundPeaks.get(i).Peak_Energy;
				mMaxBR = result2.get(mPos).FoundPeakBR.get(i);
				mMaxCh = (double) result2.get(mPos).FoundPeaks.get(i).Channel;
				mMaxBRIsoEn = result2.get(mPos).IsoPeakEn.get(i);
				mCount = i;

			}

			mFoundBrSqrtEffiSum += NcLibrary.Get_BrSqrtEffi(mEfficiency,
					result2.get(mPos).FoundPeaks.get(i).Peak_Energy, result2.get(mPos).FoundPeakBR.get(i));

		}

		for (int i = 0; i < result2.get(mPos).Peaks.size(); i++) {

			mBRSum += result2.get(mPos).Peaks.get(i).Isotope_Gamma_En_BR;
			BrSqrtEffiSum += NcLibrary.Get_BrSqrtEffi(mEfficiency, result2.get(mPos).Peaks.get(i).Peak_Energy,
					result2.get(mPos).Peaks.get(i).Isotope_Gamma_En_BR);
		}

		if (mBRSum == 0) {
			mMaxBR = 1;
			mBRSum = 0.00001;
		}

		double[] mFWHM = new double[2];
		mFWHM[0] = 1.2707811254;
		mFWHM[1] = -1.5464537062;

		// double[] FWHM_gen = new double[] { 1.2707811254, -1.5464537062 };

		// Index1 Calculration

		// mIndex1 = Math.exp(((-0.16 / Math.pow(mMaxCh * Math.sqrt(mFWHM[0]) +
		// mFWHM[1], 2))
		// * Math.pow(mMaxBRIsoEn - mMaxBRFoundEn, 2) * mMaxBR) / mBRSum);

		mIndex1 = Math.exp(((-0.16 / Math.pow(mFWHM[0] * Math.sqrt(mMaxCh) + mFWHM[1], 2))
				* Math.pow(mMaxBRIsoEn - mMaxBRFoundEn, 2) * mMaxBR) / mBRSum);

		// Hung
		// mIndex1 = Math.exp(((-0.16 / Math.pow(Math.sqrt(mFWHM[0] * mMaxCh) +
		// mFWHM[1], 2))
		// * Math.pow(mMaxBRIsoEn - mMaxBRFoundEn, 2) * mMaxBR) / mBRSum);

		result2.get(mPos).Index1 = mIndex1;

		// Index2 Calculration
		BrSqrtEffi = NcLibrary.Get_BrSqrtEffi(mEfficiency, mMaxBRIsoEn, mMaxBR);

		if ((BrSqrtEffiSum - mFoundBrSqrtEffiSum) != 0) {
			mIndex2Mid = 1.6 * ((BrSqrtEffiSum - mFoundBrSqrtEffiSum) / BrSqrtEffiSum);
			mIndex2 = mIndex1 - mIndex2Mid;
		} else {

			mIndex2 = mIndex1;
		}

		return mIndex2;
	}

	private Vector<Isotope> Find_UnknownPeak(Vector<Isotope> FoundIsotopes, Vector<NcPeak> Peak_Data) {

		// if(Th232UnkownPeakRock(FoundIsotopes)==true)return FoundIsotopes;

		Vector<Isotope> Result = new Vector<Isotope>();
		if (FoundIsotopes == null)
			return Result;

		Result.addAll(FoundIsotopes);
		// for(int i=0; i<Result.size();i++){
		// Result.get(i).Peaks.addAll(Result.get(i).Unknown_Peak);
		//// }
		Vector<Integer> UnknownPeakArrayNum = new Vector<Integer>();

		/// 1
		boolean check = false;
		for (int i = 0; i < Peak_Data.size(); i++) {

			for (int k = 0; k < FoundIsotopes.size(); k++) {
				Isotope tempIso = FoundIsotopes.get(k);

				for (int q = 0; q < tempIso.Peaks.size(); q++) {
					if (tempIso.Peaks.get(q).Energy_InWindow(Peak_Data.get(i).Peak_Energy))
						check = true;
				}
				for (int q = 0; q < tempIso.Unknown_Peak.size(); q++) {
					if (tempIso.Unknown_Peak.get(q).Energy_InWindow(Peak_Data.get(i).Peak_Energy))
						check = true;
				}
			}
			if (check == false)
				UnknownPeakArrayNum.add(i);

			check = false;
		}
		/// 2

		Vector<NcPeak> UnknownPeaks = new Vector<NcPeak>();

		/// 3
		for (int i = 0; i < UnknownPeakArrayNum.size(); i++) {
			UnknownPeaks.add(Peak_Data.get(UnknownPeakArrayNum.get(i)));
		}
		/// 3.2
		if (UnknownPeaks.size() != 0) {
			Isotope uknown_iso = new Isotope();
			uknown_iso.isotopes = "Unknown";
			uknown_iso.FoundPeaks = UnknownPeaks;
			uknown_iso.Class = "UNK";
			Result.add(uknown_iso);
		}
		/// 4 Ba-133 _ 161 kev Unkown
		/*
		 * for(int i=0; i<FoundIsotopes.size(); i++){
		 * if(FoundIsotopes.get(i).isotopes.matches("Ba-133")){ //if there is Ba133
		 * for(int k=0; k<Result.size(); k++){
		 * if(Result.get(k).Class.matches(".*UNK.*")){ // in UNKs
		 *
		 * Vector<NcPeak> TempPeak = Result.get(k).FoundPeaks; for(int q =0;
		 * q<TempPeak.size();q++){
		 * if(TempPeak.get(q).Energy_InWindow(161)){Result.remove(q);break;} }
		 *
		 * } } } }
		 */

		return Result;
	}

	public Isotope Get_Isotope(String IsotopeName) {
		Isotope result = new Isotope();
		if (mSel_Library.isEmpty() == true)
			return result;

		for (int i = 0; i < mSel_Library.size(); i++) {
			if (mSel_Library.get(i).isotopes.matches(IsotopeName))
				return mSel_Library.get(i);
		}

		return result;
	}

	private Vector<Isotope> Isotope_Filter(Vector<Isotope> targetIsotope, String mSearchIostope,
										   String[] RemoveIsotope) {

		boolean IsThereTagetIso = false;

		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches(mSearchIostope)) {
				IsThereTagetIso = true;
			}

		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {

				for (int j = 0; j < RemoveIsotope.length; j++) {

					if (targetIsotope.get(i).isotopes.matches(RemoveIsotope[j])) {

						targetIsotope.remove(i);
						break;

					}
				}

			}
		}

		return targetIsotope;
	}

	private Vector<Isotope> Filter_IdLogic_Algorithm_1(Vector<Isotope> targetIsotope) { // if
		// ID
		// U238,
		// Ga67,Ra226
		// or
		// Cs137,
		// inhibit
		// U235
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		boolean IsThereCs137 = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-238"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("Ga-67"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("Ra-226"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("Cs-137")) {
				IsThereTagetIso = true;
				IsThereCs137 = true;
			}
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235"))
					targetIsotope.remove(i);
			}
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235HE"))
					targetIsotope.remove(i);
			}
		}

		if (IsThereCs137 == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-233"))
					targetIsotope.remove(i);
			}
		}
		return targetIsotope;
	}

	private Vector<Isotope> Filter_IdLogic_Algorithm_2(Vector<Isotope> targetIsotope) { // if
		// ID
		// U235
		// or
		// U235HE
		// ,
		// inhibit
		// Tc-99m
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235"))
				IsThereTagetIso = true;
			else if (targetIsotope.get(i).isotopes.matches("U-235HE"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Tc-99m"))
					targetIsotope.remove(i);
			}
		}

		return targetIsotope;
	}

	private Vector<Isotope> Filter_IdLogic_Algorithm_3(Vector<Isotope> targetIsotope) { // if
		// is
		// there
		// Th-232
		// ,
		// inhibit
		// Co-57,Tc-99m
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Th-232"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Co-57"))
					targetIsotope.remove(i);
				else if (targetIsotope.get(i).isotopes.matches("Tc-99m"))
					targetIsotope.remove(i);
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_4(Vector<Isotope> targetIsotope, int cnt_100keV, int cnt_186keV) { // If
		// ID
		// U235
		// then
		// check
		// ratio
		// of
		// 100
		// keV
		// (x-rays)
		// peak/186
		// keV
		// peak,
		// if
		// >1
		// inhibit
		// U235
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true & cnt_100keV > cnt_186keV) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235"))
					targetIsotope.remove(i);
			}
		}
		/// ----------------
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235HE"))
				IsThereTagetIso = true;
		}

		if (IsThereTagetIso == true & cnt_100keV > cnt_186keV) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("U-235HE"))
					targetIsotope.remove(i);
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_5(int[] RealSPC, int AcqTime, int[] RealBG, int BgAcqTime,
													  Vector<Isotope> targetIsotope) { // If ID I125, I123, Am241, Co57,
		// U235 or Tc99m and Np/BKG (in
		// those ROIs) is <4 for that
		// specific isotope then inhibit
		// the ID of that specific
		// isotope (for ID of I123 use
		// ratio for 28 keV peak).
		// Vector<Isotope> temp;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}

		// -
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-125")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-123")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Co-57")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235HE")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Tc-99m")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Am-241")) {
				if (ms[(int) targetIsotope.get(i).FoundPeaks
						.get(0).Channel] < bg[(int) targetIsotope.get(i).FoundPeaks.get(0).Channel] * 4) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_6(Vector<Isotope> targetIsotope) { // If
		// ID
		// Cs137,
		// Am241,
		// Pu239,
		// 131
		// or
		// In111
		// then
		// inhibit
		// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Cs-137") | targetIsotope.get(i).isotopes.matches("Am-241")
					| targetIsotope.get(i).isotopes.matches("Pu-239") | targetIsotope.get(i).isotopes.matches("Pu-131")
					| targetIsotope.get(i).isotopes.matches("In-111")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-125")) {
					targetIsotope.remove(i);
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_7(Vector<Isotope> targetIsotope) { // If
		// ID
		// Ba133
		// inhibit
		// I123
		// and
		// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-123") | targetIsotope.get(i).isotopes.matches("I-125")) {
					targetIsotope.remove(i);
					i -= 1;
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_8(Vector<Isotope> targetIsotope) { // If
		// ID
		// U235
		// inhibit
		// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("U-235") | targetIsotope.get(i).isotopes.matches("U-235HE")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-123")) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_10(int[] RealSPC, int AcqTime, int[] RealBG, int BgAcqTime,
													   double CaliA, double CaliB, double CaliC, Vector<Isotope> targetIsotope) { // If
		// ID
		// U235
		// inhibit
		// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}

		////////// Co-60
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Co-60")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			if (ms[NcLibrary.Auto_floor(NcLibrary.Energy_to_Channel(225, CaliA, CaliB, CaliC))] < bg[NcLibrary
					.Auto_floor(NcLibrary.Energy_to_Channel(225, CaliA, CaliB, CaliC))] * 4) {
				for (int i = 0; i < targetIsotope.size(); i++) {
					if (targetIsotope.get(i).isotopes.matches("U-233")) {
						targetIsotope.remove(i);
					}
				}
			}
		}
		IsThereTagetIso = false;
		//// Ba-133
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			if (ms[(int) NcLibrary.Energy_to_Channel(295, CaliA, CaliB, CaliC)]
					* 2 > ms[(int) NcLibrary.Energy_to_Channel(141, CaliA, CaliB, CaliC)]) {
				for (int i = 0; i < targetIsotope.size(); i++) {
					if (targetIsotope.get(i).isotopes.matches("Tc-99m")) {
						targetIsotope.remove(i);
					}
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_11(int[] RealSPC, int AcqTime, int[] RealBG, int BgAcqTime,
													   double CaliA, double CaliB, double CaliC, Vector<Isotope> targetIsotope) { // If
		// ID
		// U235
		// inhibit
		// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}
		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ra-226")) {

				IsThereTagetIso = true;
			}

		}
		//////////
		if (IsThereTagetIso) {
			if (ms[(int) NcLibrary.Energy_to_Channel(636, CaliA, CaliB,
					CaliC)] < bg[(int) NcLibrary.Energy_to_Channel(636, CaliA, CaliB, CaliC)] * 4) {
				for (int i = 0; i < targetIsotope.size(); i++) {
					if (targetIsotope.get(i).isotopes.matches("I-131")) {
						targetIsotope.remove(i);
					}
				}
			}
		}

		return targetIsotope;
	}

	public Isotope get_iso() {
		Isotope iso = new Isotope();
		return iso;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_12(Vector<Isotope> targetIsotope) { // If
		// ID
		// U235
		// inhibit
		// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ra-226")) {
				IsThereTagetIso = true;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-131")) {
					targetIsotope.remove(i);
				}
			}
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
					targetIsotope.remove(i);
				}
			}
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("In-111")) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_13(Vector<Isotope> targetIsotope) { // If
		// ID
		// Ba133
		// inhibit
		// I123
		// and
		// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
				for (int k = 0; k < targetIsotope.size(); k++) {
					if (targetIsotope.get(k).isotopes.matches("Cs-137")) {
						IsThereTagetIso = true;
						break;
					}
				}
				break;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("I-131")) {
					targetIsotope.remove(i);
					break;
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_14(Vector<Isotope> targetIsotope) { // If
		// ID
		// Ba133
		// inhibit
		// I123
		// and
		// I125
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Th-232")) {
				IsThereTagetIso = true;
				break;
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("F-18")) {
					targetIsotope.remove(i);
					break;
				}
			}
		}
		if (IsThereTagetIso) {
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Tc-99m")) {
					targetIsotope.remove(i);
					break;
				}
			}
		}
		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_15(Vector<Isotope> targetIsotope, Vector<NcPeak> Peak_Data) { // If
		// ID
		// //
		// I-131
		// I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-131")) {
				IsThereTagetIso = true;
			}
		}

		for (int i = 0; i < Peak_Data.size(); i++) {

			if (Energy_Check(Peak_Data.get(i).Peak_Energy)) {
				IsThereTagetIso = true;
			}

		}

		if (IsThereTagetIso)

		{
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches("Ba-133")) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;

	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_16(Vector<Isotope> targetIsotope, Spectrum SPC, int[] RealSPC,
													   int AcqTime, int[] RealBG, int BgAcqTime) { // If

		int[] ms = new int[1024];
		double[] bg = new double[1024];

		for (int i = 0; i < 1024; i++) {
			ms[i] = RealSPC[i];
			bg[i] = ((double) RealBG[i] / BgAcqTime) * AcqTime;
		}
		int I131_364En = SpcAnalysis.ToChannel(364, SPC.Get_Coefficients());

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("I-131")) {
				if (ms[I131_364En] < (int) (bg[I131_364En] * 10)) {
					targetIsotope.remove(i);
					i -= 1;
					break;
				}
			}
		}

		return targetIsotope;
	}

	public Vector<Isotope> Filter_IdLogic_Algorithm_Nucare1(Spectrum spc, Vector<Isotope> targetIsotope,
															Vector<NcPeak> FoundPeak) { // If ID U235 inhibit I123
		// Vector<Isotope> temp;
		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches("Cs-137")) {
				IsThereTagetIso = true;
			}
			if (targetIsotope.get(i).isotopes.matches("Ra-226")) {
				return targetIsotope;
			}
		}
		if (IsThereTagetIso) {
			double measu_sum = 0;
			boolean check = false;
			Isotope Ra226 = Get_Isotope("Ra-226");
			for (int i = 0; i < FoundPeak.size(); i++) {
				if (FoundPeak.get(i).Energy_InWindow(1120)) { // Ra133 Peak
					double measu = Math.abs((((FoundPeak.get(i).Peak_Energy - 1120) / 1120) * 100));
					measu = 100 - measu;
					measu_sum += measu;

					check = true;
					Ra226.FoundPeaks.add(FoundPeak.get(i));
					break;
				}
			}
			if (check) {
				check = false;
				for (int i = 0; i < FoundPeak.size(); i++) {
					if (FoundPeak.get(i).Energy_InWindow(1780)) { // Ra133 Peak
						double measu = Math.abs((((FoundPeak.get(i).Peak_Energy - 1780) / 1780) * 100));
						measu = 100 - measu;
						measu_sum += measu;

						check = true;
						Ra226.FoundPeaks.add(FoundPeak.get(i));
						break;
					}
				}
				if (check) {
					Ra226.Confidence_Level = measu_sum / 2;
					targetIsotope.add(Ra226);
					return targetIsotope;
				}
			}
		}

		return targetIsotope;
	}

	public boolean Energy_Check(double energy) {

		double mCompareEnergy = 634;
		double L_ROI_Percent = mCompareEnergy - (mCompareEnergy * 0.07);
		double R_ROI_Percent = mCompareEnergy + (mCompareEnergy * 0.07);

		if (L_ROI_Percent <= energy && R_ROI_Percent >= energy) {
			return true;
		} else
			return false;
	}

	private boolean Th232UnkownPeakRock(Vector<Isotope> FoundIsotopes) {

		boolean FoundIsotopesTh232 = false;

		for (int i = 0; i < FoundIsotopes.size(); i++) {
			if (FoundIsotopes.get(i).isotopes.matches("Th-232")) {
				FoundIsotopesTh232 = true;
				break;
			}
		}

		if (FoundIsotopesTh232 = true) {

			return true;
		}
		return false;

	}

	public Vector<Isotope> Filter_IdLogic_Common(Vector<Isotope> targetIsotope, String Taget, String[] RemoveSource) {

		for (int i = 0; i < RemoveSource.length; i++) {
			targetIsotope = Filter_IdLogic(targetIsotope, Taget, RemoveSource[i]);

		}
		return targetIsotope;

	}

	public Vector<Isotope> Filter_IdLogic(Vector<Isotope> targetIsotope, String Taget, String RemoveSource) {

		boolean IsThereTagetIso = false;
		if (targetIsotope.isEmpty())
			return targetIsotope;

		for (int i = 0; i < targetIsotope.size(); i++) {
			if (targetIsotope.get(i).isotopes.matches(Taget)) {
				IsThereTagetIso = true;
			}
		}

		if (IsThereTagetIso)

		{
			for (int i = 0; i < targetIsotope.size(); i++) {
				if (targetIsotope.get(i).isotopes.matches(RemoveSource)) {
					targetIsotope.remove(i);
				}
			}
		}

		return targetIsotope;

	}

	//18.06.07
	//because: all always keep memory in PeakIsoMatching_H
	public Vector<Isotope> IsoRemoveLines(Vector<Isotope> result2,Vector<NcPeak> FoundPeak)
	{
		//Step1: Remove energy which is not  peak

		int NoPeak=FoundPeak.size();

		for(int noiso=0;noiso<result2.size();noiso++)
		{
			for(int EnIdx=0;EnIdx<result2.get(noiso).FoundPeaks.size();EnIdx++)
			{
				boolean flg=false;

				for(int k=0;k<NoPeak;k++)
				{
					if(result2.get(noiso).FoundPeaks.get(EnIdx).Peak_Energy==FoundPeak.get(k).Peak_Energy)
					{
						flg=true;
					}
				}

				if(flg==false)
				{
					result2.get(noiso).FoundPeaks.remove(EnIdx);
					result2.get(noiso).FoundPeakBR.remove(EnIdx);
					result2.get(noiso).IsoPeakEn.remove(EnIdx);
					EnIdx--;
				}

			}
		}



		//Step 2: Remove energy which is copied double times
		for(int noiso=0;noiso<result2.size();noiso++)
		{
			for(int EnIdx=0;EnIdx<result2.get(noiso).FoundPeaks.size();EnIdx++)
			{
				int cnt=0;
				for(int EnIdx1=0;EnIdx1<result2.get(noiso).FoundPeaks.size();EnIdx1++)
				{
					if(EnIdx!=EnIdx1)
					{
						if(EnIdx1!=EnIdx)
						{
							if(result2.get(noiso).FoundPeaks.get(EnIdx).Peak_Energy==result2.get(noiso).FoundPeaks.get(EnIdx1).Peak_Energy)
							{
								cnt=cnt+1;
							}
						}
					}
				}

				//remove peak
				if(cnt>0)
				{
					result2.get(noiso).FoundPeaks.remove(EnIdx);
					result2.get(noiso).FoundPeakBR.remove(EnIdx);
					result2.get(noiso).IsoPeakEn.remove(EnIdx);
					EnIdx--;
				}
			}
		}
		return result2;
	}

	public Vector<Isotope> AddPeakDraw(Vector<Isotope> result2,Vector<NcPeak> mFoundPeak_data,double[] FWHMCoeff,Coefficients coeff, double WndROI)
	{
		double EnTemp, BrTemp,L_ROI,R_ROI;
		double [] Thshold=new double [2];

		Isotope SourceInfo = new Isotope();

		for (int i = 0; i < mSel_Library.size(); i++)
		{
			//Step 1: Reset memory
			SourceInfo = mSel_Library.get(i);

			/*
				if(SourceInfo.FoundPeaks.size()>0)
				{
					for(int ii=0;ii<SourceInfo.FoundPeaks.size();ii++)
					{
						SourceInfo.FoundPeaks.remove(ii);

						ii--;
					}

					for(int ii=0;ii<SourceInfo.FoundPeakBR .size();ii++)
					{
						SourceInfo.FoundPeakBR.remove(ii);

						ii--;
					}

					for(int ii=0;ii<SourceInfo.FoundPeakBR .size();ii++)
					{
						SourceInfo.FoundPeakBR.remove(ii);

						ii--;
					}
				}

			 */

			//Step 2: Compare with library isotope
			for(int j=0;j<result2.size();j++)
			{
				//Adding: Major peak
				if (result2.get(j).isotopes.equals(SourceInfo.isotopes))
				{
					//Adding Major Peak
					for(int k=0;k<SourceInfo.Peaks.size();k++)
					{
						EnTemp=SourceInfo.Peaks.get(k).Peak_Energy;
						BrTemp=SourceInfo.Peaks.get(k).Isotope_Gamma_En_BR;

						Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(EnTemp, FWHMCoeff,coeff,WndROI);

						L_ROI=Thshold[0];
						R_ROI = Thshold[1];

						for (int q = 0; q < mFoundPeak_data.size(); q++)
						{
							double Entemp=mFoundPeak_data.get(q).Peak_Energy;

							if(Entemp>L_ROI&&Entemp<R_ROI)
							{
								result2.get(j).ListPeakDrawEn.add(mFoundPeak_data.get(q));

								result2.get(j).ListPeakDrawBR.add(BrTemp);

								break;
							}
						}
					}

					//Adding: Minor Peak
					for(int k=0;k<SourceInfo.IsoMinorPeakEn.size();k++)
					{
						EnTemp=SourceInfo.IsoMinorPeakEn.get(k);
						BrTemp=SourceInfo.IsoMinorPeakBR.get(k);

						Thshold=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(EnTemp, FWHMCoeff,coeff,WndROI);

						L_ROI=Thshold[0];
						R_ROI = Thshold[1];

						for (int q = 0; q < mFoundPeak_data.size(); q++)
						{
							double Entemp=mFoundPeak_data.get(q).Peak_Energy;

							if(Entemp>L_ROI&&Entemp<R_ROI)
							{
								result2.get(j).ListPeakDrawEn.add(mFoundPeak_data.get(q));

								result2.get(j).ListPeakDrawBR.add(BrTemp);

								break;
							}
						}
					}
				}

			}
		}

		return result2;
	}

	//Because memory always keep in Java
	public Vector<Isotope> PeakMatchIsotope_HH(Vector<NcPeak> mFoundPeak_data, double[] FWHMCoeff, Coefficients coeff,double WndROI,Vector<Isotope> MainResult)
	{
		// double [] EnCalCoeff=new double [3];
		// EnCalCoeff[0] = coeff.get_Coefficients()[0];
		// EnCalCoeff[1]= coeff.get_Coefficients()[1];
		// EnCalCoeff[2]= coeff.get_Coefficients()[2];

		Vector<Isotope> result2 = new Vector<Isotope>();
		Isotope SourceInfo = new Isotope();
		boolean check = false;
		double Peak_Confidence_Value_sum = 0;
		int PeakCnt = 0;

		int CountPeak = 0;
		double[] FoundMSEn = new double[10];
		double[] FoundMSNet = new double[10];
		double[] FoundSourceInfo = new double[10];
		int index = 0;
		double max1 = 0;

		for (int i = 0; i < mSel_Library.size(); i++)
		{

			SourceInfo = mSel_Library.get(i);

			//START: reset memory....
			//reset memory because in Java still keep information of previous step
			SourceInfo = mSel_Library.get(i);

			boolean FlgSrc=false;
			for (int nosrc=0;nosrc<MainResult.size();nosrc++)
			{
				if (MainResult.get(nosrc).isotopes.equals(SourceInfo.isotopes))
				{
					FlgSrc=true;
				}
			}

			if(FlgSrc==false)
			{
				for(int ii=0;ii<SourceInfo.FoundPeaks.size();ii++)
				{
					SourceInfo.FoundPeaks.remove(ii);
					ii--;
				}


				for(int ii=0;ii<SourceInfo.FoundPeakBR .size();ii++)
				{
					SourceInfo.FoundPeakBR.remove(ii);
					ii--;
				}


				for(int ii=0;ii<SourceInfo.IsoPeakEn.size();ii++)
				{
					SourceInfo.IsoPeakEn.remove(ii);
					ii--;
				}
				//if(SourceInfo.Index2<0||SourceInfo.Index2=0)
				SourceInfo.Index1=0;
				SourceInfo.Index2=0;
			}

			//End memory reset


			for (int EnCnt = 0; EnCnt < SourceInfo.Peaks.size(); EnCnt++)
			{

				CountPeak = 0;
				for (int k = 0; k < mFoundPeak_data.size(); k++)
				{

					boolean isIn = SourceInfo.Peaks.get(EnCnt).Energy_InWindow_H(mFoundPeak_data.get(k).Peak_Energy,
							FWHMCoeff, coeff, WndROI);

					if (isIn) {
						FoundMSEn[CountPeak] = mFoundPeak_data.get(k).Peak_Energy;
						FoundMSNet[CountPeak] = mFoundPeak_data.get(k).NetCnt;
						CountPeak = CountPeak + 1;
					}
				}

				if (CountPeak > 0) {
					max1 = 0.0;
					for (int j = 0; j < CountPeak; j++)
					{
						if (FoundMSNet[j] > max1)
						{
							max1 = FoundMSNet[j];
							index = j;
						}
					}

					// adding to source infor

					for (int k = 0; k < mFoundPeak_data.size(); k++)
					{

						boolean isIn = SourceInfo.Peaks.get(EnCnt).Energy_InWindow_H(mFoundPeak_data.get(k).Peak_Energy,
								FWHMCoeff, coeff, WndROI);

						if (isIn) {
							if (FoundMSEn[index] == mFoundPeak_data.get(k).Peak_Energy)
							{
								Peak_Confidence_Value_sum += Confidence_Level_Cal(mFoundPeak_data.get(k).Peak_Energy,
										SourceInfo.Peaks.get(EnCnt).Peak_Energy);

								SourceInfo.FoundPeaks.add(mFoundPeak_data.get(k));

								SourceInfo.FoundPeakBR.add(SourceInfo.Peaks.get(EnCnt).Isotope_Gamma_En_BR);

								SourceInfo.IsoPeakEn.add(SourceInfo.Peaks.get(EnCnt).Peak_Energy);

								PeakCnt++;

								break;
							}
						}
					}

				}

			}
			if (PeakCnt != 0) {

				PeakCnt = 0;

				SourceInfo.Confidence_Level = Peak_Confidence_Value_sum / SourceInfo.Get_OnlyIdEnergy_Cnt();

				result2.add(SourceInfo);

			}
			Peak_Confidence_Value_sum = 0;
			check = false;

		}
		return result2;
	}
}// end class
