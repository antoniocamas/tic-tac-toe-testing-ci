package es.codeurjc.ais.tictactoe;

import java.util.ArrayList;
import java.util.List;

public class TestScenarios {
	
	TestScenarios() {
		
		String player1Name = "Snake";
		String player2Name = "Apple";

		player2Wins4 = new ArrayList<Movement>();
		player2Wins4.add(new Movement(0, player1Name, false, null));
		player2Wins4.add(new Movement(1, player2Name, false, null));
		player2Wins4.add(new Movement(2, player1Name, false, null));
		player2Wins4.add(new Movement(4, player2Name, false, null));
		player2Wins4.add(new Movement(3, player1Name, false, null));
		player2Wins4.add(new Movement(7, player2Name, true, Movement.WINPOSITIONS[4]));
		
		player2Wins5 = new ArrayList<Movement>();
		player2Wins5.add(new Movement(0, player1Name, false, null));
		player2Wins5.add(new Movement(2, player2Name, false, null));
		player2Wins5.add(new Movement(1, player1Name, false, null));
		player2Wins5.add(new Movement(5, player2Name, false, null));
		player2Wins5.add(new Movement(3, player1Name, false, null));
		player2Wins5.add(new Movement(4, player2Name, false, null));
		player2Wins5.add(new Movement(7, player1Name, false, null));
		player2Wins5.add(new Movement(8, player2Name, true, Movement.WINPOSITIONS[5]));
		
		player1Wins3 = new ArrayList<Movement>();
		player1Wins3.add(new Movement(0, player1Name, false, null));
		player1Wins3.add(new Movement(2, player2Name, false, null));
		player1Wins3.add(new Movement(1, player1Name, false, null));
		player1Wins3.add(new Movement(5, player2Name, false, null));
		player1Wins3.add(new Movement(3, player1Name, false, null));
		player1Wins3.add(new Movement(4, player2Name, false, null));
		player1Wins3.add(new Movement(8, player1Name, false, null));
		player1Wins3.add(new Movement(7, player2Name, false, null));
		player1Wins3.add(new Movement(6, player1Name, true,  Movement.WINPOSITIONS[3]));
		
		player12Draw = new ArrayList<Movement>();
		player12Draw.add(new Movement(0, player1Name, false, null));
		player12Draw.add(new Movement(8, player2Name, false, null));
		player12Draw.add(new Movement(5, player1Name, false, null));
		player12Draw.add(new Movement(3, player2Name, false, null));
		player12Draw.add(new Movement(6, player1Name, false, null));
		player12Draw.add(new Movement(2, player2Name, false, null));
		player12Draw.add(new Movement(4, player1Name, false, null));
		player12Draw.add(new Movement(7, player2Name, false, null));
		player12Draw.add(new Movement(1, player1Name, true,  null));
		
		player1AloneWins0 = new ArrayList<Movement>();
		player1AloneWins0.add(new Movement(0, player1Name, false, null));
		player1AloneWins0.add(new Movement(1, player1Name, false, null));
		player1AloneWins0.add(new Movement(2, player1Name, true, Movement.WINPOSITIONS[0] ));
	}
		
	public List<Movement> player2Wins4;
	public List<Movement> player2Wins5;
	public List<Movement> player1Wins3;
	public List<Movement> player12Draw;
	public List<Movement> player1AloneWins0;
}