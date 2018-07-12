package com.tabio.tabioapp.topics;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mugen.Mugen;
import com.mugen.MugenCallbacks;
import com.mugen.attachers.BaseAttacher;
import com.tabio.tabioapp.AppController;
import com.tabio.tabioapp.MyMenuBus;
import com.tabio.tabioapp.R;
import com.tabio.tabioapp.api.ApiError;
import com.tabio.tabioapp.api.ApiRequest;
import com.tabio.tabioapp.api.ApiRoute;
import com.tabio.tabioapp.me.MyMenu;
import com.tabio.tabioapp.ui.BaseFragment;
import com.tabio.tabioapp.web.WebActivity;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.tabio.tabioapp.util.LogUtils.LOGD;
import static com.tabio.tabioapp.util.LogUtils.LOGE;
import static com.tabio.tabioapp.util.LogUtils.makeLogTag;

public class TopicsFragment extends BaseFragment implements TopicDataAdapter.OnTopicCallbacks {
    public static final String TAG = makeLogTag(TopicsFragment.class);

    private TopicDataAdapter adapter;

    private MyMenu myMenu;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;

    public TopicsFragment() {
    }

    public static TopicsFragment newInstance(MyMenu myMenu) {
        TopicsFragment fragment = new TopicsFragment();
        Bundle args = new Bundle();
        args.putSerializable("myMenu", myMenu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.myMenu = (MyMenu) getArguments().getSerializable("myMenu");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topics, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.adapter = new TopicDataAdapter(getActivity(), this);
        this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        this.recyclerView.setHasFixedSize(false);
        this.recyclerView.setAdapter(this.adapter);

        search();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void reset() {
        this.adapter.getObjects().clear();
        refresh();
    }

    public void reload(boolean reset) {
        if (reset) {
            reset();
        }
        search();
    }

    public void refresh() {
        if (this.adapter != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                    if (adapter.getObjects().size() < 1) {
                        showNoDataView(R.string.text_topics);
                    } else {
                        hideNoDataView();
                    }
                    if (myMenu != null) {
//                        myMenu.setObjectsCount(adapter.getObjects().size());
//                        MyMenuBus.get().post(myMenu);
                    }
                }
            });
        }
    }

    private void search() {
        Request request = new Request.Builder()
                .addHeader("Content-Type", "text/xml")
                .url(ApiRoute.GET_TOPICS)
                .get()
                .build();
        showNetworkProgress();
        AppController.getInstance().getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGE(TAG, e.getMessage());
                hideProgress();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(false);
                    XmlPullParser xpp = factory.newPullParser();
                    InputStream is = response.body().byteStream();
                    xpp.setInput(is, "UTF-8");
                    int eventType = xpp.getEventType();
                    boolean inItem = false;
                    Topic topic = new Topic();
                    String tag = "";

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                        } else if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("item")) {
                                inItem = true;
                                topic = new Topic();
                            }
                            tag = xpp.getName();
                        } else if (eventType == XmlPullParser.END_TAG) {
                            if (xpp.getName().equals("item")) {
                                inItem = false;
                                adapter.add(topic);
                            }
                        } else if (eventType == XmlPullParser.TEXT) {
                            if (inItem) {
                                if (tag.equals("title")) {
                                    topic.setTitle(xpp.getText());
                                }
                                if (tag.equals("link")) {
                                    topic.setLink(xpp.getText());
                                }
                                if (tag.equals("thumbnail")) {
                                    topic.setThumbnail(xpp.getText());
                                }
                                if (tag.equals("pubDate")) {
                                    topic.setDate(xpp.getText());
                                }
                                tag = "";
                            }
                        }
                        eventType = xpp.next();
                    }

                    hideProgress();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    hideProgress();
                    AppController.getInstance().showApiErrorAlert(getActivity(), ApiError.newUndefinedApiError());
                }

            }
        });
    }

    @Override
    public void onTopicClicked(Topic topic) {
        Intent view = new Intent(getActivity(), WebActivity.class);
        view.putExtra("url", topic.getLink());
        AppController.getInstance().sendGAScreen("トピックス詳細");
        startActivity(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
