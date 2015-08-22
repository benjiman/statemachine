package com.benjiweber.statemachine;

import java.lang.reflect.ParameterizedType;
import java.util.function.Supplier;

@Transition
public interface TransitionTo<T extends StateGuards> {
    interface OneTransition<T> extends Supplier<T> { }
    default T transition(OneTransition<T> constructor) {
        return withGuards(constructor.get());
    }

    default <U extends StateGuards> U withGuards(U next) {
        if (this instanceof StateGuards) {
            ((StateGuards)this).beforeTransition(next);
        }
        next.afterTransition(this);
        return next;
    }

    static boolean isTransition(ParameterizedType type) {
        Class<?> cls = (Class<?>)type.getRawType();
        return cls.getAnnotationsByType(Transition.class).length > 0;
    }
}
