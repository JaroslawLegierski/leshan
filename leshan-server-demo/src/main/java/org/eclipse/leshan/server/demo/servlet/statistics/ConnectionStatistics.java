package org.eclipse.leshan.server.demo.servlet.statistics;

import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessageListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConnectionStatistics {
    private enum MessageType {SEND_REQUEST, SEND_RESPONSE, RECEIVE_REQUEST, RECEIVE_RESPONSE}

    private final Map<String, EndpointData> tokenEndpointMap = new ConcurrentHashMap<>();
    private final List<Map<String, String>> cachedStatistics = new ArrayList<>();
    private final StatisticsDataProxy statisticsDataProxy;
    private final Timer periodicFlushTimer;

    public ConnectionStatistics(StatisticsDataProxy statisticsDataProxy, long periodBetweenFlushMs) {
        this.statisticsDataProxy = statisticsDataProxy;

        periodicFlushTimer = new Timer();
        periodicFlushTimer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                flushData();
                clearOldTokenEndpointMapRecords();
            }
        }, periodBetweenFlushMs, periodBetweenFlushMs);
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
                .filter(entry -> System.currentTimeMillis() - entry.getValue().createdAt > timeThreshold)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        tokens.forEach(tokenEndpointMap::remove);
    }

    private void reportMessage(MessageType type, Message message, CoapMessageListener coapMessageListener) {
        Map<String, String> messageData = new LinkedHashMap<>();
        messageData.put("messageTime", LocalDateTime.now(ZoneOffset.UTC).toString());
        messageData.put("type", type.toString());
        messageData.put("mid", String.valueOf(message.getMID()));
        messageData.put("msg", message.getType().toString());
        messageData.put("rejected", String.valueOf(message.isRejected()));
        messageData.put("acknowledged", String.valueOf(message.acknowledge()));
        messageData.put("timeout", String.valueOf(message.isTimedOut()));
        messageData.put("duplicated", String.valueOf(message.isDuplicate()));
        messageData.put("token", message.getTokenString());
        messageData.put("timestamp", String.valueOf(message.getNanoTimestamp()));

        if (coapMessageListener != null) {
            messageData.put("endpoint", coapMessageListener.getEndpoint());
        } else {
            //In this case we know it's registration request/response.
            Optional<String> endpointOptional = message.getOptions().getUriQuery().stream()
                    .filter(item -> item.startsWith("ep=")).findFirst();
            if (endpointOptional.isPresent()) {
                //Only requests carry endpoint name, so we cache it and wait for response report
                String endpoint = endpointOptional.get().substring(3);
                messageData.put("endpoint", endpoint);
                tokenEndpointMap.put(message.getTokenString(), new EndpointData(endpoint, System.currentTimeMillis()));
            } else {
                //In case of response we retrieve endpoint name from cache and delete the cached record
                messageData.put("endpoint", tokenEndpointMap.get(message.getTokenString()).endpoint);
                tokenEndpointMap.remove(message.getTokenString());
            }
        }

        putDataInCache(messageData);
    }

    public void reportSendRequest(Request request, CoapMessageListener coapMessageListener) {
        reportMessage(MessageType.SEND_REQUEST, request, coapMessageListener);
    }

    public void reportSendResponse(Response response, CoapMessageListener coapMessageListener) {
        reportMessage(MessageType.SEND_RESPONSE, response, coapMessageListener);
    }

    public void reportReceiveRequest(Request request, CoapMessageListener coapMessageListener) {
        reportMessage(MessageType.RECEIVE_REQUEST, request, coapMessageListener);
    }

    public void reportReceiveResponse(Response response, CoapMessageListener coapMessageListener) {
        reportMessage(MessageType.RECEIVE_RESPONSE, response, coapMessageListener);
    }

    private static class EndpointData {
        private final String endpoint;
        private final long createdAt;

        public EndpointData(String endpoint, long createdAt) {
            this.endpoint = endpoint;
            this.createdAt = createdAt;
        }
    }
}
