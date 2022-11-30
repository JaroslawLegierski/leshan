package org.eclipse.leshan.core.util.base64;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DefaultBase64EncoderTest {
    @Test
    public void test_encode_url_safe_and_without_padding() {
        // given
        DefaultBase64Encoder encoder = new DefaultBase64Encoder(true, true);
        byte[] input = new byte[128];
        for (int i = -128; i < 0; i++) {
            input[i + 128] = (byte) i;
        }
        String encoded = "gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp-goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2-v8DBws"
                + "PExcbHyMnKy8zNzs_Q0dLT1NXW19jZ2tvc3d7f4OHi4-Tl5ufo6err7O3u7_Dx8vP09fb3-Pn6-_z9_v8";

        // when
        String output = encoder.encode(input);

        // then
        assertEquals(encoded, output);
    }

    @Test
    public void test_encode_url_safe_and_with_padding() {
        // given
        DefaultBase64Encoder encoder = new DefaultBase64Encoder(true, false);
        byte[] input = new byte[128];
        for (int i = -128; i < 0; i++) {
            input[i + 128] = (byte) i;
        }
        String encoded = "gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp-goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2-v8DBwsP"
                + "ExcbHyMnKy8zNzs_Q0dLT1NXW19jZ2tvc3d7f4OHi4-Tl5ufo6err7O3u7_Dx8vP09fb3-Pn6-_z9_v8=";

        // when
        String output = encoder.encode(input);

        // then
        assertEquals(encoded, output);
    }

    @Test
    public void test_encode_url_unsafe_and_without_padding() {
        // given
        DefaultBase64Encoder encoder = new DefaultBase64Encoder(false, true);
        byte[] input = new byte[128];
        for (int i = -128; i < 0; i++) {
            input[i + 128] = (byte) i;
        }
        String encoded = "gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsP"
                + "ExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8";

        // when
        String output = encoder.encode(input);

        // then
        assertEquals(encoded, output);
    }

    @Test
    public void test_encode_url_unsafe_and_with_padding() {
        // given
        DefaultBase64Encoder encoder = new DefaultBase64Encoder(false, false);
        byte[] input = new byte[128];
        for (int i = -128; i < 0; i++) {
            input[i + 128] = (byte) i;
        }
        String encoded = "gIGCg4SFhoeIiYqLjI2Oj5CRkpOUlZaXmJmam5ydnp+goaKjpKWmp6ipqqusra6vsLGys7S1tre4ubq7vL2+v8DBwsP"
                + "ExcbHyMnKy8zNzs/Q0dLT1NXW19jZ2tvc3d7f4OHi4+Tl5ufo6err7O3u7/Dx8vP09fb3+Pn6+/z9/v8=";

        // when
        String output = encoder.encode(input);

        // then
        assertEquals(encoded, output);
    }

}
