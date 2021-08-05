package android.HH100;

import android.HH100.R;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefFragment extends PreferenceFragment {
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)

	@Override

	public void onCreate(Bundle savedInstanceState) 
	{

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setting);

	}

}
