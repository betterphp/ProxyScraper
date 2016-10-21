package uk.co.jacekk.proxyscraper;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import uk.co.jacekk.scraperlib.Scraper;

public class BlogspotPostsProxyListScraper extends ProxyListScraper {
	
	public BlogspotPostsProxyListScraper(Proxy.Type proxyType, String url){
		super(proxyType, url);
	}

	@Override
	public void scrape(List<Scraper<Proxy>> newScrapers, List<Proxy> results) throws IOException {
		Document document = Jsoup.parse(new URL(this.getUrl()), 5000);
		
		for (Element link : document.select(".post-title > a")){
			String postUrl = link.attr("href");
			
			if (postUrl != null){
				newScrapers.add(new BasicProxyListScraper(this.proxyType, postUrl));
			}
		}
	}
	
}
