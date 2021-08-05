package android.HH100;

import android.HH100.DB.PreferenceDB;
import android.HH100.Structure.NcLibrary;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import Debug.Log_Setting;


//import org.apache.http.client.methods.HttpPost;

//PostDataRun
//onPageFinished1
//FindAccessToken
public class RadresponderActivity extends Activity
{
	/**
	 * Created by inseon.ahn on 2018-11-28.
	 */
	public class Radresponder
	{
		public int id = 0;
		public String name = "";
		public String sponsorName ="";
		public String startDate = "";
		public String isActive ="";

	}


	public final static String CLIENT_ID = "25c7f2b4-ea90-4177-b333-73524920da3e";
	public final static String CLIENT_SECRET = "Ix7kf9aCMIyvqPnfw2uuN408GJnF9Pc4aR7anhwH";
	public final static String REQUEST_TOKEN_URL = "https://api.radresponder.net/oauth/request_token";
	public final static String AUTHORIZE_URL = "https://api.radresponder.net/oauth/authorize";
	public final static String ACCESS_TOKEN_URL = "https://api.radresponder.net/oauth/token";
	public final static String REDIRECT_URI = "http://localhost:51029/implicitgrant/callback";
	public final static String SCOPE = "create_field_survey get_accessible_events create_spectrum";
	public final static String STATE = "my-nonce";
	public final static String POST_DATA_URL = "https://api.radresponder.net/api/v2/fieldsurvey";
	public final static String POST_DATA_HEADER = "Authorization";
	public final static String POST_DATA_METHOD = "Bearer ";
	public final static String COMMUNICATIONS_METHOD = "application/json";
	public final static String COMMUNICATIONS_ENCODE = "UTF-8";
	public static final String url = AUTHORIZE_URL + "?scope=" + SCOPE + "&state=" + STATE + "&redirect_uri="
			+ REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID;
	public final static String EVENTID_TITLE = "eventId";
	public static final String GPS_LAT = "GPS_Latitude";
	public static final String GPS_LONG = "GPS_Longitude";
	public static final String DOSERATE_TYPE = "Doserate_AVGs";
	public static final String COLLECTION_DATE = "Date";
	public static final String COLLECTDATE_TITLE = "collectionDate";
	public static final String LATITUDE_TITLE = "latitude";
	public static final String LONGITUDE_TITLE = "longitude";
	public static final String COORDINATE_TITLE = "coordinate";
	public static final String RADIATIONTYPE_TITLE = "radiationType";
	public static final String RADIATIONUNIT_TITLE = "radiationUnit";
	public static final String VALUE_TITLE = "value";
	public static final String HEIGHTUNIT_TITLE = "heightUnit";
	public static final String HEIGHT_TITLE = "height";
	public static final String ORIENTATION_TITLE = "orientation";
	public static final String ISWINDOWOPEN_TITLE = "isWindowOpen";
	public static final String COMMENT_TITLE = "comment";
	public static final String FIELDSURVEYS_TITLE = "fieldSurveys";

	public static final String RADIATIONTYPE_SUBSTANCE = "gamma";
	public static final String RADIATIONUNIT_SUBSTANCE = "uR/h";
	public static final String HEIGHTUNIT_SUBSTANCE = "meter";
	public static final String ORIENTATION_SUBSTANCE = "Up";
	public static final String ISWINDOWOPEN_SUBSTANCE = "yes";
	public static String COMMENT_SUBSTANCE = "Test Survey";

	public static final String STARTTIME_TITLE = "startTime";
	public static final String STOPTIME_TITLE = "stopTime";
	public static final String DWELLTIME_TITLE = "dwellTime";
	public static final String ISBACKGROUND_TITLE = "isBackground";

	public static final String SERIALNUMBER_TITLE = "serialNumber";

	public static final String EQUIPMENT_TITLE = "equipment";

	public static final String NUCLIDETYPE_TITLE = "nuclideType";

	public static final String CONFIDENCE_TITLE = "confidence";

	public static final String DATAFILE_TITLE = "dataFile";

	public static final String DATA_TITLE = "data";

	public static final String NAME_TITLE = "tvName";

