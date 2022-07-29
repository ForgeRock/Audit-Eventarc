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
import com.fasterxml.jackson.annotation.JsonProperty;
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
 *      "jsonCredentials" : "myJsonCredentials"
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

    @JsonProperty("Eventarc Connection")
    private ConnectionConfiguration token = new ConnectionConfiguration();

    @JsonProperty("Eventarc configuration")
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

        @JsonProperty("Google service json credentials")
        private String jsonCredentials;


        /**
         * Gets Eventarc jsonCredentials for authentication.
         *
         * @return The jsonCredentials.
         */
        public String getJsonCredentials() {
            return jsonCredentials;
        }

        /**
         * Sets Eventarc jsonCredentials for authentication.
         *.
         * @param jsonCredentials The jsonCredentials.
         */
        public void setJsonCredentials(String jsonCredentials) {
            this.jsonCredentials = jsonCredentials;
        }
    }


    /**
     * Configuration of Eventarc.
     */
    public static class EventarcConfiguration {

        @JsonProperty("Google location")
        private String location;

        @JsonProperty("Google project id")
        private String project;

        @JsonProperty("Google channel (Google channel ID)")
        private String channel;

        @JsonProperty("Event type")
        private String eventType;

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

        public String getEventType() {
                    return eventType;
                }

        /**
         * Sets the Google Event Type to use when posting events to Eventarc.
         *
         * @return eventType
         */
        public void setEventType(String eventType) {
            this.eventType = eventType;
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
