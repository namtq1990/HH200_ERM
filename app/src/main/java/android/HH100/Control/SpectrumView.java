package android.HH100.Control;

import java.util.*;

import android.HH100.*;
import android.HH100.Identification.Isotope;
import android.HH100.R.color;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.GestureDetector.*;

class Isotopes {
	public String isotopes;
	public int Channel1;
	public int Channel2;
	public int Energy1;
	public int Energy2;
	public int Energy3;
	public int[] mLineColor = { Color.RED, Color.YELLOW, Color.CYAN, Color.BLUE };
}

public class SpectrumView extends View {

	public Vector<Integer> mVally = new Vector<Integer>();
	private Paint mPaints;
	private Canvas mCanvas;
	private float m_Width = 0;
	private float m_Height = 0;
	private RectF m_DataRect = new RectF();
	private int m_Channel_size = MainActivity.CHANNEL_ARRAY_SIZE;

	private int NOW_VIEW_Channel_Start_Value = 0;
	private int NOW_VIEW_Channel_End_Value = 0;
	private int BG_SIDE_BLANK = 40;
	private int BG_LR_BLANK = 45;
	private int BG_TB_BLANK = 45;
	private static final int BG_AXIS_INDEX_SIZE = 5;
	private int BG_AXIS_X_INDEX_COUNT = 5;
	private int BG_AXIS_Y_INDEX_COUNT = 5;
	private static final int AXIS_Y_DATA_DEFALT_SIZE = 10;

	private double m_data_X_MAX_size = 0;
	private double m_data_Y_MAX_value = 10;
	private double m_data_X_size = 0;
	private double m_data_Y_size = 0;

	private int[] m_Ch_Array;

	private String mTitle = null;

	private String mInform1_title = null;
	private String mInform1_value = null;

	private String mInform2_title = null;
	private String mInform2_value = null;

	private String mInform3_title = null;
	private String mInform3_value = null;

	private String mInform4_title = null;
	private String mInform4_value = null;

	private String mInform1_SoucreId_title = null;
	private String mInform1_SoucreId_value = null;

	private String mInform2_SoucreId_title = null;
	private String mInform2_SoucreId_value = null;

	private String mInform3_SoucreId_title = null;
	private String mInform3_SoucreId_value = null;

	private String mInform4_SoucreId_title = null;
	private String mInform4_SoucreId_value = null;

	private int mDataColor = Color.rgb(255, 201, 14);
	private float mLastTouchX = 0;
	private float mLastTouchY = 0;

	private int mOrientation = 2;
	private int LANDSCAPE = 2;
	private int PORTRAIT = 1;

	private boolean mIsAbleInfo = true;

	private boolean mIsChannel = true;
	private int mMaxEnergy = 0;

	private boolean mSearching_Line = false;
	private float mSearcing_Line_axisX = 0;
	private double mCaliA = 0;
	private double mCaliB = 0;
	private double mCaliC = 0;

	Spectrum mSpc = new Spectrum();

	private boolean m_LogMode = false;

	float AddSouridRightMove = (float) 0.05;
	float AddSouridTopMove = (float) 0;

	private Vector<Double> mCursor_Avg_X = new Vector<Double>(5);
	// --------------------------- ��튂 ��以�

	private ScaleGestureDetector mGd = null;
	public float mScaleFactor = 1.f;
	static boolean SCREEN_ZOOMING = false;
	boolean SCREEN_MOVING = false;
	private GestureDetector mDoubleTapGesture;

	Vector<NcPeak> mPeakInfo = new Vector<NcPeak>();

	// �꾨Т寃껊룄 �덊븯���쒖뒪爾�由ъ뒪��
	private OnGestureListener mNullListener = new OnGestureListener() {

		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}

		public void onShowPress(MotionEvent e) {
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return false;
		}

		public void onLongPress(MotionEvent e) {
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return false;
		}

