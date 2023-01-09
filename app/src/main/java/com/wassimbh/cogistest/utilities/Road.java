package com.wassimbh.cogistest.utilities;

import com.wassimbh.cogistest.data.models.GraphEdge;

public class Road {

    final GraphEdge to;
    final int distance;

    public Road(final GraphEdge to, final int distance) {
        this.to = to;
        this.distance = distance;
    }

    public GraphEdge getTo() {
        return to;
    }

    public int getDistance() {
        return distance;
    }
}
