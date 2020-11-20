package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;

@SerializableAs("intercart-router")
public class Router implements ConfigurationSerializable {
    final UUID uuid;
    final UUID creator;
    final Location location;
    final List<Location> interfaces;

    public Router(Map<String, Object> serialized) {
        this.uuid = (UUID) serialized.get("uuid");
        this.creator = (UUID) serialized.get("creator");
        this.location = (Location) serialized.get("location");
        this.interfaces = (List<Location>) serialized.get("interfaces");
    }

    public Router(UUID creator, Location location, List<Location> interfaces) {
        this.uuid = UUID.randomUUID();
        this.creator = creator;
        this.location = location;
        this.interfaces = interfaces;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getCreator() {
        return creator;
    }

    public void addInterface(Location newInterface) {
        this.interfaces.add(newInterface);
    }

    public List<Location> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    @Override
    public String toString() {
        return "Router{" +
                "uuid=" + uuid +
                ", creator=" + creator +
                ", location=" + location +
                ", interfaces=" + interfaces +
                '}';
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<>() {{
            put("uuid", uuid);
            put("creator", creator);
            put("location", location);
            put("interfaces", interfaces);
        }};
    }
}
