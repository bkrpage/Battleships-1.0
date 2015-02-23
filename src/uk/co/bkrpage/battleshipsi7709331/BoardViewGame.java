package uk.co.bkrpage.battleshipsi7709331;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

public class BoardViewGame extends BoardView{
	
	private boolean shipsSet = false;

	private HighScoreDB dbScores = new HighScoreDB(getContext());

	public BoardViewGame(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	
	public void init(){
		super.init();
		
		if (!shipsSet){
			for (int i = 0; i <= 4 ; i++ ){
				if (i <= 1){
					game.placeRandomShip(i + 1, 2);
				} else {
					game.placeRandomShip(i, 2);
				}
			}
			shipsSet = true;
		}
	}
	
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		
		float diameter = calcDiam();
		float separator = (float) (diameter * SEPARATOR_RATIO);

		int actionAtPos;

		for (int col = 0; col < game.getColumns(); col++) {
			for (int row = 0; row < game.getRows(); row++) {
				Paint paint;
				actionAtPos = game.getPlayer2Grid(col, row);

				// TODO Convert this code into correct functions - basically
				// treating it as one player.
				if (actionAtPos == Game.ACTION_MISS) {
					paint = getMissPaint();
				} else if (actionAtPos == Game.ACTION_HIT) {
					paint = getHitPaint();
				} else {
					paint = getBGPaint();
				}

				float ls = separator + (diameter + separator) * col; // left
																		// Coordinate
				float ts = separator + (diameter + separator) * row; // top
																		// coordinate
				float rs = separator + diameter + (diameter + separator) * col; // right
																				// coordinate
				float bs = separator + diameter + (diameter + separator) * row; // bottom
																				// coordinate

				canvas.drawRect(ls, ts, rs, bs, paint);
			}
		}
		
		
		canvas.drawText("Your score is: " + game.getGameScore(game.getPlayer()) + " - Current Highscore: " + dbScores.getHighScore() , 15, (separator+diameter)* 10 + 25, getTextPaint());
		canvas.drawText("Currently on " + game.getStrCurrentPlayer() + "'s turn - Wait for " + game.getStrOppositePlayer() + "'s Turn", 15,(separator+diameter)* 10 + 50, getTextPaint() );
	}

	class mListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			
			float diameter = calcDiam();
			float separator = (float) (diameter * SEPARATOR_RATIO);

			int touchedColumn;
			int touchedRow;

			float touchX = e.getX();
			float touchY = e.getY();
			
			int currentPlayer = game.getPlayer();
			int oppositePlayer = game.getOppositePlayer();
			
			
			touchedColumn = (int) Math.floor(touchX
					/ ((separator + diameter) * Game.DEFAULT_COLUMNS) * 10);
			touchedRow = (int) Math.floor(touchY
					/ ((separator + diameter) * Game.DEFAULT_ROWS) * 10);
			

			if (touchedColumn <= 9 && touchedRow <= 9) { // checks if the player
															// is clicking
															// inside the grid
				if (game.getOppositePlayerGrid(touchedColumn, touchedRow) == Game.ACTION_SHIP) {

					game.touchGridOf(touchedColumn, touchedRow, Game.ACTION_HIT, oppositePlayer); // ship
																					// hit
					game.addToGameScore(Game.SCORE_HIT, currentPlayer);
					game.sinkShipBlock();

					if (game.getShipBlocksSunk() == 17) {
						// if win
						Toast toast = Toast.makeText(getContext(), "You have won this game with a score of " + game.getGameScore(currentPlayer), Toast.LENGTH_LONG);
						toast.show();
						
						dbScores.addScore(game.getStrCurrentPlayer(),game.getGameScore(currentPlayer));
						
						
						showRestart(); // TODO This isnt working 
						
						
					} else {
						Toast toast = Toast.makeText(getContext(), "Ship Hit", Toast.LENGTH_SHORT);
						toast.show();
					}

				} else if (game.getOppositePlayerGrid(touchedColumn, touchedRow) != Game.ACTION_MISS
						&& game.getOppositePlayerGrid(touchedColumn, touchedRow) != Game.ACTION_HIT) {

					game.touchGridOf(touchedColumn, touchedRow, Game.ACTION_MISS, oppositePlayer);
					game.addToGameScore(Game.SCORE_MISS, currentPlayer);
				}
			}

			invalidate();
			return false;
		}
	}
	

	public void showRestart(){

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage("You won!");
		builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	Intent intent = new Intent(getContext(),
						StartMenu.class);

				game = null;
	            getContext().startActivity(intent);
            }
        });

		builder.show();
	}

	GestureDetector bDetector = new GestureDetector(this.getContext(),
			new mListener());

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		boolean result = bDetector.onTouchEvent(event);
		if (!result) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				result = true;
			}
		}
		return result;
	}
}
