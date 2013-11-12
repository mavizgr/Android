package ozgur.sevimli.tictactoe;

import java.util.Random;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;


/**
 * 
 * @author Peter Müller, 2011, www.carpelibrum.de
 *
 */
public class TicTacToeView  extends View  {
	
	private enum ZellenZustand {ANDROID, SPIELER, LEER};
	
	private final int ABSTAND_RAND      = 10;
	private int zellGroesse;
	private Paint liniePaint; 

	private ZellenZustand[][] spielFeld;
	private Bitmap androidIcon;
	private Bitmap spielerIcon;
	private Paint iconPaint;
	private Paint siegMarkierungPaint;
	private Random zufallszahlenGenerator;
	private Spielergebnis gewinnerInfo; 

	
	/**
	 * Konstruktor
	 * @param context
	 */
	public TicTacToeView(Context context) {
		super(context);

		Resources res = getResources();
		Drawable hintergrund = res.getDrawable(R.drawable.hintergrund); 
		this.setBackgroundDrawable(hintergrund);
		zellGroesse = (Math.min(getWidth(), getHeight()) - 2 * ABSTAND_RAND) / 3;
		
		// Symbole laden für die Spielermarken 
 	    androidIcon = BitmapFactory.decodeResource(res, R.drawable.kreuz);  // Android
	    spielerIcon = BitmapFactory.decodeResource(res, R.drawable.kreis); // Spieler
	    
	    // Zeichenstil für das Gitter
		liniePaint = new Paint();
		liniePaint.setColor(Color.RED);
		liniePaint.setStrokeWidth(5);
		liniePaint.setStyle(Style.STROKE);
		
		// Zeichenstil für Sieglinie
		siegMarkierungPaint = new Paint(); 
		siegMarkierungPaint.setColor(Color.YELLOW);
		siegMarkierungPaint.setStrokeWidth(5);
		siegMarkierungPaint.setStyle(Style.STROKE);
		
		// zum Malen der Spielermarken
		iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		// das logische Spielfeld 
		spielFeld = new ZellenZustand[3][3];
		

		
		zufallszahlenGenerator    = new Random(System.currentTimeMillis());


		datenZuruecksetzen();

	}
	
	
	






	/**
	 * Beginnt ein neues Spiel
	 * @param userFirst: true => Benutzer macht den ersten Zug 
	 */
	public void starteSpiel(boolean spielerBeginnt) {
		datenZuruecksetzen();

		if(!spielerBeginnt) {
           macheAndroidZug();
		}
	}
	
	/**
	 * Sucht zufällig ein freies Feld und setzt dort die Marke
	 */
	private void macheAndroidZug() {
		AndroidSpielzugTask androidZug = new AndroidSpielzugTask();
		androidZug.execute();		
	}
	
	
	/**
	 * Spielauswertung anzeigen
	 * @param winner: Gewinner oder null falls noch nicht ermittelt 
	 */
	private void zeigeErgebnis(Spielergebnis gewinnerInfo) {

		if(gewinnerInfo == null) {
			gewinnerInfo = bestimmeGewinner();
		}
		
		String nachricht;
		Resources ressourcen = getResources();
		
		switch(gewinnerInfo.leseZustand()) {
		     case ANDROID  : nachricht = ressourcen.getText(R.string.androidGewinner).toString();
		                     break;
		     case SPIELER  : nachricht = ressourcen.getText(R.string.spielerGewinner).toString();
                             break;
             default       : nachricht = ressourcen.getText(R.string.unentschieden).toString();
                             break;
		}
		
		// grafische Anzeige der Gewinnreihe  
		if(gewinnerInfo.leseZustand() != ZellenZustand.LEER) {
		   zeigeSiegMarkierung(gewinnerInfo);
		}
		
		
		
		AlertDialog alertDialog = new AlertDialog.Builder(this.getContext()).create();
		alertDialog.setTitle(R.string.ergebnisTitel);
		alertDialog.setMessage(nachricht);
		CharSequence okNachricht   = ressourcen.getText(R.string.okButtonLabel);
		android.os.Handler handler =  new Handler(new CallbackHandler());
		Message dummy              = Message.obtain(handler, 0);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, okNachricht, dummy); 

