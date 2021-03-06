package com.mina.george.newsfeed.ui.homeactivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.mina.george.newsfeed.NewsApplication;
import com.mina.george.newsfeed.R;
import com.mina.george.newsfeed.adapters.PostsAdapter;
import com.mina.george.newsfeed.di.activity.ActivityModule;
import com.mina.george.newsfeed.store.models.article.ArticleModel;
import com.mina.george.newsfeed.ui.base.BaseActivity;

import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public class HomeActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_posts)
    RecyclerView postsRecycler;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.container_host)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.navigationView)
    NavigationView navigationView;
    @BindView(R.id.progress)
    ProgressBar progressBar;

    @Inject
    HomeViewModelFactory homeViewModelFactory;
    @Inject
    PostsAdapter postsAdapter;

    private HomeViewModel homeViewModel;
    private boolean isLoadMoreState;

    @Override
    protected void onCreateActivityComponents() {
        NewsApplication.getComponent(this)
                .plus(new ActivityModule(this)).inject(this);
        intUi();
        homeViewModel = ViewModelProviders.of(this, homeViewModelFactory).get(HomeViewModel.class);
        homeViewModel.getArticlesLiveData().observe(this, this::bindUI);
        homeViewModel.getErrorLiveData().observe(this, this::ProcessError);
        homeViewModel.fetchNews();
        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setOnRefreshListener(homeViewModel::fetchNews);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    private void intUi() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        postsRecycler.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        postsRecycler.setAdapter(postsAdapter);
        postsAdapter.setMoreListener(this::loadMoreNews);
        navigationView.setNavigationItemSelectedListener(item -> {
            Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void ProcessError(String msg) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        if (msg != null)
            showErrorSnackbar(coordinatorLayout, msg);
    }

    private void bindUI(List<ArticleModel> articleModels) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        if (isLoadMoreState) {
            postsAdapter.appendList(articleModels);
        } else {
            postsAdapter.swapData(articleModels);
        }
    }

    private void loadMoreNews() {
        isLoadMoreState = true;
        homeViewModel.moreNews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
