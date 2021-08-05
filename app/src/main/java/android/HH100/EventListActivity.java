
package android.HH100;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.HH100.IDspectrumActivity.Check;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.Isotope;
import android.HH100.LogActivity.LogTabActivity;
import android.HH100.Structure.EventData;
import android.HH100.Structure.Mail;
import android.HH100.Structure.NcLibrary;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import static android.HH100.Structure.NcLibrary.Separate_EveryDash3;
import static android.widget.AbsListView.TRANSCRIPT_MODE_NORMAL;

public class EventListActivity extends Activity implements AbsListView.OnScrollListener{
	// private static final int CHOOSE_EVENT_FILE = 234124;

	private Context mContext;

	private ArrayList<String> mEventID, mDate, mDate1,mAcqTime,mId,mGamma,mTimer, mTimer1,mManual_ID,mFavorite_Checked,
			mEndTime,mStartTime,mSourceName,mLatitude,mLongitude,mDoserate_S,mDoserate_Unit,mConfidence_Level,mComment;

	public static int mSelPositioin = 0;

	public static EventData EventLog = null;
	public static Vector<EventData> mAllLog = null;
	MyArrayAdapter mEventArray;
	static ListView mEventList = null;
	ProgressDialog mPrgDlg;

	boolean menuBtnClick = false;

	public static boolean eventLogMenu = false;

	public static Activity EventListActivity;
	// 다이얼로그창
	AlertDialog alert1;

	//190529 EventList수정
	ArrayList<EventData> arr;
	EventListAdapter adt;
	int clickIndex = 0;
	ListView eventList;
	EventDBOper mEventDB;
	int selectId = -1;
	String photoFile = "";

	ProgressBar progressBar;
	int currentPage = 0; // 페이징변수. 초기 값은 1 이다.
	boolean lastItemVisibleFlag = false; // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 체크할 변수
	boolean mLockListView = false; // 데이터 불러올때 중복안되게 하기위한 변수
	final int OFFSET = 20;                  // 한 페이지마다 로드할 데이터 갯수.
	int dbCount = 0; // eventdb 전체 count
	boolean clickChk = false; //핸들키로 누르면 2번눌리는 현상이있음..
	ActionBar actionBar; //상단 엑션바 ex:이벤트
	TextView actionBarTitle;

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
				case 0:
					Show_Dlg(getResources().getString(R.string.email_transmit_success).toString());
					break;
				case 1:
					Show_Dlg(getResources().getString(R.string.email_transmit_failed).toString());
					break;
				case 2:
					Show_Dlg(getResources().getString(R.string.email_info_fail).toString());
					break;
				case 3:
					Show_Dlg(getResources().getString(R.string.internet_not).toString());
					break;
				default:
					break;
			}
		};
	};

	// Pos Global

	int pos1;

	@Override
	protected void onResume() {

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;


		// 해당하는 인덱스로 스크롤 이동
		if (arr != null && arr.size() != 0) {
			eventList.smoothScrollToPosition(clickIndex);
		}

		super.onResume();
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);

		setContentView(R.layout.database);

		PowerManager pm = (PowerManager) EventListActivity.this.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "");
		wl.acquire();

		mContext = this;

		mPrgDlg = new ProgressDialog(mContext);
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

        MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
        MainActivity.ACTIVITY_STATE = Activity_Mode.EVENTLOG_LIST_MAIN;

		mContext = EventListActivity.this;
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		eventList = (ListView) findViewById(R.id.eventlist);
		//eventList.setOnScrollListener(this);
		arr = new ArrayList<EventData>();

		//180803 actionbar custom
		actionBar = getActionBar();
		actionBar.setDisplayShowHomeEnabled(true); //아이콘 사용할지 여부
		actionBar.setDisplayShowTitleEnabled(false); //타이블 사용할지여부
		actionBar.setDisplayShowCustomEnabled(true);

		LinearLayout actionBarLayout = new LinearLayout(EventListActivity.this);
		actionBarLayout.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout.LayoutParams paramLLayoutBG = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		actionBarLayout.setLayoutParams(paramLLayoutBG);

		LinearLayout.LayoutParams chk = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		chk.gravity = Gravity.CENTER_VERTICAL;
		chk.weight = 1;
		actionBarTitle = new TextView(EventListActivity.this);
		actionBarTitle.setTextSize(16);
		actionBarTitle.setTextColor(Color.rgb(255, 255, 255));
		actionBarTitle.setText(getResources().getString(R.string.event_log));
		actionBarTitle.setTag("actionBar");
		actionBarTitle.setLayoutParams(chk);
		actionBarTitle.setOnClickListener(click);
		actionBarLayout.addView(actionBarTitle);

		//20.02.04
		if(MainActivity.admin) {

			final int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
	//		final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

			chk = new LinearLayout.LayoutParams(size,size);
			chk.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;
			chk.setMargins(0, 0, 20, 0);
			ImageView delete = new ImageView(EventListActivity.this);
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				delete.setBackground(ContextCompat.getDrawable(this, R.drawable.delete));
			} else {
				delete.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.delete));
			}
			delete.setLayoutParams(chk);
			delete.setTag("delete");
			delete.setOnClickListener(click);
			actionBarLayout.addView(delete);
			//actionBar.setCustomView(actionBarLayout);
		}
