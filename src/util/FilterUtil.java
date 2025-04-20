package util;

import java.util.List;
import java.util.stream.Collectors;

public class FilterUtil {
    public static <T extends Searchable> List<T> applyFilter(List<T> items, Filter filter) {
        if (filter == null || filter.isEmpty()) {
            return items; // no filter applied
        }

        return items.stream()
                    .filter(item -> item.matches(filter))
                    .collect(Collectors.toList());
    }
}
