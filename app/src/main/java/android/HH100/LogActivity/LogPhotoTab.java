package android.HH100.LogActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.HH100.CameraUtil.App;
import android.HH100.CameraUtil.Camera2Activity;
import android.HH100.CameraUtil.VideoActivity;
import android.HH100.IDspectrumActivity;
import android.HH100.IDspectrumActivity.Check;
import android.HH100.MainActivity;
import android.HH100.MainActivity.Activity_Mode;
import android.HH100.R;
import android.HH100.RecActivity;
import android.HH100.DB.EventDBOper;
import android.HH100.Structure.NcLibrary;
import android.HH100.Structure.SingleMediaScanner;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LogPhotoTab extends Activity {
	public static String EXTRA_PHOTO_FILE_NAME = "Photo";
	public static String EXTRA_VIDEO_FILE_NAME = "Video";
	public static String EXTRA_EVENT_NUMBER = "EventNumber";

	public boolean IS_TAKE_PHOTO_AND_VIDEO = true;

	Gallery mGallery = null;
	Vector<Bitmap> mThumnail = new Vector<Bitmap>();
	Vector<String> mPhoto = null;
	Vector<String> mVideo = null;
	Vector<String> mRecoder = null;

	Vector<String> mPhotoName = new Vector<String>();
	Vector<String> mVideoName = new Vector<String>();
	Vector<String> mRecoderName = new Vector<String>();

	Vector<String> mTotalTxt = new Vector<String>();

	TextView mMediaCnt = null;
	public static int mEventNumber;

	public static int mFileNumber = 0;
	public static boolean mDoubleClickRock = false;

	public interface Media {
/*		String FolderPath = Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/"
				+ EventDBOper.MIDEA_FOLDER;
*/

		String FolderPath = Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER;

	}

	Context mContext;
	Bitmap RecBitmap;
	String photoPath, videoPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_log_photo);
		mContext = this;
		mEventNumber = (int) getIntent().getIntExtra(EXTRA_EVENT_NUMBER, 0);

		RecBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rec);

		if (mEventNumber == -1) { // -1�� ANSI42 �뙆�씪�뿉�꽌 遺덈윭�삩 �씠踰ㅽ듃
			IS_TAKE_PHOTO_AND_VIDEO = false;
			return;
		}

		if (mPhoto == null) {
			EventDBOper DB = new EventDBOper();
			DB.OpenDB();

			mPhoto = DB.Get_PhotoFileName(mEventNumber );

			for (int i = 0; i < mPhoto.size(); i++) {

				mPhotoName.add(mPhoto.get(i));

				mPhoto.set(i, Media.FolderPath + "/" + mPhoto.get(i) + ".png");

				File file;
				file = new File(mPhoto.get(i));
				if (file.exists()) {

					mThumnail.add(BitmapFactory.decodeFile(mPhoto.get(i)));

				}

			}
			DB.EndDB();
			DB = null;
		}
		if (mVideo == null) {
			EventDBOper DB = new EventDBOper();
			DB.OpenDB();
			mVideo = NcLibrary.Separate_EveryDash2(DB.Get_ColumnData_inEvent(EventDBOper.VIDEO, mEventNumber ));
			if (mVideo == null)
				mVideo = new Vector<String>();
			for (int i = 0; i < mVideo.size(); i++) {

				mVideoName.add(mVideo.get(i));
				mVideo.set(i, Media.FolderPath + "/" + mVideo.get(i) + ".mp4");

				File file;
				file = new File(mVideo.get(i));
				if (file.exists()) {

					Bitmap Thum = ThumbnailUtils.createVideoThumbnail(mVideo.get(i),
							android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
					mThumnail.add(
							overlayMark(Thum, new BitmapFactory().decodeResource(getResources(), R.drawable.play)));

				}

			}
			DB.EndDB();
			DB = null;
		}

		if (mRecoder == null) {
			EventDBOper DB = new EventDBOper();
			DB.OpenDB();

			mRecoder = DB.Get_RecoderFileName(mEventNumber);

			for (int i = 0; i < mRecoder.size(); i++) {

				mRecoderName.add(mRecoder.get(i));

				mRecoder.set(i, Media.FolderPath + "/" + mRecoder.get(i) + ".amr");

				mTotalTxt.add(mRecoderName.get(i));
				File file;
				file = new File(mRecoder.get(i));
				if (file.exists()) {

					mThumnail.add(BitmapFactory
							.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.png"));

				}

			}
			DB.EndDB();
			DB = null;
		}

		mMediaCnt = (TextView) findViewById(R.id.EventLog_PhotoCount);
		mMediaCnt.setText(getResources().getString(R.string.photo_and_video) + " : " + mThumnail.size());
		Load_Thumnail();
		Init_GalleryView();

	}

	private void Init_GalleryView() {

		mMediaCnt.setText(getResources().getString(R.string.photo_and_video) + " : " + mThumnail.size());

		mGallery = (Gallery) findViewById(R.id.EventLog_Gallery);
		mGallery.setAdapter(new AddImgAdp(this));
		mGallery.setSpacing(25);



		mGallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView parent, View v, int position, long id) {
				if (mDoubleClickRock == false) {

					Log.d("time:", "DoubleTest : onItemClick Excute");

					MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;

					Intent intent = new Intent();

					intent.setAction(android.content.Intent.ACTION_VIEW);
					if (position < mPhoto.size()) {
						Uri uri = Uri.fromFile(new File(mPhoto.get(position)));
						intent.setDataAndType(uri, "image/*");
					} else if (position >= mPhoto.size() && position < mPhoto.size() + mVideo.size()
							&& mPhoto.size() != mPhoto.size() + mVideo.size()) {
						Uri uri = Uri.fromFile(new File(mVideo.get(position - mPhoto.size())));
						intent.setDataAndType(uri, "video/*");
					} else if (position >= mPhoto.size() + mVideo.size()
							&& position < mPhoto.size() + mVideo.size() + mRecoder.size()) {
						Uri uri = Uri.fromFile(new File(mRecoder.get(position - (mPhoto.size() + mVideo.size()))));
						intent.setDataAndType(uri, "audio/*");
					}

					// intent.setAction(intent.ACTION_CAMERA_BUTTON);
					startActivity(intent);
				}
			}
		});

		mGallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {

				//int i = mGallery.getSelectedItemPosition();

				Log.d("time:", "DoubleTest : onItemLongClick Excute");
				mDoubleClickRock = true;

				TimerTask mTask = new TimerTask() {
					@Override
					public void run() {
						mDoubleClickRock = false;
					}
				};

				Timer mTimer = new Timer();
				mTimer.schedule(mTask, 500);

				DeleteDlg(mGallery.getSelectedItemPosition());

				return true;
			}

		});

	}

	private Bitmap overlayMark(Bitmap baseBmp, Bitmap overlayBmp)

	{
		Bitmap resultBmp = Bitmap.createBitmap(baseBmp.getWidth(), baseBmp.getHeight(), baseBmp.getConfig());
		Canvas canvas = new Canvas(resultBmp);
		canvas.drawBitmap(baseBmp, 0, 0, null);

		Paint paint = new Paint();
		paint.setColor(Color.argb(150, 40, 40, 40));
		canvas.drawRect(new Rect(0, 0, baseBmp.getWidth(), baseBmp.getHeight()), paint);

		RectF OverRect = new RectF(0f, 0f, overlayBmp.getWidth() * 0.4f, overlayBmp.getHeight() * 0.4f);
		canvas.drawBitmap(overlayBmp, new Rect(0, 0, overlayBmp.getWidth(), overlayBmp.getHeight()),
				new RectF((baseBmp.getWidth() / 2) - (OverRect.width() / 2),
						(baseBmp.getHeight() / 2) - (OverRect.height() / 2),
						OverRect.width() + (baseBmp.getWidth() / 2) - (OverRect.width() / 2),
						OverRect.height() + (baseBmp.getHeight() / 2) - (OverRect.height() / 2)),
				null);
		return resultBmp;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (IS_TAKE_PHOTO_AND_VIDEO == true) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.event_log, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent = null;
		switch (item.getItemId())
		{
			case R.id.Photo:
				if (mPhoto == null)
					mPhoto = new Vector<String>();
				MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				for (int i = 1; i < 1000; i++) {

					File file;
					file = new File(Media.FolderPath + "/EventP" + mEventNumber + "_" + i + ".png");
					if (!file.exists()) {

						mFileNumber = i;
						break;
					}

				}

				photoPath = "EventP" + mEventNumber + "_" + mFileNumber + ".png";

	/*		File file = new File(Media.FolderPath, FileName);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));*/
				// cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT,600*600);
				//startActivityForResult(cameraIntent, 1);

				intent = new Intent(LogPhotoTab.this, Camera2Activity.class);
				//mFile = CommonUtils.createImageFile("mFile");
				//파일 저장 경로와 이름
				intent.putExtra("file", photoPath);
				intent.putExtra("hint", getResources().getString(R.string.camera_area));
				//프레이밍 영역 (전체 밝은 영역)으로 전체 화면 사용 여부
				intent.putExtra("hideBounds", false);
				//최대 허용 카메라 크기 (픽셀 수)
				intent.putExtra("maxPicturePixels", 3840 * 2160);
				startActivityForResult(intent, App.TAKE_PHOTO_CUSTOM);

				break;
			case R.id.Video:
				if (mVideo == null)
					mVideo = new Vector<String>();
				MainActivity.ActionViewExcuteCheck = Activity_Mode.EXCUTE_MODE;
				intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

				for (int i = 1; i < 1000; i++) {

					File mFile;
					mFile = new File(Media.FolderPath + "/EventV" + mEventNumber + "_" + i + ".mp4");
					if (!mFile.exists()) {

						mFileNumber = i;
						break;
					}

				}

/*			String File = "EventV" + mEventNumber + "_" + mFileNumber + ".mp4";
			file = new File(Media.FolderPath, File);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(intent, 2);*/

				intent = new Intent(LogPhotoTab.this, VideoActivity.class);
				videoPath= "EventV" + mEventNumber + "_" + mFileNumber + ".mp4";
				intent.putExtra("path",videoPath);
				startActivityForResult(intent, 2);

				break;

			case R.id.Recoder:

				intent = new Intent(LogPhotoTab.this, RecActivity.class);

				intent.putExtra(Check.ListNumber, mEventNumber);

				startActivityForResult(intent, 3);

				MainActivity.ACTIVITY_STATE = Activity_Mode.SOURCE_ID_RESULT_CAMERA;

				break;
		}
		return true;
	}

	public void Load_Thumnail() {

		mTotalTxt.clear();
		mThumnail.clear();
		mThumnail = new Vector<Bitmap>();
		for (int i = 0; i < mPhoto.size(); i++) {
			// mPhoto.set(i,Environment.getExternalStorageDirectory()+"/"+Event.DB_FOLDER+"/"+mPhoto.get(i));
			mThumnail.add(BitmapFactory.decodeFile(mPhoto.get(i)));
			mTotalTxt.add(mPhotoName.get(i));

		}

		for (int i = 0; i < mVideo.size(); i++) {
			// mVideo.set(i,Environment.getExternalStorageDirectory()+"/"+Event.DB_FOLDER+"/"+mVideo.get(i)+".mp4");
			Bitmap Thum = ThumbnailUtils.createVideoThumbnail(mVideo.get(i),
					android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
			mThumnail.add(overlayMark(Thum, new BitmapFactory().decodeResource(getResources(), R.drawable.play)));

			mTotalTxt.add(mVideoName.get(i));
		}

		for (int i = 0; i < mRecoder.size(); i++) {
			// mVideo.set(i,Environment.getExternalStorageDirectory()+"/"+Event.DB_FOLDER+"/"+mVideo.get(i)+".mp4");

			mThumnail.add(RecBitmap);

			mTotalTxt.add(mRecoderName.get(i));

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		File f = null;
		if (requestCode == 1 || requestCode == App.TAKE_PHOTO_CUSTOM )
		{
			f = new File(Media.FolderPath+"/"+photoPath);
			if(f.exists())
			{
				if (mPhoto == null)
					mPhoto = new Vector<String>();

				EventDBOper DB = new EventDBOper();

				DB.OpenDB();
				DB.Update_PhotoFileNames("EventP" + mEventNumber + '_' + mFileNumber, mEventNumber);
				DB.EndDB();
				DB = null;

				mPhotoName.add("EventP" + mEventNumber + "_" + Integer.toString(mFileNumber));
				mPhoto.add(Media.FolderPath + "/EventP" + mEventNumber + "_" + Integer.toString(mFileNumber) + ".png");

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;
				String LastImgPath = mPhoto.lastElement();
				Bitmap Photo = BitmapFactory.decodeFile(LastImgPath);
				Bitmap resized = Bitmap.createScaledBitmap(Photo, (int) (Photo.getWidth() * 0.3),
						(int) (Photo.getHeight() * 0.3), true);

				File Resizedfile = new File(LastImgPath);
				FileOutputStream fileStream = null;
				try {
					fileStream = new FileOutputStream(Resizedfile);
				} catch (FileNotFoundException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				if (fileStream != null)
					resized.compress(CompressFormat.PNG, 0, fileStream);
				Photo.recycle();
				resized.recycle();

				// if(mThumnail.size() ==
				// mPhoto.size())mThumnail.add(BitmapFactory.decodeFile(LastImgPath));
				// else mThumnail.add(mPhoto.size(),
				// BitmapFactory.decodeFile(LastImgPath));
				mThumnail.add(BitmapFactory.decodeFile(LastImgPath));
				Load_Thumnail();
				Init_GalleryView();

				new SingleMediaScanner(getApplicationContext(), Resizedfile);
			}
		} else if (requestCode == 2)
		{
			f = new File(Media.FolderPath+"/"+videoPath);
			if(f.exists())
			{
				if (mVideo == null)
					mVideo = new Vector<String>();
				EventDBOper DB = new EventDBOper();
				DB.OpenDB();

				DB.Update_VideoFileNames("EventV" + mEventNumber + '_' + mFileNumber, mEventNumber);
				DB.EndDB();
				DB = null;

				mVideo.add(Media.FolderPath + "/EventV" + mEventNumber + "_" + Integer.toString(mFileNumber) + ".mp4");

				mVideoName.add("EventV" + mEventNumber + "_" + Integer.toString(mFileNumber));
				Bitmap Thum = ThumbnailUtils.createVideoThumbnail(mVideo.lastElement(),
						android.provider.MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
				Thum = overlayMark(Thum, new BitmapFactory().decodeResource(getResources(), R.drawable.play));

				File Resizedfile = new File(
						Media.FolderPath + "/EventVP" + mEventNumber + "_" + Integer.toString(mFileNumber) + ".png");
				FileOutputStream fileStream = null;
				try {
					fileStream = new FileOutputStream(Resizedfile);
				} catch (FileNotFoundException e) {
					NcLibrary.Write_ExceptionLog(e);
				}

				if (fileStream != null)
					Thum.compress(CompressFormat.PNG, 0, fileStream);

				MediaScannerConnection.scanFile(this, new String[]{Resizedfile.getAbsolutePath()}, null, null);
				// mThumnail.add(mPhoto.size(), Thum);
				mThumnail.add(Thum);
				Load_Thumnail();
				Init_GalleryView();

				new SingleMediaScanner(getApplicationContext(), Resizedfile);
				new SingleMediaScanner(getApplicationContext(), new File(mVideo.lastElement()));
			}
		} else if (requestCode == 3) {

			ArrayList<String> recodeFileList = new ArrayList<String>();

			recodeFileList = data.getStringArrayListExtra(Check.ListValue);

			if(recodeFileList.size() > 0)
			{
				for (int i = 0; i < recodeFileList.size(); i++) {

					mRecoder.add(Media.FolderPath + "/" + recodeFileList.get(i) + ".amr");

					mRecoderName.add(recodeFileList.get(i));

				}

				EventDBOper DB = new EventDBOper();
				DB.OpenDB();
				for (int i = 0; i < recodeFileList.size(); i++) {

					DB.Update_RecoderFileNames(recodeFileList.get(i), mEventNumber);
				}
				DB.EndDB();
				DB = null;

				mThumnail.add(RecBitmap);

				Load_Thumnail();
				Init_GalleryView();
			}


		}

	}

	@Override
	protected void onResume() {

		MainActivity.ActionViewExcuteCheck = Activity_Mode.UN_EXCUTE_MODE;
		super.onResume();
	}

	public class AddImgAdp extends BaseAdapter {

		int GalItemBg;
		private Context cont;

		LayoutInflater inflater;

		TextView text1;

		public AddImgAdp(Context c) {

			cont = c;

			TypedArray typArray = obtainStyledAttributes(R.styleable.GalleryTheme);

			GalItemBg = typArray.getResourceId(R.styleable.GalleryTheme_android_galleryItemBackground, 0);

			typArray.recycle();

		}

		public int getCount() {

			return mThumnail.size();

		}

		public Object getItem(int position) {

			return position;

		}

		public long getItemId(int position) {

			return position;

		}

		public View getView(int position, View convertView, ViewGroup parent) {

			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// ImageView imgView = new ImageView(cont);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.id_result_addview, null);
			}
			text1 = (TextView) convertView.findViewById(R.id.txt01);
			ImageView imgView = (ImageView) convertView.findViewById(R.id.imgView1);
			LinearLayout mLinearLayout = (LinearLayout) convertView.findViewById(R.id.linearlayout1);

			imgView.setLayoutParams(new LinearLayout.LayoutParams(480, 377));
			text1.setText(mTotalTxt.get(position));
			imgView.setImageBitmap(mThumnail.get(position));

			imgView.setScaleType(ImageView.ScaleType.FIT_XY);

			return convertView;

		}

	}

	private void DeleteDlg(final int position1) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LogPhotoTab.this);
		dialogBuilder.setTitle(getResources().getString(R.string.delete));

		dialogBuilder.setMessage(getResources().getString(R.string.delete_message));

		if (position1 < mPhoto.size()) {

			dialogBuilder
					.setMessage(getResources().getString(R.string.delete_message) + "\n\n" + mPhotoName.get(position1));

		} else if (position1 >= mPhoto.size() && position1 < mPhoto.size() + mVideo.size()
				&& mPhoto.size() != mPhoto.size() + mVideo.size()) {

			dialogBuilder.setMessage(getResources().getString(R.string.delete_message) + "\n\n"
					+ mVideoName.get(position1 - mPhoto.size()));

		} else if (position1 >= mPhoto.size() + mVideo.size()
				&& position1 < mPhoto.size() + mVideo.size() + mRecoder.size()) {

			dialogBuilder.setMessage(getResources().getString(R.string.delete_message) + "\n\n"
					+ mRecoderName.get(position1 - (mPhoto.size() + mVideo.size())));

		}

		dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {

				EventDBOper DB = new EventDBOper();
				DB.OpenDB();

				if (position1 < mPhoto.size()) {

					File f2d = new File(mPhoto.get(position1));
					f2d.delete();
					mPhoto.remove(position1);
					mPhotoName.remove(position1);

					DB.DeleteUpdateRow(mPhotoName, mEventNumber, EventDBOper.PHOTO);

				} else if (position1 >= mPhoto.size() && position1 < mPhoto.size() + mVideo.size()
						&& mPhoto.size() != mPhoto.size() + mVideo.size()) {

					File f2d = new File(mVideo.get(position1 - mPhoto.size()));
					f2d.delete();

					mVideo.remove(position1 - mPhoto.size());
					mVideoName.remove(position1 - mPhoto.size());

					DB.DeleteUpdateRow(mVideoName, mEventNumber, EventDBOper.VIDEO);

				} else if (position1 >= mPhoto.size() + mVideo.size()
						&& position1 < mPhoto.size() + mVideo.size() + mRecoder.size()) {

					File f2d = new File(mRecoder.get(position1 - (mPhoto.size() + mVideo.size())));
					f2d.delete();

					mRecoder.remove(position1 - (mPhoto.size() + mVideo.size()));
					mRecoderName.remove(position1 - (mPhoto.size() + mVideo.size()));

					DB.DeleteUpdateRow(mRecoderName, mEventNumber, EventDBOper.RECODE);

				}

				DB.EndDB();
				DB = null;

				Load_Thumnail();
				Init_GalleryView();

			}
		});
		dialogBuilder.setNegativeButton("Cancel", null);
		dialogBuilder.setCancelable(false);
		dialogBuilder.show();

	}

	public void RefreshData() {

	}

}
