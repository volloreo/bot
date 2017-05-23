package reversiplayers;

import reversi.*;

import java.util.ArrayList;

public class AlphaBetaMem implements ReversiPlayer{
	private int color = 0;
	private int other = 0;
	
	private long maxTime = 0;
	private long tbuffer = 0;
	private long t0 = 0;
	private long tR = 0;
	private int maxDepth = 50;
	
	PointCalc PC;
	
	public void initialize(int color, long timeout){
		this.color = color;
		this.other = Utils.other(color);
		this.maxTime = timeout - tbuffer;
		if (color == GameBoard.RED)
			System.out.println("AlphaBeta ist Spieler RED.");
		else if (color == GameBoard.GREEN)
			System.out.println("AlphaBeta ist Spieler GREEN.");
		PC = new PointCalc();
	}
	
	public Coordinates nextMove(GameBoard gb){
		t0 = System.currentTimeMillis();
		tR = maxTime;
		Coordinates curBestcoor = null;
		int i = 1;
		try{
			while(i < maxDepth){
				i++;
				System.out.println("Current Depth at:"+i);
				//Now get best move
				//System.out.println("Current depth:" + String.valueOf(i));
				//System.out.println("Time used:" + String.valueOf(System.currentTimeMillis() - t0));
				curBestcoor = bestMove(gb.clone(), i);
			}
		}
		catch(OutOfTimeException e){
			System.out.println("Timeout! Last Depth at:"+i);
			return curBestcoor;
		}
		System.out.println("Last Depth at:"+ i);
		return curBestcoor;
	}
	
	private Coordinates bestMove(GameBoard gb, int maxDepth) throws OutOfTimeException{
		CheckTimeOut();
		
		//Build and "solve" AlphaBeta Tree
		//Get children
		ArrayList<Coordinates> moves = new ArrayList<Coordinates>(calcAllValidMoves(gb, color));
		
		ArrayList<Integer> values = new ArrayList<Integer>();
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		int bestIter = 0;
		int bestVal = Integer.MIN_VALUE;
		for(int i = 0; i < moves.size(); i++){
			GameBoard nextgb = gb.clone();
			nextgb.checkMove(color, moves.get(i));
			nextgb.makeMove(color, moves.get(i));
			values.add(minValue(nextgb, 1, maxDepth, alpha, beta));
			if(values.get(i) >= bestVal){
				bestVal = values.get(i);
				bestIter = i;
			}
		}
		System.out.println("Last values " + values + " Took: " + bestVal);
		//Get Coordinates with best value -> last child to set alpha
		//System.out.println("Last alpha at:"+ alpha);
		//System.out.println("Last bestVal at:"+ bestVal);
		return moves.get(bestIter);
	}
	private int maxValue(GameBoard gb, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException{//Sets alpha
		CheckTimeOut();
		
		if(nextDepth == maxDepth)
			return pts(gb, color, other);
		//Get all possible children
		ArrayList<Coordinates> moves = new ArrayList<Coordinates>(calcAllValidMoves(gb, color));
		if(moves.get(0) == null)
			return pts(gb, color, other);
		int max = a;
		//Go through children until alpha > beta or all children looked at
		for(int i = 0; i < moves.size(); i++){
			GameBoard nextgb = gb.clone();
			nextgb.checkMove(color, moves.get(i));
			nextgb.makeMove(color, moves.get(i));
			int cur = minValue(nextgb, nextDepth+1, maxDepth, a, b);
			//min = Integer.min(b, maxValue(nextgb, nextDepth+1, maxDepth, a, b));
			//Now check if time to cut
			if (cur > max){
				max = cur;
				if(max>=b)
					break;
			}
		}
		//return alpha
		return max;
	}
	private int minValue(GameBoard gb, int nextDepth, int maxDepth, int a, int b) throws OutOfTimeException{//Sets beta
		CheckTimeOut();
		
		if(nextDepth == maxDepth)
			return pts(gb, color, other);
		//Get all possible children
		ArrayList<Coordinates> moves = new ArrayList<Coordinates>(calcAllValidMoves(gb, other));
		if(moves.get(0) == null)
			return pts(gb, color, other);
		//Go through children until beta < alpha or all children looked at
		int min = b;
		for(int i = 0; i < moves.size(); i++){
			GameBoard nextgb = gb.clone();
			nextgb.checkMove(other, moves.get(i));
			nextgb.makeMove(other, moves.get(i));
			int cur = maxValue(nextgb, nextDepth+1, maxDepth, a, b);
			//min = Integer.min(b, maxValue(nextgb, nextDepth+1, maxDepth, a, b));
			//Now check if time to cut
			if (cur < min){
				min = cur;
				if(min<=a)
					break;
			}
		}
		//return beta
		return min;
	}
	private int pts(GameBoard GB, int us, int other){
		return PC.calcPoints(GB, us);
	}
	private ArrayList<Coordinates> calcAllValidMoves(GameBoard gb, int player){
		ArrayList<Coordinates> res = new ArrayList<Coordinates>();
		if(gb.isMoveAvailable(player)){
			int row = 1, col = 1;
			while(gb.validCoordinates(new Coordinates(row, col))){
				while(gb.validCoordinates(new Coordinates(row, col))){
					Coordinates curCoord = new Coordinates(row, col);
					if(gb.checkMove(player, curCoord)){
						GameBoard tempgb = gb.clone();
						tempgb.checkMove(player, curCoord);
						tempgb.makeMove(player, curCoord);
						res.add(curCoord);
					}
					row++;
				}
				row = 1;
				col++;
			}
		}
		else
			res.add((Coordinates)null);
		return res;
	}
	private void CheckTimeOut() throws OutOfTimeException{
		tR = maxTime - (System.currentTimeMillis() - t0);
		if(tR <= 0)
			throw new OutOfTimeException();
	}
	class OutOfTimeException extends Exception{}
}