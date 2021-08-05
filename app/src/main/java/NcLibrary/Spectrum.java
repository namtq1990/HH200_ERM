package NcLibrary;

import java.io.Serializable;

public class Spectrum implements Serializable {
	private static final long serialVersionUID = -6726229612941490740L;

	private static int DEAFUALT_CH_SIZE = 1024;

	private double[] _Channel = new double[DEAFUALT_CH_SIZE];
	private Coefficients _EnCalib_Coeff = new Coefficients();
	private int _AcqTime = 0;

	public Spectrum()
	{

	}
	public Spectrum(double[] Ch)
	{
		_Channel = Ch;
	}
	public Spectrum(double[] Ch,int AcqTime)
	{
		_Channel = Ch;
		_AcqTime = AcqTime;
	}
	public Spectrum(double[] Ch,int AcqTime,Coefficients Coeff)
	{
		_Channel = Ch;
		_AcqTime = AcqTime;
		_EnCalib_Coeff = Coeff;
	}

	//--Get , Set
	public double At(int Channel)
	{
		return _Channel[Channel];
	}
	public int GetChSize()
	{
		return _Channel.length;
	}
	public double[] get_Channel() {
		return _Channel;
	}
	public void set_Channel(double[] _Channel) {
		this._Channel = _Channel;
	}
	public Coefficients get_EnCalib_Coeff() {
		return _EnCalib_Coeff;
	}
	public void set_EnCalib_Coeff(Coefficients _EnCalib_Coeff) {
		this._EnCalib_Coeff = _EnCalib_Coeff;
	}
	public int get_AcqTime() {
		return _AcqTime;
	}
	public void set_AcqTime(int _ACqTime) {
		this._AcqTime = _ACqTime;
	}
}
