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
	
	private static final HashMap<Proxy.Type, String[]> sources;
	
	private String[] sites;
	private Proxy.Type type;
	private HashSet<Proxy> results;
	
	static {
		sources = new HashMap<Proxy.Type, String[]>();
		
		sources.put(Proxy.Type.HTTP, new String[]{
			"http://proxy1e.blogspot.nl/feeds/posts/default",
			"http://proxypremium.blogspot.com/feeds/posts/default",
			"http://aliveproxies.com/ipproxy/page/1/",
			"http://aliveproxies.com/ipproxy/page/2/",
			"http://aliveproxies.com/ipproxy/page/3/",
			"http://aliveproxies.com/ipproxy/page/4/",
			"http://aliveproxies.com/ipproxy/page/5/",
			"http://aliveproxies.com/ipproxy/page/6/",
			"http://aliveproxies.com/ipproxy/page/7/",
			"http://aliveproxies.com/ipproxy/page/8/",
			"http://aliveproxies.com/ipproxy/page/9/",
			"http://aliveproxies.com/ipproxy/page/10/",
			"http://aliveproxies.com/ipproxy/page/11/",
			"http://aliveproxies.com/ipproxy/page/12/",
			"http://aliveproxies.com/ipproxy/page/13/",
			"http://aliveproxies.com/ipproxy/page/14/",
			"http://aliveproxies.com/ipproxy/page/15/",
			"http://aliveproxies.com/ipproxy/page/16/",
			"http://aliveproxies.com/ipproxy/page/17/",
			"http://aliveproxies.com/ipproxy/page/18/",
			"http://aliveproxies.com/ipproxy/page/19/",
			"http://aliveproxies.com/ipproxy/page/20/",
			"http://aliveproxies.com/pages/page-scrapebox-proxies/",
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
			"http://proxyserverlist-24.blogspot.com/feeds/posts/default",
			"http://sslproxies24.blogspot.com/feeds/posts/default",
			"http://topproxys.blogspot.com/feeds/posts/default",
		});
		
		sources.put(Proxy.Type.SOCKS, new String[]{
			"http://www.socks24.org/feeds/posts/default",
			"http://www.live-socks.net/feeds/posts/default",
			"http://socks5-proxy-list.blogspot.com/feeds/posts/default",
			"http://24h-sock.blogspot.nl/",
			"http://sockproxy.blogspot.com/feeds/posts/default",
			"http://freesockproxy.blogspot.com/feeds/posts/default",
			"http://sock5us.blogspot.com/feeds/posts/default",
			"http://socks5proxyus.blogspot.com/feeds/posts/default",
			"http://proxy-50.blogspot.com/feeds/posts/default",
			"http://freeproxy80.blogspot.com/feeds/posts/default",
			"http://webcheckproxy.blogspot.com/feeds/posts/default",
			"http://proxyblogspot.blogspot.com/feeds/posts/default",
			"http://blogspotproxy.blogspot.com/feeds/posts/default",
			"http://proxysockus.blogspot.com/feeds/posts/default",
			"http://www.vipsocks24.net/feeds/posts/default",
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
	}
	
	public ProxyScraper(Proxy.Type type){
		this.sites = sources.get(type);
		
		if (this.sites == null){
			throw new IllegalArgumentException("No proxy sources for " + type.name());
		}
		
		this.type = type;
		this.results = null;
	}
	
	public void scrape(int threads){
		ProgressHandler progressHandler = new StreamOutputProgressHandler();
		CombinedListResultsHandler<Proxy> resultHandler = new CombinedListResultsHandler<Proxy>();
		ScraperQueue<ProxyListScraper, Proxy> queue = new ScraperQueue<ProxyListScraper, Proxy>(threads, 8, progressHandler, resultHandler);
		
		for (String url : this.sites){
			queue.addScraper(new ProxyListScraper(this.type, url));
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
