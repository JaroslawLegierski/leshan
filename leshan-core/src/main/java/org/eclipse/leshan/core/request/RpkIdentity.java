package org.eclipse.leshan.core.request;

import java.security.PublicKey;
import java.util.Objects;

import org.eclipse.leshan.core.util.Validate;

public class RpkIdentity implements LwM2MIdentity{

    private final PublicKey rawPublicKey;

    public RpkIdentity(PublicKey rawPublicKey) {
        Validate.notNull(rawPublicKey);
        this.rawPublicKey = rawPublicKey;
    }


    @Override public String getKeyIdentifier() {
        return null;
    }

    public PublicKey getPublicKey ()
    {
        return rawPublicKey;
    }

    @Override
    public String toString() {
        return String.format("Identity rpk=%s]", rawPublicKey);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rawPublicKey == null) ? 0 : rawPublicKey.hashCode());
        return result;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RpkIdentity that = (RpkIdentity) o;
        return Objects.equals(rawPublicKey, that.rawPublicKey);
    }
}
