package edu.ucsd.gwt2.modelview.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.blimster.gwt.threejs.core.Color;
import net.blimster.gwt.threejs.core.Geometry;
import net.blimster.gwt.threejs.core.Object3D;
import net.blimster.gwt.threejs.core.Quaternion;
import net.blimster.gwt.threejs.core.Vector3;
import net.blimster.gwt.threejs.extras.geometries.CubeGeometry;
import net.blimster.gwt.threejs.materials.LineBasicMaterial;
import net.blimster.gwt.threejs.materials.MeshBasicMaterial;
import net.blimster.gwt.threejs.materials.ParticleBasicMaterial;
import net.blimster.gwt.threejs.objects.Line;
import net.blimster.gwt.threejs.objects.Mesh;
import net.blimster.gwt.threejs.objects.ParticleSystem;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import edu.ucsd.gwt2.modelview.shared.datamodel.Annotation;
import edu.ucsd.gwt2.modelview.shared.datamodel.Dataset;
import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;
import edu.ucsd.gwt2.modelview.shared.datamodel.Point3D;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * @author Jeffrey Bush
 */
public class ModelView implements EntryPoint, DatasetAsyncCallback, UncaughtExceptionHandler, MouseWheelHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler, ContextMenuHandler, ResizeHandler, MouseOutHandler
{
	/** Dataset ID used */
	private long datasetID = 319513; // 286436 or 319513 for testing, or for a massive test 550642 (35311 annotations/geometries)

	/** Model ID used */
	private long modelID = Long.MIN_VALUE;

	/** The dataset being displayed */
	private Dataset dataset = null;
	
	/** The refresh button */
	private Button refreshButton;
	
	/** The loading box */
	private LoadingPanel loading;
	
	/** The canvas that will be used for WebGL rendering */
	private Canvas canvas;

	/** The very-high-level WebGL interface. */
	private WebGL webgl;
	
	/** The logger used for errors and the sort */
	private Logger logger = Logger.getLogger("");
	
	/** The amount of padding to add in the view plus 1 */
	private final static double PADDING = 1.1; // makes it 10% larger

    /** The spacing between Z offsets used when none is specified */
    private final static double Z_SPACING_DEFAULT = 10.0;

    /** The maximum number of geometries to display (note that this is not strictly enforced and may go slightly over in some cases) */
    private final static int GEOMETRY_DISPLAY_LIMIT = 50000;

    /**
     * 'Random' colors used by {@link #setDataset(Dataset, boolean, ScheduledCommand)}.
     * These are roughly the same colors used by IMOD when creating a new object.
     */
    private final static int[] colors =
    {
		0x00FF00, // green / lime
		0x00FFFF, // cyan / aqua
		0xFF00FF, // magenta / fuchsia
		0xFFFF00, // yellow
		0x0000FF, // blue
		0xFF0000, // red
		0x00FF7F, // spring green
		0x3333CC, // 
		0xCC3333, // Persian red / red-brown
		0xE69966, // 
		0x9966E6, // 
		0x1A9966, // 
		0x991A66, // 
		0x3399CC, // 
		0xFF7F00, // orange
		0x66991A, // 
		0x1A1A99, // 
		0xE6E666, // 
		0xE66699, // 
		0x66E6E6, // 
		0x993333, // 
		0x33CC99, // 
		0x6699E6, // 
		0x1A991A, // 
		0xCC7F33, // 
		0xFF007F, // rose
		0x007FFF, // azure
		0x9933CC, // 
		0x7FFF00, // chartreuse
		0x1A6699, // 
		0x99661A, // 
		0xCC3399, // 
		0x661A99, // 
		0x33CC33, // 
		0xE666E6, // 
		//0x7F00FF, // violet
    	//0xFFFFFF, // white
    };
    
