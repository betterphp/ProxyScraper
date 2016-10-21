package uk.co.jacekk.proxyscraper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import uk.co.jacekk.scraperlib.Scraper;

public class GatherProxySocksListScraper extends ProxyListScraper {

	private String country;
	
	public GatherProxySocksListScraper(Type proxyType, String country){
		super(proxyType, "http://gatherproxy.com/sockslist/country/?c=" + country);
		
		this.country = country;
	}

	@Override
	public void scrape(List<Scraper<Proxy>> newScrapers, List<Proxy> results) throws IOException {
		Document document = Jsoup
				.connect(this.getUrl())
				.data("Country", this.country.replaceAll("%20", "+"))
				.userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0")
				.post();
		
		for (Element row : document.select("#tblproxy tr")){
			Elements cells = row.getElementsByTag("td");
			
			if (cells.size() != 7 || row.hasClass("caption")){
				continue;
			}
			
			String ip = cells.get(1).html().replaceAll(" ", "").trim();
			String port = cells.get(2).html().replaceAll(" ",  "").trim();
			
			ip = ip.substring(24, ip.length() - 11);
			port = port.substring(24, port.length() - 11);
			
			results.add(new Proxy(this.proxyType, new InetSocketAddress(ip, Integer.parseInt(port))));
		}
	}
	
}
