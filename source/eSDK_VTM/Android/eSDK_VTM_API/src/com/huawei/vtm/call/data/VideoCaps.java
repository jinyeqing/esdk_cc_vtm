package com.huawei.vtm.call.data;


public class VideoCaps
{

    public static final int INVALID_VALUE = -1;


    /** 0:保持比例不变,加黑边 **/
    public static final int DISPLAY_TYPE_EDGE = 0;
    /** 1:拉伸裁剪 **/
    public static final int DISPLAY_TYPE_CUT = 1;
    /** 2:不加黑边，不做裁剪 **/
    public static final int DISPLAY_TYPE_NOTHING = 2;

    /** 硬解码默认码率 */
    public static final int DEFAULT_DATARATE_HARDCODEC = 512;

    /** 软解码默认码率 */
    public static final int DEFAULT_DATARATE_SOFTCODEC = 256;

    /** 默认帧率 */
    public static final int DEFAULT_FRAME_RATE = 15;

    /**
     * 可选,会话id。
     * 针对指定会话时需要这个参数，公共参数则不需要这个参数。
     */
    private String sessionId = null;

    /*****************device属性***START***********************/

    /**
     * 可选,本地视频回放是否启动.
     *
     * 0: 不启动;
     * 1: 启动. 默认1
     */
    private int playbackLocalSwitch = 1;

    /**
     * 可选，本地视频回放窗口句柄。
     */
    private int playbackLocal = INVALID_VALUE;

    /**
     * 可选,远端视频回放是否启动.
     *
     * 0: 不启动;
     * 1: 启动. 默认1
     */
    private int playbackRemoteSwitch = 1;

    /**
     * 可选，远端视频回放窗口句柄。
     */
    private int playbackRemote = INVALID_VALUE;

    /**
     * 可选,视频显示模式.
     * 移动: 0:保持比例不变,加黑边 1:拉伸裁剪 
     * 2:不加黑边，不做裁剪 android默认为0 ios默认为1
     */
    private int remoteDisplayType = DISPLAY_TYPE_EDGE;

    /** 本地视频显示模式 */
    private int localDisplayType = DISPLAY_TYPE_EDGE;

    /** 0:不做镜像(默认值) 1:上下镜像 2:左右镜像 */
    private int remoteMirrorType = NONE_MIRROR;

    /** 0:不做镜像(默认值) 1:上下镜像 2:左右镜像 */
    private int localMirrorType = NONE_MIRROR;

    /** 不做镜像(默认值) */
    public static final int NONE_MIRROR = 0;

    /** 上下镜像 */
    public static final int UP_DOWN_MIRROR = 1;

    /** 左右镜像 */
    public static final int LEFT_RIGHT_MIRROR = 2;

    /**
     * 可选，视频显示初始图像。
     *
     * 必须为jpeg图像，且长宽都是8的倍数
     */
    private String localStartImage = null;

    /**
     * 可选，视频显示初始图像。
     *
     * 必须为jpeg图像，且长宽都是8的倍数
     */
    private String remoteStartImage = null;

    /**
     * 可选,视频编解码名称。
     */
    private String name = "H264";

    /**
     * 可选,负载类型值。
     */
    private int pt = 106;

    /**
     * 设为static，在登录时协商一次后，后面不应该再去修改，否则会入不了会
     * 硬编码能力下最大支持512，软编码能力下最大支持256
     */
    // private static int mMaxbw = DEFAULT_DATARATE_SOFTCODEC;

    /**
     * 设为static，在登录时协商一次后，后面不应该再去修改，否则会入不了会
     * 可选，解码器处理的图像格式。
     *1：SQCIF格式；2：QCIF格式；3：CIF格式；4：4CIF格式；5：16CIF格式；
     *6：QQVGA格式；7：QVGA格式；8：VGA格式；9：720P。
     */
    // private static int mDecodeFramesize = 3;

    /**
     * 可选，编码器编码质量。
     */
    private int quality = 15;

    /**
     * 可选，视频显示级别，HP:100 BP:66。默认为BP
     */
    private int profile = 66;

