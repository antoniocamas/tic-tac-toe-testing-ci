package es.codeurjc.ais.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;


@RunWith(Parameterized.class)
public class TicTacToeWebTest {
	
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

    private List<WebDriver> drivers = new ArrayList<WebDriver>();

    @BeforeClass
    public static void setupClass() {
            WebDriverManager.chromedriver().setup();
            WebApp.start();
    }

    @AfterClass
    public static void teardownClass() {
            WebApp.stop();
    }

    @Before
    public void setupTest() {
        drivers.add(new ChromeDriver());
        drivers.add(new ChromeDriver());
    }

    @After
    public void teardown() {
    	for(WebDriver driver : drivers) {
            if (driver != null) {
                    driver.quit();
            }
    	}
    	drivers.clear();
    }
    

	@Test
	public void TicTacToeWeb_Generic_System_Test() throws InterruptedException {
		for(WebDriver driver : drivers) {
			driver.get("http://localhost:8080/");
		}
		
		drivers.get(0).findElement(By.id("nickname")).sendKeys(movements.get(0).label);
		drivers.get(0).findElement(By.id("startBtn")).click();

		drivers.get(1).findElement(By.id("nickname")).sendKeys(movements.get(1).label);
		drivers.get(1).findElement(By.id("startBtn")).click();
		
		int nextTurn = 1;
		
		for (Movement movement : movements) {
			int thisTurn = nextTurn == 1 ? 0 : 1;
			drivers.get(thisTurn).findElement(By.id("cell-" + movement.pos)).click();
			
			if (movement.isWinner()) {
				assertThat(
						drivers.get(thisTurn).switchTo().alert().getText()
						).isEqualTo(
								movement.label + " wins! " + movements.get(nextTurn).label + " looses.");
			}
			else if (movement.isDraw()) {
				assertThat(
						drivers.get(thisTurn).switchTo().alert().getText()
						).isEqualTo("Draw!");
			}
			nextTurn = nextTurn == 1 ? 0 : 1;
		}
	}
}