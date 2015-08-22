package com.benjiweber.statemachine;

import java.util.function.Supplier;

@Transition
public interface TriTransitionTo<T extends StateGuards, U extends StateGuards, V extends StateGuards> extends BiTransitionTo<T, U> {
    interface ThreeTransition<T> extends Supplier<T> { }
    default V transition(ThreeTransition<V> constructor) {
        return constructor.get();
    }

}
