package com.tabio.tabioapp.checkin;

import com.tabio.tabioapp.model.Store;

import java.io.Serializable;
import java.util.List;

/**
 * Created by san on 3/30/16.
 */
public class CheckinStores implements Serializable {
    private List<Store> stores;

    public CheckinStores(List<Store> stores) {
        this.stores = stores;
    }

    public List<Store> getStores() {
        return stores;
    }
}
