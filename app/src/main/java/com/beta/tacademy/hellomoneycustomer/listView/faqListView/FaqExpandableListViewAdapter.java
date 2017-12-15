package com.beta.tacademy.hellomoneycustomer.listView.faqListView;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beta.tacademy.hellomoneycustomer.R;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.FAQRecyclerView.FAQRecyclerViewAdapter;
import com.beta.tacademy.hellomoneycustomer.recyclerViews.FAQRecyclerView.FAQValueObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FaqExpandableListViewAdapter extends BaseExpandableListAdapter {
    Context mContext;
    ArrayList<FAQValueObject> mFaqValueObjectArrayList;

    public FaqExpandableListViewAdapter(Context context, ArrayList<FAQValueObject> faqValueObjectArrayList) {
        mContext = context;
        mFaqValueObjectArrayList = faqValueObjectArrayList;
    }

    public void initItem( ArrayList<FAQValueObject> faqValueObjectArrayList){
        mFaqValueObjectArrayList = faqValueObjectArrayList;
        notifyDataSetChanged();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mFaqValueObjectArrayList.get(groupPosition).getContent();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    //ChildView에 데이터 뿌리기
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.faq_child_listview_item, null);
        } else {
            view = convertView;
        }

        TextView content = (TextView)view.findViewById(R.id.content);
        content.setText(mFaqValueObjectArrayList.get(groupPosition).getContent());
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mFaqValueObjectArrayList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mFaqValueObjectArrayList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //GroupView에 데이터 뿌리
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.faq_parent_listview_item, null);
        } else {
            view = convertView;
        }

        ImageView expandToggle = (ImageView)view.findViewById(R.id.expandToggle);
        TextView title = (TextView)view.findViewById(R.id.title);

        title.setText(mFaqValueObjectArrayList.get(groupPosition).getTitle());

        if(isExpanded){
            expandToggle.setImageDrawable(mContext.getResources().getDrawable(R.drawable.expendable_open));
            title.setTypeface(null, Typeface.BOLD);
        }else{
            expandToggle.setImageDrawable(mContext.getResources().getDrawable(R.drawable.expendable_close));
            title.setTypeface(null, Typeface.NORMAL);
        }

        return view;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        // TODO Auto-generated method stub
        return super.areAllItemsEnabled();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }
}
