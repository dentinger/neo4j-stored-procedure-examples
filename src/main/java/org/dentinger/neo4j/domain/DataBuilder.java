package org.dentinger.neo4j.domain;

import java.util.List;
import java.util.Map;

import static org.dentinger.neo4j.domain.Constants.*;

public interface DataBuilder {




    static Data build(Map rawData) {
        return new Data (
                id(rawData),
                name(rawData),
                somedata(rawData),
                subData(rawData),
                relations(rawData));
    }

    static String name(Map map) {
        return map.get(NAME_KEY) != null ? (String) map.get(NAME_KEY) : "UNKNOWN";
    }

    static String id(Map map) {
        return (String) map.get(UUID_KEY);
    }

    static String somedata(Map map) { return (String)map.get(SOMEDATA_KEY); }

    static List<Data> relations(Map map) { return null;}
    static SubData subData(Map map) {

        Map sub = (Map) map.get("subData");
        if(sub != null) {
            return new SubData((String)sub.get(NAME_KEY), (String)sub.get("subData"));
        }
        return null;
    }

}