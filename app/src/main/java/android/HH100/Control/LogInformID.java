package android.HH100.Control;

import java.util.Vector;

import android.HH100.RealTimeActivity;
import android.HH100.RealTimeActivity;
import android.HH100.Identification.Isotope;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class LogInformID extends View {

	private int mOneBarHeight;
	private int mDefaultHeight;
	private int mWidth;
	private int mHeight;
	private Vector<Isotope> mID = new Vector<Isotope>();

	public LogInformID(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public LogInformID(Context context ,AttributeSet attributeSet) {
	    super(context,attributeSet);
	    
	    
	}
	
	@Override
	public void draw(Canvas canvas) {
		Paint paint = new Paint();
   
		paint.setTextSize(RealTimeActivity.TEXT_SIZE+1);
		paint.setColor(Color.LTGRAY);
		if(mID.isEmpty()){
			canvas.drawText("None",(float)(mWidth*0.1), (float)(50), paint );
		}
		else {
			for(int i=0; i<mID.size(); i++){	
				canvas.drawText(mID.get(i).isotopes +"    "+mID.get(i).Confidence_Level,(float)(mWidth*0.1), (float)(22*(i+1)), paint );			
			}
		}
		
		super.draw(canvas);
	}
	
	public void Set_ID_Result(Vector<Isotope> ID_result){
		mID = ID_result;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mOneBarHeight=(int)(30);
		if(mID.isEmpty()==false) mDefaultHeight = mID.size()*mOneBarHeight;
		if(mDefaultHeight < 120) mDefaultHeight = 120;
		
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
