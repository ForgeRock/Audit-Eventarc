/*
 * Copyright 2016-2019 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

package org.forgerock.audit.eventarc;

import static com.sun.identity.shared.datastruct.CollectionHelper.getBooleanMapAttr;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.forgerock.audit.AuditException;
import org.forgerock.audit.events.handlers.AuditEventHandler;
import org.forgerock.http.Client;
import org.forgerock.openam.audit.AuditEventHandlerFactory;
import org.forgerock.openam.audit.configuration.AuditEventHandlerConfiguration;

/**
 * This factory is responsible for creating an instance of the {@link EventarcAuditEventHandlerFactory}.
 *
 * @since 13.5.0
 */
public final class EventarcAuditEventHandlerFactory implements AuditEventHandlerFactory {

    private final Client client;

    /**
     * Makes use of the injected Client when creating new EventarcAuditEventHandlerFactory instances.
     * @param client Used when creating new EventarcAuditEventHandlerFactory instances.
     */
    @Inject
    public EventarcAuditEventHandlerFactory(final Client client) {
        this.client = client;
    }

    @Override
    public AuditEventHandler create(AuditEventHandlerConfiguration configuration) throws AuditException {
        final EventarcAuditEventHandlerConfiguration ecHandlerConfiguration = new
                EventarcAuditEventHandlerConfiguration();

        final Map<String, Set<String>> attributes = configuration.getAttributes();

        ecHandlerConfiguration.setName(configuration.getHandlerName());
        ecHandlerConfiguration.setEnabled(getBooleanMapAttr(attributes, "enabled", false));
        ecHandlerConfiguration.setTopics(attributes.get("topics"));

        return new EventarcAuditEventHandler(ecHandlerConfiguration, configuration.getEventTopicsMetaData(),
                client);
    }

}
