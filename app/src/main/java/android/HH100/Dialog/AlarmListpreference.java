package android.HH100.Dialog;


import java.io.IOException;

import android.HH100.R;
import android.HH100.R.raw;
import android.HH100.Structure.NcLibrary;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AlarmListpreference extends ListPreference {

	private MediaPlayer mMediaPlayer;
	CharSequence[] mEntries;
	CharSequence[] mEntryValues;
	private int mClickedDialogEntryIndex;
	private String mValue;

	public AlarmListpreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public AlarmListpreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	/**
	 * Sets the value of the key. This should be one of the entries in
	 * {@link #getEntryValues()}.
	 *
	 * @param value The value to set for the key.
	 */
	public void setValue(String value) {
		mValue = value;

		persistString(value);
	}

	/**
	 * Sets the value to the given index from the entry values.
	 *
	 * @param index The index of the value to set.
	 */
	public void setValueIndex(int index) {
		if (mEntryValues != null) {
			setValue(mEntryValues[index].toString());
		}
	}

	/**
	 * Returns the value of the key. This should be one of the entries in
	 * {@link #getEntryValues()}.
	 *
	 * @return The value of the key.
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 * Returns the entry corresponding to the current value.
	 *
	 * @return The entry corresponding to the current value, or null.
	 */
	public CharSequence getEntry() {
		int index = getValueIndex();
		return index >= 0 && mEntries != null ? mEntries[index] : null;
	}

	public int findIndexOfValue(String value) {
		if (value != null && mEntryValues != null) {
			for (int i = mEntryValues.length - 1; i >= 0; i--) {
				if (mEntryValues[i].equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}

	private int  getValueIndex() {

		return findIndexOfValue(mValue);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);

		mMediaPlayer = new MediaPlayer();
		mEntries = getEntries();
		mEntryValues = getEntryValues();

		if (mEntries == null || mEntryValues == null) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array.");
		}

		mClickedDialogEntryIndex = getValueIndex();
		builder.setSingleChoiceItems(mEntries, mClickedDialogEntryIndex,
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						mClickedDialogEntryIndex = which;

						String value = mEntryValues[which].toString();
						try {
							playSong(value);
						} catch (IllegalStateException e) {
							NcLibrary.Write_ExceptionLog(e);
						} catch (IOException e) {
							NcLibrary.Write_ExceptionLog(e);
						}
					}
				});

		builder.setPositiveButton("Save", this);
		builder.setNegativeButton("Cancel", this);
	}


	private void playSong(String path) throws IllegalArgumentException,
			IllegalStateException, IOException {

		//Log.d("ringtone", "playSong :: " + path);
		if(mMediaPlayer.isPlaying())mMediaPlayer.stop();
		mMediaPlayer.reset();
		mMediaPlayer = MediaPlayer.create(this.getContext(), Get_Sound_byIndexNumber(Integer.valueOf(path)));
		//mMediaPlayer.setDataSource(path);
		// mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		//  mMediaPlayer.setLooping(true);
		// mMediaPlayer.prepare();
		mMediaPlayer.start();
	}
	private int Get_Sound_byIndexNumber(int Index){
		if(Index == 0) return R.raw.warning1;
		else if(Index == 1) return R.raw.clock;
		else if(Index == 2) return R.raw.charmingbell;
		else if(Index == 3) return R.raw.trumpet;
		else if(Index == 4) return R.raw.bell;
		else if(Index == 5) return R.raw.beep1;
		else return 0;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setValue(myState.value);
	}

	private static class SavedState extends BaseSavedState {
		String value;

		public SavedState(Parcel source) {
			super(source);
			value = source.readString();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(value);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
			String value = mEntryValues[mClickedDialogEntryIndex].toString();
			if (callChangeListener(value)) {
				setValue(value);
			}
		}

		mMediaPlayer.stop();
		mMediaPlayer.release();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.value = getValue();
		return myState;
	}
}
