package com.example.pantry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

//
//
//
//
//
//

//
//
//
//Dialog not in use. Was needed for max calorie picker
//
//
//

//
//
//
//
//
//
//

//dialog help from https://www.zoftino.com/android-numberpicker-dialog-example
public class NumberPickerDialog extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final NumberPicker numberPicker = new NumberPicker(getActivity());
        int min = 100;//minimum number in dialog screen
        int max = 2000;//maximum number
        final int step = 100;//incrementations of numbers
        String[] values = new String[max / min];
        for (int i = min; i <= max; i += step) {//get 100 to 2000 in 100-sized increments
            values[(i / step) - 1] = String.valueOf(i);
        }
        //Setting
        numberPicker.setMinValue(min / step);//(1)
        numberPicker.setMaxValue(max / step);//(20) min of 1 and max of 20 means number dialog has 20 levels
        numberPicker.setDisplayedValues(values);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Calories");
        builder.setMessage("Choose maximum calories per recipe");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue()*step, numberPicker.getValue());
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //clicking cancel will automatically close dialog without anything here
            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener() {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }
}
