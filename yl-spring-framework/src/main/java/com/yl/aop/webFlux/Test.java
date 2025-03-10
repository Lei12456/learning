package com.yl.aop.webFlux;

import reactor.core.publisher.Mono;

public class Test {
    public static void main(String[] args) {
        Mono<String> empty = Mono.empty();
        Mono.just("foo").subscribe();
    }
}
