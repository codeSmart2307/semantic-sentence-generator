package utility;

import enums.OntFileType;
import models.CustomOntDoc;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class OntFileReader {

    private String filePath;

    public OntFileReader(String path) {
        this.filePath = path;
    }

    public CustomOntDoc documentLoader() throws SAXException, IOException, ParserConfigurationException {
        String namespace = FileManager.get().loadModel(filePath).getNsPrefixURI("");
        CustomOntDoc ontDoc = new CustomOntDoc(namespace, xmlDocumentLoader());
        return ontDoc;
    }

    private Document xmlDocumentLoader() throws SAXException, IOException, ParserConfigurationException {
        InputStream input = FileManager.get().open(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(input);
        document.getDocumentElement().normalize();
        return document;
    }
}
