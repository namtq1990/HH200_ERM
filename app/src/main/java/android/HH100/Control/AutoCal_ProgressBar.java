package android.HH100.Control;

import android.HH100.R;
import android.HH100.Control.SpectrumView.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class AutoCal_ProgressBar extends View {

	double mPercent = 0;

	public AutoCal_ProgressBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AutoCal_ProgressBar(Context context ,AttributeSet attributeSet) {
		super(context,attributeSet);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint data_Paints = new Paint();
		data_Paints.setColor(Color.rgb(89,17,125));

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
