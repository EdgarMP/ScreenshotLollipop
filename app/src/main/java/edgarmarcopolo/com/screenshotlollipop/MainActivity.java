package edgarmarcopolo.com.screenshotlollipop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends Activity {

    Button showChatHead;
    Button hideChatHead;

    private MediaProjection mProjection;
    private MediaProjectionManager mProjectionManager;
    private VirtualDisplay mVirtualDisplay;
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int DISPLAY_WIDTH = 480;
    private static final int DISPLAY_HEIGHT = 640;
    private int mScreenDensity;
    private Surface mSurface;
    private SurfaceView mSurfaceView;
    private int mResultCode;
    private Intent mResultData;
    private boolean fromHeadChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.v("MainActivity", "StartProjection onCreate");


        //Densidad de la pantalla
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        fromHeadChat = getIntent().getBooleanExtra("fromHeadChat", false);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        if(fromHeadChat){
            Log.i("FROM HEAD CHAT", ":D");
            mSurfaceView = new SurfaceView(this);
            mSurface = mSurfaceView.getHolder().getSurface();
            startProjection();
            //finish();

        }else{
            Log.i("NOT FROM HEAD CHAT", ":(");
            setContentView(R.layout.activity_main);

            mSurfaceView = (SurfaceView) findViewById(R.id.surface);
            mSurface = mSurfaceView.getHolder().getSurface();

            //UI Buttons
            showChatHead = (Button) findViewById(R.id.buttonShow);
            hideChatHead = (Button) findViewById(R.id.buttonHide);


            Log.v("MainActivity", "StartProjection onCreate");

            //Starts ChatHeadService
            showChatHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent i = new Intent(getApplicationContext(), ChatHeadService.class);
                    startService(i);
                }
            });

            hideChatHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Do nothing for now
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("MainActivity", "onActivityResult");
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i("MainActivity", "User cancelled");
                Toast.makeText(this, "Usuario Cancelo", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i("MainActivity", "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();

            finish();
        }
    }

    private void setUpMediaProjection() {
        mProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
    }


    public void startProjection() {
        Log.v("MainActivity", "StartProjection");
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        //VirtualDisplay virtualDisplay = createVirtualDisplay();
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener{

        @Override
        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireLatestImage();

            //Saving image
            if(img!=null){
                final Image.Plane[] planes = img.getPlanes();
                if(planes[0]!=null){
                    Log.i("Planes[0]", "NOT NULL");
                }
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * DISPLAY_WIDTH;

                //Create bitmap
                Bitmap bmp = Bitmap.createBitmap(DISPLAY_WIDTH + rowPadding / pixelStride, DISPLAY_HEIGHT, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);
                img.close();

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 40, bytes);


                String root = Environment.getExternalStorageDirectory().toString();
                File fileDir = new File(root+"/screenshotApp");
                File file = new File(File.separator+"screenshot.jpg");

                if (!fileDir.exists()){
                    if (!fileDir.mkdirs()){
                        Log.e("MainActivity", "failed to create directory");
                    }
                }else{
                    Log.i("MainActivity", fileDir.getPath()+File.separator+file);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(fileDir.getPath()+file);
                        fileOutputStream.write(bytes.toByteArray());
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setUpVirtualDisplay() {
        Log.i("MainActivity", "Setting up a VirtualDisplay: " + mSurfaceView.getWidth() + "x" + mSurfaceView.getHeight() + " (" + mScreenDensity + ")");

        ImageReader imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, PixelFormat.RGBA_8888, 2);

        mVirtualDisplay = mProjection.createVirtualDisplay("ScreenCapture",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null
                , null);

        imageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.e("onDestroy", "DESTROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOY");
//        if(fromHeadChat){
//            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//            startProjection();
//        }
//        //
    }
}
