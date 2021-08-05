package NcLibrary;


public class ROI {

	RoiWindow _Window = new RoiWindow();


	public int GetStart()
	{
		return _Window.Start;
	}
	public int GetEnd()
	{
		return _Window.End;
	}

	public double GetStart(Coefficients EnCalib_Coeff)
	{
		return SpcAnalysis.ToEnergy(_Window.Start, EnCalib_Coeff);
	}
	public double GetEnd(Coefficients EnCalib_Coeff)
	{
		return SpcAnalysis.ToEnergy(_Window.End, EnCalib_Coeff);
	}

	public class RoiWindow
	{
		int Start;
		int End;
	}
}
