package edu.ucsd.gwt2.modelview.shared.datamodel.GIS;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import edu.ucsd.gwt2.modelview.shared.datamodel.Point2D;

/**
 * GIS Geometry Data Types, used in spatial databases.
 * @author Jeffrey Bush
 */
public abstract class SpatialObject implements Serializable, IsSerializable
{
	private static final long serialVersionUID = 6109479979938288162L;
	
	public abstract SpatialType type();
	public abstract Point2D[] getPoints();
}
