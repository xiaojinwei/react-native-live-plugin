package com.baianju.live_plugin.widget;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baianju.live_plugin.R;

import java.util.List;

public class MoreBottomSheetDialog extends BaseSheetDialogFragment implements View.OnClickListener {

    private ListView listView;
    private Adapter adapter;


    /**
     * 设置数据
     * @param data
     * @param selectedIndex 等于-1，不支持选中
     */
    public void setListData(List<String> data,int selectedIndex){
        adapter = new Adapter(data,selectedIndex);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.dialog_live_base_list;
    }

    @Override
    public void initView(Bundle savedInstanceState, View rootView) {
        rootView.findViewById(R.id.tv_live_cancel).setOnClickListener(this);
        listView = rootView.findViewById(R.id.live_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mOnItemClickListener != null && adapter != null){
                    mOnItemClickListener.onItemClick(adapter.data,position,adapter.selectedIndex);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_live_cancel){
            dismiss();
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(List<String> data,int position,int selectedIndex);
    }

    public void setOnItemClickListener(OnItemClickListener l){
        this.mOnItemClickListener = l;
    }

    public static class Adapter extends BaseAdapter{

        private List<String> data;
        private int selectedIndex;

        public Adapter(List<String> data, int selectedIndex) {
            this.data = data;
            this.selectedIndex = selectedIndex;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(parent.getContext(),R.layout.item_list_live_item,null);
            TextView tv_live_item = view.findViewById(R.id.tv_live_item);
            ImageView iv_live_choose = view.findViewById(R.id.iv_live_choose);
            if(position == selectedIndex){
                iv_live_choose.setVisibility(View.VISIBLE);
            }else{
                iv_live_choose.setVisibility(View.GONE);
            }
            tv_live_item.setText(getItem(position));
            return view;
        }
    }

}
