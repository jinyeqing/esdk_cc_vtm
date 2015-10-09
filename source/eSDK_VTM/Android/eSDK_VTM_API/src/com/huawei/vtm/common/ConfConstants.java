//package com.huawei.vtm.common;
//
//public class ConfConstants
//{
//
//    /*** 组件ID定义 ***/
//    public static interface COMPONENT_IID
//    {
//        public int IID_COMPONENT_BASE = 0x0000; // 会控ID
//        public int IID_COMPONENT_DS = 0x0001; // 会控ID
//        public int IID_COMPONENT_AS = 0x0002;
//        public int IID_COMPONENT_AUDIO = 0x0004;
//        public int IID_COMPONENT_VIDEO = 0x0008;
//        public int IID_COMPONENT_RECORD = 0x0010;
//        public int IID_COMPONENT_CHAT = 0x0020;
//        public int IID_COMPONENT_POLLING = 0x0040;
//        public int IID_COMPONENT_MS = 0x0080;
//        public int IID_COMPONENT_FT = 0x0100;
//        public int IID_COMPONENT_WB = 0x0200;
//    };
//    
//    /** sharing state **/
//    public interface AS_STATE
//    {
//        public int AS_STATE_NULL = 0x0000;         //停止共享
//        public int AS_STATE_VIEW = 0x0001;         //观看共享
//        public int AS_STATE_START = 0x0002;        //开始共享
//        public int AS_STATE_PAUSE = 0x0003;        //暂停共享
//        public int AS_STATE_PAUSEVIEW = 0x0004;    //暂停观看共享（保留）
//    }
//    
//    /** sharing session **/
//    public interface AS_SESSION
//    {
//        public int AS_SESSION_CONNECT = 0x0000;
//        public int AS_SESSION_OWNER = 0x0001;
//        public int AS_SESSION_FLOWCONTRO = 0x0002;
//    }
//    
//    /** sharing action **/
//    public interface AS_ACTION
//    {
//        public int AS_ACTION_DELETE = 0x0000; // 删除
//
//        public int AS_ACTION_ADD = 0x0001; // 增加
//
//        public int AS_ACTION_MODIFY = 0x0002; // 修改
//
//        public int AS_ACTION_REQUEST = 0x0003; // 请求
//
//        public int AS_ACTION_REJECT = 0x0004; // 拒绝
//
//    }
//
//    public interface AS_PROP
//    {
//        public int AS_PROP_SAMPLING = 0x0014; // 屏幕对外共享分辨率设置
//
//        public int AS_PROP_RANGE = 0x0015; // 屏幕对外共享共享区域设置
//    }
//    
//    /** 远端数据共享类型 **/
//    public interface SHARED_TYPE
//    {
//        public static final int CONF_SHARED_DS = 1; // 文档共享
//
//        public static final int CONF_SHARED_AS = 2; // 屏幕共享
//
////        public static final int CONF_SHARED_WS = 512; // 白板共享
//        
//    }
//
//    public interface AnnotationType
//    {
//        // 类型
//        public static final int ANNOTCUSTOMER_PICTURE = 0; // 图片
//
//        public static final int ANNOTCUSTOMER_MARK = 1;// 标注
//
//        public static final int ANNOTCUSTOMER_POINTER = 2; // 点
//
//        // 标注属性
//        public static final int DS_ANNOT_FLAG_EXCLUSIVE = 0x01; // 排他的，唯一的
//
//        public static final int DS_ANNOT_FLAG_EXCLUSIVEPERUSER = 0x02; // 每个用户唯一的
//
//        public static final int DS_ANNOT_FLAG_OUTLINEFEEDBACK = 0x04;// 创建过程中显示标注虚线框，没有此标志则显示实际标注
//
//        public static final int DS_ANNOT_FLAG_FIXEDSIZE = 0x08;// 固定大小的，不随缩放改变大小
//
//        public static final int DS_ANNOT_FLAG_CANBESELECTED = 0x10; // 可以被选中
//
//        public static final int DS_ANNOT_FLAG_CANBEMOVED = 0x20;// 可以被移动
//
//        public static final int DS_ANNOT_FLAG_PAGEFRAME = 0x40;// 白板边缘的边框（特殊，外部不要用）
//
//        // ******** 标注格式参数 ************************************/
//        public static final int PIC_FORMAT_JPG = 1000;
//
//        public static final int PIC_FORMAT_PNG = 1001;
//
//        public static final int PIC_FORMAT_BMP = 1002;
//
//        // ************ 标注图片索引
//        // public static final int LOCALRES_CHECK = 0;
//        //
//        // public static final int LOCALRES_XCHECK = 1;
//        //
//        // public static final int LOCALRES_LEFTPOINTER = 2;
//        //
//        // public static final int LOCALRES_RIGHTPOINTER = 3;
//        //
//        // public static final int LOCALRES_UPPOINTER = 4;
//        //
//        // public static final int LOCALRES_DOWNPOINTER = 5;
//        //
//        // public static final int LOCALRES_LASERPOINTER = 6;
//
//        public static final int LOCALRES_CHECK = 6;
//
//        public static final int LOCALRES_XCHECK = 5;
//
//        public static final int LOCALRES_LEFTPOINTER = 4;
//
//        public static final int LOCALRES_RIGHTPOINTER = 3;
//
//        public static final int LOCALRES_UPPOINTER = 2;
//
//        public static final int LOCALRES_DOWNPOINTER = 1;
//
//        public static final int LOCALRES_LASERPOINTER = 0;
//    }
//
//}
