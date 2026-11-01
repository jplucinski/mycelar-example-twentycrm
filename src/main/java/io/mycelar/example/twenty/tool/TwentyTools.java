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

    @Tool(name = "searchPeople", description = "Find people when you do not have an id. Pass a Twenty REST filter such as name.firstName[eq]:Ada or emails.primaryEmail[eq]:ada@example.com. Omit the filter to list the first page.")
    public String searchPeople(
            @ToolParam(description = "Twenty REST filter. Example: name.firstName[eq]:Ada. Omit to list without a filter.") String filter,
            @ToolParam(description = "Maximum number of people to return. Use 20 when the user did not ask for a page size.") Integer limit) {
        return client.list("people", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getPerson", description = "Load one person by an id you already retrieved from search or from the user. Do not guess the id.")
    public String getPerson(@ToolParam(description = "Person id returned by a previous search or given by the user.") String id) {
        return client.get("people", id);
    }

    @Tool(name = "createPerson", description = "Create a person after search shows they are missing. Requires first name, last name, and primary email. Do not invent an email.")
    public String createPerson(
            @ToolParam(description = "Given name. Use the name the user provided.") String firstName,
            @ToolParam(description = "Family name. Use the name the user provided.") String lastName,
            @ToolParam(description = "Primary email address. Leave the call unmade if the user did not provide one.") String email) {
        return client.create(
                "people",
                Map.of(
                        "name",
                        Map.of("firstName", firstName, "lastName", lastName),
                        "emails",
                        Map.of("primaryEmail", email)));
    }

    @Tool(name = "updatePerson", description = "Update one existing person by id. Pass null for any first name, last name, or email the user did not ask to change.")
    public String updatePerson(
            @ToolParam(description = "Person id returned by a previous search or given by the user.") String id,
            @ToolParam(description = "Given name. Null when this field stays unchanged.") String firstName,
            @ToolParam(description = "Family name. Null when this field stays unchanged.") String lastName,
            @ToolParam(description = "Primary email. Null when this field stays unchanged.") String email) {
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

    @Tool(name = "searchCompanies", description = "Find companies when you do not have an id. Pass a Twenty REST filter such as name[eq]:Acme. Omit the filter to list the first page.")
    public String searchCompanies(
            @ToolParam(description = "Twenty REST filter. Example: name[eq]:Acme. Omit to list without a filter.") String filter,
            @ToolParam(description = "Maximum number of companies to return. Use 20 when the user did not ask for a page size.") Integer limit) {
        return client.list("companies", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getCompany", description = "Load one company by an id you already retrieved from search or from the user. Do not guess the id.")
    public String getCompany(@ToolParam(description = "Company id returned by a previous search or given by the user.") String id) {
        return client.get("companies", id);
    }

    @Tool(name = "createCompany", description = "Create a company after search shows that name is missing. Use the name the user provided.")
    public String createCompany(@ToolParam(description = "Company name exactly as the user provided it.") String name) {
        return client.create("companies", Map.of("name", name));
    }

    @Tool(name = "updateCompany", description = "Update one existing company by id. Pass null for the name when it stays unchanged.")
    public String updateCompany(
            @ToolParam(description = "Company id returned by a previous search or given by the user.") String id,
            @ToolParam(description = "Company name. Null when the name stays unchanged.") String name) {
        if (name == null) {
            throw new IllegalArgumentException("at least one field is required");
        }
        return client.patch("companies", id, Map.of("name", name));
    }

    @Tool(name = "searchOpportunities", description = "Find opportunities when you do not have an id. Pass a Twenty REST filter such as name[eq]:Acme or stage[eq]:NEW. Omit the filter to list the first page.")
    public String searchOpportunities(
            @ToolParam(description = "Twenty REST filter. Example: stage[eq]:NEW. Omit to list without a filter.") String filter,
            @ToolParam(description = "Maximum number of opportunities to return. Use 20 when the user did not ask for a page size.") Integer limit) {
        return client.list("opportunities", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getOpportunity", description = "Load one opportunity by an id you already retrieved from search or from the user. Do not guess the id.")
    public String getOpportunity(@ToolParam(description = "Opportunity id returned by a previous search or given by the user.") String id) {
        return client.get("opportunities", id);
    }

    @Tool(name = "createOpportunity", description = "Create an opportunity after search shows it is missing. Use the stage the user named. Use NEW only when they did not name one.")
    public String createOpportunity(
            @ToolParam(description = "Opportunity name exactly as the user provided it.") String name,
            @ToolParam(description = "Pipeline stage. Use the user's stage, or NEW when they did not name one.") String stage) {
        return client.create("opportunities", Map.of("name", name, "stage", stage == null ? "NEW" : stage));
    }

    @Tool(name = "updateOpportunity", description = "Update one existing opportunity by id. Pass null for any name or stage the user did not ask to change.")
    public String updateOpportunity(
            @ToolParam(description = "Opportunity id returned by a previous search or given by the user.") String id,
            @ToolParam(description = "Opportunity name. Null when this field stays unchanged.") String name,
            @ToolParam(description = "Pipeline stage. Null when this field stays unchanged.") String stage) {
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

    @Tool(name = "searchNotes", description = "Find notes when you do not have an id. Pass a Twenty REST filter such as title[eq]:Kickoff. Omit the filter to list the first page.")
    public String searchNotes(
            @ToolParam(description = "Twenty REST filter. Example: title[eq]:Kickoff. Omit to list without a filter.") String filter,
            @ToolParam(description = "Maximum number of notes to return. Use 20 when the user did not ask for a page size.") Integer limit) {
        return client.list("notes", filter, limit == null ? 20 : limit);
    }

    @Tool(name = "getNote", description = "Load one note by an id you already retrieved from search or from the user. Do not guess the id.")
    public String getNote(@ToolParam(description = "Note id returned by a previous search or given by the user.") String id) {
        return client.get("notes", id);
    }

    @Tool(name = "createNote", description = "Create a note with a title and markdown body. Link at most one of person, company, or opportunity. Do not call this while create-note is blocked for confirmation.")
    public String createNote(
            @ToolParam(description = "Short title that says what the note is about.") String title,
            @ToolParam(description = "Note body in markdown. Include only what the user asked to record.") String markdown,
            @ToolParam(description = "Person id to link, or empty. Set at most one of person, company, or opportunity.") String personId,
            @ToolParam(description = "Company id to link, or empty. Set at most one of person, company, or opportunity.") String companyId,
            @ToolParam(description = "Opportunity id to link, or empty. Set at most one of person, company, or opportunity.") String opportunityId) {
        int targets = targetCount(personId, companyId, opportunityId);
        if (targets > 1) {
            throw new IllegalArgumentException("at most one of personId, companyId, opportunityId");
        }
        String created = client.create("notes", Map.of("title", title, "bodyV2", Map.of("markdown", markdown)));
        if (targets == 1) {
            String noteId = noteId(created);
            if (!noteId.isBlank()) {
                client.createNoteTarget(noteId, personId, companyId, opportunityId);
            }
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
            return "";
        } catch (Exception e) {
            return "";
        }
    }
}
