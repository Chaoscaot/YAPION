/*
 * Copyright 2019,2020,2021 yoyosource
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yapion.serializing.serializer.object.map;

import yapion.annotations.api.SerializerImplementation;
import yapion.hierarchy.api.groups.YAPIONAnyType;
import yapion.hierarchy.types.YAPIONMap;
import yapion.hierarchy.types.YAPIONObject;
import yapion.serializing.InternalSerializer;
import yapion.serializing.data.DeserializeData;
import yapion.serializing.data.SerializeData;

import java.util.Hashtable;
import java.util.Map;

import static yapion.utils.IdentifierUtils.TYPE_IDENTIFIER;

@SerializerImplementation(since = "0.7.0")
public class HashTableSerializer implements InternalSerializer<Hashtable<?, ?>> {

    @Override
    public Class<?> type() {
        return Hashtable.class;
    }

    @Override
    public YAPIONAnyType serialize(SerializeData<Hashtable<?, ?>> serializeData) {
        YAPIONObject yapionObject = new YAPIONObject();
        yapionObject.add(TYPE_IDENTIFIER, type());
        YAPIONMap yapionMap = new YAPIONMap();
        yapionObject.add("values", yapionMap);
        for (Map.Entry<?, ?> entry : serializeData.object.entrySet()) {
            yapionMap.add(serializeData.serialize(entry.getKey()), serializeData.serialize(entry.getValue()));
        }
        return yapionObject;
    }

    @Override
    @SuppressWarnings({"java:S1149"})
    public Hashtable<?, ?> deserialize(DeserializeData<? extends YAPIONAnyType> deserializeData) {
        YAPIONMap yapionMap = ((YAPIONObject) deserializeData.object).getMap("values");
        Hashtable<Object, Object> table = new Hashtable<>();
        for (YAPIONAnyType key : yapionMap.getKeys()) {
            table.put(deserializeData.deserialize(key), deserializeData.deserialize(yapionMap.get(key)));
        }
        return table;
    }
}
