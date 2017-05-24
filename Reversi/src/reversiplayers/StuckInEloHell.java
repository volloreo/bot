package reversiplayers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import reversi.Coordinates;
import reversi.GameBoard;
import reversi.OutOfBoundsException;
import reversi.ReversiPlayer;
import reversi.Utils;

public class StuckInEloHell implements ReversiPlayer {

	private int color = 0;
	private int other = 0;

	private long maxTime = 0;
	private long tbuffer = 20;
	private long t0 = 0;

	private int startDepth = 5;
	private int maxDepth = 20;
	
	private int level = 0;

	PointCalc PC;

	public void initialize(int color, long timeout) {
		this.color = color;
		this.other = Utils.other(color);
		this.maxTime = timeout - tbuffer;

		if (color == GameBoard.RED)
			System.out.println("AlphaBetaMem ist Spieler RED.");
		else if (color == GameBoard.GREEN)
			System.out.println("AlphaBetaMem ist Spieler GREEN.");

		PC = new PointCalc();
	}

	public Coordinates nextMove(GameBoard gb) {
		t0 = System.currentTimeMillis();

		Coordinates curBestcoor = null;

		int i = startDepth;
		level += 2;

		try {
			while (i < maxDepth) {
				i++;
				System.out.println("Current Depth at:" + i);
				curBestcoor = bestMove(new BitGameBoard(gb), i);
			}
			
		} catch (OutOfTimeException e) {
			System.out.println("Timeout! Last Depth at:" + i);
			return curBestcoor;
			
		} catch (Throwable t) {
			// Better safe than sorry. Dont want a NPE failing the game
			for (int j = 1; j <= 8; j++) {
				for (int j2 = 1; j2 <= 8; j2++) {
					if (gb.checkMove(color, new Coordinates(j, j2)))
						return new Coordinates(j, j2);
				}
			}
		}

		if (gb.checkMove(color, curBestcoor)) {
			// Better safe than sorry
			for (int j = 1; j <= 8; j++) {
				for (int j2 = 1; j2 <= 8; j2++) {
					if (gb.checkMove(color, new Coordinates(j, j2)))
						return new Coordinates(j, j2);
				}
			}
		}
		System.out.println("Last Depth at:" + i);
		return curBestcoor;
	}

	private Coordinates bestMove(BitGameBoard gb, int maxDepth) throws OutOfTimeException {
		CheckTimeOut();

		int initialAlpha = -300;
		int initialBeta = 300;
		
		

		Coordinates bestMove = null;
		StringBuilder moves = new StringBuilder();
		int bestIter = initialAlpha;
		boolean repeat = false;
		
		do {
			repeat = false;
			int alpha = initialAlpha;
			int beta = initialBeta;
			System.out.println("alpha: " + alpha);
			System.out.println("beta: " + beta);
			bestIter = initialAlpha;
			
			for (Integer move : calcAllValidMoves(gb, color)) {

				BitGameBoard nextgb = (BitGameBoard) gb.clone();
				nextgb.makeMove(color, move);
				int currValue = minValue(nextgb, 1, maxDepth, alpha, beta);

				if (bestIter < currValue) {
					bestIter = currValue;
					alpha = currValue;
					int x = move >> 3;
					int y = move & (7);

					bestMove = new Coordinates(y + 1, x + 1);
				}
				moves.append("-" + currValue);
			}
		
		// We cut to sharp, need to do this again
			if (bestIter == initialAlpha) {
				initialAlpha = (int) (1.5 * initialAlpha);
				repeat = true;
				System.out.println("To sharp knives. lets grab another pair of scissores (bestIter = intialAlpha)");
			} else if (bestIter == initialBeta){
				initialBeta = (int) (1.5*initialBeta);
				repeat = true;
				System.out.println("To sharp knives. lets grab another pair of scissores (bestIter = initialBeta)");
			}
		} while (repeat);
		System.out.println("Possible Moves: " + moves + " took " + bestIter);

		return bestMove;
	}

