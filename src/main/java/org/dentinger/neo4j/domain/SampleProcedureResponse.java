package org.dentinger.neo4j.domain;

public class SampleProcedureResponse {

    public static final SampleProcedureResponse EMPTY = new SampleProcedureResponse(null, null, -1);
    public final String id;
    public final String message;
    public final Number status;

    public SampleProcedureResponse(String id, String message, Number status) {
        this.id = id;
        this.message = message;
        this.status = status;
    }
}
