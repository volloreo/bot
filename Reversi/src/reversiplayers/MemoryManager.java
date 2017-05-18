package reversiplayers;

import reversiplayers.ReversiUtils.GameStateNode;

public class MemoryManager implements IMemoryManager<GameStateNode>{

	@Override
	public GameStateNode getNewObj() {
		return new GameStateNode();
	}
	

}
