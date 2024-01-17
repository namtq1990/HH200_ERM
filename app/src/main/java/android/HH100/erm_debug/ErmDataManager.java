package android.HH100.erm_debug;

import android.HH100.R;
import android.HH100.Structure.Detector;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.HH100.TCPServerService;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ErmDataManager {
    private static final ErmDataManager ourInstance = new ErmDataManager();

    public static ErmDataManager getInstance() {
        return ourInstance;
    }

    private static final long DEFAULT_DURATION = 5 * 60000;

    private ErmDataManager() {
        mSpcPerMin = new ArrayList<>(65);
        mSpcPerDuration = new ArrayList<>();
    }

    private Context mContext;
    private ErmDBHelper mDBHelper;
    private long mDuration = 5 * 60000;
    private List<Spectrum> mSpcPerMin;
    private List<Spectrum> mSpcPerDuration;
    private Calendar mMinuteTime;
    private Calendar mDurationTime;

    public void setContext(Context context) {
        this.mContext = context;
        if (mDBHelper == null) {
            mDBHelper = new ErmDBHelper(context);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            pref.registerOnSharedPreferenceChangeListener(mListener);
            loadDurationTime(pref);
        }
    }

    private void loadDurationTime(SharedPreferences pref) {
        try {
            String duration = pref.getString(mContext.getString(R.string.p_manual_id_defalut), "300");
            mDuration = Long.parseLong(duration) * 1000;

            TCPServerService.TimeSequenceSendSpectrum = Integer.parseInt(duration);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            mDuration = DEFAULT_DURATION;
            TCPServerService.TimeSequenceSendSpectrum = 300;
        }
    }

    public void addCurrentSpectra(Detector detector) {
        try {
            Spectrum spc = new Spectrum();
            Calendar now = Calendar.getInstance();
            now.set(Calendar.MILLISECOND, 0);
            String date = NcLibrary.DATE_FORMAT.format(now.getTime());
            spc.Set_Spectrum(detector.MS);
            spc.Set_MeasurementDate(date);
            spc.Set_mGammaDoserate(detector.Get_Gamma_DoseRate_nSV());
            spc.Set_Coefficients(detector.Coeffcients);
            spc.mAcqTime = 1;

            if (mMinuteTime == null) {
                mMinuteTime = Calendar.getInstance();
                setTimerFor(mMinuteTime, false);        // After 60s
            }
            if (mDurationTime == null) {
                mDurationTime = Calendar.getInstance();
                setTimerFor(mDurationTime, true);
            }

            if (now.before(mMinuteTime)) {
                mSpcPerMin.add(spc);
            } else {
                long timeAmount = 0;
                boolean resetTempSpc = false;
                try {
                    timeAmount = getDurationBetween(mSpcPerMin.get(0), spc);
                    resetTempSpc = timeAmount > 61 * 1000;
                } catch (Exception e) {
                    e.printStackTrace();
                    resetTempSpc = true;
                }
                if (resetTempSpc) {
                    // Validate that is same minute
                    // This 2 spectrum isn't same minute, so ignore before
                    mSpcPerMin.clear();
                    mSpcPerMin.add(spc);
                    mSpcPerDuration.clear();
                    setTimerFor(mMinuteTime, false);
                    setTimerFor(mDurationTime, true);
                    return;
                }

                mSpcPerMin.add(spc);
                Spectrum minuteSpc = computeTotalSpc(mSpcPerMin);
                setTimerFor(mMinuteTime, false);
                mSpcPerMin.clear();
                if (minuteSpc != null) {
                    if (now.before(mDurationTime)) {
                        spc.Set_Spectrum(minuteSpc);
                        spc.setHasSpectra(false);
                        spc.Set_MeasurementDate(formatSavedTime(now.getTime()));
                        mDBHelper.insertEvent(spc);
                        mSpcPerDuration.add(spc);
                    } else {
                        mSpcPerDuration.add(minuteSpc);
                        Spectrum durationSpc = computeTotalSpc(mSpcPerDuration);
                        spc.setHasSpectra(true);
                        spc.Set_Spectrum(durationSpc);
                        spc.Set_MeasurementDate(formatSavedTime(now.getTime()));
                        mDBHelper.insertEvent(spc);
                        deleteOldSpectrum();
                        setTimerFor(mDurationTime, true);
                        mSpcPerDuration.clear();

                        TCPServerService.SetSpectrum(spc);
                    }
                }
            }
        } catch (Exception e) {
            NcLibrary.SaveText("ErmData saving error", "ERMLogging", true);
            e.printStackTrace();
        }

    }

    private void deleteOldSpectrum() {
        // Delete too old spectrum > 30 month
        Date now = new Date();
        long time = now.getTime() - 30L * 24L * 3600L * 1000L;
        Date lastMonth = new Date(time);
        deleteSpectrum(null, lastMonth);
    }

    public void deleteSpectrum(Date from, Date to) {
        mDBHelper.deleteSpectrum(
                from != null ? NcLibrary.DATE_FORMAT.format(from) : null,
                to != null ? NcLibrary.DATE_FORMAT.format(to) : null
        );
    }

    public Spectrum[] loadSpectrum(Date from, Date to) {
        List<Spectrum> spcs = mDBHelper.loadSpectra(from, to);
        Spectrum[] rs = new Spectrum[spcs.size()];
        return spcs.toArray(rs);
    }

    private long getDurationBetween(Spectrum spc1, Spectrum spc2) throws ParseException {
        Date date1 = NcLibrary.DATE_FORMAT.parse(spc1.Get_MesurementDate());
        Date date2 = NcLibrary.DATE_FORMAT.parse(spc2.Get_MesurementDate());
        return Math.abs(date1.getTime() - date2.getTime());
    }

    private void setTimerFor(Calendar calendar, boolean isDuration) {
        calendar.setTime(new Date());
        calendar.setTimeInMillis(calendar.getTimeInMillis() + 60000);
        calendar.set(Calendar.SECOND, 0);
        if (isDuration) {
            long curMinute = calendar.get(Calendar.MINUTE) * 60 * 1000;
            int count;
            for (count = 0; curMinute > count * mDuration; count++) {
            }
            long waitingTime = count * mDuration - curMinute;
            calendar.setTimeInMillis(calendar.getTimeInMillis() + waitingTime);
        }
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private String formatSavedTime(Date date) {
        String s = NcLibrary.DATE_FORMAT.format(date);
//        return s.substring(0, s.length() - 2) + "00";
        return s;
    }

    private Spectrum computeTotalSpc(List<Spectrum> spectrums) {
        if (spectrums == null || spectrums.size() == 0) return null;

        Spectrum rs = new Spectrum();
        double doseRate = 0;
        int acqTime = 0;

        for (Spectrum spectrum : spectrums) {
            rs.Accumulate_Spectrum(spectrum);
            doseRate += spectrum.Get_mGammaDoserate();
            acqTime += spectrum.mAcqTime;
        }
        rs.Set_mGammaDoserate(doseRate / spectrums.size());
        rs.Set_Coefficients(spectrums.get(0).Get_Coefficients().get_Coefficients());
        rs.mAcqTime = acqTime;

        return rs;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(mContext.getString(R.string.p_manual_id_defalut))) {
                loadDurationTime(sharedPreferences);
            }
        }
    };
}
