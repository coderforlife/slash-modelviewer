package edu.ucsd.gwt2.modelview.client;

import net.blimster.gwt.threejs.cameras.Camera;
import net.blimster.gwt.threejs.cameras.OrthographicCamera;
import net.blimster.gwt.threejs.cameras.PerspectiveCamera;
import net.blimster.gwt.threejs.core.Object3D;
import net.blimster.gwt.threejs.math.Color;
import net.blimster.gwt.threejs.math.Quaternion;
import net.blimster.gwt.threejs.math.Vector3;
import net.blimster.gwt.threejs.renderers.WebGLRenderer;
import net.blimster.gwt.threejs.scenes.Scene;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * A very-high-level WebGL interface. This is mainly a wrapper around the already high-level three.js specifically for this project.
 *
 * The conventions used here are that the X axis is right on the screen, the Y axis is into the screen, and the Z axis is up on the screen.
 * These really only apply to the "control" object which is what you move with the mouse. Everything in root is relative to that.
 * The "control" object is the object that is moved/rotated within the scene. It only contains "root".
 * The "root" object contains the annotations and is used to shift them so they can rotate nicely around the center.
 * Scaling is handled by manipulating the properties of the camera
 * @author Jeffrey Bush
 */
public class WebGL
{
	/**
	 * Check if WebGL can be used. This is achieved by trying to create a canvas with a WebGL context.
	 * This is necessary since the browser may understand WebGL (has WebGLRenderingContext) but due to a poor graphics card is not able to use it.
	 * @return true if we can use WebGL
	 */
	public static native boolean isAvailable()
	/*-{
		try
		{
			return !!$wnd.WebGLRenderingContext && !!document.createElement('canvas').getContext('experimental-webgl');
		}
		catch(e) { return false; }
	}-*/ ;
	
	/** The WebGL render. */
	private WebGLRenderer renderer;

	/** The WebGL scene. Contains one object: {@link WebGL#control}. */
	private Scene scene;
	/** The WebGL camera. Is either a {@link PerspectiveCamera} or {@link OrthographicCamera} depending on {@link WebGL#camera_is_perspective}. */
	private Camera camera;

	/** The object that is moved/rotated within the scene. Contains one object: {@link WebGL#root}. */
	private Object3D control;
	/** The object that contains all actual object and is used to shift them so they can rotate nicely around the center. */
	private Object3D root;
	
	/** True if the screen is pending a redraw */
	private boolean pending_redraw = false;
	
	/**
	 * The kind of {@link WebGL#camera} being used. Always use {@link WebGL#switchCameraMode()} to set.
	 * The default camera type is the <b>opposite</b> of what this is set to by default.
	 */
	private boolean camera_is_perspective = false;
	/**
	 * The vertical FOV of the {@link WebGL#camera} in radians even though three.js uses degrees. Always use {@link WebGL#setFOV(double)} or {@link WebGL#setFOVInternal(double)} to set.
	 */
	private double fov = Math.PI / 4;
	/**
	 * The aspect ratio of the {@link WebGL#camera} (width to height). Always use {@link WebGL#setSize(int, int)} to set.
	 */
	private double ratio = 1.0;
	/**
	 * The distance from the {@link WebGL#camera} to the near frustum. Anything closer will not be shown.
	 */
	private final static double NEAR = 1.0;
	/**
	 * The distance from the {@link WebGL#camera} to the far frustum. Anything further will not be shown. This means that the maximum depth of the annotations is around 100,000.
	 */
	private final static double FAR = 1000000.0;

	/**
	 * Create a new WebGL rendering with the given width and height using the given canvas.
	 * Make sure to test if WebGL {@link WebGL#isAvailable() is available} before calling this constructor.
	 * @param canvas the canvas to use for rendering
	 * @param width the starting width of the canvas
	 * @param height the starting height of the canvas
	 */
	public WebGL(Canvas canvas, int width, int height)
	{
		this.renderer = WebGLRenderer.create(canvas, true);
		this.renderer.setSize(width, height);
		this.renderer.setClearColor(Color.create(0x000000), 1.0f);

		this.root = Object3D.create();
		this.root.setName("root");
		this.control = Object3D.create();
		this.control.setName("control");
		this.control.setUseQuaternion(true);
		this.control.add(this.root);
		this.scene = Scene.create();
		this.scene.add(this.control);
		this.scene.setName("main scene");
		
		this.ratio = width / (double)height;
		this.switchCameraMode();
	}
	