    /**
     * Class that holds the corners of a bounding box.
     * The initial values used are guaranteed to work with all double value, positive or negative.
     * @author Jeffrey Bush
     * @see #addAnnotation(Object3D, Annotation, int, BoundingBox)
     */
    private class BoundingBox
    {
		public double min_x = Double.MAX_VALUE, max_x = Double.MIN_VALUE;
		public double min_y = Double.MAX_VALUE, max_y = Double.MIN_VALUE;
		public double min_z = Double.MAX_VALUE, max_z = Double.MIN_VALUE;
		
		/**
		 * Update the minimum and maximum x values based on a new x value.
		 * @param x
		 */
		public void updateX(double x)
		{
			if (x < this.min_x) { this.min_x = x; }
			if (x > this.max_x) { this.max_x = x; }
		}

		/**
		 * Update the minimum and maximum y values based on a new y value.
		 * @param y
		 */
		public void updateY(double y)
		{
			if (y < this.min_y) { this.min_y = y; }
			if (y > this.max_y) { this.max_y = y; }
		}

		/**
		 * Update the minimum and maximum z values based on a new z value.
		 * @param z
		 */
		public void updateZ(double z)
		{
			if (z < this.min_z) { this.min_z = z; }
			if (z > this.max_z) { this.max_z = z; }
		}
		
		/**
		 * Update the minimum and maximum x, y, and z values based on a new 3D point.
		 * @param z
		 */
		public void update(Point3D p)
		{
			this.updateX(p.x);
			this.updateY(p.y);
			this.updateZ(p.z);
		}

		/**
		 * Update the minimum and maximum x and y from many points.
		 * @param pts the points, the second dimension must be 2
		 */
		public void update(Point2D[] pts)
		{
			if (pts == null || pts.length == 0) { return; }
			this.updateX(pts[0].x);
			this.updateY(pts[0].y);
			for (int i = 1; i < pts.length; ++i)
			{
				double x = pts[i].x, y = pts[i].y;
				if (x < this.min_x) { this.min_x = x; } else if (x > this.max_x) { this.max_x = x; }
				if (y < this.min_y) { this.min_y = y; } else if (y > this.max_y) { this.max_y = y; }
			}
		}
		
//		/**
//		 * Update the minimum and maximum x and y from a SLASH datamodel 2D bounding box.
//		 * @param bbox the other bounding box
//		 */
//		public void update(BoundingBox2D bbox)
//		{
//			if (bbox == null) { return; }
//			if (bbox.max.x > this.max_x) { this.max_x = bbox.min.x; }
//			if (bbox.max.y > this.max_y) { this.max_y = bbox.max.y; }
//			if (bbox.min.x < this.min_x) { this.min_x = bbox.min.x; }
//			if (bbox.min.y < this.min_y) { this.min_y = bbox.min.y; }
//		}
    }
    
    /**
     * Gets the content of a DOM tag's attribute, using a default value if it is not found.
     * @param dom the document to look in
     * @param tagName the name of the tag to find
     * @param attrName the name of the attribute in that tag to find
     * @param def the default value if the value cannot be obtained
     * @return the attribute's value or the default value
     */
    private static String getAttrValue(Document dom, String tagName, String attrName, String def)
    {
    	NodeList nl = dom.getElementsByTagName(tagName);
    	if (nl.getLength() != 1) { return def; }
    	String val = ((Element)nl.item(0)).getAttribute(attrName);
    	return val == null ? def : val;
    }
    
    
    private int addGeometry(Object3D a3d, edu.ucsd.gwt2.modelview.shared.datamodel.Geometry g, BoundingBox bbox, Point3D scaling, boolean closed, boolean points, int color)
    {
		// Convert the trace data into a JavaScript array of vertices while updating the bounding box
		Point2D scaling2D = scaling.get2D();
		Point2D[] trace = g.traceData;
	    if (trace == null || trace.length == 0) { return 0; }
		Point2D[] trace_scaled = new Point2D[trace.length];
		JsArray<Vector3> vertices = JavaScriptObject.createArray().cast();
		double z = g.z * scaling.z;
		for (int i = 0; i < trace.length; ++i)
		{
			trace_scaled[i] = trace[i].multiply(scaling2D);
			vertices.push(Vector3.create(trace_scaled[i].x, trace_scaled[i].y, z));
		}
		// TODO: the bbox needs to take into account the quaternion of all parent/current annotations
		bbox.update(trace_scaled);
		//if (g.boundingRect != null) { bbox.update(g.boundingRect.scale(scaling2D)); }
		bbox.updateZ(z);
		
		// In closed contours (polygons) we need to connect the first and last point
		if (closed) { vertices.push(Vector3.create(trace_scaled[0].x, trace_scaled[0].y, z)); }
		
		// Finish creation of the geometry and add into the annotation
		Geometry geometry = Geometry.create();
	    geometry.setVertices(vertices);
	    Object3D g3d = points ?
	    		ParticleSystem.create(geometry, ParticleBasicMaterial.create(1.0, Color.create(color))) : // displays as points
	    		Line.create(geometry, LineBasicMaterial.create(color)); // displays as lines
		a3d.add(g3d);
		
		return 1;
	}
    
