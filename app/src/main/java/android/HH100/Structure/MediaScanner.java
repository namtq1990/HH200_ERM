package android.HH100.Structure;

import java.io.File;
import java.io.FilenameFilter;

import android.HH100.DB.EventDBOper;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class MediaScanner {
	
	private String TAG = "Media Scanner";
	private boolean mWasMediaScaned = false;
	private Context mSuper;
	private MediaScannerConnection msc=null;
	
	public MediaScanner(Context context) {
		mSuper = context;
		msc = new MediaScannerConnection(mSuper, mScanClient);
	}
	private MediaScannerConnectionClient mScanClient = new MediaScannerConnectionClient(){

	      public void onMediaScannerConnected() {
	            Log.i(TAG, "onMediaScannerConnected");
	            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+EventDBOper.DB_FOLDER);                // 외장 디렉토리 가져옴 
	            
	            File[] fileNames = file.listFiles(new FilenameFilter(){               // 특정 확장자만 가진 파일들을 필터링함 
	                public boolean accept(File dir, String name){
	                return true;
	                }
	            });
	            
	            if (fileNames != null)
	            {
	                for (int i = 0; i < fileNames.length ; i++)          //  파일 갯수 만큼   scanFile을 호출함 
	                {
	                    msc.scanFile(fileNames[i].getAbsolutePath(), null);
	                }
	            }
	            
	            
	            //// isotope library 
	            File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+EventDBOper.DB_LIB_FOLDER);                // 외장 디렉토리 가져옴 
	            
	            File[] fileNames2 = file2.listFiles(new FilenameFilter(){               // 특정 확장자만 가진 파일들을 필터링함 
	                public boolean accept(File dir, String name){
	                return true;
	                }
	            });
	            
	            if (fileNames2 != null)
	            {
	                for (int i = 0; i < fileNames2.length ; i++)          //  파일 갯수 만큼   scanFile을 호출함 
	                {
	                    msc.scanFile(fileNames2[i].getAbsolutePath(), null);
	                }
	            } 
	            mWasMediaScaned=true;
	      }	  
	      public void onScanCompleted(String path, Uri uri) {
	            Log.i(TAG, "onScanCompleted(" + path + ", " + uri.toString() + ")");     // 스캐닝한 정보를 출력해봄 
	      }
	};
	
	
	
	
	public boolean Start_MediaScan(){
		if (msc != null)
        {          
            if (msc.isConnected()){
                msc.disconnect();
            	return false;
            }
            else{
                msc.connect();
            	return true;
            }
        }

		return false;
	}
	
}

