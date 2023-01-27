package org.eclipse.leshan.transport.javacoap.request;

import java.security.SecureRandom;

import com.mbed.coap.packet.Opaque;

public class RandomTokenGenerator {

    private final int tokenSize;
    private final SecureRandom random;

    public RandomTokenGenerator(int tokenSize) {
        // TODO check size is between 1 and 8;
        random = new SecureRandom();
        this.tokenSize = tokenSize;
    }

    public Opaque createToken() {
        byte[] token = new byte[tokenSize];
        random.nextBytes(token);
        return Opaque.of(token);
    }
}
