package uk.co.jacekk.proxyscraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.jacekk.scraperlib.Scraper;

public class BasicProxyListScraper extends ProxyListScraper {
	
	public BasicProxyListScraper(Proxy.Type proxyType, String url){
		super(proxyType, url);
	}

	@Override
	public void scrape(List<Scraper<Proxy>> newScrapers, List<Proxy> results) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) (new URL(this.getUrl())).openConnection();
		
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		connection.setUseCaches(false);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0");
		
		BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		String line;
		StringBuilder page = new StringBuilder();
		
		while ((line = input.readLine()) != null){
			page.append(line);
			page.append('\n');
		}
		
		input.close();
		
		Matcher matcher = Pattern.compile("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):([0-9]{1,5})").matcher(page.toString());
		
		while (matcher.find()){
			try{
				String ip = matcher.group(1);
				String port = matcher.group(2);
				
				results.add(new Proxy(this.proxyType, new InetSocketAddress(ip, Integer.parseInt(port))));
			}catch (IllegalArgumentException e){
				/* Ignore invalid proxies */
			}
		}
	}
	
}
