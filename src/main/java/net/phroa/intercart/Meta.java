package net.phroa.intercart;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.Optional;
import java.util.function.Function;

public class Meta {
    public static final String META_BUILD_STATE = "ic-build-state";
    public static final String META_ROUTER_INFO = "ic-router-info";
    public static final String META_ATTACHED_ROUTER = "ic-attached-router";
    public static final String META_CURRENT_ROUTER = "ic-current-router";

    public static final String META_DESTINATION = "ic-destination";
    private final Intercart intercart;

    public Meta(Intercart intercart) {
        this.intercart = intercart;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(Metadatable target, String key) {
        T data = null;

        if (target.hasMetadata(key)) {
            for (MetadataValue metadatum : target.getMetadata(key)) {
                if (metadatum.getOwningPlugin() == intercart) {
                    Object o = metadatum.value();
                    if (o != null) {
                        data = (T) o;
                        break;
                    }
                }
            }
        }

        return Optional.ofNullable(data);
    }

    public <T> Optional<T> set(Metadatable target, String key, T value) {
        Optional<T> old = get(target, key);
        target.setMetadata(key, new FixedMetadataValue(intercart, value));

        return old;
    }

    public <T> Optional<T> remove(Metadatable target, String key) {
        Optional<T> old = get(target, key);
        target.removeMetadata(key, intercart);
        return old;
    }

    public <T, R> Optional<T> map(Metadatable target, String key, Function<T, R> callback) {
        Optional<T> old = get(target, key);
        old.ifPresent(t -> target.setMetadata(key, new FixedMetadataValue(intercart, callback.apply(t))));
        return old;
    }
}
