package helpers;

import com.amazonaws.util.XmlUtils;
import org.custommonkey.xmlunit.*;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
//import oracle.xml.diff.XmlUtils;
//import oracle.xml.diff.Options;

import java.io.File;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMLActions {

    FileHandling fh = new FileHandling();
    String m_xml1 = null;
    String m_xml2 = null;
    String m_folderResultName;
    String m_kind;

    public XMLActions(String i_xml1, String i_xml2, String i_folderResultName,String i_kind) {
        m_xml1 = i_xml1;
        m_xml2 = i_xml2;
        m_folderResultName = i_folderResultName;
        m_kind = i_kind;
    }
    public String convertXMLToJsonObjectAndReturnJsonAsString(String xmlStr){
        xmlStr=org.json.XML.toJSONObject(xmlStr).toString();
        return xmlStr;
    }
    public boolean findDiffrencesBetweenXMLFiles() {

        fh.createFolder(m_folderResultName);
        File file1 = new File(m_xml1);
        File file2 = new File(m_xml2);
        FileReader fr1 = null;
        FileReader fr2 = null;

        try {
            fr1 = new FileReader(file1);
            fr2 = new FileReader(file2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XMLUnit.setIgnoreComments(Boolean.TRUE);
        //  XMLUnit.setCompareUnmatched(Boolean.TRUE);
        XMLUnit.setNormalizeWhitespace(Boolean.TRUE);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(Boolean.TRUE);

        try {
            Diff diff = new Diff(fr1, fr2);
            System.out.println("Similar? " + diff.similar());
            System.out.println("Identical? " + diff.identical());

            DetailedDiff detDiff = new DetailedDiff(diff);
          //  detDiff.overrideMatchTracker(new MatchTrackerImpl());
            detDiff.overrideElementQualifier(new ElementNameQualifier());
            List differences = detDiff.getAllDifferences();

            for (Object object : differences) {
                fh.writeToExistedFile(m_folderResultName + m_kind, "***********************\n");
                Difference difference = (Difference) object;
                System.out.println("***********************");
                fh.writeToExistedFile(m_folderResultName + m_kind, difference.toString() + "\n");
                System.out.println(difference);
                fh.writeToExistedFile(m_folderResultName + m_kind, "***********************\n");
                System.out.println("***********************");
            }

            if(diff.similar() == false){

                return false;

            }



        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }



    class MatchTrackerImpl implements MatchTracker {


        public void matchFound(Difference difference) {
            if (difference != null) {
                NodeDetail controlNode = difference.getControlNodeDetail();
                NodeDetail testNode = difference.getTestNodeDetail();

                String controlNodeValue = printNode(controlNode.getNode());
                String testNodeValue = printNode(testNode.getNode());

                if (controlNodeValue != null) {
                    System.out.println("####################");
                    fh.writeToExistedFile(m_folderResultName + m_kind, "***********************\n");
                    System.out.println("Control Node: " + controlNodeValue);
                    fh.writeToExistedFile(m_folderResultName + m_kind, "Control Node: " + controlNodeValue + "\\n");
                }
                if (testNodeValue != null) {
                    System.out.println("Test Node: " + testNodeValue);
                    fh.writeToExistedFile(m_folderResultName + m_kind, "Test Node: " + testNodeValue + "\\n");
                    System.out.println("####################");
                    fh.writeToExistedFile(m_folderResultName + m_kind, "***********************\n");
                }
            }
        }

        private String printNode(Node node) {
            if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                StringWriter sw = new StringWriter();
                try {
                    Transformer t = TransformerFactory.newInstance().newTransformer();
                    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                    t.transform(new DOMSource(node), new StreamResult(sw));
                } catch (TransformerException te) {
                    System.out.println("nodeToString Transformer Exception");
                }
                return sw.toString();

            }
            return null;
        }

    }


}






