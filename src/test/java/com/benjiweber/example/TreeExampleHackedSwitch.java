package com.benjiweber.example;

import com.benjiweber.typeref.MethodFinder;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class TreeExampleHackedSwitch {
    sealed interface Tree permits Leaf, Node {}
    final record Node(int val, Tree left, Tree right) implements Tree {}
    final record Leaf() implements Tree {}

    @Test
    public void count_nodes_without_polymorphism_example() {
        var tree =
            new Node(
                5,
                new Node(1, new Leaf(), new Leaf()),
                new Node(
                    3,
                    new Leaf(),
                    new Node(4, new Leaf(), new Leaf())
                )
            );
        assertEquals(4, countNodes(tree));
    }

    static int countNodes(Tree tree) {
        return If.instance(tree,
            (Integer val, Tree left, Tree right) ->
                1 + countNodes(left) + countNodes(right)
        ).orElse(0);
    }

    interface ParamTypeAware extends MethodFinder {
        default Class<?> paramType(int n) {
            return method().getParameters()[(actualParamCount() - expectedParamCount()) + n].getType();
        }
        int expectedParamCount();
        private int actualParamCount() {
            return method().getParameters().length;
        }

    }
    interface MethodAwareBiFunction<L,R,TResult> extends BiFunction<L,R,TResult>, ParamTypeAware {
        default Optional<TResult> tryApply(L left, R right) {
            return acceptsTypes(left, right)
                    ? Optional.ofNullable(apply(left, right))
                    : Optional.empty();
        }

        default boolean acceptsTypes(Object left, Object right) {
            return paramType(0).isAssignableFrom(left.getClass())
                    && paramType(1).isAssignableFrom(right.getClass());
        }
        default int expectedParamCount() { return 2; }
    }

    interface MethodAwareBiConsumer<L,R> extends BiConsumer<L,R>, ParamTypeAware {
        default void tryAccept(L left, R right) {
            if (acceptsTypes(left,right)) {
                accept(left, right);
            }
        }

        default boolean acceptsTypes(Object left, Object right) {
            return paramType(0).isAssignableFrom(left.getClass())
                    && paramType(1).isAssignableFrom(right.getClass());
        }
        default int expectedParamCount() { return 2; }
    }

    interface TriFunction<T,U,V,R> {
        R apply(T t, U u, V v);
    }
    interface MethodAwareTriFunction<T,U,V,TResult> extends TriFunction<T,U,V,TResult>, ParamTypeAware {
        default Optional<TResult> tryApply(T one, U two, V three) {
            return acceptsTypes(one, two, three)
                    ? Optional.ofNullable(apply(one, two, three))
                    : Optional.empty();
        }

        default boolean acceptsTypes(Object one, Object two, Object three) {
            return paramType(0).isAssignableFrom(one.getClass())
                    && paramType(1).isAssignableFrom(two.getClass())
                    && paramType(2).isAssignableFrom(three.getClass());
        }
        default int expectedParamCount() { return 3; }
    }

    interface TriConsumer<T,U,V> {
        void accept(T t, U u, V v);
    }
    interface MethodAwareTriConsumer<T,U,V> extends TriConsumer<T,U,V>, ParamTypeAware {
        default void tryAccept(T one, U two, V three) {
            if (acceptsTypes(one, two, three)) {
                accept(one, two, three);
            }
        }

        default boolean acceptsTypes(Object one, Object two, Object three) {
            return paramType(0).isAssignableFrom(one.getClass())
                    && paramType(1).isAssignableFrom(two.getClass())
                    && paramType(2).isAssignableFrom(three.getClass());
        }
        default int expectedParamCount() { return 3; }
    }
    abstract static class Match<TResult> {
        public final Match<TResult> If = this;
        public abstract <L,R> TResult instance(Object toMatch, MethodAwareBiFunction<L,R,TResult> action);
        public abstract <T,U,V> TResult instance(Object toMatch, MethodAwareTriFunction<T,U,V,TResult> action);
    }
    interface If {
        static <TResult> Match<TResult> withFallback(TResult defaultResult) {
            return new Match<>() {
                public <L, R> TResult instance(Object toMatch, MethodAwareBiFunction<L, R, TResult> action) {
                    return TreeExampleHackedSwitch.If.instance(toMatch, action).orElse(defaultResult);
                }

                public <T, U, V> TResult instance(Object toMatch, MethodAwareTriFunction<T, U, V, TResult> action) {
                    return TreeExampleHackedSwitch.If.instance(toMatch, action).orElse(defaultResult);
                }
            };
        }
        static <L, R> void instance(Object o, MethodAwareBiConsumer<L, R> action) {
            if (o instanceof Record r) {
                if (r.getClass().getRecordComponents().length < 2) {
                    return;
                }
                action.tryAccept((L) nthComponent(0, r), (R) nthComponent(1, r));
            }
        }
        static <T,U,V> void instance(Object o, MethodAwareTriConsumer<T,U,V> action) {
            if (o instanceof Record r) {
                if (r.getClass().getRecordComponents().length < 3) {
                    return;
                }
                action.tryAccept((T) nthComponent(0, r), (U) nthComponent(1, r), (V) nthComponent(2, r));
            }
        }
        static <L, R, TResult> Optional<TResult> instance(Object o, MethodAwareBiFunction<L, R, TResult> action) {
            if (o instanceof Record r) {
                if (r.getClass().getRecordComponents().length < 2) {
                    return Optional.empty();
                }
                return action.tryApply((L) nthComponent(0, r), (R) nthComponent(1, r));
            }
            return Optional.empty();
        }
        static <T,U,V,TResult> Optional<TResult> instance(Object o, MethodAwareTriFunction<T,U,V,TResult> action) {
            if (o instanceof Record r) {
                if (r.getClass().getRecordComponents().length < 3) {
                    return Optional.empty();
                }
                return action.tryApply((T) nthComponent(0, r), (U) nthComponent(1, r), (V) nthComponent(2, r));
            }
            return Optional.empty();
        }
        private static Object nthComponent(int n, Record r)  {
            try {
                return r.getClass().getRecordComponents()[n].getAccessor().invoke(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
