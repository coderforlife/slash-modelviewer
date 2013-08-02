package edu.ucsd.gwt2.modelview.server;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.ucsd.gwt2.modelview.client.SlashService;
import edu.ucsd.gwt2.modelview.shared.datamodel.Dataset;
import edu.ucsd.gwt2.modelview.shared.datamodel.DatasetAsync;
import edu.ucsd.gwt2.modelview.shared.datamodel.Geometry;
import edu.ucsd.gwt2.modelview.shared.datamodel.Annotation;
import edu.ucsd.gwt2.modelview.shared.datamodel.Point3D;
import edu.ucsd.gwt2.modelview.shared.datamodel.Quat;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.GISException;
import edu.ucsd.gwt2.modelview.shared.datamodel.GIS.SpatialObject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SlashServiceImpl extends RemoteServiceServlet implements SlashService
{
	/**
	 * Get the base dataset information. Basically, everything except the annotations themselves.
	 * The dataset will have the annotation counts.
	 * @param datasetID the dataset to query
	 * @param modelID the model to get or negative for all models
	 * @return the root annotations (parent-less annotations) in the given dataset
	 * @throws SQLException
	 */
	public DatasetAsync getDatasetAsync(long datasetID, long modelID) throws SQLException
	{
		Connection c = Database.getConnection();
		try
		{
			DatasetAsync d = new DatasetAsync();
			
			// Get the dataset information
			d.dataset = getDatasetBase(c, datasetID);
			
			// Get the annotation IDs
			d.annotationIDs = getAnnotationIDsByDataset(c, datasetID, modelID);
			//d.dataset.annotationCount = d.annotationIDs.length;
			
			// Get the organizational structure of the annotations
			d.annotationChildren = getAnnotationChildren(c, datasetID, modelID);
			
			return d;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			Database.returnConnection(c);
		}
	}
	
	/**
	 * Get all of the annotations in a dataset. Note that for large datasets this will probably run into a memory issues.
	 * @param datasetID the dataset to query
	 * @param modelID the model to get or negative for all models
	 * @return the root annotations (parent-less annotations) in the given dataset
	 * @throws SQLException
	 * @throws GISException
	 */
	public Dataset getCompleteDataset(long datasetID, long modelID) throws SQLException, GISException
	{
		Connection c = Database.getConnection();
		try
		{
			// Get the dataset information
			Dataset d = getDatasetBase(c, datasetID);
	
			// Get the annotations
			HashMap<Long, Annotation> amap = getAnnotationsByDataset(c, datasetID, modelID);
			//d.annotationCount = amap.size();
			
			// Organize the annotations
			d.rootAnnotations = makeAnnotationTree(c, datasetID, modelID, amap);
			
			// Cleanup
			return d;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		catch (GISException ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			Database.returnConnection(c);
		}
	}
	
	/**
	 * Get annotations from their IDs.
	 * @param annotationIDs the annotation IDs of the annotations to get
	 * @return the annotations with the geometries but not the children filled out
	 * @throws SQLException
	 * @throws GISException
	 */
	public Annotation[] getAnnotations(long[] annotationIDs) throws SQLException, GISException
	{
		Connection c = Database.getConnection();
		try
		{
			return getAnnotationsById(c, annotationIDs);
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		catch (GISException ex)
		{
			ex.printStackTrace();
			throw ex;
		}
		finally
		{
			Database.returnConnection(c);
		}
	}
	
	
	///// General Retrieval and Query Functions /////
	
	/**
	 * Gets a quaternion from 4 columns of the result set, all starting with name and ending with x, y, z, and w.
	 * @param rs the result to grab numbers from
	 * @param name the base name of the quaternion columns
	 * @return the quaternion, or null if any column was null
	 * @throws SQLException
	 */
	private static Quat getQuat(ResultSet rs, String name) throws SQLException
	{
		double x = rs.getDouble(name+"x"); if (rs.wasNull()) { return null; }
		double y = rs.getDouble(name+"y"); if (rs.wasNull()) { return null; }
		double z = rs.getDouble(name+"z"); if (rs.wasNull()) { return null; }
		double w = rs.getDouble(name+"w"); if (rs.wasNull()) { return null; }
		if ((x ==   -1 && y ==   -1 && z ==   -1 && w ==   -1) || // filter out the common illegal cases
			(x ==    0 && y ==    0 && z ==    0 && w ==    0) ||
			(x == 2000 && y == 4000 && z == 3000 && w == 1000)) { return null; }
		return new Quat(x, y, z, w);
	}
	/**
	 * Gets a 3D point from 3 columns of the result set, all starting with name and ending with x, y, and z.
	 * @param rs the result to grab numbers from
	 * @param name the base name of the point columns
	 * @return the point, or null if any column was null
	 * @throws SQLException
	 */
	private static Point3D getPoint3D(ResultSet rs, String name) throws SQLException
	{
		double x = rs.getDouble(name+"x"); if (rs.wasNull()) { return null; }
		double y = rs.getDouble(name+"y"); if (rs.wasNull()) { return null; }
		double z = rs.getDouble(name+"z"); if (rs.wasNull()) { return null; }
		if ((x == -1 && y == -1 && z ==   -1) || // filter out the common illegal cases
			(x ==  0 && y ==  0 && z ==   -1) || 
			(x == -1 && y == -1 && z == 3000)) { return null; }
		return new Point3D(x, y, z);
	}
	
//	/**
//	 * Create an SQL query to get the bounding box of a particular geometry.
//	 * @param column_name the name of the column, can include the table prefix
//	 * @return the SQL query to put in the SELECT block
//	 */
//	private static String getBoundingBox2DSqlQuery(String column_name)
//	{
//		int index = column_name.indexOf('.');
//		String out_name = index > 0 ? column_name.substring(index + 1) : column_name;
//		return
//			"ST_XMin(" + column_name + ") AS " + out_name + "_minx," +
//			"ST_XMax(" + column_name + ") AS " + out_name + "_maxx," +
//			"ST_YMin(" + column_name + ") AS " + out_name + "_miny," +
//			"ST_YMax(" + column_name + ") AS " + out_name + "_maxy ";
//	}
//	/**
//	 * Gets a 2D bounding box from a column that was queried with {@link SlashServiceImpl#getBoundingBox2DSqlQuery}.
//	 * @param rs the result to grab data from
//	 * @param column_name the original column name (without the table prefix)
//	 * @return the bounding box, or null if the column was null
//	 * @throws SQLException
//	 */
//	private static BoundingBox2D getBoundingBox2D(ResultSet rs, String column_name) throws SQLException
//	{
//		double min_x = rs.getDouble(column_name + "_minx"); if (rs.wasNull()) { return null; }
//		double max_x = rs.getDouble(column_name + "_maxx"); if (rs.wasNull()) { return null; }
//		double min_y = rs.getDouble(column_name + "_miny"); if (rs.wasNull()) { return null; }
//		double max_y = rs.getDouble(column_name + "_maxy"); if (rs.wasNull()) { return null; }
//		if (min_x == 0 && min_y == 0 && max_x == 1 && max_y == 1) { return null; } // filter out bad box
//		return new BoundingBox2D(min_x, min_y, max_x, max_y);
//	}
	
	/**
	 * Convert geometry binary data in the WKB format from the database to a GISGeometry object.
	 * @param rs the result to grab data from
	 * @param column_name the geometry column name
	 * @return the geometry
	 * @throws SQLException
	 * @throws GISException
	 */
	private static SpatialObject getGeometryFromWKB(ResultSet rs, String column_name) throws SQLException, GISException
	{
		byte[] data = rs.getBytes(column_name);
		if (data == null) { return null; }
		return GISServerUtil.readWKB(data);
	}

	/**
	 * Gets all application data using a specific SQL SELECT statement. The SQL SELECT statement must have the first three column as
	 * ID (long for indexing), application key, application value. It must take one parameter of dataset ID. It may take a second
	 * parameter of model ID only if the model ID is non-negative.
	 * @see SlashServiceImpl#getApplicationDataForDataset(Connection, long)
	 * @see SlashServiceImpl#getApplicationDataForAnnotations(Connection, long, long)
	 * @see SlashServiceImpl#getApplicationDataForGeometries(Connection, long, long)
	 * @param c the database connection to use
	 * @param datasetID the dataset id
	 * @param modelID the model to get or negative for all models
	 * @param sql the SQL SELET statement
	 * @return the application data, keyed by the ID then by the application key
	 * @throws SQLException
	 */
	private static HashMap<Long, HashMap<String, String>> getApplicationData(PreparedStatement ps) throws SQLException
	{
		ResultSet rs = ps.executeQuery();
		HashMap<Long, HashMap<String, String>> data = new HashMap<Long, HashMap<String, String>>();
		while (rs.next())
		{
			long id = rs.getLong(1);
			HashMap<String, String> map = data.get(id);
			if (map == null)
			{
				data.put(id, map = new HashMap<String, String>());
			}
			map.put(rs.getString(2), rs.getString(3));
		}
		rs.close();
		ps.close();
		return data;
	}
		
	/**
	 * Gets a single integer for prepared statement. The value has to be in column 1. Returns null on errors or if the column value itself was null.
	 * @param ps prepared statement
	 * @return the integer, or null
	 * @throws SQLException
	 */
	private static Integer getInt(PreparedStatement ps) throws SQLException
	{
		ResultSet rs = ps.executeQuery();
		if (!rs.next()) { return null; }
		int x = rs.getInt(1);
		if (rs.wasNull()) { return null; }
		rs.close();
		ps.close();
		return x;
	}
	
	
	///// Dataset Retrieval /////
	/**
	 * Get the base dataset information.
	 * @param c the database connection to use
	 * @param datasetID the dataset id
	 * @return the dataset with everything filled out except annotation counts and the annotations themselves
	 * @throws SQLException
	 */
	private static Dataset getDatasetBase(Connection c, long datasetID) throws SQLException
	{
		PreparedStatement ps;
		
		// Get the dataset information
		ps = c.prepareStatement(
			// sproject_id, dataset_name, resource_path, anatomical_region_name, anatomical_region_ontology, curation_state, curator_user_name, organism_name, organism_ontology, public_availability_state, atlas_name,
			"SELECT orientation_x,orientation_y,orientation_z,orientation_w, origin_x,origin_y,origin_z, scale_factor_x,scale_factor_y,scale_factor_z, width,height,num_of_slides " + 
			// x_pixel_space,y_pixel_space,z_pixel_space, getBoundingBox2DSqlQuery("bounding_box")
			"FROM slash_dataset WHERE dataset_id=? LIMIT 1"
		);
		ps.setLong(1, datasetID);
		ResultSet rs = ps.executeQuery();
		if (!rs.next()) { return null; }
		Dataset d = new Dataset();
		d.id = datasetID;
//        d.projectID = rs.getInt("sproject_id");
//        d.datasetName = rs.getString("dataset_name");
//        d.resourcePath = rs.getString("resource_path");
//        d.anatomicalRegionName = rs.getString("anatomical_region_name");
//        d.anatomicalRegionOntology = rs.getString("anatomical_region_ontology");
//        d.isCurated = rs.getBoolean("curation_state");
//        d.curationUserName = rs.getString("curator_user_name");
//        d.organismName = rs.getString("organism_name");
//        d.organismOntology = rs.getString("organism_ontology");
//        d.isPubliclyAvailable = rs.getBoolean("public_availability_state");
//        d.atlasName = rs.getString("atlas_name");
		d.orientation = getQuat(rs, "orientation_");
		d.origin = getPoint3D(rs, "origin_");
		d.dimensions = new Point3D(rs.getDouble("width"), rs.getDouble("height"), rs.getDouble("num_of_slides"));
		d.scaleFactor = getPoint3D(rs, "scale_factor_");
//        d.boundingBox = getBoundingBox2D(rs, "bounding_box");
		rs.close();
		ps.close();
		
		// Get the application data
		// TODO: check the version_id max query
		ps = c.prepareStatement(
			"SELECT d.dataset_id, d.application_key, d.application_value " +
			"FROM slash_applicatoin_data AS d" +
			" LEFT OUTER JOIN slash_applicatoin_data AS d2 ON (d.dataset_id=d2.dataset_id AND d.application_key=d2.application_key AND d.version_id<d2.version_id) " +
			"WHERE d.dataset_id=? AND d2.dataset_id IS NULL"
		);
		ps.setLong(1, datasetID);
		d.applicationData = getApplicationData(ps).get(datasetID);
		return d;
	}
	

	///// Annotation Retrieval /////
	/**
	 * Get all of the annotations in a dataset.
	 * @param c the database connection to use
	 * @param datasetID the dataset id
	 * @param modelID the model to get or negative for all models
	 * @return the annotations, with the key of annotation id
	 * @throws SQLException
	 * @throws GISException
	 */
	private static HashMap<Long, Annotation> getAnnotationsByDataset(Connection c, long datasetID, long modelID) throws SQLException, GISException
	{
		PreparedStatement ps;
		String model = (modelID >= 0 ? " AND version_number=?" : "");
		
		// Get the geometries
		HashMap<Long, Geometry[]> geoms = getGeometriesByDataset(c, datasetID, modelID);
		
		// Get the annotation application data
		// TODO: check the version_id max query
		ps = c.prepareStatement(
			"SELECT d.annotation_id, d.application_key, d.application_value " +
			"FROM slash_annotation AS a, slash_applicatoin_data AS d" +
			" LEFT OUTER JOIN slash_applicatoin_data AS d2 ON (d.annotation_id=d2.annotation_id AND d.application_key=d2.application_key AND d.version_id < d2.version_id) " +
			"WHERE a.dataset_id=?" + model + " AND a.annotation_id=d.annotation_id AND d2.annotation_id IS NULL"
		);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		HashMap<Long, HashMap<String, String>> app_data = getApplicationData(ps);
		
		// Get the core annotation data for each annotation
		ps = c.prepareStatement(
			/* geometry_type, object_name, object_name_ont_uri, ontology_name */
			"SELECT annotation_id, version_number, color_int, orientation_x,orientation_y,orientation_z,orientation_w " +
			/*getBoundingBox2DSqlQuery("bound_box") +*/ "FROM slash_annotation WHERE dataset_id=? " + model
		);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		HashMap<Long, Annotation> annotations = new HashMap<Long, Annotation>();
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			Annotation a = getAnnotation(rs, geoms, app_data);
			annotations.put(a.id, a);
		}
		rs.close();
		ps.close();
		
		// Return the annotations
		return annotations;
	}

	/**
	 * Get all of the annotations IDs in a dataset.
	 * @param c the database connection to use
	 * @param datasetID the dataset id
	 * @param modelID the model to get or negative for all models
	 * @return the annotation IDs
	 * @throws SQLException
	 */
	private static long[] getAnnotationIDsByDataset(Connection c, long datasetID, long modelID) throws SQLException
	{
		PreparedStatement ps;
		String model = (modelID >= 0 ? " AND version_number=?" : "");
		
		// Get the count
		ps = c.prepareStatement("SELECT COUNT(annotation_id) FROM slash_annotation WHERE dataset_id=?" + model);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		int len = getInt(ps), i = 0;

		// Get the IDs
		ps = c.prepareStatement("SELECT annotation_id FROM slash_annotation WHERE dataset_id=? " + model);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		ResultSet rs = ps.executeQuery();
		long[] annotations = new long[len];
		while (rs.next())
		{
			if (i == len)
			{
				// TODO: error
			}
			annotations[i++] = rs.getLong("annotation_id");
		}
		rs.close();
		ps.close();
		return annotations;
	}
	
	/**
	 * Gets annotations from their IDs.
	 * @param c the database connection to use
	 * @param annotationIDs the annotation IDs to lookup
	 * @return the array of annotations, which may be shorter but not longer than the list of annotation IDs
	 * @throws SQLException
	 * @throws GISException
	 */
	private static Annotation[] getAnnotationsById(Connection c, long[] annotationIDs) throws SQLException, GISException
	{
		PreparedStatement ps;
		int len = annotationIDs.length, i;
		Long[] _ids = new Long[len];
		for (i = 0; i < len; ++i) { _ids[i] = annotationIDs[i]; }
		Array ids = c.createArrayOf("int8", _ids);
		
		// Get the geometries
		HashMap<Long, Geometry[]> geoms = getGeometriesByAnnotationIDs(c, ids);

		// Get the annotation application data
		// TODO: check the version_id max query
		ps = c.prepareStatement(
			"SELECT d.annotation_id, d.application_key, d.application_value " +
			"FROM slash_applicatoin_data AS d" +
			" LEFT OUTER JOIN slash_applicatoin_data AS d2 ON (d.annotation_id=d2.annotation_id AND d.application_key=d2.application_key AND d.version_id<d2.version_id) " +
			"WHERE d.annotation_id=ANY(?) AND d2.annotation_id IS NULL"
		);
		ps.setArray(1, ids);
		HashMap<Long, HashMap<String, String>> app_data = getApplicationData(ps);

		// Get the core annotation data for each annotation
		ps = c.prepareStatement(
			/* geometry_type, object_name, object_name_ont_uri, ontology_name */
			"SELECT annotation_id, version_number, color_int, orientation_x,orientation_y,orientation_z,orientation_w " +
			/*getBoundingBox2DSqlQuery("bound_box") +*/ "FROM slash_annotation WHERE annotation_id = ANY(?)"
		);
		ps.setArray(1, ids);
		ResultSet rs = ps.executeQuery();
		Annotation[] annotations = new Annotation[len];
		i = 0;
		while (rs.next())
		{
			if (i == len)
			{
				// TODO: error
			}
			annotations[i++] = getAnnotation(rs, geoms, app_data);
		}
		rs.close();
		ps.close();
		
		// Return the annotations
		return (i != len) ? Arrays.copyOf(annotations, i) : annotations;
	}

	/**
	 * Get an annotation from a result set, filling in the geometry and application data if available.
	 * @param rs the result set
	 * @param geoms the collection of geometries, indexed by annotation id, to fill into the annotations
	 * @param app_data the application data for the annotations, if it exists
	 * @return a new Annotation object completely filled in
	 * @throws SQLException
	 */
	private static Annotation getAnnotation(ResultSet rs, HashMap<Long, Geometry[]> geoms, HashMap<Long, HashMap<String, String>> app_data) throws SQLException
	{
		Annotation a = new Annotation();
		a.id = rs.getLong("annotation_id");
		a.modelID = rs.getLong("version_number");
//        a.ontologyName = rs.getString("ontology_name");
//        a.objectName = rs.getString("object_name");
//        a.objectOntologyURI = rs.getString("object_name_ont_uri");
		a.color = rs.getInt("color_int");
		if (rs.wasNull()) { a.color = Annotation.COLOR_NOT_SPECIFIED; }
		a.applicationData = app_data.get(a.id);
//        a.geometryType = rs.getString("geometry_type");
		a.geometries = geoms.get(a.id);
		a.orientation = getQuat(rs, "orientation_");
//        a.boundingBox = getBoundingBox2D(rs, "bound_box");
		return a;
	}
	
	/**
	 * Get the children annotation of annotations in the dataset.
	 * @param c the database connection to use
	 * @param datasetID the dataset id of the annotations
	 * @param modelID the model to get or negative for all models
	 * @return the map of parent annotation ID to child annotation IDs
	 * @throws SQLException
	 */
	private static HashMap<Long, long[]> getAnnotationChildren(Connection c, long datasetID, long modelID) throws SQLException
	{
		HashMap<Long, ArrayList<Long>> children = new HashMap<Long, ArrayList<Long>>();

		// Get the tree data
		PreparedStatement ps = c.prepareStatement(
			"SELECT DISTINCT t.annot_parent_id, t.annot_child_id " +
			"FROM slash_annotation AS a, slash_annotation_tree AS t " +
			"WHERE a.dataset_id=?" + (modelID >= 0 ? " AND a.version_number=?" : "") + " AND (a.annotation_id=t.annot_parent_id OR a.annotation_id=t.annot_child_id)"
		);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			long parent_id = rs.getLong(1);
			ArrayList<Long> cs = children.get(parent_id);
			if (cs == null)
			{
				children.put(parent_id, cs = new ArrayList<Long>());
			}
			cs.add(rs.getLong(2));
		}
		rs.close();
		ps.close();
	 
		// Convert ArrayList to arrays
		HashMap<Long, long[]> _children = new HashMap<Long, long[]>(children.size());
		for (Map.Entry<Long, ArrayList<Long>> e : children.entrySet())
		{
			ArrayList<Long> childs = e.getValue();
			int len = childs.size();
			long[] _childs = new long[len];
			for (int i = 0; i < len; ++i) { _childs[i] = childs.get(i); }
			_children.put(e.getKey(), _childs);
			e.setValue(null);
		}
		
		return _children;
	}
	
	/**
	 * Converts the entire list of annotations given into a tree, properly nesting children under their parent annotations.
	 * @param c the database connection to use
	 * @param datasetID the dataset id of the annotations
	 * @param modelID the model to get or negative for all models
	 * @param amap the map of annotations to make into a tree, with the key being the annotation id and the value being the annotation
	 * @return the root annotations of the tree
	 * @throws SQLException
	 */
	private static Annotation[] makeAnnotationTree(Connection c, long datasetID, long modelID, HashMap<Long, Annotation> amap) throws SQLException
	{
		// Get the children data
		HashMap<Long, long[]> children = getAnnotationChildren(c, datasetID, modelID);
		
		// Build the tree
		@SuppressWarnings("unchecked")
		HashMap<Long, Annotation> roots = (HashMap<Long, Annotation>)amap.clone();
		for (Annotation a : amap.values())
		{
			long[] child_ids = children.get(a.id);
			if (child_ids != null)
			{
				int len = child_ids.length;
				a.children = new Annotation[len];
				for (int i = 0; i < len; ++i)
				{
					Long id = child_ids[i];
					roots.remove(id);
					a.children[i] = amap.get(id);
				}
			}
		}
		
		// Return the root nodes
		return roots.values().toArray(new Annotation[roots.size()]);
	}


	///// Geometry Retrieval /////
	/**
	 * Gets all of the geometries in a dataset.
	 * @param c the database connection to use
	 * @param datasetID the dataset id
	 * @param modelID the model to get or negative for all models
	 * @return the geometries, with the key of annotation id
	 * @throws SQLException
	 * @throws GISException
	 */
	private static HashMap<Long, Geometry[]> getGeometriesByDataset(Connection c, long datasetID, long modelID) throws SQLException, GISException
	{
		PreparedStatement ps;
		String model = (modelID >= 0 ? " AND version_number=?" : "");
		
		// Get geometry application data
		// TODO: check the version_id max query
		ps = c.prepareStatement(
			"SELECT g.geom_id, d.application_key, d.application_value " +
			"FROM slash_annotation AS a, slash_geometry AS g, slash_annot_geom_map AS map, slash_applicatoin_data AS d" +
			" LEFT OUTER JOIN slash_applicatoin_data AS d2 ON (d.geom_id=d2.geom_id AND d.application_key=d2.application_key AND d.version_id < d2.version_id) " +
			"WHERE a.dataset_id=?" + model + " AND a.annotation_id=map.annotation_id AND map.geometry_id=g.geom_id AND g.z_index IS NOT NULL AND g.geom_id=d.geom_id AND d2.geom_id IS NULL"
		);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		HashMap<Long, HashMap<String, String>> app_data = getApplicationData(ps);

		// Get all of the geometries in the annotations
		ps = c.prepareStatement(
			"SELECT a.annotation_id, g.geom_id, g.geometry_type, g.z_index, ST_AsBinary(g.polyline) AS points " + // g.modified_time, g.user_id, u.user_name, getBoundingBox2DSqlQuery("g.polyline")
			"FROM slash_annotation AS a, slash_annot_geom_map AS map, slash_geometry AS g " + // slash_user AS u
			"WHERE a.dataset_id=?" + model + " AND g.z_index IS NOT NULL AND a.annotation_id=map.annotation_id AND map.geometry_id=g.geom_id" // AND g.user_id=u.user_id
		);
		ps.setLong(1, datasetID);
		if (modelID >= 0)
			ps.setLong(2, modelID);
		return getGeometries(ps, app_data);
	}
	/**
	 * Gets all of the geometries of the given annotations.
	 * @param c the database connection to use
	 * @param annotationIDs the annotations IDs to get geometries for
	 * @return the geometries, with the key of annotation id
	 * @throws SQLException
	 * @throws GISException
	 */
	private static HashMap<Long, Geometry[]> getGeometriesByAnnotationIDs(Connection c, Array annotationIDs) throws SQLException, GISException
	{
		PreparedStatement ps;
		
		// Get geometry application data
		// TODO: check the version_id max query
		ps = c.prepareStatement(
			"SELECT g.geom_id, d.application_key, d.application_value " +
			"FROM slash_geometry AS g, slash_annot_geom_map AS map, slash_applicatoin_data AS d" +
			" LEFT OUTER JOIN slash_applicatoin_data AS d2 ON (d.geom_id=d2.geom_id AND d.application_key=d2.application_key AND d.version_id<d2.version_id) " +
			"WHERE map.annotation_id=ANY(?) AND map.geometry_id=g.geom_id AND g.z_index IS NOT NULL AND g.geom_id=d.geom_id AND d2.geom_id IS NULL"
		);
		ps.setArray(1, annotationIDs);
		HashMap<Long, HashMap<String, String>> app_data = getApplicationData(ps);
		
		// Get all of the geometries in the annotations
		ps = c.prepareStatement(
			"SELECT map.annotation_id, g.geom_id, g.geometry_type, g.z_index, ST_AsBinary(g.polyline) AS points " + // g.modified_time, g.user_id, u.user_name, getBoundingBox2DSqlQuery("g.polyline")
			"FROM slash_annot_geom_map AS map, slash_geometry AS g " + // slash_user AS u 
			"WHERE map.annotation_id=ANY(?) AND map.geometry_id=g.geom_id AND g.z_index IS NOT NULL" // AND g.user_id=u.user_id
		);
		ps.setArray(1, annotationIDs);
		return getGeometries(ps, app_data);
	}
	/**
	 * Get all geometries by executing a prepared statement.
	 * @param ps the prepared statement
	 * @param app_data the application data for the geometries, if it exists
	 * @return the geometries, with the key of annotation id
	 * @throws SQLException
	 * @throws GISException
	 */
	private static HashMap<Long, Geometry[]> getGeometries(PreparedStatement ps, HashMap<Long, HashMap<String, String>> app_data) throws SQLException, GISException
	{
		HashMap<Long, ArrayList<Geometry>> gmap = new HashMap<Long, ArrayList<Geometry>>();
		ResultSet rs = ps.executeQuery();
		while (rs.next())
		{
			long annotation_id = rs.getLong("annotation_id");
			Geometry g = new Geometry();
			g.id = rs.getLong("geom_id");
			g.type = rs.getString("geometry_type");
//            g.boundingRect = getBoundingBox2D(rs, "polyline");
//            g.editTime = rs.getTimestamp("modified_time").getTime();
//            g.userName = rs.getString("user_name");
			g.z = rs.getDouble("z_index");
			g.traceData = getGeometryFromWKB(rs, "points").reduce().getPoints();
			g.applicationData = app_data.get(g.id);
			ArrayList<Geometry> gs = gmap.get(annotation_id);
			if (gs == null)
			{
				gmap.put(annotation_id, gs = new ArrayList<Geometry>());
			}
			gs.add(g);
		}
		rs.close();
		ps.close();
		
		// Convert ArrayList to arrays for efficiency (memory and time) later
		HashMap<Long, Geometry[]> geoms = new HashMap<Long, Geometry[]>(gmap.size());
		for (Map.Entry<Long, ArrayList<Geometry>> e : gmap.entrySet())
		{
			geoms.put(e.getKey(), e.getValue().toArray(new Geometry[e.getValue().size()]));
			e.setValue(null);
		}
		return geoms;
	}
}
