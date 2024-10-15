/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2;

import java.util.List;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.twelvemonkeys.imageio.plugins.bmp.BMPImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.CURImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.dcx.DCXImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.dds.DDSImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.icns.ICNSImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.iff.IFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.pcx.PCXImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.pict.PICTImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.pnm.PAMImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.pnm.PNMImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.pntg.PNTGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.sgi.SGIImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.svg.SVGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tga.TGAImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.thumbsdb.ThumbsDBImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tiff.BigTIFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.wmf.WMFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.xwd.XWDImageReaderSpi;

import edu.illinois.library.imageio.xpm.XPMImageReaderSpi;

/**
 * @author Per Salomonsson
 *
 */
public class QuickImagePlugin extends AbstractUIPlugin {
    private static QuickImagePlugin plugin;
    // private ResourceBundle resourceBundle;
    public static final String PLUGIN_ID = "me.glindholm.plugin.quickimage2.editors.QuickImageEditor";

    private static final List<ImageReaderSpi> readers = List.of(new BigTIFFImageReaderSpi(), //
            new BMPImageReaderSpi(), //
            new CURImageReaderSpi(), //
            new DCXImageReaderSpi(), //
            new DDSImageReaderSpi(), //
            new ICNSImageReaderSpi(), //
            new ICOImageReaderSpi(), //
            new IFFImageReaderSpi(), //
            new JPEGImageReaderSpi(), //
            new PAMImageReaderSpi(), //
            new PCXImageReaderSpi(), //
            new PICTImageReaderSpi(), //
            new PNTGImageReaderSpi(), //
            new PNMImageReaderSpi(), //
            new PSDImageReaderSpi(), //
            new SGIImageReaderSpi(), //
            new SVGImageReaderSpi(), //
            new TGAImageReaderSpi(), //
            new TIFFImageReaderSpi(), //
            new ThumbsDBImageReaderSpi(), //
            new WebPImageReaderSpi(), //
            new XPMImageReaderSpi(), //
            new XWDImageReaderSpi(), //
            new WMFImageReaderSpi());

    public QuickImagePlugin() {
        plugin = this;
        // try {
        // resourceBundle = ResourceBundle
        // .getBundle("me.glindholm.plugin.quickimage2.QuickimagePluginResources");
        // } catch (MissingResourceException x) {
        // resourceBundle = null;
        // }
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        for (final ImageReaderSpi readerInstance : readers) {
            IIORegistry.getDefaultInstance().registerServiceProvider(readerInstance);
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
    }

    public static QuickImagePlugin getDefault() {
        return plugin;
    }

    // public static String getResourceString(String key) {
    // ResourceBundle bundle = QuickImagePlugin.getDefault()
    // .getResourceBundle();
    // try {
    // return (bundle != null) ? bundle.getString(key) : key;
    // } catch (MissingResourceException e) {
    // return key;
    // }
    // }
    //
    // public ResourceBundle getResourceBundle() {
    // return resourceBundle;
    // }
}