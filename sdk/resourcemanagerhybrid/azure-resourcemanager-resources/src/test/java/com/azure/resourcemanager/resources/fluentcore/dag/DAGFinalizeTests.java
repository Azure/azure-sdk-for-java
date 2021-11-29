// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The tests for prepare stage of the graph (i.e. adding sub-graph in prepare stage).
 */
public class DAGFinalizeTests {
    @Test
    public void testWithoutFinalize() {
        /**
         *   |------------------>[D](2)----------->[B](1)-------------->[A](0)
         *   |                                        ^                   ^
         *   |                                        |                   |
         *  [F](4)---->[E](3)-------------------------|                   |
         *   |          |                                                 |
         *   |          |------->[G](2)----------->[C](1)------------------
         *   |
         *   |------------------------------------>[H](1)-------------->[I](0)
         */

        // Define pizzas with instant pizzas
        //
        // Level 0 pizzas
        PizzaImpl pizzaA = new PizzaImpl("A");
        PizzaImpl pizzaI = new PizzaImpl("I");
        // Level 1 pizzas
        PizzaImpl pizzaB = new PizzaImpl("B");
        pizzaB.withInstantPizza(pizzaA);
        PizzaImpl pizzaC = new PizzaImpl("C");
        pizzaC.withInstantPizza(pizzaA);
        PizzaImpl pizzaH = new PizzaImpl("H");
        pizzaH.withInstantPizza(pizzaI);
        // Level 2 pizzas
        PizzaImpl pizzaD = new PizzaImpl("D");
        pizzaD.withInstantPizza(pizzaB);
        PizzaImpl pizzaG = new PizzaImpl("G");
        pizzaG.withInstantPizza(pizzaC);
        // Level 3 pizzas
        PizzaImpl pizzaE = new PizzaImpl("E");
        pizzaE.withInstantPizza(pizzaB);
        pizzaE.withInstantPizza(pizzaG);
        // Level 4 pizzas
        PizzaImpl pizzaF = new PizzaImpl("F");
        pizzaF.withInstantPizza(pizzaD);
        pizzaF.withInstantPizza(pizzaE);
        pizzaF.withInstantPizza(pizzaH);

        // Run create to set up the underlying graph nodes with dependent details
        IPizza rootPizza = pizzaF.create();
        Assertions.assertNotNull(rootPizza);

        // Check dependencies and dependents
        //
        // ----------------------------------------------------------------------------------
        // LEVEL - 0
        // ----------------------------------------------------------------------------------
        //
        // Level 0 - "A"
        Assertions.assertEquals(pizzaA.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeA = pizzaA.taskGroup().getNode(pizzaA.key());
        Assertions.assertNotNull(nodeA);
        Assertions.assertEquals(nodeA.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeA.dependentKeys().size(), 2);
        for (String dependentKey : nodeA.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaC.key()));
        }
        // Level 0 - "I"
        Assertions.assertEquals(pizzaI.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeI = pizzaI.taskGroup().getNode(pizzaI.key());
        Assertions.assertNotNull(nodeI);
        Assertions.assertEquals(nodeI.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeI.dependentKeys().size(), 1);
        for (String dependentKey : nodeI.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaH.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 1
        // ----------------------------------------------------------------------------------
        //
        // Level 1 - "B"
        Assertions.assertEquals(pizzaB.taskGroup().getNodes().size(), 2);
        Assertions.assertNotNull(pizzaB.taskGroup().getNode(pizzaA.key()));
        TaskGroupEntry<TaskItem> nodeB = pizzaB.taskGroup().getNode(pizzaB.key());
        Assertions.assertNotNull(nodeB);
        Assertions.assertEquals(nodeB.dependencyKeys().size(), 1);
        for (String dependentKey : nodeB.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        Assertions.assertEquals(nodeB.dependentKeys().size(), 2);
        for (String dependentKey : nodeB.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key()));
        }
        // Level 1 - "C"
        Assertions.assertEquals(pizzaC.taskGroup().getNodes().size(), 2);
        Assertions.assertNotNull(pizzaC.taskGroup().getNode(pizzaA.key()));
        TaskGroupEntry<TaskItem> nodeC = pizzaC.taskGroup().getNode(pizzaC.key());
        Assertions.assertNotNull(nodeC);
        Assertions.assertEquals(nodeC.dependencyKeys().size(), 1);
        for (String dependentKey : nodeC.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        Assertions.assertEquals(nodeC.dependentKeys().size(), 1);
        for (String dependentKey : nodeC.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        // Level 1 - "H"
        Assertions.assertEquals(pizzaH.taskGroup().getNodes().size(), 2);
        Assertions.assertNotNull(pizzaH.taskGroup().getNode(pizzaI.key()));
        TaskGroupEntry<TaskItem> nodeH = pizzaH.taskGroup().getNode(pizzaH.key());
        Assertions.assertNotNull(nodeH);
        Assertions.assertEquals(nodeH.dependencyKeys().size(), 1);
        for (String dependentKey : nodeH.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaI.key()));
        }
        Assertions.assertEquals(nodeH.dependentKeys().size(), 1);
        for (String dependentKey : nodeH.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 2
        // ----------------------------------------------------------------------------------
        //
        // Level 2 - "D"
        Assertions.assertEquals(pizzaD.taskGroup().getNodes().size(), 3);
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaB.key()));
        TaskGroupEntry<TaskItem> nodeD = pizzaD.taskGroup().getNode(pizzaD.key());
        Assertions.assertNotNull(nodeD);
        Assertions.assertEquals(nodeD.dependencyKeys().size(), 1);
        for (String dependentKey : nodeD.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key()));
        }
        Assertions.assertEquals(nodeD.dependentKeys().size(), 1);
        for (String dependentKey : nodeD.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }
        // Level 2 - "G"
        Assertions.assertEquals(pizzaG.taskGroup().getNodes().size(), 3);
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaC.key()));
        TaskGroupEntry<TaskItem> nodeG = pizzaG.taskGroup().getNode(pizzaG.key());
        Assertions.assertNotNull(nodeG);
        Assertions.assertEquals(nodeG.dependencyKeys().size(), 1);
        for (String dependentKey : nodeG.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaC.key()));
        }
        Assertions.assertEquals(nodeG.dependentKeys().size(), 1);
        for (String dependentKey : nodeG.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaE.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 3
        // ----------------------------------------------------------------------------------
        //
        // Level 3 - "E"
        Assertions.assertEquals(pizzaE.taskGroup().getNodes().size(), 5);
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaB.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaC.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaG.key()));
        TaskGroupEntry<TaskItem> nodeE = pizzaE.taskGroup().getNode(pizzaE.key());
        Assertions.assertNotNull(nodeE);
        Assertions.assertEquals(nodeE.dependencyKeys().size(), 2);
        for (String dependentKey : nodeE.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        Assertions.assertEquals(nodeE.dependentKeys().size(), 1);
        for (String dependentKey : nodeE.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 4
        // ----------------------------------------------------------------------------------
        //
        // Level 4 - "F"
        Assertions.assertEquals(pizzaF.taskGroup().getNodes().size(), 9);
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaB.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaC.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaG.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaI.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaH.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaE.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaD.key()));
        TaskGroupEntry<TaskItem> nodeF = pizzaF.taskGroup().getNode(pizzaF.key());
        Assertions.assertNotNull(nodeF);
        Assertions.assertEquals(nodeF.dependencyKeys().size(), 3);
        for (String dependentKey : nodeF.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key())
                    || dependentKey.equalsIgnoreCase(pizzaH.key()));
        }
        Assertions.assertEquals(nodeF.dependentKeys().size(), 0);
    }

    @Test
    public void testFinalize() {
        // Initial dependency graph
        //
        /**
         *   |------------------>[D](2)----------->[B](1)------------->[A](0)
         *   |                                        ^                   ^
         *   |                                        |                   |
         *  [F](4)---->[E](3)-------------------------|                   |
         *   |          |                                                 |
         *   |          |------->[G](2)----------->[C](1)------------------
         *   |
         *   |------------------------------------>[H](1)-------------->[I](0)
         */

        // Level 0 pizzas
        PizzaImpl pizzaA = new PizzaImpl("A");
        PizzaImpl pizzaI = new PizzaImpl("I");
        // Level 1 pizzas
        PizzaImpl pizzaB = new PizzaImpl("B");
        pizzaB.withInstantPizza(pizzaA);
        PizzaImpl pizzaC = new PizzaImpl("C");
        pizzaC.withInstantPizza(pizzaA);
        PizzaImpl pizzaH = new PizzaImpl("H");
        pizzaH.withInstantPizza(pizzaI);
        // Level 2 pizzas
        PizzaImpl pizzaD = new PizzaImpl("D");
        pizzaD.withInstantPizza(pizzaB);
        PizzaImpl pizzaG = new PizzaImpl("G");
        pizzaG.withInstantPizza(pizzaC);
        // Level 3 pizzas
        PizzaImpl pizzaE = new PizzaImpl("E");
        pizzaE.withInstantPizza(pizzaB);
        pizzaE.withInstantPizza(pizzaG);
        // Level 4 pizzas
        PizzaImpl pizzaF = new PizzaImpl("F");
        pizzaF.withInstantPizza(pizzaD);
        pizzaF.withInstantPizza(pizzaE);
        pizzaF.withInstantPizza(pizzaH);

        // Update the above setup by adding delayed pizzas in finalize (prepare).
        // Define 3 (J, K, L) delayed pizzas (edges with '==' symbol), two of them (J, L)
        // with instant pizzas.
        //    - The delayed pizza J has an instance pizza N
        //    - The delayed pizza L has an instance pizza P
        //        - The instance pizza P has a delayed pizza Q
        /**
         *                                                       |------------>[M](0)
         *                                                       |
         *                                             |=========>[J](1)------>[N](0)
         *                                             |
         *   |------------------>[D](4)-->[B](3)----->[A](2)==================>[K](0)
         *   |                             ^           ^
         *   |                             |           |
         *  [F](6)---->[E](5)--------------|           |
         *   |          |                              |
         *   |          |------->[G](4)-->[C](3)--------
         *   |                    |
         *   |                    |==================>[L](2)----->[P](1)======>[Q](0)
         *   |
         *   |--------------------------------------------------->[H](1)------>[I](0)
         */

        PizzaImpl pizzaJ = new PizzaImpl("J");
        PizzaImpl pizzaM = new PizzaImpl("M");
        PizzaImpl pizzaN = new PizzaImpl("N");
        pizzaJ.withInstantPizza(pizzaM);
        pizzaJ.withInstantPizza(pizzaN);
        PizzaImpl pizzaK = new PizzaImpl("K");
        pizzaA.withDelayedPizza(pizzaJ);
        pizzaA.withDelayedPizza(pizzaK);
        PizzaImpl pizzaL = new PizzaImpl("L");
        PizzaImpl pizzaP = new PizzaImpl("P");
        PizzaImpl pizzaQ = new PizzaImpl("Q");
        pizzaP.withDelayedPizza(pizzaQ);
        pizzaL.withInstantPizza(pizzaP);
        pizzaG.withDelayedPizza(pizzaL);

        // Run create to set up the underlying graph nodes with dependent details
        IPizza rootPizza = pizzaF.create();
        Assertions.assertNotNull(rootPizza);
        // Check dependencies and dependents
        //
        // ----------------------------------------------------------------------------------
        // LEVEL - 0
        // ----------------------------------------------------------------------------------
        //
        // Level 0 - "M"
        Assertions.assertEquals(pizzaM.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeM = pizzaM.taskGroup().getNode(pizzaM.key());
        Assertions.assertNotNull(nodeM);
        Assertions.assertEquals(nodeM.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeM.dependentKeys().size(), 1);
        for (String dependentKey : nodeM.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaJ.key()));
        }
        // Level 0 - "N"
        Assertions.assertEquals(pizzaN.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeN = pizzaN.taskGroup().getNode(pizzaN.key());
        Assertions.assertNotNull(nodeN);
        Assertions.assertEquals(nodeN.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeN.dependentKeys().size(), 1);
        for (String dependentKey : nodeN.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaJ.key()));
        }
        // Level 0 - "K"
        Assertions.assertEquals(pizzaK.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeK = pizzaK.taskGroup().getNode(pizzaK.key());
        Assertions.assertNotNull(nodeK);
        Assertions.assertEquals(nodeK.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeK.dependentKeys().size(), 1);
        for (String dependentKey : nodeK.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        // Level 0 - "I"
        Assertions.assertEquals(pizzaI.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeI = pizzaI.taskGroup().getNode(pizzaI.key());
        Assertions.assertNotNull(nodeI);
        Assertions.assertEquals(nodeI.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeI.dependentKeys().size(), 1);
        for (String dependentKey : nodeI.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaH.key()));
        }
        // Level 0 - "Q"
        Assertions.assertEquals(pizzaQ.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<TaskItem> nodeQ = pizzaQ.taskGroup().getNode(pizzaQ.key());
        Assertions.assertNotNull(nodeQ);
        Assertions.assertEquals(nodeQ.dependencyKeys().size(), 0);
        Assertions.assertEquals(nodeQ.dependentKeys().size(), 1);
        for (String dependentKey : nodeQ.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaP.key()));
        }
        //
        // ----------------------------------------------------------------------------------
        // LEVEL - 1
        // ----------------------------------------------------------------------------------
        //
        // Level 1 - "H"
        Assertions.assertEquals(pizzaH.taskGroup().getNodes().size(), 2);
        Assertions.assertNotNull(pizzaH.taskGroup().getNode(pizzaI.key()));
        TaskGroupEntry<TaskItem> nodeH = pizzaH.taskGroup().getNode(pizzaH.key());
        Assertions.assertNotNull(nodeH);
        Assertions.assertEquals(nodeH.dependencyKeys().size(), 1);
        for (String dependentKey : nodeH.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaI.key()));
        }
        Assertions.assertEquals(nodeH.dependentKeys().size(), 1);
        for (String dependentKey : nodeH.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }
        // Level 1 - "J"
        Assertions.assertEquals(pizzaJ.taskGroup().getNodes().size(), 3);
        Assertions.assertNotNull(pizzaJ.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaJ.taskGroup().getNode(pizzaN.key()));
        TaskGroupEntry<TaskItem> nodeJ = pizzaJ.taskGroup().getNode(pizzaJ.key());
        Assertions.assertNotNull(nodeJ);
        Assertions.assertEquals(nodeJ.dependencyKeys().size(), 2);
        for (String dependentKey : nodeJ.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaM.key())
                    || dependentKey.equalsIgnoreCase(pizzaN.key()));
        }
        Assertions.assertEquals(nodeJ.dependentKeys().size(), 1);
        for (String dependentKey : nodeJ.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        // Level 1 - "P"
        Assertions.assertEquals(pizzaP.taskGroup().getNodes().size(), 2);
        Assertions.assertNotNull(pizzaP.taskGroup().getNode(pizzaQ.key()));
        TaskGroupEntry<TaskItem> nodeP = pizzaP.taskGroup().getNode(pizzaP.key());
        Assertions.assertNotNull(nodeP);
        Assertions.assertEquals(nodeP.dependencyKeys().size(), 1);
        for (String dependentKey : nodeP.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaQ.key()));
        }
        Assertions.assertEquals(nodeP.dependentKeys().size(), 1);
        for (String dependentKey : nodeP.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaL.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 2
        // ----------------------------------------------------------------------------------
        //
        // Level 1 - "L"
        Assertions.assertEquals(pizzaL.taskGroup().getNodes().size(), 3);
        Assertions.assertNotNull(pizzaL.taskGroup().getNode(pizzaQ.key()));
        Assertions.assertNotNull(pizzaL.taskGroup().getNode(pizzaP.key()));
        TaskGroupEntry<TaskItem> nodeL = pizzaL.taskGroup().getNode(pizzaL.key());
        Assertions.assertNotNull(nodeL);
        Assertions.assertEquals(nodeL.dependencyKeys().size(), 1);
        for (String dependentKey : nodeL.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaP.key()));
        }
        Assertions.assertEquals(nodeL.dependentKeys().size(), 1);
        for (String dependentKey : nodeL.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        // Level 2 - "A"
        Assertions.assertEquals(pizzaA.taskGroup().getNodes().size(), 5);
        Assertions.assertNotNull(pizzaA.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaA.taskGroup().getNode(pizzaN.key()));
        Assertions.assertNotNull(pizzaA.taskGroup().getNode(pizzaJ.key()));
        Assertions.assertNotNull(pizzaA.taskGroup().getNode(pizzaK.key()));
        TaskGroupEntry<TaskItem> nodeA = pizzaA.taskGroup().getNode(pizzaA.key());
        Assertions.assertNotNull(nodeA);
        Assertions.assertEquals(nodeA.dependencyKeys().size(), 2);
        for (String dependentKey : nodeA.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaJ.key())
                    || dependentKey.equalsIgnoreCase(pizzaK.key()));
        }
        Assertions.assertEquals(nodeA.dependentKeys().size(), 2);
        for (String dependentKey : nodeA.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaC.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 3
        // ----------------------------------------------------------------------------------
        //
        // Level 3 - "B"
        Assertions.assertEquals(pizzaB.taskGroup().getNodes().size(), 6);
        Assertions.assertNotNull(pizzaB.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaB.taskGroup().getNode(pizzaN.key()));
        Assertions.assertNotNull(pizzaB.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaB.taskGroup().getNode(pizzaK.key()));
        Assertions.assertNotNull(pizzaB.taskGroup().getNode(pizzaJ.key()));
        TaskGroupEntry<TaskItem> nodeB = pizzaB.taskGroup().getNode(pizzaB.key());
        Assertions.assertNotNull(nodeB);
        Assertions.assertEquals(nodeB.dependencyKeys().size(), 1);
        for (String dependentKey : nodeB.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        Assertions.assertEquals(nodeB.dependentKeys().size(), 2);
        for (String dependentKey : nodeB.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 4
        // ----------------------------------------------------------------------------------
        //
        // Level 4 - "D"
        Assertions.assertEquals(pizzaD.taskGroup().getNodes().size(), 7);
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaB.key()));
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaJ.key()));
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaN.key()));
        Assertions.assertNotNull(pizzaD.taskGroup().getNode(pizzaK.key()));
        TaskGroupEntry<TaskItem> nodeD = pizzaD.taskGroup().getNode(pizzaD.key());
        Assertions.assertNotNull(nodeD);
        Assertions.assertEquals(nodeD.dependencyKeys().size(), 1);
        for (String dependentKey : nodeD.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key()));
        }
        Assertions.assertEquals(nodeD.dependentKeys().size(), 1);
        for (String dependentKey : nodeD.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }

        // Level 4 - "G"
        Assertions.assertEquals(pizzaG.taskGroup().getNodes().size(), 10);
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaC.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaQ.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaL.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaP.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaJ.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaN.key()));
        Assertions.assertNotNull(pizzaG.taskGroup().getNode(pizzaK.key()));
        TaskGroupEntry<TaskItem> nodeG = pizzaG.taskGroup().getNode(pizzaG.key());
        Assertions.assertNotNull(nodeG);
        Assertions.assertEquals(nodeG.dependencyKeys().size(), 2);
        for (String dependentKey : nodeG.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaC.key())
                    || dependentKey.equalsIgnoreCase(pizzaL.key()));
        }
        Assertions.assertEquals(nodeG.dependentKeys().size(), 1);
        for (String dependentKey : nodeG.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaE.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 5
        // ----------------------------------------------------------------------------------
        //
        // Level 5 - "E"
        Assertions.assertEquals(pizzaE.taskGroup().getNodes().size(), 12);
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaG.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaQ.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaB.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaC.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaJ.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaL.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaN.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaK.key()));
        Assertions.assertNotNull(pizzaE.taskGroup().getNode(pizzaP.key()));
        TaskGroupEntry<TaskItem> nodeE = pizzaE.taskGroup().getNode(pizzaE.key());
        Assertions.assertNotNull(nodeE);
        Assertions.assertEquals(nodeE.dependencyKeys().size(), 2);
        for (String dependentKey : nodeE.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        Assertions.assertEquals(nodeE.dependentKeys().size(), 1);
        for (String dependentKey : nodeE.dependentKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 6
        // ----------------------------------------------------------------------------------
        //
        // Level 6 - "F"
        Assertions.assertEquals(pizzaF.taskGroup().getNodes().size(), 16);
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaA.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaB.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaC.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaD.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaE.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaG.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaH.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaI.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaJ.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaK.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaL.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaM.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaN.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaP.key()));
        Assertions.assertNotNull(pizzaF.taskGroup().getNode(pizzaQ.key()));
        TaskGroupEntry<TaskItem> nodeF = pizzaF.taskGroup().getNode(pizzaF.key());
        Assertions.assertNotNull(nodeF);
        Assertions.assertEquals(nodeF.dependencyKeys().size(), 3);
        for (String dependentKey : nodeF.dependencyKeys()) {
            Assertions.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key())
                    || dependentKey.equalsIgnoreCase(pizzaH.key()));
        }
        Assertions.assertEquals(nodeF.dependentKeys().size(), 0);
    }
}
