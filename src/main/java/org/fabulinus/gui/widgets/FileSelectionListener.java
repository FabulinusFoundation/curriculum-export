package org.fabulinus.gui.widgets;

import java.io.File;

/**
 * Created by Timon on 09.07.2015.
 */
public interface FileSelectionListener {

    void onFileSelected(File file);

    void onError(String error);
}
