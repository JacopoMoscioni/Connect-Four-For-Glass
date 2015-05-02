package it.semproxlab.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import it.semproxlab.R;
import it.semproxlab.classi.Vario;

public class RisultatoActivity extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("RISULTATO", "onCreate");

        setContentView(R.layout.results);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();

        //String ris = intent.getStringExtra("risultato");
        String immagine = intent.getStringExtra("immagine");
        //TextView editText = (TextView)findViewById(R.id.risultato);
        ImageView imageView = (ImageView)findViewById(R.id.immagine);

        //editText.setText(ris);
        Bitmap img;
        if (immagine.equalsIgnoreCase("FINALE")) {
            img = BitmapFactory.decodeFile(Vario.DIR_PATH + "FINALE.jpg");
        }
        else
            img = BitmapFactory.decodeFile(Vario.DIR_PATH + "TEMP.jpg");

        imageView.setImageBitmap(img);

    }

    }