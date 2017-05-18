package reversiplayers;

import reversiplayers.IMemMan;
import reversiplayers.VirtMem;

public class MemFactory {
	public static IMemMan<Integer> create() {
		return new VirtMem<Integer>();
	}
}
