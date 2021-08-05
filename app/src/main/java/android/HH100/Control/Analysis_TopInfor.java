package android.HH100.Control;

import java.text.DecimalFormat;
import java.util.Vector;

import android.HH100.RealTimeActivity;
import android.HH100.R;
import android.HH100.Structure.NcLibrary;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class Analysis_TopInfor extends View {

	private int m_Width;
	private int m_Height;

	private Paint mPaints;
	private Canvas mCanvas;

	private String mInfor1_title = "";
	private String mInfor1_Value = "";
	private String mInfor2_title = "";
	private String mInfor2_Value = "";
	private String mInfor3_title = "";
	private String mInfor3_Value = "";
	private String mInfor4_title = "";
	private String mInfor4_Value = "";
	private boolean mIsSvUnit = true;

	private double mMax_Doserate = 50000;
	private int mLog_GridCount = 3;
	private Vector<Integer> mClassColor = new Vector<Integer>();
	Path path;

	public Analysis_TopInfor(Context context, AttributeSet attrs) {
		super(context, attrs);

		mPaints = new Paint();
		path = new Path();
		mPaints = new Paint(mPaints);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		// mPaints.setStyle(Paint.Style.STROKE);
		// mPaints.setStrokeWidth(3);
		mPaints.setColor(Color.rgb(255, 255, 255));
		mPaints.setAntiAlias(true);

		mClassColor.clear();
		mClassColor.add(Color.rgb(150, 24, 150));
		mClassColor.add(Color.rgb(27, 23, 151));
		mClassColor.add(Color.rgb(44, 192, 185));
		mClassColor.add(Color.rgb(0, 150, 20)); // green
		mClassColor.add(Color.rgb(206, 28, 32));

	}

	public void Set_Class_Color(Vector<Integer> ClassColor) {
		mClassColor = ClassColor;
	}

	@SuppressWarnings("null")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		mPaints.setColor(Color.rgb(23, 22, 21));
		canvas.drawRect(0, 0, m_Width, m_Height, mPaints);// draw information
		// background

		mPaints.setColor(Color.rgb(23, 22, 21));
		canvas.drawRect(0, 0, m_Width, (int) (m_Height * 0.35), mPaints);// draw
		// information
		// background

		// 라인긋는부분

		path.reset();
		mPaints.reset();
		path.moveTo(0, 1);
		path.lineTo(m_Width, 1);
		path.moveTo(0, (int) (m_Height * 0.35));
		path.lineTo(m_Width, (int) (m_Height * 0.35));

		/*
		 * path.moveTo(m_Width-2, 1); path.lineTo(m_Width-2, (int)(m_Height *
		 * 0.35));
		 */

		mPaints.setStrokeWidth(1f);
		mPaints.setColor(Color.rgb(36, 35, 36));
		mPaints.setStyle(Paint.Style.STROKE);
		canvas.drawPath(path, mPaints);
		mPaints.reset();

		/*
		 * path.moveTo(leftInt, bottomInt + 5); path.lineTo(rightInt, bottomInt
		 * + 5);
		 */

		///////////////
		Draw_Calssification(canvas);
		Draw_Information(canvas);
		Draw_Grid(canvas);

	}

	private void Draw_Information(Canvas canvas) {
		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		// Paints.setStyle(Paint.Style.FILL_AND_STROKE);

		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE);

		// Paints.setTextAlign(Align.RIGHT);

		/*
		 * Paints.setColor(Color.GRAY); canvas.drawText(mInfor1_title, (float)
		 * (m_Width*0.45), 35, Paints); Paints.setColor(Color.WHITE);
		 * canvas.drawText( mInfor1_Value, (float)(m_Width *
		 * 0.45)+Paints.measureText(mInfor1_title)+10, 40,Paints);
		 */
		Paints.setColor(getResources().getColor(R.color.WhiteGray));
		canvas.drawText(mInfor2_title, (float) (m_Width * 0.5), 35, Paints);
		Paints.setColor(Color.WHITE);
		canvas.drawText(mInfor2_Value, (float) (m_Width * 0.5) + Paints.measureText(mInfor2_title) + 5, 35, Paints);

		Paints.setColor(getResources().getColor(R.color.WhiteGray));
		canvas.drawText(mInfor3_title, (float) (m_Width * 0.67), 35, Paints);
		Paints.setColor(Color.WHITE);
		canvas.drawText(mInfor3_Value, (float) (m_Width * 0.67) + Paints.measureText(mInfor3_title) + 5, 35, Paints);

		Paints.setColor(getResources().getColor(R.color.WhiteGray));
		canvas.drawText(mInfor4_title, (float) (m_Width * 0.85), 35, Paints);
		Paints.setColor(Color.WHITE);
		canvas.drawText(mInfor4_Value, (float) (m_Width * 0.85) + Paints.measureText(mInfor4_title) + 5, 35, Paints);

	}

	private void Draw_Grid(Canvas canvas) {

		int LR_Margin = 20;
		float Grid_Height = (float) m_Height * 0.8f;

		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(RealTimeActivity.TEXT_SIZE);

		paint.setStrokeWidth(1f);
		canvas.drawLine((float) LR_Margin, Grid_Height, (float) m_Width - LR_Margin, Grid_Height, paint);

		// nuclide
		paint.setStrokeWidth(1f);
		paint.setTextAlign(Align.RIGHT);
		paint.setColor(Color.WHITE);
		canvas.drawText(getResources().getString(R.string.nuclide), (m_Width * 0.1f) - 10, Grid_Height - 5, paint);
		paint.setColor(Color.GRAY);
		canvas.drawLine((float) m_Width * 0.1f, Grid_Height - 20, (float) m_Width * 0.1f, m_Width, paint);

		// index
		paint.setStrokeWidth(0f);
		float leftSpace = m_Width * 0.1f;
		float UsingWidth = m_Width - leftSpace - LR_Margin;
		// int LogScale_GridCount = Get_LogScale_GridCount(mMax_Doserate);

		for (int i = 0; i < mLog_GridCount; i++) {
			float OneBlockWidth = UsingWidth / mLog_GridCount;
			paint.setColor(Color.WHITE);
			canvas.drawText(
					SvToString(pow(10, i + 1), true, false, mIsSvUnit)
							+ SvToString(pow(10, i + 1), true, true, mIsSvUnit),
					(float) (m_Width * 0.1f) + (OneBlockWidth * (i + 1)) - 10, Grid_Height - 5, paint);
			paint.setColor(Color.GRAY);
			canvas.drawLine((float) (m_Width * 0.1f) + (OneBlockWidth * (i + 1)), Grid_Height - 15,
					(float) (m_Width * 0.1f) + (OneBlockWidth * (i + 1)), m_Width, paint);
		}
	}

	private void Draw_Calssification(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint paint = new Paint();
		//////////// -- classification index

		float startXGray, startXColor, rectBottom, textY, startYGray, startYColor;

		textY = 33f;

		startXGray = 6f;

		startXColor = 6f;

		startYColor = 10;

		rectBottom = 36f;

		paint.setColor(mClassColor.get(0));
		canvas.drawRect(16f + startXGray, startYColor, 29f, rectBottom + 2f, paint);

		paint.setColor(mClassColor.get(0));
		canvas.drawRect(18f + startXColor, startYColor + 2, 27f, rectBottom, paint);

		float right = 27f;

		paint.setTextSize(RealTimeActivity.TEXT_SIZE + 1);
		paint.setColor(Color.WHITE);
		canvas.drawText("SNM", right + 8f, textY, paint);



		right = right + 8f + 8f;

		// 2

		paint.setColor(mClassColor.get(1));
		canvas.drawRect(right + paint.measureText("SNM") + 8f + startXGray, startYColor,
				right + paint.measureText("SNM") + 21f, rectBottom + 2f, paint);

		paint.setColor(mClassColor.get(1));
		canvas.drawRect(right + paint.measureText("SNM") + 10f + startXColor, startYColor + 2,
				right + paint.measureText("SNM") + 19f, rectBottom, paint);
		right = right + paint.measureText("SNM") + 19f;

		paint.setColor(Color.WHITE);
		canvas.drawText("IND", right + 8f, textY, paint);
		right = right + 8f + 8f;
		// 3

		paint.setColor(mClassColor.get(2));
		canvas.drawRect(right + paint.measureText("IND") + 8f + startXGray, startYColor,
				right + paint.measureText("IND") + 21f, rectBottom + 2f, paint);

		paint.setColor(mClassColor.get(2));
		canvas.drawRect(right + paint.measureText("IND") + 10f + startXColor, startYColor + 2,
				right + paint.measureText("IND") + 19f, rectBottom, paint);
		right = right + paint.measureText("IND") + 19f;

		paint.setColor(Color.WHITE);
		canvas.drawText("MED", right + 8f, textY, paint);
		right = right + 8f + 8f;
		// 4

		paint.setColor(mClassColor.get(3));
		canvas.drawRect(right + paint.measureText("MED") + 8f + startXGray, startYColor,
				right + paint.measureText("MED") + 21f, rectBottom + 2, paint);

		paint.setColor(mClassColor.get(3));
		canvas.drawRect(right + paint.measureText("MED") + 10f + startXColor, startYColor + 2,
				right + paint.measureText("MED") + 19f, rectBottom, paint);
		right = right + paint.measureText("MED") + 19f;

		paint.setColor(Color.WHITE);
		canvas.drawText("NRM", right + 8f, textY, paint);

		right = right + 8 + 8;
		// 50

		paint.setColor(mClassColor.get(4));
		canvas.drawRect(right + paint.measureText("NRM") + 8f + startXGray, startYColor,
				right + paint.measureText("NRM") + 21f, rectBottom + 2, paint);

		paint.setColor(mClassColor.get(4));
		canvas.drawRect(right + paint.measureText("NRM") + 10f + startXColor, startYColor + 2,
				right + paint.measureText("NRM") + 19f, rectBottom, paint);
		right = right + paint.measureText("NRM") + 19;

		paint.setColor(Color.WHITE);
		canvas.drawText("UNK", right + 8f, textY, paint);
	}

	public void Set_Log_GridCount(int count) {
		mLog_GridCount = count;
	}

	public void Set_infor1(String Title, String value) {
		mInfor1_title = Title;
		mInfor1_Value = value;
	}

	public void Set_infor2(String Title, String value) {

		mInfor2_title = Title;
		mInfor2_Value = value;
	}

	public void Set_infor3(String Title, String value) {
		mInfor3_title = Title;
		mInfor3_Value = value;
	}

	public void Set_infor4(String Title, String value) {
		mInfor4_title = Title;
		mInfor4_Value = value;
	}

	private int Get_fontSize() {
		return (int) ((m_Width * 0.01) + 10);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onFinishInflate() {
		setClickable(true);
		// Log.w(Constants.TAG,"onFinishInflate()");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// height 筌욊쑴彛� 占싼덈┛ �뤃�뗫릭疫뀐옙
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

	public int pow(int x, int y) { // �젣怨� 怨꾩궛
		int result = 1;
		for (int i = 0; i < y; i++) {
			result *= x;
		}
		return result;
	}

	private int Get_LogScale_GridCount(double Doserate) {
		double sdq = pow(10, 1);
		double sdq2 = pow(10, 2);
		int GridCount = 0;
		for (int i = 0; i < 100; i++) {
			if (pow(10, i) <= Doserate && pow(10, i + 1) > Doserate) {
				GridCount = i;
				break;
			}
		}
		// m_AxisY_Grid_Count = log10(m_Display_End_AxisY); //理쒕�移댁슫�듃瑜� 10�쓣
		// 諛묒쑝濡� �븯�뒗 濡쒓렇�솕
		// m_data_Y_MAX_value = pow(10,GridCount);

		return GridCount;
	}

	public void Set_Doserate_Unit(boolean IsSv) {
		mIsSvUnit = IsSv;
	}

	public String SvToString(double nSv, boolean point, boolean OnlyUnit, boolean IsSvUnit) { // �닽�옄�삎
		// �떆蹂댄듃
		// 媛믪쓣
		// string�쑝濡�

		if (IsSvUnit == false)
			nSv = nSv * 100;
		DecimalFormat format = new DecimalFormat();
		String unit = " Sv/h";
		double value = 1;
		int checker = 0;
		if (point == true) {
			if (nSv < NcLibrary.pow(10, 3)) {
				value = nSv * 0.001;
				unit = " uSv/h";
				checker = 1;
			} else if (nSv >= NcLibrary.pow(10, 3) & nSv < NcLibrary.pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= NcLibrary.pow(10, 6) & nSv < NcLibrary.pow(10, 9)) {
				value = (nSv * 0.000001);
				unit = " mSv/h";
			} else if (nSv > NcLibrary.pow(10, 9)) {
				value = (nSv * 0.000000001);
				unit = " Sv/h";
			}
		} else {
			if (nSv < NcLibrary.pow(10, 3)) {
				value = nSv;
				unit = " nSv/h";
			} else if (nSv >= NcLibrary.pow(10, 3) & nSv < NcLibrary.pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= NcLibrary.pow(10, 6) & nSv < NcLibrary.pow(10, 9)) {
				value = nSv * 0.000001;
				unit = " mSv/h";
			} else if (nSv > NcLibrary.pow(10, 9)) {
				value = nSv * 0.000000001;
				unit = " Sv/h";
			}
		}

		if (checker == 1)
			format.applyLocalizedPattern("0.###");
		else
			format.applyLocalizedPattern("0.##");

		if (OnlyUnit) {
			if (IsSvUnit == false) {
				unit = unit.replace("Sv/h", "rem/h");
			}
			return unit;
		} else {
			if (IsSvUnit == false) {
				unit = unit.replace("Sv/h", "rem/h");
			}
			return format.format(value);
		}

	}
}