	/**
	 * @return the root object to which all other objects are added
	 */
	public Object3D getRootObject() { return this.root; }

	/**
	 * Detaching the root prevents anything from being drawn. This should be done before large numbers of objects
	 * are attached or removed from the root for efficiency (in case something causes a redraw). Make sure to call
	 * {@link #reattachRoot()} after root processing is done. Does nothing if the root is already detached.
	 */
	public void detachRoot() { this.control.remove(this.root); }

	/**
	 * Reattaching the root is required for things to be drawn again after detaching the root. Does nothing if the
	 * root is already attached.
	 * @see #detachRoot()
	 */
	public void reattachRoot()
	{
		if (this.control.getChildByName("root", false) == null)
		{
			this.control.add(this.root);
		}
	}
	
	/**
	 * Rotates everything in the scene around the axis by the rotation given followed by a redraw.
	 * @param q the quaternion describing the rotation
	 */
	public void rotate(Quaternion q)
	{
		Quaternion Q = this.control.getQuaternion();
		Q.multiplyQuaternions(q, Q);
		this.redraw();
	}
	
	/**
	 * Translates everything in the scene by the vector given followed by a redraw.
	 * @param v the 3D vector describing the translation
	 */
	public void translate(Vector3 v)
	{
		this.control.getPosition().add(v);
		this.redraw();
	}
	
	/**
	 * Removes all children from a 3D object, making sure that they are completely cleaned-up
	 * @param o the object to remove all children from
	 */
	public void clearChildren(Object3D o)
	{
		JsArray<Object3D> children = o.getChildren();
		for (int i = children.length() - 1; i >= 0; --i)
		{
			Object3D child = children.get(i);
			clearChildren(child);
			o.remove(child);
			this.renderer.deallocateObject(child);
		}
	}
	
	/**
	 * Set the 3D display to redrawn.
	 */
	public synchronized void redraw()
	{
		if (!this.pending_redraw)
		{
			Scheduler.get().scheduleDeferred(new ScheduledCommand()
			{
				public synchronized void execute()
				{
					WebGL.this.renderer.render(WebGL.this.scene, WebGL.this.camera);
					WebGL.this.pending_redraw = false;
				}
			});
			this.pending_redraw = true;
		}
	}
	
	/**
	 * Sets the current view to fit the given box by adjusting zoom (FOV) and position along with reseting rotation.
	 * @param dx the box width
	 * @param dy the box depth
	 * @param dz the box height
	 */
	public void setViewToFit(double dx, double dy, double dz)
	{
		final double center = NEAR + 2 * dy; // TODO: 2* is completely subjective and not necessarily good
		final double d = 2 * center - dy;
		// Get the vertical FOV angles based on fitting content vertically (FOV) and horizontally (FOV2)
		final double FOV = Math.atan2(dz, d); // actually half field-of-view
		final double FOV2 = Math.asin(Math.sin(Math.atan2(dx, d)) / this.ratio); // sin(atan(x)) == x/sqrt(1+x^2)
		// Set the FOV angle based on the largest angle
		this.setFOVInternal(2 * Math.max(FOV, FOV2));
		// Set the position and reset the rotation
		this.control.setPosition(Vector3.create(0, center, 0));
		this.control.setQuaternion(Quaternion.create(0, 0, 0, -1));
	}
	
