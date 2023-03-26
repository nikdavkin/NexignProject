package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

    public static void main(String[] args) throws IOException {
        Map<String, List<String>> clients = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("cdr.text"))) {
            String line = reader.readLine();
            while (line != null) {
                String client = line.substring(4, 15);
                String clientInfo = line.substring(0, 2) + ", " + line.substring(17);
                clients.putIfAbsent(client, new ArrayList<>());
                clients.get(client).add(clientInfo);
                line = reader.readLine();
            }
        }
        Path dir = Path.of("reports");
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
        double[] minsCountWrapper = new double[1];
        for (Map.Entry<String, List<String>> client : clients.entrySet()) {
            File file = dir.resolve("Report for " + client.getKey()).toFile();
            try (Writer writer = new BufferedWriter(new FileWriter(file))) {
                String tariff = client.getValue().get(0).substring(36);
                printHeader(client.getKey(), writer, tariff);
                client.getValue().sort(Comparator.comparing(o -> o.substring(4, 18)));
                minsCountWrapper[0] = 0.0;
                double sum = 0;
                List<String> value = client.getValue();
                for (int i = 0; i < value.size(); i++) {
                    String call = value.get(i);
                    String callType = call.substring(0, 2);
                    writer.write("|     " + callType + "    |");
                    LocalDateTime startDateTime = countAndPrintLocalDateTime(call, 4, 18, writer);
                    LocalDateTime endDateTime = countAndPrintLocalDateTime(call, 20, 34, writer);
                    Duration duration = Duration.between(startDateTime, endDateTime);
                    writer.write(countDuration(duration));
                    double currentMins = Math.ceil((double) duration.toMillis() / 1000 / 60);
                    boolean lastClientCall = i == client.getValue().size() - 1;
                    double currentSum = countSum(tariff, minsCountWrapper, callType, currentMins, lastClientCall);
                    writer.write(String.format("%15s", DECIMAL_FORMAT.format(currentSum) + "|\n"));
                    sum += currentSum;
                }
                writer.write("--------------------------------------------------------------------------------------\n");
                writer.write("|                                           Total Cost: |     "
                        + String.format("%15s", DECIMAL_FORMAT.format(sum)) + " rubles |\n");
                writer.write("--------------------------------------------------------------------------------------\n");
            }
        }
    }

    private static void printHeader(String client, Writer bw, String tariff) throws IOException {
        bw.write("Tariff index: " + tariff + "\n");
        bw.write("--------------------------------------------------------------------------------------\n");
        bw.write("Report for phone number " + client + ":\n");
        bw.write("--------------------------------------------------------------------------------------\n");
        bw.write("| Call Type |     Start Time      |      End Time       |   Duration   |    Cost     |\n");
        bw.write("--------------------------------------------------------------------------------------\n");
    }

    private static double countSum(String tariff, double[] minsCountWrapper, String callType, double currentMins, boolean lastClientCall) {
        switch (tariff) {
            case "06": {
                double oldMinsCount = minsCountWrapper[0];
                minsCountWrapper[0] += currentMins;
                if (minsCountWrapper[0] <= 300) {
                    if (lastClientCall) {
                        return 100;
                    }
                    return 0;
                }
                if (oldMinsCount > 300) {
                    return currentMins;
                }
                return 100 + (minsCountWrapper[0] - 300);
            }
            case "03": {
                minsCountWrapper[0] += currentMins;
                return currentMins * 1.5;
            }
            case "11": {
                double oldMinsCount = minsCountWrapper[0];
                minsCountWrapper[0] += currentMins;
                if (callType.equals("02")) {
                    return 0;
                }
                if (minsCountWrapper[0] <= 100) {
                    return currentMins * 0.5;
                }
                if (oldMinsCount > 100) {
                    return currentMins * 1.5;
                }
                return Math.ceil(minsCountWrapper[0] - 100) * 1.5 + Math.ceil(100 - oldMinsCount) * 0.5;
            }
            default:
                throw new IllegalArgumentException();
        }
    }

    private static LocalDateTime countAndPrintLocalDateTime(String call, int beginIndex, int endIndex, Writer bw) throws IOException {
        String time = call.substring(beginIndex, endIndex);
        LocalDateTime dateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String newStartTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bw.write(" " + newStartTime + " |");
        return dateTime;
    }

    private static String countDuration(Duration duration) {
        long dth = duration.toHours();
        String hours = dth < 10 ? "0" + dth : dth + "";
        long dtm = duration.toMinutes() % 60;
        String mins = dtm < 10 ? "0" + dtm : dtm + "";
        long dts = duration.toSeconds() % 60;
        String secs = dts < 10 ? "0" + dts : dts + "";
        return String.format("%15s", " " + hours + ":" + mins + ":" + secs + " |");
    }
}
