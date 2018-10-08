package com.boloorian.android.intlphonelayout

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.boloorian.android.intlphonewidget.RegionItem
import com.boloorian.android.intlphonewidget.events.ISelectionDelegate
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ISelectionDelegate{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        input_layout.setOnSelectionListener(this)
    }

    override fun onSelectRegion(regionItem: RegionItem) {
      /*  if(input_layout.isValid){
            val nationalNum = input_layout.getNationalFormat()
        }*/

    }

}
