package net.phroa.intercart;

import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class RouterInfo {
    private final UUID uuid;
    private final UUID creator;
    private final List<Location> interfaces;

    public RouterInfo(UUID creator, List<Location> interfaces) {
        this.uuid = UUID.randomUUID();
        this.creator = creator;
        this.interfaces = interfaces;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getCreator() {
        return creator;
    }

    public List<Location> getInterfaces() {
        return interfaces;
    }

    @Override
    public String toString() {
        return "RouterInfo{" +
                "uuid=" + uuid +
                ", creator=" + creator +
                ", interfaces=" + interfaces +
                '}';
    }
}
