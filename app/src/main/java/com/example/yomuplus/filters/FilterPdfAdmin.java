package com.example.yomuplus.filters;

import android.widget.Filter;

import com.example.yomuplus.adapters.AdapterCategory;
import com.example.yomuplus.adapters.AdapterPdfAdmin;
import com.example.yomuplus.models.ModelCategory;
import com.example.yomuplus.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    //Creamos un ArrayList en al que queramos buscar
    ArrayList<ModelPdf> filterList;

    AdapterPdfAdmin adapterPdfAdmin;

    //constructor


    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filterModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //validamos
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
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
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        adapterPdfAdmin.notifyDataSetChanged();
    }
}
