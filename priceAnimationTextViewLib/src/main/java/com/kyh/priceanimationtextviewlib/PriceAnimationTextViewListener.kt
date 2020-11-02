package com.kyh.priceanimationtextviewlib

abstract class PriceAnimationTextViewListener {
    open fun onMessageOnlyInputDigit(){}
    open fun onMessageZeroStart(){}
    open fun onMessageMaxPrice(price : Int){}
    open fun onMessageMaxPrice(allPriceText : String){}
}