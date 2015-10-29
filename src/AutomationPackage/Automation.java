package AutomationPackage;

import java.util.*;
import java.io.FileOutputStream;


import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;


import net.lightbody.bmp.*;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.*;
import net.lightbody.bmp.filters.*;
import net.lightbody.bmp.util.*;
import io.netty.handler.codec.http.*;


public class Automation {

	static WebDriver driver;
	static String parentWindowHandler;
	static Har har;
	static Integer numberOfEpisode;
	static Integer currentEpisode;
	static String showTitle;
	static ArrayList<String> urlList;
	static String lastUrl;
	
    public static void main(String[] args) 
    	throws InterruptedException {
    		
    	String baseUrl;
    	String endUrl;

    	baseUrl = "http://full-stream.me/";
    	endUrl = "/11521-the-blacklist-saison-3.html";
    	
    	showTitle = "The_Blacklist-s03";
    	numberOfEpisode = 4;
    	urlList = new ArrayList<String>();
    	lastUrl = "";
    	currentEpisode = 1;

    	    	
    	BrowserMobProxy proxy = new BrowserMobProxyServer();
    	proxy.start(0);
    	
    	Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
    	
    	proxy.newHar("testFullStream");
    	
    	proxy.addRequestFilter(new RequestFilter() {
            @Override
            public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
                if (messageInfo.getOriginalUrl().contains(".mp4")) {
                	System.out.println("URL trouv�e : " + messageInfo.getOriginalUrl());
                	if (!messageInfo.getOriginalUrl().equals(lastUrl)) {
                		urlList.add(showTitle + "-e" + currentEpisode.toString() + " : " + messageInfo.getOriginalUrl());
                		lastUrl = messageInfo.getOriginalUrl();
                	}
                }
                return null;
            }
        });
    	
    	
    	
        //Create a new instance of Firefox Browser
    	driver = new FirefoxDriver(capabilities);
        
        //Open the URL in firefox browser
        driver.get(baseUrl + endUrl);

        //Maximize the Browser window
        driver.manage().window().maximize();

        parentWindowHandler = driver.getWindowHandle();
        
        for (currentEpisode = 1; currentEpisode <= numberOfEpisode; currentEpisode++) {
        	driver.get(baseUrl + endUrl);
        	
        	fuckButton();

            selectEpisode(currentEpisode);
            
            fuckButton();
            
            startEpisode();
            
            startPlaying();        	
        }
        
        har = proxy.getHar();
    	
        analysis();
        //Close the browser
        driver.close();
        driver.quit();
        proxy.stop();
        
        System.out.println("Fin !");
        
    }
	
    public static void fuckButton() throws InterruptedException {
    	Integer xOffset, yOffset;
    	WebElement element;
    	Actions builder;
    	    	
    	xOffset = 505;
    	yOffset = 109;
    	
    	element = driver.findElement(By.className("series-player"));
    	
    	builder = new Actions(driver);
    	builder.moveToElement(element, xOffset, yOffset).click().build().perform();
    	
    	cleanWindows();
    }
    
    public static void selectEpisode(Integer episodeNumber) throws InterruptedException {
    	driver.findElement(By.linkText("Episode " + episodeNumber.toString())).click();
        cleanWindows();
        
        try {
        	driver.findElement(By.linkText("Player 1 VOSTFR")).click();
        	cleanWindows();
        } catch (org.openqa.selenium.NoSuchElementException ex) {
        	try {
        		driver.findElement(By.linkText("Player 1")).click();
            	cleanWindows();	
        	} catch (Exception e) {
        		System.out.println("Impossible de trouver le bon bouton pour le player");
        	} 
        } catch (Exception e) {
    		System.out.println("Impossible de trouver le bon bouton pour le player");
    	}
    }

    public static void startEpisode() throws InterruptedException {
    	Integer xOffset, yOffset;
    	WebElement element;
    	Actions builder;
    	    	
    	xOffset = 309;
    	yOffset = 293;
    	
    	element = driver.findElement(By.className("series-player"));
    	
    	builder = new Actions(driver);
    	
    	builder.moveToElement(element, xOffset, yOffset).click().build().perform();
    	cleanWindows();
    	
    	builder.moveToElement(element, xOffset, yOffset).click().build().perform();
    	cleanWindows();
    }

    public static void startPlaying() throws InterruptedException {
    	Integer xOffset, yOffset;
    	WebElement element;
    	Actions builder;
    	    	
    	xOffset = 300;
    	yOffset = 172;
    	
    	element = driver.findElement(By.className("series-player"));
    	
    	builder = new Actions(driver);
    	
    	cleanWindows();
    	builder.moveToElement(element, xOffset, yOffset).click().build().perform();
    	cleanWindows();
    	
    }

    public static void cleanWindows() throws InterruptedException {
    	
    	String subWindowHandler = null;
    	Long pauseTime = (long) 2000;
    	
    	Set<String> windowId = driver.getWindowHandles();    // get  window id of current window
        Iterator<String> iterator = windowId.iterator();
        
        Thread.sleep(pauseTime);
        
        while(iterator.hasNext()) {
        	subWindowHandler = iterator.next();
        	
        	if (!subWindowHandler.equals(parentWindowHandler)) {
        		
        		try {
        			driver.switchTo().window(subWindowHandler);            		
            		driver.close();
        		} catch (Exception e) {
        			System.out.println("Erreur � la revue des pop-ups : " + e.getStackTrace());
        		}

        		try {
        			driver.switchTo().alert().accept();
        		} catch (Exception e) {
        			System.out.println("Erreur � la fermeture des pop-ups : " + e.getStackTrace());
        		}
        	}
        }
        
        driver.switchTo().window(parentWindowHandler);
        
    }
    
    public static void analysis() {
    	
    	String strFilePath = "tt.log";
    	try {
    		FileOutputStream fos = new FileOutputStream(strFilePath);
        	har.writeTo(fos);	
    	} catch (Exception e) {
    		System.out.println("Erreur dans le log : " + e.getStackTrace());
    	}
    	Iterator<String> urlIterator = urlList.iterator();
    	while(urlIterator.hasNext()) {
    		System.out.println(urlIterator.next());
    	}
    	
    }
    
    public static void startEpisodeOld() throws InterruptedException {
    	
    	driver.findElement(By.id("first_btn")).click();
        //driver.findElement(By.xpath("//a[contains(text(),'Close ad and continue as Free User')]")).click();;
        cleanWindows();
        
        driver.findElement(By.id("second_btn")).click();
        cleanWindows();
        
    }
    
    
}
