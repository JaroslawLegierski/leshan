package org.eclipse.leshan.server.demo.servlet.statistics;

import java.util.List;
import java.util.Map;

public interface StatisticsDataSender {
    void collectDataFromCache(List<Map<String, ?>> data);
}
