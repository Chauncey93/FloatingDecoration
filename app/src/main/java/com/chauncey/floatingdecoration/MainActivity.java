package com.chauncey.floatingdecoration;

import android.content.ClipData;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Item> items;
    private Drawable mDividingLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Item item = new Item();
            item.setName("Item" + i);
            item.setTitle("Group 1");
            items.add(item);
        }
        for (int i = 0; i < 7; i++) {
            Item item = new Item();
            item.setName("Item" + i);
            item.setTitle("Group 2");
            items.add(item);
        }
        for (int i = 0; i < 8; i++) {
            Item item = new Item();
            item.setName("Item" + i);
            item.setTitle("Group 3");
            items.add(item);
        }
        for (int i = 0; i < 9; i++) {
            Item item = new Item();
            item.setName("Item" + i);
            item.setTitle("Group 4");
            items.add(item);
        }
        RecyclerView recyclerView = new RecyclerView(this);
        RecyclerView.LayoutParams lp =
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(lp);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingDecoration decoration = new FloatingDecoration(this, new FloatingDecoration.DecorationCallback() {
            @Override
            public String getGroupLabel(int position) {
                return items.get(position).getTitle();
            }
        });

        recyclerView.addItemDecoration(decoration);

        int[] attr = new int[]{android.support.v7.appcompat.R.attr.dividerHorizontal};
        TypedArray array = this.obtainStyledAttributes(attr);
        mDividingLine = array.getDrawable(0);
        array.recycle();

        decoration.setDividingLine(mDividingLine);


        recyclerView.setAdapter(new Adapter(this, items));
        setContentView(recyclerView);
    }

}
