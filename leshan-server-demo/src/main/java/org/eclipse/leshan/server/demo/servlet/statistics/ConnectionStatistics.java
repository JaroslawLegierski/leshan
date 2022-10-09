package org.eclipse.leshan.server.demo.servlet.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.leshan.server.registration.RegistrationService;

public class ConnectionStatistics {
    private final Map<String, EndpointData> tokenEndpointMap = new ConcurrentHashMap<>();
    private final List<Map<String, String>> cachedStatistics = new ArrayList<>();
    private final StatisticsDataProxy statisticsDataProxy;
    private final StatisticsDataFacade statisticsDataFacade;
    private final Timer periodicFlushTimer;

    public ConnectionStatistics(StatisticsDataProxy statisticsDataProxy, long periodBetweenFlushMs,
            RegistrationService registry) {
        this.statisticsDataProxy = statisticsDataProxy;

        periodicFlushTimer = new Timer();
        periodicFlushTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                flushData();
                clearOldTokenEndpointMapRecords();
            }
        }, periodBetweenFlushMs, periodBetweenFlushMs);

        statisticsDataFacade = new StatisticsDataFacade(registry);
    }

    private synchronized void putDataInCache(Map<String, String> messageData) {
        cachedStatistics.add(messageData);
    }

    private synchronized void flushData() {
        if (cachedStatistics.isEmpty()) {
            return;
        }
        statisticsDataProxy.collectDataFromCache(cachedStatistics);
        cachedStatistics.clear();
    }

    private void clearOldTokenEndpointMapRecords() {
        //If records sit in cache for more than an hour (adjustable), it's most likely a result
        //of registration error, so they have to be deleted
        long timeThreshold = 3_600_000;
        Set<String> tokens = tokenEndpointMap.entrySet().stream()
                .filter(entry -> System.currentTimeMillis() - entry.getValue().getCreatedAt() > timeThreshold)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        tokens.forEach(tokenEndpointMap::remove);
    }

    private void reportMessage(MessageType type, Message message, EndpointContext endpointContext) {
        Map<String, String> messageData = statisticsDataFacade.reportMessage(type, message, endpointContext,
                tokenEndpointMap);
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

    enum MessageType {SEND_REQUEST, SEND_RESPONSE, SEND_EMPTY, RECEIVE_REQUEST, RECEIVE_RESPONSE, RECEIVE_EMPTY}

    static class EndpointData {
        private final String endpoint;
        private final long createdAt;

        public EndpointData(String endpoint, long createdAt) {
            this.endpoint = endpoint;
            this.createdAt = createdAt;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}
