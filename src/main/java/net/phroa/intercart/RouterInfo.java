package net.phroa.intercart;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RouterInfo {
    private final UUID uuid;
    private final UUID creator;
    private final List<Location> interfaces;

    public RouterInfo(RouterInfo other) {
        this.uuid = other.uuid;
        this.creator = other.creator;
        this.interfaces = new ArrayList<>(other.interfaces);
    }

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

    public void addInterface(Location newInterface) {
        this.interfaces.add(newInterface);
    }

    public List<Location> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
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
