package com.lx;

import java.util.*;

/**
 * Created by lx on 17-2-16.
 */
public class HARAlgorithm {
    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public List VE2PE[];
    // Dijkstra get shortest path to To
    public int Dist[];
    public final int  infi = 9999;

    public HARAlgorithm(Utils a_utils) {
        this.utils = a_utils;
        PGFreeCapacity = new double[utils.PG.Node];
        PGFreeBandwidth = new double[utils.PG.Node][utils.PG.Node];
        VN2PN = new int[utils.VG.Node];
        VE2PE = new List[utils.VG.Edge];
        Dist = new int[utils.PG.Node];
        for(int i = 0; i < utils.VG.Edge; i ++){
            VE2PE[i] = new ArrayList<Integer>();
        }
        for(int i = 0; i < utils.PG.Node; i ++){
            PGFreeCapacity[i] = utils.PG.NodeCapacity[i];
            for(int j = 0; j < utils.PG.Node; j++)
                PGFreeBandwidth[i][j] = utils.PG.EdgeCapacity[i][j];
        }
    }

    public void Deploy(){
        // sort PG Available Resources, with two parameter(cap, id)
        List<Pair> PGLeftCap = new ArrayList<>();
        for(int i = 0; i < utils.PG.Node; i ++){
            double TotalBandwidth = 0;
            double TotalCapacity = utils.PG.NodeCapacity[i];
            for(int j = 0; j < utils.PG.Node; j ++){
                if(utils.PG.EdgeCapacity[i][j] > 0)
                    TotalBandwidth += utils.PG.EdgeCapacity[i][j];
            }
            double AvailableResources = TotalBandwidth * TotalCapacity;
            PGLeftCap.add(new Pair(AvailableResources,i));
        }
        PGLeftCap.sort(Pair.comparator);

        // sort VG Available Resources.
        List<Pair> VGCap = new ArrayList<>();
        for(int i = 0; i < utils.VG.Node; i ++){
            double TotalBandwidth = 0;
            double TotalCapacity = utils.VG.NodeCapacity[i];
            for(int j = 0; j < utils.VG.Node; j ++){
                if(utils.VG.EdgeCapacity[i][j] > 0)
                    TotalBandwidth += utils.VG.EdgeCapacity[i][j];
            }
            double AvailableResources = TotalBandwidth * TotalCapacity;
            VGCap.add(new Pair(AvailableResources,i));
        }
        VGCap.sort(Pair.comparator);

        //high cap V to high cap P
        int Indexofhost = 0;
        for(int i = 0; i < utils.VG.Node; i ++){
            int VNode = VGCap.get(i).Id;
            double VCapacity = utils.VG.NodeCapacity[VNode];
            int PNode = PGLeftCap.get(Indexofhost).Id;
            double PCapacity = PGFreeCapacity[PNode];

            while(PCapacity - VCapacity < 0) {
                Indexofhost++;
                PNode = PGLeftCap.get(Indexofhost).Id;
                PCapacity = PGFreeCapacity[PNode];
            }
            PGFreeCapacity[PNode] -= VCapacity;
            VN2PN[VNode] = PNode;
        }
        // deploy VEdges
        int VEindex = 0;
        for(int i = 0; i < utils.VG.Node; i ++){
            for(int j = i; j < utils.VG.Node; j ++){
                if (utils.VG.EdgeCapacity[i][j] > 0){
                    boolean res = DeployVPath(VN2PN[i], VN2PN[j],utils.VG.EdgeCapacity[i][j],VEindex);
                    if (res == false){
                        System.out.println("Deploy Virtual Edge " + i + " " + j + "Not Successed!");
                    }
                    else
                        System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!!");
                    VEindex ++;
                }
            }
        }
    }

    public void Dijkstra(int source, double bandwidth){
        boolean Selected[] = new boolean[utils.PG.Node];
        Arrays.fill(Dist,0);
        Dist[source] = 0;
        Selected[source] = true;

        for(int i = 0; i < utils.PG.Node; i++){
            if (i == source)
                continue;
            if(PGFreeBandwidth[source][i] >= bandwidth)
                Dist[i] = 1;
            else
                Dist[i] = infi;
        }
        for(int i = 0; i < utils.PG.Node - 1; i ++){
            int mindistance = infi;
            int minnode = -1;
            for(int j = 0; j < utils.PG.Node; j ++){
                if(Selected[j] == false && Dist[j] < mindistance){
                    minnode = j;
                    mindistance = Dist[j];
                }
            }
            if(minnode == -1)
                break;
            Selected[minnode] = true;
            for(int j = 0; j < utils.PG.Node; j ++){
                if(Selected[j] == false && PGFreeBandwidth[minnode][j] >= bandwidth){
                    if(Dist[minnode] + 1 < Dist[j]) {
                        Dist[j] = Dist[minnode] + 1;

                    }
                }
            }
        }
    }
/*
    public boolean Astar(int From, int To,double bandwidth){
        boolean Successed = false;
        if(Dist[From] == infi)
            return Successed;
        int Count[] = new int[utils.PG.Node];
        Arrays.fill(Count,0);
        Queue<Node> queue = new PriorityQueue<>(comparator);
        queue.offer(new Node(0,From));
        while(queue.isEmpty() == false){
            int len = queue.peek().Capacity;
            int v = queue.peek().Id;
            queue.poll();
            Count[v] ++;
            if(Count[To] == K){
                Successed = true;
                break;
            }
            if(Count[v] > K)
                continue;
            for(int i = 0; i < utils.PG.Node; i++){
                if(utils.PG.EdgeCapacity[v][i] > 0)
                    queue.offer(new Node(len+1,i));
            }
        }
        return Successed;
    }
*/
    public boolean DeployVPath(int From, int To, double Bandwidth, int VEindex){
        if(From == To)
            return true;

        boolean Successed = true;
        return Successed;
    }

    public Comparator<Node> comparator = new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            return o1.Capacity + Dist[o1.Id]  - o2.Capacity - Dist[o2.Id];
        }
    };
}