    /**
     * 可选，视频关键帧间隔[1,30]，单位为秒。
     */
    private int keyInterval = 10;

    /**
     * 是否开启硬件加速，默认开启
     */
    private int hdAccelerate = 1;

    /******************codec属性***END**********************/


    /******************trans属性***START**********************/

    /**
     * 必选，ip地址。
     */
    private String ipAddr = null;

    /**
     * 必选，地址类型，IPV4=0，IPV6=1。
     */
    private int addrType = 0;

    /**
     * 必选，视频媒体的本地端口。
     */
    private int localPort = 10580;

//    /**
//     * 可选，fec开关。
//     * 0：关闭fec；1：打开fec。
//     */
//    private int fec = 0;

    /**
     *  可选，Qos(Quality of service)值。
     */
    private int qos = INVALID_VALUE;

    /**
     * 动态适应(包括码率等).可以动态调整一些关键参数
     * 0：关闭ars；1：打开ars。
     */
    private int ars = 1;

    /******************trans属性***END**********************/


    /**
     * 必选，优先选择哪种传输方式，RTP(Real Time Protocol)：1，
     * SRTP(Security Real Time Protocol )：2。
     */
//    private int priorityRtp = 1;

    /**
     * 初始化视频性能参数.
     */
    public VideoCaps()
    {
        orientParams = new OrientParams();
        // codecParams = new CodecParams();
    }

