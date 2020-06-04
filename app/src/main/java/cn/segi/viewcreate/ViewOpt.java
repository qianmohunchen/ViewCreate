package cn.segi.viewcreate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import adi.example.viewoptannotation.ViewOptHost;

@ViewOptHost
public class ViewOpt {

    private static volatile IViewCreator sIViewCreator;

    static {
        String name = ViewOpt.class.getName();
        String proxyClassName = String.format("%sproxy", name);

        try {
            Class proxyClass = Class.forName(proxyClassName);
            Object instance = proxyClass.newInstance();
            if (instance instanceof IViewCreator) {
                sIViewCreator = (IViewCreator) instance;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static View createView(Context context, String name, AttributeSet attributeSet) {
        if (sIViewCreator != null) {
            View view = sIViewCreator.createView(name, context, attributeSet);
            return view;
        }
        return null;
    }
}
