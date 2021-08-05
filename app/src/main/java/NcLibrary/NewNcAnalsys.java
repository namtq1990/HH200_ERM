package NcLibrary;

import java.util.ArrayList;
import java.util.Vector;

import android.HH100.Identification.ActivityConfidence;
import android.HH100.Identification.Isotope;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.PeakAnalysis;

public class NewNcAnalsys {
	public static int CHSIZE = 1024;
	public static int BINSIZE = 850;
	public static int NoListTrueEn = 20;
	public static double Thshold_D = 0.3;
	public static double WndRatio_Co57_Tc99m = 0.45;
	public static int NoMaxCEPeak = 20;

	public static double[] FWHM_gen = new double[] { 1.4385, -3.606 };

	public static double[] InterCoeff = new double[] { -0.0000000001, 0.0000005531, -0.0008610261, 0.5684236932,
			-53.5185548731, 0.0002779219, -0.0100275772, 5.8129370431 };


	//Define Activity Threshold Threshold
	public static double ActThreshold=0.05; //


	// #region TransferFunct
	// double FWHMCoeff[2], double TF[BINSIZE]
	public static double[] TransferFunct(double[] FWHMCoeff, double[] TF) // FWHMCoeff
	// index
	// 2
	{
		double a_fit = FWHMCoeff[0];
		double b_fit = FWHMCoeff[1];

		double[] fit1 = new double[CHSIZE];

		double x = 0;

		for (int i = 0; i < CHSIZE; i++) {
			x = i + 1;
			fit1[i] = a_fit * Math.sqrt(x) + b_fit;

		}

		// resize fit1 from CHSIZE to BINSIZE
		double sc = (double) CHSIZE / (double) BINSIZE;

		double sc0 = sc / 2d;

		int ind = 0;
		double tmp;
		double sum1 = 0;
		for (int i = 0; i < BINSIZE; i++) {
			x = i + 1;
			// reset TF function
			TF[i] = 0;
			tmp = sc0 + (x - 1) * sc + 1;
			ind = (int) Math.floor(tmp) - 1;
			if (ind >= 0 && ind < CHSIZE) {
				TF[i] = fit1[ind];
				sum1 = sum1 + TF[i];
			}
		}

		// normalize
		for (int i = 0; i < BINSIZE; i++) {
			TF[i] = TF[i] / sum1 * 1024d;
		}

		// release variable

		return TF;
	}
	// #endregion

	// #region ReBinning
	// Channel spectrum占쏙옙 binning spectrum占쏙옙占쏙옙 占쏙옙환占싼댐옙. 1024ch -> 850ch
	// double ChSpec[CHSIZE], double TF[BINSIZE], double BinSpec[BINSIZE])
	public static double[] ReBinning(double[] ChSpec, double[] TF, double[] BinSpec) {
		boolean First = true;
		double Z = 0, sum1 = 0, sum2 = 0, z_flt = 0, z_flt_pre = 0, ztmp = 0;
		int ind_chn = 0;

		int z_int = 0;
		for (int i = 0; i < BINSIZE; i++) {
			Z = TF[i];

			if (Z < 1) {
				sum1 = sum1 + Z;
				sum2 = 0;

				if (sum1 > 1) {
					ind_chn = ind_chn + 1;
					sum2 = sum1 - 1;
					BinSpec[i] = (Z - sum2) * ChSpec[ind_chn - 1] + sum2 * ChSpec[ind_chn];
					sum1 = sum2;
				} else {
					sum2 = Z;
					BinSpec[i] = sum2 * ChSpec[ind_chn];
				}

			} else if (Z > 1) {
				if (First == true) {
					First = false;
					z_int = (int) Math.floor(Z);
					z_flt = Z - z_int;

					if (ind_chn + z_int + 1 > CHSIZE - 1) {
						break;
					}

					sum1 = 0;
					for (int j = 1; j <= z_int; j++) {
						sum1 = sum1 + ChSpec[ind_chn + j];
					}

					sum1 = sum1 + z_flt * ChSpec[ind_chn + z_int + 1];

					ind_chn = ind_chn + z_int + 1;
					z_flt_pre = 1 - z_flt;
				}

				else {
					ztmp = Z - z_flt_pre;

					z_int = (int) Math.floor(ztmp);
					sum1 = 0;

					if (z_int >= 1) {
						z_flt = ztmp - z_int;

						if (ind_chn + z_int + 1 > CHSIZE - 1) {
							break;
						}

						sum1 = z_flt_pre * ChSpec[ind_chn];

						for (int j = 1; j <= z_int; j++) {
							sum1 = sum1 + ChSpec[ind_chn + j];
						}

						sum1 = sum1 + z_flt * ChSpec[ind_chn + z_int + 1];

						ind_chn = ind_chn + z_int + 1;
						z_flt_pre = 1 - z_flt;

					} else {
						z_flt = Z - z_flt_pre;

						if (ind_chn + z_int + 1 > CHSIZE - 1) {
							break;
						}

						sum1 = z_flt_pre * ChSpec[ind_chn] + z_flt * ChSpec[ind_chn + 1];
						z_flt_pre = 1 - z_flt;
						ind_chn = ind_chn + 1;
					}
				}
				BinSpec[i] = sum1;
			} else if (Z == 1) {
				if (ind_chn < CHSIZE) {
					BinSpec[i] = ChSpec[ind_chn];
					ind_chn = ind_chn + 1;
				}

			}
		}
		return BinSpec;
	}
	// #endregion

	// #region ReturnReBinning
	// 占쏙옙환占쏙옙 Binning spectrum占쏙옙 占쌕쏙옙 채占쏙옙 spectrum占쏙옙占쏙옙 占쏙옙환占쏙옙킨占쏙옙.
	// double ChSpec[CHSIZE], double TF[BINSIZE], double BinSpec[BINSIZE]
	public static double[] ReturnReBinning(double[] BinSpec, double[] TF, double[] ChSpec) {
		double sumz1 = 0, sumz2 = 0, resi = 0, sum_pre = 0, sumz3 = 0, z_flt_pre_weigh = 0, z = 0, sumtmp = 0;
		int cnt = 0, ind_chn = 0, i = 0;
		boolean First = true, First1 = true;

		int z_int = 0;
		double z_pre_w = 0, z_pre = 0, z_cur = 0, z_res = 0;

		while (true) {
			cnt = 0;
			if (TF[i] < 1) {
				while (true) {
					z = TF[i + cnt];
					sumz1 = sumz1 + z;

					if (sumz1 >= 1) {
						break;
					}
					cnt = cnt + 1;
				}

				sumz2 = (z - (sumz1 - 1)) / z;

				if (First == true) {
					First = false;

					sumtmp = 0;
					for (int j = i; j <= (i + cnt - 1); j++) {
						sumtmp = sumtmp + BinSpec[j];
					}
					sumtmp = sumtmp + sumz2 * BinSpec[i + cnt];

					ChSpec[ind_chn] = sumtmp;

					sumz3 = (sumz1 - 1) / z;
				} else {
					sumtmp = 0;

					sumtmp = sumz3 * BinSpec[i - 1];

					for (int j = i; j <= (i + cnt - 1); j++) {
						sumtmp = sumtmp + BinSpec[j];
					}
					sumtmp = sumtmp + sumz2 * BinSpec[i + cnt];

					ChSpec[ind_chn] = sumtmp;

					sumz3 = (sumz1 - 1) / z;
				}

				sumz1 = sumz1 - 1;
				ind_chn = ind_chn + 1;
				i = i + cnt + 1;

				if (i > BINSIZE - 1) {
					break;
				}
			} else {

				z = TF[i];

				if (First1 == true) {
					First1 = false;
					z_int = (int) Math.floor(z);
					z_pre_w = z - z_int;
					z_pre = z_pre_w / z;

					ChSpec[ind_chn] = 1d / z * BinSpec[i];
					ind_chn = ind_chn + 1;
					i = i + 1;
				} else {
					z_cur = 1 - z_pre_w;

					ChSpec[ind_chn] = z_pre * BinSpec[i - 1] + z_cur / z * BinSpec[i];
					z_res = z - z_cur;

					if (z_res > 1) {
						z_int = (int) Math.floor(z_res);

						for (int j = 1; j <= z_int; j++) {
							ind_chn = ind_chn + 1;

							if (i > BINSIZE - 1) {
								break;
							}

							ChSpec[ind_chn] = 1 / z * BinSpec[i];
						}

						z_pre_w = z - z_cur - z_int;
						z_pre = z_pre_w / z;
					} else {
						z_pre_w = z_res;
						z_pre = z_pre_w / z;
					}
					ind_chn = ind_chn + 1;
				}
				i = i + 1;
			}

			if (i > BINSIZE - 1) {
				break;
			}
		}
		return ChSpec;
	}
	// #endregion

	// #region BGErosion
	// Binning spectrum占쏙옙占쏙옙 erosion占쏙옙 占쏙옙占쏙옙 background 占쏙옙占쏙옙트占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙.
	// double MSBinSpec[BINSIZE], double IterCoeff[4], double
	// BGEroBinSpec[BINSIZE]

	public static double[] BinEro_testspectrum(String mSpec, String SplitUnit) {

		String[] mSpecSplit = mSpec.split(SplitUnit);
		double[] mSpec1 = new double[BINSIZE];

		for (int i = 0; i < mSpec1.length; i++) {
			double a = Double.valueOf(mSpecSplit[i]).doubleValue();

			mSpec1[i] = a;

		}

		return mSpec1;
	}

	public static double[] BGErosion1(double[] MSBinSpec, double[] IterCoeff, double[] BGEroBinSpec) {
		// double[] datatmp = new double[BINSIZE];
		double a3 = IterCoeff[0];
		double a2 = IterCoeff[1];
		double a1 = IterCoeff[2];
		double a0 = IterCoeff[3];
		double x = 0;
		int noiter = 0;

		int MaxIter = 71; // Defined: MaxNoIter=70+1

		double[][] DataEro = new double[MaxIter][];
		for (int i = 0; i < MaxIter; i++) {
			DataEro[i] = new double[BINSIZE];
			for (int j = 0; j < BINSIZE; j++) {
				DataEro[i][j] = MSBinSpec[j];
			}
		}

		// 1st :Erosion and then save array
		double tmp;
		for (int iter = 0; iter < MaxIter - 1; iter++) {
			for (int i = 0; i < BINSIZE; i++) {
				if (i >= 4 && i <= BINSIZE - 5) {
					tmp = (DataEro[iter][i - 4] + DataEro[iter][i + 4]) / 2d;
					if (DataEro[iter][i] > tmp) {
						DataEro[iter][i] = tmp;

					}
					// DataEro[iter+1][i] = DataEro[iter][i];
				}
				DataEro[iter + 1][i] = DataEro[iter][i];
			}

		}

		// 2 step:

		double noiter1 = 0;

		// iterosion
		for (int ind = 0; ind < BINSIZE; ind++) {

			x = ind + 1;
			x = Math.log(x);

			noiter1 = (a3 * x * x * x + a2 * x * x + a1 * x + a0);
			noiter = (int) Math.round(noiter1);

			if (ind < 128)
				// noiter = (int) (0.0252 * (ind + 1) + 5.7552);
				noiter = (int) (0.115 * (ind + 1) + 5);

			if (noiter < 2)
				noiter = 2;
			if (noiter > 70)
				noiter = 70;

			BGEroBinSpec[ind] = DataEro[noiter - 1][ind];
		}
		return BGEroBinSpec;
	}

	// new

	public static double[] BGErosion(double[] MSBinSpec, double[] IterCoeff, double[] BGEroBinSpec, double[] TF,
									 Coefficients coeff) {
		// double[] datatmp = new double[BINSIZE];
		// for (int i = 0; i < BINSIZE; i++)
		// {
		// datatmp[i] = MSBinSpec[i];
		// }

		double a4 = IterCoeff[0];
		double a3 = IterCoeff[1];
		double a2 = IterCoeff[2];
		double a1 = IterCoeff[3];
		double a0 = IterCoeff[4];

		double b0, b1, b2;
		b2 = IterCoeff[5];
		b1 = IterCoeff[6];
		b0 = IterCoeff[7];

		double x = 0;
		int noiter = 0;

		int MaxIter = 200; // Defined: MaxNoIter=70+1

		double[][] DataEro = new double[MaxIter][];
		for (int i = 0; i < MaxIter; i++) {
			DataEro[i] = new double[BINSIZE];
			for (int j = 0; j < BINSIZE; j++) {
				DataEro[i][j] = MSBinSpec[j];
			}
		}

		// 1st :Erosion and then save array
		double tmp;
		for (int iter = 0; iter < MaxIter - 1; iter++) {
			for (int i = 0; i < BINSIZE; i++) {
				if (i >= 4 && i <= BINSIZE - 5) {
					tmp = (DataEro[iter][i - 4] + DataEro[iter][i + 4]) / 2d;
					if (DataEro[iter][i] > tmp) {
						DataEro[iter][i] = tmp;

					}
					// DataEro[iter+1][i] = DataEro[iter][i];
				}
				DataEro[iter + 1][i] = DataEro[iter][i];
			}

		}

		// 2 step:

		double[] CHArray = new double[BINSIZE]; // BinSpec[BINSIZE]
		for (int i = 0; i < BINSIZE; i++)
			CHArray[i] = 0;

		CHArray = BintoCh(TF, CHArray);

		double noiter1;
		// iterosion
		double ch, en;
		for (int ind = 0; ind < BINSIZE; ind++) {
			ch = CHArray[ind];
			en = coeff.get_Coefficients()[0] * ch * ch + coeff.get_Coefficients()[1] * ch + coeff.get_Coefficients()[2];

			x = en;

			if (en <= 145) {
				noiter1 = (int) (b2 * x * x + b1 * x + b0);
			} else {
				noiter1 = a4 * x * x * x * x + a3 * x * x * x + a2 * x * x + a1 * x + a0;
			}

			noiter = (int) Math.round(noiter1);

			if (noiter < 2)
				noiter = 2;
			if (noiter > MaxIter - 1)
				noiter = MaxIter - 1;

			BGEroBinSpec[ind] = DataEro[noiter - 1][ind];
		}

		return BGEroBinSpec;
	}

	public static double[] BintoCh(double[] TF, double[] BinSpec) {
		Boolean First = true;
		double Z = 0, sum1 = 0, sum2 = 0, z_flt = 0, z_flt_pre = 0, ztmp = 0;
		int ind_chn = 0;

		int z_int = 0;
		for (int i = 0; i < BINSIZE; i++) {
			Z = TF[i];

			if (Z < 1) {
				sum1 = sum1 + Z;
				sum2 = 0;

				if (sum1 > 1) {
					ind_chn = ind_chn + 1;
					sum2 = sum1 - 1;
					BinSpec[i] = ind_chn;
					sum1 = sum2;
				} else {
					sum2 = Z;
					BinSpec[i] = ind_chn;
				}

			} else if (Z > 1) {
				if (First == true) {
					First = false;
					z_int = (int) Math.floor(Z);
					z_flt = Z - z_int;

					if (ind_chn + z_int + 1 > CHSIZE - 1) {
						break;
					}

					sum1 = 0;
					// for (int j = 1; j <= z_int; j++)
					// {
					// sum1 = sum1 + ChSpec[ind_chn + j];
					// }

					// sum1 = sum1 + z_flt * ChSpec[ind_chn + z_int + 1];

					ind_chn = ind_chn + z_int + 1;
					z_flt_pre = 1 - z_flt;
				}

				else {
					ztmp = Z - z_flt_pre;

					z_int = (int) Math.floor(ztmp);
					sum1 = 0;

					if (z_int >= 1) {
						z_flt = ztmp - z_int;

						if (ind_chn + z_int + 1 > CHSIZE - 1) {
							break;
						}

						// sum1 = z_flt_pre * ChSpec[ind_chn];

						// for (int j = 1; j <= z_int; j++)
						// {
						// sum1 = sum1 + ChSpec[ind_chn + j];
						// }

						// sum1 = sum1 + z_flt * ChSpec[ind_chn + z_int + 1];

						ind_chn = ind_chn + z_int + 1;
						z_flt_pre = 1 - z_flt;

					} else {
						z_flt = Z - z_flt_pre;

						if (ind_chn + z_int + 1 > CHSIZE - 1) {
							break;
						}

						// sum1 = z_flt_pre * ChSpec[ind_chn] + z_flt *
						// ChSpec[ind_chn + 1];
						z_flt_pre = 1 - z_flt;
						ind_chn = ind_chn + 1;
					}
				}
				BinSpec[i] = ind_chn;
			} else if (Z == 1) {
				if (ind_chn < CHSIZE) {
					BinSpec[i] = ind_chn;
					ind_chn = ind_chn + 1;
				}

			}
		}
		return BinSpec;
	}

	public static double[] BGErosion3(double[] MSBinSpec, double[] IterCoeff, double[] BGEroBinSpec) {
		// double[] datatmp = new double[BINSIZE];
		double a3 = IterCoeff[0];
		double a2 = IterCoeff[1];
		double a1 = IterCoeff[2];
		double a0 = IterCoeff[3];
		double x = 0;
		int noiter = 0;

		int MaxIter = 71; // Defined: MaxNoIter=70+1

		double[][] DataEro = new double[MaxIter][];
		for (int i = 0; i < MaxIter; i++) {
			DataEro[i] = new double[BINSIZE];
			for (int j = 0; j < BINSIZE; j++) {
				DataEro[i][j] = MSBinSpec[j];
			}
		}

		// 1st :Erosion and then save array
		double tmp;
		for (int iter = 0; iter < MaxIter - 1; iter++) {
			for (int i = 0; i < BINSIZE; i++) {
				if (i >= 4 && i <= BINSIZE - 5) {
					tmp = (DataEro[iter][i - 4] + DataEro[iter][i + 4]) / 2d;
					if (DataEro[iter][i] > tmp) {
						DataEro[iter][i] = tmp;

					}
					// DataEro[iter+1][i] = DataEro[iter][i];
				}
				DataEro[iter + 1][i] = DataEro[iter][i];
			}

		}

		// 2 step:

		double noiter1 = 0;

		// iterosion
		for (int ind = 0; ind < BINSIZE; ind++) {

			x = ind + 1;
			x = Math.log(x);

			noiter1 = (a3 * x * x * x + a2 * x * x + a1 * x + a0);
			noiter = (int) Math.round(noiter1);

			if (ind < 128)
				// noiter = (int) (0.0252 * (ind + 1) + 5.7552);
				noiter = (int) (0.115 * (ind + 1) + 5);

			if (noiter < 2)
				noiter = 2;
			if (noiter > 70)
				noiter = 70;

			BGEroBinSpec[ind] = DataEro[noiter - 1][ind];
		}
		return BGEroBinSpec;
	}

	// #endregion

	// #region BGSubtration
	// Erosion占쏙옙 background占쏙옙 subtraction占쏙옙.
	// double MSChSpec[CHSIZE], double BGEroChSpec[CHSIZE], double
	// PPChSpec[CHSIZE]

	public static double[] BGSubtration(double[] MSChSpec, double[] ReBinChSpec, double[] PPChSpec) {
		double[] spc_sub_NB = new double[CHSIZE];
		double tmp = 0;
		for (int i = 0; i < CHSIZE; i++) {
			tmp = MSChSpec[i] - ReBinChSpec[i];
			if (tmp < 0)
				tmp = 0;
			spc_sub_NB[i] = tmp;
		}

		// smooth function
		PPChSpec = Smooth_Spc(spc_sub_NB);

		// for (int i = 0; i < CHSIZE; i++)
		// {
		//
		// spc_sub_NB[i] = PPChSpec[i];
		// }
		//
		// PPChSpec = Smooth_Spc(spc_sub_NB);

		return PPChSpec;
	}

	public static Vector<NcPeak> LC_Filter(Vector<NcPeak> PeakInfo) {
		for (int i = 0; i < PeakInfo.size(); i++) {
			if (PeakInfo.get(i).NetCnt < PeakInfo.get(i).LC) {
				PeakInfo.remove(i);
				i--;
			}
		}
		return PeakInfo;
	}

	public static double[] GenDSpecrum(double[] PPChSpec, double[] reBinChSpec, double[] FWHMCoeff, double[] DChSpec,
									   Boolean isMeasurement) {

		isMeasurement = false;
		int CHSIZE = 1024;
		double[] bg_est = new double[CHSIZE];
		double[] D = new double[CHSIZE];

		bg_est = Smooth_Spc(reBinChSpec);
		double fwhm, W, A, C, S, tmp1;
		int W_half, lowchn, highchn;
		for (int i = 0; i < CHSIZE - 2; i++) {
			if (reBinChSpec[i] > 0) {
				D[i] = 0;
				DChSpec[i] = 0;

				if (i >= 0 && i < CHSIZE - 2) {
					fwhm = FWHMCoeff[0] * Math.sqrt((double) (i + 1)) + FWHMCoeff[1];
					W = fwhm;
					W_half = (int) Math.round(W / 2d);

					if (W_half > 0) {
						lowchn = i - W_half;
						highchn = i + W_half;

						if (lowchn > 0 && highchn < CHSIZE) {
							A = 0;
							C = 0;
							for (int j = lowchn; j < highchn + 1; j++) {
								A = A + PPChSpec[j];
								C = C + bg_est[j];
								if (C > 1) {
									S = Math.sqrt(C / W) * W;
									D[i] = (A - S) * (A - S) / ((C + S) * W);

									// if (D[i] < 1) D[i] = 0;

									// DChSpec[i] = D[i];
								}
							}
						}
					}
				}

				DChSpec[i] = D[i];
			}

		}
		return DChSpec;
	}

	public static double[] BGSubtration(double[] MSChSpec, double[] ReBinChSpec) {
		double[] spc_sub_NB = new double[CHSIZE];
		double tmp = 0;
		for (int i = 0; i < CHSIZE; i++) {
			tmp = MSChSpec[i] - ReBinChSpec[i];
			if (tmp < 0)
				tmp = 0;
			spc_sub_NB[i] = tmp;
		}
		// 170215 Add Smooth_Spc
		// smooth function

		for (int i = 0; i < spc_sub_NB.length; i++) {
			spc_sub_NB[i] = spc_sub_NB[i];
		}

		spc_sub_NB = Smooth_Spc(spc_sub_NB);
		spc_sub_NB = Smooth_Spc(spc_sub_NB);

		return spc_sub_NB;
	}
	// #endregion

	// #region Smooth_Spc
	// double ChSpec[CHSIZE], double ChSpecSmoo[CHSIZE]

	// public static double[] Smooth_Spc(double[] ChSpec)
	// {
	//
	// CHSIZE = ChSpec.length;
	// double[] Data = new double[CHSIZE];
	//
	// // double aval = 0.0151;
	// // double bval = 2.96474;
	//
	// double aval = 0.01390;
	// double bval = 4.19705;
	//
	//
	//// #if LABR
	//// aval = 0.00668;
	//// bval =1.90845;
	//// #endif
	//
	// int NoSmooth =1;
	// double temp_wind = Math.round(aval * CHSIZE + bval);
	//
	// for (int i = 0; i < CHSIZE; i++)
	// {
	// // Data[i] = ChSpec[i];
	// // ChSpecSmoo[i] = 0;
	// }
	//
	// double sum1 = 0;
	// int wnd = 0;
	// int wnd_half = 0;
	//
	// for (int nosmoo = 1; nosmoo <= NoSmooth; nosmoo++)
	// {
	// //if (nosmoo > 1)
	// //{
	// // for (int j = 0; j < CHSIZE; j++)
	// // {
	// // Data[j] = ChSpecSmoo[j];
	// // }
	// //}
	// for (int j = 3; j < CHSIZE - temp_wind; j++)
	// {
	// ChSpec[j] = 0;
	//
	// sum1 = 0;
	//
	// wnd = (int)Math.floor(aval * (j + 1) + bval);
	//
	// if (wnd < 0)
	// {
	// ChSpec[j] = ChSpec[j];
	// }
	// if (wnd % 2 == 0)
	// {
	// wnd = wnd + 1;
	// }
	// wnd_half = (int)Math.floor(wnd / 2d);
	//
	// for (int k = j - wnd_half; k <= j + wnd_half; k++)
	// {
	// if (k >= 0 && k < CHSIZE) sum1 = sum1 + ChSpec[k];
	// }
	//
	// ChSpec[j] = sum1 / (double)wnd;
	// }
	// }
	//
	//
	// return ChSpec;
	// }

	public static double[] Smooth_Spc(double[] ChSpec) {
		double[] Data = new double[CHSIZE];
		double[] ChSpecSmoo = new double[CHSIZE];
		double aval = 0.0151;
		double bval = 2.96474;

		int NoSmooth = 2;
		double temp_wind = Math.round(aval * CHSIZE + bval);

		double sum1 = 0;
		int wnd = 0;
		int wnd_half = 0;

		for (int j = 0; j < CHSIZE; j++) {
			Data[j] = ChSpec[j];
		}

		for (int nosmoo = 1; nosmoo <= NoSmooth; nosmoo++) {
			if (nosmoo > 1) {
				for (int j = 0; j < CHSIZE; j++) {
					Data[j] = ChSpecSmoo[j];
				}
			}
			for (int j = 3; j < CHSIZE - temp_wind; j++) {

				sum1 = 0;

				wnd = (int) Math.floor(aval * (j + 1) + bval);

				if (wnd < 0) {
					ChSpecSmoo[j] = Data[j];
				}
				if (wnd % 2 == 0) {
					wnd = wnd + 1;
				}
				wnd_half = (int) Math.floor(wnd / 2d);

				for (int k = j - wnd_half; k <= j + wnd_half; k++) {
					if (k >= 0 && k < CHSIZE)
						sum1 = sum1 + Data[k];
				}

				ChSpecSmoo[j] = sum1 / (double) wnd;
			}
		}

		return ChSpecSmoo;
	}
	// #endregion

