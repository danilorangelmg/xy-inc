package movies.com.br.xy_inc.util;

/**
 * Created by danilo on 11/03/16.
 */
import java.util.HashMap;

import android.view.View;

public class ViewHolder {

    private HashMap<String, View> propertys;

    private HashMap<String, Object> propertysObjects;

    public ViewHolder() {
        propertys = new HashMap<String, View>();
        propertysObjects = new HashMap<String, Object>();
    }

    public void setProperty(View view, String propertyName) {
        propertys.put(propertyName, view);
    }

    public View getProperty(String propertyName) {
        return propertys.get(propertyName);
    }

    public void setPropertyObject(Object object, String propertyName) {
        propertysObjects.put(propertyName, object);
    }

    public Object getPropertyObject(String propertyName) {
        return propertysObjects.get(propertyName);
    }
}