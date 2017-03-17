package com.lx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by lx on 17-2-18.
 * cut VN to some star center;
 */
public class MergeVNAlgorithm extends Algorithm{
    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public double VEBandwidth[];
    public List VE2PE[];
    public int kill = 0;

    public MergeVNAlgorithm(Utils utils) {
        this.utils = utils;
        PGFreeCapacity = new double[utils.PG.Node];
        PGFreeBandwidth = new double[utils.PG.Node][utils.PG.Node];
        VN2PN = new int[utils.VG.Node];
        Arrays.fill(VN2PN,-1);
        VEBandwidth = new double[utils.VG.Edge];
        Arrays.fill(VEBandwidth,0);
        VE2PE = new List[utils.VG.Edge];
        for(int i = 0; i < utils.VG.Edge; i ++){
            VE2PE[i] = new ArrayList<Integer>();
        }
        for(int i = 0; i < utils.PG.Node; i ++){
            PGFreeCapacity[i] = utils.PG.NodeCapacity[i];
            for(int j = 0; j < utils.PG.Node; j++)
                PGFreeBandwidth[i][j] = utils.PG.EdgeCapacity[i][j];
        }
    }

    public void Deploy(String log){
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

        boolean Selected[] = new boolean[utils.VG.Node];
        int MergeCenterNum = (int)(utils.VG.Node / (utils.PGCapacityMean / (utils.VGCapacityMean + Math.sqrt(utils.VGCapacitySquare))));
        List VNodeOfPNode[] = new List[MergeCenterNum];
        for(int i = 0; i < MergeCenterNum; i ++)
            VNodeOfPNode[i] = new ArrayList<Integer>();
        int Center2PN[] = new int[MergeCenterNum];

        //init the Vn Center,
        for(int i = 0, Indexofhost = 0; i < MergeCenterNum; i ++,Indexofhost ++){
            int VNode = VGCap.get(i).Id;
            double VCapacity = utils.VG.NodeCapacity[VNode];
            int PNode = PGLeftCap.get(Indexofhost).Id;

            PGFreeCapacity[PNode] -= VCapacity;
            VN2PN[VNode] = PNode;
            Selected[VNode] = true;
            VNodeOfPNode[Indexofhost].add(VNode);
            Center2PN[i] = PNode;
        }

        Queue<MergeStore> queue = new PriorityQueue(MergeStore.comparator);
        for(int i = 0; i < utils.VG.Node; i ++){
            if (Selected[i] == true)
                continue;
            MergeStore Ms = VnodeDeployCost(i,VNodeOfPNode,Center2PN);
            queue.offer(Ms);
        }
        while(queue.isEmpty() == false){
            MergeStore mergetemp = queue.poll();
            // if all center cant deploy V, add a new center;
            if(mergetemp.Pnode == -1) {
                int indexovercenter = MergeCenterNum;
                int addcenter_pn = PGLeftCap.get(indexovercenter).Id;
                while(PGFreeCapacity[addcenter_pn] - utils.VG.NodeCapacity[mergetemp.Vnode] < 0){
                    indexovercenter ++;
                    addcenter_pn = PGLeftCap.get(indexovercenter).Id;
                }
                VN2PN[mergetemp.Vnode] = addcenter_pn;
                continue;
            }
            VNodeOfPNode[mergetemp.Pnode].add(mergetemp.Vnode);
            PGFreeCapacity[Center2PN[mergetemp.Pnode]] -= utils.VG.NodeCapacity[mergetemp.Vnode];
            VN2PN[mergetemp.Vnode] = Center2PN[mergetemp.Pnode];

            // one VNode deploy to one Center, update the PriorityQueue
            Queue<MergeStore> queuetmp = new PriorityQueue(MergeStore.comparator);
            int ChangedCenterID = mergetemp.Pnode;
            for(MergeStore ms : queue){
                int vn = ms.Vnode;
                int cn = ms.Pnode;
                double weight = ms.Weight;
                double newweight = ComputeCost_VN2C(vn,ChangedCenterID,VNodeOfPNode,Center2PN);
                if (newweight > weight)
                    queuetmp.offer(new MergeStore(vn,ChangedCenterID,newweight));
                else
                    queuetmp.offer(new MergeStore(vn,cn,weight));
            }
            queue = new PriorityQueue<>(queuetmp);
        }

        // deploy VEdges
        int ixxx = 0;
        int VEindex = 0;
        for(int i = 0; i < utils.VG.Node; i ++){
            for(int j = i; j < utils.VG.Node; j ++){
                if (utils.VG.EdgeCapacity[i][j] > 0){
                    boolean res = DeployVPath(VN2PN[i], VN2PN[j],utils.VG.EdgeCapacity[i][j],VEindex);
                    if (res == false){
                        BDfailcost += utils.VG.EdgeCapacity[i][j];
                        System.out.println("Deploy Virtual Edge " + i + " " + j + "  "+ " from " + VN2PN[i]+ " " + VN2PN[j] + "Not Successed!");
                    }
                    else {
                        BDcost += utils.VG.EdgeCapacity[i][j] * VE2PE[VEindex].size();
                        System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!!");
                    }
                    VEindex ++;
                }
            }
        }
        // log the cost of deploy
        int Pnodeused = PNodeUsed();
        String ResultFile = log;
        PrintWriter out = null;

        FileWriter file = null;
        try {
            file = new FileWriter(ResultFile,true);
            out = new PrintWriter(file);
            out.print("" + Pnodeused + "    ");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            out.close();
            try {
                file.close();
            } catch (IOException e) {
                System.out.println("close file error in ADD Pnode cost to result");
                e.printStackTrace();
            }
        }
    }

