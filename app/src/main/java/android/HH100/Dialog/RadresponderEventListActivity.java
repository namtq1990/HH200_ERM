package android.HH100.Dialog;
import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.RadresponderActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RadresponderEventListActivity extends Activity {
    private RadresponderAdapter3 adt;
    private ArrayList<RadresponderAdapter3.Radresponder> list = new ArrayList<>();
    String selectEvent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.radresponder);
        adt = new RadresponderAdapter3(RadresponderEventListActivity.this, list);

        Button btnNext =  (Button) findViewById(R.id.next);
        Button btnCancel =  (Button) findViewById(R.id.cancel);

        btnNext.setOnClickListener(btnClick);
        btnCancel.setOnClickListener(btnClick);

        ListView pairedListView =  (ListView) findViewById(R.id.radresponderList);
        pairedListView.setAdapter(adt);
        adt.setOnListener(cellClick);

       // String[] name = getResources().getStringArray(R.array.list_pref_eventlist3);
        String[] name = getResources().getStringArray(R.array.list_pref_eventlist3);
        String[] sponsor = getResources().getStringArray(R.array.list_pref_eventlist3);

        for(int i = 0; i<name.length; i++){
            RadresponderAdapter3.Radresponder rad = new RadresponderAdapter3.Radresponder();
            rad.name = name[i];
            rad.sponsor = sponsor[i];
            list.add(rad);
        }
    }

    //list cell clickListener
    private RadresponderAdapter3.clickListener cellClick = new RadresponderAdapter3.clickListener() {
        @Override
        public void onCellClick(int id, String name) {
            selectEvent = name;
            adt.miSelIndex = id;
            adt.notifyDataSetChanged();
            return;
        }
    };

    //터치방지
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);
        if(!dialogBounds.contains((int)event.getX(), (int)event.getY())){
            return false;
        }
        return super.dispatchTouchEvent(event);
    }


    //button cellClick Listener
    public View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.next:
                    if(adt.miSelIndex!=-1){
                        if(selectEvent.equals("")){
                            selectEvent = MainActivity.mPrefDB.Get_String_From_pref(getString(R.string.rad_response_eventid_key));
                        }
/*                        Intent intent = new Intent(RadresponderEventListActivity.this, RadresponderSponsorListActivity.class);
                        intent.putExtra("select", selectEvent);
                        startActivity(intent);*/
                        MainActivity.mPrefDB.Set_String_on_pref(getString(R.string.rad_response_eventid_key),selectEvent );
                        Toast.makeText(RadresponderEventListActivity.this, "saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;

                case R.id.cancel:
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
