/*
 * Copyright 2016-2020 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package org.forgerock.audit.eventarc;

import static org.forgerock.json.JsonValue.array;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.resource.ResourceException.newResourceException;
import static org.forgerock.json.resource.ResourceResponse.FIELD_CONTENT_ID;
import static org.forgerock.json.resource.Responses.newResourceResponse;
import org.apache.commons.io.IOUtils;
import static org.forgerock.http.handler.HttpClientHandler.OPTION_LOADER;
import static org.forgerock.http.protocol.Responses.noopExceptionFunction;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import org.forgerock.http.apache.async.AsyncHttpClientProvider;
import org.forgerock.audit.Audit;
import org.forgerock.audit.events.EventTopicsMetaData;
import org.forgerock.audit.events.handlers.AuditEventHandler;
import org.forgerock.audit.events.handlers.AuditEventHandlerBase;
import org.forgerock.http.Client;
import org.forgerock.http.HttpApplicationException;
import org.forgerock.http.handler.HttpClientHandler;
import org.forgerock.http.header.AuthorizationHeader;
import org.forgerock.http.header.ContentTypeHeader;
import org.forgerock.http.protocol.Request;
import org.forgerock.http.protocol.Response;
import org.forgerock.http.spi.Loader;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResourceHandler;
import org.forgerock.json.resource.QueryResponse;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.services.context.Context;
import org.forgerock.util.AsyncFunction;
import org.forgerock.util.CloseSilentlyFunction;
import org.forgerock.util.Options;
import org.forgerock.util.Reject;
import org.forgerock.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AuditEventHandler} for Elasticsearch.
 */
public class EventarcAuditEventHandler extends AuditEventHandlerBase  {
//      implements  BatchConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventarcAuditEventHandler.class);


    private static final String SEARCH = "/_search";
    private static final String POST = "POST";

    private final String baseUri;
    private final EventarcAuditEventHandlerConfiguration configuration;

    private final Client client;
    private final HttpClientHandler defaultHttpClientHandler;

     class CustomMessage {
        public CustomMessage(String message) {
          this.message = message;
        }
        public String message;
      }
    /**
     * Create a new {@code EventarcAuditEventHandler} instance.
     *
     * @param configuration Configuration parameters that can be adjusted by system administrators.
     * @param eventTopicsMetaData Meta-data for all audit event topics.
     * @param client HTTP client or {@code null} to use default client.
     */
    public EventarcAuditEventHandler(
            final EventarcAuditEventHandlerConfiguration configuration,
            final EventTopicsMetaData eventTopicsMetaData,
            @Audit("eventarc") final Client client) {
        super(configuration.getName(), eventTopicsMetaData, configuration.getTopics(), configuration.isEnabled());
        this.configuration = Reject.checkNotNull(configuration);
        if (client == null) {
            this.defaultHttpClientHandler = defaultHttpClientHandler();
            this.client = new Client(defaultHttpClientHandler);
        } else {
            this.defaultHttpClientHandler = null;
            this.client = client;
        }
        baseUri = buildBaseUri();
    }

    @Override
    public void startup() throws ResourceException {
    }

    @Override
    public void shutdown() throws ResourceException {
    	 if (defaultHttpClientHandler != null) {
    		            try {
    		                 defaultHttpClientHandler.close();
    		             } catch (IOException e) {
    		                 throw ResourceException.newResourceException(ResourceException.INTERNAL_ERROR,
    		                         "An error occurred while closing the default HTTP client handler", e);
    		             }
    		         }
       
    }
    
    @Override
    public Promise<QueryResponse, ResourceException> queryEvents(final Context context, final String topic,
           final QueryRequest query, final QueryResourceHandler handler) {
           return null;

    }

    private static <R> AsyncFunction<? super Exception, R, ResourceException> internalServerException() {
        return e -> new InternalServerErrorException(e.getMessage(), e).asPromise();
    }

     @Override
    public Promise<ResourceResponse, ResourceException> readEvent(final Context context, final String topic,
            final String resourceId) {

        return null;
    }
     @Override
    public Promise<ResourceResponse, ResourceException> publishEvent(final Context context, final String topic,
            final JsonValue event) {
        return publishSingleEvent(topic, event);

    }

