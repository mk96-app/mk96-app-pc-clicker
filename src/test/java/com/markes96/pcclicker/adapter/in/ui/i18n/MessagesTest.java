package com.markes96.pcclicker.adapter.in.ui.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class MessagesTest {

    private static Properties load(String name) throws IOException {
        try (InputStream in = MessagesTest.class.getResourceAsStream("/com/markes96/pcclicker/ui/i18n/" + name)) {
            assertThat(in).as(name).isNotNull();
            final Properties properties = new Properties();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return properties;
        }
    }

    @Test
    void every_translation_defines_exactly_the_same_keys() throws IOException {
        final Properties english = load("messages.properties");
        final Properties spanish = load("messages_es.properties");

        assertThat(spanish.stringPropertyNames()).containsExactlyInAnyOrderElementsOf(english.stringPropertyNames());
    }

    @Test
    void speaks_spanish_to_spanish_users() {
        final Messages messages = Messages.forLocale(Locale.forLanguageTag("es-ES"));

        assertThat(messages.get("start.button")).isEqualTo("Iniciar");
    }

    @Test
    void falls_back_to_english_for_other_languages() {
        final Messages messages = Messages.forLocale(Locale.JAPANESE);

        assertThat(messages.get("start.button")).isEqualTo("Start");
    }

    @Test
    void formats_arguments_with_the_locale_conventions() {
        final Messages spanish = Messages.forLocale(Locale.forLanguageTag("es-ES"));
        final Messages english = Messages.forLocale(Locale.ENGLISH);

        assertThat(spanish.format("summary.clickRate", 1000)).isEqualTo("1.000 clics/s");
        assertThat(english.format("summary.clickRate", 1000)).isEqualTo("1,000 clicks/s");
    }

    @Test
    void keeps_apostrophes_in_formatted_english_texts() {
        assertThat(Messages.forLocale(Locale.ENGLISH).format("start.hint", 3)).contains("You'll");
    }

    @Test
    void shows_the_key_itself_when_a_text_is_missing() {
        assertThat(Messages.forLocale(Locale.ENGLISH).get("does.not.exist")).isEqualTo("!does.not.exist!");
    }

    @Test
    void formats_integers_with_grouping() {
        assertThat(Messages.forLocale(Locale.forLanguageTag("es-ES")).number(1340)).isEqualTo("1.340");
    }
}
