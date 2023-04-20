package org.eclipse.leshan.core.request;

import java.util.Arrays;

import org.eclipse.leshan.core.util.Validate;

public class OscoreIdentity implements  LwM2MIdentity{

    private final byte[] RecipientId;

    public OscoreIdentity(byte[] RecipientId) {
        Validate.notNull(RecipientId);
        this.RecipientId = RecipientId;
    }

    @Override public String getKeyIdentifier() {
        return null;
    }
    public byte[] getRecipientId(){
        return RecipientId;
    }

    @Override
    public String toString() {
       return String.format("Identity [oscore=%s]", RecipientId);
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((RecipientId == null) ? 0 : RecipientId.hashCode());
        return result;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OscoreIdentity that = (OscoreIdentity) o;
        return Arrays.equals(RecipientId, that.RecipientId);
    }
}