    /**
     * Adds an annotation to the display, including all of its children.
     * @param parent the object to add the new annotation to
     * @param a the annotation to add to the display
     * @param color the suggested color of the annotation, if one is not specified in the annotation's application data
     * @param bbox a bounding box object that is updated to include the given annotation
     * @return the number of geometries added (including in all child annotations)
     */
    private int addAnnotation(Object3D parent, final Annotation a, int color, BoundingBox bbox, Point3D scaling)
    {
    	// Check that we can handle the geometry type
		String type = a.geometryType;
		boolean closed = "polygon".equalsIgnoreCase(type);
		boolean points = "point".equalsIgnoreCase(type);
		if (!closed && !points && !"polyline".equalsIgnoreCase(type))
		{
			this.logger.log(Level.WARNING, "Unknonwn geometry type: " + type);
			return 0;
		}
		
		// Read the WIB XML data if it is there
		String wib_xml = a.applicationData != null ? a.applicationData.get("WIB:CCDB") : null;
		if (wib_xml != null)
		{
			try
			{
				Document dom = XMLParser.parse(wib_xml);

				// Check the visible attribute
				if (!getAttrValue(dom, "visible", "state", "true").equalsIgnoreCase("true"))
				{
					this.logger.log(Level.FINE, "Skipping annotation " + a.id + " because it is marked as not visible in wib-data.");
					return 0; // TODO: all children are invisible as well?
				}
				
				// Check the color attribute
				String wib_color_str = getAttrValue(dom, "color", "value", null);
				if (wib_color_str != null) { color = Integer.parseInt(wib_color_str); } // all children use this color as default as well
			}
			catch (DOMException dome) { }
			catch (NumberFormatException nfe) { }
		}
		
		
		
		// This is a hack that reduces the number of annotation object allocated
		// If this annotation is not oriented oddly and has no children, add its geometries straight to the current annotation
		// This may end up adding geometries straight to the dataset
//		if ((a.orientation == null || (a.orientation.x == 0 && a.orientation.y == 0 && a.orientation.z == 0)) &&
//			(a.children == null || a.children.length == 0))
//		{
//			int ngeoms = 0;
//			if (a.geometries != null)
//			{
//				for (edu.ucsd.gwt2.modelview.shared.datamodel.Geometry g : a.geometries)
//				{
//					ngeoms += addGeometry(parent, g, bbox, scaling, closed, points, color);
//				}
//			}
//			return ngeoms;
//		}
		
		
		// Create the 3D object and copy the quaternion data
//		Point2D scaling2D = scaling.get2D();
		Object3D a3d = Object3D.create();
		if (a.orientation != null && (a.orientation.x != 0 || a.orientation.y != 0 || a.orientation.z != 0))
		{
			a3d.setUseQuaternion(true);
			a3d.setQuaternion(Quaternion.create(a.orientation.x, a.orientation.y, a.orientation.z, a.orientation.w).normalize());
		}
		// TODO: the bbox needs to take into account the quaternion of all parent/current annotations
//		if (a.boundingBox != null) { bbox.update(a.boundingBox.scale(scaling2D)); }
		
		// Create each geometry in this annotation
		int ngeoms = 0;
		if (a.geometries != null)
		{
			for (edu.ucsd.gwt2.modelview.shared.datamodel.Geometry g : a.geometries)
			{
				ngeoms += addGeometry(a3d, g, bbox, scaling, closed, points, color);
			}
		}
		
		// Add this annotation into the parent
		parent.add(a3d);
		
		// Go through all child annotations, adding them to this annotation
		if (a.children != null)
		{
			for (Annotation child : a.children)
			{
				ngeoms += this.addAnnotation(a3d, child, color, bbox, scaling);
			}
		}
		return ngeoms;
    }
    
