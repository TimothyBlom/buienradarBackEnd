import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.Properties;

public class BuienradarApiClient {

    private static final String API_URL = "https://data.buienradar.nl/2.0/feed/json";
    private static final String CONFIG_FILE = "src\\config.properties";

    public static void main(String[] args) {
        BuienradarApiClient client = new BuienradarApiClient();

        // Read fetch frequency from configuration
        int fetchFrequencyHours = client.readFetchFrequencyFromConfig();

        while (true) {
            String response = client.fetchDataFromApi();

            if (response != null) {
                client.saveResponseToFile(response);
                System.out.println("Response saved to file.");

                // Sleep for the specified frequency before fetching again
                try {
                    Thread.sleep(fetchFrequencyHours * 3600000); // Convert hours to milliseconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to fetch data from API.");
            }
        }
    }

    public int readFetchFrequencyFromConfig() {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(inputStream);
            return Integer.parseInt(properties.getProperty("fetchFrequencyHours", "1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1; // Default to 1 hour if config reading fails
    }

    // Method to fetch data from the Buienradar API
    public String fetchDataFromApi() {
        try {
            // Create a URL object with the API endpoint
            URL url = new URL(API_URL);

            // Open a connection to the API endpoint
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get the HTTP response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // If response code indicates success, read the response content
                InputStream inputStream = connection.getInputStream();
                byte[] responseBytes = inputStream.readAllBytes();
                return new String(responseBytes, StandardCharsets.UTF_8);
            } else {
                System.out.println("API request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to save the API response to a local file
    public void saveResponseToFile(String response) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("buienradar_response.json"))) {
            // Write the response content to the file
            writer.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
