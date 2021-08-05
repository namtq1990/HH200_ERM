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
import java.util.Locale;

/**
 * Created by inseon.ahn on 2018-7-31.
 */
public class RadresponderAdapter4 extends BaseAdapter {

    public static class ListHolder {
        public TextView tvName;
        public TextView tvSponsor;
		LinearLayout layout;
		RelativeLayout layout1;
		boolean check;
		RadioButton radio;
		int idx;
	}

	public static ListHolder mHolder;
    ArrayList<RadresponderAdapter3.Radresponder> list;
    ArrayList<RadresponderAdapter3.Radresponder> list2;
    Context context;
    public  int miSelIndex = -1;
   public RadresponderAdapter4(Context ctxt, ArrayList<RadresponderAdapter3.Radresponder> mData) {
		context = ctxt;
        list = mData;
      //  list2 = mData;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.radresponder_item2, null);
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
        if(MainActivity.mPrefDB.Get_String_From_pref(context.getString(R.string.rad_response_sponsor_key))!=null && miSelIndex == -1) {
            if (MainActivity.mPrefDB.Get_String_From_pref(context.getString(R.string.rad_response_sponsor_key)).equals(list.get(position).sponsor)) {
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

  /*  public void filter(ArrayList<RadresponderAdapter3.Radresponder>list2, String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        list.clear();
        if (charText.length() == 0) {
             list.addAll(list2);
        } else {
           for (int i = 0; i<list2.size(); i++) {
                if (list2.get(i).sponsor.toLowerCase().contains(charText)) {
                    RadresponderAdapter3.Radresponder rad = new RadresponderAdapter3.Radresponder();
                    rad.name = list2.get(i).sponsor;
                    rad.sponsor =list2.get(i).sponsor;
                    list.add(rad);
                }
            }
        }
          notifyDataSetChanged();
    }*/

public void listNotify(){
    notifyDataSetChanged();
}
  public void listClear(){
      list.clear();
  }
}
