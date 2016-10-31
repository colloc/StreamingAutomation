package AutomationPackage;

import java.util.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;


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


import org.jsoup.*;  
import org.jsoup.nodes.*;  
import org.jsoup.select.*;

/*
import com.jaunt.*;
import com.jaunt.component.*;
*/


public class Automation {

	static WebDriver driver;
	static String parentWindowHandler;
	static Har har;
	static Integer lastEpisode;
	static Integer currentEpisode;
	static Integer startingEpisode;
	static String showTitle;
	static ArrayList<String> urlList;
	static String lastUrl;
	static String destPath;
	static String site;
	
    public static void main(String[] args) 
    	throws InterruptedException {
    		
    	String baseUrl;
    	String endUrl;
    	String midUrl;	// utile si on se sert de DPSTREAM

    	baseUrl = "http://www.dpstream.net/";
    	midUrl = "serie-5963-flash-2014/saison-2/";
    	endUrl = "serie-5963-flash-2014/saison-2/episode-1.html";
    	// 6430-mozart-in-the-jungle-saison-1.html
    	// 12149-mozart-in-the-jungle-saison-2.html
    	
    	// Variables à initialiser en fonction du show que l'on veut récupérer
    	
    	site = "DPSTREAM"; // Valeurs possibles : DPSTREAM - FULLSTREAM
    	showTitle = "The_Flash_s02";
    	startingEpisode = 1;
    	lastEpisode = 1;
    	
    	urlList = new ArrayList<String>();
    	lastUrl = "";
    	currentEpisode = startingEpisode;
    	destPath = "C:/Users/Mathilde/Dev/streamingDL/";


    	BrowserMobProxy proxy = new BrowserMobProxyServer();
    	proxy.start(0);
    	
    	Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
    	
    	// Suite dernières évolutions de Firefox et Selenium 3 - a verifier 
    	capabilities.setCapability("marionette", true);
    	
    	proxy.newHar("testFullStream");
    	
    	// Paramétrage du proxy :
    	// - on regarde les requetes qui vont récupérer des fichiers ".mp4" (a priori le bon format de video)
    	// - lorsque c'était utilisé pour 
    	proxy.addRequestFilter(new RequestFilter() {
            @Override
            public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
                if (messageInfo.getOriginalUrl().contains(".mp4")) {
                	System.out.println("URL trouvée : " + messageInfo.getOriginalUrl());
                	if (!messageInfo.getOriginalUrl().equals(lastUrl)) {
                		if(site.equals("FULLSTREAM")) {
                			if(messageInfo.getOriginalUrl().contains("youwatch")) {
                				String tmp = showTitle + "-e" + formatNumber(currentEpisode) + ".mp4|" + messageInfo.getOriginalUrl();
                				urlList.add(tmp);
                    			lastUrl = messageInfo.getOriginalUrl();
                    			System.out.println("Ajout à la liste : " + tmp + " - nombre d'éléments dans la liste : " + urlList.size());
                			}
                		}
                		if(site.equals("DPSTREAM")) {
            				String tmp = showTitle + "-e" + formatNumber(currentEpisode) + ".mp4|" + messageInfo.getOriginalUrl();
            				urlList.add(tmp);
                			lastUrl = messageInfo.getOriginalUrl();
                			System.out.println("Ajout à la liste : " + tmp + " - nombre d'éléments dans la liste : " + urlList.size());
                			
                		}
                	}
                }
                return null;
            }
        });
    	
    	
        //Create a new instance of Firefox Browser
    	System.setProperty("webdriver.gecko.driver","C:/Users/Mathilde/Documents/Dev/Selenium/geckodriver-v0.11.1-win64/geckodriver.exe");
    	driver = new FirefoxDriver(capabilities);
        
        //Open the URL in firefox browser
        driver.get(baseUrl + endUrl);

        //Maximize the Browser window
        driver.manage().window().maximize();

        parentWindowHandler = driver.getWindowHandle();
  
        
        // On exécute la manipulation
        // En fonction du site utilisé
        
