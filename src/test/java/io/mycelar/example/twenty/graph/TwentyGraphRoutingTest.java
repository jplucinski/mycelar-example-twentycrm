package io.mycelar.example.twenty.graph;

import static org.assertj.core.api.Assertions.assertThat;

import io.mycelar.core.capability.CapabilityId;
import io.mycelar.core.constraint.ConstraintDecision;
import io.mycelar.core.graph.NodeId;
import io.mycelar.core.routing.MycelarRuntime;
import io.mycelar.core.routing.RouteCandidate;
import io.mycelar.core.routing.RouteCandidateMerger;
import io.mycelar.core.routing.RouteRequest;
import io.mycelar.core.routing.RoutingContext;
import io.mycelar.example.twenty.constraint.ConfirmBeforeSendEvaluator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwentyGraphRoutingTest {

    private MycelarRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime =
                MycelarRuntime.builder()
                        .graph(TwentyGraph.graph())
                        .constraintEvaluators(List.of(new ConfirmBeforeSendEvaluator()))
                        .build();
    }

    private RouteCandidate primary(String intent, RoutingContext ctx) {
        return runtime.route(
                        RouteRequest.builder().intent(intent).context(ctx).topK(1).build())
                .candidates()
                .getFirst();
    }

    private RouteCandidate merged(String intent, RoutingContext ctx) {
        return RouteCandidateMerger.merge(
                runtime.route(RouteRequest.builder().intent(intent).context(ctx).topK(2).build())
                        .candidates());
    }

    @Test
    void peopleSearchIntentDoesNotExposeCreateNote() {
        RouteCandidate c = primary("search people named Ada", RoutingContext.empty());
        assertThat(c.path().nodeIds()).extracting(NodeId::value).contains("people-read");
        assertThat(c.capabilityScope().allowed()).contains(new CapabilityId("search-people"));
        assertThat(c.capabilityScope().allowed()).doesNotContain(new CapabilityId("create-note"));
        assertThat(c.capabilityScope().allowed()).doesNotContain(new CapabilityId("search-companies"));
    }

    @Test
    void createCompanyIntent() {
        RouteCandidate c = primary("create a company named Acme", RoutingContext.empty());
        assertThat(c.path().nodeIds()).extracting(NodeId::value).contains("companies-write");
        assertThat(c.capabilityScope().allowed()).contains(new CapabilityId("create-company"));
    }

    @Test
    void updateOpportunityIntent() {
        RouteCandidate c = primary("update the opportunity stage", RoutingContext.empty());
        assertThat(c.path().nodeIds()).extracting(NodeId::value).contains("opportunities-write");
        assertThat(c.capabilityScope().allowed()).contains(new CapabilityId("update-opportunity"));
    }

    @Test
    void createNoteRequiresConfirmation() {
        RouteCandidate blocked = primary("create a note about the deal", RoutingContext.empty());
        assertThat(blocked.path().nodeIds()).extracting(NodeId::value).contains("notes-write");
        assertThat(blocked.capabilityScope().blocked())
                .anyMatch(
                        b ->
                                b.capabilityId().equals(new CapabilityId("create-note"))
                                        && b.decision() == ConstraintDecision.REQUIRE_CONFIRMATION);
        assertThat(blocked.capabilityScope().allowed()).doesNotContain(new CapabilityId("create-note"));

        RouteCandidate allowed =
                primary("create a note about the deal", RoutingContext.of(Map.of("confirmed", true)));
        assertThat(allowed.capabilityScope().allowed()).contains(new CapabilityId("create-note"));
        assertThat(allowed.capabilityScope().blocked()).isEmpty();
    }

    @Test
    void searchPeopleNamedAdaKeepsReadTools() {
        RouteCandidate c = merged("search people named Ada", RoutingContext.empty());
        assertThat(c.capabilityScope().allowed())
                .contains(new CapabilityId("search-people"), new CapabilityId("get-person"));
        assertThat(c.capabilityScope().allowed())
                .doesNotContain(
                        new CapabilityId("create-note"),
                        new CapabilityId("search-companies"),
                        new CapabilityId("update-person"));
    }

    @Test
    void updateOpportunityStageDoesNotExposeUpdatePerson() {
        RouteCandidate c = merged("update the opportunity stage", RoutingContext.empty());
        assertThat(c.capabilityScope().allowed()).contains(new CapabilityId("update-opportunity"));
        assertThat(c.capabilityScope().allowed()).doesNotContain(new CapabilityId("update-person"));
    }

    @Test
    void findAdaAndUpdateEmailMergesPeopleReadAndWrite() {
        RouteCandidate c = merged("find Ada and update her email", RoutingContext.empty());
        assertThat(c.capabilityScope().allowed())
                .contains(new CapabilityId("search-people"), new CapabilityId("update-person"));
        assertThat(c.capabilityScope().allowed()).doesNotContain(new CapabilityId("create-note"));
    }

    @Test
    void findAdaAndAddNoteRequiresConfirmation() {
        RouteCandidate blocked =
                merged("find Ada and add a note", RoutingContext.of(Map.of("confirmed", false)));
        assertThat(blocked.capabilityScope().allowed()).contains(new CapabilityId("search-people"));
        assertThat(blocked.capabilityScope().blocked())
                .anyMatch(
                        b ->
                                b.capabilityId().equals(new CapabilityId("create-note"))
                                        && b.decision() == ConstraintDecision.REQUIRE_CONFIRMATION);
        assertThat(blocked.capabilityScope().allowed()).doesNotContain(new CapabilityId("create-note"));

        RouteCandidate allowed =
                merged("find Ada and add a note", RoutingContext.of(Map.of("confirmed", true)));
        assertThat(allowed.capabilityScope().allowed()).contains(new CapabilityId("create-note"));
        assertThat(allowed.capabilityScope().blocked()).isEmpty();
    }
}
