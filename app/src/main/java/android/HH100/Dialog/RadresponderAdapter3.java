package android.HH100.Dialog;

import android.HH100.MainActivity;
import android.HH100.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by inseon.ahn on 2018-7-31.
 */
public class RadresponderAdapter3 extends BaseAdapter {

    public static class ListHolder {
        public TextView tvName;
        public TextView tvSponsor;
		LinearLayout layout;
		RelativeLayout layout1;
		boolean check;
		RadioButton radio;
		int idx;
	}

    public static class Radresponder {
       public String name;
       public String sponsor;
    }

	public static ListHolder mHolder;
    ArrayList<Radresponder> list;
    Context context;
    public  int miSelIndex = -1;
   public RadresponderAdapter3(Context ctxt, ArrayList<Radresponder> mData) {
		context = ctxt;
        list = mData;
    }
    public void setOnListener(clickListener listener)
    {
        mListener = listener;
    }
    public interface clickListener {
       void onCellClick(int id, String name);
    }
    private clickListener mListener;
    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int i)
    {
        return null;
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }


	@Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.radresponder_item1, null);
            mHolder = new ListHolder();

            mHolder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            mHolder.layout1 = (RelativeLayout) convertView.findViewById(R.id.sponsor_layout);
            mHolder.tvName = (TextView) convertView.findViewById(R.id.name);
            mHolder.tvSponsor = (TextView) convertView.findViewById(R.id.sponsor);
            mHolder.radio = (RadioButton) convertView.findViewById(R.id.check);
            convertView.setTag(mHolder);
        }
        else {
            mHolder = (ListHolder) convertView.getTag();
        }

        mHolder.idx = position;
        mHolder.layout.setOnClickListener(listClick);
        mHolder.tvSponsor.setText(list.get(position).sponsor);
        mHolder.tvName.setText(list.get(position).name);
        mHolder.tvName.setFocusableInTouchMode(false);
        mHolder.tvSponsor.setFocusableInTouchMode(false);

        if(MainActivity.mPrefDB.Get_String_From_pref(context.getString(R.string.rad_response_eventid_key))!=null && miSelIndex == -1) {
            if (MainActivity.mPrefDB.Get_String_From_pref(context.getString(R.string.rad_response_eventid_key)).equals(list.get(position).name)) {
                miSelIndex = position;
            }
        }
        if(miSelIndex==mHolder.idx){
            mHolder.radio.setChecked(true);
        }else{
            mHolder.radio.setChecked(false);
        }
      /*  if(list.get(position).name.equals("")){
            mHolder.layout1.setVisibility(View.GONE);
        }else{
            mHolder.layout1.setVisibility(View.VISIBLE);
        }*/

        mHolder.layout1.setVisibility(View.GONE);

        return convertView;
    }

    // click Listener
    private View.OnClickListener listClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            ListHolder holder = (ListHolder) view.getTag();
          // mListener.onCellClick( holder.idx, list.get(holder.idx).name, list.get(holder.idx).sponsor);
           mListener.onCellClick( holder.idx, list.get(holder.idx).sponsor);
        }
    };



}
