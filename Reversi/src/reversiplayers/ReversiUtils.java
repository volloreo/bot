package reversiplayers;

import java.util.ArrayList;
import java.util.List;

import javax.naming.TimeLimitExceededException;

import reversi.Coordinates;
import reversi.GameBoard;

public class ReversiUtils {
	

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
	}

	public List<GameStateNode> getAllPossMoves(GameBoard gb, IMemoryManager<GameStateNode> mManager,
			int player) {

		ArrayList<GameStateNode> moves = new ArrayList<>();

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Coordinates c = new Coordinates(i, j);
				if (gb.checkMove(player, c)) {
					GameStateNode node = mManager.getNewObj();
					node.coordinate = c;
					node.gameBoard = gb;
					moves.add(node);
				}
			}
		}
		return moves;
	}

	public GameStateNode getBestMove() {
		return null;
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
		for (GameStateNode node : getAllPossMoves(gb, new MemoryManager(), maxPlayer)) {
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
		for (GameStateNode node : getAllPossMoves(gb, new MemoryManager(), minPlayer)) {
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
