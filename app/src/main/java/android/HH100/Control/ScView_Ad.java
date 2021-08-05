package android.HH100.Control;

import java.text.DecimalFormat;
import java.util.Vector;

import android.HH100.EventLogActivity;
import android.HH100.IDspectrumActivity;
import android.HH100.RealTimeActivity;
import android.HH100.Identification.Isotope;
import android.HH100.Structure.NcLibrary;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.Layout.Alignment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;

public class ScView_Ad extends View{
	private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;
    
    private float mLastMotionX;
	private float mLastMotionY;
	private Handler mHandler;
	
	private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	
	int mDefaultHeight = 800;
	int mOneBarHeight = 70;
	int mWidth = 1280;
	int mHeight = mDefaultHeight;
	private int mClickRow = -1;
	private Vector<Isotope> mIsotopes = new Vector<Isotope>();
	/*private Vector<String> mIsotope = new Vector<String>();
	private Vector<Integer> mBarColor = new Vector<Integer>();
	private Vector<Double> mDoseRate = new Vector<Double>();
	*/
	private Vector<Integer> mMaxSortNum = new Vector<Integer>();
	private boolean mIsSvUnit = true;
	int mDataGridCount = 3;
	
	private Vector<Integer> mClassColor = new Vector<Integer>();
	public ScView_Ad(Context context) {
		super(context);
        mClassColor.clear();
        mClassColor.add(Color.rgb(26 , 45, 145));
        mClassColor.add(Color.rgb(123, 21, 60));
        mClassColor.add(Color.rgb(146,121,29));
        mClassColor.add(Color.rgb(29,146,55));
        
        mHandler = new keyHandler();
		
	}
	public ScView_Ad(Context context ,AttributeSet attributeSet) {
		super(context);
        mClassColor.clear();
        mClassColor.add(Color.rgb(26 , 45, 145));
        mClassColor.add(Color.rgb(123, 21, 60));
        mClassColor.add(Color.rgb(146,121,29));
        mClassColor.add(Color.rgb(29,146,55));
        mHandler = new keyHandler();
       /* for(int i=0; i<8; i++){
        	mIsotope.add(" ");
        	mBarColor.add(Color.BLACK);
        	mDoseRate.add((double)0);
        	mMaxSortNum.add(i);       	
        }*/
	}
	private class keyHandler extends Handler {
    	public void handleMessage(Message msg){
    		switch(msg.what){
	    		case SHOW_PRESS:
	    			
	  			    break;

	    		case LONG_PRESS:
	    			mClickRow = -1;
	    			invalidate();
	    			
	    			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
	        		dialogBuilder.setTitle("Explain");
	        		dialogBuilder.setMessage("asdfasfdasf");
	        		dialogBuilder.setPositiveButton("OK", null);
	        		dialogBuilder.setCancelable(false);
	        		dialogBuilder.show();
	    			break;

	    		case TAP:
	    			

	    			break;	

    			default:
    				 throw new RuntimeException("Unknown message " + msg); //never
    		}
    	}
    }
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		int LR_Margin = 20;
		
		Paint paint = new Paint();	

		paint.setColor(Color.rgb(23,22,21));
		canvas.drawRect(0,0,mWidth,mHeight,paint);
		
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(NcLibrary.Get_fontSize(mWidth));		
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(2f);
		canvas.drawLine((float)mWidth*0.1f, 0, (float)mWidth*0.1f, mHeight, paint);
		
		//////////
		
		//paint.setTextSize(Get_fontSize()-3);
		paint.setStrokeWidth(0f);
		float leftSpace = mWidth*0.1f;
		float UsingWidth = mWidth - leftSpace - LR_Margin;
		
		if(mIsotopes.isEmpty() == false)mDataGridCount = Get_LogScale_GridCount(MaxDoserate())+1;
		else mDataGridCount = 3;
		if(mDataGridCount < 3) mDataGridCount = 3;
		
