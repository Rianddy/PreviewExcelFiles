package model;

import java.sql.Connection;
import java.util.ArrayList;

import dao.Database;

import dao.Project;
import dto.FeedObjects;


import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBException;

import linkopm.newTest;
import linkopm.editXML;

//import org.openprovenance.rdf.OPMXml2Rdf; 
import org.openprovenance.model.OPMGraph; 
import org.openprovenance.model.OPMFactory; 
import org.openprovenance.model.Edge; 
import org.openprovenance.model.Account; 
import org.openprovenance.model.Annotation; 
import org.openprovenance.model.Overlaps; 
import org.openprovenance.model.AccountRef; 
import org.openprovenance.model.Processes; 
import org.openprovenance.model.Node; 
import org.openprovenance.model.Agent; 
import org.openprovenance.model.Process; 
import org.openprovenance.model.Artifact; 
import org.openprovenance.model.Used; 
import org.openprovenance.model.Role; 
import org.openprovenance.model.EmbeddedAnnotation; 
import org.openprovenance.model.WasGeneratedBy; 
import org.openprovenance.model.WasTriggeredBy; 
import org.openprovenance.model.WasDerivedFrom; 
import org.openprovenance.model.WasControlledBy; 
import org.openprovenance.model.OPMUtilities; 
import org.openprovenance.model.OPMDeserialiser; 
import org.openprovenance.model.OPMSerialiser; 
import org.openprovenance.model.OPMToDot; 

public class ProjectManager {
	 
	static String prosid;
	static String profilepath;
	static String prouser_id;
	static String protable_name;
	static String procolumns;
	static String procondition;
	
	public static void setSid(String sid) {
		prosid=sid;
	}
	
	public static void setFilepath(String filepath) {
		profilepath=filepath;
	}
	
	public static void setUser_id(String user_id) {
		prouser_id=user_id;
	}
	
	public static void setTable_name(String table_name) {
		protable_name=table_name;
	}
	
	public static void setColumns(String columns) {
		procolumns=columns;
	}
	
	public static void setCondition(String condition) {
		procondition=condition;
	}
	
	/*public ArrayList<FeedObjects> GetFeeds()throws Exception {
		ArrayList<FeedObjects> feeds = null;
		try {
			    Database database= new Database();
			    Connection connection = database.Get_Connection();
				Project project= new Project();
				feeds=project.GetFeeds(connection);
		
		} catch (Exception e) {
			throw e;
		}
		return feeds;
	}*/

	    static OPMGraph graph1;
	    static OPMGraph graph2;

	    static public OPMFactory oFactory=new OPMFactory();


	    /** Creates and serialises an OPM graph. */

	    public void testOPM1() throws JAXBException
	    {

	        OPMGraph graph=new newTest().getGraph(oFactory,prosid,prouser_id,protable_name,procolumns,procondition);
	        editXML editX = new editXML();
	        editX.newXML(prosid,profilepath,prouser_id,protable_name,procolumns,procondition);




	        OPMSerialiser serial=OPMSerialiser.getThreadOPMSerialiser();
	        StringWriter sw=new StringWriter();
	        serial.serialiseOPMGraph(sw,graph,true);
	       
	        serial.serialiseOPMGraph(new File(profilepath+prosid+".xml"),graph,true);
	        //System.out.println(sw);

	        graph1=graph;




	    }


	    /** Deserialises an OPM graph. */
	    public void testOPM2() throws JAXBException    {
	        OPMDeserialiser deserial=OPMDeserialiser.getThreadOPMDeserialiser();
	        OPMGraph graph=deserial.deserialiseOPMGraph(new File(profilepath+prosid+".xml"));

	        graph2=graph;
	        //System.out.println(graph2);

	    }

	    /** Checks that the graph read from the file is the same as the
	     * one created. */
	    public void testOPM3() throws JAXBException    {
	        //System.out.println("===> " + graph1.getAccounts());
	        //System.out.println("===> " + graph2.getAccounts());
	    }

	    /** Pretty Prints opmgraph. */
	    public void testOPM4() throws JAXBException , java.io.FileNotFoundException, java.io.IOException   {
	        //OPMToDot toDot=new OPMToDot();
	        //toDot.convert(graph1,profilepath+prosid+".dot",profilepath+prosid+".pdf");
	 

	    }



//	     public void testAnnotationRDF1() throws Exception {
//	         OPMXml2Rdf toRdf=new OPMXml2Rdf();
//	         //System.out.println("graph is " + graph1);
//	         toRdf.convert(graph1,"target/annotation1-rdf.xml");
//	     }
	    

}


