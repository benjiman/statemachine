package com.benjiweber.statemachine;


import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public interface State<DOMAINSTATETYPE extends State> extends StateGuards<DOMAINSTATETYPE> {

    default void afterTransition(DOMAINSTATETYPE from) {}
    default void beforeTransition(DOMAINSTATETYPE to) {}

    default <T extends DOMAINSTATETYPE> boolean isInState(NextState<T> constructor) {
        return constructor.type().isInstance(this);
    }

    default <T extends DOMAINSTATETYPE> void when(NextState<T> constructor, Consumer<T> use) {
        if (isInState(constructor)) {
            use.accept((T)this);
        }
    }

    interface OrElse<BASETYPE, DESIRED extends BASETYPE> {
        <E extends Exception> DESIRED orElseThrow(Supplier<E> e) throws E;
        BASETYPE ignoreIfInvalid();
        DESIRED unchecked();
    }
    interface RequestTransitionTo<BASETYPE> {
        <DESIRED extends BASETYPE> OrElse<BASETYPE, DESIRED> to(NextState<DESIRED> constructor);
    }
    default <DESIRED extends DOMAINSTATETYPE> OrElse<DOMAINSTATETYPE, DESIRED> tryTransition(NextState<DESIRED> desired) {
        return new OrElse<DOMAINSTATETYPE, DESIRED>() {
            public <E extends Exception> DESIRED orElseThrow(Supplier<E> e) throws E {
                if (canTransitionTo(desired)) {
                    return applyGuards(desired.get());
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
            public DESIRED unchecked() {
                return orElseThrow(InvalidStateTransitionException::new);
            }
        };
    }

    default <U extends StateGuards> U applyGuards(U next) {
        if (this instanceof StateGuards) {
            ((StateGuards)this).beforeTransition(next);
        }
        next.afterTransition(this);
        return next;
    }

    class InvalidStateTransitionException extends RuntimeException {}

    default <U extends DOMAINSTATETYPE> boolean canTransitionTo(NextState<U> toState) {
        return validTransitionTypes().contains(toState.type());
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
