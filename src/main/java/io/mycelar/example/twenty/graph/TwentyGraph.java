package io.mycelar.example.twenty.graph;

import io.mycelar.core.capability.CapabilityDescriptor;
import io.mycelar.core.capability.CapabilityId;
import io.mycelar.core.capability.GatewayId;
import io.mycelar.core.constraint.Constraint;
import io.mycelar.core.constraint.ConstraintId;
import io.mycelar.core.context.CapsuleType;
import io.mycelar.core.context.ContextCapsule;
import io.mycelar.core.graph.CapsuleId;
import io.mycelar.core.graph.EdgeId;
import io.mycelar.core.graph.EdgeMetadata;
import io.mycelar.core.graph.EdgeType;
import io.mycelar.core.graph.GraphEdge;
import io.mycelar.core.graph.GraphNode;
import io.mycelar.core.graph.MycelarGraph;
import io.mycelar.core.graph.NodeId;
import io.mycelar.core.graph.NodeMetadata;
import io.mycelar.core.graph.NodeType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TwentyGraph {

    private static final GatewayId GATEWAY = new GatewayId("twenty-rest");

    private TwentyGraph() {}

    public static MycelarGraph graph() {
        return MycelarGraph.builder()
                .node(
                        domain(
                                "twenty",
                                "Twenty CRM",
                                List.of("twenty", "crm"),
                                capsule(
                                        "cap-twenty",
                                        CapsuleType.INSTRUCTION,
                                        "Answer from this Twenty CRM workspace. Call only capabilities listed as available. Never invent ids or records; if a lookup is empty, say so.",
                                        10)))
                .node(
                        resource(
                                "people",
                                "people",
                                List.of("people", "person", "contact", "contacts", "email"),
                                capsule(
                                        "cap-people",
                                        CapsuleType.DOMAIN_CONTEXT,
                                        "A person is a contact with name.firstName, name.lastName, and emails.primaryEmail. Search before creating so you do not duplicate an existing contact.",
                                        20)))
                .node(
                        resource(
                                "companies",
                                "companies",
                                List.of("company", "companies", "account", "accounts", "organization"),
                                capsule(
                                        "cap-companies",
                                        CapsuleType.DOMAIN_CONTEXT,
                                        "A company is an account identified by its name. Search by name before creating another company with the same name.",
                                        20)))
                .node(
                        resource(
                                "opportunities",
                                "opportunities",
                                List.of("opportunity", "opportunities", "deal", "deals", "pipeline", "stage"),
                                capsule(
                                        "cap-opportunities",
                                        CapsuleType.DOMAIN_CONTEXT,
                                        "An opportunity is a deal with a name and a stage, such as NEW. Search before creating. On update, change only the name or stage the user named.",
                                        20)))
                .node(
                        resource(
                                "notes",
                                "notes",
                                List.of("note", "notes", "comment", "memo"),
                                capsule(
                                        "cap-notes",
                                        CapsuleType.DOMAIN_CONTEXT,
                                        "A note has a title and a markdown body. Attach it to at most one target: a person, a company, or an opportunity.",
                                        20)))
                .node(group("people-read", "read", "find people", List.of("people", "person"), "Look up people. Use search when you have a name or email and no id. Use get only with a person id you already retrieved."))
                .node(group("people-write", "write", "write people", List.of("people", "person", "email"), "Change people. Create a person only after search shows they are missing. On update, send only the name or email the user asked to change."))
                .node(group("people-context", "context", "people context", List.of("people", "person"), "When you mention a person, use the first name, last name, and primary email returned by the tool. Do not guess a missing email."))
                .node(group("companies-read", "read", "read companies", List.of("company", "companies"), "Look up companies. Use search when you have a name and no id. Use get only with a company id you already retrieved."))
                .node(group("companies-write", "write", "write companies", List.of("company", "companies"), "Change companies. Create a company only after search shows that name is missing. On update, change only the name the user gave."))
                .node(group("opportunities-read", "read", "read opportunities", List.of("opportunity", "deal"), "Look up opportunities. Use search when you have a deal name or stage and no id. Use get only with an opportunity id you already retrieved."))
                .node(group("opportunities-write", "write", "write opportunities", List.of("opportunity", "deal", "stage"), "Change opportunities. Create one only after search shows it is missing. On update, change only the name or stage the user gave."))
                .node(group("notes-read", "read", "read notes", List.of("note", "notes"), "Look up notes. Use search when the user described a note and you have no id. Use get only with a note id you already retrieved."))
                .node(group("notes-write", "write", "create notes", List.of("note", "notes"), "Create a note with a title and a markdown body. If this capability is blocked, say what you would write and do not call the tool on this turn."))
                .node(group("notes-context", "context", "notes context", List.of("note", "notes"), "Link a note to one person, one company, or one opportunity, never more than one. Creating a note is a write, so wait when confirmation is required."))
                .node(capability("search-people", "searchPeople", "Find people by name.firstName or emails.primaryEmail.", List.of("search", "find", "people", "person", "named")))
                .node(capability("get-person", "getPerson", "Load one person by id.", List.of("get", "read", "person", "people")))
                .node(capability("create-person", "createPerson", "Create a person with first name, last name, and primary email.", List.of("create", "add", "new", "person", "people")))
                .node(capability("update-person", "updatePerson", "Update a person's name or email by id.", List.of("update", "patch", "person", "people", "email")))
                .node(capability("search-companies", "searchCompanies", "Find companies by name.", List.of("search", "find", "company", "companies")))
                .node(capability("get-company", "getCompany", "Load one company by id.", List.of("get", "read", "company")))
                .node(capability("create-company", "createCompany", "Create a company by name.", List.of("create", "add", "new", "company", "named")))
                .node(capability("update-company", "updateCompany", "Update a company's name by id.", List.of("update", "patch", "company")))
                .node(capability("search-opportunities", "searchOpportunities", "Find opportunities by name or stage.", List.of("search", "find", "opportunity", "deal")))
                .node(capability("get-opportunity", "getOpportunity", "Load one opportunity by id.", List.of("get", "read", "opportunity", "deal")))
                .node(capability("create-opportunity", "createOpportunity", "Create an opportunity with a name and a stage. A missing stage is NEW.", List.of("create", "add", "new", "opportunity", "deal")))
                .node(capability("update-opportunity", "updateOpportunity", "Update an opportunity's name or stage by id.", List.of("update", "patch", "opportunity", "deal", "stage")))
                .node(capability("search-notes", "searchNotes", "Find notes by title or body.", List.of("search", "note", "notes")))
                .node(capability("get-note", "getNote", "Load one note by id.", List.of("get", "read", "note")))
                .node(capability("create-note", "createNote", "Create a note with a title and markdown body, linked to at most one target.", List.of("create", "add", "note", "notes")))
                .node(
                        constraint(
                                "confirm-before-send",
                                "confirm-before-send",
                                "Block note creation until this turn has confirmed set to true. Tell the user the title and body you would save, then stop."))
                .node(
                        context(
                                "person-profile",
                                "person profile",
                                List.of("profile"),
                                capsule(
                                        "cap-person-profile",
                                        CapsuleType.CAPABILITY_CONTEXT,
                                        "Read a person from the tool result: name.firstName, name.lastName, and emails.primaryEmail. Do not fill in a missing email.",
                                        5)))
                .node(
                        context(
                                "communication-policy",
                                "communication policy",
                                List.of("policy"),
                                capsule(
                                        "cap-policy",
                                        CapsuleType.CONSTRAINT,
                                        "Creating a note writes to the CRM. If create-note is blocked with REQUIRE_CONFIRMATION, tell the user the title and body you would save and stop. Call createNote only after they confirm.",
                                        5)))
                .edge(contains("twenty-people", "twenty", "people"))
                .edge(contains("twenty-companies", "twenty", "companies"))
                .edge(contains("twenty-opportunities", "twenty", "opportunities"))
                .edge(contains("twenty-notes", "twenty", "notes"))
                .edge(contains("people-to-read", "people", "people-read"))
                .edge(contains("people-to-write", "people", "people-write"))
                .edge(contains("people-to-context", "people", "people-context"))
                .edge(contains("people-read-search", "people-read", "search-people"))
                .edge(contains("people-read-get", "people-read", "get-person"))
                .edge(contains("people-write-create", "people-write", "create-person"))
                .edge(contains("people-write-update", "people-write", "update-person"))
                .edge(contains("people-context-profile", "people-context", "person-profile"))
                .edge(contains("companies-to-read", "companies", "companies-read"))
                .edge(contains("companies-to-write", "companies", "companies-write"))
                .edge(contains("companies-read-search", "companies-read", "search-companies"))
                .edge(contains("companies-read-get", "companies-read", "get-company"))
                .edge(contains("companies-write-create", "companies-write", "create-company"))
                .edge(contains("companies-write-update", "companies-write", "update-company"))
                .edge(contains("opportunities-to-read", "opportunities", "opportunities-read"))
                .edge(contains("opportunities-to-write", "opportunities", "opportunities-write"))
                .edge(contains("opportunities-read-search", "opportunities-read", "search-opportunities"))
                .edge(contains("opportunities-read-get", "opportunities-read", "get-opportunity"))
                .edge(contains("opportunities-write-create", "opportunities-write", "create-opportunity"))
                .edge(contains("opportunities-write-update", "opportunities-write", "update-opportunity"))
                .edge(contains("notes-to-read", "notes", "notes-read"))
                .edge(contains("notes-to-write", "notes", "notes-write"))
                .edge(contains("notes-to-context", "notes", "notes-context"))
                .edge(contains("notes-read-search", "notes-read", "search-notes"))
                .edge(contains("notes-read-get", "notes-read", "get-note"))
                .edge(contains("notes-write-create", "notes-write", "create-note"))
                .edge(contains("notes-context-policy", "notes-context", "communication-policy"))
                .edge(requires("create-note-confirm", "create-note", "confirm-before-send"))
                .edge(expands("people-profile", "people", "person-profile"))
                .edge(expands("notes-policy", "notes", "communication-policy"))
                .edge(relates("people-read-write", "people-read", "people-write"))
                .edge(relates("companies-read-write", "companies-read", "companies-write"))
                .edge(relates("opportunities-read-write", "opportunities-read", "opportunities-write"))
                .edge(relates("people-read-notes-write", "people-read", "notes-write"))
                .build();
    }

    private static GraphNode domain(String id, String label, List<String> keywords, ContextCapsule capsule) {
        return node(id, NodeType.DOMAIN, label, label, keywords, List.of(capsule), Optional.empty(), Optional.empty());
    }

    private static GraphNode resource(String id, String label, List<String> keywords, ContextCapsule capsule) {
        return node(id, NodeType.RESOURCE, label, label, keywords, List.of(capsule), Optional.empty(), Optional.empty());
    }

    private static GraphNode group(String id, String label, String description, List<String> keywords, String text) {
        return node(
                id,
                NodeType.RESOURCE,
                label,
                description,
                keywords,
                List.of(capsule("cap-" + id, CapsuleType.DOMAIN_CONTEXT, text, 15)),
                Optional.empty(),
                Optional.empty());
    }

    private static GraphNode context(String id, String label, List<String> keywords, ContextCapsule capsule) {
        return node(id, NodeType.CONTEXT, label, label, keywords, List.of(capsule), Optional.empty(), Optional.empty());
    }

    private static GraphNode capability(String id, String operationId, String description, List<String> keywords) {
        return node(
                id,
                NodeType.CAPABILITY,
                id,
                id,
                keywords,
                List.of(),
                Optional.of(
                        new CapabilityDescriptor(
                                new CapabilityId(id),
                                operationId,
                                description,
                                GATEWAY,
                                operationId,
                                Map.of())),
                Optional.empty());
    }

    private static GraphNode constraint(String id, String type, String description) {
        return node(
                id,
                NodeType.CONSTRAINT,
                id,
                id,
                List.of(),
                List.of(),
                Optional.empty(),
                Optional.of(new Constraint(new ConstraintId(id), type, description, Map.of())));
    }

    private static GraphNode node(
            String id,
            NodeType type,
            String label,
            String description,
            List<String> keywords,
            List<ContextCapsule> capsules,
            Optional<CapabilityDescriptor> capability,
            Optional<Constraint> constraint) {
        return new GraphNode(
                new NodeId(id),
                type,
                new NodeMetadata(label, description, List.of(), keywords, Map.of()),
                capsules,
                capability.orElse(null),
                constraint.orElse(null));
    }

    private static ContextCapsule capsule(String id, CapsuleType type, String text, int priority) {
        return new ContextCapsule(new CapsuleId(id), type, text, priority, 1.0, null);
    }

    private static GraphEdge contains(String edgeId, String source, String target) {
        return edge(edgeId, source, target, EdgeType.CONTAINS);
    }

    private static GraphEdge requires(String edgeId, String source, String target) {
        return edge(edgeId, source, target, EdgeType.REQUIRES);
    }

    private static GraphEdge relates(String edgeId, String source, String target) {
        return edge(edgeId, source, target, EdgeType.RELATES_TO);
    }

    private static GraphEdge expands(String edgeId, String source, String target) {
        return edge(edgeId, source, target, EdgeType.EXPANDS_TO);
    }

    private static GraphEdge edge(String edgeId, String source, String target, EdgeType type) {
        return new GraphEdge(
                new EdgeId(edgeId),
                new NodeId(source),
                new NodeId(target),
                type,
                1.0,
                new EdgeMetadata(Map.of()));
    }
}
