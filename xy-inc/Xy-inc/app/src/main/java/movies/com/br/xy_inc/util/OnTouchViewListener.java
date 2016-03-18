package movies.com.br.xy_inc.util;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by danilo on 13/03/16.
 */
public class OnTouchViewListener implements View.OnTouchListener {


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof ImageButton) {
            final ImageButton button = (ImageButton) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                button.setColorFilter(Color.parseColor("#FF808080"), PorterDuff.Mode.MULTIPLY);
                button.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                button.clearColorFilter();
                button.invalidate();
            }
        } else if (v instanceof ImageView) {
            final ImageView button = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                button.setColorFilter(Color.parseColor("#FF808080"), PorterDuff.Mode.MULTIPLY);
                button.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                button.clearColorFilter();
                button.invalidate();
            }
        } else {
            final View view = (View) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                view.getBackground().setColorFilter(Color.parseColor("#FF808080"), PorterDuff.Mode.MULTIPLY);
                view.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
                view.getBackground().clearColorFilter();
                view.invalidate();
            }
        }
        return false;
    }
}
