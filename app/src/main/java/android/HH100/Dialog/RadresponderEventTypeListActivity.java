package android.HH100.Dialog;
import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.Radresponder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class RadresponderEventTypeListActivity extends Activity implements Serializable {
    RadresponderEventTypeAdapter adt;
    ArrayList<RadresponderAdapter3.Radresponder> list = new ArrayList<>();
    ArrayList<RadresponderAdapter3.Radresponder> tempList = new ArrayList<>();

    public String selectEvent = "";
    public String selectSponsor = "";
    ListView pairedListView;
    EditText select;
    ArrayList<Radresponder> sponsor; //서버에서 받은 리스트
    TextView txtSponsor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.radresponder4);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.7); //Display 사이즈의 70%
        int height = (int) (display.getHeight() * 0.9);  //Display 사이즈의 90%
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        Button btnSave =  (Button) findViewById(R.id.save);
        Button btnCancel =  (Button) findViewById(R.id.cancel);
        ImageView imgCancel = (ImageView)findViewById(R.id.imgCancel);
        TextView txtSponsor = (TextView)findViewById(R.id.sponsor);

        btnSave.setOnClickListener(btnClick);
        btnCancel.setOnClickListener(btnClick);
        imgCancel.setOnClickListener(btnClick);

        select =  (EditText) findViewById(R.id.select);
        select.setImeOptions(EditorInfo.IME_FLAG_NO_FULLSCREEN);
        String sponsorkey = MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_sponsor_key));
        txtSponsor.setText("sponsor name : "+sponsorkey);

        if(MainActivity.radresponderCheck){
            sponsor = MainActivity.radresponderList;
        }else{
            sponsor = (ArrayList<Radresponder>) getIntent().getSerializableExtra("sponsor");
            // MainActivity.radresponderList = (ArrayList<Radresponder>) getIntent().getSerializableExtra("sponsor");
        }

        //검색창
        select.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                String text = select.getText().toString().toLowerCase(Locale.getDefault());
                filter( tempList, text);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
        });


   /*    String[] name = getResources().getStringArray(R.array.list_pref_eventlist3);
        String[] sponsor = getResources().getStringArray(R.array.list_pref_eventlist3);*/
        ArrayList<String> temp = new  ArrayList<String>();
       for(int i = 0; i<sponsor.size(); i++){
               if(!temp.contains(sponsor.get(i).name) && sponsor.get(i).sponsorName.equals(sponsorkey)) {
                   temp.add(sponsor.get(i).name);
                   RadresponderAdapter3.Radresponder rad = new RadresponderAdapter3.Radresponder();
                   rad.name = sponsor.get(i).name;
                   rad.sponsor = sponsor.get(i).sponsorName;
                   list.add(rad);
               }
        }

        Log.e("ahn","onCreate");
        tempList =list;
        adt = new RadresponderEventTypeAdapter(RadresponderEventTypeListActivity.this, list);
        pairedListView =  (ListView) findViewById(R.id.radresponderList);
        pairedListView.setAdapter(adt);
        adt.setOnListener(cellClick);
    }

    //list cell clickListener
    private RadresponderEventTypeAdapter.clickListener cellClick = new RadresponderEventTypeAdapter.clickListener() {
        @Override
        public void onCellClick(int id, String name) {
            adt.miSelIndex = id;
            adt.notifyDataSetChanged();
            selectEvent = name;
            return;
        }
    };

    //터치방지
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);
        if(!dialogBounds.contains((int)event.getX(), (int)event.getY())){
            InputMethodManager keyboard = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            return false;
        }
        return super.dispatchTouchEvent(event);
    }
    //button cellClick Listener
    public View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.save:
                  // MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.r`ad_response_eventlist_key),selectEvent );
                    //20.01.06 rad_response_eventid_key로 수정 기존(0,1로만 저장)
                    if(adt.miSelIndex != -1){
                        if(selectEvent.equals("")){
                            selectEvent =   MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key));
                        }
                        MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.rad_response_eventid_key),selectEvent );
                        if(!MainActivity.radresponderCheck){
                            MainActivity.radresponderCheck = true;
                            MainActivity.radresponderList = sponsor;
                        }
                        Toast.makeText(RadresponderEventTypeListActivity.this, "saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                       // Toast.makeText(RadresponderSponsorListActivity.this, "Please select Sponsor Name\n(default value : Berkeley Nucleonics) ", Toast.LENGTH_SHORT).show();
                        Toast.makeText(RadresponderEventTypeListActivity.this, "Please select EventType ", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.cancel:
                    finish();
                    break;

                case R.id.imgCancel:
                    select.setText("");
                    break;
            }
        }
    };

    public void filter( ArrayList<RadresponderAdapter3.Radresponder> list23 , String charText) {
        //charText = charText.toLowerCase(Locale.getDefault());
        //list.clear();
        ArrayList<RadresponderAdapter3.Radresponder> list4 = new ArrayList<>();
        if (charText.length() == 0) {
            //list.addAll(radresponderList);
            list4 = list23;
        } else {
            for (int i = 0; i<list23.size(); i++) {
                if (list23.get(i).name.toLowerCase().contains(charText)) {
                    RadresponderAdapter3.Radresponder rad = new RadresponderAdapter3.Radresponder();
                    rad.name = list23.get(i).name;
                    rad.sponsor =list23.get(i).sponsor;
                    list4.add(rad);
                }
            }
        }
        //list = list4;
       // this.radresponderList =radresponderList;
        adt.list = list4;
        adt.miSelIndex = -1;
        adt.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
