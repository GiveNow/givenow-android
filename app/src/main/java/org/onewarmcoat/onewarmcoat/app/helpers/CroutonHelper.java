package org.onewarmcoat.onewarmcoat.app.helpers;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import org.onewarmcoat.onewarmcoat.app.R;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class CroutonHelper {
    private static final int DURATION_5_SEC = 5000;

    public static Crouton createInfoCrouton(Activity activity, String title, String message) {
        // Inflate a custom view
        View customView = activity.getLayoutInflater().inflate(R.layout.custom_crouton_info, null);
        TextView tvCroutonTitle = (TextView) customView.findViewById(R.id.tvCroutonTitle);
        TextView tvCroutonMsg = (TextView) customView.findViewById(R.id.tvCroutonMsg);

        tvCroutonTitle.setText(title);
        tvCroutonMsg.setText(message);

        // Display the view just by calling "show"
        Configuration config = new Configuration.Builder().setDuration(DURATION_5_SEC).build();

        return Crouton.make(activity, customView, R.id.content_frame, config);
    }
}