    public boolean DeployVPath(int From, int To, double Bandwidth, int VEindex){
        if(From == To)
            return true;

        boolean Successed = true;
        int infi = 9999;
        int Dist[] = new int[utils.PG.Node];
        int Path[] = new int[utils.PG.Node];
        Arrays.fill(Path,-1);

        boolean Selected[] = new boolean[utils.PG.Node];
        Dist[From] = 0;
        Selected[From] = true;
        for(int i = 0; i < utils.PG.Node; i++){
            if (i == From)
                continue;
            if(PGFreeBandwidth[From][i] >= Bandwidth)
                Dist[i] = 1;
            else
                Dist[i] = infi;
        }
        for(int i = 0; i < utils.PG.Node - 1; i ++){
            int minnode = -1;
            double mindistance = infi;
            if(Dist[To] < infi){
                break;
            }
            for(int j = 0; j < utils.PG.Node; j ++){
                if(Selected[j] == false && Dist[j] < mindistance){
                    minnode = j;
                    mindistance = Dist[j];
                }
            }
            if(minnode == -1){
                System.out.println("Have Search " + i + "  i, cant find path");
                return false;
            }
            Selected[minnode] = true;
            for(int j = 0; j < utils.PG.Node; j ++){
                if(Selected[j] == false && PGFreeBandwidth[minnode][j] >= Bandwidth){
                    if(Dist[minnode] + 1 < Dist[j]) {
                        Dist[j] = Dist[minnode] + 1;
                        Path[j] = minnode;
                        //System.out.println("jjjjj   " + j + "   minnode   " + minnode);
                    }
                }
            }
        }

        List pathnode = new ArrayList<Integer>();
        int temp = To;
        while(Path[temp] != -1){
            temp = Path[temp];
            pathnode.add(temp);
        }
        VEBandwidth[VEindex] = Bandwidth;
        VE2PE[VEindex].add(From);
        for(int i = 0; i < pathnode.size(); i ++){
            VE2PE[VEindex].add(pathnode.get(pathnode.size()-i-1));
        }
        VE2PE[VEindex].add(To);
        for(int i = 0; i < VE2PE[VEindex].size() - 1; i ++){
            int sour = (Integer) VE2PE[VEindex].get(i);
            int des = (Integer) VE2PE[VEindex].get(i + 1);
            PGFreeBandwidth[sour][des] -= Bandwidth;
            PGFreeBandwidth[des][sour] -= Bandwidth;
            if (PGFreeBandwidth[sour][des] < 0) {
                Successed = false;
                System.out.println("When Not Successed From  "+sour + "  To  " + des +"  Left BD"+ PGFreeBandwidth[sour][des]);
            }
        }
        return Successed;
    }

    // Compute the Cost when Vnode deploy in Center i
    public double ComputeCost_VN2C(int Vnode, int i, List[] VNodeOfPNode, int[] Center2PN){
        double Min = -1;
        if( PGFreeCapacity[Center2PN[i]] - utils.VG.NodeCapacity[Vnode] < 0)
            return Min;
        List<Integer> Vnodelist = VNodeOfPNode[i];
        double bandwidthcost = 0;
        for(Integer node : Vnodelist){
            if(utils.VG.EdgeCapacity[node][Vnode] > 0)
                bandwidthcost += utils.VG.EdgeCapacity[node][Vnode];
        }
        if(bandwidthcost == 0)
            return Min;
        else
            return bandwidthcost;
    }

    // to Vnode i, find which Center to host and the How is the Cost
    public MergeStore VnodeDeployCost(int Vnode, List[] VNodeOfPNode,int[] Center2PN){
        double Min = -1;
        int Minnode = -1;
        for(int i = 0; i < VNodeOfPNode.length; i ++){
            if( PGFreeCapacity[Center2PN[i]] - utils.VG.NodeCapacity[Vnode] < 0)
                continue;
            List<Integer> Vnodelist = VNodeOfPNode[i];
            double bandwidthcost = 0;
            for(Integer node : Vnodelist){
                if(utils.VG.EdgeCapacity[node][Vnode] > 0)
                    bandwidthcost += utils.VG.EdgeCapacity[node][Vnode];
            }
            if (bandwidthcost != 0 && bandwidthcost > Min) {
                Min = bandwidthcost;
                Minnode = i;
            }
        }
        return new MergeStore(Vnode,Minnode,Min);
    }

