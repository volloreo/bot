/**
 * @version v0.1
 * v0.1 base
 */

package reversiplayers;

import reversi.*;

public class PointCalc{
	//Factors for Modifiers
	private boolean depr_parity = false;
	private long parity_fac = 1;
	private long mobility_fac = 1;
	private long corner_fac = 10;
	private long stab_fac = 0;
	
	private int our_corners;
	private int their_corners;
	private int our_moves;
	private int their_moves;
	
	private int per = 1000;
	
	//Constructor
	public PointCalc(){}
	
	public PointCalc(long parityWeight, long mobilityWeight, long cornerWeight, long stabilityWeight){
		parity_fac = parityWeight;
		mobility_fac = mobilityWeight;
		corner_fac = cornerWeight;
		stab_fac = stabilityWeight;
	}
	//Set parameter to Long.MIN_VALUE to keep same
	public void changeWeight(long parityWeight, long mobilityWeight, long cornerWeight, long stabilityWeight){
		if(parityWeight != Long.MIN_VALUE)
			parity_fac = parityWeight;
		if(mobilityWeight != Long.MIN_VALUE)
			mobility_fac = mobilityWeight;
		if(cornerWeight != Long.MIN_VALUE)
			corner_fac = cornerWeight;
		if(stabilityWeight != Long.MIN_VALUE)
			stab_fac = stabilityWeight;
		
	}
	
	public int calcPoints(GameBoard gb, int maxPlayer){
		int minPlayer = Utils.other(maxPlayer);
		int res = 0;
		//Add deprecated parity
		if(depr_parity)
			res += depr_par(gb, maxPlayer);
		//Add coin parity modifier
		res += parity(gb, maxPlayer, minPlayer) * parity_fac;
		//Evaluate board for next modifiers
		eval_board(gb, maxPlayer, minPlayer);
		//Add mobility modifier
		res += mobility() * mobility_fac;
		//Add corners captured modifier
		res += corners() * corner_fac;
		//Add stability modifier
		return res;
	}
	
	private void eval_board(GameBoard gb, int us, int them){
		//Reset counters
		our_corners = 0;
		their_corners = 0;
		our_moves = 0;
		their_moves = 0;
		//Count up how many corners we/they captured
		try{
			int occupation = gb.getOccupation(new Coordinates(1, 1));
			if(occupation == us)
				our_corners++;
			if(occupation == them)
				their_corners++;
			occupation = gb.getOccupation(new Coordinates(1, 8));
			if(occupation == us)
				our_corners++;
			if(occupation == them)
				their_corners++;
			occupation = gb.getOccupation(new Coordinates(8, 1));
			if(occupation == us)
				our_corners++;
			if(occupation == them)
				their_corners++;
			occupation = gb.getOccupation(new Coordinates(8, 8));
			if(occupation == us)
				our_corners++;
			if(occupation == them)
				their_corners++;
		}
		catch (OutOfBoundsException e){
			System.out.println("OutOfBoundsException thrown in eval_board, calcPoints");
		}
		//Go through entire board
		for(int row = 1; row < 9; row++){
			for (int col = 1; col < 9; col++){
				//Count up how many possible moves we/they have
				Coordinates c = new Coordinates(row, col);
				if(gb.checkMove(us, c))
					our_moves++;
				if(gb.checkMove(them, c))
					their_moves++;
			}
		}
	}
	
	private int depr_par(GameBoard gb, int us){
		return gb.countStones(us);
	}
	
	private int parity(GameBoard gb, int us, int them){
		long ours = gb.countStones(us);
		long theirs = gb.countStones(them);
		return (int)(per * (ours - theirs)/(ours + theirs));
	}
	//Should be called after eval_board
	private int mobility(){
		if(our_moves + their_moves != 0)
			return per *(our_moves - their_moves)/(our_moves+their_moves);
		else
			return 0;
	}
	//Should be called after eval_board
	private int corners(){
		if(our_corners+their_corners != 0)
			return per*(our_corners - their_corners)/4;
		else
			return 0;
	}
}