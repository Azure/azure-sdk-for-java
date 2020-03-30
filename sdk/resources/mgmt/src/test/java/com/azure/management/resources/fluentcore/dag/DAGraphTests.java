/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        dag.prepareForEnumeration();
        ItemHolder nextNode = dag.getNext();
        int i = 0;
        while (nextNode != null) {
            Assertions.assertEquals(nextNode.key(), expectedOrder.get(i));
            dag.reportCompletion(nextNode);
            nextNode = dag.getNext();
            i++;
        }

        System.out.println("done");
    }

    @Test
    public void testGraphDependency() {
        /**
         *   |-------->[D]------>[B]---------->[A]
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
        graphB.addDependencyGraph(graphA);

        DAGraph<String, ItemHolder> graphC = createGraph("C");
        graphC.addDependencyGraph(graphA);

        DAGraph<String, ItemHolder> graphH = createGraph("H");
        graphH.addDependencyGraph(graphI);

        DAGraph<String, ItemHolder> graphG = createGraph("G");
        graphG.addDependencyGraph(graphC);

        DAGraph<String, ItemHolder> graphE = createGraph("E");
        graphE.addDependencyGraph(graphB);
        graphE.addDependencyGraph(graphG);

        DAGraph<String, ItemHolder> graphD = createGraph("D");
        graphD.addDependencyGraph(graphB);

        DAGraph<String, ItemHolder> graphF = createGraph("F");
        graphF.addDependencyGraph(graphD);
        graphF.addDependencyGraph(graphE);
        graphF.addDependencyGraph(graphH);

        DAGraph<String, ItemHolder> dag = graphF;
        dag.prepareForEnumeration();

        ItemHolder nextNode = dag.getNext();
        int i = 0;
        while (nextNode != null) {
            Assertions.assertEquals(expectedOrder.get(i), nextNode.key());
            // Process the node
            dag.reportCompletion(nextNode);
            nextNode = dag.getNext();
            i++;
        }
    }

    @Test
    public void testGraphDeadLockDetection() {
        boolean dlDetected;

        // ----------------------------------------------------
        /**
         * [A] <-----------> [A]
         */
        dlDetected = false;
        DAGraph<String, ItemHolder> graphX = createGraph("X");
        try {
            graphX.addDependencyGraph(graphX);
        } catch (IllegalStateException exception) {
            dlDetected = exception.getMessage().contains("X -> ") && exception.getMessage().contains(" -> X");
        }
        Assertions.assertTrue(dlDetected, "Expected exception is not thrown");

        // ----------------------------------------------------
        /**
         * [A] -----------> [B]
         *  ^                 ^
         *  |                 |
         *  |                 |
         *                    |
         *  [C]<--------------
         */
        dlDetected = false;
        DAGraph<String, ItemHolder> graphA = createGraph("A");
        DAGraph<String, ItemHolder> graphB = createGraph("B");
        DAGraph<String, ItemHolder> graphC = createGraph("C");

        graphA.addDependencyGraph(graphB);
        graphC.addDependencyGraph(graphA);
        try {
            graphB.addDependencyGraph(graphC);
        } catch (IllegalStateException exception) {
            dlDetected = exception.getMessage().contains("B -> ") && exception.getMessage().contains(" -> B");
        }
        Assertions.assertTrue(dlDetected, "Expected exception is not thrown");

        // ----------------------------------------------------
        /**
         * [2] ------------> [1]
         *                  ^  |
         *  ----------------|  |
         *  |                  |
         *  [3]<----------------
         */
        dlDetected = false;
        DAGraph<String, ItemHolder> graph1 = createGraph("1");
        DAGraph<String, ItemHolder> graph2 = createGraph("2");
        DAGraph<String, ItemHolder> graph3 = createGraph("3");

        graph2.addDependencyGraph(graph1);
        graph1.addDependencyGraph(graph3);
        try {
            graph3.addDependencyGraph(graph1);
        } catch (IllegalStateException exception) {
            dlDetected = exception.getMessage().contains("3 -> ") && exception.getMessage().contains(" -> 3");
        }
        Assertions.assertTrue(dlDetected, "Expected exception is not thrown");
    }

    @Test
    public void testGraphNodeTableBubblingUp() {
        // ----------------------------------------------------
        // Graph-1

        /**
         * [B] -----------> [A]
         *  ^                 ^
         *  |                 |
         *  |                 |
         *                    |
         *  [C]----------------
         */
        DAGraph<String, ItemHolder> graphA = createGraph("A");
        DAGraph<String, ItemHolder> graphB = createGraph("B");
        DAGraph<String, ItemHolder> graphC = createGraph("C");

        graphB.addDependencyGraph(graphA);
        graphC.addDependencyGraph(graphA);
        graphC.addDependencyGraph(graphB);

        DAGraph<String, ItemHolder> graph1Root = graphC;

        // ----------------------------------------------------
        // Graph-2

        /**
         * [E] ---> [D] ---> G
         *  ^
         *  |
         *  |
         *  [F]
         */
        DAGraph<String, ItemHolder> graphD = createGraph("D");
        DAGraph<String, ItemHolder> graphE = createGraph("E");
        DAGraph<String, ItemHolder> graphF = createGraph("F");
        DAGraph<String, ItemHolder> graphG = createGraph("G");

        graphE.addDependencyGraph(graphD);
        graphD.addDependencyGraph(graphG);
        graphF.addDependencyGraph(graphE);

        DAGraph<String, ItemHolder> graph2Root = graphF;

        // ----------------------------------------------------
        // Graph-3
        /**
         * [J] ---> [H] ---> I
         */

        DAGraph<String, ItemHolder> graphJ = createGraph("J");
        DAGraph<String, ItemHolder> graphH = createGraph("H");
        DAGraph<String, ItemHolder> graphI = createGraph("I");

        graphJ.addDependencyGraph(graphH);
        graphH.addDependencyGraph(graphI);

        DAGraph<String, ItemHolder> graph3Root = graphJ;

        // ----------------------------------------------------
        // Graph-4

        // Combine 3 graphs using their roots
        // graph1Root == graphC
        graph1Root.addDependentGraph(graph3Root); // graph3Root == graphJ
        graph1Root.addDependentGraph(graph2Root); // graph2Root == graphF

        DAGraph<String, ItemHolder> graph4Root1 = graph2Root;   // graphF
        DAGraph<String, ItemHolder> graph4Root2 = graph3Root;   // graphJ

        /**
         * [B] -----------> [A]
         *  ^                 ^
         *  |                 |
         *  |                 |
         *                    |
         *  [C]----------------
         *  ^ ^              (graph4Root2)
         *  | |
         *  |  ---------------[J] ---> [H] ---> I
         *  |
         *  |                 [E] ---> [D] ---> G
         *  |                  ^
         *  |                  |
         *  |                  |
         *  |-----------------[F]  (graph4Root1)
         */


        //======================================================
        // Validate nodeTables (graph1Root)

        ItemHolder nodeA_G1 = graph1Root.getNode("A");
        Assertions.assertEquals(1, nodeA_G1.owner().nodeTable.size());
        assertExactMatch(nodeA_G1.owner().nodeTable.keySet(), new String[] {"A"});

        ItemHolder nodeB_G1 = graph1Root.getNode("B");
        Assertions.assertEquals(2, nodeB_G1.owner().nodeTable.size());
        assertExactMatch(nodeB_G1.owner().nodeTable.keySet(), new String[] {"A", "B"});

        ItemHolder nodeC_G1 = graph1Root.getNode("C");
        Assertions.assertEquals(3, nodeC_G1.owner().nodeTable.size());
        assertExactMatch(nodeC_G1.owner().nodeTable.keySet(), new String[] {"A", "B", "C"});

        //======================================================
        // Validate nodeTables (graph4Root1)

        ItemHolder nodeA_G41 = graph4Root1.getNode("A");
        Assertions.assertEquals(1, nodeA_G41.owner().nodeTable.size());
        assertExactMatch(nodeA_G41.owner().nodeTable.keySet(), new String[] {"A"});

        ItemHolder nodeB_G41 = graph4Root1.getNode("B");
        Assertions.assertEquals(2, nodeB_G41.owner().nodeTable.size());
        assertExactMatch(nodeB_G41.owner().nodeTable.keySet(), new String[] {"A", "B"});

        ItemHolder nodeC_G41 = graph4Root1.getNode("C");
        Assertions.assertEquals(3, nodeC_G41.owner().nodeTable.size());
        assertExactMatch(nodeC_G41.owner().nodeTable.keySet(), new String[] {"A", "B", "C"});

        ItemHolder nodeG_G41 = graph4Root1.getNode("G");
        Assertions.assertEquals(1, nodeG_G41.owner().nodeTable.size());
        assertExactMatch(nodeG_G41.owner().nodeTable.keySet(), new String[] {"G"});

        ItemHolder nodeD_G41 = graph4Root1.getNode("D");
        Assertions.assertEquals(2, nodeD_G41.owner().nodeTable.size());
        assertExactMatch(nodeD_G41.owner().nodeTable.keySet(), new String[] {"D", "G"});

        ItemHolder nodeE_G41 = graph4Root1.getNode("E");
        Assertions.assertEquals(3, nodeE_G41.owner().nodeTable.size());
        assertExactMatch(nodeE_G41.owner().nodeTable.keySet(), new String[] {"E", "D", "G"});

        ItemHolder nodeF_G41 = graph4Root1.getNode("F");
        Assertions.assertEquals(7, nodeF_G41.owner().nodeTable.size());
        assertExactMatch(nodeF_G41.owner().nodeTable.keySet(), new String[] {"E", "F", "D", "G", "A", "B", "C"});

        //======================================================
        // Validate nodeTables (graph4Root2)

        ItemHolder nodeA_G42 = graph4Root2.getNode("A");
        Assertions.assertEquals(1, nodeA_G42.owner().nodeTable.size());
        assertExactMatch(nodeA_G42.owner().nodeTable.keySet(), new String[] {"A"});

        ItemHolder nodeB_G42 = graph4Root2.getNode("B");
        Assertions.assertEquals(2, nodeB_G42.owner().nodeTable.size());
        assertExactMatch(nodeB_G42.owner().nodeTable.keySet(), new String[] {"A", "B"});

        ItemHolder nodeC_G42 = graph4Root2.getNode("C");
        Assertions.assertEquals(3, nodeC_G42.owner().nodeTable.size());
        assertExactMatch(nodeC_G42.owner().nodeTable.keySet(), new String[] {"A", "B", "C"});

        ItemHolder nodeI_G42 = graph4Root2.getNode("I");
        Assertions.assertEquals(1, nodeI_G42.owner().nodeTable.size());
        assertExactMatch(nodeI_G42.owner().nodeTable.keySet(), new String[] {"I"});

        ItemHolder nodeH_G42 = graph4Root2.getNode("H");
        Assertions.assertEquals(2, nodeH_G42.owner().nodeTable.size());
        assertExactMatch(nodeH_G42.owner().nodeTable.keySet(), new String[] {"I", "H"});

        ItemHolder nodeJ_G42 = graph4Root2.getNode("J");
        Assertions.assertEquals(6, nodeJ_G42.owner().nodeTable.size());
        assertExactMatch(nodeJ_G42.owner().nodeTable.keySet(), new String[] {"I", "H", "J", "A", "B", "C"});

        // System.out.println(combinedGraphRoot.nodeTable.keySet());

        // ----------------------------------------------------
        // Graph-1

        /**
         * [L] -----------> [K]
         *  ^                 ^
         *  |                 |
         *  |                 |
         *                    |
         *  [M]----------------
         */
        DAGraph<String, ItemHolder> graphK = createGraph("K");
        DAGraph<String, ItemHolder> graphL = createGraph("L");
        DAGraph<String, ItemHolder> graphM = createGraph("M");


        graphL.addDependencyGraph(graphK);
        graphM.addDependencyGraph(graphL);
        graphM.addDependencyGraph(graphK);


        // Add a non-root node in this graph as dependency of a non-root node in the first graph.
        //
        graphA.addDependencyGraph(graphL);

        /**
         *                   |---------> [L] -----------> [K]
         *                   |           ^                 ^
         *                   |           |                 |
         *                   |           |                 |
         *                   |                             |
         *                   |           [M]----------------
         *                   |
         * [B] -----------> [A]
         *  ^                 ^
         *  |                 |
         *  |                 |
         *                    |
         *  [C]----------------
         *  ^ ^
         *  | |
         *  |  ---------------[J] ---> [H] ---> I
         *  |
         *  |                 [E] ---> [D] ---> G
         *  |                  ^
         *  |                  |
         *  |                  |
         *  |-----------------[F]   (graph4Root1)
         */

        //======================================================
        // Validate nodeTables (graph4Root1)

        ItemHolder nodeK_G41 = graph4Root1.getNode("K");
        Assertions.assertEquals(1, nodeK_G41.owner().nodeTable.size());
        assertExactMatch(nodeK_G41.owner().nodeTable.keySet(), new String[] {"K"});

        ItemHolder nodeL_G41 = graph4Root1.getNode("L");
        Assertions.assertEquals(2, nodeL_G41.owner().nodeTable.size());
        assertExactMatch(nodeL_G41.owner().nodeTable.keySet(), new String[] {"K", "L"});

        ItemHolder nodeA_G41_updated = graph4Root1.getNode("A");
        Assertions.assertEquals(3, nodeA_G41_updated.owner().nodeTable.size());
        assertExactMatch(nodeA_G41_updated.owner().nodeTable.keySet(), new String[] {"K", "L", "A"});

        ItemHolder nodeB_G41_updated = graph4Root1.getNode("B");
        Assertions.assertEquals(4, nodeB_G41_updated.owner().nodeTable.size());
        assertExactMatch(nodeB_G41_updated.owner().nodeTable.keySet(), new String[] {"K", "L", "A", "B"});

        ItemHolder nodeC_G41_updated = graph4Root1.getNode("C");
        Assertions.assertEquals(5, nodeC_G41_updated.owner().nodeTable.size());
        assertExactMatch(nodeC_G41_updated.owner().nodeTable.keySet(), new String[] {"K", "L", "A", "B", "C"});

        ItemHolder nodeF_G41_updated = graph4Root1.getNode("F");
        Assertions.assertEquals(9, nodeF_G41_updated.owner().nodeTable.size());
        assertExactMatch(nodeF_G41_updated.owner().nodeTable.keySet(), new String[] {"K", "L", "A", "B", "C", "F", "E", "D", "G"});

        ItemHolder nodeG_G41_noUpdate = graph4Root1.getNode("G");
        Assertions.assertEquals(1, nodeG_G41_noUpdate.owner().nodeTable.size());
        assertExactMatch(nodeG_G41_noUpdate.owner().nodeTable.keySet(), new String[] {"G"});

        ItemHolder nodeD_G41_noUpdate = graph4Root1.getNode("D");
        Assertions.assertEquals(2, nodeD_G41_noUpdate.owner().nodeTable.size());
        assertExactMatch(nodeD_G41_noUpdate.owner().nodeTable.keySet(), new String[] {"D", "G"});

        ItemHolder nodeE_G41_noUpdate = graph4Root1.getNode("E");
        Assertions.assertEquals(3, nodeE_G41_noUpdate.owner().nodeTable.size());
        assertExactMatch(nodeE_G41_noUpdate.owner().nodeTable.keySet(), new String[] {"E", "D", "G"});
    }

    private DAGraph<String, ItemHolder> createGraph(String resourceName) {
        ItemHolder node = new ItemHolder(resourceName, "data" + resourceName);
        DAGraph<String, ItemHolder> graph = new DAGraph<>(node);
        return graph;
    }

    private void assertExactMatch(Set<String> set, String [] values) {
        HashSet<String> s = new HashSet<>();
        s.addAll(set);

        s.removeAll(Arrays.asList(values));
        if (s.size() != 0) {
            Assertions.assertTrue(false, "Content of set " + set + " does not match with provided array " + Arrays.asList(values));
        }
    }
}
