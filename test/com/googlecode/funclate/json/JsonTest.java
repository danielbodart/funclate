package com.googlecode.funclate.json;

import com.googlecode.funclate.Model;
import com.googlecode.totallylazy.Randoms;
import com.googlecode.totallylazy.Uri;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.googlecode.funclate.Model.model;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JsonTest {
    @Test
    public void correctlyParsesASingleRootElement() throws Exception {
        Map<String, Object> result = Json.parse(("{\"root\" : \"text\"}"));

        assertThat((String) result.get("root"), is("text"));
    }

    @Test
    public void correctlyRendersASingleRootElement() throws Exception {
        String result = Json.toJson(model().
                add("root", "text"));

        assertThat(result,
                is("{\"root\":\"text\"}"));
    }

    @Test
    public void correctlyRendersAndParsesIntegersAndText() throws Exception {
        Model model = model().
                add("root", model().
                        add("child", BigDecimal.valueOf(1)).
                        add("child", BigDecimal.valueOf(-5)).
                        add("child", "text"));

        String json = Json.toJson(model);

        assertThat(json,
                is("{\"root\":{\"child\":[1,-5,\"text\"]}}"));

        assertThat(Json.parse(json), is(model.toMap()));
    }

    @Test
    public void handlesOtherDataType() throws Exception {
        String json = Json.toJson(model().add("uri", Uri.uri("http://code.google.com/p/funclate/")));

        assertThat(json, is("{\"uri\":\"http://code.google.com/p/funclate/\"}"));
    }

    @Test
    public void handlesNumbers() throws Exception {
        Integer number = Randoms.integers().head();

        String json = Json.toJson(model().add("number", number));

        assertThat(json, is("{\"number\":" + number + "}"));
    }

    @Test
    public void handlesBooleans() throws Exception {
        assertThat(Json.toJson(model().add("Boolean", Boolean.TRUE)), is("{\"Boolean\":true}"));
        assertThat(Json.toJson(model().add("boolean", false)), is("{\"boolean\":false}"));
    }

    @Test
    public void correctlyRendersAModel() throws Exception {
        String result = Json.toJson(model().
                add("root", model().
                        add("foo", "bar").
                        add("foo", model().
                                add("baz", 1).
                                add("baz", 2))));

        assertThat(result,
                is("{\"root\":{\"foo\":[\"bar\",{\"baz\":[1,2]}]}}"));
    }

    @Test
    public void shouldPreserveNewLineCharacters() throws Exception {
        String result = Json.toJson(model().add("text", "this is \\n a test"));
        Map<String, Object> parsed = Json.parse(result);
        assertThat((String) parsed.get("text"), is("this is \\n a test"));
    }


    @Test
    public void handlesQuotedText() throws Exception {
        String result = Json.toJson(model().add("text", "He said \"Hello\" then ..."));

        assertThat(result, is("{\"text\":\"He said \\\"Hello\\\" then ...\"}"));

        Map<String, Object> parsed = Json.parse(result);
        assertThat((String) parsed.get("text"), is("He said \"Hello\" then ..."));
    }
}
