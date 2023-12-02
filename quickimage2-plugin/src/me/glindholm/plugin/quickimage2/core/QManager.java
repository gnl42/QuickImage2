/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.core;

import me.glindholm.plugin.quickimage2.editors.QuickImageEditor;
import me.glindholm.plugin.quickimage2.widgets.QStatusCanvas;
import me.glindholm.plugin.quickimage2.widgets.QuickImageCanvas;

/**
 * @author Per Salomonsson
 * 
 */
public class QManager {
    private QuickImageCanvas imageCanvas;
    private QStatusCanvas statusCanvas;
    private ImageOrganizer imageOrganizer;
    private QuickImageEditor imageEditor;

    public QuickImageCanvas getImageCanvas() {
        return imageCanvas;
    }

    public void setImageCanvas(final QuickImageCanvas imageCanvas) {
        this.imageCanvas = imageCanvas;
    }

    public QuickImageEditor getImageEditor() {
        return imageEditor;
    }

    public void setImageEditor(final QuickImageEditor imageEditor) {
        this.imageEditor = imageEditor;
    }

    public ImageOrganizer getImageOrganizer() {
        return imageOrganizer;
    }

    public void setImageOrganizer(final ImageOrganizer imageOrganizer) {
        this.imageOrganizer = imageOrganizer;
    }

    public QStatusCanvas getStatusCanvas() {
        return statusCanvas;
    }

    public void setStatusCanvas(final QStatusCanvas statusCanvas) {
        this.statusCanvas = statusCanvas;
    }
}
