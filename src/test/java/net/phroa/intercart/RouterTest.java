package net.phroa.intercart;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RouterTest {
    CIDR ALL = new CIDR(0, 0);
    CIDR LOCALHOST = new CIDR(0x7F000001, 32);
    CIDR TEN = new CIDR(0x0A000000, 8);
    CIDR ELEVEN = new CIDR(0x0B000000, 8);
    CIDR TEN_AND_ELEVEN = new CIDR(0x0A000000, 7);
    CIDR TEN_AND_ELEVEN_UPPER = new CIDR(0x0B000000, 7);

    UUID CREATOR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    Location ROUTER_LOCATION = new Location(null, 0, 0, 0);
    Router ROUTER;

    List<Location> INTERFACE_LOCATIONS = new ArrayList<>();

    {
        for (int i = 1; i <= 12; i++) {
            INTERFACE_LOCATIONS.add(new Location(null, i, i, i));
        }
    }

    @BeforeEach
    void clean() {
        ROUTER = new Router(CREATOR_UUID, ROUTER_LOCATION, Lists.newArrayList());
        INTERFACE_LOCATIONS.forEach(ROUTER::addInterface);
    }

    @Test
    void getInterfaces() {
        assertEquals(12, ROUTER.getInterfaces().size());
    }

    @Test
    void route() {
        assertEquals(0, ROUTER.routingTable.size());
        assertNull(ROUTER.route("1.2.3.4"));

        ROUTER.routingTable.put(ALL, 0);
        assertEquals(1, ROUTER.routingTable.size());
        assertEquals(INTERFACE_LOCATIONS.get(0), ROUTER.route("10.2.3.4"));

        ROUTER.routingTable.put(TEN_AND_ELEVEN, 1);
        assertEquals(2, ROUTER.routingTable.size());
        assertEquals(INTERFACE_LOCATIONS.get(0), ROUTER.route("1.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(1), ROUTER.route("10.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(1), ROUTER.route("11.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(0), ROUTER.route("12.2.3.4"));

        ROUTER.routingTable.put(TEN, 2);
        ROUTER.routingTable.put(ELEVEN, 3);
        assertEquals(4, ROUTER.routingTable.size());
        assertEquals(INTERFACE_LOCATIONS.get(0), ROUTER.route("1.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(2), ROUTER.route("10.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(3), ROUTER.route("11.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(0), ROUTER.route("12.2.3.4"));

        ROUTER.routingTable.remove(ALL);
        assertEquals(3, ROUTER.routingTable.size());
        assertNull(ROUTER.route("1.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(2), ROUTER.route("10.2.3.4"));
        assertEquals(INTERFACE_LOCATIONS.get(3), ROUTER.route("11.2.3.4"));
        assertNull(ROUTER.route("12.2.3.4"));
    }
}