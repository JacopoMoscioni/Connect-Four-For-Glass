package it.semproxlab.template;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.View;
import android.util.Log;

public class AppDrawer implements SurfaceHolder.Callback {
    private static final String TAG = "AppDrawer";
    private final AppViewer mView;
    private SurfaceHolder mHolder;

    public AppDrawer(Context context) {
    	Log.i(TAG, "AppDrawer Constructor");
        mView = new AppViewer(context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	Log.i(TAG, "surfaceChanged, width=" + width + ", height=" + height);
    	// Measure and layout the view with the canvas dimensions.
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

    	Log.i(TAG, "measuredWidth="+widthMeasureSpec+", measuredHeight="+heightMeasureSpec);
        mView.measure(widthMeasureSpec, heightMeasureSpec);
    	Log.i(TAG, "getMeasuredWidth="+mView.getMeasuredWidth()+", getMeasuredHeight="+mView.getMeasuredHeight());
        mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());        
        draw(mView);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	Log.i(TAG, "surfaceCreated");
        mHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.i(TAG, "surfaceDestroyed");
        mHolder = null;
    }

    private void draw(View view) {
    	Log.i(TAG, "draw");
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
        	Log.i(TAG, "canvas is not null");
            view.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
        else {
        	Log.i(TAG, "canvas is null");
        }
    }	

    
}
