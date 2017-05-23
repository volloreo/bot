package reversiplayers;

import reversi.*;
import reversiplayers.ReversiUtils.GameStateNode;

import java.util.ArrayList;
import java.util.List;

public class AlphaBetaMem implements ReversiPlayer {
	private int color = 0;
	private int other = 0;

	private long maxTime = 0;
	private long tbuffer = 0;
	private long t0 = 0;
	private long tR = 0;
	private int maxDepth = 20;
	private List<Coordinates> allMoves = new ArrayList<Coordinates>(64);
	
	PointCalc PC;

	public void initialize(int color, long timeout) {
		this.color = color;
		this.other = Utils.other(color);
		this.maxTime = timeout - tbuffer;
		
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				allMoves.add(new Coordinates(i,j));
			}
		}
		
		if (color == GameBoard.RED)
			System.out.println("AlphaBetaMem ist Spieler RED.");
		else if (color == GameBoard.GREEN)
			System.out.println("AlphaBetaMem ist Spieler GREEN.");
		PC = new PointCalc();
	}

	public Coordinates nextMove(GameBoard gb) {
		t0 = System.currentTimeMillis();
		tR = maxTime;
		Coordinates curBestcoor = null;
		int i = 1;
		try {
			while (i < maxDepth) {
				i++;
				System.out.println("Current Depth at:" + i);
				// Now get best move
				// System.out.println("Current depth:" + String.valueOf(i));
				// System.out.println("Time used:" +
				// String.valueOf(System.currentTimeMillis() - t0));
				curBestcoor = bestMove(gb.clone(), i);
			}
		} catch (OutOfTimeException e) {
			System.out.println("Timeout! Last Depth at:" + i);
			allMoves.remove(curBestcoor);
			return curBestcoor;
		}
		System.out.println("Last Depth at:" + i);
		allMoves.remove(curBestcoor);
		return curBestcoor;
	}

	private Coordinates bestMove(GameBoard gb, int maxDepth) throws OutOfTimeException {
		CheckTimeOut();

		// Build and "solve" AlphaBeta Tree
		// Get children
		// ArrayList<Coordinates> moves = new
		// ArrayList<Coordinates>(calcAllValidMoves(gb, color));
		// ArrayList<Integer> values = new ArrayList<Integer>();

		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int bestIter = Integer.MIN_VALUE;
		Coordinates bestMove = null;
		try {
			StringBuilder moves = new StringBuilder();
			for (Coordinates move : calcAllValidMoves(gb, color)) {

				GameBoard nextgb = gb.clone();
				nextgb.checkMove(color, move);
				nextgb.makeMove(color, move);
				// values.add(minValue(nextgb, 1, maxDepth, alpha, beta));
				int currValue = minValue(nextgb, 1, maxDepth, alpha, beta);
				
				if (bestIter < currValue) {
					bestIter = currValue;
					bestMove = move;
				}
				moves.append("-" + currValue);
			} 
			System.out.println("Possible Moves: " + moves + " took " + bestIter) ;
		} catch (NoMoveAvaiableException e) {
			return null;
		}
		
		// Get Coordinates with best value -> last child to set alpha
		// System.out.println("Last alpha at:"+ alpha);
		// System.out.println("Last bestVal at:"+ bestVal);
		return bestMove;
	}

	private int maxValue(GameBoard gb, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException {// Sets
																												// alpha
		CheckTimeOut();

		if (nextDepth == maxDepth)
			return pts(gb, color, other);
		// Get all possible children
		// ArrayList<Coordinates> moves = new
		// ArrayList<Coordinates>(calcAllValidMoves(gb, color));
		// if(moves.get(0) == null)
		// return pts(gb, color, other);
		int max = a;
		// Go through children until alpha > beta or all children looked at
		try {
			for (Coordinates move : calcAllValidMoves(gb, color)) {
				GameBoard nextgb = gb.clone();
				nextgb.checkMove(color, move);
				nextgb.makeMove(color, move);

				int cur = minValue(nextgb, nextDepth + 1, maxDepth, a, b);
				// min = Integer.min(b, maxValue(nextgb, nextDepth+1, maxDepth,
				// a, b));
				// Now check if time to cut
				if (cur > max) {
					max = cur;
					if (max >= b)
						break;
				}
			}
		} catch (NoMoveAvaiableException e) {
			return pts(gb, color, other);
		}
		// return alpha
		return max;
	}

	private int minValue(GameBoard gb, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException {// Sets
																												// beta
		CheckTimeOut();

		if (nextDepth == maxDepth)
			return pts(gb, color, other);

		// Go through children until beta < alpha or all children looked at
		int min = b;
		try {
			for (Coordinates move : calcAllValidMoves(gb, other)) {
				GameBoard nextgb = gb.clone();
				nextgb.checkMove(other, move);
				nextgb.makeMove(other, move);
				int cur = maxValue(nextgb, nextDepth + 1, maxDepth, a, b);
				// min = Integer.min(b, maxValue(nextgb, nextDepth+1, maxDepth,
				// a,
				// b));
				// Now check if time to cut
				if (cur < min) {
					min = cur;
					if (min <= a)
						break;
				}
			}
		} catch (NoMoveAvaiableException e) {
			return pts(gb, color, other);
		}
		// return beta
		return min;
	}

	private int pts(GameBoard GB, int us, int other) {
		return PC.calcPoints(GB, us);
	}

	private ArrayList<Coordinates> calcAllValidMoves(GameBoard gb, int player) throws NoMoveAvaiableException {

		ArrayList<Coordinates> res = new ArrayList<Coordinates>(10);

		if (gb.isMoveAvailable(player)) {
			for (Coordinates move: allMoves) {
				if (gb.validCoordinates(move) && gb.checkMove(player, move)) {
				//	System.out.println("Found valid move for \n " + gb + " move: " + move);
					res.add(move);
				}
			}
		} else
			throw new NoMoveAvaiableException();

		return res;
	}

	private void CheckTimeOut() throws OutOfTimeException {
		if (maxTime - (System.currentTimeMillis() - t0) <= 0)
			throw new OutOfTimeException();
	}

	class OutOfTimeException extends Exception {
	}

	class NoMoveAvaiableException extends Exception {
	}
}