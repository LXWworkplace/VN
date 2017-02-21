package com.lx;

import java.util.Comparator;

/**
 * Created by lx on 17-2-14.
 */
public class Pair {
    public double Capacity;
    public int Id;
    public static Comparator comparator = new Comparator<Pair>() {
        @Override
        public int compare(Pair o1, Pair o2) {
            if(o1.Capacity - o2.Capacity == 0)
                return 0;
            else if(o1.Capacity - o2.Capacity > 0)
                return -1;
            else
                return 1;
        }
    };

    public Pair(double capcity, int id) {
        Capacity = capcity;
        Id = id;
    }
}
