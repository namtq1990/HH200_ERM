package android.HH100.Control;

import java.text.*;

import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.RealTimeActivity;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.hardware.camera2.params.RggbChannelVector;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

public class GuageView extends View {

	private static final String TAG = "GaugeView";
	private static final boolean D = MainActivity.D;
	private static final int MAX_ARC_VALUE = 230;

	private Paint mPaints;
	private RectF mGaugeSize;

	// private RectF mGaugeSize1;
	private double mValue = 0;
	private int DEAFALUT_MAX_LOG = 4;

	private int RealWidth;
	private int RealHeight;

	private String mCaption1 = ""; // 怨꾧린�뙋�쓽 �닔移� 寃쎄퀎瑜� �쐞�븳 �닔
	private String mCaption2 = "";
	private String mCaption3 = "";
	private String mCaption4 = "";
	private String mCaption5 = "";

	private boolean mIsSvUnit = false;

	private double mMaxValue = 0;

	// public double mNow_Inputed_nSv = 0;
	private int mCPS = 0;
	public int mP = 10; // �샎�옄��湲� �떆�뿕�슜

	private boolean mStart = false;
	private String WarningInfo = null;
	private boolean mIsEvent = false;

	public GuageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// this.text = attrs.getAttributeValue(null,"text");

	}

	public GuageView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStyle(Paint.Style.STROKE);
		mPaints.setStrokeWidth(10);
		mPaints.setColor(Color.rgb(0, 200, 0));
		mPaints.setAntiAlias(true);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		mGaugeSize = new RectF(20, 20, 300, 300);
		// mGaugeSize1 = new RectF(120, 20, 400, 300);

	}

	public GuageView(Context context) {
		super(context);
		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStyle(Paint.Style.STROKE);
		mPaints.setStrokeWidth(5);
		mPaints.setColor(Color.rgb(0, 200, 0));
		mPaints.setAntiAlias(true);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		mGaugeSize = new RectF(20, 20, 300, 300);
		// mGaugeSize1 = new RectF(120, 20, 400, 300);

	}

	public void Set_toSvUnit(boolean IsSv) {

		mIsSvUnit = IsSv;

	}

	public void SetEvent(boolean Start) {
		mIsEvent = Start;
	}

	public int pow(int x, int y) { // �젣怨� 怨꾩궛
		int result = 1;
		for (int i = 0; i < y; i++) {
			result *= x;
		}
		return result;
	}

	public String SvToString(double nSv, boolean point) { // �닽�옄�삎 �떆蹂댄듃 媛믪쓣
		// string�쑝濡�

		if (mIsSvUnit == false)
			nSv = nSv * 100;

		DecimalFormat format = new DecimalFormat();
		String unit = " Sv/h";
		double value = 1;
		int checker = 0;
		if (point == true) {
			if (nSv < pow(10, 3)) {
				value = nSv * 0.001;
				unit = " uSv/h";
				checker = 1;
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = (nSv * 0.000001);
				unit = " mSv/h";
			} else if (nSv > pow(10, 9)) {
				value = (nSv * 0.000000001);
				unit = " Sv/h";
			}
		} else {
			if (nSv < pow(10, 3)) {
				value = nSv;
				unit = " nSv/h";
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = nSv * 0.000001;
				unit = " mSv/h";
			} else if (nSv > pow(10, 9)) {
				value = nSv * 0.000000001;
				unit = " Sv/h";
			}
		}

		if (checker == 1)
			format.applyLocalizedPattern("0.###");
		else
			format.applyLocalizedPattern("0.##");

		if (mIsSvUnit == false) {
			unit = unit.replace("Sv/h", "rem/h");
		}
		return format.format(value) + unit;

	}

	public String SvToString(double nSv, boolean point, boolean OnlyUnit) { // �닽�옄�삎
		// �떆蹂댄듃
		// 媛믪쓣
		// string�쑝濡�

		if (mIsSvUnit == false)
			nSv = nSv * 100;
		DecimalFormat format = new DecimalFormat();
		String unit = " Sv/h";
		double value = 1;
		int checker = 0;
		if (point == true) {
			if (nSv < pow(10, 3)) {
				value = nSv * 0.001;
				unit = " uSv/h";
				checker = 1;
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = (nSv * 0.000001);
				unit = " mSv/h";
			} else if (nSv > pow(10, 9)) {
				value = (nSv * 0.000000001);
				unit = " Sv/h";
			}
		} else {
			if (nSv < pow(10, 3)) {
				value = nSv;
				unit = " nSv/h";
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = " uSv/h";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = nSv * 0.000001;
				unit = " mSv/h";
			} else if (nSv > pow(10, 9)) {
				value = nSv * 0.000000001;
				unit = " Sv/h";
			}
		}

		if (checker == 1)
			format.applyLocalizedPattern("0.###");
		else
			format.applyLocalizedPattern("0.##");

		if (OnlyUnit) {
			if (mIsSvUnit == false)
			{
				unit = unit.replace("Sv/h", "rem/h");
			}
			return unit;
		} else {
			if (mIsSvUnit == false)
			{
				unit = unit.replace("Sv/h", "rem/h");
			}
			return format.format(value);
		}

	}

	public void SETnSv(double Value, int CPS) { // 蹂� �뙣�꼸�뿉 吏곸젒�쟻�씤 �꽑�웾�쓣
		// �엯�젰�븳�떎.
		if (Value > 1000000000)
			Stop();

		mValue = Value;// (float)SvToGuageBarValue(Value);
		mMaxValue = ((int) Math.log10(mValue)) + 1;
		if (mMaxValue <= 4)
			mMaxValue = 4;

		mCPS = CPS;

		mCaption5 = Prefix_CPS((int) Math.pow(10, mMaxValue));
		mCaption4 = Prefix_CPS((int) Math.pow(10, mMaxValue - 1));
		mCaption3 = Prefix_CPS((int) Math.pow(10, mMaxValue - 2));
		mCaption2 = Prefix_CPS((int) Math.pow(10, mMaxValue - 3));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawColor(Color.alpha(Color.WHITE));

		Paint ppaint = new Paint();
		ppaint.setStyle(Paint.Style.FILL_AND_STROKE);
		ppaint.setStrokeWidth(1);
		ppaint.setAntiAlias(true);
		ppaint.setColor(Color.WHITE);
		ppaint.setTextSize(15);

		int DoserateTextSize = (int) (getHeight() * 0.12);
		int CpsTextSize = (int) (getWidth() * 0.07);
		if (mStart == true) {
			DrawCaption(canvas, ppaint, mCaption1, mCaption2, mCaption3, mCaption4, mCaption5);

			if (mIsEvent) {
				mPaints.setColor(Color.RED);
				// mPaints.setShadowLayer(15,0, 0, Color.RED);
				ppaint.setColor(Color.rgb(222, 175, 14));
				ppaint.setTextSize(DoserateTextSize);
				// ppaint.setShadowLayer((float)10, (float)0, (float)0,
				// Color.YELLOW);
			} else {

				mPaints.setColor(Color.WHITE);
				// mPaints.setShadowLayer(15,0, 0, Color.rgb(22,22,21));
				ppaint.setColor(Color.WHITE);
				ppaint.setTextSize(DoserateTextSize);
				// ppaint.setShadowLayer((float)15, (float)0, (float)0,
				// Color.GREEN);

			}

			double gap = mMaxValue - DEAFALUT_MAX_LOG;
			if (gap < 0)
				gap = 0;
			if (mMaxValue == 0) {

				double ValueLog = Math.log10((mValue == 0) ? 1 : mValue);
				double Percentage = 0;

				canvas.drawArc(mGaugeSize, -205, (float) (MAX_ARC_VALUE * Percentage), false, mPaints);

				// canvas.drawArc(mGaugeSize1, -205, (float) (MAX_ARC_VALUE *
				// Percentage), false, mPaints);
			} else {
				double ValueLog = Math.log10((mValue == 0) ? 1 : mValue);
				double Percentage = (ValueLog - gap) / (mMaxValue - gap);

				canvas.drawArc(mGaugeSize, -205, (float) (MAX_ARC_VALUE * Percentage), false, mPaints);

				// canvas.drawArc(mGaugeSize1, -205, (float) (MAX_ARC_VALUE *
				// Percentage), false, mPaints);
			}

			// -----------------------------------------------------
			float ValueWidth = 0;
			float UnitWidth = 0;
			String unit = " " + SvToString(mValue, true, true);
			String value = SvToString(mValue, true, false);

			ValueWidth = ppaint.measureText(value);
			ppaint.setTextSize(CpsTextSize);
			UnitWidth = ppaint.measureText(unit);

			float size = (ValueWidth + UnitWidth) / 2;

			ppaint.setTextSize(DoserateTextSize);
			canvas.drawText(value, (RealWidth / 2) - size, RealHeight / (float) 2, ppaint);
			ppaint.setTextSize(CpsTextSize);
			canvas.drawText(unit, ((RealWidth / 2) - size) + ValueWidth, RealHeight / (float) 2, ppaint);

			ppaint.setTextSize(CpsTextSize);
			ppaint.setColor(Color.GRAY);
			String CPS = String.valueOf(Prefix_CPS(mCPS)) + " cps";
			ValueWidth = ppaint.measureText(CPS);
			ValueWidth = (RealWidth / 2) - (ValueWidth / 2);

			int sdq = measureTextHeight("0", ppaint);
			canvas.drawText(CPS, ValueWidth, (RealHeight / (float) 2) + sdq + 15, ppaint);

			if (WarningInfo != null) {
				ppaint.setColor(Color.DKGRAY);
				// canvas.drawRect(new Rect((RealWidth/2)-40,(int)
				// (RealHeight*0.67),(RealWidth/2)+40,(int) (RealHeight*0.73)),
				// ppaint);

				ppaint.setTextSize(DoserateTextSize - 45);
				ppaint.setTextAlign(Align.CENTER);

				ppaint.setColor(Color.argb(180, 180, 180, 180));
				ppaint.setStrokeWidth(0.3f);
				ValueWidth = ppaint.measureText(WarningInfo);
				ValueWidth = (RealWidth / 2) - (ValueWidth / 2);
				canvas.drawText(WarningInfo, RealWidth / 2, (float) (RealHeight * 0.8), ppaint);

			}
		}

	}

	public int measureTextHeight(String text, Paint paint) {
		Rect result = new Rect();
		// Measure the text rectangle to get the height
		paint.getTextBounds(text, 0, text.length(), result);
		return result.height();
	}

	private void Draw_Warning_Info(Canvas canvas) {
		int DoserateTextSize = (int) (RealWidth * 0.12);
		double ValueWidth = 0;

		if (mCPS > 200) {
			Paint ppaint = new Paint();
			ppaint.setColor(getResources().getColor(R.color.DarkGray));
			canvas.drawRect(new Rect(0, (int) (RealHeight * 0.80), RealWidth, (int) (RealHeight * 0.87)), ppaint);

			ppaint.setTextSize(DoserateTextSize - 35);
			ppaint.setTextAlign(Align.CENTER);

			ppaint.setColor(Color.WHITE);
			ppaint.setStrokeWidth(3);
			ValueWidth = ppaint.measureText(WarningInfo);
			ValueWidth = (RealWidth / 2) - (ValueWidth / 2);
			canvas.drawText(WarningInfo, RealWidth / 2, (float) (RealHeight * 0.86), ppaint);
		}
	}

	public void Show_WarningInfo_Text(String Text) {
		WarningInfo = Text;
	}

	public void Hide_WarningInfo_Text() {
		WarningInfo = null;
	}

	public String Prefix_CPS(int CPS) {
		DecimalFormat format = new DecimalFormat();
		String Result = null;

		char Pref = 0;
		int ConversionFactor = 1;
		if (CPS > 100000) {
			ConversionFactor = 1000;
			Pref = 'K';
		} // count媛� 留롮븘吏� 寃쎌슦 SI Prefix�궗�슜
		if (CPS > 100000000) {
			ConversionFactor = 1000000;
			Pref = 'M';
		}

		if (ConversionFactor == 1)
			Result = String.valueOf(CPS);
		else
			format.applyLocalizedPattern("0.#");
		Result = format.format(CPS / (double) ConversionFactor) + Pref;

		return Result;
	}

	public static int getSweepInc() {

		return 1;
	}

	public void setRect(Rect rect) {
		/*
		 * mBigOval.left =rect.left; mBigOval.right =rect.right; mBigOval.top
		 * =rect.top; mBigOval.bottom =rect.bottom;
		 */
	}

	public void SetPanelValue() {
		// Drawable mBluetoothImage_flag = this.get;

	}

	public void DrawCaption(Canvas canvas, Paint ppaint, String caption1, String caption2, String caption3,
							String caption4, String caption5) {
		ppaint.setTextSize(RealTimeActivity.TEXT_SIZE);
		ppaint.setTextAlign(Align.RIGHT);

		canvas.drawText(caption1, (float) (getWidth() * 0.16), (float) (getHeight() * 0.7), ppaint);

		subscriptDrawText(canvas, ppaint, (float) (getWidth() * 0.23), (float) (getHeight() * 0.35),
				(int) Math.pow(10, mMaxValue - 3), RealTimeActivity.TEXT_SIZE);
		ppaint.setTextAlign(Align.RIGHT);
		subscriptDrawText(canvas, ppaint, getWidth() / 2, (float) (mGaugeSize.height() * 0.22),
				(int) Math.pow(10, mMaxValue - 2), RealTimeActivity.TEXT_SIZE);
		ppaint.setTextAlign(Align.RIGHT);
		subscriptDrawText(canvas, ppaint, (float) (getWidth() * 0.79), (float) (getHeight() * 0.35),
				(int) Math.pow(10, mMaxValue - 1), RealTimeActivity.TEXT_SIZE);

		subscriptDrawText(canvas, ppaint, (float) (getWidth() * 0.81), (float) (getHeight() * 0.7),
				(int) Math.pow(10, mMaxValue), RealTimeActivity.TEXT_SIZE);

		ppaint.setTextSize(15);
		ppaint.setStrokeWidth((float) 0.1);
		canvas.drawText("(nSv/h)", (float) (getWidth() * 0.91), (float) (getHeight() * 0.702), ppaint);

		ppaint.setTextAlign(Align.LEFT);

		/*
		 * canvas.drawText(caption2, (float) (getWidth() * 0.2), (float)
		 * (getHeight() * 0.35), ppaint);
		 *
		 * ppaint.setTextAlign(Align.CENTER); canvas.drawText(caption3,
		 * getWidth() / 2, (float) (mGaugeSize.height() * 0.22), ppaint);
		 *
		 * ppaint.setTextAlign(Align.RIGHT); canvas.drawText(caption4, (float)
		 * (getWidth() * 0.8), (float) (getHeight() * 0.35), ppaint);
		 * canvas.drawText(caption5, (float) (getWidth() * 0.84), (float)
		 * (getHeight() * 0.7), ppaint);
		 *
		 * ppaint.setTextAlign(Align.LEFT);
		 */
	}

	public void Start() {

		mStart = true;
		invalidate();

	}

	public void Stop()
	{

		mStart = false;
		invalidate();

	}

	@Override
	protected void onFinishInflate() {
		setClickable(true);
		// Log.w(Constants.TAG,"onFinishInflate()");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
		// Log.w(Constants.TAG,"onMeasure("+widthMeasureSpec+","+heightMeasureSpec+")");
		// LayoutWidth=widthSize;
		// LayoutHeight=heightSize;

		if (widthSize > heightSize) {
			RealWidth = heightSize;
			RealHeight = widthSize;
		}

		else {
			RealWidth = widthSize;
			RealHeight = heightSize;
		}

		/*
		 * mBigOval.left =(RealWidth/(float)5.2); mBigOval.top
		 * =RealHeight/(float)3.25; mBigOval.right
		 * =(RealWidth-(RealWidth/(float)5.2));
		 * mBigOval.bottom=RealHeight/(float)3.25+((RealWidth/(float)5.2)*3)+10;
		 */

		mGaugeSize.left = (float) (RealWidth * 0.08);
		mGaugeSize.top = (float) (RealHeight * 0.08);
		mGaugeSize.right = (float) (RealWidth - (RealWidth * 0.08));
		mGaugeSize.bottom = (float) (RealHeight - (RealHeight * 0.08));

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

	}

	public void subscriptDrawText(Canvas canvas, Paint paint, float txtX, float txtY, int value, Float TextSize) {
		// int value = Integer.parseInt(inputMessage);

		if (value == 10) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "1", TextSize);

		} else if (value == 100) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "2", TextSize);

		} else if (value == 1000) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "3", TextSize);

		} else if (value == 10000) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "4", TextSize);

		} else if (value == 100000) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "5", TextSize);

		} else if (value == 1000000) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "6", TextSize);

		} else if (value == 10000000) {

			subscriptDrawText(canvas, paint, txtX, txtY, "10", "7", TextSize);

		}

	}

	public void subscriptDrawText(Canvas canvas, Paint paint, float txtX, float txtY, String inputMessage,
								  String supScript, Float TextSize) {

		paint.setColor(Color.rgb(157, 158, 158));
		paint.setStrokeWidth((float) 0.2);

		TextSize = TextSize + 3;

		float subAndSupTextSize = TextSize * (float) 0.65;

		float subScriptDownLangth = TextSize * (float) 0.253;
		float supScriptUpLangth = TextSize * (float) 0.392;

		paint.setTextSize(TextSize);

		float height = paint.getFontSpacing();
		canvas.drawText(inputMessage, txtX, txtY, paint);
		float height1 = 0;
		float width1 = paint.measureText(inputMessage);

		if (supScript != null) {
			// Paint paint1 = new Paint();
			paint.setColor(Color.rgb(157, 158, 158));
			paint.setStrokeWidth((float) 0.2);
			paint.setTextSize(subAndSupTextSize);

			float height3 = paint.getFontSpacing();

			canvas.drawText(supScript, txtX + width1 - 14,
					txtY - ((height - height1) / subAndSupTextSize) - supScriptUpLangth, paint);
		}
	}

}