    /**
     * Sets the current dataset used. This function operates in an asynchronous manner. Pass a command to be executed on
     * completion if you need to know when it finishes.
     * @param dataset the dataset to set the display to (should not include any that have a parent)
     * @param reset_view if the view should be reset to fit around the dataset being displayed 
     * @param onComplete the command to run when completed, or null if this notification is not necessary
     */
	private void setDataset(final Dataset dataset, final boolean reset_view, final ScheduledCommand onComplete)
	{
		final Scheduler s = Scheduler.get();
		final Object3D root = this.webgl.getRootObject();
		this.webgl.detachRoot();

		// Remove all previous displayed objects
		this.webgl.clearChildren(root);

		// Check that there is something to draw
		if (dataset.rootAnnotations == null || dataset.rootAnnotations.length == 0)
		{
			this.logger.log(Level.FINE, "Nothing to draw!");
			this.webgl.reattachRoot();
			this.webgl.redraw();
			this.dataset = dataset;
			if (onComplete != null) { s.scheduleDeferred(onComplete); }
			return;
		}

		// Basic dataset properties
		final BoundingBox bbox = new BoundingBox();
		final Point3D scaling = (dataset.scaleFactor == null) ? new Point3D(1, 1, Z_SPACING_DEFAULT) : dataset.scaleFactor;
		//if (dataset.boundingBox != null) { bbox.update(dataset.boundingBox.scale(scaling.get2D())); }
		if (dataset.origin != null)
		{
			bbox.update(dataset.origin.multiply(scaling));
			if (dataset.dimensions != null) { bbox.update(dataset.origin.add(dataset.dimensions).multiply(scaling)); }
		}
		else if (dataset.dimensions != null) { bbox.update(dataset.dimensions.multiply(scaling)); }
				
		// The code that gets run at the very end
		final ScheduledCommand finish = new ScheduledCommand()
		{
			public void execute()
			{
				// Check that there is something to draw
				if (bbox.max_x == Double.MIN_VALUE)
				{
					ModelView.this.logger.log(Level.FINE, "Nothing to draw!");
				}
				else
				{
					// Get many useful properties of the bounding box
					// "d*" is the length of an axis of the box
					// "c*" is the center of the axis of the box
					double dx = bbox.max_x - bbox.min_x, dy = bbox.max_y - bbox.min_y, dz = bbox.max_z - bbox.min_z;
					double cx = bbox.min_x + dx/2, cy = bbox.min_y + dy/2, cz = bbox.min_z + dz/2;
					
					// Adjust so that no side is too small
					double d_min = 0.2 * Math.max(Math.max(dx, dy), dz); // all sides must be at least as large 20% * maximum length of a side
					if (d_min == 0) { d_min = 100.0; } // apparently we only have a point, 100 seems like a decent value
					if (dx < d_min) { dx = d_min; }
					if (dy < d_min) { dy = d_min; }
					if (dz < d_min) { dz = d_min; }
					
					// Add padding to all sides
					dx *= PADDING; dy *= PADDING; dz *= PADDING;
					
					// Adjust the "root" position so that it is centered within the "control" object, allowing it to be rotated nicely
					root.setPosition(Vector3.create(-cx, -cy, -cz));
					
					if (reset_view)
					{
						// Set view based on content
						ModelView.this.webgl.setViewToFit(dx, dy, dz);
					}
					
					// DEBUG: Draw bounding box
					Geometry box = CubeGeometry.create(dx, dy, dz);
					Mesh m = Mesh.create(box, MeshBasicMaterial.create(0x444444, true));
					m.setPosition(Vector3.create(cx, cy, cz));
					root.add(m);
				    //System.out.println("BBOX: (" + bbox.min_x + "," + bbox.min_y + "," + bbox.min_z + "),(" + bbox.max_x + "," + bbox.max_y + "," + bbox.max_z + ")");
				}
				
				ModelView.this.webgl.reattachRoot();
				ModelView.this.webgl.redraw();
				ModelView.this.dataset = dataset;

				if (onComplete != null) { s.scheduleDeferred(onComplete); }
			}
		};
		
		// Add all annotations with each branch a different "random" default color
		// Also gets the bounding box for all annotations
		final int rootLen = dataset.rootAnnotations.length;
		s.scheduleIncremental(new RepeatingCommand()
		{
			private int i = 0, ngeoms = 0;
			public boolean execute()
			{
				int c = i % colors.length;
				ngeoms += ModelView.this.addAnnotation(root, dataset.rootAnnotations[i], colors[c], bbox, scaling);
				if (c == 0) // we don't want it to update the progress every time as that would slow it down
				{
					ModelView.this.loading.setProgress(i, rootLen);
				}
				if (++i == rootLen || ngeoms >= ModelView.GEOMETRY_DISPLAY_LIMIT)
				{
					// Finish up
					if (i != rootLen)
					{
						Window.alert("Only " + ngeoms + " geometries are being shown due to limitations of WebGL.");
					}
					s.scheduleDeferred(finish);
					return false;
				}
				return true;
			}
		});
	}

