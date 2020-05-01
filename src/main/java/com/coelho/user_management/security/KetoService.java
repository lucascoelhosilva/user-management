package com.coelho.user_management.security;

import com.coelho.user_management.constants.Constants;
import com.coelho.user_management.exceptions.CustomException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import sh.ory.keto.ApiClient;
import sh.ory.keto.ApiException;
import sh.ory.keto.Configuration;
import sh.ory.keto.api.EnginesApi;
import sh.ory.keto.model.OryAccessControlPolicy;
import sh.ory.keto.model.OryAccessControlPolicyRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KetoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KetoService.class);

    private final String ketoUrl;

    public KetoService(String ketoUrl) {
        this.ketoUrl = ketoUrl;
    }

    public void upsertOryAccessControlPolicy(RoutingContext rc) {
        LOGGER.debug("upsert ===== {0}", rc.getBodyAsString());

        OryAccessControlPolicy body = rc.getBodyAsJson().mapTo(OryAccessControlPolicy.class);
        body.setId(UUID.randomUUID().toString());

        String flavor = "exact"; // String | The ORY Access Control Policy flavor. Can be \"regex\", \"glob\", and \"exact\".

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(ketoUrl);

        EnginesApi apiInstance = new EnginesApi(defaultClient);
        try {
            apiInstance.upsertOryAccessControlPolicy(flavor, body);
            rc.response().setStatusCode(201).end();
        } catch (ApiException e) {
            throw new CustomException(e.getMessage(), e.getCode());
        }
    }

    public void listAccessControlPolicies(RoutingContext rc) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(ketoUrl);

        EnginesApi apiInstance = new EnginesApi(defaultClient);
        String flavor = "exact"; // String | The ORY Access Control Policy flavor. Can be \"regex\", \"glob\", and \"exact\"

        Optional<String> paramLimit = rc.queryParam("limit").stream().findFirst();
        Long limit = paramLimit.map(Long::valueOf).orElse(10L); // Long | The maximum amount of policies returned.
        Optional<String> paramOffset = rc.queryParam("offset").stream().findFirst();
        Long offset = paramOffset.map(Long::valueOf).orElse(0L); // Long | The offset from where to start looking.

        String subject = rc.request().getParam("subject"); // String | The subject for whom the policies are to be listed.
        String resource = rc.request().getParam("resource"); // String | The resource for which the policies are to be listed.
        String action = rc.request().getParam("action"); // String | The action for which policies are to be listed.
        try {
            List<OryAccessControlPolicy> result = apiInstance.listOryAccessControlPolicies(flavor, limit, offset, subject, resource, action);
            rc.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
                    .setStatusCode(200)
                    .end(Json.encodePrettily(result));
        } catch (ApiException e) {
            throw new CustomException(e.getMessage(), e.getCode());
        }
    }

    public void upsertAccessControlPolicyRole(RoutingContext rc) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(ketoUrl);

        EnginesApi apiInstance = new EnginesApi(defaultClient);
        String flavor = "exact"; // String | The ORY Access Control Policy flavor. Can be \"regex\", \"glob\", and \"exact\".

        OryAccessControlPolicyRole body = rc.getBodyAsJson().mapTo(OryAccessControlPolicyRole.class);
        try {
            apiInstance.upsertOryAccessControlPolicyRole(flavor, body);
            rc.response().setStatusCode(201).end();
        } catch (ApiException e) {
            throw new CustomException(e.getMessage(), e.getCode());
        }
    }

    public void listAccessControlPolicyRoles(RoutingContext rc) {
        Optional<String> paramLimit = rc.queryParam("limit").stream().findFirst();
        Long limit = paramLimit.map(Long::valueOf).orElse(10L); // Long | The maximum amount of policies returned.

        Optional<String> paramOffset = rc.queryParam("offset").stream().findFirst();
        Long offset = paramOffset.map(Long::valueOf).orElse(0L); // Long | The offset from where to start looking.


        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(ketoUrl);

        EnginesApi apiInstance = new EnginesApi(defaultClient);

        String flavor = "exact"; // String | The ORY Access Control Policy flavor. Can be \"regex\", \"glob\", and \"exact\"

        String member = rc.request().getParam("member"); // String | The member for which the roles are to be listed.
        try {
            List<OryAccessControlPolicyRole> result = apiInstance.listOryAccessControlPolicyRoles(flavor, limit, offset, member);
            rc.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON_UTF8)
                    .setStatusCode(200)
                    .end(Json.encodePrettily(result));
        } catch (ApiException e) {
            throw new CustomException(e.getMessage(), e.getCode());
        }
    }
}