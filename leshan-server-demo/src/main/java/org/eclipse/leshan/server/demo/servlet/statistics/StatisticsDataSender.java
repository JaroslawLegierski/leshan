package org.eclipse.leshan.server.demo.servlet.statistics;

import org.eclipse.leshan.server.demo.servlet.EventServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StatisticsDataSender implements StatisticsDataProxy {
    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    @Override public void collectDataFromCache(List<Map<String, String>> data) {
        try {
            sendPOST("http://localhost:8081/post-statistics", serializeToJSON(data));
            LOG.info("Sent {} data points to statistics service", data.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String serializeToJSON(List<Map<String, String>> data) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');

        for(Map<String, String> map : data) {
            stringBuilder.append('{');
            for(Map.Entry<String, String> entry : map.entrySet()) {
                stringBuilder.append(String.format("\"%s\":\"%s\",", entry.getKey(), entry.getValue()));
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.append("},");
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    private static void sendPOST(String urlString, String body) throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        byte[] out = body.getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        int responseCode = http.getResponseCode();
    }
}
