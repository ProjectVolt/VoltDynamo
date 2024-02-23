package io.github.capure.dynamo.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.github.capure.dynamo.config.CppConfig;
import io.github.capure.dynamo.config.LangConfig;

public enum Language {
    C(0),
    CPP(1),
    PYTHON(2);

    private final int value;

    @JsonCreator
    Language(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public LangConfig getConfig() {
        switch (this) {
            case C:
                throw new UnsupportedOperationException("C is not supported yet");
            case CPP:
                return new CppConfig();
            case PYTHON:
                throw new UnsupportedOperationException("Python is not supported yet");
            default:
                return null;
        }
    }
}
