package com.benjiweber.statemachine;

import org.junit.Test;

import java.util.stream.Stream;

import static com.benjiweber.statemachine.RegexExample.APlusB.match;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegexExample {

    sealed interface APlusB extends State<APlusB> permits Start, A, B, NoMatch {
        static APlusB match(String s) {
            return new Start().match(s);
        }
    };
    static final class Start implements APlusB, BiTransitionTo<A,NoMatch> {
        public APlusB match(String s) {
            if (s.length() < 1) return transition(NoMatch::new);
            if (s.charAt(0) == 'A') return transition(A::new).match(s.substring(1));
            return transition(NoMatch::new).match(s.substring(1));
        }
    }
    static final class A implements APlusB, TriTransitionTo<A,B,NoMatch> {
        public APlusB match(String s) {
            if (s.length() < 1) return transition(NoMatch::new);
            if (s.charAt(0) == 'A') return transition(A::new).match(s.substring(1));
            if (s.charAt(0) == 'B') return transition(B::new).match(s.substring(1));
            return transition(NoMatch::new);
        }
    }
    static final class B implements APlusB, Match, TransitionTo<NoMatch> {
        public APlusB match(String s) {
            if (s.length() < 1) return this;
            return transition(NoMatch::new);
        }
    }
    static final class NoMatch implements APlusB {
        public APlusB match(String s) {
            return this;
        }
    }
    interface Match {}

    @Test
    public void regex_match() {
        APlusB match = APlusB.match("AAAAAB");
        assertTrue(match instanceof Match);
        assertFalse(match instanceof NoMatch);
    }

    @Test
    public void regex_invalid_char() {
        APlusB match = APlusB.match("AAACAAB");
        assertTrue(match instanceof NoMatch);
        assertFalse(match instanceof Match);
    }

    @Test
    public void regex_trailing_chars() {
        APlusB match = APlusB.match("AAAAABB");
        assertTrue(match instanceof NoMatch);
        assertFalse(match instanceof Match);
    }

    @Test
    public void regex_minimal_match() {
        APlusB match = APlusB.match("AB");
        assertTrue(match instanceof Match);
        assertFalse(match instanceof NoMatch);
    }

    @Test
    public void regex_long_match() {
        APlusB match = APlusB.match("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB");
        assertTrue(match instanceof Match);
        assertFalse(match instanceof NoMatch);
    }

}
