package com.benjiweber.statemachine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrafficLightExample {

    interface TrafficLight extends State<TrafficLight> {}
    static class Green implements TrafficLight, TransitionTo<SolidAmber> {}
    static class SolidAmber implements TrafficLight, TransitionTo<Red> {}
    static class Red implements TrafficLight, TransitionTo<FlashingAmber> {}
    static class FlashingAmber implements TrafficLight, TransitionTo<Green> {}


    @Test
    public void traffic_light_typechecked_example() {
        Green signal = new Green();
        //uncomment a transition and it will fail to compile.
        signal = signal
            .transition(SolidAmber::new)
            .transition(Red::new)
            .transition(FlashingAmber::new)
            .transition(Green::new);
    }

    @Test
    public void runtime_transitions_possible() {
        TrafficLight light = new Green();
        light = light
            .transitionFrom(Green::new)
            .to(SolidAmber::new)
            .unchecked();

        assertTrue(light instanceof SolidAmber);
    }


    @Test(expected = State.InvalidStateTransitionException.class)
    public void runtime_transitions_throw_exception_when_not_possible() {
        TrafficLight light = new Green();
        light = light
                .transitionFrom(Green::new)
                .to(Red::new)
                .unchecked();
    }

    @Test
    public void runtime_transitions_can_ignore_errors() {
        TrafficLight light = new Green();
        light = light
                .transitionFrom(Green::new)
                .to(Red::new)
                .ignoreIfInvalid();

        assertFalse(light instanceof Red);
        assertTrue(light instanceof Green);
    }

}
