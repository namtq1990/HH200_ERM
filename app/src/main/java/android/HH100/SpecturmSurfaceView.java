package android.HH100;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.HH100.Structure.NcLibrary;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpecturmSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback {


    SurfaceHolder holder = null;
    public Camera camera;
    Bitmap mSpcImage = null;
    // ViewThread mThread;
    public SpecturmSurfaceView(Context context,AttributeSet attrs,int defStyle) {
        super(context,attrs,defStyle);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setWillNotDraw(false);

        // mThread = new ViewThread(holder, context);

    }
    public SpecturmSurfaceView(Context context ,AttributeSet attributeSet) {
        super(context,attributeSet);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setWillNotDraw(false);
        //  mThread = new ViewThread(holder, context);

    }
    public SpecturmSurfaceView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setWillNotDraw(false);
        // mThread = new ViewThread(holder, context);

    }

    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width,int height) {
        Camera.Parameters parameters = camera.getParameters();
        //        parameters.setPreviewSize(w, h);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // mThread.setRunning(true);
        //mThread.start();

        camera = Camera.open();
        try {
            camera.setPreviewDisplay(holder);


            camera.setPreviewCallback(new PreviewCallback() {

                public void onPreviewFrame(byte[] data, Camera arg1) {
                    /*FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));    
                        outStream.write(data);
                        outStream.close();
                       
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                    }
                        SpecturmSurfaceView.this.invalidate();*/
                }
            });

        } catch (IOException e) {
            NcLibrary.Write_ExceptionLog(e);
        }

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureSize(1280, 720);
        camera.setParameters(parameters);

    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera = null;
	  /*  boolean done = true;
	
	    while (done) {
			try {
				mThread.join();
				done = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	}
			
		}*/
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            camera.takePicture(null, null, takePicture);
        }
        return super.onTouchEvent(event);
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p= new Paint(Color.RED);

        p.setColor(Color.RED);
        p.setTextSize(40);

        canvas.drawText("PREVIEW", 40, 20, p );
        if(mSpcImage != null){
            canvas.drawBitmap(mSpcImage, new Rect(0,0,mSpcImage.getWidth(),mSpcImage.getHeight()),
                    new Rect(10,10,300,300), p);
        }
    }

    public void Set_Spectrum_Bitmap(Bitmap image) {
        mSpcImage = image;

    }
    private android.hardware.Camera.PictureCallback takePicture = new android.hardware.Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
        }
    };
  /*  public class ViewThread extends Thread {

        SurfaceHolder mHolder;                  // SurfaceHolder를 저장할 변수
        public ViewThread(SurfaceHolder holder, Context context){
            mHolder = holder;
        }

        @Override
        public void run() {

            Canvas canvas = null;
            while(true){
                canvas = mHolder.lockCanvas();  

                try {
                    synchronized (mHolder) {    

                    	 Paint p= new Paint(Color.RED); 
                         
                         
                         p.setStrokeCap(Paint.Cap.BUTT);
                         p.setTextSize(20);
                               
                         canvas.drawText("PREVIEW", 10, 10, p );

                    }
                } finally {                                 
                    if(canvas != null){ 
                        mHolder.unlockCanvasAndPost(canvas);
                   }
                }
            }
        }
    }*/
}
