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

//        final EventBufferingConfiguration bufferConfig = configuration.getBuffering();
//        if (bufferConfig.isEnabled()) {
//            final Duration writeInterval =
//                    bufferConfig.getWriteInterval() == null || bufferConfig.getWriteInterval().isEmpty()
//                            ? null
//                            : Duration.duration(bufferConfig.getWriteInterval());
//            batchIndexer = BufferedBatchPublisher.newBuilder(this)
//                    .capacity(bufferConfig.getMaxSize())
//                    .writeInterval(writeInterval)
//                    .maxBatchEvents(bufferConfig.getMaxBatchedEvents())
//                    .averagePerEventPayloadSize(BATCH_INDEX_AVERAGE_PER_EVENT_PAYLOAD_SIZE)
//                    .autoFlush(ALWAYS_FLUSH_BATCH_QUEUE)
//                    .build();
//        } else {
//            batchIndexer = null;
//        }
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
//        final int pageSize = query.getPageSize() <= 0 ? DEFAULT_PAGE_SIZE : query.getPageSize();
//        // set the offset to either first the offset provided, or second the paged result cookie value, or finally 0
//        final int offset;
//        if (query.getPagedResultsOffset() != 0) {
//            offset = query.getPagedResultsOffset();
//        } else if (query.getPagedResultsCookie() != null) {
//            offset = Integer.valueOf(query.getPagedResultsCookie());
//        } else {
//            offset = DEFAULT_OFFSET;
//        }
//
//        final JsonValue payload =
//                json(object(field(
//                        QUERY, query.getQueryFilter().accept(ELASTICSEARCH_QUERY_FILTER_VISITOR, null).getObject())));
//        try {
//            final Request request = createRequest(GET, buildSearchUri(topic, pageSize, offset), payload.getObject());
//            return client.send(request).thenAsync(closeSilently(response -> {
//                if (!response.getStatus().isSuccessful()) {
//                    final String message =
//                            "Elasticsearch response (" + indexName + "/" + topic + SEARCH + "): "
//                            + response.getEntity();
//                    return newResourceException(response.getStatus().getCode(), message).asPromise();
//                }
//
//                return response.getEntity().getJsonAsync()
//                        .then(JsonValue::json)
//                        .then(events -> {
//                            for (JsonValue event : events.get(HITS).get(HITS)) {
//                                handler.handleResource(
//                                        newResourceResponse(event.get(FIELD_CONTENT_ID).asString(), null,
//                                                ElasticsearchUtil.denormalizeJson(event.get(SOURCE))));
//                            }
//                            final int totalResults = events.get(HITS).get(TOTAL).asInteger();
//                            final String pagedResultsCookie = (pageSize + offset) >= totalResults
//                                    ? null
//                                    : Integer.toString(pageSize + offset);
//                            return newQueryResponse(pagedResultsCookie,
//                                    CountPolicy.EXACT,
//                                    totalResults);
//                        })
//                        .thenCatchAsync(internalServerException());
//            }), noopExceptionAsyncFunction());
//        } catch (URISyntaxException e) {
//            return new InternalServerErrorException(e.getMessage(), e).asPromise();
//        }
    }

    private static <R> AsyncFunction<? super Exception, R, ResourceException> internalServerException() {
        return e -> new InternalServerErrorException(e.getMessage(), e).asPromise();
    }

     @Override
    public Promise<ResourceResponse, ResourceException> readEvent(final Context context, final String topic,
            final String resourceId) {
//        final Request request;
//        try {
//            request = createRequest(GET, buildEventUri(topic, resourceId), null);
//        } catch (Exception e) {
//            final String error = String.format("Unable to read audit entry for topic=%s, _id=%s", topic, resourceId);
//            LOGGER.error(error, e);
//            return new InternalServerErrorException(error, e).asPromise();
//        }
//
//        return client.send(request).thenAsync(closeSilently(response -> {
//            if (!response.getStatus().isSuccessful()) {
//                return resourceException(indexName, topic, resourceId, response).asPromise();
//            }
//
//            return response.getEntity().getJsonAsync()
//                    .then(JsonValue::json)
//                    .then(jsonValue -> {
//                        // the original audit JSON is under _source, and we also add back the _id
//                        jsonValue = ElasticsearchUtil.denormalizeJson(jsonValue.get(SOURCE));
//                        jsonValue.put(FIELD_CONTENT_ID, resourceId);
//                        return newResourceResponse(resourceId, null, jsonValue);
//                    })
//                    .thenCatchAsync(internalServerException());
//        }), noopExceptionAsyncFunction());
        return null;
    }
     @Override
    public Promise<ResourceResponse, ResourceException> publishEvent(final Context context, final String topic,
            final JsonValue event) {
//        if (batchIndexer == null) {
//            return publishSingleEvent(topic, event);
//        }
//        else {
//            if (!batchIndexer.offer(topic, event)) {
//                return new ServiceUnavailableException("Elasticsearch batch indexer full, so dropping audit event "
//                        + indexName + "/" + topic + "/" + event.get("_id").asString()).asPromise();
//            }
//            return newResourceResponse(event.get(ResourceResponse.FIELD_CONTENT_ID).asString(), null,
//                    event).asPromise();
//        }
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
                                               topic, "1.0.0",
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

//    /**
//     * Adds an audit event to an Elasticsearch Bulk API payload.
//     *
//     * @param topic Event topic
//     * @param event Event JSON payload
//     * @param payload Elasticsearch Bulk API payload
//     * @throws BatchException indicates failure to add-to-batch
//     */
//    @Override
//    public void addToBatch(final String topic, final JsonValue event, final StringBuilder payload)
//            throws BatchException {
//        try {
//            // _id is a protected Elasticsearch field so we must remove it before writing
//            JsonValue eventCopy = event.copy();
//            final String resourceId = eventCopy.get(FIELD_CONTENT_ID).asString();
//            eventCopy.remove(FIELD_CONTENT_ID);
//            final String jsonPayload = ElasticsearchUtil.normalizeJson(eventCopy);
//
//            // newlines have special significance in the Bulk API
//            // https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html
//            payload.append("{ \"index\" : { \"_type\" : ")
//                    .append(OBJECT_MAPPER.writeValueAsString(topic))
//                    .append(", \"_id\" : ")
//                    .append(OBJECT_MAPPER.writeValueAsString(resourceId))
//                    .append(" } }\n")
//                    .append(jsonPayload)
//                    .append('\n');
//        } catch (IOException e) {
//            throw new BatchException("Unexpected error while adding to batch", e);
//        }
//    }

//    /**
//     * Publishes a <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html">Bulk API</a>
//     * payload to Elasticsearch.
//     *
//     * @param payload Elasticsearch Bulk API payload
//     */
//    @Override
//    public Promise<Void, BatchException> publishBatch(final String payload) {
//        final Request request;
//        try {
//            request = createRequest(buildBulkUri(), payload);
//        } catch (URISyntaxException e) {
//            return newExceptionPromise(new BatchException("Incorrect URI", e));
//        }
//
//        return client.send(request)
//                .thenAsync(closeSilently(processBatchResponse()), noopExceptionAsyncFunction());
//    }

//    private AsyncFunction<Response, Void, BatchException> processBatchResponse() {
//        return response -> {
//            if (!response.getStatus().isSuccessful()) {
//                throw new BatchException("Elasticsearch batch index failed: " + response.getEntity());
//            }
//
//            return response.getEntity().getJsonAsync()
//                    .then(JsonValue::json, e -> {
//                        throw new BatchException("Unexpected error while publishing batch", e);
//                    })
//                    .then(responseJson -> {
//                        if (responseJson.get("errors").asBoolean()) {
//                            // one or more batch index operations failed, so log failures
//                            final JsonValue items = responseJson.get("items");
//                            final int n = items.size();
//                            final List<Object> failureItems = new ArrayList<>(n);
//                            for (int i = 0; i < n; ++i) {
//                                final JsonValue item = items.get(i).get("index");
//                                final Integer status = item.get("status").asInteger();
//                                if (status >= 400) {
//                                    failureItems.add(item);
//                                }
//                            }
//                            try {
//                                String jsonItems = OBJECT_MAPPER.writeValueAsString(failureItems);
//                                String message = "One or more Elasticsearch batch index entries failed: " + jsonItems;
//                                throw new BatchException(message);
//                            } catch (JsonProcessingException e) {
//                                throw new BatchException("Unexpected error while publishing batch", e);
//                            }
//                        }
//                        return null;
//                    });
//        };
//    }


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