		public boolean onDown(MotionEvent e) {
			return false;
		}
	};

	private OnDoubleTapListener mDoubleTapListener = new OnDoubleTapListener() {

		public boolean onSingleTapConfirmed(MotionEvent e) {

			return false;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {

			return false;
		}

		public boolean onDoubleTap(MotionEvent e) {

			float a = e.getX();

			if (!(m_DataRect.left < e.getX() & m_DataRect.right > e.getX() & m_DataRect.top < e.getY()
					& m_DataRect.bottom - 50 > e.getY()))
				return false;

			if (m_Channel_size == MainActivity.CHANNEL_ARRAY_SIZE) {
				float TouchedScreenPercent = ((e.getX() - m_DataRect.left) / m_DataRect.width()) * 100;
				int TouchedChannel = (int) ((TouchedScreenPercent / 100) * m_Channel_size);

				if (TouchedChannel > 150 | TouchedChannel < (MainActivity.CHANNEL_ARRAY_SIZE - 150)) {
					NOW_VIEW_Channel_Start_Value = TouchedChannel - 150;
					NOW_VIEW_Channel_End_Value = TouchedChannel + 150;
					m_Channel_size = Math.abs(NOW_VIEW_Channel_Start_Value - NOW_VIEW_Channel_End_Value);
				}
				if (TouchedChannel < 150) {
					NOW_VIEW_Channel_Start_Value = 0;
					NOW_VIEW_Channel_End_Value = 300;
					m_Channel_size = Math.abs(NOW_VIEW_Channel_Start_Value - NOW_VIEW_Channel_End_Value);
				}
				if (TouchedChannel > (MainActivity.CHANNEL_ARRAY_SIZE - 150)) {
					NOW_VIEW_Channel_Start_Value = MainActivity.CHANNEL_ARRAY_SIZE - 150;
					NOW_VIEW_Channel_End_Value = MainActivity.CHANNEL_ARRAY_SIZE;
					m_Channel_size = Math.abs(NOW_VIEW_Channel_Start_Value - NOW_VIEW_Channel_End_Value);
				}
			} else {
				NOW_VIEW_Channel_Start_Value = 0;
				NOW_VIEW_Channel_End_Value = MainActivity.CHANNEL_ARRAY_SIZE;
				m_Channel_size = NOW_VIEW_Channel_End_Value;
			}
			invalidate();
			return true;
		}
	};

	// --------------------------

	private Vector<Isotope> mFoundIsotopes = new Vector<Isotope>();
	private Vector<Isotope> mUnknownPeak = new Vector<Isotope>();

	public SpectrumView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public SpectrumView(Context context, AttributeSet attributeSet) {

		super(context, attributeSet);
		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		// mPaints.setStyle(Paint.Style.STROKE);
		// mPaints.setStrokeWidth(3);
		mPaints.setColor(Color.rgb(255, 255, 255));
		mPaints.setAntiAlias(true);

		mGd = new ScaleGestureDetector(context, new ScaleListener());
		mDoubleTapGesture = new GestureDetector(context, mNullListener); // �붾툝���쒖뒪爾��앹꽦
		mDoubleTapGesture.setOnDoubleTapListener(mDoubleTapListener); // �붾툝
																		// ��由ъ뒪���깅줉

		// mPaints.setStrokeCap(Paint.Cap.BUTT);

	}

	public SpectrumView(Context context) {

		super(context);

		mPaints = new Paint();
		mPaints = new Paint(mPaints);
		mPaints.setStrokeCap(Paint.Cap.BUTT);
		// mPaints.setStyle(Paint.Style.STROKE);
		// mPaints.setStrokeWidth(3);
		mPaints.setColor(Color.rgb(255, 255, 255));
		mPaints.setAntiAlias(true);

	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu) {

		super.onCreateContextMenu(menu);
	}

	private String Prefix(int Value) {
		if (Value > 1000 && Value <= 1000000)
			return (int) (Value * 0.001) + "k";
		if (Value > 1000000)
			return (int) (Value * 0.00001) + "m";

		return String.valueOf(Value);
	}

	@SuppressWarnings("null")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mGd != null)
			mGd.onTouchEvent(event);
		if (mDoubleTapGesture != null)
			mDoubleTapGesture.onTouchEvent(event); // �쒖뒪泥섎뒗 �붾툝��쭔 �몄떇, �ъ슜.
		final int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = event.getX();
			final float y = event.getY();

			mLastTouchX = x;
			mLastTouchY = y;

			// ---------- �섎떒 �쒖튂諛�
			if (y > m_DataRect.bottom - 30 & x > m_DataRect.left & x < m_DataRect.right) {
				mSearching_Line = true;
				mCursor_Avg_X.add((double) x);
				mSearcing_Line_axisX = x;
			}

			// -------------

			if (m_DataRect.left < x & m_DataRect.right > x & m_DataRect.top < y & m_DataRect.bottom - 50 > y) {
				SCREEN_MOVING = true;
			}

			// -----------------------

			// ------------------------

			break;

		}

		case MotionEvent.ACTION_MOVE: {
			// ---------- �섎떒 �쒖튂諛�
			final float x = event.getX();
			final float y = event.getY();

			int zoom_ratio = (int) Math.abs(mLastTouchX - x);
			float temp = (float) (m_Channel_size * 0.001);

			if (zoom_ratio > 200)
				zoom_ratio = 10;
			zoom_ratio = (int) (zoom_ratio * temp);

			if (mSearching_Line == true & y > m_DataRect.bottom & x > m_DataRect.left & x < m_DataRect.right) {
				if (mCursor_Avg_X.size() < 5) {
					mCursor_Avg_X.add((double) x);
					mSearcing_Line_axisX = x;
				} else {
					mCursor_Avg_X.remove(0);
					mCursor_Avg_X.add((double) x);

					double Avg = 0;
					for (int i = 0; i < mCursor_Avg_X.size(); i++) {
						Avg += mCursor_Avg_X.get(i);
					}
					Avg = Avg / mCursor_Avg_X.size();

					mSearcing_Line_axisX = (float) Avg;
				}
			}
			// --------
			if (SCREEN_MOVING == true & SCREEN_ZOOMING != true & y < m_DataRect.bottom - 50) {
				if (mLastTouchX < x) {
					if (NOW_VIEW_Channel_Start_Value - zoom_ratio > 0) {
						NOW_VIEW_Channel_Start_Value -= zoom_ratio;
						NOW_VIEW_Channel_End_Value -= zoom_ratio;
					}

				} else {
					if (NOW_VIEW_Channel_End_Value + zoom_ratio < 1024) {
						NOW_VIEW_Channel_End_Value += zoom_ratio;
						NOW_VIEW_Channel_Start_Value += zoom_ratio;
					}
				}
			}
			mLastTouchX = x;
			invalidate();
			break;
		}
		case MotionEvent.ACTION_UP: {
			if (mSearching_Line == true) {
				mCursor_Avg_X.clear();
			} // mSearching_Line=false;
			if (SCREEN_MOVING == true)
				SCREEN_MOVING = false;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: // �먮쾲吏��먭��쎌쓣 �쇱뿀��寃쎌슦

			break;

		case MotionEvent.ACTION_POINTER_DOWN:

			break;
		case MotionEvent.ACTION_POINTER_3_UP:
			break;
		default:
			break;
		}

		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		mCanvas = canvas;
		mPaints.setTextSize(NcLibrary.Get_fontSize(m_Width) + 3);
		// canvas.save();
		// canvas.scale(mScaleFactor, mScaleFactor);
		m_data_Y_MAX_value = MAX_in_ChArray(m_Ch_Array);
		BG_LR_BLANK = (int) (mPaints.measureText("0000") + 15);
		BG_TB_BLANK = (int) (m_Height * 0.08);
		m_DataRect.set(BG_LR_BLANK + 10, BG_TB_BLANK, m_Width - BG_LR_BLANK, m_Height - BG_TB_BLANK);
		m_data_X_size = m_DataRect.width() / m_Channel_size;
		m_data_Y_size = m_DataRect.height() / m_data_Y_MAX_value;

		//// Grid count
		BG_AXIS_X_INDEX_COUNT = (int) (12 - (m_Channel_size * 0.01));
		if (BG_AXIS_X_INDEX_COUNT <= 3)
			BG_AXIS_X_INDEX_COUNT = 4;

		if (m_LogMode)
			BG_AXIS_Y_INDEX_COUNT = Get_LogScale_GridCount_AxisY();
		else
			BG_AXIS_Y_INDEX_COUNT = BG_AXIS_X_INDEX_COUNT;

		/////

		if (mTitle != null) {
			Paint title_paint = new Paint();
			// data_Paints.sets
			title_paint.setAntiAlias(true);
			title_paint.setStyle(Paint.Style.FILL_AND_STROKE);
			title_paint.setStrokeWidth(1);
			title_paint.setTextSize(23);
			title_paint.setColor(Color.rgb(255, 101, 14));
			/*
			 * canvas.drawText(mTitle, //珥덇린 媛�0 (float)(m_DataRect.right*0.3),
			 * (m_DataRect.top - 15), title_paint);
			 */
		}

		draw_BG(canvas);

		if (mIsAbleInfo) {
			if (mIsChannel == true)
				draw_X_Y_Caption(canvas, "Counts", "Channel");
			else
				draw_X_Y_Caption(canvas, "Counts", "KeV");

			if (mInform1_title != null | mInform1_value != null)
				draw_inform_1(canvas, mInform1_title, mInform1_value);
			if (mInform2_title != null | mInform2_value != null)
				draw_inform_2(canvas, mInform2_title, mInform2_value);
			if (mInform3_title != null | mInform3_value != null)
				draw_inform_3(canvas, mInform3_title, mInform3_value);
			if (mInform4_title != null | mInform4_value != null)
				draw_inform_4(canvas, mInform4_title, mInform4_value);

			if (mInform1_SoucreId_title != null | mInform1_SoucreId_value != null)
				draw_inform_sourceid_1(canvas, mInform1_SoucreId_title, mInform1_SoucreId_value);
			if (mInform2_SoucreId_title != null | mInform2_SoucreId_value != null)
				draw_inform_sourceid_2(canvas, mInform2_SoucreId_title, mInform2_SoucreId_value);
			if (mInform3_SoucreId_title != null | mInform3_SoucreId_value != null)
				draw_inform_sourceid_3(canvas, mInform3_SoucreId_title, mInform3_SoucreId_value);
			if (mInform4_SoucreId_title != null | mInform4_SoucreId_value != null)
				draw_inform_sourceid_4(canvas, mInform4_SoucreId_title, mInform4_SoucreId_value);

		}
		//// .....//// �곗씠�곕� 洹몃┛��
		Paint data_Paints = new Paint();
		// data_Paints.setStrokeWidth((float)m_data_X_size);
		data_Paints.setColor(mDataColor);

		Path SpcPath = new Path();

		Draw_ROI_WhitColor(canvas);

		SpcPath.reset();
		data_Paints.setColor(mDataColor);
		SpcPath.moveTo(m_DataRect.left, m_DataRect.bottom);
		for (int i = 0; i < m_Channel_size; i++)// draw data
		{

			// if(m_Ch_Array[i+NOW_VIEW_Channel_Start_Value] == 0) continue;
			if (m_LogMode) {
				double log_yMax = Math.log10(m_data_Y_MAX_value);
				double log_yValue = Math.log10(m_Ch_Array[i + NOW_VIEW_Channel_Start_Value]);
				if (m_Ch_Array[i + NOW_VIEW_Channel_Start_Value] == 0)
					log_yValue = 0;
				if (m_Ch_Array[i + NOW_VIEW_Channel_Start_Value] == 1)
					log_yValue = Math.log10(2) - 0.1;
				log_yValue = (log_yValue / log_yMax) * 100;

				//////
				double DataY = (log_yValue / 100) * m_DataRect.height();

				SpcPath.lineTo((float) ((m_DataRect.left + 1.5) + (m_data_X_size * (i + 1)) - (m_data_X_size / 2)),
						(float) (m_DataRect.bottom - DataY));

				/*
				 * canvas.drawLine((float)((m_DataRect.left+1.5)+(m_data_X_size*
				 * (i+1))-(m_data_X_size/2)), //1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡
				 * (float)(m_DataRect.bottom-DataY), (float)(m_DataRect.left+1.5+
				 * (m_data_X_size*(i+1))-(m_data_X_size/2)), //1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡
				 * (m_DataRect.bottom), data_Paints);
				 */

			} else {
				try {
					canvas.drawLine((float) ((m_DataRect.left + 1.5) + (m_data_X_size * (i + 1)) - (m_data_X_size / 2)), // 1.5��Y異�諛깃렇�쇱슫���좎쓽

							(float) (m_DataRect.bottom
									- (m_data_Y_size * (m_Ch_Array[i + NOW_VIEW_Channel_Start_Value]))),
							(float) (m_DataRect.left + 1.5 + (m_data_X_size * (i + 1)) - (m_data_X_size / 2)), // 1.5��Y異�諛깃렇�쇱슫���좎쓽

							(m_DataRect.bottom), data_Paints);
				} catch (Exception e) {
					// TODO: handle exception
				}

			}

		}
		////// .......////
		SpcPath.lineTo(m_DataRect.right, m_DataRect.bottom);

		data_Paints.setStyle(Paint.Style.STROKE);

		// --->
		/*
		 * Paint Gra_p = new Paint(); Gra_p.setStyle(Paint.Style.FILL);
		 * Gra_p.setAntiAlias(true); Gra_p.setShader(new LinearGradient(0,
		 * (float)m_Height*0.6f, 0, m_Height, Color.argb(5,255,201,14),
		 * Color.argb(20,255,201,14), TileMode.CLAMP));
		 * 
		 * canvas.drawPath(SpcPath, Gra_p);
		 */
		canvas.drawPath(SpcPath, data_Paints);
		// ---<

		if (mVally.isEmpty() == false) {
			data_Paints.setColor(Color.MAGENTA);
			for (int i = 0; i < mVally.size(); i++) {

				int chnn = mVally.get(i) - NOW_VIEW_Channel_Start_Value;
				if (chnn > 0 & chnn < NOW_VIEW_Channel_End_Value) {
					float Ch_P = ((float) chnn / (float) (NOW_VIEW_Channel_End_Value - NOW_VIEW_Channel_Start_Value));
					Ch_P = m_DataRect.width() * Ch_P;
					Ch_P += m_DataRect.left;

					canvas.drawLine(Ch_P, m_DataRect.top, Ch_P, m_DataRect.bottom, data_Paints);
				}
			}
		}
		if (mSearching_Line == true) {
			draw_Cursor_Line(canvas);

			/*
			 * if(mCaliA != 0){ double temp =
			 * ((mSearcing_Line_axisX-m_DataRect.left)/m_DataRect.width())*100; temp =
			 * (temp/100)*m_Channel_size; temp += NOW_VIEW_Channel_Start_Value;
			 * 
			 * double Roi_window =
			 * NcLibrary.Get_Roi_window_by_energy(NcLibrary.Channel_to_Energy(( int)temp,
			 * mCaliA, mCaliB,mCaliC)); double L_ROI_Percent = 1.0-(Roi_window*0.01); double
			 * R_ROI_Percent = 1.0+(Roi_window*0.01);
			 * 
			 * double L_Channel =
			 * NcLibrary.Energy_to_Channel((NcLibrary.Channel_to_Energy((int) temp, mCaliA,
			 * mCaliB,mCaliC)*L_ROI_Percent),mCaliA,mCaliB,mCaliC); double R_Channel =
			 * NcLibrary.Energy_to_Channel((NcLibrary.Channel_to_Energy((int) temp, mCaliA,
			 * mCaliB,mCaliC)*R_ROI_Percent),mCaliA,mCaliB,mCaliC);
			 * 
			 * data_Paints.setColor(Color.GREEN); for(int i = 0; i < m_Channel_size-1;
			 * i++)//draw data { if(m_Ch_Array[i+NOW_VIEW_Channel_Start_Value] == 0)
			 * continue; //if(i > NcLibrary.Auto_floor(R_Channel)) break;
			 * if(i+NOW_VIEW_Channel_Start_Value >= NcLibrary.Auto_floor(L_Channel) &
			 * NcLibrary.Auto_floor(R_Channel) >= i+NOW_VIEW_Channel_Start_Value){
			 * if(m_LogMode){ double log_yMax = Math.log10(m_data_Y_MAX_value); double
			 * log_yValue = Math.log10(m_Ch_Array[i+NOW_VIEW_Channel_Start_Value]);
			 * if(m_Ch_Array[i+NOW_VIEW_Channel_Start_Value] == 0) log_yValue = 0;
			 * if(m_Ch_Array[i+NOW_VIEW_Channel_Start_Value] == 1) log_yValue =
			 * Math.log10(2)-0.1; log_yValue = (log_yValue/log_yMax)*100;
			 * 
			 * ////// double DataY =(log_yValue/100)*m_DataRect.height();
			 * 
			 * canvas.drawLine((float)((m_DataRect.left+1.5)+(m_data_X_size*(i+1
			 * ))-(m_data_X_size/2)), //1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡
			 * (float)(m_DataRect.bottom-DataY),
			 * (float)(m_DataRect.left+1.5+(m_data_X_size*(i+1))-(m_data_X_size/ 2)),
			 * //1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡 (m_DataRect.bottom), data_Paints);
			 * 
			 * } else { canvas.drawLine((float)((m_DataRect.left+1.5)+(m_data_X_size*(i+1
			 * ))-(m_data_X_size/2)), //1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡
			 * (float)(m_DataRect.bottom-(m_data_Y_size*(m_Ch_Array[i+
			 * NOW_VIEW_Channel_Start_Value]))),
			 * (float)(m_DataRect.left+1.5+(m_data_X_size*(i+1))-(m_data_X_size/ 2)),
			 * //1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡 (m_DataRect.bottom), data_Paints); } }//end for }
			 * }//end if
			 */
		}
		draw_ID_Result(canvas, m_DataRect);
		// canvas.restore();

	}

	public void Draw_ROI_WhitColor(Canvas canvas) {
		Paint data_Paints = new Paint();
		data_Paints.setColor(mDataColor);

		Path SpcPath = new Path();

		if (!mFoundIsotopes.isEmpty() | !mUnknownPeak.isEmpty()) {

			Vector<Isotope> targetIsotopes = new Vector<Isotope>();
			targetIsotopes.addAll(mFoundIsotopes);
			targetIsotopes.addAll(mUnknownPeak);

			for (int k = 0; k < targetIsotopes.size(); k++) 
			{
				data_Paints.setColor(targetIsotopes.get(k).ClassColor);
				data_Paints.setAlpha(130);

				SpcPath.reset();

				Vector<Double> Energys = new Vector<Double>();
				Vector<NcPeak> Vally = new Vector<NcPeak>();
				
				//for (int i = 0; i < targetIsotopes.get(k).FoundPeaks.size(); i++) 
				//{
					//Energys.add(targetIsotopes.get(k).FoundPeaks.get(i).Peak_Energy);
					//Vally.add(targetIsotopes.get(k).FoundPeaks.get(i));
				//}
				
				
				//Adding Minor Peak
				for (int i = 0; i < targetIsotopes.get(k).ListPeakDrawEn.size(); i++) 
				{
					Energys.add(targetIsotopes.get(k).ListPeakDrawEn.get(i).Peak_Energy);
					Vally.add(targetIsotopes.get(k).ListPeakDrawEn.get(i));
				}

				//20.03.05
				if(targetIsotopes.get(k).ListPeakDrawEn!=null){
					targetIsotopes.get(k).ListPeakDrawEn.clear();
				}
				/*
				 * if(targetIsotopes.get(k).Energy1 !=
				 * 0)Energys.add(targetIsotopes.get(k).Energy1);
				 * if(targetIsotopes.get(k).Energy2 !=
				 * 0)Energys.add(targetIsotopes.get(k).Energy2);
				 * if(targetIsotopes.get(k).Energy3 !=
				 * 0)Energys.add(targetIsotopes.get(k).Energy3);
				 * if(targetIsotopes.get(k).Energy4 !=
				 * 0)Energys.add(targetIsotopes.get(k).Energy4);
				 * if(targetIsotopes.get(k).Energy5 !=
				 * 0)Energys.add(targetIsotopes.get(k).Energy5);
				 * 
				 * if(targetIsotopes.get(k).Channel1_Vally !=
				 * null)Vally.add(targetIsotopes.get(k).Channel1_Vally);
				 * if(targetIsotopes.get(k).Channel2_Vally !=
				 * null)Vally.add(targetIsotopes.get(k).Channel2_Vally);
				 * if(targetIsotopes.get(k).Channel3_Vally !=
				 * null)Vally.add(targetIsotopes.get(k).Channel3_Vally);
				 * if(targetIsotopes.get(k).Channel4_Vally !=
				 * null)Vally.add(targetIsotopes.get(k).Channel4_Vally);
				 * if(targetIsotopes.get(k).Channel5_Vally !=
				 * null)Vally.add(targetIsotopes.get(k).Channel5_Vally);
				 */
				for (int q = 0; q < Energys.size(); q++) {
					int L_Channel = NcLibrary.Auto_floor(Vally.get(q).ROI_Left);
					int R_Channel = NcLibrary.Auto_floor(Vally.get(q).ROI_Right);

					if (L_Channel < 0)
						L_Channel = 0;
					if (R_Channel > 1024)
						R_Channel = 1024;

					boolean FirstCheck = false;
					for (int i = 0; i < m_Channel_size; i++)// draw data
					{
						if (i + NOW_VIEW_Channel_Start_Value >= L_Channel) {// &
																			// R_Channel
																			// >=
																			// i+NOW_VIEW_Channel_Start_Value){
							if (m_LogMode) {
								double log_yMax = Math.log10(m_data_Y_MAX_value);
								double log_yValue = Math.log10(m_Ch_Array[i + NOW_VIEW_Channel_Start_Value]);
								if (m_Ch_Array[i + NOW_VIEW_Channel_Start_Value] == 0)
									log_yValue = 0;
								if (m_Ch_Array[i + NOW_VIEW_Channel_Start_Value] == 1)
									log_yValue = Math.log10(2) - 0.1;
								log_yValue = (log_yValue / log_yMax) * 100;

								//// first point
								if (FirstCheck == false) {
									SpcPath.moveTo((float) ((m_DataRect.left + 1.5) + (m_data_X_size * (i + 1))
											- (m_data_X_size / 2)), (float) m_DataRect.bottom);
									FirstCheck = true;
								}

								//// last point
								if (i + NOW_VIEW_Channel_Start_Value >= R_Channel
										| NOW_VIEW_Channel_End_Value <= i + NOW_VIEW_Channel_Start_Value
										| i == m_Channel_size - 1) {
									SpcPath.lineTo((float) ((m_DataRect.left + 1.5) + (m_data_X_size * (i))
											- (m_data_X_size / 2)), (float) m_DataRect.bottom);
									break;
								} else {//// middle data
									double DataY = (log_yValue / 100) * m_DataRect.height();
									SpcPath.lineTo((float) ((m_DataRect.left + 1.5) + (m_data_X_size * (i + 1))
											- (m_data_X_size / 2)), (float) (m_DataRect.bottom - DataY - 2));
								}
							}

							else {
								canvas.drawLine(
										(float) ((m_DataRect.left + 1.5) + (m_data_X_size * (i + 1))
												- (m_data_X_size / 2)), // 1.5��Y異�諛깃렇�쇱슫���좎쓽
																		// �먭퍡
										(float) (m_DataRect.bottom
												- (m_data_Y_size * (m_Ch_Array[i + NOW_VIEW_Channel_Start_Value]))),
										(float) (m_DataRect.left + 1.5 + (m_data_X_size * (i + 1))
												- (m_data_X_size / 2)), // 1.5��Y異�諛깃렇�쇱슫���좎쓽
																		// �먭퍡
										(m_DataRect.bottom), data_Paints);
							}
						} // end for
					} // end for
				} // end for

				data_Paints.setStyle(Style.FILL);
				canvas.drawPath(SpcPath, data_Paints);
			}
		}
	}

	public void LogMode(boolean IsTrue) {

		m_LogMode = IsTrue;
	}

	private int Get_PowRange(double TargetValue) {

		if (TargetValue == 0)
			return 0;

		int Result = 0;
		for (int i = 0; i < 100; i++) {
			int aa = pow(10, i);
			int aa2 = pow(10, i + 1);
			if (pow(10, i) <= TargetValue && pow(10, i + 1) > TargetValue) {
				Result = i;
				break;
			}
		}
		// m_AxisY_Grid_Count = log10(m_Display_End_AxisY); //최대카운트를 10을 밑으로 하는
		// 로그화

		return Result;
	}

	private int Get_LogScale_GridCount_AxisY() {

		double sdq = pow(10, 1);
		double sdq2 = pow(10, 2);
		int GridCount = 0;
		for (int i = 0; i < 100; i++) {
			if (pow(10, i) <= m_data_Y_MAX_value && pow(10, i + 1) > m_data_Y_MAX_value) {
				GridCount = i;
				break;
			}
		}
		// m_AxisY_Grid_Count = log10(m_Display_End_AxisY); //최대카운트를 10을 밑으로 하는
		// 로그화
		m_data_Y_MAX_value = pow(10, GridCount);

		return GridCount;
	}

	public void Add_Found_Isotope(Isotope Isotope) {

		// temp.Channel1 = Channel;
		if (Isotope.Class.matches(".*UNK.*"))
			mUnknownPeak.add(Isotope);
		else
			mFoundIsotopes.add(Isotope);
		Sort_IsotopeList();
	}

	public void Set_Found_Isotope(Vector<Isotope> Isotopes) {
		mFoundIsotopes.clear();
		mFoundIsotopes = Isotopes;
	}

	private void Sort_IsotopeList() {
		if (mFoundIsotopes.isEmpty())
			return;

		Vector<Isotope> Temp = mFoundIsotopes;
		Vector<Isotope> Result = new Vector<Isotope>();
		int MaxArray = 0;

		while (Temp.size() != 0) {
			MaxArray = MaxDoserate_Isotope(Temp);
			Result.add(Temp.get(MaxArray));
			Temp.remove(MaxArray);
		}
		mFoundIsotopes.removeAllElements();
		mFoundIsotopes = Result;
	}

	private int MaxDoserate_Isotope(Vector<Isotope> isotope) {
		double temp = 0;
		int Number = 0;
		Isotope Result;
		for (int i = 0; i < isotope.size(); i++) {
			if (temp < isotope.get(i).DoseRate) {
				temp = isotope.get(i).DoseRate;
				Number = i;
			}
		}
		Result = isotope.get(Number);
		return Number;
	}

	public void Clear_Found_Isotopes() {
		mFoundIsotopes.clear();
		mUnknownPeak.clear();
	}

	private void draw_ID_Result(Canvas canvas, RectF dataRect) {

		// TODO Auto-generated method stub
		Paint paint = new Paint();
		// data_Paints.sets
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(1);
		paint.setTextSize(RealTimeActivity.TEXT_SIZE + 5);
		paint.setColor(Color.rgb(230, 220, 0));
		paint.setTextAlign(Align.RIGHT);
		// paint.setColor(Color.LTGRAY);

		int text_height = (measureTextHeight("0", paint) + 20);
		String Screening_Process ="";

		for (int i = 0; i < mFoundIsotopes.size(); i++) 
		{
		
			   //0109 추가 Screening_Process ==0 이면 red로 표시
				if(mFoundIsotopes.get(i).Screening_Process == 1)
				{
					paint.setColor(Color.rgb(230, 220, 0));
				}
					
				else
				{
					paint.setColor(Color.rgb(230, 220, 0));
					//paint.setColor(Color.rgb(255, 0, 0));
				}
					

			canvas.drawText(mFoundIsotopes.get(i).isotopes + "  " + mFoundIsotopes.get(i).Get_ConfidenceLevel() + "%", (dataRect.right - 10), (float) (dataRect.bottom * 0.2 + (i * text_height)), paint);
		}
	}

	/*
	 * public void Set_inform_box_item(){
	 * 
	 * }
	 */
	private void draw_Cursor_Line(Canvas canvas) {

		Paint data_Paints = new Paint();
		data_Paints.setStrokeWidth(1);
		data_Paints.setAntiAlias(true);
		// data_Paints.setShadowLayer(3,0, 0, Color.WHITE);

		data_Paints.setColor(Color.rgb(51, 181, 229));
		canvas.drawLine((mSearcing_Line_axisX), // 1.5��Y異�諛깃렇�쇱슫���좎쓽 �먭퍡
				(m_DataRect.top), (mSearcing_Line_axisX), // 1.5��Y異�諛깃렇�쇱슫���좎쓽
															// �먭퍡
				(m_DataRect.bottom), data_Paints);

		//////////////////////////
		double temp = ((mSearcing_Line_axisX - m_DataRect.left) / m_DataRect.width()) * 100;
		temp = (temp / 100) * m_Channel_size;
		temp += NOW_VIEW_Channel_Start_Value;

		double Top = (m_Ch_Array[(int) temp] / m_Height) * 100;
		Top = (Top / 100) * m_Height;

		//////////////////////////////
		int Channel = (int) temp;
		if (mIsChannel == false)
			temp = NcLibrary.Channel_to_Energy((int) temp, mCaliA, mCaliB, mCaliC);

		data_Paints.setAntiAlias(true);
		// data_Paints.setShadowLayer(1,1, 1, Color(255,201,14));
		data_Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		data_Paints.setStrokeWidth(1);
		data_Paints.setTextSize(RealTimeActivity.TEXT_SIZE + 2);
		data_Paints.setColor(Color.rgb(51, 181, 229));

		boolean L_check = false;
		float text_top = (float) (m_DataRect.bottom * 0.2 + ((float) mFoundIsotopes.size() * 33.0));

		if (mFoundIsotopes.size() == 0) {
			if (mSearcing_Line_axisX > m_DataRect.right * 0.72)
				L_check = true;
			text_top = (float) (m_DataRect.bottom * 0.2) - 20;
		} else if (mSearcing_Line_axisX < m_DataRect.right * 0.72)
			text_top = (float) (m_DataRect.bottom * 0.2) - 20;
		else
			L_check = true;

		int text_height = measureTextHeight("0", data_Paints);

		String Value = (mIsChannel == true)
				? String.valueOf((int) temp) + " Channel (" + String.format("%,d", m_Ch_Array[Channel]) + " cnt" + ")"
				: String.valueOf((int) temp) + " keV (" + String.format("%,d", m_Ch_Array[Channel]) + " cnt" + ")";

		if (L_check)
			canvas.drawText(Value, (mSearcing_Line_axisX - 10) - data_Paints.measureText(Value), text_top + 20,
					data_Paints);
		else
			canvas.drawText(Value, (mSearcing_Line_axisX + 10), text_top + 20, data_Paints);

		data_Paints.setTextSize(RealTimeActivity.TEXT_SIZE);
		// Value = String.valueOf(m_Ch_Array[Channel]) + " count";
//		String Time = "";
//		if (mInform1_value != null) {
//			Time = mInform1_value;
//			Time.replace("sec", "");
//		}

		if (mPeakInfo.size() != 0) {
			double NetCnt = 0;
			for (int i = 0; i < mPeakInfo.size(); i++) {
				NcPeak TempPeak = mPeakInfo.get(i);
				boolean isIn = TempPeak.Energy_InWindow(temp);
				if (isIn) {
					NetCnt = mPeakInfo.get(i).NetCnt / mSpc.Get_AcqTime();
					break;
				}
			}
			Value = "(Net : " + String.format("%,d", (int) NetCnt) + " Bq" + ")";

			if (NetCnt != 0) {
				if (L_check)
					canvas.drawText(Value, (mSearcing_Line_axisX - 10) - data_Paints.measureText(Value),
							text_top + text_height + 30, data_Paints);
				else
					canvas.drawText(Value, (mSearcing_Line_axisX + 20), text_top + text_height + 30, data_Paints);
			}

		}
		// int time = Integer.parseInt(Time);
		// String a = mInform1_value;

		// if (L_check)
		// canvas.drawText(Value, (mSearcing_Line_axisX - 10) -
		// data_Paints.measureText(Value),
		// text_top + text_height + 60, data_Paints);
		// else
		// canvas.drawText(Value, (mSearcing_Line_axisX + 20), text_top + text_height +
		// 60, data_Paints);

	}

	private int Color(int i, int j, int k) {
		// TODO Auto-generated method stub
		return 0;
	}

	private void draw_BG(Canvas canvas) {

		mPaints.setTextSize(RealTimeActivity.TEXT_SIZE - 5);

		canvas.drawLine((float) m_DataRect.left, (m_Height - BG_TB_BLANK), m_DataRect.right, (m_Height - BG_TB_BLANK),
				mPaints); // x
		canvas.drawLine(m_DataRect.left, m_DataRect.top, m_DataRect.left, m_DataRect.bottom, mPaints);// y

		Paint GraphLine_Paints = new Paint();
		GraphLine_Paints.setStyle(Paint.Style.FILL);
		GraphLine_Paints.setStrokeWidth(1f);
		GraphLine_Paints.setColor(Color.argb(60, 100, 175, 71));

		Paint GraphLine_Paints2 = new Paint();
		GraphLine_Paints2.setStyle(Paint.Style.FILL);
		GraphLine_Paints2.setStrokeWidth(1f);
		GraphLine_Paints2.setColor(Color.argb(30, 96, 165, 67));

		double X_index_size = m_DataRect.width() / BG_AXIS_X_INDEX_COUNT;
		for (int i = 0; i < BG_AXIS_X_INDEX_COUNT; i++) // x
		{
			canvas.drawLine((float) (m_DataRect.left + (X_index_size * (i + 1))), // �몃뜳���먯꽑��洹몃┝
					(m_DataRect.bottom), (float) (m_DataRect.left + (X_index_size * (i + 1))),
					(m_DataRect.bottom + BG_AXIS_INDEX_SIZE), mPaints);

			canvas.drawLine((float) (m_DataRect.left + (X_index_size * (i + 1))), // �곗씠�곗쁺��뿉
																					// 紐⑤늿
																					// 醫낆씠瑜�洹몃┝
					(m_DataRect.top), (float) (m_DataRect.left + (X_index_size * (i + 1))), (m_DataRect.bottom),
					GraphLine_Paints);

			canvas.drawLine((float) (m_DataRect.left + (X_index_size * (i + 1))) - (float) (X_index_size / 2), // �곗씠�곗쁺��뿉
																												// 紐⑤늿
																												// 醫낆씠瑜�洹몃┝
					(m_DataRect.top), (float) (m_DataRect.left + (X_index_size * (i + 1))) - (float) (X_index_size / 2),
					(m_DataRect.bottom), GraphLine_Paints2);

			double PrintingValue = ((double) (NOW_VIEW_Channel_End_Value - NOW_VIEW_Channel_Start_Value)
					/ BG_AXIS_X_INDEX_COUNT) * (i + 1);// �몃뜳���띿뒪��媛믪쓣 洹몃┝

			int text_height = measureTextHeight("0", mPaints);
			mPaints.setColor(Color.rgb(192, 192, 192));

			mPaints.setTextAlign(Align.RIGHT);
			if (mIsChannel == true) {
				if (i == 0) {
					canvas.drawText(String.valueOf(NOW_VIEW_Channel_Start_Value), // 珥덇린
																					// 媛�0
							(m_DataRect.left + 20), (m_DataRect.bottom) + 5 + text_height, mPaints);
				}
				canvas.drawText(String.valueOf((int) PrintingValue + NOW_VIEW_Channel_Start_Value),
						(float) (m_DataRect.left + (X_index_size * (i + 1))), (m_DataRect.bottom) + 5 + text_height,
						mPaints);
			} else {
				if (i == 0) {
					int value = (int) NcLibrary.Channel_to_Energy(NOW_VIEW_Channel_Start_Value, mCaliA, mCaliB, mCaliC);
					if (value < 0)
						value = 0;
					canvas.drawText(String.valueOf(value), // 珥덇린 媛�0
							(m_DataRect.left + 10), (m_DataRect.bottom) + 5 + text_height, mPaints);
				}
				int value = (int) NcLibrary.Channel_to_Energy(PrintingValue + NOW_VIEW_Channel_Start_Value, mCaliA,
						mCaliB, mCaliC);
				if (value < 0)
					value = 0;
				canvas.drawText(String.valueOf(value), (float) (m_DataRect.left + (X_index_size * (i + 1))),
						(m_DataRect.bottom) + 5 + text_height, mPaints);
			}
			mPaints.setTextAlign(Align.LEFT);
		} //// end for
		mPaints.setTextAlign(Align.RIGHT);
		double Y_index_size = m_DataRect.height() / BG_AXIS_Y_INDEX_COUNT;
		for (int i = 0; i < BG_AXIS_Y_INDEX_COUNT; i++) // y
		{
			canvas.drawLine((m_DataRect.left - BG_AXIS_INDEX_SIZE),
					(float) (m_DataRect.bottom - (Y_index_size * (i + 1))), (m_DataRect.left),
					(float) (m_DataRect.bottom - (Y_index_size * (i + 1))), mPaints);

			canvas.drawLine((m_DataRect.left), (float) (m_DataRect.bottom - (Y_index_size * (i + 1))),
					(m_DataRect.right), (float) (m_DataRect.bottom - (Y_index_size * (i + 1))), GraphLine_Paints);

			canvas.drawLine((m_DataRect.left),
					(float) (m_DataRect.bottom - (Y_index_size * (i + 1))) + (float) (Y_index_size / 2),
					(m_DataRect.right),
					(float) (m_DataRect.bottom - (Y_index_size * (i + 1))) + (float) (Y_index_size / 2),
					GraphLine_Paints2);

			double PrintingValue = 0;

			if (m_LogMode)
				PrintingValue = pow(10, i + 1);// �몃뜳��媛믪쓣 洹몃┝
			else
				PrintingValue = ((m_data_Y_MAX_value) / BG_AXIS_Y_INDEX_COUNT) * (i + 1);// �몃뜳��媛믪쓣

			subscriptDrawText((int) PrintingValue, canvas, mPaints, (m_DataRect.left) - 17,
					(float) (m_DataRect.bottom - (Y_index_size * (i + 1))) + 10, "10", "2",
					RealTimeActivity.TEXT_SIZE - 5);

			// 洹몃┝

			/*
			 * canvas.drawText(String.valueOf(0),//珥덇린 媛�0 (m_DataRect.left-10),
			 * (m_DataRect.bottom+10),mPaints);
			 */

			/*
			 * canvas.drawText(Prefix((int) PrintingValue), (m_DataRect.left - 10), (float)
			 * (m_DataRect.bottom - (Y_index_size * (i + 1))) + 10, mPaints);
			 */

		} // end for
		mPaints.setTextAlign(Align.LEFT);
	}

	public void Change_X_to_Energy(double A, double B, double C) {

		mCaliA = A;
		mCaliB = B;
		mCaliC = C;
		mIsChannel = false;
	}

	public void Change_X_to_Energy(double[] Abc) {

		if (Abc == null)
			return;
		mCaliA = Abc[0];
		mCaliB = Abc[1];
		mCaliC = Abc[2];
		mIsChannel = false;
	}

	public void Change_X_to_Channel() {

		mIsChannel = true;
	}

	private void draw_X_Y_Caption(Canvas canvas, String X_caption, String Y_caption) {

		Paint data_Paints = new Paint();
		// data_Paints.sets
		data_Paints.setAntiAlias(true);
		data_Paints.setStyle(Paint.Style.FILL);
		data_Paints.setStrokeWidth(3);
		data_Paints.setColor(Color.rgb(157, 158, 158));
		data_Paints.setTextSize(NcLibrary.Get_fontSize(m_Width));

		canvas.drawText(X_caption, (m_DataRect.left) - 10, (m_DataRect.top) - 15, data_Paints);
		canvas.drawText(Y_caption, (m_DataRect.right + 8), (m_DataRect.bottom) + 25, data_Paints);

	}

	public void SetTitle(String title) {
		mTitle = title;
	}

	public void Set_DataColor(int color) {
		mDataColor = color;
	}

	public void Set_inform(String Name, String Value) {

		mInform1_title = Name;
		mInform1_value = Value;
	}

	public void Set_inform2(String Name, String Value) {

		mInform2_title = Name;
		mInform2_value = Value;
	}

	public void Set_inform3(String Name, String Value) {
		mInform3_title = Name;
		mInform3_value = Value;
	}

	public void Set_inform4(String Name, String Value) {
		mInform4_title = Name;
		mInform4_value = Value;
	}

	public void Set_SoureId_inform(String Name, String Value) {

		mInform1_SoucreId_title = Name;
		mInform1_SoucreId_value = Value;
	}

	public void Set_SoureId_inform2(String Name, String Value) {

		mInform2_SoucreId_title = Name;
		mInform2_SoucreId_value = Value;
	}

	public void Set_SoureId_inform3(String Name, String Value) {
		mInform3_SoucreId_title = Name;
		mInform3_SoucreId_value = Value;
	}

	public void Set_SoureId_inform4(String Name, String Value) {
		mInform4_SoucreId_title = Name;
		mInform4_SoucreId_value = Value;
	}

	public void Set_inform(String Name1, String Value1, String Name2, String Value2) {
		mInform1_title = Name1;
		mInform1_value = Value1;

		mInform2_title = Name2;
		mInform2_value = Value2;
	}

	public void Set_inform(String Name1, String Value1, String Name2, String Value2, String Name3, String Value3,
			String Name4, String Value4) {
		mInform1_title = Name1;
		mInform1_value = Value1;

		mInform2_title = Name2;
		mInform2_value = Value2;

		mInform3_title = Name3;
		mInform3_value = Value3;

		mInform4_title = Name4;
		mInform4_value = Value4;
	}

	private void draw_inform_1(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);

		int textHeight = measureTextHeight("0", Paints);

		Paints.setColor(getResources().getColor(R.color.WhiteGray));
		canvas.drawText(Name, (float) (m_DataRect.right * 0.3), (m_DataRect.top - 3 - textHeight), Paints);

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value, (float) (m_DataRect.right * 0.3) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight), Paints);

	}

	private void draw_inform_2(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);
		Paints.setColor(getResources().getColor(R.color.WhiteGray));

		int textHeight = measureTextHeight("0", Paints);
		canvas.drawText(Name, (float) (m_DataRect.right * 0.5), (m_DataRect.top - 3 - textHeight), Paints);

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value, (float) (m_DataRect.right * 0.5) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight), Paints);

	}

	private void draw_inform_3(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);

		int textHeight = measureTextHeight("0", Paints);

		Paints.setColor(getResources().getColor(R.color.WhiteGray));
		canvas.drawText(Name, (float) (m_DataRect.right * 0.7), (m_DataRect.top - 3 - textHeight), Paints);
		String temp = Name + ": ";

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value, (float) (m_DataRect.right * 0.7) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight), Paints);

	}

	private void draw_inform_4(Canvas canvas, String Name, String Value) {
		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);

		int textHeight = measureTextHeight("0", Paints);

		Paints.setColor(getResources().getColor(R.color.WhiteGray));
		canvas.drawText(Name, (float) (m_DataRect.right * 0.9), (m_DataRect.top - 3 - textHeight), Paints);
		String temp = Name + ": ";

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value, (float) (m_DataRect.right * 0.9) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight), Paints);

	}

	private void draw_inform_sourceid_1(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);

		int textHeight = measureTextHeight("0", Paints);

		Paints.setColor(getResources().getColor(R.color.Brown));
		canvas.drawText(Name, (float) (m_DataRect.right * (0.44 + AddSouridRightMove)),
				(m_DataRect.top - 3 - textHeight - AddSouridTopMove), Paints);

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value, (float) (m_DataRect.right * (0.44 + AddSouridRightMove) - AddSouridTopMove)
				+ Paints.measureText(Name) + 10, (m_DataRect.top - 3 - textHeight), Paints);

	}

	private void draw_inform_sourceid_2(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);
		Paints.setColor(getResources().getColor(R.color.Brown));

		int textHeight = measureTextHeight("0", Paints);
		canvas.drawText(Name, (float) (m_DataRect.right * (0.533 + AddSouridRightMove)),
				(m_DataRect.top - 3 - textHeight - AddSouridTopMove), Paints);

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value,
				(float) (m_DataRect.right * (0.533 + AddSouridRightMove)) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight - AddSouridTopMove), Paints);

	}

	private void draw_inform_sourceid_3(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);

		int textHeight = measureTextHeight("0", Paints);

		Paints.setColor(getResources().getColor(R.color.Brown));
		canvas.drawText(Name, (float) (m_DataRect.right * (0.693 + AddSouridRightMove)),
				(m_DataRect.top - 3 - textHeight - AddSouridTopMove), Paints);
		String temp = Name + ": ";
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE + 11);

		Paints.setStrokeWidth(0.3f);
		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value,
				(float) (m_DataRect.right * (0.653 + AddSouridRightMove)) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight - AddSouridTopMove), Paints);

	}

	private void draw_inform_sourceid_4(Canvas canvas, String Name, String Value) {

		Paint Paints = new Paint();
		Paints.setAntiAlias(true);
		Paints.setStyle(Paint.Style.FILL_AND_STROKE);
		Paints.setStrokeWidth(1);
		Paints.setTextSize(RealTimeActivity.TEXT_SIZE - 2);

		int textHeight = measureTextHeight("0", Paints);

		Paints.setColor(getResources().getColor(R.color.Brown));
		canvas.drawText(Name, (float) (m_DataRect.right * 0.89), (m_DataRect.top - 3 - textHeight), Paints);
		String temp = Name + ": ";

		// Paints.setTextSize(NcLibrary.Get_fontSize(m_Width)+3);
		Paints.setColor(Color.rgb(255, 255, 255));
		canvas.drawText(Value, (float) (m_DataRect.right * 0.89) + Paints.measureText(Name) + 10,
				(m_DataRect.top - 3 - textHeight), Paints);

	}

	public void setChArraySize(int size) {
		m_Channel_size = size;
		NOW_VIEW_Channel_End_Value = size;
		m_Ch_Array = new int[size];

	}

	public void SetPeakInfo(Vector<NcPeak> Peaks) {
		mPeakInfo = Peaks;
	}
	

	public void SetChArray(int[] Channel) {
		m_Ch_Array = Channel;
	}

	public void SetChArray(Spectrum Channel) {
		m_Ch_Array = Channel.ToInteger();

	}

	public void Show_Info(boolean IsShow) {

		mIsAbleInfo = IsShow;
	}

	public double MAX_in_ChArray(int[] channel) {
		double max_Count = 0;
		for (int i = NOW_VIEW_Channel_Start_Value; i < NOW_VIEW_Channel_End_Value; i++) {
			max_Count = (max_Count < channel[i]) ? channel[i] : max_Count;
		}
		if (m_LogMode == false)
			return (AXIS_Y_DATA_DEFALT_SIZE < max_Count) ? max_Count * 1.2 : AXIS_Y_DATA_DEFALT_SIZE;
		else {

			return pow(10,
					Get_PowRange((AXIS_Y_DATA_DEFALT_SIZE < max_Count) ? max_Count : AXIS_Y_DATA_DEFALT_SIZE) + 1);
		}
	}

	public String PrefixUnit(double Value) {

		String Result = null;
		char Pref = 0;
		int ConversionFactor = 1;
		if (Value > 100000) {
			ConversionFactor = 1000;
			Pref = 'K';
		} // count가 많아질 경우 SI Prefix사용
		if (Value > 100000000) {
			ConversionFactor = 1000000;
			Pref = 'M';
		}

		if (ConversionFactor == 1)
			Result.format("%d", (int) Value);
		else
			Result.format("%.1f%c", Value / (double) ConversionFactor, Pref);

		return Result;
	}

	public int measureTextHeight(String text, Paint paint) {
		Rect result = new Rect();
		// Measure the text rectangle to get the height
		paint.getTextBounds(text, 0, text.length(), result);
		return result.height();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onFinishInflate() {
		setClickable(true);
		// Log.w(Constants.TAG,"onFinishInflate()");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// height 吏꾩쭨 �ш린 援ы븯湲�
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

		// width 吏꾩쭨 �ш린 援ы븯湲�
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

	public int pow(int x, int y) { // 제곱 계산
		int result = 1;
		for (int i = 0; i < y; i++) {
			result *= x;
		}
		return result;
	}

	/////////////////////////////
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		float mZoom_L_Percent = 0;

		private int mZoom_mode = 0;
		private final int ZOOM_IN = 41;
		private final int ZOOM_OUT = 42;

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (SCREEN_ZOOMING == false)
				return false;
			mScaleFactor *= detector.getScaleFactor();

			if (detector.getPreviousSpan() > detector.getCurrentSpan())
				mZoom_mode = ZOOM_OUT;
			else
				mZoom_mode = ZOOM_IN;

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			/// --------
			float temp = (float) (m_Channel_size * 0.001); // 以�媛먮룄
			int zoom_ratio = (int) Math.abs((detector.getCurrentSpan() - detector.getPreviousSpan()));
			zoom_ratio = (int) (zoom_ratio * temp);
			int L_ratio = Math.round((mZoom_L_Percent / 100) * zoom_ratio);
			int R_ratio = Math.round((Math.abs(L_ratio - zoom_ratio)));
			/// -------

			if (mZoom_mode == ZOOM_OUT) {
				if (NOW_VIEW_Channel_Start_Value >= 0 & NOW_VIEW_Channel_End_Value <= MainActivity.CHANNEL_ARRAY_SIZE
						& NOW_VIEW_Channel_End_Value - NOW_VIEW_Channel_Start_Value > 100) {
					if (NOW_VIEW_Channel_Start_Value - L_ratio >= 0)
						NOW_VIEW_Channel_Start_Value -= L_ratio; // zoom
					else
						NOW_VIEW_Channel_Start_Value = 0;
					if (NOW_VIEW_Channel_End_Value + R_ratio <= MainActivity.CHANNEL_ARRAY_SIZE)
						NOW_VIEW_Channel_End_Value += R_ratio; // zoom
					else
						NOW_VIEW_Channel_End_Value = MainActivity.CHANNEL_ARRAY_SIZE;
				}
			} else {
				if (NOW_VIEW_Channel_Start_Value >= 0 & NOW_VIEW_Channel_End_Value <= MainActivity.CHANNEL_ARRAY_SIZE
						& (NOW_VIEW_Channel_End_Value - R_ratio) - (NOW_VIEW_Channel_Start_Value + L_ratio) > 100) {

					NOW_VIEW_Channel_Start_Value += L_ratio; // zoom
					NOW_VIEW_Channel_End_Value -= R_ratio; // zoom
				}
			}

			m_Channel_size = NOW_VIEW_Channel_End_Value - NOW_VIEW_Channel_Start_Value;

			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			float BeginX = detector.getFocusX();
			float BeginY = detector.getFocusY();

			if (m_DataRect.left < BeginX & m_DataRect.right > BeginX & m_DataRect.top < BeginY
					& m_DataRect.bottom - 50 > BeginY) {
				SCREEN_ZOOMING = true;
				mZoom_L_Percent = ((BeginX - m_DataRect.left) / m_DataRect.width()) * 100;
			} else
				return false;

			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			SCREEN_ZOOMING = false;
			super.onScaleEnd(detector);
		}

	}

	public void subscriptDrawText(int value, Canvas canvas, Paint paint, float txtX, float txtY, String inputMessage,

			String supScript, Float TextSize) {

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

		paint.setColor(Color.rgb(192, 192, 192));
		paint.setStrokeWidth((float) 0.3);

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
			paint.setColor(Color.rgb(255, 255, 255));
			paint.setStrokeWidth((float) 0.3);
			paint.setTextSize(subAndSupTextSize);

			float height3 = paint.getFontSpacing();

			canvas.drawText(supScript, txtX + width1 - 14,
					txtY - ((height - height1) / subAndSupTextSize) - supScriptUpLangth, paint);
		}
	}

	public void SetSpectrum(Spectrum Spc) {
		mSpc = Spc;

	}

}
