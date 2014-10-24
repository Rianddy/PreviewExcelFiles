package linkopm;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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
//import org.openprovenance.rdf.OPMXml2Rdf; 


/**
 * A graph constructor
 */
public class newTest 
{ 

    
    public OPMGraph getGraph(OPMFactory oFactory, String sid, String user_id, String table_name, String columns, String condition) 
    {
    	Account account1=oFactory.newAccount("search");
        Collection<Account> search=Collections.singleton(account1);
        
        
        Process p1=oFactory.newProcess("p1",
                                       search,
                                       "Submitted to Colfusion");     

        Artifact a1=oFactory.newArtifact("a1",
                search,
                "Table name is: "+table_name);
        Artifact a2=oFactory.newArtifact("a2",
                search,
                "Table in database: "+table_name);



        Used u1=oFactory.newUsed(p1,oFactory.newRole("in"),a1,search);

        Agent ag1=oFactory.newAgent("ag1", search, "uid is: "+user_id+"\n sid is: "+sid);
        WasControlledBy cb1=oFactory.newWasControlledBy(p1, null, ag1, search);


        WasGeneratedBy wg1=oFactory.newWasGeneratedBy("wg1",a2,oFactory.newRole("out"),p1," ",search);


        Overlaps ov1=oFactory.newOverlaps(search);


        WasDerivedFrom wdf1=oFactory.newWasDerivedFrom("wdf1",a2,a1,"wasDerivedFrom",search);


        //annotation for process
        //EmbeddedAnnotation ann1=oFactory.newEmbeddedAnnotation("ann1","http://property.org/condition", condition, search);
        //ann1.getProperty().add(oFactory.newProperty("http://property.org/numericQuality", 0.4));
        //oFactory.addAnnotation(p1,ann1);

        //annotation for artifact
        oFactory.addAnnotation(a1,oFactory.newEmbeddedAnnotation("ann3","http://property.org/hasColumns", columns, search));
        oFactory.addAnnotation(a2,oFactory.newEmbeddedAnnotation("ann4","http://property.org/hasColumns", columns, search));          

        OPMGraph graph=oFactory.newOPMGraph(search,
        		new Overlaps[] { ov1 },
        		new Process[] {p1},
        		new Artifact[] {a1,a2},
        		new Agent[] {ag1},
        		new Object[] {u1,
        		wg1,                            
        		wdf1,
        		cb1},
        		new Annotation[] {} );





        return graph;

    }

    /*private static String readString(String prompt) { 
    	Scanner scanner = new Scanner(System.in); 
    	System.out.print(prompt); 
    	return scanner.nextLine(); 
    } */


}
