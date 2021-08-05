package android.HH100.Structure;

import NcLibrary.Coefficients;
import NcLibrary.NewNcAnalsys;

public class NcPeak {

	public int Channel;
	public double Peak_Energy;

	public boolean Energy_InWindow(double energy) {
		double Roi_window = NcLibrary.Get_Roi_window_by_energy(Peak_Energy);

		double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
		double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

		if (Peak_Energy * L_ROI_Percent < energy && Peak_Energy * R_ROI_Percent > energy) {
			return true;
		} else
			return false;
	}

	public boolean Energy_InWindow_H(double energy, double[] FWHMCoeff, Coefficients coeff,double WndROI) {

		double[] Thshold = new double[2];

		// Thshold=NcLibrary.Get_Roi_window_by_energy_used_FWHM(Peak_Energy,
		// FWHMCoeff,EnergyCoeff3P);

		//Coefficients mCoeff = new Coefficients();

		//mCoeff.set_Coefficients(EnergyCoeff3P);

		// public void set_Coefficients(double[] _Coeffcients) {
		// this._Coeffcients = _Coeffcients;
		// }

		Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(Peak_Energy, FWHMCoeff, coeff,WndROI);

		// double Roi_window = NewNcAnalsys.Get_Roi_window_by_energy(Peak_Energy);

		double L_ROI = Thshold[0];

		double R_ROI = Thshold[1];

		if (L_ROI < energy && R_ROI > energy) {
			return true;
		} else
			return false;
	}

	public double LC;
	public double Peak;
	public int ROI_Left;
	public int ROI_Right;
	public double sigma;
	public double PeakEst;
	public double Height;
	public double BG_a;
	public double BG_b;
	public double NetCnt;
	// public double Peak_Energy;
	public double peak_Uncertainty;
	public double True_Energy_keV;
	public double BR_Factor;
	public double Mesuared_Activty;
	public double Peak_MDA;
	public double Half_life_time;
	public double True_Current_Activity_Bg;
	public double Used_for_Boolen;
	public double FWHM_Kev;
	public double Efficiency;
	public double Background_Net_Count;
	public double Isotope_Gamma_En_BR;
	public double Doserate;

}
