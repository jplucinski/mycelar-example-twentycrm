package io.mycelar.example.twenty.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import io.mycelar.core.constraint.Constraint;
import io.mycelar.core.constraint.ConstraintDecision;
import io.mycelar.core.constraint.ConstraintId;
import io.mycelar.core.routing.RoutingContext;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfirmBeforeSendEvaluatorTest {

    private final ConfirmBeforeSendEvaluator evaluator = new ConfirmBeforeSendEvaluator();
    private final Constraint constraint =
            new Constraint(
                    new ConstraintId("confirm-before-send"),
                    "confirm-before-send",
                    "Require confirmation before creating a note",
                    Map.of());

    @Test
    void allowOnlyWhenConfirmedTrue() {
        assertThat(evaluator.supports(constraint)).isTrue();
        assertThat(evaluator.evaluate(constraint, RoutingContext.empty()))
                .isEqualTo(ConstraintDecision.REQUIRE_CONFIRMATION);
        assertThat(evaluator.evaluate(constraint, RoutingContext.of(Map.of("confirmed", true))))
                .isEqualTo(ConstraintDecision.ALLOW);
        assertThat(evaluator.evaluate(constraint, RoutingContext.of(Map.of("confirmed", "true"))))
                .isEqualTo(ConstraintDecision.REQUIRE_CONFIRMATION);
    }
}