    /*public String getVideoCapsParams()
    {
        StringBuffer strParam = new StringBuffer("<media>\r\n");
        if (!StringUtil.isStringEmpty(sessionId))
        {
            strParam.append("<session-id>").append(sessionId)
                    .append("</session-id>\r\n");
        }
        //device 属性
        strParam.append("<device>\r\n");
        strParam.append("<capturer-index>").append(cameraIndex)
                .append("</capturer-index>\r\n");
        // 摄像头角度旋转 // 2013.9.10修改
        strParam.append("<capturer-rotation>").append(cameraRotation)
                .append("</capturer-rotation>\r\n");
        if (orient != INVALID_VALUE)
        {
            // 横竖屏协商.
            strParam.append("<orient").append(" portrait=")
                    .append(orientPortrait).append(" landscape=")
                    .append(orientLandscape).append(" seascape=")
                    .append(orientSeascape).append(">").append(orient)
                    .append("</orient>\r\n");
        }
        if (playbackLocal != INVALID_VALUE)
        {
            strParam.append("<playback-local-switch>")
                    .append(playbackLocalSwitch)
                    .append("</playback-local-switch>\r\n");

            strParam.append(
                    "<playback-local top=\"0\" left=\"0\" width=\"176\" height=\"144\" displaytype=\"")
                    .append(localDisplayType).append("\" mirrortype=\"")
                    .append(localMirrorType).append("\"");
            if (localStartImage != null)
            {
                strParam.append(" start-img=\"").append(localStartImage)
                        .append("\"");
            }
            strParam.append(">").append(playbackLocal)
                    .append("</playback-local>\r\n");
        }

        if (playbackRemote != INVALID_VALUE)
        {
            strParam.append("<playback-remote-switch>")
                    .append(playbackRemoteSwitch)
                    .append("</playback-remote-switch>\r\n");

            strParam.append(
                    "<playback-remote top=\"0\" left=\"0\" width=\"176\" height=\"144\" displaytype=\"")
                    .append(remoteDisplayType).append("\" mirrortype=\"")
                    .append(remoteMirrorType).append("\"");
            if (remoteStartImage != null)
            {
                strParam.append(" start-img=\"").append(remoteStartImage)
                        .append("\"");
            }
            strParam.append(">").append(playbackRemote)
                    .append("</playback-remote>\r\n");
        }
        strParam.append("</device>\r\n");

        //codec属性
        strParam.append("<codec>\r\n");
        strParam.append("<name>").append(name).append("</name>\r\n");
        strParam.append("<pt>").append(pt).append("</pt>\r\n");
        strParam.append("<datarate>").append(datarate)
                .append("</datarate>\r\n");
        strParam.append("<maxbw>").append(mMaxbw).append("</maxbw>\r\n");
        strParam.append("<framesize>").append(framesize)
                .append("</framesize>\r\n");
        strParam.append("<decode-framesize>").append(mDecodeFramesize)
                .append("</decode-framesize>\r\n");
        strParam.append("<framerate>").append(framerate)
                .append("</framerate>\r\n");
        strParam.append("<quanlity>").append(quality).append("</quanlity>\r\n");
        strParam.append("<keyinterval>").append(keyInterval)
                .append("</keyinterval>\r\n");
        strParam.append("<profile>").append(profile).append("</profile>\r\n");
        strParam.append("<hdAccelerate>").append(hdAccelerate)
                .append("</hdAccelerate>\r\n");
        strParam.append("</codec>\r\n");

        //trans属性
        strParam.append("<trans>\r\n");

        if (!StringUtil.isStringEmpty(ipAddr))
        {
            strParam.append("<addr>").append(ipAddr).append("</addr>\r\n");
            strParam.append("<addr-type>").append(addrType)
                    .append("</addr-type>\r\n");
        }

        strParam.append("<local-port>").append(localPort)
                .append("</local-port>\r\n");

        if (qos != INVALID_VALUE)
        {
            strParam.append("<qos>").append(qos).append("</qos>\r\n");
        }
//        strParam.append("<fec>").append(fec).append("</fec>\r\n");
        strParam.append("<ars>").append(ars).append("</ars>\r\n");
        strParam.append("</trans>\r\n");

        int mode = ContactLogic.getIns().getMyOtherInfo().getSrtpMode();//(0-不加密 1-自动模式 2-强制加密)

        // Fast  1、2、3 可以对应到BMP上的 不加密、加密、不强制加密（自动模式）
        if(mode == 0)
        {
            strParam.append("<rtp-type>").append("1").append("</rtp-type>\r\n");
            strParam.append("<priority-rtp>").append("1").append("</priority-rtp>\r\n");
        }
        else if(mode == 1)
        {
            strParam.append("<rtp-type>").append("3").append("</rtp-type>\r\n");
            strParam.append("<priority-rtp>").append("2").append("</priority-rtp>\r\n");
        }
        else if(mode == 2)
        {
            strParam.append("<rtp-type>").append("2").append("</rtp-type>\r\n");
            strParam.append("<priority-rtp>").append("2").append("</priority-rtp>\r\n");
        }

        strParam.append("</media>");

        String param = strParam.toString();
        Logger.beginDebug(EspaceService.TAG).p("param:" + param).end();
        return param;
    }*/

    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * 可选,会话id。
     * 针对指定会话时需要这个参数，公共参数则不需要这个参数。
     */
    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public int getCameraIndex()
    {
        return orientParams.cameraIndex;
    }

    /**
     * 可选,摄像头索引值,对手机客户端只存在 {0, 1}
     */
    public void setCameraIndex(int cameraIndex)
    {
        orientParams.cameraIndex = cameraIndex;
    }

    /**
     * 可选，设置视频捕获（逆时针旋转）的角度。
     * 仅Android/iOS平台有效。
     * 0：0度；1：90度；2：180度；3：270度；
     * {0,1,2,3}
     */
    /*public void setCameraRotation(int cameraRotation)
    {
        orientParams.cameraRotation = cameraRotation;
    }*/

    public int getPlaybackLocal()
    {
        return playbackLocal;
    }

    /**
     * 可选，本地视频回放窗口句柄。
     */
    public void setPlaybackLocal(int playbackLocal)
    {
        this.playbackLocal = playbackLocal;
    }

    public int getPlaybackRemote()
    {
        return playbackRemote;
    }

    /**
     * 可选，远端视频回放窗口句柄。
     */
    public void setPlaybackRemote(int playbackRemote)
    {
        this.playbackRemote = playbackRemote;
    }

    public int getRemoteDisplayType()
    {
        return remoteDisplayType;
    }

