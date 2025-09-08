package com.example.kamera

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView


class ListAdapter(val ctx: Context, val myDataList: List<ListData>): BaseAdapter(){
    override fun getCount(): Int {
        return myDataList.size
    }

    override fun getItem(position: Int): Bitmap? {
        return myDataList[position].image
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var myConvertView = convertView
        if (myConvertView == null) {
            myConvertView = LayoutInflater.from(ctx).inflate(R.layout.list_item, parent, false)
        }
        val currentItem = getItem(position)
        val imageItem = myConvertView?.findViewById<ImageView>(R.id.list_image_view)

        imageItem?.setImageBitmap(currentItem)

        return myConvertView!!

    }
}