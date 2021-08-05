package android.HH100.Control;

import java.util.*;
import java.util.regex.*;

import android.R.string;
import android.HH100.RealTimeActivity;
import android.HH100.R;
import android.HH100.Control.SpectrumView.*;
import android.HH100.Identification.Isotope;
import android.HH100.R.color;
import android.content.*;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.LinearLayout;

public class LogInform extends View {

	private int mWidth = 0;
	private int mHeight =0;
	
	private int mNumber;
	private String mDate;
	private String mTime;
	private String mAcqTime;
	private String mAgent;
	private String mLocation;
	private String mMaxDose;
	private String mAvgDose;
	private String mAvgCPS;
	private String mTotalCount;
	private String mComment;
	private Vector<String> mPhotoPath = new Vector<String>();
	private int mSelPhotoNum = 0;
	private RectF mPhotoRect = new RectF();
	private Pattern drawTextSanitizerFilter = Pattern.compile("[\t\n],");

	private Vector<Isotope> mIdResult = new Vector<Isotope>();
	private PointF mLastTouchDown = new PointF();
	
	private boolean mCommMode = false;
	
	public LogInform(Context context,AttributeSet attrs,int defStyle) {        
    	super(context,attrs,defStyle);    
    	

    }
	public LogInform(Context context ,AttributeSet attributeSet) {
	    super(context,attributeSet);
	    
	    
	}
	public LogInform(Context context) {
	    super(context);
	    
	}
	/////
	
