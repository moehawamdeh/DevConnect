package org.ieeemadc.devconnect.model.JSONparser;

import org.ieeemadc.devconnect.model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserJsonParser {
    public User parse(JSONObject jsonObject){
        if(jsonObject==null)
            return null;
        User user=new User();
        user.setId(jsonObject.optString("id"));
        user.setPhotoURL(jsonObject.optString("photoURL"));
        user.setLocation(jsonObject.optString("location"));
        user.setVotes(jsonObject.optLong("votes"));
        user.setName(jsonObject.optString("name"));
        user.setFollowers(jsonObject.optLong("followers"));
        return user;
    }
    public List<User> parseResults(JSONObject jsonObject)
    {
        if (jsonObject == null)
            return null;
        List<User> results = new ArrayList<>();
        JSONArray hits = jsonObject.optJSONArray("hits");
        if (hits == null)
            return null;
        for (int i = 0; i < hits.length(); ++i) {
            JSONObject hit = hits.optJSONObject(i);
            if (hit == null)
                continue;
            User user = parse(hit);
            if (user == null)
                continue;
            results.add(user);
        }
        return results;
    }
}
