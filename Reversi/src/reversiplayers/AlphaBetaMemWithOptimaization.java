package reversiplayers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

import reversi.Coordinates;
import reversi.GameBoard;
import reversi.OutOfBoundsException;
import reversi.ReversiPlayer;
import reversi.Utils;
import reversiplayers.IMemMan.NoFreeNodesException;

public class AlphaBetaMemWithOptimaization implements ReversiPlayer {
	private int color = 0;
	private int other = 0;

	private long maxTime = 0;
	private long tbuffer = 1000;
	private long t0 = 0;
	private long tR = 0;
	private int startDepth = 3;
	private int maxDepth = 10;
	
	private static List<Coordinates> allMoves = new ArrayList<>(Arrays.asList(
		new Coordinates(1,1),
		new Coordinates(1,2),
		new Coordinates(1,3),
		new Coordinates(1,4),
		new Coordinates(1,5),
		new Coordinates(1,6),
		new Coordinates(1,7),
		new Coordinates(1,8),
		new Coordinates(2,1),
		new Coordinates(2,2),
		new Coordinates(2,3),
		new Coordinates(2,4),
		new Coordinates(2,5),
		new Coordinates(2,6),
		new Coordinates(2,7),
		new Coordinates(2,8),
		new Coordinates(3,1),
		new Coordinates(3,2),
		new Coordinates(3,3),
		new Coordinates(3,4),
		new Coordinates(3,5),
		new Coordinates(3,6),
		new Coordinates(3,7),
		new Coordinates(3,8),
		new Coordinates(4,1),
		new Coordinates(4,2),
		new Coordinates(4,3),
		new Coordinates(4,4),
		new Coordinates(4,5),
		new Coordinates(4,6),
		new Coordinates(4,7),
		new Coordinates(4,8),
		new Coordinates(5,1),
		new Coordinates(5,2),
		new Coordinates(5,3),
		new Coordinates(5,4),
		new Coordinates(5,5),
		new Coordinates(5,6),
		new Coordinates(5,7),
		new Coordinates(5,8),
		new Coordinates(6,1),
		new Coordinates(6,2),
		new Coordinates(6,3),
		new Coordinates(6,4),
		new Coordinates(6,5),
		new Coordinates(6,6),
		new Coordinates(6,7),
		new Coordinates(6,8),
		new Coordinates(7,1),
		new Coordinates(7,2),
		new Coordinates(7,3),
		new Coordinates(7,4),
		new Coordinates(7,5),
		new Coordinates(7,6),
		new Coordinates(7,7),
		new Coordinates(7,8),
		new Coordinates(8,1),
		new Coordinates(8,2),
		new Coordinates(8,3),
		new Coordinates(8,4),
		new Coordinates(8,5),
		new Coordinates(8,6),
		new Coordinates(8,7),
		new Coordinates(8,8)
	));
	
	private int headNodeId = -1;
	private int currentNodeCalculating = -1;
	
	PointCalc PC;
	IMemMan<GameBoardImpl> memManager;

	public void initialize(int color, long timeout) {
		this.color = color;
		this.other = Utils.other(color);
		this.maxTime = timeout - tbuffer;
		
		if (color == GameBoardImpl.RED)
			System.out.println("AlphaBetaMem ist Spieler RED.");
		else if (color == GameBoardImpl.GREEN)
			System.out.println("AlphaBetaMem ist Spieler GREEN.");
		
		
		PC = new PointCalc();
		memManager = MemFactory.createVirtGameBoardMem(16000000);
		
	}

