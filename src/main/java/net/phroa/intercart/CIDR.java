package net.phroa.intercart;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SerializableAs("intercart-cidr")
public class CIDR implements ConfigurationSerializable {
    private final int address;
    private final int mask;

    public CIDR(int address, int mask) {
        this.mask = mask;
        this.address = address & getSubnetMask();
    }

    public CIDR(Map<String, Object> serialized) {
        var parsed = CIDR.parse((String) serialized.get("cidr"));
        this.address = parsed.address;
        this.mask = parsed.mask;
    }

    public static CIDR parse(String cidr) {
        try {
            var parts = cidr.split("/", -1);
            int mask;
            if (parts.length == 1) {
                mask = 32;
            } else if (parts.length > 2) {
                throw new IllegalArgumentException("CIDR has more than 1 / in it");
            } else {
                mask = Integer.parseInt(parts[1]);
            }

            if (mask < 0 || mask > 32) {
                throw new IllegalArgumentException("CIDR mask must be between [0, 32]");
            }

            var host = parts[0].split("\\.");
            if (host.length != 4) {
                throw new IllegalArgumentException("CIDR isn't in dotted-decimal format");
            }

            var a = Integer.parseInt(host[0]);
            var b = Integer.parseInt(host[1]);
            var c = Integer.parseInt(host[2]);
            var d = Integer.parseInt(host[3]);
            if (a > 255 || b > 255 || c > 255 || d > 255
                    || a < 0 || b < 0 || c < 0 || d < 0) {
                throw new IllegalArgumentException("CIDR component is not within 0-255");
            }
            var ip = a << 24
                    | b << 16
                    | c << 8
                    | d;

            return new CIDR(ip, mask);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("CIDR was passed something other than numbers", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d/%d",
                address >> 24 & 0xFF,
                address >> 16 & 0xFF,
                address >> 8 & 0xFF,
                address & 0xFF, mask);
    }

    public int getAddress() {
        return address;
    }

    public int getMask() {
        return mask;
    }

    public int getSubnetMask() {
        return (int) (0xFFFFFFFFL << (32 - mask));
    }

    public boolean contains(CIDR other) {
        var subnetMask = getSubnetMask();
        return (this.address & subnetMask) == (other.address & subnetMask);
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<>() {{
            put("cidr", toString());
        }};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CIDR cidr = (CIDR) o;
        return address == cidr.address && mask == cidr.mask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, mask);
    }
}