	public void Set_ID_Result(Vector<Isotope> Value){
		mIdResult = Value;
	}
	public void Set_Comment(String text){
		mComment = text;
	}
	public void Set_Data(int EventNumber, String date,String time, String acqtime,String agent,String location,String maxDose,String avgDose,String avgCPS,String totalCount,String Comment)
	{
		mNumber= EventNumber;
		mDate = date;
		mTime = time;
		mAcqTime = acqtime;
		mAgent = agent;
		mLocation = location;
		mMaxDose = maxDose;
		mAvgDose = avgDose;
		mAvgCPS = avgCPS;
		mTotalCount = totalCount;
		mComment = Comment;
	}
	public void Set_Photo(Vector<String> Photo)
	{
		mPhotoPath = Photo;
	}
	public void Add_Photo(String Photo)
	{
		mPhotoPath.add(Photo);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {      
        // height 筌욊쑴彛� 占싼덈┛ �뤃�뗫릭疫뀐옙
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = 0;
        switch(heightMode) {
        case MeasureSpec.UNSPECIFIED:
            heightSize = heightMeasureSpec;
            break;
        case MeasureSpec.AT_MOST:
            heightSize = 20;
            break;
        case MeasureSpec.EXACTLY:
            heightSize = MeasureSpec.getSize(heightMeasureSpec);
            break;
        }

        // width 筌욊쑴彛� 占싼덈┛ �뤃�뗫릭疫뀐옙
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = 0;
        switch(widthMode) {
        case MeasureSpec.UNSPECIFIED:
            widthSize = widthMeasureSpec;
            break;
        case MeasureSpec.AT_MOST:
            widthSize = 100;
            break;
        case MeasureSpec.EXACTLY:
            widthSize = MeasureSpec.getSize(widthMeasureSpec);
            break;
        }
      
       
    	mWidth = widthSize;
    	mHeight = heightSize;
   	
        setMeasuredDimension(widthSize, heightSize);
		}
	
	@SuppressWarnings("null")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint paint = new Paint();
    	//data_Paints.setStrokeWidth((float)m_data_X_size);
		
		
		if(!mCommMode){
			paint.setTextSize(RealTimeActivity.TEXT_SIZE+5);
			paint.setColor(Color.YELLOW);
			canvas.drawText("Info",(float)(mWidth*0.1), (float)(40), paint );
			paint.setColor(Color.GRAY);
			canvas.drawText("Comment",(float)(mWidth*0.4), (float)(40), paint );
			
			/////
			paint.setColor(Color.rgb(255,201,14));
			paint.setTextSize(RealTimeActivity.TEXT_SIZE+1);
			canvas.drawText("Event: ",(float)(0), (float)(mHeight*0.25), paint );
	    	canvas.drawText("Date: ",(float)(0), (float)(mHeight*0.32), paint );
	    	canvas.drawText("Time: ",(float)(0), (float)(mHeight*0.39), paint );
	    	canvas.drawText("Alarm duration: ",(float)(0), (float)(mHeight*0.46), paint );
	    	canvas.drawText("Agent: ",(float)(0), (float)(mHeight*0.53), paint );
	    	canvas.drawText("Location: ",(float)(0), (float)(mHeight*0.6), paint );
	    	canvas.drawText("Max.DR: ",(float)(0), (float)(mHeight*0.7), paint );
	    	canvas.drawText("Avg.DR: ",(float)(0), (float)(mHeight*0.77), paint );
	    	////
	    	
	    	paint.setColor(Color.LTGRAY);
	    	canvas.drawText(String.valueOf("#"+mNumber),(float)(paint.measureText("Event: ")+3), (float)(mHeight*0.25), paint );
	    	canvas.drawText(mDate,(float)(paint.measureText("Date: ")+3), (float)(mHeight*0.32), paint );
	    	canvas.drawText(mTime,(float)(paint.measureText("Time: ")+3), (float)(mHeight*0.39), paint );
	    	canvas.drawText(mAcqTime+" Sec",(float)(paint.measureText("Alarm duration: ")+3), (float)(mHeight*0.46), paint );
	    	canvas.drawText(mAgent,(float)(paint.measureText("Agent: ")+3), (float)(mHeight*0.53), paint );
	    	canvas.drawText(mLocation,(float)(paint.measureText("Location: ")+3), (float)(mHeight*0.6), paint );
	    	canvas.drawText(mMaxDose,(float)(paint.measureText("Max.DR: ")+3), (float)(mHeight*0.7), paint );
	    	canvas.drawText(mAvgDose,(float)(paint.measureText("Avg.DR: ")+3), (float)(mHeight*0.77), paint );
	    	
	    	
	    	paint.setColor(Color.rgb(200,39,39));
			paint.setTextSize(RealTimeActivity.TEXT_SIZE+2);
	    	canvas.drawText("Radionuclide ID",(float)(0), (float)(mHeight*0.93), paint );
	    	/*
	    	paint.setColor(Color.LTGRAY);
			paint.setTextSize(KainacActivity.TEXT_SIZE+1);
	    	for(int i=0; i<mIdResult.size(); i++){
	    		double Height = mHeight*0.05;
	    		Height = Height*i;
	    		canvas.drawText(mIdResult.get(i).isotopes+"  "+mIdResult.get(i).Comment,(float)(20), (float)((mHeight*0.72)+Height), paint );
	    	}
	    	*/
		}
		else {
			paint.setTextSize(RealTimeActivity.TEXT_SIZE+5);
			paint.setColor(Color.GRAY);
			canvas.drawText("Info",(float)(mWidth*0.1), (float)(40), paint );
			paint.setColor(Color.YELLOW);
			canvas.drawText("Comment",(float)(mWidth*0.7), (float)(40), paint );
			
			paint.setColor(Color.rgb(255,201,14));
			paint.setTextSize(RealTimeActivity.TEXT_SIZE+2);
			canvas.drawText("Comment : ",(float)(0), (float)(mHeight*0.15), paint );
			
			paint.setColor(Color.LTGRAY);
			//mComment = drawTextSanitizer(mComment);
			
			
			Paint.FontMetrics fm = paint.getFontMetrics();
			float fullHeight = fm.top - fm.bottom;
				
			drawMultiline(mComment,0,(int)((mHeight*0.15)+25),paint,canvas);
			
			paint.setColor(Color.rgb(255,201,14));
			canvas.drawText("Photo",(float)(0), (float)(mHeight*0.5), paint );
			if(mPhotoPath.size() != 0){ 
								
				paint.setColor(Color.rgb(200,201,64));
				paint.setTextSize(RealTimeActivity.TEXT_SIZE+1);
				paint.setTextAlign(Align.RIGHT);
				canvas.drawText((mSelPhotoNum+1)+"/"+mPhotoPath.size(),(float)(mWidth-30), (float)(mHeight*0.6)-10, paint );
				
				paint.setTextAlign(Align.LEFT);
				
				
            	Bitmap Photo = BitmapFactory.decodeFile(mPhotoPath.get(mSelPhotoNum));
            	
				double ss = Photo.getWidth();
				Rect imgRect = new Rect();
					
				imgRect.set(0,0,Photo.getWidth(),Photo.getHeight());
				
				mPhotoRect.set(1.0f,(float)(mHeight*0.6),(float)mWidth-30,(float)(mHeight-30));
				
				canvas.drawRect(mPhotoRect.left-1,mPhotoRect.top-1,mPhotoRect.right+1,mPhotoRect.bottom+1, paint);
				canvas.drawBitmap(Photo, imgRect,mPhotoRect , paint);
				Photo.recycle();
				
				//canvas.drawRect(mPhotoRect, paint);
				
			}
		}
			
		super.onDraw(canvas);
	}
	private String drawTextSanitizer(String string) {
		  Matcher m = drawTextSanitizerFilter.matcher(string);
		  string = m.replaceAll(",").replace('\n', ' ').replace('\n', ' ');
		  return string;
		 }

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) { 
		
	}          
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {               
	     
   }
    public void drawMultiline(String str, int x, int y, Paint paint,Canvas canvas)
    {
        for (String line: str.split("\n"))
        {
              canvas.drawText(line, x, y, paint);
              y += -paint.ascent() + paint.descent();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	//     	
    	if(event.getAction()==MotionEvent.ACTION_DOWN){
    		mLastTouchDown.x =3;
    		return true;
    	}
		if(event.getAction() == MotionEvent.ACTION_UP){
			
			//if(mPhotoRect.contains(mLastTouchDown.x , mLastTouchDown.y ) & mPhotoRect.contains(event.getX(), event.getY())& mCommMode == true){ // if Photo area 
				/*if(mLastTouchDown.x == 0){ //left				
						mSelPhotoNum -=1 ;
						if(mSelPhotoNum <0)mSelPhotoNum =mPhotoPath.size();		
						mLastTouchDown.x =3;
				}
				else if(mLastTouchDown.x ==1){ //right					
						mSelPhotoNum +=1 ;
						if(mSelPhotoNum == mPhotoPath.size()-1)mSelPhotoNum =0;	
						mLastTouchDown.x =3;
				}*/
			//}
			if(mPhotoRect.contains(event.getX(), event.getY()) == false){
			//	if(mCommMode)Swiching_Info_Comment_Page();
			//	else Swiching_Info_Comment_Page();
			}
			invalidate();
		}
		else if(event.getAction() ==MotionEvent.ACTION_MOVE){
			if(event.getHistorySize()>3){
				if(mPhotoRect.contains(event.getHistoricalX(0) , event.getHistoricalY(0) )& mCommMode == true){ // if Photo area 
					if(event.getHistoricalX(0)> event.getX()){ //left				
						mLastTouchDown.x = 0;				
					}
					else if(event.getHistoricalX(0)<event.getX()){ //right					
						mLastTouchDown.x =1;	
					}
				}				
			}
		}
		
    	return super.onTouchEvent(event);
    }
    /*private void Swiching_Info_Comment_Page(){
	if(mCommMode==false){
			mCommMode = true;
			LogInform layout = (LogInform) findViewById(R.id.LogInform);		
			LinearLayout.LayoutParams Param = (LinearLayout.LayoutParams) layout.getLayoutParams();
			Param.weight = 0;
			layout.setLayoutParams(Param);	
		}
		else{
			mCommMode = false;
			LogInform layout = (LogInform) findViewById(R.id.LogInform);		
			LinearLayout.LayoutParams Param = (LinearLayout.LayoutParams) layout.getLayoutParams();
			Param.weight = 1;
			layout.setLayoutParams(Param);	
		}
	}*/
}
