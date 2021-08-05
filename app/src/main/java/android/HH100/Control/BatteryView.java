package android.HH100.Control;

import android.HH100.R;
import android.HH100.RealTimeActivity;
import android.HH100.Control.SpectrumView.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class BatteryView extends View {

	double mPercent = 0;

	public BatteryView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BatteryView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint data_Paints = new Paint();
		data_Paints.setColor(Color.rgb(192, 192, 192));

		double height = getHeight();
		double width = getWidth()* 0.9 ;

		canvas.drawRoundRect(new RectF(0f, 0f, (float) (width), (float) height), 4, 4, data_Paints);

		canvas.drawRoundRect(new RectF((float) 38f, 6f, (float) (getWidth()), (float) (getHeight() * 0.8)), 3, 3, data_Paints);


		data_Paints.setColor(Color.rgb(94, 180, 55));
		double Fact = mPercent * 0.01;
		// canvas.drawRect(new RectF(0f,0f,(float)(width*Fact),(float)height),
		// data_Paints);

		canvas.drawRoundRect(new RectF(0f, 0f, (float) (width   * Fact), (float) height), 4, 4, data_Paints);

		if(mPercent  >10 && mPercent <95){

			canvas.drawRoundRect(new RectF((float) (width   * 0.1), 0f, (float) (width   * Fact), (float) height), 0, 0, data_Paints);
		}
		//canvas.drawRoundRect(new RectF(10f, 0f, (float) (width   * Fact), (float) height), 0, 0, data_Paints);



		if(mPercent > 98){
			data_Paints.setColor(Color.rgb(94, 180, 55));
			canvas.drawRoundRect(new RectF((float) 38f, 6f, (float) (getWidth()), (float) (getHeight() * 0.8)), 3, 3, data_Paints);
			canvas.drawRoundRect(new RectF(0f, 0f, (float) (width   * 1), (float) height), 4, 4, data_Paints);

		}
		data_Paints.setColor(Color.rgb(230, 230, 230));
		data_Paints.setTextSize(15f);
		data_Paints.setStrokeWidth(2.5f);
		canvas.drawText("SAM", (float)(width   * 0.2), (float)(height   * 0.73),
				data_Paints);





		super.onDraw(canvas);
	}

	public void Set_Value(double Percent) {
		mPercent = Percent;
	}
}
