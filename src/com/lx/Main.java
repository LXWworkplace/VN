package com.lx;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        List B = new ArrayList<Store>();
        for(int i = 0; i < 2; i++){
            List total = new ArrayList();
            for(int j = 0; j <= i;j ++)
                total.add(j);
            int dist[] = new int[2];
            B.add(new Store(total,dist ,1));
        }
        int b = 0;
        int c= 0;
    }
}
