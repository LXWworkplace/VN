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

    public void NewGenerate(){
        int Ratio = (int) Math.pow(10,Case);
        int maxVnet = MaxVnetNum;
        File file ;
        PrintWriter out = null;
        try {
            file = new File(OutputRGresult);
            out = new PrintWriter(file);
        }catch (Exception e){
            System.out.println("Generate exception !!");
            e.printStackTrace();
        }
        boolean BornWidow[] = new boolean[1000];
        int BornBrite[] = new int[1000];
        boolean KillWidow[] = new boolean[1000];
        List KillBrite[] = new List[1000];
        // avoid maped same VN two times;
        List<Integer> mappedVN = new ArrayList();
        for (int i = 0; i < 1000;i ++)
            KillBrite[i] = new ArrayList<Integer>();
        int loop = 0;
        int finalloopcount = 20;
        double lamda = 3;
        Random random = new Random();
        int Vnet = 0;
        while(maxVnet > Vnet){
            int randomnum = getPossionVariable(lamda);
            while (randomnum >= 20)
                randomnum = getPossionVariable(lamda);
            for(int i = 0; i < randomnum; i ++){
                int index = random.nextInt(20);
                while(BornWidow[loop*20+index] == true)
                    index = random.nextInt(20);
                BornWidow[loop*20+index] = true;

                //generate live time
                int Livetime = getExponentialVariable(2);
                while(Livetime > 40)
                    Livetime = getExponentialVariable(2);
                KillWidow[loop*20+index+Livetime] = true;

                //generate VNet topology
                int first = random.nextInt(9) + 1;
                boolean second = random.nextBoolean();
                int VNsize = first * Ratio;
                if(second)
                    VNsize = first * Ratio + 5 * Ratio / 10;
                while(mappedVN.contains(VNsize)){
                    first = random.nextInt(9) + 1;
                    second = random.nextBoolean();
                    VNsize = first * Ratio;
                    if (second)
                        VNsize = first * Ratio + 5 * Ratio / 10;
                }
                BornBrite[loop*20+index] = VNsize;
                KillBrite[loop*20+index+Livetime].add(VNsize);
                mappedVN.add(VNsize);
            }
            loop ++;
            Vnet += randomnum;
        }
        for(int i = 0; i < 1000; i ++){
            if(BornWidow[i]){
                int VNsize = BornBrite[i];
                out.println("add " + VNsize + ".brite");
            }
            if(KillWidow[i]){
                for(int j = 0; j < KillBrite[i].size(); j ++) {
                    int VNsize = (Integer) KillBrite[i].get(j);
                    out.println("kill " + VNsize + ".brite");
                }
            }
        }
        out.close();

    }

    // generate Possion random num
    private static int getPossionVariable(double lamda) {

        int x = 0;
        double y = Math.random(), cdf = getPossionProbability(x, lamda);
        while (cdf < y) {
            x++;
            cdf += getPossionProbability(x, lamda);
        }
        return x;
    }
    private static double getPossionProbability(int k, double lamda) {
        double c = Math.exp(-lamda), sum = 1;
        for (int i = 1; i <= k; i++) {
            sum *= lamda / i;
        }
        return sum * c;
    }

    // generate Exponential random num
    public int getExponentialVariable (double lambda) {
        Random random = new Random();
        double u = random.nextDouble();

        int x = 0;
        double cdf = 0;
        while (u >= cdf) {
            x ++;
            cdf = 1 - Math.exp(-1.0 * lambda * x / 20);
        }
        return x;
    }

    public static void main(String[] args){
        ReqGenerator rg = new ReqGenerator(2,10,4,"200.brite","/home/lx/Brite/PNet","/home/lx/Brite/VNet","/home/lx/VN/RGoutput/RGresult.txt");
        try{
            rg.NewGenerate();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