////
		actionBar.setCustomView(actionBarLayout);
		loadDB(currentPage);

	}


	// click Listener
	private View.OnClickListener click = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			switch ((String) view.getTag()) {
				case "actionBar":
					eventList.setSelection(0); //최상단으로 이동
					break;
				case "delete":
					NcLibrary.deleteDB(EventListActivity.this, new NcLibrary.OnOk(){
						@Override
						public void delete(int delete) {
							if(delete==1){
								currentPage = 0;
								dbCount = 0;
								//	eventList.invalidateViews();
								eventList.setAdapter(null);
							//	adt.notifyDataSetChanged();

							//	adt.notifyDataSetChanged();

								((Activity) EventListActivity.this).runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(EventListActivity.this, getResources().getString(R.string.delete_success), Toast.LENGTH_LONG).show();
									}
								});

							}else if(delete == 2){
								((Activity) EventListActivity.this).runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(EventListActivity.this, getResources().getString(R.string.delete_failed), Toast.LENGTH_LONG).show();
									}
								});
							}
						}
					});

					break;
			}

		}
	};
	public void KeyExecute(final int keyvalue) {

		new Thread(new Runnable() {

			public void run() {

				new Instrumentation().sendKeyDownUpSync(keyvalue);

			}
		}).start();
	}
/*	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Toast.makeText(getApplicationContext(), "onKeyDown "+keyCode, Toast.LENGTH_LONG).show();
		return super.onKeyDown(keyCode, event);
	}*/


