/*******************************************************************************
 * Copyright (c) 2023 Sierra Wireless and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.integration.tests.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to test client/server use case in Dynamic IP environment (E.g. NAT)
 */
public class SimpleNat {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleNat.class);

    private static final int BUFFER_SIZE = 2048;

    private final InetSocketAddress natAddress;
    private final InetSocketAddress serverAddress;
    private InetSocketAddress clientAddress;

    private DatagramChannel clientToNatChannel;
    private DatagramChannel NatToServerChannel;
    private Selector selector;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    private volatile boolean running;
    private volatile boolean reassignAddress = false;

    public SimpleNat(InetSocketAddress natAddress, InetSocketAddress serverAddress) {
        this.natAddress = natAddress;
        this.serverAddress = serverAddress;
    }

    public InetSocketAddress getAddress() {
        try {
            return (InetSocketAddress) clientToNatChannel.getLocalAddress();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void start() {
        executor.execute(() -> {

            try {
                selector = Selector.open();

                clientToNatChannel = DatagramChannel.open();
                clientToNatChannel.bind(natAddress);
                clientToNatChannel.configureBlocking(false);
                clientToNatChannel.register(selector, SelectionKey.OP_READ);

                NatToServerChannel = DatagramChannel.open();
                NatToServerChannel.configureBlocking(false);
                NatToServerChannel.register(selector, SelectionKey.OP_READ);

                running = true;
                while (running) {
                    selector.select(100);
                    // Handle events if any
                    Set<SelectionKey> selecteds = selector.selectedKeys();
                    for (SelectionKey selected : selecteds) {
                        if (selected.channel() == clientToNatChannel) {
                            handleClientPackets();

                        } else if (selected.channel() == NatToServerChannel) {
                            handlerServerPackets();

                        } else {
                            LOGGER.error(String.format("Unexpected selected channel "));
                        }
                    }
                    // reassign address if needed
                    if (reassignAddress) {
                        DatagramChannel previousChannel = NatToServerChannel;

                        try {
                            NatToServerChannel = DatagramChannel.open();
                            NatToServerChannel.configureBlocking(false);
                            NatToServerChannel.register(selector, SelectionKey.OP_READ);
                            selector.wakeup();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            reassignAddress = false;
                            try {
                                previousChannel.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        try {
            running = false;
            clientToNatChannel.close();
            NatToServerChannel.close();
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeAddress() {
        reassignAddress = true;
    }

    private void handleClientPackets() throws IOException {
        LOGGER.debug("Handle Client Packets");
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        InetSocketAddress sourceAddress = (InetSocketAddress) clientToNatChannel.receive(buffer);
        if (sourceAddress == null) {
            return;
        } else {
            // maybe better to store this on connect event ?
            clientAddress = sourceAddress;
        }

        buffer.flip();
        LOGGER.info("send {} bytes to server", buffer.remaining(), serverAddress);
        NatToServerChannel.send(buffer, serverAddress);
        buffer.clear();
    }

    private void handlerServerPackets() throws IOException {
        LOGGER.debug("Handle Server Packets");
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        InetSocketAddress sourceAddress;

        sourceAddress = (InetSocketAddress) NatToServerChannel.receive(buffer);
        if (sourceAddress == null) {
            return;
        }
        if (!sourceAddress.equals(serverAddress)) {
            LOGGER.error(String.format("We should only receive data from server %s", serverAddress));
            throw new IllegalStateException(String.format("We should only receive data from server %s", serverAddress));
        }
        if (clientAddress == null) {
            LOGGER.error("Client should send data first before sever send data");
            throw new IllegalStateException("Client should send data first before sever send data");
        }
        buffer.flip();
        LOGGER.info("send {} bytes to client", buffer.remaining(), clientAddress);
        clientToNatChannel.send(buffer, clientAddress);

        buffer.clear();
    }

}