package android.HH100.Structure;

import java.io.Serializable;

import android.util.Log;

public class Neutron implements Serializable {
	private static final long serialVersionUID = -6726229612941490740L;

	private double mCPS;
	private double mTotalCPS;
	private int mElapsedTime;
	private double mMax_Count;

	public double Get_CPS()
	{
		return mCPS;
	}
	public void Set_CPS(int Cps){
		mCPS = Cps;
		mTotalCPS += Cps;
		mElapsedTime +=1;
		if(mMax_Count < Cps) mMax_Count = Cps;
	}
	public void Set_CPS(double Cps){
		mCPS = Cps;
		mTotalCPS += Cps;
		mElapsedTime +=1;
		if(mMax_Count < Cps) mMax_Count = Cps;
	}

	public double Get_MaxCount()
	{
		String result = new java.text.DecimalFormat("#.##").format(mMax_Count);
		return Double.valueOf(result); //소수점 1자리수로 반올림
		//return mMax_Count;
	}
	public double Get_AvgCps(){
		String result = new java.text.DecimalFormat("#.##").format((mTotalCPS/(double)mElapsedTime));
		return Double.valueOf(result); //소수점 1자리수로 반올림
		//return (double)mTotalCPS/(double)mElapsedTime;
	}
	public void Reset_Acummul_data()
	{
		mMax_Count = 0;
		mTotalCPS = 0;
	}
	public void Init()
	{
		mCPS = 0;
		//mTotal = 0;
		mElapsedTime = 0;
		mMax_Count = 0;
		mTotalCPS = 0;
	}
}
