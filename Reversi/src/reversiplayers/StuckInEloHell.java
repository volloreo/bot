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
	private long tbuffer = 0;
	private long t0 = 0;
	private long tR = 0;
	private int maxDepth = 8;
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
				curBestcoor = bestMove(new BitGameBoard(gb), i);
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
			//	nextgb.checkMove(color, move);
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
			//	nextgb.checkMove(color, move);
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
				//nextgb.checkMove(other, move);
				nextgb.makeMove(other, move);
				int cur = maxValue(nextgb, nextDepth + 1, maxDepth, a, b);
				//System.out.println("max value " + cur);
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

	private List<Coordinates> calcAllValidMoves(GameBoard gb, int player) throws NoMoveAvaiableException {

		if(1==1)
			return ((BitGameBoard)gb).getMoves(player);
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

	
	private static class BitGameBoard implements GameBoard {

		BitBoard internBoard = null;
		
		public List<Coordinates> getMoves(int player) {
			return internBoard.getValidMoves(player).stream().map(point -> {
				int x = point >> 3;
				
				int y = point & (7);/*
				System.out.println("mapping: " + point);
				System.out.println("X: " + x);
				System.out.println("Y: " + y);*/
				return new Coordinates(y+1,x+1);
			}).collect(Collectors.toList());
		}
		public  BitGameBoard(GameBoard gameBoard) {
			this(new BitBoard());
			
			for (int i = 1; i <= 8; i++) {
				for (int j = 1; j <= 8; j++) {
					try {
						int occupation = gameBoard.getOccupation(new Coordinates(i, j));
						internBoard.setPosition(new Coordinates(i, j), occupation);
					} catch (OutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
	//		System.out.println("player: " + arg0 + " count stones: "+ internBoard.countTokens(arg0) + " \n" + internBoard);
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

	class OutOfTimeException extends Exception {
	}

	class NoMoveAvaiableException extends Exception {
	}
}