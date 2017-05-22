package reversiplayers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.TimeLimitExceededException;

import reversi.Coordinates;
import reversi.GameBoard;
import reversi.Utils;
import reversiplayers.IMemMan.NoFreeNodesException;

public class ReversiUtils {

	private final long timeOut;
	private long startTime;
	private IMemMan<GameStateNode> mManager;
	private int startDepth = 7;
	private static final boolean DEBUG = true;
	private GameStateNode headNode;
	private boolean init = false;

	public ReversiUtils(long timeout) {
		System.out.println("Created Utils with timeout: " + timeout);
		this.timeOut = timeout == 0 ? Integer.MAX_VALUE : timeout;
		
		mManager = MemFactory.createVirtMem(1600000);
		
		//Set Root Node
		headNode = new GameStateNode();
		headNode.address = 0;
		mManager.setHeadNode(0);
		mManager.set(0, headNode);
		
	}
	
	private static void log(String msg) {
		if (DEBUG)
			System.out.println(msg);
	}

	
	public List<GameStateNode> getAllPossMoves(GameStateNode parentNode, int player) throws TimeLimitExceededException {

		//Check if move was already calculated.
		List<Integer> children = mManager.getChildren(parentNode.address);
		/*
		if (!parentNode.finishedCalc) {
			System.err.println("Found node which children werent calc successfully");
		}*/
		if (parentNode.finishedCalc  && !children.isEmpty()) {
			//log("GetAllPossMoves:  Found children List for parent node. Returning list.");
			return children.stream().map(c -> mManager.get(c)).collect(Collectors.toList());
		}
		
		ArrayList<GameStateNode> moves = new ArrayList<>();
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				

				Coordinates c = new Coordinates(i, j);
				checkTime();
				
				if (parentNode.gameBoard.checkMove(player, c)) {
					// Add new Node and init it, if it is a legal move.
					int nodeIndex;
					try {
						nodeIndex = mManager.newNode();
					} catch (NoFreeNodesException e) {
						System.err.println("No Free Nodes Exception!");
						return moves;
					}
					GameStateNode node;
					if (mManager.get(nodeIndex) == null) {
						node = new GameStateNode();
						mManager.set(nodeIndex, node);
					} else {
						node = mManager.get(nodeIndex);
					}

					node.coordinate = c;
					node.finishedCalc = false;
					node.address = nodeIndex;
					node.gameBoard = parentNode.gameBoard.clone();
					node.points = 0;
					mManager.linkNodes(parentNode.address, node.address);
					moves.add(node);
					checkTime();
				}
			}
		}
		parentNode.finishedCalc = true;
		return moves;
	}

	
	
	
	public GameStateNode getBestMove(GameBoard gb, int player) {
		startTime = System.currentTimeMillis();
		log("In getBestMove");
		
		if (!init) {
			log("In init");
			init = true;
			headNode.gameBoard = gb.clone();
		} else {
			log("getBestMove  - Getting 2nd Order Head Node. searching for it...");
			for (Integer childId : mManager.getGrandchildren(headNode.address)) {
				if (mManager.get(childId).equals(gb)) {
					headNode = mManager.get(childId);
					mManager.setHeadNode(childId);
					log("getBestMove - Found head node: " + headNode);
					break;
				}
			}
		}
		
		log("Getting best Move + GB: \n " + gb);
		
		GameStateNode bestNode = new GameStateNode();
		bestNode.points = Integer.MIN_VALUE;
		for (int i = 0; i < 2; i++) {
			try {
				for (GameStateNode node : getAllPossMoves(headNode, player)) {
					minMove(node, startDepth + 0, Integer.MIN_VALUE, Integer.MAX_VALUE, player, Utils.other(player));

					if (bestNode.points < node.points)
						bestNode = node;
				}
				System.out.println("No time limit happened!");
			} catch (TimeLimitExceededException e) {
				System.err.println("Got time limit e");
				break;
			}
			log("GetBestMove() increasing Depth: " + i);
		}
		log("getBestMove() returning: " + bestNode);
		
		if(!gb.checkMove(player, bestNode.coordinate)) {
			System.out.println("RETURN MOVE INVALID!!!!");
			for (int i = 1; i <= 8; i++) {
				for (int j = 1; j <= 8; j++) {
					Coordinates c = new Coordinates(i, j);
					if (gb.checkMove(player, c)) {
						bestNode.coordinate = c;
						bestNode.gameBoard = gb.clone();
						bestNode.gameBoard.checkMove(player, c);
						bestNode.gameBoard.makeMove(player, c);
						return bestNode;
					}
				}
			}
		}
		return bestNode;
	}

	private int getHeuristicPoints(GameBoard gb, int player) {
		return gb.countStones(player) ;//- gb.countStones(Utils.other(player));
	}
	
	private void checkTime() throws TimeLimitExceededException {
		//log("checkTime diff: " + (System.currentTimeMillis() - startTime));
		if (timeOut <= System.currentTimeMillis() - startTime)
			throw new TimeLimitExceededException();
	}

	private int maxMove(GameStateNode rootNode, int depth, int alpha, int beta, int maxPlayer,
			int minPlayer) throws TimeLimitExceededException {
	
		rootNode.gameBoard.checkMove(minPlayer, rootNode.coordinate);
		rootNode.gameBoard.makeMove(minPlayer, rootNode.coordinate);

		if (depth == 0) {
			rootNode.points = getHeuristicPoints(rootNode.gameBoard, maxPlayer);
			return rootNode.points;
		}

		int max = alpha;
		for (GameStateNode node : getAllPossMoves(rootNode, maxPlayer)) {
			checkTime();

			int value = minMove(node, depth - 1, max, beta, maxPlayer, minPlayer);
			node.points = value;
			if (value > max) {
				max = value;
				if (max >= beta)
					break;
			}

		}
		return max;
	}

	private int minMove(GameStateNode rootNode, int depth, int alpha, int beta, int maxPlayer,
			int minPlayer) throws TimeLimitExceededException {
		rootNode.gameBoard.checkMove(maxPlayer, rootNode.coordinate);
		rootNode.gameBoard.makeMove(maxPlayer, rootNode.coordinate);
		

		if (depth == 0) {
			rootNode.points = getHeuristicPoints(rootNode.gameBoard, maxPlayer);
			return rootNode.points;
		}

		int min = beta;
		for (GameStateNode node : getAllPossMoves(rootNode, minPlayer)) {

			checkTime();

			int value = maxMove(node, depth - 1, alpha, min, maxPlayer, minPlayer);
			node.points = value;
			if (value < min) {
				min = value;
				if (min <= alpha)
					break;
			}

		}
		return min;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static class GameStateNode {

		public GameBoard gameBoard;
		public int points;
		public boolean finishedCalc = false;
		public int address;
		public Coordinates coordinate;
		public List<GameStateNode> children = new ArrayList<>(7);

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof GameBoard) {
				return gameBoard.toString().equals(obj.toString());
			}
			return super.equals(obj);
		}

		@Override
		public String toString() {
			return "Board: \n " + gameBoard + " points: " + points + " coord " + coordinate;
		}
	}
	
	
}