	public static final String ISOTOPES_TITLE = "isotopes";

	public static final String START_TIME = "Start_Time";
	public static final String END_TIME = "End_Time";

	public static final String START_TIME_NOT_UTC = "Start_Time_not_utc";

	public static final String STOP_TIME_NOT_UTC = "Stop_Time_not_utc";

	public static final String LEVEL_S = "Level_S";
	public static final String SOURCE_NAME_S = "Source_Name_S";
	public static final String DOSERATE_S = "DoseRate_S";
	public static final String DOSERATE_UNIT = "DoseRate_Unit";

	public static final int HEIGHT_SUBSTANCE = 1;

	public static final String METER_TITLE = "meter";
	public static final String PROBE_TITLE = "probe";
	public static final String EXPOSURES_TITLE = "exposures";
	public static final String SPECTRA_TITLE = "spectra";

	public static String mEmergency = "0";
	public static String mTestingTraining = "1";

	WebView mWebView;

	String access_TokenKey, GUrl, receiveData;

	int switchInt = 0;

	//PostDataThread PostDataThread;
	GetDataThread GetDataThread;

	Context mContext;
	String response;
	ProgressDialog mPrgDlg;
	boolean submitBtnClick = true, TransportSuccess = false, runOneClick = false;

	ArrayList<String> IDArray = new ArrayList<String>();
	ArrayList<String> nameArray = new ArrayList<String>();

	String eventID = null;
	String Encode64Str;

	Log_Setting mLog = new Log_Setting();

	PreferenceDB prefDB;
	String name ="";
	String sponsor ="";
	boolean check = false;

	ArrayList<Radresponder> rad = new ArrayList<Radresponder>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);



		setContentView(R.layout.radresponder_auth);

		setLayout();

		mContext = this;

		//boolean wificheck1 = wificheck();
/*
		if (NcLibrary.isNetworkOnline(mContext)) {
			Toast.makeText(mContext, getString(R.string.rad_response_wifi_disconnect), Toast.LENGTH_SHORT).show();
			finish();
		}
*/

		//PostDataThread = new PostDataThread();
		//GetDataThread = new GetDataThread();

		mPrgDlg = new ProgressDialog(mContext);
		mPrgDlg.setIndeterminate(true);
		mPrgDlg.setCancelable(false);

		prefDB = new PreferenceDB(getApplicationContext());

		String abcd = prefDB.Get_String_From_pref(getString(R.string.rad_response_used_not_key));
		//String abcde = getString(R.string.rad_response_eventlist_key);
		//20.01.06 rad_response_eventid_key로 수정 기존(0,1로만 저장)
		String abc = getString(R.string.rad_response_eventid_key);

		//String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventlist_key));
		if (abc == null) {
			abc = "0";
		}

		//name = abc.equals(mEmergency) ? getString(R.string.rad_response_eventlist11)  : abc;
		name =  prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key)) == null ? getString(R.string.rad_response_eventlist11) : prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key) );
		sponsor =  prefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key))== null ? getString(R.string.radresponder_sponsor2)  : prefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key));