	// #region GenDSpecrum
	// Photo peak spectrum占쏙옙占쏙옙 Peak占쏙옙 占싯삼옙占싹깍옙 占쏙옙占쏙옙 D spectrum占쏙옙 占쏙옙占쏙옙占싼댐옙.
	// double PPChSpec[CHSIZE], double BGEroChSpec[CHSIZE], double FWHMCoeff[2],
	// double DChSpec[CHSIZE]
	public static double[] GenDSpecrum(double[] PPChSpec, double[] reBinChSpec, double[] FWHMCoeff, double[] DChSpec,
									   double DspecTheshold, boolean IsOneSecMode) {

		double MaxY = NcLibrary.FindMaxValue(reBinChSpec, 0, 1024);

		double theshold = 1;
		double[] bg_est = new double[CHSIZE];
		double[] D = new double[CHSIZE];

		bg_est = Smooth_Spc(reBinChSpec);
		double fwhm, W = 0, A = 0, C = 0, S = 0, tmp1;
		int W_half, lowchn, highchn;

		String D_Str = null, A_Str = null, S_Str = null, C_Str = null, W_Str = null;

		for (int i = 0; i < CHSIZE - 2; i++) {
			// if(reBinChSpec[i]> 1 ){
			D[i] = 0;
			DChSpec[i] = 0;

			if (i >= 0 && i < CHSIZE - 2) {
				fwhm = FWHMCoeff[0] * Math.sqrt((double) (i + 1)) + FWHMCoeff[1];
				W = fwhm;
				W_half = (int) Math.round(W / 2d);

				if (W_half > 0) {
					lowchn = i - W_half;
					highchn = i + W_half;

					if (lowchn > 0 && highchn < CHSIZE) {
						A = 0;
						C = 0;
						for (int j = lowchn; j < highchn + 1; j++) {

							A = A + PPChSpec[j];
							C = C + bg_est[j];

							if (C > theshold) {
								if (IsOneSecMode) {

									S = 0;
								} else {
									// if (reBinChSpec[i] / MaxY > 0.05) {
									// S = 0;
									// } else {

									S = Math.sqrt(C / W) * W;
									// }
								}
								D[i] = (A - S) * (A - S) / ((C + S) * W);

							}
						}
					}
				}
			}

			// log scale
			tmp1 = 0;
			if (D[i] > DspecTheshold)
				// tmp1 = Math.log10(D[i]);
				tmp1 = D[i];
			if (tmp1 > 0)

				D_Str += Double.toString(tmp1) + ",";
			A_Str += Double.toString(A) + ",";
			S_Str += Double.toString(S) + ",";
			W_Str += Double.toString(W) + ",";
			C_Str += Double.toString(C) + ",";
			DChSpec[i] = tmp1;
		}

		String Sum = "\n D : " + D_Str + "\n A : " + A_Str + "\n S : " + S_Str + "\n W : " + W_Str + "\n C : " + C_Str
				+ "\n";

		// }
		return DChSpec;
	}
	// #endregion

