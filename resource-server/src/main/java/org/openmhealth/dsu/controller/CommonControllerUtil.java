package org.openmhealth.dsu.controller;

import org.openmhealth.dsu.domain.EndUserUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * A collection of small routines shared across controllers.
 * Created by wwadge on 16/07/2016.
 */

public class CommonControllerUtil {

    public static String getEndUserId(Authentication authentication, String specifiedEndUserId) {

        if (authentication instanceof OAuth2Authentication) {
            String grant = ((OAuth2Authentication) authentication).getOAuth2Request().getRequestParameters().get("grant_type");
            if ("client_credentials".equals(grant)) {
                return specifiedEndUserId;
            }
        }

        return ((EndUserUserDetails) authentication.getPrincipal()).getUsername();
    }


}
