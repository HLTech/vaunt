package com.hltech.vaunt.generator.domain.representation.annotation;

import com.hltech.vaunt.core.domain.model.DestinationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(Consumers.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Consumer {
    String EMPTY = "empty";

    String providerName();
    DestinationType destinationType();
    String destinationName();
    String messageId() default EMPTY;
}
