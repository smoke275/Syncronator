package com.syncro.persistence;

import com.syncro.resources.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public class AppProps extends Properties{
    private static final Logger LOGGER = Logger.getLogger(AppProps.class.getName());
    private static AppProps appProps;
    private URL url;
    private AppProps(){
        super();
        //url = AppProps.class.getResource("/dist/properties/editable.properties");
        url = Constants.getResource("editable.properties");
        LOGGER.info("AppLogs Location:: "+url);
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
            LOGGER.info("AppLogs Preserving:: "+url);
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