    /**
     * 可选,视频显示模式.
     * 移动: 0:保持比例不变,加黑边 1:拉伸裁剪 
     * 2:不加黑边，不做裁剪 android默认为0 ios默认为1
     */
    public void setRemoteDisplayType(int displayType)
    {
        this.remoteDisplayType = displayType;
    }

    public int getLocalDisplayType()
    {
        return localDisplayType;
    }

    public void setLocalDisplayType(int displayType)
    {
        this.localDisplayType = displayType;
    }

    public int getRemoteMirrorType()
    {
        return remoteMirrorType;
    }

    /** 0:不做镜像(默认值) 1:上下镜像 2:左右镜像 */
    public void setRemoteMirrorType(int remoteMirrorType)
    {
        this.remoteMirrorType = remoteMirrorType;
    }

    public int getLocalMirrorType()
    {
        return localMirrorType;
    }

    /** 0:不做镜像(默认值) 1:上下镜像 2:左右镜像 */
    public void setLocalMirrorType(int localMirrorType)
    {
        this.localMirrorType = localMirrorType;
    }

    public String getName()
    {
        return name;
    }

    /**
     * 可选,视频编解码名称。
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public int getPt()
    {
        return pt;
    }

    /**
     * 可选,负载类型值。
     */
    public void setPt(int pt)
    {
        this.pt = pt;
    }

    /*public int getDatarate()
    {
        return codecParams.datarate;
    }*/

    /**
     * 可选,编解码器目标输出比特率。移动版本默认是256。
     */
    /*public void setDatarate(int datarate)
    {
        codecParams.datarate = datarate;
    }*/

    /*public int getFramesize()
    {
        return codecParams.framesize;
    }*/

    /**
     * 可选，编码器处理的图像格式。
     * 1：SQCIF格式；2：QCIF格式；3：CIF格式；4：4CIF格式；5：16CIF格式；
     * 6：QQVGA格式；7：QVGA格式；8：VGA格式；9：720P。
     *
     */
    /*public void setFramesize(int framesize)
    {
        codecParams.framesize = framesize;
    }*/

    /*public int getDecodeFramesize()
    {
        return mDecodeFramesize;
    }*/

    /**
     * 可选，解码器处理的图像格式。
     *1：SQCIF格式；2：QCIF格式；3：CIF格式；4：4CIF格式；5：16CIF格式；
     *6：QQVGA格式；7：QVGA格式；8：VGA格式；9：720P。
     */
    /*public static void setDecodeFramesize(int decodeFramesize)
    {
        mDecodeFramesize = decodeFramesize;
    }*/

    /*public int getFramerate()
    {
        return codecParams.framerate;
    }*/

    /**
     * 必选，编解码器编码的目标帧率。
     * pc: 根据图像格式和网路情况不同可以设置{5,7,10}
     * 移动：根据图像格式和网路情况不同可以设置{5,7}
     */
    /*public void setFramerate(int framerate)
    {
        codecParams.framerate = framerate;
    }*/

    public int getQuality()
    {
        return quality;
    }

    /**
     * 可选，编码器编码质量。
     */
    public void setQuality(int quality)
    {
        this.quality = quality;
    }

    public int getProfile()
    {
        return profile;
    }

    /**
     * 可选，视频显示级别，HP:100 BP:66。默认为BP
     */
    public void setProfile(int profile)
    {
        this.profile = profile;
    }

    public int getKeyInterval()
    {
        return keyInterval;
    }

    /**
     * 可选，视频关键帧间隔[1,30]，单位为秒。
     */
    public void setKeyInterval(int keyInterval)
    {
        this.keyInterval = keyInterval;
    }

    public String getIpAddr()
    {
        return ipAddr;
    }

    /**
     * 必选，ip地址。
     */
    public void setIpAddr(String ipAddr)
    {
        this.ipAddr = ipAddr;
    }

    public int getAddrType()
    {
        return addrType;
    }

    /**
     * 必选，地址类型，IPV4=0，IPV6=1。
     */
    public void setAddrType(int addrType)
    {
        this.addrType = addrType;
    }

    public int getLocalPort()
    {
        return localPort;
    }

