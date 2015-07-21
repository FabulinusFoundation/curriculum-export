package org.fabulinus.exporting;

import org.fabulinus.model.Content;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Timon on 21.07.2015.
 */
public class Exporter extends Thread {
    private final File fileToWrite;
    private final List<Content> selectedContent;

    public Exporter(File fileToWrite, List<Content> selectedContent) {
        this.fileToWrite = fileToWrite;
        this.selectedContent = selectedContent;
    }

    @Override
    public void run() {
        try {
            FileWriter writer = new FileWriter(fileToWrite);
            BufferedWriter out = new BufferedWriter(writer);
            Map<ComparableContent, List<ComparableContent>> grouped = groupByParent(selectedContent);
            List<ComparableContent> sorted = new ArrayList<>(grouped.keySet());
            Collections.sort(sorted);
            for (ComparableContent content : sorted) {
                out.append(content.description());
                out.newLine();
                List<ComparableContent> contents = grouped.get(content);
                Collections.sort(contents);
                for (ComparableContent cc : contents) {
                    out.append("\t").append(cc.getName());
                    out.newLine();
                }
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
    }

    private Map<ComparableContent, List<ComparableContent>> groupByParent(List<Content> selectedContent){
        Map<ComparableContent, List<ComparableContent>> result = new TreeMap<>();
        for (Content content : selectedContent) {
            ComparableContent cc = new ComparableContent(content.getParent());
            List<ComparableContent> contents = result.get(cc);
            if (contents == null){
                contents = new ArrayList<>();
            }
            contents.add(new ComparableContent(content));
            result.put(cc, contents);
        }
        return  result;
    }

    private class ComparableContent extends Content implements Comparable<Content> {

        public ComparableContent(@Nonnull Content content) {
            super(content.getName(), content.getParent(), content.getRow());
        }

        @Override
        public int compareTo(@Nonnull Content o) {
            return Integer.compare(this.getRow(), o.getRow());
        }
    }
}
