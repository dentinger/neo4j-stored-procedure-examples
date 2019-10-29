package org.dentinger.neo4j.domain;

import org.neo4j.graphdb.Label;

public class Constants {

    public static final Label DATA_LABEL = Label.label("Data");
    public static final Label SUBDATA_LABEL = Label.label("SubData");
    public static final String NAME_KEY = "name";
    public static final String UUID_KEY = "id";
    public static final String SOMEDATA_KEY = "somedata";

}
