package com.lx;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        File file = new File("/home/lx/asdf");
        if(!file.exists())
        {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