/*		abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key));
		if (abc == null) {
			abc = "0";
		}
		sponsor = abc.equals(mEmergency) ? getString(R.string.radresponder_sponsor2)  : abc;*/

		mWebView.setVisibility(View.VISIBLE);

		//191022 usim internet 추가
		if (!NcLibrary.isNetworkOnline(mContext)) {
			Toast.makeText(mContext, getString(R.string.internet_not), Toast.LENGTH_SHORT).show();
			finish();
		}
		else {
			tumblrWebView(AUTHORIZE_URL + "?scope=" + SCOPE + "&state=" + STATE + "&redirect_uri=" + REDIRECT_URI+ "&response_type=token&client_id=" + CLIENT_ID);
		}


		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Event.xml";



		//https://api.radresponder.net/oauth/authorize?scope=create_field_survey%20get_accessible_events%20create_spectrum&state=my-nonce&redirect_uri=http://localhost:51029/implicitgrant/callback&response_type=token&client_id=25c7f2b4-ea90-4177-b333-73524920da3e
		byte[] EventByte = FileToByte(path);
		Encode64Str = Encodebyte(EventByte);

	}

	private class WebViewClientClass extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{

			view.loadUrl(url);
			return true;
		}

		public void onPageFinished(WebView view, String url)
		{

			if (submitBtnClick)
			{


				Uri uri = Uri.parse(url);
				GUrl = url;
				String abc = null;
				System.out.println(GUrl);

				if (url.toString().contains("access_token"))
				{


					mWebView.setVisibility(View.GONE);

					// FindAccessToken
					if (url.contains("#"))
					{
						String[] aa = url.split("#");
						String[] ab = aa[1].split("=");
						String[] ac = ab[1].split("&");
						access_TokenKey = ac[0];
					}

					//GetDataThread.start();
					//	handler.sendEmptyMessage(3);
/*
					((Activity) mContext).runOnUiThread(new Runnable()
					{
						public void run()
						{
							if(mPrgDlg.isShowing())
							{
								mPrgDlg.cancel();
							}
							mPrgDlg.setMessage(getString(R.string.rad_response_data_transmitting));
							mPrgDlg.show();
						}
					});
*/

					if(mPrgDlg.isShowing())
					{
						mPrgDlg.cancel();
					}
					mPrgDlg.setMessage(getString(R.string.rad_response_data_transmitting));
					mPrgDlg.show();


					GetHttp thread = new GetHttp();
					thread.start();
					submitBtnClick = false;

					//decreaseBar();



				}
			}

			super.onPageFinished(view, url);
		}

	}

	private void setLayout() {

		mWebView = (WebView) findViewById(R.id.webview);
	}

	public void tumblrWebView(String authUrl) {

		try {
			URLEncoder.encode(authUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(authUrl);
		mWebView.setWebViewClient(new WebViewClientClass());

	}


	Handler handler = new Handler();
	class GetHttp extends Thread
	{

		@Override
		public void run()
		{

			try
			{
				URL url = new URL("https://api.radresponder.net/api/v2/event");
				HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty("Authorization", "Bearer "+access_TokenKey);
				httpURLConnection.setRequestProperty("Accept", "application/json");
				httpURLConnection.setConnectTimeout(10000); // 연결 타임아웃 설정
				httpURLConnection.setReadTimeout(10000);	 // 읽기 타임아웃 설정
				InputStream inputStream;
				JSONArray jsonArray;
				if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					inputStream = httpURLConnection.getInputStream();
					StringBuilder sb = new StringBuilder();
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
					String result;
					while((result = br.readLine())!=null)
					{
						sb.append(result);
					}

					String a = "";
					jsonArray = new JSONArray(sb.toString());
					for(int i = 0; i < jsonArray.length(); i++)
					{
						Radresponder Item = new Radresponder();
						JSONObject jsonobject = jsonArray.getJSONObject(i);
						Item.id       = jsonobject.getInt("id");
						Item.name    = jsonobject.getString("name");
						Item.sponsorName  = jsonobject.getString("sponsorName");
						Item.startDate = jsonobject.getString("startDate");
						//	Item.isActive = jsonobject.getString("isActive");

						rad.add(Item);
						//a = a+ "tvName : "+Item.tvName+", sponsorName : "+Item.sponsorName+"\n";
					}

/*
					for(int i = 0; i < rad.size(); i++) {
						a = a+ i +", tvName : "+rad.get(i).name+", sponsorName : "+rad.get(i).sponsorName+"\n";
					}
					Log.e("ahn",a);*/
					httpURLConnection.disconnect();
					//PostDataThread.start();
					PostHttp th = new PostHttp();
					th.start();
				}
				else
				{
					inputStream = httpURLConnection.getErrorStream();
				}


			} catch (Exception e) {
				mPrgDlg.dismiss();

				((Activity) mContext).runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(mContext, " error : "+e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});

				finish();

			}

		}

	}

	class PostHttp extends Thread
	{
		@SuppressWarnings("deprecation")
		public String GetGMT() {

			Calendar mCurrentCalendar = Calendar.getInstance();
			long ttl2 = mCurrentCalendar.getTimeInMillis();

			Date today = null;

			String s = "";
			String mGmtStr = "00";
			if (mLog.D) {

				today = new Date(ttl2);
				Log.i(mLog.Radresponder, mLog.Radresponder + " today.getHours(Original) " + today.toString());
			} else {
				today = new Date(ttl2);
			}

			try {

				//	String mToday = today.toGMTString();
				int mGmtInt = today.getTimezoneOffset();

				int mHour = (int) (mGmtInt / 60) * -1;
				mGmtStr = String.format("%02d", mHour);
				if (mGmtInt == 0 && mHour==0 )
				{
					mGmtStr = "+00";
				}

				else
				{
					if(mHour>0)
					{
						mGmtStr = "+" + mGmtStr;
					}else {
						mGmtStr = String.format("%03d", mHour);
					}

				}

//				String[] S = today.toString().split("GMT");
//				String[] S2 = S[1].split(":");
//				s = S2[0];
			} catch (ArrayIndexOutOfBoundsException e) {
//				handler.obtainMessage(12, getResources().getString(R.string.Radresponder_GMT_Error_Msg)).sendToTarget();
				// TODO: handle exception
			}
			if (mLog.D) {

				Log.i(mLog.Radresponder, mLog.Radresponder + " TimeDiffernt " + s);
			}
			return mGmtStr + "00";

		}

		@Override
		public void run()
		{
			try {
				URL url = new URL("https://api.radresponder.net/api/v2/spectra");
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
				httpURLConnection.setRequestProperty("Content-Type", "application/json");
				httpURLConnection.setRequestProperty("Accept", "application/json");
				httpURLConnection.setRequestProperty("Authorization", "Bearer " + access_TokenKey);
				httpURLConnection.setConnectTimeout(10000); // 연결 타임아웃 설정
				httpURLConnection.setReadTimeout(10000);     // 읽기 타임아웃 설정
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setDoInput(true);

				//
				int EVENTID_SUBSTANCE = 0;
				for (int i = 0; i < rad.size(); i++) {
/*					if(name.equals(rad.get(i).name)&&rad.get(i).sponsorName.equals("Berkeley Nucleonics")) {
						EVENTID_SUBSTANCE = rad.get(i).id;
						break;
					}*/

					if (name.equals(rad.get(i).name) && rad.get(i).sponsorName.equals(sponsor)) {
						EVENTID_SUBSTANCE = rad.get(i).id;
						check = true;
						break;
					}
				}

				if (check) {

					Intent intent = getIntent();

					String GPS_Latitude = intent.getStringExtra(GPS_LAT);
					String GPS_Longitude = intent.getStringExtra(GPS_LONG);
					String Doserate_AVGs = intent.getStringExtra(DOSERATE_TYPE);
					String Date = intent.getStringExtra(COLLECTION_DATE);

					//191023 변경 GetGMT();
					Calendar calendar21 = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
					String timeZone = new SimpleDateFormat("Z").format(calendar21.getTime());
					String gmt = timeZone.substring(0, 3) + ":" + timeZone.substring(3, 5);

/*
				TimeZone tz = TimeZone.getDefault();
				Calendar cal = GregorianCalendar.getInstance(tz);
				int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
				String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
				offset = "GMT"+(offsetInMillis >= 0 ? "+" : "-") + offset;
*/
					String Start_Time = intent.getStringExtra(START_TIME) + gmt;
					String End_Time = intent.getStringExtra(END_TIME) + gmt;
					COMMENT_SUBSTANCE = intent.getStringExtra(COMMENT_TITLE);
					String[] Source_Names = intent.getStringExtra(SOURCE_NAME_S).split(",");
					String[] Doserate_S = intent.getStringExtra(DOSERATE_S).split(",");
					String[] Doserate_Unit = intent.getStringExtra(DOSERATE_UNIT).split(",");

					if (Doserate_Unit[0].equals("uSv/h")) {
						Doserate_S = ConverterToUrem(Doserate_S);
					}

					String[] Level_S = intent.getStringExtra(LEVEL_S).split(",");
					//System.out.println("Doserate_AVGs :" + Doserate_AVGs);
					JSONObject spectra = new JSONObject();
					JSONObject item = new JSONObject();
					JSONObject coordinateArray = new JSONObject();
					JSONObject dataFileArray = new JSONObject();
					JSONArray spectraArray = new JSONArray();
					JSONArray isotopeArray = new JSONArray();
					JSONArray exposuresArray = new JSONArray();

					item.put("eventId", EVENTID_SUBSTANCE);
					item.put("collectionDate", Date + " " + Start_Time);
					coordinateArray.put("latitude", GPS_Latitude == null ? "" : GPS_Latitude);
					coordinateArray.put("longitude", GPS_Longitude == null ? "" : GPS_Longitude);
					item.put("coordinate", coordinateArray);
					item.put("heightUnit", "meter");
					item.put("height", 1);
					item.put("dwellTime", 460);
					item.put("isBackground", false);
					//item.put("comment", COMMENT_SUBSTANCE);
					item.put("comment", COMMENT_SUBSTANCE);
					dataFileArray.put("data", Encode64Str);
					dataFileArray.put("name", "data.xml");
					item.put("dataFile", dataFileArray);
					item.put("startTime", Date + " " + Start_Time);
					item.put("stopTime", Date + " " + End_Time);

					if (!Source_Names[0].equals("None")) {
						for (int i = 0; i < Source_Names.length; i++) {
							if (!Source_Names[i].equals("Unknown")) {
								JSONObject isotope = new JSONObject();
								isotope.put("nuclideType", Source_Names[i]);
								isotope.put("confidence", Level_S[i]);
								isotopeArray.put(isotope);
							}
						}
					}
					item.put("isotopes", isotopeArray);
					if (!Source_Names[0].equals("None") && !Source_Names[0].equals("")) {
						for (int i = 0; i < Source_Names.length; i++) {
							if (!Source_Names[i].equals("Unknown")) {
								JSONObject exposures = new JSONObject();
								JSONObject meter_sub = new JSONObject();
								JSONObject probe_sub = new JSONObject();
								exposures.put("radiationType", RADIATIONTYPE_SUBSTANCE);
								exposures.put("radiationUnit", RADIATIONUNIT_SUBSTANCE);
								//	exposures.put("value", Double.valueOf(Doserate_S[i]).doubleValue());
								exposures.put("value", Doserate_S[i]);
								exposures.put("heightUnit", "in");
								exposures.put("height", 3);
								exposures.put("orientation", ORIENTATION_SUBSTANCE);
								exposures.put("comment", "");
								exposuresArray.put(exposures);
							}
						}
					}
					item.put("exposures", exposuresArray);
					spectraArray.put(item);
					spectra.put("spectra", spectraArray);

					OutputStream outputStream = httpURLConnection.getOutputStream();
					outputStream.write(spectra.toString().getBytes());
					outputStream.flush();
					outputStream.close();

					if (mPrgDlg.isShowing())
						mPrgDlg.dismiss();

					String inputStream;

					if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						inputStream = httpURLConnection.getResponseMessage();
						((Activity) mContext).runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(mContext, getString(R.string.rad_response_submit_success) + " ", Toast.LENGTH_LONG).show();
							}
						});

						finish();
					} else {
						final String error = httpURLConnection.getResponseMessage();
						((Activity) mContext).runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(mContext, getString(R.string.rad_response_submit_failure) + " error : " + error, Toast.LENGTH_LONG).show();
							}
						});
						finish();
					}
				}
				else{

					if (mPrgDlg.isShowing())
						mPrgDlg.dismiss();

					((Activity) mContext).runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(mContext, getString(R.string.rad_response_submit_failure) + " : Please check the Event ID and try again. \n(Setup -> Administrator -> RadResponder Mode -> Event ID)", Toast.LENGTH_LONG).show();
						}
					});
					finish();
				}
			}
			catch (Exception e) {
				mPrgDlg.dismiss();

				((Activity) mContext).runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(mContext, " error : "+e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});

				finish();
			}

		}

	}

	public class GetDataThread extends Thread {

		private static final String TAG = "ExampleThread2";
		private int n1 = 0;
		private int n2 = 0;

		String abcd;

		public void run() {

			GET("");

		}

		public String GET(String url) {

			InputStream inputStream = null;
			String result = "";

			HttpClient client = new HttpClient();

			// Create a method instance.
			GetMethod method = new GetMethod("https://api.radresponder.net/api/v2/event");

			//PostMethod method
			System.out.println(method);
			method.setRequestHeader("Authorization", "Bearer " + access_TokenKey);
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			System.out.println(method);

			// Provide custom retry handler is necessary

			try {
				// Execute the method.
				int statusCode = client.executeMethod(method);

				if (statusCode == HttpStatus.SC_OK)
				{
					// Read the response body.
					byte[] responseBody = method.getResponseBody();
					String mResponseBodyStr;
					System.out.println(new String(responseBody));
					receiveData = new String(responseBody);

					if (receiveData.contains("name") || receiveData.contains("sponsorName") || receiveData.contains("id")
							|| receiveData.contains("endDate") || receiveData.contains("startDate"))
					{
						TransportSuccess = true;

						//String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventlist_key));
						//20.01.06 rad_response_eventid_key로 수정 기존(0,1로만 저장)
						String abc = prefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key));

						if (abc == null)
						{
							abc = "0";
						}

						if (abc.equals(mEmergency))
						{
							eventID = split(receiveData, getString(R.string.rad_response_eventlist11));
						}
						else if (abc.equals(mTestingTraining))
						{
							eventID = split(receiveData, getString(R.string.rad_response_eventlist22));
						}

						//PostDataThread.start();
						GetDataThread.interrupt();
					}
					else
					{
						TransportSuccess = false;
						((Activity) mContext).runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(mContext, getString(R.string.rad_response_submit_failure)+" ", Toast.LENGTH_SHORT).show();
							}
						});
						GetDataThread.interrupt();
						finish();
					}

				}
				else
				{
					((Activity) mContext).runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(mContext, getString(R.string.rad_response_submit_failure), Toast.LENGTH_SHORT).show();
						}
					});
					GetDataThread.interrupt();
					finish();
				}


			} catch (HttpException e) {
				System.err.println("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Fatal transport error: " + e.getMessage());
				e.printStackTrace();
			} finally {
				// Release the connection.
				method.releaseConnection();
			}

			return result;
		}

		// convert inputstream to String
		private String convertInputStreamToString(InputStream inputStream) throws IOException {

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = "";
			String result = "";
			while ((line = bufferedReader.readLine()) != null)
				result += line;

			inputStream.close();
			return result;

		}

	}

	private String split(String data, String word) {

		String result = "";
		String[] TotalLengthArray;
		String[] firstSplitArray;
		String[] firstSplitArray2;

		firstSplitArray = data.split(word);
		String[] IDSplit = null;

		IDSplit = firstSplitArray[0].split("id");
		String IDSplit2 = null;
		IDSplit2 = IDSplit[IDSplit.length - 1];

		String[] IDSplit3 = null;
		IDSplit3 = IDSplit2.split(":");

		String[] IDSplit4 = null;

		IDSplit4 = IDSplit3[1].split(",");

		String IDSplit5 = null;

		IDSplit5 = IDSplit4[0];

		return IDSplit5;
	}


	private boolean wificheck() {
		boolean bIsWiFiConnect = false;
		ConnectivityManager oManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo oInfo = oManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (oInfo != null && oInfo.isAvailable() && oInfo.isConnected())
			return true;
		return false;

	}

	public String Encodebyte(byte[] byteArray) {

		String fileStr = new String(Base64.encodeBase64(byteArray));

		return fileStr;

	}

	public byte[] FileToByte(String path) {

		File fileName = new File(path);
		byte[] b = null;

		RandomAccessFile f;
		try {
			f = new RandomAccessFile(fileName, "r");
			b = new byte[(int) f.length()];
			f.read(b);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}

	public String[] ConverterToUrem(String[] OriginalArray) {

		// double sum;

		for (int i = 0; i < OriginalArray.length; i++) {
			double sum = Double.valueOf(OriginalArray[i]).doubleValue();
			sum = sum * 100;
			OriginalArray[i] = Double.toString(sum);

		}
		return OriginalArray;
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mPrgDlg.isShowing())
		{
			mPrgDlg.dismiss();
		}


	}

}
