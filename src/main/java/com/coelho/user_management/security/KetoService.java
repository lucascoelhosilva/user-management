package com.coelho.user_management.security;

import com.coelho.user_management.exceptions.CustomException;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import sh.ory.keto.ApiClient;
import sh.ory.keto.ApiException;
import sh.ory.keto.Configuration;
import sh.ory.keto.api.EnginesApi;
import sh.ory.keto.model.OryAccessControlPolicy;

import java.util.List;
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

    public void listAccessControlPolicyRoles(RoutingContext rc) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(ketoUrl);

        EnginesApi apiInstance = new EnginesApi(defaultClient);

        String flavor = "exact"; // String | The ORY Access Control Policy flavor. Can be \"regex\", \"glob\", and \"exact\"
        Long limit = 10L; // Long | The maximum amount of policies returned.
        Long offset = 0L; // Long | The offset from where to start looking.

        String subject = "lucascoelhosilvacs@gmail.com"; // String | The subject for whom the policies are to be listed.
        String resource = "user-management:user"; // String | The resource for which the policies are to be listed.
        String action = "delete"; // String | The action for which policies are to be listed.

        try {
            List<OryAccessControlPolicy> result = apiInstance.listOryAccessControlPolicies(flavor, limit, offset, subject, resource, action);
            rc.response().setStatusCode(200).end(Json.encodePrettily(result));
        } catch (ApiException e) {
            throw new CustomException(e.getMessage(), e.getCode());
        }
    }
}