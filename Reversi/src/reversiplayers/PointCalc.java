/**
 * @version v0.3
 * v0.1 base
 * v0.2 addition curDepth to calcPoints
 * v0.3 with stability
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
	
	private int[][] matrix;
	private int[][] cur_m;
	private int our_corners;
	private int their_corners;
	private int our_moves;
	private int their_moves;
	
	private boolean ulC;
	private boolean urC;
	private boolean brC;
	private boolean blC;
	
	private int most_kids=0;
	
	private int per = 1000;
	
	private int max = Integer.MIN_VALUE;
	private int min = Integer.MAX_VALUE;
	
	//Constructor
	public PointCalc(){
		this(0, 1, 10, 1);
	}
	
	public PointCalc(long parityWeight, long mobilityWeight, long cornerWeight, long stabilityWeight){
		parity_fac = parityWeight;
		mobility_fac = mobilityWeight;
		corner_fac = cornerWeight;
		stab_fac = stabilityWeight;
		
		matrix = new int[][]{
							{50,  -30, 22, 20, 20, 22, -30, 50},
							{-30, -30, 22, 10, 10, 22, -30, -30},
							{22,   22, 22, 10, 10, 22,  22, 22},
							{20,   10, 10, 10, 10, 10,  10, 20},
							{20,   10, 10, 10, 10, 10,  10, 20},
							{22,   22, 22, 10, 10, 22,  22, 22},
							{-30, -30, 22, 10, 10, 22, -30, -30},
							{50,  -30, 22, 20, 20, 22, -30, 50}};
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
	
	public int calcPoints(GameBoard gb, int maxPlayer, int curDepth){
		int minPlayer = Utils.other(maxPlayer);
		int res = 0;
		/*//Add deprecated parity
		if(depr_parity)
			res += depr_par(gb, maxPlayer);*/
		//Add coin parity modifier
		//res += parity(gb, maxPlayer, minPlayer) * parity_fac;
		//Evaluate board for next modifiers (corners and parity returned, prep for mobility)
		res += eval_board(gb, maxPlayer, minPlayer) * stab_fac;
		//System.out.println("WeightedParity+Stability:"+res);
		//Add mobility modifier
		res += mobility() * mobility_fac;
		//System.out.println("+Mobility:               "+res);
		//Add corners captured modifier
		res += corners() * corner_fac;
		//System.out.println("+Corners(again):         "+res);
		//System.out.println(" ");
		//Add stability modifier
		return res;
	}
	
	private int eval_board(GameBoard gb, int us, int them){
		int pts = 0;
		//Reset counters
		our_corners = 0;
		their_corners = 0;
		our_moves = 0;
		their_moves = 0;
		//Reset flags -> corners have been captured
		ulC = false;
		urC = false;
		blC = false;
		brC = false;
		//Reset matrix
		cur_m = copy_m(matrix);
		
		//Count up how many corners we/they captured
		try{
			int occupation = gb.getOccupation(new Coordinates(1, 1));
			if(occupation == us){
				our_corners++;
				ulC = true;
			}
			if(occupation == them){
				their_corners++;
				ulC = true;
			}
			occupation = gb.getOccupation(new Coordinates(1, 8));
			if(occupation == us){
				our_corners++;
				urC = true;
			}
			if(occupation == them){
				their_corners++;
				urC = true;
			}
			occupation = gb.getOccupation(new Coordinates(8, 1));
			if(occupation == us){
				our_corners++;
				blC = true;
			}
			if(occupation == them){
				their_corners++;
				blC = true;
			}
			occupation = gb.getOccupation(new Coordinates(8, 8));
			if(occupation == us){
				our_corners++;
				brC = true;
			}
			if(occupation == them){
				their_corners++;
				brC = true;
			}
		}
		catch (OutOfBoundsException e){
			System.out.println("OutOfBoundsException thrown in eval_board, calcPoints");
		}
		//If corners taken, modify corner zone modifiers, start spread stab
		if(ulC){
			spread_stab(cur_m, 0, 0);
			cur_m[1][0] += 25;
			cur_m[1][1] += 15;
			cur_m[0][1] += 25;
		}
		if(urC){
			spread_stab(cur_m, 7, 0);
			cur_m[0][6] += 25;
			cur_m[1][6] += 15;
			cur_m[1][7] += 25;
		}
		if(blC){
			spread_stab(cur_m, 7, 0);
			cur_m[6][0] += 25;
			cur_m[6][1] += 15;
			cur_m[7][1] += 25;
		}
		if(brC){
			spread_stab(cur_m, 7, 7);
			cur_m[6][7] += 25;
			cur_m[6][6] += 15;
			cur_m[7][6] += 25;
		}
		//Go through entire board
		try{
		for(int row = 1; row < 9; row++){
			for (int col = 1; col < 9; col++){
				Coordinates c = new Coordinates(row, col);
				//WEIGHTED PARITY
				if(gb.getOccupation(c) == us){
					//Spread stability around itself
					spread_stab(cur_m, row, col);
					//Add weighted parity of position (w corners)
					pts += cur_m[row-1][col-1];
				}
				if(gb.getOccupation(c) == them){
					pts -= cur_m[row-1][col-1];
				}
				//MOBILITY: Count up how many possible moves we/they have
				if(gb.checkMove(us, c))
					our_moves++;
				if(gb.checkMove(them, c))
					their_moves++;
			}
		}
		}
		catch(OutOfBoundsException e){}
		/*//Debug
		if(pts > max)
			max = pts;
		if(pts < min)
			min = pts;
		System.out.println("max:"+max+" min:"+min);*/
		return pts;
	}
	
	private void spread_stab(int[][] m, int row, int col){
		for(int i = row-1; i < row+1; i++){
			for(int j = col-1; j < col+1; j++){
				if(!(i == row && j == col)){//if not itself
					if(i>0 && i<8 && j>0 && j<8){//if valid coord
						if(!(m[i][j] > 30))	//if pos not at max stab
							m[i][j] += 10;	//add 10 stab
					}
				}
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
		/*if(our_moves > most_kids)
			most_kids = our_moves;
		System.out.println("Most moves:" + most_kids);*/
		if(our_moves + their_moves != 0)
			return per *(our_moves - their_moves)/(our_moves+their_moves);
		else
			return 0;
	}
	//Should be called after eval_board
	private int corners(){
		if(our_corners+their_corners != 0)
			return per*((our_corners - their_corners)/4);
		else
			return 0;
	}
	//m has to be 8x8
	private int[][] copy_m(int[][] m){
		int[][] res = new int[8][8];
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++)
				res[i][j] = m[i][j];
		}
		return res;
	}
}