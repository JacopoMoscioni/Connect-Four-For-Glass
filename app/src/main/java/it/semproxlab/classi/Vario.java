package it.semproxlab.classi;


import android.graphics.Color;
import android.os.Environment;

import java.io.File;

public class Vario {

    public final static int COLORE_PEDINE_GIOCATORE_R = Color.RED;
    public final static int COLORE_PEDINE_GIOCATORE_Y = Color.YELLOW;
    public final static int COLORE_GRIGLIA = Color.BLUE;
    public final static double PENDENZA_RETTE_SIMILI = Math.PI/8;
    public final static int DIM_ALTEZZA_IMG_INPUT = 700;
    public final static int HOUGH_THRESHOLD = 40;
    public final static int THRESHOLD_AREA_PEDINE = 100;
    public final static int GRIGLIA_NUM_COLONNE = 7;
    public final static int GRIGLIA_NUM_RIGHE = 6;
    public final static int TURNO_UMANO = 1;
    public final static int TURNO_COMPUTER = 2;

    public final static int ERR_GENERICO= -1;
    public final static int ERR_NUM_PEZZI_NON_VALIDO = -2;
    public final static int ERR_GRIGLIA_NON_TROVATA = -3;

    public final static String DIR_PATH = Environment.getExternalStorageDirectory()+ File.separator + "DCIM/Camera/";
}
