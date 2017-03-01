package com.lx;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by lx on 17-3-1.
 */
public class Controller {
    public ReqGenerator RG;

    public Controller(ReqGenerator RG) {
        this.RG = RG;
    }

    public void Mapping(int op) throws FileNotFoundException {
        Algorithm algorithm;
        Utils utils = new Utils();
        File file = new File(RG.OutputRGresult);
        Scanner scanner = new Scanner(file);
        String line;
        if(scanner.hasNextLine())
            line = scanner.nextLine();
        else {
            System.out.println("when read from outputRGresult, it is null");
            return;
        }
        String[] linearray = line.split(" ");
        utils.setPGPath((RG.PGfolderpath+"/" + RG.PGfile));
        utils.setVGPath((RG.VGfolderpath+"/" + linearray[1]));
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
        algorithm.Deploy();
        algorithm.AddVNlog();
        while(scanner.hasNextLine()){
            line = scanner.nextLine();
            linearray = line.split(" ");
            // add VN
            if(linearray[0].equals("add")){
                algorithm.RestructVN((RG.VGfolderpath+"/" + linearray[1]));
                algorithm.Deploy();
                algorithm.AddVNlog();
            }
            // kill VN
            else{
                String logpath = "home/lx/VN/VNlog/" + linearray[1];
                algorithm.KillLiveVN(logpath);
            }
        }

    }
}
