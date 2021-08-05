package android.HH100;
/*package android.HH100;


import com.google.android.maps.*;


import android.app.*;
import android.content.*;
import android.graphics.*;
//import com.google.android.maps.map//
import android.location.*;
import android.os.*;
import android.widget.*;

public class MapViewActivity extends MapActivity {
	MapView mMap;
	double mLatitude =0;
	double mLongitude =0;
	private LocationManager mLocationManager; 
	
	private class GPSListener implements LocationListener{

		double mLatitude = 0;
		double mLongitude = 0;

		public double GetLatitude(){
			return mLatitude;
		}
		public double GetLongitude(){
			return mLongitude;
		}
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();

			Toast.makeText(getApplicationContext(), String.valueOf(mLatitude), 2000).show();
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}
	private GPSListener Gpslis;
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.map);
		
		
		MapController mapControl = mMap.getController();
		
		mapControl.setZoom(17);
		mMap.setBuiltInZoomControls(true);
		
		
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		//mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, Gpslis);
		Location mMyLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		mLatitude = mMyLocation.getLatitude();
		mLongitude = mMyLocation.getLongitude();
		GeoPoint pt = new GeoPoint((int)(mLatitude*1000000), (int)(mLongitude*1000000));
		mapControl.setCenter(pt);
	}
	
	class Tourmap extends Overlay {
	      public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	         super.draw(canvas, mapView, shadow);

	         Paint pnt = new Paint();
	         Bitmap bit;
	         Point pt;

	         pnt.setAntiAlias(true);
	         pnt.setTextSize(30);

	 

	         // 상단에 움직이지 않는 문자 넣기
	         canvas.drawText("경복궁 지도", 10, 40, pnt);

	         Projection projection;
	         projection = mapView.getProjection();

	 

	         // 좌표에 광화문 그림 넣기 즉 좌표와 같이 움직인다.
	        bit = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
	       pt = projection.toPixels(new GeoPoint((int)(mLatitude*1000000), (int)(mLongitude*1000000)),null);
	        canvas.drawBitmap(bit, pt.x, pt.y, pnt);
	         		      
	    }
	      
	   // 화면을 터치하면 좌표가 나온다
	      public boolean onTap(GeoPoint p, MapView mapView) {
	         String msg;
	         msg = "x = " + p.getLatitudeE6() + ", y = " + p.getLongitudeE6();
	         Toast.makeText(MapViewActivity.this, msg, Toast.LENGTH_SHORT).show();
	          return true;
	      }

  }
}
*/