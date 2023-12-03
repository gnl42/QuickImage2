/*
 * Software released under Common Public License (CPL) v1.0
 */
package me.glindholm.plugin.quickimage2.editors;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import me.glindholm.plugin.quickimage2.QuickImagePlugin;
import me.glindholm.plugin.quickimage2.core.ImageOrganizer;
import me.glindholm.plugin.quickimage2.core.QManager;
import me.glindholm.plugin.quickimage2.util.LogUtil;
import me.glindholm.plugin.quickimage2.widgets.QStatusCanvas;
import me.glindholm.plugin.quickimage2.widgets.QuickImageCanvas;

/**
 * @author Per Salomonsson
 *
 */
public class QuickImageEditor extends EditorPart {
    private ToolItem previous, next, rotate, zoomIn, zoomOut, zoomOrg, zoomFit, view;
    private Composite parent;
    private String iconsdir;
    private QManager manager;

    private Image previousImage;
    private Image nextImage;
    private Image rotateImage;
    private Image zoomInImage;
    private Image zoomOutImage;
    private Image zoom100Image;
    private Image zoomFitImage;
    private Image viewThumb;
    private Image viewFullsize;

    @Override
    public void createPartControl(final Composite parent) {
        this.parent = parent;
        // fileorg = new FileOrganizer();
        manager = new QManager();
        manager.setImageOrganizer(new ImageOrganizer(manager, parent.getDisplay()));
        manager.setImageEditor(this);

        try {
            // iconsdir =
            // FileLocator.resolve(QuickImagePlugin.getDefault().getBundle().getEntry("/")).getFile() + "icons"
            // + File.separator;
            URL dir = FileLocator.find(QuickImagePlugin.getDefault().getBundle(), new Path("icons"), null);
            dir = FileLocator.toFileURL(dir);
            iconsdir = dir.getPath() + File.separator;
        } catch (final IOException e1) {
            LogUtil.error(e1);
        }

        // find out what kind the resource(s) to load is
        final IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof FileEditorInput) {
            // opened from file system so lets se what is in the current
            // directory as well
            final FileEditorInput fileEditorInput = (FileEditorInput) getEditorInput();
            final IEditorInput file = getEditorInput();
            setPartName(file.getName());

            manager.getImageOrganizer().setPath(fileEditorInput.getPath().removeLastSegments(1).toOSString(), fileEditorInput.getName());
        } else if (editorInput.getAdapter(ILocationProvider.class) != null) {
            final ILocationProvider location = editorInput.getAdapter(ILocationProvider.class);
            final IPath path = location.getPath(editorInput);
            setPartName(editorInput.getName());
            manager.getImageOrganizer().setPath(path.removeLastSegments(1).toOSString(), editorInput.getName());
        } else if (editorInput instanceof final IStorageEditorInput storageEditorInput) {
            IStorage storage;
            try {
                storage = storageEditorInput.getStorage();
                setPartName(storage.getName());
                manager.getImageOrganizer().setStorage(storage);
            } catch (final CoreException e) {
                e.printStackTrace();
            }
        } else if (editorInput.getAdapter(IFile.class) != null) {
            final IFile file = editorInput.getAdapter(IFile.class);
            setPartName(file.getName());
            manager.getImageOrganizer().setPath(file.getLocation().removeLastSegments(1).toOSString(), file.getName());
        } else {
            // could not display image, show err message instead
            LogUtil.error("could not display image for " + editorInput);
        }

        initElements();

