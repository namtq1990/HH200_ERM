package NcLibrary;

import java.util.Vector;

import android.HH100.MainActivity;
import android.HH100.Structure.NcLibrary;
import android.graphics.Point;


public class NcMath
{
    public static double Percent(double Value, double MaxValue)
    {
        return (Value/MaxValue)*100.0;
    }
    public static int Auto_floor(double value)
    {
        return (int)Math.floor(value + 0.5);
    }

    public static double ToPolynomial_FittingValue_AxisX1(double AxisY_Value, double [] coeff) //Target�� Coeff�� �����Ͽ� ��ȯ�Ѵ�.
    {
        if(coeff == null) return 0 ;

        if(coeff.length<= 1) return 0;

        //  int argSize = coeff.Length();
        double Result = 0;

        try{
            switch (coeff.length)
            {
                case 2:

                    Result = (AxisY_Value - coeff[1]) / coeff[0];
                    break;

                case 3:

                    Result = 2 * coeff[0];
                    Result = (-(coeff[1]) + Math.sqrt((coeff[1] * coeff[1]) - ((4 * coeff[0]) * (coeff[2] - AxisY_Value)))) / Result;
                    break;
            }
        }
        catch(Exception e){
        }


        return Result;
    }

    public static double ToPolynomial_FittingValue_AxisX(double AxisY_Value, Coefficients coeff) //Target�� Coeff�� �����Ͽ� ��ȯ�Ѵ�.
    {
        if(coeff == null) return 0 ;
        if(coeff.Length() <= 1) return 0;

        int argSize = coeff.Length();
        double Result = 0;

        try{
            switch (coeff.Length())
            {
                case 2:

                    Result = (AxisY_Value - coeff.At(1)) / coeff.At(0);
                    break;

                case 3:

                    Result = 2 * coeff.At(0);
                    Result = (-(coeff.At(1)) + Math.sqrt((coeff.At(1) * coeff.At(1)) - ((4 * coeff.At(0)) * (coeff.At(2) - AxisY_Value)))) / Result;
                    break;
            }
        }
        catch(Exception e){
        }


        return Result;
    }
    public static double ToPolynomial_FittingValue_AxisY(double AxisX_Value, Coefficients coeff) //Target�� Coeff�� �����Ͽ� ��ȯ�Ѵ�.
    {
        if (coeff == null) return 0;
        if (coeff.Length() <= 1) return 0;

        int argSize = coeff.Length();
        double Result = 0;

        try
        {
            switch (coeff.Length())
            {
                case 2:

                    Result = (coeff.At(0) * AxisX_Value) + coeff.At(1);
                    break;

                case 3:

                    Result = (coeff.At(0) * (AxisX_Value * AxisX_Value)) + (coeff.At(1) * AxisX_Value) + coeff.At(2);
                    break;
            }
        }
        catch (Exception e)
        {
            Result = 0;
        }

        return Result;
    }
    public static Coefficients Fitting_Quadratic_equation(Point pt1,Point pt2,Point pt3)
    {
        try{
            if(pt1.x ==0 || pt2.x==0 || pt3.x==0 || pt1.y==0 || pt2.y==0 || pt3.y==0)
            {
                return new Coefficients();
            }

            ///////////////////////////////////////////////////
            //// 3 point calibration
            double[] calChArr = new double[3];
            calChArr[0]=pt1.x;
            calChArr[1]=pt2.x;
            calChArr[2]=pt3.x;

            double[] calEnArr = new double[3];
            calEnArr[0] = pt1.y;
            calEnArr[1] = pt2.y;
            calEnArr[2] = pt3.y;


            double[][] chMatrix = new double[3][3];
            chMatrix[0][0] = pt1.x*pt1.x;
            chMatrix[0][1] = pt1.x;
            chMatrix[0][2] = 1;

            chMatrix[1][0] = pt2.x*pt2.x;
            chMatrix[1][1] = pt2.x;
            chMatrix[1][2] = 1;

            chMatrix[2][0] = pt3.x*pt3.x;
            chMatrix[2][1] = pt3.x;
            chMatrix[2][2] = 1;


            double revD= chMatrix[0][0]*chMatrix[1][1]*chMatrix[2][2] +chMatrix[2][0]*chMatrix[0][1]*chMatrix[1][2] +chMatrix[1][0]*chMatrix[2][1]*chMatrix[0][2]
                    - chMatrix[0][2]*chMatrix[1][1]*chMatrix[2][0] -chMatrix[0][0]*chMatrix[1][2]*chMatrix[2][1] - chMatrix[1][0]*chMatrix[0][1]*chMatrix[2][2];

            double[][] revMatrix = new double[3][3];
            revMatrix[0][0]=(chMatrix[1][1]*chMatrix[2][2] - chMatrix[1][2]*chMatrix[2][1])/revD;
            revMatrix[0][1]=-(chMatrix[0][1]*chMatrix[2][2] - chMatrix[0][2]*chMatrix[2][1])/revD;
            revMatrix[0][2]=(chMatrix[0][1]*chMatrix[1][2] - chMatrix[0][2]*chMatrix[1][1])/revD;

            revMatrix[1][0]=-(chMatrix[1][0]*chMatrix[2][2] - chMatrix[1][2]*chMatrix[2][0])/revD;
            revMatrix[1][1]=(chMatrix[0][0]*chMatrix[2][2] - chMatrix[0][2]*chMatrix[2][0])/revD;
            revMatrix[1][2]=-(chMatrix[0][0]*chMatrix[1][2] - chMatrix[0][2]*chMatrix[1][0])/revD;

            revMatrix[2][0]=(chMatrix[1][0]*chMatrix[2][1] - chMatrix[1][1]*chMatrix[2][0])/revD;
            revMatrix[2][1]=-(chMatrix[0][0]*chMatrix[2][1] - chMatrix[0][1]*chMatrix[2][0])/revD;
            revMatrix[2][2]=(chMatrix[0][0]*chMatrix[1][1] - chMatrix[0][1]*chMatrix[1][0])/revD;


            double Param[] = new double[3];
            for(int i=0;i<3;i++)
            {	for(int j=0;j<3;j++)
            {
                Param[i]=Param[i]+revMatrix[i][j]*calEnArr[j];
            }
            }

            return new Coefficients(Param);
        }catch(Exception e){
            return new Coefficients();
        }

    }

