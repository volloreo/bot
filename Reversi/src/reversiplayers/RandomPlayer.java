package reversiplayers;
import java.util.ArrayList;
import java.util.Random;

import reversi.Arena;
import reversi.Coordinates;
import reversi.GameBoard;
import reversi.ReversiPlayer;


public class RandomPlayer implements ReversiPlayer
{
	/**
	 * Die Farbe des Spielers.
	 */
	private int color = 0;

	public RandomPlayer()
	{
		System.out.println("RandomPlayer erstellt.");
	}

	/**
	 * Speichert die Farbe und den Timeout-Wert in Instanzvariablen ab. Diese
	 * Methode wird vor Beginn des Spiels von {@link Arena} aufgerufen.
	 * 
	 * @see reversi.ReversiPlayer
	 */
	public void initialize(int color, long timeout)
	{
		this.color = color;
		if (color == GameBoard.RED)
		{
			System.out.println("RandomPlayer ist Spieler RED.");
		}
		else if (color == GameBoard.GREEN)
		{
			System.out.println("RandomPlayer ist Spieler GREEN.");
		}
	}

	/**
	 * Macht einen zufälligen zug
	 * @see reversi.ReversiPlayer
	 * @return Der Zug.
	 */
	public Coordinates nextMove(GameBoard gb)
	{

		ArrayList<Coordinates> list = getAllPossMoves(gb);
		
		if(list.isEmpty()) 
			return null;
		
		return list.get((new Random()).nextInt(list.size()));
	} 
	
	private ArrayList<Coordinates> getAllPossMoves(GameBoard gb) {
		
		ArrayList<Coordinates> moves = new ArrayList<>();
		
		for (int i = 0; i <= 8; i++) {
			for (int j = 0; j <= 8; j++) {
				Coordinates c = new Coordinates(i,j);
				if(gb.checkMove(color, c)) {
					moves.add(c);
				}
			}
		}
		return moves;
	}
}