package com.boloorian.android.intlphonewidget.adapter

/**
 * Created by khosrov on 09/05/2018.
 */
import android.content.Context;
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.boloorian.android.intlphonewidget.R
import com.boloorian.android.intlphonewidget.RegionItem
import com.boloorian.android.intlphonewidget.adapter.holder.ViewHolder


class RegionSpinnerAdapter
/**
 * Constructor
 *
 * @param context Context
 */
(context: Context,mode:Int) : ArrayAdapter<RegionItem>(context, 0) {
    private val mLayoutInflater: LayoutInflater
    private var mode:Int?

    init {
        mLayoutInflater = LayoutInflater.from(context)
        this.mode = mode
    }

    companion object {
        val FLAG_VIEW = 0
        val ISO_VIEW = 1
        val COMPOSIT_VIEW =2
    }
    /**
     * Drop down item view
     *
     * @param position    position of item
     * @param convertView View of item
     * @param parent      parent view of item's view
     * @return covertView
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item_content, parent, false)
            viewHolder = ViewHolder()
            viewHolder.mImageView = convertView!!.findViewById(R.id.imv_country_flag) as ImageView
            viewHolder.mNameView = convertView.findViewById(R.id.txv_country_label) as TextView
            viewHolder.mDialCode = convertView.findViewById(R.id.txv_country_prefix) as TextView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val country = getItem(position)
        viewHolder.mImageView!!.setImageResource(getFlagResource(country))
        viewHolder.mNameView!!.text = country!!.name
        viewHolder.mDialCode!!.text = String.format("+%s", country.prefix)
        return convertView
    }

    /**
     * Drop down selected view
     *
     * @param position    position of selected item
     * @param convertView View of selected item
     * @param parent      parent of selected view
     * @return convertView
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val country = getItem(position)
        when(mode){
            FLAG_VIEW ->{
                if (convertView == null) {
                    convertView =  ImageView(context)
                }
                (convertView as ImageView).setImageResource(getFlagResource(country))
            }
            ISO_VIEW ->{
                if (convertView == null) {
                    convertView =  TextView(context)
                }
                (convertView as TextView).setText(country.iso)
            }
            COMPOSIT_VIEW ->{
                var apiv:AppCompatImageView?=null
                var aptv: AppCompatTextView?=null
                if (convertView == null) {
                    convertView =  getCompositView(parent);
                    apiv = convertView.findViewById(R.id.flag_id)
                    aptv = convertView.findViewById(R.id.iso_id)
                }
                apiv?.setImageResource(getFlagResource(country))
                aptv?.setText(country.iso)
            }
        }


        return convertView!!
    }

    /**
     * Create the Composit view in the case of COMPOSIT_VIEW
     * @param parent      parent of view
     * @return compositView
     * notice composit view refere to combonation of Flag & ISO view
     */
    fun getCompositView(parent: ViewGroup):View{
        var layoutInflater=LayoutInflater.from(this.context);
        return layoutInflater.inflate(R.layout.composit_view_content, parent, false)
    }


    /**
     * Fetch flag resource by RegionItem for mode = flag_view
     *
     * @param country RegionItem
     * @return int of resource | 0 value if not exists
     */
    private fun getFlagResource(country: RegionItem?): Int {
        return context.resources.getIdentifier("country_" + country!!.iso!!.toLowerCase(), "drawable", context.packageName)
    }

}