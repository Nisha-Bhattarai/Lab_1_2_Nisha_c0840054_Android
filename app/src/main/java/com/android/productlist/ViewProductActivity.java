package com.android.productlist;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.productlist.Adapters.productAdapter;
import com.android.productlist.Database.Product;
import com.android.productlist.Database.ProductDatabase;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ViewProductActivity extends AppCompatActivity {

    SearchView productSearchView;
    RecyclerView productRecyclerView;
    ConstraintLayout productViewParentCL;

    List<String> productName = new ArrayList<String>();
    List<Double> productPrice = new ArrayList<Double>();
    List<Integer> productUid = new ArrayList<Integer>();

    ProductDatabase productDB;
    productAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        // Define ActionBar object
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#4169e1"));

        // Set BackgroundDrawable
        actionBar.setBackgroundDrawable(colorDrawable);

        String provider_name = getIntent().getStringExtra("provider_name");
        getSupportActionBar().setTitle(provider_name.toUpperCase()); // setting activity's appbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // setting home button to appbar

        //Initializing all Views and ViewGroups
        productSearchView = findViewById(R.id.productSearchView);
        productRecyclerView = findViewById(R.id.productRecyclerView);
        productViewParentCL = findViewById(R.id.productViewParentCL);

        // initializing room database
        productDB = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class,"product-database").allowMainThreadQueries().build();

        if(productUid.size()>0) {

            // create layout manager for recyclerview
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

            // initialize Product Adapter
            productAdapter = new productAdapter(productName, productPrice, productUid);

            // set Adapter to recyclerview
            productRecyclerView.setAdapter(productAdapter);

            // set layout manager to recyclerview
            productRecyclerView.setLayoutManager(layoutManager);

            // set search functionality to Product recyclerview
            productSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    productAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    productAdapter.getFilter().filter(newText);
                    return false;
                }
            });

            //deleting product on swipe
            ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    TextView titleTV = viewHolder.itemView.findViewById(R.id.titleTV);
                    TextView subtitleTV = viewHolder.itemView.findViewById(R.id.subtitleTV);
                    TextView idTV = viewHolder.itemView.findViewById(R.id.productIDTV);
                    int userIdTV = Integer.parseInt(idTV.getText().toString());
                    String prodName = titleTV.getText().toString();
                    Double prodPrice = Double.valueOf(subtitleTV.getText().toString().replace("Price : $",""));

                    Product product = productDB.productDao().loadAllByProductids(userIdTV);

                    productDB.productDao().deleteproductbyuid(userIdTV);

                    productName.remove(prodName);
                    productPrice.remove(prodPrice);
                    productUid.remove(Integer.valueOf(userIdTV));

                    final int index = viewHolder.getAdapterPosition();

                    productAdapter.notifyItemRemoved(index);

                    Snackbar.make(productViewParentCL,"Product Deleted: "+prodName,5000).setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            productDB.productDao().insertProduct(product);
                            productName.add(index,prodName);
                            productPrice.add(index,prodPrice);
                            productUid.add(index,userIdTV);

                            productAdapter.notifyItemInserted(index);

                        }
                    }).show();

                }
            };

            // attaching item touch helper to recyclerview
            ItemTouchHelper ithprod = new ItemTouchHelper(itemTouchHelper);
            ithprod.attachToRecyclerView(productRecyclerView);



        }
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