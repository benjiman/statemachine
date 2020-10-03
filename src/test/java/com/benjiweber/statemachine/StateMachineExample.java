package com.benjiweber.statemachine;

import com.benjiweber.statemachine.State.InvalidStateTransitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static com.benjiweber.statemachine.StateMachineExample.Customer.customer;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineExample {

    sealed interface OrderStatus extends State<OrderStatus> permits Pending, CheckingOut, Purchased, Shipped, Cancelled, Failed, Refunded {
        default void notifyProgress(Customer customer, EmailSender sender) {}
    }
    static final class Pending implements OrderStatus, BiTransitionTo<CheckingOut, Cancelled> {}
    static final class CheckingOut implements OrderStatus, BiTransitionTo<Purchased, Cancelled> {}
    static final class Purchased implements OrderStatus, BiTransitionTo<Shipped, Failed> {
        public void notifyProgress(Customer customer, EmailSender emailSender) {
            emailSender.sendEmail("fulfillment@mycompany.com", "Customer order pending");
            emailSender.sendEmail(customer.email(), "Your order is on its way");
        }
    }

    static final class Shipped implements OrderStatus, TransitionTo<Refunded> {}
    static final class Cancelled implements OrderStatus {
        public void notifyProgress(Customer customer, EmailSender emailSender) {
            emailSender.sendEmail("fulfillment@mycompany.com", "Customer order cancelled");
            emailSender.sendEmail(customer.email(), "Your order has been cancelled");
        }
    }
    final class Failed implements OrderStatus {
        @Override
        public void afterTransition(OrderStatus from) {
            failureLog.warning("Oh bother! failed from " + from.getClass().getSimpleName());
        }
    }
    static final class Refunded implements OrderStatus {}

    @Test
    public void typesafe_statemachine_example() {
        Pending pending = new Pending();
        CheckingOut checkingOut = pending.transition(CheckingOut::new);
        Cancelled cancelled = pending.transition(Cancelled::new);
        // cancelled.transition(... compile failure, method doesn't exist
        Purchased purchased = checkingOut.transition(Purchased::new);

        // checkingOut.transition(Refunded::new) compile failure. Refunded is not a valid transition
    }

    @Test
    public void check_if_in_state() {
        OrderStatus pending = new Pending();
        assertTrue(pending.isInState(Pending::new));
        assertFalse(pending.isInState(Cancelled::new));
    }

    @Test
    public void we_can_match_on_state_type() {
        AtomicBoolean wasPending = new AtomicBoolean();
        wasPending.set(false);

        OrderStatus state = new Pending();
        state.when(CheckingOut::new, checkingOut -> {
            fail("state should not be checking out");
        });
        state.when(Pending::new, pending -> {
            pending.transition(CheckingOut::new);
            wasPending.set(true);
        });

        assertTrue(wasPending.get());
    }

    @Test
    public void runtime_checked_transition() {
        OrderStatus state = new Pending();
        assertTrue(state instanceof Pending);
        state = state
            .tryTransition(CheckingOut::new)
            .unchecked();
        assertTrue(state instanceof CheckingOut);
    }

    @Test
    public void runtime_checked_transition_ignoring_failure() {
        OrderStatus state = new Pending();
        assertTrue(state instanceof Pending);
        state = state
            .tryTransition(Refunded::new)
            .ignoreIfInvalid();
        assertFalse(state instanceof Refunded);
        assertTrue(state instanceof Pending);
    }

    @Test(expected = InvalidStateTransitionException.class)
    public void runtime_checked_transition_throwing_when_invalid() {
        OrderStatus state = new Pending();
        assertTrue(state instanceof Pending);
        state = state
            .tryTransition(Refunded::new)
            .unchecked();
    }

    @Test(expected = OhNoes.class)
    public void runtime_checked_transition_throwing_requested_exception_when_invalid() throws OhNoes {
        OrderStatus state = new Pending();
        assertTrue(state instanceof Pending);
        state = state
            .tryTransition(Refunded::new)
            .orElseThrow(OhNoes::new);
    }

    @Test
    public void behaviour_on_states() throws OhNoes {
        Customer customer = customer("spam@example.com");

        OrderStatus state = new Pending();
        state.notifyProgress(customer, emailSender);

        verifyZeroInteractions(emailSender);

        state = state
            .tryTransition(CheckingOut::new)
            .orElseThrow(OhNoes::new)
            .transition(Purchased::new);

        state.notifyProgress(customer, emailSender);

        verify(emailSender).sendEmail(customer.email(), "Your order is on its way");
        verify(emailSender).sendEmail("fulfillment@mycompany.com", "Customer order pending");
    }

    @Test
    public void guard_transition_and_capture_state_from_containing_class() throws OhNoes {
        OrderStatus state = new Pending();
        Purchased purchased = state
            .tryTransition(CheckingOut::new)
            .orElseThrow(OhNoes::new)
            .transition(Purchased::new);

        verifyZeroInteractions(failureLog);

        purchased.transition(Failed::new);

        verify(failureLog).warning("Oh bother! failed from Purchased");
    }

    @Test
    public void finding_valid_transitions_at_runtime() {
        Pending pending = new Pending();
        assertEquals(
                asList(CheckingOut.class, Cancelled.class),
                pending.validTransitionTypes()
        );
    }


    static class OhNoes extends Exception {}

    interface Customer {
        String email();
        static Customer customer(String email) { return () -> email; }
    }
    interface EmailSender  {
        void sendEmail(String to, String message);
    }
    @Mock EmailSender emailSender;
    @Mock Logger failureLog;

}