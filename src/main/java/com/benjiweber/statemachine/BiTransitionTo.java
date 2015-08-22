package com.benjiweber.statemachine;

import java.util.function.Supplier;

@Transition
public interface BiTransitionTo<T extends StateGuards, U extends StateGuards> extends TransitionTo<T> {
    interface TwoTransition<T> extends Supplier<T> { }
    default U transition(TwoTransition<U> constructor) {
        return withGuards(constructor.get());
    }
}
