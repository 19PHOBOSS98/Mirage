package net.phoboss.mirage.utility;

import com.google.gson.JsonObject;

public interface Book {
    default Book validateNewBookSettings(JsonObject newSettings) throws Exception {
        return null;
    }
}
