package android.HH100.Structure;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class NewAnalsysData{

	public double getPeak() {
		return Peak;
	}
	public void setPeak(double peak) {
		Peak = peak;
	}
	public double getROI_Left() {
		return ROI_Left;
	}
	public void setROI_Left(double rOI_Left) {
		ROI_Left = rOI_Left;
	}
	public double getROI_Right() {
		return ROI_Right;
	}
	public void setROI_Right(double rOI_Right) {
		ROI_Right = rOI_Right;
	}
	public double getSigma() {
		return sigma;
	}
	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	public double getPeakEst() {
		return PeakEst;
	}
	public void setPeakEst(double peakEst) {
		PeakEst = peakEst;
	}
	public double getHeight() {
		return Height;
	}
	public void setHeight(double height) {
		Height = height;
	}
	public double getBG_a() {
		return BG_a;
	}
	public void setBG_a(double bG_a) {
		BG_a = bG_a;
	}
	public double getBG_b() {
		return BG_b;
	}
	public void setBG_b(double bG_b) {
		BG_b = bG_b;
	}
	public double getNetCnt() {
		return NetCnt;
	}
	public void setNetCnt(double netCnt) {
		NetCnt = netCnt;
	}
	public double getPeak_Energy() {
		return Peak_Energy;
	}
	public void setPeak_Energy(double peak_Energy) {
		Peak_Energy = peak_Energy;
	}
	public double getPeak_Uncertainty() {
		return peak_Uncertainty;
	}
	public void setPeak_Uncertainty(double peak_Uncertainty) {
		this.peak_Uncertainty = peak_Uncertainty;
	}
	public double getTrue_Energy_keV() {
		return True_Energy_keV;
	}
	public void setTrue_Energy_keV(double true_Energy_keV) {
		True_Energy_keV = true_Energy_keV;
	}
	public double getBR_Factor() {
		return BR_Factor;
	}
	public void setBR_Factor(double bR_Factor) {
		BR_Factor = bR_Factor;
	}
	public double getMesuared_Activty() {
		return Mesuared_Activty;
	}
	public void setMesuared_Activty(double mesuared_Activty) {
		Mesuared_Activty = mesuared_Activty;
	}
	public double getPeak_MDA() {
		return Peak_MDA;
	}
	public void setPeak_MDA(double peak_MDA) {
		Peak_MDA = peak_MDA;
	}
	public double getHalf_life_time() {
		return Half_life_time;
	}
	public void setHalf_life_time(double half_life_time) {
		Half_life_time = half_life_time;
	}
	public double getTrue_Current_Activity_Bg() {
		return True_Current_Activity_Bg;
	}
	public void setTrue_Current_Activity_Bg(double true_Current_Activity_Bg) {
		True_Current_Activity_Bg = true_Current_Activity_Bg;
	}
	public double getUsed_for_Boolen() {
		return Used_for_Boolen;
	}
	public void setUsed_for_Boolen(double used_for_Boolen) {
		Used_for_Boolen = used_for_Boolen;
	}
	public double getFWHM_Kev() {
		return FWHM_Kev;
	}
	public void setFWHM_Kev(double fWHM_Kev) {
		FWHM_Kev = fWHM_Kev;
	}
	public double getEfficiency() {
		return Efficiency;
	}
	public void setEfficiency(double efficiency) {
		Efficiency = efficiency;
	}
	public double getBackground_Net_Count() {
		return Background_Net_Count;
	}
	public void setBackground_Net_Count(double background_Net_Count) {
		Background_Net_Count = background_Net_Count;
	}
	public double Peak;
	public double ROI_Left;
	public double ROI_Right;
	public double sigma;
	public double PeakEst;
	public double Height;
	public double BG_a ;
	public double BG_b ;
	public double NetCnt;
	public double Peak_Energy;
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

	//public double Neutron_MAX;




}
