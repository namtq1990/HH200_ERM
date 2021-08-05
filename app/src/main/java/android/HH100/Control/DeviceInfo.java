package android.HH100.Control;

import android.HH100.Structure.NcLibrary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class DeviceInfo extends View{
	private int m_Height=100;
	private int m_Width=480;
	public DeviceInfo(Context context,AttributeSet attrs,int defStyle) {
		super(context,attrs,defStyle);

	}
	public DeviceInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public DeviceInfo(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		Paint paint = new Paint();
		paint.setTextSize(NcLibrary.Get_fontSize(m_Width)+5);


		paint.setColor(Color.rgb(65	, 65, 65));
		canvas.drawRect(0, 0, m_Width, m_Height, paint);

		paint.setColor(Color.rgb(50, 50, 50));
		canvas.drawRect(0, (m_Height/3), m_Width, m_Height-(m_Height/3), paint);
		/////////
/*		paint.setColor(Color.WHITE);  
		canvas.drawText("Paired", 10, (m_Height/3)+3, paint);		
		canvas.drawText("NONE", 10, (m_Height/3)+30, paint);
		
		paint.setColor(Color.WHITE);
		canvas.drawText("Paired", 10, (m_Height/3)+3, paint);		
		canvas.drawText("NONE", 10, (m_Height/3)+30, paint);
	*/
	}

	private void Set_info1(String Title, String Value)
	{

	}


	///////////////////
	@Override
	protected void onFinishInflate() {
		setClickable(true);
		//Log.w(Constants.TAG,"onFinishInflate()");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// height 吏꾩쭨 �ш린 援ы븯湲�
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

		// width 吏꾩쭨 �ш린 援ы븯湲�
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


		m_Width = widthSize;
		m_Height = heightSize;

		setMeasuredDimension(widthSize, heightSize);
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

	}
	public int pow(int x, int y){ // 제곱 계산
		int result = 1;
		for(int i=0; i<y; i++){
			result*=x;
		}
		return result;
	}
}
