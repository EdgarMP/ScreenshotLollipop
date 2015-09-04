package edgarmarcopolo.com.screenshotlollipop;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

//Probably I need just an unbound service.
public class ChatHeadService extends Service {
    private WindowManager windowManager;
    private ImageView chatHead;
    private boolean mLongClick = false;
    private MediaProjectionManager mMediaProjectionManager;
    static final int SCREENSHOT_REQUEST = 1;  // The request code

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);


        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.ic_launcher);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;



        windowManager.addView(chatHead, params);

//        chatHead.setOnTouchListener(new View.OnTouchListener() {
//            private int initialX;
//            private int initialY;
//            private float initialTouchX;
//            private float initialTouchY;
//
//            @Override public boolean onTouch(View v, MotionEvent event) {
//
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            initialX = params.x;
//                            initialY = params.y;
//                            initialTouchX = event.getRawX();
//                            initialTouchY = event.getRawY();
//                            return false;
//                        case MotionEvent.ACTION_UP:
//                            mLongClick = true;
//                            return false;
//                        case MotionEvent.ACTION_MOVE:
//                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
//                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
//                            windowManager.updateViewLayout(chatHead, params);
//                            return false;
//                    }
//
//                return false;
//            }
//        });

        chatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v("ChatHeadService", "onClick");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                intent.putExtra("fromHeadChat", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
    }
}