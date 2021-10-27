package com.ALKaisi.coronavirustracker.services;

import com.ALKaisi.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private  static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/datasets/covid-19/master/data/countries-aggregated.csv";
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    // The method below excute code every seconds
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
       HttpResponse<String> httpResponse =  client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        for (CSVRecord record : records) {
            LocationStats locationstats = new LocationStats();
            locationstats.setState(record.get("Country"));
            locationstats.setCountry(record.get("Country"));
            int latestCases = Integer.parseInt(record.get(record.size() -1));
            int prevDayCases = Integer.parseInt(record.get(record.size() -2));
            locationstats.setLatestTotalCases(latestCases);
            locationstats.setDiffFromPrevDay(latestCases - prevDayCases);
            System.out.println(locationstats);
            newStats.add(locationstats);

        }
        this.allStats = newStats;
    }

}
