package uk.co.jacekk.proxyscraper;

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
			"http://free-proxyserver-list.blogspot.com/feeds/posts/default",
			"http://free-ssl-proxies.blogspot.com/feeds/posts/default",
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
		});
		
		sources.put(Proxy.Type.SOCKS, new String[]{
			"http://www.socks24.org/feeds/posts/default",
			"http://www.live-socks.net/feeds/posts/default",
			"http://www.vip-socks.net/search/label/VIP%20Socks",
			"http://www.vip-socks.net/search/label/Socks%205",
			"http://socks5-proxy-list.blogspot.com/feeds/posts/default",
			"http://socks5-servers.blogspot.com/feeds/posts/default",
			"http://24h-sock.blogspot.nl/",
			"http://sockproxy.blogspot.com/feeds/posts/default",
			"http://elitesocksproxy.blogspot.nl/feeds/posts/default",
			"http://freesockproxy.blogspot.com/feeds/posts/default",
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
	
}
