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
package client;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * Filter to dump out client request information
 */
public class LoggingFilter implements ClientRequestFilter, WriterInterceptor {
    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        String method = clientRequestContext.getMethod();
        URI uri = clientRequestContext.getUri();
        MultivaluedMap<String, Object> headers = clientRequestContext.getHeaders();
        System.out.printf("Sending %s to: %s\n", method, uri);
        System.out.printf("Headers:\n");
        for(String key : headers.keySet()) {
            System.out.printf("  %s: %s\n", key, headers.getFirst(key));
        }
        if(clientRequestContext.hasEntity()) {
            final OutputStream stream = new LoggingStream(clientRequestContext.getEntityStream());
            clientRequestContext.setEntityStream(stream);
            clientRequestContext.setProperty("client.LoggingStream", stream);
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        final LoggingStream stream = (LoggingStream) context.getProperty("client.LoggingStream");
        context.proceed();
        if (stream != null) {
            System.out.printf("Body: %s\n", stream.getString(StandardCharsets.UTF_8));
        }
        System.out.printf("-----------\n");
    }

    private class LoggingStream extends FilterOutputStream {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LoggingStream(OutputStream out) {
            super(out);
        }

        /**
         * Get string represention of entity
         * @param charset to use for new String(byte[], ..., charset)
         * @return String for entity
         */
        String getString(Charset charset) {
            final byte[] entity = baos.toByteArray();
            return new String(entity, 0, entity.length, charset);
        }

        /**
         * Write entity to both byte array and request output stream
         * @param i
         * @throws IOException
         */
        @Override
        public void write(final int i) throws IOException {
            baos.write(i);
            out.write(i);
        }
    }
}
