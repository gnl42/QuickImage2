/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.core;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SwtCallable;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * @author Per Salomonsson
 *
 */
public class ImageHolder {

    private Image thumb;
    private Image fullsize;
    private IStorage storage;
    private File file;
    private String displayName = null;
    private final Display display;
    private final int space = 16;
    private int absx, absy = 0;
    private final Point dim = new Point(140, 160);
    private boolean selected = false;
    private final Color colorGray;
    private final Color colorTitleBackground;
    private final QManager manager;
    private long imageSize = 0;
    static Font font;
    private final File unavailable;

    // TODO add "implements comparable" etc

    public ImageHolder(final QManager manager, final Display display) {
        this.manager = manager;
        this.display = display;
        colorGray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        colorTitleBackground = display.getSystemColor(SWT.COLOR_TITLE_BACKGROUND);

        if (font == null) {
            try {
                font = new Font(display, "", 8, SWT.NORMAL);
            } catch (final Exception e) {
                font = display.getSystemFont();
            }
            dim.y = dim.x + font.getFontData()[0].getHeight() + 10;
        }

        unavailable = new File(manager.getImageEditor().getIconsdir() + "broken_image.gif");

    }

    /**
     * Use Point to transport x= width and y=height of the area that each thumb needs. including text
     *
     * @return
     */
    public Point getThumbDimension() {
        return dim;
    }

    public void drawThumb(final GC gc, final int inX, final int inY) {
        int x = inX;
        int y = inY;

        try {
            if (thumb == null) {
                initThumb();
            }

            final Color c = gc.getForeground();
            if (isSelected()) {
                gc.setLineWidth(3);
                gc.setForeground(colorTitleBackground);
            } else {
                gc.setForeground(colorGray);
            }

            final Rectangle rect = new Rectangle(x + space / 2, y + space / 2, dim.x - space, dim.x - space);
            final Rectangle rectClip = new Rectangle(rect.x - 2, rect.y - 2, rect.width + 4, dim.y);
            gc.setClipping(rectClip);

            gc.drawRectangle(rect);

            gc.setFont(font);
            gc.setForeground(colorTitleBackground);
            gc.drawString(getDisplayName(), rect.x, rect.y + rect.height + 2, true);

            absx = x;
            absy = y;
            x += dim.x / 2 - thumb.getBounds().width / 2;
            y += dim.x / 2 - thumb.getBounds().height / 2;
            gc.drawImage(thumb, x, y);
            gc.setLineWidth(1);
            gc.setForeground(c);
        } catch (final RuntimeException | InterruptedException | ExecutionException | IOException e) {
            replaceWithCannotDisplayImage();
            drawThumb(gc, inX, inY);
            // manager.getImageOrganizer().removeHolder(this);
            // e.printStackTrace();
        }
    }

    public boolean mouseClickedOver(final int x, final int y) {
        return x > absx && x < absx + dim.x && y > absy && y < absy + dim.y;
    }

    public void drawFullsize(final GC gc, final int inX, final int inY) {
        final int x = inX;
        final int y = inY;

        try {
            if (fullsize == null || fullsize.isDisposed()) {
                initFullsize();
            }

            gc.drawImage(fullsize, x, y);
        } catch (final RuntimeException | IOException | CoreException | InterruptedException | ExecutionException e) {
            replaceWithCannotDisplayImage();
            drawFullsize(gc, inX, inY);
        }
    }

    public IStorage getStorage() {
        return storage;
    }

    public void setStorage(final IStorage storage) {
        this.storage = storage;
        setDisplayName(storage.getName());
    }

    public long getImageSize() {
        return imageSize;
    }

    public void setFile(final File file) {
        this.file = file;
        setDisplayName(file.getName());
        imageSize = file.length();
    }

    private void initFullsize() throws RuntimeException, FileNotFoundException, IOException, CoreException, InterruptedException, ExecutionException {
        if (fullsize != null && !fullsize.isDisposed()) {
            fullsize.dispose();
        }

        fullsize = getImage(
                file != null ? new FileImageInputStream(new File(file.getAbsolutePath())) : new FileCacheImageInputStream(storage.getContents(), null), false);
    }

    public Image getFullsize() {
        try {
            if (fullsize == null || fullsize.isDisposed()) {
                initFullsize();
            }
        } catch (final RuntimeException | IOException | CoreException | InterruptedException | ExecutionException e) {
            replaceWithCannotDisplayImage();
            try {
                initFullsize();
            } catch (RuntimeException | IOException | CoreException | InterruptedException | ExecutionException e1) {
            }
        }

        return fullsize;
    }