		if(mClickRow != -1){
			paint.setColor(Color.LTGRAY);
			canvas.drawRect(new Rect(0,mOneBarHeight*mClickRow,mWidth,mOneBarHeight*(mClickRow+1)), paint);
		}
		for(int i=0; i<mDataGridCount; i++){
			float OneBlockWidth = UsingWidth/mDataGridCount;
			paint.setColor(Color.GRAY); 
			canvas.drawLine((float)(mWidth*0.1f)+(OneBlockWidth*(i+1)),0, (float)(mWidth*0.1f)+(OneBlockWidth*(i+1)), mHeight, paint);
		}
		for(int i = 0; i<mIsotopes.size(); i++){
			Draw_IsotopeData(canvas,i,mIsotopes.get(mMaxSortNum.get(i)).isotopes,mIsotopes.get(mMaxSortNum.get(i)).DoseRate,mIsotopes.get(mMaxSortNum.get(i)).ClassColor);
			// Draw_IsotopeData(canvas,i,mIsotope.get(i),mDoseRate.get(i),mBarColor.get(i));
		}
		
	}
	private void Draw_IsotopeData(Canvas canvas, int Index_Num, String Isotope, double Doserate,int BarColor){
				
		//int Iso_Doserate_Height = 40;//40; // HH - 55
		Paint paint = new Paint();	
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(RealTimeActivity.TEXT_SIZE+2);
		paint.setStrokeWidth(0f);
		
		
		
		paint.setTextAlign(Align.RIGHT);
		int NameTextHeight = (int) (Math.abs(paint.ascent()) + Math.abs(paint.descent())) ;
		canvas.drawText(Isotope, (mWidth*0.1f)-5, (mOneBarHeight*Index_Num)+(mOneBarHeight/2)+2, paint);
		
		paint.setColor(BarColor);
		double width = GetLogScale_Width(NcLibrary.pow(10, mDataGridCount),Doserate);
		canvas.drawRect((float)(mWidth*0.1f)+1,(float)(mOneBarHeight*Index_Num)+(mOneBarHeight*0.2f),(float)width+(float)(mWidth*0.1f),(float)((mOneBarHeight*(Index_Num+1))-(mOneBarHeight*0.2f)), paint);
		
		paint.setStrokeWidth(0f);
		paint.setColor(Color.WHITE);
		
		Rect textRect = null;
		String value = "";
		
		if(Doserate > 0){
			if(paint.measureText(SvToString(Doserate,true,false,mIsSvUnit)+SvToString(Doserate,true,true,mIsSvUnit))+20 < (float)width)
			{
				value = SvToString(Doserate,true,false,mIsSvUnit)+SvToString(Doserate,true,true,mIsSvUnit);
				//paint.getTextBounds(value, 0, value.length(), textRect);
				canvas.drawText(value, (float)width+(mWidth*0.1f)-20, (float)(mOneBarHeight*Index_Num)+(mOneBarHeight*0.5f), paint);
			}
			else{
				value = SvToString(Doserate,true,false,mIsSvUnit)+SvToString(Doserate,true,true,mIsSvUnit);
				//paint.getTextBounds(value, 0, value.length(), textRect);
				paint.setTextAlign(Align.LEFT);
				canvas.drawText(value, (float)width+(mWidth*0.1f)+20, (float)(mOneBarHeight*Index_Num)+(mOneBarHeight*0.5f), paint);
			}
		}
	}
	public int Get_Clicked_Row(Point TouchPoint){
		
		for(int i=0; i<mIsotopes.size(); i++){
			int Top = mOneBarHeight*i; 
			int Bottom = mOneBarHeight*(i+1);
			if(TouchPoint.y > Top & TouchPoint.y < Bottom){
				return i;
			}
		}
		return -1;
	}
	private double GetLogScale_Width(double Max, double DoseRate){
		
		double log_yMax = Math.log10(Max);
		double log_yValue = Math.log10(DoseRate);

		log_yValue = (log_yValue/log_yMax)*100.0;
		
		//////
		double DataY =(log_yValue/100)*(float)(mWidth-(mWidth*0.1f)-20);
		return DataY;
	}
	public void Add_IsotopeData(Isotope Iso){
		mIsotopes.add(Iso);
		mMaxSortNum.clear();
		Sort_byMax();
		
	}


	public boolean onTouchE2vent(MotionEvent event) {
		
		return true;
	}

    @Override
    public boolean onTouchEvent(MotionEvent ev){
    	final int action = ev.getAction();
    	final float x = ev.getX();
    	final float y = ev.getY();
    	
    	switch(action){
	    	case MotionEvent.ACTION_DOWN:    
	    		mClickRow = Get_Clicked_Row(new Point((int)ev.getX(),(int)ev.getY()));
	    		invalidate();
	    		mHandler.removeMessages(LONG_PRESS);
	    		mHandler.sendEmptyMessageAtTime(LONG_PRESS, ev.getDownTime()+ TAP_TIMEOUT + LONGPRESS_TIMEOUT);
	    		break;	    		
	    	case MotionEvent.ACTION_MOVE:	    		
	    		mHandler.removeMessages(LONG_PRESS);
	    		break;   	
	    	case MotionEvent.ACTION_UP:
	    		
	    		
	    		if(mClickRow != -1){
		    		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
	        		dialogBuilder.setTitle(mIsotopes.get(mClickRow).isotopes);
	        		dialogBuilder.setMessage(mIsotopes.get(mClickRow).Comment);
	        		dialogBuilder.setPositiveButton("OK", null);
	        		dialogBuilder.setCancelable(false);
	        		dialogBuilder.show();
	    		}
	    		mClickRow = -1;
	    		invalidate();
	    		mHandler.removeMessages(LONG_PRESS);
	    		break;
	    	case MotionEvent.ACTION_CANCEL:
	    		mClickRow = -1;
	    		mHandler.removeMessages(LONG_PRESS);
	    		break;
    	}
    	return true;	

    }
	public void RemoveAll_IsotopeData(){
		mIsotopes.clear();
		mMaxSortNum.clear();
		
		
	}
	private void Sort_byMax(){
		double result = 0;
		int temp = 0;
		Vector<Double> DoseTemp = new Vector<Double>();
		
		for(int i=0; i<mIsotopes.size(); i++){
			DoseTemp.add(mIsotopes.get(i).DoseRate);
		}
		
		for(int k=0; k<DoseTemp.size(); k++){
			for(int i=0; i<DoseTemp.size(); i++){
				if(DoseTemp.get(i) == -5555.5 | DoseTemp.get(i) == result) continue;
				
				if(result < DoseTemp.get(i)){
					result = DoseTemp.get(i);
					temp = i ;
				}
			}
			mMaxSortNum.add(temp);
			DoseTemp.set(temp, (double) -5555.5);
			result = -10;
		}
		
		
		
	}
	private double MaxDoserate(){
		double result = 0;
		for(int i=0; i<mIsotopes.size(); i++){
			if(result < mIsotopes.get(i).DoseRate){
				result = mIsotopes.get(i).DoseRate;
			}
		}
		return result;
	}
	public void SetLogGridCount(int count) {
		mDataGridCount = count;
	}
	public void Set_IsSv_Unit(boolean IsSv){
		mIsSvUnit = IsSv;
	}
    public String SvToString(double nSv,boolean point, boolean OnlyUnit, boolean IsSvUnit){ // �닽�옄�삎 �떆蹂댄듃 媛믪쓣 string�쑝濡�
	    	
	    	if(IsSvUnit == false)nSv = nSv * 100;
		    DecimalFormat format = new DecimalFormat();
			String unit = " Sv/h";
			double value = 1;
	    	int checker = 0;
		     if(point == true){
		    	if(nSv < NcLibrary.pow(10,3)){value=nSv*0.001; unit=" uSv/h";checker=1;}
		    	else if(nSv >= NcLibrary.pow(10,3) & nSv < NcLibrary.pow(10,6)){value=(nSv*0.001); unit=" uSv/h";}
		    	else if(nSv >= NcLibrary.pow(10,6) & nSv < NcLibrary.pow(10,9)){value=(nSv*0.000001); unit=" mSv/h";}
		    	else if(nSv >  NcLibrary.pow(10,9)) {value=(nSv*0.000000001); unit=" Sv/h";}
		    	}
		     else{
		    	 if(nSv < NcLibrary.pow(10,3)){value = nSv; unit = " nSv/h";}
		     	 else if(nSv >= NcLibrary.pow(10,3) & nSv < NcLibrary.pow(10,6)){value = (nSv*0.001); unit = " uSv/h";}
		     	 else if(nSv >= NcLibrary.pow(10,6) & nSv < NcLibrary.pow(10,9)){value =  nSv*0.000001; unit = " mSv/h";}
		     	 else if(nSv >  NcLibrary.pow(10,9)) {value = nSv*0.000000001 ; unit =" Sv/h";}
		     	}
	     
		     
		     if(checker == 1)format.applyLocalizedPattern("0.###");
		     else format.applyLocalizedPattern("0.##");
		     
		     if(OnlyUnit){
		    	 if(IsSvUnit == false){
		    		 unit = unit.replace("Sv/h","rem/h");
			    }
		    	 return unit;
		     }
		     else {
		    	 if(IsSvUnit == false){
		    		 unit = unit.replace("Sv/h","rem/h");
			    }
		    	 return format.format(value);
		     }
	     
	    }
	private int Get_LogScale_GridCount(double Doserate)
	{
		double sdq = NcLibrary.pow(10,1);
		double sdq2 = NcLibrary.pow(10,2);
		int GridCount = 0;
		for(int i=0; i<100; i++){
			if(NcLibrary.pow(10,i) <= Doserate && NcLibrary.pow(10,i+1) > Doserate){
				GridCount = i;
				break;
			}
		}
		//m_AxisY_Grid_Count = log10(m_Display_End_AxisY); //理쒕�移댁슫�듃瑜� 10�쓣 諛묒쑝濡� �븯�뒗 濡쒓렇�솕
		//m_data_Y_MAX_value = pow(10,GridCount);
	
		return GridCount;
	}
	public int Get_Grid_Count(){
		return mDataGridCount;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mOneBarHeight=(int)(mWidth *0.07);
		mDefaultHeight = mIsotopes.size()*mOneBarHeight;
		if(mDefaultHeight < 800) mDefaultHeight = 800;
		
		mWidth = widthMeasureSpec;
		
		mHeight =  MeasureSpec.makeMeasureSpec(mDefaultHeight, MeasureSpec.EXACTLY);
		
		int heightMode = MeasureSpec.getMode(mHeight);
        int heightSize = 0;
        switch(heightMode) {
        case MeasureSpec.UNSPECIFIED:
            heightSize = mHeight;
            break;
        case MeasureSpec.AT_MOST:
            heightSize = 20;
            break;
        case MeasureSpec.EXACTLY:
            heightSize = MeasureSpec.getSize(mHeight);
            break;
        }

        // width 筌욊쑴彛� 占싼덈┛ �뤃�뗫릭疫뀐옙
        int widthMode = MeasureSpec.getMode(mWidth);
        int widthSize = 0;
        switch(widthMode) {
        case MeasureSpec.UNSPECIFIED:
            widthSize = mWidth;
            break;
        case MeasureSpec.AT_MOST:
            widthSize = 100;
            break;
        case MeasureSpec.EXACTLY:
            widthSize = MeasureSpec.getSize(mWidth);
            break;
        }
        
        mWidth = widthSize;
        mHeight = heightSize;
        
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mDefaultHeight, MeasureSpec.EXACTLY));
	}
	
	
}
