package android.HH100.Identification;

import java.util.Vector;

import android.HH100.Structure.NcPeak;
import android.HH100.Structure.Spectrum;

public interface FindPeakMode {

	public Vector<NcPeak> Find_Peak(Spectrum MS, Spectrum BG);

}
