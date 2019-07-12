package com.sorter.putrialutfi.sorter.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sorter.putrialutfi.sorter.MainActivity;
import com.sorter.putrialutfi.sorter.Model.Things;
import com.sorter.putrialutfi.sorter.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<Things> listThings;
    private Context context;
    MainActivity mainActivity;

    public RecyclerViewAdapter(ArrayList<Things> listThings, Context context, MainActivity main) {
        this.listThings = listThings;
        this.context = context;
        this.mainActivity = main;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final String nama_barang = listThings.get(position).getNama_barang();
        final String harga_ecer = listThings.get(position).getHarga_ecer();
        final String harga_resell = listThings.get(position).getHarga_resell();

        holder.namaBarang.setText(nama_barang);
        holder.hargaEcer.setText(harga_ecer);
        holder.hargaResell.setText(harga_resell);

        holder.listItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String actions[] = {"Update", "Delete"};
                AlertDialog.Builder alertDialog =  new AlertDialog.Builder(v.getContext());
                alertDialog.setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mainActivity.updateBarang(position);
                                break;
                            case 1:
                                mainActivity.deleteBarang(position);
                                break;
                        }
                    }
                });
                alertDialog.create();
                alertDialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listThings != null) {
            return listThings.size();
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView namaBarang, hargaEcer, hargaResell;
        private LinearLayout listItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            namaBarang = itemView.findViewById(R.id.nama_barang);
            hargaEcer = itemView.findViewById(R.id.harga_ecer);
            hargaResell = itemView.findViewById(R.id.harga_resell);
            listItem = itemView.findViewById(R.id.list_item);
        }
    }
}
