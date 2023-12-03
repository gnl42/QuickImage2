/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * @author Per Salomonsson
 *
 */
public class ImageOrganizer {
    public static final int VIEW_THUMB = 0;
    public static final int VIEW_FULLSIZE = 1;

    public String path;
    public String filename;
    private int index = 0;

    private final List<ImageHolder> holders = new ArrayList<>();
    // private ImageHolder[] holders;
    private final Display display;
    private int activeView = VIEW_FULLSIZE;
    private final QManager manager;

    public ImageOrganizer(final QManager manager, final Display display) {
        this.manager = manager;
        this.display = display;
    }

    /**
     * Can be used instead of setPath
     *
     */
    public void setStorage(final IStorage storage) {
        path = null;
        filename = null;
        holders.clear();
        final ImageHolder holder = new ImageHolder(manager, display);
        holder.setStorage(storage);
        holders.add(holder);
    }

    public void setPath(final String path, final String filename) {
        this.path = path;
        this.filename = filename;
        initList();
    }

    private ImageHolder holder(final int index) {
        return holders.get(index);
    }

    public void removeHolder(final ImageHolder holder) {
        holders.remove(holder);
        while (index >= holders.size() && index > 0) {
            index--;
        }

        manager.getImageEditor().setPartName(getCurrent().getDisplayName());
    }

    public void removeHolder(int index) {
        holders.remove(index);

        while (index >= holders.size() && index > 0) {
            index--;
        }
    }

    public ImageHolder getNext() {
        if (holder(index).getFullsize() != null && !holder(index).getFullsize().isDisposed()) {
            holder(index).getFullsize().dispose();
        }

        index = (index + 1) % holders.size();

        return holder(index);
    }

    public int getCount() {
        return holders.size();
    }

    public boolean isSingle() {
        return getHolders().size() == 1;
    }

    public List<ImageHolder> getHolders() {
        return holders;
    }

    public ImageHolder getPrevious() {
        if (holder(index).getFullsize() != null && !holder(index).getFullsize().isDisposed()) {
            holder(index).getFullsize().dispose();
        }

        if (index > 0) {
            index--;
        }

        return holder(index);
    }

    public boolean hasNext() {
        return holders.size() - 1 != index;
    }

    public boolean hasPrevious() {
        return index != 0;
    }

    public ImageHolder getCurrent() {
        return holder(index);
    }

    public int getTotalWidth() {
        return 0;
    }

    private void initList() {
        final File f = new File(path);
        final File[] files = f.listFiles((FileFilter) name -> !name.getName().startsWith("."));

        if (files != null) {
            final String[] sfiles = new String[files.length];

            for (int i = 0; i < files.length; i++) {
                sfiles[i] = files[i].getAbsolutePath();
            }

            Arrays.sort(sfiles, String.CASE_INSENSITIVE_ORDER);

            for (int i = 0; i < sfiles.length; i++) {
                if (sfiles[i].endsWith(filename)) {
                    index = i;
                }

                final ImageHolder h = new ImageHolder(manager, display);
                h.setFile(new File(sfiles[i]));
                holders.add(h);
            }
        }
    }

    public Point getThumbWidth() {
        if (holders == null) {
            return new Point(1, 1);
        }

        return holder(0).getThumbDimension();
    }

    public int getActiveView() {
        return activeView;
    }

    public void setActiveView(final int activeView) {
        this.activeView = activeView;
    }

    public boolean selectHolder(final int x, final int y) {
        boolean tmp = false;
        boolean found = false;
        for (int i = 0; i < holders.size(); i++) {
            tmp = holder(i).mouseClickedOver(x, y);
            if (tmp && !found) {
                if (holder(index).hasFullsize() && !holder(index).getFullsize().isDisposed()) {
                    holder(index).getFullsize().dispose();
                }
                index = i;
                found = true;
                holder(i).setSelected(true);
            } else {
                holder(i).setSelected(false);
            }
        }

        return found;
    }

    public void setCurrentToSelected() {
        for (int i = 0; i < holders.size(); i++) {
            if (holder(i).isSelected()) {
                holder(index).getFullsize().dispose();
                holder(i).setSelected(false);
            }
        }
        getCurrent().setSelected(true);
    }

    public void setSelectedToCurrent() {
        for (int i = 0; i < holders.size(); i++) {
            if (holder(i).isSelected()) {
                holder(index).getFullsize().dispose();
                index = i;
                break;
            }
        }
    }

    public void dispose() {
        for (int i = 0; i < holders.size(); i++) {
            holder(i).dispose();
        }
    }
}

class ImageFileFilter implements FileFilter {
    String[] patterns = { ".bmp", ".cur", ".gif", ".ico", ".incs", ".jpeg", ".jpg", ".pbm", ".pcx", ".pgm", ".pict", ".png", ".pnm", ".ppm", ".psd", ".sgi",
            ".svg", ".tga", ".tif", ".tiff", ".webp", ".xpm", ".xwd" };

    @Override
    public boolean accept(final File f) {
        for (final String pattern : patterns) {
            if (f.getName().toLowerCase().endsWith(pattern)) {
                return true;
            }
        }

        return false;
    }
}
