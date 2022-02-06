package com.benjiweber.example;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class TreeExampleWithPolymorphism {
    sealed interface Tree permits Leaf, Node {
        default <R> Optional<R> match(TriFunction<Integer, Tree, Tree,R> f) {
            return Optional.empty();
        }
    }
    final record Node(int val, Tree left, Tree right) implements Tree {
        public <R> Optional<R> match(TriFunction<Integer, Tree, Tree,R> f) {
            return Optional.of(f.apply(val, left, right));
        }
    }
    final record Leaf() implements Tree {}

    @Test
    public void count_nodes_switch_statement() {
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
        return tree.match(
            (val, left, right) -> 1 + countNodes(left) + countNodes(right)
        ).orElse(0);
    }

    interface TriFunction<T,U,V,R> {
        R apply(T t,U u,V v);
    }

}
