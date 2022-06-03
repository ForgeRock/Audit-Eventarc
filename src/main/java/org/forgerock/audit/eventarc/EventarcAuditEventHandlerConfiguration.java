/*
 * Copyright 2016-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package org.forgerock.audit.eventarc;

import org.forgerock.audit.events.handlers.EventHandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * A configuration for the Eventarc audit event handler.
 * <p>
 * This configuration object can be created from JSON. Example of valid JSON configuration:
 * </p>
 * <pre>
 *  {
 *    "name" : "eventarc",
 *    "topics": [ "access", "activity", "config", "authentication" ],
 *    "connection" : {
 *      "accessToken" : "myAccessToken"
 *    },
 *    "eventarcConfiguration" : {
 *      "specVersion" : 1.0,
 *      "project" : "forgerock-eventarc",
 *      "channel" : "test-channel"
 *    }
 *  }
 * </pre>
 */
public class EventarcAuditEventHandlerConfiguration extends EventHandlerConfiguration {

    @JsonPropertyDescription("audit.handlers.eventarc.connection")
    private ConnectionConfiguration token = new ConnectionConfiguration();

    @JsonPropertyDescription("audit.handlers.eventarc.buffering")
    private EventarcConfiguration eventarc = new EventarcConfiguration();

    /**
     * Gets configuration of connection to Elasticsearch.
     *
     * @return configuration of connection to Elasticsearch
     */
    public ConnectionConfiguration getToken() {
        return token;
    }

    /**
     * Sets configuration of connection to Elasticsearch.
     *
     * @param token configuration of connection to Elasticsearch
     */
    public void setToken(ConnectionConfiguration token) {
        this.token = token;
    }

    /**
     * Gets configuration of event buffering.
     *
     * @return configuration of event buffering
     */
    public EventarcConfiguration getEventarc() {
        return eventarc;
    }

    /**
     * Sets configuration of event buffering.
     *
     * @param eventarc configuration of event buffering
     */
    public void setEventarc(EventarcConfiguration eventarc) {
        this.eventarc = eventarc;
    }

    @Override
    public boolean isUsableForQueries() {
        return true;
    }

    /**
     * Configuration of connection to Eventarc.
     */
    public static class ConnectionConfiguration {

        @JsonPropertyDescription("audit.handlers.eventarc.connection.accessToken")
        private String accessToken;


        /**
         * Gets Eventarc accessToken for authentication.
         *
         * @return The accessToken.
         */
        public String getAccessToken() {
            return accessToken;
        }

        /**
         * Sets Eventarc accessToken for authentication.
         *.
         * @param accessToken The accessToken.
         */
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }


    /**
     * Configuration of Eventarc.
     */
    public static class EventarcConfiguration {

        @JsonPropertyDescription("audit.handlers.eventarc.configuration.specVersion")
        private String specVersion;

        @JsonPropertyDescription("audit.handlers.eventarc.configuration.location")
        private String location;

        @JsonPropertyDescription("audit.handlers.eventarc.configuration.project")
        private String project;

        @JsonPropertyDescription("audit.handlers.eventarc.configuration.channel")
        private String channel;

        /**
         * Gets the spec version.
         *
         * @return specVersion
         */
        public String getSpecVersion() {
            return specVersion;
        }

        /**
         * Sets the spec version.
         *
         * @param specVersion
         */
        public void setSpecVersion(String specVersion) {
            this.specVersion = specVersion;
        }

        /**
         * Gets the Google Project location to use when posting events to Eventarc.
         *
         * @return project
         */
        public String getLocation() {
            return location;
        }

        /**
         * Sets the Google Project location to use when posting events to Eventarc.
         *
         * @return location
         */
        public void setLocation(String location) {
            this.location = location;
        }

        /**
         * Gets the Google Project to use when posting events to Eventarc.
         *
         * @return project
         */
        public String getProject() {
            return project;
        }

        /**
         * Sets the Google Project to use when posting events to Eventarc.
         *
         * @return project
         */
        public void setProject(String project) {
            this.project = project;
        }

        /**
         * Gets the Eventarc channel to use when posting events to Eventarc.
         *
         * @return channel
         */
        public String getChannel() {
            return channel;
        }

        /**
         * Sets the Eventarc channel to use when posting events to Eventarc.
         *
         * @param channel
         */
        public void setChannel(String channel) {
            this.channel = channel;
        }
    }
}
