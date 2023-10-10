package nz.ac.auckland.se306.group12.models.datastructures;

public interface IndexableResolver<T extends Indexable> {

  /**
   * Resolves the corresponding {@link Indexable} from a given index.
   *
   * @param index The index to resolve
   * @return The {@link Indexable} corresponding to the given index
   */
  T resolveFromIndex(int index);

}
