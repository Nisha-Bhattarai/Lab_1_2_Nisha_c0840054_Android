package com.android.productlist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.productlist.Database.Product;
import com.android.productlist.Database.ProductDatabase;

import java.util.List;


public class ProductEntryActivity extends AppCompatActivity {

    EditText productIdET, productNameET, productDescET, productPriceET;
    TextView locationTV, saveProductTV, renameProductTV;

    Double latitude = 1000.0;
    Double longitude = 1000.0;

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                        latitude = data.getDoubleExtra("latitude",0);
                        longitude = data.getDoubleExtra("longitude",0);

                        System.out.println(latitude+"/"+longitude);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_entry);

        getSupportActionBar().setTitle("Product Entry");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productIdET = findViewById(R.id.productIdET);
        productNameET = findViewById(R.id.productNameET);
        productDescET = findViewById(R.id.productDescET);
        productPriceET = findViewById(R.id.productPriceET);
        locationTV = findViewById(R.id.locationTV);
        saveProductTV = findViewById(R.id.saveProductTV);
        renameProductTV = findViewById(R.id.renameProductTV);

        ProductDatabase pdtdb = Room.databaseBuilder(getApplicationContext(),ProductDatabase.class,"product-database").allowMainThreadQueries().build();

        String productName = getIntent().getStringExtra("productName");
        int prodId = getIntent().getIntExtra("uid",0);

        if(prodId!=0)
        {
            Product product = pdtdb.productDao().loadAllByProductids(prodId);
            productIdET.setText(String.valueOf(prodId));
            productIdET.setFocusable(false);
            productNameET.setText(String.valueOf(productName));
            productDescET.setText(String.valueOf(product.getProductdescription()));
            productPriceET.setText(String.valueOf(product.getProductprice()));
            latitude = product.getLat();
            longitude = product.getLng();


            System.out.println(latitude);

            getSupportActionBar().setTitle("Edit item : "+productName);
        }

        locationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(ProductEntryActivity.this, MapsActivity.class);
                    intent.putExtra("movable","true");
                    intent.putExtra("uid",prodId);

                    intent.putExtra("latitude",latitude);
                    intent.putExtra("longitude",longitude);
                    someActivityResultLauncher.launch(intent);


            }
        });

        saveProductTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> uid = pdtdb.productDao().getallids();
                System.out.println(uid);
                if (productIdET.getText().toString().equals("")) {
                    productIdET.setError("Enter a Product Id");
                } else if (productNameET.getText().toString().equals("")) {
                    productNameET.setError("Enter a Product Name");
                } else if (productDescET.getText().toString().equals("")) {
                    productDescET.setError("Enter a Product Description");
                } else if (productPriceET.getText().toString().equals("")) {
                    productPriceET.setError("Enter a Product Price");
//                } else if (latitude == 1000.0 || longitude == 1000.0) {
//                    Toast.makeText(getApplicationContext(), "Please select location", Toast.LENGTH_LONG).show();
                }
                else if(prodId == 0 && uid.contains(Integer.parseInt(productIdET.getText().toString())))
                {
                    productIdET.setError("Product Id already exists, please enter a new id");
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProductEntryActivity.this);
                    alert.setMessage("Product Id already exists, please enter a new id");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.create().show();
                }
                else {

                    Product product = new Product(Integer.parseInt(productIdET.getText().toString()),
                            productNameET.getText().toString(), productDescET.getText().toString(),
                            Double.parseDouble(productPriceET.getText().toString()),
                            latitude, longitude);

                    AlertDialog.Builder alert = new AlertDialog.Builder(ProductEntryActivity.this);

                    if(prodId!=0)
                    {
                        pdtdb.productDao().updateProduct(product);
                        alert.setMessage("Successfully updated the product");
                    }
                    else {
                        pdtdb.productDao().insertProduct(product);
                        alert.setMessage("Successfully added the product");
                    }
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            Intent intent = new Intent(ProductEntryActivity.this,ProductListActivity.class);
                            startActivity(intent);
                        }
                    });
                    alert.create().show();
                }
            }
        });

        renameProductTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ProductEntryActivity.this);
                alert.setTitle("Cancel ?");
                alert.setMessage("Are you sure you want to cancel?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.create().show();
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