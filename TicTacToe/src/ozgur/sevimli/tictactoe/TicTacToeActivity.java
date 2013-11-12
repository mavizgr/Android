package ozgur.sevimli.tictactoe;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 
 * @author Peter Müller, 2011, www.carpelibrum.de
 *
 */
public class TicTacToeActivity extends Activity implements OnClickListener {
	
	private TicTacToeView ticTacToeView;

	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);
        

        // UI vervollständigen
        ticTacToeView = new TicTacToeView(this); 
        LinearLayout layout = (LinearLayout) findViewById(R.id.linear);
        layout.addView(ticTacToeView);
        
        Button button = (Button) findViewById(R.id.spieler);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.android);
        button.setOnClickListener(this);
        
        button = (Button) findViewById(R.id.beenden);
        button.setOnClickListener(this); 
    }


	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		      case R.id.spieler : ticTacToeView.starteSpiel(true);   // Spieler beginnt 
		                          break;
		      case R.id.android : ticTacToeView.starteSpiel(false);  // Android beginnt
                                  break;
		      case R.id.beenden : // Spiel beenden
		    	                  finish();
		    	                  break;
		   
		}
	}
    
    
}