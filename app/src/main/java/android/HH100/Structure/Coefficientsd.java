package android.HH100.Structure;

import java.io.Serializable;

public class Coefficientsd implements Serializable {
	private static final long serialVersionUID = -6726229612941490740L;

	private double[] mCoefficients = null;
	public Coefficientsd()
	{
	}
	public Coefficientsd(double[] coeff)
	{
		mCoefficients = coeff;
	}
	public Coefficientsd(double A,double B,double C)
	{
		mCoefficients = new double[]{A,B,C};
	}
	public double[] Get_Coefficients()
	{
		return mCoefficients;
	}
	public String ToLog()
	{
		String result="";

		try{
			for(int i=0; i<mCoefficients.length; i++)
			{
				result += mCoefficients[i]+",";
			}
			return result;
		}catch(Exception e)
		{
			return result;
		}
	}
}
