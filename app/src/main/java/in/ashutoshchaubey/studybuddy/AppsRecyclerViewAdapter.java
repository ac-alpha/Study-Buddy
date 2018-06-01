package in.ashutoshchaubey.studybuddy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ashutoshchaubey on 02/06/18.
 */

public class AppsRecyclerViewAdapter extends RecyclerView.Adapter<AppsRecyclerViewAdapter.appViewHolder> {

    private ItemClickListener mClickListener;
    private LayoutInflater mInflater;
    private ArrayList<AppItem> mData;

    public AppsRecyclerViewAdapter(Context context, ArrayList<AppItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public AppsRecyclerViewAdapter.appViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.app_item,parent,false);
        return new appViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppsRecyclerViewAdapter.appViewHolder holder, int position) {
        holder.appName.setText(mData.get(position).getName().toString());
        holder.appIcon.setImageDrawable(mData.get(position).getIcon());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class appViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView appName;
        ImageView appIcon;

        public appViewHolder(View itemView) {
            super(itemView);
            appName = (TextView) itemView.findViewById(R.id.app_name);
            appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        }

        @Override
        public void onClick(View view) {
            if(mClickListener!=null){
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

}
