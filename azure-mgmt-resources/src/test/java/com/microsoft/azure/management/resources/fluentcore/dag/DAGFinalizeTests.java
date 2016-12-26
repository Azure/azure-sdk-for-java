/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreateUpdateTask;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertNotNull(rootPizza);

        // Check dependencies and dependents
        //
        // ----------------------------------------------------------------------------------
        // LEVEL - 0
        // ----------------------------------------------------------------------------------
        //
        // Level 0 - "A"
        Assert.assertEquals(pizzaA.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeA = pizzaA.taskGroup().getNode(pizzaA.key());
        Assert.assertNotNull(nodeA);
        Assert.assertEquals(nodeA.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeA.dependentKeys().size(), 2);
        for (String dependentKey : nodeA.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaC.key()));
        }
        // Level 0 - "I"
        Assert.assertEquals(pizzaI.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeI = pizzaI.taskGroup().getNode(pizzaI.key());
        Assert.assertNotNull(nodeI);
        Assert.assertEquals(nodeI.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeI.dependentKeys().size(), 1);
        for (String dependentKey : nodeI.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaH.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 1
        // ----------------------------------------------------------------------------------
        //
        // Level 1 - "B"
        Assert.assertEquals(pizzaB.taskGroup().getNodes().size(), 2);
        Assert.assertNotNull(pizzaB.taskGroup().getNode(pizzaA.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeB = pizzaB.taskGroup().getNode(pizzaB.key());
        Assert.assertNotNull(nodeB);
        Assert.assertEquals(nodeB.dependencyKeys().size(), 1);
        for (String dependentKey : nodeB.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        Assert.assertEquals(nodeB.dependentKeys().size(), 2);
        for (String dependentKey : nodeB.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key()));
        }
        // Level 1 - "C"
        Assert.assertEquals(pizzaC.taskGroup().getNodes().size(), 2);
        Assert.assertNotNull(pizzaC.taskGroup().getNode(pizzaA.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeC = pizzaC.taskGroup().getNode(pizzaC.key());
        Assert.assertNotNull(nodeC);
        Assert.assertEquals(nodeC.dependencyKeys().size(), 1);
        for (String dependentKey : nodeC.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        Assert.assertEquals(nodeC.dependentKeys().size(), 1);
        for (String dependentKey : nodeC.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        // Level 1 - "H"
        Assert.assertEquals(pizzaH.taskGroup().getNodes().size(), 2);
        Assert.assertNotNull(pizzaH.taskGroup().getNode(pizzaI.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeH = pizzaH.taskGroup().getNode(pizzaH.key());
        Assert.assertNotNull(nodeH);
        Assert.assertEquals(nodeH.dependencyKeys().size(), 1);
        for (String dependentKey : nodeH.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaI.key()));
        }
        Assert.assertEquals(nodeH.dependentKeys().size(), 1);
        for (String dependentKey : nodeH.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 2
        // ----------------------------------------------------------------------------------
        //
        // Level 2 - "D"
        Assert.assertEquals(pizzaD.taskGroup().getNodes().size(), 3);
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaB.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeD = pizzaD.taskGroup().getNode(pizzaD.key());
        Assert.assertNotNull(nodeD);
        Assert.assertEquals(nodeD.dependencyKeys().size(), 1);
        for (String dependentKey : nodeD.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key()));
        }
        Assert.assertEquals(nodeD.dependentKeys().size(), 1);
        for (String dependentKey : nodeD.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }
        // Level 2 - "G"
        Assert.assertEquals(pizzaG.taskGroup().getNodes().size(), 3);
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaC.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeG = pizzaG.taskGroup().getNode(pizzaG.key());
        Assert.assertNotNull(nodeG);
        Assert.assertEquals(nodeG.dependencyKeys().size(), 1);
        for (String dependentKey : nodeG.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaC.key()));
        }
        Assert.assertEquals(nodeG.dependentKeys().size(), 1);
        for (String dependentKey : nodeG.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaE.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 3
        // ----------------------------------------------------------------------------------
        //
        // Level 3 - "E"
        Assert.assertEquals(pizzaE.taskGroup().getNodes().size(), 5);
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaB.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaC.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaG.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeE = pizzaE.taskGroup().getNode(pizzaE.key());
        Assert.assertNotNull(nodeE);
        Assert.assertEquals(nodeE.dependencyKeys().size(), 2);
        for (String dependentKey : nodeE.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        Assert.assertEquals(nodeE.dependentKeys().size(), 1);
        for (String dependentKey : nodeE.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }

        // ----------------------------------------------------------------------------------
        // LEVEL - 4
        // ----------------------------------------------------------------------------------
        //
        // Level 4 - "F"
        Assert.assertEquals(pizzaF.taskGroup().getNodes().size(), 9);
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaB.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaC.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaG.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaI.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaH.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaE.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaD.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeF = pizzaF.taskGroup().getNode(pizzaF.key());
        Assert.assertNotNull(nodeF);
        Assert.assertEquals(nodeF.dependencyKeys().size(), 3);
        for (String dependentKey : nodeF.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key())
                    || dependentKey.equalsIgnoreCase(pizzaH.key()));
        }
        Assert.assertEquals(nodeF.dependentKeys().size(), 0);
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
        Assert.assertNotNull(rootPizza);
        // Check dependencies and dependents
        //
        // ----------------------------------------------------------------------------------
        // LEVEL - 0
        // ----------------------------------------------------------------------------------
        //
        // Level 0 - "M"
        Assert.assertEquals(pizzaM.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeM = pizzaM.taskGroup().getNode(pizzaM.key());
        Assert.assertNotNull(nodeM);
        Assert.assertEquals(nodeM.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeM.dependentKeys().size(), 1);
        for (String dependentKey : nodeM.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaJ.key()));
        }
        // Level 0 - "N"
        Assert.assertEquals(pizzaN.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeN = pizzaN.taskGroup().getNode(pizzaN.key());
        Assert.assertNotNull(nodeN);
        Assert.assertEquals(nodeN.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeN.dependentKeys().size(), 1);
        for (String dependentKey : nodeN.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaJ.key()));
        }
        // Level 0 - "K"
        Assert.assertEquals(pizzaK.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeK = pizzaK.taskGroup().getNode(pizzaK.key());
        Assert.assertNotNull(nodeK);
        Assert.assertEquals(nodeK.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeK.dependentKeys().size(), 1);
        for (String dependentKey : nodeK.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        // Level 0 - "I"
        Assert.assertEquals(pizzaI.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeI = pizzaI.taskGroup().getNode(pizzaI.key());
        Assert.assertNotNull(nodeI);
        Assert.assertEquals(nodeI.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeI.dependentKeys().size(), 1);
        for (String dependentKey : nodeI.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaH.key()));
        }
        // Level 0 - "Q"
        Assert.assertEquals(pizzaQ.taskGroup().getNodes().size(), 1);
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeQ = pizzaQ.taskGroup().getNode(pizzaQ.key());
        Assert.assertNotNull(nodeQ);
        Assert.assertEquals(nodeQ.dependencyKeys().size(), 0);
        Assert.assertEquals(nodeQ.dependentKeys().size(), 1);
        for (String dependentKey : nodeQ.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaP.key()));
        }
        //
        // ----------------------------------------------------------------------------------
        // LEVEL - 1
        // ----------------------------------------------------------------------------------
        //
        // Level 1 - "H"
        Assert.assertEquals(pizzaH.taskGroup().getNodes().size(), 2);
        Assert.assertNotNull(pizzaH.taskGroup().getNode(pizzaI.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeH = pizzaH.taskGroup().getNode(pizzaH.key());
        Assert.assertNotNull(nodeH);
        Assert.assertEquals(nodeH.dependencyKeys().size(), 1);
        for (String dependentKey : nodeH.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaI.key()));
        }
        Assert.assertEquals(nodeH.dependentKeys().size(), 1);
        for (String dependentKey : nodeH.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }
        // Level 1 - "J"
        Assert.assertEquals(pizzaJ.taskGroup().getNodes().size(), 3);
        Assert.assertNotNull(pizzaJ.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaJ.taskGroup().getNode(pizzaN.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeJ = pizzaJ.taskGroup().getNode(pizzaJ.key());
        Assert.assertNotNull(nodeJ);
        Assert.assertEquals(nodeJ.dependencyKeys().size(), 2);
        for (String dependentKey : nodeJ.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaM.key())
                || dependentKey.equalsIgnoreCase(pizzaN.key()));
        }
        Assert.assertEquals(nodeJ.dependentKeys().size(), 1);
        for (String dependentKey : nodeJ.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        // Level 1 - "P"
        Assert.assertEquals(pizzaP.taskGroup().getNodes().size(), 2);
        Assert.assertNotNull(pizzaP.taskGroup().getNode(pizzaQ.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeP = pizzaP.taskGroup().getNode(pizzaP.key());
        Assert.assertNotNull(nodeP);
        Assert.assertEquals(nodeP.dependencyKeys().size(), 1);
        for (String dependentKey : nodeP.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaQ.key()));
        }
        Assert.assertEquals(nodeP.dependentKeys().size(), 1);
        for (String dependentKey : nodeP.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaL.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 2
        // ----------------------------------------------------------------------------------
        //
        // Level 1 - "L"
        Assert.assertEquals(pizzaL.taskGroup().getNodes().size(), 3);
        Assert.assertNotNull(pizzaL.taskGroup().getNode(pizzaQ.key()));
        Assert.assertNotNull(pizzaL.taskGroup().getNode(pizzaP.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeL = pizzaL.taskGroup().getNode(pizzaL.key());
        Assert.assertNotNull(nodeL);
        Assert.assertEquals(nodeL.dependencyKeys().size(), 1);
        for (String dependentKey : nodeL.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaP.key()));
        }
        Assert.assertEquals(nodeL.dependentKeys().size(), 1);
        for (String dependentKey : nodeL.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        // Level 2 - "A"
        Assert.assertEquals(pizzaA.taskGroup().getNodes().size(), 5);
        Assert.assertNotNull(pizzaA.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaA.taskGroup().getNode(pizzaN.key()));
        Assert.assertNotNull(pizzaA.taskGroup().getNode(pizzaJ.key()));
        Assert.assertNotNull(pizzaA.taskGroup().getNode(pizzaK.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeA = pizzaA.taskGroup().getNode(pizzaA.key());
        Assert.assertNotNull(nodeA);
        Assert.assertEquals(nodeA.dependencyKeys().size(), 2);
        for (String dependentKey : nodeA.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaJ.key())
                    || dependentKey.equalsIgnoreCase(pizzaK.key()));
        }
        Assert.assertEquals(nodeA.dependentKeys().size(), 2);
        for (String dependentKey : nodeA.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaC.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 3
        // ----------------------------------------------------------------------------------
        //
        // Level 3 - "B"
        Assert.assertEquals(pizzaB.taskGroup().getNodes().size(), 6);
        Assert.assertNotNull(pizzaB.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaB.taskGroup().getNode(pizzaN.key()));
        Assert.assertNotNull(pizzaB.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaB.taskGroup().getNode(pizzaK.key()));
        Assert.assertNotNull(pizzaB.taskGroup().getNode(pizzaJ.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeB = pizzaB.taskGroup().getNode(pizzaB.key());
        Assert.assertNotNull(nodeB);
        Assert.assertEquals(nodeB.dependencyKeys().size(), 1);
        for (String dependentKey : nodeB.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaA.key()));
        }
        Assert.assertEquals(nodeB.dependentKeys().size(), 2);
        for (String dependentKey : nodeB.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 4
        // ----------------------------------------------------------------------------------
        //
        // Level 4 - "D"
        Assert.assertEquals(pizzaD.taskGroup().getNodes().size(), 7);
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaB.key()));
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaJ.key()));
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaN.key()));
        Assert.assertNotNull(pizzaD.taskGroup().getNode(pizzaK.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeD = pizzaD.taskGroup().getNode(pizzaD.key());
        Assert.assertNotNull(nodeD);
        Assert.assertEquals(nodeD.dependencyKeys().size(), 1);
        for (String dependentKey : nodeD.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key()));
        }
        Assert.assertEquals(nodeD.dependentKeys().size(), 1);
        for (String dependentKey : nodeD.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }

        // Level 4 - "G"
        Assert.assertEquals(pizzaG.taskGroup().getNodes().size(), 10);
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaC.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaQ.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaL.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaP.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaJ.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaN.key()));
        Assert.assertNotNull(pizzaG.taskGroup().getNode(pizzaK.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeG = pizzaG.taskGroup().getNode(pizzaG.key());
        Assert.assertNotNull(nodeG);
        Assert.assertEquals(nodeG.dependencyKeys().size(), 2);
        for (String dependentKey : nodeG.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaC.key())
                || dependentKey.equalsIgnoreCase(pizzaL.key()));
        }
        Assert.assertEquals(nodeG.dependentKeys().size(), 1);
        for (String dependentKey : nodeG.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaE.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 5
        // ----------------------------------------------------------------------------------
        //
        // Level 5 - "E"
        Assert.assertEquals(pizzaE.taskGroup().getNodes().size(), 12);
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaG.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaQ.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaB.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaC.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaJ.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaL.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaN.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaK.key()));
        Assert.assertNotNull(pizzaE.taskGroup().getNode(pizzaP.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeE = pizzaE.taskGroup().getNode(pizzaE.key());
        Assert.assertNotNull(nodeE);
        Assert.assertEquals(nodeE.dependencyKeys().size(), 2);
        for (String dependentKey : nodeE.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaB.key())
                    || dependentKey.equalsIgnoreCase(pizzaG.key()));
        }
        Assert.assertEquals(nodeE.dependentKeys().size(), 1);
        for (String dependentKey : nodeE.dependentKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaF.key()));
        }
        // ----------------------------------------------------------------------------------
        // LEVEL - 6
        // ----------------------------------------------------------------------------------
        //
        // Level 6 - "F"
        Assert.assertEquals(pizzaF.taskGroup().getNodes().size(), 16);
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaA.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaB.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaC.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaD.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaE.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaG.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaH.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaI.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaJ.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaK.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaL.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaM.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaN.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaP.key()));
        Assert.assertNotNull(pizzaF.taskGroup().getNode(pizzaQ.key()));
        TaskGroupEntry<IPizza, CreateUpdateTask<IPizza>> nodeF = pizzaF.taskGroup().getNode(pizzaF.key());
        Assert.assertNotNull(nodeF);
        Assert.assertEquals(nodeF.dependencyKeys().size(), 3);
        for (String dependentKey : nodeF.dependencyKeys()) {
            Assert.assertTrue(dependentKey.equalsIgnoreCase(pizzaD.key())
                    || dependentKey.equalsIgnoreCase(pizzaE.key())
                    || dependentKey.equalsIgnoreCase(pizzaH.key()));
        }
        Assert.assertEquals(nodeF.dependentKeys().size(), 0);
    }
}