    /**
     * Publishes a single event to the provided topic.
     *
     * @param topic The topic where to publish the event.
     * @param event The event to publish.
     * @return a promise with either a response or an exception
     */
    protected Promise<ResourceResponse, ResourceException> publishSingleEvent(final String topic,
            final JsonValue event) {

        final String resourceId = event.get(FIELD_CONTENT_ID).asString();
        event.remove(FIELD_CONTENT_ID);

        try {
            final String jsonPayload = event.toString();
            event.put(FIELD_CONTENT_ID, resourceId);
            JsonValue payload = json(object(
                    field("channel", "projects/" + configuration.getEventarc().getProject() + "/locations/" +
                            configuration.getEventarc().getLocation() + "/channels/" +
                            configuration.getEventarc().getChannel()),
                    field("events", array(
                            buildEvent(resourceId,
                                       "//forgerock/projects/" + configuration.getEventarc().getProject() + "/topics/" +
                                               topic, "1.0",
                                       jsonPayload,
                                       "google.cloud.pubsub.topic.v1.messagePublished")
                    ))));

            final Request request = createRequest(buildEventUri(), payload);

            return client.send(request).then(CloseSilentlyFunction.closeSilently(response -> {
                if (!response.getStatus().isSuccessful()) {
                    throw resourceException(topic, resourceId, response);
                }
                return newResourceResponse(event.get(ResourceResponse.FIELD_CONTENT_ID).asString(), null,
                        event);
            }), noopExceptionFunction());
        } catch (Exception e) {
            final String error = String.format("Unable to create audit entry for topic=%s, _id=%s", topic, resourceId);
            return new InternalServerErrorException(error, e).asPromise();
        }
    }


    /**
     * Builds an Eventarc API URI for operating on a single event (e.g., index, get, etc.).
     *
     * @return URI
     */
    protected String buildEventUri() {
        return buildBaseUri();
    }


    /**
     * Builds an Elasticsearch API URI for Search API.
     *
     * @param topic The audit topic to search.
     * @param pageSize The number of results to return.
     * @param offset The number of results to skip.
     * @return The search uri.
     */
    protected String buildSearchUri(final String topic, final int pageSize, final int offset) {
        return buildBaseUri() + "/" + topic + SEARCH + "?size=" + pageSize + "&from=" + offset;
    }

    protected JsonValue buildEvent(String id, String source, String specVersion, String payload, String type) {
        return json(object(
                field("@type", "type.googleapis.com/io.cloudevents.v1.CloudEvent"),
                field("attributes", object(
                              field("datacontenttype", object(
                                      field("ceString", "application/json")
                              )))),
                field("id", id),
                field("source", source),
                field("specVersion", specVersion),
                field("textData", payload),
                field("type", configuration.getEventarc().getEventType())

        ));
    }

    /**
     * Builds an Eventarc API base URI.
     *
     * @return Base URI
     */
    protected String buildBaseUri() {
        if (baseUri != null) {
            return baseUri;
        }
        final EventarcAuditEventHandlerConfiguration.EventarcConfiguration connection = configuration.getEventarc();
        return "https://eventarcpublishing.googleapis.com/v1/projects/" + connection.getProject() + "/locations/" +
                connection.getLocation() + "/channels/" + connection.getChannel() + ":publishEvents";
    }

    /**
     * Gets an {@code Exception} {@link Promise} containing an Eventarc HTTP response status and payload.
     *
     * @param topic Event topic
     * @param resourceId Event ID
     * @param response HTTP response
     * @return {@code Exception} {@link Promise}
     */
    protected static ResourceException resourceException(final String topic, final String resourceId, final Response response) {
        if (response.getStatus().getCode() == ResourceException.NOT_FOUND) {
            return new NotFoundException("Object " + resourceId + " not found in " + "/" + topic);
        }
        final String message = "Elasticsearch response (" + "/" + topic + "/" + resourceId + "): "
                + response.getEntity();
        return newResourceException(response.getStatus().getCode(), message);
    }

    private Request createRequest(final String uri, final Object payload)
            throws URISyntaxException {
        final Request request = new Request();
        request.setMethod(EventarcAuditEventHandler.POST);
        request.setUri(uri);
        String accessToken = null;
		try {
			accessToken = GoogleCredentials
			    .fromStream(IOUtils.toInputStream(String.valueOf(configuration.getToken().getJsonCredentials())))
			    .createScoped("https://www.googleapis.com/auth/cloud-platform")
			    .refreshAccessToken()
			    .getTokenValue();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        if (payload != null) {
            request.getHeaders().put(ContentTypeHeader.NAME, "application/json");
            request.getHeaders().put(AuthorizationHeader.NAME, "Bearer " + accessToken);
            request.getHeaders().put("X-Goog-User-Project", configuration.getEventarc().getProject());
            request.setEntity(payload);
        }
        return request;
    }

    private HttpClientHandler defaultHttpClientHandler() {
        try {
            return new HttpClientHandler(
                    Options.defaultOptions()
                           .set(OPTION_LOADER, new Loader() {
                                @Override
                                public <S> S load(Class<S> service, Options options) {
                                    return service.cast(new AsyncHttpClientProvider());
                                }
                            }));
        } catch (HttpApplicationException e) {
            throw new RuntimeException("Error while building default HTTP Client", e);
        }
    }
}
