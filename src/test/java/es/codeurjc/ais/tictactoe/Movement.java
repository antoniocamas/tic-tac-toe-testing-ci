package es.codeurjc.ais.tictactoe;

public class Movement {
	
	Movement(int pos, String label, boolean winner, int[] winLine) {
		this.pos = pos;
		this.label = label;
		this.gameEnder = winner;
		this.winLine = winLine;
	}
		
	public int pos;
	public String label;
	public boolean gameEnder;
	public int[] winLine;
	
	public static final int[][] WINPOSITIONS = { 
			{ 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, 
			{ 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 }, { 6, 4, 2 } };
	
	public boolean isWinner() {
		return this.gameEnder && this.winLine != null;
	};
	
	public boolean isDraw() {
		return this.gameEnder && this.winLine == null;
	};
}