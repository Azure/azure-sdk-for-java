package com.microsoft.windowsazure.services.table.models;

import java.util.ArrayList;
import java.util.List;

public class Query {
    private List<String> selectFields = new ArrayList<String>();
    private String from;
    private Filter filter;
    private List<String> orderByFields = new ArrayList<String>();
    private Integer top;

    public List<String> getSelectFields() {
        return selectFields;
    }

    public Query setSelectFields(List<String> selectFields) {
        this.selectFields = selectFields;
        return this;
    }

    public Query addSelectField(String selectField) {
        this.selectFields.add(selectField);
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Query setFrom(String from) {
        this.from = from;
        return this;
    }

    public Filter getFilter() {
        return filter;
    }

    public Query setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    public List<String> getOrderByFields() {
        return orderByFields;
    }

    public Query setOrderByFields(List<String> orderByFields) {
        this.orderByFields = orderByFields;
        return this;
    }

    public Query addOrderByField(String orderByField) {
        this.orderByFields.add(orderByField);
        return this;
    }

    public Integer getTop() {
        return top;
    }

    public Query setTop(Integer top) {
        this.top = top;
        return this;
    }
}
