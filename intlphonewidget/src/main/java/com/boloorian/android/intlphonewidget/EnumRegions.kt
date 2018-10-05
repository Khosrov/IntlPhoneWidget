package com.boloorian.android.intlphonewidget

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by khosrov on 09/05/2018.
 */
object EnumRegions {
    /***********************************************************
     *  Attributes
     **********************************************************/
    private var mRegions: Regions? = null


    /***********************************************************
     * Business method
     **********************************************************/
    /**
     * Read JSON from RAW resource
     *
     * @param context  Context
     * @param resource Resource int of the RAW file
     * @return JSON
     */
    private fun getJsonFromRaw(context: Context, resource: Int): String? {
        val json: String
        try {
            val inputStream = context.resources.openRawResource(resource)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }

    /**
     * Import RegionList from RAW JSON resource
     *
     * @param context Context
     * @return RegionList
     */
    fun getRegions(context: Context): Regions {
        var regionList = mRegions
        if (regionList != null) {
            return regionList
        }
        regionList = Regions()
        try {
            val countries = JSONArray(getJsonFromRaw(context, R.raw.regioninfo))
            for (i in 0 until countries.length()) {
                try {
                    val country = countries.get(i) as JSONObject
                    regionList.add(RegionItem(country.getString("name"), country.getString("iso2"), country.getInt("dialCode")))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return regionList
    }


    class Regions : ArrayList<RegionItem>() {
        /**
         * Read item index on the list by iso
         *
         * @param iso RegionItem's iso2
         * @return index of the item in the list
         */
        fun indexOfIso(iso: String): Int {
            for (i in 0 until this.size) {
                if (this[i].iso?.toUpperCase().equals(iso.toUpperCase())) {
                    return i
                }
            }
            return -1
        }

        /**
         * Read item index on the list by dial coder
         *
         * @param prefix RegionItem's dial code prefix
         * @return index of the item in the list
         */
        fun indexOfPrefix(prefix: Int): Int {
            for (i in 0 until this.size) {
                if (this[i].prefix == prefix) {
                    return i
                }
            }
            return -1
        }
    }
}