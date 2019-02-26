package com.xiaomo.hotpush;

import com.facebook.react.ReactInstanceManager;
/**
 * Created by xiaomo on 2019/1/18   永无bug
 */

public interface ReactInstanceHolder {
    /**
     * Get the current {@link ReactInstanceManager} instance. May return null.
     */
    ReactInstanceManager getReactInstanceManager();
}