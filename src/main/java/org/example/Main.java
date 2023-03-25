package org.example;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        File dir = new File("reports");
        if (!dir.exists()) {
            boolean ignored = dir.mkdir();
        }
        for (Map.Entry<String, List<String>> client : clients.entrySet()) {
            File file = new File("reports\\Report for " + client.getKey());
            try (FileWriter fw = new FileWriter(file)) {
                boolean ignored = file.createNewFile();
                String tariff = client.getValue().get(0).substring(36);
                fw.write("Tariff index: " + tariff + "\n");
                fw.write("--------------------------------------------------------------------------------------\n");
                fw.write("Report for phone number " + client.getKey() + ":\n");
                fw.write("--------------------------------------------------------------------------------------\n");
                fw.write("| Call Type |     Start Time      |      End Time       |   Duration   |    Cost     |\n");
                fw.write("--------------------------------------------------------------------------------------\n");
                client.getValue().sort(Comparator.comparing(o -> o.substring(4, 18)));
                double minsCount = 0;
                double sum = 0;
                int count = 0;
                DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
                DecimalFormat df = new DecimalFormat("0.00", dfs);
                for (String call : client.getValue()) {
                    count++;
                    String callType = call.substring(0, 2);
                    fw.write("|     " + callType + "    |");
                    String startTime = call.substring(4, 18);
                    LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String newStartTime = startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    fw.write(" " + newStartTime + " |");
                    String endTime = call.substring(20, 34);
                    LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String newEndTime = endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    fw.write(" " + newEndTime + " |");
                    Duration duration = Duration.between(startDateTime, endDateTime);
                    long dth = duration.toHours();
                    String hours = dth < 10 ? "0" + dth : dth + "";
                    long dtm = duration.toMinutes() % 60;
                    String mins = dtm < 10 ? "0" + dtm : dtm + "";
                    long dts = duration.toSeconds() % 60;
                    String secs = dts < 10 ? "0" + dts : dts + "";
                    fw.write(String.format("%15s", " " + hours + ":" + mins + ":" + secs + " |"));
                    double currentMins = Math.ceil((double) duration.toMillis() / 1000 / 60);
                    double currentSum;
                    switch (tariff) {
                        case "06": {
                            double oldMinsCount = minsCount;
                            minsCount += currentMins;
                            if (minsCount <= 300) {
                                if (count == client.getValue().size()) {
                                    fw.write(String.format("%15s", df.format(100) + "|\n"));
                                    sum += 100;
                                    continue;
                                }
                                fw.write(String.format("%15s", df.format(0) + "|\n"));
                                continue;
                            }
                            if (oldMinsCount > 300) {
                                currentSum = currentMins;
                                fw.write(String.format("%15s", df.format(currentSum) + "|\n"));
                                sum += currentSum;
                                continue;
                            }
                            currentSum = 100 + (minsCount - 300);
                            fw.write(String.format("%15s", df.format(currentSum) + "|\n"));
                            sum += currentSum;
                            break;
                        }
                        case "03": {
                            currentSum = currentMins * 1.5;
                            fw.write(String.format("%15s", df.format(currentSum) + "|\n"));
                            minsCount += currentMins;
                            sum += currentSum;
                            break;
                        }
                        case "11": {
                            double oldMinsCount = minsCount;
                            minsCount += currentMins;
                            if (callType.equals("02")) {
                                fw.write(String.format("%15s", df.format(0) + "|\n"));
                                continue;
                            }
                            if (minsCount <= 100) {
                                currentSum = currentMins * 0.5;
                                fw.write(String.format("%15s", df.format(currentSum) + "|\n"));
                                sum += currentSum;
                                continue;
                            }
                            if (oldMinsCount > 100) {
                                currentSum = currentMins * 1.5;
                                fw.write(String.format("%15s", df.format(currentSum) + "|\n"));
                                sum += currentSum;
                                continue;
                            }
                            currentSum = Math.ceil(minsCount - 100) * 1.5 + Math.ceil(100 - oldMinsCount) * 0.5;
                            fw.write(String.format("%15s", df.format(currentSum) + "|\n"));
                            sum += currentSum;
                            break;
                        }
                    }
                }
                fw.write("--------------------------------------------------------------------------------------\n");
                fw.write("|                                           Total Cost: |     "
                        + String.format("%15s", df.format(sum)) + " rubles |\n");
                fw.write("--------------------------------------------------------------------------------------\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}