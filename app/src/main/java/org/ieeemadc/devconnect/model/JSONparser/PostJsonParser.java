package org.ieeemadc.devconnect.model.JSONparser;

import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostJsonParser {
    public Post parse(JSONObject jsonObject){
        if(jsonObject==null)
            return null;
        Post post=new Post();
        post.setTitle(jsonObject.optString("title"));
        post.setPostID(jsonObject.optString("postID"));
        post.setPublisherID(jsonObject.optString("publisherID"));
        post.setPublisher(jsonObject.optString("publisher"));
        post.setPublisherPhotoURL(jsonObject.optString("publisherPhotoURL"));
        post.setBannerURL(jsonObject.optString("bannerURL"));
        post.setVotes(jsonObject.optLong("votes"));
        post.setDescription(jsonObject.optString("description"));
        return post;
    }
    public List<Post> parseResults(JSONObject jsonObject)
    {
        if (jsonObject == null)
            return null;
        List<Post> results = new ArrayList<>();
        JSONArray hits = jsonObject.optJSONArray("hits");
        if (hits == null)
            return null;
        for (int i = 0; i < hits.length(); ++i) {
            JSONObject hit = hits.optJSONObject(i);
            if (hit == null)
                continue;
            Post post = parse(hit);
            if (post == null)
                continue;
            results.add(post);
        }
        return results;
    }
}
