package nz.ac.auckland.se306.group12.models.datastructures;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import lombok.RequiredArgsConstructor;

/**
 * A set of {@link T indexables} that are stored in a bitmap. This is used to increase performance
 * of operations like {@link #contains(Object)}, {@link #add(T)} and {@link #remove(Object)}.
 * <p>
 * As this uses an integer for the bitmap, it is limited to 32 elements, or a maximum index of
 * {@link #MAX_INDEX}. Attempting to add an {@link Indexable} with an invalid index will cause an
 * {@link IllegalArgumentException} to be thrown.
 * <p>
 * Additionally, operations like {@link #containsAll(Collection)}, {@link #addAll(Collection)},
 * {@link #removeAll(Collection)}, {@link #retainAll(Collection)} have been optimised when being
 * used with another {@link BitSet} as it leverages bitwise operations on the two bitmaps, which
 * makes them significantly faster than the default implementations.
 */
@RequiredArgsConstructor
public class BitSet<T extends Indexable> implements Set<T> {

  /**
   * As we are using an int to store the bitmap, we can only store up to 32 elements, or a maximum
   * index of 31 as integers are 32 bits long.
   */
  public static final int MAX_INDEX = 31;

  private final IndexableResolver<T> indexableResolver;

  /**
   * The bitmap stores the {@link Indexable indexables} in this set. Each bit represents an
   * {@link Indexable} where the position of the bit corresponds to the index of the element. If the
   * bit is set ({@code 1}) then that element is present in the set, otherwise if it is unset
   * ({@code 0}) then that element is not present.
   */
  private int bitmap = 0;
  private int count = 0;

  /**
   * Creates a new {@link BitSet} from an existing {@link Set}.
   *
   * @param existingSet The existing {@link Set} to create a new {@link BitSet} from
   */
  public BitSet(Set<T> existingSet) {
    if (existingSet instanceof BitSet<T> bitSet) {
      this.bitmap = bitSet.bitmap;
      this.count = bitSet.count;
      this.indexableResolver = bitSet.indexableResolver;

    } else {
      this.addAll(existingSet);
      this.indexableResolver = null;
    }
  }

  /**
   * Creates a new {@link BitSet} from an existing BitSet.
   *
   * @param existingBitSet The existing {@link BitSet} to create a new BitSet from
   */
  public BitSet(BitSet<T> existingBitSet) {
    this.bitmap = existingBitSet.bitmap;
    this.count = existingBitSet.count;
    this.indexableResolver = existingBitSet.indexableResolver;
  }

  /**
   * Creates a new {@link BitSet}. As no {@link IndexableResolver} is specified, this {@link BitSet}
   * cannot be iterated over. Attempting to do so will generate an
   * {@link UnsupportedOperationException}.
   */
  public BitSet() {
    this.indexableResolver = null;
  }

  /**
   * Creates a new {@link BitSetCollector} which can be used to collect a stream of
   * {@link T indexables} into a {@link BitSet}.
   * <p>
   * E.g.
   * <pre>
   * taskGraph.getTasks().stream()
   *         .filter(Task::isSource)
   *         .collect(BitSet.collect(taskGraph));
   * </pre>
   *
   * @param indexableResolver The {@link IndexableResolver} to resolve the {@link T indexables}
   *                          from
   * @return A new {@link BitSetCollector}
   */
  public static <T extends Indexable> BitSetCollector<T> collect(
      IndexableResolver<T> indexableResolver
  ) {
    return new BitSetCollector<>(indexableResolver);
  }

  /**
   * Creates a new {@link BitSetCollector} which can be used to collect a stream of
   * {@link T indexables} into a {@link BitSet}.
   * <p>
   * E.g.
   * <pre>
   * taskGraph.getTasks().stream()
   *         .filter(Task::isSource)
   *         .collect(BitSet.collect(taskGraph));
   * </pre>
   * <p>
   * Note that by not specifying an {@link IndexableResolver} you will not be able to iterate over
   * the {@link BitSet}. Attempting to do so will generate an
   * {@link UnsupportedOperationException}.
   *
   * @return A new {@link BitSetCollector}
   */
  public static <T extends Indexable> BitSetCollector<T> collect() {
    return new BitSetCollector<>(null);
  }