	public Coordinates nextMove(GameBoard gb) {
		t0 = System.currentTimeMillis();
		tR = maxTime;
		
		System.out.println("NextMove: GameBoard: " + gb);
		if (headNodeId == -1) {
			System.out.println("No head ndoe set. going to create new GameBoardImpl");
			try {
				headNodeId = memManager.newNode(new GameBoardImpl(gb));
			} catch (NoFreeNodesException e) {
				System.err.println("NO fre nodes Exception in nextMove!");
			}
		} else {
			for (Integer address : memManager.getGrandchildren(headNodeId)) {
				if (memManager.get(address).equals(gb)) {
					System.out.println("Found current board state: " + memManager.get(address));
					System.out.println("Last Move: " + memManager.get(address).getLastMove());
					headNodeId = address;
					break;
				}
			}
		}

		memManager.setHeadNode(headNodeId);
		
		Coordinates curBestcoor = null;
		int i = startDepth;
		try {
			while (i < maxDepth) {
				i++;
				System.out.println("Current Depth at:" + i);
				// Now get best move
				// System.out.println("Current depth:" + String.valueOf(i));
				// System.out.println("Time used:" +
				// String.valueOf(System.currentTimeMillis() - t0));
				curBestcoor = bestMove(headNodeId, i);
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

	private Coordinates bestMove(int headId, int maxDepth) throws OutOfTimeException {
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
			
			GameBoardImpl headBoard = memManager.get(headNodeId);
			
			List<Integer> children = memManager.getChildren(headNodeId);
			
			if (children.isEmpty()) {
				for (Coordinates move : headBoard.getAllMoves(color)) {

					GameBoardImpl nextgb = headBoard.clone();
					
					int address;
					try {
						address = memManager.newNode(nextgb);
						memManager.linkNodes(headNodeId, address);
					} catch (NoFreeNodesException e) {
						return bestMove;
					}
					
					nextgb.checkMove(color, move);
					nextgb.makeMove(color, move);
					// values.add(minValue(nextgb, 1, maxDepth, alpha, beta));
					int currValue = minValue(address, 1, maxDepth, alpha, beta);
					
					if (bestIter < currValue) {
						bestIter = currValue;
						bestMove = move;
					}
					moves.append("-" + currValue);
				} 
				System.out.println("Possible Moves: " + moves + " took " + bestIter) ;
				return bestMove;
			} else {
				for (Integer child : children) {
					CheckTimeOut();
					int currValue = minValue(child, 1, maxDepth, alpha, beta);
					
					if (bestIter < currValue) {
						bestIter = currValue;
						bestMove = memManager.get(child).getLastMove();
					}
					moves.append("-" + currValue);
				}
			}
			
			
			
		/*	//TODO;
			for (Coordinates move : calcAllValidMoves(impl, color)) {

				GameBoardImpl nextgb = impl.clone();
				
				int address;
				try {
					address = memManager.newNode(nextgb);
				} catch (NoFreeNodesException e) {
					return bestMove;
				}
				
				nextgb.checkMove(color, move);
				nextgb.makeMove(color, move);
				// values.add(minValue(nextgb, 1, maxDepth, alpha, beta));
				int currValue = minValue(address, 1, maxDepth, alpha, beta);
				
				if (bestIter < currValue) {
					bestIter = currValue;
					bestMove = move;
				}
				moves.append("-" + currValue);
			} 
			System.out.println("Possible Moves: " + moves + " took " + bestIter) ;*/
			
		} catch (NoMoveAvaiableException e) {
			return null;
		}
		
		// Get Coordinates with best value -> last child to set alpha
		// System.out.println("Last alpha at:"+ alpha);
		// System.out.println("Last bestVal at:"+ bestVal);
		return bestMove;
	}

	private int maxValue(int GameBoardImplAdress, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException {// Sets
																												// alpha
		CheckTimeOut();
		
		GameBoardImpl gb = memManager.get(GameBoardImplAdress);
		
		if (nextDepth == maxDepth)
			return pts(gb, color, other);
		// Get all possible children
		// ArrayList<Coordinates> moves = new
		// ArrayList<Coordinates>(calcAllValidMoves(gb, color));
		// if(moves.get(0) == null)
		// return pts(gb, color, other);
		int max = a;
		// Go through children until alpha > beta or all children looked at
		
		if (currentNodeCalculating != GameBoardImplAdress) {
			List<Integer> children = memManager.getChildren(GameBoardImplAdress);
			if (!children.isEmpty()) {
				
				for (Integer child : children) {
					CheckTimeOut();
					int cur = minValue(child, nextDepth + 1, maxDepth, a, b);
					// maxDepth,
					// a, b));
					// Now check if time to cut
					if (cur > max) {
						max = cur;
						if (max >= b)
							break;
					}
				}
			}
		}
		try {
			for (Coordinates move : gb.getAllMoves(color)) {
				CheckTimeOut();
				
				GameBoardImpl nextgb = gb.clone();
				nextgb.checkMove(color, move);
				nextgb.makeMove(color, move);
				
				int gbAdress;
				try {
					gbAdress = memManager.newNode(nextgb);
				} catch (NoFreeNodesException e) {
					System.err.println("No Free Node Exception. Stopping calculation");
					return pts(gb, color, other);
				}
				//long linkNodeTime = System.currentTimeMillis();
				
				memManager.linkNodes(GameBoardImplAdress, gbAdress);
				//System.out.println("Link Nodes took:" + (System.currentTimeMillis() - linkNodeTime));

				int cur = minValue(gbAdress, nextDepth + 1, maxDepth, a, b);
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

	private int minValue(int gameBoardImplAddress, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException {// Sets
																												// beta
		CheckTimeOut();
		
		GameBoardImpl gb = memManager.get(gameBoardImplAddress);

		if (nextDepth == maxDepth)
			return pts(gb, color, other);
		
		// Go through children until beta < alpha or all children looked at
		int min = b;
		
		if (currentNodeCalculating != gameBoardImplAddress) {
			List<Integer> children = memManager.getChildren(gameBoardImplAddress);
			if (!children.isEmpty()) {
				
				for (Integer child : children) {

					CheckTimeOut();
					int cur = maxValue(child, nextDepth + 1, maxDepth, a, b);
					// maxDepth,
					// a, b));
					// Now check if time to cut
					if (cur > min) {
						min = cur;
						if (min >= b)
							break;
					}
				}
			}
		}
		try {
			for (Coordinates move : gb.getAllMoves(other)) {
				GameBoardImpl nextgb = gb.clone();

				CheckTimeOut();
				int nextGbAddress;
				try {
					nextGbAddress = memManager.newNode(nextgb);
				} catch (NoFreeNodesException e) {
					System.err.println("No Free Node Exception. Stopping calculation");
					return pts(gb, color, other);
				}
				
				nextgb.checkMove(other, move);
				nextgb.makeMove(other, move);
				
				//long linkNodeTime = System.currentTimeMillis();
				
				memManager.linkNodes(gameBoardImplAddress, nextGbAddress);
				//System.out.println("Link Nodes took:" + (System.currentTimeMillis() - linkNodeTime));
				int cur = maxValue(nextGbAddress, nextDepth + 1, maxDepth, a, b);
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

	private int pts(GameBoardImpl GB, int us, int other) {
		return PC.calcPoints(GB, us);
	}


	private void CheckTimeOut() throws OutOfTimeException {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
		String methodName = e.getMethodName();
		System.out.println(methodName + "--  Checking time: " + (System.currentTimeMillis() - t0));
		if (maxTime - (System.currentTimeMillis() - t0) <= 0)
			throw new OutOfTimeException();
	}

	static class OutOfTimeException extends Exception {
	}

	static class NoMoveAvaiableException extends Exception {
	}
	
	
	public static class GameBoardImpl implements GameBoard {
		
		private List<Coordinates> possMoves = null;
		private final GameBoard gb;
		private Coordinates lastMove;
		private Integer points;
		

		private ArrayList<Coordinates> calcAllValidMoves(GameBoardImpl gb, int player) throws NoMoveAvaiableException {

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
		
		public int calcPoints(PointCalc calc, int maxPlayer) {
			if (points == null) {
				points = calc.calcPoints(this, maxPlayer);
			}
			return points;
		}
		
		public List<Coordinates> getAllMoves(int player) throws NoMoveAvaiableException {
			if (possMoves == null) {
				possMoves = calcAllValidMoves(this, player);
			}
			return possMoves;
		}
		
		public GameBoardImpl(GameBoard gb) {
			this.gb = gb;
		}
		
		public List<Coordinates> getPossMoves() {
			return possMoves;
		}
		
		@Override
		public boolean checkMove(int arg0, Coordinates arg1) {
			return gb.checkMove(arg0, arg1);
		}

		@Override
		public GameBoardImpl clone() {
			GameBoardImpl impl = new GameBoardImpl(gb.clone());
			return impl;
		}

		@Override
		public int countStones(int arg0) {
			return gb.countStones(arg0);
		}

		@Override
		public int getOccupation(Coordinates arg0) throws OutOfBoundsException {
			return gb.getOccupation(arg0);
		}

		@Override
		public int getSize() {
			return gb.getSize();
		}

		@Override
		public boolean isFull() {
			return gb.isFull();
		}

		@Override
		public boolean isMoveAvailable(int arg0) {
			return gb.isMoveAvailable(arg0);
		}

		@Override
		public void makeMove(int arg0, Coordinates arg1) {
			gb.makeMove(arg0, arg1);
			lastMove = arg1;
			
		}

		@Override
		public boolean validCoordinates(Coordinates arg0) {
			return gb.validCoordinates(arg0);
		}
		
		public GameBoardImpl cloneGameBoard() {
			return new GameBoardImpl(gb.clone());
		}
		
		@Override
		public boolean equals(Object obj) {
			return gb.toString().equals(obj.toString());
		}

		public Coordinates getLastMove() {
			return lastMove;
		}
		@Override
		public String toString() {
			return gb.toString();
		}
	}
}