package android.HH100.Control;

import android.HH100.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class BatteryBar extends View {

	private int mWidth;
	private int mHeight;

	private double mValuePercent;


	private Paint mPaints;
	private Canvas mCanvas;

	public BatteryBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//mWidth = display.getWidth();
		//mHeight = display.getHeight();

		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		mPaints.setColor(Color.rgb(255, 255, 255));
		mPaints.setAntiAlias(true);
	}

	public BatteryBar(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//mWidth = display.getWidth();
		//mHeight = display.getHeight();

		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		mPaints.setColor(Color.rgb(255, 255, 255));
		mPaints.setAntiAlias(true);
	}

	public BatteryBar(Context context) {
		super(context);
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		//mWidth = display.getWidth();
		//mHeight = display.getHeight();	

		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		mPaints.setColor(Color.rgb(255, 255, 255));
		mPaints.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub


		int BlockCount = 6;

		float SeperatedBlock = ((mWidth-20)-(BlockCount*2))/BlockCount;

		float SeperPercent = 100f/BlockCount;
		int NowBatteryBlockCount = (int) (mValuePercent / SeperPercent);


		////////////draw Background
		Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.battery);
		canvas.drawBitmap(bg,new Rect(11,0,bg.getWidth(),bg.getHeight()),new Rect(0,0,126,75 ), null);

		mPaints.setColor(Color.GRAY);
		mPaints.setShadowLayer(0,0, 0, Color.CYAN);
		for(int i=0; i<BlockCount; i++){

			if(i != 0) canvas.drawRect(new RectF(((SeperatedBlock*i))+2,22f,((SeperatedBlock*(i+1))),(float)mHeight-22), mPaints);
		}


		/////////////draw value
		if(NowBatteryBlockCount == BlockCount){
			NowBatteryBlockCount -=1;
			mPaints.setColor(Color.GREEN);
			mPaints.setShadowLayer(8,0, 0, Color.GREEN);
		}
		else if(NowBatteryBlockCount < 2){
			mPaints.setColor(Color.RED);
			mPaints.setShadowLayer(8,0, 0, Color.RED);
		}
		else {
			mPaints.setColor(Color.CYAN);
			mPaints.setShadowLayer(8,0, 0, Color.CYAN);
		}

		NowBatteryBlockCount +=1;
		for(int i=0; i<NowBatteryBlockCount; i++){
			if(i != 0) canvas.drawRect(new RectF(((SeperatedBlock*i))+2,22f,((SeperatedBlock*(i+1))),(float)mHeight-22), mPaints);
		}
		super.onDraw(canvas);
	}

	///////////////////////////////////////////////////////////////////////////////////////////아래는
	// 커스텀뷰를 위한 함수들
	@Override
	protected void onFinishInflate() {
		setClickable(true);
		// Log.w(Constants.TAG,"onFinishInflate()");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// height 진짜 크기 구하기
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
		mHeight = heightSize;

		// width 진짜 크기 구하기
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
		mWidth = widthSize;

		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

	}

	public double getValuePercent() {
		return mValuePercent;
	}

	public void setValuePercent(double mValuePercent) {
		this.mValuePercent = mValuePercent;
	}
}