        manager.getImageOrganizer().setActiveView(ImageOrganizer.VIEW_FULLSIZE);
        manager.getImageCanvas().setIconsPath(iconsdir);
        manager.getImageCanvas().updateFullsizeData();
        manager.getStatusCanvas().updateWithCurrent();
    }

    private void initElements() {
        final FormLayout layout = new FormLayout();
        final Composite compos = new Composite(parent, SWT.NONE);
        compos.setLayout(layout);
        compos.setLayoutData(new FormData());
        final FormData toolbarData = new FormData();

        manager.setImageCanvas(new QuickImageCanvas(manager, compos, SWT.NONE));
        manager.setStatusCanvas(new QStatusCanvas(manager, compos, SWT.NONE));

        final ToolBar toolBar = new ToolBar(compos, SWT.FLAT);
        toolBar.setLayoutData(toolbarData);

        previous = new ToolItem(toolBar, SWT.FLAT);
        previous.setToolTipText("Previous Image");
        previousImage = new Image(parent.getDisplay(), iconsdir + "previous.gif");
        previous.setImage(previousImage);
        previous.setSelection(true);

        next = new ToolItem(toolBar, SWT.FLAT);
        next.setToolTipText("Next Image");
        nextImage = new Image(parent.getDisplay(), iconsdir + "next.gif");
        next.setImage(nextImage);

        rotate = new ToolItem(toolBar, SWT.FLAT);
        rotate.setToolTipText("Rotate");
        rotateImage = new Image(parent.getDisplay(), iconsdir + "rotate.gif");
        rotate.setImage(rotateImage);

        viewThumb = new Image(parent.getDisplay(), iconsdir + "thumb.gif");
        viewFullsize = new Image(parent.getDisplay(), iconsdir + "fullsize.gif");
        view = new ToolItem(toolBar, SWT.FLAT);
        view.setToolTipText("view Thumbnails");
        view.setImage(viewThumb);

        new ToolItem(toolBar, SWT.SEPARATOR);

        zoomIn = new ToolItem(toolBar, SWT.FLAT);
        zoomIn.setToolTipText("zoom in");
        zoomInImage = new Image(parent.getDisplay(), iconsdir + "zoom_in.gif");
        zoomIn.setImage(zoomInImage);

        zoomOut = new ToolItem(toolBar, SWT.FLAT);
        zoomOut.setToolTipText("zoom out");
        zoomOutImage = new Image(parent.getDisplay(), iconsdir + "zoom_out.gif");
        zoomOut.setImage(zoomOutImage);

        zoomOrg = new ToolItem(toolBar, SWT.FLAT);
        zoomOrg.setToolTipText("zoom original size");
        zoom100Image = new Image(parent.getDisplay(), iconsdir + "zoom_100.gif");
        zoomOrg.setImage(zoom100Image);

        zoomFit = new ToolItem(toolBar, SWT.CHECK);
        zoomFit.setToolTipText("fit image in window");
        zoomFitImage = new Image(parent.getDisplay(), iconsdir + "zoom_fit.gif");
        zoomFit.setImage(zoomFitImage);

        final FormData canvasData = new FormData();

        // imgCanvas = new QuickImageCanvas(compos, SWT.NONE);
        manager.getImageCanvas().setLayoutData(canvasData);

        final FormData statusData = new FormData();

        // statusCanvas = new QStatusCanvas(imgCanvas, compos, SWT.NONE);
        manager.getStatusCanvas().setLayoutData(statusData);

        canvasData.top = new FormAttachment(toolBar, 0);
        canvasData.bottom = new FormAttachment(100, -30);
        canvasData.right = new FormAttachment(100, 0);
        canvasData.left = new FormAttachment(0, 0);

        toolbarData.top = new FormAttachment(0, 0);
        toolbarData.left = new FormAttachment(0, 0);
        toolbarData.right = new FormAttachment(100, 0);

        statusData.top = new FormAttachment(manager.getImageCanvas(), 0);
        statusData.bottom = new FormAttachment(100, 0);
        statusData.right = new FormAttachment(100, 0);
        statusData.left = new FormAttachment(0, 0);

        rotate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                zoomFit.setSelection(false);
                manager.getImageCanvas().rotate();
            }
        });

        previous.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                clickedPrevious(e);
            }
        });

        next.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                clickedNext(e);
            }
        });

        view.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                toggleView();
            }
        });

        // parent.addDisposeListener(new DisposeListener() {
        //
        // public void widgetDisposed(DisposeEvent e)
        // {
        // disposeAll();
        // }
        // });

        zoomIn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                zoomFit.setSelection(false);
                manager.getImageCanvas().zoomIn();
            }
        });

        zoomOut.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                zoomFit.setSelection(false);
                manager.getImageCanvas().zoomOut();
            }
        });

        zoomFit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                manager.getImageCanvas().zoomFit();
            }
        });

        zoomOrg.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                zoomFit.setSelection(false);
                manager.getImageCanvas().zoomOriginal();
            }
        });

        manager.getImageCanvas().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(final ControlEvent e) {
                if (zoomFit.getSelection()) {
                    manager.getImageCanvas().zoomFit();
                }
            }
        });

        previous.setEnabled(manager.getImageOrganizer().hasPrevious());
        next.setEnabled(manager.getImageOrganizer().hasNext());

        if (manager.getImageOrganizer().isSingle()) {
            next.setEnabled(false);
            previous.setEnabled(false);
            view.setEnabled(false);
        }
    }

    @Override
    public void dispose() {
        manager.getImageOrganizer().dispose();
        manager.getImageCanvas().dispose();
        manager.getStatusCanvas().dispose();

        if (previousImage != null && !previousImage.isDisposed()) {
            previousImage.dispose();
        }
        if (nextImage != null && !nextImage.isDisposed()) {
            nextImage.dispose();
        }
        if (rotateImage != null && !rotateImage.isDisposed()) {
            rotateImage.dispose();
        }
        if (zoomInImage != null && !zoomInImage.isDisposed()) {
            zoomInImage.dispose();
        }
        if (zoomOutImage != null && !zoomOutImage.isDisposed()) {
            zoomOutImage.dispose();
        }
        if (zoom100Image != null && !zoom100Image.isDisposed()) {
            zoom100Image.dispose();
        }
        if (zoomFitImage != null && !zoomFitImage.isDisposed()) {
            zoomFitImage.dispose();
        }

        // Runtime.getRuntime().gc();
        super.dispose();
    }

    // private void disposeAll()
    // {
    // System.out.println("QuickImageEditor.disposeAll()");
    // // previous.getImage().dispose();
    // // next.getImage().dispose();
    // // rotate.getImage().dispose();
    // // zoomIn.getImage().dispose();
    // // zoomOut.getImage().dispose();
    // // view.getImage().dispose();
    // }

    public void toggleView() {
        if (manager.getImageOrganizer().getActiveView() == ImageOrganizer.VIEW_FULLSIZE) {
            previous.setEnabled(false);
            next.setEnabled(false);
            rotate.setEnabled(false);
            manager.getImageOrganizer().setActiveView(ImageOrganizer.VIEW_THUMB);
            manager.getImageOrganizer().setCurrentToSelected();
            view.setImage(viewFullsize);
            view.setToolTipText("View Fullsize");
            view.setEnabled(false);
            view.setEnabled(true);
            zoomIn.setEnabled(false);
            zoomOut.setEnabled(false);
            zoomFit.setEnabled(false);
            zoomOrg.setEnabled(false);
        } else {
            previous.setEnabled(manager.getImageOrganizer().hasPrevious());
            next.setEnabled(manager.getImageOrganizer().hasNext());
            rotate.setEnabled(true);
            manager.getImageOrganizer().setActiveView(ImageOrganizer.VIEW_FULLSIZE);
            // manager.getImageOrganizer().setSelectedToCurrent();
            view.setImage(viewThumb);
            view.setToolTipText("View Thumbnails");
            manager.getImageCanvas().updateFullsizeData();
            view.setEnabled(false);
            view.setEnabled(true);
            zoomIn.setEnabled(true);
            zoomOut.setEnabled(true);
            zoomFit.setSelection(false);
            zoomFit.setEnabled(true);
            zoomOrg.setEnabled(true);
        }

        manager.getImageCanvas().updateThumbData();

    }

    private void clickedPrevious(final SelectionEvent e) {
        manager.getImageOrganizer().getPrevious();
        manager.getImageCanvas().updateFullsizeData();
        manager.getStatusCanvas().updateWithCurrent();
        setPartName(manager.getImageOrganizer().getCurrent().getDisplayName());
        previous.setEnabled(manager.getImageOrganizer().hasPrevious());
        next.setEnabled(manager.getImageOrganizer().hasNext());
    }

    private void clickedNext(final SelectionEvent e) {
        manager.getImageOrganizer().getNext();
        manager.getImageCanvas().updateFullsizeData();
        manager.getStatusCanvas().updateWithCurrent();
        setPartName(manager.getImageOrganizer().getCurrent().getDisplayName());
        previous.setEnabled(manager.getImageOrganizer().hasPrevious());
        next.setEnabled(manager.getImageOrganizer().hasNext());

    }

    @Override
    public void setPartName(final String s) {
        super.setPartName(s);
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) {
        setSite(site);
        setInput(input);
    }

    @Override
    public void setFocus() {
        if (manager.getImageCanvas() != null) {
            manager.getImageCanvas().setFocus();
        }
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    public String getIconsdir() {
        return iconsdir;
    }

    public void setIconsdir(final String iconsdir) {
        this.iconsdir = iconsdir;
    }
}