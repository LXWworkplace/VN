package com.lx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Random;

/**
 * Created by lx on 17-2-28.
 * generate a work file about how VNet arrival and killed;
 */
public class ReqGenerator {
    public int Case;                        // 1 or 2 ,3
    public int MaxVnetNum;                  // the scala of this mapping task;
    public int LimitVnet;                   // in this PG,limit num of Vnet simultaneously servered
    public String PGfile;
    public String PGfolderpath;             //   "/home/lx/Brite/PNet"
    public String VGfolderpath;             //  "/home/lx/Brite/VNet"
    public String OutputRGresult;           // "/home/lx/VN/RGoutput"

    public ReqGenerator(int aCase, int maxVnetNum, int limitVnet, String PGfile, String PGfolderpath, String VGfolderpath, String outputRGresult) {
        Case = aCase;
        MaxVnetNum = maxVnetNum;
        LimitVnet = limitVnet;
        this.PGfile = PGfile;
        this.PGfolderpath = PGfolderpath;
        this.VGfolderpath = VGfolderpath;
        OutputRGresult = outputRGresult;
    }

    public void Generate() throws FileNotFoundException {
        int Ratio = (int) Math.pow(10,Case);
        int maxVnet = MaxVnetNum;
        File file = new File(OutputRGresult);
        PrintWriter out = new PrintWriter(file);

        // already maped VN
        List AddedVN = new ArrayList<Integer>();
        // avoid maped same VN two times;
        List mappedVN = new ArrayList<Integer>();
        // Lived VN now
        List liveVN = new ArrayList<Integer>();
        Random random = new Random();
        int first = random.nextInt(9) + 1;
        boolean second = random.nextBoolean();
        int VNsize = first * Ratio;
        if(second == true)
            VNsize = first * Ratio + 5 * Ratio / 10;
        AddedVN.add(VNsize);
        mappedVN.add(VNsize);
        liveVN.add(VNsize);
        out.println("add " + VNsize + ".brite");
        maxVnet --;
        while(maxVnet > 0){
            int randomindex = random.nextInt(3);
            // add VN
            if(randomindex <=1){
                if(AddedVN.size() >= LimitVnet)
                    continue;
                do {
                    first = random.nextInt(9) + 1;
                    second = random.nextBoolean();
                    VNsize = first * Ratio;
                    if (second == true)
                        VNsize = first * Ratio + 5 * Ratio / 10;
                }while(mappedVN.contains(VNsize) == true);
                AddedVN.add(VNsize);
                mappedVN.add(VNsize);
                liveVN.add(VNsize);
                out.println("add " + VNsize + ".brite");
                maxVnet --;
            }
            // kill VN
            else{
                if(AddedVN.size() == 0)
                    continue;
                int size = AddedVN.size();
                int killedindex = random.nextInt(size);
                VNsize = (Integer) AddedVN.get(killedindex);
                AddedVN.remove(killedindex);
                out.println("kill " + VNsize + ".brite");
                liveVN.remove((Object)VNsize);
            }

        }
        while(!liveVN.isEmpty()){
            VNsize = (Integer) liveVN.remove(0);
            out.println("kill " + VNsize + ".brite");
        }
        out.close();

    }
    public static void main(String[] args){
        ReqGenerator rg = new ReqGenerator(2,10,4,"200.brite","/home/lx/Brite/PNet","/home/lx/Brite/VNet","/home/lx/VN/RGoutput/RGresult.txt");
        try{
            rg.Generate();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
