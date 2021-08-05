package android.HH100.CameraUtil;


import android.HH100.LogActivity.LogPhotoTab;
import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.Structure.NcLibrary;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jakewharton.rxbinding2.view.RxView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Camera2 API. Android Lollipop 及以后版本的 Android 使用 Camera2 API.
 * <p>
 * 从https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/
 * com/example/android/camera2basic/Camera2BasicFragment.java拷贝而来.
 * <p>
 * 进行了一些修改, 以文档注释的形式写出.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Camera2Activity extends Activity {

/*
    TextureView mTextureView = (TextureView)findViewById(R.id.texture_camera_preview);
    ImageView mIvCameraButton = (ImageView)findViewById(R.id.iv_camera_button);
    TextView mTvCameraHint = (TextView)findViewById(R.id.tv_camera_hint);
    View mViewDark0 = (View)findViewById(R.id.view_camera_dark0);
    LinearLayout mViewDark1 = (LinearLayout)findViewById(R.id.view_camera_dark1);
*/

/*@BindView(R.id.texture_camera_preview)
TextureView mTextureView;
    @BindView(R.id.iv_camera_button)
    ImageView mIvCameraButton;
    @BindView(R.id.tv_camera_hint)
    TextView mTvCameraHint;
    @BindView(R.id.view_camera_dark0)
    View mViewDark0;
    @BindView(R.id.view_camera_dark1)
    LinearLayout mViewDark1;*/

    TextureView mTextureView;
    ImageButton mIvCameraButton;
    Unbinder mUnbinder;
    View mViewDark0;
    LinearLayout mViewDark1;
    TextView mTvCameraHint;
    File tempFile;
    Bitmap tempBitmap;
    ImageView imgResult;
    RelativeLayout resultLayout;
    Button btnRetry, btnOk;
    RelativeLayout mainLayout;
    LayoutInflater inflater1;
    byte[] byteArray;
    boolean isAddView = false;
    boolean mManualFocusEngaged;
    private final int MSG_TAKE_PICTURES = 181002;
    CameraCharacteristics characteristics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //   setContentView(getContentViewResId());
        //setContentView(R.layout.camera2activity);
        //       mUnbinder = ButterKnife.bind(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainLayout = (RelativeLayout) inflater.inflate(R.layout.camera2activity, null);
        setContentView(mainLayout);

        mTextureView = (TextureView) mainLayout.findViewById(R.id.texture_camera_preview);
        mIvCameraButton = (ImageButton) mainLayout.findViewById(R.id.iv_camera_button);
        mTvCameraHint = (TextView) mainLayout.findViewById(R.id.tv_camera_hint);
        mViewDark0 = (View) mainLayout.findViewById(R.id.view_camera_dark0);
        mViewDark1 = (LinearLayout) mainLayout.findViewById(R.id.view_camera_dark1);

         inflater1 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        resultLayout = (RelativeLayout) inflater1.inflate(R.layout.camera2activity_pictures, null);
        imgResult = (ImageView) resultLayout.findViewById(R.id.IDIMG_RESULT);
        btnRetry = (Button) resultLayout.findViewById(R.id.IDBTN_RETRY);
        btnOk = (Button) resultLayout.findViewById(R.id.IDBTN_OK);
        btnRetry.setOnClickListener(btnClick);
        btnOk.setOnClickListener(btnClick);

        mFile = new File(LogPhotoTab.Media.FolderPath +"/"+getIntent().getStringExtra("file"));
        mTvCameraHint.setText(getIntent().getStringExtra("hint"));
        if (getIntent().getBooleanExtra("hideBounds", false))
        {
            mViewDark0.setVisibility(View.INVISIBLE);
            mViewDark1.setVisibility(View.INVISIBLE);
        }
        mMaxPicturePixels = getIntent().getIntExtra("maxPicturePixels", 3840 * 2160);

        mIvCameraButton.setFocusable(true);
        mIvCameraButton.setFocusableInTouchMode(true);
        mIvCameraButton.requestFocus();
        //mTextureView.setOnTouchListener(this);

        //카메라 버튼
        RxView.clicks(mIvCameraButton)
                .throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> takePicture());
    }


