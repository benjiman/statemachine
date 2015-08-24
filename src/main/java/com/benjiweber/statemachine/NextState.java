package com.benjiweber.statemachine;

import com.benjiweber.typeref.MethodAwareConsumer;
import com.benjiweber.typeref.MethodFinder;

import java.util.Objects;
import java.util.function.Supplier;

public interface NextState<T> extends Supplier<T>, MethodFinder {
    default Class<T> type() {
        try {
            return Objects.equals(method().getName(), "<init>")
                    ? (Class<T>) getContainingClass()
                    : (Class<T>) method().getReturnType();
        } catch (UnableToGuessMethodException e) {
            return (Class<T>) getContainingClass();
        }
    }
}
