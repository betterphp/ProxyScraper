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

public class GatherProxyListScraper extends ProxyListScraper {

	private Integer page;
	private String type;
	
	public GatherProxyListScraper(Type proxyType, String url, String type, Integer page){
		super(proxyType, url);
		
		this.page = page;
		this.type = type;
	}

	@Override
	public void scrape(List<Scraper<Proxy>> newScrapers, List<Proxy> results) throws IOException {
		Document document = Jsoup
				.connect(this.getUrl())
				.data("Type", this.type)
				.data("Uptime", "0")
				.data("PageIdx", this.page.toString())
				.userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0")
				.post();
		
		for (Element row : document.select("#tblproxy tr")){
			Elements cells = row.getElementsByTag("td");
			
			if (cells.size() != 8 || row.hasClass("caption")){
				continue;
			}
			
			String ip = cells.get(1).html().trim();
			String port = cells.get(2).html().trim();
			
			ip = ip.substring(24, ip.length() - 11);
			port = port.substring(31, port.length() - 12);
			
			results.add(new Proxy(this.proxyType, new InetSocketAddress(ip, Integer.parseInt(port, 16))));
		}
		
		// If this is the first page add a new scraper for the others
		if (this.page.equals(1)){
			for (Element pageLink : document.select(".pagenavi > a")){
				Integer nextPage = Integer.parseInt(pageLink.text().trim());
				
				if (nextPage > 1){
					newScrapers.add(new GatherProxyListScraper(this.proxyType, this.getUrl(), this.type, nextPage));
				}
			}
		}
	}
	
}
