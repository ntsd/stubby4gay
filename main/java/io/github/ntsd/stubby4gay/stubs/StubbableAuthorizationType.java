package io.github.ntsd.stubby4gay.stubs;

import io.github.ntsd.stubby4gay.utils.StringUtils;

public enum StubbableAuthorizationType {

    BASIC("Basic"),
    BEARER("Bearer"),
    CUSTOM("Custom");

    private final String name;
    private final String property;

    StubbableAuthorizationType(final String name) {
        this.name = name;
        this.property = String.format("authorization-%s", StringUtils.toLower(this.name));
    }

    public String asYAMLProp() {
        return property;
    }

    public String asString() {
        return name;
    }
}
