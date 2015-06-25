package com.puttey.pustikins.stormy.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.puttey.pustikins.stormy.R;
import com.puttey.pustikins.stormy.Weather.Hour;

/**
 * Created by Pustikins on 6/22/2015.
 */
public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder>{

    private Hour[] mHours;
    private Context mContext;

    public HourAdapter(Context context, Hour[] hours){
        mHours = hours;
        mContext = context;
    }

    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_list_item, parent, false);
        HourViewHolder viewHolder = new HourViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HourViewHolder holder, int position){
        holder.bindHour(mHours[position]);

    }

    @Override
    public int getItemCount(){
        return mHours.length;
    }




    public class HourViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{

        public TextView mTimeLabel;
        public TextView mSummary;
        public TextView mTemperature;
        public ImageView mIconImageView;

        public HourViewHolder(View itemView){
            super(itemView);
            mTimeLabel = (TextView) itemView.findViewById(R.id.timeLabel);
            mSummary = (TextView) itemView.findViewById(R.id.summaryLabel);
            mTemperature = (TextView) itemView.findViewById(R.id.temperatureLabel);
            mIconImageView = (ImageView) itemView.findViewById(R.id.iconImageView);

            itemView.setOnClickListener(this);
        }

        public void bindHour(Hour hour){
            mTimeLabel.setText(hour.getHour());
            mSummary.setText(hour.getSummary());
            mTemperature.setText(hour.getTemperature() + "");
            mIconImageView.setImageResource(hour.getIconId());
        }

        @Override
        public void onClick(View v){
            String time = mTimeLabel.getText().toString();
            String temperature = mTemperature.getText().toString();
            String summary = mSummary.getText().toString();
            String message = String.format("At %s it will be %s and %s",
                    time, temperature, summary);
        }

    }


}
