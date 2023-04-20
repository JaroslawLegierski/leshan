package org.eclipse.leshan.core.request;

import java.util.Objects;

import org.eclipse.leshan.core.util.Validate;

public class PskIdentity implements LwM2MIdentity{

    private final String pskIdentity;

    public PskIdentity(String pskIdentity) {
        Validate.notNull(pskIdentity);
        this.pskIdentity = pskIdentity;
        }

    @Override public String getKeyIdentifier()
    {
        return null;
    }

    public String getpskIdentity() {

        return pskIdentity;
    }

    @Override
    public String toString() {
        return String.format("Identity [psk=%s]", pskIdentity);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pskIdentity == null) ? 0 : pskIdentity.hashCode());
        return result;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PskIdentity that = (PskIdentity) o;
        return Objects.equals(pskIdentity, that.pskIdentity);
    }
}
