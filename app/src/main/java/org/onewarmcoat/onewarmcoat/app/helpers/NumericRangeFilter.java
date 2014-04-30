package org.onewarmcoat.onewarmcoat.app.helpers;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Numeric range Filter.
 */
public class NumericRangeFilter implements InputFilter {
    /**
     * Maximum value.
     */
    private final double maximum;
    /**
     * Minimum value.
     */
    private final double minimum;

    /**
     * Creates a new filter between 0.00 and 999,999.99.
     */
    public NumericRangeFilter() {
        this(0.00, 999999.99);
    }

    /**
     * Creates a new filter.
     *
     * @param p_min Minimum value.
     * @param p_max Maximum value.
     */
    NumericRangeFilter(double p_min, double p_max) {
        maximum = p_max;
        minimum = p_min;
    }

    @Override
    public CharSequence filter(
            CharSequence p_source, int p_start,
            int p_end, Spanned p_dest, int p_dstart, int p_dend
    ) {
        try {
            String v_valueStr = p_dest.toString().concat(p_source.toString());
            double v_value = Double.parseDouble(v_valueStr);
            if (v_value <= maximum && v_value >= minimum) {
                // Returning null will make the EditText to accept more values.
                return null;
            }
        } catch (NumberFormatException p_ex) {
            // do nothing
        }
        // Value is out of range - return empty string.
        return "";
    }
}