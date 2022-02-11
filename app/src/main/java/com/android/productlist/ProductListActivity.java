package com.android.productlist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.productlist.Adapters.productAdapter;
import com.android.productlist.Database.Product;
import com.android.productlist.Database.ProductDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    LinearLayout productLinearLayout;
    FloatingActionButton addProductFAB;
    SearchView productSearchView;
    ConstraintLayout productCL;

    RecyclerView productRV;
    productAdapter productAda;
    ProductDatabase productDB;

    List<String> productName = new ArrayList<String>();
    List<Double> productPrice = new ArrayList<Double>();
    List<Integer> uid = new ArrayList<Integer>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

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

        //Initializing all Views and ViewGroups
        productLinearLayout = findViewById(R.id.productLinearLayout);
        addProductFAB = findViewById(R.id.addProductFAB);
        productSearchView = findViewById(R.id.productSV);
        productCL = findViewById(R.id.productCL);

        productRV = findViewById(R.id.productRV);

        productLinearLayout.setVisibility(View.GONE); //hiding product layout

        productDB = Room.databaseBuilder(getApplicationContext(), ProductDatabase.class,"product-database").allowMainThreadQueries().build();

        populateData();

        System.out.println(productDB.productDao().getAll());

        productLinearLayout.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle("Product List"); // setting activity's appbar title

        loadProduct();


        addProductFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductListActivity.this,ProductEntryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(ProductListActivity.this);
        alert.setTitle("Exit ?");
        alert.setMessage("Are you sure you want to exit this Application ?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.create().show();
    }

    public void loadProduct()
    {
        // initializing room database
        productDB = Room.databaseBuilder(getApplicationContext(),ProductDatabase.class,"product-database").allowMainThreadQueries().build();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        // retrieving all product names from room database
        productName = productDB.productDao().getallproducts();

        // retrieving all products descriptions from room database
        productPrice = productDB.productDao().getallprodprice();

        // retrieving all product ids from room database
        uid = productDB.productDao().getallids();

        // Product Adapter initialize
        productAda = new productAdapter(productName, productPrice,uid);

        // set layout manager to Product recyclerview
        productRV.setLayoutManager(layoutManager);

        // set adapter to Product recyclerview
        productRV.setAdapter(productAda);

        // set search functionality to Product recyclerview
        productSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productAda.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAda.getFilter().filter(newText);
                return false;
            }
        });



        //swipe to delete the product
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
                TextView productIDTV = viewHolder.itemView.findViewById(R.id.productIDTV);
                int prodId = Integer.parseInt(productIDTV.getText().toString());
                String prodName = titleTV.getText().toString();
                Double prodPrice = Double.valueOf(subtitleTV.getText().toString().replace("Price : $",""));

                Product product = productDB.productDao().loadAllByProductids(prodId);

                productDB.productDao().deleteproductbyuid(prodId);

                productName.remove(prodName);
                productPrice.remove(prodPrice);
                uid.remove(Integer.valueOf(prodId));

                final int index = viewHolder.getAdapterPosition();

                productAda.notifyItemRemoved(index);

                Snackbar.make(productCL,"Product Deleted: "+prodName,5000).setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        productDB.productDao().insertProduct(product);
                        productName.add(index,prodName);
                        productPrice.add(index,prodPrice);
                        uid.add(index,prodId);

                        productAda.notifyItemInserted(index);

                    }
                }).show();

            }
        };

        // attaching item touch helper to product recyclerview
        ItemTouchHelper ithprod = new ItemTouchHelper(itemTouchHelper);
        ithprod.attachToRecyclerView(productRV);
    }

    //Populating the data
    public void populateData() {

        SharedPreferences pref = getSharedPreferences("pref_first", MODE_PRIVATE);
        int first = pref.getInt("first", 1);

        if (first == 1) {
            List<Product> products = new ArrayList<>();

            Product product = new Product(1, "Smart Plug",
                    "Amazon Smart Plug works with Alexa to add voice control to any outlet.",
                    74.98, 43.6532, -79.38322);
            products.add(product);

            product = new Product(2	,"Winter Dark: Audible's Thriller of the Year 2019"	," Master hacker, highly intelligent, combat trained: Winter is a very modern spy.",
                    3.99	,45.4215,	-75.6972);
            products.add(product);

            product = new Product(3	,"GearTOP Black Balaclava Ski Mask"	,"From UV coverage, to added cold weather protection, Our premium grade balaclavas are sure to give you the all weather protection you need!",
                    175.0,43.741667,	-79.373333);
            products.add(product);

            product = new Product(4	,"Aveeno Baby Soothing Baby Bath Treatment"	,"Relieves and soothes dry, itchy, irritated skin.",
                    11.94,43.6532	,-79.38322);
            products.add(product);

            product = new Product(5	,"L'Oreal Paris Voluminous Lash Mascara"	,"Volumizing And Lengthening Mascara: This volumizing and lengthening mascara."	,
                    10.99,49.2827,	-123.1207);
            products.add(product);

            product = new Product(6,	"Atomic Habits"	,"No matter your goals, Atomic Habits offers a proven framework for improving--every day."	,
                    16.99,51.0447	,-114.0719);
            products.add(product);

            product = new Product(	7,	"Gildan Men’s Fleece Sweatshirt","Longer dropped shoulder, straighter armhole, and wider, shorter sleeves.",
                    15.50,49.2827,	-123.1207);
            products.add(product);

            product = new Product(8,	"Arctix Womens Insulated Snow Pants"	,"85 grams ThermaTech Insulation offers warmth in a lightweight, low bulk garment."	,
                    15.0,51.0447,	-114.0719);
            products.add(product);

            product = new Product(9,	"Carhartt Men's Acrylic Watch Hat",	"Stretchable;The crown length is 13 1/4 inches."
                    ,50.0,49.260833,	-123.113889);
            products.add(product);

            product = new Product(10,	"Fossil Men's Machine Chronograph Watch"	,"Fossil has always been inspired by American creativity and ingenuity.",
                    147.82	,51.0447,-114.0719);
            products.add(product);

            productDB.productDao().insertProducts(products);

            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("first",0);
            editor.apply();
        }
    }
}