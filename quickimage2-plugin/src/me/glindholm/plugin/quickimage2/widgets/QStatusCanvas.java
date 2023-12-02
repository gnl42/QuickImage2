/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.widgets;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import me.glindholm.plugin.quickimage2.core.ImageHolder;
import me.glindholm.plugin.quickimage2.core.ImageOrganizer;
import me.glindholm.plugin.quickimage2.core.QManager;

/**
 * @author Per Salomonsson
 * 
 */
public class QStatusCanvas extends Canvas {
    private String filesize = "";
    private int height = 0;
    private int width = 0;
    private String filename = "";
    private int depth = 0;
    private Image image;
    private final DecimalFormat df = new DecimalFormat("0.000");
    private final Color COLOR_DARK_GRAY;
    private final QManager manager;

    public QStatusCanvas(final QManager manager, final Composite parent, final int style) {
        // super(parent, style | SWT.BORDER);
        super(parent, style | SWT.FLAT);
        this.manager = manager;

        addPaintListener(event -> paint(event.gc));
        COLOR_DARK_GRAY = parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        // separator = new Color(getDisplay(), 140,140,140);
        // setBackground(new Color(parent.getDisplay(), 255,0,0));
    }

    public void updateWithCurrent() {
        if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_FULLSIZE) {
            image = manager.getImageOrganizer().getCurrent().getFullsize();
            if (image != null) {
                depth = image.getImageData().depth;
                width = image.getBounds().width;
                height = image.getBounds().height;
            }
        }

        final ImageHolder holder = manager.getImageOrganizer().getCurrent();
        filename = holder.getDisplayName();
        filesize = df.format(holder.getImageSize());
        if (holder.getImageSize() == 0) {
            filesize = "unknown";
        }
        redraw();
    }

    void paint(final GC gc) {
        if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_FULLSIZE) {
            gc.drawString("Size (kb): " + filesize, 5, 1);
            gc.drawString("Depth: " + depth, 140, 1);
            gc.drawString(width + " x " + height, 225, 1);
            gc.drawString("Name: " + filename, 325, 1);
            gc.setForeground(COLOR_DARK_GRAY);
            gc.drawLine(135, 0, 135, 24);
            gc.drawLine(210, 0, 210, 24);
            gc.drawLine(320, 0, 320, 24);
        } else {
            gc.drawString("Size (kb): " + filesize, 5, 1);
            gc.drawString("Name: " + filename, 140, 1);
            gc.setForeground(COLOR_DARK_GRAY);
            gc.drawLine(135, 0, 135, 24);
        }
    }
}
