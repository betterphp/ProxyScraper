package uk.co.jacekk.proxyscraper;

import java.net.Proxy;
import uk.co.jacekk.scraperlib.Scraper;

abstract public class ProxyListScraper extends Scraper<Proxy> {
	
	protected Proxy.Type proxyType;
	
	public ProxyListScraper(Proxy.Type proxyType, String url){
		super(url);
		
		this.proxyType = proxyType;
	}

}
