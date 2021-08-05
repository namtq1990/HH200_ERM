package NcLibrary;

public class Isotope {

	enum IsoClass {SNM,MED,UNK,NORM,IND}

	public String Name;
	public Energy[] Peaks;
	public IsoClass Class = IsoClass.UNK;
	public String Tag;

	class Energy{
		double Kev;
		double BR;
	}

}