/*    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        NcLibrary.SaveText("dispatchKeyEventdispatchKeyEvent\n");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // keydown logic
            NcLibrary.SaveText("dispatchKeyEvent\n");
            NcLibrary.SaveText("event  "+event+"\n");

            if(event.getAction() == KeyEvent.KEYCODE_ENTER)
            {
                NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTER\n");
                return false;
            }
        }
        if(event.getAction() == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTERR\n");
            return false;
        }
        return super.dispatchKeyEvent(event);
    }*/

/*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        NcLibrary.SaveText("dispatchKeyEventdispatchKeyEvent\n");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // keydown logic
            NcLibrary.SaveText("dispatchKeyEvent\n");
            if(event.getAction() == KeyEvent.KEYCODE_ENTER)
            {
                NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTER\n");
                return true;
            }
        }
        if(event.getAction() == KeyEvent.KEYCODE_ENTER)
        {
            NcLibrary.SaveText("dispatchKeyEvent KEYCODE_ENTERR\n");
            return true;
        }
        return super.dispatchKeyEvent(event);
    }



    public void KeyExecute(final int keyvalue) {

        new Thread(new Runnable() {

            public void run() {

                new Instrumentation().sendKeyDownUpSync(keyvalue);

            }
        }).start();


    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       // NcLibrary.SaveText("onKeyDown \n");
        switch (keyCode)
        {

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;

            case KeyEvent.KEYCODE_ENTER:
             //   NcLibrary.SaveText("KEYCODE_ENTER \n");
                if(!isAddView)
                {
                    takePicture();
                    return false;
                }
                else
                {
                    return true;
                }
        }
        return false;
    }

    //click Listener
    public View.OnClickListener btnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            switch (v.getId())
            {
                case R.id.IDBTN_RETRY:
                    mIvCameraButton.setFocusable(true);
                    btnRetry.setFocusable(true);
                    btnRetry.requestFocus();
                    btnRetry.setFocusableInTouchMode(true);

                    mFinishCalled = false;
                    isAddView =false;
                    if (tempFile!=null && tempFile.exists())
                    {
                        tempFile.delete();
                    }
                    if (mFile!=null && mFile.exists())
                    {
                        mFile.delete();
                    }
                   ((ViewGroup) resultLayout.getParent()).removeView(resultLayout);
                    //mainLayout.removeAllViews();
                    break;

                case R.id.IDBTN_OK:
                    if (tempFile!=null && tempFile.exists())
                    {
                        tempFile.delete();
                    }
                   finish();
                    break;


                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    /**
     * finish()
     */
    volatile boolean mFinishCalled;

    /**
     * 최대 허용 카메라 크기 (픽셀 수)
     */
    long mMaxPicturePixels;

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    static final String TAG = "Camera2BasicFragment";

    /**
     * Camera state: Showing camera preview.
     */
    static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    String mCameraId;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    CameraDevice mCameraDevice;

    /**
     * The {@link Size} of camera preview.
     */
    Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Toast.makeText(Camera2Activity.this, getResources().getString(R.string.camera_falied), Toast.LENGTH_LONG).show();
            mFinishCalled = true;
            finish();
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    File mFile;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the sApp from exiting before closing the camera.
     */
    Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    /**
                     * 判断可以立即拍摄的autoFocusState增加到4种.
                     */
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState ||
                            CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        /**
                         * 判断可以立即拍摄的autoExposureState增加到4种.
                         */
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED ||
                                aeState == CaptureResult.CONTROL_AE_STATE_LOCKED ||
                                aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                  int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        double minRatio = ((double) w) / ((double) h) * 0.95;
        double maxRatio = ((double) w) / ((double) h) * 1.05;
        for (Size option : choices) {
            double ratio = ((double) option.getWidth()) / ((double) option.getHeight());
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    /**
                     * 现在允许宽高比相对于16:9有正负5%的误差.
                     */
                    ratio >= minRatio && ratio <= maxRatio) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings({"ConstantConditions", "SuspiciousNameCombination"})
    void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                 characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) continue;

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) continue;

                // For still image captures, we use the largest available size.
                /*Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());*/
                /**
                   * OutputSizes에서 16 : 9 비율을 만족하는 최대 크기를 찾고 픽셀 수는 3840 * 2160을 초과하지 않습니다.
                   * 찾을 수없는 경우 16 : 9 비율을 만족하는 최대 크기 (픽셀 수가 3840 * 2160을 초과 할 수 있음)를 선택하고 여전히 찾을 수없는 경우 최대 크기를 반환합니다.
                 */
                Size largest = Camera2Utils.findBestSize(map.getOutputSizes(ImageFormat.JPEG), mMaxPicturePixels);
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270)
                            swappedDimensions = true;
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180)
                            swappedDimensions = true;
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH;

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT;

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the camera.
     */
    void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(4, TimeUnit.SECONDS))
                throw new RuntimeException("Time out waiting to lock camera opening.");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Camera2Activity.this, getResources().getString(R.string.camera_falied), Toast.LENGTH_LONG).show();
            mFinishCalled = true;
            finish();
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (null == mCameraDevice) return;

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    try {
                        // Auto focus should be continuous for camera preview.
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // Flash is automatically enabled when necessary.
                        setAutoFlash(mPreviewRequestBuilder);

                        // Finally, we start displaying the camera preview.
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Camera2Activity.this, getResources().getString(R.string.camera_preview_falied), Toast.LENGTH_LONG).show();
                        mFinishCalled = true;
                        finish();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(Camera2Activity.this, getResources().getString(R.string.camera_preview_falied), Toast.LENGTH_LONG).show();
                    mFinishCalled = true;
                    finish();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Camera2Activity.this, getResources().getString(R.string.camera_preview_falied), Toast.LENGTH_LONG).show();
            mFinishCalled = true;
            finish();
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) return;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        //확대해서 저장하기
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    void takePicture() {
        lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    void lockFocus() {
        try
        {
         //   MediaPlayer cameraSound = MediaPlayer.create(this, R.raw.camera_click);


            MainActivity.mAlarmSound = MediaPlayer.create(Camera2Activity.this, R.raw.camera_click);
            MainActivity.mAlarmSound.setVolume(100, 100);
            MainActivity.mAlarmSound.start();

            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    void captureStillPicture() {
        try {
            if (null == mCameraDevice) return;
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d(TAG, mFile.toString());


                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        /**
         * 카메라가 자동 온 / 오프 플래시를 지원하면 플래시를 사용하십시오. 그렇지 않으면 플래시가 항상 꺼집니다.
         */
        if (mFlashSupported)
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }

    //Override in your touch-enabled view (this can be differen than the view you use for displaying the cam preview)
 /*   @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        final int actionMasked = motionEvent.getActionMasked();
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        if (actionMasked != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (mManualFocusEngaged) {
            Log.d(TAG, "Manual focus already engaged");
            return true;
        }

        final Rect sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        //TODO: here I just flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
        final int y = (int)((motionEvent.getX() / (float)view.getWidth())  * (float)sensorArraySize.height());
        final int x = (int)((motionEvent.getY() / (float)view.getHeight()) * (float)sensorArraySize.width());
        final int halfTouchWidth  = 150; //(int)motionEvent.getTouchMajor(); //TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
        final int halfTouchHeight = 150; //(int)motionEvent.getTouchMinor();
        MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth,  0),
                Math.max(y - halfTouchHeight, 0),
                halfTouchWidth  * 2,
                halfTouchHeight * 2,
                MeteringRectangle.METERING_WEIGHT_MAX - 1);

        CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                mManualFocusEngaged = false;

                if (request.getTag() == "FOCUS_TAG") {
                    //the focus trigger is complete -
                    //resume repeating (preview surface will get frames), clear AF trigger
                    mCaptureSession = session;
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);
                    try {
                        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Log.e(TAG, "Manual AF failure: " + failure);
                mManualFocusEngaged = false;
            }
        };

        //first stop the existing repeating request
        try {
            mCaptureSession.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //cancel any existing AF trigger (repeated touches, etc.)
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
        try {
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Now add a new AF trigger with focus region
        if (isMeteringAreaAFSupported()) {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusAreaTouch});
        }
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewRequestBuilder.setTag("FOCUS_TAG"); //we'll capture this later for resuming the preview

        //then we ask for a single request (not repeating!)
        try {
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mManualFocusEngaged = true;

        return true;
    }
*/
    private boolean isMeteringAreaAFSupported()
    {
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
    }
    /**
     * Saves a JPEG {@link Image} into the specified {@link File}.
     */
    class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        final Image mImage;
        /**
         * The file we save the image into.
         */



        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }


        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void run()
        {
            //사진저장
            mFinishCalled = true;
             tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/temp.jpg");
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            try {
                //if (mFile.exists()) mFile.delete();
                FileOutputStream output = new FileOutputStream(tempFile);
                output.write(bytes);
                try {
                    mImage.close();
                } catch (Exception ignored) {
                }
                try {
                    output.close();
                } catch (Exception ignored) {
                }

                //잘린 이미지로 최종 저장
                tempBitmap = BitmapUtils.compressToResolution(tempFile, 1920 * 1080);
                tempBitmap = BitmapUtils.crop(tempBitmap);

                OutputStream out = null;
                try
                {
                    out = new FileOutputStream(mFile);
                    tempBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }


                handler.obtainMessage(MSG_TAKE_PICTURES).sendToTarget();



            } catch (Exception e) {
                e.printStackTrace();
            }



            //mFile = new File(data.getStringExtra("file"));
/*            File mFile1 = null;
            Observable.just(mFile)
                    //파일을 비트 맵으로 디코딩
                    .map(file -> BitmapUtils.compressToResolution(file, 1920 * 1080))
                    .map(BitmapUtils::crop)
                    .map(bitmap -> BitmapUtils.writeBitmapToFile(bitmap, "mFile"))
                    .subscribeOn(Schedulers.io())
                    .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        mFile = file;
                       // Uri uri = Uri.parse("file://" + mFile1.toString());
                      //  ImagePipeline imagePipeline = Fresco.getImagePipeline();
                        //清除该Uri的Fresco缓存. 若不清除，对于相同文件名的图片，Fresco会直接使用缓存而使得Drawee得不到更新.
                   //     imagePipeline.evictFromMemoryCache(uri);
                   //     imagePipeline.evictFromDiskCache(uri);
                       // FrescoUtils.load("file://" + mFile1.toString()).resize(240, 164).into((SimpleDraweeView) imgResult);

                        //파일저장


                        File tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/sdd.jpg");
                        OutputStream out = null;

                        try {
                            tempFile.createNewFile();
                            out = new FileOutputStream(tempFile);

                            Bitmap fullBitmap = BitmapFactory.decodeFile(mFile1.toString());
                            fullBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    });*/
        }
    }


    private final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.what == MSG_TAKE_PICTURES)
            {
                isAddView = true;
                mIvCameraButton.setFocusable(false);
               addContentView(resultLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
               Glide.with(Camera2Activity.this).asBitmap().load(tempFile).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)).thumbnail(1.0f).into(imgResult);


            }
        }

    };

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs)
        {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }



    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (tempFile!=null && tempFile.exists())
        {
            tempFile.delete();
        }
        if (mFile!=null && mFile.exists())
        {
            mFile.delete();
        }

        if(isAddView)
        {
            isAddView = false;
          if(resultLayout!=null)
           {
                ((ViewGroup) resultLayout.getParent()).removeView(resultLayout);
           }
        }
        else
        {
            mFinishCalled = true;
            finish();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
        if (!mFinishCalled) finish();
    }

}