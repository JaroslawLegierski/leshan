package org.eclipse.leshan.server.demo.servlet.statistics;

import java.util.List;
import java.util.Map;

public class StatisticsLogger implements StatisticsDataProxy {

    @Override public void collectDataFromCache(List<Map<String, String>> data) {
        System.out.println("######################STAT DATA######################");
        int i = 0;
        for(Map<String, String> map : data) {
            System.out.printf("#%d: ", ++i);
            for(Map.Entry<String, String> entry : map.entrySet()) {
                System.out.printf("[%s]: %s, ", entry.getKey(), entry.getValue());
            }
            System.out.println();
        }

    }
}
