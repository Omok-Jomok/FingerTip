package com.example.fingertip;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchRecordAdapter extends RecyclerView.Adapter<SearchRecordAdapter.ViewHolder>{
    private ArrayList<SearchRecordItem> mSearchRecordList;
    private Context context;

    public SearchRecordAdapter(ArrayList<SearchRecordItem> mSearchRecordList, Context context){
        this.mSearchRecordList = mSearchRecordList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_record_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecordAdapter.ViewHolder holder, int position) {
        holder.onBind(mSearchRecordList.get(position));
        SearchRecordItem searchRecordItem = mSearchRecordList.get(position);
        holder.product_name.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int mPosition = holder.getAdapterPosition();

                Context context = view.getContext();
                Intent onlineMallActivity = new Intent(context, OnlineMallActivity.class);
                onlineMallActivity.putExtra("now_search_product",  mSearchRecordList.get(mPosition).getProduct());

                ((SearchCoupangActivity)context).startActivity(onlineMallActivity);
            }
        });
    }

    public void setSearchRecordList(ArrayList<SearchRecordItem> list){
        this.mSearchRecordList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSearchRecordList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView product_name;
        TextView search_day;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            product_name = (TextView) itemView.findViewById(R.id.tv_item_name);
            search_day = (TextView) itemView.findViewById(R.id.tv_item_day);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION)
                    {
                        // click event
                    }
                }
            });
        }

        void onBind(SearchRecordItem item){
            int DDay = item.getDay() + 31;
            product_name.setText("   " + item.getProduct());
            search_day.setText(item.getMonth() + "." + DDay);
        }
    }
}
