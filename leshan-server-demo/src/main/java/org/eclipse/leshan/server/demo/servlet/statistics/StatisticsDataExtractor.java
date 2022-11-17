package org.eclipse.leshan.server.demo.servlet.statistics;

import java.util.Map;

import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.elements.EndpointContext;

public interface StatisticsDataExtractor {
    Map<String, ?> extractData(Message message, ConnectionStatistics.MessageType type, EndpointContext endpointContext);
}
