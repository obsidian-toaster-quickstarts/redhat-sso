/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
