package android.HH100;

import java.io.*;
import java.util.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import android.HH100.R;
import android.HH100.Control.*;
import android.HH100.DB.*;
import android.HH100.EventListActivity.MyArrayAdapter;
import android.HH100.Identification.FindPeaksM;
import android.HH100.Identification.FindPeaksN;
import android.HH100.Identification.Isotope;
import android.HH100.LogActivity.LogTabActivity;
import android.HH100.R.id;
import android.HH100.R.layout;
import android.HH100.R.menu;
import android.HH100.R.string;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.NcPeak;
import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera.CameraInfo;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.*;
import android.view.*;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;

class VallyResult {
	double A;
	double B;
	double Ax;
	double Bx;
}

public class EventLogActivity extends Activity {
	public final static String EXTRA_EVENT_DATA = "EventData";
	private final int PHOTO_RESULT = 223;

	private SpectrumView mSpectrum;
	private String mPhotoName = null;
	private boolean mIsInfoPage = true;
	static String mComment = "";
	// private LogInformID mID_View;
	private int mEventNumber;
	private EventData mEventData = null;
	// private TextView mID_View= null;

	private ArrayList<String> mID_AD = new ArrayList<String>();
	private ListView mID_ListView = null;
	private MyArrayAdapter mEventArray;

	Animation mTextAni = null;
	TextView textTemp = null;

	public EventLogActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout m_MainLayout = (LinearLayout) inflater.inflate(R.layout.log_spectrum, null);

		mEventData = (EventData) getIntent().getSerializableExtra(EXTRA_EVENT_DATA);
		if (mEventData == null)
			mEventData = new EventData();

		Display_ID_Result();

		mID_ListView = (ListView) m_MainLayout.findViewById(R.id.EventLog_ID_List);
		mID_ListView.setAdapter(new MyArrayAdapter(this));
		mID_ListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				/*
				 * AlertDialog.Builder dialogBuilder = new
				 * AlertDialog.Builder(EventLogActivity.this);
				 * dialogBuilder.setTitle(mEventData.Detected_Isotope.get(arg2).isotopes);
				 * dialogBuilder.setMessage(mEventData.Detected_Isotope.get(arg2).Comment);
				 * dialogBuilder.setPositiveButton("Video", new
				 * DialogInterface.OnClickListener(){ public void onClick(DialogInterface
				 * dialog, int whichButton){ Intent intent = new Intent(Intent.ACTION_VIEW); Uri
				 * uri = Uri.fromFile(new
				 * File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Event.
				 * DB_LIB_FOLDER+"/Cs-137.mp4")); intent.setDataAndType(uri, "video/*");
				 * startActivity(intent);
				 *
				 * } }); dialogBuilder.setNegativeButton("Close", null);
				 * dialogBuilder.setCancelable(false); dialogBuilder.show();
				 */

