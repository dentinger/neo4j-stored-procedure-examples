package org.dentinger.neo4j.domain;

public class SampleProcedureResponse {

    public static final SampleProcedureResponse EMPTY = new SampleProcedureResponse(null, null);
    public final String gtin;
    public final String message;

    public SampleProcedureResponse(String gtin, String message) {
        this.gtin = gtin;
        this.message = message;
    }
}
