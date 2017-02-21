package com.lx;

import java.util.List;

/**
 * Created by lx on 17-2-17.
 */
public class Store {
    public List Path;
    public int Dist[];
    int Des;

    public Store(List path, int[] dist, int des) {
        Path = path;
        Dist = dist;
        Des = des;
    }
}