  /**
   * Updates the bitmap to the new value and recalculates the new count. This should only be used
   * when making large changes to the {@link BitSet} as the cost of recalculating the count is not
   * justified when only adding/removing a single element. If the bitmap is not changed, this will
   * not change anything or recalculate the count.
   *
   * @param newBitmap The new bitmap
   * @return {@code true} if the bitmap was changed, {@code false} otherwise
   */
  private boolean setBitmap(int newBitmap) {
    if (this.bitmap == newBitmap) {
      return false;
    }

    this.bitmap = newBitmap;
    this.count = Integer.bitCount(this.bitmap);
    return true;
  }

  /**
   * Checks that the index of the {@link Indexable} is within the allowed bounds of 0 to
   * {@link #MAX_INDEX}. If the index is not valid an {@link IllegalArgumentException} is thrown.
   *
   * @param indexable The {@link Indexable} to check the index of
   * @throws IllegalArgumentException If the index is not valid
   */
  private void assertValidIndex(Indexable indexable) {
    if (indexable.getIndex() < 0 || indexable.getIndex() > MAX_INDEX) {
      throw new IllegalArgumentException(String.format(
          "%s index %d is outside the range of supported indices (0 to %d) for BitSet",
          indexable.getClass().getSimpleName(),
          indexable.getIndex(),
          MAX_INDEX));
    }
  }

  /**
   * Checks that this BitSet contains an {@link Indexable} with the given index.
   *
   * @param index The index of the {@link Indexable} to check for
   * @return {@code true} if the index is contained, {@code false} otherwise
   */
  private boolean containsIndex(int index) {
    // Check that there is a 1 bit at the index
    return (this.bitmap & (1 << index)) != 0;
  }

