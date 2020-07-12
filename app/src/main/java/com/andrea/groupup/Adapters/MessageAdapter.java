package com.andrea.groupup.Adapters;

import com.andrea.groupup.Constants;
import com.andrea.groupup.Models.MemberData;
import com.andrea.groupup.Models.Message;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.andrea.groupup.R;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.println;

public class MessageAdapter extends BaseAdapter{
    List<Message> messages = new ArrayList<Message>() ;
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }


    public void add(Message message ) {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) {
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else {
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

            holder.name.setText(message.getMemberData().getName());
            holder.messageBody.setText(message.getText());
            Drawable d = new BitmapDrawable(context.getResources(), message.getBitmap());
            holder.avatar.setBackground(d);

            //download image
            //Picasso.get().load(Constants.BASE_URL + "/" + message.getMemberData().getColor()).into( (ImageView) convertView.findViewById(R.id.avatar));
            /*GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            drawable.setColor(Color.parseColor(message.getMemberData().getColor()));*/
        }

        return convertView;
    }

}

class MessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
}