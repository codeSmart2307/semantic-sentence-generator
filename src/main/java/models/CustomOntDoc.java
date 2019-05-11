package models;

import org.w3c.dom.Document;

public class CustomOntDoc {
    private String namespace;
    private Document ontDocument;

    public CustomOntDoc(String namespace, Document ontDocument) {
        this.namespace = namespace;
        this.ontDocument = ontDocument;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Document getOntDocument() {
        return ontDocument;
    }

    public void setOntDocument(Document ontDocument) {
        this.ontDocument = ontDocument;
    }
}
