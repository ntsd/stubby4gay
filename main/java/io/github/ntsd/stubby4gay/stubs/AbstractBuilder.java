package io.github.ntsd.stubby4gay.stubs;


import io.github.ntsd.stubby4gay.yaml.ConfigurableYAMLProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.ntsd.generics.TypeSafeConverter.as;

public abstract class AbstractBuilder<T extends ReflectableStub> {

    final Map<ConfigurableYAMLProperty, Object> fieldNameAndValues;

    AbstractBuilder() {
        this.fieldNameAndValues = new HashMap<>();
    }

    <E> E getStaged(final Class<E> clazzor, final ConfigurableYAMLProperty property, E defaultValue) {
        return fieldNameAndValues.containsKey(property) ? as(clazzor, fieldNameAndValues.get(property)) : defaultValue;
    }

    public void stage(final Optional<ConfigurableYAMLProperty> fieldNameOptional, final Optional<Object> fieldValueOptional) {
        if (fieldNameOptional.isPresent() && fieldValueOptional.isPresent()) {
            fieldNameAndValues.put(fieldNameOptional.get(), fieldValueOptional.get());
        }
    }

    public abstract T build();
}
