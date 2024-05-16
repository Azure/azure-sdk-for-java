/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * Test for the {link ListResult<T>} type.
 * 
 */
public class GenericListResultTest {
    private final String[] expectedStrings = { "One", "Two", "Three" };

    @Test
    public void emptyListResultIsEmpty() throws Exception {
        assertTrue(new ListResult<String>(new ArrayList<String>()).isEmpty());
    }

    @Test
    public void listWithContentsIsNotEmpty() throws Exception {
        ListResult<String> result = createStringListResult();

        assertFalse(result.isEmpty());
    }

    @Test
    public void createWithEmptyCollectionGivesNoResults() throws Exception {
        List<AssetInfo> result = new ListResult<AssetInfo>(
                new ArrayList<AssetInfo>());

        assertEquals(0, result.size());
    }

    @Test
    public void createWithCollectionContentsContainsContents() throws Exception {
        List<String> result = createStringListResult();

        assertEquals(expectedStrings.length, result.size());
    }

    @Test
    public void createWithCollectionContentsCanRetrieveContents()
            throws Exception {
        List<String> result = createStringListResult();

        for (int i = 0; i < expectedStrings.length; ++i) {
            assertEquals(expectedStrings[i], result.get(i));
        }
    }

    @Test
    public void canIterateThroughResults() throws Exception {
        List<String> result = createStringListResult();

        int i = 0;
        for (String s : result) {
            assertEquals(expectedStrings[i], s);
            ++i;
        }
    }

    @Test
    public void canCheckContainsForObjectInList() throws Exception {
        ListResult<String> result = createStringListResult();

        assertTrue(result.contains(expectedStrings[1]));
    }

    @Test
    public void canCheckContainsForObjectNotInList() throws Exception {
        ListResult<String> result = createStringListResult();

        assertFalse(result.contains("This is not a string in the list"));
    }

    @Test
    public void canCheckContainsAllForObjectsInList() throws Exception {
        ListResult<String> result = createStringListResult();
        List<String> contains = new ArrayList<String>();
        contains.add(expectedStrings[2]);
        contains.add(expectedStrings[1]);

        assertTrue(result.containsAll(contains));
    }

    @Test
    public void canCheckContainsAllForObjectsNotInList() throws Exception {
        ListResult<String> result = createStringListResult();
        List<String> contains = new ArrayList<String>();
        contains.add(expectedStrings[2]);
        contains.add("This is not in the list");

        assertFalse(result.containsAll(contains));
    }

    @Test
    public void canGetIndexOfItemInList() throws Exception {
        ListResult<String> result = createStringListResult();

        assertEquals(1, result.indexOf(expectedStrings[1]));
    }

    @Test
    public void indexOfItemNotInListIsMinusOne() throws Exception {
        ListResult<String> result = createStringListResult();

        assertEquals(-1, result.indexOf("Not in collection"));
    }

    @Test
    public void lastIndexOfItemInCollectionIsCorrect() throws Exception {
        ListResult<String> result = new ListResult<String>(Arrays.asList("c",
                "b", "c", "a"));

        assertEquals(2, result.lastIndexOf("c"));
    }

    @Test
    public void lastIndexOfItemNotInCollectionIsMinusOne() throws Exception {
        ListResult<String> result = createStringListResult();

        assertEquals(-1, result.lastIndexOf("Not in collection"));
    }

    @Test
    public void optionalListMethodsAreUnsupported() throws Exception {
        final ListResult<String> result = createStringListResult();

        assertListIsImmutable(result);
    }

    @Test
    public void canGetWorkingListIteratorFromResult() throws Exception {
        ListResult<String> result = createStringListResult();

        ListIterator<String> iterator = result.listIterator();
        int i = 0;
        for (i = 0; i < expectedStrings.length; ++i) {
            assertIteratorAtPositionMovingForward(i, iterator);
        }

        for (; i >= 0; --i) {
            assertIteratorAtPositionMovingBackward(i, iterator);
        }
    }

    @Test
    public void canGetWorkingListIteratorAtIndexFromResult() throws Exception {
        ListResult<String> result = createStringListResult();

        ListIterator<String> iterator = result.listIterator(1);
        int i = 1;
        for (; i < expectedStrings.length; ++i) {
            assertIteratorAtPositionMovingForward(i, iterator);
        }

        for (; i >= 0; --i) {
            assertIteratorAtPositionMovingBackward(i, iterator);
        }
    }

