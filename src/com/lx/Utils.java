package com.lx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by lx on 17-2-14.
 */
public class Utils {
    public PhysicalGraph PG;
    public VirtualGraph VG;

    public String PGPath;
    public String VGPath;

    public double PGBandwidthMean = 10;
    public double PGBandwidthSquare = 4;

    public double PGCapacityMean = 1.2;
    public double PGCapacitySquare = 0.01;

    public double VGCapacityMean = 0.1;
    public double VGCapacitySquare = 0.01;

    public double VGBandwidthMean = 1;
    public double VGBandwidthSquare = 0.09;

    public String getPGPath() {
        return PGPath;
    }

    public void setPGPath(String PGPath) {
        this.PGPath = PGPath;
    }

    public String getVGPath() {
        return VGPath;
    }

    public void setVGPath(String VGPath) {
        this.VGPath = VGPath;
    }

    public Utils(){
        PG = new PhysicalGraph();
        VG = new VirtualGraph();
    }
    public void ConstructPhysicalGraph(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(PGPath));
            String temp = reader.readLine();
            while(temp.contains("Nodes:") == false)
                temp = reader.readLine();
            String Nodestr[] = temp.split(" ");
            PG.Node = Integer.parseInt(Nodestr[2]);
            PG.NodeCapacity = new double[PG.Node];
            for (int i = 0; i < PG.Node; i ++){
                temp = reader.readLine();
                PG.NodeCapacity[i] = NormRandom(PGCapacityMean,PGCapacitySquare);
            }
            while(temp.contains("Edges:") == false)
                temp = reader.readLine();
            String Edgestr[] = temp.split(" ");
            PG.Edge = Integer.parseInt(Edgestr[2]);
            PG.EdgeCapacity = new double[PG.Node][PG.Node];
            for(int i = 0; i < PG.Node; i ++)
                Arrays.fill(PG.EdgeCapacity[i],-1);
            for(int i = 0; i < PG.Edge; i ++){
                temp = reader.readLine();
                String line[] = temp.split("\\t");
                int from = Integer.parseInt(line[1]);
                int to = Integer.parseInt(line[2]);
                double capcity = NormRandom(PGBandwidthMean,PGBandwidthSquare);
                PG.EdgeCapacity[from][to] = capcity;
                PG.EdgeCapacity[to][from] = capcity;
            }
            reader.close();
            //System.out.println(""+PG.Node);
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("read from brite error!");
        }
        finally {
            if(reader != null) {
                try{
                    reader.close();
                }catch (IOException e1){

                }
            }
        }
    }

    public void ConstructVirtualGraph(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(VGPath));
            String temp = reader.readLine();
            while(temp.contains("Nodes:") == false)
                temp = reader.readLine();
            String Nodestr[] = temp.split(" ");
            VG.Node = Integer.parseInt(Nodestr[2]);
            VG.NodeCapacity = new double[VG.Node];
            for (int i = 0; i < VG.Node; i ++){
                temp = reader.readLine();
                VG.NodeCapacity[i] = NormRandom(VGCapacityMean,VGCapacitySquare);
            }
            while(temp.contains("Edges:") == false)
                temp = reader.readLine();
            String Edgestr[] = temp.split(" ");
            VG.Edge = Integer.parseInt(Edgestr[2]);
            VG.EdgeCapacity = new double[VG.Node][VG.Node];
            for(int i = 0; i < VG.Node; i ++)
                Arrays.fill(VG.EdgeCapacity[i],-1);
            for(int i = 0; i < VG.Edge; i ++){
                temp = reader.readLine();
                String line[] = temp.split("\\t");
                int from = Integer.parseInt(line[1]);
                int to = Integer.parseInt(line[2]);
                double capcity = NormRandom(VGBandwidthMean,VGBandwidthSquare);
                VG.EdgeCapacity[from][to] = capcity;
                VG.EdgeCapacity[to][from] = capcity;
            }
            reader.close();
            //System.out.println(""+PG.Node);
        }
        catch (IOException e){
            e.printStackTrace();
            System.out.println("read from brite error!");
        }
        finally {
            if(reader != null) {
                try{
                    reader.close();
                }catch (IOException e1){

                }
            }
        }
    }

    public double NormRandom(double mean, double square){
        Random random = new Random();
        double rand = Math.sqrt(square)* random.nextGaussian()+mean;
        while(rand <= 0 || rand < mean - 2 * Math.sqrt(square) || rand > mean + 2 * Math.sqrt(square))
            rand = Math.sqrt(square)* random.nextGaussian()+mean;
        return rand;
    }

    public static void main(String[] args) {
        Utils U = new Utils();
        U.PGPath = "/home/lx/graphs/200.brite";
        U.ConstructPhysicalGraph();
    }
}