		alertDialog.show();
	}
	
	/** Zeichne Strich durch die Zellen, die eine komplette Reihe bilden 
	 * 
	 */
	private void zeigeSiegMarkierung(Spielergebnis info) {
		this.gewinnerInfo = info;
		invalidate();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Ermittele, wer gewonnen hat
	 * @return
	 */
	private Spielergebnis bestimmeGewinner() {
		
		// Horizontalen testen
		for(int i = 0; i < 3; i++) {
			ZellenZustand zustand = spielFeld[i][0];

			if(zustand == spielFeld[i][1] && zustand == spielFeld[i][2]) {
				return new Spielergebnis(zustand, i , 0, i, 2);
			}
		}
		
		// Vertikalen testen
		for(int i = 0; i < 3; i++) {
			ZellenZustand zustand = spielFeld[0][i];

			if(zustand == spielFeld[1][i] && zustand == spielFeld[2][i]) {
				return new Spielergebnis(zustand, 0, i, 2, i);
			}
		}
		
		// Diagonalen testen
		ZellenZustand zustand = spielFeld[0][0];
		
		if(zustand == spielFeld[1][1] && zustand == spielFeld[2][2]) {
			return new Spielergebnis(zustand, 0, 0, 2, 2);
		}
		
        zustand = spielFeld[0][2];
		
		if(zustand == spielFeld[1][1] && zustand == spielFeld[2][0]) {
			return new Spielergebnis(zustand, 0 , 2, 2, 0);
		}
		
		
		return new Spielergebnis(ZellenZustand.LEER);
	}
	
	
	/**
	 * Bestimme, wieviele Spielfelder noch frei sind 
	 * @return
	 */
	private int bestimmeAnzahlFreieFelder() {
		int num = 0;
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(spielFeld[i][j] == ZellenZustand.LEER) {
					num++;
				}
			}
		}
		
		return num; 
	}
	
	
	
	/**
	 * interne Daten zurücksetzen 
	 */
	private void datenZuruecksetzen() {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				spielFeld[i][j] = ZellenZustand.LEER;
			}
		}
		
		gewinnerInfo = null; 
		invalidate();
	}

	
	
	
    /**
     * Spieler macht seinen Zug
     */
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		
    	int action = event.getAction();
    	
    	if(action == MotionEvent.ACTION_DOWN) {
    		return true;
    	}
    	else if(action == MotionEvent.ACTION_UP) {
    		// Spieler hat losgelassen -> Eingabe verarbeiten
    		float xPos = event.getX() - ABSTAND_RAND;
        	float yPos = event.getY() - ABSTAND_RAND;
        	
        	int xZelle  = (int) (xPos / zellGroesse);  // auf Zeile/Spalte umrechnen 
        	int yZelle  = (int) (yPos / zellGroesse);
        	
        	if(spielFeld[xZelle][yZelle] == ZellenZustand.LEER) {
        		// noch frei -> besetzen
        		spielFeld[xZelle][yZelle] = ZellenZustand.SPIELER;
        		invalidate();
        		
        		Spielergebnis current = bestimmeGewinner();
        		
        		if(current.leseZustand() == ZellenZustand.SPIELER) {
        			// Spieler hat gewonnen
        			zeigeErgebnis(current);
        		}
        		else {
        			// direkt den Android-Zug machen
        			this.macheAndroidZug();
        		}
        	}
        	
        	return true; // Event wurde verarbeitet 
    	}
    
    	
    	
    	
    	return super.onTouchEvent(event);
	}









	/** 
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		
		// Gitter zeichnen	
		float posMax = ABSTAND_RAND + 3 * zellGroesse; 
		float start; 
		
		for(int i = 1; i <= 2; i++) {
			start = ABSTAND_RAND + i * zellGroesse;
			canvas.drawLine(start, ABSTAND_RAND, start, posMax, liniePaint);
			canvas.drawLine(ABSTAND_RAND, start, posMax, start, liniePaint);
		}
		
		int zellGroesse_Halbe = zellGroesse / 2; 
		
		// Spielermarken zeichnen
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				float xPos = ABSTAND_RAND + i * zellGroesse;
				float yPos = ABSTAND_RAND + j * zellGroesse;
				float mitteX = xPos + zellGroesse_Halbe;
				float mitteY = yPos + zellGroesse_Halbe;
				Bitmap spielMarke;
				
				switch(spielFeld[i][j]) {
				      case ANDROID: spielMarke = androidIcon;
				                    break;
				      case SPIELER: spielMarke = spielerIcon;
				                    break;
				      default     : spielMarke = null; 
				}
				
				if(spielMarke != null) {	
    	            int iconWidth_2 = spielMarke.getWidth() / 2;
				    canvas.drawBitmap(spielMarke,  mitteX - iconWidth_2 , 
				    		mitteY - iconWidth_2, iconPaint);
				}

			}
		}
		
		// siegreiche Linie zeichnen 
		if(this.gewinnerInfo != null) {
			int startX = ABSTAND_RAND + zellGroesse_Halbe + gewinnerInfo.leseStartX() * zellGroesse;
			int startY = ABSTAND_RAND + zellGroesse_Halbe + gewinnerInfo.leseStartY() * zellGroesse;
			int endX   = ABSTAND_RAND + zellGroesse_Halbe + gewinnerInfo.leseEndX()   * zellGroesse;
			int endY   = ABSTAND_RAND + zellGroesse_Halbe + gewinnerInfo.leseEndY()   * zellGroesse;
			canvas.drawLine(startX, startY, endX, endY, siegMarkierungPaint);
		}

	}






	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int sizeX = (w - 2 * ABSTAND_RAND) / 3;
        int sizeY = (h - 2 * ABSTAND_RAND) / 3;

        zellGroesse = Math.min(sizeX, sizeY);
    }
	
	
	private class Spielergebnis {
		private ZellenZustand zustand;
		private int startX;
		private int startY;
		private int endX;
		private int endY; 
		
		/**
		 * Konstruktor für Spielergebnis
		 * @param zustand: Spielergebnis
		 * @param startX: x,y von Startzelle
		 * @param startY
		 * @param endX: x,y von Endzelle 
		 * @param endY
		 */
		public Spielergebnis(ZellenZustand zustand, int startX, int startY, int endX, int endY) {
			this.zustand  = zustand;
			this.startX = startX;
			this.startY = startY;
			this.endX   = endX;
			this.endY   = endY; 
		}
		
		
		/**
		 * Konstruktor
		 * @param state: Spielergebnis
		 */
		public Spielergebnis(ZellenZustand zustand) {
			this.zustand = zustand; 
			startX = -1;
			startY = -1;
			endX   = -1;
			endY   = -1; 
		}

		/**
		 * @return der Zustand 
		 */
		public ZellenZustand leseZustand() {
			return zustand;
		}

		/**
		 * @return  startX
		 */
		public int leseStartX() {
			return startX;
		}

		/**
		 * @return  startY
		 */
		public int leseStartY() {
			return startY;
		}

		/**
		 * @return  endX
		 */
		public int leseEndX() {
			return endX;
		}

		/**
		 * @return  endY
		 */
		public int leseEndY() {
			return endY;
		}
		
		
	}
	
	/**
	 * 
	 * Berechnet den Zug für die Android-Seite und zeigt ihn an
	 *
	 */
	private class AndroidSpielzugTask extends AsyncTask<Void, Void, Spielergebnis> {
		
		/**
		 * den Android Zug berechnen
		 * @return Spielauswertung
		 */
	    protected Spielergebnis doInBackground(Void ... args) {

	    	// ein bisschen warten, damit der Spieler nicht überrumpelt wird
	    	try {
	    	   Thread.sleep(1000); // 1 sec.	
	    	}
	    	catch(Exception ex) {
	    	}
	    	
	    	int anzahlFreieFelder           = bestimmeAnzahlFreieFelder();

		      if(anzahlFreieFelder > 0) {
			     while(true) {
				   int x = zufallszahlenGenerator.nextInt(3);
				   int y = zufallszahlenGenerator.nextInt(3);
				
				   if(spielFeld[x][y] == ZellenZustand.LEER) {
				     	// leeres Feld gefunden: jetzt als besetzt markieren
				     	anzahlFreieFelder--;
				   	    spielFeld[x][y] = ZellenZustand.ANDROID;
					    break;
				   }
			    }
		      }
		      
   		    Spielergebnis winnerInfo = bestimmeGewinner();

	        return winnerInfo; 
		   }
	    
	    // Zug anzeigen
	    protected void onPostExecute(Spielergebnis ergebnis) {
	        invalidate();
			
	        int anzahlFrei = bestimmeAnzahlFreieFelder();
	        
			if(ergebnis.leseZustand() != ZellenZustand.LEER 
					|| anzahlFrei == 0) {
	           zeigeErgebnis(null);
			}
	    }


	}
	
	
	
	private class CallbackHandler implements Callback {

		@Override
		public boolean handleMessage(Message arg0) {
			return true;
		}
		
	}
}
