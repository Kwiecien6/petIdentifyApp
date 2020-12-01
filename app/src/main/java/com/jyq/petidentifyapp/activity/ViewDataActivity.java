package com.jyq.petidentifyapp.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;

import com.jyq.petidentifyapp.R;
import com.jyq.petidentifyapp.adapter.PetAdapter;
import com.jyq.petidentifyapp.db.DatabaseHelper;
import com.jyq.petidentifyapp.db.PetInfo;

import java.util.List;

public class ViewDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DatabaseHelper helper = new DatabaseHelper(this);
        List<PetInfo> pets = helper.query();
        helper.close();
        recyclerView.setAdapter(new PetAdapter(pets));

    }
}