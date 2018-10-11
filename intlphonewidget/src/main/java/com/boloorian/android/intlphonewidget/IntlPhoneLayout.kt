package com.boloorian.android.intlphonewidget

/**
 * Created by khosrov on 09/05/2018.
 * The main
 */
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.text.TextUtils
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import com.boloorian.android.intlphonewidget.adapter.RegionSpinnerAdapter
import com.boloorian.android.intlphonewidget.adapter.RegionSpinnerAdapter.Companion.COMPOSIT_VIEW
import com.boloorian.android.intlphonewidget.adapter.RegionSpinnerAdapter.Companion.FLAG_VIEW
import com.boloorian.android.intlphonewidget.adapter.RegionSpinnerAdapter.Companion.ISO_VIEW
import com.boloorian.android.intlphonewidget.events.ISelectionDelegate
import com.boloorian.android.intlphonewidget.events.IntlPhoneInputListener

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;



import java.util.Locale;


class IntlPhoneLayout : RelativeLayout {

    /***********************************************************
     *  Attributes
     **********************************************************/
    private val DEFAULT_COUNTRY = Locale.getDefault().country
    private var mRegionSpinner: Spinner? = null
    private var mPhoneEdit: EditText? = null
    private var mRegionSpinnerAdapter: RegionSpinnerAdapter? = null
    private var mPhoneNumberWatcher = PhoneNumberWatcher(DEFAULT_COUNTRY)
    private val mPhoneUtil = PhoneNumberUtil.getInstance()

    private var mCountries: EnumRegions.Regions? = null
    private var mIntlPhoneInputListener: IntlPhoneInputListener? = null
    private var mDelaget: ISelectionDelegate? = null

    /**
     * Get selected country
     *
     * @return RegionItem
     */
    var selectedCountry: RegionItem? = null
        private set


    /**
     * Spinner listener
     */
    private val mRegionSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            selectedCountry = mRegionSpinnerAdapter!!.getItem(position)

            mPhoneEdit!!.removeTextChangedListener(mPhoneNumberWatcher)

            mPhoneNumberWatcher = PhoneNumberWatcher(selectedCountry!!.iso)

            mPhoneEdit!!.addTextChangedListener(mPhoneNumberWatcher)