				return true;
			}
		});
		mID_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventLogActivity.this);
				dialogBuilder.setTitle(mEventData.Detected_Isotope.get(arg2).isotopes);
				dialogBuilder.setMessage(mEventData.Detected_Isotope.get(arg2).Comment);
				final String ss = mEventData.Detected_Isotope.get(arg2).isotopes;// .replace("-", "");
				final File video = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
						+ EventDBOper.DB_LIB_FOLDER + "/" + ss + ".mp4");
				if (ss.matches("") == false & video.isFile()) {
					dialogBuilder.setPositiveButton(getResources().getString(R.string.video),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

									Intent intent = new Intent(Intent.ACTION_VIEW);

									Uri uri = Uri.fromFile(video);
									intent.setDataAndType(uri, "video/*");
									startActivity(intent);

								}
							});
				}
				dialogBuilder.setNegativeButton(getResources().getString(R.string.close), null);
				dialogBuilder.setCancelable(false);
				dialogBuilder.show();
			}
		});

		mSpectrum = (SpectrumView) m_MainLayout.findViewById(R.id.LogSpectrum);
		mSpectrum.setChArraySize(MainActivity.CHANNEL_ARRAY_SIZE);
		mSpectrum.LogMode(true);

		mSpectrum.Change_X_to_Energy(mEventData.MS.Get_Coefficients().get_Coefficients());
		mSpectrum.Set_inform(getResources().getString(R.string.acq_time),
				String.valueOf(mEventData.MS.Get_AcqTime() + getResources().getString(R.string.sec)));
		mSpectrum.Set_inform2(getResources().getString(R.string.avg_cps),
				String.valueOf((int) mEventData.MS.Get_AvgCPS() + (int) mEventData.AvgFillCps));
		mSpectrum.Set_inform3(getResources().getString(R.string.total_count),
				String.valueOf((int) mEventData.MS.Get_TotalCount()));
		mSpectrum.SetChArray(mEventData.MS.ToInteger());

		mSpectrum.SetSpectrum(mEventData.MS);

		textTemp = (TextView) m_MainLayout.findViewById(R.id.ID_Title);
		setContentView(m_MainLayout);

		PreferenceDB mPrefDB = new PreferenceDB(this);

		EventDBOper mEventDBOper = new EventDBOper();
		mEventDBOper.Set_Crytal_Info(mPrefDB.Get_CryStal_Type_Number_pref());

		mEventData.MS.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
		mEventData.MS.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
		mEventData.MS.setFWHM(mEventDBOper.Cry_Info.FWHM);

		mEventData.BG.setWnd_Roi(mEventDBOper.Cry_Info.Wnd_ROI_En);
		mEventData.BG.setFindPeakN_Coefficients(mEventDBOper.Cry_Info.FindPeakN_Coefficients);
		mEventData.BG.setFWHM(mEventDBOper.Cry_Info.FWHM);

		Vector<NcPeak> peakInfo_bg = new Vector<NcPeak>();
		peakInfo_bg = FindPeaksN.GetPPSpectrum_H(mEventData.BG);

		mEventData.BG.SetPeakInfo(peakInfo_bg);

		FindPeaksN mFPM = new FindPeaksN();

		Vector<NcPeak> Peaks = mFPM.Find_Peak(mEventData.MS, mEventData.BG);

		mSpectrum.SetPeakInfo(Peaks);

	}

	private void Display_ID_Result() {

		String Result = "";

		for (int i = 0; i < mEventData.Detected_Isotope.size(); i++) {
			if (mEventData.Doserate_unit == -1) {
				Result += mEventData.Detected_Isotope.get(i).isotopes + "\n  Confidence Level:"
						+ mEventData.Detected_Isotope.get(i).Get_ConfidenceLevel() + "\n  Activity:"
						+ mEventData.Detected_Isotope.get(i).DoseRate_S + "\n\n";
			} else {
				Result += mEventData.Detected_Isotope.get(i).isotopes + "\n  Confidence Level:"
						+ mEventData.Detected_Isotope.get(i).Get_ConfidenceLevel() + "\n  Dose Rate:"
						+ mEventData.Detected_Isotope.get(i).DoseRate_S + "\n\n";
			}
		}

		for (int i = 0; i < mEventData.Detected_Isotope.size(); i++) {
			mID_AD.add("asd");
		}

		/// mID_View.setText("");
	}

	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		// mInform.Set_Comment(mComment);
		// Inform.invalidate();

		super.onResume();
	}

	private int Get_IsoClass_Color(String Class) {
		Vector<Integer> mClassColor = new Vector<Integer>();
		mClassColor.add(Color.rgb(150, 24, 150));
		mClassColor.add(Color.rgb(27, 23, 151));
		mClassColor.add(Color.rgb(44, 192, 185));
		mClassColor.add(Color.rgb(10, 150, 20));
		mClassColor.add(Color.RED);

		if (Class == null)
			return 0;
		if (Class.matches(".*SNM.*"))
			return mClassColor.get(0);
		if (Class.matches(".*IND.*"))
			return mClassColor.get(1);
		if (Class.matches(".*MED.*"))
			return mClassColor.get(2);
		if (Class.matches(".*NORM.*"))
			return mClassColor.get(3);
		if (Class.matches(".*UNK.*"))
			return mClassColor.get(4);

		return Color.GRAY;
	}

	private Vector<String> Get_Bitmap_WithFileName(Vector<String> FileName)

	{
		File sdcard = Environment.getExternalStorageDirectory();
		File dbpath = new File(sdcard.getAbsolutePath() + File.separator + EventDBOper.DB_FOLDER);
		if (!dbpath.exists()) {

			dbpath.mkdirs();
		}
		String dbfile = dbpath.getAbsolutePath() + File.separator;

		Vector<String> result = new Vector<String>();
		for (int i = 0; i < FileName.size(); i++) {
			File check = new File(dbfile + FileName.get(i));
			if (check.isFile()) {
				// Bitmap bitmap = BitmapFactory.decodeFile(dbfile+FileName.get(i));
				result.add(dbfile + FileName.get(i));
			}
		}
		return result;
	}

	public VallyResult Find_Vally(double[] arrfloat, int Energy, double EnergyFittingParam[])

	{

		double Roi_window = NcLibrary.Get_Roi_window_by_energy_VAllY(Energy);
		double L_ROI_Percent = 1.0 - (Roi_window * 0.01);
		double R_ROI_Percent = 1.0 + (Roi_window * 0.01);

		// double factor = 0.2;
		// if(En < 63) factor = 0.2;
		int Center = (int) (NcLibrary.Energy_to_Channel(Energy, EnergyFittingParam[0], EnergyFittingParam[1],
				EnergyFittingParam[2]));// *(1-factor);
		int L_location = (int) (Center * L_ROI_Percent);
		int R_location = (int) (Center * R_ROI_Percent);
		VallyResult result = new VallyResult();
		// peak value媛� �뾾�쑝硫� 寃곌낵濡� 0�쓣 return

		double startpx = 0;
		double startpy = 0;
		double endpx = 0;
		double endpy = 0;
		double midpx = 0;
		double midpy = 0;

		// int center = firstlocation+ (int)((secondlocation - firstlocation)/2);

		int contcnt = 0;
		int cntlimit = 2;// NcLibrary.Auto_floor(NcLibrary.Get_Roi_window_by_energy(NcLibrary.Channel_to_Energy(peakvalue,
		// calib_A2, calib_B2, calib_C2)));
		double threshold = 0;

		int findflag = 0;
		for (int i = Center - (int) (Center * 0.01); i > L_location; i--) {
			if ((arrfloat[i - 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						startpx = i + (cntlimit - 1);
						startpy = arrfloat[i];
						break;
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}
		if (findflag == 0 || contcnt < cntlimit) {
			startpx = L_location;
			startpy = arrfloat[L_location];
		}
		contcnt = 0;
		findflag = 0;
		for (int i = Center + (int) (Center * 0.01); i < R_location; i++) {
			if ((arrfloat[i + 1] - arrfloat[i]) >= threshold) {
				if (findflag == 0) {
					findflag = 1;
					contcnt = 1;
				} else {
					contcnt++;
					if (contcnt >= cntlimit) {
						endpx = i - (cntlimit - 1);
						endpy = arrfloat[i];
						break;
					}
				}
			} else {
				findflag = 0;
				contcnt = 0;
			}
		}
		if (findflag == 0 || contcnt < cntlimit) {
			endpx = R_location;
			endpy = arrfloat[R_location];
		}

		midpx = Center;
		midpy = arrfloat[Center];

		// final A, B 媛믨퀎�궛
		result.A = (startpy - endpy) / (startpx - endpx);
		result.B = startpy - (result.A * startpx);
		result.Ax = startpx;
		result.Bx = endpx;
		return result;
	}

	public double[] ft_smooth(double[] ihist, double aval, double bval) {

		int temp_wind = (int) Math.ceil(aval * 1024 + bval);
		double temp;
		int window = 0;
		int window_half = 0;
		double temp_sum = 0;
		double temphist1[] = new double[1024];
		double temphist2[] = new double[1024];
		double Result[] = new double[1024];
		for (int i = 0; i < 1024; i++) {
			temphist1[i] = 0;
			temphist2[i] = 0;
		}

		for (int i = 4; i < 1024 - temp_wind; i++) {

			temp_sum = 0;
			temp = 0;
			window = (int) Math.ceil(aval * i + bval);

			if (window <= 0)
				continue;

			if (window % 2 == 0)
				window = window + 1;

			window_half = window / 2 - 1;

			for (int j = i - window_half; j <= i + window_half; j++)
				temp_sum = temp_sum + ihist[j];

			temp = temp_sum / window;
			temphist1[i] = temp;
		}
		for (int i = 4; i < 1024 - temp_wind; i++) {

			temp_sum = 0;
			temp = 0;
			window = (int) Math.ceil(aval * i + bval);

			if (window <= 0)
				continue;

			if (window % 2 == 0)
				window = window + 1;

			window_half = window / 2 - 1;

			for (int j = i - window_half; j <= i + window_half; j++)
				temp_sum = temp_sum + temphist1[j];

			temp = temp_sum / window;
			temphist2[i] = temp;
		}
		for (int i = 0; i < 1024; i++)
			Result[i] = temphist2[i];

		return Result;
	}

	///////////////////////////////////////////////////////////////////
	class MyArrayAdapter extends ArrayAdapter {

		Context context;

		MyArrayAdapter(Context context) {

			super(context, R.layout.id_list_row, mID_AD);
			this.context = context;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = ((Activity) context).getLayoutInflater();

			View row = inflater.inflate(R.layout.id_list_row, null);

			TextView EventNum = (TextView) row.findViewById(R.id.Id_list_Name);

			//0109 추가 Screening_Process ==0 이면 red로 표시
			if(mEventData.Detected_Isotope.get(position).Screening_Process == 1)
			{
				EventNum.setTextColor(Color.rgb(230, 220, 0));
			}
			else
			{
				//EventNum.setTextColor(Color.rgb(255, 0, 0));
				EventNum.setTextColor(Color.rgb(230, 220, 0));
			}
			EventNum.setText(mEventData.Detected_Isotope.get(position).isotopes);


			TextView incharge = (TextView) row.findViewById(R.id.Id_list_Confidence);
			incharge.setText(getResources().getString(R.string.confidence_level) + ": "
					+ mEventData.Detected_Isotope.get(position).Get_ConfidenceLevel()+"% "  );

			TextView value = (TextView) row.findViewById(R.id.Id_list_Doserate);
			if (mEventData.Doserate_unit == -1) {
				value.setText(getResources().getString(R.string.activity) + ": "+ mEventData.Detected_Isotope.get(position).DoseRate_S+" ");
			} else {
				value.setText(getResources().getString(R.string.dose_rate) + ": "+ mEventData.Detected_Isotope.get(position).DoseRate_S+" ");
			}

			// TextView Detector = (TextView)row.findViewById(R.id.Detector);
			// Detector.setText(mTimer.get(position));

			return row;

		}

	};
}
