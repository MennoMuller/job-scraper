package com.mennomuller.jobscraper;

public class Main {
    public static void main(String[] args) throws Exception {
        Scraper scraper = new Scraper();
        scraper.checkCompanies();
        scraper.closeDriver();
        scraper.checkNewListings();
        scraper.writeToFile();
    }
}