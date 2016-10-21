package uk.co.jacekk.proxyscraper;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class AdaptiveProxyList {
	
	private Random random;
	private HashMap<Proxy, Integer> proxies;
	
	public AdaptiveProxyList(Proxy[] proxies){
		this.random = new Random();
		this.proxies = new HashMap<>();
		
		for (Proxy proxy : proxies){
			this.proxies.put(proxy, 0);
		}
	}
	
	public List<Proxy> getAll(){
		return new ArrayList<Proxy>(this.proxies.keySet());
	}
	
	public Integer size(){
		return this.proxies.size();
	}
	
	public synchronized Proxy getRandom(){
		Proxy[] list = this.proxies.keySet().toArray(new Proxy[this.proxies.size()]);

		return list[this.random.nextInt(list.length)];
	}
	
	public synchronized void logFailure(Proxy proxy){
		if (!this.proxies.containsKey(proxy)){
			throw new IllegalArgumentException("Proxy does not exist in list");
		}
				
		this.proxies.put(proxy, this.proxies.get(proxy) + 1);
	}
	
	public synchronized Integer getTotalFailures(Proxy proxy){
		return this.proxies.get(proxy);
	}
	
	public synchronized Proxy getBestRandom(Double consider){
		ArrayList<Proxy> proxyList = new ArrayList<>(this.proxies.keySet());
		ArrayList<Proxy> candidates = new ArrayList<>();
		
		proxyList.sort(new Comparator<Proxy>(){

			@Override
			public int compare(Proxy a, Proxy b){
				Integer aFailures = proxies.get(a);
				Integer bFailures = proxies.get(b);
				
				return aFailures.compareTo(bFailures);
			}
	
		});
		
		for (Integer i = 0; i < proxyList.size() * consider; ++i){
			candidates.add(proxyList.get(i));
		}
		
		return candidates.get(this.random.nextInt(candidates.size()));
	}
	
}
