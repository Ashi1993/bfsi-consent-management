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

package org.wso2.bfsi.identity.extensions.auth.extensions.request.validator;

import org.testng.annotations.Test;
import org.wso2.bfsi.identity.extensions.auth.extensions.request.validator.models.BFSIRequestObject;
import org.wso2.bfsi.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.openidconnect.model.RequestObject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test for request object validator.
 */
public class RequestObjectValidatorTest {

    @Test(dataProvider = "dp-checkValidRequestObject", dataProviderClass = ReqObjectTestDataProvider.class)
    public void checkValidRequestObject(RequestObject requestObject,
                                        OAuth2Parameters oAuth2Parameters) throws Exception {

        // Mock
        BFSIRequestObjectValidator bfsiDefaultRequestObjectValidator = mock(BFSIRequestObjectValidator.class);
        when(bfsiDefaultRequestObjectValidator.validateBFSIConstraints(any(), anyMap()))
                .thenReturn(new ValidationResponse(true));

        BFSIRequestObjectValidationExtension uut = spy(new BFSIRequestObjectValidationExtension());
        doReturn(true).when(uut).isRegulatory(any());
        doReturn(true).when(uut).validateIAMConstraints(any(), any());
        doReturn("accounts payments").when(uut).getAllowedScopes(any());

        // Assign
        BFSIRequestObjectValidationExtension.bfsiDefaultRequestObjectValidator = bfsiDefaultRequestObjectValidator;

        // Act
        boolean result = uut.validateRequestObject(requestObject, oAuth2Parameters);

        // Assert
        assertTrue("Valid request object should pass", result);
    }


    @Test(dataProvider = "dp-checkIncorrectRequestObject", dataProviderClass = ReqObjectTestDataProvider.class,
            expectedExceptions = RequestObjectException.class)
    public void checkIncorrectRequestObject(RequestObject requestObject,
                                          OAuth2Parameters oAuth2Parameters) throws Exception {

        // Mock
        BFSIRequestObjectValidationExtension uut = spy(new BFSIRequestObjectValidationExtension());
        doReturn(true).when(uut).validateIAMConstraints(any(), any());
        doReturn(true).when(uut).isRegulatory(any());

        // Assign
        BFSIRequestObjectValidationExtension.bfsiDefaultRequestObjectValidator = new BFSIRequestObjectValidator();

        // Act
        boolean result = uut.validateRequestObject(requestObject, oAuth2Parameters);

        // Assert
        assertTrue("InValid request object should throw exception", result);
    }


    @Test(dataProvider = "dp-checkInValidRequestObject", dataProviderClass = ReqObjectTestDataProvider.class,
            expectedExceptions = RequestObjectException.class)
    public void checkInValidRequestObject(RequestObject requestObject,
                                          OAuth2Parameters oAuth2Parameters) throws Exception {

        // Mock
        BFSIRequestObjectValidator bfsiRequestObjectValidator = mock(BFSIRequestObjectValidator.class);
        when(bfsiRequestObjectValidator.validateBFSIConstraints(any(), anyMap()))
                .thenReturn(new ValidationResponse(true));

        BFSIRequestObjectValidationExtension uut = spy(new BFSIRequestObjectValidationExtension());
        doReturn(true).when(uut).isRegulatory(any());
        doReturn(true).when(uut).validateIAMConstraints(any(), any());
        doReturn("accounts payments").when(uut).getAllowedScopes(any());

        // Assign
        BFSIRequestObjectValidationExtension.bfsiDefaultRequestObjectValidator = bfsiRequestObjectValidator;

        // Act
        boolean result = uut.validateRequestObject(requestObject, oAuth2Parameters);

        // Assert
        assertTrue("InValid request object should throw exception", result);
    }

    @Test(dataProvider = "dp-checkValidRequestObject", dataProviderClass = ReqObjectTestDataProvider.class)
    public void checkChildClassCreation(RequestObject requestObject,
                                        OAuth2Parameters oAuth2Parameters) throws RequestObjectException {

        class UKRequestObject extends BFSIRequestObject {
            public UKRequestObject(BFSIRequestObject childObject) {
                super(childObject);
            }
        }

        BFSIRequestObject bfsiRequestObject = new BFSIRequestObject(requestObject);
        UKRequestObject ukRequestObject = new UKRequestObject(bfsiRequestObject);

        // Assert
        assertEquals("Inheritance should be preserved in toolkit child classes",
                8, ukRequestObject.getClaimsSet().getClaims().size());

        // Assert
        assertEquals(3, ukRequestObject.getRequestedClaims().size());

        // Assert
        assertEquals("code id_token", ukRequestObject.getClaim("response_type"));

        // Assert
        assertEquals("code id_token", ukRequestObject.getClaimValue("response_type"));
    }
}
