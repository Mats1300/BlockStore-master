package net.sothatsit.blockstore.chunkstore;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents the location of the base of a {@link ChunkStore}.
 *
 * ChunkStores occur every 16 blocks horizontally and every 64 blocks
 * vertically.
 */
public final class ChunkLoc {
    /**
     * The corresponding {@link ChunkStore} X coordinate.
     */
    public final int x;

    /**
     * The corresponding {@link ChunkStore} Y coordinate.
     */
    public final int y;

    /**
     * The corresponding {@link ChunkStore} Z coordinate.
     */
    public final int z;

    /**
     * Constructor
     *
     * @param x corresponding {@link ChunkStore} X coordinate.
     * @param y
     * @param z
     */
    public ChunkLoc(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Return the base X block coordinate of the corresponding
     * {@link ChunkStore}.
     *
     * @return the base X block coordinate of the corresponding
     *         {@link ChunkStore}.
     */
    public int getBlockX() {
        return x * 16;
    }

    /**
     * Return the base Y block coordinate of the corresponding
     * {@link ChunkStore}.
     *
     * @return the base Y block coordinate of the corresponding
     *         {@link ChunkStore}.
     */
    public int getBlockY() {
        return y * 64;
    }

    /**
     * Return the base Z block coordinate of the corresponding
     * {@link ChunkStore}.
     *
     * @return the base Z block coordinate of the corresponding
     *         {@link ChunkStore}.
     */
    public int getBlockZ() {
        return z * 16;
    }

    /**
     * Return true if this ChunkLoc exists in the specified World.
     *
     * @param world the world.
     * @return true if this ChunkLoc exists in the specified World.
     * @TODO 1.17 this class will need to account for negative Y coordinates.
     */
    public boolean exists(World world) {
        return y >= -64 && y * 64 < world.getMaxHeight();
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(x) ^ Integer.hashCode(y) ^ Integer.hashCode(z);
    }

    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkLoc)) {
            return false;
        }

        ChunkLoc other = (ChunkLoc) obj;

        return other.x == x && other.y == y && other.z == z;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "{x: " + x + ", y: " + y + ", z: " + z + "}";
    }

    /**
     * Return the ChunkLoc corresponding to the specified Location.
     *
     * @param location the Location.
     * @return the ChunkLoc corresponding to the specified Location.
     */
    public static ChunkLoc fromLocation(Location location) {
        return fromLocation(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Return the ChunkLoc corresponding to the specified coordinates.
     *
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @param z the Z coordinate.
     * @return the ChunkLoc corresponding to the specified coordinates.
     */
    public static ChunkLoc fromLocation(double x, double y, double z) {
        int cx = (int) Math.floor(x / 16d);
        int cy = (int) Math.floor(y / 64d);
        int cz = (int) Math.floor(z / 16d);

        return new ChunkLoc(cx, cy, cz);
    }

}
