package com.benjiweber.typeref;

import java.util.function.Consumer;

public interface MethodAwareConsumer<T> extends Consumer<T>, MethodFinder { }
