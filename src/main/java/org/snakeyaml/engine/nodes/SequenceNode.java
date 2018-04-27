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
package org.snakeyaml.engine.nodes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.snakeyaml.engine.common.FlowStyle;
import org.snakeyaml.engine.exceptions.Mark;

/**
 * Represents a sequence.
 * <p>
 * A sequence is a ordered collection of nodes.
 * </p>
 */
public class SequenceNode extends CollectionNode<Node> {
    final private List<Node> value;

    public SequenceNode(Tag tag, boolean resolved, List<Node> value,
                        FlowStyle flowStyle, Optional<Mark> startMark, Optional<Mark> endMark) {
        super(tag, startMark, endMark, flowStyle);
            Objects.requireNonNull(value,"value in a Node is required.");
        this.value = value;
        this.resolved = resolved;
    }

    @Override
    public NodeId getNodeId() {
        return NodeId.sequence;
    }

    /**
     * Returns the elements in this sequence.
     *
     * @return Nodes in the specified order.
     */
    public List<Node> getValue() {
        return value;
    }

    public void setListType(Class<? extends Object> listType) {
        for (Node node : value) {
            node.setType(listType);
        }
    }

    public String toString() {
        return "<" + this.getClass().getName() + " (tag=" + getTag() + ", value=" + getValue()
                + ")>";
    }
}