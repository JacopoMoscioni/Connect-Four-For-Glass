package it.semproxlab.classi;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class ComputerVisionForza4 {

    private static Mat inputImg;
    private static Mat ritagliata;

    public static int getMoveForImage(Mat immagine, boolean debug)
            throws Exception {

        inputImg = immagine.clone();

        double altezza1 = inputImg.size().height;
        double larghezza1 = inputImg.size().width;


        //ridimensioniamo l'immagine ad una grandezza più gestibile
        double altezza2 = Vario.DIM_ALTEZZA_IMG_INPUT;
        double larghezza2 = ((altezza2 *larghezza1)/altezza1);
        Imgproc.resize(inputImg, inputImg, new Size(larghezza2,altezza2));


        System.out.println("altezza: "+altezza1 + " larghezza: "+larghezza1);
        System.out.println("altezza: "+altezza2 + " larghezza: "+larghezza2);


        Mat temp = inputImg.clone();
        if (debug) salva(temp, "1_originale");

        //rendiamo l'immagine in bianco e nero ponendo di bianco il blu e di nero il resto
        Mat grigliaBiancoNera = binarizza(temp, Vario.COLORE_GRIGLIA, debug);


        ritagliata = elaborazioni(grigliaBiancoNera.clone(), debug);

        if (ritagliata == null){
            return Vario.ERR_GRIGLIA_NON_TROVATA;
        }

        if (debug) salva(ritagliata, "3_ritagliata");



        // individuo le pedine del giocatore Y
        ArrayList<double[]> pedineY = individuazioneGettoni(ritagliata, Vario.COLORE_PEDINE_GIOCATORE_Y,debug);
        System.out.println("Trovate " + pedineY.size() + " pedine del giocatore Y");


        // individuo le pedine del giocatore R
        ArrayList<double[]> pedineR = individuazioneGettoni(ritagliata, Vario.COLORE_PEDINE_GIOCATORE_R,debug);
        System.out.println("Trovate " + pedineR.size() + " pedine del giocatore R");


        for(double[] cerchio : pedineR){
            Core.circle(ritagliata, new Point(cerchio[0],cerchio[1]), (int) cerchio[2], new Scalar(255,0,0), 5);
            Core.circle(ritagliata, new Point(cerchio[0],cerchio[1]), 3, new Scalar(0,0,0), 2);
        }

        for(double[] cerchio : pedineY){
            Core.circle(ritagliata, new Point(cerchio[0],cerchio[1]), (int) cerchio[2], new Scalar(255,255,0), 5);

            Core.circle(ritagliata, new Point(cerchio[0],cerchio[1]), 3, new Scalar(0,0,0), 2);
        }


        if (Math.abs(pedineR.size() - pedineY.size()) > 1) {
            return Vario.ERR_NUM_PEZZI_NON_VALIDO;
        }


        int mossaConsigliata = digitalizzazioneGettoni(ritagliata.size(), pedineR, pedineY);

        System.out.println(mossaConsigliata);

        return mossaConsigliata;
    }

    


    private static Mat binarizza(Mat image, int color, boolean debug){

        //per capire i range da usare per la funzione inRange, mi è venuto in forte
        //aiuto questo sito http://colorizer.org/ per identificare i corretti valori HSV
        //per i colori
        Mat imageHSV = new Mat();
        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_RGB2HSV);
        Mat threshold = new Mat();
        if(color == Vario.COLORE_GRIGLIA){
            Core.inRange(imageHSV, new Scalar(70, 70, 70), new Scalar(120, 255, 255), threshold);
            if (debug) salva(threshold, "2_binarizzata");
        }
        else if(color == Vario.COLORE_PEDINE_GIOCATORE_R){

            //Core.inRange(imageHSV, new Scalar(0, 120, 70), new Scalar(15, 255, 255), threshold);
            Core.inRange(imageHSV, new Scalar(0, 140, 65), new Scalar(15, 255, 255), threshold);

            if (debug) salva(threshold, "4_binarizzata_R");
        }
        else if(color == Vario.COLORE_PEDINE_GIOCATORE_Y){
            Core.inRange(imageHSV, new Scalar(20, 100, 100), new Scalar(30, 255, 255), threshold);
            if (debug) salva(threshold, "4_binarizzata_Y");
        }

        return threshold;
    }



    private static ArrayList<double[]> individuazioneGettoni(Mat sourceImage, int colorePedina, boolean debug) {

        //isoliamo dalla griglia le pedine di un certo colore
        //rendendo bianco quel colore e nero tutto il resto
        Mat boardThreshold = binarizza(sourceImage, colorePedina, debug);

        //troviamo i contorni (come già fatto in precedenza per la griglia)
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(boardThreshold, contours, new Mat(),
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));



        ArrayList<double[]> listaPedineTrovate = new ArrayList<double[]>();
        for (int i = 0; i < contours.size(); i++) {
            //per ogni contornamento (che ipoteticamente rappresentano ciascuno una pedina)
            //andiamo a calcolarne l'area e poi se è sufficientemente grande computiamo
            //il raggio e centro del cerchio incluso nell'area

            double area = Imgproc.contourArea(contours.get(i));
            if (area > Vario.THRESHOLD_AREA_PEDINE) {

                //adesso modelliamo in un cerchio la regione trovata
                //ovvero estrapoliamo il centro e il raggio del cerchio
                //rappresentato dalla regione contornata

                Point c = new Point();
                float[] r = new float[1];

                Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(i).toArray()), c, r );


                //inserisco un double composto da 3 elementi: i primi due indicano le coordinate del centro
                //il terzo elemento indica la lunghezza del raggio
                listaPedineTrovate.add( new double[]{c.x,c.y,r[0]} );
            }
        }

        return listaPedineTrovate;
    }

    private static Mat elaborazioni(Mat grigliaBiancoNera, boolean debug){


        //trovo i contorni della griglia (è facile perché è ben evidenziata dopo il primo filtro)
        ArrayList<MatOfPoint> contorni = new ArrayList<MatOfPoint>();

        Imgproc.findContours(grigliaBiancoNera, contorni, new Mat(),
                Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        //trovo la regione più grande
        int indice = 0;
        double maxArea = 0;
        for (int i = 0; i < contorni.size(); i++) {
            double area = Imgproc.contourArea(contorni.get(i));
            if (area > maxArea) {
                maxArea = area;
                indice = i;
            }
        }

        // creo una matrice che contiene il bordo della griglia individuata.
        //per fare questo mi creo prima una matrice con tutti 0 (nero),
        // e poi ci stampo dentro la griglia con valore 255 (bianco)
        Mat bordoGriglia = Mat.zeros(grigliaBiancoNera.size(), grigliaBiancoNera.type());

        Imgproc.drawContours(bordoGriglia, contorni, indice, new Scalar(255));


        //stampo l'immagine che rappresenta il bordo della griglia
        if (debug) salva(bordoGriglia, "3_bordoIndividuato");

        //ora individuo le linee dritte lungo il bordo
        Mat lines = new Mat();
        Imgproc.HoughLines(bordoGriglia, lines, 1, Math.PI/180, Vario.HOUGH_THRESHOLD);

        ArrayList<double[]> al = new ArrayList<double[]>();
        for (int i = 0; i < lines.cols(); i++) {
            al.add(lines.get(0, i));
        }

        System.out.println("individuate " + al.size()+ " linee.");

        //mi creo il vettore contenente i 4 punti rappresentanti i 4 spigoli della griglia
        Mat spigoliTrovati = trovaSpigoli(lines);
        if (spigoliTrovati == null){
            return null;
        }

        System.out.println(spigoliTrovati.dump());


        //OCCHIO ALL'ORDINE!!! E' importante che combacino con i punti ritornati dentro a spigoliTrovati!!
        //DUE GIORNI PERSI PER QUESTA COSA!!!!

        Mat spigoliNuovi = new Mat(new Size(4, 1), CvType.CV_32FC2);

        spigoliNuovi.put(0, 0, 0, 0);
        spigoliNuovi.put(0, 1, inputImg.size().width, 0);
        spigoliNuovi.put(0, 2, 0, inputImg.size().height);
        spigoliNuovi.put(0, 3, inputImg.size().width, inputImg.size().height);

        System.out.println(spigoliNuovi.dump());

        Mat  trasformata= Imgproc.getPerspectiveTransform(spigoliTrovati, spigoliNuovi);
        Mat grigliaTagliataERaddrizzata = new Mat(inputImg.size(), inputImg.type());
        Imgproc.warpPerspective(inputImg, grigliaTagliataERaddrizzata, trasformata, grigliaTagliataERaddrizzata.size());

        return grigliaTagliataERaddrizzata;
    }



   
    private static Mat trovaSpigoli(Mat lines){


        // Andiamo a creare una lista contenente tutti i punti rappresentanti un'intersezione
        ArrayList<Point> points = new ArrayList<Point>();

        for (int i = 0; i < lines.cols(); i++) {
            for (int j = i + 1; j < lines.cols(); j++) {

                double[] retta1 = lines.get(0, i);
                double[] retta2 = lines.get(0, j);
                //dentro a retta1 e retta2 troviamo 2 valori:
                //il primo è il parametro rho, il secondo è theta

                Point point = trovaIntersezioneTra2Rette(retta1[0],retta1[1],retta2[0],retta2[1]);

                //determino se il punto trovato è un punto buono
                if (
                        //se il point calcolato dalla funzione intersezione è diverso da null
                        point != null
                    &&
                        //e se l'intersezione delle linee è dentro all'immagine lateralmente
                        point.x <= inputImg.width() && point.x >= 0
                      &&
                        //e anche superiormente e inferiormente
                        point.y <= inputImg.height() && point.y >= 0
                      &&
                        //e se inoltre le rette non sono simili (cioè con più o meno la stessa inclinazione)
                        Math.abs(retta1[1] - retta2[1]) >= Vario.PENDENZA_RETTE_SIMILI
                   )
                {
                    //allora considero il punto point una possibile intersezione
                    //e lo inserisco nella lista grezza (penserò poi filtrarli ulteriormente)

                   // System.out.println("X = "+point.x+ ", Y = "+point.y);
                    points.add(point);
                }
            }
        }

        System.out.println("trovate "+points.size() + " intersezioni");
        if (points.size() < 4){
            return null;
        }

        /*
        arrivati a questo punto, dentro a points abbiamo tutti i punti che rappresentano
        una intersezione nella griglia tra le rette trovate.

        Ora dobbiamo andare a riconoscere questi punti per identificare i 4 spigoli

        per estrapolare gli spigoli utilizziamo una clusterizzazione e per questo
        ci viene in aiuto l'algoritmo kmeans già implementato nella ibreria openCV
        */
        
        Mat clusterizzazioni = clusterizza(points);
        
        /*
        dentro a clusterizzazioni abbiamo gli indici da 0 a 3 che identificano ciascun
        gruppo. Ciascuno dei 4 indici indica l'appartenenza al gruppo degli spigoli:
        in alto a destra, in alto a sinistra, in basso a destra e in basso a sinistra

        ora andiamo a fare la media dei punti all'interno di ciascun gruppo, per
        ottenere come risultato un solo punto per ogni gruppo.
        */

        Point[] spigoli = media(clusterizzazioni,points);
       
        /*
        ora che ho i 4 spigoli devo saperli orientare e quindi li ordino per capire
        quale è quello in alto a destra, quello in alto a sinistra e così via:
        */

        Point [] puntiOrdinati = new Point[4];


        Point medio = new Point();
        for (int i = 0; i < 4; i++){

            //sfrutto lo stesso ciclo per fare due cose:
            //inizializzare i punti dentro all'array ordinato
            puntiOrdinati[i] = new Point();

            //e cominciare il calcolo della media di quelli trovati
            medio.x += spigoli[i].x;
            medio.y += spigoli[i].y;
        }
        //finisco il calcolo della media
        medio.x /=4;
        medio.y /=4;


        for (int i = 0; i < 4; i++){
            if (spigoli[i].x < medio.x) //SINISTRA
                if ( spigoli[i].y < medio.y)    //altoSX
                    puntiOrdinati[0] = spigoli[i];
                else                            //bassoSX
                    puntiOrdinati[1] = spigoli[i];
            else                        //DESTRA
            if ( spigoli[i].y < medio.y)        //altoDX
                puntiOrdinati[2] = spigoli[i];
            else                                //bassoDX
                puntiOrdinati[3] = spigoli[i];
        }


        Mat temp = new Mat(new Size(4, 1), CvType.CV_32FC2);

        temp.put(0, 0, puntiOrdinati[0].x, puntiOrdinati[0].y);
        temp.put(0, 1, puntiOrdinati[2].x, puntiOrdinati[2].y);
        temp.put(0, 2, puntiOrdinati[1].x, puntiOrdinati[1].y);
        temp.put(0, 3, puntiOrdinati[3].x, puntiOrdinati[3].y);

        return temp;
    }
    
    private static Point[] media(Mat clusterizzazioni, ArrayList<Point> points){
		Point[] pointTemp = new Point[4];

        for (int i = 0; i<pointTemp.length; i++){
			pointTemp[i] = new Point(0,0);
			
		}

		int [] indexTemp = new int[]{0,0,0,0}; 
		
			
			clusterizzazioni.convertTo(clusterizzazioni, CvType.CV_64FC3);
			int size = (int) (clusterizzazioni.total() * clusterizzazioni.channels());
			double[] temp = new double[size]; 
			clusterizzazioni.get(0, 0, temp);
			
			for (int ii = 0; ii < size; ii++){
			  // System.out.println("LLLL"+(int)temp[ii]);
			   pointTemp[ (int) temp[ii]].x +=points.get(ii).x;
			   pointTemp[ (int) temp[ii]].y +=points.get(ii).y;
			   indexTemp[ (int) temp[ii]]++;
			}
			
			
			//ora ridivido per il numero totale dei punti, così da ottenere la media
			for (int i = 0; i < pointTemp.length; i++){
				pointTemp[i].x = pointTemp[i].x / indexTemp[i];
				pointTemp[i].y = pointTemp[i].y / indexTemp[i];
			}
			return pointTemp;
		}

    private static Mat clusterizza(ArrayList<Point> points){
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER,100,0.1);

        //questa è la variabile che verrà riempita con i gruppi
        Mat clusterizzazioni = new Mat();
        Mat mp1 = Converters.vector_Point2d_to_Mat(points);

        Mat temp = new Mat();
        mp1.convertTo(temp, CvType.CV_32F, 1.0 / 255.0);

        //questo è il numero di classi che vogliamo creare..
        //ce ne servono 4 perché gli spigoli che ci interessano sono 4
        int clusters = 4;
        Core.kmeans(temp, clusters, clusterizzazioni, criteria, 2, Core.KMEANS_PP_CENTERS);

        return clusterizzazioni;
    }


    //funzione che prende in input i parametri rho e theta di due rette e restituisce
    //un point che indica il punto di intersezione (se le rette son parallele ritorna null)
    private static Point trovaIntersezioneTra2Rette(double r1, double t1, double r2, double t2) {
        double cosT1 = Math.cos(t1);
        double sinT1 = Math.sin(t1);
        double cosT2 = Math.cos(t2);
        double sinT2 = Math.sin(t2);

        double det = cosT1 * sinT2 - sinT1 * cosT2;
        double x, y;

        if ( det != 0 ) {
            x = (sinT2 * r1 - sinT1 * r2) / det;
            y = (-cosT2 * r1 + cosT1 * r2) / det;

            return new Point(x,y);
        }
        else{ //determinante = 0 --> rette parallele
            return null;
        }
    }


    private static int digitalizzazioneGettoni(Size griglia, ArrayList<double[]> pedineR, ArrayList<double[]> pedineY) {

        Logica logic = new Logica();


        ArrayList<double[]> temp = new ArrayList<double[]>();
        temp.addAll(pedineR);
        temp.addAll(pedineY);

        float w = (float) griglia.width / Vario.GRIGLIA_NUM_COLONNE;
        float h = (float) griglia.height / Vario.GRIGLIA_NUM_RIGHE;

        for (int i = 0; i < Vario.GRIGLIA_NUM_RIGHE; i++) {
            for (int j = 0; j < Vario.GRIGLIA_NUM_COLONNE; j++) {
                Point centroCellaImmaginaria = new Point( (j * w) + (w / 2), (Vario.GRIGLIA_NUM_RIGHE-1 - i ) * h + (h / 2));

                for (int k = 0; k < temp.size(); k++) {
                    double[] cerchio = temp.get(k);

                    //da raffinare perché non sempre individua tutte le pedine (soprattutto quando la foto
                    //è stata scattata in una prospettiva abbastanza accentuata
                    if (Math.pow(centroCellaImmaginaria.x - cerchio[0], 2) + Math.pow(centroCellaImmaginaria.y - cerchio[1], 2)
                            <= Math.pow(cerchio[2], 2)) {
                        if (k < pedineR.size())
                            logic.inserisciGettone(Vario.TURNO_COMPUTER,j);
                        else
                            logic.inserisciGettone(Vario.TURNO_UMANO,j);
                        break;
                    }
                }
            }
        }

        int [] t = logic.getAltezzeGettoni();
        int [] altezzeGettoni = new int[Vario.GRIGLIA_NUM_COLONNE];

        //copio dentro a altezzeGettoni i valori di t (non faccio un semplice altezzeGettoni = t;
        //perché poi ogni modifica che faccio su t viene riportata anche su altezzeGettoni
        System.arraycopy(t, 0, altezzeGettoni, 0, t.length);


        if (pedineR.size() >= pedineY.size())
            logic.setTurno(Vario.TURNO_UMANO);
        else
            logic.setTurno(Vario.TURNO_COMPUTER);

        System.out.println(logic.stampaGriglia());
        String turno = logic.getTurno();
        System.out.println("turno: "+turno);
        System.out.println("sto pensando alla mossa...");
        logic.pensaMossa();

        int [] altezzeGettoni2 = logic.getAltezzeGettoni();

        int mossa = Vario.ERR_GENERICO;
        for (int i = 0; i < altezzeGettoni.length; i++) {
            if (altezzeGettoni2[i] < altezzeGettoni[i]) {
                mossa = i;
                break;
            }
        }

        //se qualcosa non ha funzionato, ritorno l'errore
        if (mossa == Vario.ERR_GENERICO)
            return Vario.ERR_GENERICO;

        //altrimenti continuo l'elaborazione dell'immagine da visualizzare a schermo all'utente

/*
        Point pt1 = new Point((w * mossa) + (w / 2),0);
        Point pt2 = new Point((w * mossa)+ (w / 2),500);
        Core.line(ritagliata,pt1,pt2,new Scalar(0,0,0),3);
*/
        Scalar coloreNuovaMossa;
        if (turno.equalsIgnoreCase("R"))
            coloreNuovaMossa = new Scalar(255,0,0);
        else
            coloreNuovaMossa = new Scalar(255,255,0);

        Core.circle(ritagliata,new Point((w * mossa) + (w / 2),(h * altezzeGettoni[mossa]) + (h / 2)),45,coloreNuovaMossa,-1);
        Core.circle(ritagliata,new Point((w * mossa) + (w / 2),(h * altezzeGettoni[mossa]) + (h / 2)),45,new Scalar(0,0,0),5);
        salva(ritagliata, "FINALE");

        System.out.println(logic.stampaGriglia());
        System.out.println("turno: "+logic.getTurno());



        return mossa;
    }

    private static void salva(Mat image,String name) {
        Bitmap bmp;
        try {

            bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(image, bmp);
            File temp = new File(Vario.DIR_PATH);
            SaveImage(bmp, temp, name + ".jpg");
            SaveImage(bmp, temp, "TEMP.jpg");
        }
        catch (Exception e){
            Log.d("Exception", e.getMessage());
        }
    }


    private static void SaveImage(Bitmap finalBitmap, File myDir, String name) {

        File file = new File (myDir, name);
        if (file.exists ()) {
            System.out.println(name+" ESISTE");
        }
        else{
            System.out.println(name+" NON ESISTE");
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}