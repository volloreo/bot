package reversiplayers;

import reversiplayers.AlphaBetaMemWithOptimaization.GameBoardImpl;
import reversiplayers.ReversiUtils.GameStateNode;

public class MemFactory {
	public static IMemMan<GameStateNode> createVirtMem(int size) {
		return new VirtMem<GameStateNode>(size);
	}
	public static IMemMan<GameBoardImpl> createVirtGameBoardMem(int size) {
		return new VirtMem<GameBoardImpl>(size);
	}
	
	
}
