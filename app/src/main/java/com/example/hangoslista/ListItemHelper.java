package com.example.hangoslista;

import java.util.Comparator;

class ListItemHelper{

    enum SortState{
        BY_ID,
        BY_NAME,
        BY_PRICE;

        private static SortState[] vals = values();
        public SortState next()
        {
            return vals[(this.ordinal()+1) % vals.length];
        }
    }

    static class IdComparator implements Comparator<ListItem> {
        @Override
        public int compare(ListItem o1, ListItem o2) {
            int val = Integer.compare(o1.getItemId(), o2.getItemId());
            if(val != 0)    return val;
            val = o1.getItemName().compareTo(o2.getItemName());
            if(val != 0)    return val;
            return Integer.compare(o1.getItemPrice(), o2.getItemPrice());
        }
    }

    static class NameComparator implements Comparator<ListItem>{
        @Override
        public int compare(ListItem o1, ListItem o2) {
            int val = o1.getItemName().compareTo(o2.getItemName());
            if(val != 0)    return val;
            val = Integer.compare(o1.getItemPrice(), o2.getItemPrice());
            if(val != 0)    return val;
            return Integer.compare(o1.getItemId(), o2.getItemId());
        }
    }

    static class PriceComparator implements Comparator<ListItem>{
        @Override
        public int compare(ListItem o1, ListItem o2) {
            int val = Integer.compare(o1.getItemPrice(), o2.getItemPrice());
            if (val != 0) return val;
            val = o1.getItemName().compareTo(o2.getItemName());
            if (val != 0) return val;
            return Integer.compare(o1.getItemId(), o2.getItemId());
        }
    }
}