package uk.co.jacekk.proxyscraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class ProxyCheckThread extends Thread implements Runnable {
	
	private ProxyList list;
	private Proxy proxy;
	private String reference;
	
	public ProxyCheckThread(ProxyList list, Proxy proxy, String reference){
		this.list = list;
		this.proxy = proxy;
		this.reference = reference;
	}
	
	@Override
	public int hashCode(){
		return this.proxy.hashCode();
	}
	
	@Override
	public boolean equals(Object compare){
		if (compare == this){
			return true;
		}
		
		if (!(compare instanceof ProxyCheckThread)){
			return false;
		}
		
		return ((ProxyCheckThread) compare).proxy.equals(this.proxy);
	}
	
	@Override
	public void run(){
		try{
			HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacekk.co.uk/ip.php")).openConnection(this.proxy);
			
			connection.setReadTimeout(4000);
			connection.setConnectTimeout(4000);
			connection.setUseCaches(false);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String response;
			StringBuilder builder = new StringBuilder();
			
			while ((response = input.readLine()) != null){
				builder.append(response);
			}
			
			response = builder.toString().trim();
			
			input.close();
			
			if (response.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}") && !response.equals(this.reference)){
				synchronized (this.list.working){
					this.list.working.add(this.proxy);
				}
			}
		}catch (Exception e){
			if (!(e instanceof IOException)){
				e.printStackTrace();
			}
		}
		
		synchronized (this.list.threads){
			this.list.threads.remove(this);
			this.list.threads.notify();
		}
		
	//	System.out.println(this.list.getTotalWorking() + "/" + this.list.getTotal());
	}
	
}
