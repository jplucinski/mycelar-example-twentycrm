package io.mycelar.example.twenty.twenty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

public final class TwentyClient {

    private final RestClient rest;

    public TwentyClient(RestClient rest) {
        this.rest = Objects.requireNonNull(rest, "rest");
    }

    public String list(String plural, String filter, int limit) {
        try {
            return rest.get()
                    .uri(
                            uri -> {
                                uri.path("/{plural}").queryParam("limit", limit);
                                if (filter != null && !filter.isBlank()) {
                                    uri.queryParam("filter", filter);
                                }
                                return uri.build(plural);
                            })
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new TwentyException("Twenty list failed for " + plural, e);
        }
    }

    public String get(String plural, String id) {
        try {
            return rest.get().uri("/{plural}/{id}", plural, id).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            throw new TwentyException("Twenty get failed for " + plural + "/" + id, e);
        }
    }

    public String create(String plural, Map<String, Object> body) {
        try {
            return rest.post().uri("/{plural}", plural).body(body).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            throw new TwentyException("Twenty create failed for " + plural, e);
        }
    }

    public String patch(String plural, String id, Map<String, Object> body) {
        try {
            return rest.patch().uri("/{plural}/{id}", plural, id).body(body).retrieve().body(String.class);
        } catch (RestClientResponseException e) {
            throw new TwentyException("Twenty patch failed for " + plural + "/" + id, e);
        }
    }

    public String createNoteTarget(String noteId, String personId, String companyId, String opportunityId) {
        Objects.requireNonNull(noteId, "noteId");
        int set = 0;
        if (personId != null && !personId.isBlank()) {
            set++;
        }
        if (companyId != null && !companyId.isBlank()) {
            set++;
        }
        if (opportunityId != null && !opportunityId.isBlank()) {
            set++;
        }
        if (set != 1) {
            throw new IllegalArgumentException("exactly one of personId, companyId, opportunityId is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("noteId", noteId);
        if (personId != null && !personId.isBlank()) {
            body.put("personId", personId);
        }
        if (companyId != null && !companyId.isBlank()) {
            body.put("companyId", companyId);
        }
        if (opportunityId != null && !opportunityId.isBlank()) {
            body.put("opportunityId", opportunityId);
        }
        return create("noteTargets", body);
    }
}
