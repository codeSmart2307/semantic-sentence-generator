import enums.OntFileType;
import models.CustomOntDoc;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.base.Sys;
import utility.OntFileReader;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class SentenceGenerator {

    private static final String PATH = "E:/Projects/semantic-web-projects/university.rdf";
    private static String namespace;
    private static Document ontDocument;
    private HashMap<String, HashMap<String, String>> ontMap = new HashMap<String, HashMap<String, String>>();

    public static void main(String[] args) {
        OntFileReader ontFileReader = new OntFileReader(PATH);
        CustomOntDoc ontDoc;
        try {
            ontDoc = ontFileReader.documentLoader();
            namespace = ontDoc.getNamespace();
            ontDocument = ontDoc.getOntDocument();
            System.out.println("Namespace: " + namespace);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        SentenceGenerator generator = new SentenceGenerator();
        generator.individualExtractor();
        generator.generateSentences();
    }

    private void individualExtractor() {
        // Check for OWL / RDF extensions to differentiate the processing
        String fileType = PATH.substring(PATH.lastIndexOf('.') + 1).trim();

        if (fileType.equals(OntFileType.RDF.getType())) {
            OntModel model = ModelFactory.createOntologyModel(); // Load knowldge model
            InputStream in = FileManager.get().open(PATH);
            model.read(in, "");
            ExtendedIterator instances = model.listIndividuals();
            String qry;

            while (instances.hasNext()) {
                Individual ind = (Individual) instances.next();// Get first individual
                String individual = ind.getURI();

                // Here the SPARQL query has been parameterized via assigning individual / subject only.
                // Then, accordindly all predicates and objects associated with the individual will be captured.
                qry = "SELECT ?y ?z WHERE {" + "<" + individual + "> ?y ?z ." + "}";

                // Extract individual / subject
                String subject = (individual.replace(namespace, ""));

                ontMap.put(subject, new HashMap<String, String>());

                Query query = QueryFactory.create(qry);
                QueryExecution qexec = QueryExecutionFactory.create(query, model);

                try {
                    ResultSet rs = qexec.execSelect();
                    String predicate;
                    String object;
                    System.out.println("========================= Subject: " + subject + " =============================");
                    while (rs.hasNext()) {
                        QuerySolution sol = rs.nextSolution();
                        String predicateUriWithNamespace = sol.get("y").toString();
                        /*
                            This is an interesting logic to eliminate unwanted URLs
                            If from a paricular resource url, if the host is coming
                            as www.w3.org, that means it`s not a resource related url
                            but RDF syntax related, which we do not need
                        */
                        URL url1 = new URL(predicateUriWithNamespace);
                        String rslt = url1.getHost();
                        if (rslt.equals("www.w3.org")) continue;

                        // This becomes the predicate
                        // Remove namespace URL and get the end value only
                        predicate = predicateUriWithNamespace.replace(namespace, "");

                        String objectUriWithNamespace = sol.get("z").toString(); // This is the object value
                        object = objectUriWithNamespace.replace(namespace, "");
                        String propertyType = "";
                        if (sol.get("z").isResource()) {
                            // Object value can be a resource
                            // Then definitely those will be OBJECT PROPERTIES
                            propertyType = "Object";
                        } else if (sol.get("z").isLiteral()) {
                            // Object values can be literals
                            // Then definitely, those will be DATA PROPERTIES
                            propertyType = "Data";
                        }
                        System.out.println("Predicate: " + predicate);
                        System.out.println("Property Type: " + propertyType);
                        System.out.println("Object: " + object);

                        ontMap.get(subject).put(predicate, object);

                        System.out.println("----------------------------------------------------");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } finally {
                    qexec.close();
                }
            }
        }
    }

    private void generateSentences() {
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
        Realiser realiser = new Realiser(lexicon);

        SPhraseSpec p = nlgFactory.createClause();
        Iterator<String> subjectKeyIterator = ontMap.keySet().iterator();
        while (subjectKeyIterator.hasNext()) {
            String currSubject = subjectKeyIterator.next();
            String subject, verb, object;

            if (currSubject.contains("Lecturer")) {
                subject = ontMap.get(currSubject).get("first_name") + " " + ontMap.get(currSubject).get("last_name");
                verb = "teaches";
                object = ontMap.get(currSubject).get("teaches");
            } else if (currSubject.contains("Student")) {
                subject = ontMap.get(currSubject).get("first_name") + " " + ontMap.get(currSubject).get("last_name");
                verb = "studies";
                object = ontMap.get(currSubject).get("studies");
            } else {
                subject = currSubject;
                verb = "is a";
                object = "course";
            }

            p.setSubject(subject);
            p.setVerb(verb);
            p.setObject(object);

            String output = realiser.realiseSentence(p); // Realiser created earlier.
            System.out.println("Sentence: " + output);
        }


    }
}
