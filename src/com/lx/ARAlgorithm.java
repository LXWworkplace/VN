package com.lx;

import com.lx.Pair;
import com.lx.Utils;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by lx on 17-2-15.
 */
public class ARAlgorithm extends Algorithm{
    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public double VEBandwidth[];
    public List VE2PE[];
    public final double LimitRatio = 0.1;
    public int kill = 0;

    public ARAlgorithm(Utils a_utils) {
        this.utils = a_utils;
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
            double TotalCapacity = PGFreeCapacity[i];
            for(int j = 0; j < utils.PG.Node; j ++){
                if(PGFreeBandwidth[i][j] > 0)
                    TotalBandwidth += PGFreeBandwidth[i][j];
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

            while(!CheckPNodeAR(PNode) || PCapacity - VCapacity < 0) {
                Indexofhost++;
                PNode = PGLeftCap.get(Indexofhost).Id;
                PCapacity = PGFreeCapacity[PNode];
            }
            PGFreeCapacity[PNode] -= VCapacity;
            VN2PN[VNode] = PNode;
        }

        // deploy VEdges
        int VEindex = 0;
        int falsevlinkmapped = 0;
        for(int i = 0; i < utils.VG.Node; i ++){
            for(int j = i; j < utils.VG.Node; j ++){
                if (utils.VG.EdgeCapacity[i][j] > 0){
                    boolean res = DeployVPath(VN2PN[i], VN2PN[j],utils.VG.EdgeCapacity[i][j],VEindex);
                    if (res == false){
                        BDfailcost += utils.VG.EdgeCapacity[i][j];
                        falsevlinkmapped ++;
                        System.out.println("Deploy Virtual Edge " + i + " " + j + "Not Successed!");
                    }
                    else {
                        BDcost += utils.VG.EdgeCapacity[i][j] * VE2PE[VEindex].size();
                        System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!!");
                    }
                    VEindex ++;
                }
            }
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
        if (!CheckPathBD(VE2PE[VEindex],Bandwidth)) {
            VE2PE[VEindex].clear();
            return false;
        }
        for(int i = 0; i < VE2PE[VEindex].size() - 1; i ++){
            int sour = (Integer) VE2PE[VEindex].get(i);
            int des = (Integer) VE2PE[VEindex].get(i + 1);
            PGFreeBandwidth[sour][des] -= Bandwidth;
            PGFreeBandwidth[des][sour] -= Bandwidth;
        }
        return Successed;
    }

    // judge if a Pnode freeresource is satisfied
    public boolean CheckPNodeAR(int PNode){
        boolean Successed = true;
        double FreeBD = 0;
        double BD = 0;
        if(PGFreeCapacity[PNode]/utils.PG.NodeCapacity[PNode] < LimitRatio)
            return false;
        for(int i = 0; i < utils.PG.Node; i ++){
            if(utils.PG.EdgeCapacity[PNode][i] > 0){
                BD += utils.PG.EdgeCapacity[PNode][i];
                FreeBD += PGFreeBandwidth[PNode][i];
            }
        }
        if(FreeBD/BD < LimitRatio)
            Successed = false;
        return Successed;
    }

    // check if  a  path sufficate bandwidth limit
    public boolean CheckPathBD(List Path, double bandwidth){
        if(Path.size() == 0)
            return true;
        for(int i = 0; i < Path.size() - 1; i ++){
            int from = (Integer) Path.get(i);
            int to = (Integer) Path.get(i+1);
            if(PGFreeBandwidth[from][to] < bandwidth)
                return false;
        }
        return true;
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

    public static void main(String[] args){
        // for test
    }
}

