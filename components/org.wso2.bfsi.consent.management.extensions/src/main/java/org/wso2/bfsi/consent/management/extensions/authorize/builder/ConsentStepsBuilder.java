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

package org.wso2.bfsi.consent.management.extensions.authorize.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.extensions.authorize.ConsentPersistStep;
import org.wso2.bfsi.consent.management.extensions.authorize.ConsentRetrievalStep;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionUtils;
import org.wso2.bfsi.consent.management.extensions.internal.ConsentExtensionsDataHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder class for consent steps.
 */
public class ConsentStepsBuilder {

    private static final Log log = LogFactory.getLog(ConsentStepsBuilder.class);
    private List<ConsentPersistStep> consentPersistSteps = null;
    private List<ConsentRetrievalStep> consentRetrievalSteps = null;
    private static final String RETRIEVE = "Retrieve";
    private static final String PERSIST = "Persist";

    public void build() {

        try {
            Map<String, Map<Integer, String>> stepsConfig = ConsentExtensionsDataHolder.getInstance()
                    .getConfigurationService().getConsentAuthorizeSteps();
            Map<Integer, String> persistIntegerStringMap = stepsConfig.get(PERSIST);
            if (persistIntegerStringMap != null) {
                consentPersistSteps = persistIntegerStringMap.keySet().stream()
                        .map(integer -> (ConsentPersistStep) ConsentExtensionUtils
                                .getClassInstanceFromFQN(persistIntegerStringMap.get(integer)))
                        .collect(Collectors.toList());
                log.debug("Persistence steps loaded successfully");
            }
            Map<Integer, String> retrieveIntegerStringMap = stepsConfig.get(RETRIEVE);
            if (retrieveIntegerStringMap != null) {
                consentRetrievalSteps = retrieveIntegerStringMap.keySet().stream()
                        .map(integer -> (ConsentRetrievalStep) ConsentExtensionUtils
                                .getClassInstanceFromFQN(retrieveIntegerStringMap.get(integer)))
                        .collect(Collectors.toList());
                log.debug("Retrieval steps loaded successfully");
            }
        } catch (ConsentManagementRuntimeException e) {
            log.error(String.format("Authorize steps not loaded successfully. Please verify configurations. %s",
                    e.getMessage().replaceAll("\n\r", "")));
        }
    }

    public List<ConsentPersistStep> getConsentPersistSteps() {
        return consentPersistSteps;
    }

    public List<ConsentRetrievalStep> getConsentRetrievalSteps() {
        return consentRetrievalSteps;
    }
}
