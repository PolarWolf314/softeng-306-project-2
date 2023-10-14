package nz.ac.auckland.se306.group12.models.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link LinkedHashMap} which supports a max size. When the max size is exceeded, the eldest
 * entries are removed from the map.
 * <p>
 * Taken from <a href="https://stackoverflow.com/a/5601377">Stack Overflow</a>.
 *
 * @param <K> The type of the keys in the map
 * @param <V> The type of the values in the map
 */
public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {

  private final int maxSize;

  /**
   * Creates a new {@link MaxSizeHashMap} instance with the given max size and initial capacity. It
   * will have the default load factor of 0.75.
   *
   * @param maxSize         The max size of the map
   * @param initialCapacity The initial capacity of the map
   */
  public MaxSizeHashMap(int maxSize, int initialCapacity) {
    super(initialCapacity);
    this.maxSize = maxSize;
  }

  /**
   * The default implementation of this method always returns {@code false}. This overrides it to
   * evict the eldest entries when the max size is exceeded.
   *
   * @param eldest The least recently inserted entry in the map, or if this is an access-ordered
   *               map, the least recently accessed entry. This is the entry that will be removed if
   *               this method returns {@code true}. If the map was empty prior to the {@code put}
   *               or {@code putAll} invocation resulting in this invocation, this will be the entry
   *               that was just inserted; in other words, if the map contains a single entry, the
   *               eldest entry is also the newest.
   * @return {@code true} if the current size exceeds the max size, {@code false} otherwise
   */
  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return this.size() > this.maxSize;
  }

}
