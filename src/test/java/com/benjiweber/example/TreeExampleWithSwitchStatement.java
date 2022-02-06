package com.benjiweber.example;

import com.benjiweber.typeref.MethodFinder;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class TreeExampleWithSwitchStatement {
    sealed interface Tree permits Leaf, Node { }
    final record Node(int val, Tree left, Tree right) implements Tree { }
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
        return switch (tree) {
            case Leaf l -> 0;
            case Node n -> 1 + countNodes(n.left()) + countNodes(n.right());
        };
    }

}
