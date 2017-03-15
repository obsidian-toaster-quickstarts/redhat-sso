package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Base64;

/**
 * A simple utilty program that takes the master.json realm configuration file and base64 encodes it for use in the
 * fabric8/2-secret.yml data.sso-demo.json entry
 */
public class EncodeJsonConfig {
    public static void main(String[] args) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = EncodeJsonConfig.class.getResourceAsStream("/master.json");
        byte[] tmp = new byte[1024];
        int length = is.read(tmp);
        while (length > 0) {
            baos.write(tmp, 0, length);
            length = is.read(tmp);
        }
        is.close();
        baos.close();
        tmp = baos.toByteArray();
        Base64.Encoder encoder = Base64.getEncoder();
        String encoded = encoder.encodeToString(tmp);
        System.out.printf("  sso-demo.json: %s\n", encoded);
    }

}
