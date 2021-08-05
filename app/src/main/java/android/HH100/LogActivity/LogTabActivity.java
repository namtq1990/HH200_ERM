package android.HH100.LogActivity;

import java.io.File;
import java.util.Vector;

import android.HH100.EventListActivity;
import android.HH100.EventLogActivity;
import android.HH100.R;
import android.HH100.TextDlg;
import android.HH100.DB.EventDBOper;
import android.HH100.DB.PreferenceDB;
import android.HH100.Identification.FindPeaksM;
import android.HH100.Identification.Isotope;
import android.HH100.Identification.IsotopesLibrary;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcPeak;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.Toast;

public class LogTabActivity extends TabActivity implements OnTabChangeListener {
	public static String EXTRA_EVENT_DATA = "EventLogData";
	EventData mEventLog = new EventData();
	TabHost tabs;

	public static Activity LogTabActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.event_log);

		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		layoutParams.screenBrightness = (float) 1.0;
		getWindow().setAttributes(layoutParams);
		// Hold Screen rotate


		int id  = getIntent().getIntExtra("_id",0);
		EventDBOper mEventDB = new EventDBOper();
		mEventDB.OpenDB();
		mEventLog = mEventDB.LoadEventDB(id);

		IsotopesLibrary isolib = new IsotopesLibrary(this);
		PreferenceDB mPrefDB = new PreferenceDB(this);
		isolib.Set_LibraryName(mPrefDB.Get_Selected_IsoLibName());

		if(mEventLog.Detected_Isotope !=null)
		{
			for (int i = 0; i < mEventLog.Detected_Isotope.size(); i++) {
				mEventLog.Detected_Isotope.get(i).HelpVideo = isolib.Get_Isotope(mEventLog.Detected_Isotope.get(i).isotopes).HelpVideo;
			}
		}

		SetupTabs();
		LogTabActivity = this;


	}

	private void SetupTabs() {

		// TODO Auto-generated method stub
		tabs = getTabHost();
		TabHost.TabSpec spec = null;
		Intent intent = null;
		TabWidget tabW = tabs.getTabWidget();


		spec = tabs.newTabSpec("tab1");
		intent = new Intent(this, EventLogActivity.class);
		intent.putExtra("mode", "new");
		intent.putExtra("initialize", true);
		intent.putExtra("request", true);
		intent.putExtra(EventLogActivity.EXTRA_EVENT_DATA, mEventLog);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		spec.setContent(intent);

		spec.setIndicator(getResources().getString(R.string.event_id));
		tabs.addTab(spec);

		spec = tabs.newTabSpec("tab2");
		intent = new Intent(this, LogInfoTab.class);
		intent.putExtra("mode", "new");
		intent.putExtra("initialize", true);
		intent.putExtra("request", true);
		intent.putExtra(LogInfoTab.EXTRA_EVENT_DATA, mEventLog);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		spec.setContent(intent);

		spec.setIndicator(getResources().getString(R.string.event_info));
		tabs.addTab(spec);
		////
		spec = tabs.newTabSpec("tab3");
		intent = new Intent(this, LogPhotoTab.class);
		intent.putExtra("mode", "new");
		intent.putExtra("initialize", true);
		intent.putExtra("request", true);
		intent.putExtra(LogPhotoTab.EXTRA_PHOTO_FILE_NAME, mEventLog.PhotoFileName1);

		if (mEventLog.Doserate_unit == -1) {
			intent.putExtra(LogPhotoTab.EXTRA_EVENT_NUMBER, -1);
		} else {
			intent.putExtra(LogPhotoTab.EXTRA_EVENT_NUMBER, mEventLog.Event_Number);
		}

		// intent.putExtra(LogPhotoTab.VIDEO_FILE_NAME, value);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		spec.setContent(intent);
		spec.setIndicator(getResources().getString(R.string.photo_video));
		tabs.addTab(spec);

		tabs.setCurrentTab(0);

		for (int tab = 0; tab < tabs.getTabWidget().getChildCount(); ++tab) {

			tabs.getTabWidget().getChildAt(tab).getLayoutParams().height = 70;

		}

		tabs.setOnTabChangedListener(this);

	}

	// event log focus ADD - Start
	public void onTabChanged(String tabId) {
		String strMsg;
		strMsg = tabId;
		// tabs.getTabWidget().getChildAt(1).requestFocus();

		// Log.d("time:", "tab2 : " + tabId);
		if (tabId.equals("tab2")) {
			tabs.getTabWidget().getChildAt(1).requestFocus();
			// Toast.makeText(getApplicationContext(), "�� ����", 1).show();
		}

	}
// event log focus ADD - End
}
