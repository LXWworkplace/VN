package com.lx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.MonitorInfo;
import java.util.*;

/**
 * Created by lx on 17-2-22.
 * main Algorithm is based on Link-opt
 */
public class BaseAlgorithm1 extends Algorithm{
    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public double VEBandwidth[];
    public List VE2PE[];

    public BaseAlgorithm1(Utils utils) {
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
        Queue<Link> Linkqueue = new PriorityQueue<>(Link.comparator);
        for(int i = 0; i < utils.VG.Node; i ++) {
            for (int j = i; j < utils.VG.Node; j++) {
                if (utils.VG.EdgeCapacity[i][j] > 0) {
                    Linkqueue.offer(new Link(i, j, utils.VG.EdgeCapacity[i][j]));
                }
            }
        }
        int VEindex = 0;
        int falsevlinkmapped = 0;
        while(Linkqueue.isEmpty() == false){
            Link Linktmp = Linkqueue.poll();
            int i = Linktmp.From;
            int j = Linktmp.To;
            boolean Successed = false;
            List path = new ArrayList<Integer>();
            if(VN2PN[i] != -1 && VN2PN[j] != -1){
                Successed = TwoNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),path);
                if(Successed == false) {
                    BDfailcost += utils.VG.EdgeCapacity[i][j];
                    falsevlinkmapped ++;
                    System.out.println("Type 1 failed  there is not enougn BD from " + VN2PN[i] + "  to  " + VN2PN[j]);
                    continue;
                }
                else{
                    for(int k = 0; k < path.size() - 1; k ++){
                        int from = (Integer)path.get(k);
                        int to = (Integer)path.get(k+1);
                        PGFreeBandwidth[from][to] -= utils.VG.EdgeCapacity[i][j];
                        PGFreeBandwidth[to][from] -= utils.VG.EdgeCapacity[i][j];
                    }
                    VE2PE[VEindex] = new ArrayList(path);
                    VEBandwidth[VEindex] = utils.VG.EdgeCapacity[i][j];
                    BDcost += utils.VG.EdgeCapacity[i][j] * VE2PE[VEindex].size();
                    System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 1");
                }
            }
            else if((VN2PN[i] == -1 && VN2PN[j] != -1) || (VN2PN[i] != -1 && VN2PN[j] == -1)){
                if(VN2PN[i] == -1)
                    Successed = OneNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),i,VN2PN[j],path);
                else
                    Successed = OneNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),j,VN2PN[i],path);
                if(Successed == false) {
                    BDfailcost += utils.VG.EdgeCapacity[i][j];
                    falsevlinkmapped ++;
                    System.out.println("Type 2 failed  cant find appropriate path " + i + "  to  " + j);
                    continue;
                }
                else{
                    int pnode = (Integer) path.get(path.size()-1);
                    if(VN2PN[i] == -1) {
                        VN2PN[i] = pnode;
                        PGFreeCapacity[pnode] -= utils.VG.NodeCapacity[i];
                    }
                    else {
                        VN2PN[j] = pnode;
                        PGFreeCapacity[pnode] -= utils.VG.NodeCapacity[j];
                    }
                    for(int k = 0; k < path.size() - 1; k ++){
                        int from = (Integer)path.get(k);
                        int to = (Integer)path.get(k+1);
                        PGFreeBandwidth[from][to] -= utils.VG.EdgeCapacity[i][j];
                        PGFreeBandwidth[to][from] -= utils.VG.EdgeCapacity[i][j];
                    }
                    VE2PE[VEindex] = new ArrayList(path);
                    VEBandwidth[VEindex] = utils.VG.EdgeCapacity[i][j];
                    BDcost += utils.VG.EdgeCapacity[i][j] * VE2PE[VEindex].size();
                    System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 2");
                }
            }
            else{
                Link Plink[] = new Link[1];
                Plink[0] = new Link(-1,-1,-1);
                Successed = NoneNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),Plink);
                if(Successed == false){
                    // judge if the Pnode Freecapacity is not enough
                    if(Plink[0].From == -1 && Plink[0].To == -1 && Plink[0].Bandwidth == -1){
                        // ReMap VNode i
                        Queue<Pair> queue1 = new PriorityQueue<>(Pair.comparator);
                        for(int k = 0; k < utils.PG.Node; k ++){
                            if(PGFreeCapacity[k] >= utils.VG.NodeCapacity[i])
                                queue1.offer(new Pair(ComputeAllFreeBD(k),k));
                        }
                        if(queue1.isEmpty()){
                            BDfailcost += utils.VG.EdgeCapacity[i][j];
                            falsevlinkmapped ++;
                            System.out.println("Type 3 failed again by fail-tactics find appropriate path " +  i  +"  to  " + j);
                            continue;
                        }
                        Pair pair = queue1.poll();
                        VN2PN[i] = pair.Id;
                        PGFreeCapacity[pair.Id] -= utils.VG.NodeCapacity[i];

                        // ReMap VNode j
                        Queue<Pair> queue2 = new PriorityQueue<>(Pair.comparator);
                        for(int k = 0; k < utils.PG.Node; k ++){
                            if(PGFreeCapacity[k] >= utils.VG.NodeCapacity[j])
                                queue2.offer(new Pair(ComputeAllFreeBD(k),k));
                        }
                        if(queue2.isEmpty()){
                            BDfailcost += utils.VG.EdgeCapacity[i][j];
                            falsevlinkmapped ++;
                            System.out.println("Type 3 failed again by fail-tactics find appropriate path " +  i  +"  to  " + j);
                            continue;
                        }
                        Pair pair1 = queue2.poll();
                        VN2PN[j] = pair1.Id;
                        PGFreeCapacity[pair1.Id] = utils.VG.NodeCapacity[j];

                        //  ReMap Link i->j
                        Link newlink = new Link(i,j,utils.VG.EdgeCapacity[i][j]);
                        List<Integer> remaplist = new ArrayList<>();
                        boolean res = TwoNodeDeployed(newlink,remaplist);
                        if (!res){
                            BDfailcost += utils.VG.EdgeCapacity[i][j];
                            falsevlinkmapped ++;
                            System.out.println("Type 3 failed again by fail-tactics find appropriate path " +  i  +"  to  " + j);
                            continue;
                        }
                        else{
                            for(int k = 0; k < remaplist.size() - 1; k ++){
                                int from = remaplist.get(k);
                                int to = remaplist.get(k+1);
                                PGFreeBandwidth[from][to] -= utils.VG.EdgeCapacity[i][j];
                                PGFreeBandwidth[to][from] -= utils.VG.EdgeCapacity[i][j];
                            }
                            VE2PE[VEindex] = new ArrayList(remaplist);
                            VEBandwidth[VEindex] = utils.VG.EdgeCapacity[i][j];
                            BDcost += utils.VG.EdgeCapacity[i][j] * VE2PE[VEindex].size();
                            System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 3  fail-tactics");
                        }
                    }
                    //System.out.println("Type 3 failed  cant find appropriate path " +  i  +"  to  " + j);
                    continue;
                }
                else{
                    int maxid = Plink[0].From;
                    int minid = Plink[0].To;
                    if(PGFreeCapacity[maxid] < PGFreeCapacity[minid]) {
                        int swap = maxid;
                        maxid = minid;
                        minid = swap;
                    }
                    int vmaxid = i;
                    int vminid = j;
                    if(utils.VG.NodeCapacity[i] < utils.VG.NodeCapacity[j]){
                        int swap = vmaxid;
                        vmaxid = vminid;
                        vminid = swap;
                    }
                    VN2PN[vmaxid] = maxid;
                    VN2PN[vminid] = minid;
                    PGFreeCapacity[maxid] -= utils.VG.NodeCapacity[vmaxid];
                    PGFreeCapacity[minid] -= utils.VG.NodeCapacity[vminid];
                    PGFreeBandwidth[maxid][minid] -= utils.VG.EdgeCapacity[i][j];
                    PGFreeBandwidth[minid][maxid] -= utils.VG.EdgeCapacity[i][j];
                    VE2PE[VEindex].add(minid);
                    VE2PE[VEindex].add(maxid);
                    VEBandwidth[VEindex] = utils.VG.EdgeCapacity[i][j];
                    BDcost += utils.VG.EdgeCapacity[i][j] * VE2PE[VEindex].size();
                    System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 3");
                }

            }
            VEindex ++;
        }
        if(falsevlinkmapped == 0){
            VNmapped ++;
            Vlinkmapped += utils.VG.Edge;
            Vlinksum += utils.VG.Edge;
        }
        else{
            Vlinkmapped += (utils.VG.Edge - falsevlinkmapped);
            Vlinksum += utils.VG.Edge;
        }
        // log the cost of deploy
        int Pnodeused = PNodeUsed();
        double PNodeBalanceRatio = ComputePnodeBalanceRatio();
        double PLinkBalanceRatio = ComputePLinkBalanceRatio();
        double BalanceRatio = PLinkBalanceRatio * PNodeBalanceRatio;
        String ResultFile = log;
        PrintWriter out = null;

        FileWriter file = null;
        try {
            file = new FileWriter(ResultFile,true);
            out = new PrintWriter(file);
            out.println(Pnodeused + "    " +PNodeBalanceRatio + "   " + PLinkBalanceRatio + "   " + BalanceRatio);
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

    // the method when the Vedge's two node didn't deployed;
    public boolean NoneNodeDeployed(Link Vlink, Link Plink[]){
        Queue queue = new PriorityQueue<Link>(Link.comparator);
        for(int i = 0; i < utils.PG.Node; i ++){
            for(int j = i + 1; j < utils.PG.Node; j ++)
                if(PGFreeBandwidth[i][j] > Vlink.Bandwidth)
                    queue.offer(new Link(i,j,PGFreeBandwidth[i][j]));
        }
        if(queue.isEmpty())
            return false;
        Link top = (Link) queue.poll();
        if(top.Bandwidth < Vlink.Bandwidth)
            return false;
        double BigerPGFreeCapacity = Math.max(PGFreeCapacity[top.From],PGFreeCapacity[top.To]);
        double SmallerPGFreeCapacity = Math.min(PGFreeCapacity[top.From],PGFreeCapacity[top.To]);
        double BigerVNodeCapacity = Math.max(utils.VG.NodeCapacity[Vlink.From],utils.VG.NodeCapacity[Vlink.To]);
        double SmallerVNodeCapacity = Math.min(utils.VG.NodeCapacity[Vlink.From],utils.VG.NodeCapacity[Vlink.To]);
        while(BigerPGFreeCapacity < BigerVNodeCapacity || SmallerPGFreeCapacity < SmallerVNodeCapacity){
            if(queue.isEmpty())
                return false;
            top = (Link) queue.poll();
            if(top.Bandwidth < Vlink.Bandwidth)
                return false;
            BigerPGFreeCapacity = Math.max(PGFreeCapacity[top.From],PGFreeCapacity[top.To]);
            SmallerPGFreeCapacity = Math.min(PGFreeCapacity[top.From],PGFreeCapacity[top.To]);
        }
        Plink[0] = new Link(top.From,top.To,top.Bandwidth);
        return true;
    }

    // the method when Vedge's one node didn't deployed; VNode -> the not deployed; PNode-> where is the deployed Vnode; list -> the path find
    // limit k loop for path
    public boolean OneNodeDeployed(Link Vlink, int VNode, int PNode, List list){
        boolean Successed = false;
        boolean Selected[] = new boolean[utils.PG.Node];
        Selected[PNode] = true;
        int Path[] = new int[utils.PG.Node];
        Arrays.fill(Path, -1);
        Path[PNode] = 0;
        Queue<Pair> queue = new PriorityQueue<>(Pair.comparator);
        for(int i = 0; i < utils.PG.Node; i ++){
            if(PGFreeBandwidth[PNode][i] >= Vlink.Bandwidth){
                Path[i] = PNode;
                queue.offer(new Pair(PGFreeCapacity[i],i));
            }
        }
        int successednode = -1;
        int k = 0;
        while(queue.isEmpty() == false){
            Queue<Pair> queue1 = new PriorityQueue<>(Pair.comparator);
            while(queue.isEmpty() == false){
                Pair pair = (Pair) queue.poll();
                if(PGFreeCapacity[pair.Id] >= utils.VG.NodeCapacity[VNode]){
                    successednode = pair.Id;
                    Successed = true;
                    break;
                }
                else{
                    Selected[pair.Id] = true;
                    for(int i = 0; i < utils.PG.Node; i ++){
                        if(Selected[i] == false && PGFreeBandwidth[pair.Id][i] >= Vlink.Bandwidth){
                            Path[i] = pair.Id;
                            queue1.offer(new Pair(PGFreeCapacity[i],i));
                        }
                    }
                }
            }
            if (Successed == true)
                break;
            else{
                queue = queue1;
            }
            k++;
        }
        if(Successed == true){
            list.add(successednode);
            int temp = Path[successednode];
            while(temp != 0){
                list.add(temp);
                temp = Path[temp];
            }
            Collections.reverse(list);
        }
        return Successed;
    }

    // the method for two Vnode hava been deployed
    public boolean TwoNodeDeployed(Link Vlink, List list){
        boolean Successed = true;
        int From = VN2PN[Vlink.From];
        int To = VN2PN[Vlink.To];
        if (From == To)
            return Successed;

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
            if(PGFreeBandwidth[From][i] >= Vlink.Bandwidth)
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
                if(Selected[j] == false && PGFreeBandwidth[minnode][j] >= Vlink.Bandwidth){
                    if(Dist[minnode] + 1 < Dist[j]) {
                        Dist[j] = Dist[minnode] + 1;
                        Path[j] = minnode;
                        //System.out.println("jjjjj   " + j + "   minnode   " + minnode);
                    }
                }
            }
        }

        int temp = To;
        list.add(temp);
        while(Path[temp] != -1){
            temp = Path[temp];
            list.add(temp);
        }
        list.add(From);
        Collections.reverse(list);
        return true;
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

    //used for compute balanceratio
    public double ComputePnodeBalanceRatio(){
        int count = 0;
        double max = 0;
        double sum = 0;
        for(int i = 0 ; i < utils.PG.Node; i ++){
            if(utils.PG.NodeCapacity[i] - PGFreeCapacity[i] > 0.000001){
                sum += PGFreeCapacity[i];
                count ++;
                if(PGFreeCapacity[i] > max)
                    max = PGFreeCapacity[i];
            }
        }
        return max/(sum/count);
    }

    public double ComputePLinkBalanceRatio(){
        int count = 0;
        double max = 0;
        double sum = 0;
        for(int i = 0; i < utils.PG.Node; i++){
            for(int j = i + 1; j < utils.PG.Node; j ++){
                if(utils.PG.EdgeCapacity[i][j] > 0){
                    if(utils.PG.EdgeCapacity[i][j] - PGFreeBandwidth[i][j] > 0.000001){
                        sum += PGFreeBandwidth[i][j];
                        count ++;
                        if(PGFreeBandwidth[i][j] > max)
                            max = PGFreeBandwidth[i][j];
                    }
                }
            }
        }
        return max/(sum/count);
    }

    //Compute a Pnode，s all free bd
    public double ComputeAllFreeBD(int PNode){
        double res = 0;
        for(int i = 0; i < utils.PG.Node; i ++){
            if (PGFreeBandwidth[PNode][i] > 0)
                res += PGFreeBandwidth[PNode][i];
        }
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
