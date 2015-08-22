package com.benjiweber.statemachine;

public interface StateGuards<T> {
    void afterTransition(T from);
    void beforeTransition(T to);
}
