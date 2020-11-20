package net.phroa.intercart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Meta {
    public static final String META_DESTINATION = "ic-destination";

    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();
    private final Gson gson = new Gson();

    private final Intercart intercart;

    public Meta(Intercart intercart) {
        this.intercart = intercart;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConfigurationSerializable> Optional<T> get(Entity target, String key) {
        for (String tag : target.getScoreboardTags()) {
            if (tag.startsWith(key)) {
                var data = tag.substring(tag.indexOf(':') + 1);
                if (data.equals("null")) {
                    return Optional.empty();
                }
                return Optional.ofNullable((T) ConfigurationSerialization.deserializeObject(gson.fromJson(data, MAP_TYPE)));
            }
        }

        return Optional.empty();
    }

    public <T extends ConfigurationSerializable> Optional<T> set(Entity target, String key, T value) {
        var data = value.serialize();
        data.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(value.getClass()));
        Optional<T> old = get(target, key);

        target.addScoreboardTag(key + ":" + gson.toJson(data, MAP_TYPE));

        return old;
    }

    public <T extends ConfigurationSerializable> Optional<T> remove(Entity target, String key) {
        Optional<T> old = get(target, key);
        for (String tag : target.getScoreboardTags()) {
            if (tag.startsWith(key)) {
                target.removeScoreboardTag(tag);
                break;
            }
        }
        return old;
    }

    public <T extends ConfigurationSerializable, R extends ConfigurationSerializable> Optional<T> map(Entity target, String key, Function<T, R> callback) {
        Optional<T> old = get(target, key);
        old.ifPresent(d -> set(target, key, d));
        return old;
    }
}