    /**
     * 必选，视频媒体的本地端口。
     */
    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

//    public int getFec()
//    {
//        return fec;
//    }

//    /**
//     * 可选，fec开关。
//     * 0：关闭fec；1：打开fec。
//     */
//    public void setFec(int fec)
//    {
//        this.fec = fec;
//    }

    public int getQos()
    {
        return qos;
    }

    /**
     *  可选，Qos(Quality of service)值。
     */
    public void setQos(int qos)
    {
        this.qos = qos;
    }

    /**
     * 是否开启硬件加速，默认开启
     */
    public void setHdAccelerate(int hdAccelerate)
    {
        this.hdAccelerate = hdAccelerate;
    }

    public int getHdAccelerate()
    {
        return hdAccelerate;
    }

    /**
     * 可选，视频横竖屏情况，仅对对移动平台有效{1,2,3}
     *
     *   1：竖屏；2：横屏；3：反向横屏
     */
    public void setOrient(int orient)
    {
        orientParams.orient = orient;
    }

    /**
     * 可选，竖屏视频捕获（逆时针旋转）角度。仅对移动平台有效。
     *
     *  0：0度；1：90度；2：180度；3：270度；
     */
    public void setOrientPortrait(int orientPortrait)
    {
        orientParams.orientPortrait = orientPortrait;
    }

    /**
     * 可选，横屏视频捕获（逆时针旋转）角度。仅对移动平台有效。
     *
     *  0：0度；1：90度；2：180度；3：270度；
     */
    public void setOrientLandscape(int orientLandscape)
    {
        orientParams.orientLandscape = orientLandscape;
    }

    /**
     * 可选，反向横屏视频捕获（逆时针旋转）角度。仅对移动平台有效。
     *
     *  0：0度；1：90度；2：180度；3：270度；
     */
    public void setOrientSeascape(int orientSeascape)
    {
        orientParams.orientSeascape = orientSeascape;
    }

    public int getPlaybackLocalSwitch()
    {
        return playbackLocalSwitch;
    }

    /**
     * 可选,本地视频回放是否启动.
     *
     * 0: 不启动;
     * 1: 启动. 默认1
     */
    public void setPlaybackLocalSwitch(int playbackLocalSwitch)
    {
        this.playbackLocalSwitch = playbackLocalSwitch;
    }

    public int getPlaybackRemoteSwitch()
    {
        return playbackRemoteSwitch;
    }

    /**
     * 可选,远端视频回放是否启动.
     *
     * 0: 不启动;
     * 1: 启动. 默认1
     */
    public void setPlaybackRemoteSwitch(int playbackRemoteSwitch)
    {
        this.playbackRemoteSwitch = playbackRemoteSwitch;
    }

    /*public int getMaxbw()
    {
        return mMaxbw;
    }*/

    /**
     * 硬编码能力下最大支持512，软编码能力下最大支持256
     */
    /*public static void setMaxbw(int maxbw)
    {
        mMaxbw = maxbw;
    }*/

    public String getRemoteStartImage()
    {
        return remoteStartImage;
    }

    /**
     * 可选，视频显示初始图像。
     *
     * 必须为jpeg图像，且长宽都是8的倍数
     */
    public void setRemoteStartImage(String remoteStartImage)
    {
        this.remoteStartImage = remoteStartImage;
    }

    public String getLocalStartImage()
    {
        return localStartImage;
    }

    /**
     * 可选，视频显示初始图像。
     *
     * 必须为jpeg图像，且长宽都是8的倍数
     */
    public void setLocalStartImage(String localStartImage)
    {
        this.localStartImage = localStartImage;
    }

    public int getArs()
    {
        return ars;
    }

    /**
     * 动态适应(包括码率等).可以动态调整一些关键参数
     * 0：关闭ars；1：打开ars。
     */
    public void setArs(int ars)
    {
        this.ars = ars;
    }

    /**
      * Function: 解析 摄像头的旋转角度
      * @param param
      * @return void
      */
    public  void parserOrientation(String param)
    {
        // this.orientLandscape = XXXX
    }

    private OrientParams orientParams = null;

