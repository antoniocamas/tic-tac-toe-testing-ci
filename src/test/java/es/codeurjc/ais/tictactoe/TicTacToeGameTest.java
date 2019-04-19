package es.codeurjc.ais.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import es.codeurjc.ais.tictactoe.TicTacToeGame.CellMarkedValue;
import es.codeurjc.ais.tictactoe.TicTacToeGame.EventType;
import es.codeurjc.ais.tictactoe.TicTacToeGame.WinnerValue;


@RunWith(Parameterized.class)
public class TicTacToeGameTest {
	
	@Parameters
	public static Collection<Object[]> data(){
		
		TestScenarios scenarios = new TestScenarios();
	    Object[][] data = {
	    	{ scenarios.player2Wins4 },
	        { scenarios.player1Wins3 },
	    	{ scenarios.player12Draw }
	    };
	    
	    return Arrays.asList(data);
	}	
	@Parameter(0) public List<Movement> movements;

	
	@Test
	public void TicTacToeGame_Generic_Integration_Test() {
		
		//Given
		TicTacToeGame game = new TicTacToeGame();
		
		Connection c1 = mock(Connection.class);
		Connection c2 = mock(Connection.class);
			
		game.addConnection(c1);
		game.addConnection(c2);
		
		List<Player> players = new CopyOnWriteArrayList<>();
		players.add(new Player(0, movements.get(0).label, "x"));
				
		//When
		game.addPlayer(players.get(0));
				
		//Then
		verify(c1).sendEvent(eq(EventType.JOIN_GAME), eq(players));
		verify(c2).sendEvent(eq(EventType.JOIN_GAME), eq(players));
		
		//When
		players.add(new Player(1, movements.get(1).label, "o"));
		game.addPlayer(players.get(1));
		
		//Then
		verify(c1, times(2)).sendEvent(eq(EventType.JOIN_GAME), eq(players));
		verify(c2, times(2)).sendEvent(eq(EventType.JOIN_GAME), eq(players));	
	
		verify(c1).sendEvent(eq(EventType.SET_TURN), eq(players.get(0)));
		verify(c2).sendEvent(eq(EventType.SET_TURN), eq(players.get(0)));
		
	
		int nextTurn = 1;
	
		for (Movement movement : movements) {
			int thisTurn = nextTurn == 1 ? 0 : 1;
			game.mark(movement.pos);
			
			ArgumentCaptor<CellMarkedValue> argumentCell = ArgumentCaptor.forClass(CellMarkedValue.class);
			
			verify(c1).sendEvent(eq(EventType.MARK), argumentCell.capture());
			assertThat(argumentCell.getValue().cellId).isEqualTo(movement.pos);
			assertThat(argumentCell.getValue().player.getId()).isEqualTo(thisTurn);
			
			verify(c2).sendEvent(eq(EventType.MARK), argumentCell.capture());
			assertThat(argumentCell.getValue().cellId).isEqualTo(movement.pos);
			assertThat(argumentCell.getValue().player.getId()).isEqualTo(thisTurn);
			
			if (movement.isWinner()) {
				
				ArgumentCaptor<WinnerValue> argument = ArgumentCaptor.forClass(WinnerValue.class);
				
				verify(c1).sendEvent(eq(EventType.GAME_OVER), argument.capture());
				assertThat(argument.getValue().pos).isEqualTo(movement.winLine);
				assertThat(argument.getValue().player.getId()).isEqualTo(thisTurn);
				
				verify(c2).sendEvent(eq(EventType.GAME_OVER), argument.capture());
				assertThat(argument.getValue().pos).isEqualTo(movement.winLine);
				assertThat(argument.getValue().player.getId()).isEqualTo(thisTurn);
			}
			else if (movement.isDraw()) {
				ArgumentCaptor<WinnerValue> argument = ArgumentCaptor.forClass(WinnerValue.class);
				
				verify(c1).sendEvent(eq(EventType.GAME_OVER), argument.capture());
				assertThat(argument.getValue()).isNull();
				verify(c2).sendEvent(eq(EventType.GAME_OVER), argument.capture());
				assertThat(argument.getValue()).isNull();
			}
			else {
				verify(c1).sendEvent(eq(EventType.SET_TURN), eq(players.get(nextTurn)));
				verify(c2).sendEvent(eq(EventType.SET_TURN), eq(players.get(nextTurn)));
			}
			nextTurn = nextTurn == 1 ? 0 : 1;
			reset(c1);
			reset(c2);
		}
				
		
	}
}