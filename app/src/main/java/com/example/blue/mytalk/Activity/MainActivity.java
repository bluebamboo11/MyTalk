package com.example.blue.mytalk.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.blue.mytalk.Fragment.ChatFragment;
import com.example.blue.mytalk.Fragment.FriendsFragment;
import com.example.blue.mytalk.Fragment.MailFragment;
import com.example.blue.mytalk.R;
import com.example.blue.mytalk.SignInActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat");
        setupViewPage();
        setupTablayout();
        if (mFirebaseUser == null) {
              startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

    }

    private void setupTablayout() {
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);

    }

    private void setupViewPage() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        getSupportActionBar().setTitle("Chat");
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_black_24dp);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);
                        break;
                    case 1:
                        getSupportActionBar().setTitle("Mail");
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_write_24dp);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_black_24dp);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_write_24dp);
                        break;
                    case 2:
                        getSupportActionBar().setTitle("Friend");
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chat_write_24dp);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_email_write_24dp);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people_black_24dp);
                        break;

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                startActivity(new Intent(this, SignInActivity.class));
                return true;
                  default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChatFragment();
                case 1:
                    return new MailFragment();
                case 2:
                    return new FriendsFragment();
            }

            return new ChatFragment();
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "";
                case 1:
                    return "";
                case 2:
                    return "";
            }
            return null;
        }
    }
}
