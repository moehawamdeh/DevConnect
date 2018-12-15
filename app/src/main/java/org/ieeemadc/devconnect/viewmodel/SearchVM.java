package org.ieeemadc.devconnect.viewmodel;

import android.app.Application;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.ieeemadc.devconnect.model.JSONparser.PostJsonParser;
import org.ieeemadc.devconnect.model.JSONparser.UserJsonParser;
import org.ieeemadc.devconnect.R;
import org.ieeemadc.devconnect.Utils.DevConnectUtils;
import org.ieeemadc.devconnect.model.FireStoreFetcher;
import org.ieeemadc.devconnect.model.Post;
import org.ieeemadc.devconnect.model.User;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class SearchVM extends AndroidViewModel implements FireStoreFetcher.ApiKeysListener ,CompletionHandler {
    private MutableLiveData<List<Post>>mPosts;
    private MutableLiveData<List<User>>mUsers;
    private MutableLiveData<String>mError;
    private Client mClient;
    private Index mIndex;
    private boolean searchingUsers=false;

    public SearchVM(@NonNull Application application) {
        super(application);
        mPosts=new MutableLiveData<>();
        mUsers=new MutableLiveData<>();
        mError=new MutableLiveData<>();
        FireStoreFetcher fetcher=new FireStoreFetcher();
        fetcher.fetchApiKeys(this);
    }

    public MutableLiveData<List<Post>> getPosts() {
        return mPosts;
    }

    public MutableLiveData<List<User>> getUsers() {
        return mUsers;
    }

    public MutableLiveData<String> getError() {
        return mError;
    }
    @Override
    public void onFetchComplete(String appID, String secretKey, String searchKey) {
        mClient=new Client(appID,searchKey);
    }

    @Override
    public void onFetchFailed(String error) {
        mError.setValue(error);
    }
    public void searchForUser(String name, String location,List<String>keywords){
        if(mClient==null)
        {
            mError.setValue(getApplication().getString(R.string.error_ccured_search));
            return;
        }
        mIndex =mClient.getIndex("dev_users");
        boolean skipName=(name==null || name.isEmpty());
        boolean skipLocation=(location==null||location.isEmpty());
        boolean skipKeyword=(keywords==null||keywords.isEmpty());
        if(skipName&&skipLocation&&skipKeyword)
        {
            mError.setValue(getApplication().getString(R.string.error_search_empty));
            return;
        }
        String term=skipName?"":name;
        term = skipKeyword?term:term+" "+ DevConnectUtils.joinString(", ", keywords);
        term= skipLocation?term:term+" "+location;
        Query query=new Query(term);
        searchingUsers=true;
        mIndex.searchAsync(query,null,this);
    }
    public void searchForPost(String title,String time,List<String>keywords){
        if(mClient==null)
        {
            mError.setValue(getApplication().getString(R.string.error_ccured_search));
            return;
        }
        mIndex =mClient.getIndex("dev_posts");
        boolean skipTitle=(title==null || title.isEmpty());
        boolean skipKeyword=(keywords==null||keywords.isEmpty());
        if(skipTitle&&skipKeyword)
        {
            mError.setValue(getApplication().getString(R.string.error_search_empty));
            return;
        }
        String term=skipTitle?"":title;
        term = skipKeyword?term:term+" "+ DevConnectUtils.joinString(", ", keywords);
        long timeStamp=DevConnectUtils.stringToUnixTimeStamp(getApplication(),time);
        Query query=new Query(term);
        if(timeStamp!=-1)
            query=query.setFilters("mTimeCreated"+timeStamp);
        searchingUsers=false;
        mIndex.searchAsync(query,null,this);
    }

    @Override
    public void requestCompleted(JSONObject jsonObject, AlgoliaException e) {
        if(searchingUsers){
            if(e!=null)
            {
                mUsers.setValue(null);
                mError.setValue(e.getMessage());
            }else{
                List<User>users=new UserJsonParser().parseResults(jsonObject);
                mUsers.setValue(users);
            }
            searchingUsers=false;
        }else{
            if(e!=null)
            {
                mPosts.setValue(null);
                mError.setValue(e.getMessage());
            }else{
                List<Post>posts=new PostJsonParser().parseResults(jsonObject);
                mPosts.setValue(posts);
                if(posts.isEmpty())
                    mError.setValue(getApplication().getString(R.string.no_results));
            }
            searchingUsers=false;
        }

    }
}
