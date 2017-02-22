package com.lx;

import java.lang.management.MonitorInfo;
import java.util.*;

/**
 * Created by lx on 17-2-22.
 * main Algorithm is based on Link-opt
 */
public class BaseAlgorithm1 {
    public Utils utils;
    public double PGFreeCapacity[];
    public double PGFreeBandwidth[][];
    public int VN2PN[];
    public List VE2PE[];

    public BaseAlgorithm1(Utils utils) {
        this.utils = utils;
        PGFreeCapacity = new double[utils.PG.Node];
        PGFreeBandwidth = new double[utils.PG.Node][utils.PG.Node];
        VN2PN = new int[utils.VG.Node];
        Arrays.fill(VN2PN,-1);
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
        Queue<Link> Linkqueue = new PriorityQueue<>(Link.comparator);
        for(int i = 0; i < utils.VG.Node; i ++) {
            for (int j = i; j < utils.VG.Node; j++) {
                if (utils.VG.EdgeCapacity[i][j] > 0) {
                    Linkqueue.offer(new Link(i, j, utils.VG.EdgeCapacity[i][j]));
                }
            }
        }
        while(Linkqueue.isEmpty() == false){
            Link Linktmp = Linkqueue.poll();
            int i = Linktmp.From;
            int j = Linktmp.To;
            boolean Successed = false;
            List path = new ArrayList<Integer>();
            if(VN2PN[i] != -1 && VN2PN[j] != -1){
                Successed = TwoNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),path);
                if(Successed == false)
                    System.out.println("Type 1 failed  there is not enougn BD from " + VN2PN[i] + "  to  " + VN2PN[j]);
                else{
                    for(int k = 0; k < path.size() - 1; k ++){
                        int from = (Integer)path.get(k);
                        int to = (Integer)path.get(k+1);
                        PGFreeBandwidth[from][to] -= utils.VG.EdgeCapacity[i][j];
                        PGFreeBandwidth[to][from] -= utils.VG.EdgeCapacity[i][j];
                    }
                    System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 1");
                }
            }
            else if((VN2PN[i] == -1 && VN2PN[j] != -1) || (VN2PN[i] != -1 && VN2PN[j] == -1)){
                if(VN2PN[i] == -1)
                    Successed = OneNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),i,VN2PN[j],path);
                else
                    Successed = OneNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),j,VN2PN[i],path);
                if(Successed == false)
                    System.out.println("Type 2 failed  cant find appropriate path " +  i  +"  to  " + j);
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
                    System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 2");
                }
            }
            else{
                Link Plink[] = new Link[1];
                Successed = NoneNodeDeployed(new Link(i,j,utils.VG.EdgeCapacity[i][j]),Plink);
                if(Successed == false){

                    System.out.println("Type 3 failed  cant find appropriate path " +  i  +"  to  " + j);
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
                    System.out.println("Deploy Virtual Edge " + i + " " + j + "Successed!!!!!!!!!!!!!!!!!!!! by type 3");
                }

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

    public static void main(String[] args){
        TreeSet<Integer> queue = new TreeSet<>();
        queue.add(1);
        queue.add(3);
        queue.add(-2);



        for(Integer i : queue)
            System.out.println(i);
    }
}