	/**
	 * Gets the height of the plane that goes through the center of the displayed data and is parallel to the camera without taking into account any rotations.
	 * This is used for conversions between perspective and orhtographic mode and conversions between pixel distances and 3D distances.
	 * To get central plane width, simply multiply the height by the {@link WebGL#getRatio() aspect ratio}.
	 * @see WebGL#getRatio()
	 * @return the central plane height
	 */
	public double getCentralPlaneHeight() { return this.control.getPosition().getY() * 2.0 * Math.tan(this.fov / 2.0); }

	/**
	 * Same as {@link WebGL#setFOV()} except does not call {@link WebGL#redraw()}.
	 * @param fov the vertical field of view in radians
	 */
	private void setFOVInternal(double fov)
	{
		this.fov = Math.max(Math.min(fov, Math.PI - 0.1), 0.1); // make sure to stay >0 and <PI (can/should not be equal to those) 
		if (this.camera_is_perspective)
		{
			PerspectiveCamera pc = (PerspectiveCamera)this.camera;
			pc.setFov(Math.toDegrees(this.fov));
			pc.updateProjectionMatrix();
		}
		else
		{
			// See switchCameraMode() for an explanation of this
			double h = this.getCentralPlaneHeight() / 2, w = h * this.ratio;
			OrthographicCamera oc = (OrthographicCamera)this.camera;
			oc.setLeft(-w); oc.setRight(w);
			oc.setTop(h); oc.setBottom(-h);
			oc.updateProjectionMatrix();
		}
	}
	
	/**
	 * Sets the vertical FOV of the camera, essentially controlling the zoom. Calls {@link WebGL#redraw()}.
	 * @see WebGL#getFOV()
	 * @param fov the vertical field of view in radians
	 */
	public void setFOV(double fov) { this.setFOVInternal(fov); this.redraw(); }
	
	/**
	 * Get the vertical FOV.
	 * @see WebGL#setFOV()
	 * @return the vertical field of view in radians
	 */
	public double getFOV() { return this.fov; }
	
	/**
	 * Sets the size of the rendering, adjusting the size of the output canvas and the aspect ratio of the camera. Calls {@link WebGL#redraw()}.
	 * @see WebGL#getRatio()
	 * @param width the new width in pixels
	 * @param height the new height in pixels
	 */
	public void setSize(int width, int height)
	{
		this.ratio = width / (double)height;
		if (this.camera_is_perspective)
		{
			PerspectiveCamera pc = (PerspectiveCamera)this.camera;
			pc.setAspect(this.ratio);
			pc.updateProjectionMatrix();
		}
		else
		{
			// See switchCameraMode() for an explanation of this
			double h = this.getCentralPlaneHeight() / 2, w = h * this.ratio;
			OrthographicCamera oc = (OrthographicCamera)this.camera;
			oc.setLeft(-w); oc.setRight(w);
			oc.setTop(h); oc.setBottom(-h); // doesn't actually change
			oc.updateProjectionMatrix();
		}
		this.renderer.setSize(width, height);
		this.redraw();
	}
	
	/**
	 * Gets the aspect ratio of the camera (width / height).
	 * @see WebGL#setSize(int, int)
	 */
	public double getRatio() { return this.ratio; }
	
	/**
	 * Switches the camera mode between perspective and orhtographic modes.
	 */
	public void switchCameraMode()
	{
		if (this.camera != null) { this.renderer.deallocateObject(this.camera); }
		if (this.camera_is_perspective)
		{
			// Make it so the central plane does not change size, however things in front of it with get bigger and things behind it will get smaller
			double h = this.getCentralPlaneHeight() / 2, w = h * this.ratio;
			this.camera = OrthographicCamera.create(-w, w, h, -h, NEAR, FAR);
		}
		else
		{
			this.camera = PerspectiveCamera.create(Math.toDegrees(this.fov), this.ratio, NEAR, FAR);
		}
		this.camera.setUp(Vector3.create(0.0, 0.0, 1.0)); // sets "up" on the monitor to be the +Z axis 
		this.camera.lookAt(Vector3.create(0.0, 1.0, 0.0)); // sets "into" the monitor to be the +Y axis
		this.camera_is_perspective = !this.camera_is_perspective;
		this.redraw();
	}
}

