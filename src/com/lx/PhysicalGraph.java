package com.lx;

/**
 * Created by lx on 17-2-14.
 */
public class PhysicalGraph {
    public int Node;
    public int Edge;

    public double NodeCapacity[];
    public double EdgeCapacity[][];

    public PhysicalGraph() {
    }

    public PhysicalGraph(int node, int edge) {
        Node = node;
        Edge = edge;
    }
}
