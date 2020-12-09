package com.kyh.priceanimationtextview

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kyh.priceanimationtextviewlib.PriceAnimationTextViewListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        priceAnimationTextView.setPriceAnimationTextViewListener(object :
            PriceAnimationTextViewListener() {
            override fun onMessageOnlyInputDigit() {
                Toast.makeText(this@MainActivity, "숫자만 입력 가능 합니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onMessageZeroStart() {
                Toast.makeText(this@MainActivity, "0으로 시작 할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onMessageMaxPrice(price: Int) {
                //onMessageMaxPrice(price: Int),onMessageMaxPrice(allPriceText: String) 둘중 하나 사용
                //Toast.makeText(this@MainActivity, "최대 금액은 $price 입니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onMessageMaxPrice(allPriceText: String) {
                Toast.makeText(this@MainActivity, "최대 금액은 $allPriceText 입니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        btn0.setOnClickListener {
            if (it is TextView) {
                priceAnimationTextView.addText(it.text)
            }
        }
        btn1.setOnClickListener {
            if (it is TextView) {
                priceAnimationTextView.addText(it.text)
            }
        }
        btn2.setOnClickListener {
            if (it is TextView) {
                priceAnimationTextView.addText(it.text)
            }
        }
        btn4.setOnClickListener {
            priceAnimationTextView.backButton()
        }
        btn5.setOnClickListener {
            Toast.makeText(this, priceAnimationTextView.getText(), Toast.LENGTH_SHORT).show()
        }
        btn6.setOnClickListener {
            Toast.makeText(this, priceAnimationTextView.getAllText(), Toast.LENGTH_SHORT).show()
        }

        btn7.setOnClickListener {
            priceAnimationTextView.clear()
        }
    }
}