package es.codeurjc.ais.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BoardTestParam {
	
	@Parameters
	public static Collection<Object[]> data(){

		TestScenarios scenarios = new TestScenarios();
		
	    Object[][] data = {
	    	{ scenarios.player1AloneWins0 },
	    	{ scenarios.player1Wins3 },
	    	{ scenarios.player2Wins5 },
	    	{ scenarios.player12Draw }
	    };
	    
	    return Arrays.asList(data);
	}
	
	@Parameter(0) public List<Movement> movements;

	@Test
	public void GivenABoard_when_play_getsWinnerRight() {
		Board board = new Board();
					
		for (Movement movement : movements) {
			board.getCell(movement.pos).value = movement.label; 
			if (movement.isWinner()) {
				assertThat(board.getCellsIfWinner(movement.label)).isEqualTo(movement.winLine);
			}
			else if (movement.isDraw()) {
				assertThat(board.checkDraw()).isTrue();
			}
			else {
				assertThat(board.getCellsIfWinner(movement.label)).isNull();
			}
		}
	}
}