package com.benjiweber.statemachine;


import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public interface State<DOMAINSTATETYPE> extends StateGuards<DOMAINSTATETYPE> {

    @Override default void afterTransition(DOMAINSTATETYPE from) {}
    default void beforeTransition(DOMAINSTATETYPE to) {}

    default <T extends DOMAINSTATETYPE> boolean isInState(Supplier<T> constructor) {
        return constructor.get().getClass().isInstance(this);
    }

    default <T extends DOMAINSTATETYPE> void when(Supplier<T> constructor, Consumer<T> use) {
        if (isInState(constructor)) {
            use.accept((T)this);
        }
    }

    interface OrElse<BASETYPE, ORIGINAL extends BASETYPE,DESIRED extends BASETYPE> {
        <E extends Exception> DESIRED orElseThrow(Supplier<E> e) throws E;
        BASETYPE ignoreIfInvalid();
        DESIRED unchecked();
    }
    interface RequestTransitionTo<BASETYPE, ORIGINAL extends BASETYPE> {
        <DESIRED extends BASETYPE> OrElse<BASETYPE, ORIGINAL, DESIRED> to(Supplier<DESIRED> constructor);
    }
    default <ORIGINAL extends DOMAINSTATETYPE> RequestTransitionTo<DOMAINSTATETYPE, ORIGINAL> transitionFrom(Supplier<ORIGINAL> fromState) {
        return new RequestTransitionTo<DOMAINSTATETYPE, ORIGINAL>() {
            public <DESIRED extends DOMAINSTATETYPE> OrElse<DOMAINSTATETYPE, ORIGINAL, DESIRED> to(Supplier<DESIRED> desired) {
                return new OrElse<DOMAINSTATETYPE, ORIGINAL, DESIRED>() {
                    public <E extends Exception> DESIRED orElseThrow(Supplier<E> e) throws E {
                        if (isInState(fromState) && canTransitionTo(desired)) {
                            return desired.get();
                        }

                        throw e.get();
                    }
                    public DOMAINSTATETYPE ignoreIfInvalid() {
                        try {
                            return unchecked();
                        } catch (InvalidStateTransitionException e) {
                            return (DOMAINSTATETYPE)State.this;
                        }
                    }

                    @Override
                    public DESIRED unchecked() {
                        return orElseThrow(InvalidStateTransitionException::new);
                    }
                };
            }
        };
    }


    class InvalidStateTransitionException extends RuntimeException {}

    default <U extends DOMAINSTATETYPE> boolean canTransitionTo(Supplier<U> toState) {
        return validTransitionTypes().contains(toState.get().getClass());
    }

    default List<Class<?>> validTransitionTypes() {
        return asList(getClass().getGenericInterfaces())
            .stream()
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .filter(TransitionTo::isTransition)
            .flatMap(type -> asList(type.getActualTypeArguments()).stream())
            .map(type -> (Class<?>) type)
            .collect(toList());
    }

}
