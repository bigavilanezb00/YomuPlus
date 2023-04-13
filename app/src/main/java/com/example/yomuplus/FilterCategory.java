package com.example.yomuplus;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    //Creamos un ArrayList en al que queramos buscar
    ArrayList<ModelCategory> filterList;

    AdapterCategory adapterCategory;

    //constructor


    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filterModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //validamos
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)) {
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>) results.values;

        adapterCategory.notifyDataSetChanged();
    }
}
