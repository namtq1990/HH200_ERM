package android.HH100.Dialog;
import android.HH100.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class RadresponderListActivity1 extends Activity {
    private RadresponderAdapter adt;
    private ArrayList<RadresponderAdapter.Radresponder> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.radresponder);
        adt = new RadresponderAdapter(RadresponderListActivity1.this, list);

        ListView pairedListView =  (ListView) findViewById(R.id.radresponderList);
        pairedListView.setAdapter(adt);

        String[] name = getResources().getStringArray(R.array.radresponder_name);
        String[] sponsor = getResources().getStringArray(R.array.radresponder_sponsor);

        for(int i = 0; i<name.length; i++){
            RadresponderAdapter.Radresponder rad = new RadresponderAdapter.Radresponder();
            rad.name = name[i];
            rad.sponsor = sponsor[i];
            list.add(rad);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
