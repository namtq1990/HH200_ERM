package android.HH100.Control;

import android.HH100.R;
import android.HH100.Control.SpectrumView.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class ProgressBar extends View {
	
	double mPercent = 0;

	public ProgressBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ProgressBar(Context context ,AttributeSet attributeSet) {
        super(context,attributeSet);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Paint data_Paints = new Paint();
    	data_Paints.setColor(Color.rgb(243,152,15));
    	
    	double height = getHeight();
    	double width = getWidth();
    	
    	double Fact = mPercent*0.01;
    	canvas.drawRect(new RectF(0f,0f,(float)(width*Fact),(float)height), data_Paints);
		
		super.onDraw(canvas);
	}
	public void Set_Value(double Percent) {
		mPercent = Percent;
	}
}
