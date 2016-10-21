package uk.co.jacekk.proxyscraper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import uk.co.jacekk.scraperlib.Scraper;

public class ProxyCheckScraper extends Scraper<Proxy> {
	
	private Proxy proxy;
	private String reference;
	private Gson gson;
	
	public ProxyCheckScraper(Proxy proxy, String reference){
		super(null);
		
		this.proxy = proxy;
		this.reference = reference;
		this.gson = (new GsonBuilder()).create();
	}
	
	@Override
	public void scrape(List<Scraper<Proxy>> newScrapers, List<Proxy> results) throws IOException {
		HttpURLConnection connection;
		String remoteAddr = null;
		String forwardedAddr = null;
		
		try {
			connection = (HttpURLConnection) (new URL("https://jacekk.co.uk/verify.php")).openConnection(this.proxy);
			
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(6000);
			connection.setUseCaches(false);
			
			InputStreamReader input = new InputStreamReader(connection.getInputStream());
			
			HashMap<String, String> fields = this.gson.fromJson(input, new TypeToken<HashMap<String, String>>(){}.getType());
			
			remoteAddr = fields.get("remote_addr");
			forwardedAddr = fields.get("forwarded_addr");
			
			input.close();
		} catch (IOException exception){
			throw new IOException("Failed to connect via proxy");
		}
	
		// Proxy didn't connect or somehow sent our real IP
		if (remoteAddr == null || remoteAddr.equals(this.reference)){
			throw new IOException("Proxy returned no content or somehow sent our real IP");
		}
		
		// Proxy passed out real IP
		if (forwardedAddr != null && forwardedAddr.equals(this.reference)){
			throw new IOException("Proxy sent our real IP as a HTTP header");
		}
		
		results.add(this.proxy);
	}
	
}
