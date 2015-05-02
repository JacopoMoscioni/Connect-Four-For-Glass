package it.semproxlab.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.glass.content.Intents;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import it.semproxlab.R;
import it.semproxlab.classi.ComputerVisionForza4;

public class PreviewActivity extends Activity
{
	public static String TAG = "PreviewActivity";

    private static final int TAKE_PICTURE_REQUEST = 1;
	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;
	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	private GestureDetector mGestureDetector;


	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); 
		}
		catch (Exception e){
			Log.e(TAG, "Camera non disponibile");
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");

        setContentView(R.layout.preview);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPreview = (SurfaceView)findViewById(R.id.preview);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

		mCamera = getCameraInstance();
		if (mCamera != null)
			startPreview();        
	
		mGestureDetector = new GestureDetector(this);

		mGestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					Log.v(TAG, "TAP");
					
					takePicture(); //scattiamo la foto se intercettiamo una pressione breve
					
					return true;
				} else if (gesture == Gesture.TWO_TAP) {
					Log.v(TAG, "TWO_TAP");
					return true;
				} else if (gesture == Gesture.SWIPE_RIGHT) {
					Log.v(TAG, "SWIPE_RIGHT");
					return true;
				} else if (gesture == Gesture.SWIPE_LEFT) {
					return true;
				} else if (gesture == Gesture.LONG_PRESS) {
					Log.v(TAG, "LONG_PRESS");
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					Log.v(TAG, "SWIPE_DOWN");
					return false;
				} else if (gesture == Gesture.SWIPE_UP) {
					Log.v(TAG, "SWIPE_UP");
					return true;
				} else if (gesture == Gesture.THREE_LONG_PRESS) {
					Log.v(TAG, "THREE_LONG_PRESS");
					return true;
				} else if (gesture == Gesture.THREE_TAP) {
					Log.v(TAG, "THREE_TAP");
					return true;
				} else if (gesture == Gesture.TWO_LONG_PRESS) {
					Log.v(TAG, "TWO_LONG_PRESS");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_DOWN) {
					Log.v(TAG, "TWO_SWIPE_DOWN");
					return false;
				} else if (gesture == Gesture.TWO_SWIPE_LEFT) {
					Log.v(TAG, "TWO_SWIPE_LEFT");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
					Log.v(TAG, "TWO_SWIPE_RIGHT");
					return true;
				} else if (gesture == Gesture.TWO_SWIPE_UP) {
					Log.v(TAG, "TWO_SWIPE_UP");
					return true;
				}
				Log.v(TAG,"ahaha");
				return false;
			}
		});
		
		}
	
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	private void configPreview(int width, int height) {   	
		Log.v(TAG, "configPreview");
		Log.v(TAG, mCamera == null ? "mCamera is null" : "mCamera is not null");  
		Log.v(TAG, mPreviewHolder.getSurface() == null ? "mPreviewHolder.getSurface() is null" : "mPreviewHolder.getSurface() is not null");  

		if ( mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			}
			catch (IOException e) {
				Toast.makeText(PreviewActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}

			if ( !mCameraConfigured ) {
				Camera.Parameters parameters = mCamera.getParameters();

				List<int[]> sizes = parameters.getSupportedPreviewFpsRange();
				for (int[] size : sizes) {            	    
					Log.v(TAG, String.format(">>>> getSupportedPreviewFpsRange: %d, %d", size[0], size[1]));
				}

				parameters.setPreviewFpsRange(30000, 30000);
				parameters.setPreviewSize(640, 360);

				mCamera.setParameters(parameters);

				mCameraConfigured = true;
			}
		}
	}



	private void startPreview() {
		Log.v(TAG, "entering startPreview");

		if ( mCameraConfigured && mCamera != null ) {
			Log.v(TAG, "before calling mCamera.startPreview");
			mCamera.startPreview();
			mInPreview = true;
		}
	}


	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated( SurfaceHolder holder ) {
			Log.v(TAG, "surfaceCreated");
		}

		public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
			Log.v(TAG, "surfaceChanged="+width+","+height);
			configPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed( SurfaceHolder holder ) {
			Log.v(TAG, "surfaceDestroyed");
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	};










    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.e("____", "OpenCV loaded successfully");

                        /* Now enable camera view to start receiving frames */
                    //mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };




	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);

		// Re-acquire the camera and start the preview.
		if (mCamera == null) {
			mCamera = getCameraInstance();
			if (mCamera != null) {
				Log.v(TAG, "mCamera!=null");
				configPreview(624, 352);
				startPreview();
			}	
			else
				Log.v(TAG, "mCamera==null");
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "onPause");
		if ( mInPreview ) {
			Log.v(TAG,  "mInPreview is true");
			mCamera.stopPreview();

			mCamera.release();
			mCamera = null;
			mInPreview = false;
		}
		super.onPause();
	}    

	
	//questo è per gestire anche il pulsante fisico!
	//se l'utente preme il pulsante fisico, scattiamo la foto e la gestiamo
	//nell'app (non la facciamo gestire al sistema operativo)
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.v(TAG,  "onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Log.v(TAG,  "onKeyDown11");
            // user tapped touchpad, do something
        	if ( mInPreview ) {
				mCamera.stopPreview();

				mCamera.release();
				mCamera = null;
				mInPreview = false;
			}
        	
			takePicture();

            return true;
        }
        else
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            Log.v(TAG,  "onKeyDown22");
			if ( mInPreview ) {
				mCamera.stopPreview();

				mCamera.release();
				mCamera = null;
				mInPreview = false;
			}
			takePicture();
			return true;


		} else {
            Log.v(TAG,  "onKeyDown33");
			return super.onKeyDown(keyCode, event);
		}
		
		
		}
	
	
	private void takePicture() {
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("YAYAY!!!");

        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            String thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
            String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
            System.out.println("YESSS!!!");

            processPictureWhenReady(picturePath);

            finish();
            // TODO: Show the thumbnail to the user while the full picture is being  processed.
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
            System.out.println("WOOO IT'S READY!!!!!!");

            azione(picturePath);

        } else {
            System.out.println("DAMN IT'S NOT READY YET");

            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);
                        System.out.println("mumble mumble");

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }
    private void azione(String path){

        try {

            Log.e("processPicture", "dentro al try");

            Bitmap cameraBitmap = BitmapFactory.decodeFile(path);
            //cameraBitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.im5);

            Mat ImageMat = new Mat();// ( cameraBitmap.getHeight(), cameraBitmap.getWidth(), CvType.CV_8U, new Scalar(4));
            Utils.bitmapToMat(cameraBitmap, ImageMat);
            System.out.println("FINITO");

                /*
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Uri uri = Uri.parse("android.resource://"+"/drawable/im5");
                Bitmap bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, bmpFactoryOptions);
                Mat ImageMat = new Mat();
                Utils.bitmapToMat(bmp, ImageMat);
*/
            int m =  ComputerVisionForza4.getMoveForImage(ImageMat, true);

            System.out.println("mossa da fare: " + m);

            //TODO: da sistemare sta parte qui...... bisogna fare in modo che se c'è una fila di
            //forza 4 venga evidenziata, e comunque quando capita un errore deve essere notificato
            //meglio di come è ora.......
            Intent intent = new Intent(getBaseContext(), RisultatoActivity.class);
            if (m < 0) {
                intent.putExtra("immagine","ERRORE");
            }
            else
                intent.putExtra("immagine", "FINALE");

            startActivity(intent);

        } catch (Exception e) {
            Log.e("ERR",""+e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Done");

    }
}