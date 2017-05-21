package reversiplayers;

import reversi.Arena;
import reversi.Coordinates;
import reversi.GameBoard;
import reversi.ReversiPlayer;

public class ReversiPlayerMinMax implements ReversiPlayer {
	/**
	 * Die Farbe des Spielers.
	 */
	private int color = 0;
	
	private ReversiUtils utils;

	public ReversiPlayerMinMax() {
		System.out.println("Reversi Player erstellt.");
	}

	/**
	 * Speichert die Farbe und den Timeout-Wert in Instanzvariablen ab. Diese
	 * Methode wird vor Beginn des Spiels von {@link Arena} aufgerufen.
	 * 
	 * @see reversi.ReversiPlayer
	 */
	public void initialize(int color, long timeout) {
		this.color = color;
		utils = new ReversiUtils(timeout - 400);
		
		if (color == GameBoard.RED) {
			System.out.println("RandomPlayer ist Spieler RED.");
		} else if (color == GameBoard.GREEN) {
			System.out.println("RandomPlayer ist Spieler GREEN.");
		}
	}

	/**
	 * Macht einen zufälligen zug
	 * 
	 * @see reversi.ReversiPlayer
	 * @return Der Zug.
	 */
	public Coordinates nextMove(GameBoard gb) {
		return utils.getBestMove(gb, color).coordinate;
	}

}