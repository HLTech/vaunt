package com.hltech.vaunt.generator.domain.annotation;

import com.hltech.vaunt.generator.domain.representation.DestinationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Provider {

    DestinationType destinationType();
    String destinationName();
}
