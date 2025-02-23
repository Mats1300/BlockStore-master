package net.sothatsit.blockstore.chunkstore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableMap;

import net.sothatsit.blockstore.util.Checks;

/**
 * This class maps metadata names to 0-based indices, assigned in the order they
 * were registered.
 */
public class NameStore {

    /**
     * Provides read and write locks.
     */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Lock for reads.
     */
    private final Lock readLock = readWriteLock.readLock();

    /**
     * Lock for writes.
     */
    private final Lock writeLock = readWriteLock.writeLock();

    /**
     * List of all registered names, with no duplicate entries.
     *
     * 0-based array index is the ID of the name.
     */
    private final List<String> names = new CopyOnWriteArrayList<>();

    /**
     * Map from name to 0-based ID.
     */
    private final Map<String, Integer> ids = new ConcurrentHashMap<>();

    /**
     * Return the ID corresponding to the name, or -1 of not found.
     *
     * @param name   the name to look up.
     * @param create if true, the name can be added with a new ID when not
     *               found, in which case, the return ID will never be negative.
     * @return the ID corresponding to the name, or -1 if not found.
     */
    public int toId(String name, boolean create) {
        Checks.ensureNonNull(name, "name");

        try {
            readLock.lock();

            int id = ids.getOrDefault(name, -1);

            if (!create || id >= 0) {
                return id;
            }
        } finally {
            readLock.unlock();
        }

        return addName(name);
    }

    /**
     * Register the name and return the corresponding ID.
     *
     * @param name the name to add.
     * @return the corresponding 0-based ID.
     */
    private int addName(String name) {
        try {
            writeLock.lock();

            int id = names.size();

            names.add(name);
            ids.put(name, id);

            return id;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Return the name that corresponds to the specified 0-based ID.
     *
     * @param id the ID.
     * @return the corresponding ID.
     */
    public String fromId(int id) {
        Checks.ensureTrue(id >= 0, "Invalid id " + id + ", valid ids are >= 0");
        Checks.ensureTrue(id < names.size(), "Invalid id " + id + ", outside of the range of known ids");

        try {
            readLock.lock();

            return names.get(id);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Convert a map of integer IDs and Object values to a map from the
     * corresponding name to the same values; that is, each ID is converted into
     * it's corresponding name and has a mapping to the object value from the
     * parameter.
     *
     * This method is used to convert a map from IDs to stored metadata into a
     * map from names to stored metadata.
     *
     * @param values a map from integer IDs to Object values.
     * @return a map from names to Object values.
     */
    public Map<String, Object> keysFromId(Map<Integer, Object> values) {
        Checks.ensureNonNull(values, "values");

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        values.forEach((keyId, value) -> {
            String key = fromId(keyId);

            builder.put(key, value);
        });

        return builder.build();
    }

    /**
     * Takes an ID-keyed map containing maps from IDs to values, looks up the
     * names corresponding to all the IDs and returns the corresponding map of
     * maps with names where the IDs were.
     *
     * @param values the map of maps with integer ID keys.
     * @return the corresponding map of maps with string keys.
     */
    public Map<String, Map<String, Object>> deepKeysFromId(Map<Integer, Map<Integer, Object>> values) {
        Checks.ensureNonNull(values, "values");

        ImmutableMap.Builder<String, Map<String, Object>> builder = ImmutableMap.builder();

        values.forEach((keyId, subValuesByIds) -> {
            String key = fromId(keyId);
            Map<String, Object> subValues = keysFromId(subValuesByIds);

            builder.put(key, subValues);
        });

        return builder.build();
    }

    /**
     * Write this instance to the specified ObjectOutputStream.
     *
     * @param stream the stream.
     * @throws IOException
     */
    public void write(ObjectOutputStream stream) throws IOException {
        Checks.ensureNonNull(stream, "stream");

        try {
            readLock.lock();

            stream.writeInt(names.size());

            for (String name : names) {
                stream.writeUTF(name);
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Read this instance from the specified ObjectInputStream.
     *
     * @param stream the stream.
     * @throws IOException
     */
    public void read(ObjectInputStream stream) throws IOException {
        Checks.ensureNonNull(stream, "stream");

        try {
            writeLock.lock();

            int amount = stream.readInt();

            for (int i = 0; i < amount; ++i) {
                addName(stream.readUTF());
            }
        } finally {
            writeLock.unlock();
        }
    }
}
