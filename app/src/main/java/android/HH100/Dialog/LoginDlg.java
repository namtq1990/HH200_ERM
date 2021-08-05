package android.HH100.Dialog;

import android.HH100.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginDlg extends Activity {
	public final static int LOGIN_USER = 552;
	public final static int LOGIN_ADMIN = 553;
	public final static String EXTRA_ADMIN_PW = "Admin_pw";
	
	private String mAdmin_Password = "1234";
	
	private LinearLayout m_MainLayout;
	private Button mBtn_User;
	private Button mBtn_Admin;
	private EditText mEdit_Password;
	public static Activity LoginfoTab;
	
	private void login() {
		// TODO Auto-generated method stub

	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		mAdmin_Password = getIntent().getStringExtra(EXTRA_ADMIN_PW);
		
		
		/////
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_MainLayout            =   (LinearLayout) inflater.inflate(R.layout.login, null);

		mEdit_Password = (EditText) m_MainLayout.findViewById(R.id.edit_Pass);
		mBtn_User = (Button) m_MainLayout.findViewById(R.id.btn_User);
		mBtn_User.setOnTouchListener((new Button.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					setResult(LOGIN_USER);
					finish();
					return true;
				}
				return false;
			}
		}));
		mBtn_Admin = (Button) m_MainLayout.findViewById(R.id.btn_Admin);
		mBtn_Admin.setOnTouchListener((new Button.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					
					if(mEdit_Password.getText().toString().matches(mAdmin_Password)){
						setResult(LOGIN_ADMIN);
						finish();
					}
					else  Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_SHORT).show();
												   				
					return true; 
				}
				return false;
			}
		}));
		setContentView(m_MainLayout);
		LoginfoTab = this;
	}
	 @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}
}