    private Image getImage(final ImageInputStream in, final boolean createThumb) throws InterruptedException, ExecutionException {
        final SwtCallable<Image, Exception> load2 = () -> {
            final BufferedImage img = ImageIO.read(in);
            if (img != null) {
                ImageData data = convertToSWT(img);
                if (createThumb) {
                    float w = 0;
                    float h = 0;
                    boolean doscale = false;

                    if (data.height > dim.x - space * 2) {
                        doscale = true;
                        h = (float) (data.height - (dim.x - space * 2)) / data.height;
                    }
                    if (data.width > dim.x - space * 2) {
                        doscale = true;
                        w = (float) (data.width - (dim.x - space * 2)) / data.width;
                    }

                    if (doscale) {
                        final float scale = Math.max(w, h);
                        w = data.width - data.width * scale;
                        h = data.height - data.height * scale;
                        if (w < 1) {
                            w = 1;
                        }
                        if (h < 1) {
                            h = 1;
                        }
                        data = data.scaledTo((int) w, (int) h);
                    }
                }
                return new Image(display, data);
            } else {
                throw new IOException("Unable to load");
            }
        };
        return BusyIndicator.compute(load2).get();
    }

    /**
     * Returns if the fullsize image has been initialize or not.. ie null or not.
     */
    public boolean hasFullsize() {
        if (fullsize != null && !fullsize.isDisposed()) {
            return true;
        }

        return false;
    }

    private void initThumb() throws FileNotFoundException, InterruptedException, ExecutionException, IOException {
        if (thumb != null) {
            thumb.dispose();
        }

        thumb = getImage(new FileImageInputStream(new File(file.getAbsolutePath())), true);
    }

    public void dispose() {
        if (fullsize != null && !fullsize.isDisposed()) {
            fullsize.dispose();
        }

        if (thumb != null && !thumb.isDisposed()) {
            thumb.dispose();
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    private void replaceWithCannotDisplayImage() {
        try {
            dispose();
            fullsize = null;
            thumb = null;
            final String tmpName = getDisplayName();
            setFile(unavailable);
            setDisplayName(tmpName + " (image could not be displayed)");
        } catch (final Exception e) {
            manager.getImageOrganizer().removeHolder(this);
        }
    }

    /**
     * From: https://stackoverflow.com/questions/6498467/conversion-from-bufferedimage-to-swt-image
     *
     * snippet 156: convert between SWT Image and AWT BufferedImage.
     * <p>
     * For a list of all SWT example snippets see https://www.eclipse.org/swt/snippets/
     */
    private static ImageData convertToSWT(final BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            /*
             * DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel(); PaletteData
             * palette = new PaletteData( colorModel.getRedMask(), colorModel.getGreenMask(),
             * colorModel.getBlueMask()); ImageData data = new ImageData(bufferedImage.getWidth(),
             * bufferedImage.getHeight(), colorModel.getPixelSize(), palette); WritableRaster raster =
             * bufferedImage.getRaster(); int[] pixelArray = new int[3]; for (int y = 0; y < data.height; y++) {
             * for (int x = 0; x < data.width; x++) { raster.getPixel(x, y, pixelArray); int pixel =
             * palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2])); data.setPixel(x, y,
             * pixel); } }
             */
            final DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
            final PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
            final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    final int rgb = bufferedImage.getRGB(x, y);
                    final int pixel = palette.getPixel(new RGB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                    if (colorModel.hasAlpha()) {
                        data.setAlpha(x, y, rgb >> 24 & 0xFF);
                    }
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            final IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
            final int size = colorModel.getMapSize();
            final byte[] reds = new byte[size];
            final byte[] greens = new byte[size];
            final byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            final RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
            }
            final PaletteData palette = new PaletteData(rgbs);
            final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            final WritableRaster raster = bufferedImage.getRaster();
            final int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        } else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            final ComponentColorModel colorModel = (ComponentColorModel) bufferedImage.getColorModel();
            // ASSUMES: 3 BYTE BGR IMAGE TYPE
            final PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
            final ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
            // This is valid because we are using a 3-byte Data model with no transparent
            // pixels
            data.transparentPixel = -1;
            final WritableRaster raster = bufferedImage.getRaster();
            final int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    final int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        return null;
    }

    @Override
    public String toString() {
        return "ImageHolder [displayName=" + displayName + "]";
    }
}
