package uk.co.jacekk.proxyscraper;

import java.net.Proxy;

public class Main {
	
	public static void main(String[] args){
		int scrapeThreads = 6;
		int checkThreads = 24;
		
		if (args.length == 2){
			try{
				scrapeThreads = Integer.parseInt(args[0]);
			}catch (NumberFormatException e){
				System.err.println("Failed to parse number of threads '" + args[0] + "' will use " + scrapeThreads);
			}
			
			try{
				checkThreads = Integer.parseInt(args[0]);
			}catch (NumberFormatException e){
				System.err.println("Failed to parse number of threads '" + args[1] + "' will use " + checkThreads);
			}
		}
		
		ProxyScraper scraper = new ProxyScraper(Proxy.Type.SOCKS);
		
		scraper.scrape(scrapeThreads);
		
		ProxyList list = scraper.getProxyList();
		
		long start = System.currentTimeMillis();
		
		list.check(checkThreads);
		
		long timeTaken = (System.currentTimeMillis() - start) / 1000;
		
		System.out.println();
		System.out.println("Checked " + list.getTotal() + " proxies and found " + list.getTotalWorking() + " working in " + timeTaken + " seconds");
		System.out.println();
	}
	
}
