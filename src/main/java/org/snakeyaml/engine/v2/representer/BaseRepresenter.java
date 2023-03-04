/**
 * Copyright (c) 2018, http://www.snakeyaml.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snakeyaml.engine.v2.representer;

import org.snakeyaml.engine.v2.api.RepresentToNode;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.AnchorNode;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represent basic YAML structures: scalar, sequence, mapping
 */
public abstract class BaseRepresenter {
    /**
     * Keep representers which must match the class exactly
     */
    protected final Map<Class<?>, RepresentToNode> representers = new HashMap();
    /**
     * in Java 'null' is not a type. So we have to keep the null representer
     * separately otherwise it will coincide with the default representer which
     * is stored with the key null.
     */
    protected RepresentToNode nullRepresenter;
    // the order is important (map can be also a sequence of key-values)
    /**
     * Keep representers which match a parent of the class to be represented
     */
    protected final Map<Class<?>, RepresentToNode> parentClassRepresenters = new LinkedHashMap();
    protected ScalarStyle defaultScalarStyle = ScalarStyle.PLAIN;
    protected FlowStyle defaultFlowStyle = FlowStyle.AUTO;
    protected final Map<Object, Node> representedObjects = new IdentityHashMap<Object, Node>() {
        @Override
        public Node put(Object key, Node value) {
            return super.put(key, new AnchorNode(value));
        }
    };

    protected Object objectToRepresent;

    /**
     * Represent the provided Java instance to a Node
     *
     * @param data - Java instance to be represented
     * @return The Node to be serialized
     */
    public Node represent(Object data) {
        Node node = representData(data);
        representedObjects.clear();
        objectToRepresent = null;
        return node;
    }

    /**
     * Find the representer which is suitable to represent the internal structure of the provided instance to
     * a Node
     *
     * @param data - the data to be serialized
     * @return RepresentToNode to call to create a Node
     */
    protected Optional<RepresentToNode> findRepresenterFor(Object data) {
        Class<?> clazz = data.getClass();
        // check the same class
        if (representers.containsKey(clazz)) {
            return Optional.of(representers.get(clazz));
        } else {
            // check the parents
            for (Map.Entry<Class<?>, RepresentToNode> parentRepresenterEntry : parentClassRepresenters.entrySet()) {
                if (parentRepresenterEntry.getKey().isInstance(data)) {
                    return Optional.of(parentRepresenterEntry.getValue());
                }
            }
            return Optional.empty();
        }
    }

    protected final Node representData(Object data) {
        objectToRepresent = data;
        // check for identity
        if (representedObjects.containsKey(objectToRepresent)) {
            return representedObjects.get(objectToRepresent);
        }
        // check for null first
        if (data == null) {
            return nullRepresenter.representData(null);
        }
        RepresentToNode representer = findRepresenterFor(data)
                .orElseThrow(() -> new YamlEngineException("Representer is not defined for " + data.getClass()));
        return representer.representData(data);
    }

    protected Node representScalar(Tag tag, String value, ScalarStyle style) {
        if (style == ScalarStyle.PLAIN) {
            style = this.defaultScalarStyle;
        }
        return new ScalarNode(tag, value, style);
    }

    protected Node representScalar(Tag tag, String value) {
        return representScalar(tag, value, ScalarStyle.PLAIN);
    }

    protected Node representSequence(Tag tag, Iterable<?> sequence, FlowStyle flowStyle) {
        int size = 10;// default for ArrayList
        if (sequence instanceof List<?>) {
            size = ((List<?>) sequence).size();
        }
        List<Node> value = new ArrayList<>(size);
        SequenceNode node = new SequenceNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        FlowStyle bestStyle = FlowStyle.FLOW;
        for (Object item : sequence) {
            Node nodeItem = representData(item);
            if (!(nodeItem instanceof ScalarNode && ((ScalarNode) nodeItem).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            value.add(nodeItem);
        }
        if (flowStyle == FlowStyle.AUTO) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle);
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }

    protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
        List<NodeTuple> value = new ArrayList<>(mapping.size());
        MappingNode node = new MappingNode(tag, value, flowStyle);
        representedObjects.put(objectToRepresent, node);
        FlowStyle bestStyle = FlowStyle.FLOW;
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            Node nodeKey = representData(entry.getKey());
            Node nodeValue = representData(entry.getValue());
            if (!(nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).isPlain())) {
                bestStyle = FlowStyle.BLOCK;
            }
            value.add(new NodeTuple(nodeKey, nodeValue));
        }
        if (flowStyle == FlowStyle.AUTO) {
            if (defaultFlowStyle != FlowStyle.AUTO) {
                node.setFlowStyle(defaultFlowStyle);
            } else {
                node.setFlowStyle(bestStyle);
            }
        }
        return node;
    }
}