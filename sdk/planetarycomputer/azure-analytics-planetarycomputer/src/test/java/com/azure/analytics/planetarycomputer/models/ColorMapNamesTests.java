// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/**
 * Unit tests for {@link ColorMapNames} expandable string enum.
 */
public final class ColorMapNamesTests {

    @Test
    public void testFromString() {
        ColorMapNames accent = ColorMapNames.fromString("accent");
        Assertions.assertEquals(ColorMapNames.ACCENT, accent);

        ColorMapNames viridis = ColorMapNames.fromString("viridis");
        Assertions.assertEquals(ColorMapNames.VIRIDIS, viridis);

        ColorMapNames plasma = ColorMapNames.fromString("plasma");
        Assertions.assertEquals(ColorMapNames.PLASMA, plasma);

        ColorMapNames inferno = ColorMapNames.fromString("inferno");
        Assertions.assertEquals(ColorMapNames.INFERNO, inferno);

        ColorMapNames magma = ColorMapNames.fromString("magma");
        Assertions.assertEquals(ColorMapNames.MAGMA, magma);
    }

    @Test
    public void testFromStringCustomValue() {
        ColorMapNames custom = ColorMapNames.fromString("my-custom-colormap");
        Assertions.assertNotNull(custom);
        Assertions.assertEquals("my-custom-colormap", custom.toString());
    }

    @Test
    public void testValues() {
        Collection<ColorMapNames> allValues = ColorMapNames.values();
        Assertions.assertNotNull(allValues);
        Assertions.assertFalse(allValues.isEmpty());
        // There should be many predefined colormaps
        Assertions.assertTrue(allValues.size() > 100);
        Assertions.assertTrue(allValues.contains(ColorMapNames.ACCENT));
        Assertions.assertTrue(allValues.contains(ColorMapNames.VIRIDIS));
        Assertions.assertTrue(allValues.contains(ColorMapNames.PLASMA));
        Assertions.assertTrue(allValues.contains(ColorMapNames.INFERNO));
    }

    @Test
    public void testKnownConstants() {
        // Test a representative sample of the defined constants
        Assertions.assertNotNull(ColorMapNames.ACCENT);
        Assertions.assertNotNull(ColorMapNames.BLUES);
        Assertions.assertNotNull(ColorMapNames.CIVIDIS);
        Assertions.assertNotNull(ColorMapNames.COOLWARM);
        Assertions.assertNotNull(ColorMapNames.DARK2);
        Assertions.assertNotNull(ColorMapNames.GNBU);
        Assertions.assertNotNull(ColorMapNames.GREENS);
        Assertions.assertNotNull(ColorMapNames.GREYS);
        Assertions.assertNotNull(ColorMapNames.HOT);
        Assertions.assertNotNull(ColorMapNames.JET);
        Assertions.assertNotNull(ColorMapNames.MAGMA);
        Assertions.assertNotNull(ColorMapNames.OCEAN);
        Assertions.assertNotNull(ColorMapNames.ORANGES);
        Assertions.assertNotNull(ColorMapNames.PASTEL1);
        Assertions.assertNotNull(ColorMapNames.PLASMA);
        Assertions.assertNotNull(ColorMapNames.PURPLES);
        Assertions.assertNotNull(ColorMapNames.RAINBOW);
        Assertions.assertNotNull(ColorMapNames.REDS);
        Assertions.assertNotNull(ColorMapNames.SET1);
        Assertions.assertNotNull(ColorMapNames.SPECTRAL);
        Assertions.assertNotNull(ColorMapNames.TERRAIN);
        Assertions.assertNotNull(ColorMapNames.VIRIDIS);
        Assertions.assertNotNull(ColorMapNames.YLGNBU);
    }

    @Test
    public void testToString() {
        Assertions.assertEquals("accent", ColorMapNames.ACCENT.toString());
        Assertions.assertEquals("viridis", ColorMapNames.VIRIDIS.toString());
        Assertions.assertEquals("plasma", ColorMapNames.PLASMA.toString());
        Assertions.assertEquals("inferno", ColorMapNames.INFERNO.toString());
    }

    @Test
    public void testEquality() {
        ColorMapNames accent1 = ColorMapNames.fromString("accent");
        ColorMapNames accent2 = ColorMapNames.fromString("accent");
        Assertions.assertEquals(accent1, accent2);
        Assertions.assertEquals(accent1, ColorMapNames.ACCENT);
    }
}
