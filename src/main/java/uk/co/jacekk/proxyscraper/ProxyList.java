package uk.co.jacekk.proxyscraper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class ProxyList {
	
	private List<Proxy> proxies;
	protected List<Proxy> working;
	protected ArrayBlockingQueue<ProxyCheckThread> threads;
	
	public ProxyList(List<Proxy> proxies){
		this.proxies = proxies;
		this.working = Collections.synchronizedList(new ArrayList<Proxy>());
	}
	
	public ProxyList(File file, Proxy.Type type) throws IOException {
		this.proxies = new ArrayList<Proxy>();
		this.working = new ArrayList<Proxy>();
		
		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		
		String line;
		
		while ((line = input.readLine()) != null){
			String[] parts = line.split(":");
			
			this.proxies.add(new Proxy(type, new InetSocketAddress(parts[0], Integer.parseInt(parts[1]))));
		}
		
		input.close();
	}
	
	public synchronized List<Proxy> getWorking(){
		return this.working;
	}
	
	public int getTotal(){
		return this.proxies.size();
	}
	
	public synchronized int getTotalWorking(){
		return this.working.size();
	}
	
	public void check(int threads){
		String reference = "127.0.0.1";
		
		try{
			HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacekk.co.uk/ip.php")).openConnection();
			
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
		
		this.threads = new ArrayBlockingQueue<ProxyCheckThread>(threads);
		
		Iterator<Proxy> iterator = this.proxies.iterator();
		
		while (iterator.hasNext()){
			Proxy proxy = iterator.next();
			iterator.remove();
			
			try{
				ProxyCheckThread thread = new ProxyCheckThread(this, proxy, reference);
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
	
}
