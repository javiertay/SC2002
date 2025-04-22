package util;

import java.util.ArrayList;
import java.util.List;

public class Breadcrumb {
    private final List<String> trail = new ArrayList<>();

    public void push(String label) {
        trail.add(label);
    }

    public void pop() {
        if (!trail.isEmpty()) trail.remove(trail.size() - 1);
    }

    public String getPath() {
        return String.join(" > ", trail);
    }
}
