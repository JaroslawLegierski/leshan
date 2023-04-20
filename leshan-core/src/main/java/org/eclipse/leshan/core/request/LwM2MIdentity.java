package org.eclipse.leshan.core.request;

public interface LwM2MIdentity {
    String getKeyIdentifier(); // TODO I don't know if we really need this.
    String toString();
    boolean equals(Object obj);
    int hashCode();
}