            setHint()
            mDelaget?.onSelectRegion(selectedCountry!!)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    /**
     * Get number
     *
     * @return Phone number in E.164 format | null on error
     */
    /**
     * Set Number
     *
     * @param number E.164 format or national format
     */
    var number: String?
        get() {
            val phoneNumber = phoneNumber ?: return null

            return mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
        }
        set(number) = try {
            var iso: String? = null
            if (selectedCountry != null) {
                iso = selectedCountry!!.iso
            }
            val phoneNumber = mPhoneUtil.parse(number, iso)

            val regCodeForNumber = mPhoneUtil.getRegionCodeForNumber(phoneNumber);
            if(regCodeForNumber!=null) {
                val countryIdx = mCountries!!.indexOfIso(mPhoneUtil.getRegionCodeForNumber(phoneNumber))
                selectedCountry = mCountries!![countryIdx]
                mRegionSpinner!!.setSelection(countryIdx)
                mPhoneEdit!!.setText(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL))
            }else{
                mRegionSpinner!!.setSelection(0)
                mPhoneEdit!!.setText("")
            }


        } catch (ignored: NumberParseException) {
        }

    val text: String?
        get() = number

    /**
     * getNationalFormat
     * @return Local (national) format : ex: France mobile: 0684XXXXXX
     */
    fun getNationalFormat() : String{
        return PhoneNumberUtil.normalizeDigitsOnly(mPhoneEdit?.text.toString())
    }

    /**
     * setPrefix
     * select a country by prefix
     */
    fun setPrefix(prefix:Int) {
        val countryIdx = mCountries!!.indexOfPrefix(prefix)
        selectedCountry = mCountries!![countryIdx]
        mRegionSpinner!!.setSelection(countryIdx)
    }
    /**
     * Get PhoneNumber object
     * @return PhonenUmber | null on error
     */
    val phoneNumber: Phonenumber.PhoneNumber?
        get() {
            try {
                var iso: String? = null
                if (selectedCountry != null) {
                    iso = selectedCountry!!.iso
                }
                return mPhoneUtil.parse(mPhoneEdit!!.text.toString(), iso)
            } catch (ignored: NumberParseException) {
                return null
            }

        }

    /**
     * Check if number is valid
     *
     * @return boolean
     */
    val isValid: Boolean
        get() {
            val phoneNumber = phoneNumber
            return phoneNumber != null && mPhoneUtil.isValidNumber(phoneNumber)
        }

    /**
     * Returns the error message that was set to be displayed with
     * [.setError], or `null` if no error was set
     * or if it the error was cleared by the widget after user input.
     *
     * @return error message if known, null otherwise
     */
    /**
     * Sets an error message that will be displayed in a popup when the EditText has focus.
     *
     * @param error error message to show
     */
    var error: CharSequence
        get() = mPhoneEdit!!.error
        set(error) {
            mPhoneEdit!!.error = error
        }

    /**
     * Constructors
     */
    constructor(context: Context) : super(context) {
        if(!isInEditMode){
            init(null)
        }
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        if(!isInEditMode){
            init(attrs)
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs,defStyleAttr) {
        if(!isInEditMode){
            init(attrs)
        }
    }
    @TargetApi(21)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        if(!isInEditMode){
            init(attrs)
        }
    }


    /**
     * Init after constructor
     */
    private fun init(attrs: AttributeSet?) {
        inflatter(attrs)
        //get spinner mode

        /**+
         * RegionItem spinner
         */
        mRegionSpinner = findViewById(R.id.asp_phone_widget) as Spinner
        val mode = getViewMode(attrs)
        mRegionSpinnerAdapter = RegionSpinnerAdapter(context, mode)
        mRegionSpinner!!.adapter = mRegionSpinnerAdapter

        mCountries = EnumRegions.getRegions(context)
        mRegionSpinnerAdapter!!.addAll(mCountries!!)
        mRegionSpinner!!.onItemSelectedListener = mRegionSpinnerListener

        setFlagDefaults(attrs)

        /**
         * Phone text field
         */
        mPhoneEdit = findViewById(R.id.edt_phone_widget) as EditText
        mPhoneEdit!!.addTextChangedListener(mPhoneNumberWatcher)

        setDefault()
        setEditTextDefaults(attrs)

    }

    /***********************************************************
     * Business method
     **********************************************************/

    /**
     * get InputLayout
     * @param attrs      AttributeSet
     * @return TextInputLayout
     */
    fun getInputLayout():TextInputLayout{
       return findViewById<TextInputLayout>(R.id.txtil_phone_widget)
    }
    /**
     * get InputEditText
     * @param attrs      AttributeSet
     * @return InputEditText
     */
    fun getTextInputEditText():TextInputEditText{
        return findViewById<TextInputEditText>(R.id.edt_phone_widget)
    }
    /**
     * get view mode : FLAG,ISO,Composit
     * @param attrs      AttributeSet
     * @return mode
     */
    fun getViewMode(attrs: AttributeSet?):Int{
        val a = context.obtainStyledAttributes(attrs, R.styleable.IntlPhoneLayout)
        val mode = a.getInt(R.styleable.IntlPhoneLayout_mode, FLAG_VIEW)
        a.recycle()
        return mode
    }
    /**
    * inflate according to style-khosrov
    * @param attrs      AttributeSet
    * */
    fun inflatter(attrs: AttributeSet?){
        val a = context.obtainStyledAttributes(attrs, R.styleable.IntlPhoneLayout)
        val md =a.getBoolean(R.styleable.IntlPhoneLayout_materialDesign,false);
        val layoutID = if(md) getLayout(attrs) else R.layout.phone_layout
        View.inflate(context, layoutID, this)
        a.recycle()
    }
    /**
    getLayout
    * get layout according to mode
    * @param attrs      AttributeSet
    **/
    private fun getLayout(attrs: AttributeSet?):Int{
        val mode = getViewMode(attrs)
        when(mode){
            FLAG_VIEW,ISO_VIEW->return R.layout.stylish_phone_layout
            COMPOSIT_VIEW->return R.layout.stylish_phone_layout_composit
        }
        return R.layout.stylish_phone_layout
    }

    /**
       setFlagDefaults
     * set defalut attributes
     * @param attrs      AttributeSet
     **/
    private fun setFlagDefaults(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.IntlPhoneLayout)
        val paddingEnd = a.getDimensionPixelSize(R.styleable.IntlPhoneLayout_flagPaddingEnd, resources.getDimensionPixelSize(R.dimen.flag_default_padding_right))
        val paddingStart = a.getDimensionPixelSize(R.styleable.IntlPhoneLayout_flagPaddingStart, resources.getDimensionPixelSize(R.dimen.flag_default_padding))
        val paddingTop = a.getDimensionPixelSize(R.styleable.IntlPhoneLayout_flagPaddingTop, resources.getDimensionPixelSize(R.dimen.flag_default_padding))
        val paddingBottom = a.getDimensionPixelSize(R.styleable.IntlPhoneLayout_flagPaddingBottom, resources.getDimensionPixelSize(R.dimen.flag_default_padding))
        mRegionSpinner!!.setPadding(paddingStart, paddingTop, paddingEnd, paddingBottom)
        a.recycle()
    }

    private fun setEditTextDefaults(attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val a = context.obtainStyledAttributes(attrs, R.styleable.IntlPhoneLayout)
        val textSize = a.getDimensionPixelSize(R.styleable.IntlPhoneLayout_textSize, resources.getDimensionPixelSize(R.dimen.text_size_default))
        mPhoneEdit!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        val color = a.getColor(R.styleable.IntlPhoneLayout_textColor, -1)
        if (color != -1) {
            mPhoneEdit!!.setTextColor(color)
        }
        val hintColor = a.getColor(R.styleable.IntlPhoneLayout_textColorHint, -1)
        if (hintColor != -1) {
            mPhoneEdit!!.setHintTextColor(color)
        }
        a.recycle()
    }

    /**
     * Useful helper to Hide keyboard
     */
    fun hideKeyboard() {
        val inputMethodManager = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(mPhoneEdit!!.windowToken, 0)
    }

    /**
     * Set default value
     * Will try to retrieve phone number from device
     */
    fun setDefault() {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val phone = telephonyManager.line1Number//not works on all devices
            if (phone != null && !phone.isEmpty()) {
                this.number = phone
            } else {
                val iso = telephonyManager.networkCountryIso
                setEmptyDefault(iso)
            }
        } catch (e: SecurityException) {
            setEmptyDefault()
        }

    }

    /**
     * Set default value with
     *
     * @param iso ISO2 of country
     */
    fun setEmptyDefault(isoID: String?) {
        var iso = isoID
        if (iso == null || iso.isEmpty()) {
            iso = DEFAULT_COUNTRY
        }
        val defaultIdx = mCountries!!.indexOfIso(iso!!)
        selectedCountry = mCountries!![defaultIdx]
        mRegionSpinner!!.setSelection(defaultIdx)
    }

    /**
     * Alias for setting empty string of default settings from the device (using locale)
     */
    private fun setEmptyDefault() {
        setEmptyDefault(null)
    }

    /**
     * Set hint number for country
     */
    private fun setHint() {
        if (mPhoneEdit != null && selectedCountry != null && selectedCountry!!.iso != null) {
            val phoneNumber = mPhoneUtil.getExampleNumberForType(selectedCountry!!.iso, PhoneNumberUtil.PhoneNumberType.MOBILE)
            if (phoneNumber != null) {
                mPhoneEdit!!.setHint(mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL))
            }
        }
    }

    /**
     * Phone number watcher
     */
    private inner class PhoneNumberWatcher : PhoneNumberFormattingTextWatcher {
        private var lastValidity: Boolean = false

        constructor() : super() {}

        //TODO solve it! support for android kitkat
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        constructor(countryCode: String?) : super(countryCode) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            super.onTextChanged(s, start, before, count)
            try {
                var iso: String? = null
                if (selectedCountry != null) {
                    iso = selectedCountry!!.iso
                }
                val phoneNumber = mPhoneUtil.parse(s.toString(), iso)
                if(phoneNumber!=null){
                    iso = mPhoneUtil.getRegionCodeForNumber(phoneNumber)
                }

                if (iso != null) {
                    val countryIdx = mCountries!!.indexOfIso(iso)
                    mRegionSpinner!!.setSelection(countryIdx)
                }
            } catch (ignored: NumberParseException) {
            }

            if (mIntlPhoneInputListener != null) {
                val validity = isValid
                if (validity != lastValidity) {
                    mIntlPhoneInputListener!!.done(this@IntlPhoneLayout, validity)
                }
                lastValidity = validity
            }
        }
    }


    fun setOnSelectionListener(listener: ISelectionDelegate) {
        this.mDelaget = listener
    }

    /**
     * Add validation listener
     *
     * @param listener IntlPhoneInputListener
     */
    fun setOnValidityChange(listener: IntlPhoneInputListener) {
        mIntlPhoneInputListener = listener
    }

    /**
     * Sets an error message that will be displayed in a popup when the EditText has focus along
     * with an icon displayed at the right-hand side.
     *
     * @param error error message to show
     */
    fun setError(error: CharSequence, icon: Drawable) {
        mPhoneEdit!!.setError(error, icon)
    }


    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mPhoneEdit!!.isEnabled = enabled
        mRegionSpinner!!.isEnabled = enabled
    }

    /**
     * Set keyboard done listener to detect when the user click "DONE" on his keyboard
     *
     * @param listener IntlPhoneInputListener
     */
    fun setOnKeyboardDone(listener: IntlPhoneInputListener) {
        mPhoneEdit!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener.done(this@IntlPhoneLayout, isValid)
            }
            false
        }
    }

}