@Override
public boolean dispatchKeyEvent(KeyEvent event)
{
	int[] coordinates = new int[2];
	long downTime = 0;
	long eventTime = 0;
	MotionEvent down_event, up_event;
	try {
		getCurrentFocus().getLocationOnScreen(coordinates);
	}
	catch (NullPointerException e) {
		return super.dispatchKeyEvent(event);
	}

	//getCurrentFocus().getLocationOnScreen(coordinates);

	if(!clickChk) {
		clickChk = true;
		switch (event.getKeyCode()) {

			case KeyEvent.KEYCODE_ENTER:
				eventList.getSelectedView().getLocationOnScreen(coordinates);

				downTime = SystemClock.uptimeMillis();
				eventTime = SystemClock.uptimeMillis();
				down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, coordinates[0], coordinates[1], 0);
				up_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, coordinates[0], coordinates[1], 0);
				eventList.dispatchTouchEvent(down_event);
				eventList.dispatchTouchEvent(up_event);
				return false;

			case KeyEvent.KEYCODE_POWER: //longclick 일때

				eventList.getSelectedView().getLocationOnScreen(coordinates);

				downTime = SystemClock.uptimeMillis();
				eventTime = SystemClock.uptimeMillis();
				down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, coordinates[0], coordinates[1], 0);
				eventList.dispatchTouchEvent(down_event);
				return false;
		}
	}
	else {
		clickChk = false;
	}




		return super.dispatchKeyEvent(event);
	}

	public void loadDB(int page) {
		//List에 사용될 DB내용들 저장
		try {
			mEventDB = new EventDBOper();
			mEventDB.OpenDB();
			dbCount = (int) mEventDB.getEventDBCount();
			eventList.setTag("event");

			Cursor cu = mEventDB.LoadEventList(page * OFFSET, OFFSET);

			if (cu.getCount() != 0) {

				while (cu.moveToNext()) {
					EventData Item = new EventData();
					Item.Event_Number = Integer.parseInt(cu.getString(cu.getColumnIndex("_id")));
					Item.EventData = cu.getString(cu.getColumnIndex("Date"));
					Item.AcqTime = cu.getString(cu.getColumnIndex("AcqTime"));
					Item.Doserate_AVGs = cu.getString(cu.getColumnIndex("Avg_Gamma"));
					Item.Favorite_Checked = cu.getString(cu.getColumnIndex("Favorite"));
					Item.Event_Detector = cu.getString(cu.getColumnIndex("Event_Detector"));
					Item.StartTime = cu.getString(cu.getColumnIndex("begin"));
					Item.PhotoFileName1 = Separate_EveryDash3(cu.getString(cu.getColumnIndex("Photo")));

					String temp = cu.getString(cu.getColumnIndex("Identification"));
					Vector<String> IsoTemp = NcLibrary.Separate_EveryDash2(temp, '|');

					String temp1 = "None";
					for (int i = 0; i < IsoTemp.size(); i++) {
						Isotope iso = new Isotope();
						iso.Set_Result_OnlyDB_v1_5(IsoTemp.get(i));
						ArrayList<Isotope> tempIdentification = new ArrayList<Isotope>();
						tempIdentification.add(iso);

						if (tempIdentification != null && tempIdentification.size() != 0) {
							temp1 = "";
							for (int k = 0; k < tempIdentification.size(); k++) {
								if (k == tempIdentification.size() - 1) {
									temp1 = tempIdentification.get(k).isotopes;
									Item.Identification.add(temp1);
									break;
								} else {
									temp1 = temp1 + tempIdentification.get(k).isotopes + ", ";
									Item.Identification.add(temp1);
								}

							}
						}

					}
					arr.add(Item);
				}

				cu.close();
				mEventDB.EndDB();

				adt = new EventListAdapter(EventListActivity.this, arr);
				eventList.setAdapter(adt);
				adt.setOnListener(onListCellClick);
				eventList.setSelection((currentPage * OFFSET) - 1);
			} else {
				if (currentPage != 0) {
					Toast.makeText(EventListActivity.this, getResources().getString(R.string.eventlog_load_failed), Toast.LENGTH_SHORT).show();
					currentPage--;
					eventList.setSelection(adt.getCount() - 1);
				} else {
					eventList.setAdapter(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	//clickListener
	private EventListAdapter.clickListener onListCellClick = new EventListAdapter.clickListener()
	{
		@Override
		public void onCellClick(String type, final int id, int index, final int photoSize)
		{
			clickIndex = index;
			selectId = id;
			Log.e("ahn", "clickIndex : " + clickIndex);
			if (type.equals("click"))
			{
				try
				{
		/*			DBUpdate();

					mSelPositioin = Integer.valueOf(mEventID.get(position)) - 1;

					EventLog = mAllLog.get(mSelPositioin);

					Intent intent = new Intent(mContext, LogTabActivity.class);
					startActivity(intent);*/

					//NcLibrary.SaveText("clickIndex  "+id+"\n");
					Intent intent = new Intent(EventListActivity.this, LogTabActivity.class);
					intent.putExtra("_id", selectId);
					startActivity(intent);

					//	return;
				}catch (Exception e)
				{
					//NcLibrary.SaveText("click Exception\n");
					NcLibrary.Write_ExceptionLog(e);
				}

			}
			if (type.equals("longclick"))
			{

				PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
				boolean abc = false;
				try
				{
					abc = prefDB.Get_RadresponderMode_From_pref();
				} catch (NullPointerException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				if (abc == false) {
					SendEmailDlg(selectId);
				} else {
					DialogRadio(selectId);
				}

			}

			return;
		}

	};


	private void Show_Dlg(String Message) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity.this);
		// dialogBuilder.setTitle(Title);
		dialogBuilder.setMessage(Message);
		dialogBuilder.setNegativeButton("OK", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		int aa = 0;

		String str = data.getAction();
		str = str.replace("file://", "");

		try {
			if (str.matches(".*.xml") == false)
				throw new SAXException();

			EventLog = NcLibrary.Event_XML.ReadXML_ANSI42(str);

			Intent intent = new Intent(mContext, LogTabActivity.class);
			startActivity(intent);
		} catch (SAXException e1) {
			Show_Dlg(getResources().getString(R.string.not_event_file));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			NcLibrary.Write_ExceptionLog(e1);
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			NcLibrary.Write_ExceptionLog(e1);
		}

		// --

		super.onActivityResult(requestCode, resultCode, data);
	}

	public boolean isNetworkOnline() {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			return false;
		}
		return status;

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

		DecimalFormat format = new DecimalFormat();
		String unit = "Sv";
		double value = 1;

		if (point == true) {
			if (nSv < pow(10, 3)) {
				value = nSv;
				unit = "nSv";
			} else if (nSv >= pow(10, 3) & nSv < pow(10, 6)) {
				value = (nSv * 0.001);
				unit = "uSv";
			} else if (nSv >= pow(10, 6) & nSv < pow(10, 9)) {
				value = (nSv * 0.000001);
				unit = "mSv";
			} else if (nSv > pow(10, 9)) {
				value = (nSv * 0.000000001);
				unit = "Sv";
			}
		} else {
			if (nSv < pow(10, 3))
				return (long) nSv + "nSv";
			else if (nSv >= pow(10, 3) & nSv < pow(10, 6))
				return (long) (nSv * 0.001) + "uSv";
			else if (nSv >= pow(10, 6) & nSv < pow(10, 9))
				return (long) (nSv * 0.000001) + "mSv";
			else if (nSv > pow(10, 9))
				return (long) (nSv * 0.000000001) + "Sv";
		}

		format.applyLocalizedPattern("0.##");

		return format.format(value) + unit;
	}


	@Override
	public void onBackPressed() {
/*		// TODO Auto-generated method stub
		MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		//190102 추가
		MainActivity.SendU2AA();
		if(	MainActivity.U2AATimerTask!=null)
		{
			MainActivity.U2AATimerTask.cancel();
			MainActivity.U2AATimerTask = null;
		}
		MainActivity.U2AATimerTask = new TimerTask()
		{
			@Override
			public void run() {
				MainActivity.SendU2AA();
			}
		};
		Timer mTimer = new Timer();
		mTimer.schedule(MainActivity.U2AATimerTask , 1500);
		NcLibrary.SaveText1("onBackPress\n","test");*/
		super.onBackPressed();

	}
	// ArrayAdapter�뿉�꽌 �긽�냽諛쏅뒗 而ㅼ뒪�� ArrayAdapter

	private Account Get_Gmail_account() {
		Account result = null;

		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(mContext).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				result = account;

			}
		}

		return result;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	class MyArrayAdapter extends ArrayAdapter {

		Context context;

		public ListHolder mHolder;
		public class ListHolder
		{
			public TextView EventNum;
			public TextView ManualID;
			public TextView incharge;
			public TextView value;
			public TextView date;
			public TextView date_time;
			public TextView location;
			public TextView FavoriteCheck;
		}


		MyArrayAdapter(Context context) {

			super(context, R.layout.database_row, mDate);
			this.context = context;

		}



		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
			{
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				convertView = inflater.inflate(R.layout.database_row, null);
				mHolder = new ListHolder();

				mHolder.EventNum = (TextView) convertView.findViewById(R.id.Event_Num);
				mHolder.ManualID = (TextView) convertView.findViewById(R.id.Manual_ID);
				mHolder.incharge = (TextView) convertView.findViewById(R.id.location);
				mHolder.value = (TextView) convertView.findViewById(R.id.value);
				mHolder.date = (TextView) convertView.findViewById(R.id.date);
				mHolder.date_time = (TextView) convertView.findViewById(R.id.date_time);
				mHolder.location = (TextView) convertView.findViewById(R.id.incharge);
				mHolder.FavoriteCheck = (TextView) convertView.findViewById(R.id.Add_Favorite_Txt);
				convertView.setTag(mHolder);
			}
			else
			{
				mHolder = (ListHolder) convertView.getTag();
			}


			mHolder.EventNum.setText(mEventID.get(position));
			mHolder.ManualID.setText(mManual_ID.get(position));
			mHolder.incharge.setText(getResources().getString(R.string.alarm_duration) + " : " + mAcqTime.get(position) + " "+ getResources().getString(R.string.sec) + "   ");
			mHolder.value.setText(getResources().getString(R.string.avg_doserate) + " : " + mGamma.get(position) + "   ");
			mHolder.date.setText(mDate1.get(position));
			mHolder.date_time.setText(mTimer1.get(position));
			mHolder.location.setText(getResources().getString(R.string.radionuclide_id) + " : " + mId.get(position) + "   ");
			try {
				if (mFavorite_Checked.get(position).equals(Check.Favorite_True)) {
					mHolder.FavoriteCheck.setVisibility(View.VISIBLE);

				} else {
					mHolder.FavoriteCheck.setVisibility(View.INVISIBLE);
				}
			} catch (Exception e) {
				NcLibrary.Write_ExceptionLog(e);
			}

			// TextView Detector = (TextView)row.findViewById(R.id.Detector);
			// Detector.setText(mTimer.get(position));

			return convertView;
		}

	};

	@Override

	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
		}
		if (id == R.id.action_settings2) {

			Intent intent = new Intent(EventListActivity.this, RadresponderActivity.class);
			intent.putExtra("GPS_Latitude", mLatitude.get(pos1));
			intent.putExtra("GPS_Longitude", mLongitude.get(pos1));

			String[] mGamma1 = mGamma.get(pos1).split(" ");

			intent.putExtra("Doserate_AVGs", mGamma1[0]);
			intent.putExtra("Date", mEndTime.get(pos1));
			startActivity(intent);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void DialogRadio(int id) {

		final CharSequence[] PhoneModels = { "Email", "RadResponder" };
		final AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);

		// alt_bld.setIcon(R.drawable.icon);
		alt_bld.setTitle(getString(R.string.rad_response_question_transmission_system));

		alt_bld.setSingleChoiceItems(PhoneModels, -1, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {

				if (PhoneModels[item].equals("Email")) {

					SendEmailDlg(id);
				}
				if (PhoneModels[item].equals("RadResponder")) {

					PreferenceDB prefDB = new PreferenceDB(getApplicationContext());
					//String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventlist_key));
					//20.01.06 rad_response_eventid_key로 수정 기존(0,1로만 저장)
					String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key));

					abc =  prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key)) == null ? getString(R.string.rad_response_eventlist11) : prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key) );
					if (abc.equals(getString(R.string.rad_response_eventlist11))) {
						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity.this);
						dialogBuilder.setTitle(getResources().getString(R.string.Radresponder_Emergency_Msg_Title));
						dialogBuilder
								.setMessage(getResources().getString(R.string.Radresponder_Emergency_Msg_Contents));
						dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog, int whichButton) {
										RadResponderRun(selectId);

									}
								});
						dialogBuilder.setNegativeButton("Cancel", null);
						dialogBuilder.setCancelable(false);
						dialogBuilder.show();
					} /*else if (abc.equals(RadresponderActivity.mTestingTraining)) {
						RadResponderRun(selectId);
					}*/else{
						RadResponderRun(selectId);
					}

				}

				// dialog.cancel();
				alert1.dismiss();

			}
		});

		AlertDialog alert = alt_bld.create();
		alert1 = alert;
		alert.show();
	}

	public void SendEmailDlg(int _id) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventListActivity.this);
		dialogBuilder.setTitle(getResources().getString(R.string.transmit_N42));
		dialogBuilder.setMessage(getResources().getString(R.string.send_toRCBCenter_event));

		dialogBuilder.setNegativeButton("Cancel", null);

		dialogBuilder.setNeutralButton(getResources().getString(R.string.transmit),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

						// SendEmail(Integer.valueOf(mEventID.get(pos1))
						// - 1);

						EventDBOper db = new EventDBOper();
						db.OpenDB();
						EventData event = db.LoadEventDB(_id);
						db.EndDB();

						CreateCsvFile(event);
						CreateN42File(event);

						NcLibrary.SendEmail(event, mContext, mHandler);

					}
				});

		dialogBuilder.setPositiveButton(getResources().getString(R.string.XML_Save),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {

						EventDBOper db = new EventDBOper();
						db.OpenDB();
						EventData event = db.LoadEventDB(_id);
						db.EndDB();

						CreateCsvFile(event);
						CreateN42File(event);

						int serial = 3;
						String suffix = String.format("%03d", serial);

						String path = NcLibrary.Event_XML.WriteXML_toANSI42(event,
								"Event" + String.format("%03d", (event.Event_Number)) + "_" + event.EventData + "_"
										+  event.StartTime.replace(":", "_") + "(" + event.mInstrument_Name + ").xml");

						File f = new File(path);
						if(f.exists())
						{
							Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_LONG).show();
						}

					}
				});
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();

	}

	public void RadResponderRun(int id) {


		EventDBOper db = new EventDBOper();
		db.OpenDB();
		EventData event = db.LoadEventDB(id);
		db.EndDB();

		CreateCsvFile(event);
		NcLibrary.Event_XML.WriteXML_toANSI42(event);

		Intent intent = new Intent(EventListActivity.this, RadresponderActivity.class);
		intent.putExtra(RadresponderActivity.GPS_LAT, event.GPS_Latitude+"");
		intent.putExtra(RadresponderActivity.GPS_LONG, event.GPS_Longitude+"");


		String[] Doserate_AVGs = event.Doserate_AVGs.split(" ");
		intent.putExtra(RadresponderActivity.DOSERATE_TYPE, Doserate_AVGs[1]);
		intent.putExtra(RadresponderActivity.COLLECTION_DATE, event.EventData);
		intent.putExtra(RadresponderActivity.START_TIME, event.StartTime);
		intent.putExtra(RadresponderActivity.END_TIME, event.EndTime);


		if(event.Detected_Isotope !=null && event.Detected_Isotope.size() != 0)
		{
			String[] doserate = event.Detected_Isotope.get(0).DoseRate_S.split(" ");
			intent.putExtra(RadresponderActivity.SOURCE_NAME_S, event.Detected_Isotope.get(0).isotopes);
			intent.putExtra(RadresponderActivity.DOSERATE_S, doserate[0]);
			intent.putExtra(RadresponderActivity.DOSERATE_UNIT, doserate[1]);
			intent.putExtra(RadresponderActivity.LEVEL_S, event.Detected_Isotope.get(0).Confidence_Level+"");
		}
		else
		{
			intent.putExtra(RadresponderActivity.SOURCE_NAME_S, "None");
			intent.putExtra(RadresponderActivity.DOSERATE_S, "");
			intent.putExtra(RadresponderActivity.DOSERATE_UNIT, "");
			intent.putExtra(RadresponderActivity.LEVEL_S, "");
		}



		intent.putExtra(RadresponderActivity.COMMENT_TITLE, event.Comment);

		startActivity(intent);
	}


	@Override
	protected void onDestroy() {

		MainActivity.ACTIVITY_STATE = Activity_Mode.FIRST_ACTIVITY;
		//190102 추가
		//NcLibrary.U2AATimer();
		super.onDestroy();
	}

	public void SendEmail(final int pos) {
		try {

			Thread thread = new Thread() {

				@Override
				public void run() {

					super.run();

					if (isNetworkOnline() == false) {
						mHandler.sendEmptyMessage(3);
						mPrgDlg.dismiss();
						return;
					}

					PreferenceDB pref = new PreferenceDB(EventListActivity.this.mContext);
					if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) {
						mHandler.sendEmptyMessage(2);
						mPrgDlg.dismiss();
						return;
					}

					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String sender_server = pref.Get_sender_Server();
					String sender_port = pref.Get_sender_Port();
					String recv_mail = pref.Get_recv_email();

					Mail m = new Mail(sender, sender_pw, sender_server, sender_port);

					// Array of emailIds where you want to
					// sent
					String[] toArr = new String[1];
					toArr[0] = recv_mail;
					m.setTo(toArr);

					// Your emailid(from)
					m.setFrom(sender);
					m.setSubject("SAM III PeakAbout - Event Data");
					m.setBody("From " + mAllLog.get(pos).mInstrument_Name);
					try {
						m.addAttachment(GetCsvPath(), "SAM (" + CurrentDate() + " ).csv");
						m.addAttachment(NcLibrary.Event_XML.WriteXML_toANSI42(mAllLog.get(pos)),
								mAllLog.get(pos).EventData + "_" + mAllLog.get(pos1).StartTime + "("
										+ mAllLog.get(pos).mInstrument_Name + ").xml");
					} catch (Exception e1) {
						NcLibrary.Write_ExceptionLog(e1);
					}

					try {
						if (m.send()) {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(0);
							Log.v("Forgot Password mail", "Success");

						} else {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(1);
							Log.v("Forgot Password mail", "Not Success");
						}
					} catch (Exception e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(2);
						Log.e("MailApp", "Could not send email", e);
					}

				}

			};

			thread.start();

		} catch (Exception e) {

			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void SendEmail(final EventData m_EventData, final Context mContext, final Handler mHandler) {

		try {

			final ProgressDialog mPrgDlg;
			mPrgDlg = new ProgressDialog(mContext);
			mPrgDlg.setIndeterminate(true);
			mPrgDlg.setCancelable(false);

			Thread thread = new Thread() {

				@Override
				public void run() {

					super.run();

					if (isNetworkOnline() == false) {
						mHandler.sendEmptyMessage(3);
						mPrgDlg.dismiss();
						return;
					}

					PreferenceDB pref = new PreferenceDB(mContext);
					if (pref.Get_sender_email() == "" | pref.Get_sender_email() == null) {
						mHandler.sendEmptyMessage(2);
						mPrgDlg.dismiss();
						return;
					}

					String sender = pref.Get_sender_email();
					String sender_pw = pref.Get_sender_pw();
					String sender_server = pref.Get_sender_Server();
					String sender_port = pref.Get_sender_Port();
					String recv_mail = pref.Get_recv_email();

					Mail m = new Mail(sender, sender_pw, sender_server, sender_port);

					// Array of emailIds where you want to
					// sent
					String[] toArr = new String[1];
					toArr[0] = recv_mail;
					m.setTo(toArr);

					// Your emailid(from)
					m.setFrom(sender);
					m.setSubject("SAM III PeakAbout - Event Data");
					m.setBody("From " + m_EventData.mInstrument_Name);
					try {
						m.addAttachment(GetCsvPath(), "SAM (" + CurrentDate() + " ).csv");
						m.addAttachment(NcLibrary.Event_XML.WriteXML_toANSI42(m_EventData), m_EventData.EventData + "_"
								+ m_EventData.StartTime + "(" + m_EventData.mInstrument_Name + ").xml");
					} catch (Exception e1) {
						NcLibrary.Write_ExceptionLog(e1);
					}

					try {
						if (m.send()) {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(0);
							Log.v("Forgot Password mail", "Success");

						} else {
							mPrgDlg.dismiss();
							mHandler.sendEmptyMessage(1);
							Log.v("Forgot Password mail", "Not Success");
						}
					} catch (Exception e) {
						mPrgDlg.dismiss();
						mHandler.sendEmptyMessage(2);
						Log.e("MailApp", "Could not send email", e);
					}

				}

			};

			thread.start();

		} catch (Exception e) {

			NcLibrary.Write_ExceptionLog(e);
		}
	}

	public void CreateCsvFile(EventData event)
	{
		String[] mGamma1 = event.Doserate_AVGs.split(" ");
		String mDoserate = "";
		if (mGamma1[1].equals("uSv/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() * 100;
			mDoserate = String.format("%.6f", d);
		}

		if (mGamma1[1].equals("urem/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue();
			mDoserate = String.format("%.6f", d);
		}
		if (mGamma1[1].equals("mSv/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() * 1000000;
			mDoserate = String.format("%.6f", d);
		}

		if (mGamma1[1].equals("Sv/h")) {
			double d = Double.valueOf(mGamma1[0]).doubleValue() * 10000;
			mDoserate = String.format("%.6f", d);
		}

		String mGPS_Latitude = String.format("%.6f", event.GPS_Latitude);
		String mGPS_Longitude = String.format("%.6f", event.GPS_Longitude);
		String mDate = DateChange(event);

		//String[] Isotope = mId.get(pos1).split(",");
		//String[] Isotope = event.Detected_Isotope;
		String mIsotope = "";
		for (int i = 0; i <event.Detected_Isotope.size();i++) {

			mIsotope += event.Detected_Isotope.get(i) + " ";
		}

		String mCPM = String.valueOf((int) event.MS.Get_AvgCPS() * 60);

		String mComment = event.Comment;

		String mInstrument_Name = event.mInstrument_Name;

		String EventLogNumber = String.valueOf(event.Event_Number);
		String enc = new java.io.OutputStreamWriter(System.out).getEncoding();
		try {

			String head = " ,EventLogNumber, Serial number, DATE, Background_CPM, Latitude , Longitude, ISOTOPE, Avg. Dose Rate(uR/h) \r\n";

			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(GetCsvPath()), "MS949"));
			writer.write(head);

			String Row = "\r\n" + " " + "," + EventLogNumber + "," + mInstrument_Name + "," + mDate + ", " + mCPM + ", "
					+ mGPS_Latitude + "," + mGPS_Longitude + "," + mIsotope + "," + mDoserate;

			writer.write(Row);

			writer.close();

		} catch (Exception e) {
			NcLibrary.Write_ExceptionLog(e);
			// TODO: handle exception
		}

	}

	private String GetCsvPath() {

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EventDB.csv";

		return path;
	}

	private String DateChange(EventData event) {

		String mDateStr = event.EventData;
		String[] mDateArray = mDateStr.split("-");
		String mDateSub = mDateArray[2] + "/" + mDateArray[1] + "/" + mDateArray[0];

		return mDateSub;
	}

	public String CurrentDate() {
		Date date = new Date();
		Format formatter;
		formatter = new SimpleDateFormat("dd-MM-yyyy"); // d는 day 이런식
		String mDateStr = formatter.format(date);

		return mDateStr;
	}

	public void DBUpdate() {
		EventDBOper mEventDB = new EventDBOper();
		mEventDB.OpenDB();
		mAllLog = mEventDB.Load_ALL_Event();
		mEventDB.EndDB();
		mEventDB = null;

	}

	public void CreateN42File(EventData event) {

		NcLibrary.Event_XML.WriteXML_toANSI42(event);
	}

	//190529페이징 추가
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
		// 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
		// 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
		// 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
		Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다", Toast.LENGTH_LONG).show();
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false)
		{

			// 화면이 바닦에 닿을때 처리
			// 로딩중을 알리는 프로그레스바를 보인다.
				progressBar.setVisibility(View.VISIBLE);

			// 다음 데이터를 불러온다.

			getItem();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
		// visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
		// totalItemCount : 리스트 전체의 총 갯수
		// 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
		Toast.makeText(getApplicationContext(), "내용이 txt파일로 저장되었습니다1", Toast.LENGTH_LONG).show();
		lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

	}

	private void getItem()
	{
		// 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
		mLockListView = true;

		// 다음 20개의 데이터를 불러와서 리스트에 저장한다.
		currentPage++;
		//mLockListView = false;
		loadDB(currentPage);


			// 1초 뒤 프로그레스바를 감추고 데이터를 갱신하고, 중복 로딩 체크하는 Lock을 했던 mLockListView변수를 풀어준다.
			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					adt.notifyDataSetChanged();
					eventList.setSelection( (currentPage*OFFSET)-1);
					progressBar.setVisibility(View.GONE);
					mLockListView = false;
				}
			},200);

	}

}