    public static Coefficients Fitting_Linear_equation(Point pt1, Point pt2)
    {
        double[] result = new double[2];
        try
        {
            double resultA = ((float)(Math.max(pt1.y, pt2.y) - Math.min(pt1.y, pt2.y)) / (Math.max(pt1.x, pt2.x) - Math.min(pt1.x, pt2.x)));
            double resultB = (float)(pt1.y - pt1.x * resultA);

            result = new double[] { resultA, resultB };
        }
        catch (Exception e)
        {

        }
        return new Coefficients(result);
    }

    public static Coefficients Fitting_Exp(Vector<Point> pts)
    {

        double Sum_X=0, Sum_P=0, Sum_X2=0, Sum_XP=0;
        double x, y, a, b;

        for (int i = 0; i < pts.size(); i++)
        {
            Sum_X = Sum_X + pts.elementAt(i).x;
            Sum_P = Sum_P + Math.log(pts.elementAt(i).y);
            Sum_X2 = Sum_X2 + pts.elementAt(i).x * pts.elementAt(i).x;
            Sum_XP = Sum_XP + pts.elementAt(i).x * Math.log(pts.elementAt(i).y);
        }

        b = ((double)pts.size() * Sum_XP - Sum_X * Sum_P) / ((double)pts.size()  * Sum_X2 - Sum_X * Sum_X);
        a = Sum_P / (double)pts.size()  - (b * Sum_X) / (double)pts.size() ;
        a = Math.exp(a);
        //b = exp(b);

        Coefficients result = new Coefficients();
        result.set_Coefficients(new double[]{a,b});

        return result;
    }
    public static double ToExpFitting(double AxisX, Coefficients coeff)
    {
        return coeff.At(0) * Math.exp(coeff.At(1) * AxisX);
    }


    public static double []  Background_GainStabilization(double [] spc, int Before_K40, int Now_K40)
    {
        double[] NewBG = new double[1024];
        try {
            if (Before_K40 == Now_K40)
                return spc;

            //double[] BG = new double[this.Real_BG.Get_Ch_Size()];


            //BG =this.Real_BG.ToDouble();

            // background adjustment
            int tempindex = 0;
            float diffgap = 0;


            float temp = 0;

            double tempchvalue=0;
            double modvalue=0;
            double modPercent=0;

            int x1,x2;
            double a,b,x,y,y1,y2;


            if (Now_K40 == 0)
                diffgap = 1;
            else
                diffgap = (float) Now_K40 / (float) Before_K40;

            for (int i = 0; i < MainActivity.CHANNEL_ARRAY_SIZE; i++)
            {
                tempchvalue= (double)(i)/diffgap;

                tempindex = (int)Math.floor(tempchvalue); // floor(4.8)=4
                modvalue= tempchvalue - tempindex;

                if(tempindex>0&&tempindex<MainActivity.CHANNEL_ARRAY_SIZE-2)
                {
                    NewBG[i]=(1-modvalue)*spc[tempindex]+modvalue*spc[tempindex+1];
                }

            }


        }
        catch (Exception e)
        {
            NcLibrary.Write_ExceptionLog(e);
        }

        return NewBG;
    }
}