  /**
   * @inheritDoc
   */
  @Override
  public int size() {
    return this.count;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean isEmpty() {
    return this.count == 0;
  }

  /**
   * If the object is not an instance of {@link Indexable} this will always return {@code false}.
   *
   * @inheritDoc
   */
  @Override
  public boolean contains(Object object) {
    if (object instanceof Indexable indexable) {
      // We have to check that the index is valid to prevent it overflowing the int
      this.assertValidIndex(indexable);
      return this.containsIndex(indexable.getIndex());
    }
    return false;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean add(T indexable) {
    boolean isContained = this.contains(indexable);
    if (isContained) {
      return false;
    }

    // We know the index is valid because of the contains check
    // Add a 1 bit to the bitmap at the index of the object
    this.bitmap |= (1 << indexable.getIndex());
    this.count++;
    return true;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean remove(Object object) {
    if (!this.contains(object)) {
      return false;
    }

    // We know that the object is an indexable and that the index is valid because of the contains check
    Indexable indexable = (Indexable) object;
    this.bitmap &= ~(1 << indexable.getIndex());
    this.count--;
    return true;
  }

  /**
   * If the collection is an {@link BitSet} this will use bitwise operations to check if all the
   * {@link Indexable indexables} are contained, which is significantly faster than the default
   * implementation. Otherwise, it will iterate through the collection and check that each element
   * is contained.
   *
   * @inheritDoc
   */
  @Override
  public boolean containsAll(Collection<?> collection) {
    if (collection instanceof BitSet<?> otherBitSet) {
      return (this.bitmap & otherBitSet.bitmap) == otherBitSet.bitmap;
    }

    for (Object object : collection) {
      if (!this.contains(object)) {
        return false;
      }
    }

    return true;
  }

  /**
   * If the collection is an {@link BitSet} this will use bitwise operations to add all the elements
   * at once, which is significantly faster than the default implementation. Otherwise, it will
   * iterate through the collection and add each element individually.
   *
   * @inheritDoc
   */
  @Override
  public boolean addAll(Collection<? extends T> collection) {
    if (collection instanceof BitSet<? extends T> otherBitSet) {
      // Combine the bitmaps of the two BitSets
      return this.setBitmap(this.bitmap | otherBitSet.bitmap);
    }

    int oldCount = this.count;
    for (T indexable : collection) {
      this.add(indexable);
    }
    return this.count != oldCount;
  }

  /**
   * If the collection is an {@link BitSet} this will use bitwise operations retain only the
   * elements also in the other collection all at once, which is significantly faster than the
   * default implementation. Otherwise, it will iterate through all the elements in this set and
   * remove each element individually if it's not contained in the collection.
   *
   * @inheritDoc
   */
  @Override
  public boolean retainAll(Collection<?> collection) {
    if (collection instanceof BitSet<?> otherBitSet) {
      // Remove the bits of this BitSet that are not in the other BitSet
      return this.setBitmap(this.bitmap & otherBitSet.bitmap);
    }
    return this.removeIf(indexable -> !collection.contains(indexable));
  }

  /**
   * If the collection is an {@link BitSet} this will use bitwise operations to remove all the
   * elements at once, which is significantly faster than the default implementation. Otherwise, it
   * will iterate through the elements in the collection and remove them individually.
   *
   * @inheritDoc
   */
  @Override
  public boolean removeAll(Collection<?> collection) {
    if (collection instanceof BitSet<?> otherBitSet) {
      // Remove the bits of the other BitSet from this BitSet
      return this.setBitmap(this.bitmap & ~otherBitSet.bitmap);
    }

    // It's likely faster to iterate through the other collection than to iterate through this one
    int oldCount = this.count;
    for (Object object : collection) {
      this.remove(object);
    }
    return this.count != oldCount;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void clear() {
    this.count = 0;
    this.bitmap = 0;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Iterator<T> iterator() {
    if (this.indexableResolver == null) {
      throw new UnsupportedOperationException(
          "You must specify an IndexableResolver to use iterator()");
    }

    return new Iterator<>() {
      private int currentIndex = 0;
      private int currentCount = BitSet.this.count;

      @Override
      public boolean hasNext() {
        return this.currentCount != 0;
      }

      @Override
      public T next() {
        while (this.currentIndex <= MAX_INDEX) {
          if (BitSet.this.containsIndex(this.currentIndex)) {
            this.currentCount--;
            return BitSet.this.indexableResolver.resolveFromIndex(this.currentIndex++);
          }
          this.currentIndex++;
        }

        // This should theoretically never happen because of the check in hasNext()
        return null;
      }
    };
  }

  /**
   * The resulting array will only contain the included elements. They will be ordered based on
   * their {@link Indexable} index, but the index will not match their position in the array, i.e.
   * {@code null} will not be used to pad the array for missing elements.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Object[] toArray() {
    Object[] array = new Object[this.count];
    return this.toArray(array);
  }

  /**
   * The resulting array will only contain the included elements. They will be ordered based on
   * their {@link Indexable} index, but the index will not match their position in the array, i.e.
   * {@code null} will not be used to pad the array for missing elements.
   * <p>
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public <K> K[] toArray(K[] array) {
    if (array.length < this.count) {
      array = (K[]) Array.newInstance(array.getClass().getComponentType(), this.count);
    }
    int index = 0;
    for (T indexable : this) {
      array[index++] = (K) indexable;
    }

    return array;
  }

  /**
   * A custom {@link Collector} that can be used to convert a stream of {@link T indexables} into a
   * {@link BitSet}.
   */
  @RequiredArgsConstructor
  public static class BitSetCollector<T extends Indexable> implements
      Collector<T, BitSet<T>, BitSet<T>> {

    private final IndexableResolver<T> indexableResolver;

    /**
     * @inheritDoc
     */
    @Override
    public Supplier<BitSet<T>> supplier() {
      return () -> new BitSet<>(this.indexableResolver);
    }

    /**
     * @inheritDoc
     */
    @Override
    public BiConsumer<BitSet<T>, T> accumulator() {
      return BitSet::add;
    }

    /**
     * @inheritDoc
     */
    @Override
    public BinaryOperator<BitSet<T>> combiner() {
      return (bitSet1, bitSet2) -> {
        // This is going to be superfast because of the bitwise operations >:)
        bitSet1.addAll(bitSet2);
        return bitSet1;
      };
    }

    /**
     * @inheritDoc
     */
    @Override
    public Function<BitSet<T>, BitSet<T>> finisher() {
      return (bitSet) -> bitSet;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<Characteristics> characteristics() {
      return Set.of(Characteristics.UNORDERED);
    }

  }

}
