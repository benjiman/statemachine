package com.benjiweber.statemachine;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LightExample {

    Switch lightSwitch = new Off();

    public sealed class Switch implements State<Switch> permits On, Off {
        @Override
        public void afterTransition(Switch from) {
            LightExample.this.lightSwitch = Switch.this;
        }
    }
    public final class On extends Switch implements TransitionTo<Off> {}
    public final class Off extends Switch implements TransitionTo<On> {}

    @Test
    public void stateful_switch() {
        assertTrue(lightSwitch instanceof Off);
        lightSwitch.tryTransition(On::new).ignoreIfInvalid();
        assertTrue(lightSwitch instanceof On);
        lightSwitch.tryTransition(Off::new).ignoreIfInvalid();
        assertTrue(lightSwitch instanceof Off);
    }
}
