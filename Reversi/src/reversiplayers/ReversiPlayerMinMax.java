package reversiplayers;

import java.util.ArrayList;

import javax.naming.TimeLimitExceededException;

import reversi.Arena;
import reversi.Coordinates;
import reversi.GameBoard;
import reversi.ReversiPlayer;
import reversi.Utils;

public class ReversiPlayerMinMax implements ReversiPlayer {
	/**
	 * Die Farbe des Spielers.
	 */
	private int color = 0;
	private long timelimit;
	private long startTime;
	private int otherPlayer = Utils.other(color);

	public ReversiPlayerMinMax() {
		System.out.println("Reversi Player erstellt.");
	}

	private int heuristicPoints(GameBoard gb) {
		return gb.countStones(color) - gb.countStones(otherPlayer);
	}



	private Coordinates calcNextMove(GameBoard gb) {
		startTime = System.currentTimeMillis();
		Coordinates bestCoord = null;
		int max = Integer.MIN_VALUE;
		try {
			for (Coordinates move : getAllPossMoves(gb, color)) {
				int val = minMove(move, Integer.MAX_VALUE, gb.clone(), Integer.MIN_VALUE, Integer.MAX_VALUE);
				if (max < val) {
					max = val;
					bestCoord = move;
				}
			}
		} catch (TimeLimitExceededException e) {
			System.err.println("Got time limit exception");
		}
		System.out.println("returning: " + bestCoord);
		return bestCoord;
	}

	private int minMove(Coordinates coordinate, int depth, GameBoard gb, int alpha, int beta) throws TimeLimitExceededException {
		gb.checkMove(otherPlayer, coordinate);
		gb.makeMove(otherPlayer, coordinate);

		if (depth == 0) {
			return heuristicPoints(gb);
		}

		
		for (Coordinates move : getAllPossMoves(gb, otherPlayer)) {
			
			beta = Math.min(beta, maxMove(move, depth - 1, gb.clone(), alpha, beta));
			if (beta <= alpha )
				break;
			
			if (timelimit <= (System.currentTimeMillis() - startTime)) {
				throw new TimeLimitExceededException();
			}
		}
		return beta;
	}
	
	private int maxMove(Coordinates coordinate, int depth, GameBoard gb, int alpha, int beta) throws TimeLimitExceededException {
		gb.checkMove(color, coordinate);
		gb.makeMove(color, coordinate);

		if (depth == 0) {
			int points = gb.countStones(color) - gb.countStones(Utils.other(color));
			return points;
		}
;

		for (Coordinates move : getAllPossMoves(gb, color)) {
			alpha = Math.max(alpha, minMove(move, depth - 1, gb.clone(), alpha, beta));
			
			if (alpha >= beta)
				break;
			
			if (timelimit >= (System.currentTimeMillis() - startTime)) {
				throw new TimeLimitExceededException();
			}

		}
		return alpha;
	}

	/**
	 * Speichert die Farbe und den Timeout-Wert in Instanzvariablen ab. Diese
	 * Methode wird vor Beginn des Spiels von {@link Arena} aufgerufen.
	 * 
	 * @see reversi.ReversiPlayer
	 */
	public void initialize(int color, long timeout) {
		this.color = color;
		this.otherPlayer = Utils.other(color);
		timelimit = timeout;

		if (color == GameBoard.RED) {
			System.out.println("RandomPlayer ist Spieler RED.");
		} else if (color == GameBoard.GREEN) {
			System.out.println("RandomPlayer ist Spieler GREEN.");
		}
	}

	/**
	 * Macht einen zufälligen zug
	 * 
	 * @see reversi.ReversiPlayer
	 * @return Der Zug.
	 */
	public Coordinates nextMove(GameBoard gb) {
		System.out.println("Next Move:");
		GameBoard gb2 = gb.clone();
		GameBoard gb3 = gb.clone();
		System.out.println("2: " + gb2 + "3:" + gb3);
		System.out.println(gb2.toString().equals(gb3.toString()));
		System.out.println(gb2.hashCode() + " 3:  " + gb3.hashCode());
		return calcNextMove(gb);
	}

	private ArrayList<Coordinates> getAllPossMoves(GameBoard gb, int player) {

		ArrayList<Coordinates> moves = new ArrayList<>();

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Coordinates c = new Coordinates(i, j);
				if (gb.checkMove(player, c)) {
					moves.add(c);
				}
			}
		}
		return moves;
	}
}