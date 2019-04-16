package com.stackoverflow.demo.springstatemachinetriggers;

import org.awaitility.Duration;
import org.junit.Test;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import static org.awaitility.Awaitility.await;


public class TestTriggersDelay {

    @Test
    public void testTriggersWithDelay() throws Exception {
        StateMachine<String, String> machine = buildMachine();
        StateMachineTestPlan<String, String> plan =
                StateMachineTestPlanBuilder.<String, String>builder()
                        .defaultAwaitTime(2)
                        .stateMachine(machine)
                        .step()
                        .expectStates("SI")
                        .and()

                        .step()
                        .sendEvent("E")
                        .and()
                        .build();
        plan.test();
        await().atMost(Duration.ONE_MINUTE).until(() ->
                machine.getExtendedState().getVariables().getOrDefault("myKey", "").equals("Action Happened"));
    }

    private StateMachine<String, String> buildMachine() throws Exception {
        StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

        builder.configureConfiguration()
                .withConfiguration()
                .autoStartup(true);

        builder.configureStates()
                .withStates()
                .initial("SI")
                .state("A")
                .end("SF");
        builder.configureTransitions()
                .withExternal()
                .source("SI")
                .target("A")
                .event("E")
                .and()

                .withInternal()
                .source("A")
                .action(this::myAction)
                .timerOnce(5000);

        return builder.build();
    }

    private void myAction(StateContext<String, String> context) {
        context.getExtendedState().getVariables().putIfAbsent("myKey", "Action Happened");
        System.out.println("Internal action from A to A");
    }

}
