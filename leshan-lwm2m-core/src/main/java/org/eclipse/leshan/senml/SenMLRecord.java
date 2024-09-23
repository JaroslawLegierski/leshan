/*******************************************************************************
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
 *     Boya Zhang - initial API and implementation
 *******************************************************************************/

package org.eclipse.leshan.senml;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.leshan.core.util.Hex;

/**
 * The class representing the SenML Record.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8428#section-4">rfc8428 - Sensor Measurement Lists (SenML)</a>
 */
public class SenMLRecord {

    public enum Type {
        STRING, NUMBER, BOOLEAN, OPAQUE, OBJLNK
    }

    private String baseName = null;
    private BigDecimal baseTime;

    private String name;
    private BigDecimal time;

    private Number numberValue;
    private Boolean booleanValue;
    private String objectLinkValue;
    private String stringValue;
    private byte[] opaqueValue;

    public Type getType() {
        if (booleanValue != null) {
            return Type.BOOLEAN;
        }
        if (numberValue != null) {
            return Type.NUMBER;
        }
        if (objectLinkValue != null) {
            return Type.OBJLNK;
        }
        if (stringValue != null) {
            return Type.STRING;
        }
        if (opaqueValue != null) {
            return Type.OPAQUE;
        }
        return null;
    }

    public void setTime(BigDecimal time) {
        this.time = time;
    }

    public BigDecimal getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Number getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Number numberValue) {
        this.numberValue = numberValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public String getObjectLinkValue() {
        return objectLinkValue;
    }

    public void setObjectLinkValue(String objectLinkValue) {
        this.objectLinkValue = objectLinkValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setOpaqueValue(byte[] opaqueValue) {
        this.opaqueValue = opaqueValue;
    }

    public byte[] getOpaqueValue() {
        return opaqueValue;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public BigDecimal getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(BigDecimal baseTime) {
        this.baseTime = baseTime;
    }

    public Object getResourceValue() {
        if (booleanValue != null) {
            return booleanValue;
        }
        if (numberValue != null) {
            return numberValue;
        }
        if (objectLinkValue != null) {
            return objectLinkValue;
        }
        if (stringValue != null) {
            return stringValue;
        }
        if (opaqueValue != null) {
            return opaqueValue;
        }
        return null;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SenMLRecord))
            return false;
        SenMLRecord that = (SenMLRecord) o;

        boolean comparablyEqualTime = (time == null && that.time == null)
                || (time != null && that.time != null && time.compareTo(that.time) == 0);

        boolean comparablyEqualBaseTime = (baseTime == null && that.baseTime == null)
                || (baseTime != null && that.baseTime != null && baseTime.compareTo(that.baseTime) == 0);

        return Objects.equals(baseName, that.baseName) && comparablyEqualBaseTime && Objects.equals(name, that.name)
                && comparablyEqualTime && Objects.equals(numberValue, that.numberValue)
                && Objects.equals(booleanValue, that.booleanValue)
                && Objects.equals(objectLinkValue, that.objectLinkValue)
                && Objects.equals(stringValue, that.stringValue) && Arrays.equals(opaqueValue, that.opaqueValue);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(baseName, baseTime != null ? baseTime.stripTrailingZeros() : null, name,
                time != null ? time.stripTrailingZeros() : null, numberValue, booleanValue, objectLinkValue,
                stringValue, Arrays.hashCode(opaqueValue));
    }

    @Override
    public String toString() {
        return String.format(
                "SenMLRecord [baseName=%s, baseTime=%s, name=%s, time=%s, numberValue=%s, booleanValue=%s, objectLinkValue=%s, stringValue=%s, opaque=%s]",
                baseName, baseTime, name, time, numberValue, booleanValue, objectLinkValue, stringValue,
                opaqueValue != null ? Hex.encodeHexString(opaqueValue) : "null");
    }
}