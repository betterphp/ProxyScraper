package uk.co.jacekk.proxyscraper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class ProxyScraper {
	
	private static final HashMap<Proxy.Type, String[]> sources;
	
	private String[] sites;
	private Proxy.Type type;
	
	protected ArrayBlockingQueue<ProxyScraperThread> threads;
	protected List<Proxy> results;
	
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
		this.results = Collections.synchronizedList(new ArrayList<Proxy>());
	}
	
	public void scrape(int threads){
		if (threads > this.sites.length){
			threads = this.sites.length;
		}
		
		this.threads = new ArrayBlockingQueue<ProxyScraperThread>(threads);
		
		for (String url : this.sites){
			try{
				ProxyScraperThread thread = new ProxyScraperThread(this, url);
				this.threads.put(thread);
				thread.start();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
		synchronized (this.threads){
			while (!this.threads.isEmpty()){
				try{
					this.threads.wait();
				}catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public Proxy.Type getType(){
		return this.type;
	}
	
	public List<Proxy> getResults(){
		return this.results;
	}
	
	public ProxyList getProxyList(){
		return new ProxyList(this.results);
	}
	
	public ProxyList scrapeProxyList(int threads){
		this.scrape(threads);
		
		return this.getProxyList();
	}
	
	public static void main(String args[]){
		if (args.length != 3){
			System.err.println("Usage: ProjexyScraper.jar <type> <threads> <file>");
			System.exit(1);
		}
		
		Proxy.Type proxyType = null;
		int threads = 10;
		
		try{
			proxyType = Proxy.Type.valueOf(args[0]);
		}catch (IllegalArgumentException e){
			System.err.println(args[0] + " is not a valid proxy type");
			System.exit(1);
		}
		
		try{
			threads = Integer.parseInt(args[1]);
		}catch (NumberFormatException e){
			System.err.println("Usage: ProjexyScraper.jar <threads> <file>");
			System.exit(1);
		}
		
		File file = new File(args[2]);
		
		try{
			file.createNewFile();
		}catch (IOException e){
			System.err.println("Failed to create file: " + e.getMessage());
			System.exit(1);
		}
		
		ProxyScraper scraper = new ProxyScraper(proxyType);
		ProxyList list = scraper.scrapeProxyList(threads);
		
		list.check(threads);
		
		try{
			FileWriter writer = new FileWriter(file);
			
			for (Proxy proxy : list.getWorking()){
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
