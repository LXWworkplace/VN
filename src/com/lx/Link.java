package com.lx;

import java.util.Comparator;

/**
 * Created by lx on 17-2-22.
 */
public class Link {
    public int From, To;
    public double Bandwidth;
    public static Comparator<Link> comparator = new Comparator<Link>() {
        @Override
        public int compare(Link o1, Link o2) {
            if(o1.Bandwidth - o2.Bandwidth == 0)
                return 0;
            else if(o1.Bandwidth - o2.Bandwidth > 0)
                return -1;
            else
                return 1;
        }
    };

    public Link(int from, int to, double bandwidth) {
        From = from;
        To = to;
        Bandwidth = bandwidth;
    }
}
