/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.authentication.endpoint;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.bfsi.authentication.endpoint.util.AuthenticationUtils;
import org.wso2.bfsi.authentication.endpoint.util.Constants;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigParser;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.consent.management.extensions.authservlet.OBAuthServletInterface;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The servlet responsible for the confirm page in auth web flow.
 */
public class OBConsentConfirmServlet extends HttpServlet {

    static OBAuthServletInterface obAuthServletTK;
    private static final long serialVersionUID = 6106269597832678046L;
    private static Logger log = LoggerFactory.getLogger(OBConsentConfirmServlet.class);

    @SuppressFBWarnings("COOKIE_USAGE")
    // Suppressed content - browserCookies.put(cookie.getName(), cookie.getValue())
    // Suppression reason - False Positive : The cookie values are only read and here. No sensitive info is added to
    //                      the cookie in this step.
    // Suppressed warning count - 1
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        setAuthExtension();

        HttpSession session = request.getSession();
        Map<String, String> metadata = new HashMap<>();
        Map<String, String> browserCookies = new HashMap<>();
        JSONObject consentData = new JSONObject();

        //retrieve commonAuthId to be stored for co-relation of consent Id and access token issued
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            browserCookies.put(cookie.getName(), cookie.getValue());
        }
        consentData.put("cookies", browserCookies);

        // Add authorisationId if available
        String authorisationId = request.getParameter("authorisationId");
        if (StringUtils.isNotEmpty(authorisationId)) {
            metadata.put("authorisationId", authorisationId);
        }

        consentData.put("type", request.getParameter("type"));
        consentData.put("approval", request.getParameter("consent"));
        consentData.put("userId", session.getAttribute("username"));

        // add TK data
        if (obAuthServletTK != null) {
            Map<String, String> updatedMetadata = obAuthServletTK.updateConsentMetaData(request);
            if (updatedMetadata != null) {
                metadata.putAll(updatedMetadata);
            }

            Map<String, Object> updatedConsentData = obAuthServletTK.updateConsentData(request);
            if (updatedConsentData != null) {
                updatedConsentData.forEach(consentData::put);
            }
        }

        consentData.put("metadata", metadata);

        String redirectURL = persistConsentData(
                consentData, request.getParameter("sessionDataKeyConsent"), getServletContext());

        // Invoke authorize flow
        if (redirectURL != null) {
            response.sendRedirect(redirectURL);

        } else {
            session.invalidate();
            response.sendRedirect("retry.do?status=Error&statusMsg=Error while persisting consent");
        }

    }

    @Generated(message = "Contains the tested code of HTTPClient")
    String persistConsentData(JSONObject consentData, String sessionDataKey, ServletContext servletContext) {

        String persistenceBaseURL = servletContext.getInitParameter("persistenceBaseURL");
        String persistenceUrl = persistenceBaseURL + "/" + sessionDataKey;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPatch dataRequest = new HttpPatch(persistenceUrl);
            dataRequest.addHeader("accept", "application/json");
            dataRequest.addHeader("Authorization", "Basic " + OBConsentServlet.getConsentApiCredentials());
            StringEntity body = new StringEntity(consentData.toString(), ContentType.APPLICATION_JSON);
            dataRequest.setEntity(body);
            HttpResponse dataResponse = client.execute(dataRequest);

            if (dataResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_MOVED_TEMP &&
                    dataResponse.getLastHeader("Location") != null) {
                return dataResponse.getLastHeader("Location").getValue();
            } else {
                String retrievalResponse = IOUtils.toString(dataResponse.getEntity().getContent(),
                        String.valueOf(StandardCharsets.UTF_8));
                JSONObject data = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(retrievalResponse);
                String errorResponse = AuthenticationUtils.getErrorResponseForRedirectURL(data);
                if (data.containsKey(Constants.REDIRECT_URI) && StringUtils.isNotEmpty(errorResponse)) {
                    URI errorURI = new URI(data.get(Constants.REDIRECT_URI).toString().concat(errorResponse));
                    return errorURI.toString();
                } else {
                    return null;
                }
            }
        } catch (IOException | ParseException | URISyntaxException e) {
            log.error("Exception while calling persistence endpoint", e);
            return null;
        }
    }
    /**
     * Retrieve the config.
     */
     void setAuthExtension() {
         try {
             obAuthServletTK = (OBAuthServletInterface) Class.forName(ConsentManagementConfigParser.getInstance()
                     .getAuthServletExtension()).getDeclaredConstructor().newInstance();
         } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
             log.error("Webapp extension not found", e);
         }
    }

}