    public OrientParams getOrientParams()
    {
        return orientParams;
    }

//    public static int checkFrameSize(int size, boolean isHardCodec)
//    {
//        switch (size)
//        {
//            case UCResource.VideoFrameSize.SQCIF: // 1, 3, 5, 7, 9, 2, 4, 6, 8
//            case UCResource.VideoFrameSize.QQVGA:
//            case UCResource.VideoFrameSize.QCIF:
//            case UCResource.VideoFrameSize.QVGA:
//            case UCResource.VideoFrameSize.CIF:
//                return size;
//            case UCResource.VideoFrameSize.VGA:
//                if (!isHardCodec)
//                {
//                    return UCResource.VideoFrameSize.CIF;
//                }
//                else
//                {
//                    return size;
//                }
//            case UCResource.VideoFrameSize._4CIF:
//            case UCResource.VideoFrameSize._16CIF:
//            case UCResource.VideoFrameSize._720P:
//            default:
//                if (isHardCodec)
//                {
//                    return UCResource.VideoFrameSize._4CIF;
//                }
//                else
//                {
//                    return UCResource.VideoFrameSize.CIF;
//                }
//        }
//    }

    public static int checkFrameRate(int frameRate)
    {
        // 范围1-15，默认15
        if (1 > frameRate || DEFAULT_FRAME_RATE < frameRate)
        {
            frameRate = DEFAULT_FRAME_RATE;
        }
        return frameRate;
    }

    public static int checkDataRate(int dataRate, boolean isHardCodec)
    {
        // 硬编码能力范围1-512，默认512
        if (isHardCodec)
        {
            if (1 > dataRate || DEFAULT_DATARATE_HARDCODEC < dataRate)
            {
                dataRate = DEFAULT_DATARATE_HARDCODEC;
            }
        }
        // 软编码能力范围1-256，256
        else
        {
            if (1 > dataRate || DEFAULT_DATARATE_SOFTCODEC < dataRate)
            {
                dataRate = DEFAULT_DATARATE_SOFTCODEC;
            }
        }
        // 正常范围透传
        return dataRate;
    }

    public static class OrientParams
    {
        /** 可选,摄像头索引值,对手机客户端只存在 {0, 1} */
        public int cameraIndex = 1;

        /**
         * 可选，设置视频捕获（逆时针旋转）的角度。
         * 仅Android/iOS平台有效。
         * 0：0度；1：90度；2：180度；3：270度；
         * {0,1,2,3}
         */
        // public int cameraRotation = INVALID_VALUE;

        /**
         * 可选，视频横竖屏情况，仅对对移动平台有效{1,2,3}
         *
         *   1：竖屏；2：横屏；3：反向横屏
         */
         public int orient = INVALID_VALUE;

        /**
         * 可选，竖屏视频捕获（逆时针旋转）角度。仅对移动平台有效。
         *
         *  0：0度；1：90度；2：180度；3：270度；
         */
        public int orientPortrait = 0;

        /**
         * 可选，横屏视频捕获（逆时针旋转）角度。仅对移动平台有效。
         *
         *  0：0度；1：90度；2：180度；3：270度；
         */
        public int orientLandscape = 0;

        /**
         * 可选，反向横屏视频捕获（逆时针旋转）角度。仅对移动平台有效。
         *
         *  0：0度；1：90度；2：180度；3：270度；
         */
        public int orientSeascape = 0;
    }

    // public static class CodecParams
    // {
        /**
         * 可选，编码器处理的图像格式。
         * 1：SQCIF格式；2：QCIF格式；3：CIF格式；4：4CIF格式；5：16CIF格式；
         * 6：QQVGA格式；7：QVGA格式；8：VGA格式；9：720P。
         */
        // public int framesize = 3;

        /**
         * 必选，编解码器编码的目标帧率。
         * pc: 根据图像格式和网路情况不同可以设置{5,7,10}
         * 移动：根据图像格式和网路情况不同可以设置{5,7}
         */
        // public int framerate = 7;

        /** 可选,编解码器目标输出比特率。移动版本默认是256。*/
        // public int datarate = DEFAULT_DATARATE_SOFTCODEC;
    // }


}
