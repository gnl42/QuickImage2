/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.widgets;

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

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
    private static final DecimalFormat df = new DecimalFormat("0.000");
    private final Color COLOR_DARK_GRAY;
    private final QManager manager;
    private final Composite parent;

    public QStatusCanvas(final QManager manager, final Composite parent, final int style) {
        // super(parent, style | SWT.BORDER);
        super(parent, style | SWT.FLAT);
        this.parent = parent;
        this.manager = manager;

        addPaintListener(event -> paint(event.gc));
        COLOR_DARK_GRAY = parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        // separator = new Color(getDisplay(), 140,140,140);
        // setBackground(new Color(parent.getDisplay(), 255,0,0));
    }

    public void updateWithCurrent() {
        ImageHolder current = manager.getImageOrganizer().getCurrent();

        image = current.getFullsize();
        if (image != null) {
            depth = image.getImageData().depth;
            width = image.getBounds().width;
            height = image.getBounds().height;
        }

        filename = current.getDisplayName();
        filesize = format(current.getImageSize());
        if (current.getImageSize() == 0) {
            filesize = "unknown";
        }
        redraw();
    }

    private static String format(long size) {
        if (size < 0) {
            return "-";
        }
        if (size < 1024) {
            // format as byte
             DecimalFormat fmt = new DecimalFormat("0 bytes");
            return fmt.format(size);
        } else if (size >= 1024 && size <= 1048575) {
            // format as KB
            double formattedValue = size / 1024.0;
            df.applyPattern("0.00 KB");
            return df.format(formattedValue);
        } else if (size >= 1048576 && size <= 1073741823) {
            // format as MB
            double formattedValue = size / 1048576.0;
            df.applyPattern("0.00 MB");
            return df.format(formattedValue);
        }

        // format as GB
        double formattedValue = size / 1073741824.0;
        df.applyPattern("0.00 GB");
        return df.format(formattedValue);
    }


    void paint(final GC gc) {
        int x = 0;
        int canvasHeight = this.getSize().y;

        x += Math.max(addText(filesize, x, gc), 110);
        gc.drawLine(x, 0, x, canvasHeight);

        x += Math.max(addText("Depth: " + depth, x, gc), 100);
        gc.drawLine(x, 0, x, canvasHeight);

        x += Math.max(addText(width + "x" + height, x, gc), 105);
        gc.drawLine(x, 0, x, canvasHeight);

        addText("Name: " + filename, x, gc);

        gc.setForeground(COLOR_DARK_GRAY);
    }

    private int addText(String text, int startPos, GC gc) {
        Text size = new Text(parent, SWT.BORDER);
        size.setText(text);
        Point point = size.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        gc.drawString(text, startPos + 5, 1);

        return point.x + 5;
    }
}
