package com.huawei.meeting.func;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.huawei.vtm.common.Constants;
import com.huawei.vtm.utils.LogUtils;

public class ConfXmlParser
{
    public ConfXmlParser()
    {
        try
        {
            _Map = new HashMap<String, String>();
        }
        catch (Exception e)
        {
            LogUtils.e("ConfXmlParser",
                    "ConfXmlParser() | error" + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private Map<String, String> _Map;
    boolean _bSuccess = false;

    public boolean parser(String strXml)
    {
        _Map.clear();
        _bSuccess = false;
        XmlPullParser parser = Xml.newPullParser();

        // 解析
        try
        {
            InputStream is = new ByteArrayInputStream(strXml.getBytes(Constants.CHARSET_UTF_8));
            // auto-detect the encoding from the stream
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            String tag = null;
            String value = null;
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT && !done)
            {
                String name = null;
                switch (eventType)
                {
//                case XmlPullParser.START_DOCUMENT:
//                    ;
//                    break;
                case XmlPullParser.START_TAG:
                    {
                        tag = parser.getName();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    {
                        name = parser.getName();
                        if (name.equalsIgnoreCase("MSG"))
                        {
                            done = true;
                            _bSuccess = true;
                        }
                        else if (!(name.length() == 0)
                                && value != null && !(value.length() == 0)
                                && name.equalsIgnoreCase(tag))
                        {
                            _Map.put(tag, value);
                        }
                        else
                        {
                            _bSuccess = false;
                            done = true;
                        }
                    }
                    break;
                case XmlPullParser.TEXT:
                    {
                        value = parser.getText();
                        if (value.length() == 0)
                        {
                            ;
                        }
                    }
                    break;
                default:
                    break;
                }
                eventType = parser.next();
            }
        }
        catch (Exception e)
        {
            LogUtils.e("ConfXmlParser", "Parser | error" + e.toString());
            return _bSuccess;
        }
        return _bSuccess;
    }

    public String getStringByTag(String tag)
    {
        if (_bSuccess)
            return _Map.get(tag);
        else
            return null;
    }

    public Integer getInterByTag(String tag)
    {
        if (_bSuccess)
        {
            String str = _Map.get(tag);
            return Integer.valueOf(str);
        }
        else
            return null;
    }

    public Double getDoubleByTag(String tag)
    {
        if (_bSuccess)
        {
            String str = _Map.get(tag);
            return Double.valueOf(str);
        }
        else
            return null;
    }
}