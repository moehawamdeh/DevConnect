package org.ieeemadc.devconnect.view.displaypost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import org.ieeemadc.devconnect.model.SerializablePost;

public abstract class PostActivity extends AppCompatActivity {
    abstract void getPassedPost();
    abstract void setupBasicView(SerializablePost post);
    protected void setupActionBar(@NonNull Toolbar toolbar){
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
