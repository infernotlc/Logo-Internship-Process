package org.example;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GetCitiesFromAPI {

    private static final String API_URL = "https://countriesnow.space/api/v0.1/countries/cities";

    public static void main(String[] args) {
        String country = "turkey";
        try {
            ArrayList<String> cityNames = fetchCityNames(country);
            if (cityNames != null) {
                searchCityNames(cityNames);
            } else {
                System.out.println("Failed to fetch city names.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> fetchCityNames(String country) throws IOException, JSONException {
        HttpURLConnection connection = createPostConnection(API_URL);
        setHeaders(connection, Map.of("Content-Type", "application/json"));

        JSONObject requestBody = new JSONObject();
        requestBody.put("country", country);
        sendRequest(connection, requestBody.toString());

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return parseResponse(connection);
        } else {
            System.out.println("Error: HTTP response code " + responseCode);
            return null;
        }
    }

    private static HttpURLConnection createPostConnection(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

    private static void setHeaders(HttpURLConnection connection, Map<String, String> headers) {
        headers.forEach(connection::setRequestProperty);
    }

    private static void sendRequest(HttpURLConnection connection, String requestBody) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
    }

    private static ArrayList<String> parseResponse(HttpURLConnection connection) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject responseJson = new JSONObject(response.toString());
        JSONArray cities = responseJson.getJSONArray("data");

        ArrayList<String> cityNames = new ArrayList<>();
        for (int i = 0; i < cities.length(); i++) {
            cityNames.add(cities.getString(i));
        }
        return cityNames;
    }

    private static void searchCityNames(ArrayList<String> cityNames) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Search characters (or 'q' to quit): ");
            String searchQuery = scanner.nextLine();

            if (searchQuery.equalsIgnoreCase("q")) {
                break;
            }

            Pattern pattern = Pattern.compile(searchQuery, Pattern.CASE_INSENSITIVE);
            boolean matchFound = false;

            for (String cityName : cityNames) {
                Matcher matcher = pattern.matcher(cityName);
                if (matcher.find()) {
                    System.out.println("Match found: " + cityName);
                    matchFound = true;
                }
            }

            if (!matchFound) {
                System.out.println("No matches found for '" + searchQuery + "'");
            }
        }
        scanner.close();
    }
}

