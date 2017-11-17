/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DAGraphTests {
    @Test
    public void testDAGraphGetNext() {
        /**
         *   |-------->[D]------>[B]-----------[A]
         *   |                   ^              ^
         *   |                   |              |
         *  [F]------->[E]-------|              |
         *   |          |                       |
         *   |          |------->[G]----->[C]----
         *   |
         *   |-------->[H]-------------------->[I]
         */
        List<String> expectedOrder = new ArrayList<>();
        expectedOrder.add("A"); expectedOrder.add("I"); // Level 0
        expectedOrder.add("B"); expectedOrder.add("C"); expectedOrder.add("H"); // Level 1
        expectedOrder.add("D"); expectedOrder.add("G"); // Level 2
        expectedOrder.add("E"); // Level 3
        expectedOrder.add("F"); // Level 4

        ItemHolder nodeA = new ItemHolder("A", "dataA");
        ItemHolder nodeI = new ItemHolder("I", "dataI");

        ItemHolder nodeB = new ItemHolder("B", "dataB");
        nodeB.addDependency(nodeA.key());

        ItemHolder nodeC = new ItemHolder("C", "dataC");
        nodeC.addDependency(nodeA.key());

        ItemHolder nodeH = new ItemHolder("H", "dataH");
        nodeH.addDependency(nodeI.key());

        ItemHolder nodeG = new ItemHolder("G", "dataG");
        nodeG.addDependency(nodeC.key());

        ItemHolder nodeE = new ItemHolder("E", "dataE");
        nodeE.addDependency(nodeB.key());
        nodeE.addDependency(nodeG.key());

        ItemHolder nodeD = new ItemHolder("D", "dataD");
        nodeD.addDependency(nodeB.key());


        ItemHolder nodeF = new ItemHolder("F", "dataF");
        nodeF.addDependency(nodeD.key());
        nodeF.addDependency(nodeE.key());
        nodeF.addDependency(nodeH.key());

        DAGraph<String, ItemHolder> dag = new DAGraph<>(nodeF);
        dag.addNode(nodeA);
        dag.addNode(nodeB);
        dag.addNode(nodeC);
        dag.addNode(nodeD);
        dag.addNode(nodeE);
        dag.addNode(nodeG);
        dag.addNode(nodeH);
        dag.addNode(nodeI);

        dag.prepare();
        ItemHolder nextNode = dag.getNext();
        int i = 0;
        while (nextNode != null) {
            Assert.assertEquals(nextNode.key(), expectedOrder.get(i));
            dag.reportCompletion(nextNode);
            nextNode = dag.getNext();
            i++;
        }

        System.out.println("done");
    }

    @Test
    public void testGraphMerge() {
        /**
         *   |-------->[D]------>[B]-----------[A]
         *   |                   ^              ^
         *   |                   |              |
         *  [F]------->[E]-------|              |
         *   |          |                       |
         *   |          |------->[G]----->[C]----
         *   |
         *   |-------->[H]-------------------->[I]
         */
        List<String> expectedOrder = new ArrayList<>();
        expectedOrder.add("A"); expectedOrder.add("I"); // Level 0
        expectedOrder.add("B"); expectedOrder.add("C"); expectedOrder.add("H"); // Level 1
        expectedOrder.add("D"); expectedOrder.add("G"); // Level 2
        expectedOrder.add("E"); // Level 3
        expectedOrder.add("F"); // Level 4

        DAGraph<String, ItemHolder> graphA = createGraph("A");
        DAGraph<String, ItemHolder> graphI = createGraph("I");

        DAGraph<String, ItemHolder> graphB = createGraph("B");
        graphA.merge(graphB);

        DAGraph<String, ItemHolder> graphC = createGraph("C");
        graphA.merge(graphC);

        DAGraph<String, ItemHolder> graphH = createGraph("H");
        graphI.merge(graphH);

        DAGraph<String, ItemHolder> graphG = createGraph("G");
        graphC.merge(graphG);

        DAGraph<String, ItemHolder> graphE = createGraph("E");
        graphB.merge(graphE);
        graphG.merge(graphE);

        DAGraph<String, ItemHolder> graphD = createGraph("D");
        graphB.merge(graphD);

        DAGraph<String, ItemHolder> graphF = createGraph("F");
        graphD.merge(graphF);
        graphE.merge(graphF);
        graphH.merge(graphF);

        DAGraph<String, ItemHolder> dag = graphF;
        dag.prepare();

        ItemHolder nextNode = dag.getNext();
        int i = 0;
        while (nextNode != null) {
            Assert.assertEquals(expectedOrder.get(i), nextNode.key());
            // Process the node
            dag.reportCompletion(nextNode);
            nextNode = dag.getNext();
            i++;
        }
    }

    private DAGraph<String, ItemHolder> createGraph(String resourceName) {
        ItemHolder node = new ItemHolder(resourceName, "data" + resourceName);
        DAGraph<String, ItemHolder> graph = new DAGraph<>(node);
        return graph;
    }
}
