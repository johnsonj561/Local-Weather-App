package com.puttey.pustikins.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.puttey.pustikins.stormy.R;
import com.puttey.pustikins.stormy.Weather.Day;

/**
 * Created by Pustikins on 6/20/2015.
 */
public class DayAdapter extends BaseAdapter{

    private Context mContext;
    private Day[] mDays;

    /**
     * Constructor
     * @param context
     * @param days
     */
    public DayAdapter(Context context, Day[] days){
        mContext = context;
        mDays = days;
    }


    @Override
    public int getCount(){
        return mDays.length;
    }

    @Override
    public Object getItem(int position){
        return mDays[position];
    }

    @Override
    public long getItemId(int position){
        return 0;   //we won't be using this
                    //can be used to tag items for easy reference
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;


        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.temperatureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Day day = mDays[position];
        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(day.getTemperatureMax() + "");
        if(position == 0){
            holder.dayLabel.setText("Today");
        }
        else{
            holder.dayLabel.setText(day.getDayOfTheWeek());
        }

        return convertView;
    }

    private static class ViewHolder{
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;
    }
}
