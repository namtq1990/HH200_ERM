package android.HH100.Control;

import java.util.Vector;

import android.HH100.RealTimeActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

public class RealActivitySpectrumView extends View {

	private Rect mMainRect;
	private Rect mDataRect;

	private int mAxisY_Count = 3;
	private int mAxisX_Count = 5;

	private double[] mData = new double[30];

	private double mAxisY_MaxValue;
	private String mAxisY_Unit = "cps";

	public RealActivitySpectrumView(Context context) {
		super(context);

	}

	public RealActivitySpectrumView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public RealActivitySpectrumView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public void Set_AxisY_Unit(String Unit) {
		mAxisY_Unit = Unit;
	}

	public void Set_Data(double[] Data) {
		mData = Data;
		double Max = 0;
		for (int i = 0; i < mData.length; i++)
			Max = Math.max(Max, mData[i]);
		mAxisY_MaxValue = (double) (Max * 1.2);

		mAxisY_Count = 3;

		// String str = Integer.toString(i);

		// Toast.makeText(getApplicationContext(), str, 1).show();
	}

	public void Set_Data(Vector<Double> Data) {

		if (Data == null)
			return;

		mData = new double[Data.size()];
		for (int i = 0; i < Data.size(); i++)
			mData[i] = Data.get(i);

		double Max = 0;
		for (int i = 0; i < mData.length; i++)
			Max = Math.max(Max, mData[i]);
		mAxisY_MaxValue = (double) (Max * 1.2);
		mAxisY_Count = 3;

		if (mAxisY_Count == 1 || mAxisY_Count == 2) {

			mAxisY_Count = 3;

		}

	}

	private int Get_LogScale_GridCount_AxisY() {
		double sdq = Math.pow(10, 1);
		double sdq2 = Math.pow(10, 2);
		int GridCount = 0;
		for (int i = 0; i < 100; i++) {
			/*
			 * if (Math.pow(10, i) <= mAxisY_MaxValue && Math.pow(10, i + 1) >
			 * mAxisY_MaxValue) { GridCount = i; break; }
			 */

			if ((500 * i) <= mAxisY_MaxValue && (500 * (i + 1)) > mAxisY_MaxValue) {
				GridCount = i;
				break;
			}

		}
		return GridCount + 1;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Paint paint = new Paint();
		paint.setColor(Color.rgb(22, 22, 21));

		canvas.drawRect(mMainRect, paint);

		Draw_Grid(canvas);

		Paint Gra_p = new Paint();
		Gra_p.setStyle(Paint.Style.FILL);
		Gra_p.setAntiAlias(true);

		Gra_p.setShader(new LinearGradient(0, (float) 0, 0, mDataRect.height() * 0.2f, Color.argb(255, 0, 0, 0),
				Color.argb(200, 0, 0, 0), TileMode.CLAMP));
		// canvas.drawRect(new
		// Rect(mDataRect.left,mDataRect.top,mDataRect.right,(int)(mDataRect.top+(mDataRect.height()*0.2))),
		// Gra_p);
		Gra_p.setShader(new LinearGradient(0, (float) 0, 0, mDataRect.height() * 0.2f, Color.argb(200, 0, 0, 0),
				Color.argb(255, 0, 0, 0), TileMode.CLAMP));
		// canvas.drawRect(new
		// Rect(mDataRect.left,(int)(mDataRect.bottom-(mDataRect.height()*0.2)),mDataRect.right,mDataRect.bottom),
		// Gra_p);

		Draw_Axis(canvas);
		Draw_AxisX_Text(canvas);
		Draw_AxisY_Text(canvas);
		Draw_Graph(canvas);

		paint.setColor(Color.rgb(173, 125, 68));

		/*
		 * paint.setTextAlign(Align.RIGHT);
		 * paint.setTypeface(Typeface.create(Typeface.DEFAULT,
		 * Typeface.NORMAL)); paint.setTextSize(RealTimeActivity.TEXT_SIZE + 5);
		 * canvas.drawText(mAxisY_Unit, mDataRect.right, mDataRect.top , paint);
		 */

		super.onDraw(canvas);
	}

	private void Draw_Graph(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.rgb(255, 201, 14));
		paint.setStrokeWidth(3);

		Path SpcPath = new Path();
		SpcPath.reset();
		boolean MoveToCheck = false;

