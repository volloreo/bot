package reversiplayers;

import java.util.ArrayList;
import java.util.List;

import javax.naming.TimeLimitExceededException;

import reversi.Coordinates;
import reversi.GameBoard;
import reversi.Utils;

public class ReversiUtils {

	private final long timeOut;
	private long startTime;
	private IMemMan<GameStateNode> mManager;
	private int depth = 7;

	public static class GameStateNode {

		public GameBoard gameBoard;
		public int points;
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
			return "Board: " + gameBoard + " points: " + points + " coord " + coordinate;
		}
	}

	public ReversiUtils(long timeout) {
		System.out.println("Created Utils with timeout: " + timeout);
		this.timeOut = timeout == 0 ? Integer.MAX_VALUE : timeout;

		mManager = MemFactory.createVirtMem();

	}

	public List<GameStateNode> getAllPossMoves(GameBoard gb, int player) {

		ArrayList<GameStateNode> moves = new ArrayList<>();

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Coordinates c = new Coordinates(i, j);
				if (gb.checkMove(player, c)) {
					int nodeIndex = mManager.newNode();
					GameStateNode node;
					if (mManager.get(nodeIndex) == null) {
						node = new GameStateNode();
						mManager.set(nodeIndex, node);
					} else {
						node = mManager.get(nodeIndex);
					}

					node.coordinate = c;
					node.gameBoard = gb;
					node.points = 0;
					moves.add(node);
				}
			}
		}
		return moves;
	}

	public GameStateNode getBestMove(GameBoard gb, int player) {
		startTime = System.currentTimeMillis();
		System.out.println("Getting best Move + GB: " + gb);
		GameStateNode bestNode = new GameStateNode();
		bestNode.points = Integer.MIN_VALUE;

		try {
			for (GameStateNode node : getAllPossMoves(gb, player)) {
				System.out.println("Node before " + node);

				minMove(node, depth, gb.clone(), Integer.MIN_VALUE, Integer.MAX_VALUE, player, Utils.other(player));

				System.out.println("Node after " + node);

				if (bestNode.points < node.points)
					bestNode = node;
			}
			System.out.println("No time limit happened!");
		} catch (TimeLimitExceededException e) {
			System.out.println("Got time limit e");
		}

		return bestNode;
	}

	private int getHeuristicPoints(GameBoard gb, int player) {
		return gb.countStones(player);
	}

	private int maxMove(GameStateNode rootNode, int depth, GameBoard gb, int alpha, int beta, int maxPlayer,
			int minPlayer) throws TimeLimitExceededException {
		gb.checkMove(maxPlayer, rootNode.coordinate);
		gb.makeMove(maxPlayer, rootNode.coordinate);

		if (depth == 0) {
			rootNode.points = getHeuristicPoints(gb, maxPlayer);
			return rootNode.points;
		}

		int max = alpha;
		for (GameStateNode node : getAllPossMoves(gb, maxPlayer)) {

			if (timeOut <= System.currentTimeMillis() - startTime)
				throw new TimeLimitExceededException();

			int value = minMove(node, depth - 1, gb.clone(), max, beta, maxPlayer, minPlayer);
			node.points = value;
			if (value > max) {
				max = value;
				if (max >= beta)
					break;
			}

		}
		return max;
	}

	private int minMove(GameStateNode rootNode, int depth, GameBoard gb, int alpha, int beta, int maxPlayer,
			int minPlayer) throws TimeLimitExceededException {
		gb.checkMove(minPlayer, rootNode.coordinate);
		gb.makeMove(minPlayer, rootNode.coordinate);

		if (depth == 0) {
			rootNode.points = getHeuristicPoints(gb, maxPlayer);
			return rootNode.points;
		}

		int min = beta;
		for (GameStateNode node : getAllPossMoves(gb, minPlayer)) {

			if (timeOut <= System.currentTimeMillis() - startTime)
				throw new TimeLimitExceededException();

			int value = maxMove(node, depth - 1, gb.clone(), alpha, min, maxPlayer, minPlayer);
			node.points = value;
			if (value < min) {
				min = value;
				if (min <= alpha)
					break;
			}

		}
		return min;
	}
}
