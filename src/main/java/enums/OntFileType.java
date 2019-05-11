package enums;

public enum OntFileType {
    RDF ("rdf"),
    OWL ("owl");

    private final String TYPE;

    OntFileType(String type) {
        this.TYPE = type;
    }

    public String getType() {
        return this.TYPE;
    }

}
