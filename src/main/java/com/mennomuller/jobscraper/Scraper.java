package com.mennomuller.jobscraper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scraper {

    private final FirefoxOptions options = new FirefoxOptions();
    private final WebDriver driver;
    private ArrayList<JobListing> jobListings = new ArrayList<>();

    private final String[] filterWords = {"consultant", "manager", "administratief", "analist", "beheerder", "support", "service", "people", "trainer", "dtp'er", "redactie", "medewerker"};

    public Scraper() {
        WebDriverManager.firefoxdriver().setup();

        //set to headless so the browser window isn't visible
        options.addArguments("--headless");

        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        driver.navigate().to("https://animo.id/jobs");
    }

    public void checkAnimo() throws InterruptedException {
        driver.navigate().to("https://animo.id/jobs");

        // lists of WebElements that store all the job postings
        List<WebElement> raw_job_descs = driver.findElements(By.cssSelector("p.line-clamp-3"));
        List<WebElement> raw_job_links = driver.findElements(By.cssSelector("div[href].grow"));

        if (raw_job_links.size() == 0) {
            System.out.println("NO jobs at Animo, check for site changes.");
            return;
        }
        WebDriver driver2 = new FirefoxDriver(options);
        driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        // loop through raw_jobs and add them to the big job list
        for (int i = 0; i < raw_job_links.size(); i++) {
            String description = raw_job_descs.get(i).getText();
            String jobLink = "https://animo.id" + raw_job_links.get(i).getAttribute("href");
            driver2.navigate().to(jobLink);
            //noinspection BusyWait
            Thread.sleep(1000);
            String jobTitle = driver2.findElement(By.tagName("h1")).getText();
            jobListings.add(new JobListing("Animo Solutions", jobTitle, description, jobLink));
        }
        driver2.close();
    }

    public void checkBloxs() {
        driver.navigate().to("https://www.bloxs.com/en/about-bloxs/vacancies/");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("elementskit-post-card"));

        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Bloxs, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("entry-title")).findElement(By.tagName("a")).getText();
            String description = e.findElement(By.tagName("p")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Bloxs", jobTitle, description, jobLink));
        }
    }

    public void checkCadac() throws InterruptedException {
        driver.navigate().to("https://www.cadac.com/cadac-careers/vacatures/#sort=standard&location=utrecht");
        // click the accept cookies button
        driver.findElement(By.id("ost-btn-accept-all-cookies")).click();
        Thread.sleep(1000);
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("product-card--job"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Cadac, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            if (e.findElement(By.className("product-card__location")).getText().equals("Utrecht")) {
                String jobTitle = e.findElement(By.tagName("b")).getText();
                String description = e.findElement(By.className("product-card__helper-text")).getText();
                String jobLink = e.getAttribute("href");
                jobListings.add(new JobListing("Cadac Group", jobTitle, description, jobLink));
            }
        }
    }

    public void checkExact() {
        try {
            driver.navigate().to("https://www.exact.com/careers/vacancies#overview");
            // click the accept cookies button
            driver.findElement(By.id("stormdigital-action-accept-all")).click();
            // filter for the Netherlands
            driver.findElement(By.id("attribute_5_49")).click();
            // filter for Technology
            driver.findElement(By.id("attribute_4_4")).click();
            // filter for Utrecht
            driver.findElement(By.id("attribute_3_786")).click();
            // list of WebElements that store all the job postings
            List<WebElement> raw_jobs = driver.findElements(By.className("vacancy-list-item__link"));
            if (raw_jobs.size() == 0) {
                System.out.println("NO jobs at Exact, check for site changes.");
                return;
            }
            // loop through raw_jobs and add them to the big job list
            for (WebElement e : raw_jobs) {
                String jobTitle = e.findElement(By.className("vacancy-list-item__title")).getText();
                String description = e.findElement(By.className("vacancy-list-item__description")).getText();
                String jobLink = e.getAttribute("href");
                jobListings.add(new JobListing("Exact", jobTitle, description, jobLink));
            }
        } catch (Exception e) {
            System.out.println("NO jobs at Exact, check for site changes.");
        }
    }

    public void checkFaqta() {
        driver.navigate().to("https://werkenbijfaqta.nl/");
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("sqs-block-button-container--center"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Faqta, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        WebDriver driver2 = new FirefoxDriver(options);
        driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("sqs-block-button-element")).getText();
            if (jobTitle.equals("Open sollicitatie")) {
                continue;
            }
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            driver2.navigate().to(jobLink);
            String description = driver2.findElements(By.cssSelector("div.index-section-wrapper p")).get(1).getText();
            jobListings.add(new JobListing("Faqta", jobTitle, description, jobLink));
        }
        driver2.close();
    }

    public void checkHydroLogic() {
        driver.navigate().to("https://www.hydrologic.nl/werken-bij-hydrologic/vacatures-ict/");
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElement(By.className("tiles-nomix")).findElements(By.tagName("li"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at HydroLogic, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        WebDriver driver2 = new FirefoxDriver(options);
        driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.tagName("h2")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            driver2.navigate().to(jobLink);
            String description = driver2.findElement(By.tagName("em")).getText();
            jobListings.add(new JobListing("HydroLogic", jobTitle, description, jobLink));
        }
        driver2.close();
    }

    public void checkIncentro() throws InterruptedException {
        driver.navigate().to("https://careers.incentro.com/nl-NL/vacatures?locale=Utrecht");
        Thread.sleep(1000);
        driver.findElement(By.className("ch2-allow-all-btn")).click();
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.cssSelector("ul.light li"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Incentro, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.tagName("h2")).getText();
            if (jobTitle.equals("Ik wil graag bij Incentro werken!")) {
                continue;
            }
            String description = e.findElement(By.tagName("p")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Incentro", jobTitle, description, jobLink));
        }
    }

    public void checkIntragen() {
        driver.navigate().to("https://intragen.bamboohr.com/careers");
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("css-ldt7kr"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Intragen, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.tagName("a")).getText();
            StringBuilder description = new StringBuilder();
            for (WebElement el : e.findElements(By.tagName("p"))) {
                description.append(el.getText()).append(" ");
            }
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Intragen", jobTitle, description.toString(), jobLink));
        }
    }

    public void checkJio() {
        driver.navigate().to("https://www.werkenvoornederland.nl/vacatures?term=Justiti%C3%ABle%20ICT%20Organisatie&vakgebied=CVG.08&type=vacature&werkgever=01480&postcode=3511CE&afstand=20km");
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("vacancy"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at JIO, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("vacancy__title")).findElement(By.tagName("a")).getText();
            String description = e.findElement(By.className("vacancy__description")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            String employer = e.findElement(By.className("vacancy__employer")).getText();
            if (employer.contains("Justitiële ICT Organisatie")) {
                jobListings.add(new JobListing("JIO", jobTitle, description, jobLink));
            }
        }
    }

    public void checkKeylane() {
        driver.navigate().to("https://careers.keylane.com/jobs/?search=&fcat%5B%5D=software-development&fcnt%5B%5D=netherlands");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("bkwa-item"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Keylane, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        WebDriver driver2 = new FirefoxDriver(options);
        driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("bkwa-item-title")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            driver2.navigate().to(jobLink);
            String description = driver2.findElements(By.cssSelector("div#left-column p")).get(1).getText();
            jobListings.add(new JobListing("Keylane", jobTitle, description, jobLink));
        }
        driver2.close();
    }

    public void checkMoogue() {
        driver.navigate().to("https://www.moogue.com/vacatures");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("single_vacature"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Moogue, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("title")).getText();
            String description = e.findElement(By.className("col-sm-9")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Moogue", jobTitle, description, jobLink));
        }
    }

    public void checkPridis() throws InterruptedException {
        driver.navigate().to("https://pridis.com/about/careers");
        Thread.sleep(1000);
        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("t422__container"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Pridis, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("t422__title")).getText();
            String description = e.findElement(By.className("t422__descr")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Pridis", jobTitle, description, jobLink));
        }
    }

    public void checkQuintor() {
        driver.navigate().to("https://quintor.nl/vacatures/#vacature-professional");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElement(By.className("fusion-builder-column-1")).findElements(By.className("post-content"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Quintor, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("entry-title")).findElement(By.tagName("a")).getText();
            String description = e.findElement(By.tagName("p")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Quintor", jobTitle, description, jobLink));
        }
    }

    public void checkSensorFact() {
        driver.navigate().to("https://www.sensorfact.eu/careers/");

        List<String> departments = driver.findElements(By.className("department")).stream().map(WebElement::getText).toList();
        int dev_dept = departments.indexOf("Development");

        // list of WebElements that store all the job postings for Development
        List<WebElement> raw_jobs = driver.findElements(By.className("stretch")).get(dev_dept).findElements(By.className("vacancy-card"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at SensorFact, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("h6")).getText();
            String description = e.findElements(By.tagName("li")).get(0).getText() + " " + e.findElements(By.tagName("li")).get(1).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("SensorFact", jobTitle, description, jobLink));
        }
    }

    public void checkSocialBrothers() {
        driver.navigate().to("https://www.werkenbijsocialbrothers.nl/vacature/?search=&team=Front-end%20(WordPress),Front-end%20(React)&subcat=Fulltime&clang=");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("jobs__post"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Social Brothers, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("jobs__title")).getText();
            String description = e.findElement(By.className("jobs__content")).getText();
            String jobLink = e.getAttribute("href");
            jobListings.add(new JobListing("Social Brothers", jobTitle, description, jobLink));
        }
    }

    public void checkTaxonic() {
        driver.navigate().to("https://www.taxonic.com/werken-bij-taxonic/");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("uagb-post__inner-wrap"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Taxonic, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("uagb-post__title")).findElement(By.tagName("a")).getText();
            String description = e.findElement(By.className("uagb-post__excerpt")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Taxonic", jobTitle, description, jobLink));
        }
    }

    @SuppressWarnings("unused")
    public void checkTheHyve() {
        driver.navigate().to("https://www.thehyve.nl/jobs");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.cssSelector("li a[rel]"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at The Hyve, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        WebDriver driver2 = new FirefoxDriver(options);
        driver2.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        for (WebElement e : raw_jobs) {
            String jobTitle = e.getText();
            String jobLink = e.getAttribute("href");
            driver2.navigate().to(jobLink);
            String description = driver2.findElements(By.tagName("p")).get(0).getText();
            jobListings.add(new JobListing("The Hyve", jobTitle, description, jobLink));
        }
        driver2.close();
    }

    public void checkValk() {
        driver.navigate().to("https://www.valksolutions.nl/career/vacancies/");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("eael-entry-wrapper"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Valk Solutions, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("eael-entry-title")).findElement(By.tagName("a")).getText();
            String description = e.findElement(By.tagName("p")).getText();
            String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
            jobListings.add(new JobListing("Valk Solutions", jobTitle, description, jobLink));
        }
    }

    public void checkVolksbank() throws InterruptedException {
        driver.navigate().to("https://werkenbij.devolksbank.nl/nl/nl/search-results");
        // filter for software development
        driver.findElement(By.id("AandachtsgebiedAccordion")).click();
        driver.findElement(By.cssSelector("input[data-ph-at-text=\"Software development \"] + span.checkbox")).click();
        Thread.sleep(1000);


        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.className("jobs-list-item"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at Volksbank, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("job-title")).findElement(By.tagName("span")).getText();
            String description = e.findElement(By.cssSelector("span.au-target.city span.au-target")).getText();
            if (description.contains("Utrecht")) {
                String jobLink = e.findElement(By.tagName("a")).getAttribute("href");
                jobListings.add(new JobListing("de Volksbank", jobTitle, description, jobLink));
            }
        }
    }

    public void checkWeCity() {
        driver.navigate().to("https://www.wecity.nl/nl/vacatures");

        // list of WebElements that store all the job postings
        List<WebElement> raw_jobs = driver.findElements(By.cssSelector("a.group"));
        if (raw_jobs.size() == 0) {
            System.out.println("NO jobs at WeCity, check for site changes.");
            return;
        }
        // loop through raw_jobs and add them to the big job list
        for (WebElement e : raw_jobs) {
            String jobTitle = e.findElement(By.className("font-bold")).getText();
            String description = e.findElement(By.className("line-clamp-1")).getText();
            String jobLink = e.getAttribute("href");
            jobListings.add(new JobListing("WeCity", jobTitle, description, jobLink));
        }
    }

    public void writeToFile() {
        //write to json file
        ObjectMapper objectMapper = new ObjectMapper();
        File jobsFile = new File("jobs-data.json");
        try {
            objectMapper.writeValue(jobsFile,jobListings);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void closeDriver() {
        driver.close();
    }

    public List<JobListing> removeIrrelevantJobs(List<JobListing> allJobs) {
        return allJobs.stream()
                .filter(job -> Arrays.stream(filterWords)
                        .noneMatch(word -> job.jobTitle().toLowerCase().contains(word)))
                .toList();
    }

    public void checkNewListings() {
        ObjectMapper objectMapper = new ObjectMapper();
        File jobsFile = new File("jobs-data.json");
        try {
            //get previous list of jobs from JSON file
            List<JobListing> oldJobs = objectMapper.readValue(jobsFile, new TypeReference<>(){});
            //filter out the ones that have previously appeared
            List<JobListing> newJobs = jobListings.stream()
                    .filter(job -> oldJobs.stream()
                            .noneMatch(job::equals))
                    .toList();



            if (newJobs.size() > 0) {
                LocalDateTime now = LocalDateTime.now();
                File jobFile = new File("job-lists\\" + now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth() + "_" + now.getHour() + "_" + now.getMinute() + "_" + now.getSecond() + "_jobs.txt");
                if (jobFile.createNewFile()) {
                    System.out.println("File created: " + jobFile.getName());
                } else {
                    System.out.println("File already exists.");
                }
                FileWriter jobWriter = new FileWriter(jobFile);

                System.out.println(newJobs.size() + " new job " + (newJobs.size() == 1 ? "listing found." : "listings found."));
                jobWriter.write(newJobs.size() + " new job " + (newJobs.size() == 1 ? "listing found.\n\n" : "listings found.\n\n"));
                for (JobListing job : newJobs) {
                    System.out.println(job.companyName());
                    jobWriter.write(job.companyName() + "\n");
                    System.out.println(job.jobTitle());
                    jobWriter.write(job.jobTitle() + "\n");
                    System.out.println(job.description());
                    jobWriter.write(job.description() + "\n");
                    System.out.println(job.link());
                    jobWriter.write(job.link() + "\n\n");
                }
                jobWriter.close();
            } else {
                System.out.println("0 new job listings found.");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void deleteAllListings() {
        jobListings.clear();
    }

    public void checkCompanies() throws InterruptedException {
        System.out.println("Searching for jobs...");
        LocalTime start = LocalTime.now();
        checkAnimo();
        checkBloxs();
        checkCadac();
        checkExact();
        checkFaqta();
        checkHydroLogic();
        checkIncentro();
        checkIntragen();
        checkJio();
        checkKeylane();
        checkMoogue();
        checkPridis();
        checkQuintor();
        checkSensorFact();
        checkSocialBrothers();
        checkTaxonic();
        //checkTheHyve();
        checkValk();
        checkVolksbank();
        checkWeCity();

        jobListings = new ArrayList<>(removeIrrelevantJobs(jobListings));
        Duration timer = Duration.between(start, LocalTime.now());
        System.out.println("Search finished in " + timer.toMinutes() + ":" + timer.toSecondsPart());
    }
}
