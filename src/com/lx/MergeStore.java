package com.lx;

import java.util.Comparator;

/**
 * Created by lx on 17-2-19.
 */
public class MergeStore {
    int Vnode;
    int Pnode;
    double Weight;

    public MergeStore(int vnode, int pnode, double weight) {
        Vnode = vnode;
        Pnode = pnode;
        Weight = weight;
    }
    public static Comparator<MergeStore> comparator = new Comparator<MergeStore>() {
        @Override
        public int compare(MergeStore o1, MergeStore o2) {
            if(o1.Weight - o2.Weight == 0)
                return 0;
            else if(o1.Weight - o2.Weight > 0)
                return -1;
            else
                return 1;
        }
    };
}
