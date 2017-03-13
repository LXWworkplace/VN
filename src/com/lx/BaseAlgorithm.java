package com.lx;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.*;
import java.util.*;

/**
 * Created by lx on 17-2-14.
 */
public class BaseAlgorithm extends Algorithm{

    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public double VEBandwidth[];
    public List VE2PE[];

    public BaseAlgorithm(Utils utils) {
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

    public void Deploy(){
        // sort PG Free Capacity , with two parameter(cap, id)
        List<Pair> PGLeftCap = new ArrayList<>();
        for(int i = 0; i < utils.PG.Node; i ++){
            PGLeftCap.add(new Pair(PGFreeCapacity[i],i));
        }
        PGLeftCap.sort(Pair.comparator);

        // sort VG Free Capacity.
        List<Pair> VGCap = new ArrayList<>();
        for(int i = 0; i < utils.VG.Node; i ++){
            VGCap.add(new Pair(utils.VG.NodeCapacity[i],i));
        }
        VGCap.sort(Pair.comparator);

        //high cap V to high cap P
        int Indexofhost = 0;
        for(int i = 0; i < utils.VG.Node; i ++){
            int VNode = VGCap.get(i).Id;
            double VCapacity = VGCap.get(i).Capacity;

            while(PGLeftCap.get(Indexofhost).Capacity - VCapacity < 0)
                Indexofhost ++;
            PGLeftCap.get(Indexofhost).Capacity -= VCapacity;
            VN2PN[VNode] = PGLeftCap.get(Indexofhost).Id;
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
        System.out.println("asdfg");
    }

    public boolean DeployVPath(int From, int To, double Bandwidth, int VEindex){
        // one VEdge in one PNode
        if (From == To){
            return true;
        }
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
            if(utils.PG.EdgeCapacity[From][i] > 0)
                Dist[i] = 1;
            else
                Dist[i] = infi;
        }
        for(int i = 0; i < utils.PG.Node - 1; i ++){
            int mindistance = infi;
            int minnode = -1;
            if(Dist[To] < infi){
                break;
            }
            for(int j = 0; j < utils.PG.Node; j ++){
                if(Selected[j] == false && Dist[j] < mindistance){
                    minnode = j;
                    mindistance = Dist[j];
                }
            }
            Selected[minnode] = true;
            for(int j = 0; j < utils.PG.Node; j ++){
                if(Selected[j] == false && utils.PG.EdgeCapacity[minnode][j] >= 0){
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
    /*
    public boolean DeployVPath(int From, int To, double Bandwidth, int VEindex){
        boolean Visit[] = new boolean[utils.PG.Node];
        boolean Successed = false;
        Visit[From] = true;
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(From);
        VE2PE[VEindex].add(From);
        while(queue.isEmpty() == false){
            if(Successed == true)
                break;
            int node = queue.poll();
            for(int i = 0; i < utils.PG.Node; i ++) {
                if (utils.PG.EdgeCapacity[node][i] <= 0)
                    continue;
                else {
                    if (Visit[i] == false && PGFreeBandwidth[node][i] >= Bandwidth) {
                        Visit[i] = true;
                        if (i == To) {
                            Successed = true;
                            VE2PE[VEindex].add(i);
                            break;
                        } else {
                            queue.offer(i);
                            VE2PE[VEindex].add(i);
                        }
                    }
                }
            }
        }
        return Successed;
    }
*/


}