    public int PNodeUsed(){
        int res = 0;
        for(int i = 0; i < utils.PG.Node; i ++){
            if(Math.abs(PGFreeCapacity[i] - utils.PG.NodeCapacity[i]) > 0.000001)
                res ++;
        }
        return res;
    }

    public int MaxPathLength(){
        int res = 0;
        for(int i = 0; i < VE2PE.length; i ++)
            if(VE2PE[i].size() > res)
                res = VE2PE[i].size();
        return res;
    }

    public void RestructVN(String path){
        utils.setVGPath(path);
        utils.ConstructVirtualGraph();
        VN2PN = new int[utils.VG.Node];
        Arrays.fill(VN2PN,-1);
        VEBandwidth = new double[utils.VG.Edge];
        Arrays.fill(VEBandwidth,0);
        VE2PE = new List[utils.VG.Edge];
        for(int i = 0; i < utils.VG.Edge; i ++){
            VE2PE[i] = new ArrayList<Integer>();
        }
    }

    public double AddVNlog(){
        double RevenueRatio = utils.VGBandwidthMean / utils.VGCapacityMean;
        double Capacityrevenue = 0;
        double Bandwidthrevenue = 0;
        String path = "/home/lx/VN/VNlog/"+utils.VG.Node+".brite";
        File file;
        PrintWriter out = null;
        try{
            file = new File(path);
            if(!file.exists()){
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            out = new PrintWriter(file);
            out.println("VN2PN  VN  PN  VNcapacity");
            for(int i = 0; i < utils.VG.Node; i ++){
                // VN i didn't map
                if(VN2PN[i] == -1)
                    continue;
                Capacityrevenue += utils.VG.NodeCapacity[i];
                out.println(i + " " + VN2PN[i] + " " + utils.VG.NodeCapacity[i]);
            }
            out.println("VE2PE from to Vbandwidth");
            for(int i = 0; i < VE2PE.length; i++){
                // VEdge i didn't map
                if(VE2PE[i].size() == 0)
                    continue;
                for(int j = 0; j < VE2PE[i].size(); j++){
                    out.print(VE2PE[i].get(j)+" ");
                }
                Bandwidthrevenue += VEBandwidth[i] / VE2PE[i].size();
                out.println(VEBandwidth[i]);
            }
        }catch (Exception e){
            System.out.println("in addVNlog there is a exception");
            e.printStackTrace();
        }finally {
            out.close();
        }
        Bandwidthrevenue += RevenueHideinPnode();
        return Capacityrevenue * RevenueRatio + Bandwidthrevenue;
    }

    public void KillLiveVN(String path){
        File file;
        Scanner scanner = null;
        try {
            file = new File(path);
            scanner = new Scanner(file);
            if(!scanner.hasNextLine()){
                System.out.println("kill Vn read a empty log  file!");
                return;
            }
            String line = scanner.nextLine();
            String linearray[] = line.split(" +");
            while(scanner.hasNextLine()){
                line = scanner.nextLine();
                linearray = line.split(" +");
                if(linearray[0].equals("VE2PE"))
                    break;
                int pnode = Integer.parseInt(linearray[1]);
                double freebandwidth = Double.parseDouble(linearray[2]);
                PGFreeCapacity[pnode] += freebandwidth;
            }
            while(scanner.hasNextLine()){
                line = scanner.nextLine();
                linearray = line.split(" +");
                double freebandwidth = Double.parseDouble(linearray[linearray.length-1]);
                for(int i = 0; i < linearray.length - 2; i ++){
                    int from = Integer.parseInt(linearray[i]);
                    int to = Integer.parseInt(linearray[i + 1]);
                    PGFreeBandwidth[from][to] += freebandwidth;
                    PGFreeBandwidth[to][from] += freebandwidth;
                }
            }

        }catch (Exception e){
            System.out.println("int kill live Vn, there is a exception");
            e.printStackTrace();
        }finally {
            scanner.close();
        }
        /*
        kill++;
        if(kill == 10){
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            for(int i = 0; i < utils.PG.Node; i ++){
                for(int j = 0; j < utils.PG.Node; j ++){
                    if (utils.PG.EdgeCapacity[i][j] > 0)
                        System.out.println("" + utils.PG.EdgeCapacity[i][j]+ "    " + (utils.PG.EdgeCapacity[i][j]- PGFreeBandwidth[i][j]));
                }
            }
        }
        */
    }

    // add  bandwidth revenue from same node
    public double RevenueHideinPnode(){
        double HidenRevenue = 0;
        for(int i = 0; i < utils.VG.Node; i ++){
            for(int j = i+1; j < utils.VG.Node; j ++){
                if(utils.VG.EdgeCapacity[i][j] > 0 && VN2PN[i] == VN2PN[j])
                    HidenRevenue += utils.VG.EdgeCapacity[i][j] * 2;
            }
        }
        return HidenRevenue;
    }

    public static void main(String[] args){
        TreeSet<Integer> queue = new TreeSet<>();
        queue.add(1);
        queue.add(3);
        queue.add(-2);



        for(Integer i : queue)
            System.out.println(i);
    }
}
