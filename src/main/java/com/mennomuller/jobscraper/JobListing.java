package com.mennomuller.jobscraper;

import java.io.Serializable;

public record JobListing(String companyName, String jobTitle, String description, String link) implements Serializable {
}
