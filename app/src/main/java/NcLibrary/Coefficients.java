package NcLibrary;

import java.io.Serializable;
import java.sql.Date;


public class Coefficients implements Serializable
{
    private static final long serialVersionUID = -6726229612941490740L;

    private double[] _Coeffcients;
    private Date _MesurementDate;

    public Coefficients()
    {
    }
    public Coefficients(double[] Param)
    {
        _Coeffcients = Param;
    }


    public double At(int i)
    {
        return _Coeffcients[i];
    }
    public int Length()
    {
        return _Coeffcients.length;
    }

    public Date get_MesurementDate() {
        return _MesurementDate;
    }
    public void set_MesurementDate(Date _MesurementDate) {
        this._MesurementDate = _MesurementDate;
    }
    public double[] get_Coefficients() {
        return _Coeffcients;
    }
    public void set_Coefficients(double[] _Coeffcients) {
        this._Coeffcients = _Coeffcients;
    }

    public String ToString()
    {
        String result="";

        try{
            for(int i=0; i<_Coeffcients.length; i++)
            {
                result += _Coeffcients[i]+",";
            }
            return result;
        }catch(Exception e)
        {
            return result;
        }
    }
}
