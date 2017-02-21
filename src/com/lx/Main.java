package com.lx;


import java.util.*;

public class Main {

    public static void main(String[] args) {
        Utils util = new Utils();
        util.setPGPath("/home/lx/graphs/50.brite");
        util.setVGPath("/home/lx/graphs/200.brite");
        util.ConstructPhysicalGraph();
        util.ConstructVirtualGraph();
        BaseAlgorithm ba = new BaseAlgorithm(util);
        ARAlgorithm ar = new ARAlgorithm(util);
        HARAlgorithm har = new HARAlgorithm(util);
        Yen_ARAlgorithm yar = new Yen_ARAlgorithm(util);
        MergeVNAlgorithm mer = new MergeVNAlgorithm(util);
        mer.Deploy();
    }
}
