package org.fabulinus.model;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Timon on 06.07.2015.
 */
public class Content {
    private final String name;
    private final Content parent;
    private final HashMap<String, Content> children;

    public Content(String name, Content parent) {
        this.name = name;
        this.parent = parent;
        this.children = new HashMap<>();
    }

    public Content getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public Collection<Content> getChildren() {
        return children.values();
    }

    public void addChild(Content child){
        children.put(child.getName(), child);
    }

    public Content findOrCreate(@Nonnull String... contentNames){
        if (contentNames.length == 0){
            return this;
        } else {
            String contentName = contentNames[0];
            Content child = children.get(contentName);
            if (child == null){
                child = new Content(contentName, this);
                addChild(child);
            }
            String[] subCopy = Arrays.copyOfRange(contentNames, 1, contentNames.length);
            return child.findOrCreate(subCopy);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null){
            if (obj == this){
                return true;
            }
            if (obj instanceof Content){
                return name.equals(((Content) obj).name);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public String description(){
        String result = String.format("%-40s", name);
        Content parent = this.parent;
        while (parent != null){
            result = String.format("%-40s %-40s", parent.name, result);
            parent = parent.parent;
        }
        return result;
    }
}
