package reversiplayers;

public class Test {

	public static void main(String[] args) {
		BitBoard board = new BitBoard();
		board.setPosition(4, 4, 1);
		board.setPosition(4, 5, 1);
		

		board.setPosition(5, 4, 2);
		board.setPosition(5, 5, 2);
		
		System.out.println(board);
		System.out.println(board.countTokens(1) + " tokens 1");
		System.out.println(board.countTokens(2) + " tokens 2");
	}

}
