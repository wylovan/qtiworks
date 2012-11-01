/* Copyright (c) 2012, University of Edinburgh.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * This software is derived from (and contains code from) QTItools and MathAssessEngine.
 * QTItools is (c) 2008, University of Southampton.
 * MathAssessEngine is (c) 2010, University of Edinburgh.
 */
package uk.ac.ed.ph.jqtiplus.state;

import uk.ac.ed.ph.jqtiplus.internal.util.DumpMode;
import uk.ac.ed.ph.jqtiplus.internal.util.ObjectDumperOptions;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.Ordering;
import uk.ac.ed.ph.jqtiplus.node.test.Selection;
import uk.ac.ed.ph.jqtiplus.running.TestPlanner;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the shape of an {@link AssessmentTest} once
 * {@link Ordering} and {@link Selection} have been applied.
 * <p>
 * Nodes corresponding to {@link AssessmentItemRef}s in the plan will satisfy
 * the following properties:
 * <ul>
 *   <li>Their identifier is unique amongst other such Nodes</li>
 *   <li>The resulting item was successfully looked up</li>
 * </ul>
 * <p>
 * An instance of this class should be consider immutable once created.
 *
 * @see TestPlanNode
 * @see TestPlanner
 */
@ObjectDumperOptions(DumpMode.DEEP)
public final class TestPlan implements Serializable {

    private static final long serialVersionUID = 5176553452095038589L;

    /** Root of the {@link TestPlanNode} tree */
    private final TestPlanNode testPlanRootNode;

    /** Map of all {@link TestPlanNode}s, in the correct order */
    private final Map<TestPlanNodeKey, TestPlanNode> testPlanNodeMap;

    /**
     * Map of the {@link TestPlanNode}s corresponding to each {@link Identifier}.
     * There will be multiple elements in the case of non-unique {@link Identifier}s and
     * selection with replacement.
     */
    private final Map<Identifier, List<TestPlanNode>> testPlanNodesByIdentifierMap;

    /** (This is the most efficient constructor) */
    public TestPlan(final TestPlanNode testPlanRootNode, final Map<Identifier, List<TestPlanNode>> testPlanNodesByIdentifierMap) {
        this.testPlanRootNode = testPlanRootNode;
        this.testPlanNodesByIdentifierMap = testPlanNodesByIdentifierMap;

        final Map<TestPlanNodeKey, TestPlanNode> testPlanNodeMapBuilder = new LinkedHashMap<TestPlanNodeKey, TestPlanNode>();
        for (final List<TestPlanNode> testPlanNodeList : testPlanNodesByIdentifierMap.values()) {
            for (final TestPlanNode testPlanNode : testPlanNodeList) {
                testPlanNodeMapBuilder.put(testPlanNode.getKey(), testPlanNode);
            }
        }
        this.testPlanNodeMap = Collections.unmodifiableMap(testPlanNodeMapBuilder);
    }

    /** (Convenience constructor) */
    public TestPlan(final TestPlanNode testPlanRootNode) {
        final Map<Identifier, List<TestPlanNode>> testPlanNodesByIdentifierMapBuilder = new LinkedHashMap<Identifier, List<TestPlanNode>>();
        final Map<TestPlanNodeKey, TestPlanNode> testPlanNodeMapBuilder = new LinkedHashMap<TestPlanNodeKey, TestPlanNode>();
        for (final TestPlanNode testPlanNode : testPlanRootNode.searchDescendants()) {
            final TestPlanNodeKey key = testPlanNode.getKey();
            if (key==null) {
                throw new IllegalArgumentException("Did not expect to find a Node " + testPlanNode + " with null key");
            }
            testPlanNodeMapBuilder.put(key, testPlanNode);
            List<TestPlanNode> nodesByIdentifier = testPlanNodesByIdentifierMapBuilder.get(key.getIdentifier());
            if (nodesByIdentifier==null) {
                nodesByIdentifier = new ArrayList<TestPlanNode>();
                testPlanNodesByIdentifierMapBuilder.put(key.getIdentifier(), nodesByIdentifier);
            }
            nodesByIdentifier.add(testPlanNode);
        }
        this.testPlanRootNode = testPlanRootNode;
        this.testPlanNodesByIdentifierMap = Collections.unmodifiableMap(testPlanNodesByIdentifierMapBuilder);
        this.testPlanNodeMap = Collections.unmodifiableMap(testPlanNodeMapBuilder);
    }

    public Map<TestPlanNodeKey, TestPlanNode> getTestPlanNodeMap() {
        return testPlanNodeMap;
    }

    public TestPlanNode getTestPlanRootNode() {
        return testPlanRootNode;
    }

    public List<TestPlanNode> getTestPartNodes() {
        return testPlanRootNode.getChildren();
    }

    public List<TestPlanNode> getNodes(final Identifier identifier) {
        final List<TestPlanNode> nodesForIdentifier = testPlanNodesByIdentifierMap.get(identifier);
        if (nodesForIdentifier==null) {
            return null;
        }
        return Collections.unmodifiableList(nodesForIdentifier);
    }

    public TestPlanNode getNodeInstance(final Identifier identifier, final int instanceNumber) {
        final List<TestPlanNode> nodesForIdentifier = testPlanNodesByIdentifierMap.get(identifier);
        if (nodesForIdentifier==null) {
            return null;
        }
        if (instanceNumber<0 || instanceNumber>=nodesForIdentifier.size()) {
            return null;
        }
        return nodesForIdentifier.get(instanceNumber);
    }

    public List<TestPlanNode> searchNodes(final TestNodeType testNodeType) {
        return testPlanRootNode.searchDescendants(testNodeType);
    }

    //-------------------------------------------------------------------

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this))
                + "(testPlanRootNode=" + testPlanRootNode
                + ")";
    }

    public String debugStructure() {
        final StringBuilder result = new StringBuilder();
        buildStructure(result, testPlanRootNode.getChildren(), 0);
        return result.toString();
    }

    private void buildStructure(final StringBuilder result, final List<TestPlanNode> testPlanNodes, final int indent) {
        for (final TestPlanNode testPlanNode : testPlanNodes) {
            for (int i = 0; i < indent; i++) {
                result.append("  ");
            }
            result.append(testPlanNode.getTestNodeType())
                .append('(')
                .append(testPlanNode.getKey())
                .append(")\n");
            buildStructure(result, testPlanNode.getChildren(), indent + 1);
        }
    }

}
