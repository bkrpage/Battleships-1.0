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

	private HighScoreDB dbScores = new HighScoreDB(getContext());

	public BoardViewGame(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		float diameter = calcDiam();
		float separator = (float) (diameter * SEPARATOR_RATIO);

		int actionAtPos;

		for (int col = 0; col < game.getColumns(); col++) {
			for (int row = 0; row < game.getRows(); row++) {
				Paint paint;
				actionAtPos = game.getOppositePlayerGrid(col, row);
				
				if (actionAtPos == Game.ACTION_MISS) {
					paint = getMissPaint();
				} else if (actionAtPos == Game.ACTION_HIT) {
					paint = getHitPaint();
				} else if (game.getPlayer() == Game.PLAYER_TWO && actionAtPos == Game.ACTION_SHIP){
					paint = getShipPaint();
				} else {
					paint = getBGPaint();
				}

				// Calculates co-ordinates for the grid squares
				float ls = separator + (diameter + separator) * col; // left
				float ts = separator + (diameter + separator) * row; // top
				float rs = separator + diameter + (diameter + separator) * col; // right
				float bs = separator + diameter + (diameter + separator) * row; // bottom
																			
				canvas.drawRect(ls, ts, rs, bs, paint);
			}
		}
		
		// This is the code that draws the info below the grid
		canvas.drawText("Your score is: " + game.getGameScore(game.getPlayer()) , 15, (separator+diameter)* 10 + 25, getTextPaint());
		canvas.drawText(game.getShipBlocksSunk(game.getOppositePlayer()) + " / 17 blocks sunk", 15,(separator+diameter)* 10 + 50, getTextPaint() );

		canvas.drawText("Computer's Score: " + game.getGameScore(game.getOppositePlayer()) , (separator + diameter) * 5, (separator+diameter)* 10 + 25, getTextPaint());
		canvas.drawText(game.getShipBlocksSunk(game.getPlayer()) + " / 17 blocks sunk",(separator + diameter) * 5,(separator+diameter)* 10 + 50, getTextPaint() );
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
			
			// Calculations based on co-ordinates and grid measurements to get the column and row touched
			touchedColumn = (int) Math.floor(touchX
					/ ((separator + diameter) * Game.DEFAULT_COLUMNS) * 10);
			touchedRow = (int) Math.floor(touchY
					/ ((separator + diameter) * Game.DEFAULT_ROWS) * 10);
			
			if (touchedColumn <= 9 && touchedRow <= 9) { // Error prevention - NullPointers if touching outside grid without this statement
				if (game.getOppositePlayerGrid(touchedColumn, touchedRow) == Game.ACTION_SHIP) {

					game.touchGridOf(touchedColumn, touchedRow, Game.ACTION_HIT, oppositePlayer); // ship hit
					game.addToGameScore(Game.SCORE_HIT, currentPlayer);
					game.sinkShipBlock(oppositePlayer);

					if (game.getShipBlocksSunk(oppositePlayer) == 17) {
						// if win - 17 is number of ship blocks.
						dbScores.addScore(game.getStrCurrentPlayer(),game.getGameScore(currentPlayer));
						showRestart(true);
					} else {
						Toast toast = Toast.makeText(getContext(), "Ship Hit", Toast.LENGTH_SHORT);
						toast.show();
					}
					
				} else if (game.getOppositePlayerGrid(touchedColumn, touchedRow) != Game.ACTION_MISS
						&& game.getOppositePlayerGrid(touchedColumn, touchedRow) != Game.ACTION_HIT) {
					// if the co-ord is not already hit or missed.
					game.touchGridOf(touchedColumn, touchedRow, Game.ACTION_MISS, oppositePlayer);
					game.addToGameScore(Game.SCORE_MISS, currentPlayer);
				}
			}
			
			game.computerPlay();
			
			// Checks if the computer has won.
			if(game.getShipBlocksSunk(currentPlayer) == 17){
				showRestart(false);
			}
			
			invalidate(); //redraw
			return true;
		}
	}
	
	/**
	 * This method shows a dialog box to restart the game after a player or
	 * computer win
	 * @param win 	If the human player has won.
	 */
	public void showRestart(boolean win){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		if (win){
			builder.setMessage("You won with a score of " + game.getGameScore(Game.PLAYER_ONE));
		} else {
			builder.setMessage("You lost!");
		}
		builder.setCancelable(false);
		builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int id) {
            	Intent intent = new Intent(getContext(),
						StartMenu.class);
            	//restarts game
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
