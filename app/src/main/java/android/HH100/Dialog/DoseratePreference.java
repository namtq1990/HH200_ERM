package android.HH100.Dialog;


import android.HH100.MainActivity;
import android.HH100.PreferenceActivity;
import android.HH100.R;
import android.HH100.R.raw;
import android.HH100.SetupSpectrumActivity;
import android.HH100.Structure.NcLibrary;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import java.io.IOException;

public class DoseratePreference extends ListPreference {

	private MediaPlayer mMediaPlayer;
	CharSequence[] mEntries;
	CharSequence[] mEntryValues;
	private int mClickedDialogEntryIndex;
	private String mValue;

	public DoseratePreference(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	public DoseratePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	protected void onPrepareDialogBuilder(Builder builder)
	{
		super.onPrepareDialogBuilder(builder);

		mMediaPlayer = new MediaPlayer();
		mEntries = getEntries();
		mEntryValues = getEntryValues();
		mClickedDialogEntryIndex =MainActivity.mPrefDB.Get_String_From_pref(getContext().getResources().getString(R.string.setup_dr_unit_key)).equals("1") ? 0:1;
		setValueIndex(mClickedDialogEntryIndex);

		if (mEntries == null || mEntryValues == null)
		{
			throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
		}

		builder.setSingleChoiceItems(mEntries, mClickedDialogEntryIndex, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				mClickedDialogEntryIndex = which;
			}
		});

		builder.setPositiveButton("Save",new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{
				String s = MainActivity.mPrefDB.Get_String_From_pref(getContext().getResources().getString(R.string.setup_dr_unit_key));
				String summary = PreferenceActivity.HealthThre1.getSummary().toString();
				//SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
				String a = mClickedDialogEntryIndex == 0 ? "1": "0";
				if (!s.equals(a))
				{
					//Change_HealthAlarm_Unit((Integer.valueOf(newValue.toString()) == 0) ? true : false);

					if (mClickedDialogEntryIndex == 1)
					{
						summary = summary.replace("rem/h", "Sv/h");
						PreferenceActivity.HealthThre1.setSummary(summary);

						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
						int Value = Integer.valueOf(pref.getString(getContext().getResources().getString(R.string.healthy_threshold), "0"));
						Value = (int) NcLibrary.Rem_To_Sv(Value);
						PreferenceActivity.HealthThre1.setText(String.valueOf(Value));

						MainActivity.mPrefDB.Set_String_on_pref(getContext().getString(R.string.setup_dr_unit_key), "0");

					}
					else
					{

						summary = summary.replace("Sv/h", "rem/h");
						PreferenceActivity.HealthThre1.setSummary(summary);

						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
						int Value = Integer.valueOf(pref.getString(getContext().getResources().getString(R.string.healthy_threshold), "0"));
						Value = (int) NcLibrary.Sv_To_Rem(Value);
						PreferenceActivity.HealthThre1.setText(String.valueOf(Value));

						MainActivity.mPrefDB.Set_String_on_pref(getContext().getString(R.string.setup_dr_unit_key), "1");
					}
				}
			}
		});
		builder.setNegativeButton("Cancel", this);
	}


}
