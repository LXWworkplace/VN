package com.lx;

import java.io.*;

/**
 * Created by lx on 17-3-1.
 */
public class Algorithm {


    public void Deploy(){

    }

    public void RestructVN(String path){

    }

    public double AddVNlog(){
        return -1;
    }

    public void KillLiveVN(String path){

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
