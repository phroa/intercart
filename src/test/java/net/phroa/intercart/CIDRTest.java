package net.phroa.intercart;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CIDRTest {
    CIDR ALL = new CIDR(0, 0);
    CIDR LOCALHOST = new CIDR(0x7F000001, 32);
    CIDR TEN = new CIDR(0x0A000000, 8);
    CIDR ELEVEN = new CIDR(0x0B000000, 8);
    CIDR TEN_AND_ELEVEN = new CIDR(0x0A000000, 7);
    CIDR TEN_AND_ELEVEN_UPPER = new CIDR(0x0B000000, 7);

    @Test
    void parse() {
        assertEquals(ALL, CIDR.parse("0.0.0.0/0"));
        assertNotEquals(ALL, CIDR.parse("0.0.0.0"));
        assertEquals(LOCALHOST, CIDR.parse("127.0.0.1"));
        assertEquals(LOCALHOST, CIDR.parse("127.0.0.1/32"));
        assertEquals(TEN, CIDR.parse("10.0.0.0/8"));
        assertEquals(ELEVEN, CIDR.parse("11.0.0.0/8"));
        assertEquals(TEN_AND_ELEVEN, CIDR.parse("10.0.0.0/7"));
        assertEquals(TEN_AND_ELEVEN, CIDR.parse("11.0.0.0/7"));
        assertEquals(TEN_AND_ELEVEN_UPPER, CIDR.parse("11.0.0.0/7"));

        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4/-1"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4/33"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4/30/"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4//4"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4.5"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4.5/3"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.3.4.5/3/"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("a.b.c.d"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1.2.c.5"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("67.201.255.256"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("67.201.-1.254"));
        assertThrows(IllegalArgumentException.class, () -> CIDR.parse("1234"));
    }

    @Test
    void getAddress() {
        assertEquals(0, ALL.getAddress());
        assertEquals(0x7F000001, LOCALHOST.getAddress());
        assertEquals(0x0A000000, TEN_AND_ELEVEN.getAddress());
        assertEquals(0x0A000000, TEN_AND_ELEVEN_UPPER.getAddress());
    }

    @Test
    void getMask() {
        assertEquals(0, ALL.getMask());
        assertEquals(32, LOCALHOST.getMask());
        assertEquals(7, TEN_AND_ELEVEN.getMask());
        assertEquals(7, TEN_AND_ELEVEN_UPPER.getMask());
    }

    @Test
    void getSubnetMask() {
        assertEquals(0x00000000, ALL.getSubnetMask());
        assertEquals(0xFFFFFFFF, LOCALHOST.getSubnetMask());
        assertEquals(0xFF000000, TEN.getSubnetMask());
        assertEquals(0xFE000000, TEN_AND_ELEVEN.getSubnetMask());
        assertEquals(0xFE000000, TEN_AND_ELEVEN_UPPER.getSubnetMask());
    }

    @Test
    void contains() {
        assertTrue(ALL.contains(ALL));
        assertTrue(ALL.contains(LOCALHOST));
        assertTrue(ALL.contains(TEN));
        assertTrue(ALL.contains(TEN_AND_ELEVEN));
        assertTrue(LOCALHOST.contains(LOCALHOST));
        assertTrue(TEN.contains(TEN));
        assertTrue(TEN_AND_ELEVEN.contains(TEN));
        assertTrue(TEN_AND_ELEVEN.contains(ELEVEN));
        assertTrue(TEN_AND_ELEVEN.contains(TEN_AND_ELEVEN_UPPER));

        assertFalse(TEN_AND_ELEVEN.contains(LOCALHOST));
        assertFalse(TEN_AND_ELEVEN.contains(ALL));
        assertFalse(TEN.contains(ELEVEN));
        assertFalse(ELEVEN.contains(TEN));
    }

    @Test
    void hashability() {
        var set = new HashSet<CIDR>();
        set.add(ALL);
        set.add(TEN_AND_ELEVEN);
        set.add(TEN_AND_ELEVEN_UPPER);
        assertEquals(2, set.size());
    }
}