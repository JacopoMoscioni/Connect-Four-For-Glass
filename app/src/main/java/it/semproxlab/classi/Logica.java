package it.semproxlab.classi;

public class Logica {


    final int larghezzaGriglia = Vario.GRIGLIA_NUM_COLONNE;
    final int altezzaGriglia = Vario.GRIGLIA_NUM_RIGHE;
    final int umano = Vario.TURNO_UMANO;
    final int computer = Vario.TURNO_COMPUTER;
    int[][] griglia;
    int[] altezzeGettoni;
    int turno = 1;


    Logica() {
        griglia = new int[larghezzaGriglia][altezzaGriglia];
        altezzeGettoni = new int[larghezzaGriglia];

        for (int i = 0; i < larghezzaGriglia; i++)
            for (int j = 0; j < altezzaGriglia; j++)
                griglia[i][j] = 0;

        System.out.println("Griglia inizializzata");

        for (int i = 0; i < larghezzaGriglia; i++)
            altezzeGettoni[i] = altezzaGriglia - 1;

        System.out.println("Altezza gettoni inizializzata");
    }


    int inserisciGettone(int giocatore, int x) {
        griglia[x][altezzeGettoni[x]]= giocatore;
        altezzeGettoni[x]--;
        if (controlla(x,altezzeGettoni[x]+1,4,giocatore)==true){
            return turno;
        }

        if (turno == 1)
            turno = 2;
        else
            turno = 1;
        return 0;
    }

    String stampaGriglia() {
        String temp =new String();

        for (int j=0; j<altezzaGriglia;j++)
            for (int i=0; i<larghezzaGriglia;i++){
                temp= temp+griglia[i][j]+" ";
                if (i==larghezzaGriglia-1
                        && j<altezzaGriglia-1)
                    temp=temp+"\n";
            }

        temp = temp.replaceAll("0","-");
        temp = temp.replaceAll("1","Y");
        temp = temp.replaceAll("2","R");

        return temp;
    }


    boolean controlla(int coordX, int coordY, int numComb, int turno) {
        int x, y, conta;
        //DIAGONALE 1
        x = coordX;
        y = coordY;
        conta = 1;
        for (int i = ++x, j = --y; ; i++, j--)
            if (i < larghezzaGriglia && j >= 0 && griglia[i][j] == turno)
                conta++;
            else
                break;
        if (conta >= numComb)
            return true;
        else {
            //entro qua dentro perché controllo la parte opposta della diagonale, ma non azzero il conteggio perché il gettone potrebbe essere in una posizione centrale, quindi per vedere se ho vinto devo sommare le pedine di egual colore a destra con le pedine di egual colore a sinistra
            x = coordX;
            y = coordY;
            for (int i = --x, j = ++y; ; i--, j++)
                if (i >= 0 && j < altezzaGriglia && griglia[i][j] == turno)
                    conta++;
                else
                    break;
        }
        if (conta >= numComb)
            return true;
        else // arrivo qui se nella diagonale precedente non ho vinto
            conta = 1; // resetto a 1 la variabile conta e procedo
        //DIAGONALE 2
        x = coordX;
        y = coordY;
        for (int i = --x, j = --y; ; i--, j--)
            if (i >= 0 && j >= 0 && griglia[i][j] == turno)
                conta++;
            else
                break;
        if (conta >= numComb)
            return true;
        else {
            x = coordX;
            y = coordY;
            for (int i = ++x, j = ++y; ; i++, j++)
                if (i < larghezzaGriglia && j < altezzaGriglia
                        && griglia[i][j] == turno)
                    conta++;
                else
                    break;
        }
        if (conta >= numComb)
            return true;
        else
            conta = 1; //resetto di nuovo la variabile conta e procedo
        //ORIZZONTALE
        x = coordX;
        y = coordY;
        for (int i = --x; ; i--)
            if (i >= 0 && griglia[i][y] == turno)
                conta++;
            else
                break;

        if (conta >= numComb)
            return true;
        else {
            x = coordX;
            y = coordY;
            for (int i = ++x; ; i++)
                if (i < larghezzaGriglia && griglia[i][y] == turno)
                    conta++;
                else
                    break;
        }
        if (conta >= numComb)
            return true;
        else
            conta = 1;
        //VERTICALE - è la parte più semplice perché devo solo controllare verso il basso senza preoccuparmi se sono in una posizione centrale o no
        x = coordX;
        y = coordY;
        for (int i = x, j = ++y; ; j++)
            if (j < altezzaGriglia && griglia[i][j] == turno) {
                conta++;
                continue;
            } else
                break;
        if (conta >= numComb)
            return true;
        else
            return false;
    }

