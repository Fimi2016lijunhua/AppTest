/*
package com.hndw.smartlibrary.until;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

*/
/**
 * @author ljh create 2019-3-20
 *//*

public class PreferenceTool {
    private SecuredPreferenceStore preferenceStore;
    private static PreferenceTool tool;

    public PreferenceTool() {
        preferenceStore = SecuredPreferenceStore.getSharedInstance();
    }

    public static PreferenceTool getInstance() {
        if (tool == null) {
            synchronized (PreferenceTool.class) {
                tool = new PreferenceTool();
            }
        }
        return tool;
    }


    public void editString(String key,String value){
        preferenceStore.edit().putString(key,value).commit();
    }

    public String getStringValue(String key){
        return preferenceStore.getString(key,"");
    }

    public void editBoolean(String key,boolean value){
        preferenceStore.edit().putBoolean(key,value).commit();
    }

    public boolean getBooleanValue(String key){
        return preferenceStore.getBoolean(key,false);
    }

    public void editLong(String key, Long value){
        preferenceStore.edit().putLong(key,value).commit();
    }

    public long getLongValue(String key){
        return preferenceStore.getLong(key,0);
    }

    public void editFloat(String key,float value){
        preferenceStore.edit().putFloat(key,value).commit();
    }

    public float getFloatValue(String key){
        return preferenceStore.getFloat(key,0.0f);
    }

    public void editInt(String key,int value){
        preferenceStore.edit().putFloat(key,value).commit();
    }

    public int getIntValue(String key){
        return preferenceStore.getInt(key,0);
    }

    public void eidtLanguage(String value){
        preferenceStore.edit().putString("default_language",value).commit();
    }

    public String getDefaultLanguage(){
        return preferenceStore.getString("default_language","zh");
    }
}
*/
