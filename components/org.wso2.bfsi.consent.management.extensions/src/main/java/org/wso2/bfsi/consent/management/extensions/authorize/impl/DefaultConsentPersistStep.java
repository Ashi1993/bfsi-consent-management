/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.consent.management.extensions.authorize.impl;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.consent.management.dao.models.ConsentResource;
import org.wso2.bfsi.consent.management.extensions.authorize.ConsentPersistStep;
import org.wso2.bfsi.consent.management.extensions.authorize.model.ConsentData;
import org.wso2.bfsi.consent.management.extensions.authorize.model.ConsentPersistData;
import org.wso2.bfsi.consent.management.extensions.common.ConsentException;
import org.wso2.bfsi.consent.management.extensions.common.ResponseStatus;
import org.wso2.bfsi.consent.management.extensions.internal.ConsentExtensionsDataHolder;

import java.util.ArrayList;

/**
 * Consent persist step default implementation.
 */
public class DefaultConsentPersistStep implements ConsentPersistStep {

    private static final Log log = LogFactory.getLog(DefaultConsentPersistStep.class);
    @Override
    public void execute(ConsentPersistData consentPersistData) throws ConsentException {

        try {
            ConsentData consentData = consentPersistData.getConsentData();
            ConsentResource consentResource;

            if (consentData.getConsentId() == null && consentData.getConsentResource() == null) {
                log.error("Consent ID not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Consent ID not available in consent data");
            }

            if (consentData.getConsentResource() == null) {
                consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentData.getConsentId(), false);
            } else {
                consentResource = consentData.getConsentResource();
            }

            if (consentData.getAuthResource() == null) {
                log.error("Auth resource not available in consent data");
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                        "Auth resource not available in consent data");
            }

            consentPersist(consentPersistData, consentResource);

        } catch (ConsentManagementException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                    "Exception occured while persisting consent");
        }
    }

    public void consentPersist(ConsentPersistData consentPersistData, ConsentResource consentResource)
            throws ConsentManagementException {

        ConsentData consentData = consentPersistData.getConsentData();
        boolean isApproved = consentPersistData.getApproval();
        JSONObject payload = consentPersistData.getPayload();

        if (payload.get("accountIds") == null || !(payload.get("accountIds") instanceof JSONArray)) {
            log.error("Account IDs not available in persist request");
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    "Account IDs not available in persist request");
        }

        JSONArray accountIds = (JSONArray) payload.get("accountIds");
        ArrayList<String> accountIdsString = new ArrayList<>();
        for (Object account : accountIds) {
            if (!(account instanceof String)) {
                log.error("Account IDs format error in persist request");
                throw new ConsentException(ResponseStatus.BAD_REQUEST,
                        "Account IDs format error in persist request");
            }
            accountIdsString.add((String) account);
        }
        String consentStatus;
        String authStatus;

        if (isApproved) {
            consentStatus = "Authorized";
            authStatus = "Authorized";
        } else {
            consentStatus = "Rejected";
            authStatus = "Rejected";
        }

        ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                .bindUserAccountsToConsent(consentResource, consentData.getUserId(),
                        consentData.getAuthResource().getAuthorizationID(), accountIdsString, authStatus,
                        consentStatus);

    }
}
