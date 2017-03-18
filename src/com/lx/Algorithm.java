package com.lx;

import java.io.*;

/**
 * Created by lx on 17-3-1.
 */
public class Algorithm {
    public double BDcost;               // the successed mapped link cost * dist
    public double BDfailcost;           // the unsuccessed mapped link cost

    public double VNmapped;
    public double Vlinkmapped;
    public double Vlinksum;

    public Algorithm(){
        BDcost = 0;
        BDfailcost = 0;
        VNmapped = 0;
        Vlinkmapped = 0;
        Vlinksum = 0;
    }

    public void Deploy(String log){

    }

    public void RestructVN(String path){

    }

    public double AddVNlog(){
        return -1;
    }

    public void KillLiveVN(String path){

    }
    public int PNodeUsed(){
        int res = 0;
        return res;
    }

    public int MaxPathLength(){
        int res = 0;
        return res;
    }

    // for test
    public static void main(String[] args) throws IOException {
        FileWriter file = new FileWriter("/home/lx/lsls",true);
        PrintWriter out = new PrintWriter(file);
        out.println("ahglka");
        out.close();
        file.close();

        FileWriter file1 = new FileWriter("/home/lx/lsls",true);
        PrintWriter in = new PrintWriter(file1);
        in.println("lixiong");
        in.close();
        file1.close();
    }
}
