package es.codeurjc.ais.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testcontainers.containers.BrowserWebDriverContainer;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;


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

	private static final Logger LOGGER = LoggerFactory.getLogger(BrowserWebDriverContainer.class);
	public static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
	
	@Rule
	public BrowserWebDriverContainer chrome1 = new BrowserWebDriverContainer<>()
    	.withDesiredCapabilities(DesiredCapabilities.chrome())
    	.withRecordingMode(RECORD_ALL, new File("target"))
    	.withLogConsumer(logConsumer);
	
	@Rule
	public BrowserWebDriverContainer chrome2 = new BrowserWebDriverContainer<>()
    	.withDesiredCapabilities(DesiredCapabilities.chrome())
    	.withRecordingMode(RECORD_ALL, new File("target"))
    	.withLogConsumer(logConsumer);
	
    private List<RemoteWebDriver> drivers = new ArrayList<RemoteWebDriver>();

    @BeforeClass
    public static void setupClass() {    		
            WebApp.startOnPort("7070");
    }

    @AfterClass
    public static void teardownClass() {
            WebApp.stop();
    }

    @Before
    public void setupTest() {
        drivers.add(chrome1.getWebDriver());
        drivers.add(chrome2.getWebDriver());
    }

    @After
    public void teardown() {
    	for(RemoteWebDriver driver : drivers) {
            if (driver != null) {
                    driver.quit();
            }
    	}
    	drivers.clear();
    }
    
    
    
    public String getAppUrl() {
    	return System.getProperty("APP_URL");
    }
    
	@Test
	public void TicTacToeWeb_Generic_System_Test() throws InterruptedException {
		for(RemoteWebDriver driver : drivers) {
			driver.get(this.getAppUrl());
			Thread.sleep(10000);
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