package com.lx;

import java.io.*;
import java.util.Scanner;

/**
 * Created by lx on 17-3-1.
 */
public class Controller {
    public ReqGenerator RG;
    public String ResultParent = "/home/lx/VN/Result/";

    public Controller(ReqGenerator RG) {
        this.RG = RG;
    }

    public double[] Mapping(int op, String Resutlt) throws FileNotFoundException {
        String ResultPath = Resutlt;
        double revenue = 0;
        Algorithm algorithm;
        Utils utils = new Utils();
        File file = new File(RG.OutputRGresult);
        Scanner scanner = new Scanner(file);
        String line;
        if(scanner.hasNextLine())
            line = scanner.nextLine();
        else {
            System.out.println("when read from outputRGresult, it is null");
            return new double[4];
        }
        String[] linearray = line.split(" ");
        utils.setPGPath((RG.PGfolderpath+"/" + RG.PGfile));
        utils.setVGPath((RG.VGfolderpath+"/" + linearray[1]));
        utils.ConstructVirtualGraph();
        utils.ConstructPhysicalGraph();
        switch (op){
            case 1:
                algorithm = new BaseAlgorithm(utils);
                break;
            case 2:
                algorithm = new BaseAlgorithm1(utils);
                break;
            case 3:
                algorithm = new ARAlgorithm(utils);
                break;
            case 4:
                algorithm = new Yen_ARAlgorithm(utils);
                break;
            default:
                algorithm = new MergeVNAlgorithm(utils);
        }
        algorithm.Deploy(ResultPath);
        revenue += algorithm.AddVNlog();
        while(scanner.hasNextLine()){
            line = scanner.nextLine();
            linearray = line.split(" ");
            // add VN
            if(linearray[0].equals("add")){
                algorithm.RestructVN((RG.VGfolderpath+"/" + linearray[1]));
                algorithm.Deploy(ResultPath);
                algorithm.AddVNlog();
            }
            // kill VN
            else{
                String logpath = "/home/lx/VN/VNlog/" + linearray[1];
                algorithm.KillLiveVN(logpath);
            }
        }

        double res[] = new double[4];
        //res[0] = algorithm.BDcost + algorithm.BDfailcost * algorithm.MaxPathLength();
        res[0] = algorithm.BDcost + algorithm.BDfailcost * 10;
        res[1] = revenue;
        res[2] = algorithm.VNmapped / 10;
        res[3] = algorithm.Vlinkmapped/algorithm.Vlinksum;
        return res;
    }

    public static void main(String[] args){
        int aCase = 2;
        int maxVnetNum = 10;
        int LimitVnet = 2;
        String PGfile = "200.brite";
        String PGfolderpath = "/home/lx/Brite/PNet";
        String VGfolderpath = "/home/lx/Brite/VNet";
        String outputRGresult = "/home/lx/VN/RGoutput/RGresult.txt";
        double res[] = new double[2];
        ReqGenerator RG = new ReqGenerator(aCase,maxVnetNum,LimitVnet,PGfile,PGfolderpath,VGfolderpath,outputRGresult);
        //RG.NewGenerate();
        RG.Generate();
        Controller controller = new Controller(RG);

        int loop = 5;
        for(int i = 1; i <= loop; i ++){
            String ResultFile;
            PrintWriter out = null;

            long StartTime = System.nanoTime();
            ResultFile = controller.ResultParent + "ResultWithOp"+ i +".txt";
            FileWriter file = null;
            try {
                file = new FileWriter(ResultFile,true);
                out = new PrintWriter(file);
                out.println("Execute with case "+ aCase + " With Op  "+ i);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                out.close();
                try {
                    file.close();
                } catch (IOException e) {
                    System.out.println("close file error in ADD title to result");
                    e.printStackTrace();
                }
            }

            // Execute
            try {
                res = controller.Mapping(i,ResultFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            long EndTime = System.nanoTime();
            double ExecuteTime = (EndTime - StartTime) / Math.pow(10,9);

            // ADD ExecuteTime, revenue to result
            try {
                file = new FileWriter(ResultFile,true);
                out = new PrintWriter(file);
                out.println();
                out.println("In this loop the revenue is   " +(res[1] / ExecuteTime)+ "   BDcost is   " + res[0] + "   ExecuteTime   "+ ExecuteTime + "  VNmappedratio  " + res[2] + "  VLinkmapped ratio  " + res[3]);
            }catch (Exception e){
                System.out.println("Add ExecutionTime occur a Exception");
                e.printStackTrace();
            }finally {
                out.close();
                try {
                    file.close();
                } catch (IOException e) {
                    System.out.println("close file error in ADD ExecuteTime, revenue to result");
                    e.printStackTrace();
                }
            }
        }


    }
}