    @Test
    public void listIteratorIsImmutable() throws Exception {
        ListResult<String> result = createStringListResult();
        final ListIterator<String> iterator = result.listIterator();

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                iterator.add("new string");
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                iterator.remove();
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                iterator.set("replace string");
            }
        });
    }

    @Test
    public void listIteratorIsImmutableWhenCreatedWithIndex() throws Exception {
        ListResult<String> result = createStringListResult();
        final ListIterator<String> iterator = result.listIterator(1);

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                iterator.add("new string");
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                iterator.remove();
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                iterator.set("replace string");
            }
        });
    }

    @Test
    public void canGetObjectArrayFromResult() throws Exception {
        ListResult<String> result = createStringListResult();
        Object[] array = result.toArray();

        assertNotNull(array);
        assertEquals(result.size(), array.length);
        for (int i = 0; i < result.size(); ++i) {
            assertEquals(result.get(i), array[i]);
        }
    }

    @Test
    public void canGetTypedArrayFromResult() throws Exception {
        ListResult<String> result = createStringListResult();
        String[] array = result.toArray(new String[0]);

        assertNotNull(array);
        assertEquals(result.size(), array.length);
        for (int i = 0; i < result.size(); ++i) {
            assertEquals(result.get(i), array[i]);
        }
    }

    @Test
    public void canGetExpectedSublistFromResult() throws Exception {
        ListResult<String> result = createStringListResult();
        List<String> sublist = result.subList(1, 3);

        assertEquals(2, sublist.size());
        assertEquals(result.get(1), sublist.get(0));
        assertEquals(result.get(2), sublist.get(1));
    }

    @Test
    public void sublistIsImmutable() throws Exception {
        ListResult<String> result = createStringListResult();
        final List<String> sublist = result.subList(1, 3);

        assertListIsImmutable(sublist);
    }

    private ListResult<String> createStringListResult() {
        ListResult<String> result = new ListResult<String>(
                Arrays.asList(expectedStrings));

        return result;
    }

    // / Assertion helpers ///

    private void assertIteratorState(int position, ListIterator<String> iterator) {
        boolean expectHasNext = position < expectedStrings.length;
        boolean expectHasPrevious = position != 0;

        assertNotNull("Iterator is null", iterator);
        assertEquals(String.format("HasNext at position %d", position),
                expectHasNext, iterator.hasNext());
        assertEquals(String.format("HasPrevious at position %d", position),
                expectHasPrevious, iterator.hasPrevious());

        if (expectHasNext) {
            assertEquals(String.format("NextIndex at position %d", position),
                    position, iterator.nextIndex());
        }

        if (expectHasPrevious) {
            assertEquals(
                    String.format("PreviousIndex at position %d", position),
                    position - 1, iterator.previousIndex());
        }

    }

    private void assertIteratorAtPositionMovingForward(int position,
            ListIterator<String> iterator) {
        assertIteratorState(position, iterator);

        if (iterator.hasNext()) {
            assertEquals(expectedStrings[position], iterator.next());
        } else {
            try {
                iterator.next();
                fail("Exception should have been thrown");
            } catch (NoSuchElementException ex) {
                // Ok, we expected this
            }
        }
    }

    private void assertIteratorAtPositionMovingBackward(int position,
            ListIterator<String> iterator) {
        assertIteratorState(position, iterator);

        if (iterator.hasPrevious()) {
            assertEquals(expectedStrings[position - 1], iterator.previous());
        } else {
            try {
                iterator.previous();
                fail("Exception should have been thrown");
            } catch (NoSuchElementException ex) {
                // Ok, we expected this
            }
        }
    }

    private interface Action {
        void Do();
    }

    private void assertUnsupported(Action action) {
        try {
            action.Do();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // This is ok
        }
    }

    private void assertListIsImmutable(final List<String> list) {
        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.add("new string");
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.add(2, "new string");
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.addAll(Arrays.asList("a", "b"));
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.addAll(2, Arrays.asList("a", "b"));
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.clear();
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.remove(1);
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.remove(list.get(1));
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.removeAll(Arrays.asList(list.get(0), list.get(1)));
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.retainAll(Arrays.asList(list.get(1)));
            }
        });

        assertUnsupported(new Action() {
            @Override
            public void Do() {
                list.set(1, "new string");
            }
        });
    }
}
