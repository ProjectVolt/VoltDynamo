package io.github.capure.dynamo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.capure.dynamo.config.CppConfig;
import io.github.capure.dynamo.config.LangConfig;

public class LanguageTest {
    @Test
    public void testGetConfig() {
        Language language = Language.CPP;
        LangConfig config = language.getConfig();
        assertNotNull(config);
        assertTrue(config instanceof CppConfig);
    }

    @Test
    public void testConstructor() {
        Language language = Language.C;
        assertNotNull(language);
        assertEquals(0, language.getValue());
    }
}
