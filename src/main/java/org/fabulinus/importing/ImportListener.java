package org.fabulinus.importing;

import org.fabulinus.model.Content;

/**
 * Created by Timon on 06.07.2015.
 */
public interface ImportListener {

    void onProgress(double progress);

    void onImportDone(Content content);

    void onError(String error);
}
