package android.HH100.LogActivity;

import android.HH100.EventListActivity;
import android.HH100.EventLogActivity;
import android.HH100.R;
import android.HH100.DB.EventDBOper;
import android.HH100.R.string;
import android.HH100.Structure.EventData;
import android.HH100.Structure.NcLibrary;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LogInfoTab extends Activity {
	public final static String EXTRA_EVENT_DATA = "EventData";
	EventData mEventData = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_log_info);

		mEventData = (EventData) getIntent().getSerializableExtra(EXTRA_EVENT_DATA);
		Upate_View(mEventData);

		final EditText comment = ((EditText) findViewById(R.id.editT_Comment));
		comment.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				switch (arg1) {
					case EditorInfo.IME_ACTION_DONE:
						if (mEventData == null)
							break;
						EventDBOper mEventDB;
						mEventDB = new EventDBOper();

						if( comment.getText().toString() == null || comment.getText().toString().length() == 0)
						{
							mEventDB.Set_Comment("", mEventData.Event_Number);
						}
						else
						{
							String tem = comment.getText().toString();
							tem = tem.replace("\"", "'");
							tem = tem.replace("'", "''");
							mEventDB.Set_Comment(tem, mEventData.Event_Number);
						}

						mEventDB = null;

						break;

					default:

						return false;

				}
				return false;
			}

		});


	}

	private void Upate_View(EventData EventData) {
		((TextView) findViewById(R.id.LogInfoTab_n1st_row_2)).setText("#" + EventData.Event_Number);
		((TextView) findViewById(R.id.LogInfoTab_n1st_row_4)).setText(EventData.EventData);

		((TextView) findViewById(R.id.LogInfoTab_n2nd_row_2)).setText(EventData.StartTime);
		((TextView) findViewById(R.id.LogInfoTab_n2nd_row_4))
				.setText(String.valueOf(EventData.MS.Get_AcqTime()) + " Sec");

		((TextView) findViewById(R.id.LogInfoTab_n3rd_row_2)).setText(EventData.mUser);
		((TextView) findViewById(R.id.LogInfoTab_n3rd_row_4)).setText(EventData.mLocation);

		((TextView) findViewById(R.id.LogInfoTab_n4th_row_2)).setText(EventData.Doserate_AVGs);
		((TextView) findViewById(R.id.LogInfoTab_n4th_row_4)).setText(EventData.Neutron_AVGs);

		((TextView) findViewById(R.id.LogInfoTab_n5th_row_2)).setText(EventData.Doserate_MAXs);
		((TextView) findViewById(R.id.LogInfoTab_n5th_row_4)).setText(EventData.Neutron_MAXs);

		((TextView) findViewById(R.id.LogInfoTab_n6th_row_2)).setText(String.format("%.5f", EventData.GPS_Latitude));
		((TextView) findViewById(R.id.LogInfoTab_n6th_row_4)).setText(String.format("%.5f", EventData.GPS_Longitude));

		((EditText) findViewById(R.id.editT_Comment)).setText(EventData.Comment);
		// ((TextView)findViewById(R.id.LogInfoTab_ManualID)).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.LogInfoTab_ManualID)).setText(EventData.Event_Detector);

	}

}
