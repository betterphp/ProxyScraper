package uk.co.jacekk.proxyscraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import uk.co.jacekk.scraperlib.CombinedListResultsHandler;
import uk.co.jacekk.scraperlib.ProgressHandler;
import uk.co.jacekk.scraperlib.ScraperQueue;
import uk.co.jacekk.scraperlib.StreamOutputProgressHandler;

public class ProxyScraper {
	
	private static final HashMap<Proxy.Type, String[]> basicSources;
	private static final HashMap<Proxy.Type, String[]> blogspotSources;
	
	private Proxy.Type type;
	private HashSet<Proxy> results;
	
	static {
		basicSources = new HashMap<>();
		blogspotSources = new HashMap<>();
		
		basicSources.put(Proxy.Type.HTTP, new String[]{
			"https://proxy-list.org/english/index.php?p=1",
			"https://proxy-list.org/english/index.php?p=2",
			"https://proxy-list.org/english/index.php?p=3",
			"https://proxy-list.org/english/index.php?p=4",
			"https://proxy-list.org/english/index.php?p=5",
			"https://proxy-list.org/english/index.php?p=6",
			"https://proxy-list.org/english/index.php?p=7",
			"https://proxy-list.org/english/index.php?p=8",
			"https://proxy-list.org/english/index.php?p=9",
			"https://proxy-list.org/english/index.php?p=10",
			"http://www.samair.ru/proxy/proxy-01.htm",
			"http://www.samair.ru/proxy/proxy-02.htm",
			"http://www.samair.ru/proxy/proxy-03.htm",
			"http://www.samair.ru/proxy/proxy-04.htm",
			"http://www.samair.ru/proxy/proxy-05.htm",
			"http://www.samair.ru/proxy/proxy-06.htm",
			"http://www.samair.ru/proxy/proxy-07.htm",
			"http://www.samair.ru/proxy/proxy-08.htm",
			"http://www.samair.ru/proxy/proxy-09.htm",
			"http://www.samair.ru/proxy/proxy-10.htm",
			"http://www.samair.ru/proxy/proxy-11.htm",
			"http://www.samair.ru/proxy/proxy-12.htm",
			"http://www.samair.ru/proxy/proxy-13.htm",
			"http://www.samair.ru/proxy/proxy-14.htm",
			"http://www.samair.ru/proxy/proxy-15.htm",
			"http://www.samair.ru/proxy/proxy-16.htm",
			"http://www.samair.ru/proxy/proxy-17.htm",
			"http://www.samair.ru/proxy/proxy-18.htm",
			"http://www.samair.ru/proxy/proxy-19.htm",
			"http://www.samair.ru/proxy/proxy-20.htm",
			"http://www.samair.ru/proxy/proxy-21.htm",
			"http://www.samair.ru/proxy/proxy-22.htm",
			"http://www.samair.ru/proxy/proxy-23.htm",
			"http://www.samair.ru/proxy/proxy-24.htm",
			"http://www.samair.ru/proxy/proxy-25.htm",
			"http://www.samair.ru/proxy/proxy-26.htm",
			"http://www.samair.ru/proxy/proxy-27.htm",
			"http://www.samair.ru/proxy/proxy-28.htm",
			"http://www.samair.ru/proxy/proxy-29.htm",
			"http://www.samair.ru/proxy/proxy-30.htm",
		});
		
		blogspotSources.put(Proxy.Type.HTTP, new String[]{
			"http://proxyserverlist-24.blogspot.co.uk/search/label/Fast%20Proxy%20Server?max-results=100&start=0&by-date=false",
			"http://proxyserverlist-24.blogspot.co.uk/search/label/Proxy%20Server%20List?max-results=100&start=0&by-date=false",
			"http://newfreshproxies24.blogspot.co.uk/search/label/Fresh%20New%20Proxies?max-results=100&start=0&by-date=false",
			"http://sslproxies24.blogspot.co.uk/search/label/SSL%20Proxies?max-results=100&start=0&by-date=false",
		});
		
		basicSources.put(Proxy.Type.SOCKS, new String[]{
			"http://www.samair.ru/proxy/socks01.htm",
			"http://www.samair.ru/proxy/socks02.htm",
			"http://www.samair.ru/proxy/socks03.htm",
			"http://www.samair.ru/proxy/socks04.htm",
			"http://www.samair.ru/proxy/socks05.htm",
			"http://myproxylists.com/socks-proxy-lists",
			"http://www.atomintersoft.com/free_socks5_proxy_list",
			"http://www.proxylists.net/?SOCKS#SOCKS4",
			"http://www.proxylists.net/?SOCKS#SOCKS5",
		});
		
		blogspotSources.put(Proxy.Type.SOCKS, new String[]{
			"http://socksproxylist24.blogspot.co.uk/search/label/Socks%20Proxy?max-results=100&start=0&by-date=false",
			"http://www.socks24.org/search/label/VIP%20Socks%205?max-results=100&start=0&by-date=false",
			"http://www.socks24.org/search/label/US%20Socks?max-results=100&start=0&by-date=false",
			"http://www.socks24.org/search/label/Socks%20Proxy?max-results=100&start=0&by-date=false",
			"http://www.live-socks.net/search/label/Socks%205?max-results=100&start=0&by-date=false",
			"http://www.vipsocks24.net/search/label/Socks%205?max-results=100&start=0&by-date=false",
			"http://socksproxylist24.blogspot.co.uk/search/label/Socks%20Proxy?max-results=100&start=0&by-date=false",
		});
	}
	
