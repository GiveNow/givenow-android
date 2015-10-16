package org.onewarmcoat.onewarmcoat.app.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("DonationCategory")
public class DonationCategory extends ParseObject {

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

    public static ParseQuery<DonationCategory> getTop9() {
        return ParseQuery.getQuery(DonationCategory.class)
                .orderByAscending("priority")
                .setLimit(9);
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

    public double getDonationValue() {
        return getDouble("donationValue");
    }

    public void setDonationValue(double value) {
        put("donationValue", value);
    }

}
