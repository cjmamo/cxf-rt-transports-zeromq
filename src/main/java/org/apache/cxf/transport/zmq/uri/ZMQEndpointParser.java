/*
 * Copyright 2012 Claude Mamo
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.cxf.transport.zmq.uri;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.transport.zmq.EndpointConfig;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ZMQEndpointParser {
    private static final Logger LOG = LogUtils.getL7dLogger(ZMQEndpointParser.class);

    private ZMQEndpointParser() {
    }

    public static EndpointConfig createEndpointConfig(String uri) throws Exception {
        URI u = new URI(uri);
        String path = u.getSchemeSpecificPart();

        String endpointURI = path.substring(1, path.indexOf("?"));
        Map<String, String> parameters = parseParameters(new URI(path.substring(1, path.length() - 1)));

        LOG.log(Level.FINE, "Creating endpoint uri=[" + uri + "], path=[" + path + "], parameters=[" + parameters + "]");

        EndpointConfig endpointConfig = new EndpointConfig(endpointURI);

        configureProperties(endpointConfig, parameters);

        return endpointConfig;
    }

    private static Map<String, String> parseParameters(URI uri) throws URISyntaxException {
        String query = uri.getQuery();
        if (query == null) {
            String schemeSpecificPart = uri.getSchemeSpecificPart();
            int idx = schemeSpecificPart.lastIndexOf('?');
            if (idx < 0) {
                return Collections.emptyMap();
            } else {
                query = schemeSpecificPart.substring(idx + 1);
            }
        } else {
            query = stripPrefix(query, "?");
        }
        return parseQuery(query);
    }

    private static Map<String, String> parseQuery(String uri) throws URISyntaxException {
        try {
            Map<String, String> rc = new HashMap<String, String>();
            if (uri != null) {
                String[] parameters = uri.split("&");
                for (String parameter : parameters) {
                    int p = parameter.indexOf("=");
                    if (p >= 0) {
                        String name = URLDecoder.decode(parameter.substring(0, p), "UTF-8");
                        String value = URLDecoder.decode(parameter.substring(p + 1), "UTF-8");
                        rc.put(name, value);
                    } else {
                        rc.put(parameter, null);
                    }
                }
            }
            return rc;
        } catch (UnsupportedEncodingException e) {
            URISyntaxException se = new URISyntaxException(e.toString(), "Invalid encoding");
            se.initCause(e);
            throw se;
        }
    }

    private static String stripPrefix(String value, String prefix) {
        if (value.startsWith(prefix)) {
            return value.substring(prefix.length());
        }
        return value;
    }

    private static void configureProperties(EndpointConfig endpointConfig, Map<String, String> parameters) {
        String socketOperation = parameters.get(ZMQURIConstants.SOCKETOPERATION_PARAMETER_NAME);
        String socketType = parameters.get(ZMQURIConstants.SOCKETTYPE_PARAMETER_NAME);
        String filter = parameters.get(ZMQURIConstants.FILTER_PARAMETER_NAME);

        endpointConfig.setSocketOperation(Enum.valueOf(ZMQURIConstants.SocketOperation.class, socketOperation.toUpperCase()));
        endpointConfig.setSocketType(Enum.valueOf(ZMQURIConstants.SocketType.class, socketType.toUpperCase()));
        endpointConfig.setFilter(filter);
    }
}
