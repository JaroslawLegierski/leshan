package org.eclipse.leshan.server.demo.servlet.statistics;

import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

import java.util.*;

public class ConnectionStatistics {
    private enum MessageType {SEND_REQUEST, SEND_RESPONSE, RECEIVE_REQUEST, RECEIVE_RESPONSE}

    private final List<Map<String, String>> cachedStatistics = new ArrayList<>();
    private final StatisticsDataProxy statisticsDataProxy;
    private final Timer periodicFlushTimer;

    public ConnectionStatistics(StatisticsDataProxy statisticsDataProxy, long periodBetweenFlushMs) {
        this.statisticsDataProxy = statisticsDataProxy;

        periodicFlushTimer = new Timer();
        periodicFlushTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                flushData();
            }
        }, periodBetweenFlushMs, periodBetweenFlushMs);
    }

    private synchronized void putDataInCache(Map<String, String> messageData) {
        cachedStatistics.add(messageData);
    }

    private synchronized void flushData() {
        if(cachedStatistics.isEmpty()) {
            return;
        }
        statisticsDataProxy.collectDataFromCache(cachedStatistics);
        cachedStatistics.clear();
    }

    private void reportMessage(MessageType type, Message message) {
        Map<String, String> messageData = new LinkedHashMap<>();
        messageData.put("TYPE", type.toString());
        messageData.put("MID", String.valueOf(message.getMID()));
        messageData.put("MSG", message.getType().toString());
        messageData.put("REJECTED", String.valueOf(message.isRejected()));
        messageData.put("ACKNOWLEDGED", String.valueOf(message.acknowledge()));
        messageData.put("TIMEOUT", String.valueOf(message.isTimedOut()));
        messageData.put("DUPLICATED", String.valueOf(message.isDuplicate()));
        messageData.put("TOKEN", message.getTokenString());
        messageData.put("TIMESTAMP", String.valueOf(message.getNanoTimestamp()));
        messageData.put("PAYLOAD", message.getPayloadString());
        putDataInCache(messageData);
    }

    public void reportSendRequest(Request request) {
        reportMessage(MessageType.SEND_REQUEST, request);
    }

    public void reportSendResponse(Response response) {
        reportMessage(MessageType.SEND_RESPONSE, response);
    }

    public void reportReceiveRequest(Request request) {
        reportMessage(MessageType.RECEIVE_REQUEST, request);
    }

    public void reportReceiveResponse(Response response) {
        reportMessage(MessageType.RECEIVE_RESPONSE, response);
    }
}
