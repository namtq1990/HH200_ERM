package android.HH100.Structure;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import android.HH100.DB.EventDBOper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class HW_Crytal_Info {

	public String Crystal_Name;
	public double Crystal_Number;


	public double Wnd_ROI_En = 0;

	public double[] FWHM;
	public double[] FindPeakN_Coefficients;

	public int Cu_Crystal_Name = 0;
	public int Cu_Crystal_Number = 1;
	public int Cu_FWHM = 2;
	public int Cu_Coefficients = 4;
	public int Cu_Wnd_ROI_En = 5;


}
