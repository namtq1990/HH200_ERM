package NcLibrary;


public class SpcAnalysis
{
    public static final int CS137_EN1 = 32;
    public static final int CS137_EN2 = 662;
    public static final int K40_EN = 1462;

    public static double ToEnergy(int Channel, Coefficients coeff)
    {
        return NcMath.ToPolynomial_FittingValue_AxisY(Channel,coeff);
    }
    public static int ToEnergy_Int(int Channel, Coefficients coeff)
    {
        return NcMath.Auto_floor( NcMath.ToPolynomial_FittingValue_AxisY(Channel,coeff) );
    }
    public static int ToChannel(double Energy, Coefficients coeff)
    {
        return NcMath.Auto_floor( NcMath.ToPolynomial_FittingValue_AxisX(Energy, coeff) );
    }
    public static double ToChannel_Double(double Energy, Coefficients coeff)
    {
        return NcMath.ToPolynomial_FittingValue_AxisX(Energy, coeff);
    }

    public static Spectrum Smooth_Spc(Spectrum spc)
    {
        int repeat=2;
        float aval = 0.015f;
        float bval = 2.96474f;

        Spectrum temp = spc;

        for (int i = 0; i < repeat; i++)
        {
            temp = ft_Smooth(temp, aval, bval);
        }


        return temp;
    }
    public static Spectrum ft_Smooth(Spectrum spc)
    {
        float aval = 0.015f;
        float bval = 2.96474f;

        int temp_wind = (int)Math.ceil(aval * spc.GetChSize() + bval);

        double temp_sum = 0;
        double[] temphist1 = new double[spc.GetChSize()];

        try
        {
            for (int i = 4; i < spc.GetChSize() - temp_wind; i++)
            {
                temp_sum = 0;
                int window = (int)Math.ceil(aval * i + bval);

                if (window <= 0)
                    continue;

                if (window % 2 == 0)
                    window = window + 1;

                int window_half = window / 2;

                for (int j = i - window_half; j <= i + window_half; j++)
                    temp_sum = temp_sum + spc.At(j);


                temphist1[i] = temp_sum / window;
            }

        }
        catch (Exception e)
        {

        }
        Spectrum result = new Spectrum(temphist1,spc.get_AcqTime(),spc.get_EnCalib_Coeff());
        return result;
    }
    public static Spectrum ft_Smooth(Spectrum spc , float aVal, float bVal)
    {
        float aval = aVal;
        float bval = bVal;

        int temp_wind = (int)Math.ceil(aval * spc.GetChSize() + bval);

        double temp_sum = 0;
        double[] temphist1 = new double[spc.GetChSize()];

        try
        {
            for (int i = 4; i < spc.GetChSize() - temp_wind; i++)
            {
                temp_sum = 0;
                int window = (int)Math.ceil(aval * i + bval);

                if (window <= 0)
                    continue;

                if (window % 2 == 0)
                    window = window + 1;

                int window_half = window / 2;

                for (int j = i - window_half; j <= i + window_half; j++)
                    temp_sum = temp_sum + spc.At(j);


                temphist1[i] = temp_sum / window;
            }

        }
        catch (Exception e)
        {

        }
        Spectrum result = new Spectrum(temphist1,spc.get_AcqTime(),spc.get_EnCalib_Coeff());
        return result;
    }
}
