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
    final Map<CIDR, Integer> routingTable;

    public Router(Map<String, Object> serialized) {
        this.uuid = UUID.fromString((String) serialized.get("uuid"));
        this.creator = UUID.fromString((String) serialized.get("creator"));
        this.location = (Location) serialized.get("location");
        this.interfaces = (List<Location>) serialized.get("interfaces");
        this.routingTable = deserializeRoutingTable((Map<String, Object>) serialized.get("routingTable"));
    }

    public Router(UUID creator, Location location, List<Location> interfaces) {
        this.uuid = UUID.randomUUID();
        this.creator = creator;
        this.location = location;
        this.interfaces = interfaces;
        this.routingTable = new HashMap<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getCreator() {
        return creator;
    }

    public void addInterface(Location newInterface) {
        this.interfaces.add(newInterface.toBlockLocation());
    }

    public List<Location> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    private Map<CIDR, Integer> deserializeRoutingTable(Map<String, Object> serialized) {
        var out = new HashMap<CIDR, Integer>();

        for (Map.Entry<String, Object> entry : serialized.entrySet()) {
            out.put(CIDR.parse(entry.getKey()), interfaces.indexOf(entry.getValue()));
        }

        return out;
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
            put("uuid", uuid.toString());
            put("creator", creator.toString());
            put("location", location);
            put("interfaces", interfaces);
            put("routingTable", new HashMap<String, Location>() {{
                for (Entry<CIDR, Integer> entry : routingTable.entrySet()) {
                    put(entry.getKey().toString(), interfaces.get(entry.getValue()));
                }
            }});
        }};
    }

    public Location route(Destination destination) {
        var cidr = CIDR.parse(destination.destination);
        // Test all interfaces, return one with most specific mask
        var bestMask = -1;
        var bestInterface = -1;
        for (Map.Entry<CIDR, Integer> entry : routingTable.entrySet()) {
            var tableCidr = entry.getKey();
            var tableMask = tableCidr.getMask();
            if (tableCidr.contains(cidr) && tableMask > bestMask) {
                bestInterface = entry.getValue();
                bestMask = tableMask;
            }
        }

        if (bestInterface == -1) {
            return null;
        }
        return interfaces.get(bestInterface);
    }
}
