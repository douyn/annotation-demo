package com.dou.demo.annotation_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.dou.demo.knife_annotation.BindView;
import com.dou.demo.knife_annotation.OnClick;
import com.dou.demo.knife_api.Knife;

public class MainActivity extends AppCompatActivity {

    @BindView(id = R.id.tv_content)
    TextView tv_content;

    @OnClick(id = R.id.tv_content)
    public void onclick(){
        Toast.makeText(this, "ggggggg", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Knife.bind(this);
        tv_content.setText("hello knife");
    }
}
