package uk.co.jacekk.proxyscraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyScraperThread extends Thread implements Runnable {
	
	private ProxyScraper scraper;
	private URL url;
	
	public ProxyScraperThread(ProxyScraper scraper, String url) throws MalformedURLException {
		super("Proxy Scraper Thread: " + url);
		
		this.scraper = scraper;
		this.url = new URL(url);
	}
	
	@Override
	public void run(){
		try{
			HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
			
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setUseCaches(false);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String line;
			StringBuilder page = new StringBuilder();
			
			while ((line = input.readLine()) != null){
				page.append(line);
				page.append('\n');
			}
			
			input.close();
			
			Matcher matcher = Pattern.compile("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):([0-9]{1,5})").matcher(page.toString());
			
			synchronized (this.scraper.results){
				while (matcher.find()){
					try{
						String ip = matcher.group(1);
						String port = matcher.group(2);
						
						Proxy proxy = new Proxy(this.scraper.getType(), new InetSocketAddress(ip, Integer.parseInt(port)));
						
						if (!this.scraper.results.contains(proxy)){
							this.scraper.results.add(proxy);
						}
					}catch (IllegalArgumentException e){
						/* Ignore invalid proxies */
					}
				}
			}
		}catch (IOException e){
			e.printStackTrace();
		}
		
		synchronized (this.scraper.threads){
			this.scraper.threads.remove(this);
			this.scraper.threads.notify();
		}
	}
	
}
