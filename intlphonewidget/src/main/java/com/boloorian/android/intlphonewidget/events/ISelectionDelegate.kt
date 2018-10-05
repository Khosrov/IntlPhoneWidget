package com.boloorian.android.intlphonewidget.events

import com.boloorian.android.intlphonewidget.RegionItem

/**
 * Created by khosrov on 09/05/2018.
 *
 * validation listener
 * To notify selected item
 */
interface ISelectionDelegate {
    fun onSelectRegion(regionItem: RegionItem)
}