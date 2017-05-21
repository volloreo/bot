package reversiplayers;

import reversiplayers.IMemMan;
import reversiplayers.VirtMem;
import reversiplayers.ReversiUtils.GameStateNode;

public class MemFactory {
	public static IMemMan<GameStateNode> createVirtMem(int size) {
		return new VirtMem<GameStateNode>(size);
	}
}
