package android.HH100.erm_debug;

import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.Spectrum;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ErmDBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DB_FILE = "ErmDB.db";
    private static final String TABLE_SPECTRA = "ErmSpectra";
    private static final String COLUMN_SPECTRA_ID = "Id";
    private static final String COLUMN_SPECTRA_TIME = "Time";
    private static final String COLUMN_SPECTRA_DOSERATE = "DoseRate";
    private static final String COLUMN_SPECTRA_ACQ_TIME = "AcqTime";
    private static final String COLUMN_SPECTRA_CALIBRATION = "Calibration";
    private static final String COLUMN_SPECTRA_DATA = "Spectra";
    private static final String SQL_CREATE_DB = String.format("CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT," +       // ID
                    "%s TEXT NOT NULL," +       // TIme
                    "%s double NOT NULL," +                 // DoseRate
                    "%s TEXT NOT NULL," +                   // Calibration
                    "%s INTEGER NOT NULL," +            // Acq Time
                    "%s TEXT NOT NULL" +                    // Spectra
                    ")", TABLE_SPECTRA,
            COLUMN_SPECTRA_ID,
            COLUMN_SPECTRA_TIME,
            COLUMN_SPECTRA_DOSERATE,
            COLUMN_SPECTRA_CALIBRATION,
            COLUMN_SPECTRA_ACQ_TIME,
            COLUMN_SPECTRA_DATA);

    private SQLiteDatabase mDb;

    public ErmDBHelper(Context context) {
        super(context, DB_FILE, null, VERSION);
        mDb = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertEvent(Spectrum spc) {
        double[] param = spc.Get_Coefficients().get_Coefficients();
        if (param == null) {
            param = new double[]{0, 0, 0};
        }

        String data = spc.hasSpectra() ? spc.ToString() : "";

        ContentValues values = new ContentValues();
        values.put(COLUMN_SPECTRA_TIME, spc.Get_MesurementDate());
        values.put(COLUMN_SPECTRA_DOSERATE, spc.Get_mGammaDoserate());
        values.put(COLUMN_SPECTRA_CALIBRATION, String.format("%s %s %s", param[0], param[1], param[2]));
        values.put(COLUMN_SPECTRA_ACQ_TIME, spc.Get_AcqTime());
        values.put(COLUMN_SPECTRA_DATA, data);
        mDb.insert(TABLE_SPECTRA, null, values);
    }

    public List<Spectrum> loadSpectra(Date from, Date to) {
        String[] projection = new String[]{
                COLUMN_SPECTRA_TIME,
                COLUMN_SPECTRA_DOSERATE,
                COLUMN_SPECTRA_ACQ_TIME,
                COLUMN_SPECTRA_CALIBRATION,
                COLUMN_SPECTRA_DATA
        };
        String selection = "";
        String[] selectionArgs = null;
        if (from != null && to != null) {
            selection = String.format("DATETIME(%s) BETWEEN DATETIME( ? ) AND DATETIME( ? )",
                    COLUMN_SPECTRA_TIME);
            selectionArgs = new String[]{
                    NcLibrary.DATE_FORMAT.format(from),
                    NcLibrary.DATE_FORMAT.format(to)
            };
        }

        List<Spectrum> spectrums = new ArrayList<>();
        try (Cursor cursor = mDb.query(TABLE_SPECTRA, projection,
                selection, selectionArgs, null, null, null, null)) {
            Spectrum spectrum;
            while (cursor.moveToNext()) {
                Spectrum temp = new Spectrum();
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_SPECTRA_TIME));
                double doseRate = cursor.getDouble(cursor.getColumnIndex(COLUMN_SPECTRA_DOSERATE));
                int acqTime = cursor.getInt(cursor.getColumnIndex(COLUMN_SPECTRA_ACQ_TIME));
                String calibration = cursor.getString(cursor.getColumnIndex(COLUMN_SPECTRA_CALIBRATION));
                String[] data = cursor.getString(cursor.getColumnIndex(COLUMN_SPECTRA_DATA)).split(";");
                if (data.length > 1) {
                    double[] spcData = new double[data.length];
                    for (int i = 0; i < spcData.length; i++) {
                        spcData[i] = Double.parseDouble(data[i]);
                    }
                    temp.setHasSpectra(true);
                    temp.Set_Spectrum(spcData, acqTime);
                } else {
                    temp.setHasSpectra(false);
                    temp.mAcqTime = acqTime;
                }

                double[] param = new double[3];
                String[] calibs = calibration.split(" ");
                for (int i = 0; i < param.length; i++) {
                    param[i] = Double.parseDouble(calibs[i]);
                }
                temp.Set_MeasurementDate(date);
                temp.Set_mGammaDoserate(doseRate);
                temp.mAcqTime = acqTime;
                temp.Set_Coefficients(param);

                spectrum = temp;
                spectrums.add(spectrum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return spectrums;
    }

    private static final SimpleDateFormat MINUTE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
}
