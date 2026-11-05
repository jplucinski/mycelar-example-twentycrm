package io.mycelar.example.twenty.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mycelar.example.twenty.twenty.TwentyClient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public final class TwentyTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TwentyClient client;

    public TwentyTools(TwentyClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Tool(name = "searchPeople", description = "Find people. Omit the filter to list the first page.")
    public String searchPeople(
            @ToolParam(description = "Twenty REST filter. Example: name.firstName[eq]:Ada. Omit to list without a filter.") String filter,
            @ToolParam(description = "Page size. Null uses 20.") Integer limit) {
        return client.list("people", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getPerson", description = "Load one person by id.")
    public String getPerson(@ToolParam(description = "Person id.") String id) {
        return client.get("people", id);
    }

    @Tool(name = "createPerson", description = "Create a person with first name, last name, and primary email.")
    public String createPerson(
            @ToolParam(description = "Given name.") String firstName,
            @ToolParam(description = "Family name.") String lastName,
            @ToolParam(description = "Primary email.") String email) {
        return client.create(
                "people",
                Map.of(
                        "name",
                        Map.of("firstName", firstName, "lastName", lastName),
                        "emails",
                        Map.of("primaryEmail", email)));
    }

    @Tool(name = "updatePerson", description = "Update one person by id. Null fields are omitted.")
    public String updatePerson(
            @ToolParam(description = "Person id.") String id,
            @ToolParam(description = "Given name, or null to leave unchanged.") String firstName,
            @ToolParam(description = "Family name, or null to leave unchanged.") String lastName,
            @ToolParam(description = "Primary email, or null to leave unchanged.") String email) {
        Map<String, Object> name = new LinkedHashMap<>();
        if (firstName != null) {
            name.put("firstName", firstName);
        }
        if (lastName != null) {
            name.put("lastName", lastName);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (!name.isEmpty()) {
            body.put("name", name);
        }
        if (email != null) {
            body.put("emails", Map.of("primaryEmail", email));
        }
        if (body.isEmpty()) {
            throw new IllegalArgumentException("at least one field is required");
        }
        return client.patch("people", id, body);
    }

    @Tool(name = "searchCompanies", description = "Find companies. Omit the filter to list the first page.")
    public String searchCompanies(
            @ToolParam(description = "Twenty REST filter. Example: name[eq]:Acme. Omit to list without a filter.") String filter,
            @ToolParam(description = "Page size. Null uses 20.") Integer limit) {
        return client.list("companies", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getCompany", description = "Load one company by id.")
    public String getCompany(@ToolParam(description = "Company id.") String id) {
        return client.get("companies", id);
    }

    @Tool(name = "createCompany", description = "Create a company by name.")
    public String createCompany(@ToolParam(description = "Company name.") String name) {
        return client.create("companies", Map.of("name", name));
    }

    @Tool(name = "updateCompany", description = "Update one company by id.")
    public String updateCompany(
            @ToolParam(description = "Company id.") String id,
            @ToolParam(description = "Company name. Required.") String name) {
        if (name == null) {
            throw new IllegalArgumentException("at least one field is required");
        }
        return client.patch("companies", id, Map.of("name", name));
    }

    @Tool(name = "searchOpportunities", description = "Find opportunities. Omit the filter to list the first page.")
    public String searchOpportunities(
            @ToolParam(description = "Twenty REST filter. Example: stage[eq]:NEW. Omit to list without a filter.") String filter,
            @ToolParam(description = "Page size. Null uses 20.") Integer limit) {
        return client.list("opportunities", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getOpportunity", description = "Load one opportunity by id.")
    public String getOpportunity(@ToolParam(description = "Opportunity id.") String id) {
        return client.get("opportunities", id);
    }

    @Tool(name = "createOpportunity", description = "Create an opportunity. A null stage is sent as NEW.")
    public String createOpportunity(
            @ToolParam(description = "Opportunity name.") String name,
            @ToolParam(description = "Pipeline stage. Null becomes NEW.") String stage) {
        return client.create("opportunities", Map.of("name", name, "stage", stage == null ? "NEW" : stage));
    }

    @Tool(name = "updateOpportunity", description = "Update one opportunity by id. Null fields are omitted.")
    public String updateOpportunity(
            @ToolParam(description = "Opportunity id.") String id,
            @ToolParam(description = "Opportunity name, or null to leave unchanged.") String name,
            @ToolParam(description = "Pipeline stage, or null to leave unchanged.") String stage) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (name != null) {
            body.put("name", name);
        }
        if (stage != null) {
            body.put("stage", stage);
        }
        if (body.isEmpty()) {
            throw new IllegalArgumentException("at least one field is required");
        }
        return client.patch("opportunities", id, body);
    }

    @Tool(name = "searchNotes", description = "Find notes. Omit the filter to list the first page.")
    public String searchNotes(
            @ToolParam(description = "Twenty REST filter. Example: title[eq]:Kickoff. Omit to list without a filter.") String filter,
            @ToolParam(description = "Page size. Null uses 20.") Integer limit) {
        return client.list("notes", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getNote", description = "Load one note by id.")
    public String getNote(@ToolParam(description = "Note id.") String id) {
        return client.get("notes", id);
    }

    @Tool(name = "createNote", description = "Create a note with a title and markdown body. Link at most one of person, company, or opportunity.")
    public String createNote(
            @ToolParam(description = "Note title.") String title,
            @ToolParam(description = "Note body in markdown.") String markdown,
            @ToolParam(description = "Person id to link, or empty. At most one target.") String personId,
            @ToolParam(description = "Company id to link, or empty. At most one target.") String companyId,
            @ToolParam(description = "Opportunity id to link, or empty. At most one target.") String opportunityId) {
        int targets = targetCount(personId, companyId, opportunityId);
        if (targets > 1) {
            throw new IllegalArgumentException("at most one of personId, companyId, opportunityId");
        }
        String created = client.create("notes", Map.of("title", title, "bodyV2", Map.of("markdown", markdown)));
        if (targets == 1) {
            client.createNoteTarget(noteId(created), personId, companyId, opportunityId);
        }
        return created;
    }

    private static int targetCount(String personId, String companyId, String opportunityId) {
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
        return set;
    }

    private static String noteId(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            for (String path : new String[] { "/data/createNote/id", "/data/note/id", "/id" }) {
                String value = root.at(path).asText();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("note id missing from create response", e);
        }
        throw new IllegalStateException("note id missing from create response");
    }
}
