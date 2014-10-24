package linkopm;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class editXML {
	public void newXML(String sid, String filepath, String user_id, String table_name, String columns, String condition){
		 try {
			 
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				
		 
				
				String id[]={"1","2","3","4","5","6","7","11","22","33","44","55","66","77"};
				String content[]={"Table name is: "+table_name,"Table in database: "+table_name,"Submitted to Colfusion",
						"Uid is: "+user_id,"Sid is:"+sid,"hasColumns: "+columns,"hasColumns: "+columns,
						"wasDerivedFrom"," "," ","",
						" "," "," "};
				String weight[]={"2.0","2.0","1.5","1.0","1.0","1.0","1.0"};
				String sourcelist[]={"2","2","3","3","3","6","7"};
				String target[]={"1","3","1","4","5","1","2"};
				
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("graphml");
				doc.appendChild(rootElement);
		 
				
		        //<key id="weight" for="node" attr.name="weight" attr.type="double"/>
				Element key1= doc.createElement("key");
				rootElement.appendChild(key1);
				Attr att1 = doc.createAttribute("id");
				att1.setValue("label");
				key1.setAttributeNode(att1);
				Attr att2 = doc.createAttribute("for");
				att2.setValue("all");
				key1.setAttributeNode(att2);
				Attr att3 = doc.createAttribute("attr.name");
				att3.setValue("label");
				key1.setAttributeNode(att3);
				Attr att4 = doc.createAttribute("attr.type");
				att4.setValue("string");
				key1.setAttributeNode(att4);
				
				Element key2= doc.createElement("key");
				rootElement.appendChild(key2);
				Attr att5 = doc.createAttribute("id");
				att5.setValue("weight");
				key2.setAttributeNode(att5);
				Attr att6 = doc.createAttribute("for");
				att6.setValue("node");
				key2.setAttributeNode(att6);
				Attr att7 = doc.createAttribute("attr.name");
				att7.setValue("weight");
				key2.setAttributeNode(att7);
				Attr att8 = doc.createAttribute("attr.type");
				att8.setValue("double");
				key2.setAttributeNode(att8);
				
				
				

				Element graph = doc.createElement("graph");
				rootElement.appendChild(graph);
				Attr attr1 = doc.createAttribute("edgedefault");
				attr1.setValue("directed");
				graph.setAttributeNode(attr1);
		 
				//nodes
				for(int i=0;i<7;i++){
					Element node = doc.createElement("node");
					graph.appendChild(node);
					Attr attr2 = doc.createAttribute("id");
					attr2.setValue(id[i]);
					node.setAttributeNode(attr2);
					
					Element data=doc.createElement("data");
					node.appendChild(data);
					Attr attr3=doc.createAttribute("key");
					attr3.setValue("label");
					data.appendChild(doc.createTextNode(content[i]));
					data.setAttributeNode(attr3);
					
					Element data2=doc.createElement("data");
					node.appendChild(data2);
					Attr attr4=doc.createAttribute("key");
					attr4.setValue("weight");
					data2.appendChild(doc.createTextNode(weight[i]));
					data2.setAttributeNode(attr4);
				}
				
				//edges
				for(int i=0;i<7;i++){
					Element edge = doc.createElement("edge");
					graph.appendChild(edge);
					Attr attr2 = doc.createAttribute("id");
					attr2.setValue(id[i+7]);
					edge.setAttributeNode(attr2);
					Attr attr3 = doc.createAttribute("source");
					attr3.setValue(sourcelist[i]);
					edge.setAttributeNode(attr3);
					Attr attr4 = doc.createAttribute("target");
					attr4.setValue(target[i]);
					edge.setAttributeNode(attr4);
					
					Element data=doc.createElement("data");
					edge.appendChild(data);
					Attr attr5=doc.createAttribute("key");
					attr5.setValue("label");
					data.appendChild(doc.createTextNode(content[i+7]));
					data.setAttributeNode(attr5);
					
				}
		 
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new File(filepath+sid+"_visual.xml"));
		 
				// Output to console for testing
				// StreamResult result = new StreamResult(System.out);
		 
				transformer.transform(source, result);
		 
				System.out.println("File saved!");
		 
			  } catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			  } catch (TransformerException tfe) {
				tfe.printStackTrace();
			  }
	}

}
