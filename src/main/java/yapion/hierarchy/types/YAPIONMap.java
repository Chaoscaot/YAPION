// SPDX-License-Identifier: Apache-2.0
// YAPION
// Copyright (C) 2019,2020 yoyosource

package yapion.hierarchy.types;

import yapion.annotations.deserialize.YAPIONLoadExclude;
import yapion.annotations.serialize.YAPIONSave;
import yapion.annotations.serialize.YAPIONSaveExclude;
import yapion.hierarchy.Type;
import yapion.hierarchy.YAPIONAny;
import yapion.hierarchy.YAPIONVariable;
import yapion.parser.JSONMapper;
import yapion.parser.YAPIONParser;
import yapion.parser.YAPIONParserMapMapping;
import yapion.parser.YAPIONParserMapObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@YAPIONSave(context = "*")
public class YAPIONMap extends YAPIONAny {

    private final Map<YAPIONAny, YAPIONAny> variables = new LinkedHashMap<>();
    @YAPIONSaveExclude(context = "*")
    @YAPIONLoadExclude(context = "*")
    private final List<YAPIONParserMapMapping> mappingList = new ArrayList<>();
    @YAPIONSaveExclude(context = "*")
    @YAPIONLoadExclude(context = "*")
    private final Map<String, YAPIONAny> mappingVariables = new LinkedHashMap<>();

    @Override
    public Type getType() {
        return Type.MAP;
    }

    @Override
    public long referenceValue() {
        return getType().getReferenceValue();
    }

    @Override
    public String getPath(YAPIONAny yapionAny) {
        for (Map.Entry<YAPIONAny, YAPIONAny> entry : variables.entrySet()) {
            if (entry.getValue() == yapionAny) {
                return entry.getKey().toString();
            }
        }
        return "";
    }

    @Override
    public void toOutputStream(OutputStream outputStream) throws IOException {
        long id = 0;
        outputStream.write("<".getBytes(StandardCharsets.UTF_8));
        boolean b = false;
        for (Map.Entry<YAPIONAny, YAPIONAny> entry : variables.entrySet()) {
            String id1 = String.format("%01X", id++);
            String id2 = String.format("%01X", id++);

            if (b) {
                outputStream.write(",".getBytes(StandardCharsets.UTF_8));
            }
            b = true;

            outputStream.write((id1 + ":" + id2).getBytes(StandardCharsets.UTF_8));
            outputStream.write(("#" + id1).getBytes(StandardCharsets.UTF_8));
            entry.getKey().toOutputStream(outputStream);
            outputStream.write(("#" + id2).getBytes(StandardCharsets.UTF_8));
            entry.getValue().toOutputStream(outputStream);
        }
        outputStream.write(">".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toJSONString() {
        YAPIONObject yapionObject = new YAPIONObject();
        YAPIONArray mapping = new YAPIONArray();
        yapionObject.add(JSONMapper.MAP, mapping);

        long id = 0;
        for (Map.Entry<YAPIONAny, YAPIONAny> entry : variables.entrySet()) {
            String id1 = String.format("%01X", id++);
            String id2 = String.format("%01X", id++);

            mapping.add(new YAPIONValue<>(id1 + ":" + id2));
            yapionObject.add("#" + id1, entry.getKey());
            yapionObject.add("#" + id2, entry.getValue());
        }
        return yapionObject.toJSONString();
    }

    public YAPIONMap add(YAPIONAny key, YAPIONAny value) {
        variables.put(key, value);
        return this;
    }

    public YAPIONMap add(YAPIONVariable variable) {
        return add(new YAPIONValue<>(variable.getName()), variable.getValue());
    }

    public YAPIONMap add(YAPIONParserMapObject variable) {
        mappingVariables.put(variable.variable.getName().substring(1), variable.variable.getValue());
        variable.variable.getValue().setParent(this);
        return this;
    }

    public YAPIONMap add(YAPIONParserMapMapping mapping) {
        mappingList.add(mapping);
        return this;
    }

    public synchronized YAPIONMap finishMapping() {
        if (mappingVariables.isEmpty()) {
            return this;
        }

        for (YAPIONParserMapMapping mapping : mappingList) {
            String[] strings = mapping.mapping.get().split(":");
            if (strings.length != 2) {
                continue;
            }

            variables.put(mappingVariables.get(strings[0]), mappingVariables.get(strings[1]));
        }

        mappingVariables.clear();
        mappingList.clear();
        return this;
    }

    public YAPIONAny get(YAPIONAny key) {
        return variables.get(key);
    }

    public List<YAPIONAny> getKeys() {
        return new ArrayList<>(variables.keySet());
    }

    @Override
    protected Optional<YAPIONSearch<? extends YAPIONAny>> get(String key) {
        YAPIONVariable variable = YAPIONParser.parse("{" + key + "}").getVariable("");
        if (variable == null) return Optional.empty();
        YAPIONAny anyKey = variable.getValue();
        if (anyKey == null) return Optional.empty();
        YAPIONAny anyValue = get(anyKey);
        if (anyValue == null) return Optional.empty();
        return Optional.of(new YAPIONSearch<>(anyValue));
    }

    @Override
    public String toString() {
        long id = 0;

        StringBuilder st = new StringBuilder();
        st.append("<");
        for (Map.Entry<YAPIONAny, YAPIONAny> entry : variables.entrySet()) {
            String id1 = String.format("%01X", id++);
            String id2 = String.format("%01X", id++);

            st.append(id1).append(":").append(id2);
            st.append("#").append(id1).append(entry.getKey().toString());
            st.append("#").append(id2).append(entry.getValue().toString());
        }
        st.append(">");
        return st.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YAPIONMap)) return false;
        YAPIONMap yapionMap = (YAPIONMap) o;
        return Objects.equals(variables, yapionMap.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variables);
    }
}