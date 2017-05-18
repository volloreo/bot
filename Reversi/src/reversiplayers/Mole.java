package reversiplayers;

import java.io.*;
import reversi.*;
import java.util.ArrayList;

public class Mole implements ReversiPlayer{
	private int color = 0;
	private long maxTime = 0;
	
	public void initialize(int color, long timeout){
		this.color = color;
		this.maxTime = timeout;
		if (color == GameBoard.RED)
		{
			System.out.println("Mole ist Spieler RED.");
		}
		else if (color == GameBoard.GREEN)
		{
			System.out.println("Mole ist Spieler GREEN.");
		}
	}
	
	public Coordinates nextMove(GameBoard gb){
		ArrayList<Coordinates> possibleMoves = new ArrayList<Coordinates>();
		ArrayList<Integer> pointDifference = new ArrayList<Integer>();
		GameBoard future;
		int row = 1, col = 1;
		if(gb.isMoveAvailable(this.color)){
			//First get list of all possible moves
			while(gb.validCoordinates(new Coordinates(row, col))){
				while(gb.validCoordinates(new Coordinates(row, col))){
					Coordinates curCoord = new Coordinates(row, col);
					if(gb.checkMove(this.color, curCoord)){
						possibleMoves.add(curCoord);
						//Now we simulate the next move
						future = gb.clone();
						future.checkMove(color, curCoord);
						future.makeMove(color, curCoord);
						pointDifference.add(future.countStones(color) - future.countStones(Utils.other(color)));
					}
					row++;
				}
				row = 1;
				col++;
			}
			//Now we make the move with the best difference in our favor
			int bestDif = Integer.MIN_VALUE;
			int bestDifIter = 0;
			for(int i = 0; i < pointDifference.size(); i++){
				if(pointDifference.get(i) > bestDif){
					bestDif = pointDifference.get(i);
					bestDifIter = i;
				}	
			}
			//System.out.println("Mole: BestDif =" + String.valueOf(bestDif));
			return possibleMoves.get(bestDifIter);
		}
		else
			return null;
	}
}