package com.example.unitalk;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import java.util.List;

public class MarketplaceAdapter extends RecyclerView.Adapter<MarketplaceAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    // Constructor to initialize the context and product list
    public MarketplaceAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create the ViewHolder
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Get the current product and bind it to the ViewHolder
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format("RM %.2f", product.getPrice())); // Use String.format here
        holder.username.setText(product.getUsername()); // Bind username

        // Load the product image using Glide
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_product_placeholder) // Placeholder while loading
                .into(holder.productImage);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SellerInfoActivity.class);
            intent.putExtra("sellerUsername", product.getUsername());
            intent.putExtra("productName", product.getName());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productDescription", product.getDescription());
            intent.putExtra("productImageUrl", product.getImageUrl());
            intent.putExtra("sellerLat", product.getSellerLat());
            intent.putExtra("sellerLng", product.getSellerLng());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the product list
        return productList.size();
    }

    // ViewHolder class that holds the views for each item
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView productName, productPrice, username; // Added username
        public ImageView productImage;

        public ProductViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.productName);
            productPrice = view.findViewById(R.id.productPrice);
            username = view.findViewById(R.id.username); // Initialize username
            productImage = view.findViewById(R.id.productImage);
        }
    }
}
