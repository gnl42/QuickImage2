/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.widgets;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

import me.glindholm.plugin.quickimage2.core.ImageHolder;
import me.glindholm.plugin.quickimage2.core.ImageOrganizer;
import me.glindholm.plugin.quickimage2.core.QManager;

/**
 * @author Per Salomonsson
 *
 */
public class QuickImageCanvas extends Canvas {
    private Image originalImage;
    private Image workingImage;
    private Image backImage;
    private final Composite parent;
    private int clientw, clienth, imgx, imgy, imgw, imgh, scrolly, scrollx, mousex, mousey = 1;
    private final Color COLOR_WIDGET_BACKGROUND;
    private boolean listenForMouseMovement = false;
    private Cursor handOpen, handClosed;
    private double zoomScale = 1;
    private final QManager manager;

    public QuickImageCanvas(final QManager manager, final Composite parent, final int style) {
        super(parent, style | SWT.BORDER | SWT.NO_BACKGROUND | SWT.V_SCROLL | SWT.H_SCROLL);

        this.manager = manager;
        COLOR_WIDGET_BACKGROUND = parent.getBackground();

        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(final ControlEvent e) {
                updateScrollbarPosition();
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                eventMouseDoubleClick(e);
            }

            @Override
            public void mouseDown(final MouseEvent e) {
                eventMouseDown(e);
            }

            @Override
            public void mouseUp(final MouseEvent e) {
                listenForMouseMovement = false;
                setCursor(handOpen);
            }
        });

        addMouseMoveListener(e -> {
            if (listenForMouseMovement) {
                followMouse(e);
            }
        });

        addPaintListener(event -> paint(event.gc));

        getHorizontalBar().setEnabled(true);
        getHorizontalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                updateHorizontalScroll((ScrollBar) event.widget);
            }
        });

        getVerticalBar().setEnabled(true);
        getVerticalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                updateVerticalScroll((ScrollBar) event.widget);
            }
        });

        this.parent = parent;
        // updateFullsizeData();
        // setCurrentFullsizeImage(manager.getImageOrganizer().getCurrent().getFullsize());
        // workingImage =
        // manager.getImageOrganizer().getCurrent().getFullsize();
    }

    private void eventMouseDown(final MouseEvent e) {
        listenForMouseMovement = true;
        mousex = e.x;
        mousey = e.y;
        setCursor(handClosed);

        if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_THUMB) {
            manager.getImageOrganizer().selectHolder(e.x, e.y);
            manager.getStatusCanvas().updateWithCurrent();
            manager.getImageEditor().setPartName(manager.getImageOrganizer().getCurrent().getDisplayName());
            redraw();
        }
    }

    private void eventMouseDoubleClick(final MouseEvent e) {
        if (!manager.getImageOrganizer().isSingle()) {
            if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_FULLSIZE) {
                manager.getImageOrganizer().getCurrent().getFullsize().dispose();
                manager.getImageEditor().toggleView();
            } else if (manager.getImageOrganizer().selectHolder(e.x, e.y)) {
                manager.getImageEditor().toggleView();
            }
        }

        manager.getStatusCanvas().updateWithCurrent();

        // redraw();
    }

    public void setIconsPath(final String path) {
        handOpen = new Cursor(getDisplay(), new ImageData(path + "cursor_hand_open.gif"), 10, 0);
        handClosed = new Cursor(getDisplay(), new ImageData(path + "cursor_hand_closed.gif"), 10, 0);
        setCursor(handOpen);
    }

    private void followMouse(final MouseEvent e) {

        if (clientw < imgw) {
            final int mouseDiffX = mousex - e.x;
            mousex = e.x;
            imgx -= mouseDiffX;
            getHorizontalBar().setSelection(getHorizontalBar().getSelection() + mouseDiffX);
            final int minx = clientw - imgw;
            final int maxx = 0;
            if (imgx < minx) {
                imgx = minx;
            }
            if (imgx > maxx) {
                imgx = maxx;
            }
            scrollx = getHorizontalBar().getSelection();
        }
        if (clienth < imgh) {
            final int mouseDiffY = mousey - e.y;
            mousey = e.y;
            imgy -= mouseDiffY;
            getVerticalBar().setSelection(getVerticalBar().getSelection() + mouseDiffY);
            final int miny = clienth - imgh;
            final int maxy = 0;
            if (imgy < miny) {
                imgy = miny;
            }
            if (imgy > maxy) {
                imgy = maxy;
            }

            scrolly = getVerticalBar().getSelection();
        }

        redraw();
    }

    //
    // public void setSourceImage(File file)
    // {
    // if (workingImage != null)
    // workingImage.dispose();
    //
    // workingImage = new Image(getDisplay(), new
    // ImageData(file.getAbsolutePath()));
    // updateScrollbarPosition();
    // }

    void paint(final GC gc) {
        if (backImage != null) {
            backImage.dispose();
        }

        backImage = new Image(getDisplay(), clientw, clienth);

        final GC backGC = new GC(backImage);
        backGC.setBackground(COLOR_WIDGET_BACKGROUND);
        backGC.setClipping(getClientArea());
        backGC.fillRectangle(getClientArea());
        // backGC.drawImage(workingImage, imgx, imgy);

        final ImageOrganizer organizer = manager.getImageOrganizer();

        if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_FULLSIZE) {
            backGC.drawImage(workingImage, imgx, imgy);
        } else
        // draw thumbnails
        {
            final Point thumbDim = organizer.getThumbWidth();

            int cols = clientw / organizer.getThumbWidth().x;
            if (cols < 1) {
                cols = 1;
            }
            int rows = organizer.getCount() / cols;
            if (organizer.getCount() > rows * cols) {
                rows++;
            }

            final List<ImageHolder> holders = organizer.getHolders();
            int index = 0;
            for (int i = 0; i < rows; i++) {
                if (imgy + i * thumbDim.x > clienth) { // make sure that only the
                    // neccessary thumbs are
                    // initiated (and not all ==
                    // very SLOW)
                    break;
                }
                for (int k = 0; k < cols; k++) {
                    if (index == organizer.getCount()) {
                        break;
                    }

                    holders.get(index).drawThumb(backGC, k * thumbDim.x, imgy + i * thumbDim.y);
                    index++;
                }
            }
        }

        gc.drawImage(backImage, 0, 0);
        backGC.dispose();
    }

    private void updateScrollVisibility() {
        // only show when neccessary
        getHorizontalBar().setVisible(clientw < imgw);
        getVerticalBar().setVisible(clienth < imgh);
    }

    private void updateVerticalScroll(final ScrollBar bar) {
        imgy -= bar.getSelection() - scrolly;
        scrolly = bar.getSelection();
        redraw();
    }

    private void updateHorizontalScroll(final ScrollBar bar) {
        imgx -= bar.getSelection() - scrollx;
        scrollx = bar.getSelection();
        redraw();
    }

    public void zoomFit() {

        zoomScale = 1;
        double scaleWidth, scaleHeight = 0;

        scaleWidth = originalImage.getBounds().width - getClientArea().width;
        scaleHeight = originalImage.getBounds().height - getClientArea().height;

        if (scaleWidth > 0) {
            scaleWidth = scaleWidth / originalImage.getBounds().width;
        }
        if (scaleHeight > 0) {
            scaleHeight = scaleHeight / originalImage.getBounds().height;
        }

        if (scaleWidth > scaleHeight && scaleWidth > 0) {
            zoomScale = 1 - scaleWidth;
        } else if (scaleHeight > scaleWidth && scaleHeight > 0) {
            zoomScale = 1 - scaleHeight;
        }

        if (zoomScale < 0.001) {
            zoomScale = 0.001;
        }

        onZoom();
    }

    public void zoomIn() {
        if (zoomScale < 1) {
            zoomScale *= 2;
        } else {
            zoomScale += 0.5;
            if (zoomScale > 4) {
                zoomScale = 4;
            }
        }

        onZoom();
    }

    public void zoomOut() {
        if (zoomScale <= 1) {
            if (zoomScale > 0.001) {
                zoomScale /= 2;
            }
        } else {
            zoomScale -= 0.5;
        }

        onZoom();
    }

    public void zoomOriginal() {
        zoomScale = 1;
        onZoom();
    }

    private void onZoom() {
        int w = (int) (originalImage.getBounds().width * zoomScale);
        int h = (int) (originalImage.getBounds().height * zoomScale);
        if (w < 1) {
            w = 1;
        }
        if (h < 1) {
            h = 1;
        }

        final ImageData imageData = originalImage.getImageData().scaledTo(w, h);
        if (workingImage != null && !workingImage.isDisposed()) {
            workingImage.dispose();
        }

        workingImage = new Image(getDisplay(), imageData);
        updateScrollbarPosition();
    }

    public void updateThumbData() {
        updateScrollbarPosition();
    }

    public void updateFullsizeData() {
        disposeImages();
        zoomScale = 1;
        originalImage = manager.getImageOrganizer().getCurrent().getFullsize();
        workingImage = new Image(getDisplay(), originalImage.getImageData());

        updateScrollbarPosition();
    }

    public void rotate() {
        final ImageData originalData = workingImage.getImageData();
        final PaletteData originalPalette = originalData.palette;
        ImageData tmpData;
        PaletteData tmpPalette;

        if (originalPalette.isDirect) {
            tmpPalette = new PaletteData(originalPalette.redMask, originalPalette.greenMask, originalPalette.blueMask);
        } else {
            tmpPalette = new PaletteData(originalPalette.getRGBs());
        }

        tmpData = new ImageData(originalData.height, originalData.width, originalData.depth, tmpPalette);

        tmpData.transparentPixel = originalData.transparentPixel;

        for (int i = 0; i < originalData.width; i++) {
            for (int k = 0; k < originalData.height; k++) {
                tmpData.setPixel(k, originalData.width - 1 - i, originalData.getPixel(i, k));
            }
        }

        if (workingImage != null) {
            workingImage.dispose();
        }
        workingImage = new Image(getDisplay(), tmpData);
        updateScrollbarPosition();
    }

    private void updateScrollbarPosition() {
        clientw = getClientArea().width;
        clienth = getClientArea().height;
        if (clientw < 1) {
            clientw = 1;
        }
        if (clienth < 1) {
            clienth = 1;
        }

        if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_FULLSIZE) {
            imgh = workingImage.getBounds().height;
            imgw = workingImage.getBounds().width;
        } else {
            final ImageOrganizer organizer = manager.getImageOrganizer();
            int cols = clientw / organizer.getThumbWidth().x;
            if (cols < 1) {
                cols = 1;
            }
            int rows = organizer.getCount() / cols;
            if (organizer.getCount() > rows * cols) {
                rows++;
            }

            imgh = organizer.getThumbWidth().y * rows;
            imgw = clientw / organizer.getThumbWidth().x;
        }

        updateScrollVisibility();
        getVerticalBar().setSelection(0);
        getHorizontalBar().setSelection(0);
        imgx = clientw / 2 - imgw / 2;
        imgy = clienth / 2 - imgh / 2;

        if (imgx < 0) {
            imgx = 0;
        }
        if (imgy < 0) {
            imgy = 0;
        }

        scrollx = getHorizontalBar().getSelection();
        scrolly = getVerticalBar().getSelection();

        final ScrollBar vertical = getVerticalBar();
        vertical.setMaximum(imgh);
        vertical.setThumb(Math.min(clienth, imgh));
        vertical.setIncrement(40);
        vertical.setPageIncrement(clienth);

        final ScrollBar horizontal = getHorizontalBar();
        horizontal.setMaximum(imgw);
        horizontal.setThumb(Math.min(clientw, imgw));
        horizontal.setIncrement(40);
        horizontal.setPageIncrement(clientw);

        redraw();
    }

    private void disposeImages() {
        if (originalImage != null && !originalImage.isDisposed()) {
            originalImage.dispose();
        }
        if (workingImage != null && !workingImage.isDisposed()) {
            workingImage.dispose();
        }
        if (backImage != null && !backImage.isDisposed()) {
            backImage.dispose();
        }

    }

    @Override
    public void dispose() {
        disposeImages();

        if (handOpen != null && !handOpen.isDisposed()) {
            handOpen.dispose();
        }
        if (handClosed != null && !handClosed.isDisposed()) {
            handClosed.dispose();
        }

        super.dispose();
    }
}