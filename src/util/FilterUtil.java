package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
* Utility for applying a Filter object to a list of ISearchable items.
* Returns a filtered list based on matching logic.
* 
* @author Javier
* @version 1.0
*/
public class FilterUtil {
    /**
    * Filters a collection based on the given filter rules.
    *
    * @param items The generic type of items to filter.
    * @param filter The filter criteria.
    * @return A new list of filtered items.
    */
    public static <T extends ISearchable> List<T> applyFilter(Collection<T> items, Filter filter) {
        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>(items); // no filter applied
        }

        return items.stream()
                    .filter(item -> item.matches(filter))
                    .collect(Collectors.toList());
    }
}
