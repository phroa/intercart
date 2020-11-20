package net.phroa.intercart;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@SerializableAs("intercart-destination")
public class Destination implements ConfigurationSerializable {
    public final int destination;

    public Destination(int destination) {
        this.destination = destination;
    }

    public Destination(Map<String, Object> serialized) {
        this.destination = ((Double) serialized.get("destination")).intValue();
    }

    @Override
    public String toString() {
        return "Destination{" +
                "destination=" + destination +
                '}';
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<>() {{
            put("destination", destination);
        }};
    }
}