	// #region ReturnNoPeak
	// double DChSpec[]
	public static int ReturnNoPeak(double[] DChSpec, double theshold) {
		int cnt = 0;
		double a1, a2, b1, b2;

		for (int i = 3; i < CHSIZE; i++) {
			if (DChSpec[i] > theshold) {
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
	// #endregion

	// #region FindPeak
	// double DChSpec[CHSIZE], double **PeakInfo
	public static Vector<NcPeak> FindPeak(double[] DChSpec, Vector<NcPeak> PeakInfo, double theshold) {
		int cnt = 0;
		double a1, a2, b1, b2;

		for (int i = 3; i < CHSIZE; i++) {
			if (DChSpec[i] > theshold) {
				a1 = DChSpec[i] - DChSpec[i + 1];
				a2 = DChSpec[i] - DChSpec[i - 1];

				b1 = DChSpec[i] - DChSpec[i + 2];
				b2 = DChSpec[i] - DChSpec[i - 2];

				if (a1 > 0 && a2 > 0 && b1 > 0 && b2 > 0) {
					PeakInfo.get(cnt).Peak = i;
					cnt = cnt + 1;
				}
			}
		}

		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	public static Vector<NcPeak> FindPeak(double[] DChSpec, Vector<NcPeak> PeakInfo) {
		int cnt = 0;
		double a1, a2, b1, b2;
		NcPeak peakData;
		for (int i = 3; i < CHSIZE; i++) {
			if (DChSpec[i] > Thshold_D) {
				a1 = DChSpec[i] - DChSpec[i + 1];
				a2 = DChSpec[i] - DChSpec[i - 1];

				b1 = DChSpec[i] - DChSpec[i + 2];
				b2 = DChSpec[i] - DChSpec[i - 2];

				if (a1 > 0 && a2 > 0 && b1 > 0 && b2 > 0) {
					peakData = new NcPeak();
					peakData.Channel = i;//
					PeakInfo.add(peakData);
					cnt = cnt + 1;
				}
			}
		}

		return PeakInfo;
	}



	public static Vector<NcPeak> FindPeak_Beta(double[] PPSpec,double[] DChSpec, Vector<NcPeak> PeakInfo, double[] FWHMCoeff)
	{
		//1st: Searching Peak in PP Spectrum
		int[] Peak_ch_PP = new int[100];
		int[] Peak_ch_D = new int[100];
		double a1, a2, b1, b2;

		int NoPeakPP = 0;
		int NoPeakD = 0;
		NcPeak peakData;

		for (int i = 4; i < CHSIZE - 4; i++)
		{
			//1st: Searching Peak in PP Spectrum
			if (PPSpec[i] > Thshold_D)
			{
				a1 = PPSpec[i] - PPSpec[i + 1];
				a2 = PPSpec[i] - PPSpec[i - 1];

				b1 = PPSpec[i] - PPSpec[i + 2];
				b2 = PPSpec[i] - PPSpec[i - 2];

				if (a1 > 0 && a2 > 0 && b1 > 0 && b2 > 0)
				{
					Peak_ch_PP[NoPeakPP] = i;
					NoPeakPP = NoPeakPP + 1;
				}
			}

			//2st: Searching Peak in D Spectum Spectrum
			if (DChSpec[i] > Thshold_D)
			{
				a1 = DChSpec[i] - DChSpec[i + 1];
				a2 = DChSpec[i] - DChSpec[i - 1];

				b1 = DChSpec[i] - DChSpec[i + 2];
				b2 = DChSpec[i] - DChSpec[i - 2];

				if (a1 > 0 && a2 > 0 && b1 > 0 && b2 > 0)
				{
					peakData = new NcPeak();
					peakData.Channel = i;//
					PeakInfo.add(peakData);

					Peak_ch_D[NoPeakD] = i;
					NoPeakD = NoPeakD + 1;
				}
			}

		}


		// 3rd: Adding Peak
		int PeakTemp;
		int	Flg = 0;
		double tmp;
		double fwhm;

		double Thsld;

		for (int  i = 0; i < NoPeakPP; i++)
		{
			PeakTemp = Peak_ch_PP[i];
			fwhm = FWHMCoeff[0] * Math.sqrt(PeakTemp) + FWHMCoeff[1];

			Thsld=0.3*fwhm;

			if(Thsld<=2)
			{
				Thsld=2;
			}

			Flg = 0;

			for (int  j = 0; j < NoPeakD; j++)
			{
				tmp = Math.abs(PeakTemp - Peak_ch_D[j]);

				//if (tmp<0.3*fwhm)	Flg = 1;
				if (tmp<Thsld)	Flg = 1;
			}

			if (DChSpec[PeakTemp] > Thshold_D&&Flg == 0)
			{
				peakData = new NcPeak();
				peakData.Channel = PeakTemp;//
				PeakInfo.add(peakData);
			}
		}

		return PeakInfo;
	}

	// 3Point EnergyCalibration
	public static ArrayList<PeakAnalysis> FindPeak(double[] DChSpec, ArrayList<PeakAnalysis> PeakInfo,
												   int[][] peakRoi) {
		int cnt = 0;
		double a1, a2, b1, b2;

		PeakAnalysis peakData;

		for (int i = 3; i < CHSIZE; i++) {
			if (DChSpec[i] > 1) {
				a1 = DChSpec[i] - DChSpec[i + 1];
				a2 = DChSpec[i] - DChSpec[i - 1];

				b1 = DChSpec[i] - DChSpec[i + 2];
				b2 = DChSpec[i] - DChSpec[i - 2];

				if (a1 > 0 && a2 > 0 && b1 > 0 && b2 > 0) {

					if (i > peakRoi[cnt][1] && i < peakRoi[cnt][2]) {
						peakData = new PeakAnalysis();
						peakData.Channel = i;//
						PeakInfo.add(peakData);
						cnt = cnt + 1;

						if (cnt >= 3)
							break;
					}
				}
			}
		}

		if (PeakInfo.size() != 3)
			try {

			} catch (Exception e) {
				// TODO: handle exception
			}

		return PeakInfo;
	}
	// #endregion

	// #region SearchROI
	// double DChSpec[CHSIZE], int NoPeak, double **PeakInfo
	public static Vector<NcPeak> SearchROI(double[] DChSpec, int NoPeak, Vector<NcPeak> PeakInfo) {
		int ROI_L, ROI_R;

		for (int i = 0; i < NoPeak; i++) {
			ROI_L = (int) PeakInfo.get(i).Peak;
			ROI_R = (int) PeakInfo.get(i).Peak;

			// search for left

			while (true) {
				ROI_L = ROI_L - 1;
				if (DChSpec[ROI_L] == 0)
					break;

				if (DChSpec[ROI_L] >= DChSpec[ROI_L + 1]) {
					ROI_L = ROI_L + 1;
					break;
				}
			}

			// search for right
			while (true) {
				ROI_R = ROI_R + 1;
				if (DChSpec[ROI_R] == 0)
					break;

				if (DChSpec[ROI_R] >= DChSpec[ROI_R - 1]) {
					ROI_R = ROI_R - 1;
					break;
				}
			}

			PeakInfo.get(i).ROI_Left = (int) (ROI_L);
			PeakInfo.get(i).ROI_Right = (int) (ROI_R);

			if (ROI_R > 1023) {

			}
		}

		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	public static ArrayList<PeakAnalysis> SearchROI(double[] DChSpec, int NoPeak, ArrayList<PeakAnalysis> PeakInfo) {
		int ROI_L, ROI_R;
		for (int i = 0; i < NoPeak; i++) {
			ROI_L = (int) PeakInfo.get(i).Channel;
			ROI_R = (int) PeakInfo.get(i).Channel;

			// search for left

			while (true) {
				ROI_L = ROI_L - 1;
				if (DChSpec[ROI_L] == 0)
					break;

				if (DChSpec[ROI_L] >= DChSpec[ROI_L + 1]) {
					ROI_L = ROI_L + 1;
					break;
				}
			}

			// search for right
			while (true) {
				ROI_R = ROI_R + 1;
				if (DChSpec[ROI_R] == 0)
					break;

				if (DChSpec[ROI_R] >= DChSpec[ROI_R - 1]) {
					ROI_R = ROI_R - 1;
					break;
				}
			}

			PeakInfo.get(i).ROI_L = ROI_L;
			PeakInfo.get(i).ROI_R = ROI_R;
		}

		return PeakInfo;
	}

	// #endregion

	// #region GaussianFitInit
	// double PPChSpec[CHSIZE], int NoPeak, double FWHMCoeff[], double
	// **PeakInfo
	public static Vector<NcPeak> GaussianFitInit(double[] PPChSpec, int NoPeak, double[] FWHMCoeff,
												 Vector<NcPeak> PeakInfo) {
		// weighting factor
		double[] W = new double[CHSIZE];
		double[] g = new double[CHSIZE];

		for (int i = 0; i < CHSIZE; i++) {
			if (PPChSpec[i] > 0)
				W[i] = 1d / PPChSpec[i];
			else
				W[i] = 1;

			g[i] = 0;
		}

		double peak, Sig_Expect, fwhm, mse;
		double Sig_Low, Sig_High;

		double[][] MA = new double[3][3];
		double[][] MA1 = new double[3][3];
		double[] MB = new double[3];
		double[] MC = new double[3];

		for (int i = 0; i < 3; i++) {
			// MA[i] = new double[3];
			// MA1[i] = new double[3];

			for (int j = 0; j < 3; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		double X, tmp, bgtemp;
		double ROI_L, ROI_R, B0, B1, H;
		double B11, B00, sigest, peak_est, h_est;

		for (int noid = 0; noid < NoPeak; noid++) {
			peak = PeakInfo.get(noid).Peak;
			ROI_L = PeakInfo.get(noid).ROI_Left;
			ROI_R = PeakInfo.get(noid).ROI_Right;

			fwhm = FWHMCoeff[0] * Math.sqrt(peak) + FWHMCoeff[1];
			Sig_Expect = fwhm / 2.355;

			Sig_Low = Math.round(Sig_Expect - 5);
			Sig_High = Math.round(Sig_Expect + 5);

			if (Sig_Low < 0.5)
				Sig_Low = 0.5;

			mse = (double) 1000000000000000.0;

			B11 = 0;
			B00 = 0;
			sigest = 0;
			peak_est = 0;
			h_est = 0;

			for (double idpeak = peak - 2; idpeak < peak + 2; idpeak = idpeak + 0.1) {
				for (double sig = Sig_Low; sig < Sig_High; sig = sig + 0.01) {
					// generate Gaussian

					g = Gauss1d(g, sig, idpeak, ROI_L, ROI_R);

					// reset matrix A and B
					for (int k1 = 0; k1 < 3; k1++) {
						for (int k2 = 0; k2 < 3; k2++) {
							MA[k1][k2] = 0;
						}
						MB[k1] = 0;
					}

					for (int i = (int) ROI_L; i <= ROI_R; i++) {
						X = i;
						MA[0][0] = MA[0][0] + W[i] * X * X;
						MA[0][1] = MA[0][1] + W[i] * X;
						MA[0][2] = MA[0][2] + W[i] * X * g[i];

						MA[1][0] = MA[1][0] + W[i] * X;
						MA[1][1] = MA[1][1] + W[i];
						MA[1][2] = MA[1][2] + W[i] * g[i];

						MA[2][0] = MA[2][0] + W[i] * X * g[i];
						MA[2][1] = MA[2][1] + W[i] * g[i];
						MA[2][2] = MA[2][2] + W[i] * g[i] * g[i];

						MB[0] = MB[0] + W[i] * PPChSpec[i] * X;
						MB[1] = MB[1] + W[i] * PPChSpec[i];
						MB[2] = MB[2] + W[i] * PPChSpec[i] * g[i];
					}

					MA1 = InverseMatrix(MA1, MA, 3);
					MC = MultiMatrix(MC, MA1, MB, 3);

					B1 = MC[0];
					B0 = MC[1];
					H = MC[2];

					tmp = 0;
					bgtemp = 0;
					for (int i = (int) ROI_L; i <= ROI_R; i++) {
						bgtemp = i * B1 + B0;
						tmp = tmp + W[i] * (PPChSpec[i] - g[i] * H - bgtemp) * (PPChSpec[i] - g[i] * H - bgtemp);
					}

					if (tmp < mse) {
						mse = tmp;
						B11 = B1;
						B00 = B0;
						sigest = sig;
						peak_est = idpeak;
						h_est = H;
					}
				}
			}

			PeakInfo.get(noid).sigma = sigest;
			PeakInfo.get(noid).PeakEst = peak_est;
			PeakInfo.get(noid).Height = h_est;
			PeakInfo.get(noid).BG_a = B11;
			PeakInfo.get(noid).BG_b = B00;
		}

		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	public static ArrayList<PeakAnalysis> GaussianFitInit(double[] PPChSpec, int NoPeak, double[] FWHMCoeff,
														  ArrayList<PeakAnalysis> PeakInfo, Coefficients coeff) {
		// weighting factor
		double[] W = new double[CHSIZE];
		double[] g = new double[CHSIZE];

		for (int i = 0; i < CHSIZE; i++) {
			if (PPChSpec[i] > 0)
				W[i] = 1d / PPChSpec[i];
			else
				W[i] = 1;

			g[i] = 0;
		}

		double peak, Sig_Expect, fwhm, mse;
		double Sig_Low, Sig_High;

		double[][] MA = new double[3][3];
		double[][] MA1 = new double[3][3];
		double[] MB = new double[3];
		double[] MC = new double[3];

		for (int i = 0; i < 3; i++) {
			// MA[i] = new double[3];
			// MA1[i] = new double[3];

			for (int j = 0; j < 3; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		double X, tmp, bgtemp;
		double ROI_L, ROI_R, B0, B1, H;
		double B11, B00, sigest, peak_est, h_est;

		for (int noid = 0; noid < NoPeak; noid++) {
			peak = PeakInfo.get(noid).Channel;
			ROI_L = PeakInfo.get(noid).ROI_L;
			ROI_R = PeakInfo.get(noid).ROI_R;

			fwhm = FWHMCoeff[0] * Math.sqrt(peak) + FWHMCoeff[1];
			Sig_Expect = fwhm / 2.355;

			Sig_Low = Math.round(Sig_Expect - 5);
			Sig_High = Math.round(Sig_Expect + 5);

			if (Sig_Low < 0.5)
				Sig_Low = 0.5;

			mse = (double) 1000000000000000.0;

			B11 = 0;
			B00 = 0;
			sigest = 0;
			peak_est = 0;
			h_est = 0;

			for (double idpeak = peak - 2; idpeak < peak + 2; idpeak = idpeak + 0.1) {
				for (double sig = Sig_Low; sig < Sig_High; sig = sig + 0.01) {
					// generate Gaussian

					g = Gauss1d(g, sig, idpeak, ROI_L, ROI_R);

					// reset matrix A and B
					for (int k1 = 0; k1 < 3; k1++) {
						for (int k2 = 0; k2 < 3; k2++) {
							MA[k1][k2] = 0;
						}
						MB[k1] = 0;
					}

					for (int i = (int) ROI_L; i <= ROI_R; i++) {
						X = i;
						MA[0][0] = MA[0][0] + W[i] * X * X;
						MA[0][1] = MA[0][1] + W[i] * X;
						MA[0][2] = MA[0][2] + W[i] * X * g[i];

						MA[1][0] = MA[1][0] + W[i] * X;
						MA[1][1] = MA[1][1] + W[i];
						MA[1][2] = MA[1][2] + W[i] * g[i];

						MA[2][0] = MA[2][0] + W[i] * X * g[i];
						MA[2][1] = MA[2][1] + W[i] * g[i];
						MA[2][2] = MA[2][2] + W[i] * g[i] * g[i];

						MB[0] = MB[0] + W[i] * PPChSpec[i] * X;
						MB[1] = MB[1] + W[i] * PPChSpec[i];
						MB[2] = MB[2] + W[i] * PPChSpec[i] * g[i];
					}

					MA1 = InverseMatrix(MA1, MA, 3);
					MC = MultiMatrix(MC, MA1, MB, 3);

					B1 = MC[0];
					B0 = MC[1];
					H = MC[2];

					tmp = 0;
					bgtemp = 0;
					for (int i = (int) ROI_L; i <= ROI_R; i++) {
						bgtemp = i * B1 + B0;
						tmp = tmp + W[i] * (PPChSpec[i] - g[i] * H - bgtemp) * (PPChSpec[i] - g[i] * H - bgtemp);
					}

					if (tmp < mse) {
						mse = tmp;
						B11 = B1;
						B00 = B0;
						sigest = sig;
						peak_est = idpeak;
						h_est = H;
					}
				}
			}

			PeakInfo.get(noid).Sigma = sigest;
			PeakInfo.get(noid).PeakEst = peak_est;
			PeakInfo.get(noid).Energy.Kev = coeff.get_Coefficients()[0] * peak_est * peak_est
					+ coeff.get_Coefficients()[1] * peak_est + coeff.get_Coefficients()[2];
			PeakInfo.get(noid).Height = h_est;
			PeakInfo.get(noid).BG_a = B11;
			PeakInfo.get(noid).BG_b = B00;
		}

		return PeakInfo;
	}
	// #endregion

	// #region GaussinFit
	// double PPChSpec[CHSIZE], int NoPeak, double FWHMCoeff[2], double
	// **PeakInfo
	public static double[][] GaussinFit(double[] PPChSpec, int NoPeak, double[] FWHMCoeff, double[][] PeakInfo) {
		// weighting factor
		// weighting factor
		double[] W = new double[CHSIZE];
		double[] g = new double[CHSIZE];

		for (int i = 0; i < CHSIZE; i++) {
			if (PPChSpec[i] > 0)
				W[i] = 1 / PPChSpec[i];
			else
				W[i] = 1;

			g[i] = 0;
		}

		double[][] MA = new double[3][3];
		double[][] MA1 = new double[3][3];
		double[] MB = new double[3];
		double[] MC = new double[3];

		for (int i = 0; i < 3; i++) {
			// MA[i] = new double[3];
			// MA1[i] = new double[3];

			for (int j = 0; j < 3; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		double X, tmp, bgtemp;
		double ROI_L, ROI_R, B0, B1, H;
		double B11, B00, sigest, peak_est, h_est;

		double peak, fwhm, sig, mse;

		for (int noid = 0; noid < NoPeak; noid++) {
			peak = PeakInfo[noid][0];
			ROI_L = PeakInfo[noid][1];
			ROI_R = PeakInfo[noid][2];

			mse = (double) 1000000000000000.0;
			B11 = 0;
			B00 = 0;
			sigest = 0;
			peak_est = 0;
			h_est = 0;

			for (double idpeak = peak - 2; idpeak < peak + 2; idpeak = idpeak + 0.1) {
				fwhm = FWHMCoeff[0] * Math.sqrt(idpeak) + FWHMCoeff[1];
				sig = fwhm / 2.355;

				// generate Gaussian

				g = Gauss1d(g, sig, idpeak, ROI_L, ROI_R);
				// reset matrix A and B
				for (int k1 = 0; k1 < 3; k1++) {
					for (int k2 = 0; k2 < 3; k2++) {
						MA[k1][k2] = 0;
					}
					MB[k1] = 0;
				}

				for (int i = (int) ROI_L; i < ROI_R; i++) {
					X = i;
					MA[0][0] = MA[0][0] + W[i] * X * X;
					MA[0][1] = MA[0][1] + W[i] * X;
					MA[0][2] = MA[0][2] + W[i] * X * g[i];

					MA[1][0] = MA[1][0] + W[i] * X;
					MA[1][1] = MA[1][1] + W[i];
					MA[1][2] = MA[1][2] + W[i] * g[i];

					MA[2][0] = MA[2][0] + W[i] * X * g[i];
					MA[2][1] = MA[2][1] + W[i] * g[i];
					MA[2][2] = MA[2][2] + W[i] * g[i] * g[i];

					MB[0] = MB[0] + W[i] * PPChSpec[i] * X;
					MB[1] = MB[1] + W[i] * PPChSpec[i];
					MB[2] = MB[2] + W[i] * PPChSpec[i] * g[i];
				}

				MA1 = InverseMatrix(MA1, MA, 3);
				MC = MultiMatrix(MC, MA1, MB, 3);

				B1 = MC[0];
				B0 = MC[1];
				H = MC[2];

				tmp = 0;
				bgtemp = 0;
				for (int i = (int) ROI_L; i < ROI_R; i++) {
					bgtemp = i * B1 + B0;
					tmp = tmp + W[i] * (PPChSpec[i] - g[i] * H - bgtemp) * (PPChSpec[i] - g[i] * H - bgtemp);
				}

				if (tmp < mse) {
					mse = tmp;
					B11 = B1;
					B00 = B0;
					sigest = sig;
					peak_est = idpeak;
					h_est = H;
				}
			}

			PeakInfo[noid][3] = sigest;
			PeakInfo[noid][4] = peak_est;
			PeakInfo[noid][5] = h_est;
			PeakInfo[noid][6] = B11;
			PeakInfo[noid][7] = B00;
		}

		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	public static ArrayList<PeakAnalysis> GaussinFit(double[] PPChSpec, int NoPeak, double[] FWHMCoeff,
													 ArrayList<PeakAnalysis> PeakInfo, Coefficients coeff) {
		// weighting factor
		// weighting factor
		double[] W = new double[CHSIZE];
		double[] g = new double[CHSIZE];

		for (int i = 0; i < CHSIZE; i++) {
			if (PPChSpec[i] > 0)
				W[i] = 1 / PPChSpec[i];
			else
				W[i] = 1;

			g[i] = 0;
		}

		double[][] MA = new double[3][3];
		double[][] MA1 = new double[3][3];
		double[] MB = new double[3];
		double[] MC = new double[3];

		for (int i = 0; i < 3; i++) {
			// MA[i] = new double[3];
			// MA1[i] = new double[3];

			for (int j = 0; j < 3; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		double X, tmp, bgtemp;
		double ROI_L, ROI_R, B0, B1, H;
		double B11, B00, sigest, peak_est, h_est;

		double peak, fwhm, sig, mse;

		for (int noid = 0; noid < NoPeak; noid++) {
			peak = PeakInfo.get(noid).Channel;
			ROI_L = PeakInfo.get(noid).ROI_L;
			ROI_R = PeakInfo.get(noid).ROI_R;

			mse = (double) 1000000000000000.0;
			B11 = 0;
			B00 = 0;
			sigest = 0;
			peak_est = 0;
			h_est = 0;

			for (double idpeak = peak - 2; idpeak < peak + 2; idpeak = idpeak + 0.1) {
				fwhm = FWHMCoeff[0] * Math.sqrt(idpeak) + FWHMCoeff[1];
				sig = fwhm / 2.355;

				// generate Gaussian

				g = Gauss1d(g, sig, idpeak, ROI_L, ROI_R);
				// reset matrix A and B
				for (int k1 = 0; k1 < 3; k1++) {
					for (int k2 = 0; k2 < 3; k2++) {
						MA[k1][k2] = 0;
					}
					MB[k1] = 0;
				}

				for (int i = (int) ROI_L; i < ROI_R; i++) {

					X = i;
					MA[0][0] = MA[0][0] + W[i] * X * X;
					MA[0][1] = MA[0][1] + W[i] * X;
					MA[0][2] = MA[0][2] + W[i] * X * g[i];

					MA[1][0] = MA[1][0] + W[i] * X;
					MA[1][1] = MA[1][1] + W[i];
					MA[1][2] = MA[1][2] + W[i] * g[i];

					MA[2][0] = MA[2][0] + W[i] * X * g[i];
					MA[2][1] = MA[2][1] + W[i] * g[i];
					MA[2][2] = MA[2][2] + W[i] * g[i] * g[i];

					MB[0] = MB[0] + W[i] * PPChSpec[i] * X;
					MB[1] = MB[1] + W[i] * PPChSpec[i];
					MB[2] = MB[2] + W[i] * PPChSpec[i] * g[i];
				}

				MA1 = InverseMatrix(MA1, MA, 3);
				MC = MultiMatrix(MC, MA1, MB, 3);

				B1 = MC[0];
				B0 = MC[1];
				H = MC[2];

				tmp = 0;
				bgtemp = 0;
				for (int i = (int) ROI_L; i < ROI_R; i++) {
					bgtemp = i * B1 + B0;
					tmp = tmp + W[i] * (PPChSpec[i] - g[i] * H - bgtemp) * (PPChSpec[i] - g[i] * H - bgtemp);
				}

				if (tmp < mse) {
					mse = tmp;
					B11 = B1;
					B00 = B0;
					sigest = sig;
					peak_est = idpeak;
					h_est = H;
				}
			}

			PeakInfo.get(noid).Sigma = sigest;
			PeakInfo.get(noid).PeakEst = peak_est;
			PeakInfo.get(noid).Height = h_est;
			PeakInfo.get(noid).Energy.Kev = coeff.get_Coefficients()[0] * peak_est * peak_est
					+ coeff.get_Coefficients()[1] * peak_est + coeff.get_Coefficients()[2];
			PeakInfo.get(noid).BG_a = B11;
			PeakInfo.get(noid).BG_b = B00;
		}

		return PeakInfo;
	}
	// #endregion

	// #region Gauss1d
	// double *g, double sig, double peak, double ROI_L, double ROI_R
	public static double[] Gauss1d(double[] g, double sig, double peak, double ROI_L, double ROI_R) {
		double x = 0, tmp = 0;
		for (int i = (int) ROI_L; i <= ROI_R; i++) {
			x = i;
			tmp = (x - peak) * (x - peak) / (2 * sig * sig);

			g[i] = Math.exp(-tmp);
		}

		return g;
	}
	// #endregion

	// #region InverseMatrix
	// double **A1, double **A, int n
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
	// #endregion

	// #region MultiMatrix
	// double *M, double **A, double *B, int N
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
	// #endregion

	// #region EnergyCalibration3Point
	// double **PeakInfo, double *EnergyList, int NoSize, int NoOrderPoly,
	// double *EnergyCoeff3P
	public static double[] EnergyCalibration3Point(double[][] PeakInfo, double[] EnergyList, int NoSize,
												   int NoOrderPoly, double[] EnergyCoeff3P) {
		// EnergyCoeff3P = 0;
		double a = 0, b = 0, c = 0; // y=a*x^2+b*x+c

		if (NoOrderPoly == 1) // format: y=b*x+c
		{
			a = 0;
			b = (EnergyList[1] - EnergyList[0]) / (PeakInfo[1][0] - PeakInfo[0][0]);
			c = EnergyList[1] - b * PeakInfo[1][0];
			EnergyCoeff3P[0] = b;
			EnergyCoeff3P[1] = c;
		}

		if (NoSize == 2) {
			double ch1 = PeakInfo[0][0];
			double ch2 = PeakInfo[1][0];
			double ch3 = PeakInfo[2][0];

			double erg1 = EnergyList[0];
			double erg2 = EnergyList[1];
			double erg3 = EnergyList[2];

			double[][] MA = new double[3][3];
			double[][] MA1 = new double[3][3];
			double[] MB = new double[3];
			double[] MC = new double[3];

			for (int i = 0; i < 3; i++) {
				// MA[i] = new double[3];
				// MA1[i] = new double[3];

				for (int j = 0; j < 3; j++) {
					MA[i][j] = 0;
					MA1[i][j] = 0;
				}

				MB[i] = 0;
				MC[i] = 0;
			}

			MA[0][0] = ch1 * ch1;
			MA[0][1] = ch1;
			MA[0][2] = 1;
			MA[1][0] = ch2 * ch2;
			MA[1][1] = ch2;
			MA[1][2] = 1;
			MA[2][0] = ch3 * ch3;
			MA[2][1] = ch3;
			MA[2][2] = 1;

			MB[0] = erg1;
			MB[1] = erg2;
			MB[2] = erg3;

			MA1 = InverseMatrix(MA1, MA, 3);
			MC = MultiMatrix(MC, MA1, MB, 3);

			a = MC[0];
			b = MC[1];
			c = MC[2];

			EnergyCoeff3P[0] = a;
			EnergyCoeff3P[1] = b;
			EnergyCoeff3P[2] = c;
		}

		return EnergyCoeff3P;
	}

	// C# 占쏙옙占쏙옙
	public static double[] EnergyCalibration3Point(ArrayList<PeakAnalysis> PeakInfo, int[][] trueEnergys) {
		// EnergyCoeff3P = 0;
		double a = 0, b = 0, c = 0; // y=a*x^2+b*x+c

		double ch1 = PeakInfo.get(0).Channel;
		double ch2 = PeakInfo.get(1).Channel;
		double ch3 = PeakInfo.get(2).Channel;

		double erg1 = trueEnergys[0][0];
		double erg2 = trueEnergys[1][0];
		double erg3 = trueEnergys[2][0];

		double[][] MA = new double[3][3];
		double[][] MA1 = new double[3][3];
		double[] MB = new double[3];
		double[] MC = new double[3];

		for (int i = 0; i < 3; i++) {
			// MA[i] = new double[3];
			// MA1[i] = new double[3];

			for (int j = 0; j < 3; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		MA[0][0] = ch1 * ch1;
		MA[0][1] = ch1;
		MA[0][2] = 1;
		MA[1][0] = ch2 * ch2;
		MA[1][1] = ch2;
		MA[1][2] = 1;
		MA[2][0] = ch3 * ch3;
		MA[2][1] = ch3;
		MA[2][2] = 1;

		MB[0] = erg1;
		MB[1] = erg2;
		MB[2] = erg3;

		MA1 = InverseMatrix(MA1, MA, 3);
		MC = MultiMatrix(MC, MA1, MB, 3);

		a = MC[0];
		b = MC[1];
		c = MC[2];

		double[] EnergyCoeff3P = new double[3];

		EnergyCoeff3P[0] = a;
		EnergyCoeff3P[1] = b;
		EnergyCoeff3P[2] = c;

		return EnergyCoeff3P;
	}

	// #endregion

	// #region EnergyCalibrationNPoint
	// double **PeakInfo, double *EnergyList, int NoPeakCal, int NoOrderPolyCal,
	// double *EnergyCoeffNP
	public static double[] EnergyCalibrationNPoint(double[][] PeakInfo, double[] EnergyList, int NoPeakCal,
												   int NoOrderPolyCal, double[] EnergyCoeffNP) {
		// EnergyCoeffNP = 0;

		double[][] MA = new double[NoOrderPolyCal + 1][NoOrderPolyCal + 1];
		double[][] MA1 = new double[NoOrderPolyCal + 1][NoOrderPolyCal + 1];
		double[] MB = new double[NoOrderPolyCal + 1];
		double[] MC = new double[NoOrderPolyCal + 1];

		for (int i = 0; i < NoOrderPolyCal + 1; i++) {
			// MA[i] = new double[NoOrderPolyCal + 1];
			// MA1[i] = new double[NoOrderPolyCal + 1];

			for (int j = 0; j < NoOrderPolyCal + 1; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		if (NoOrderPolyCal == 1) // erg=a*ch+b
		{
			double sumx0, sumx1, sumx2, sumy0, sumy1;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumy0 = 0;
			sumy1 = 0;
			for (int i = 0; i < NoPeakCal; i++) {
				sumx0 = sumx0 + Math.pow(PeakInfo[i][0], 0.0);
				sumx1 = sumx1 + Math.pow(PeakInfo[i][0], 1.0);
				sumx2 = sumx2 + Math.pow(PeakInfo[i][0], 2.0);

				sumy0 = sumy0 + EnergyList[i];
				sumy1 = sumy1 + EnergyList[i] * PeakInfo[i][0];
			}

			MA[0][0] = sumx1;
			MA[0][1] = sumx0;
			MA[1][0] = sumx2;
			MA[1][0] = sumx1;

			MB[0] = sumy0;
			MB[1] = sumy1;

			MA1 = InverseMatrix(MA1, MA, 2);
			MC = MultiMatrix(MC, MA1, MB, 2);

			EnergyCoeffNP[0] = MC[0]; // coeficient: a
			EnergyCoeffNP[1] = MC[1]; // coeficient: b
		} else if (NoOrderPolyCal == 2) // erg=a*ch^2+b*ch+c
		{

			double sumx0, sumx1, sumx2, sumx3, sumx4, sumy0, sumy1, sumy2;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;

			for (int i = 0; i < NoPeakCal; i++) {
				sumx0 = sumx0 + Math.pow(PeakInfo[i][0], 0.0);
				sumx1 = sumx1 + Math.pow(PeakInfo[i][0], 1.0);
				sumx2 = sumx2 + Math.pow(PeakInfo[i][0], 2.0);
				sumx3 = sumx3 + Math.pow(PeakInfo[i][0], 3.0);
				sumx4 = sumx4 + Math.pow(PeakInfo[i][0], 4.0);

				sumy0 = sumy0 + EnergyList[i];
				sumy1 = sumy1 + EnergyList[i] * Math.pow(PeakInfo[i][0], 1.0);
				sumy2 = sumy2 + EnergyList[i] * Math.pow(PeakInfo[i][0], 2.0);
			}

			MA[0][0] = sumx2;
			MA[0][1] = sumx1;
			MA[0][2] = sumx0;
			MA[1][0] = sumx3;
			MA[1][1] = sumx2;
			MA[1][2] = sumx1;
			MA[2][0] = sumx4;
			MA[2][1] = sumx3;
			MA[2][2] = sumx2;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;

			MA1 = InverseMatrix(MA1, MA, 3);
			MC = MultiMatrix(MC, MA1, MB, 3);

			EnergyCoeffNP[0] = MC[0]; // coeficient: a
			EnergyCoeffNP[1] = MC[1]; // coeficient: b
			EnergyCoeffNP[2] = MC[2]; // coeficient: c
		} else if (NoOrderPolyCal == 3) // erg=a*ch^3+b*ch^2+c*ch+d
		{
			double sumx0, sumx1, sumx2, sumx3, sumx4, sumx5, sumx6, sumy0, sumy1, sumy2, sumy3;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumx5 = 0;
			sumx6 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;
			sumy3 = 0;

			for (int i = 0; i < NoPeakCal; i++) {
				sumx0 = sumx0 + Math.pow(PeakInfo[i][0], 0.0);
				sumx1 = sumx1 + Math.pow(PeakInfo[i][0], 1.0);
				sumx2 = sumx2 + Math.pow(PeakInfo[i][0], 2.0);
				sumx3 = sumx3 + Math.pow(PeakInfo[i][0], 3.0);
				sumx4 = sumx4 + Math.pow(PeakInfo[i][0], 4.0);
				sumx5 = sumx5 + Math.pow(PeakInfo[i][0], 5.0);
				sumx6 = sumx6 + Math.pow(PeakInfo[i][0], 6.0);

				sumy0 = sumy0 + EnergyList[i];
				sumy1 = sumy1 + EnergyList[i] * Math.pow(PeakInfo[i][0], 1.0);
				sumy2 = sumy2 + EnergyList[i] * Math.pow(PeakInfo[i][0], 2.0);
				sumy3 = sumy3 + EnergyList[i] * Math.pow(PeakInfo[i][0], 3.0);
			}

			MA[0][0] = sumx3;
			MA[0][1] = sumx2;
			MA[0][2] = sumx1;
			MA[0][3] = sumx0;
			MA[1][0] = sumx4;
			MA[1][1] = sumx3;
			MA[1][2] = sumx2;
			MA[1][3] = sumx1;
			MA[2][0] = sumx5;
			MA[2][1] = sumx4;
			MA[2][2] = sumx3;
			MA[2][3] = sumx2;
			MA[3][0] = sumx6;
			MA[3][1] = sumx5;
			MA[3][2] = sumx4;
			MA[3][3] = sumx3;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;
			MB[3] = sumy2;

			MA1 = InverseMatrix(MA1, MA, 4);
			MC = MultiMatrix(MC, MA1, MB, 4);

			EnergyCoeffNP[0] = MC[0]; // coeficient: a
			EnergyCoeffNP[1] = MC[1]; // coeficient: b
			EnergyCoeffNP[2] = MC[2]; // coeficient: c
			EnergyCoeffNP[3] = MC[3]; // coeficient: d
		} else if (NoOrderPolyCal == 4) // erg=a*ch^4+b*ch^3+c*ch^2+d*ch+e
		{
			double sumx0, sumx1, sumx2, sumx3, sumx4, sumx5, sumx6, sumx7, sumx8, sumy0, sumy1, sumy2, sumy3, sumy4;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumx5 = 0;
			sumx6 = 0;
			sumx7 = 0;
			sumx8 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;
			sumy3 = 0;
			sumy4 = 0;

			for (int i = 0; i < NoPeakCal; i++) {
				sumx0 = sumx0 + Math.pow(PeakInfo[i][0], 0.0);
				sumx1 = sumx1 + Math.pow(PeakInfo[i][0], 1.0);
				sumx2 = sumx2 + Math.pow(PeakInfo[i][0], 2.0);
				sumx3 = sumx3 + Math.pow(PeakInfo[i][0], 3.0);
				sumx4 = sumx4 + Math.pow(PeakInfo[i][0], 4.0);
				sumx5 = sumx5 + Math.pow(PeakInfo[i][0], 5.0);
				sumx6 = sumx6 + Math.pow(PeakInfo[i][0], 6.0);
				sumx7 = sumx7 + Math.pow(PeakInfo[i][0], 7.0);
				sumx8 = sumx8 + Math.pow(PeakInfo[i][0], 8.0);

				sumy0 = sumy0 + EnergyList[i];
				sumy1 = sumy1 + EnergyList[i] * Math.pow(PeakInfo[i][0], 1.0);
				sumy2 = sumy2 + EnergyList[i] * Math.pow(PeakInfo[i][0], 2.0);
				sumy3 = sumy3 + EnergyList[i] * Math.pow(PeakInfo[i][0], 3.0);
				sumy4 = sumy4 + EnergyList[i] * Math.pow(PeakInfo[i][0], 4.0);
			}

			MA[0][0] = sumx4;
			MA[0][1] = sumx3;
			MA[0][2] = sumx2;
			MA[0][3] = sumx1;
			MA[0][4] = sumx0;
			MA[1][0] = sumx5;
			MA[2][1] = sumx4;
			MA[1][2] = sumx3;
			MA[1][3] = sumx2;
			MA[1][4] = sumx1;
			MA[2][0] = sumx6;
			MA[2][1] = sumx5;
			MA[2][2] = sumx4;
			MA[2][3] = sumx3;
			MA[2][4] = sumx2;
			MA[3][0] = sumx7;
			MA[3][1] = sumx6;
			MA[3][2] = sumx5;
			MA[3][3] = sumx4;
			MA[3][4] = sumx3;
			MA[4][0] = sumx8;
			MA[4][1] = sumx7;
			MA[5][2] = sumx6;
			MA[4][3] = sumx5;
			MA[4][4] = sumx4;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;
			MB[3] = sumy2;
			MB[4] = sumy3;

			MA1 = InverseMatrix(MA1, MA, 5);
			MC = MultiMatrix(MC, MA1, MB, 5);

			EnergyCoeffNP[0] = MC[0]; // coeficient: a
			EnergyCoeffNP[1] = MC[1]; // coeficient: b
			EnergyCoeffNP[2] = MC[2]; // coeficient: c
			EnergyCoeffNP[3] = MC[3]; // coeficient: d
			EnergyCoeffNP[4] = MC[4]; // coeficient: e
		}

		return EnergyCoeffNP;
	}
	// #endregion

	// #region FWHMCalibration
	// double **PeakInfo, int NoPeakFWHM, int NoOrderPolyFWHM, double
	// FWHMCoeff[2]
	public static double[] FWHMCalibration(Vector<NcPeak> PeakInfo, int NoPeakFWHM, int NoOrderPolyFWHM,
										   double[] FWHMCoeff) {
		double[] x = new double[NoPeakFWHM];
		double[] y = new double[NoPeakFWHM];

		int NoTruePeak = 0;

		double tmp;
		for (int i = 0; i < NoPeakFWHM; i++) {
			// if (PeakInfo.get(i).Used_for_Boolen == 1.0) {
			tmp = PeakInfo.get(i).Peak;
			x[NoTruePeak] = Math.sqrt(tmp);
			y[NoTruePeak] = PeakInfo.get(i).sigma * 2.355; // resolution
			NoTruePeak = NoTruePeak + 1;
			// }

		}
		double[][] MA = new double[2][2];
		double[][] MA1 = new double[2][2];
		double[] MB = new double[2];
		double[] MC = new double[2];

		for (int i = 0; i < 2; i++) {
			// MA[i] = new double[2];
			// MA1[i] = new double[2];

			for (int j = 0; j < 2; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		double sumx0, sumx1, sumx2, sumy0, sumy1;

		sumx0 = 0;
		sumx1 = 0;
		sumx2 = 0;
		sumy0 = 0;
		sumy1 = 0;
		for (int i = 0; i < NoTruePeak; i++) {
			sumx0 = sumx0 + Math.pow(x[i], 0.0);
			sumx1 = sumx1 + Math.pow(x[i], 1.0);
			sumx2 = sumx2 + Math.pow(x[i], 2.0);

			sumy0 = sumy0 + y[i];
			sumy1 = sumy1 + y[i] * x[i];
		}

		MA[0][0] = sumx1;
		MA[0][1] = sumx0;
		MA[1][0] = sumx2;
		MA[1][1] = sumx1;

		MB[0] = sumy0;
		MB[1] = sumy1;

		MA1 = InverseMatrix(MA1, MA, 2);
		MC = MultiMatrix(MC, MA1, MB, 2);

		FWHMCoeff[0] = MC[0]; // coeficient: a
		FWHMCoeff[1] = MC[1]; // coeficient: b

		return FWHMCoeff;
	}

	// C# 占쏙옙占쏙옙
	public static double[] FWHMCalibration(ArrayList<PeakAnalysis> PeakInfo, int NoPeakFWHM, double[] FWHMCoeff) {
		double[] x = new double[NoPeakFWHM];
		double[] y = new double[NoPeakFWHM];

		int NoTruePeak = 0;

		double tmp;
		for (int i = 0; i < NoPeakFWHM; i++) {
			if (PeakInfo.get(i).Used == true) {
				tmp = PeakInfo.get(i).PeakEst;
				x[NoTruePeak] = Math.sqrt(tmp);
				y[NoTruePeak] = PeakInfo.get(i).Sigma * 2.355; // resolution
				NoTruePeak = NoTruePeak + 1;
			}
		}
		double[][] MA = new double[2][2];
		double[][] MA1 = new double[2][2];
		double[] MB = new double[2];
		double[] MC = new double[2];

		for (int i = 0; i < 2; i++) {
			// MA[i] = new double[2];
			// MA1[i] = new double[2];

			for (int j = 0; j < 2; j++) {
				MA[i][j] = 0;
				MA1[i][j] = 0;
			}

			MB[i] = 0;
			MC[i] = 0;
		}

		double sumx0, sumx1, sumx2, sumy0, sumy1;

		sumx0 = 0;
		sumx1 = 0;
		sumx2 = 0;
		sumy0 = 0;
		sumy1 = 0;
		for (int i = 0; i < NoTruePeak; i++) {
			sumx0 = sumx0 + Math.pow(x[i], 0.0);
			sumx1 = sumx1 + Math.pow(x[i], 1.0);
			sumx2 = sumx2 + Math.pow(x[i], 2.0);

			sumy0 = sumy0 + y[i];
			sumy1 = sumy1 + y[i] * x[i];
		}

		MA[0][0] = sumx1;
		MA[0][1] = sumx0;
		MA[1][0] = sumx2;
		MA[1][1] = sumx1;

		MB[0] = sumy0;
		MB[1] = sumy1;

		MA1 = InverseMatrix(MA1, MA, 2);
		MC = MultiMatrix(MC, MA1, MB, 2);

		FWHMCoeff[0] = MC[0]; // coeficient: a
		FWHMCoeff[1] = MC[1]; // coeficient: b

		return FWHMCoeff;
	}
	// #endregion

	// #region EfficiencyFitting

	// double **PeakInfo, double **RefCRMInfo_CurrentAct, double MeasuredTime,
	// int NoPeakEff, int NoOrderPolyEff, double *EffCoeff
	public static double[] EfficiencyFitting(Vector<NcPeak> PeakInfo, double MeasuredTime, int NoPeakEff,
											 int NoOrderPolyEff, double[] EffCoeff) {

		if (NoOrderPolyEff == 4) {
			double[] x = new double[NoPeakEff];
			double[] y = new double[NoPeakEff];

			int NoTruePeak = 0;

			double tmp;
			for (int i = 0; i < NoPeakEff; i++) {
				if (PeakInfo.get(i).Used_for_Boolen == 1.0) {
					tmp = PeakInfo.get(i).PeakEst;

					x[NoTruePeak] = Math.log(tmp);
					tmp = PeakInfo.get(i).NetCnt / PeakInfo.get(i).True_Current_Activity_Bg / PeakInfo.get(i).BR_Factor
							/ MeasuredTime;

					y[NoTruePeak] = Math.log(tmp);

					NoTruePeak = NoTruePeak + 1;
				}

			}

			// fitting
			double sumx0, sumx1, sumx2, sumx3, sumx4, sumx5, sumx6, sumx7, sumx8, sumy0, sumy1, sumy2, sumy3, sumy4;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumx5 = 0;
			sumx6 = 0;
			sumx7 = 0;
			sumx8 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;
			sumy3 = 0;
			sumy4 = 0;

			for (int i = 0; i < NoTruePeak; i++) {
				sumx0 = sumx0 + Math.pow(x[i], 0.0);
				sumx1 = sumx1 + Math.pow(x[i], 1.0);
				sumx2 = sumx2 + Math.pow(x[i], 2.0);
				sumx3 = sumx3 + Math.pow(x[i], 3.0);
				sumx4 = sumx4 + Math.pow(x[i], 4.0);
				sumx5 = sumx5 + Math.pow(x[i], 5.0);
				sumx6 = sumx6 + Math.pow(x[i], 6.0);
				sumx7 = sumx7 + Math.pow(x[i], 7.0);
				sumx8 = sumx8 + Math.pow(x[i], 8.0);

				sumy0 = sumy0 + y[i];
				sumy1 = sumy1 + y[i] * Math.pow(x[i], 1.0);
				sumy2 = sumy2 + y[i] * Math.pow(x[i], 2.0);
				sumy3 = sumy3 + y[i] * Math.pow(x[i], 3.0);
				sumy4 = sumy4 + y[i] * Math.pow(x[i], 4.0);
			}

			double[][] MA = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[][] MA1 = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[] MB = new double[NoOrderPolyEff + 1];
			double[] MC = new double[NoOrderPolyEff + 1];

			for (int i = 0; i < NoOrderPolyEff + 1; i++) {
				// MA[i] = new double[NoOrderPolyEff + 1];
				// MA1[i] = new double[NoOrderPolyEff + 1];

				for (int j = 0; j < NoOrderPolyEff + 1; j++) {
					MA[i][j] = 0;
					MA1[i][j] = 0;
				}

				MB[i] = 0;
				MC[i] = 0;
			}

			MA[0][0] = sumx4;
			MA[0][1] = sumx3;
			MA[0][2] = sumx2;
			MA[0][3] = sumx1;
			MA[0][4] = sumx0;
			MA[1][0] = sumx5;
			MA[1][1] = sumx4;
			MA[1][2] = sumx3;
			MA[1][3] = sumx2;
			MA[1][4] = sumx1;
			MA[2][0] = sumx6;
			MA[2][1] = sumx5;
			MA[2][2] = sumx4;
			MA[2][3] = sumx3;
			MA[2][4] = sumx2;
			MA[3][0] = sumx7;
			MA[3][1] = sumx6;
			MA[3][2] = sumx5;
			MA[3][3] = sumx4;
			MA[3][4] = sumx3;
			MA[4][0] = sumx8;
			MA[4][1] = sumx7;
			MA[4][2] = sumx6;
			MA[4][3] = sumx5;
			MA[4][4] = sumx4;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;
			MB[3] = sumy3;
			MB[4] = sumy4;

			MA1 = InverseMatrix(MA1, MA, 5);
			MC = MultiMatrix(MC, MA1, MB, 5);

			EffCoeff[0] = MC[0]; // coeficient: a
			EffCoeff[1] = MC[1]; // coeficient: b
			EffCoeff[2] = MC[2]; // coeficient: c
			EffCoeff[3] = MC[3]; // coeficient: d
			EffCoeff[4] = MC[4]; // coeficient: e
		}

		if (NoOrderPolyEff == 3) {

			double[] x = new double[NoPeakEff];
			double[] y = new double[NoPeakEff];

			int NoTruePeak = 0;

			double tmp;
			for (int i = 0; i < NoPeakEff; i++) {
				if (PeakInfo.get(i).Used_for_Boolen == 1.0) {
					tmp = PeakInfo.get(i).PeakEst;

					x[NoTruePeak] = Math.log(tmp);

					tmp = PeakInfo.get(i).NetCnt / PeakInfo.get(i).True_Current_Activity_Bg / PeakInfo.get(i).BR_Factor
							/ MeasuredTime;

					y[NoTruePeak] = Math.log(tmp);

					NoTruePeak = NoTruePeak + 1;
				}
			}

			// fitting
			double sumx0, sumx1, sumx2, sumx3, sumx4, sumx5, sumx6, sumx7, sumx8, sumy0, sumy1, sumy2, sumy3;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumx5 = 0;
			sumx6 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;
			sumy3 = 0;

			for (int i = 0; i < NoTruePeak; i++) {
				sumx0 = sumx0 + Math.pow(x[i], 0.0);
				sumx1 = sumx1 + Math.pow(x[i], 1.0);
				sumx2 = sumx2 + Math.pow(x[i], 2.0);
				sumx3 = sumx3 + Math.pow(x[i], 3.0);
				sumx4 = sumx4 + Math.pow(x[i], 4.0);
				sumx5 = sumx5 + Math.pow(x[i], 5.0);
				sumx6 = sumx6 + Math.pow(x[i], 6.0);

				sumy0 = sumy0 + y[i];
				sumy1 = sumy1 + y[i] * Math.pow(x[i], 1.0);
				sumy2 = sumy2 + y[i] * Math.pow(x[i], 2.0);
				sumy3 = sumy3 + y[i] * Math.pow(x[i], 3.0);
			}

			double[][] MA = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[][] MA1 = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[] MB = new double[NoOrderPolyEff + 1];
			double[] MC = new double[NoOrderPolyEff + 1];

			for (int i = 0; i < NoOrderPolyEff + 1; i++) {
				// MA[i] = new double[NoOrderPolyEff + 1];
				// MA1[i] = new double[NoOrderPolyEff + 1];

				for (int j = 0; j < NoOrderPolyEff + 1; j++) {
					MA[i][j] = 0;
					MA1[i][j] = 0;
				}

				MB[i] = 0;
				MC[i] = 0;
			}

			MA[0][0] = sumx3;
			MA[0][1] = sumx2;
			MA[0][2] = sumx1;
			MA[0][3] = sumx0;
			MA[1][0] = sumx4;
			MA[1][1] = sumx3;
			MA[1][2] = sumx2;
			MA[1][3] = sumx1;
			MA[2][0] = sumx5;
			MA[2][1] = sumx4;
			MA[2][2] = sumx3;
			MA[2][3] = sumx2;
			MA[3][0] = sumx6;
			MA[3][1] = sumx5;
			MA[3][2] = sumx4;
			MA[3][3] = sumx3;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;
			MB[3] = sumy3;

			MA1 = InverseMatrix(MA1, MA, 4);
			MC = MultiMatrix(MC, MA1, MB, 4);

			EffCoeff[1] = MC[0]; // coeficient: a
			EffCoeff[2] = MC[1]; // coeficient: b
			EffCoeff[3] = MC[2]; // coeficient: c
			EffCoeff[4] = MC[3]; // coeficient: d
		}

		return EffCoeff;
	}

	// C# 占쏙옙占쏙옙 占쏙옙 占싹븝옙 占쏙옙占쏙옙 占쏙옙占쏙옙
	public static double[] EfficiencyFitting(ArrayList<PeakAnalysis> PeakInfo, double MeasuredTime, int NoPeakEff,
											 int NoOrderPolyEff, double[] EffCoeff) {

		if (NoOrderPolyEff == 4) {
			double[] x = new double[NoPeakEff];
			double[] y = new double[NoPeakEff];

			int NoTruePeak = 0;

			double tmp;
			for (int i = 0; i < NoPeakEff; i++) {
				if (PeakInfo.get(i).Used == true) {
					// tmp = PeakInfo[i].PeakEst;
					// tmp = PeakInfo[i].Energy.Kev;
					// PeakInfo[noid].Energy.Kev
					tmp = PeakInfo.get(i).TrueEnergy;
					x[NoTruePeak] = Math.log(tmp);

					tmp = PeakInfo.get(i).NetCount / PeakInfo.get(i).TrueActivity / PeakInfo.get(i).BRFactor
							/ MeasuredTime;// netcnt / activity / br
					// /MeasuredTime

					y[NoTruePeak] = Math.log(tmp);

					NoTruePeak = NoTruePeak + 1;

				}

			}

			// fitting
			double sumx0, sumx1, sumx2, sumx3, sumx4, sumx5, sumx6, sumx7, sumx8, sumy0, sumy1, sumy2, sumy3, sumy4;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumx5 = 0;
			sumx6 = 0;
			sumx7 = 0;
			sumx8 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;
			sumy3 = 0;
			sumy4 = 0;

			for (int i = 0; i < NoTruePeak; i++) {
				sumx0 = sumx0 + Math.pow(x[i], 0.0);
				sumx1 = sumx1 + Math.pow(x[i], 1.0);
				sumx2 = sumx2 + Math.pow(x[i], 2.0);
				sumx3 = sumx3 + Math.pow(x[i], 3.0);
				sumx4 = sumx4 + Math.pow(x[i], 4.0);
				sumx5 = sumx5 + Math.pow(x[i], 5.0);
				sumx6 = sumx6 + Math.pow(x[i], 6.0);
				sumx7 = sumx7 + Math.pow(x[i], 7.0);
				sumx8 = sumx8 + Math.pow(x[i], 8.0);

				sumy0 = sumy0 + y[i];
				sumy1 = sumy1 + y[i] * Math.pow(x[i], 1.0);
				sumy2 = sumy2 + y[i] * Math.pow(x[i], 2.0);
				sumy3 = sumy3 + y[i] * Math.pow(x[i], 3.0);
				sumy4 = sumy4 + y[i] * Math.pow(x[i], 4.0);
			}

			double[][] MA = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[][] MA1 = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[] MB = new double[NoOrderPolyEff + 1];
			double[] MC = new double[NoOrderPolyEff + 1];

			for (int i = 0; i < NoOrderPolyEff + 1; i++) {
				// MA[i] = new double[NoOrderPolyEff + 1];
				// MA1[i] = new double[NoOrderPolyEff + 1];

				for (int j = 0; j < NoOrderPolyEff + 1; j++) {
					MA[i][j] = 0;
					MA1[i][j] = 0;
				}

				MB[i] = 0;
				MC[i] = 0;
			}

			MA[0][0] = sumx4;
			MA[0][1] = sumx3;
			MA[0][2] = sumx2;
			MA[0][3] = sumx1;
			MA[0][4] = sumx0;
			MA[1][0] = sumx5;
			MA[1][1] = sumx4;
			MA[1][2] = sumx3;
			MA[1][3] = sumx2;
			MA[1][4] = sumx1;
			MA[2][0] = sumx6;
			MA[2][1] = sumx5;
			MA[2][2] = sumx4;
			MA[2][3] = sumx3;
			MA[2][4] = sumx2;
			MA[3][0] = sumx7;
			MA[3][1] = sumx6;
			MA[3][2] = sumx5;
			MA[3][3] = sumx4;
			MA[3][4] = sumx3;
			MA[4][0] = sumx8;
			MA[4][1] = sumx7;
			MA[4][2] = sumx6;
			MA[4][3] = sumx5;
			MA[4][4] = sumx4;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;
			MB[3] = sumy3;
			MB[4] = sumy4;

			MA1 = InverseMatrix(MA1, MA, 5);
			MC = MultiMatrix(MC, MA1, MB, 5);

			EffCoeff[0] = MC[0]; // coeficient: a
			EffCoeff[1] = MC[1]; // coeficient: b
			EffCoeff[2] = MC[2]; // coeficient: c
			EffCoeff[3] = MC[3]; // coeficient: d
			EffCoeff[4] = MC[4]; // coeficient: e
		}

		if (NoOrderPolyEff == 3) {

			double[] x = new double[NoPeakEff];
			double[] y = new double[NoPeakEff];

			int NoTruePeak = 0;

			double tmp;
			for (int i = 0; i < NoPeakEff; i++) {
				if (PeakInfo.get(i).Used == true) {
					// tmp = PeakInfo[i].PeakEst;
					tmp = PeakInfo.get(i).TrueEnergy;
					x[NoTruePeak] = Math.log(tmp);

					tmp = PeakInfo.get(i).NetCount / PeakInfo.get(i).TrueActivity / PeakInfo.get(i).BRFactor
							/ MeasuredTime;// netcnt / activity / br
					// /MeasuredTime

					y[NoTruePeak] = Math.log(tmp);

					NoTruePeak = NoTruePeak + 1;
				}
			}

			// fitting
			double sumx0, sumx1, sumx2, sumx3, sumx4, sumx5, sumx6, sumx7, sumx8, sumy0, sumy1, sumy2, sumy3;

			sumx0 = 0;
			sumx1 = 0;
			sumx2 = 0;
			sumx3 = 0;
			sumx4 = 0;
			sumx5 = 0;
			sumx6 = 0;
			sumy0 = 0;
			sumy1 = 0;
			sumy2 = 0;
			sumy3 = 0;

			for (int i = 0; i < NoTruePeak; i++) {
				sumx0 = sumx0 + Math.pow(x[i], 0.0);
				sumx1 = sumx1 + Math.pow(x[i], 1.0);
				sumx2 = sumx2 + Math.pow(x[i], 2.0);
				sumx3 = sumx3 + Math.pow(x[i], 3.0);
				sumx4 = sumx4 + Math.pow(x[i], 4.0);
				sumx5 = sumx5 + Math.pow(x[i], 5.0);
				sumx6 = sumx6 + Math.pow(x[i], 6.0);

				sumy0 = sumy0 + y[i];
				sumy1 = sumy1 + y[i] * Math.pow(x[i], 1.0);
				sumy2 = sumy2 + y[i] * Math.pow(x[i], 2.0);
				sumy3 = sumy3 + y[i] * Math.pow(x[i], 3.0);
			}

			double[][] MA = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[][] MA1 = new double[NoOrderPolyEff + 1][NoOrderPolyEff + 1];
			double[] MB = new double[NoOrderPolyEff + 1];
			double[] MC = new double[NoOrderPolyEff + 1];

			for (int i = 0; i < NoOrderPolyEff + 1; i++) {
				// MA[i] = new double[NoOrderPolyEff + 1];
				// MA1[i] = new double[NoOrderPolyEff + 1];

				for (int j = 0; j < NoOrderPolyEff + 1; j++) {
					MA[i][j] = 0;
					MA1[i][j] = 0;
				}

				MB[i] = 0;
				MC[i] = 0;
			}

			MA[0][0] = sumx3;
			MA[0][1] = sumx2;
			MA[0][2] = sumx1;
			MA[0][3] = sumx0;
			MA[1][0] = sumx4;
			MA[1][1] = sumx3;
			MA[1][2] = sumx2;
			MA[1][3] = sumx1;
			MA[2][0] = sumx5;
			MA[2][1] = sumx4;
			MA[2][2] = sumx3;
			MA[2][3] = sumx2;
			MA[3][0] = sumx6;
			MA[3][1] = sumx5;
			MA[3][2] = sumx4;
			MA[3][3] = sumx3;

			MB[0] = sumy0;
			MB[1] = sumy1;
			MB[2] = sumy2;
			MB[3] = sumy3;

			MA1 = InverseMatrix(MA1, MA, 4);
			MC = MultiMatrix(MC, MA1, MB, 4);

			EffCoeff[0] = MC[0]; // coeficient: a
			EffCoeff[1] = MC[1]; // coeficient: b
			EffCoeff[2] = MC[2]; // coeficient: c
			EffCoeff[3] = MC[3]; // coeficient: d
		}

		return EffCoeff;
	}

	// #endregion

	// #region NetCount //Peak ROI net count 占쏙옙占�
	// double **PeakInfo, int NoPeak, double FWHMCoeff[2]
	public static Vector<NcPeak> NetCount(Vector<NcPeak> PeakInfo, int NoPeak, double[] FWHMCoeff) {
		double pi = 3.1415926535897932384626433832795;
		double x, fit_fwhm, netcnt = 0, H = 0;

		for (int i = 0; i < NoPeak; i++) {
			x = PeakInfo.get(i).Peak;
			fit_fwhm = FWHMCoeff[0] * Math.sqrt(x) + FWHMCoeff[1];
			H = PeakInfo.get(i).Height;
			netcnt = Math.sqrt(pi) * H * Math.sqrt(2) * (fit_fwhm / 2.355);
			PeakInfo.get(i).NetCnt = netcnt;
		}
		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	public static ArrayList<PeakAnalysis> NetCount(ArrayList<PeakAnalysis> PeakInfo, int NoPeak, double[] FWHMCoeff) {
		double pi = 3.1415926535897932384626433832795;
		double x, fit_fwhm, netcnt = 0, H = 0;

		for (int i = 0; i < NoPeak; i++) {
			x = PeakInfo.get(i).PeakEst;
			fit_fwhm = FWHMCoeff[0] * Math.sqrt(x) + FWHMCoeff[1];
			H = PeakInfo.get(i).Height;
			netcnt = Math.sqrt(pi) * H * Math.sqrt(2) * (fit_fwhm / 2.355);
			PeakInfo.get(i).NetCount = netcnt;
		}
		return PeakInfo;
	}
	// #endregion

	// #region BGNetCount //Peak ROI net count 占쏙옙占�
	// double BGEroChSpec[CHSIZE], double **PeakInfo, int NoPeak
	public static Vector<NcPeak> BGNetCount(double[] BGEroChSpec, Spectrum OriginalBG, Spectrum MS,
											Vector<NcPeak> PeakInfo, int NoPeak, boolean IsOneSecMode) {
		double ROI_L, ROI_R;
		double bg_major;// major background=natural backgorund+ scatering
		// Background
		double bg_minor; // minor backgorud: residual backgroud=bg_a*x+b;

		for (int i = 0; i < NoPeak; i++) {
			ROI_L = PeakInfo.get(i).ROI_Left;
			ROI_R = PeakInfo.get(i).ROI_Right;
			bg_major = 0;
			bg_minor = 0;

			for (int j = (int) ROI_L; j <= ROI_R; j++) {
				bg_major = bg_major + BGEroChSpec[j];
				bg_minor = bg_minor + PeakInfo.get(i).BG_a * j + PeakInfo.get(i).BG_b;
			}
			// PeakInfo.get(i).Background_Net_Count = (bg_major + bg_minor) +
			// Original_BG_NetCount(OriginalBG, MS.Get_AcqTime(),
			// PeakInfo.get(i).ROI_Left, PeakInfo.get(i).ROI_Right) ;
			PeakInfo.get(i).Background_Net_Count = (bg_major + bg_minor) + Original_BG_NetCount(OriginalBG,
					MS.get_AcqTime(), PeakInfo.get(i).ROI_Left, PeakInfo.get(i).ROI_Right, IsOneSecMode);

		}

		return PeakInfo;
	}

	public static double Original_BG_NetCount(Spectrum OriginalBG, int AcqTime, double ROI_L, double ROI_R,
											  boolean IsOneSecMode) {

		double Sum = 0;// major background=natural backgorund+ scatering
		double[] TempBG = new double[OriginalBG.GetChSize()];
		double[] OneSecBG = new double[OriginalBG.GetChSize()];

		for (int i = 0; i < OneSecBG.length; i++) {
			if (IsOneSecMode) {
				OneSecBG[i] = OriginalBG.At(i) / OriginalBG.get_AcqTime();
			} else {
				OneSecBG[i] = (OriginalBG.At(i) / OriginalBG.get_AcqTime()) * AcqTime;
			}
		}

		for (int j = (int) ROI_L; j <= ROI_R; j++) {
			Sum += OneSecBG[j];
		}

		return Sum;
	}

	// Add 170216
	public static Vector<NcPeak> PPNetCount(double[] PPChSpec, Vector<NcPeak> PeakInfo, int NoPeak) {
		double ROI_L, ROI_R;
		double bg_major;// major background=natural backgorund+ scatering
		// Background
		double bg_minor; // minor backgorud: residual backgroud=bg_a*x+b;

		for (int i = 0; i < NoPeak; i++) {
			ROI_L = PeakInfo.get(i).ROI_Left;
			ROI_R = PeakInfo.get(i).ROI_Right;
			bg_major = 0;
			bg_minor = 0;

			for (int j = (int) ROI_L; j <= ROI_R; j++) {
				bg_major = bg_major + PPChSpec[j];

			}
			PeakInfo.get(i).NetCnt = bg_major + bg_minor;
		}

		return PeakInfo;
	}

	// C#占쏙옙占쏙옙
	public static Vector<NcPeak> BGNetCount(double[] BGEroChSpec, Vector<NcPeak> PeakInfo, int NoPeak) {
		double ROI_L, ROI_R;
		double bg_major;// major background=natural backgorund+ scatering
		// Background
		double bg_minor; // minor backgorud: residual backgroud=bg_a*x+b;

		for (int i = 0; i < NoPeak; i++) {
			ROI_L = PeakInfo.get(i).ROI_Left;
			ROI_R = PeakInfo.get(i).ROI_Right;
			bg_major = 0;
			bg_minor = 0;

			for (int j = (int) ROI_L; j <= ROI_R; j++) {
				bg_major = bg_major + BGEroChSpec[j];
				bg_minor = bg_minor + PeakInfo.get(i).BG_a * j + PeakInfo.get(i).BG_b;
			}
			PeakInfo.get(i).Background_Net_Count = bg_major + bg_minor;
		}

		return PeakInfo;
	}
	// #endregion

	// #region PeakUncertainty //Peak占쏙옙 Uncertainty 占쏙옙占�
	// double ChSpec[CHSIZE], double **PeakInfo, int NoPeak
	public static Vector<NcPeak> PeakUncertainty(double[] ChSpec, Vector<NcPeak> PeakInfo, int NoPeak) {
		double B, G, P;
		double ROI_L, ROI_R;
		double del_P_sqr, del_P;

		for (int i = 0; i < NoPeak; i++) {
			ROI_L = PeakInfo.get(i).ROI_Left;
			ROI_R = PeakInfo.get(i).ROI_Right;

			G = 0;
			for (int j = (int) ROI_L; j <= ROI_R; j++) {
				G = G + ChSpec[j]; // gross count
			}
			B = PeakInfo.get(i).Background_Net_Count;
			P = G - B;
			del_P_sqr = G + (ROI_R - ROI_L + 1) / (2 * 2) * B;
			del_P = Math.sqrt(del_P_sqr);
			PeakInfo.get(i).peak_Uncertainty = del_P / P * 100; // uncertainty
			// error (%)
		}
		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	public static ArrayList<PeakAnalysis> PeakUncertainty(double[] ChSpec, ArrayList<PeakAnalysis> PeakInfo, int NoPeak) // MeasurementSpc
	{
		double B, G, P;
		double ROI_L, ROI_R;
		double del_P_sqr, del_P;

		for (int i = 0; i < NoPeak; i++) {
			ROI_L = PeakInfo.get(i).ROI_L;
			ROI_R = PeakInfo.get(i).ROI_R;

			G = 0;
			for (int j = (int) ROI_L; j <= ROI_R; j++) {
				G = G + ChSpec[j]; // gross count
			}
			B = PeakInfo.get(i).BgNetCount;
			P = G - B;
			del_P_sqr = G + (ROI_R - ROI_L + 1) / (2 * 2) * B;
			del_P = Math.sqrt(del_P_sqr);
			PeakInfo.get(i).Uncertain = del_P / P * 100; // uncertainty error
			// (%)
		}
		return PeakInfo;
	}
	// #endregion

	// #region PeakChannelToEnergy //Channel占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙占쏙옙 占쏙옙환
	// double **PeakInfo, int NoPeak, double *EnergyCoeff3P
	public static Vector<NcPeak> PeakChannelToEnergy(Vector<NcPeak> PeakInfo, int NoPeak, double[] EnergyCoeff3P) {
		double ch, erg, a, b, c;

		a = EnergyCoeff3P[0];
		b = EnergyCoeff3P[1];
		c = EnergyCoeff3P[2];

		for (int i = 0; i < NoPeak; i++) {
			ch = PeakInfo.get(i).Peak;
			erg = a * Math.pow(ch, 2.0) + b * Math.pow(ch, 1.0) + c;
			PeakInfo.get(i).Peak_Energy = erg;
		}

		return PeakInfo;
	}

	public static Vector<NcPeak> PeakChannelToEnergy(Vector<NcPeak> PeakInfo, int NoPeak, Coefficients EnergyCoeff3P) {
		double ch, erg, a, b, c;

		a = EnergyCoeff3P.get_Coefficients()[0];
		b = EnergyCoeff3P.get_Coefficients()[1];
		c = EnergyCoeff3P.get_Coefficients()[2];

		for (int i = 0; i < NoPeak; i++)
		{
			//ch = PeakInfo.get(i).Channel+1;
			ch = PeakInfo.get(i).Channel;
			erg = a * Math.pow(ch, 2.0) + b * Math.pow(ch, 1.0) + c;
			PeakInfo.get(i).Peak_Energy = erg;
		}

		return PeakInfo;
	}
	// #endregion

	// #region SourceIdentify
	// double **PeakInfo, int NoPeak
	public static Vector<NcPeak> SourceIdentify(Vector<NcPeak> PeakInfo, int NoPeak) {
		double[] BR = new double[] { 0.3592, // activtiy and BR for AM-241
				// source
				0.0366, 0.8551, 0.7990, 0.6497, 0.8499, 0.9932, 1, 1, 0.9932 };

		double[] Peak_True = new double[] { 59.54, 88.03, 122.06, 165.86, 391.7, 661.66, 898.04, 1173.23, 1332.49,
				1836.05 };

		double[] low_erg = new double[] { 55, 82, 115, 157, 376, 640, 874, 1147, 1306, 1809 };

		double[] high_erg = new double[] { 64, 94, 130, 175, 408, 683, 922, 1199, 1359, 1863 };

		double[] true_act = new double[] { 14557.1607299469, 114499.2975, 6061.942057, 2566.969602, 1617.837216,
				4237.875727, 1700.361378, 2316.207975, 2316.207975, 1700.361378 };

		double erg;
		for (int i = 0; i < NoPeak; i++) {
			PeakInfo.get(i).BR_Factor = 1;
			erg = PeakInfo.get(i).Peak_Energy;

			for (int j = 0; j < 10; j++) {
				if (erg >= low_erg[j] && erg <= high_erg[j]) {
					PeakInfo.get(i).BR_Factor = BR[j];
					PeakInfo.get(i).True_Energy_keV = Peak_True[j];
					PeakInfo.get(i).True_Current_Activity_Bg = true_act[j];
					PeakInfo.get(i).Used_for_Boolen = 1;// this is source
				}

			}
		}

		return PeakInfo;
	}

	// C# 占쏙옙占쏙옙
	// Efficiency 占쏙옙占쏙옙占쏙옙 획占쏙옙占쏙옙 Peak占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙占쏙옙 占쏙옙占쏙옙 CRM 占쏙옙臼占�
	// 占쏙옙占쏙옙占쏙옙 占쏙옙占� Peak占쏙옙
	// 占쏙옙占쏙옙占실억옙 Efficiency
	// 占쏙옙轅� 占쏙옙占쏙옙.
	/*
	 * public static ArrayList<PeakAnalysis> SourceIdentify(ArrayList<PeakAnalysis>
	 * PeakInfo, ArrayList<IsoCalibration> SelectCRMInfo) { double erg; for (int i =
	 * 0; i < PeakInfo.size(); i++) { PeakInfo.get(i).BRFactor = 1; erg =
	 * PeakInfo.get(i).Energy.Kev;
	 *
	 * for (int j = 0; j < SelectCRMInfo.Count; j++) { if (erg >=
	 * SelectCRMInfo[j].Energy.Kev * 0.92 && erg <= SelectCRMInfo[j].Energy.Kev *
	 * 1.08) { PeakInfo.get(i).Energy.BR = SelectCRMInfo[j].Energy.BR;
	 * PeakInfo.get(i).Energy.Uncertain = SelectCRMInfo[j].Energy.Uncertain;
	 * PeakInfo.get(i).BRFactor = SelectCRMInfo[j].Energy.BR / 100d;
	 * PeakInfo.get(i).TrueEnergy = SelectCRMInfo[j].Energy.Kev;
	 * PeakInfo.get(i).TrueActivity = SelectCRMInfo[j].ActualActivity;
	 * PeakInfo.get(i).Used = true;// this is source break; } } }
	 *
	 * for (int i = 0; i < PeakInfo.size(); i++) { if (PeakInfo.get(i).Used ==
	 * false) { PeakInfo.remove(i); i--; } }
	 *
	 *
	 *
	 *
	 *
	 * return PeakInfo; }
	 */

	/*
	 * public static ArrayList<PeakAnalysis> SourceMatching(ArrayList<PeakAnalysis>
	 * PeakInfo, ArrayList<IsoCalibration> SelectCRMInfo) { double erg; for (int i =
	 * 0; i < PeakInfo.size(); i++) { PeakInfo.get(i).BRFactor = 1; erg =
	 * PeakInfo.get(i).Energy.Kev;
	 *
	 * for (int j = 0; j < SelectCRMInfo.Count; j++) { if (erg >=
	 * SelectCRMInfo[j].Energy.Kev * 0.92 && erg <= SelectCRMInfo[j].Energy.Kev *
	 * 1.08) { PeakInfo.get(i).Energy.BR = SelectCRMInfo[j].Energy.BR;
	 * PeakInfo.get(i).BRFactor = SelectCRMInfo[j].Energy.BR / 100d;
	 * PeakInfo.get(i).TrueEnergy = SelectCRMInfo[j].Energy.Kev;
	 * PeakInfo.get(i).TrueActivity = SelectCRMInfo[j].ActualActivity;
	 * PeakInfo.get(i).Used = true;// this is source break; } } }
	 *
	 * return PeakInfo; }
	 */

	// #endregion

	// #region PeakActivity //Peak ROI占쏙옙 activity 占쏙옙占�
	// double **PeakInfo, double MeasuredTime, int NoPeak, double *EffCoeff
	public static Vector<NcPeak> PeakActivity(Vector<NcPeak> PeakInfo, double MeasuredTime, int NoPeak,
											  double[] EffCoeff) {
		double x, eff, tmp;

		for (int i = 0; i < NoPeak; i++) {
			x = Math.log(PeakInfo.get(i).PeakEst);
			tmp = EffCoeff[0] * Math.pow(x, 4.0) + EffCoeff[1] * Math.pow(x, 3.0) + EffCoeff[2] * Math.pow(x, 2.0)
					+ EffCoeff[3] * Math.pow(x, 1.0) + EffCoeff[4] * Math.pow(x, 0.0);

			eff = Math.exp(tmp);

			PeakInfo.get(i).Mesuared_Activty = PeakInfo.get(i).NetCnt / eff / MeasuredTime / PeakInfo.get(i).BR_Factor;
		}

		return PeakInfo;
	}

	public static Vector<Isotope> Get_C(Vector<Isotope> PeakInfo, double MeasuredTime, double[] EffCoeff, int mPos) {
		double x, eff, tmp;

		for (int i = 0; i < PeakInfo.get(mPos).FoundPeaks.size(); i++) {
			x = Math.log(PeakInfo.get(mPos).FoundPeaks.get(i).Peak_Energy);
			tmp = EffCoeff[0] * Math.pow(x, 4.0) + EffCoeff[1] * Math.pow(x, 3.0) + EffCoeff[2] * Math.pow(x, 2.0)
					+ EffCoeff[3] * Math.pow(x, 1.0) + EffCoeff[4] * Math.pow(x, 0.0);

			eff = Math.exp(tmp);
			double k = 37000; // C value:uCi
			double C = PeakInfo.get(mPos).FoundPeaks.get(i).NetCnt
					/ (double) (eff * PeakInfo.get(mPos).Peaks.get(i).Isotope_Gamma_En_BR * MeasuredTime * k);

			PeakInfo.get(mPos).C.add(C);
		}

		return PeakInfo;
	}

	// C#占쏙옙占쏙옙
	public static ArrayList<PeakAnalysis> PeakActivity(ArrayList<PeakAnalysis> PeakInfo, double MeasuredTime,
													   int NoPeak, Coefficients EffCoeff) {
		double x, eff, tmp;

		for (int i = 0; i < NoPeak; i++) {
			x = Math.log(PeakInfo.get(i).PeakEst);
			tmp = EffCoeff.get_Coefficients()[0] * Math.pow(x, 4.0) + EffCoeff.get_Coefficients()[1] * Math.pow(x, 3.0)
					+ EffCoeff.get_Coefficients()[2] * Math.pow(x, 2.0)
					+ EffCoeff.get_Coefficients()[3] * Math.pow(x, 1.0)
					+ EffCoeff.get_Coefficients()[4] * Math.pow(x, 0.0);

			eff = Math.exp(tmp);

			PeakInfo.get(i).MSActivity = PeakInfo.get(i).NetCount / eff / MeasuredTime / PeakInfo.get(i).BRFactor;
		}

		return PeakInfo;
	}

	// #endregion

	// #region GetPeakMDA //MDA 占쏙옙占�

	// C#占쏙옙占쏙옙
	// double **PeakInfo, double MeasuredTime, int NoPeak, double *EffCoeff,
	// double TineSampletoBeginMeasure
	public static ArrayList<PeakAnalysis> GetPeakMDA(ArrayList<PeakAnalysis> PeakInfo, double MeasuredTime, int NoPeak,
													 Coefficients EffCoeff, double TineSampletoBeginMeasure) {
		double K = 1.645;
		double ln2 = Math.log(2.0);
		double Kc, Kw, LC, LD, T1, eff_corrected, br, V, Cf, Uf, MDA, KX;
		double Ib, B;
		KX = 1;
		Cf = 1;
		V = 1;

		double t_half, tc, tw; // tc: elapse real clock time during measurement;
		// tw: elapsed time from referce date of isotope
		// to the beggining of measurement

		double tmp, x;
		for (int i = 0; i < NoPeak; i++) {
			if (PeakInfo.get(i).TrueEnergy > 0) // true energy
			{

				t_half = PeakInfo.get(i).TrueActivity;
				T1 = MeasuredTime; // however, tc= real clock time during
				// measurement; unit secon
				tc = T1 / (double) 86400; // unit:days
				tw = TineSampletoBeginMeasure;// biginning measurement
				// -Reference date

				tmp = (ln2 * tc) / t_half;

				Kc = 1.0 / tmp * (1 - Math.exp(-tmp));

				tmp = (ln2 * tw) / t_half;

				Kw = Math.exp(-tmp);

				x = Math.log(PeakInfo.get(i).PeakEst);
				tmp = EffCoeff.get_Coefficients()[0] * Math.pow(x, 4.0)
						+ EffCoeff.get_Coefficients()[1] * Math.pow(x, 3.0)
						+ EffCoeff.get_Coefficients()[2] * Math.pow(x, 2.0)
						+ EffCoeff.get_Coefficients()[3] * Math.pow(x, 1.0)
						+ EffCoeff.get_Coefficients()[4] * Math.pow(x, 0.0);

				eff_corrected = Math.exp(tmp);

				// calculate LC and LD
				B = PeakInfo.get(i).NetCount;
				Ib = PeakInfo.get(i).BgNetCount;
				LC = K * Math.sqrt(2 * B + Ib);

				LD = K * K + 2 * LC;

				br = PeakInfo.get(i).BRFactor;
				V = 1;
				Uf = 37000;

				MDA = LD / (T1 * eff_corrected * br * V * Kc * Kw * KX * Cf * Uf);

				PeakInfo.get(i).MDA = MDA;

			}
		}

		return PeakInfo;
	}

	public static double[][] GetPeakMDA(double[][] PeakInfo, double MeasuredTime, int NoPeak, double[] EffCoeff,
										double TineSampletoBeginMeasure) {
		double K = 1.645;
		double ln2 = Math.log(2.0);
		double Kc, Kw, LC, LD, T1, eff_corrected, br, V, Cf, Uf, MDA, KX;
		double Ib, B;
		KX = 1;
		Cf = 1;
		V = 1;

		double t_half, tc, tw; // tc: elapse real clock time during measurement;
		// tw: elapsed time from referce date of isotope
		// to the beggining of measurement

		double tmp, x;
		for (int i = 0; i < NoPeak; i++) {
			if (PeakInfo[i][13] > 0) // true energy
			{

				t_half = PeakInfo[i][18];
				T1 = MeasuredTime; // however, tc= real clock time during
				// measurement; unit secon
				tc = T1 / (double) 86400; // unit:days
				tw = TineSampletoBeginMeasure;// biginning measurement
				// -Reference date

				tmp = (ln2 * tc) / t_half;

				Kc = 1.0 / tmp * (1 - Math.exp(-tmp));

				tmp = (ln2 * tw) / t_half;

				Kw = Math.exp(-tmp);

				x = Math.log(PeakInfo[i][4]);
				tmp = EffCoeff[0] * Math.pow(x, 4.0) + EffCoeff[1] * Math.pow(x, 3.0) + EffCoeff[2] * Math.pow(x, 2.0)
						+ EffCoeff[3] * Math.pow(x, 1.0) + EffCoeff[4] * Math.pow(x, 0.0);

				eff_corrected = Math.exp(tmp);

				// calculate LC and LD
				B = PeakInfo[i][8];
				Ib = PeakInfo[i][10];
				LC = K * Math.sqrt(2 * B + Ib);

				LD = K * K + 2 * LC;

				br = PeakInfo[i][14];
				V = 1;
				Uf = 37000;

				MDA = LD / (T1 * eff_corrected * br * V * Kc * Kw * KX * Cf * Uf);

				PeakInfo[i][16] = MDA;

			}
		}

		return PeakInfo;
	}
	// #endregion

	// #region VariableSmooth // 占쏙옙占쏙옙占십울옙
	// 占쏙옙占쏙옙 WBC占쏙옙占쏙옙 占쏙옙占쏙옙構占� 占쌍댐옙 smoothing method
	// double **PeakInfo, int NoPeak, double *EnergyCoeff3P
	// public double[][] VariableSmooth(double[][] PeakInfo, int NoPeak,
	// double[] EnergyCoeff3P)
	// {
	// double ch, erg, a, b, c;

	// a = EnergyCoeff3P[0];
	// b = EnergyCoeff3P[1];
	// c = EnergyCoeff3P[2];

	// for (int i = 0; i < NoPeak; i++)
	// {
	// ch = PeakInfo[i][4];
	// erg = a * Math.Pow(ch, 2.0) + b * Math.Pow(ch, 1.0) + c;
	// PeakInfo[i][9] = erg;
	// }

	// return PeakInfo;
	// }
	// #endregion

	/// add new funcion 2017.10.27: HongJae Lee

	public static Vector<NcPeak> Calculate_LC(Vector<NcPeak> PeakInfo) {

		double No_avg_bg_ch = 2;
		double L, R, W, BGTemp, sigma;
		double k = 1.645; // % 1.645: if 95 % confidence

		for (int i = 0; i < PeakInfo.size(); i++) {
			L = PeakInfo.get(i).ROI_Left;
			R = PeakInfo.get(i).ROI_Right;

			BGTemp = PeakInfo.get(i).Background_Net_Count;

			W = R - L + 1;

			sigma = W / (2d * No_avg_bg_ch);

			PeakInfo.get(i).LC = k * Math.sqrt(BGTemp * (1 + sigma));
		}

		return PeakInfo;
	}

	public static Vector<Isotope> C_Validation_Filter(Vector<Isotope> Result2, double[] Act, double[] RMSE,
													  double[][][] List_InforIso1, double[][] PeakInfor, double[] Eff_coeff, int TimeMS) {

		// Isolist: List Isotope
		// ListInforIso: Strure
		// ListInforIso[0] ={ListTrueEn[], ListBr[], ListMSEn[], ListBr[]

		// PeakInfor: 2 dimension
		// PeakInfor[0,0]: PeakChn
		// PeakInfor[0,1]: ROI_L
		// PeakInfor[0,2]: ROI_R
		// PeakInfor[0,3]: MSEnerg
		// PeakInfor[0,4]: PP Netcount
		// PeakInfor[0,5]: BGCount
		// PeakInfor[0,6]: LC

		// String[] ListIso=new String[Result2.size()];
		// for(int i=0;i<Result2.size())

		int NoIso = Result2.size();
		String[] ListIso_Final = new String[NoIso];
		// int NoListTrueEn = 20;

		// Step 1: Find Reference Isotope

		int MaxInd0 = 0;// Index of Reference isotope
		double MaxAct = 0;

		for (int i = 0; i < NoIso; i++) {
			if (Act[i] > MaxAct) {
				MaxAct = Act[i];
				MaxInd0 = i;
			}
		}

		double sdv0 = RMSE[MaxInd0];
		ListIso_Final[0] = Result2.get(MaxInd0).isotopes;

		if (sdv0 > 1e-4 && sdv0 < 100) {

		} else {
			sdv0 = Math.sqrt(MaxAct);
		}

		//double sdv0_act = sdv0 * 2 * MaxAct;

		//17.11.15 uypdate
		double sdv0_act = sdv0 * 3 * MaxAct;

		// Step 2: Processing algorithm
		double[][] PeakEnBr_Ref = new double[][] {};

		double[][] List_Infor = new double[NoListTrueEn][4];

		int NoMaxEn = 0;
		double en, roi, lowen, highen;
		double m_act_iso;
		int iso_cnt = 0;
		for (int i = 0; i < NoIso; i++) {
			if (Math.abs(i - MaxInd0) > 0) {
				// get Infor
				for (int j = 0; j < NoListTrueEn; j++) {
					for (int k = 0; k < 4; k++) {
						List_Infor[j][k] = List_InforIso1[i][j][k];
					}
				}

				PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(Result2.get(MaxInd0));

				NoMaxEn = PeakEnBr_Ref.length;

				double[] Flg_MixEn = new double[NoMaxEn];

				for (int j = 0; j < NoMaxEn; j++) {
					en = PeakEnBr_Ref[j][0];
					roi = Get_Roi_window_by_energy(en);

					lowen = en * (1 - roi * 0.01);

					highen = en * (1 + roi * 0.01);

					for (int k = 0; k < NoListTrueEn; k++) {
						if (List_Infor[k][2] > 0) {
							if (List_Infor[k][2] > lowen && List_Infor[k][2] < highen) {
								Flg_MixEn[j] = List_Infor[k][2];
							}
						}

					}

				}

				// step 2:
				m_act_iso = Calculate_Act_Iso_From_Ref(Flg_MixEn, List_Infor, MaxAct + sdv0_act, PeakEnBr_Ref,
						PeakInfor, Eff_coeff, TimeMS);

				// condition for exist isotope
				if (m_act_iso > 0.01 * MaxAct) {
					iso_cnt = iso_cnt + 1;
					ListIso_Final[iso_cnt] = Result2.get(i).isotopes;

					// m_act_iso = Calculate_Act_Iso_From_Ref(Flg_MixEn, List_Infor, MaxAct ,
					// PeakEnBr_Ref, PeakInfor, coeff, TimeMS);
				}

			}
		}

		// Final Result
		boolean flg1 = false;
		for (int i = 0; i < Result2.size(); i++) {
			flg1 = false;
			for (int j = 0; j < iso_cnt + 1; j++) {
				if (Result2.get(i).isotopes.equals(ListIso_Final[j])) {
					flg1 = true;
				}
			}

			if (flg1 == false) {
				Result2.remove(i);
				--i;
			}
		}

		return Result2;
	}

	// get branching and Peak energy
	// Major peak+ minor peak

	public static double[][] Get_MajorMinor_PeakEn_BR(Isotope mIso) {

		int num = mIso.IsoMinorPeakEn.size() + mIso.Peaks.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];

		for (int i = 0; i < mIso.Peaks.size(); i++) {
			true_en[i] = mIso.Peaks.get(i).Peak_Energy;
			true_br[i] = mIso.Peaks.get(i).Isotope_Gamma_En_BR;
		}

		for (int i = 0; i < mIso.IsoMinorPeakEn.size(); i++) {
			true_en[mIso.Peaks.size() + i] = mIso.IsoMinorPeakEn.get(i);
			true_br[mIso.Peaks.size() + i] = mIso.IsoMinorPeakBR.get(i);

		}

		double[][] PeakEn_Br = new double[true_en.length][2];

		for (int i = 0; i < true_en.length; i++) {
			PeakEn_Br[i][0] = true_en[i];
			PeakEn_Br[i][1] = true_br[i];
		}

		return PeakEn_Br;
	}

	public static double Get_Roi_window_by_energy(double en) {
		return (72 * Math.pow(en, -0.43));
	}
	public static double EntoCh_Cali(double Erg,Coefficients coeff)
	{

		double a = coeff.get_Coefficients()[0];
		double b = coeff.get_Coefficients()[1];
		double c = coeff.get_Coefficients()[2]-Erg;


		double Ch=(-b+Math.sqrt(b*b-4*a*c))/(2*a);

		return Ch;
	}
	public static double ChntoEn_Cali(double Chn,Coefficients coeff)
	{

		double a = coeff.get_Coefficients()[0];
		double b = coeff.get_Coefficients()[1];
		double c = coeff.get_Coefficients()[2];


		double En=a*Chn*Chn+b*Chn+c;

		return En;
	}
	public static double [] Get_Roi_window_by_energy_used_FWHM(double en,double[] FWHMCoeff,Coefficients coeff,double Ratio)
	{
		//double Ratio=0.6;

		double Ch=EntoCh_Cali(en,coeff);


		double FWHM=FWHMCoeff[0]*Math.sqrt(Ch)+FWHMCoeff[1];

		double ROI_L=Ch-Ratio*FWHM;
		double ROI_R=Ch+Ratio*FWHM;

		double ROI_L_En=ChntoEn_Cali(ROI_L,coeff);
		double ROI_R_En=ChntoEn_Cali(ROI_R,coeff);

		double [] Thshold=new double [2];
		Thshold[0]=ROI_L_En;
		Thshold[1]=ROI_R_En;

		return Thshold;
	}

	public static double Calculate_Act_Iso_From_Ref(double[] Flg_MixEn, double[][] List_Infor, double ActRef,
													double[][] PeakEn_BR, double[][] PeakInfo, double[] EffCoeff, int MStime) {
		int NoMaxEn = Flg_MixEn.length;

		double[] Net_From_Cand0 = new double[NoMaxEn];
		double en;

		// %% step 1.3: compare with sigma

		// %% Step 2.2: Calculate activity from
		// %% we assuming that mixuture energy of 1 peak contain maximum 2 isotopes

		for (int i = 0; i < NoMaxEn; i++) {
			if (Flg_MixEn[i] > 0) {
				en = Flg_MixEn[i];

				Net_From_Cand0[i] = ActRef * 37000d * PeakEn_BR[i][1] * Get_Eff(en, EffCoeff) * MStime;
			}
		}

		// %% Step 2.3: Subtract Activity from Certaint Isotope

		int M = PeakInfo.length; // size of PeakInfo
		double[] PeakInfor_Net = new double[M];
		for (int i = 0; i < M; i++) {
			PeakInfor_Net[i] = PeakInfo[i][4];
		}
		// copy net count of PeakInfor(2 dimensions) to PeakInfor_Net(1 dimension)

		for (int i = 0; i < M; i++) {
			for (int j = 0; j < NoMaxEn; j++) {
				if (Flg_MixEn[j] > 0) {
					if (Flg_MixEn[j] == PeakInfo[i][3])// compare with measurement energy
					{
						PeakInfor_Net[i] = PeakInfor_Net[i] - Net_From_Cand0[j];
						if (PeakInfor_Net[i] < 0) {
							PeakInfor_Net[i] = 0;
						}
					}
				}
			}

		}

		// %% Recalculate List_Infor
		//Hung Modifye 17.11.09

		// 1st: start
		double[][] List_Infor_AddNet = new double[NoListTrueEn][5];
		// get value
		for (int i = 0; i < NoListTrueEn; i++) {
			for (int j = 0; j < 4; j++) {
				List_Infor_AddNet[i][j] = List_Infor[i][j];
			}
		}

		int NoListTrueEn = List_Infor.length;

		double[] Act_tmp = new double[NoListTrueEn];

		for (int i = 0; i < NoListTrueEn; i++) {
			if (List_Infor[i][2] > 0) {
				for (int j = 0; j < M; j++) {
					if (PeakInfo[j][3] == List_Infor[i][2]) {
						Act_tmp[i] = PeakInfor_Net[j] / List_Infor[i][3] / MStime / Get_Eff(PeakInfo[j][3], EffCoeff)
								/ 37000d;
						List_Infor_AddNet[i][4] = PeakInfor_Net[j];
					}
				}
			}
		}

		// %% calculate average activity of iso_name

		//3nd: modifed
		//Hung Modified: 17.11.09
		int NoPeakEnTemp = 0;
		for (int  i = 0; i < 20; i++)
		{
			if (List_Infor_AddNet[i][0] > 0)
			{
				NoPeakEnTemp = NoPeakEnTemp + 1;
			}
		}


		double avg_act = 0;

		if (NoPeakEnTemp > 0)
		{
			double[] Act = new double[1];

			double[] PeakEnTemp = new double[NoPeakEnTemp];
			double[][] BRTemp = new double[NoPeakEnTemp][1];
			double[] BRTemp1 = new double[NoPeakEnTemp];
			double[] PPNetTemp = new double[NoPeakEnTemp];
			double[] WTemp = new double[NoPeakEnTemp];

			for (int i = 0; i < NoPeakEnTemp; i++)
			{
				PeakEnTemp[i] = List_Infor_AddNet[i][0];
				BRTemp[i][0] = List_Infor_AddNet[i][1];
				BRTemp1[i] = List_Infor_AddNet[i][1];
				PPNetTemp[i] = List_Infor_AddNet[i][4];
				WTemp[i] = 1;
			}
			ActivityConfidence mAct = new ActivityConfidence();

			Act = mAct.ActCorrect(EffCoeff, PeakEnTemp, BRTemp, PPNetTemp, WTemp, MStime);

			avg_act = Act[0];

			double Ratio = Caculate_Ratio(List_Infor_AddNet, EffCoeff);

			int FlgCond = 0;
			if (Ratio == 0)
			{
				double RMSE_Cal = mAct.StdErr(avg_act, BRTemp1, PeakEnTemp, PPNetTemp, MStime, EffCoeff);

				//condition
				if (RMSE_Cal < 1.5)
				{
					FlgCond = 1;
				}

			}
			else
			{
				//condition
				if (Ratio>0.5 && Ratio<1.5)
				{
					FlgCond = 1;
				}

			}

			if (FlgCond == 0) // this isotope is not true, to remove it, we just set activiy =0;
			{
				avg_act = 0;
			}

		}

		/*int m_cnt = 0;
		double sum_act = 0;
		for (int i = 0; i < NoListTrueEn; i++) {
			if (Act_tmp[i] > 0) {
				m_cnt = m_cnt + 1;
				sum_act = sum_act + Act_tmp[i];
			}
		}

		double avg_act = 0;

		if (m_cnt > 0) {
			avg_act = sum_act / (double) m_cnt;
		}
		*/
		return avg_act;
	}
	public static double Caculate_Ratio( double[][] List_Infor_AddNet, double[] Eff_Coef)
	{
		double Ratio = 0.0;
		int cnt = 0;
		double min1 = 100000000, max1 = 0;
		double en_max = 0;
		double br_max = 0;
		double en_min = 0;
		double br_min = 0;

		for (int i = 0; i < NoListTrueEn; i++)
		{
			if (List_Infor_AddNet[i][4] > 0)
			{
				cnt = cnt + 1;

				if (List_Infor_AddNet[i][ 4] > max1)
				{
					max1 = List_Infor_AddNet[i][ 4];
					en_max = List_Infor_AddNet[i][0];
					br_max = List_Infor_AddNet[i][1];
				}

				if (List_Infor_AddNet[i][ 4] < min1)
				{
					min1 = List_Infor_AddNet[i][ 4];

					en_min = List_Infor_AddNet[i][0];
					br_min = List_Infor_AddNet[i][1];
				}
			}
		}
		if (cnt > 1)
		{
			Ratio = max1 / min1;
			double Br_Eff_max = br_max*Get_Eff(en_max, Eff_Coef);
			double Br_Eff_min = br_min*Get_Eff(en_min, Eff_Coef);
			double Ratio_Br_Eff = Br_Eff_max / Br_Eff_min;

			Ratio = Ratio / Ratio_Br_Eff;
		}
		return Ratio;
	}
	public static double Get_Eff(double en, double[] EffCoeff) {
		double x = Math.log(en);

		double tmp = EffCoeff[0] * Math.pow(x, 4.0) + EffCoeff[1] * Math.pow(x, 3.0) + EffCoeff[2] * Math.pow(x, 2.0)
				+ EffCoeff[3] * Math.pow(x, 1.0) + EffCoeff[4];

		double eff = Math.exp(tmp);

		return eff;
	}

	public static Vector<NcPeak> SearchROI_N(double[] Spc, Vector<NcPeak> PeakInfo, double[] FWHMCoeff) {
		int NoPeak = PeakInfo.size();
		int ROI_L, ROI_R;
		double fwhm_ch = 0;
		double[] Pt = new double[2];
		for (int i = 0; i < NoPeak; i++) {

			// search for left
			fwhm_ch = FWHMCoeff[0] * Math.sqrt(PeakInfo.get(i).Channel) + FWHMCoeff[1];

			ROI_L = (int) (PeakInfo.get(i).Channel - fwhm_ch * 2d);
			ROI_R = (int) (PeakInfo.get(i).Channel + fwhm_ch * 2d);

			Pt = Find_Vally(Spc, ROI_L, ROI_R, (int) PeakInfo.get(i).Channel);

			// if we can not find, we will set ROI as default?
			// if(Pt[0]== ROI_L) Pt[0]= (int)(PeakInfo.get(i).Channel - fwhm_ch);
			// if (Pt[1] == ROI_R) Pt[1] = (int)(PeakInfo.get(i).Channel + fwhm_ch);

			PeakInfo.get(i).ROI_Left = (int) Pt[0];
			PeakInfo.get(i).ROI_Right = (int) Pt[1];
		}

		return PeakInfo;
	}

	public static double[] Find_Vally(double[] arrfloat, int firstlocation, int secondlocation, int peakvalue) {
		// start x
		int startpx = 0;
		int center = peakvalue;
		int contcnt = 0;
		int cntlimit = 2;

		double threshold = 0;

		int findflag = 0;

		for (int i = center - (int) (center * 0.02); i >= firstlocation; i--) {
			if ((arrfloat[i - 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						startpx = i + (cntlimit - 1);
						if (center == startpx) {

						} else {
							break;
						}
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}

		if (findflag == 0 || contcnt < cntlimit) {
			startpx = firstlocation;
			// startpy = arrfloat[firstlocation];
		}

		// search for py
		int endpx = 0;
		contcnt = 0;
		findflag = 0;
		for (int i = center + (int) (center * 0.01); i <= secondlocation; i++) {
			if ((arrfloat[i + 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						endpx = i - (cntlimit - 1);
						if (center == endpx) {

						} else {
							break;
						}
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}

		if (findflag == 0 || contcnt < cntlimit) {
			endpx = secondlocation;
			// endpy = arrfloat[secondlocation];
		}

		double[] Valley_Pt = new double[2];
		Valley_Pt[0] = startpx;
		Valley_Pt[1] = endpx;

		return Valley_Pt;

	}

	public static Vector<NcPeak> NetCount_N(double[] Spc, Vector<NcPeak> PeakInfo, double[] FWHMCoeff) {

		double pi = 3.1415926535897932384626433832795;

		int NoPeak = PeakInfo.size();

		int ind_ch;

		double x1, y1, x2, y2, a, b;

		int ind_ch_max = 0;
		double max_val = 0;
		double Hest = 0;
		double fit_fwhm;

		for (int i = 0; i < NoPeak; i++) {
			ind_ch = (int) PeakInfo.get(i).Channel;

			x1 = PeakInfo.get(i).ROI_Left;
			y1 = Spc[(int) x1];

			x2 = PeakInfo.get(i).ROI_Right;
			y2 = Spc[(int) x2];

			if (x2 == x1)
				x2 = x1 + 1;

			a = (y2 - y1) / (x2 - x1);
			b = y1 - a * x1;

			ind_ch_max = 0;
			max_val = 0;

			for (int j = ind_ch - 2; j < ind_ch + 2; j++) {
				if (j > 0 && j < CHSIZE) {
					if (Spc[j] > max_val) {
						max_val = Spc[j];
						ind_ch_max = j;
					}
				}
			}

			if (ind_ch_max == 0)
				ind_ch_max = ind_ch;

			Hest = Spc[ind_ch_max] - (a * ind_ch_max + b);

			if (Hest < 0)
				Hest = 0;

			fit_fwhm = FWHMCoeff[0] * Math.sqrt((double) ind_ch_max) + FWHMCoeff[1];

			PeakInfo.get(i).NetCnt = Math.sqrt(pi) * Hest * Math.sqrt(2) * (fit_fwhm / 2.355);

			PeakInfo.get(i).BG_a = a;
			PeakInfo.get(i).BG_b = b;

		}

		return PeakInfo;
	}

	public static Vector<NcPeak> NetBGSubtract_N(Vector<NcPeak> PeakInfo, Vector<NcPeak> PeakInfo_bg, double mstime,double bgtime,double[] FWHMCoeff, Coefficients coeff,double WndROI)
	{
		double erg;
		double erg_bg;
		double roi;
		double lowen, highen;
		double [] Thshold=new double [2];

		for (int i = 0; i < PeakInfo.size(); i++)
		{
			erg = PeakInfo.get(i).Peak_Energy;

			if (erg > 0)
			{
				//roi = Get_Roi_window_by_energy(erg);// %%% percent
				//roi = roi / 2d;

				Thshold=Get_Roi_window_by_energy_used_FWHM(erg,FWHMCoeff,coeff,WndROI) ;

				lowen = Thshold[0];

				highen = Thshold[1];
				//lowen = erg * (1 - roi * 0.01);

				//highen = erg * (1 + roi * 0.01);

				for (int j = 0; j < PeakInfo_bg.size(); j++)
				{
					erg_bg = PeakInfo_bg.get(j).Peak_Energy;

					if (erg_bg >= lowen && erg_bg <= highen) {
						PeakInfo.get(i).NetCnt = PeakInfo.get(i).NetCnt - PeakInfo_bg.get(j).NetCnt * mstime / bgtime;
						PeakInfo.get(i).Background_Net_Count = PeakInfo.get(i).Background_Net_Count
								+ PeakInfo_bg.get(j).NetCnt * mstime / bgtime;
					}
				}
			}

		}

		double Thsld_BG = 10; // removinng noise peak

		for (int i = 0; i < PeakInfo.size(); i++) {
			if (PeakInfo.get(i).NetCnt < Thsld_BG) {
				PeakInfo.remove(i);
				i--;
			}

		}
		return PeakInfo;

	}

	//
	// public static String[] CValue_Filter(double[] Spc, String[] ListIso,
	// ArrayList<PeakAnalysis> PeakInfo,
	// double[] FWHMCoeff, Coefficients coeff, double mstime, double bgtime) {
	// double Thshld_Un = 10;
	// double Thshld_RMSE = 1.2;
	//
	// int NoIso = ListIso.length;
	//
	// // 1st Step: Create 3 Dimension List_InforIso
	// double[][] PeakMajorEn_Br = new double[][] {};
	// double[][][] List_InforIso = new double[NoIso][NoListTrueEn][4];
	//
	// double erg, roi, lowen, highen;
	// for (int i = 0; i < NoIso; i++) {
	// PeakMajorEn_Br = Get_Major_PeakEn_BR(ListIso[i]);
	//
	// int NoPeakMajor = PeakMajorEn_Br.length / PeakMajorEn_Br[0].length;
	//
	// if (NoPeakMajor > 0) {
	// // Get TrueEnergy and Branching ratio
	// for (int j = 0; j < NoPeakMajor; i++) {
	// List_InforIso[i][j][0] = PeakMajorEn_Br[j][0];
	// List_InforIso[i][j][1] = PeakMajorEn_Br[j][1];
	// }
	//
	// // Get Mesuarement energy and branching ratio
	// for (int k = 0; k < PeakInfo.size(); k++) {
	//
	// for (int j = 0; j < NoPeakMajor; i++) {
	// erg = PeakMajorEn_Br[j][0];
	// roi = Get_Roi_window_by_energy(erg);// %%% percent
	//
	// lowen = erg * (1 - roi * 0.01);
	//
	// highen = erg * (1 + roi * 0.01);
	//
	// if (PeakInfo.get(k).MSEnergy >= lowen && PeakInfo.get(k).MSEnergy <= highen)
	// {
	// List_InforIso[i][j][2] = PeakInfo.get(k).MSEnergy;
	// List_InforIso[i][j][3] = PeakMajorEn_Br[j][1];
	// }
	//
	// }
	//
	// }
	// }
	// }
	//
	// // Step 2: Condition
	// // double Peak: result2 = FindCvalue.ActivityCorrection(MsSpc, result2,
	// // Peak_data, SPC.Get_AcqTime(), Eff_Coef);
	//
	// double[][] Peak_data = new double[][] {};
	//
	// double[][][] List_InforIso1 = new double[NoIso][NoListTrueEn][4];
	//
	// // initilize value
	// for (int i = 0; i < NoIso; i++) {
	// for (int j = 0; j < NoListTrueEn; j++) {
	// for (int k = 0; k < 4; k++) {
	// List_InforIso1[i][j][k] = List_InforIso[i][j][k];
	// }
	// }
	// }
	//
	// String[] List_Iso1 = new String[NoIso];
	//
	// for (int i = 0; i < NoIso; i++) {
	// List_Iso1[i] = ListIso[i];
	// }
	//
	// double[] Act = new double[] {};
	// double[] Uncer = new double[] {};
	// double[] RMSE = new double[] {};
	//
	// int NoID = 0;
	//
	// while (true) {
	//
	// // 1st: get Peak_data
	// Boolean Flag1;
	// int cnt = 0;
	//
	// for (int i = 0; i < PeakInfo.size(); i++) {
	// Flag1 = false;
	//
	// for (int j = 0; j < NoIso; j++) {
	// for (int k = 0; k < NoListTrueEn; k++) {
	// if (List_InforIso[j][k][2] == PeakInfo.get(i).MSEnergy) {
	// Flag1 = true;
	// }
	// }
	// }
	//
	// if (Flag1 == true) {
	// cnt = cnt + 1;
	// }
	// }
	//
	// Peak_data = new double[cnt][6];
	//
	// for (int i = 0; i < PeakInfo.size(); i++) {
	// Flag1 = false;
	//
	// for (int j = 0; j < NoIso; j++) {
	// for (int k = 0; k < NoListTrueEn; k++) {
	// if (List_InforIso[j][k][2] == PeakInfo.get(i).MSEnergy) {
	// Flag1 = true;
	// }
	// }
	// }
	//
	// if (Flag1 == true) {
	// Peak_data[cnt][0] = PeakInfo.get(i).Channel;
	// Peak_data[cnt][1] = PeakInfo.get(i).ROI_L;
	// Peak_data[cnt][2] = PeakInfo.get(i).ROI_R;
	// Peak_data[cnt][3] = PeakInfo.get(i).MSEnergy;
	// Peak_data[cnt][4] = PeakInfo.get(i).NetCount;
	// Peak_data[cnt][5] = PeakInfo.get(i).BgNetCount;
	// cnt = cnt + 1;
	// }
	// }
	//
	// NoIso = List_Iso1.length;
	//
	// // result2 = FindCvalue.ActivityCorrection(MsSpc, result2, Peak_data,
	// // SPC.Get_AcqTime(), Eff_Coef);
	// // result2, will be tranfewr to Act, Uncer, RMSE
	//
	// NoID = Act.length;
	//
	// for (int i = 0; i < NoIso; i++) {
	// if (Act[i] > 0) {
	// if (Uncer[i] <= Thshld_Un && RMSE[i] <= Thshld_RMSE) {
	// NoID = NoID + 1;
	// }
	// }
	// }
	//
	// if (NoID > 0) {
	// String[] Nu_new = new String[NoID];
	// double[][][] List_InforIso2 = new double[NoID][NoListTrueEn][4];
	//
	// int cnt1 = 0;
	//
	// for (int i = 0; i < NoIso; i++) {
	// if (Act[i] > 0) {
	// if (Uncer[i] <= Thshld_Un && RMSE[i] <= Thshld_RMSE) {
	// Nu_new[cnt1] = List_Iso1[i];
	// for (int j = 0; j < NoListTrueEn; j++) {
	// for (int k = 0; k < 4; k++) {
	// List_InforIso2[cnt1][j][k] = List_InforIso1[i][j][k];
	// }
	// }
	// cnt1 = cnt1 + 1;
	// }
	// }
	//
	// }
	//
	// if (cnt1 == NoID) {
	// break;
	// } else {
	// List_Iso1 = new String[cnt1];
	// List_InforIso1 = new double[cnt1][NoListTrueEn][4];
	//
	// for (int i = 0; i < cnt1; i++) {
	// List_Iso1[i] = Nu_new[i];
	//
	// for (int j = 0; j < NoListTrueEn; j++) {
	// for (int k = 0; k < 4; k++) {
	// List_InforIso1[i][j][k] = List_InforIso2[i][j][k];
	// }
	// }
	// }
	// }
	// } else {
	//
	// break;
	// }
	//
	// }
	//
	// // Conditon for activity validation filter
	//
	// NoIso = List_Iso1.length;
	//
	// if (NoIso > 1) {
	// List_Iso1 = C_Validation_Filter(List_Iso1, Act, RMSE, List_InforIso1,
	// Peak_data, coeff, (int) mstime);
	//
	// }
	//
	// return ListIso;
	//
	// // public static String[] C_Validation_Filter(String[] ListIso, double[] Act,
	// // double[] RMSE, double[,,] List_InforIso1, double[][] PeakInfor,
	// Coefficients
	// // coeff, int TimeMS)
	//
	// }

	public static double[][] Get_Major_PeakEn_BR(Isotope mIso) {

		int num = mIso.IsoMinorPeakEn.size() + mIso.Peaks.size();
		double[] true_en = new double[num];
		double[] true_br = new double[num];

		for (int i = 0; i < mIso.Peaks.size(); i++) {
			true_en[i] = mIso.Peaks.get(i).Peak_Energy;
			true_br[i] = mIso.Peaks.get(i).Isotope_Gamma_En_BR;
		}

		// for (int i = 0; i < mIso.IsoMinorPeakEn.size(); i++)
		// {
		// true_en[mIso.Peaks.size() + i] = mIso.IsoMinorPeakEn.get(i);
		// true_br[mIso.Peaks.size() + i] = mIso.IsoMinorPeakBR.get(i);
		//
		// }

		double[][] PeakEn_Br = new double[mIso.Peaks.size()][2];
		for (int i = 0; i < mIso.Peaks.size(); i++) {
			PeakEn_Br[i][0] = true_en[i];
			PeakEn_Br[i][1] = true_br[i];
		}

		return PeakEn_Br;
	}

	public static Vector<Isotope> CValue_Filter(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,
												double[] FWHMCoeff, double[] Eff_Coeff, double mstime) {
		String[] ListIso = new String[Result2.size()];

		for (int i = 0; i < Result2.size(); i++) {
			ListIso[i] = Result2.get(i).isotopes;
		}

		double Thshld_Un = 10;
		double Thshld_RMSE = 1.2;

		int NoIso = ListIso.length;

		// 1st Step: Create 3 Dimension List_InforIso
		double[][] PeakMajorEn_Br = new double[NoListTrueEn][2];
		double[][][] List_InforIso = new double[NoIso][NoListTrueEn][4];

		double[][] aa = new double[3][4];
		int m1 = aa.length;
		int m2 = aa[0].length;

		double erg, roi, lowen, highen;
		for (int i = 0; i < NoIso; i++) {
			PeakMajorEn_Br = Get_Major_PeakEn_BR(Result2.get(i));

			int NoPeakMajor = PeakMajorEn_Br.length;

			if (NoPeakMajor > 0) {
				// Get TrueEnergy and Branching ratio
				for (int j = 0; j < NoPeakMajor; j++) {
					List_InforIso[i][j][0] = PeakMajorEn_Br[j][0];
					List_InforIso[i][j][1] = PeakMajorEn_Br[j][1];
				}

				// Get Mesuarement energy and branching ratio
				for (int k = 0; k < PeakInfo.size(); k++) {

					for (int j = 0; j < NoPeakMajor; j++) {
						erg = PeakMajorEn_Br[j][0];
						roi = Get_Roi_window_by_energy(erg);// %%% percent

						lowen = erg * (1 - roi * 0.01);

						highen = erg * (1 + roi * 0.01);

						if (PeakInfo.get(k).Peak_Energy >= lowen && PeakInfo.get(k).Peak_Energy <= highen) {
							List_InforIso[i][j][2] = PeakInfo.get(k).Peak_Energy;
							List_InforIso[i][j][3] = PeakMajorEn_Br[j][1];
						}

					}

				}
			}
		}

		// Step 2: Condition
		// double Peak: result2 = FindCvalue.ActivityCorrection(MsSpc, result2,
		// Peak_data, SPC.Get_AcqTime(), Eff_Coef);

		//double[][] Peak_data = new double[][] {};

		double[][][] List_InforIso1 = new double[NoIso][NoListTrueEn][4];

		// initilize value
		for (int i = 0; i < NoIso; i++) {
			for (int j = 0; j < NoListTrueEn; j++) {
				for (int k = 0; k < 4; k++) {
					List_InforIso1[i][j][k] = List_InforIso[i][j][k];
				}
			}
		}

		String[] List_Iso1 = new String[NoIso];

		for (int i = 0; i < NoIso; i++) {
			List_Iso1[i] = ListIso[i];
		}

		String[] Final_iso = new String[] {};

		int NoID = 0;
		while (true) {
			NoIso = List_Iso1.length;

			if (NoIso == 0) {
				Final_iso = new String[1];
				Final_iso[0] = "FALSE";
				break;
			} else {
				// 1st: get Peak_data
				Boolean Flag1;
				int cnt = 0;

				for (int i = 0; i < PeakInfo.size(); i++) {
					Flag1 = false;

					for (int j = 0; j < NoIso; j++) {
						for (int k = 0; k < NoListTrueEn; k++) {
							if (List_InforIso1[j][k][2] == PeakInfo.get(i).Peak_Energy) {
								Flag1 = true;
							}
						}
					}

					if (Flag1 == true) {
						cnt = cnt + 1;
					}
				}

				/*
				Peak_data = new double[cnt][6];

				cnt = 0;
				for (int i = 0; i < PeakInfo.size(); i++) {
					Flag1 = false;

					for (int j = 0; j < NoIso; j++) {
						for (int k = 0; k < NoListTrueEn; k++) {
							if (List_InforIso1[j][k][2] == PeakInfo.get(i).Peak_Energy) {
								Flag1 = true;
							}
						}
					}

					if (Flag1 == true) {
						Peak_data[cnt][0] = PeakInfo.get(i).Channel;
						Peak_data[cnt][1] = PeakInfo.get(i).ROI_Left;
						Peak_data[cnt][2] = PeakInfo.get(i).ROI_Right;
						Peak_data[cnt][3] = PeakInfo.get(i).Peak_Energy;
						Peak_data[cnt][4] = PeakInfo.get(i).NetCnt;
						Peak_data[cnt][5] = PeakInfo.get(i).Background_Net_Count;
						cnt = cnt + 1;
					}
				}

				// remove noise peak energy
				for (int s = 0; s < PeakInfo.size(); s++) {
					boolean mCheck = false;
					for (int q = 0; q < Result2.size(); q++) {

						for (int e = 0; e < Result2.get(q).FoundPeaks.size(); e++) {
							if (PeakInfo.get(s) == Result2.get(q).FoundPeaks.get(e)) {
								mCheck = true;
							}
						}

					}
					if (mCheck == false) {
						PeakInfo.remove(s);
						--s;
					}
				}

				*/

				ActivityConfidence FindCvalue = new ActivityConfidence();

				Result2 = FindCvalue.ActivityCorrection(Spc, Result2, PeakInfo, mstime, Eff_Coeff);

				NoID = 0;

				for (int s = 0; s < NoIso; s++) {
					if (Result2.get(s).Act > 0) {
						if (Result2.get(s).Uncer <= Thshld_Un && Result2.get(s).RMSE <= Thshld_RMSE) {
							NoID = NoID + 1;
						}
					}
				}

				if (NoID > 0) {
					String[] Nu_new = new String[NoID];
					double[][][] List_InforIso2 = new double[NoID][NoListTrueEn][4];

					int cnt1 = 0;

					for (int i = 0; i < NoIso; i++) {
						if (Result2.get(i).Act > 0) {
							if (Result2.get(i).Uncer <= Thshld_Un && Result2.get(i).RMSE <= Thshld_RMSE) {
								Nu_new[cnt1] = List_Iso1[i];

								for (int j = 0; j < NoListTrueEn; j++) {
									for (int k = 0; k < 4; k++) {
										List_InforIso2[cnt1][j][k] = List_InforIso1[i][j][k];
									}
								}
								cnt1 = cnt1 + 1;
							}
						}

					}

					if (cnt1 == NoIso) {
						Final_iso = new String[Nu_new.length];
						for (int i = 0; i < Nu_new.length; i++) {
							Final_iso[i] = Nu_new[i];
						}

						break;
					} else {
						List_Iso1 = new String[cnt1];
						List_InforIso1 = new double[cnt1][NoListTrueEn][4];

						for (int i = 0; i < cnt1; i++) {
							List_Iso1[i] = Nu_new[i];

							for (int j = 0; j < NoListTrueEn; j++) {
								for (int k = 0; k < 4; k++) {
									List_InforIso1[i][j][k] = List_InforIso2[i][j][k];
								}
							}

						}

						// remove
						NoIso = List_Iso1.length;
						boolean flag_ch = true;
						for (int e = 0; e < Result2.size(); e++) {
							flag_ch = true;
							for (int j = 0; j < NoIso; j++) {
								if (Result2.get(e).isotopes.equals(List_Iso1[j])) {
									flag_ch = false;
								}
							}
							if (flag_ch == true) {
								Result2.remove(e);
								e--;
							}

						}

					}
				} else {
					Final_iso = new String[1];
					Final_iso[0] = "FALSE";
					break;
				}
			}

		}

		// remove candidate
		// removed Result2

		NoIso = Final_iso.length;
		boolean flag_ch1 = true;
		for (int i = 0; i < Result2.size(); i++) {
			flag_ch1 = true;
			for (int j = 0; j < NoIso; j++) {
				if (Result2.get(i).isotopes.equals(Final_iso[j])) {
					flag_ch1 = false;
				}

			}

			if (flag_ch1 == true) {
				Result2.remove(i);
				i--;
			}

		}

		// Conditon for activity validation filter
		double[] Act = new double[Result2.size()];
		double[] RMSE = new double[Result2.size()];

		for (int i = 0; i < Result2.size(); i++) {
			Act[i] = Result2.get(i).Act;
			RMSE[i] = Result2.get(i).RMSE;
		}

		if (NoIso > 1)
		{
			double[][] Peak_data = new double[PeakInfo.size()][6];

			for (int i = 0; i < PeakInfo.size(); i++)
			{
				Peak_data[i][0] = PeakInfo.get(i).Channel;
				Peak_data[i][1] = PeakInfo.get(i).ROI_Left;
				Peak_data[i][2] = PeakInfo.get(i).ROI_Right;
				Peak_data[i][3] = PeakInfo.get(i).Peak_Energy;
				Peak_data[i][4] = PeakInfo.get(i).NetCnt;
				Peak_data[i][5] = PeakInfo.get(i).Background_Net_Count;
			}


			Result2 = C_Validation_Filter(Result2, Act, RMSE, List_InforIso1, Peak_data, Eff_Coeff, (int) mstime);

		}

		return Result2;

		// public static string[] C_Validation_Filter(string[] ListIso, double[] Act,
		// double[] RMSE, double[,,] List_InforIso1, double[,] PeakInfor,
		// Coefficients<double> coeff, int TimeMS)

	}

	public static Vector<Isotope> CValue_Filter_H_old(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo, double[] FWHMCoeff,	Coefficients coeff, double [] Eff_Coeff ,
												  double mstime,double WndROI, double Act_Thshld)
	{
		// Adding condition to prohibit Co57 and Tc99m
		Result2=NewNcAnalsys.AddCondition_Co57_TC99m(Result2, FWHMCoeff, coeff) ;

		//Adding condition to prohibit WGPU by logic when RGPu is IDed
		Result2=AddCondition_WGPu_RGPU(Result2, PeakInfo,FWHMCoeff,	coeff,WndROI);

		if(Result2.size()>0)
		{

			//double Thshld_Un = 10;
			//double Thshld_RMSE = 1.2;

			double Thshld_Un = 20;
			double Thshld_RMSE = 2.0;

/*			double Thshld_Un = 20;
			double Thshld_RMSE = 1.5;*/

			// Step 1: Remove noise candidate to improve find activity by solving matrix

			ActivityConfidence FindCvalue = new ActivityConfidence();


			Result2=ScreeningProcess_1st(Spc, Result2, PeakInfo, FWHMCoeff, coeff, Eff_Coeff, mstime, Thshld_Un, Thshld_RMSE,WndROI);
			//Step 2:  Calculate activity


			String[] ListIso = new String[Result2.size()];

			for (int i = 0; i<Result2.size(); i++)
			{
				ListIso[i] = Result2.get(i).isotopes;
			}


			int NoIso = ListIso.length;

			String[] List_Iso1 = new String[NoIso];

			for (int i = 0; i < NoIso; i++)
			{
				List_Iso1[i] = ListIso[i];
			}

			String[] Final_iso = new String[]{};
			int NoID = 0;

			while (true)
			{
				NoIso = List_Iso1.length;

				if(NoIso==0)
				{
					Final_iso=new String[1];
					Final_iso[0]="FALSE";
					break;
				}
				else
				{
					Result2 = FindCvalue.ActivityCorrection_H1(Spc, Result2, PeakInfo,mstime ,FWHMCoeff,coeff, Eff_Coeff,WndROI);

					//Condition
					NoID = 0;

					for (int s = 0; s < NoIso; s++)
					{
						if (Result2.get(s).Act > 0)
						{
							if (Result2.get(s).Uncer <= Thshld_Un && Result2.get(s).RMSE <= Thshld_RMSE)
							{
								NoID = NoID + 1;
							}
						}
					}



					//Condition 1
					if (NoID > 0)
					{
						String[] Nu_new = new String[NoID];
						double[][][] List_InforIso2 = new double[NoID][NoListTrueEn][4];

						int cnt1 = 0;

						for (int i = 0; i < NoIso; i++)
						{
							if (Result2.get(i).Act > 0)
							{
								if (Result2.get(i).Uncer <= Thshld_Un && Result2.get(i).RMSE <= Thshld_RMSE)
								{
									Nu_new[cnt1] = List_Iso1[i];

									cnt1 = cnt1 + 1;
								}
							}

						}

						if (cnt1 == NoIso)
						{
							Final_iso = new String[Nu_new.length];
							for (int i = 0; i < Nu_new.length; i++)
							{
								Final_iso[i] = Nu_new[i];
							}

							break;
						}
						else
						{
							List_Iso1 = new String[cnt1];

							for (int i = 0; i < cnt1; i++)
							{
								List_Iso1[i] = Nu_new[i];

							}

							//remove
							NoIso = List_Iso1.length;
							boolean flag_ch = true;
							for (int e = 0; e < Result2.size(); e++)
							{
								flag_ch = true;
								for (int j = 0; j<NoIso; j++)
								{
									if (Result2.get(e).isotopes.equals(List_Iso1[j]))
									{
										flag_ch = false;
									}
								}
								if (flag_ch == true)
								{
									Result2.remove(e);
									e--;
								}

							}

						}
					}
					else
					{
						Final_iso = new String[1];
						Final_iso[0] = "FALSE";
						break;
					}


				}
			}


			//remove candidate
			//removed Result2

			NoIso = Final_iso.length;
			boolean flag_ch1 = true;
			for (int i = 0; i < Result2.size(); i++)
			{
				flag_ch1 = true;
				for (int j = 0; j<NoIso; j++)
				{
					if (Result2.get(i).isotopes.equals(Final_iso[j]))
					{
						flag_ch1 = false;
					}

				}

				if (flag_ch1 == true)
				{
					Result2.remove(i);
					i--;
				}

			}


			// Conditon for activity validation filter
			double[] Act = new double[Result2.size()];


			for (int i = 0; i < Result2.size(); i++)
			{
				Act[i] = Result2.get(i).Act;
			}

			//take thesold
			if (NoIso > 1)
			{

				Result2=C_Thshold_Filter(Result2,Act,Act_Thshld); //Threshold 5%
			}

		}
		return Result2;
	}

	public static Vector<Isotope> CValue_Filter_H(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo, double[] FWHMCoeff,	Coefficients coeff, double [] Eff_Coeff ,
												  double mstime,double WndROI, double Act_Thshld)
	{
		// Adding condition to prohibit Co57 and Tc99m
		Result2=NewNcAnalsys.AddCondition_Co57_TC99m(Result2, FWHMCoeff, coeff) ;

		//Adding condition to prohibit WGPU by logic when RGPu is IDed
		Result2=AddCondition_WGPu_RGPU(Result2, PeakInfo,FWHMCoeff,	coeff,WndROI);

		if(Result2.size()>0)
		{

			double Thshld_Un = 10;
			double Thshld_RMSE = 1.2;
			double Uncer_cs=100;


			//	double Thshld_Un = 20;
			//	double Thshld_RMSE = 2.0;

/*			double Thshld_Un = 20;
			double Thshld_RMSE = 1.5;*/

			// Step 1: Remove noise candidate to improve find activity by solving matrix


			ActivityConfidence FindCvalue = new ActivityConfidence();


			Result2=ScreeningProcess_1st(Spc, Result2, PeakInfo, FWHMCoeff, coeff, Eff_Coeff, mstime, Thshld_Un, Thshld_RMSE,WndROI);
			//Step 2:  Calculate activity


			String[] ListIso = new String[Result2.size()];

			for (int i = 0; i<Result2.size(); i++)
			{
				ListIso[i] = Result2.get(i).isotopes;
			}


			int NoIso = ListIso.length;

			String[] List_Iso1 = new String[NoIso];

			for (int i = 0; i < NoIso; i++)
			{
				List_Iso1[i] = ListIso[i];
			}

			String[] Final_iso = new String[]{};
			int NoID = 0;

			while (true)
			{
				NoIso = List_Iso1.length;

				if(NoIso==0)
				{
					Final_iso=new String[1];
					Final_iso[0]="FALSE";
					break;
				}
				else
				{
					Result2 = FindCvalue.ActivityCorrection_H1(Spc, Result2, PeakInfo,mstime ,FWHMCoeff,coeff, Eff_Coeff,WndROI);

					//2019.08.29: Adding condition threadshold
					//Starting
					boolean FlgThshld=false;
					Thshld_Un = 10;
					Thshld_RMSE = 1.2;

					for (int s = 0; s < NoIso; s++)
					{
						if (Result2.get(s).Act == 0) // have at least 1 source w/ Activty=0
						{
							FlgThshld=true;
						}
					}

					if(FlgThshld==true)
					{
						Thshld_Un = 20;
						Thshld_RMSE = 2;
					}
					else
					{
						Thshld_Un = 10;
						Thshld_RMSE = 1.2;
					}

					//end ---- 2019.08.29
					//Condition
					NoID = 0;

					for (int s = 0; s < NoIso; s++)
					{
						if (Result2.get(s).Act > 0)
						{
							//adding condition for cs-137
							if(AddExceptLogic_Cs(Result2.get(s))==true)
							{
								if (Result2.get(s).Uncer <= Uncer_cs && Result2.get(s).RMSE <= 10)
								{
									NoID = NoID + 1;
								}
							}
							else
							{
								if (Result2.get(s).Uncer <= Thshld_Un && Result2.get(s).RMSE <= Thshld_RMSE) {
									NoID = NoID + 1;
								}
							}
						}
					}



					//Condition 1
					if (NoID > 0)
					{
						String[] Nu_new = new String[NoID];
						double[][][] List_InforIso2 = new double[NoID][NoListTrueEn][4];

						int cnt1 = 0;

						for (int i = 0; i < NoIso; i++)
						{
							if (Result2.get(i).Act > 0)
							{
								//adding condition for cs-137
								if(AddExceptLogic_Cs(Result2.get(i))==true)
								{
									if (Result2.get(i).Uncer <= Uncer_cs && Result2.get(i).RMSE <= 10)
									{
										Nu_new[cnt1] = List_Iso1[i];

										cnt1 = cnt1 + 1;
									}
								}
								else {
									if (Result2.get(i).Uncer <= Thshld_Un && Result2.get(i).RMSE <= Thshld_RMSE)
									{
										Nu_new[cnt1] = List_Iso1[i];

										cnt1 = cnt1 + 1;
									}
								}
							}

						}

						if (cnt1 == NoIso)
						{
							Final_iso = new String[Nu_new.length];
							for (int i = 0; i < Nu_new.length; i++)
							{
								Final_iso[i] = Nu_new[i];
							}

							break;
						}
						else
						{
							List_Iso1 = new String[cnt1];

							for (int i = 0; i < cnt1; i++)
							{
								List_Iso1[i] = Nu_new[i];

							}

							//remove
							NoIso = List_Iso1.length;
							boolean flag_ch = true;
							for (int e = 0; e < Result2.size(); e++)
							{
								flag_ch = true;
								for (int j = 0; j<NoIso; j++)
								{
									if (Result2.get(e).isotopes.equals(List_Iso1[j]))
									{
										flag_ch = false;
									}
								}
								if (flag_ch == true)
								{
									Result2.remove(e);
									e--;
								}

							}

						}
					}
					else
					{
						Final_iso = new String[1];
						Final_iso[0] = "FALSE";
						break;
					}


				}
			}


			//remove candidate
			//removed Result2

			NoIso = Final_iso.length;
			boolean flag_ch1 = true;
			for (int i = 0; i < Result2.size(); i++)
			{
				flag_ch1 = true;
				for (int j = 0; j<NoIso; j++)
				{
					if (Result2.get(i).isotopes.equals(Final_iso[j]))
					{
						flag_ch1 = false;
					}

				}

				if (flag_ch1 == true)
				{
					Result2.remove(i);
					i--;
				}

			}


			// Conditon for activity validation filter
			double[] Act = new double[Result2.size()];


			for (int i = 0; i < Result2.size(); i++)
			{
				Act[i] = Result2.get(i).Act;
			}

			//take thesold
			if (NoIso > 1)
			{

				Result2=C_Thshold_Filter(Result2,Act,Act_Thshld); //Threshold 5%
			}

		}
		return Result2;
	}


	public static Vector<Isotope> C_Thshold_Filter(Vector<Isotope> Result2,double[] Act,double UnLimit)
	{

		int MaxInd0 = 0;// Index of Reference isotope
		double MaxAct = 0;
		int NoIso = Result2.size();
		double SumAct=0;

		for (int i = 0; i < NoIso; i++)
		{
			if (Act[i] > MaxAct)
			{
				MaxAct = Act[i];
				MaxInd0 = i;
			}
			SumAct=SumAct+Act[i];
		}

		double Ratio;

		for (int i=0;i<Result2.size();i++)
		{
			Ratio= Result2.get(i).Act/SumAct;
			if(Ratio<UnLimit)
			{
				Result2.remove(i);
				i--;
			}

		}
		return Result2;
	}
	public static Vector<NcPeak> CValue_Return_UnclaimedEn(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo, double[] FWHMCoeff,	Coefficients coeff, double [] Eff_Coeff , double mstime,double WndROI)
	{

		double Thshld_Un = 10;
		double Thshld_RMSE = 1.2;


		// Step 1: Remove noise candidate to improve find activity by solving matrix

		ActivityConfidence FindCvalue = new ActivityConfidence();

		Vector<NcPeak> UnClaimedPeakInfo = new Vector<NcPeak>();

		UnClaimedPeakInfo = FindCvalue.Return_UnclaimedEn(Spc, Result2, PeakInfo,mstime ,FWHMCoeff,coeff, Eff_Coeff,WndROI);


		return UnClaimedPeakInfo;
	}

	public static Vector<Isotope> IsotopeID_UnClaimedLine(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,	Coefficients coeff, double [] Eff_Coeff , double mstime,double WndROI)
	{
		double [] EnCalCoeff=new double [3];
		EnCalCoeff[0] = coeff.get_Coefficients()[0];
		EnCalCoeff[1]= coeff.get_Coefficients()[1];
		EnCalCoeff[2]= coeff.get_Coefficients()[2];

		//Hung Modified: 17/11/22

		int NoSrc=Result2.size();
		int []NoLineEn=new int [NoSrc];


		double [] TruePeak=new double [100];


		double[][] PeakEnBr_Ref = new double[][] {};

		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;

		double roi_tmp, Left_thsld, High_thsld;

		double []Thshold=new double[2];

		double TrueKeV, TrueBr;
		int CountLineEn;
		boolean Flag;
		for(int i=0;i<NoSrc;i++)
		{
			CountLineEn=0;
			PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(Result2.get(i));

			NoMaxEn = PeakEnBr_Ref.length;

			for(int k=0;k<NoMaxEn;k++)
			{
				TrueKeV=PeakEnBr_Ref[k][0];
				TrueBr=PeakEnBr_Ref[k][1];

				Flag=false;

				if(TrueBr>0)
				{
					Thshold=Get_Roi_window_by_energy_used_FWHM(TrueKeV, FWHMCoeff,coeff,WndROI);

					Left_thsld=Thshold[0];
					High_thsld=Thshold[1];

					for(int j=0;j<PeakInfo.size();j++)
					{
						EnTmp = PeakInfo.get(j).Peak_Energy;

						if (EnTmp >= Left_thsld && EnTmp <= High_thsld)
						{
							Flag=true;
						}
					}

				}

				if(Flag==true)
				{
					CountLineEn=CountLineEn+1;
				}
			}

			NoLineEn[i]=CountLineEn;

		}

		int []NoLineEn1=new int [NoSrc];
		int []NoLineEn2=new int [NoSrc];

		for(int i=0;i<NoSrc;i++)
		{
			NoLineEn1[i]=NoLineEn[i];
			NoLineEn2[i]=NoLineEn[i];
		}



		double Thsld1=0.3*PeakInfo.size();


		//Step 1: Select 1st data
		int [] ListMax1=new int [NoSrc];
		int [] ListMax2=new int [NoSrc];
		int [] ListCand1=new int[NoSrc];
		int [] ListCand2=new int[NoSrc];


		int cnt_src1=0, cnt_src2=0;
		int index1;

		for(int k=0;k<1;k++)
		{
			ListMax1=ReListIndMax(ListMax1,NoLineEn1,Thsld1);

			for(int j=0;j<NoSrc;j++)
			{
				if(ListMax1[j]>0)
				{


					index1=ListMax1[j]-1;

					ListCand1[index1]=1;

					cnt_src1=cnt_src1+1;
					NoLineEn1[index1]=0;
					ListMax1[j]=0;

				}
			}

		}


		//Step 2: Select step
		cnt_src2=0;
		for(int k=0;k<2;k++)
		{
			ListMax2=ReListIndMax(ListMax2,NoLineEn2,Thsld1);

			for(int j=0;j<NoSrc;j++)
			{
				if(ListMax2[j]>0)
				{


					index1=ListMax2[j]-1;

					ListCand2[index1]=1;

					cnt_src2=cnt_src2+1;
					NoLineEn2[index1]=0;
					ListMax2[j]=0;
				}
			}

		}



		//to remove



		//select candidate
		int indextmp;
		if(cnt_src2<PeakInfo.size())
		{
			for(int i=0;i<NoSrc;i++)
			{
				if(ListCand2[i]==0)
				{
					Result2.get(i).Act=-1;
				}
			}



		}
		else
		{

			for(int i=0;i<NoSrc;i++)
			{
				if(ListCand1[i]==0)
				{
					Result2.get(i).Act=-1;
				}
			}
		}

		//remove
		for (int i = 0; i < Result2.size(); i++)
		{

			if(Result2.get(i).Act==-1)
			{
				Result2.remove(i);
				i--;
			}

		}

		//Calculate activity

		if(Result2.size()>0)
		{
			ActivityConfidence FindCvalue = new ActivityConfidence();

			Result2 = FindCvalue.ActivityCorrection_H1(Spc, Result2, PeakInfo,mstime ,FWHMCoeff,coeff, Eff_Coeff,WndROI);

			for (int e = 0; e < Result2.size(); e++)
			{
				if (Result2.get(e).Act < 0)
				{
					Result2.remove(e);
					e--;
				}

			}

		}
		return Result2;
	}

	public static int[] ReListIndMax(int []ListMax,int[] Data, double thsld)
	{
		int NoMax=Data.length;

		int max1=0;

		for(int i=0;i<NoMax;i++)
		{
			if(Data[i]>thsld)
			{
				if(Data[i]>max1)
				{
					max1=Data[i];

				}
			}
		}

		//int []ListMax=new int [NoMax];

		int cnt=0;
		if(max1>thsld)
		{
			cnt=0;
			for(int i=0;i<NoMax;i++)
			{
				if(Data[i]==max1)
				{
					ListMax[cnt]=i+1;
					cnt=cnt+1;
				}
			}
		}

		return ListMax;
	}
	public static Vector<Isotope> AddingIsotope(Vector<Isotope> Result1,Vector<Isotope> Result2) //1+2=3
	{
		Vector<Isotope> result3 = new Vector<Isotope>();

		Isotope mIso;

		for(int i=0;i<Result1.size();i++)
		{
			mIso=Result1.get(i);
			result3.add(mIso);
		}

		for(int i=0;i<Result2.size();i++)
		{
			mIso=Result2.get(i);
			result3.add(mIso);
		}


		return result3;
	}

	public static Vector<Isotope> AddCondition_WGPu_RGPU(Vector<Isotope> Result2, Vector<NcPeak> PeakInfo, double[] FWHMCoeff,	Coefficients coeff,double WndROI)
	{

		boolean WGPu_Flg=false,RGPu_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("WGPu"))
			{
				WGPu_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("RGPu"))
			{
				RGPu_Flg=true;
			}
		}

		//if two sources are exist
		//if 662 and 1001
		if(WGPu_Flg==true&&RGPu_Flg==true)
		{
			double EnTmp;
			double [] Thshold1=new double [2];
			double [] Thshold2=new double [2];
			double Left_thsld1,High_thsld1,Left_thsld2,High_thsld2;
			boolean Flg_662=false, Flg_1001=false;


			Thshold1=Get_Roi_window_by_energy_used_FWHM(662, FWHMCoeff,coeff,WndROI);

			Left_thsld1=Thshold1[0];
			High_thsld1=Thshold1[1];

			Thshold2=Get_Roi_window_by_energy_used_FWHM(1001, FWHMCoeff,coeff,WndROI);

			Left_thsld2=Thshold2[0];
			High_thsld2=Thshold2[1];


			for (int i = 0; i < PeakInfo.size(); i++)
			{
				EnTmp= PeakInfo.get(i).Peak_Energy;

				if(EnTmp>=Left_thsld1&&EnTmp<=High_thsld1)
				{
					Flg_662=true;
				}

				if(EnTmp>=Left_thsld2&&EnTmp<=High_thsld2)
				{
					Flg_1001=true;
				}
			}

			//remove WGPu
			if(Flg_662==true&&Flg_1001==true)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("WGPu"))
					{
						Result2.remove(i);
						--i;
					}
				}
			}
			//remove RGPu
			else
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("RGPu"))
					{
						Result2.remove(i);
						--i;
					}
				}
			}
		}


		return Result2;
	}


	//0109
	public static Vector<Isotope> ScreeningProcess_1st_old(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo, double[] FWHMCoeff,	Coefficients coeff, double [] Eff_Coeff , double mstime,double Thshld_Un, double Thshld_rmse,double WndROI)
	{
		ActivityConfidence FindCvalue = new ActivityConfidence();

		int NoIso=Result2.size();


		if(NoIso>0)
		{
			int NoMaxCombine=0;

			//1st: Determine number combination
			if(NoIso<=2)
			{
				NoMaxCombine=NoIso;
			}
			else
			{
				NoMaxCombine=2;
			}


			// for single source and mix soures
			int [][]A;
			int NoRowA=0, NoColA=0;
			int index,cnt=0;
			int cnt_ID_Remove=0;

			String[] ListIso_Remove = new String[1000];

			for(int i=0;i<NoMaxCombine;i++)
			{


				A=CalCombination(NoIso,i+1);

				NoRowA=A.length;
				NoColA=A[0].length;

				cnt=0;

				for(int j=0;j<NoRowA;j++)
				{

					Vector<Isotope> ResultTemp = new Vector<Isotope>();


					for(int k=0;k<NoColA;k++)
					{

						Isotope pData = new Isotope();

						index=A[j][k];

						if(index>0)
						{
							index=index-1;



							pData.Peaks=Result2.get(index).Peaks;
							pData.Unknown_Peak=Result2.get(index).Unknown_Peak;
							pData.FoundPeaks=Result2.get(index).FoundPeaks;
							pData.FoundPeakBR=Result2.get(index).FoundPeakBR;

							pData.IsoPeakEn=Result2.get(index).IsoPeakEn;
							pData.IsoMinorPeakEn=Result2.get(index).IsoMinorPeakEn;
							pData.IsoMinorPeakBR=Result2.get(index).IsoMinorPeakBR;
							pData.C=Result2.get(index).C;


							pData.HelpVideo = Result2.get(index).HelpVideo;

							pData.isotopes = Result2.get(index).isotopes;

							pData.DoseRate = Result2.get(index).DoseRate;
							pData.DoseRate_S = Result2.get(index).DoseRate_S;
							pData.Class = Result2.get(index).Class;
							pData.ClassColor = Result2.get(index).ClassColor;
							pData.Comment = Result2.get(index).Comment;
							pData.measure_eff = Result2.get(index).measure_eff;
							pData.Confidence_Level = Result2.get(index).Confidence_Level;

							pData.Index1 = Result2.get(index).Index1;
							pData.Index2 = Result2.get(index).Index2;


							pData.Act = Result2.get(index).Act;
							pData.Uncer = Result2.get(index).Uncer;
							pData.RMSE = Result2.get(index).RMSE;


							ResultTemp.add(pData);


						}
					}

					ResultTemp = FindCvalue.ActivityCorrection_H1(Spc, ResultTemp, PeakInfo,mstime ,FWHMCoeff,coeff, Eff_Coeff,WndROI);


					for (int s = 0; s < ResultTemp.size(); s++)
					{
						if (ResultTemp.get(s).Act < 0 || ResultTemp.get(s).Uncer >Thshld_Un )
						{
							ListIso_Remove[cnt_ID_Remove] = ResultTemp.get(s).isotopes;
							cnt_ID_Remove=cnt_ID_Remove+1;
						}
					}

				}

			}



			//removed
			if(cnt_ID_Remove>0)
			{
				boolean Flg=false;
				for (int i = 0; i < Result2.size(); i++)
				{
					Flg=false;

					for(int j=0;j<cnt_ID_Remove;j++)
					{
						if (Result2.get(i).isotopes.equals(ListIso_Remove[j]))
						{
							Flg=true;
						}
					}

					if(Flg==true)
					{
						Result2.remove(i);
						--i;
					}
				}
			}

		}

		//Calculation
		return Result2;

	}

	public static Vector<Isotope> ScreeningProcess_1st(double[] Spc, Vector<Isotope> Result2, Vector<NcPeak> PeakInfo, double[] FWHMCoeff,	Coefficients coeff, double [] Eff_Coeff , double mstime,double Thshld_Un, double Thshld_rmse,double WndROI)
	{
		ActivityConfidence FindCvalue = new ActivityConfidence();

		int NoIso=Result2.size();


		if(NoIso>0)
		{
			int NoMaxCombine=0;

			//1st: Determine number combination
			if(NoIso<=2)
			{
				NoMaxCombine=NoIso;
			}
			else
			{
				NoMaxCombine=2;
			}


			// for single source and mix soures
			int [][]A;
			int NoRowA=0, NoColA=0;
			int index,cnt=0;
			int cnt_ID_Remove=0;

			String[] ListIso_Remove = new String[1000];

			for(int i=0;i<NoMaxCombine;i++)
			{


				A=CalCombination(NoIso,i+1);

				NoRowA=A.length;
				NoColA=A[0].length;

				cnt=0;

				for(int j=0;j<NoRowA;j++)
				{

					Vector<Isotope> ResultTemp = new Vector<Isotope>();


					for(int k=0;k<NoColA;k++)
					{

						Isotope pData = new Isotope();

						index=A[j][k];

						if(index>0)
						{
							index=index-1;



							pData.Peaks=Result2.get(index).Peaks;
							pData.Unknown_Peak=Result2.get(index).Unknown_Peak;
							pData.FoundPeaks=Result2.get(index).FoundPeaks;
							pData.FoundPeakBR=Result2.get(index).FoundPeakBR;

							pData.IsoPeakEn=Result2.get(index).IsoPeakEn;
							pData.IsoMinorPeakEn=Result2.get(index).IsoMinorPeakEn;
							pData.IsoMinorPeakBR=Result2.get(index).IsoMinorPeakBR;
							pData.C=Result2.get(index).C;


							pData.HelpVideo = Result2.get(index).HelpVideo;

							pData.isotopes = Result2.get(index).isotopes;

							pData.DoseRate = Result2.get(index).DoseRate;
							pData.DoseRate_S = Result2.get(index).DoseRate_S;
							pData.Class = Result2.get(index).Class;
							pData.ClassColor = Result2.get(index).ClassColor;
							pData.Comment = Result2.get(index).Comment;
							pData.measure_eff = Result2.get(index).measure_eff;
							pData.Confidence_Level = Result2.get(index).Confidence_Level;

							pData.Index1 = Result2.get(index).Index1;
							pData.Index2 = Result2.get(index).Index2;


							pData.Act = Result2.get(index).Act;
							pData.Uncer = Result2.get(index).Uncer;
							pData.RMSE = Result2.get(index).RMSE;


							ResultTemp.add(pData);


						}
					}

					ResultTemp = FindCvalue.ActivityCorrection_H1(Spc, ResultTemp, PeakInfo,mstime ,FWHMCoeff,coeff, Eff_Coeff,WndROI);


					for (int s = 0; s < ResultTemp.size(); s++)
					{
						//add more
						//old
						/*
						if (ResultTemp.get(s).Act < 0 || ResultTemp.get(s).Uncer >Thshld_Un )
						{
							ListIso_Remove[cnt_ID_Remove] = ResultTemp.get(s).isotopes;
							cnt_ID_Remove=cnt_ID_Remove+1;
						}
						*/
						if(AddExceptLogic_Cs(ResultTemp.get(s))==true)
						{
							if (ResultTemp.get(s).Act < 0 || ResultTemp.get(s).Uncer >100 )
							{
								ListIso_Remove[cnt_ID_Remove] = ResultTemp.get(s).isotopes;
								cnt_ID_Remove=cnt_ID_Remove+1;
							}
						}
						else
						{
							if (ResultTemp.get(s).Act < 0 || ResultTemp.get(s).Uncer >Thshld_Un )
							{
								ListIso_Remove[cnt_ID_Remove] = ResultTemp.get(s).isotopes;
								cnt_ID_Remove=cnt_ID_Remove+1;
							}

						}
					}

				}

			}



			//removed
			if(cnt_ID_Remove>0)
			{
				boolean Flg=false;
				for (int i = 0; i < Result2.size(); i++)
				{
					Flg=false;

					for(int j=0;j<cnt_ID_Remove;j++)
					{
						if (Result2.get(i).isotopes.equals(ListIso_Remove[j]))
						{
							Flg=true;
						}
					}

					if(Flg==true)
					{
						Result2.remove(i);
						--i;
					}
				}
			}

		}

		//Calculation
		return Result2;

	}

	//0109
	public static int [][] CalCombination(int n, int r)
	{

		int NCR=nChoosek(n,r); //number combination

		int [][] A=new int [NCR][r];


		if(NCR>0)
		{
			//combination of 1
			// C(n,2)

			if(r==1)
			{
				int cnt=0;
				for (int i=0;i<n;i++)
				{
					A[cnt][0]=i+1;
					cnt=cnt+1;
				}
			}


			//combination of 2
			// C(n,2)

			if(r==2)
			{
				int cnt=0;

				for (int i=0;i<n;i++)
				{
					for(int j=i+1;j<n;j++)
					{
						A[cnt][0]=i+1;
						A[cnt][1]=j+1;
						cnt=cnt+1;
					}
				}
			}


			//combination of 3
			// C(n,3)


			if(r==3)
			{
				int cnt=0;

				for (int i=0;i<n;i++)
				{
					for(int j=i+1;j<n;j++)
					{
						for(int k=j+1;k<n;k++)
						{
							A[cnt][0]=i+1;
							A[cnt][1]=j+1;
							A[cnt][2]=k+1;
							cnt=cnt+1;
						}

					}
				}
			}


		}


		return A;
	}

	//0109
	public static int nChoosek(int n, int r)
	{
		int result = n;
		if(r>n)    result=0;

		if(r*2>n)   r=n-r;

		if(r==0)    result=1;

		for (int i=2;i<=r;i++)
		{
			result=result*(n-i+1);

			result=(int)((double) result/(double)i);
		}

		return result;
	}

	//condition for Co57 and Tc99m
	//Date: 18.02.21
	public static Vector<Isotope> AddCondition_Co57_TC99m(Vector<Isotope> Result2, double[] FWHMCoeff,Coefficients coeff)
	{
		boolean Co57_Flg=false,Tc99m_Flg=false;

		double En_Co57=0, En_Tc99m=0;

		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Co-57"))
			{
				Co57_Flg=true;
				En_Co57=Result2.get(i).FoundPeaks.get(0).Peak_Energy;

			}

			if (Result2.get(i).isotopes.equals("Tc-99m"))
			{
				Tc99m_Flg=true;
				En_Tc99m=Result2.get(i).FoundPeaks.get(0).Peak_Energy;
			}
		}

		//if two sources are exist
		//if 662 and 1001

		if(Co57_Flg==true&&Tc99m_Flg==true)
		{
			double EnTmp;
			double [] Thshold1=new double [2];
			double [] Thshold2=new double [2];
			double Left_thsld1,High_thsld1,Left_thsld2,High_thsld2;
			boolean Flg_Co=false, Flg_Tc=false;


			Thshold1=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(123, FWHMCoeff,coeff,WndRatio_Co57_Tc99m);

			Left_thsld1=Thshold1[0];
			High_thsld1=Thshold1[1];

			Thshold2=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(141, FWHMCoeff,coeff,WndRatio_Co57_Tc99m);

			Left_thsld2=Thshold2[0];
			High_thsld2=Thshold2[1];


			if(En_Co57>=Left_thsld1&&En_Co57<=High_thsld1)
			{
				Flg_Co=true;
			}

			if(En_Tc99m>=Left_thsld2&&En_Tc99m<=High_thsld2)
			{
				Flg_Tc=true;
			}


			//remove Co57
			if(Flg_Co==false)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("Co-57"))
					{

						Result2.remove(i);
						--i;
					}

				}
			}

			//remove Tc99m
			if(Flg_Tc ==false)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("Tc-99m"))
					{

						Result2.remove(i);
						--i;
					}

				}
			}
		}


		return Result2;
	}




	//180326
	public static Vector<Isotope> AddCondition_Ra_Ba(Vector<Isotope> Result2)
	{

		boolean Ra_Flg=false,Ba_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Ra-226"))
			{
				Ra_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("Ba-133"))
			{
				Ba_Flg=true;
			}
		}

		//if two sources are exist
		//if 662 and 1001
		if(Ra_Flg==true&&Ba_Flg==true)
		{
			for (int i = 0; i < Result2.size(); i++)
			{
				if (Result2.get(i).isotopes.equals("Ba-133"))
				{
					Result2.remove(i);
					--i;
				}

			}
		}


		return Result2;
	}

	//Hung.18.05.16
	//Logic: Remove Compton Edge for Cs137 and
	public static Vector<Isotope> LogicComptonPeakCs_Co60(Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{

		if(Result2.size()>0)
		{
			Result2=NewNcAnalsys.LogicComptonPeak("Cs-137", Result2, PeakInfo, FWHMCoeff, coeff, WinROI);
			Result2=NewNcAnalsys.LogicComptonPeak("Co-60", Result2, PeakInfo, FWHMCoeff, coeff, WinROI);
		}



		return Result2;
	}


	public static Vector<Isotope> LogicComptonPeak(String IsoName,Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{

		double []CEPeak=new double[NoMaxCEPeak];
		int NoCEPeak=0;

		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals(IsoName))
			{
				CEPeak=FinCEPeak(Result2.get(i), PeakInfo, FWHMCoeff, coeff, WinROI);
			}
		}

		for(int j=0;j<NoMaxCEPeak;j++)
		{
			if(CEPeak[j]>0)
			{
				NoCEPeak=NoCEPeak+1;
			}
		}

		if(NoCEPeak>0)
		{
			Result2=CELogicPeak(Result2, CEPeak,NoCEPeak,PeakInfo,FWHMCoeff, coeff, WinROI);
		}
		return Result2;
	}

	public static double[][] Get_Minor_PeakEn_BR(Isotope mIso)
	{

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

	//Compton Peak logic
	//Only Search:

	public static Vector<Isotope> CELogicPeak(Vector<Isotope> Result2,double [] CEPeak,int NoCEPeak, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{

		double ErgTemp;

		for(int i=0;i<Result2.size();i++)
		{

			int NoFoundCE=0;
			for(int j=0;j<Result2.get(i).FoundPeaks.size();j++)
			{
				ErgTemp=Result2.get(i).FoundPeaks.get(j).Peak_Energy;

				for(int k=0;k<NoCEPeak;k++)
				{
					if(CEPeak[k]==ErgTemp)
					{
						NoFoundCE=NoFoundCE+1;
					}
				}
			}

			//Remove source

			if(NoFoundCE>0)
			{
				if(NoFoundCE==Result2.get(i).FoundPeaks.size())
				{
					Result2.remove(i);
					--i;

					//boolean flg=CheckMinorPeak( Result2.get(i),PeakInfo,FWHMCoeff, coeff, WinROI);

					//if(flg==false)
					//{
					//	Result2.remove(i);
					//	--i;
					//}
				}
			}

		}
		return Result2;
	}

	public static double [] FinCEPeak(Isotope mIso,Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{
		double [] CEPeak=new double [NoMaxCEPeak];

		double [][] PeakBr_minor=Get_Minor_PeakEn_BR(mIso);
		double [] Thshold1=new double [2];
		double Left_thsld1,High_thsld1;

		int NoMaxEn = PeakBr_minor.length;
		double EnergyTmp;


		double  erg0,br0;

		int  NoCEPeak=0;

		boolean Flg=false;

		NoCEPeak=0;
		for(int i=0;i<PeakInfo.size();i++)
		{
			EnergyTmp=PeakInfo.get(i).Peak_Energy;
			Flg=false;

			for(int j=0;j<NoMaxEn;j++)
			{
				erg0=PeakBr_minor[j][0];	//C.E peak from Idea

				br0=PeakBr_minor[j][1];    //C.E peak from Idea

				if(br0==0)
				{
					Thshold1=Get_Roi_window_by_energy_used_FWHM(erg0, FWHMCoeff,coeff,WinROI);

					Left_thsld1=Thshold1[0];
					High_thsld1=Thshold1[1];

					if(EnergyTmp>=Left_thsld1&&EnergyTmp<=High_thsld1)
					{
						Flg=true;
					}
				}
			}

			//Step 2: Select C.E peak
			if(Flg==true)
			{
				if(NoCEPeak<NoMaxCEPeak)
				{
					CEPeak[NoCEPeak]=EnergyTmp;
					NoCEPeak=NoCEPeak+1;
				}
			}

		}

		return CEPeak;
	}

	public static boolean CheckMinorPeak(Isotope mIso,Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{
		double [][] PeakBr_minor=Get_Minor_PeakEn_BR(mIso);

		double [] Thshold1=new double [2];
		double Left_thsld1,High_thsld1;

		int NoMaxEn = PeakBr_minor.length;
		double EnergyTmp;


		double  erg0,br0;

		boolean Flg=false;

		for(int i=0;i<PeakInfo.size();i++)
		{
			EnergyTmp=PeakInfo.get(i).Peak_Energy;
			for(int j=0;j<NoMaxEn;j++)
			{
				erg0=PeakBr_minor[j][0];	//C.E peak from Idea

				br0=PeakBr_minor[j][1];    //C.E peak from Idea

				if(br0>0.1&&erg0>40) //br0: with Branching 10%
				{
					Thshold1=Get_Roi_window_by_energy_used_FWHM(erg0, FWHMCoeff,coeff,WinROI);

					Left_thsld1=Thshold1[0];
					High_thsld1=Thshold1[1];

					if(EnergyTmp>=Left_thsld1&&EnergyTmp<=High_thsld1)
					{
						Flg=true;
					}
				}
			}
		}

		return Flg;
	}

	public static Vector<Isotope> AddCondition_Ra_Ba(Vector<Isotope> Result2,Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff, double WinROI,double []Eff_Coeff, double Acqtime,double Thrshld_Index2_MinorMajorPeak, double ActThrshld)
	{
		boolean Ra_Flg=false,Ba_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Ra-226"))
			{
				Ra_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("Ba-133"))
			{
				Ba_Flg=true;
			}
		}

		if(Ra_Flg==true&&Ba_Flg==true)
		{
			double [] ReIndex=new double [2];
			for (int i = 0; i < Result2.size(); i++)
			{
				if (Result2.get(i).isotopes.equals("Ba-133"))
				{
					ReIndex=IndexFillter_Major_Minor_H(Result2.get(i), PeakInfo, Acqtime,FWHMCoeff, coeff, Eff_Coeff,WinROI);

					if(ReIndex[1]<Thrshld_Index2_MinorMajorPeak)
					{
						Result2.remove(i);
						--i;
					}
				}

			}
		}



		//again
		Ra_Flg=false;Ba_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Ra-226"))
			{
				Ra_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("Ba-133"))
			{
				Ba_Flg=true;
			}
		}



		//if two sources are exist
		//if 662 and 1001
		if(Ra_Flg==true&&Ba_Flg==true)
		{

			//1st confidence: Confidence Index


			// End
			//Checking 32 keV still
			double en1=295,en2=352, en3=609,en4=1120,en5=1768;
			double br1=0.18, br2=0.32,br3=0.42,br4=0.15,br5=0.15; //branching ratio from Mark
			//double br1=0.044,br2=0.055,br3=0.455,br4=0.149,br5=0.153;
			//double br1_ba=0.1517, br2_ba=0.71;
			double br1_ba=0.26, br2_ba=0.71;
			double[] Flag_295kev=Peak_Is_ROI(en1,PeakInfo,FWHMCoeff,coeff,WinROI);
			double []Flag_352kev=Peak_Is_ROI(en2,PeakInfo,FWHMCoeff,coeff,WinROI);
			double [] Flag_609kev=Peak_Is_ROI(en3,PeakInfo,FWHMCoeff,coeff,WinROI);
			double [] Flag_1120kev=Peak_Is_ROI(en4,PeakInfo,FWHMCoeff,coeff,WinROI);
			double [] Flag_1768kev=Peak_Is_ROI(en5,PeakInfo,FWHMCoeff,coeff,WinROI);

			//	double mCheck=Flag_295kev[0]*Flag_352kev[0]*Flag_609kev[0]*Flag_1120kev[0]*Flag_1768kev[0];
			double mCheck=Flag_295kev[0]*Flag_352kev[0]*Flag_609kev[0];

			if(mCheck>0)
			{
				double eff1=Get_Eff(en1,Eff_Coeff);
				double eff2=Get_Eff(en2,Eff_Coeff);
				double eff3=Get_Eff(en3,Eff_Coeff);
				double eff4=Get_Eff(en4,Eff_Coeff);
				double eff5=Get_Eff(en5,Eff_Coeff);

				//double Act_Ra=(Flag_609kev[2]/eff3/br3/Acqtime+Flag_1120kev[2]/eff4/br4/Acqtime+Flag_1768kev[2]/eff5/br5/Acqtime)/3.0;
				double Act_Ra=(Flag_609kev[2]/eff3/br3/Acqtime);
				double NetRa_295=Act_Ra*eff1*Acqtime*br1;
				double NetRa_352=Act_Ra*eff2*Acqtime*br2;

				double NetBa_352=Flag_352kev[2]-NetRa_352;

				double NetBa_295=Flag_295kev[2]-NetRa_295;

				double Act_Ba=(NetBa_352/eff2/br2_ba/Acqtime+NetBa_295/eff2/br1_ba/Acqtime)/2.0;

				boolean Remove_Ba=false;

				if(Act_Ba>0)
				{
					double sumAct=Act_Ba+Act_Ra;

					double ratio=Act_Ba/sumAct;

					if(ratio<ActThrshld)
					{
						Remove_Ba=true;
					}
				}
				else
				{
					Remove_Ba=true;
				}


				if(Remove_Ba==true)
				{
					for (int i = 0; i < Result2.size(); i++)
					{
						if (Result2.get(i).isotopes.equals("Ba-133"))
						{
							Result2.remove(i);
							--i;
						}

					}
				}

			}


		}

		return Result2;

	}



	public static Vector<Isotope> AddCondition_Exception_Isopte(String ExpectedIso, String UnExpectIsotope,Vector<Isotope> Result2,Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff, double WinROI,double []Eff_Coeff, double Acqtime,
																double Thrshld_Index2, double ActThshld )
	{

		//Step 1: Using all major and minor by calculating index for all major and minor peak
		boolean Ra_Flg=false,UnExpectIsotope_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals(ExpectedIso))
			{
				Ra_Flg=true;
			}

			if (Result2.get(i).isotopes.equals(UnExpectIsotope))
			{
				UnExpectIsotope_Flg=true;
			}
		}

		if(Ra_Flg==true&&UnExpectIsotope_Flg==true)
		{

			double [] ReIndex=new double [2];
			for (int i = 0; i < Result2.size(); i++)
			{
				if (Result2.get(i).isotopes.equals(UnExpectIsotope))
				{
					ReIndex=IndexFillter_Major_Minor_H(Result2.get(i), PeakInfo, Acqtime,FWHMCoeff, coeff, Eff_Coeff,WinROI);

					if(ReIndex[1]<Thrshld_Index2)
					{
						Result2.remove(i);
						--i;
					}
				}
			}
		}



		//Using again activity for comfirmation
		Ra_Flg=false;UnExpectIsotope_Flg=false;

		double Act_Ra=0,Act_UnExpectIsotope=0;
		double Sum_Act=0;
		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals(ExpectedIso))
			{
				Ra_Flg=true;
				Act_Ra=Result2.get(i).Act;
			}

			if (Result2.get(i).isotopes.equals(UnExpectIsotope))
			{
				UnExpectIsotope_Flg=true;
				Act_UnExpectIsotope=Result2.get(i).Act;
			}

			Sum_Act=Sum_Act+Result2.get(i).Act;
		}

		if(Ra_Flg==true&&UnExpectIsotope_Flg==true)
		{

			double Ratio=Act_UnExpectIsotope/Sum_Act;

			if(Ratio<ActThshld)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals(UnExpectIsotope))
					{
						Result2.remove(i);
						--i;
					}
				}
			}
		}

		return Result2;
	}
	public static double [] Peak_Is_ROI(double en,Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI )
	{
		double [] Result=new double [3]; //Result[0]: value 0 or 1 (not or yes), Result[1]: PeakEn, Result[2]: Count


		double [] Thshold1=new double [2];
		double Left_thsld1,High_thsld1;
		double EnergyTmp=0;

		Thshold1=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(en, FWHMCoeff,coeff,WinROI);

		Left_thsld1=Thshold1[0];
		High_thsld1=Thshold1[1];

		for(int i=0;i<PeakInfo.size();i++)
		{
			EnergyTmp=PeakInfo.get(i).Peak_Energy;

			if(EnergyTmp>=Left_thsld1&&EnergyTmp<=High_thsld1)
			{
				Result[0]=1;
				Result[1]=EnergyTmp;
				Result[2]=PeakInfo.get(i).NetCnt;
			}
		}

		return Result;

	}

	public static double [] IndexFillter_Major_Minor_H(Isotope result2, Vector<NcPeak> PeakInfo_new,double MsTime,double[] FWHMCoeff, Coefficients coeff, double[] Eff_Coef,double WinROI)
	{
		//Hung Modified: 17/11/22
		double [] ListIsoEn=new double [100];
		double [] ListIsoBr=new double [100];
		double [] ListFoundEn=new double [100];
		double [] ListFoundBr=new double [100];


		double[][] PeakEnBr_Ref = new double[][] {};
		int NoMaxEn;
		int cntPeakTrue=0;
		double EnTmp,BrTmp;
		double sumListIsoBr=0;

		PeakEnBr_Ref = Get_MajorMinor_PeakEn_BR(result2);

		NoMaxEn = PeakEnBr_Ref.length;
		//Except Ba-133 because Ba-133: 32keV 109% Branching, this peak is x-ray peak
		if (result2.isotopes.equals("Ba-133"))
		{
			for(int i=0;i<NoMaxEn;i++)
			{
				EnTmp = PeakEnBr_Ref[i][0];
				BrTmp= PeakEnBr_Ref[i][1];
				if(EnTmp==32) //32 keV
				{
					PeakEnBr_Ref[i][1]=0;

				}
			}
		}


		for (int j = 0; j < NoMaxEn; j++)
		{
			EnTmp = PeakEnBr_Ref[j][0];
			BrTmp= PeakEnBr_Ref[j][1];


			if(BrTmp>0)
			{
				ListIsoEn[cntPeakTrue]=EnTmp;
				ListIsoBr[cntPeakTrue]=BrTmp;

				sumListIsoBr=sumListIsoBr+BrTmp;

				cntPeakTrue=cntPeakTrue+1;
			}

		}


		//Find Peak: based on minor and major peak

		//remove noise peak
		int FoundPeakSize= PeakInfo_new.size();
		double roi_tmp,Left_thsld,High_thsld;

		double[] Thshold = new double[2];


		for(int i=0;i<cntPeakTrue;i++)
		{
			Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(ListIsoEn[i], FWHMCoeff, coeff,WinROI);
			Left_thsld = Thshold[0];
			High_thsld = Thshold[1];

			for (int j=0; j<FoundPeakSize;j++)
			{
				EnTmp= PeakInfo_new.get(j).Peak_Energy;

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					ListFoundEn[i]=EnTmp;
					ListFoundBr[i]=ListIsoBr[i];
				}
			}

		}



		//Step 2: For each observed peak,confidence of each peak
		double Index1=1;

		double [] Thshld=new double [2];
		double ETOL,dev_en, tmp, tmp1;

		for (int i=0;i<NoMaxEn;i++)
		{
			if(ListFoundEn[i]>0&&ListIsoBr[i]>0)
			{
				Thshld=NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(ListIsoEn[i],FWHMCoeff,coeff,WinROI);

				ETOL=Thshld[1]-Thshld[0];

				dev_en=ListFoundEn[i]-ListIsoEn[i];

				tmp=-0.16/(ETOL*ETOL)*(dev_en*dev_en)*ListFoundBr[i];

				tmp1=tmp/sumListIsoBr;

				Index1=Index1*Math.exp(tmp1);

			}
		}


		//Step 3: Caluate Index 2
		double Index2=0;
		double SumNotMatchObservePeak=0;
		double SumPeakLibrary=0;

		double mCalEfficiency=0,X=0,efftmp3;

		for (int i=0;i<NoMaxEn;i++)
		{

			X=Math.log(ListIsoEn[i]);


			efftmp3=Eff_Coef[0] * Math.pow(X, 4) + Eff_Coef[1] * Math.pow(X, 3)+ Eff_Coef[2] * Math.pow(X, 2) + Eff_Coef[3] * X + Eff_Coef[4];
			mCalEfficiency =Math.exp(efftmp3);

			if(ListFoundEn[i]==0)
			{
				SumNotMatchObservePeak=SumNotMatchObservePeak+ListIsoBr[i]*Math.sqrt(mCalEfficiency);
			}

			SumPeakLibrary=SumPeakLibrary+ListIsoBr[i]*Math.sqrt(mCalEfficiency);

		}

		if(SumPeakLibrary>0)
		{
			Index2=Index1-1.6*SumNotMatchObservePeak/SumPeakLibrary;
		}
		else
		{
			Index2=0;
		}

		double [] ReIndex=new double [2];

		ReIndex[0]=Index1;
		ReIndex[1]=Index2;

		return ReIndex;

	}

	//add logic table for
	public static Vector<Isotope> LogicUranium_HE(String isoname, String isoname_he,Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{
		boolean Src_Flg1=false,Src_Flg2=false;

		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals(isoname))
			{
				Src_Flg1=true;
			}

			if (Result2.get(i).isotopes.equals(isoname_he))
			{
				Src_Flg2=true;
			}
		}

		// Set condition
		//if there are 2615 keV then U-233HE
		//if there are not, source ID: U-233
		if(Src_Flg1==true&&Src_Flg2==true)
		{
			//Find Peak: based on minor and major peak

			//remove noise peak
			int FoundPeakSize= PeakInfo.size();
			double roi_tmp,Left_thsld,High_thsld;

			double[] Thshold = new double[2];
			double EnTmp;

			Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(2615, FWHMCoeff, coeff,WinROI);
			Left_thsld = Thshold[0];
			High_thsld = Thshold[1];

			boolean HighEnrich_Flg=false;

			for (int j=0; j<FoundPeakSize;j++)
			{
				EnTmp= PeakInfo.get(j).Peak_Energy;

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					HighEnrich_Flg=true;
				}
			}



			//remove source without High enrichment
			if(HighEnrich_Flg==true) //if HighEnrich_Flg: true then remove isoname,
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals(isoname))
					{
						Result2.remove(i);
						--i;
					}
				}
			}
			else //if HighEnrich_Flg: false then remove isoname_he
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals(isoname_he))
					{
						Result2.remove(i);
						--i;
					}
				}
			}

		}

		return Result2;
	}

	//Logic condition for High enriched uranium

	public static Vector<Isotope> LogicHighEnricUranium(Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{
		// U233 vs U233-HE
		Result2=LogicUranium_HE( "U-233","U-233HE",Result2,PeakInfo,FWHMCoeff,coeff,WinROI);

		//U235 vs U-235HE
		Result2=LogicUranium_HE( "U-235","U-235HE",Result2,PeakInfo,FWHMCoeff,coeff,WinROI);

		return Result2;
	}

	public static Vector<Isotope> AddCondition_Cs_U233HE_U235HE(Vector<Isotope> Result2)
	{

		boolean Cs_Flg=false,U233HE_Flg=false,U235HE_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Cs-137"))
			{
				Cs_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("U-233HE"))
			{
				U233HE_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("U-235HE"))
			{
				U235HE_Flg=true;
			}
		}

		//remove U233HE
		if(Cs_Flg==true&&U233HE_Flg==true)
		{
			for (int i = 0; i < Result2.size(); i++)
			{
				if (Result2.get(i).isotopes.equals("U-233HE"))
				{

					Result2.remove(i);
					--i;
				}

			}
		}

		//remove U-235HE
		if(Cs_Flg==true&&U235HE_Flg==true)
		{
			for (int i = 0; i < Result2.size(); i++)
			{
				if (Result2.get(i).isotopes.equals("U-235HE"))
				{

					Result2.remove(i);
					--i;
				}

			}
		}


		return Result2;
	}

	public static boolean AddExceptLogic_Cs(Isotope result2)
	{
		boolean flg=false;

		if (result2.isotopes.equals("Cs-137"))
		{
			int NoPeak=result2.IsoPeakEn.size();

			//text file writing
			// Nopeak,result2.IsoPeakEn.get(0), uncertainty, rmse
			if(NoPeak==1)
			{
				double abs1=result2.IsoPeakEn.get(0)-662;

				if(Math.abs(abs1)<=2)
				{
					flg=true;
				}
			}


			//NcLibrary.SaveText_HNM("NoPeak,"+NoPeak+"flg,"+flg+"result2.IsoPeakEn.get(0),"+result2.IsoPeakEn.get(0)+"result2.Uncer,"+result2.Uncer+"result2.RMSE,"+result2.RMSE+"Act,"+result2.Act+"\n");
		}

		return flg;
	}

	//Adding Logic table for : Lu177 vs Sm153
	public static Vector<Isotope> Logic_Lu177_Sm153(Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{
		boolean Lu_Flg=false,Sm_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Lu-177"))
			{
				Lu_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("Sm-153"))
			{
				Sm_Flg=true;
			}

		}

		//remove Sm-153 if peak 208keV is existing
		if(Lu_Flg==true&&Sm_Flg==true)
		{

			double roi_tmp,Left_thsld,High_thsld;
			double[] Thshold = new double[2];
			double EnTmp;

			Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(208, FWHMCoeff, coeff,WinROI);
			Left_thsld = Thshold[0];
			High_thsld = Thshold[1];

			boolean Peak208keV_FLg=false;

			for (int j=0; j<PeakInfo.size();j++)
			{
				EnTmp= PeakInfo.get(j).Peak_Energy;

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Peak208keV_FLg=true;
				}
			}

			//remove Lu177 if 208 keV do not exist
			if(Peak208keV_FLg==false)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("Lu-177"))
					{
						Result2.remove(i);
						--i;
					}
				}
			}

			//Remove Sm-153 if 208keV is exsit
			if(Peak208keV_FLg==true)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("Sm-153"))
					{
						Result2.remove(i);
						--i;
					}
				}
			}
		}


		return Result2;
	}


	public static Vector<Isotope> Logic_Cs137_Co67(Vector<Isotope> Result2, Vector<NcPeak> PeakInfo,double[] FWHMCoeff,Coefficients coeff,double WinROI)
	{
		boolean Cs_Flg=false,Co67_Flg=false;


		for (int i = 0; i < Result2.size(); i++)
		{
			if (Result2.get(i).isotopes.equals("Cs-137"))
			{
				Cs_Flg=true;
			}

			if (Result2.get(i).isotopes.equals("Co-67"))
			{
				Co67_Flg=true;
			}

		}

		//remove Sm-153 if peak 208keV is existing
		if(Cs_Flg==true&&Co67_Flg==true)
		{

			double roi_tmp,Left_thsld,High_thsld;
			double[] Thshold = new double[2];
			double EnTmp;

			Thshold = NewNcAnalsys.Get_Roi_window_by_energy_used_FWHM(2155, FWHMCoeff, coeff,WinROI);
			Left_thsld = Thshold[0];
			High_thsld = Thshold[1];

			boolean Peak2155keV_FLg=false;

			for (int j=0; j<PeakInfo.size();j++)
			{
				EnTmp= PeakInfo.get(j).Peak_Energy;

				if(EnTmp>=Left_thsld&&EnTmp<=High_thsld)
				{
					Peak2155keV_FLg=true;
				}
			}


			//Remove Sm-153 if 208keV is exist
			if(Peak2155keV_FLg==false)
			{
				for (int i = 0; i < Result2.size(); i++)
				{
					if (Result2.get(i).isotopes.equals("Co-57"))
					{
						Result2.remove(i);
						--i;
					}
				}
			}
		}


		return Result2;
	}
}