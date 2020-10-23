# PriceAnimationTextView

[![](https://jitpack.io/v/kimyounghoons/PriceAnimationTextView.svg)](https://jitpack.io/#kimyounghoons/PriceAnimationTextView)

<p align="center">
<img src="https://github.com/kimyounghoons/PriceAnimationTextView/blob/main/screenshot/animation.gif" width="400" height="800"/>
</p>

# usage
Add dependencies in build.gradle:
```
 dependencies {
       implementation 'com.github.kimyounghoons:PriceAnimationTextView:1.0.1'
    }
```
Or Maven:
```
<dependency>
  <groupId>com.github.kimyounghoons</groupId>
  <artifactId>PriceAnimationTextView</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```
## XML Layout
```
<com.kyh.priceanimationtextviewlib.PriceAnimationTextView
            android:id="@+id/priceAnimationTextView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="30dp"
            android:maxLength="10"
            android:orientation="horizontal"
            android:textColor="#000000"
            android:textSize="15sp"
            android:textStyle="bold"
            app:endText="원"
            app:hintColor="#cccccc"
            app:hintText="금액 입력"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent" />
```
### default values
```
textSize : 25px  
textStyle : bold  
textColor : #0000000  
endText : 원  
hintColor : #cccccc  
hintText : 금액 입력
```

## Kotlin
```
priceAnimationTextView.addText("1")
priceAnimationTextView.backButton()
```
