package com.markes96.pcclicker.adapter.out.persistence;

import com.markes96.pcclicker.application.port.out.SettingsStorageException;
import com.markes96.pcclicker.application.port.out.UserSettingsRepository;
import com.markes96.pcclicker.domain.configuration.ClickLimit;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.control.KeyBindings;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.domain.settings.Favorites;
import com.markes96.pcclicker.domain.settings.Preferences;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileLibrary;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import com.markes96.pcclicker.domain.settings.UserSettings;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Stores the user's settings in a UTF-8 {@code .properties} file: no extra dependency, and readable
 * and editable by hand.
 *
 * <p>Reading is forgiving: a value that is missing or no longer valid (an edited file, a range that
 * changed in a later version) falls back to its default without losing the rest. Writing goes to a
 * temporary file that then replaces the old one, so a crash never leaves a half-written file.
 */
public final class PropertiesSettingsRepository implements UserSettingsRepository {

    static final int FORMAT = 1;

    private static final String PRESET_PREFIX = "preset:";
    private static final String PROFILE_PREFIX = "profile:";

    private static final Logger LOG = System.getLogger(PropertiesSettingsRepository.class.getName());

    private final Path file;

    public PropertiesSettingsRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    @Override
    public Optional<UserSettings> load() {
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (NoSuchFileException firstLaunch) {
            return Optional.empty();
        } catch (IOException | IllegalArgumentException unreadable) {
            throw new SettingsStorageException("Cannot read the settings from " + file, unreadable);
        }
        return Optional.of(read(new Values(properties)));
    }

    @Override
    public void save(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        final SortedProperties properties = new SortedProperties();
        write(settings, properties);
        try {
            final Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            final Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
            try {
                try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                    properties.store(writer, "MK96 PC Clicker settings");
                }
                replace(temporary);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException failure) {
            throw new SettingsStorageException("Cannot write the settings to " + file, failure);
        }
    }

