package com.android.productlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.productlist.Database.Product;
import com.android.productlist.Database.ProductDatabase;

public class ViewEntryActivity extends AppCompatActivity {

    TextView editProductTV, productIdTV, productNameTV, productDescTV, productPriceTV, productLocationTV;

    ProductDatabase pdtdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);

        String productName = getIntent().getStringExtra("productName");
        int uid = getIntent().getIntExtra("uid",0);
        getSupportActionBar().setTitle(productName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editProductTV = findViewById(R.id.editProductTV);
        productIdTV = findViewById(R.id.productIdTV);
        productNameTV = findViewById(R.id.productNameTV);
        productDescTV = findViewById(R.id.productDescTV);
        productPriceTV = findViewById(R.id.productPriceTV);
        productLocationTV = findViewById(R.id.locationTV);

        pdtdb = Room.databaseBuilder(getApplicationContext(),ProductDatabase.class,"product-database").allowMainThreadQueries().build();

        Product allproducts = pdtdb.productDao().loadAllByProductids(uid);

        productNameTV.setText(String.valueOf(allproducts.getProductname()));
        productDescTV.setText(String.valueOf(allproducts.getProductdescription()));
        productPriceTV.setText(String.valueOf(allproducts.getProductprice()));

        productLocationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewEntryActivity.this,MapsActivity.class);
                intent.putExtra("movable","false");
                intent.putExtra("productName",productName);

                System.out.println("latitude"+ allproducts.getLat());
                intent.putExtra("latitude",allproducts.getLat());
                intent.putExtra("longitude",allproducts.getLng());
                startActivity(intent);
            }
        });

        editProductTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewEntryActivity.this,ProductEntryActivity.class);
                intent.putExtra("uid",uid);
                intent.putExtra("productName",productName);
                startActivity(intent);
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}