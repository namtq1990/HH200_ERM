package android.HH100;

import java.util.regex.*;

import android.HH100.*;
import android.HH100.DB.*;
import android.HH100.R.id;
import android.HH100.R.layout;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class TextDlg extends Activity {
	private EditText edit;
	private Button SaveBtn;
	private Pattern drawTextSanitizerFilter = Pattern.compile("[\t\n],");
	public TextDlg() {
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stubeditText1
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout m_MainLayout =   (LinearLayout) inflater.inflate(R.layout.comment_dlg, null);
		
		edit = (EditText) m_MainLayout.findViewById(R.id.Edit1);
		if(EventLogActivity.mComment != "No comment") edit.setText(EventLogActivity.mComment);
		
		SaveBtn = (Button) m_MainLayout.findViewById(R.id.SaveBtn);
		SaveBtn.setOnTouchListener((new Button.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					
					EventLogActivity.mComment = edit.getText().toString();
					EventDBOper mEventDB;
					mEventDB = new EventDBOper();
			        //mEventDB.start();
			        if( EventLogActivity.mComment != "") mEventDB.Set_Comment(EventLogActivity.mComment, EventListActivity.mSelPositioin);
			        mEventDB.EndDB();
			        //mEventDB.stop();
			        mEventDB = null;
			    
					finish();
					return true; 
				}
				return false;
			}
		}));
		setContentView(m_MainLayout);
	}
	private String drawTextSanitizer(String string) {
		  Matcher m = drawTextSanitizerFilter.matcher(string);
		  string = m.replaceAll(",").replace('\n', ' ').replace('\n', ' ');
		  return string;
	}
}
