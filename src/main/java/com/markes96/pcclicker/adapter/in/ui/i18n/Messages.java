package com.markes96.pcclicker.adapter.in.ui.i18n;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

/** Localised texts of the user interface. English is the fallback for unsupported languages. */
public final class Messages {

    private static final String BUNDLE = "com.markes96.pcclicker.ui.i18n.messages";

    private final ResourceBundle bundle;
    private final Locale locale;

    private Messages(ResourceBundle bundle, Locale locale) {
        this.bundle = bundle;
        this.locale = locale;
    }

    public static Messages forLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        // No fallback to the JVM default locale: unsupported languages get the base (English) texts.
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        return new Messages(bundle, locale);
    }

    public static Messages forDefaultLocale() {
        return forLocale(Locale.getDefault(Locale.Category.DISPLAY));
    }

    /** Text for {@code key}, or {@code !key!} when missing so the gap is obvious but harmless. */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException missing) {
            return "!" + key + "!";
        }
    }

    /** Text for {@code key} with its {@code {n}} placeholders replaced using the locale's conventions. */
    public String format(String key, Object... arguments) {
        return new MessageFormat(get(key), locale).format(arguments);
    }

    public String number(long value) {
        return NumberFormat.getIntegerInstance(locale).format(value);
    }

    public Locale locale() {
        return locale;
    }
}
