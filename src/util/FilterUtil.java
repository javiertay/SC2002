package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FilterUtil {
    public static <T extends ISearchable> List<T> applyFilter(Collection<T> items, Filter filter) {
        if (filter == null || filter.isEmpty()) {
            return new ArrayList<>(items); // no filter applied
        }

        return items.stream()
                    .filter(item -> item.matches(filter))
                    .collect(Collectors.toList());
    }
}
