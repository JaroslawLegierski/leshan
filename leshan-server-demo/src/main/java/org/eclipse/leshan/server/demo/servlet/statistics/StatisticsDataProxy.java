package org.eclipse.leshan.server.demo.servlet.statistics;

import java.util.List;
import java.util.Map;

public interface StatisticsDataProxy {
    void collectDataFromCache(List<Map<String, String>> data);
}
