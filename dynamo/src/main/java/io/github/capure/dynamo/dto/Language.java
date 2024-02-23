package io.github.capure.dynamo.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.github.capure.dynamo.config.CConfig;
import io.github.capure.dynamo.config.CppConfig;
import io.github.capure.dynamo.config.LangConfig;
import io.github.capure.dynamo.config.PythonConfig;

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
                return new CConfig();
            case CPP:
                return new CppConfig();
            case PYTHON:
                return new PythonConfig();
            default:
                return null;
        }
    }
}