        if(site.equals("FULLSTREAM")) {
            for (currentEpisode = startingEpisode; currentEpisode <= lastEpisode; currentEpisode++) {
            	driver.get(baseUrl + endUrl);
            	
            	fuckButton();

                selectEpisode(currentEpisode);
                
                fuckButton();
                
                startEpisode();
                
                startPlaying();        	
            }
        	
        }
  /**/      
        if(site.equals("DPSTREAM")) {
        	
        	ArrayList<String> urlToOpenList;
        	
            for (currentEpisode = startingEpisode; currentEpisode <= lastEpisode; currentEpisode++) {
            	endUrl = "episode-" + currentEpisode.toString() + ".html";
            	urlToOpenList = scrapDP(baseUrl + midUrl + endUrl, "exashare", "360", "VOSTFR");
            	driver.get(urlToOpenList.get(0));

            	// On fait confiance au site et on prend directement la premiere URL qui est dans la liste - a priori pas besoin de la parcourir
            	
            	/*
            		
            	// On prépare l'URL en fonction du numéro de l'épisode
            	//midUrl = "serie-5963-flash-2014/saison-2/";
            	endUrl = "episode-" + currentEpisode.toString() + "1.html";        	
            	driver.get(baseUrl + midUrl + endUrl);
            	
            	
            	
            	// On va utiliser jsoup comme API pour faire le scrapping de la page
                try {
                	Document doc = Jsoup.connect(baseUrl + midUrl + endUrl).get();
                	Element table = doc.select("<table class=\"col-md-12 table-bordered table-striped table-condensed cf\">").get(0);
            	
                	Element tbody = table.select("tbody").get(0);
            	
                	Elements rows = tbody.select("tr");
            	
                	for (int i = 1; i < rows.size(); i++) {
                		Element row = rows.get(i);
                		Elements cols = row.select("td");
                		Element colSite = cols.get(1);
                		System.out.println("Texte de la première colonne : " + colSite.text());
                		System.out.println("Test sur le contenu : " + colSite.text().contains("exashare") );
            		
                	}
                	
                } catch (Exception ex) {
                	System.err.println("erreur dans le scraping du tableau HTML" + ex.getMessage());
                }
            	
//              Element table = userAgent.doc.findFirst("<table class=\"col-md-12 table-bordered table-striped table-condensed cf\">");
*/
                	
            	
            }
        }

        /*
        har = proxy.getHar();
    	
        
        //Close the browser
        driver.close();
        driver.quit();
        
        analysis();
        
        proxy.stop();
        */
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

    public static ArrayList<String> scrapDP(String urlDP, String videoProvider, String videoQuality, String language) {

    	ArrayList<String> videoURLList = new ArrayList<String>();
    	
    	// On va utiliser jsoup comme API pour faire le scrapping de la page
    	System.out.println("URL à traiter : " + urlDP);
        try {
        	Document doc = Jsoup.connect(urlDP).userAgent("Mozilla").get();
        	System.out.println("10");
        	Element div = doc.getElementById("listView");
        	
        	//Element table = doc.select("<table class=\"col-md-12 table-bordered table-striped table-condensed cf\">").get(0);
        	Element table = div.child(1);
        	//System.out.println("19 : " + table.outerHtml());
        	System.out.println("20");
        	Element tbody = table.select("tbody").get(0);
        	System.out.println("30");
        	Elements rows = tbody.select("tr");
        	System.out.println("40");
        	for (int i = 1; i < rows.size(); i++) {
        		//System.out.println("41");
        		Element row = rows.get(i);
        		Elements cols = row.select("td");
        		
        		String lineVideoProvider = cols.get(0).text().trim();
        		String lineLanguage = cols.get(1).text();
        		String lineVideoQuality = cols.get(2).text();
        		String lineVideoLink = cols.get(5).child(0).attr("href");
        		
        		System.out.println("Ligne : " + i + " | Provider : " + lineVideoProvider + " | Langue : " + lineLanguage + " | Quality : " + lineVideoQuality + " | URL : " + lineVideoLink);
        		
        		if(lineVideoProvider.contains(videoProvider)) {
        			if(lineLanguage.contains(language)) {
        				if(lineVideoQuality.contains(videoQuality)) {
        					videoURLList.add(lineVideoLink);
        				}
        			}
        		}
        		
        	}
        	System.out.println("50");
        } catch (Exception ex) {
        	System.err.println("erreur dans le scraping du tableau HTML" + ex.getMessage());
        	ex.printStackTrace();
        }
    	
        return videoURLList;
        
    }
    
