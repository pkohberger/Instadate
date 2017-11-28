package com.quickblox.instadate.groupchatwebrtc.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.quickblox.instadate.core.ui.adapter.BaseSelectableListAdapter;
import com.quickblox.instadate.groupchatwebrtc.R;
import com.quickblox.users.model.QBUser;
import com.quickblox.instadate.groupchatwebrtc.utils.UsersUtils;

import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsAdapter extends BaseSelectableListAdapter<QBUser> {

    private SelectedItemsCountsChangedListener selectedItemsCountChangedListener;

    public OpponentsAdapter(Context context, List<QBUser> users) {
        super(context, users);
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_opponents_list, null);
            holder = new ViewHolder();
            holder.opponentIconWebView = (WebView) convertView.findViewById(R.id.image_opponent_icon_web_view);
            holder.opponentName = (TextView) convertView.findViewById(R.id.opponentsName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QBUser user = getItem(position);

        if (user != null) {

            String html = UsersUtils.getWebViewPortraitHTML(user);

            holder.opponentIconWebView.loadData(html, "text/html", null);

            /**
             * disable WebView scrolling
             */
            holder.opponentIconWebView.setScrollbarFadingEnabled(true);
            holder.opponentIconWebView.setVerticalScrollBarEnabled(false);
            holder.opponentIconWebView.setHorizontalScrollBarEnabled(false);

            holder.opponentName.setText(user.getFullName());

        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(position);
                selectedItemsCountChangedListener.onCountSelectedItemsChanged(selectedItems.size());
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        TextView opponentName;
        WebView opponentIconWebView;
    }

    public void setSelectedItemsCountsChangedListener(SelectedItemsCountsChangedListener selectedItemsCountsChanged){
        if (selectedItemsCountsChanged != null) {
            this.selectedItemsCountChangedListener = selectedItemsCountsChanged;
        }
    }

    public interface SelectedItemsCountsChangedListener{
        void onCountSelectedItemsChanged(int count);
    }
}
