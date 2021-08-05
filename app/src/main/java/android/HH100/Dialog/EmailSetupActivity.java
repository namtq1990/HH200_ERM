package android.HH100.Dialog;

import android.HH100.DB.PreferenceDB;
import android.HH100.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class EmailSetupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.email_setup);
		
		
		PreferenceDB prefDB = new PreferenceDB(EmailSetupActivity.this);
		((EditText)findViewById(R.id.et_send_address)).setText(prefDB.Get_sender_email());
		((EditText)findViewById(R.id.et_send_pw)).setText(prefDB.Get_sender_pw());
		((EditText)findViewById(R.id.et_send_server)).setText(prefDB.Get_sender_Server());
		((EditText)findViewById(R.id.et_send_port)).setText(prefDB.Get_sender_Port());
		((EditText)findViewById(R.id.et_recv_address)).setText(prefDB.Get_recv_email());
		
		
		
		Button btn_ok = (Button) findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PreferenceDB prefDB = new PreferenceDB(EmailSetupActivity.this);
				
				try{
					String sender_addr = ((EditText)findViewById(R.id.et_send_address)).getText().toString();
					if(sender_addr.isEmpty()) throw new Exception();
					
					String sender_pw = ((EditText)findViewById(R.id.et_send_pw)).getText().toString();
					if(sender_pw.isEmpty())throw new Exception();
					
					String sender_sv = ((EditText)findViewById(R.id.et_send_server)).getText().toString();
					if(sender_sv.isEmpty())throw new Exception();
					
					String sender_port = ((EditText)findViewById(R.id.et_send_port)).getText().toString();
					if(sender_port.isEmpty())throw new Exception();
					
					String recv_addr = ((EditText)findViewById(R.id.et_recv_address)).getText().toString();
					if(recv_addr.isEmpty())throw new Exception();
					
					prefDB.Set_sender_Server(sender_sv);
					prefDB.Set_sender_Port(sender_port);
					prefDB.Set_sender_pw(sender_pw);
					prefDB.Set_sender_email(sender_addr);					
					prefDB.Set_recv_address(recv_addr);
					
					finish();
				}catch(Exception e){
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EmailSetupActivity.this);
			   		dialogBuilder.setMessage(getResources().getString(R.string.plz_input_all_info));
			   		dialogBuilder.setNegativeButton("OK", null);
			   		dialogBuilder.setCancelable(false);
			   		dialogBuilder.show();
				}
				
				
			}
		});
	}
	
}