    public static void selectEpisode(Integer episodeNumber) throws InterruptedException {
    	driver.findElement(By.linkText("Episode " + episodeNumber.toString())).click();
        cleanWindows();
        
        System.out.println("On va sélectionner l'épisode : " + episodeNumber);
        
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
        			System.out.println("Erreur à la revue des pop-ups : " + e.getStackTrace());
        		}

        		try {
        			driver.switchTo().alert().accept();
        		} catch (Exception e) {
        			System.out.println("Erreur à la fermeture des pop-ups : " + e.getStackTrace());
        		}
        	}
        }
        
        driver.switchTo().window(parentWindowHandler);
        
    }
    
    public static void analysis() {
    	
    	String strFilePath = "tt.log";
    	String fileName, url, tmp;
    	
    	String outputFilename;
    	File outFile;
    	FileWriter outFileWriter;
    	
    	outputFilename = "d:/streaming/liste_URL-" + showTitle + ".txt";
    	
    	int separatorPosition;
    	try {
    		FileOutputStream fos = new FileOutputStream(strFilePath);
        	har.writeTo(fos);	
    	} catch (Exception e) {
    		System.out.println("Erreur dans le log : " + e.getStackTrace());
    	}
    	
    	
    	try {
    		outFile = new File(outputFilename);
    		if(outFile.exists()) {
    			outFile.delete();
    		}
    		outFile.createNewFile();
    		outFileWriter = new FileWriter(outFile);
    		
    		Iterator<String> urlIterator = urlList.iterator();
        	
    		while(urlIterator.hasNext()) {
        		tmp = urlIterator.next();
        		System.out.println("Ligne analysée : " + tmp);
        		separatorPosition = tmp.lastIndexOf('|');
        		fileName = tmp.substring(0, separatorPosition);
        		url = tmp.substring(separatorPosition + 1);
        		System.out.println("Nom du fichier : " + fileName);
        		System.out.println("URL : " + url);
        		//saveFile(url, fileName, destPath);
        		
        		outFileWriter.write(tmp);
        		outFileWriter.write(System.getProperty("line.separator"));
        		outFileWriter.flush();
        		
        	}
    		
    		outFileWriter.close();
    		
    	} catch (Exception e) {
    		System.out.println("Erreur dans l'écriture du fichier : " + e.getStackTrace());
    	} 
    	
    }
    
    public static void startEpisodeOld() throws InterruptedException {
    	
    	driver.findElement(By.id("first_btn")).click();
        //driver.findElement(By.xpath("//a[contains(text(),'Close ad and continue as Free User')]")).click();;
        cleanWindows();
        
        driver.findElement(By.id("second_btn")).click();
        cleanWindows();
        
    }
    
    public static void saveFile(String remoteURL, String fileName, String filePath) {
    	
    	InputStream input = null;
    	FileOutputStream writeFile = null;
    	Integer fileLength = 0;
    	
    	System.out.println("Début de l'enregistrement du fichier");
    	
    	try {
    		URL url = new URL(remoteURL);
    		URLConnection connection = url.openConnection();
    		fileLength = connection.getContentLength();
    		
    		if (fileLength.equals(-1)) {
    			System.out.println("Erreur dans l'URL : " + remoteURL);
    			return;
    		}
    		
    		input = connection.getInputStream();
    		writeFile = new FileOutputStream(filePath + fileName);
    		byte[] buffer = new byte[1024];
    		int read;
    		
    		while ((read = input.read(buffer))> 0) {
    			writeFile.write(buffer, 0, read);
    			writeFile.flush();
    		}
    		System.out.println("Fin du téléchargement du fichier : " + filePath + fileName);
    	} catch (IOException e) {
            System.out.println("Error while trying to download the file.");
            e.printStackTrace();
    	} finally {
            try
            {
                writeFile.close();
                input.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    	}
    	
    	System.out.println("Fin de l'enregistrement du fichier");
    	
    }
    
    public static String formatNumber(Integer number) {
    	String res;
    	Integer len;
    	
    	res = number.toString();
    	len = new Integer(res.length());

    	if (len.equals(1)) {
    		res = "0" + res;
    	}
    	
    	return res;
    }
    
}