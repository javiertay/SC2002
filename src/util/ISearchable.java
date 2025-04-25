package util;

/**
* Interface implemented by classes that support filter-based searching.
* Required by FilterUtil to apply dynamic filter rules.
* 
* @author Javier
* @version 1.0
*/
public interface ISearchable {
    /**
    * Determines whether the current object matches the given filter criteria.
    *
    * @param filter The filter to apply.
    * @return True if the object matches the filter; false otherwise.
    */
    boolean matches(Filter filter);
}
