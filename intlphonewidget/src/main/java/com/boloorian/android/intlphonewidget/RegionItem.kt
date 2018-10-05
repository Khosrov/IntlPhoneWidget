package com.boloorian.android.intlphonewidget

/**
 * Created by khosrov on 09/05/2018.
 */
class RegionItem
/**
 * Constructor
 *
 * @param name     String
 * @param iso      String of ISO2
 * @param dialCode int
 */
(name: String, iso: String, prefixCode: Int) {

    var name: String? = null
    var iso: String? = null
    var prefix: Int = 0


    init {
        this.name = name
        this.iso = iso.toUpperCase()
        this.prefix = prefixCode
    }

    /**
     * Check if equals
     *
     * @param o Object to compare
     * @return boolean
     */
    override fun equals(o: Any?): Boolean {
        return o is RegionItem && o.iso!!.toUpperCase() == this.iso!!.toUpperCase()
    }
}