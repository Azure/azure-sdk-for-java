package com.azure.ai.inkrecognizer;

import java.util.List;

class InkStrokeImplementor implements InkStroke {

    private List<InkPoint> inkPoints;
    private InkStrokeKind inkStrokeKind;
    private long id;
    private String language;

    InkStrokeImplementor() {
    }

    void setInkPoints(List<InkPoint> inkPoints) {
        this.inkPoints = inkPoints;
    }

    void setInkStrokeKind(String inkStrokeKind) {
        this.inkStrokeKind = InkStrokeKind.getInkStrokeKindOrDefault(inkStrokeKind);
    }

    void setId(long id) {
        this.id = id;
    }

    void setLanguage(String language) {
        this.language = language;
    }

    public Iterable<InkPoint> getInkPoints() {
        return inkPoints;
    }

    public InkStrokeKind getKind() {
        return inkStrokeKind;
    }

    public long getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

}