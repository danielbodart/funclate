package com.googlecode.funclate.json;

import com.googlecode.funclate.Model;
import com.googlecode.funclate.json.grammar.Grammar;
import com.googlecode.totallylazy.Callable1;

import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class Json {
    public static final String SEPARATOR = ",";

    public static  String toJson(Object value) {
        if (value instanceof String) return quote((String) value);
        if (value instanceof Map) return toObjectLiteral((Map) value);
        if (value instanceof Iterable) return toArray((Iterable) value);
        if (value instanceof Number) return String.valueOf(value);
        if (value instanceof Model) return String.valueOf(value);
        return quote(String.valueOf(value));
    }

    public static  String quote(String value) {
        return format("\"%s\"", value.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    public static String toPair(Object key, Object value) {
        return format("%s:%s", quote(String.valueOf(key)), toJson(value));
    }

    public static  String toArray(Iterable values) {
        return sequence(values).map(toJson()).toString("[", SEPARATOR, "]");
    }

    public static  String toObjectLiteral(Map map) {
        return sequence(map.entrySet()).map(asString()).toString("{", SEPARATOR, "}");
    }

    public static Callable1<? super Map.Entry, String> asString() {
        return new Callable1<Map.Entry, String>() {
            public String call(Map.Entry entry) throws Exception {
                return toPair(entry.getKey(), entry.getValue());
            }
        };
    }

    public static Callable1<Object, String> toJson() {
        return new Callable1<Object, String>() {
            public String call(Object value) throws Exception {
                return toJson(value);
            }
        };
    }

    public static Map<String, Object> parse(String json) {
        return Grammar.OBJECT.parse(json);
    }
}
