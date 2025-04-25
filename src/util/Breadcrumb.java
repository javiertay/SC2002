package util;

import java.util.ArrayList;
import java.util.List;

/**
* Utility for tracking user navigation paths within the CLI system.
* Used to display breadcrumb-style paths.
* 
* @author Javier
* @version 1.0
*/
public class Breadcrumb {
    private final List<String> trail = new ArrayList<>();

    /**
    * Adds a new label to the breadcrumb trail.
    *
    * @param label The name of the current menu or section.
    */
    public void push(String label) {
        trail.add(label);
    }

    /**
    * Removes the most recent label from the trail.
    */
    public void pop() {
        if (!trail.isEmpty()) trail.remove(trail.size() - 1);
    }

    /**
    * Returns the full breadcrumb path joined by arrows.
    *
    * @return Formatted breadcrumb string.
    */
    public String getPath() {
        return String.join(" > ", trail);
    }
}
