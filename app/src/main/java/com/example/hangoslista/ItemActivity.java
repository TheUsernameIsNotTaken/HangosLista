package com.example.hangoslista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity
public class ItemActivity extends AppCompatActivity {

    @ViewById(R.id.itemDataText)
    TextView itemDataText;

    @ViewById(R.id.itemNameText)
    TextView itemNameText;

    @ViewById(R.id.itemPriceText)
    TextView itemPriceText;

    @ViewById(R.id.itemNameEditText)
    EditText itemNameEditText;

    @ViewById(R.id.itemPriceEditText)
    EditText itemPriceEditText;

    public final static String NAME_RESULT = "ABC_ITEMNAME";
    public final static String PRICE_RESULT = "ABC_ITEMPRICE";

    @Click(R.id.addItemButton)
    public void addItemClick() {
        String name = itemNameEditText.getText().toString();
        String price = itemPriceEditText.getText().toString();

        if(name.isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.empty_itemName), Toast.LENGTH_SHORT).show();
            return;
        }
        if(price.isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.empty_itemPrice), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent replyIntent = new Intent();
        replyIntent.putExtra(NAME_RESULT, name);
        replyIntent.putExtra(PRICE_RESULT, price);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    @Click(R.id.cancelItemButton)
    public void cancelItemClick() {
        Intent replyIntent = new Intent();
        setResult(RESULT_CANCELED, replyIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
    }
}