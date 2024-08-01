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

package org.wso2.bfsi.identity.extensions.grant.type.handlers;

import org.wso2.bfsi.consent.management.common.util.CommonUtils;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.identity.extensions.util.IdentityCommonUtils;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeGrantHandler;

/**
 * BFSI specific authorization code grant handler.
 */
public class BFSIAuthorizationCodeGrantHandler extends AuthorizationCodeGrantHandler {

    @Override
    @Generated(message = "Excluding from code coverage since it requires a super call")
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (CommonUtils.isRegulatoryApp(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId())) {
                OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = super.issue(tokReqMsgCtx);
                executeInitialStep(oAuth2AccessTokenRespDTO, tokReqMsgCtx);
                tokReqMsgCtx.setScope(IdentityCommonUtils.removeInternalScopes(tokReqMsgCtx.getScope()));
                return oAuth2AccessTokenRespDTO;
            }
        } catch (RequestObjectException e) {
            throw new IdentityOAuth2Exception(e.getMessage());
        }
        return super.issue(tokReqMsgCtx);
    }

    /**
     * Extend this method to perform any actions which requires internal scopes.
     *
     * @param oAuth2AccessTokenRespDTO
     * @param tokReqMsgCtx
     */
    public void executeInitialStep(OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO,
                                   OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

    }

    /**
     * Extend this method to perform any actions related when issuing refresh token.
     *
     * @return
     */
    @Override
    public boolean issueRefreshToken() throws IdentityOAuth2Exception {

        return super.issueRefreshToken();
    }
}
