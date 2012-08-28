package com.googlecode.funclate;

import com.googlecode.totallylazy.Arrays;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Pair;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.one;
import static com.googlecode.totallylazy.matchers.IterableMatcher.hasExactly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

abstract public class ModelContract {
    private Model createModel() {
        return modelFactory().create();
    }

    private Model createModel(Iterable<Pair<String, Object>> values) {
        return modelFactory().create(values);
    }

    private Model fromMap(Map<String, Object> root) {
        return modelFactory().create(root);
    }

    private Model parse(String serialized) {
        return modelFactory().create(serialized);
    }

    protected abstract ModelFactory modelFactory();

    @Test
    public void supportsSingleValues() throws Exception {
        Model model = createModel().
                add("key", "value");
        assertThat(model.get("key", String.class), is("value"));
    }

    @Test
    public void supportsSet() throws Exception {
        Model model = createModel().add("key", "value").set("key", "foo");
        assertThat(model.get("key", String.class), is("foo"));
    }

    @Test
    public void supportsCopy() throws Exception {
        Model model = createModel().
                add("key", "value");
        Model copy = model.copy();
        assertThat(copy, is(not(sameInstance(model))));
        assertThat(copy, is(model));
    }

    @Test
    public void returnsEmptyListWhenNoValues() throws Exception {
        assertThat(createModel().getValues("key"), is(empty()));
    }

    @Test
    public void supportsMultiValues() throws Exception {
        Model model = createModel().
                add("key", "one").
                add("key", "two");
        assertThat(model.getValues("key", String.class), hasExactly("one", "two"));
    }

    @Test
    public void supportsListMultiValues() throws Exception {
        Model model = createModel().
                add("key", Arrays.list("one")).
                add("key", Arrays.list("two"));
        assertThat(model.getValues("key", String.class), hasExactly("one", "two"));
    }

    @Test
    public void multiValuesCanBeRetrievedAsTheFirstValue() throws Exception {
        Model model = createModel().
                add("key", "one").
                add("key", "two");
        assertThat(model.get("key", String.class), is("one"));
    }

    @Test
    public void shouldPreserveOrdering() throws Exception {
        Model original = createModel().
                add("1", "1").add("2", "2").add("2", "3");

        Set<Map.Entry<String, Object>> entries = original.entries();
        Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
        Map.Entry<String, Object> first = iterator.next();
        Map.Entry<String, Object> second = iterator.next();
        assertThat(first.getKey(), is("1"));
        assertThat(first.getValue(), equalTo((Object) "1"));
        assertThat(second.getKey(), is("2"));
        assertThat(second.getValue(), equalTo((Object) new ArrayList<String>(Arrays.list("2", "3"))));
    }

    @Test
    public void supportsRemove() throws Exception {
        final Model model = createModel(one(Pair.<String, Object>pair("key", "value")));
        model.remove("key");
        model.add("key", "value");
        final Pair<Model, Option<String>> value = Pair.pair(createModel(), some("value"));
        final Pair<Model, Option<String>> key = model.remove("key", String.class);
        assertThat(key, is(value));
    }

    @Test
    public void shouldPreserveListOrderingWhenConvertingToJson() throws Exception {
        Model original = createModel().
                add("2", "3").add("2", "2");

        MatcherAssert.assertThat(original.toString(), equalTo("{\"2\":[\"3\",\"2\"]}"));
    }

    @Test
    public void canConvertToAMap() throws Exception {
        Model original = createModel().
                add("users", createModel().
                        add("user", createModel().
                                add("name", "Dan").
                                add("tel", "34567890")).
                        add("user", createModel().
                                add("name", "Mat").
                                add("tel", "978532")));

        Map<String, Object> root = original.toMap();
        Map<String, Object> users = (Map<String, Object>) root.get("users");
        List<Map<String, Object>> user = (List<Map<String, Object>>) users.get("user");

        Map<String, Object> dan = user.get(0);
        MatcherAssert.assertThat((String) dan.get("name"), is("Dan"));
        MatcherAssert.assertThat((String) dan.get("tel"), is("34567890"));

        Map<String, Object> mat = user.get(1);
        MatcherAssert.assertThat((String) mat.get("name"), is("Mat"));
        MatcherAssert.assertThat((String) mat.get("tel"), is("978532"));

        // reverse it

        Model reversed = fromMap(root);
        MatcherAssert.assertThat(reversed, is(original));
    }

    @Test
    public void supportsConvertingToStringAndBack() throws Exception {
        Model original = createModel().
                add("users", createModel().
                        add("user", createModel().
                                add("name", "Dan").
                                add("tel", "34567890")).
                        add("user", createModel().
                                add("name", "Mat").
                                add("tel", "978532")));

        String serialized = original.toString();
        Model result = parse(serialized);
        MatcherAssert.assertThat(result, is(original));
    }
}