    String getTurno(){
        if (turno == 1)
            return "Y";
        else
            return "R";
    }

    void setTurno(int turno){
        this.turno = turno;
    }

    boolean vincePartita(){
        for(int i=0;i<larghezzaGriglia;i++)
            if (posizioneAccettabile(i, -1))
                if (controlla(i,altezzeGettoni[i], 4, computer)){
                    inserisciGettone(computer,i);
                    System.out.println("HO VINTO");
                    return true;
                }
        return false;
    }

    boolean evitaSconfitta(){
        for(int i=0;i<larghezzaGriglia;i++)
            if (posizioneAccettabile(i, -1))
                if (controlla(i, altezzeGettoni[i], 4, umano)){
                    inserisciGettone(computer,i);
                    System.out.println("Evito la sconfitta");
                    return true;
                }
        return false;
    }

    boolean posizioneAccettabile(int x, int altezza){
        //il parametro altezza serve per sapere quante caselle si vuole controllare di una colonna,
        // ad esempio se gli si viene passato -1 significa che deve controllare solo se c'è spazio
        // per una casella, se gli viene passato 0, controlla se ci sono 2 caselle libere ecc...
        if (x<0 || x>larghezzaGriglia-1)
            return false;
        if(altezzeGettoni[x]<=altezza)
            return false;
        else
            return true;
    }

    void pensaMossa(){
        if (!vincePartita())
            if (!evitaSconfitta())
                if (!creaTrappolaNumero1())
                    if (!evitaTrappolaNumero1())
                        if (!trovaCombinazioni(3,computer))
                            if (!trovaCombinazioni(3,umano))
                                eseguoMossaCaso();
    }


    int eseguoMossaCaso(){
        int rand = (int)(larghezzaGriglia * Math.random());
        for (int i=0;i<larghezzaGriglia;i++ ){
            rand=++rand%larghezzaGriglia;
            System.out.println("La mossa a caso è: "+rand);

            int risposta =
                    rinuncioAllaMossaSeFarebbeVincereLavversario(rand);
            if (risposta == 1)
                continue;
            else
            if (risposta == 2)
                return 1;
            else
            if (risposta == 3)
                if (posizioneAccettabile (rand,-1)){
                    inserisciGettone(computer, rand);
                    return 2;
                }
        }
        //se si arriva qui significa due cose: o che la griglia è piena, oppure che purtroppo
        // qualsiasi mossa faccio l'avversario vincerà subito dopo, quindi ora controllo: se la
        // griglia è piena allora è patta, altrimenti forzo l'inserimento nella prima colonna libera,
        // tanto dove la metto so già che perderei (ammesso che il mio avversario si accorga).
        for (int i=0; i<larghezzaGriglia;i++ ){
            if (altezzeGettoni[i] != -1){
                System.out.println("costretto a mettere il gettone in una posizione scomoda, speriamo che l'avversario non se ne accorga");
                inserisciGettone(computer,i);
                break;
            }
        }
        return 100;
    }

    int rinuncioAllaMossaSeFarebbeVincereLavversario(int colonna){
        if (posizioneAccettabile(colonna, 0)){
            inserisciGettone(computer, colonna);
            if(controlla(colonna,altezzeGettoni[colonna],4,umano)){
                rimuoviGettone(colonna);
                System.out.println("rimosso: "+colonna);
                return 1;
            }
            else
                return 2;
        }
        else
            return 3;
    }

    void rimuoviGettone(int x){
        altezzeGettoni[x]++;
        griglia[x][altezzeGettoni[x]]= 0;

        if (turno == umano)
            turno = computer;
        else
            turno = umano;
    }


    boolean trovaCombinazioni(int diQuanto, int diChePlayer){
        int c=0;
        //parto da una mossa a caso e poi effettuo i controlli
        int mossa = 1+(int)((larghezzaGriglia-2) * Math.random());
        while(c<larghezzaGriglia){
            if (posizioneAccettabile(mossa, -1)
                    && mossa >= 0 && mossa < larghezzaGriglia
                    && controlla(mossa,altezzeGettoni[mossa],diQuanto,diChePlayer)){
                if (rinuncioAllaMossaSeFarebbeVincereLavversario(mossa) == 2){
                    String verbo="";
                    if (diChePlayer == computer)
                        verbo = "creato";
                    else
                        verbo = "evitato";
                    System.out.println("ho appena "+verbo+" una combinazione " +
                            "di "+diQuanto+" elementi del player: "+diChePlayer);
                    return true;
                }
            }
            else{//se la mossa non soddisfa i requisiti ne scelgo un'altra
                mossa=++mossa%larghezzaGriglia;
            }
            c++;
        }
        return false;
    }