	public ProxyScraper(Proxy.Type type){
		if (!basicSources.containsKey(type) && !blogspotSources.containsKey(type)){
			throw new IllegalArgumentException("No proxy sources for " + type.name());
		}
		
		this.type = type;
		this.results = null;
	}
	
	public void scrape(int threads){
		ProgressHandler progressHandler = new StreamOutputProgressHandler();
		CombinedListResultsHandler<Proxy> resultHandler = new CombinedListResultsHandler<Proxy>();
		ScraperQueue<BasicProxyListScraper, Proxy> queue = new ScraperQueue<BasicProxyListScraper, Proxy>(threads, 8, progressHandler, resultHandler);
		
		if (basicSources.containsKey(this.type)){
			for (String url : basicSources.get(this.type)){
				queue.addScraper(new BasicProxyListScraper(this.type, url));
			}
		}
		
		if (blogspotSources.containsKey(this.type)){
			for (String url : blogspotSources.get(this.type)){
				queue.addScraper(new BlogspotPostsProxyListScraper(this.type, url));
			}
		}
		
		if (this.type == Proxy.Type.HTTP){
			queue.addScraper(new GatherProxyListScraper(this.type, "http://gatherproxy.com/proxylist/anonymity/?t=Elite", "elite", 1));
			queue.addScraper(new GatherProxyListScraper(this.type, "http://gatherproxy.com/proxylist/anonymity/?t=Anonymous", "anonymous", 1));
		}else if (this.type == Proxy.Type.SOCKS){
			queue.addScraper(new GatherProxySocksListScraper(this.type, "United%20States"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Brazil"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Mexico"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Sweden"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Norway"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "France"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Taiwan"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "India"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Republic%20of%20Korea"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Denmark"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Russia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "China"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Ukraine"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Canada"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Austria"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Switzerland"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Colombia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Germany"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Australia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Finland"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Venezuela"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Romania"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Poland"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "South%20Africa"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Italy"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Netherlands"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Bolivia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Hong%20Kong"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Puerto%20Rico"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Argentina"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Serbia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Bangladesh"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Kazakhstan"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Cambodia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Pakistan"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Botswana"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Indonesia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "United%20Kingdom"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Haiti"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Spain"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Singapore"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Guam"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Vietnam"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "U.S.%20Virgin%20Islands"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Slovak%20Republic"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Chile"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Belgium"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Republic%20of%20Moldova"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Malaysia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Bulgaria"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Iraq"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Czech%20Republic"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Turkey"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Ecuador"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Guinea"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Kenya"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Belarus"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Iran"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Estonia"));
			queue.addScraper(new GatherProxySocksListScraper(this.type, "Malawi"));
		}
		
		queue.scrape();
		
		this.results = new HashSet<Proxy>(resultHandler.getResults());
	}
	
	public void check(int threads){
		String reference = "127.0.0.1";
		
		ProgressHandler progressHandler = new StreamOutputProgressHandler();
		CombinedListResultsHandler<Proxy> resultHandler = new CombinedListResultsHandler<Proxy>();
		ScraperQueue<ProxyCheckScraper, Proxy> queue = new ScraperQueue<ProxyCheckScraper, Proxy>(threads, 1, progressHandler, resultHandler);
		
		try{
			HttpURLConnection connection = (HttpURLConnection) (new URL("https://jacekk.co.uk/ip.php")).openConnection();
			
			connection.setReadTimeout(4000);
			connection.setConnectTimeout(4000);
			connection.setUseCaches(false);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			StringBuilder builder = new StringBuilder();
			
			while ((reference = input.readLine()) != null){
				builder.append(reference);
			}
			
			reference = builder.toString().trim();
			
			input.close();
		}catch (Exception e){
			e.printStackTrace();
		}
				
		for (Proxy proxy : this.results){
			queue.addScraper(new ProxyCheckScraper(proxy, reference));
		}
		
		queue.scrape();
		
		this.results = new HashSet<Proxy>(resultHandler.getResults());
	}
	
	public Proxy.Type getType(){
		return this.type;
	}
	
	public Set<Proxy> getResults(){
		return this.results;
	}
	
	public static void main(String args[]){
		if (args.length != 2){
			System.err.println("Usage: ProxyScraper.jar <type> <file>");
			System.exit(1);
		}
		
		Proxy.Type proxyType = null;
		
		try{
			proxyType = Proxy.Type.valueOf(args[0]);
		}catch (IllegalArgumentException e){
			System.err.println(args[0] + " is not a valid proxy type");
			System.exit(1);
		}
		
		File file = new File(args[1]);
		
		try{
			file.createNewFile();
		}catch (IOException e){
			System.err.println("Failed to create file: " + e.getMessage());
			System.exit(1);
		}
		
		ProxyScraper scraper = new ProxyScraper(proxyType);
		
		scraper.scrape(10);
		scraper.check(250);
		
		try{
			FileWriter writer = new FileWriter(file);
			
			for (Proxy proxy : scraper.getResults()){
				InetSocketAddress address = (InetSocketAddress) proxy.address();
				
				writer.write(address.getHostString() + ":" + address.getPort());
				writer.write('\n');
			}
			
			writer.close();
		}catch (IOException e){
			System.err.println("Failed to write proxy file: " + e.getMessage());
			System.exit(1);
		}
	}
	
}
