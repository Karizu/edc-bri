package id.co.bri.brizzi.customview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by indra on 23/12/15.
 */
public class HelveticaTextView extends TextView {

    public HelveticaTextView(Context context) {
        super(context);
        init();
    }

    public HelveticaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HelveticaTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "HelveticaNeue.ttf");
        setTypeface(tf);
    }
}