    boolean evitaTrappolaNumero1(){
        int c=0;
        boolean tolto=false;

        while (c<larghezzaGriglia){
            if (posizioneAccettabile(c,-1)){

                //questa riga perlustra se nella griglia è possibile che l'umano possa fare un terno
                if (c!=0 && c!=larghezzaGriglia-1
                        && controlla(c,altezzeGettoni[c],3,umano)){
                    inserisciGettone(umano,c);

                    if (
                        //questo blocco controlla il caso in cui gli 1 iniziali sono adiacenti e l'1 di prova è a destra: 0 0 ? 1 1 c ? 0 0
                            (c <= (larghezzaGriglia-1)-3 && c >= 0+1 && posizioneAccettabile(c-1,-1) && controlla(c-1,altezzeGettoni[c-1],4,umano) && posizioneAccettabile(c+3,-1) &&
                                    controlla(c+3,altezzeGettoni[c+3],4,umano))
                                    ||
                                    //questo blocco controlla il caso in cui gli 1 iniziali sono adiacenti e l'1 di prova è a sinistra: 0 0 ? c 1 1 ? 0 0
                                    (c <= (larghezzaGriglia-1)-1 && c >= 0+3 && posizioneAccettabile(c-3,-1) && controlla(c-3, altezzeGettoni[c-3], 4, umano) && posizioneAccettabile(c+1,-1) &&
                                            controlla(c+1, altezzeGettoni[c+1], 4, umano))
                                    ||
                                    //questo blocco controlla il caso in cui gli 1 iniziali sono separati e l'1 di prova è in mezzo: 0 0 ? 1 c 1 ? 0 0
                                    (c <= (larghezzaGriglia-1)-2 && c >= 0+2  && posizioneAccettabile(c-2,-1) && controlla(c-2, altezzeGettoni[c-2], 4, umano) && posizioneAccettabile(c+2,-1) &&
                                            controlla(c+2, altezzeGettoni[c+2], 4, umano))
                            ){
                        rimuoviGettone(c);
                        tolto = true;
                        //qui diciamo che il gettone deve essere
                        //inserito solo se  poi non si perderebbe
                        //alla mossa successiva
                        if (evitaSconfitta() == false){
                            inserisciGettone(computer, c);
                            if (evitaSconfitta() == false){
                                return true;
                            }
                            else
                                rimuoviGettone(c);
                        }
                    }
                    if(tolto == false)
                        rimuoviGettone(c);
                }
            }
            c++;
        }
        return false;
    }

    boolean creaTrappolaNumero1(){
        int c,x=c=0;

        while (x<larghezzaGriglia){
            if (posizioneAccettabile(c,-1))

                //questa riga guarda se si può fare un terno
                if (c!=0 && c != larghezzaGriglia-1 &&
                        controlla(c, altezzeGettoni[c], 3, computer)){
                    inserisciGettone(computer, c);

                    if (
                            (c <= (larghezzaGriglia-1)-3 && c >= 0+1 && posizioneAccettabile(c-1,-1) && controlla(c-1, altezzeGettoni[c-1], 4, computer) && posizioneAccettabile(c+3,-1) && controlla(c+3,altezzeGettoni[c+3],4,computer))
                                    ||
                                    (c <= (larghezzaGriglia-1)-1 && c >= 0+3 && posizioneAccettabile(c-3,-1) && controlla(c-3, altezzeGettoni[c-3], 4, computer) && posizioneAccettabile(c+1,-1) && controlla(c+1,altezzeGettoni[c+1],4,computer))
                                    ||
                                    (c <= (larghezzaGriglia-1)-2 && c >= 0+2  && posizioneAccettabile(c-2,-1) && controlla(c-2, altezzeGettoni[c-2], 4, computer) && posizioneAccettabile(c+2,-1) && controlla(c+2,altezzeGettoni[c+2],4,computer))
                            ){

                        //se arrivo qui significa che la trappola è stata creata, ma devo verificare se la mossa è conveniente
                        rimuoviGettone(c);
                        int risposta =
                                rinuncioAllaMossaSeFarebbeVincereLavversario(c);

                        if (risposta == 2 || risposta == 3)
                            return true;
                    }
                    else
                        rimuoviGettone(c);
                }
            x++;
            c= ++c % larghezzaGriglia;
        }
        return false;
    }


    int[] getAltezzeGettoni(){
        return altezzeGettoni;

    }
}

