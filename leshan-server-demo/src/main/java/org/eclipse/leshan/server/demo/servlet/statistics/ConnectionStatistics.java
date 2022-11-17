package org.eclipse.leshan.server.demo.servlet.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.EndpointContext;

public class ConnectionStatistics {
    enum MessageType {SEND_REQUEST, SEND_RESPONSE, SEND_EMPTY, RECEIVE_REQUEST, RECEIVE_RESPONSE, RECEIVE_EMPTY}

    private final List<Map<String, ?>> cachedStatistics = new ArrayList<>();
    private final StatisticsDataSender statisticsDataSender;
    private final StatisticsDataExtractor statisticsDataExtractor;

    public ConnectionStatistics(StatisticsDataSender statisticsDataSender,
            StatisticsDataExtractor statisticsDataExtractor, long periodBetweenFlushMs) {
        this.statisticsDataSender = statisticsDataSender;
        this.statisticsDataExtractor = statisticsDataExtractor;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                flushData();
            }
        }, periodBetweenFlushMs, periodBetweenFlushMs);
    }

    private synchronized void putDataInCache(Map<String, ?> messageData) {
        cachedStatistics.add(messageData);
    }

    private synchronized void flushData() {
        if (cachedStatistics.isEmpty()) {
            return;
        }
        statisticsDataSender.collectDataFromCache(cachedStatistics);
        cachedStatistics.clear();
    }

    private void reportMessage(MessageType type, Message message, EndpointContext endpointContext) {
        Map<String, ?> messageData = statisticsDataExtractor.extractData(message, type, endpointContext);
        putDataInCache(messageData);
    }

    public void reportSendRequest(Request request, EndpointContext endpointContext) {
        reportMessage(MessageType.SEND_REQUEST, request, endpointContext);
    }

    public void reportSendResponse(Response response, EndpointContext endpointContext) {
        reportMessage(MessageType.SEND_RESPONSE, response, endpointContext);
    }

    public void reportSendEmpty(EmptyMessage empty, EndpointContext endpointContext) {
        reportMessage(MessageType.SEND_EMPTY, empty, endpointContext);
    }

    public void reportReceiveRequest(Request request, EndpointContext endpointContext) {
        reportMessage(MessageType.RECEIVE_REQUEST, request, endpointContext);
    }

    public void reportReceiveResponse(Response response, EndpointContext endpointContext) {
        reportMessage(MessageType.RECEIVE_RESPONSE, response, endpointContext);
    }

    public void reportReceiveEmpty(EmptyMessage empty, EndpointContext endpointContext) {
        reportMessage(MessageType.RECEIVE_EMPTY, empty, endpointContext);
    }

}