	private int maxValue(BitGameBoard gb, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException {// Sets
																												// alpha
		CheckTimeOut();

		if (nextDepth == maxDepth)
			return pts(gb, color, other);
		int max = a;

		List<Integer> moves = calcAllValidMoves(gb, color);

		if (moves.isEmpty())
			return pts(gb, color, other);

		for (Integer move : moves) {
			BitGameBoard nextgb = (BitGameBoard) gb.clone();
			nextgb.makeMove(color, move);

			// Last Move imporvement
			
			int cur = minValue(nextgb, nextDepth + 1, maxDepth, a, b);
			
			
			if (cur > max) {
				max = cur;
				if (max >= b)
					break;
			}
		}
		// return alpha
		return max;
	}

	private int minValue(BitGameBoard gb, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException {// Sets
																												// beta
		CheckTimeOut();

		if (nextDepth == maxDepth)
			return pts(gb, color, other);

		// Go through children until beta < alpha or all children looked at
		int min = b;

		List<Integer> moves = calcAllValidMoves(gb, other);

		if (moves.isEmpty())
			return pts(gb, color, other);

		for (Integer move : moves) {
			BitGameBoard nextgb = (BitGameBoard) gb.clone();
			// nextgb.checkMove(other, move);
			nextgb.makeMove(other, move);
			
			int cur = maxValue(nextgb, nextDepth + 1, maxDepth, min, min + 1);
			
			// Now check if time to cut
			if (cur < min) {
				min = cur;
				if (min <= a)
					break;
			}
		}

		// return beta
		return min;
	}

	private int pts(GameBoard GB, int us, int other) {
		return PC.calcPoints(GB, us, level);
	}

	private List<Integer> calcAllValidMoves(BitGameBoard gb, int player) {
		return ((BitGameBoard) gb).getIntegerMoves(player);
	}

	class OutOfTimeException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	class NoMoveAvaiableException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Wrapper for the BitGameBoard
	 * 
	 * @author René Zurbrügg
	 *
	 */
	private static class BitGameBoard implements GameBoard {

		BitBoard internBoard = null;

		/*
		 * public List<Coordinates> getMoves(int player) { return
		 * internBoard.getValidMoves(player).stream().map(point -> { int x =
		 * point >> 3;
		 * 
		 * int y = point & (7); return new Coordinates(y + 1, x + 1);
		 * }).collect(Collectors.toList()); }
		 */

		public List<Integer> getIntegerMoves(int player) {
			return internBoard.getValidMoves(player);
		}

		public void makeMove(int player, int move) {
			internBoard.makeMove(player, move);
		}

		public BitGameBoard(GameBoard gameBoard) {
			this(new BitBoard());

			for (int i = 1; i <= 8; i++) {
				for (int j = 1; j <= 8; j++) {
					try {
						int occupation = gameBoard.getOccupation(new Coordinates(i, j));
						internBoard.setPosition(new Coordinates(i, j), occupation);
					} catch (OutOfBoundsException e) {
						throw new RuntimeException();
					}
				}
			}
		}

		public BitGameBoard(BitBoard internBoard) {
			this.internBoard = internBoard;
		}

		@Override
		public boolean checkMove(int arg0, Coordinates arg1) {
			return internBoard.isValidMove(arg1, arg0);
		}

		@Override
		public BitGameBoard clone() {
			return new BitGameBoard(internBoard.copyBoard());
		}

		@Override
		public int countStones(int arg0) {
			// System.out.println("player: " + arg0 + " count stones: "+
			// internBoard.countTokens(arg0) + " \n" + internBoard);
			return internBoard.countTokens(arg0);
		}

		@Override
		public int getOccupation(Coordinates arg0) throws OutOfBoundsException {
			return internBoard.getPosition(arg0);
		}

		@Override
		public int getSize() {
			return 64;
		}

		@Override
		public boolean isFull() {
			return false;
		}

		@Override
		public boolean isMoveAvailable(int arg0) {
			return internBoard.hasValidMove(arg0);
		}

		@Override
		public void makeMove(int player, Coordinates coord) {
			internBoard.makeMove(player, internBoard.coordinateToPositionInteger(coord));
		}

		@Override
		public boolean validCoordinates(Coordinates arg0) {
			return true;
		}

	}

	private void CheckTimeOut() throws OutOfTimeException {
		if (maxTime - (System.currentTimeMillis() - t0) <= 0)
			throw new OutOfTimeException();
	}
}