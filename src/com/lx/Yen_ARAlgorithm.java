package com.lx;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by lx on 17-2-16.
 */
public class Yen_ARAlgorithm extends Algorithm{
    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public double VEBandwidth[];
    public List VE2PE[];
    // Dijkstra get shortest path to To
    public int Dist[];
    public final int  infi = 9999;
    public final int Yen_K = 10;
    public int count = 0;

    public Yen_ARAlgorithm(Utils a_utils) {
        this.utils = a_utils;
        PGFreeCapacity = new double[utils.PG.Node];
        PGFreeBandwidth = new double[utils.PG.Node][utils.PG.Node];
        VN2PN = new int[utils.VG.Node];
        Arrays.fill(VN2PN,-1);
        VEBandwidth = new double[utils.VG.Edge];
        Arrays.fill(VEBandwidth,0);
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

    public void Dijkstra(int source, int des, int Dist[], List Path, boolean Selected[]){
        Arrays.fill(Dist,0);
        Dist[source] = 0;
        Selected[source] = true;
        int Prev[] = new int[utils.PG.Node];
        Arrays.fill(Prev, -1);

        for(int i = 0; i < utils.PG.Node; i++){
            if (i == source)
                continue;
            if(!Selected[i] && PGFreeBandwidth[source][i] > 0)
                Dist[i] = 1;
            else
                Dist[i] = infi;
        }
        for(int i = 0; i < utils.PG.Node - 1; i ++){
            int mindistance = infi;
            int minnode = -1;
            if(Dist[des] < infi){
                break;
            }
            for(int j = 0; j < utils.PG.Node; j ++){
                if(!Selected[j] && Dist[j] < mindistance){
                    minnode = j;
                    mindistance = Dist[j];
                }
            }
            if(minnode == -1)
                break;
            Selected[minnode] = true;
            for(int j = 0; j < utils.PG.Node; j ++){
                if(!Selected[j]  && PGFreeBandwidth[minnode][j] >= 0){
                    if(Dist[minnode] + 1 < Dist[j]) {
                        Dist[j] = Dist[minnode] + 1;
                        Prev[j] = minnode;
                    }
                }
            }
        }
        if(Dist[des] == infi)
            return;
        Path.add(des);
        int temp = Prev[des];
        while(temp != -1){
            Path.add(temp);
            temp = Prev[temp];
        }
        Path.add(source);
        Collections.reverse(Path);
    }

    public boolean YenKsp(int source, int des, int K, List Path, double bandwidth){
        List A = new ArrayList<Store>();
        Queue<Store> B = new PriorityQueue<>(comparator);

        int tempdist[] = new int[utils.PG.Node];
        List temppath = new ArrayList<Integer>();
        boolean Selected[] = new boolean[utils.PG.Node];
        Dijkstra(source,des,tempdist,temppath,Selected);
        if(temppath.size() == 0)
            return false;
        else{
            boolean flag = CheckPathBD(temppath,bandwidth);
            if(flag == true){
                Path.addAll(temppath);
                return true;
            }
            else{
                A.add(new Store(temppath,tempdist,des));
            }
        }

        for(int k = 1; k <= K; k++){
            Store sp = (Store) A.get(k-1);
            List last_path = sp.Path;
            int last_dist[] = sp.Dist;

            //The spur node ranges from the first node to the next to last node in the previous k-shortest path.
            for(int i = 0; i < last_path.size() - 1; i ++){
                int spur_node = (Integer) last_path.get(i);
                List root_path = new ArrayList(last_path.subList(0,i+1));
                int root_distance = last_dist[spur_node];

                // Remove the links that are part of the previous shortest paths
                // which share the same root path.
                List remove_edges = new ArrayList<Edge>();
                for(int j = 0; j < A.size(); j ++){
                    List temp_path = ((Store)A.get(j)).Path;
                    if(i + 1 < temp_path.size() && IsSamePath(temp_path.subList(0,i+1),root_path)){
                        int from = spur_node;
                        int to = (Integer)(temp_path.get(i+1));
                        remove_edges.add(new Edge(from,to,PGFreeBandwidth[from][to]));

                        // for debug!
                        if(PGFreeBandwidth[from][to] == -1)
                            System.out.println("??????????????????????????????????????????????????");
                        // for debug!
                        PGFreeBandwidth[from][to] = -1;
                        PGFreeBandwidth[to][from] = -1;
                    }
                }
                // removed each node in root_path from Graph;
                Arrays.fill(Selected,false);
                for(int j = 0 ; j < root_path.size() - 1; j ++)
                    Selected[(Integer) (root_path.get(j))] = true;

                // Calculate the spur path from the spur node to the target
                int spur2tar_dist[] = new int[utils.PG.Node];
                List spur2tar_path = new ArrayList<Integer>();
                Dijkstra(spur_node,des,spur2tar_dist,spur2tar_path,Selected);

                if(spur2tar_path.size() != 0){
                    List Total_path = new ArrayList<>(root_path.subList(0,root_path.size()-1));
                    Total_path.addAll(spur2tar_path);

                    int Total_dist[] = new int[utils.PG.Node];
                    Arrays.fill(Total_dist,infi);
                    for(int j = 0; j < root_path.size(); j ++)
                        Total_dist[(Integer)root_path.get(j)] = last_dist[(Integer)root_path.get(j)];
                    for(int j = 1; j < spur2tar_path.size(); j ++)
                        Total_dist[(Integer)spur2tar_path.get(j)] = root_distance + spur2tar_dist[(Integer)spur2tar_path.get(j)];

                    if(CheckPathBD(Total_path,bandwidth) && !CheckPathInB(Total_path,B)) {

                        B.offer(new Store(Total_path, Total_dist, des));
                    }
                }
                for(int j = 0; j < remove_edges.size(); j ++){
                    Edge temp = (Edge)remove_edges.get(j);
                    PGFreeBandwidth[temp.From][temp.to] = temp.bandwidth;
                    PGFreeBandwidth[temp.to][temp.From] = temp.bandwidth;
                }
            }
            if(B.size() == 0)
                return false;
            else{
                Store temp = B.poll();
                if(CheckPathBD(temp.Path,bandwidth)){
                    //Path.addAll(temp.Path);
                    for(int j = 0; j < temp.Path.size(); j++)
                        Path.add((temp.Path.get(j)));
                    return true;
                }
                else
                    A.add(temp);
            }
        }
        return false;
    }

    // check if a path already in B
    public  boolean CheckPathInB(List path, Queue<Store> B){
        if(B.size() == 0)
            return false;
        boolean Successed = false;
        for(Store s : B){
            List pathofb = s.Path;
            if(path.size() != pathofb.size())
                continue;
            else{
                boolean flag = false;
                for(int i = 0; i < path.size(); i ++){
                    if((Integer)(path.get(i)) != (Integer) (pathofb.get(i))){
                        flag = true;
                        break;
                    }

                }
                if(flag == false) {
                    Successed = true;
                    break;
                }
            }
        }
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

    public  boolean IsSamePath(List L1, List L2){
        if(L1.size() != L2.size())
            return false;
        boolean flag  = true;
        for(int i = 0; i < L1.size(); i ++){
            if(L1.get(i) != L2.get(i)){
                flag = false;
                break;
            }
        }
        return flag;
    }
    public boolean DeployVPath(int From, int To, double Bandwidth, int VEindex){
        if(From == To)
            return true;

        boolean Successed = true;
        List Path = new ArrayList<Integer>();
        if(YenKsp(From,To,Yen_K,Path,Bandwidth) == false)
            Successed = false;
        else{
            if(Path.isEmpty() || !CheckPathBD(Path,Bandwidth))
                return false;
            for(int i = 0 ; i < Path.size() - 1; i ++){
                int sour = (Integer) Path.get(i);
                int des = (Integer) Path.get(i + 1);
                PGFreeBandwidth[sour][des] -= Bandwidth;
                PGFreeBandwidth[des][sour] -= Bandwidth;
            }
            VE2PE[VEindex].addAll(Path);
            VEBandwidth[VEindex] = Bandwidth;
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
                // VEdge i didn't map or VEdge in a PNode
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
        //count ++;
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
        if(count == 10 ){
            for(int i = 0 ; i < utils.PG.Node; i ++){
                System.out.println("  "+(utils.PG.NodeCapacity[i] - PGFreeCapacity[i]));
            }
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1  ");
            for(int i = 0 ; i < utils.PG.Node; i ++){
                for (int j = i; j < utils.PG.Node; j ++)
                    if(utils.PG.EdgeCapacity[i][j] > 0)
                    System.out.println( i + "  " + j + "  "+(utils.PG.EdgeCapacity[i][j] - PGFreeBandwidth[i][j]));
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

    public Comparator<Store> comparator = new Comparator<Store>() {
        @Override
        public int compare(Store o1, Store o2) {
            return o1.Dist[o1.Des] - o2.Dist[o2.Des];
        }
    };

}
