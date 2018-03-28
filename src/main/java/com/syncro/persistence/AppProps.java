package com.syncro.persistence;

import com.syncro.views.FileExplorer;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class AppProps extends Properties{
    private static AppProps appProps;
    private URL url;
    private AppProps(){
        super();
        url = AppProps.class.getResource("/properties/editable.properties");
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return super.getProperty(key, defaultValue);
    }

    public void preserve(){
        try {
            appProps.store(new FileWriter(appProps.getUrl().getPath()), "");
        } catch (IOException e) {
        e.printStackTrace();
        }

    }

    public static AppProps getInstance(){
        if(appProps == null) {
            appProps = new AppProps();
            try {
                appProps.load(appProps.getUrl().openStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return appProps;
    }
}
