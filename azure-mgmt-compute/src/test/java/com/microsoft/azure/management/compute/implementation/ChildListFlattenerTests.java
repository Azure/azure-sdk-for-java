package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.RestException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChildListFlattenerTests {

    @Test
    public void testFlattener() throws Exception {

        PagedList<Integer> parentList = new PagedList<Integer>(new ParentPage(0)) {
            @Override
            public Page<Integer> nextPage(String nextPageLink) throws RestException, IOException {
                return new ParentPage(Integer.parseInt(nextPageLink));
            }
        };

        ChildListFlattener<Integer, Integer> flattener = new ChildListFlattener<>(parentList, new ChildListFlattener.ChildListLoader<Integer, Integer>() {
            @Override
            public PagedList<Integer> loadList(final Integer parent) {
                return new PagedList<Integer>(new ChildPage(parent, 0)) {
                    @Override
                    public Page<Integer> nextPage(String nextPageLink) throws RestException, IOException {
                        return new ChildPage(parent, Integer.parseInt(nextPageLink));
                    }
                };
            }
        });

        List<Integer> flattenedList = flattener.flatten();
        Assert.assertEquals(6, flattenedList.size());
        Assert.assertEquals(1, (int) flattenedList.get(0));
        Assert.assertEquals(2, (int) flattenedList.get(1));
        Assert.assertEquals(3, (int) flattenedList.get(2));
        Assert.assertEquals(2, (int) flattenedList.get(3));
        Assert.assertEquals(4, (int) flattenedList.get(4));
        Assert.assertEquals(6, (int) flattenedList.get(5));
    }

    private class EmptyPage implements Page<Integer> {
        @Override
        public String getNextPageLink() {
            return null;
        }

        @Override
        public List<Integer> getItems() {
            return new ArrayList<>();
        }
    }

    private class ParentPage implements Page<Integer> {
        private int page;

        public ParentPage(int page) {
            this.page = page;
        }

        @Override
        public String getNextPageLink() {
            if (page == 5) {
                return null;
            }
            return Integer.toString(page + 1);
        }

        @Override
        public List<Integer> getItems() {
            if (page == 2) {
                List<Integer> items = new ArrayList<>();
                items.add(1);
                items.add(2);
                items.add(3);
                items.add(4);
                return items;
            } else {
                return new ArrayList<>();
            }
        }
    }

    private class ChildPage implements Page<Integer> {
        private int parent;
        private int page;

        public ChildPage(int parent, int page) {
            this.parent = parent;
            this.page = page;
        }

        @Override
        public String getNextPageLink() {
            if (page == 5) {
                return null;
            }
            return Integer.toString(page + 1);
        }

        @Override
        public List<Integer> getItems() {
            if ((parent == 1 || parent == 2) && page == 2) {
                List<Integer> items = new ArrayList<>();
                items.add(parent);
                items.add(2 * parent);
                items.add(3 * parent);
                return items;
            } else {
                return new ArrayList<>();
            }
        }
    }
}
