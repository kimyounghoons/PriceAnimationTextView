package com.kyh.priceanimationtextview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn1.setOnClickListener {
            priceAnimationTextView.addText("1")
        }
        btn2.setOnClickListener {
            priceAnimationTextView.addText("2")
        }

        btn3.setOnClickListener {
            priceAnimationTextView.addText("3")
        }
        btn4.setOnClickListener {
            priceAnimationTextView.backButton()
        }
    }
}