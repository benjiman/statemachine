package com.benjiweber.statemachine;

import com.benjiweber.typeref.MethodAwareConsumer;
import com.benjiweber.typeref.MethodFinder;

import java.util.function.Supplier;

public interface NextState<T> extends Supplier<T>, MethodFinder {
    default Class<T> type() {
        return (Class<T>) getContainingClass();
    }
}