		for (int i = 0; i < mData.length; i++) {
			if (mData[i] == -1.0)
				continue;

			float OneWidth = (mDataRect.width() / (mData.length - 1));
			float line_X = OneWidth * (i);

			// double log_yMax = Math.log10(Math.pow(10, (mAxisY_Count)));

			double log_yMax = ((((int) mAxisY_MaxValue / 3) * mAxisY_Count));

			double log_yValue = (mData[i]);

			if (mData[i] == 0)
				log_yValue = 0;
			if (mData[i] == 1)
				log_yValue = Math.log10(2) - 0.1;

			log_yValue = ((log_yValue / log_yMax) * 100) * 0.01;

			if (MoveToCheck == false) {
				SpcPath.moveTo((float) (mDataRect.left + line_X), (float) (mDataRect.bottom - (mDataRect.height() * log_yValue)));
				MoveToCheck = true;
			} else
				SpcPath.lineTo((float) (mDataRect.left + line_X), (float) (mDataRect.bottom - (mDataRect.height() * log_yValue)));

		}

		paint.setStyle(Paint.Style.STROKE);
		canvas.drawPath(SpcPath, paint);
		paint.reset();
	}


	private void Draw_Grid(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.DKGRAY);
		paint.setStrokeWidth(2);

		for (int i = 0; i < mAxisY_Count; i++) {

			float OneWidth = (mDataRect.height() / mAxisY_Count);
			float line_Y = OneWidth * (i + 1);

			canvas.drawLine(mDataRect.left, mDataRect.bottom - line_Y, mDataRect.right, mDataRect.bottom - line_Y,
					paint);

		}

		paint.reset();
	}

	private void Draw_Axis(Canvas canvas) {

		Paint paint = new Paint();
		paint.setColor(Color.rgb(157, 158, 158));
		paint.setStrokeWidth(2);
		canvas.drawLine(mDataRect.left, mDataRect.bottom, mDataRect.right, mDataRect.bottom, paint);
		// canvas.drawLine(mDataRect.left, mDataRect.top, mDataRect.left,
		// mDataRect.bottom, paint);
		paint.reset();
	}

	private void Draw_AxisX_Text(Canvas canvas) {

		Paint paint = new Paint();
		paint.setColor(Color.rgb(157, 158, 158));
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(RealTimeActivity.TEXT_SIZE);
		for (int i = 1; i < mAxisX_Count; i++) {

			float OneWidth = (mDataRect.width() / mAxisX_Count);
			float line_X = OneWidth * (i);

			canvas.drawText((mAxisX_Count - i) + "0s", line_X + mDataRect.left, mDataRect.bottom + 30, paint);
			if (i == 4) {
				canvas.drawText("Now", mDataRect.right, mDataRect.bottom + 30, paint);
				break;
			}

		}
		paint.reset();
	}

	private void Draw_AxisY_Text(Canvas canvas) {

		Paint paint = new Paint();
		paint.setColor(Color.rgb(157, 158, 158));

		paint.setTextAlign(Align.RIGHT);
		paint.setTextSize(RealTimeActivity.TEXT_SIZE);

		float textWidth = paint.measureText("00000");
		for (int i = 0; i < mAxisY_Count + 1; i++) {

			float OneWidth = (mDataRect.height() / mAxisY_Count);
			float line_Y = OneWidth * (i);

			/*
			 * canvas.drawText(NcLibrary.Prefix((int) Math.pow(10, i)),
			 * mDataRect.left - 5, mDataRect.bottom - line_Y, paint);
			 */

			if (i == 0) {

				canvas.drawText(Cut_Decimal_Point((i * ((int) mAxisY_MaxValue / 3))), mDataRect.left - 7,
						mDataRect.bottom - line_Y + 2, paint);

			} else {

				canvas.drawText(Cut_Decimal_Point((i * ((int) mAxisY_MaxValue / 3))), mDataRect.left - 7,
						mDataRect.bottom - line_Y + 7, paint);
			}

		}
		paint.reset();
	}

	private void OnRectSize(int Width, int height) {
		if (mMainRect == null)
			mMainRect = new Rect(0, 0, Width, height);
		if (mDataRect == null)
			mDataRect = new Rect(80, 50, Width - 30, height - 40);

	}

	public int measureTextHeight(String text, Paint paint) {
		Rect result = new Rect();
		// Measure the text rectangle to get the height
		paint.getTextBounds(text, 0, text.length(), result);
		return result.height();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = 0;
		switch (heightMode) {
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
		switch (widthMode) {
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

		setMeasuredDimension(widthSize, heightSize);
		OnRectSize(widthSize, heightSize);
	}

	public String Cut_Decimal_Point(int value) {

		float sum = 0;
		String sumStr = "";

		if ((int) value == 0) {

			sum = (float) value / 1000;

			sumStr = String.format("%.0f", sum);

			// sumStr = sumStr + "k";

		} else if ((int) value >= ((int) mAxisY_MaxValue / 3)) {

			sum = (float) value / 1000;

			sumStr = String.format("%.1f", sum);
			float sum2;
			sum2 = Float.valueOf(sumStr).floatValue();

			sum2 = sum2 - (int) sum2;
			if (sum2 == 0) {

				sumStr = String.format("%.0f", sum);
			}

			sumStr = sumStr + "k";

		}

		return sumStr;
	}
}