	/**
	 * This is the entry point method.
	 * Sets up the WebGL stuff, adds event handlers, and loads the initial data.
	 */
	@Override
	public void onModuleLoad()
	{
		GWT.setUncaughtExceptionHandler(this);
		
		///// Get Dataset ID /////
		try
		{
			this.datasetID = Long.parseLong(Window.Location.getParameter("datasetID"));
			this.modelID = Long.parseLong(Window.Location.getParameter("modelID"));
		}
		catch (NumberFormatException ex) { }

		
		///// Setup WebGL /////
		if (!Canvas.isSupported() || !WebGL.isAvailable())
		{
			HTMLPanel p = new HTMLPanel("This site uses WebGL which your browser either does not support or does not have turned on.<br>See <a href=\"http://get.webgl.org/\" target=\"_blank\">http://get.webgl.org/</a> for more information.");
			p.getElement().setId("error-box");
			RootPanel.get().add(p);
			return;
		}
        this.canvas = Canvas.createIfSupported();
        this.canvas.getCanvasElement().setId("renderer");
        this.webgl = new WebGL(this.canvas, Window.getClientWidth(), Window.getClientHeight());
        RootPanel.get().add(this.canvas);


        ///// Add Canvas Event Handlers /////
        this.canvas.addDomHandler(this, ContextMenuEvent.getType());
        this.canvas.addMouseWheelHandler(this);
        this.mouseDownReg = this.canvas.addMouseDownHandler(this);
        Window.addResizeHandler(this);

        
        ///// Create the buttons /////
        // Button panel
        HorizontalPanel buttons = new HorizontalPanel();
        buttons.getElement().setId("buttons");
        RootPanel.get().add(buttons);

        // Camera mode button
        final Button cameraModeButton = new Button("Switch Camera Mode");
        buttons.add(cameraModeButton);
        cameraModeButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent event) { ModelView.this.webgl.switchCameraMode(); } });
        
        // Refresh button
        this.refreshButton = new Button("Refresh");
        buttons.add(refreshButton);
		refreshButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent event) { ModelView.this.refresh(); } });
		
		
		///// Create the Loading Popup /////
		this.loading = new LoadingPanel();

		
		///// Do initial load /////
		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() { public void execute() { ModelView.this.refresh(); } });
	}
	
	/**
	 * Unwraps {@link com.google.gwt.event.shared.UmbrellaException}s if they contains only one exception.
	 * @param e any throwable
	 * @return a possibly unwrapped throwable
	 */
	public Throwable unwrapUmbrellaException(Throwable e)
	{
		if (e instanceof UmbrellaException)
		{
			UmbrellaException ue = (UmbrellaException)e;
			if (ue.getCauses().size() == 1)
			{
				return unwrapUmbrellaException(ue.getCauses().iterator().next());
			}
		}
		return e;
	}
	
	/**
	 * Log all uncaught exceptions.
	 * @param e the throwable error
	 */
	@Override
	public void onUncaughtException(Throwable e)
	{
		this.logger.log(Level.SEVERE, "Uncaught Exception", e);
	}
	
	/**
	 * Implementation of AsyncCallback onFailure. When a call has failed, log and report it.
	 * @param e the throwable error
	 */
	@Override
	public void onFailure(Throwable e)
	{
		this.logger.log(Level.SEVERE, "Loading Failed", e);  
		Window.alert("There was an error while loading the data:\n" + e);
		this.loading.hide();
		this.refreshButton.setEnabled(true);
	}
	/**
	 * Implementation of DatasetAsyncCallback onProgress. Updates the loading bar when more annotations have loaded.
	 * @param current the current number of annotations loaded
	 * @param total the total number of annotations that will be loaded
	 */
	@Override
	public void onProgress(int current, int total)
	{
		this.loading.setText("Loading annotations... " + current + " / " + total);
		this.loading.setProgress(current, total);
	}
	/**
	 * Implementation of AsyncCallback&lt;Dataset&gt; onSuccess. This starts the drawing of the dataset.
	 * @param result the result that was loaded
	 */
	@Override
	public void onSuccess(final Dataset result)
	{
		this.loading.setText("Drawing...");
		this.setDataset(result, ModelView.this.dataset == null, new ScheduledCommand()
		{
			public void execute()
			{ 
				ModelView.this.loading.hide();
				ModelView.this.refreshButton.setEnabled(true);
			}
		});
	}
	
	/**
	 * Refreshes the dataset.
	 */
	public void refresh()
	{
		this.loading.show("Loading dataset...");
		this.refreshButton.setEnabled(false);
		ClientSlashService.getDataset(this.datasetID, this.modelID, this);
	}
	
	/**
	 * When resized adjust the {@link #ratio aspect ratio}}, update the {@link #camera}, and resize the {@link #renderer}.
	 * @param event the resize event
	 */
	@Override
    public void onResize(ResizeEvent event) { this.webgl.setSize(event.getWidth(), event.getHeight()); }
	
	/**
	 * Prevents default and stops propagation for the context menu event.
	 * @param event the context menu event
	 */
	@Override
	public void onContextMenu(ContextMenuEvent event) { event.preventDefault(); event.stopPropagation(); }
	
	/**
	 * When the mouse wheel is used change the zoom.
	 * @param event the mouse wheel event
	 */
	@Override
	public void onMouseWheel(MouseWheelEvent event)
	{
		double scale = event.getDeltaY() / 2.9; // the scroll wheel seems to be multiples of 3, so to remain just >1 dividing by 2.9 seems decent
		if (scale < 0) { scale = -1 / scale; } // if scrolling up (negative) zoom in (shrink FOV)
		this.webgl.setFOV(this.webgl.getFOV() * scale);
        event.preventDefault(); // make sure the page does not scroll
        event.stopPropagation();
	}

	// The registration handles for the mouse events that are added and removed during the click and drag process
	private HandlerRegistration mouseDownReg = null, mouseMoveReg = null, mouseUpReg = null, mouseOutReg= null;
	/** The most recent mouse x position in pixels. */
	private int mouse_x;
	/** The most recent mouse y position in pixels. */
	private int mouse_y;
	/** If the right mouse button was pressed, this is true. */
	private boolean mouse_right_button;

	/**
	 * When the mouse goes down register the mouse up, move, and out handlers, remove the down handler, and get the initial mouse position.
	 * @param event the mouse down event
	 */
	@Override
	public void onMouseDown(MouseDownEvent event)
	{
		this.mouseUpReg   = this.canvas.addMouseUpHandler(this);
		this.mouseMoveReg = this.canvas.addMouseMoveHandler(this);
        this.mouseOutReg  = this.canvas.addMouseOutHandler(this);
		this.mouseDownReg.removeHandler(); this.mouseDownReg  = null;
		this.mouse_x = event.getScreenX();
		this.mouse_y = event.getScreenY();
		this.mouse_right_button = event.getNativeButton() == NativeEvent.BUTTON_RIGHT;
	}

	/**
	 * When the mouse is moved while down, either rotate or translate the object depending on the mouse button.
	 * @param event the mouse move event
	 */
	@Override
	public void onMouseMove(MouseMoveEvent event)
	{
		// Get the amount the mouse moved since the last down / move event as a percentage of the size of the canvas in the X and Y screen directions
		CanvasElement ce = this.canvas.getCanvasElement();
		double dx = (event.getScreenX() - mouse_x) / (double)ce.getWidth();
		double dy = (event.getScreenY() - mouse_y) / (double)ce.getHeight();
		
		if (this.mouse_right_button)
		{
			// Rotate
			// Made to do a complete rotation when the mouse is moved from one end of the canvas to the other
			// qx is the x-angle around the +Y axis
			// qy is the y-angle around the +Z axis
			// see http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToQuaternion/index.htm
			double half_angle_x = dx * Math.PI, half_angle_y = dy * Math.PI;
			Quaternion qx = Quaternion.create(0, 0, Math.sin(half_angle_x), Math.cos(half_angle_x));
			Quaternion qy = Quaternion.create(Math.sin(half_angle_y), 0, 0, Math.cos(half_angle_y));
			this.webgl.rotate(qx.multiplySelf(qy));
		}
		else
		{
			// Translate
			// Made so that the central plane of the object as it currently shown will move with the mouse exactly
			// The X and Y screen directions are mapped to the +X and -Z axes in the 3D world
			double h = this.webgl.getCentralPlaneHeight(), w = h * this.webgl.getRatio();
			this.webgl.translate(Vector3.create(dx * w, 0, - dy * h));
		}
        
        // Update last mouse position with the current mouse position
		this.mouse_x = event.getScreenX();
		this.mouse_y = event.getScreenY();
	}

	/**
	 * On mouse up unregister the mouse up, move, and out handlers, and add back the the down handler.
	 * @param event the mouse up event
	 */
	@Override
	public void onMouseUp(MouseUpEvent event)
	{
		this.mouseDownReg = this.canvas.addMouseDownHandler(this);
		this.mouseMoveReg.removeHandler(); this.mouseMoveReg = null;
		this.mouseUpReg.removeHandler();   this.mouseUpReg   = null;
		this.mouseOutReg.removeHandler();  this.mouseOutReg  = null;
	}

	/**
	 * On mouse out unregister the mouse up, move, and out handlers, and add back the the down handler.
	 * @param event the mouse up event
	 */
	@Override
	public void onMouseOut(MouseOutEvent event)
	{
		this.mouseDownReg = this.canvas.addMouseDownHandler(this);
		this.mouseMoveReg.removeHandler(); this.mouseMoveReg = null;
		this.mouseUpReg.removeHandler();   this.mouseUpReg   = null;
		this.mouseOutReg.removeHandler();  this.mouseOutReg  = null;
	}
}
