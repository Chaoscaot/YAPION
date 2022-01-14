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

package yapion.hierarchy.output.flavours;

import yapion.hierarchy.output.AbstractOutput;
import yapion.hierarchy.types.YAPIONType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JSONFlavour implements Flavour {

    private static Set<HierarchyTypes> unsupportedTypesSet = new HashSet<>();

    static {
        unsupportedTypesSet.add(HierarchyTypes.COMMENT);
        unsupportedTypesSet.add(HierarchyTypes.POINTER);
        unsupportedTypesSet.add(HierarchyTypes.MAP);
        unsupportedTypesSet = Collections.unmodifiableSet(unsupportedTypesSet);
    }

    @Override
    public Set<HierarchyTypes> unsupportedTypes() {
        return unsupportedTypesSet;
    }

    @Override
    public void begin(HierarchyTypes hierarchyTypes, AbstractOutput output) {
        switch (hierarchyTypes) {
            case OBJECT:
                output.consume("{");
                break;
            case ARRAY:
                output.consume("[");
                break;
        }
    }

    @Override
    public void end(HierarchyTypes hierarchyTypes, AbstractOutput output) {
        switch (hierarchyTypes) {
            case OBJECT:
                output.consume("}");
                break;
            case ARRAY:
                output.consume("]");
                break;
        }
    }

    @Override
    public void beginElement(HierarchyTypes hierarchyTypes, AbstractOutput output, ElementData elementData) {
        if (hierarchyTypes == HierarchyTypes.OBJECT) {
            output.consume("\"").consume(elementData.getName()).consume("\":");
            output.consumePrettified(" ");
        }
    }

    @Override
    public void elementSeparator(HierarchyTypes hierarchyTypes, AbstractOutput output, boolean afterLast) {
        if (!afterLast) {
            output.consume(",");
        }
    }

    @Override
    public <T> void elementValue(HierarchyTypes hierarchyTypes, AbstractOutput output, ValueData<T> valueData) {
        output.consume(valueData.getWrappedValue().getValueHandler().outputJSON(valueData.getValue(), YAPIONType.VALUE));
    }
}
