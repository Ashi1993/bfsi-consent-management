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

package org.wso2.bfsi.identity.extensions.auth.extensions.response.handler;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.bfsi.identity.extensions.auth.extensions.response.handler.impl.BFSIDefaultResponseTypeHandlerImpl;
import org.wso2.bfsi.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.identity.openidconnect.model.RequestedClaim;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * test for response type handler.
 */
public class ResponseTypeHandlerTest {

    @Test
    public void checkValidHybridResponseTypeHandling() throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd"});

        BFSIHybridResponseTypeHandlerExtension uut = spy(new BFSIHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doReturn(true).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSIHybridResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
        assertEquals(999L, argument.getValue().getRefreshTokenvalidityPeriod());
        assertEquals(2, argument.getValue().getApprovedScope().length);
    }

    @Test
    public void checkValidHybridResponseTypeHandlingForNonRegulatory()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd"});

        BFSIHybridResponseTypeHandlerExtension uut = spy(new BFSIHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doReturn(false).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSIHybridResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
        assertEquals(0L, argument.getValue().getRefreshTokenvalidityPeriod());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkValidHybridResponseTypeHandlingForException()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd"});

        BFSIHybridResponseTypeHandlerExtension uut = spy(new BFSIHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doThrow(RequestObjectException.class).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSIHybridResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkValidHybridResponseTypeHandlingForIdentityOAuth2Exception()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(999L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(null);

        BFSIHybridResponseTypeHandlerExtension uut = spy(new BFSIHybridResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doReturn(true).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSIHybridResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test
    public void checkValidCodeResponseTypeHandling() throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd", "rr"});

        BFSICodeResponseTypeHandlerExtension uut = spy(new BFSICodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doReturn(true).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSICodeResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

       // Assert
        verify(uut).issue(argument.capture());
        assertEquals(109L, argument.getValue().getRefreshTokenvalidityPeriod());
        assertEquals(3, argument.getValue().getApprovedScope().length);
    }

    @Test
    public void checkValidCodeResponseTypeHandlingForNonRegulatory()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd", "rr"});

        BFSICodeResponseTypeHandlerExtension uut = spy(new BFSICodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doReturn(false).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSICodeResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
        assertEquals(0L, argument.getValue().getRefreshTokenvalidityPeriod());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkCodeResponseTypeHandlingForRequestObjectError()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(new String[]{"Asd", "addd", "rr"});

        BFSICodeResponseTypeHandlerExtension uut = spy(new BFSICodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doThrow(RequestObjectException.class).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSICodeResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test(expectedExceptions = IdentityOAuth2Exception.class)
    public void checkCodeResponseTypeHandlingForIdentityOAuth2Exception()
            throws IdentityOAuth2Exception, RequestObjectException {

        // Mock
        BFSIResponseTypeHandler bfsiResponseTypeHandler = mock(BFSIDefaultResponseTypeHandlerImpl.class);
        when(bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(any())).thenReturn(109L);
        when(bfsiResponseTypeHandler.updateApprovedScopes(any())).thenReturn(null);

        BFSICodeResponseTypeHandlerExtension uut = spy(new BFSICodeResponseTypeHandlerExtension());
        doReturn(null).when(uut).issueCode(any());
        doReturn(true).when(uut).isRegulatory(any());

        ArgumentCaptor<OAuthAuthzReqMessageContext> argument =
                ArgumentCaptor.forClass(OAuthAuthzReqMessageContext.class);

        // Assign
        BFSICodeResponseTypeHandlerExtension.bfsiResponseTypeHandler = bfsiResponseTypeHandler;

        // Act
        uut.issue(new OAuthAuthzReqMessageContext(new OAuth2AuthorizeReqDTO()));

        // Assert
        verify(uut).issue(argument.capture());
    }

    @Test
    public void checkHandlerLogic()  {

        OAuthAuthzReqMessageContext mock = mock(OAuthAuthzReqMessageContext.class);
        when(mock.getRefreshTokenvalidityPeriod()).thenReturn(6666L);
        when(mock.getApprovedScope()).thenReturn(new String[]{"1"});

        BFSIResponseTypeHandler uut = new BFSIDefaultResponseTypeHandlerImpl();

        assertEquals(6666L, uut.updateRefreshTokenValidityPeriod(mock));

    }

    @Test
    public void checkHandlerUpdateApprovedScopes() throws RequestObjectException {

        try (MockedStatic<IdentityExtensionsDataHolder> identityDataHolderMock =
                     mockStatic(IdentityExtensionsDataHolder.class)) {
            OAuthAuthzReqMessageContext mock = mock(OAuthAuthzReqMessageContext.class);
            when(mock.getRefreshTokenvalidityPeriod()).thenReturn(6666L);
            when(mock.getApprovedScope()).thenReturn(new String[]{"1"});
            OAuth2AuthorizeReqDTO mockAuthReq = mock(OAuth2AuthorizeReqDTO.class);
            when(mock.getAuthorizationReqDTO()).thenReturn(mockAuthReq);
            when(mockAuthReq.getSessionDataKey()).thenReturn("123");

            RequestedClaim claim = new RequestedClaim();
            claim.setName("openbanking_intent_id");
            claim.setValue("123");

            List<RequestedClaim> requestedClaims = List.of(claim);

            RequestObjectService mockRequestObjectService = mock(RequestObjectService.class);
            when(mockRequestObjectService.getRequestedClaimsForSessionDataKey(anyString(), anyBoolean()))
                    .thenReturn(requestedClaims);
            IdentityExtensionsDataHolder dataHolder = mock(IdentityExtensionsDataHolder.class);
            identityDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getRequestObjectService()).thenReturn(mockRequestObjectService);
            when(dataHolder.getConfigurationMap())
                    .thenReturn(Map.of("Identity.ConsentIDClaimName", "consent_id"));

            BFSIResponseTypeHandler uut = new BFSIDefaultResponseTypeHandlerImpl();
            String[] result = uut.updateApprovedScopes(mock);

            assertEquals(2, result.length);
        }
    }

    @Test
    public void checkHandlerUpdateApprovedScopesWithoutConsentId() throws RequestObjectException {

        try (MockedStatic<IdentityExtensionsDataHolder> identityDataHolderMock =
                     mockStatic(IdentityExtensionsDataHolder.class)) {
            OAuthAuthzReqMessageContext mock = mock(OAuthAuthzReqMessageContext.class);
            when(mock.getRefreshTokenvalidityPeriod()).thenReturn(6666L);
            when(mock.getApprovedScope()).thenReturn(new String[]{"1"});
            OAuth2AuthorizeReqDTO mockAuthReq = mock(OAuth2AuthorizeReqDTO.class);
            when(mock.getAuthorizationReqDTO()).thenReturn(mockAuthReq);
            when(mockAuthReq.getSessionDataKey()).thenReturn("123");

            RequestedClaim claim = new RequestedClaim();
            claim.setName("validity");
            claim.setValue("123");

            List<RequestedClaim> requestedClaims = List.of(claim);

            RequestObjectService mockRequestObjectService = mock(RequestObjectService.class);
            when(mockRequestObjectService.getRequestedClaimsForSessionDataKey(anyString(), anyBoolean()))
                    .thenReturn(requestedClaims);
            IdentityExtensionsDataHolder dataHolder = mock(IdentityExtensionsDataHolder.class);
            when(dataHolder.getRequestObjectService()).thenReturn(mockRequestObjectService);
            identityDataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolder);

            BFSIResponseTypeHandler uut = new BFSIDefaultResponseTypeHandlerImpl();
            String[] result = uut.updateApprovedScopes(mock);

            assertEquals(1, result.length);
        }
    }

    @Test
    public void checkHandlerUpdateApprovedScopesForNullConsentId()  {

        OAuthAuthzReqMessageContext mock = mock(OAuthAuthzReqMessageContext.class);
        when(mock.getRefreshTokenvalidityPeriod()).thenReturn(6666L);
        when(mock.getApprovedScope()).thenReturn(new String[]{"1"});
        OAuth2AuthorizeReqDTO mockAuthReq = mock(OAuth2AuthorizeReqDTO.class);
        when(mock.getAuthorizationReqDTO()).thenReturn(mockAuthReq);
        when(mockAuthReq.getSessionDataKey()).thenReturn("123");

        BFSIResponseTypeHandler uut = new BFSIDefaultResponseTypeHandlerImpl();
        String[] result = uut.updateApprovedScopes(mock);

        assertEquals(1, result.length);

    }
}
