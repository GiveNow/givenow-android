package io.givenow.app.models;

import android.content.Context;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Locale;

@ParseClassName("DonationCategory")
public class DonationCategory extends ParseObject {

    private boolean mClickable = true;

    private boolean mSelected = false;

    public DonationCategory() {
        super();
    }

    public DonationCategory(int priority, ParseFile image,
                            String name_en, String description_en,
                            String name_de, String description_de) {
        super();
        setPriority(priority);
        setImage(image);
        setName(name_en, name_de);
        setDescription(description_en, description_de);
    }

    public static ParseQuery<DonationCategory> fetchTop9() {
        return ParseQuery.getQuery(DonationCategory.class)
                .orderByAscending("priority")
                .setLimit(9);
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public boolean isClickable() {
        return mClickable;
    }

    public void setClickable(boolean b) {
        mClickable = b;
    }

    public String getName(Context context) {
        if (context.getResources().getConfiguration().locale.getLanguage()
                .equals(Locale.GERMAN.getLanguage())) {
            return getNameDE();
        } else {
            return getNameEN();
        }
    }

    public String getDescription(Context context) {
        if (context.getResources().getConfiguration().locale.getLanguage()
                .equals(Locale.GERMAN.getLanguage())) {
            return getDescriptionDE();
        } else {
            return getDescriptionEN();
        }
    }

    public String getNameEN() {
        return getString("name_en");
    }

    public String getNameDE() {
        return getString("name_de");
    }

    public void setName(String name_en, String name_de) {
        put("name_en", name_en);
        put("name_de", name_de);
    }

    public String getDescriptionEN() {
        return getString("description_en");
    }

    public String getDescriptionDE() {
        return getString("description_de");
    }

    public void setDescription(String description_en, String description_de) {
        put("description_en", description_en);
        put("description_de", description_de);
    }

    public int getPriority() {
        return getInt("priority");
    }

    public void setPriority(int priority) {
        put("priority", priority);
    }

    public ParseFile getImage() {
        return getParseFile("image");
    }

    public void setImage(ParseFile imageFile) {
        put("image", imageFile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DonationCategory that = (DonationCategory) o;

        return getObjectId().equals(that.getObjectId());
    }

    @Override
    public int hashCode() {
        return getObjectId().hashCode();
    }
}