    private void replace(Path temporary) throws IOException {
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // --- Reading ---

    private static UserSettings read(Values values) {
        final ProfileLibrary profiles = readProfiles(values);
        return new UserSettings(
                readSession(values, "workspace."),
                profiles,
                readPreferences(values),
                readFavorites(values, profiles));
    }

    /** Favourites of profiles that no longer exist are dropped. */
    private static Favorites readFavorites(Values values, ProfileLibrary profiles) {
        final int count = values.value("favorites.count", Integer::parseInt, 0);
        final Set<ProfileRef> refs = new LinkedHashSet<>();
        for (int index = 0; index < count; index++) {
            values.optional("favorite." + index, PropertiesSettingsRepository::profileRef)
                    .filter(ref -> !(ref instanceof ProfileRef.OfSaved saved) || profiles.profiles().stream()
                            .anyMatch(profile -> profile.name().key().equals(saved.key())))
                    .ifPresent(refs::add);
        }
        return Favorites.of(refs);
    }

    private static ProfileRef profileRef(String text) {
        if (text.startsWith(PRESET_PREFIX)) {
            return ProfileRef.of(Preset.valueOf(text.substring(PRESET_PREFIX.length())));
        }
        if (text.startsWith(PROFILE_PREFIX)) {
            return ProfileRef.of(new ProfileName(text.substring(PROFILE_PREFIX.length())));
        }
        throw new IllegalArgumentException("Not a profile reference: " + text);
    }

    private static SessionSettings readSession(Values values, String prefix) {
        final ClickSettings defaultClick = ClickSettings.disabled();
        final MovementSettings defaultMovement = MovementSettings.disabled();
        final ClickSettings click = new ClickSettings(
                values.bool(prefix + "click.enabled", defaultClick.enabled()),
                values.value(prefix + "click.rate", text -> new ClickRate(Integer.parseInt(text)), defaultClick.rate()),
                values.value(prefix + "click.button", MouseButton::valueOf, defaultClick.button()),
                values.value(prefix + "click.type", ClickType::valueOf, defaultClick.type()),
                values.value(prefix + "click.variation", text -> new RateVariation(Integer.parseInt(text)),
                        defaultClick.variation()),
                values.value(prefix + "click.limit", PropertiesSettingsRepository::clickLimit, defaultClick.limit()));
        final MovementSettings movement = new MovementSettings(
                values.bool(prefix + "movement.enabled", defaultMovement.enabled()),
                values.value(prefix + "movement.pattern", MovementPattern::valueOf, defaultMovement.pattern()),
                values.value(prefix + "movement.radius", text -> new Radius(Integer.parseInt(text)),
                        defaultMovement.radius()),
                values.value(prefix + "movement.vertices", text -> new VertexCount(Integer.parseInt(text)),
                        defaultMovement.vertices()));
        final TimeLimit timeLimit = values.value(
                prefix + "timeLimit.minutes", PropertiesSettingsRepository::timeLimit, TimeLimit.unlimited());
        return new SessionSettings(click, movement, timeLimit);
    }

    private static ProfileLibrary readProfiles(Values values) {
        final int count = values.value("profiles.count", Integer::parseInt, 0);
        final List<Profile> profiles = new ArrayList<>();
        for (int index = 0; index < count && profiles.size() < ProfileLibrary.MAX_PROFILES; index++) {
            final String prefix = "profile." + index + ".";
            final Optional<ProfileName> name = values.optional(prefix + "name", ProfileName::new);
            if (name.isEmpty()) {
                LOG.log(Level.WARNING, "Skipping stored profile #" + index + ": its name is missing or invalid");
                continue;
            }
            profiles.add(new Profile(name.get(), readSession(values, prefix)));
        }
        return ProfileLibrary.of(profiles);
    }

    private static Preferences readPreferences(Values values) {
        final Preferences defaults = Preferences.defaults();
        return new Preferences(
                readKeys(values, defaults.keys()),
                values.value("preferences.countdownSeconds", text -> new CountdownSeconds(Integer.parseInt(text)),
                        defaults.countdown()),
                values.bool("preferences.showPanel", defaults.showPanel()));
    }

    /** Keys are read as a whole: a single broken or repeated key restores every default. */
    private static KeyBindings readKeys(Values values, KeyBindings defaults) {
        final Map<Hotkey, Key> keys = new EnumMap<>(Hotkey.class);
        for (Hotkey hotkey : Hotkey.values()) {
            keys.put(hotkey, values.value("preferences.key." + hotkey.name(), Key::valueOf, defaults.keyFor(hotkey)));
        }
        try {
            return new KeyBindings(keys);
        } catch (IllegalArgumentException conflicting) {
            LOG.log(Level.WARNING, "Stored hotkeys conflict; using the default keys", conflicting);
            return defaults;
        }
    }

    private static ClickLimit clickLimit(String text) {
        final int clicks = Integer.parseInt(text);
        return clicks == 0 ? ClickLimit.unlimited() : ClickLimit.ofClicks(clicks);
    }

    private static TimeLimit timeLimit(String text) {
        final int minutes = Integer.parseInt(text);
        return minutes == 0 ? TimeLimit.unlimited() : TimeLimit.ofMinutes(minutes);
    }

    // --- Writing ---

    private static void write(UserSettings settings, Properties properties) {
        properties.setProperty("format", String.valueOf(FORMAT));
        writeSession(settings.workspace(), "workspace.", properties);

        final List<Profile> profiles = settings.profiles().profiles();
        properties.setProperty("profiles.count", String.valueOf(profiles.size()));
        for (int index = 0; index < profiles.size(); index++) {
            final String prefix = "profile." + index + ".";
            properties.setProperty(prefix + "name", profiles.get(index).name().value());
            writeSession(profiles.get(index).settings(), prefix, properties);
        }

        final List<String> favorites = settings.favorites().refs().stream()
                .map(ref -> switch (ref) {
                    case ProfileRef.OfPreset preset -> PRESET_PREFIX + preset.preset().name();
                    case ProfileRef.OfSaved saved -> PROFILE_PREFIX + saved.key();
                })
                .sorted()
                .toList();
        properties.setProperty("favorites.count", String.valueOf(favorites.size()));
        for (int index = 0; index < favorites.size(); index++) {
            properties.setProperty("favorite." + index, favorites.get(index));
        }

        final Preferences preferences = settings.preferences();
        preferences.keys().asMap().forEach((hotkey, key) ->
                properties.setProperty("preferences.key." + hotkey.name(), key.name()));
        properties.setProperty("preferences.countdownSeconds", String.valueOf(preferences.countdown().value()));
        properties.setProperty("preferences.showPanel", String.valueOf(preferences.showPanel()));
    }

    private static void writeSession(SessionSettings settings, String prefix, Properties properties) {
        final ClickSettings click = settings.click();
        properties.setProperty(prefix + "click.enabled", String.valueOf(click.enabled()));
        properties.setProperty(prefix + "click.rate", String.valueOf(click.rate().clicksPerSecond()));
        properties.setProperty(prefix + "click.button", click.button().name());
        properties.setProperty(prefix + "click.type", click.type().name());
        properties.setProperty(prefix + "click.variation", String.valueOf(click.variation().percent()));
        properties.setProperty(prefix + "click.limit", String.valueOf(click.limit().clicks().orElse(0)));

        final MovementSettings movement = settings.movement();
        properties.setProperty(prefix + "movement.enabled", String.valueOf(movement.enabled()));
        properties.setProperty(prefix + "movement.pattern", movement.pattern().name());
        properties.setProperty(prefix + "movement.radius", String.valueOf(movement.radius().pixels()));
        properties.setProperty(prefix + "movement.vertices", String.valueOf(movement.vertices().value()));

        properties.setProperty(prefix + "timeLimit.minutes",
                String.valueOf(settings.timeLimit().duration().map(Duration::toMinutes).orElse(0L)));
    }

    /** Typed, forgiving access to the stored values. */
    private record Values(Properties properties) {

        <T> T value(String key, Function<String, T> parser, T fallback) {
            return optional(key, parser).orElse(fallback);
        }

        <T> Optional<T> optional(String key, Function<String, T> parser) {
            final String text = properties.getProperty(key);
            if (text == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(parser.apply(text.strip()));
            } catch (RuntimeException invalid) {
                LOG.log(Level.WARNING, "Ignoring invalid stored value " + key + "=" + text);
                return Optional.empty();
            }
        }

        boolean bool(String key, boolean fallback) {
            return value(key, text -> switch (text) {
                case "true" -> Boolean.TRUE;
                case "false" -> Boolean.FALSE;
                default -> throw new IllegalArgumentException("Not a boolean: " + text);
            }, fallback);
        }
    }

    /** Writes the keys in alphabetical order, so the file reads well and diffs cleanly. */
    private static final class SortedProperties extends Properties {

        private static final long serialVersionUID = 1L;

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            final Map<Object, Object> sorted = new TreeMap<>(Comparator.comparing(Object::toString));
            for (Map.Entry<Object, Object> entry : super.entrySet()) {
                sorted.put(entry.getKey(), entry.getValue());
            }
            return Collections.unmodifiableSet(sorted.entrySet());
        }
    }
}
