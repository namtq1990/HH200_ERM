package android.HH100.Structure;

public class PeakAnalysis {

    public Energy Energy ;
    public double Channel ;
    public double MSEnergy ;
    public double TrueEnergy ;
    public String IsotopeName ;

    public int ROI_L ;
    public int ROI_R ;

    public double Sigma ;
    //  public Energy Energy { get; set; }
    public double PeakEst ;
    public double Height ;
    public double BG_a ;
    public double BG_b ;
    public static double NetCount ;
    public static double BgNetCount ;
    public double Uncertain ;
    public double BRFactor;
    public double MSActivity ;
    public double MDA ;
    public double TrueActivity ;

    public double reserve1;


    public double FWHM ;

    public double Efficiency ;

    public boolean Used ;

    public void PeakAnalysis()
    {
        Energy = new Energy();
    }

    public class Energy
    {
        public double Kev;
        public float BR ;
        public float Uncertain ;
    }

    public static double GrossPeakArea() {
        return NetCount + BgNetCount;
    }
}
